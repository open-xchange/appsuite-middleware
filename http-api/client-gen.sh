#!/bin/bash
set -e

export DEFINITION_TYPE=http_api # http_api, drive_api, rest_api
export DEFINITION_PACKAGE_NAME=httpclient
export JAR_NAME=httpClient
## Tasks to execute
export BUILD_CLIENT_JAR=true
export REPLACE_TEST_JARS=true
export RECREATE=false
export RESOLVE=true
show_usage() {
echo -n "
  $0 [OPTIONS]

    -h | --help                 Print this help screen
    -d | --definition           The OpenAPI definition type to build (default: 'http_api'; optional: 'drive_api', 'rest_api') 
         --skip-client-build    Skips client compilation after src got created
         --skip-update-libs     If set this will copy the built jars (client and sources) to openexchange-test/lib/
         --skip-resolve         If set the json resolve will be skipped
         --recreate             If set the out directory will be removed prior to client-src generation

"
}

### reading config from command line; overriding defaults
POSITIONAL=()
while [[ $# -gt 0 ]]
do
key="$1"

case $key in
    -h|--help)
    show_usage
    exit 0
    ;;
    --debug)
    set -x
    shift
    ;;
    -d|--definition)
    DEFINITION_TYPE=$2
    shift
    shift
    ;;
    --skip-client-build)
    echo "Skipping client build - only src is created ..."
    BUILD_CLIENT_JAR=false
    shift
    ;;
    --skip-update-libs)
    echo "Replacing built client jar in openexchange-tests folder ..."
    REPLACE_TEST_JARS=false
    shift
    ;;
    --skip-resolve)
    echo "Skipping openAPI.json resolve ..."
    RESOLVE=false
    shift
    ;;
    --recreate)
    echo "Recreating output directory (removing it) ..."
    RECREATE=true
    shift
    ;;
    *)    # unknown option
    POSITIONAL+=("$1") # save it in an array for later
    shift 
    ;;
esac
done
set -- "${POSITIONAL[@]}" # restore positional parameters


if [ "$DEFINITION_TYPE" = "rest_api" ]; then
   DEFINITION_PACKAGE_NAME=restclient
   JAR_NAME=restClient
fi

if [ "$DEFINITION_TYPE" = "drive_api" ]; then
   DEFINITION_PACKAGE_NAME=driveclient
   JAR_NAME=driveClient
fi

export START_FOLDER=$(PWD)
export DEFINITION_FOLDER=$(PWD)/${DEFINITION_TYPE}
export OPENAPI_DEFINITION="${DEFINITION_FOLDER}/openAPI.json"
export CLIENT_CONFIG="$(PWD)/client-gen/config/${DEFINITION_TYPE}.json"
export CLIENT_OUT="$(PWD)/client-gen/out/${DEFINITION_TYPE}"

if ! type "mvn" > /dev/null; then
    echo "Error apache maven is not installed but required! "
    exit 1
fi

if [ "$RESOLVE" = true ]; then
    npm install
    node resolve.js ${DEFINITION_TYPE}

    if [ "$DEFINITION_TYPE" = "http_api" ]; then
        node resolve.js drive_api
    fi
fi

pushd client-gen
if [ ! -f openapi-generator-cli-4.0.0.jar ]; then
    echo "OpenApi generator not yet downloaded ..."
    sleep 2
    curl http://central.maven.org/maven2/org/openapitools/openapi-generator-cli/4.0.0/openapi-generator-cli-4.0.0.jar --output openapi-generator-cli-4.0.0.jar
fi

if [ "$RECREATE" = true ]; then
    rm -fr "$(PWD)/client-gen/out/"
fi

java -jar openapi-generator-cli-4.0.0.jar generate \
        -g java \
        --template-dir templates/templates_${DEFINITION_TYPE} \
        --generate-alias-as-model \
        --minimal-update \
        --library jersey2 \
        --config ${CLIENT_CONFIG} \
        --input-spec ${OPENAPI_DEFINITION} \
        --output ${CLIENT_OUT}

/bin/cp -vf "${PWD}/templates/OAuth_${DEFINITION_PACKAGE_NAME}.java" "${CLIENT_OUT}/src/main/java/com/openexchange/testing/${DEFINITION_PACKAGE_NAME}/invoker/auth/OAuth.java"


    if [ "$DEFINITION_TYPE" = "http_api" ]; then
        java -jar openapi-generator-cli-4.0.0.jar generate \
                -g java \
                --template-dir templates/templates_${DEFINITION_TYPE} \
                --generate-alias-as-model \
                --minimal-update \
                --library jersey2 \
                --config ${CLIENT_CONFIG} \
                --input-spec ${START_FOLDER}/drive_api/openAPI.json \
                --output ${CLIENT_OUT}
    fi

if [ "$BUILD_CLIENT_JAR" = true ]; then
    pushd ${CLIENT_OUT}
    mvn package  -Dmaven.test.skip=true

    if [ "$REPLACE_TEST_JARS" = true ]; then
        /bin/cp -vf ${CLIENT_OUT}/target/${JAR_NAME}-java.jar ../../../../openexchange-test/lib/${JAR_NAME}-java.jar
        /bin/cp -vf ${CLIENT_OUT}/target/${JAR_NAME}-java-sources.jar ../../../../openexchange-test/lib/source/${JAR_NAME}-java-sources.jar
    fi
fi






