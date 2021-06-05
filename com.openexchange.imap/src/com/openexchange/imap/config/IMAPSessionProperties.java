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

package com.openexchange.imap.config;

import java.util.Map;
import java.util.Properties;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeSessionPropertyNames;
import com.openexchange.systemproperties.SystemPropertiesUtils;

/**
 * {@link IMAPSessionProperties} - Default properties for an IMAP session established via <code>JavaMail</code> API
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPSessionProperties {

    private static volatile Properties imapSessionProperties;

    /**
     * No instantiation
     */
    private IMAPSessionProperties() {
        super();
    }

    /**
     * Creates a <b>cloned</b> version of default IMAP session properties
     *
     * @return a cloned version of default IMAP session properties
     */
    public static Properties getDefaultSessionProperties() {
        Properties tmp = imapSessionProperties;
        if (null == tmp) {
            synchronized (IMAPSessionProperties.class) {
                tmp = imapSessionProperties;
                if (null == tmp) {
                    initializeIMAPProperties();
                    tmp = imapSessionProperties;
                }
            }
        }
        return (Properties) tmp.clone();
    }

    /**
     * Resets the default IMAP session properties
     */
    public static void resetDefaultSessionProperties() {
        Properties tmp = imapSessionProperties;
        if (null != tmp) {
            synchronized (IMAPSessionProperties.class) {
                tmp = imapSessionProperties;
                if (null != tmp) {
                    imapSessionProperties = null;
                }
            }
        }
    }

    /**
     * This method can only be exclusively accessed
     */
    private static void initializeIMAPProperties() {
        /*
         * Define imap session properties
         */
        imapSessionProperties = MimeDefaultSession.getDefaultMailProperties();
        /*
         * Set some global JavaMail properties
         */
        final Properties properties = imapSessionProperties;
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
        /*
         * A connected IMAPStore maintains a pool of IMAP protocol objects for use in communicating with the IMAP server. The IMAPStore will
         * create the initial AUTHENTICATED connection and seed the pool with this connection. As folders are opened and new IMAP protocol
         * objects are needed, the IMAPStore will provide them from the connection pool, or create them if none are available. When a folder
         * is closed, its IMAP protocol object is returned to the connection pool if the pool is not over capacity.
         */
        properties.put(MimeSessionPropertyNames.PROP_MAIL_IMAP_CONNECTIONPOOLSIZE, "1");
        /*
         * A mechanism is provided for timing out idle connection pool IMAP protocol objects. Timed out connections are closed and removed
         * (pruned) from the connection pool.
         */
        properties.put(MimeSessionPropertyNames.PROP_MAIL_IMAP_CONNECTIONPOOLTIMEOUT, "1000");
        if (!properties.containsKey(MimeSessionPropertyNames.PROP_MAIL_MIME_CHARSET)) {
            properties.put(MimeSessionPropertyNames.PROP_MAIL_MIME_CHARSET, MailProperties.getInstance().getDefaultMimeCharset());
            System.getProperties().put(
                MimeSessionPropertyNames.PROP_MAIL_MIME_CHARSET,
                MailProperties.getInstance().getDefaultMimeCharset());
        }
        /*
         * Maximum size of a message to buffer in memory when appending to an IMAP folder. If not set, or set to -1, there is no maximum and
         * all messages are buffered. If set to 0, no messages are buffered. If set to (e.g.) 8192, messages of 8K bytes or less are
         * buffered, larger messages are not buffered. Buffering saves cpu time at the expense of short term memory usage. If you commonly
         * append very large messages to IMAP mailboxes you might want to set this to a moderate value (1M or less).
         */
        properties.put("mail.imap.appendbuffersize", "1048576");
        /*
         * Controls whether the IMAP partial-fetch capability should be used. Defaults to true.
         */
        properties.put("mail.imap.partialfetch", "true");
        /*
         * Partial fetch size in bytes. Defaults to 1MB.
         */
        properties.put("mail.imap.fetchsize", "1048576");
        /*
         * Deny NTLM authentication
         */
        properties.put("mail.imap.auth.ntlm.disable", "true");
        /*
         * Take over system properties related to "mail."
         */
        for (Map.Entry<Object, Object> systemProperty : SystemPropertiesUtils.getSystemProperties().entrySet()) {
            String propName = systemProperty.getKey().toString();
            if (propName.startsWith("mail.")) {
                properties.put(propName, systemProperty.getValue());
            }
        }
        /*
         * Apply configured JavaMail properties from file
         */
        final Properties javaMailProperties = MailProperties.getInstance().getJavaMailProperties();
        if (javaMailProperties != null) {
            /*
             * Overwrite current JavaMail-Specific properties with the ones defined in javamail.properties
             */
            properties.putAll(javaMailProperties);
        }
    }
}
