#!../bats/bin/bats

source ../../lib/oxfunctions.sh
# oxfunctions sets +e see bug #40756
set -e

PROPFILE=""

setup() {
  PROPFILE=/tmp/$RANDOM.properties
  cat <<EOF > $PROPFILE
#some documentation
some.existing.property=foo
EOF
  touch -d "01 Jan 1970 00:00:00 -0000" $PROPFILE
}

teardown() {
  rm $PROPFILE
}

@test "remove not existing property" {
  ox_remove_property not.existing.property $PROPFILE
  [[ $(stat -c %Y $PROPFILE) -eq 0 ]]
}

@test "remove existing property" {
  ox_remove_property some.existing.property $PROPFILE
  [[ $(stat -c %Y $PROPFILE) -ne 0 ]]
}
