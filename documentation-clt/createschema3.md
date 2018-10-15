% CREATESCHEMA(1)

# NAME

createschema - creates additional database schemas which can be used for the creation of contexts


# SYNOPSIS

**createschema** [-h|--help]

**createschema** -A *adminuser* -P *adminpass* [-i *database_id*][--responsetimeout *seconds*][--csv]


# DESCRIPTION

This command-line-tool allows to create additional database schemata which can be used
during the creation of contexts.

It's either possible to determine the database where the schema is created yourself or
to let the middleware automatically decide where to create the newly schema.

Please notice that the created schemata will **not** be available for the automatic schema
select strategy. Instead use the "--schema" parameter for context creation.


# OPTIONS

**-h**, **--help**
:   Prints a help text

**--environment**
:   Show info about commandline environment

**--nonl**
:   Remove all newlines (\\n) from output

**-A**, **--adminuser** *user*
:   Master admin user name for authentication

**-P**, **--adminpass** *passwor*
:   Master admin password for authentication

**--responsetimeout**
:   The optional response timeout in seconds when reading data from server (default: 0s; infinite)

**-i**, **--id** *id*
:   An optional database id

**--csv**
:   Format output to csv


# EXAMPLES

**createschema -A masteradmin -P secret --csv**

Creates a new schema in the best suited database and outputs the database id and the schema name in csv format.


**createschema -A masteradmin -P secret -i 3**

Creates a new schema in the database with id 3.
