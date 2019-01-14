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

package com.openexchange.mail.config;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.java.Strings;
import com.openexchange.net.HostList;

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

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final AtomicReference<HostList> imapHostList;
    private final AtomicReference<HostList> smtpHostList;

    /**
     * Initializes a new {@link MailProxyConfig}.
     */
    private MailProxyConfig() {
        super();
        imapHostList = new AtomicReference<HostList>();
        smtpHostList = new AtomicReference<HostList>();
    }

    public HostList getImapNonProxyHostList() {
        return getHostListFrom(true);
    }

    public HostList getSmtpNonProxyHostList() {
        return getHostListFrom(false);
    }

    private HostList getHostListFrom(boolean imap) {
        AtomicReference<HostList> hostListReference = imap ? imapHostList : smtpHostList;
        HostList hostList = hostListReference.get();
        if (hostList == null) {
            synchronized (hostListReference) {
                hostList = hostListReference.get();
                if (hostList == null) {
                    String property = System.getProperty(imap ? IMAP_NON_PROXY_HOST : SMTP_NON_PROXY_HOST);
                    hostList = Strings.isEmpty(property) ? HostList.EMPTY : HostList.valueOf(property.replace('|', ','));
                    hostListReference.set(hostList);
                }
            }
        }
        return hostList;
    }

}
