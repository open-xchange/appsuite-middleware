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

#some property doc with -> characters to confuse cli option parsing
confuse.parser=true
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

@test "set property to same value" {
  set +e
  ox_set_property some.existing.property foo $PROPFILE
  set -e
  [[ $(stat -c %Y $PROPFILE) -eq 0 ]]
}

@test "set property to new value" {
  set +e
  ox_set_property some.existing.property bar $PROPFILE
  set -e
  [[ $(stat -c %Y $PROPFILE) -ne 0 ]]
}

@test "contains property value" {
  if contains some.existing.property $PROPFILE; then
    return 0
  else
    return 1
  fi
}

@test "not contains property value" {
  if contains some.notexisting.property $PROPFILE; then
    return 1
  else
    return 0
  fi
}

@test "not confused by dashes" {
  if contains "-> characters" $PROPFILE; then
    return 0
  else
    return 1
  fi
}
