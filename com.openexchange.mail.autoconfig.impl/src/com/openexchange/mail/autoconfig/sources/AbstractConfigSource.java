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

            // Apply data from best fitting incoming server
            {
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

                if (null == currentIncomingServer) {
                    return null;
                }

                autoconfig.setMailPort(currentIncomingServer.getPort());
                autoconfig.setMailProtocol(currentIncomingServer.getType().getKeyword());
                SocketType incomingSocket = currentIncomingServer.getSocketType();
                switch (incomingSocket) {
                    case PLAIN:
                        autoconfig.setMailSecure(false);
                        autoconfig.setMailStartTls(false);
                        break;
                    case SSL:
                        autoconfig.setMailSecure(true);
                        autoconfig.setMailStartTls(false);
                        break;
                    case STARTTLS:
                        autoconfig.setMailSecure(false);
                        autoconfig.setMailStartTls(true);
                        break;
                    default:
                        break;
                }
                autoconfig.setMailServer(currentIncomingServer.getHostname());
                autoconfig.setUsername(currentIncomingServer.getUsername());
            }

            // Apply data from best fitting outgoing server
            {
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

                if (null == currentOutgoingServer) {
                    return null;
                }

                autoconfig.setTransportPort(currentOutgoingServer.getPort());
                autoconfig.setTransportProtocol(currentOutgoingServer.getType().getKeyword());
                SocketType outgoingSocket = currentOutgoingServer.getSocketType();
                switch (outgoingSocket) {
                    case PLAIN:
                        autoconfig.setTransportSecure(false);
                        autoconfig.setTransportStartTls(false);
                        break;
                    case SSL:
                        autoconfig.setTransportSecure(true);
                        autoconfig.setTransportStartTls(false);
                        break;
                    case STARTTLS:
                        autoconfig.setTransportSecure(false);
                        autoconfig.setTransportStartTls(true);
                        break;
                    default:
                        break;
                }

                autoconfig.setTransportServer(currentOutgoingServer.getHostname());
            }

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

    /**
     * Checks if secure connection is enforced but not available.
     *
     * @param forceSecure
     * @param autoconfig
     * @return
     */
    protected boolean skipDueToForcedSecure(boolean forceSecure, DefaultAutoconfig autoconfig) {
        return forceSecure && ((!autoconfig.isMailSecure().booleanValue() && !autoconfig.isMailStartTls()) || (!autoconfig.isTransportSecure().booleanValue() && !autoconfig.isTransportStartTls()));
    }

}
