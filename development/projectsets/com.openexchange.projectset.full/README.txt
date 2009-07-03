1. Ensure ProjectSet-Plugin is installed. If not, use the eclipse updater
   with update site: http://vpms.de.csc.com/projectset/update
   
   Moreover ensure you have a proper CVS repository location configured
   through CVS perspective in eclipse.

2. Open file "com.openexchange.projectset.full.psf" which is opened in
   ProjectSet-View. Press the "Load/Replace all recursively" button. This
   may take a few minutes since all listed repositories are fetched from
   CVS.
   
3. Launch the "com.openexchange.mail.filter/Open-Xchange Mail Filter Generator.launch" launcher file
   with ant. Right click on file and select "Run as" and press first entry.
   This launcher generates the missing source files in mail filter bundle.

   >>> Refresh (F5) bundle project "com.openexchange.mail.filter" <<<
   
4. Launch the "open-xchange-development/Open-Xchange Configuration Generator.launch" launcher file
   with ant. Right click on file and select "Run as" and press first entry.
   This launcher generates the needed configuration/property files in
   expected directory.
 
   >>> Refresh (F5) project "open-xchange-development" <<<
   
5. Launch the "openexchange-test-gui/build.xml" launcher file with ant.
   Right click on file and select "Run as" and press first entry.
   The build.xml performs necessary changes in ".classpath" file to resolve
   compile errors.
 
   >>> Refresh (F5) project "openexchange-test-gui" <<<
   
6. To finally start the Open-Xchange server in OSGi runtime launch the
   "com.openexchange.server/Open-Xchange Server Startup.launch" launcher file. Right click on file
   and select "Run as" and press first entry.
   
   
7. To build open-xchange-axis2 correctly, please execute "jar" task in the open-xchange-axis2 build.xml
  - then hit F5 to refresh all projects including open-xchange-axis2
   
TODO: Explain how to import profile files for formatter, clean-up, etc.