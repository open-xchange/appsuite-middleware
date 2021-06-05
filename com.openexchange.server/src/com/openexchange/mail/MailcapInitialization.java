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

package com.openexchange.mail;

import java.util.HashSet;
import java.util.Set;
import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;

/**
 * {@link MailcapInitialization} - Initializes the {@link MailcapCommandMap mailcap}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailcapInitialization {

    private static final MailcapInitialization SINGLETON = new MailcapInitialization();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static MailcapInitialization getInstance() {
        return SINGLETON;
    }

    private boolean initialized;

    /**
     * Initializes a new {@link MailcapInitialization}.
     */
    private MailcapInitialization() {
        super();
    }

    /**
     * Initializes the {@link MailcapCommandMap mailcap}.
     */
    public synchronized void init() {
        if (!initialized) {
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
            initialized = true;
        }
    }
}