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

package com.openexchange.mail.mime;

import java.util.Properties;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.config.MailReloadable;

/**
 * {@link MimeDefaultSession} - Provides access to default instance of {@link javax.mail.Session}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MimeDefaultSession {

    /**
     * No instance
     */
    private MimeDefaultSession() {
        super();
    }

    private static volatile javax.mail.Session instance;

    private static volatile Properties properties;

    /**
     * Applies basic properties to system properties and instantiates the singleton instance of {@link javax.mail.Session}.
     *
     * @return The default instance of {@link javax.mail.Session}
     */
    public static javax.mail.Session getDefaultSession() {
        javax.mail.Session tmp = instance;
        if (tmp == null) {
            synchronized (MimeDefaultSession.class) {
                tmp = instance;
                if (tmp == null) {
                    /*
                     * Define session properties
                     */
                    final Properties systemProperties = System.getProperties();
                    systemProperties.putAll(getDefaultMailProperties());
                    instance = tmp = javax.mail.Session.getInstance(((Properties) (systemProperties.clone())), null);
                }
            }
        }
        return tmp;
    }

    static {
        MailReloadable.getInstance().addReloadable(new Reloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                instance = null;
                properties = null;
            }

            @Override
            public Interests getInterests() {
                return null;
            }
        });
    }

    /**
     * Gets a clone of the default mail properties.
     *
     * @return A clone of the default mail properties
     */
    public static Properties getDefaultMailProperties() {
        Properties p = properties;
        if (null == properties) {
            synchronized (MimeDefaultSession.class) {
                p = properties;
                if (null == properties) {
                    p = new Properties();
                    p.put(MimeSessionPropertyNames.PROP_MAIL_MIME_BASE64_IGNOREERRORS, "true");
                    p.put(MimeSessionPropertyNames.PROP_ALLOWREADONLYSELECT, "true");
                    p.put(MimeSessionPropertyNames.PROP_MAIL_MIME_ENCODEEOL_STRICT, "true");
                    p.put(MimeSessionPropertyNames.PROP_MAIL_MIME_DECODETEXT_STRICT, "false");
                    p.put(MimeSessionPropertyNames.PROP_MAIL_MIME_MULTIPART_ALLOWEMPTY, "true");
                    final MailProperties mailProperties = MailProperties.getInstance();
                    final String defaultMimeCharset = mailProperties.getDefaultMimeCharset();
                    if (null == defaultMimeCharset) {
                        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MimeDefaultSession.class);
                        log.warn("Missing default MIME charset in mail configuration. Mail configuration is probably not initialized. Using fallback 'UTF-8' instead");
                        p.put(MimeSessionPropertyNames.PROP_MAIL_MIME_CHARSET, "UTF-8");
                    } else {
                        p.put(MimeSessionPropertyNames.PROP_MAIL_MIME_CHARSET, defaultMimeCharset);
                    }
                    final Properties javaMailProperties = mailProperties.getJavaMailProperties();
                    if (javaMailProperties != null) {
                        /*
                         * Overwrite current JavaMail-Specific properties with the ones defined in javamail.properties
                         */
                        p.putAll(javaMailProperties);
                    }
                    properties = p;
                }
            }
        }
        return (Properties) p.clone();
    }

}
