TODO

build.xml:
  Reads a ProjectSet file. Then it parses already built bundles from /opt/open-xchange and bundles references in the ProjectSet. This
  information is used to compile the build order. This build order is then executed and the referenced bundles are built to
  /opt/open-xchange or into any distribution package.

build-project.xml:
  Is copied by the build.xml script into each bundle. There it is able to build any type of bundle. It needs the following properties for
  the classpath of the bundle:
  - <bundleIdentifier>.requiredClasspath
  - <bundleIdentifier>.deepClasspath
  The first is necessary to build the bundle, the second is necessary to execute the integrated unit tests.
  And it needs all source folders of the bundle:
  - <bundleIdentifier>.sourceDirs
  This is added to allow several source code folders to easily maintain API incompatible versions.

buildAll.xml:
  Currently reads the packages.txt file and calls then build.xml for every ProjectSet named there. This should be replaced later on by
  reading a global ProjectSet file referencing the ProjectSets to build.
