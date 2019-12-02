---
title: appsuite-history
icon: far fa-circle
tags: Administration, Command Line tools
---

# NAME

appsuite-history - manages an appsuite history

# SYNOPSIS

**appsuiteui-history** [-h|--help]

**appsuiteui-history** [-t *timestamp*] [-a *apps_path*] [-m *manifests_path*] [--history_apps *apps_history_path*] [--history_manifests *manifests_history_path*]

# DESCRIPTION

This CLT checks the apps and manifests folders for new versions and copies them to the designated
history folder. It is also possible to force this by using the --timestamp parameter. This is
necessary in case the touchappsuite clt has been used on frontend nodes. Please use the same
timestamp here as well.

# OPTIONS

**-h**, **--help**
: Prints a help text

**-t**, **--timestamp** *timestamp*
: Updates the version.txt files with this timestamp before doing any other checks.

**-a**, **--apps** *apps_path*
: The optional path to the installed apps.

**-m**, **--manifests** *manifests_path*
: The optional path to the installed manifests.

**--history_apps** *apps_history_path*
: The optional path to the apps history folder.

**--history_manifests** *manifests_history_path*
: The optional path to the manifests history folder.
