![alt text](https://forum.beam-mw.com/uploads/beam_mw/original/1X/261e2a2eba2b6c8aadae678673f9e8e09a78f5cf.png "Beam Logo")

[twitter](https://twitter.com/beamprivacy) | [medium](https://medium.com/beam-mw) | [reddit](https://www.reddit.com/r/beamprivacy/) | [beam forum](http://forum.beam-mw.com) | [gitter](https://gitter.im/beamprivacy/Lobby) | [telegram](https://t.me/BeamPrivacy) | [bitcointalk](https://bitcointalk.org/index.php?topic=5052151.0) | [youtube](https://www.youtube.com/channel/UCddqBnfSPWibf4f8OnEJm_w?)

Beam wallet app for Android allows you to confidentially exchange funds anywhere you are.

Read documentation [here](https://documentation.beam.mw).

Things that make BEAM special include:

* Users have complete control over their privacy - a user decides which information will be available and to which parties, having complete control over their personal data in accordance to their will and applicable laws.
* Confidentiality without penalty - in BEAM confidential transactions do not cause bloating of the blockchain, avoiding excessive computational overhead or penalty on performance or scalability while completely concealing the transaction value.
* No trusted setup required.
* Blocks are mined using Equihash Proof-of-Work algorithm.
* Limited emission using periodic halving.
* No addresses are stored in the blockchain - no information whatsoever about either the sender or the receiver of a transaction is stored in the blockchain.
* Superior scalability through compact blockchain size - using the “cut-through” feature of Mimblewimble makes the BEAM blockchain orders of magnitude smaller than any other blockchain implementation.
* BEAM supports many transaction types such as escrow transactions, time locked transactions, atomic swaps and more.


# Roadmap
- February-March 2019    : Testnet Betas
- March 2019       : Mainnet release

# Current status
Mainnet is released

# Known limitations and workarounds:
- When sending beams from mobile to desktop wallet, the QR code scanning works from desktop wallet version 2.0
- The app will always choose a random node to connect to; connecting to specific node will be added later.
- Restore flow (which requires integrated or dedicated node) is currently implemented on desktop only, hence to restore mobile funds use desktop wallet with the same seed the mobile wallet was created with.
- In case the user has both mobile and a desktop wallets which are using the same seed phrase, the funds sent to the desktop wallet won’t appear on the mobile wallet. If the desktop wallet is running a local node, it will see funds sent to both wallets. The reason is that the local node (integrated into desktop wallet) always monitors the blockchain for UTXOs related to the seed of the wallet. The mobile wallet does not run a local node and thus can only monitor transactions sent to its specific SBBS addresses. Of course, no funds will be lost in any event.
- If multiple wallets are restored from the same seed phrase, transaction history and addresses will not be shared among the wallets.


# How to build

# Build status

# Support
Android Beam Wallet is currently in Mainnet In case you encounter any problem, please open a GitHub ticket at https://github.com/BeamMW/android-wallet/issues, or email us at testnet@beam.mw, or communicate via [Telegram](https://t.me/joinchat/DNuv_REgViDpHhj3U7ylxA)
