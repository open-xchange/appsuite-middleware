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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.common.osgi;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import javax.activation.MailcapCommandMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.mailcap.OXMailcapCommandMap;


/**
 * {@link CommonActivator} - The activator for common bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CommonActivator implements BundleActivator {

    private volatile ServiceRegistration<MailcapCommandMap> mailcapRegistration;

    /**
     * Initializes a new {@link CommonActivator}.
     */
    public CommonActivator() {
        super();
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        final Log logger = LogFactory.getLog(CommonActivator.class);
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
                		"#image/jpeg;;       x-java-content-handler=com.sun.mail.handlers.image_jpeg\n" +
                		"";
                mailcapRegistration = context.registerService(MailcapCommandMap.class, new OXMailcapCommandMap(mailcap), null);

                String userHome = System.getProperty("user.home");
                if (userHome != null) {
                    final class Closer {
                        void close(final Closeable toClose) {
                            if (null != toClose) {
                                try {
                                    toClose.close();
                                } catch (final Exception e) {
                                    // Ignore
                                }
                            }
                        }
                    }

                    final Closer closer = new Closer();
                    final String path = userHome + File.separator + ".mailcap";
                    final File userMailcap = new File(path);

                    final Map<String, String> caps = new HashMap<String, String>(6);
                    caps.put("text/plain", "text/plain;;        x-java-content-handler=com.sun.mail.handlers.text_plain");
                    caps.put("text/html", "text/html;;        x-java-content-handler=com.sun.mail.handlers.text_html");
                    caps.put("text/xml", "text/xml;;        x-java-content-handler=com.sun.mail.handlers.text_xml");
                    caps.put("multipart/*", "multipart/*;;       x-java-content-handler=com.sun.mail.handlers.multipart_mixed; x-java-fallback-entry=true");
                    caps.put("message/rfc822", "message/rfc822;;        x-java-content-handler=com.sun.mail.handlers.message_rfc822");

                    if (userMailcap.exists()) {
                        BufferedReader reader = null;
                        try {
                            reader = new BufferedReader(new FileReader(userMailcap));
                            for (String line; (line = reader.readLine()) != null;) {
                                line = line.trim();
                                if (line.length() > 0 && !line.startsWith("#")) {
                                    final int pos = line.indexOf(';');
                                    if (pos > 0) {
                                        caps.remove(line.substring(0, pos).trim());
                                    }
                                }
                            }
                        } finally {
                            closer.close(reader);
                        }
                    }

                    if (!caps.isEmpty()) {
                        Writer writer = null;
                        try {
                            writer = new FileWriter(userMailcap, true);
                            final String lineSep = System.getProperty("line.separator");
                            writer.write(lineSep);
                            for (final String cap : caps.values()) {
                                writer.write(cap + lineSep);
                            }
                            writer.write(lineSep);
                            writer.flush();
                        } finally {
                            closer.close(writer);
                        }
                    }
                }
            }
        } catch (final Exception e) {
            logger.info("Starting bundle 'com.openexchange.common' failed: " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        final Log logger = LogFactory.getLog(CommonActivator.class);
        logger.info("Stopping bundle: com.openexchange.common");
        try {
            // Add any shut-down operations here
            final ServiceRegistration<MailcapCommandMap> mailcapRegistration = this.mailcapRegistration;
            if (null != mailcapRegistration) {
                mailcapRegistration.unregister();
                this.mailcapRegistration = null;
            }
            com.mysql.jdbc.AbandonedConnectionCleanupThread.shutdown();
        } catch (final Exception e) {
            logger.info("Stopping bundle 'com.openexchange.common' failed: " + e.getMessage(), e);
            throw e;
        }
    }

}
