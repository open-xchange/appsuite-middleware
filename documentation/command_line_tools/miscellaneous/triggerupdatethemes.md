---
title: triggerupdatethemes
icon: far fa-circle
tags: Administration, Command Line tools
package: open-xchange-core
---

# NAME

triggerupdatethemes - Triggers the update-themes.sh

# SYNOPSIS

**reloadconfiguration** OPTIONS

# DESCRIPTION

This command line tool triggers update or generation of UI themes via update-themes.sh, located in `/opt/open-xchange/appsuite/share/update-themes.sh`

# OPTIONS

**-h**, **--help**
: Prints a help text

**-u**
: Trigger update-themes

**-r**
: Remove dirty file that triggers an ui theme rebuild (Not recommended, will cause inconsistent ui)