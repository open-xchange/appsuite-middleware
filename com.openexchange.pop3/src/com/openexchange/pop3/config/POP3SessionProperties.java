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

package com.openexchange.pop3.config;

import java.util.Map;
import java.util.Properties;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeSessionPropertyNames;
import com.openexchange.systemproperties.SystemPropertiesUtils;

/**
 * {@link POP3SessionProperties} - Default properties for an POP3 session established via <code>JavaMail</code> API
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3SessionProperties {

    private static volatile Properties pop3SessionProperties;

    /**
     * No instantiation
     */
    private POP3SessionProperties() {
        super();
    }

    /**
     * Creates a <b>cloned</b> version of default POP3 session properties
     *
     * @return a cloned version of default POP3 session properties
     */
    public static Properties getDefaultSessionProperties() {
        Properties tmp = pop3SessionProperties;
        if (null == tmp) {
            synchronized (POP3SessionProperties.class) {
                tmp = pop3SessionProperties;
                if (null == tmp) {
                    initializePOP3Properties();
                    tmp = pop3SessionProperties;
                }
            }
        }
        return (Properties) tmp.clone();
    }

    /**
     * Resets the default POP3 session properties
     */
    public static void resetDefaultSessionProperties() {
        if (pop3SessionProperties != null) {
            synchronized (POP3SessionProperties.class) {
                if (null != pop3SessionProperties) {
                    pop3SessionProperties = null;
                }
            }
        }
    }

    /**
     * This method can only be exclusively accessed
     */
    private static void initializePOP3Properties() {
        /*
         * Define POP3 session properties
         */
        pop3SessionProperties = MimeDefaultSession.getDefaultMailProperties();
        /*
         * Set some global JavaMail properties
         */
        final Properties properties = pop3SessionProperties;
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
         * A connected POP3Store maintains a pool of POP3 protocol objects for use in communicating with the POP3 server. The POP3Store will
         * create the initial AUTHENTICATED connection and seed the pool with this connection. As folders are opened and new POP3 protocol
         * objects are needed, the POP3Store will provide them from the connection pool, or create them if none are available. When a folder
         * is closed, its POP3 protocol object is returned to the connection pool if the pool is not over capacity.
         */
        properties.put("mail.pop3.connectionpoolsize", "1");
        /*
         * A mechanism is provided for timing out idle connection pool POP3 protocol objects. Timed out connections are closed and removed
         * (pruned) from the connection pool.
         */
        properties.put("mail.pop3.connectionpooltimeout", "1000");
        if (!properties.containsKey(MimeSessionPropertyNames.PROP_MAIL_MIME_CHARSET)) {
            properties.put(MimeSessionPropertyNames.PROP_MAIL_MIME_CHARSET, MailProperties.getInstance().getDefaultMimeCharset());
            System.getProperties().put(
                MimeSessionPropertyNames.PROP_MAIL_MIME_CHARSET,
                MailProperties.getInstance().getDefaultMimeCharset());
        }
        if (POP3Properties.getInstance().getPOP3Timeout() > 0) {
            properties.put("mail.pop3.timeout", String.valueOf(POP3Properties.getInstance().getPOP3Timeout()));
        }
        if (POP3Properties.getInstance().getPOP3ConnectionTimeout() > 0) {
            properties.put(
                "mail.pop3.connectiontimeout",
                String.valueOf(POP3Properties.getInstance().getPOP3ConnectionTimeout()));
        }
        /*
         * RSET before quit means any messages, that have been marked as deleted by the POP3 server, are unmarked.
         */
        properties.put("mail.pop3.rsetbeforequit", "true");
        /*
         * Deny NTLM authentication
         */
        properties.put("mail.pop3.auth.ntlm.disable", "true");
        for (Map.Entry<Object, Object> systemProperty : SystemPropertiesUtils.getSystemProperties().entrySet()) {
            String propName = systemProperty.getKey().toString();
            if (propName.startsWith("mail.")) {
                properties.put(propName, systemProperty.getValue());
            }
        }
        if (MailProperties.getInstance().getJavaMailProperties() != null) {
            /*
             * Overwrite current JavaMail-Specific properties with the ones defined in javamail.properties
             */
            properties.putAll(MailProperties.getInstance().getJavaMailProperties());
        }
    }
}
