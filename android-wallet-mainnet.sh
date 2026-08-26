#!/bin/bash
# Fetch mainnet libwallet-jni.so for all ABIs.
#
# Source: BeamMW/beam GitHub Actions "Build" workflow artifacts on master
# (artifact names: libwallet-jni-<abi>-<version>). The old source,
# builds.beam.mw/mainnet/latest, has served stale 2021 (v5.2) libraries for
# years; those crash at runtime with UnsatisfiedLinkError because the app
# calls newer JNI methods.
#
# Requires: gh (authenticated: `gh auth login`), tar.
# Optional: llvm-strip (CI artifacts are unstripped, ~111 MB each).
set -euo pipefail

repo="BeamMW/beam"
jnilibs="./app/src/mainnet/jniLibs"
abis=(arm64-v8a armeabi-v7a x86 x86_64)

if ! command -v gh >/dev/null; then
    echo "error: gh CLI is required (https://cli.github.com), then run: gh auth login" >&2
    exit 1
fi

strip_tool=""
for candidate in llvm-strip /opt/homebrew/opt/llvm/bin/llvm-strip \
    "${ANDROID_NDK_LATEST_HOME:-}/toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-strip"; do
    if command -v "$candidate" >/dev/null; then strip_tool="$candidate"; break; fi
done

run_id=$(gh run list --repo "$repo" --workflow build.yml --branch master \
    --status success --limit 1 --json databaseId --jq '.[0].databaseId')
if [ -z "$run_id" ]; then
    echo "error: no successful master Build run found on $repo" >&2
    exit 1
fi
echo "Using $repo Build run $run_id"

tmpdir=$(mktemp -d)
trap 'rm -rf "$tmpdir"' EXIT

for abi in "${abis[@]}"; do
    name=$(gh api "repos/$repo/actions/runs/$run_id/artifacts" \
        --jq ".artifacts[] | select(.name | startswith(\"libwallet-jni-$abi-\")) | .name" | head -1)
    if [ -z "$name" ]; then
        echo "error: no libwallet-jni-$abi-* artifact in run $run_id" >&2
        exit 1
    fi
    echo "Downloading $name"
    gh run download "$run_id" --repo "$repo" -n "$name" -D "$tmpdir/$abi"
    so=$(find "$tmpdir/$abi" -name libwallet-jni.so | head -1)
    if [ -n "$strip_tool" ]; then
        "$strip_tool" --strip-unneeded "$so"
    else
        echo "warning: llvm-strip not found; installing unstripped library (~111 MB)" >&2
    fi
    mkdir -p "$jnilibs/$abi"
    cp "$so" "$jnilibs/$abi/libwallet-jni.so"
done

# Remove the legacy armeabi/armv8 dirs the old script created: they held copies
# of the 64-bit arm64 library (never loadable on a real armeabi device) and only
# bloated the universal APK by ~2x the largest .so. minSdk 23 hardware is
# ARMv7+, so armeabi-v7a already covers every 32-bit ARM device.
for legacy in armeabi armv8; do
    rm -rf "$jnilibs/$legacy"
done

echo "Done. Installed ABIs: ${abis[*]}"
