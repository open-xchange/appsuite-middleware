---
title: parsei18nyaml
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-core
---

# NAME

parsei18nyaml - parse i18n yaml

# SYNOPSIS

**parsei18nyaml** [OPTIONS] | [-h]

# DESCRIPTION

This command line tool parses the Open-Xchange i18n YAML

# OPTIONS

**-h**, **--help**
: Prints a help text

**-y**, **--yamlfile**
: The name of the YAML file to parse; either a fully qualified path name or only the name to look it up in default directory /opt/open-xchange/etc"

**-o**, **--output**
: The path name for the .pot output file; e.g. "/tmp/yamlstrings.pot"

**-f**, **--force**
: Whether to force .pot creation. That is to overwrite the denoted path name in case it does already exist.

**-d**, **--date**
: Whether to set the "POT-Creation-Date" in the headers section of the .pot file; e.g. "POT-Creation-Date: 2015-10-18 06:46+0100\\n"