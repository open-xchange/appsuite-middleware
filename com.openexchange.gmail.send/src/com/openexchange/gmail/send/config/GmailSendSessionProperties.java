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

package com.openexchange.gmail.send.config;

import java.util.Properties;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeSessionPropertyNames;
import com.openexchange.systemproperties.SystemPropertiesUtils;

/**
 * {@link GmailSendSessionProperties} - Default properties for an Gmail Send session established via <code>JavaMail</code> API
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GmailSendSessionProperties {

    private static volatile Properties sessionProperties;

    /**
     * No instantiation
     */
    private GmailSendSessionProperties() {
        super();
    }

    /**
     * Creates a <b>cloned</b> version of default Gmail Send session properties
     *
     * @return a cloned version of default Gmail Send session properties
     */
    public static Properties getDefaultSessionProperties() {
        Properties tmp = sessionProperties;
        if (null == tmp) {
            synchronized (GmailSendSessionProperties.class) {
                tmp = sessionProperties;
                if (null == tmp) {
                    initializeGmailSendProperties();
                    tmp = sessionProperties;
                }
            }
        }
        return (Properties) tmp.clone();
    }

    /**
     * Resets default Gmail Send session properties
     */
    public static void resetDefaultSessionProperties() {
        if (null != sessionProperties) {
            synchronized (GmailSendSessionProperties.class) {
                if (null != sessionProperties) {
                    sessionProperties = null;
                }
            }
        }
    }

    /**
     * This method can only be exclusively accessed
     */
    private static void initializeGmailSendProperties() {
        /*
         * Define Gmail Send properties
         */
        sessionProperties = MimeDefaultSession.getDefaultMailProperties();
        /*
         * Set some global JavaMail properties
         */
        final Properties properties = sessionProperties;
        properties.putAll(SystemPropertiesUtils.cloneSystemProperties());
        if (!properties.containsKey(MimeSessionPropertyNames.PROP_MAIL_MIME_BASE64_IGNOREERRORS)) {
            properties.put(MimeSessionPropertyNames.PROP_MAIL_MIME_BASE64_IGNOREERRORS, "true");
            System.getProperties().put(MimeSessionPropertyNames.PROP_MAIL_MIME_BASE64_IGNOREERRORS, "true");
        }
        if (!properties.containsKey(MimeSessionPropertyNames.PROP_ALLOWREADONLYSELECT)) {
            properties.put(MimeSessionPropertyNames.PROP_ALLOWREADONLYSELECT, "true");
            System.getProperties().put(MimeSessionPropertyNames.PROP_ALLOWREADONLYSELECT, "true");
        }
        if (!properties.containsKey(MimeSessionPropertyNames.PROP_MAIL_MIME_ENCODEEOL_STRICT)) {
            properties.put(MimeSessionPropertyNames.PROP_MAIL_MIME_ENCODEEOL_STRICT, "true");
            System.getProperties().put(MimeSessionPropertyNames.PROP_MAIL_MIME_ENCODEEOL_STRICT, "true");
        }
        if (!properties.containsKey(MimeSessionPropertyNames.PROP_MAIL_MIME_DECODETEXT_STRICT)) {
            properties.put(MimeSessionPropertyNames.PROP_MAIL_MIME_DECODETEXT_STRICT, "false");
            System.getProperties().put(MimeSessionPropertyNames.PROP_MAIL_MIME_DECODETEXT_STRICT, "false");
        }
        if (!properties.containsKey(MimeSessionPropertyNames.PROP_MAIL_MIME_CHARSET)) {
            properties.put(MimeSessionPropertyNames.PROP_MAIL_MIME_CHARSET, MailProperties.getInstance().getDefaultMimeCharset());
            System.getProperties().put(
                MimeSessionPropertyNames.PROP_MAIL_MIME_CHARSET,
                MailProperties.getInstance().getDefaultMimeCharset());
        }
        if (MailProperties.getInstance().getJavaMailProperties() != null) {
            /*
             * Overwrite current JavaMail-Specific properties with the ones defined in javamail.properties
             */
            properties.putAll(MailProperties.getInstance().getJavaMailProperties());
        }
    }
}
