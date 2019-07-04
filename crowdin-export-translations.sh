#!/usr/bin/env bash

curl \
  https://api.crowdin.com/api/project/beammw/export?key=$CROWDIN_PROJECT_KEY

if [ "" == "`which wget`" ]; then echo "wget Not Found"; if [ -n "`which apt-get`" ]; then sudo apt-get -y install wget ; elif [ -n "`which yum`" ]; then sudo yum -y install wget ; fi ; fi

archiveName=languages.zip

mkdir temp

wget -O temp/$archiveName https://api.crowdin.com/api/project/beammw/download/all.zip?key=$CROWDIN_PROJECT_KEY && unzip ./temp/$archiveName -d ./temp && rm -rf ./temp/$archiveName

resFolder=$(pwd)/app/src/main/res

cd temp/

for D in *; do
    if [ -d "${D}" ]; then
        locale=$(echo "${D}"| cut -c1-2)
        echo $locale   # your processing here
        localeFolder="$resFolder/values-$locale"
        mkdir $localeFolder
        mv "${D}/strings.xml" $localeFolder
    fi
done

cd ..

rm -rf ./temp

