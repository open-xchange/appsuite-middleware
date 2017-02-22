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

package com.openexchange.userfeedback.mail.config;

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.userfeedback.mail.osgi.Services;

/**
 * {@link MailProperties}
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since 7.8.4
 */
public class MailProperties implements Reloadable {

    private static String SENDER_NAME = "com.openexchange.userfeedback.mail.senderName";
    private static String SENDER_NAME_DEFAULT = "";

    private static String SENDER_ADDRESS = "com.openexchange.userfeedback.mail.senderAddress";
    private static String SENDER_ADDRESS_DEFAULT = "";

    private static String SMTP_HOSTNAME = "com.openexchange.userfeedback.smtp.hostname";
    private static String SMTP_HOSTNAME_DEFAULT = "";

    private static String SMTP_PORT = "com.openexchange.userfeedback.smtp.port";
    private static String SMTP_PORT_DEFAULT = "";

    private static String SMTP_PROTOCOL = "com.openexchange.userfeedback.smtp.protocol";
    private static String SMTP_PROTOCOL_DEFAULT = "TLSv1";

    private static String SMTP_TIMEOUT = "com.openexchange.userfeedback.smtp.timeout";
    private static int SMTP_TIMEOUT_DEFAULT = 50000;

    private static String SMTP_CONNECTION_TIMEOUT = "com.openexchange.userfeedback.smtp.connectionTimeout";
    private static int SMTP_CONNECTION_TIMEOUT_DEFAULT = 10000;

    private static volatile String senderName;
    private static volatile String senderAddress;
    private static volatile String smtpHostname;
    private static volatile String smtpPort;
    private static volatile String smtpProtocol;
    private static volatile Integer smtpTimeout;
    private static volatile Integer smtpConnectionTimeout;

    public MailProperties() {
        super();
    }

    public static String getSenderName() {
        String loadedSenderName = senderName;
        if (loadedSenderName == null) {
            synchronized (MailProperties.class) {
                loadedSenderName = senderName;
                if (loadedSenderName == null) {
                    loadedSenderName = loadStringValue(SENDER_NAME, SENDER_NAME_DEFAULT);
                    senderName = loadedSenderName;
                }
            }
        }
        return loadedSenderName;
    }

    public static String getSenderAddress() {
        String loadedSenderAddress = senderAddress;
        if (loadedSenderAddress == null) {
            synchronized (MailProperties.class) {
                loadedSenderAddress = senderAddress;
                if (loadedSenderAddress == null) {
                    loadedSenderAddress = loadStringValue(SENDER_ADDRESS, SENDER_ADDRESS_DEFAULT);
                    senderAddress = loadedSenderAddress;
                }
            }
        }
        return loadedSenderAddress;
    }

    public static String getSmtpHostname() {
        String loadedSmtpHostname = smtpHostname;
        if (loadedSmtpHostname == null) {
            synchronized (MailProperties.class) {
                loadedSmtpHostname = smtpHostname;
                if (loadedSmtpHostname == null) {
                    loadedSmtpHostname = loadStringValue(SMTP_HOSTNAME, SMTP_HOSTNAME_DEFAULT);
                    smtpHostname = loadedSmtpHostname;
                }
            }
        }
        return loadedSmtpHostname;
    }

    public static String getSmtpPort() {
        String loadedSmtpPort = smtpPort;
        if (loadedSmtpPort == null) {
            synchronized (MailProperties.class) {
                loadedSmtpPort = smtpPort;
                if (loadedSmtpPort == null) {
                    loadedSmtpPort = loadStringValue(SMTP_PORT, SMTP_PORT_DEFAULT);
                    smtpPort = loadedSmtpPort;
                }
            }
        }
        return loadedSmtpPort;
    }

    public static String getSmtpProtocol() {
        String loadedSmtpProtocol = smtpProtocol;
        if (loadedSmtpProtocol == null) {
            synchronized (MailProperties.class) {
                loadedSmtpProtocol = smtpProtocol;
                if (loadedSmtpProtocol == null) {
                    loadedSmtpProtocol = loadStringValue(SMTP_PROTOCOL, SMTP_PROTOCOL_DEFAULT);
                    smtpProtocol = loadedSmtpProtocol;
                }
            }
        }
        return loadedSmtpProtocol;
    }

    public static Integer getSmtpTimeout() {
        Integer loadedSmtpTimeout = smtpTimeout;
        if (loadedSmtpTimeout == null) {
            synchronized (MailProperties.class) {
                loadedSmtpTimeout = smtpTimeout;
                if (loadedSmtpTimeout == null) {
                    loadedSmtpTimeout = loadIntegerValue(SMTP_TIMEOUT, SMTP_TIMEOUT_DEFAULT);
                    smtpTimeout = loadedSmtpTimeout;
                }
            }
        }
        return loadedSmtpTimeout;
    }

    public static Integer getSmtpConnectionTimeout() {
        Integer loadedSmtpConnectionTimeout = smtpConnectionTimeout;
        if (loadedSmtpConnectionTimeout == null) {
            synchronized (MailProperties.class) {
                loadedSmtpConnectionTimeout = smtpConnectionTimeout;
                if (loadedSmtpConnectionTimeout == null) {
                    loadedSmtpConnectionTimeout = loadIntegerValue(SMTP_CONNECTION_TIMEOUT, SMTP_CONNECTION_TIMEOUT_DEFAULT);
                    smtpConnectionTimeout = loadedSmtpConnectionTimeout;
                }
            }
        }
        return loadedSmtpConnectionTimeout;
    }

    private static Integer loadIntegerValue(String key, int defaultValue) {
        Integer propertyValue = null;
        ConfigurationService service = Services.getService(ConfigurationService.class);
        propertyValue = service.getIntProperty(key, defaultValue);
        return propertyValue;
    }

    private static String loadStringValue(String key, String defaultValue) {
        String propertyValue = null;
        ConfigurationService service = Services.getService(ConfigurationService.class);
        propertyValue = service.getProperty(key, defaultValue);
        return propertyValue;
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        senderName = null;
        senderAddress = null;
        smtpHostname = null;
        smtpPort = null;
        smtpProtocol = null;
        smtpTimeout = null;
        smtpConnectionTimeout = null;
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties(SENDER_NAME, SENDER_ADDRESS, SMTP_HOSTNAME, SMTP_PORT, SMTP_PROTOCOL, SMTP_TIMEOUT, SMTP_CONNECTION_TIMEOUT);
    }

}
