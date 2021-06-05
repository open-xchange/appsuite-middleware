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

package com.openexchange.common.osgi;

import javax.activation.MailcapCommandMap;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.LoggerFactory;
import com.openexchange.mailcap.OXMailcapCommandMap;


/**
 * {@link CommonActivator} - The activator for common bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CommonActivator implements BundleActivator {

    private ServiceRegistration<MailcapCommandMap> mailcapRegistration;

    /**
     * Initializes a new {@link CommonActivator}.
     */
    public CommonActivator() {
        super();
    }

    @Override
    public synchronized void start(final BundleContext context) throws Exception {
        final org.slf4j.Logger logger = LoggerFactory.getLogger(CommonActivator.class);
        logger.info("Starting bundle: com.openexchange.common");
        try {
            // Add any start-up operations here
            {
                final String mailcap = "" +
                		"#\n" +
                		"#\n" +
                		"# Default mailcap file for the JavaMail System.\n" +
                		"#\n" +
                		"# JavaMail content-handlers:\n" +
                		"#\n" +
                		"text/plain;;        x-java-content-handler=com.sun.mail.handlers.text_plain\n" +
                		"text/html;;     x-java-content-handler=com.sun.mail.handlers.text_html\n" +
                		"text/xml;;      x-java-content-handler=com.sun.mail.handlers.text_xml\n" +
                		"multipart/*;;       x-java-content-handler=com.sun.mail.handlers.multipart_mixed; x-java-fallback-entry=true\n" +
                		"message/rfc822;;    x-java-content-handler=com.sun.mail.handlers.message_rfc822\n" +
                		"#\n" +
                		"# can't support image types because java.awt.Toolkit doesn't work on servers\n" +
                		"#\n" +
                		"#image/gif;;        x-java-content-handler=com.sun.mail.handlers.image_gif\n" +
                		"#image/jpeg;;       x-java-content-handler=com.sun.mail.handlers.image_jpeg\n";
                mailcapRegistration = context.registerService(MailcapCommandMap.class, new OXMailcapCommandMap(mailcap), null);
            }

            String propValue = System.getProperty("networkaddress.cache.ttl", "3600");
            java.security.Security.setProperty("networkaddress.cache.ttl" , propValue);

            propValue = System.getProperty("networkaddress.cache.negative.ttl", "10");
            java.security.Security.setProperty("networkaddress.cache.negative.ttl" , propValue);
        } catch (Exception e) {
            logger.error("Starting bundle ''com.openexchange.common'' failed", e);
            throw e;
        }
    }

    @Override
    public synchronized void stop(final BundleContext context) throws Exception {
        final org.slf4j.Logger logger = LoggerFactory.getLogger(CommonActivator.class);
        logger.info("Stopping bundle: com.openexchange.common");
        try {
            // Add any shut-down operations here
            final ServiceRegistration<MailcapCommandMap> mailcapRegistration = this.mailcapRegistration;
            if (null != mailcapRegistration) {
                mailcapRegistration.unregister();
                this.mailcapRegistration = null;
            }
            com.mysql.jdbc.AbandonedConnectionCleanupThread.uncheckedShutdown();
        } catch (Exception e) {
            logger.error("Stopping bundle 'com.openexchange.common' failed", e);
            throw e;
        }
    }

}
