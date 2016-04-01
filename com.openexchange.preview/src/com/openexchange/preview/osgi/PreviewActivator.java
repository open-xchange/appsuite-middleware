/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.preview.osgi;

import java.awt.Graphics2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.jopendocument.model.OpenDocument;
import org.jopendocument.renderer.ODTRenderer;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.html.HtmlService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.preview.InternalPreviewService;
import com.openexchange.preview.PreviewService;
import com.openexchange.preview.internal.DelegationPreviewService;
import com.openexchange.preview.internal.TikaPreviewService;

/**
 * {@link PreviewActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PreviewActivator extends HousekeepingActivator {

    /*-
     * I managed to converted RTF/Doc files to PDF using OpenOffice, here my steps: 1) Installed OpenOffice 2.4 (I failed to compile in
     * OpenOffice 3 coz they have changed the jar files folder structure) 2) You need these 3 files in your classpath: unoil.jar, juh.jar,
     * ridl.jar You will find them in: "C:\Program Files\OpenOffice.org 2.4\program\classes
     * " (Note: Do NOT copy out these 3 files, you must linked them in the original location, or else you will get runtime error for not able to find the executable) ---------------------------------- try { // get the remote office component context com.sun.star.uno.XComponentContext xContext = com.sun.star.comp.helper.Bootstrap.bootstrap(); // get the remote office service manager com.sun.star.lang.XMultiComponentFactory xMCF = xContext.getServiceManager(); Object oDesktop = xMCF.createInstanceWithContext( "
     * com.sun.star.frame.Desktop
     * ", xContext); com.sun.star.frame.XComponentLoader xCompLoader = (com.sun.star.frame.XComponentLoader) UnoRuntime.queryInterface( com.sun.star.frame.XComponentLoader.class, oDesktop); java.io.File file = new java.io.File(sourceFile); StringBuffer sLoadUrl = new StringBuffer("
     * file:///"); sLoadUrl.append(file.getCanonicalPath().replace(' ', '/')); file = new java.io.File(outputFile); StringBuffer sSaveUrl =
     * new StringBuffer("file:///"); sSaveUrl.append(file.getCanonicalPath().replace(' ', '/')); com.sun.star.beans.PropertyValue[]
     * propertyValue = new com.sun.star.beans.PropertyValue[1]; propertyValue[0] = new com.sun.star.beans.PropertyValue();
     * propertyValue[0].Name = "Hidden"; propertyValue[0].Value = new Boolean(true); Object oDocToStore = xCompLoader.loadComponentFromURL(
     * sLoadUrl.toString(), "_blank", 0, propertyValue ); com.sun.star.frame.XStorable xStorable =
     * (com.sun.star.frame.XStorable)UnoRuntime.queryInterface( com.sun.star.frame.XStorable.class, oDocToStore ); propertyValue = new
     * com.sun.star.beans.PropertyValue[ 2 ]; propertyValue[0] = new com.sun.star.beans.PropertyValue(); propertyValue[0].Name =
     * "Overwrite"; propertyValue[0].Value = new Boolean(true); propertyValue[1] = new com.sun.star.beans.PropertyValue();
     * propertyValue[1].Name = "FilterName"; propertyValue[1].Value = "writer_pdf_Export"; xStorable.storeToURL( sSaveUrl.toString(),
     * propertyValue ); System.out.println("\nDocument \"" + sLoadUrl + "\" saved under \"" + sSaveUrl + "\"\n");
     * com.sun.star.util.XCloseable xCloseable = (com.sun.star.util.XCloseable)
     * UnoRuntime.queryInterface(com.sun.star.util.XCloseable.class, oDocToStore ); if (xCloseable != null ) { xCloseable.close(false); }
     * else { com.sun.star.lang.XComponent xComp = (com.sun.star.lang.XComponent) UnoRuntime.queryInterface(
     * com.sun.star.lang.XComponent.class, oDocToStore ); xComp.dispose(); } System.out.println("document closed!"); System.exit(0);
     * ---------------------------------- Hope that helps, enjoy!
     *
     * http://www.artofsolving.com/opensource/jodconverter
     *
     * http://java.dzone.com/news/integrate-openoffice-java
     *
     * http://www.jopendocument.org/
     * http://www.jopendocument.org/tutorial_pdf.html
     *
     * http://jodconverter.svn.sourceforge.net/viewvc/jodconverter/branches/3.0.x-200801-commons-pool/
     * http://stackoverflow.com/questions/625241/how-can-i-use-openoffice-in-server-mode-as-a-multithreaded-service
     * --> soffice -headless "-accept=socket,host=localhost,port=8100;urp;"
     *
     * http://user.services.openoffice.org/en/forum/viewtopic.php?f=20&t=16567
     * --> OO in OSGi
     *
     * http://shervinasgari.blogspot.com/2008/12/dynamically-generate-odt-and-pdf.html
     *
     * http://oodaemon.sourceforge.net/
     *
     *
     * http://code.google.com/p/java2word/
     *
     * http://www.tutego.de/blog/javainsel/2011/08/microsoft-office-dokumente-in-java-verarbeiten/
     *
     * http://blogs.reucon.com/srt/2007/02/25/using_openoffice_org_from_java_applications.html
     *
     *
     * -----
     * Generating PDF and Previewing it as an Image iText and PDF Renderer
     * http://technology.amis.nl/blog/4174/java-generating-pdf-and-previewing-it-as-an-image-itext-and-pdf-renderer
     * http://jmupdf.sourceforge.net/joomla/
     * -----
     */

    /**
     * Initializes a new {@link PreviewActivator}.
     */
    public PreviewActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        final DelegationPreviewService delegationPreviewService = new DelegationPreviewService(new TikaPreviewService(this));
        registerService(PreviewService.class, delegationPreviewService);
        /*
         * Trackers
         */
        trackService(ManagedFileManagement.class);
        trackService(HtmlService.class);
        track(InternalPreviewService.class, delegationPreviewService);
        openTrackers();
        /*
         * Possible event handlers
         */

//        {
//            final EventHandler eventHandler = new EventHandler() {
//
//                @Override
//                public void handleEvent(final Event event) {
//                    /*-
//                     * http://www.artofsolving.com/opensource/jodconverter
//                     *
//                     *
//                     *
//                     */
//                    final String topic = event.getTopic();
//                    if (SessiondEventConstants.TOPIC_ADD_SESSION.equals(topic)) {
//                        try {
//                            final Session session = (Session) event.getProperty(SessiondEventConstants.PROP_SESSION);
//
//                            final byte[] bytes = ("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
//                            		" <head>\n" +
//                            		"    <meta content=\"text/html; charset=UTF-8\" http-equiv=\"Content-Type\"/>\n" +
//                            		" </head>\n" +
//                            		" <body>\n" +
//                            		"  <p style=\"margin: 0pt;\">\n" +
//                            		"   <span>\n" +
//                            		"    <span></span>\n" +
//                            		"    Hallo,\n" +
//                            		"   </span>\n" +
//                            		"  </p>\n" +
//                            		"  <p style=\"margin: 0pt;\">\n" +
//                            		"   <span>&#160;</span>\n" +
//                            		"  </p>\n" +
//                            		"  <p style=\"margin: 0pt;\">\n" +
//                            		"   <span>minserver <b>sollte</b> eigentlich <i>nur</i> das c.o.server bundle und seine Abh&#228;ngigkeiten enthalten. c.o.subscribe ist nicht ben&#246;tigt f&#252;r c.o.server. Somit sollten wir lieber c.o.subscribe aus minserver entfernen und in servergui oder full unterbringen.</span>\n" +
//                            		"  </p>\n" +
//                            		"  <p style=\"margin: 0pt;\">\n" +
//                            		"   <span>&#160;</span>\n" +
//                            		"  </p>\n" +
//                            		"  <p style=\"margin: 0pt;\">\n" +
//                            		"   <span>Gru&#223;</span>\n" +
//                            		"  </p>\n" +
//                            		"  <p style=\"margin: 0pt;\">\n" +
//                            		"   <span>Marcus&#160;</span>\n" +
//                            		"  </p>\n" +
//                            		"  <p style=\"margin: 0px; \"></p>\n" +
//                            		"  <div style=\"margin: 5px 0px;\">\n" +
//                            		"   <br/>\n" +
//                            		"   Dennis Sieben &#60;dennis.sieben@open-xchange.com&#62; hat am 21. September 2011 um 16:49 geschrieben:\n" +
//                            		"   <br/>\n" +
//                            		"   <br/>\n" +
//                            		"   &#62; Hallo zusammen,\n" +
//                            		"   <br/>\n" +
//                            		"   &#62;\n" +
//                            		"   <br/>\n" +
//                            		"   &#62; ich hatte gerade gesehen, dass das subscribe Bundle was nun ja im minserver\n" +
//                            		"   <br/>\n" +
//                            		"   &#62; ProjectSet liegt noch zwei weitere Bundles braucht um gl&#252;cklich zu sein:\n" +
//                            		"   <br/>\n" +
//                            		"   &#62;\n" +
//                            		"   <br/>\n" +
//                            		"   &#62; com.openexchange.datatypes.genericonf.storage\n" +
//                            		"   <br/>\n" +
//                            		"   &#62; com.openexchange.secret.recovery\n" +
//                            		"   <br/>\n" +
//                            		"   &#62;\n" +
//                            		"   <br/>\n" +
//                            		"   &#62; Angeh&#228;ngter Patch f&#252;gt diese zum minserver hinzu. Kann ich das so &#228;ndern?\n" +
//                            		"   <br/>\n" +
//                            		"   &#62;\n" +
//                            		"   <br/>\n" +
//                            		"   &#62; Gru&#223;,\n" +
//                            		"   <br/>\n" +
//                            		"   &#62;\n" +
//                            		"   <br/>\n" +
//                            		"   &#62;&#160; &#160; &#160; &#160; &#160;D7\n" +
//                            		"   <br/>\n" +
//                            		"   &#62; --\n" +
//                            		"   <br/>\n" +
//                            		"   &#62; Dennis Sieben\n" +
//                            		"   <br/>\n" +
//                            		"   &#62; Professional Services\n" +
//                            		"   <br/>\n" +
//                            		"   &#62; Open-Xchange GmbH\n" +
//                            		"   <br/>\n" +
//                            		"   &#62; Phone&#160; &#160;+49 2761 8385 0\n" +
//                            		"   <br/>\n" +
//                            		"   &#62; Fax&#160; &#160; &#160;+49 911 180 1419\n" +
//                            		"   <br/>\n" +
//                            		"   &#62; -------------------------------------------------------------------------------\n" +
//                            		"   <br/>\n" +
//                            		"   &#62; Open-Xchange AG,&#160; Maxfeldstr. 9, 90409 N&#252;rnberg, Amtsgericht N&#252;rnberg HRB\n" +
//                            		"   <br/>\n" +
//                            		"   &#62; 24738\n" +
//                            		"   <br/>\n" +
//                            		"   &#62; Vorstand: Rafael Laguna de la Vera, Aufsichtsratsvorsitzender: Richard Seibt\n" +
//                            		"   <br/>\n" +
//                            		"   &#62;\n" +
//                            		"   <br/>\n" +
//                            		"   &#62; European Office: Open-Xchange GmbH, Martinstr. 41, D-57462 Olpe, Germany\n" +
//                            		"   <br/>\n" +
//                            		"   &#62; Amtsgericht Siegen, HRB 8718, Gesch&#228;ftsf&#252;hrer: Frank Hoberg, Martin Kauss\n" +
//                            		"   <br/>\n" +
//                            		"   &#62;\n" +
//                            		"   <br/>\n" +
//                            		"   &#62; US Office: OX Software GmbH, 303 South Broadway, Tarrytown, New York 10591\n" +
//                            		"   <br/>\n" +
//                            		"   &#62; -------------------------------------------------------------------------------\n" +
//                            		"  </div>\n" +
//                            		" </body>\n" +
//                            		"</html>").replaceAll("(\r?\n)+", "").replaceAll("(  )+", "").getBytes();
//
//
//                            final String content =
//                                tikaPreviewService.getPreviewFor(
//                                    Streams.newByteArrayInputStream(bytes),
//                                    PreviewOutput.TEXT,
//                                    session).getContent();
//
//                            System.out.println(content);
//
//                            final Writer writer =
//                                new OutputStreamWriter(new FileOutputStream("/Users/thorben/Documents/test-tika.html"), "UTF-8");
//                            try {
//                                writer.write(content);
//                                writer.flush();
//                            } finally {
//                                Streams.close(writer);
//                            }
//                        } catch (final Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            };
//            final Dictionary<String, Object> dict = new Hashtable<String, Object>(1);
//            dict.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.getAllTopics());
//            // registerService(EventHandler.class, eventHandler, dict);
//        }

        /*
         * PDF generation
         */
        // pdfWithOpenDocument();
    }

    private void pdfWithJodConverter() {
        // http://shervinasgari.blogspot.com/2010/08/migrating-from-jodconverter-2-to.html
        final OfficeManager om = new DefaultOfficeManagerConfiguration()
        .setOfficeHome("/usr/lib/openoffice")
        .setPortNumbers(8100, 8101, 8102, 8103)
        .buildOfficeManager();

        final OfficeManager officeManager = new DefaultOfficeManagerConfiguration().buildOfficeManager();
        officeManager.start();

        final OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
        converter.convert(new File("/Users/thorben/git/backend/org.apache.tika/test-documents/testWORD_embeded.doc"), new File("/Users/thorben/git/backend/org.apache.tika/test-documents/mytest.pdf"));

        officeManager.stop();
    }

    private void pdfWithOpenDocument() throws FileNotFoundException, DocumentException {
        // Load the ODS file
        final OpenDocument doc = new OpenDocument();
        doc.loadFrom("/Users/thorben/git/backend/org.apache.tika/test-documents/testOpenOffice2.odt");

        // Open the PDF document
        final Document document = new Document(PageSize.A4);
        final File outFile = new File("/Users/thorben/git/backend/org.apache.tika/test-documents/mytest.pdf");

        final PdfDocument pdf = new PdfDocument();

        document.addDocListener(pdf);

        final FileOutputStream fileOutputStream = new FileOutputStream(outFile);
        final PdfWriter writer = PdfWriter.getInstance(pdf, fileOutputStream);
        pdf.addWriter(writer);

        document.open();

        // Create a template and a Graphics2D object
        final Rectangle pageSize = document.getPageSize();
        final int w = (int) (pageSize.getWidth() * 0.9);
        final int h = (int) (pageSize.getHeight() * 0.95);
        final PdfContentByte cb = writer.getDirectContent();
        final PdfTemplate tp = cb.createTemplate(w, h);

        final Graphics2D g2 = tp.createPrinterGraphics(w, h, null);
        // If you want to prevent copy/paste, you can use
        // g2 = tp.createGraphicsShapes(w, h, true, 0.9f);

        tp.setWidth(w);
        tp.setHeight(h);

        // Configure the renderer
        final ODTRenderer renderer = new ODTRenderer(doc);
        renderer.setIgnoreMargins(true);
        renderer.setPaintMaxResolution(true);

        // Scale the renderer to fit width
        renderer.setResizeFactor(renderer.getPrintWidth() / w);
        // Render
        renderer.paintComponent(g2);
        g2.dispose();

        // Add our spreadsheet in the middle of the page
        final float offsetX = (pageSize.getWidth() - w) / 2;
        final float offsetY = (pageSize.getHeight() - h) / 2;
        cb.addTemplate(tp, offsetX, offsetY);
        // Close the PDF document
        document.close();
    }

}
