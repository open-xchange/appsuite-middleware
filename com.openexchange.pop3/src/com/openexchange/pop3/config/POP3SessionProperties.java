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

package com.openexchange.pop3.config;

import java.util.Properties;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeSessionPropertyNames;

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
        if (MailProperties.getInstance().getJavaMailProperties() != null) {
            /*
             * Overwrite current JavaMail-Specific properties with the ones defined in javamail.properties
             */
            properties.putAll(MailProperties.getInstance().getJavaMailProperties());
        }
    }
}
