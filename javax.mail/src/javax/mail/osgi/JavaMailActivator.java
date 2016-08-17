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

package javax.mail.osgi;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import com.sun.mail.imap.QueuingIMAPStore;
import com.sun.mail.util.MailLogger;


/**
 * {@link JavaMailActivator} - The activator for JavaMail bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JavaMailActivator implements BundleActivator {

    /**
     * Initializes a new {@link JavaMailActivator}.
     */
    public JavaMailActivator() {
        super();
    }

    @Override
    public void start(final BundleContext context) throws Exception {
        try {
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
        } catch (final Exception e) {
            final MailLogger logger = new MailLogger(JavaMailActivator.class, "JavaMail Activator", true, System.out);
            logger.log(Level.SEVERE, "Error starting JavaMail bundle.", e);
            throw e;
        }
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        try {
            QueuingIMAPStore.shutdown();
        } catch (final Exception e) {
            final MailLogger logger = new MailLogger(JavaMailActivator.class, "JavaMail Activator", true, System.out);
            logger.log(Level.SEVERE, "Error stopping JavaMail bundle.", e);
            throw e;
        }
    }


}
