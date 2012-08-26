
To install the disruptor bundle you need to install the following extension/fragment bundle:

com.openexchange.bundles/jars/com.openexchange.system.extension-0.0.1.jar

For equinox you could add sun.misc to one of:

org.osgi.framework.system.packages
org.osgi.framework.system.packages.extra
org.osgi.framework.bootdelegation

to the system properties to export needed classes.

An alternative solution using a fragment can be found in this blog:
http://blog.meschberger.ch/2008/10/osgi-bundles-require-classes-from.html
