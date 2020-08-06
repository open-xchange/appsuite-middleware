---
title: Appsuite file history
icon: fa-history
tags: Administration, Update Task, Appsuite
---

# Introduction

Currently some of the fronted code is delivered by the middleware itself in addition to the files provided by the apache web server. In case of an rolling upgrade the situation may arise that a not yet updated frontend requests files from an already updated middleware node. It's possible that these versions are incompatible and this can lead to unpredictable errors in the frontend. This is not a big issue as long as the update process is fast, but if the update process takes longer to finish it becomes a problem. In order to mitigate this problem the middleware now stores a previous version of those frontend files locally and is able to provides the adequate files for both frontend versions during an upgrade scenario. So for example if one performs an upgrade from 7.10.4 to 7.10.5 the updated middleware nodes still know the frontend files for the old version and provide those files to requests from not yet updated frontend nodes.

# Downsides and limitations

This aproach comes with some downsides and limitations. Obviously the local disk usage increases, because the middleware needs to store the files of the previous version. Therefore the package open-xchange-appsuite-backend requires three times more disk space as before. There is an additional copy of the current installed version and also another copy of the previous version. To mitigate the impact of the disk usage the history only supports the most recent previously installed version. This can also be a newer version in case of a downgrade. So in case a frontend version doesn't match the previous one, the middleware will return the files of the currently installed version.

# Command line tool

Normally the middleware takes care of the actuality of the history files during startup, but in certain cases it may be desired to do this without starting up a middleware node. E.g. in case you want to use images. For this scenario a new stand-alone command line tool named appsuiteui-history was developed which prepares the history folders. All you have to do is run

  appsuiteui-history
  
The clt uses decent fallback values for the installed and history folders. 

__Important__:
In case you only upgraded a frontend plugin you __must__ call the clt with the `--timestamp` parameter and use the same value as for the touchappsuite clt. 
