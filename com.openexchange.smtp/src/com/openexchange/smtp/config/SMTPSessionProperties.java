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

package com.openexchange.smtp.config;

import java.util.Properties;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.mail.mime.MimeSessionPropertyNames;
import com.openexchange.smtp.SmtpReloadable;

/**
 * {@link SMTPSessionProperties} - Default properties for an SMTP session established via <code>JavaMail</code> API
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SMTPSessionProperties {

    private static volatile Properties sessionProperties;

    /**
     * No instantiation
     */
    private SMTPSessionProperties() {
        super();
    }

    /**
     * Creates a <b>cloned</b> version of default SMTP session properties
     *
     * @return a cloned version of default SMTP session properties
     */
    public static Properties getDefaultSessionProperties() {
        Properties tmp = sessionProperties;
        if (null == tmp) {
            synchronized (SMTPSessionProperties.class) {
                tmp = sessionProperties;
                if (null == tmp) {
                    initializeSMTPProperties();
                    tmp = sessionProperties;
                }
            }
        }
        return (Properties) tmp.clone();
    }

    /**
     * Resets default SMTP session properties
     */
    public static void resetDefaultSessionProperties() {
        if (null != sessionProperties) {
            synchronized (SMTPSessionProperties.class) {
                if (null != sessionProperties) {
                    sessionProperties = null;
                }
            }
        }
    }

    static {
        SmtpReloadable.getInstance().addReloadable(new Reloadable() {

            @Override
            public void reloadConfiguration(final ConfigurationService configService) {
                sessionProperties = null;
            }

            @Override
            public Interests getInterests() {
                return Reloadables.interestsForFiles("smtp.properties");
            }
        });
    }

    /**
     * This method can only be exclusively accessed
     */
    private static void initializeSMTPProperties() {
        /*
         * Define SMTP properties
         */
        sessionProperties = MimeDefaultSession.getDefaultMailProperties();
        /*
         * Set some global JavaMail properties
         */
        final Properties properties = sessionProperties;
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
        /*
         * Deny NTLM authentication
         */
        properties.put("mail.smtp.auth.ntlm.disable", "true");
        if (MailProperties.getInstance().getJavaMailProperties() != null) {
            /*
             * Overwrite current JavaMail-Specific properties with the ones defined in javamail.properties
             */
            properties.putAll(MailProperties.getInstance().getJavaMailProperties());
        }
    }
}
