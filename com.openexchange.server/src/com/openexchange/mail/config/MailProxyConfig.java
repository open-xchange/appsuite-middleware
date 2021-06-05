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

package com.openexchange.mail.config;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.java.Strings;
import com.openexchange.net.HostList;
import com.openexchange.systemproperties.SystemPropertiesUtils;

/**
 * {@link MailProxyConfig}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class MailProxyConfig {

    private static MailProxyConfig INSTANCE = new MailProxyConfig();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static MailProxyConfig getInstance() {
        return INSTANCE;
    }

    private static final String IMAP_NON_PROXY_HOST = "mail.imap.proxy.nonProxyHosts";
    private static final String SMTP_NON_PROXY_HOST = "mail.smtp.proxy.nonProxyHosts";
    private static final String POP3_NON_PROXY_HOST = "mail.pop3.proxy.nonProxyHosts";

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static enum Protocol {
        IMAP, SMTP, POP3;
    }

   // -------------------------------------------------------------------------------------------------------------------------------------

    private final AtomicReference<HostList> imapHostList;
    private final AtomicReference<HostList> smtpHostList;
    private final AtomicReference<HostList> pop3HostList;

    /**
     * Initializes a new {@link MailProxyConfig}.
     */
    private MailProxyConfig() {
        super();
        imapHostList = new AtomicReference<HostList>();
        smtpHostList = new AtomicReference<HostList>();
        pop3HostList = new AtomicReference<HostList>();
    }

    public HostList getImapNonProxyHostList() {
        return getHostListFrom(Protocol.IMAP);
    }

    public HostList getSmtpNonProxyHostList() {
        return getHostListFrom(Protocol.SMTP);
    }

    public HostList getPop3NonProxyHostList() {
        return getHostListFrom(Protocol.POP3);
    }

    private HostList getHostListFrom(Protocol proto) {
        AtomicReference<HostList> hostListReference;
        switch (proto) {
            case IMAP:
                hostListReference = imapHostList;
                break;
            case POP3:
                hostListReference = smtpHostList;
                break;
            case SMTP:
                hostListReference = pop3HostList;
                break;
            default:
                throw new IllegalStateException("Unknown protocol: " + proto);
        }

        HostList hostList = hostListReference.get();
        if (hostList == null) {
            synchronized (hostListReference) {
                hostList = hostListReference.get();
                if (hostList == null) {
                    String propertyName;
                    switch (proto) {
                        case IMAP:
                            propertyName = IMAP_NON_PROXY_HOST;
                            break;
                        case POP3:
                            propertyName = POP3_NON_PROXY_HOST;
                            break;
                        case SMTP:
                            propertyName = SMTP_NON_PROXY_HOST;
                            break;
                        default:
                            throw new IllegalStateException("Unknown protocol: " + proto);
                    }
                    String property = SystemPropertiesUtils.getProperty(propertyName);
                    hostList = Strings.isEmpty(property) ? HostList.EMPTY : HostList.valueOf(property.replace('|', ','));
                    hostListReference.set(hostList);
                }
            }
        }
        return hostList;
    }

}
