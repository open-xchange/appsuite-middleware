1. Ensure ProjectSet-Plugin is installed. If not, use the eclipse updater
   with update site: http://vpms.de.csc.com/projectset/update
   
   Moreover ensure you have a proper CVS repository location configured
   through CVS perspective in eclipse.

   Bundle "org.eclipse.equinox.event" must be available at Plug-in Development's
   target platform.
   If not, copy "com.openexchange.common/jars/org.eclipse.equinox.event_1.2.0.v20100503.jar"
   to "${eclipse-installation-dir}/plugins". Reload target platform or restart
   eclipse.

2. Open file "com.openexchange.projectset.servergui.psf" which is opened in
   ProjectSet-View. Press the "Load/Replace all recursively" button. This
   may take a few minutes since all listed repositories are fetched from
   CVS.
   
3. Launch the "open-xchange-development/Open-Xchange Configuration Generator.launch" launcher file
   with ant. Right click on file and select "Run as" and press first entry.
   This launcher generates the needed configuration/property files in
   expected directory.
 
   >>> Refresh (F5) project "open-xchange-development" <<<
   
4. Build AJAX front-end by executing default ant task in build.xml file
   "open-xchange-gui/build.xml".
   
   Enable mod_proxy in your apache and add an appropriate configuration to
   apache's "conf.d" directory.
   See configuration file "com.openexchange.server/doc/examples/proxy_ajp_noncluster.conf".
   
   Next step is to make AJAX front-end accessible. A good way would be to link
   the directory "open-xchange-gui" located in your workspace to system user's
   "public_html" directory:
   'ln -s ${workspace_loc}/open-xchange-gui Open-Xchange-GUI'
   The front-end is then accessible from: "http://localhost/~[username]/Open-Xchange-GUI"
   
   
5. To finally start the Open-Xchange server in OSGi runtime launch the
   "com.openexchange.server/Open-Xchange Server Startup.launch" launcher file. Right click on file
   and select "Run as" and press first entry.
   
TODO: Explain how to import profile files for formatter, clean-up, etc.