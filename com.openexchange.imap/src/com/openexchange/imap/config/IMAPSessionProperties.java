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

package com.openexchange.imap.config;

import java.util.Properties;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeSessionPropertyNames;

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
         * Partial fetch size in bytes. Defaults to 16K.
         */
        properties.put("mail.imap.fetchsize", "65536");
        /*
         * Deny NTLM authentication
         */
        properties.put("mail.imap.auth.ntlm.disable", "true");
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
