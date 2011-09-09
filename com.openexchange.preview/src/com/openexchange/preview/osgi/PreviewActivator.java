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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.html.HTMLService;
import com.openexchange.java.Streams;
import com.openexchange.preview.PreviewOutput;
import com.openexchange.preview.PreviewService;
import com.openexchange.preview.internal.TikaPreviewService;
import com.openexchange.server.osgiservice.HousekeepingActivator;
import com.openexchange.server.osgiservice.SimpleRegistryListener;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;

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
     * http://java.dzone.com/news/integrate-openoffice-java
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
        final TikaPreviewService tikaPreviewService = new TikaPreviewService(this);
        registerService(PreviewService.class, tikaPreviewService);
        /*
         * Trackers
         */
        track(ManagedFileManagement.class, new SimpleRegistryListener<ManagedFileManagement>() {

            @Override
            public void added(final ServiceReference<ManagedFileManagement> ref, final ManagedFileManagement service) {
                addService(ManagedFileManagement.class, service);
            }

            @Override
            public void removed(final ServiceReference<ManagedFileManagement> ref, final ManagedFileManagement service) {
                removeService(ManagedFileManagement.class);
            }
        });
        track(HTMLService.class, new SimpleRegistryListener<HTMLService>() {

            @Override
            public void added(final ServiceReference<HTMLService> ref, final HTMLService service) {
                addService(HTMLService.class, service);
            }

            @Override
            public void removed(final ServiceReference<HTMLService> ref, final HTMLService service) {
                removeService(HTMLService.class);
            }
        });
        openTrackers();
        /*
         * Possible event handlers
         */
        {
            final EventHandler eventHandler = new EventHandler() {

                @Override
                public void handleEvent(final Event event) {
                    /*-
                     * http://www.artofsolving.com/opensource/jodconverter
                     *
                     *
                     *
                     */
                    final String topic = event.getTopic();
                    if (SessiondEventConstants.TOPIC_ADD_SESSION.equals(topic)) {
                        try {
                            final Session session = (Session) event.getProperty(SessiondEventConstants.PROP_SESSION);

                            final String content =
                                tikaPreviewService.getPreviewFor(
                                    "file:///Users/thorben/git/backend/org.apache.tika/test-documents/testWORD_embeded.doc",
                                    PreviewOutput.HTML,
                                    session).getContent();

                            System.out.println(content);

                            final Writer writer =
                                new OutputStreamWriter(new FileOutputStream("/Users/thorben/Documents/test-tika.html"), "UTF-8");
                            try {
                                writer.write(content);
                                writer.flush();
                            } finally {
                                Streams.close(writer);
                            }
                        } catch (final Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            final Dictionary<String, Object> dict = new Hashtable<String, Object>(1);
            dict.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.getAllTopics());
            // registerService(EventHandler.class, eventHandler, dict);
        }

    }

}
