1. Ensure ProjectSet-Plugin is installed. If not, use the eclipse updater
   with update site: http://vpms.de.csc.com/projectset/update
   
   Moreover ensure you have a proper CVS repository location configured
   through CVS perspective in eclipse.

2. Open file "com.openexchange.projectset.full.psf" which is opened in
   ProjectSet-View. Press the "Load/Replace all..." button. This may take
   a few minutes since all listed repositories are fetched from CVS.
   
3. Launch the "Open-Xchange Mail Filter Generator.launch" launcher file
   with ant. Right click on file and select "Run as" and press first entry.
   This launcher generates the missing source files in mail filter bundle.
   Refresh bundle project "com.openexchange.mail.filter".
   
4. Launch the "Open-Xchange Configuration Generator.launch" launcher file
   with ant. Right click on file and select "Run as" and press first entry.
   This launcher generates the needed configuration/property files in
   expected directory.
   Refresh project "open-xchange-development"
   
5. To finally start the Open-Xchange server in OSGi runtime launch the
   "Open-Xchange Server Startup.launch" launcher file. Right click on file
   and select "Run as" and press first entry.
   
TODO: Explain how to import profile files for formatter, clean-up, etc.