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

package com.openexchange.mail.mime;

import java.util.Map;
import java.util.Properties;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.config.MailReloadable;
import com.openexchange.systemproperties.SystemPropertiesUtils;

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
                    instance = tmp = javax.mail.Session.getInstance(SystemPropertiesUtils.cloneSystemProperties(), null);
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
                    for (Map.Entry<Object, Object> systemProperty : SystemPropertiesUtils.getSystemProperties().entrySet()) {
                        String propName = systemProperty.getKey().toString();
                        if (propName.startsWith("mail.")) {
                            p.put(propName, systemProperty.getValue());
                        }
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
