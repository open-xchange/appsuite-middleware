/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package javax.mail.osgi;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Session;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.ProtocolListener;
import com.sun.mail.imap.QueuingIMAPStore;
import com.sun.mail.util.MailLogger;


/**
 * {@link JavaMailActivator} - The activator for JavaMail bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JavaMailActivator implements BundleActivator {

    private ServiceTracker<ProtocolListener, ProtocolListener> protocolListenerTracker;

    /**
     * Initializes a new {@link JavaMailActivator}.
     */
    public JavaMailActivator() {
        super();
    }

    @Override
    public synchronized void start(final BundleContext context) throws Exception {
        try {
            Session.setActiveBundle(context.getBundle());
            /*-
             * Add handlers for mail MIME types
             *
                #
                #
                # Default mailcap file for the JavaMail System.
                #
                # JavaMail content-handlers:
                #
                text/plain;;            x-java-content-handler=com.sun.mail.handlers.text_plain
                text/html;;             x-java-content-handler=com.sun.mail.handlers.text_html
                text/xml;;              x-java-content-handler=com.sun.mail.handlers.text_xml
                multipart/*;;           x-java-content-handler=com.sun.mail.handlers.multipart_mixed; x-java-fallback-entry=true
                message/rfc822;;        x-java-content-handler=com.sun.mail.handlers.message_rfc822
                #
                # can't support image types because java.awt.Toolkit doesn't work on servers
                #
                #image/gif;;            x-java-content-handler=com.sun.mail.handlers.image_gif
                #image/jpeg;;           x-java-content-handler=com.sun.mail.handlers.image_jpeg
             */
            final MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
            final Set<String> types = new HashSet<String>(java.util.Arrays.asList(mc.getMimeTypes()));
            if (!types.contains("text/html")) {
                mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
            }
            if (!types.contains("text/xml")) {
                mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
            }
            if (!types.contains("text/plain")) {
                mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
            }
            if (!types.contains("multipart/*")) {
                mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed; x-java-fallback-entry=true");
            }
            if (!types.contains("message/rfc822")) {
                mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
            }
            CommandMap.setDefaultCommandMap(mc);

            ServiceTracker<ProtocolListener, ProtocolListener> protocolListenerTracker = new ServiceTracker<>(context, ProtocolListener.class, new ServiceTrackerCustomizer<ProtocolListener, ProtocolListener>() {

                @Override
                public void removedService(ServiceReference<ProtocolListener> reference, ProtocolListener protocolListener) {
                    IMAPStore.removeProtocolListener(protocolListener);
                    context.ungetService(reference);
                }

                @Override
                public void modifiedService(ServiceReference<ProtocolListener> reference, ProtocolListener protocolListener) {
                    // Ignore
                }

                @Override
                public ProtocolListener addingService(ServiceReference<ProtocolListener> reference) {
                    ProtocolListener protocolListener = context.getService(reference);
                    IMAPStore.addProtocolListener(protocolListener);
                    return protocolListener;
                }
            });
            this.protocolListenerTracker = protocolListenerTracker;
            protocolListenerTracker.open();
        } catch (Exception e) {
            final MailLogger logger = new MailLogger(JavaMailActivator.class, "JavaMail Activator", true, System.out);
            logger.log(Level.SEVERE, "Error starting JavaMail bundle.", e);
            throw e;
        }
    }

    @Override
    public synchronized void stop(final BundleContext context) throws Exception {
        try {
            QueuingIMAPStore.shutdown();
            ServiceTracker<ProtocolListener, ProtocolListener> protocolListenerTracker = this.protocolListenerTracker;
            if (null != protocolListenerTracker) {
                this.protocolListenerTracker = null;
                protocolListenerTracker.close();
            }
            Session.setActiveBundle(null);
        } catch (Exception e) {
            final MailLogger logger = new MailLogger(JavaMailActivator.class, "JavaMail Activator", true, System.out);
            logger.log(Level.SEVERE, "Error stopping JavaMail bundle.", e);
            throw e;
        }
    }


}
