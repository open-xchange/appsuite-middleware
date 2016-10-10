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

package com.openexchange.mail.autoconfig.sources;

import static com.openexchange.mail.autoconfig.xmlparser.Server.EMAILADDRESS;
import static com.openexchange.mail.autoconfig.xmlparser.Server.MAILLOCALPART;
import com.openexchange.mail.autoconfig.DefaultAutoconfig;
import com.openexchange.mail.autoconfig.xmlparser.ClientConfig;
import com.openexchange.mail.autoconfig.xmlparser.EmailProvider;
import com.openexchange.mail.autoconfig.xmlparser.IncomingServer;
import com.openexchange.mail.autoconfig.xmlparser.OutgoingServer;
import com.openexchange.mail.autoconfig.xmlparser.Server.SocketType;

/**
 * {@link AbstractConfigSource}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public abstract class AbstractConfigSource implements ConfigSource {

    /**
     * Initializes a new {@link AbstractConfigSource}.
     */
    public AbstractConfigSource() {
        super();
    }

    protected DefaultAutoconfig getBestConfiguration(ClientConfig clientConfig, String domain) {
        DefaultAutoconfig autoconfig = new DefaultAutoconfig();
        for (EmailProvider emailProvider : clientConfig.getEmailProvider()) {
            if (!emailProvider.getDomains().contains(domain)) {
                continue;
            }

            IncomingServer currentIncomingServer = null;
            for (IncomingServer incomingServer : emailProvider.getIncomingServer()) {
                // First incomingServer
                if (currentIncomingServer == null) {
                    currentIncomingServer = incomingServer;
                    continue;
                }
                // Better ServerType (e.g. IMAP > POP3)
                if (incomingServer.getType().compareTo(currentIncomingServer.getType()) > 0) {
                    currentIncomingServer = incomingServer;
                    continue;
                }
                // Better SocketType (e.g. SSL > STARTTLS)
                if (incomingServer.getType().compareTo(currentIncomingServer.getType()) == 0 && incomingServer.getSocketType().compareTo(
                    currentIncomingServer.getSocketType()) > 0) {
                    currentIncomingServer = incomingServer;
                    continue;
                }
            }

            autoconfig.setMailPort(currentIncomingServer.getPort());
            autoconfig.setMailProtocol(currentIncomingServer.getType().getKeyword());
            SocketType incomingSocket = currentIncomingServer.getSocketType();
            autoconfig.setMailSecure(incomingSocket == SocketType.SSL /*|| incomingSocket == SocketType.STARTTLS*/);
            autoconfig.setMailServer(currentIncomingServer.getHostname());

            OutgoingServer currentOutgoingServer = null;
            for (OutgoingServer outgoingServer : emailProvider.getOutgoingServer()) {
                // First outgoingServer
                if (currentOutgoingServer == null) {
                    currentOutgoingServer = outgoingServer;
                    continue;
                }
                // Better ServerType (e.g. SMTP > ???)
                if (outgoingServer.getType().compareTo(currentOutgoingServer.getType()) > 0) {
                    currentOutgoingServer = outgoingServer;
                    continue;
                }
                // Better SocketType (e.g. SSL > STARTTLS)
                if (outgoingServer.getType().compareTo(currentOutgoingServer.getType()) == 0 && outgoingServer.getSocketType().compareTo(
                    currentOutgoingServer.getSocketType()) > 0) {
                    currentOutgoingServer = outgoingServer;
                    continue;
                }
            }

            autoconfig.setTransportPort(currentOutgoingServer.getPort());
            autoconfig.setTransportProtocol(currentOutgoingServer.getType().getKeyword());
            SocketType outgoingSocket = currentOutgoingServer.getSocketType();
            autoconfig.setTransportSecure(outgoingSocket == SocketType.SSL /*|| outgoingSocket == SocketType.STARTTLS*/);
            autoconfig.setTransportServer(currentOutgoingServer.getHostname());

            autoconfig.setUsername(currentIncomingServer.getUsername());

            break;
        }

        return autoconfig;
    }

    /**
     * @param autoconfig
     * @param emailLocalPart
     * @param emailDomain
     */
    protected void replaceUsername(DefaultAutoconfig autoconfig, String emailLocalPart, String emailDomain) {
        if (null != autoconfig) {
            String username = autoconfig.getUsername();
            if (null != username) {
                if (username.equalsIgnoreCase(EMAILADDRESS)) {
                    autoconfig.setUsername(new StringBuilder(24).append(emailLocalPart).append("@").append(emailDomain).toString());
                } else if (username.equalsIgnoreCase(MAILLOCALPART)) {
                    autoconfig.setUsername(emailLocalPart);
                }
            }
        }
    }

}
