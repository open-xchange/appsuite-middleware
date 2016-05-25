#!/bin/bash
# Open-Xchange quick installation script
#
# 2016 - now by
#
# Published under the terms and conditions of GPLv2
#
# Please read the configuration part of this script carefully and customize it
# for your needs. OX won't work correctly without proper configuration!
#
# This script requires the an optional companion script to create users, groups
# and ressources after finishing the installation process.

# Let's get the party started
show_usage() {
echo "
Usage:
 bash generate-api.sh [OPTIONS]

OPTIONS:
 -f:  API folder path (default 'http_api')
 -l:  Source language the code should be generated for (default 'java')
 -h:  Show this help screen"
} 

# Default options, can be overwritten by parameters
APIFOLDER="http_api"
LANG="java"

while getopts "f::l::" opts; do
  case ${opts} in
	f) APIFOLDER="${OPTARG}" ;;
	l) LANG="${OPTARG}" ;;
	\?) 
	echo "Invalid option: -$OPTARG" >&2
	show_usage
	exit 1
	;;
  esac
done

SWAGGERJSON="../${APIFOLDER}/http_api-openapi.json"
OUTPUT="${APIFOLDER}/clients/${LANG}"
TEMPLATE="templates/${LANG}"
CONFIG="${APIFOLDER}/configs/${LANG}.json"


echo API FOLDER  = "${APIFOLDER}"
echo LANGUAGE  = "${LANG}"

rm -rf %outputFolder%
java -jar ./lib/swagger-codegen-cli.jar generate -i ${SWAGGERJSON} -l ${LANG} -o ${OUTPUT} -t ${TEMPLATE} -c ${CONFIG}

echo "
Generation complete, thanks for using Open-Xchange. Have a lot of fun!
"
