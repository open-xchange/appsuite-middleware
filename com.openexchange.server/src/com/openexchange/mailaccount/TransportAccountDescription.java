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

package com.openexchange.mailaccount;

import java.net.URISyntaxException;
import javax.mail.internet.idn.IDNA;
import com.openexchange.exception.OXException;
import com.openexchange.tools.net.URITools;

/**
 * {@link TransportAccountDescription}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class TransportAccountDescription {

    private int id;
    private String login;
    private String name;
    private String password;
    private String personal;
    private String primaryAddress;
    private String replyTo;
    private TransportAuth transportAuth;
    private int transportPort;
    private String transportProtocol;
    private boolean transportSecure;
    private String transportServer;
    private boolean transportStartTls;
    private String transportUrl;

    /**
     * Generates transport server URL
     *
     * @return The transport server URL
     * @throws OXException If URL cannot be parsed
     */
    public String generateTransportServerURL() throws OXException {
        if (null != transportUrl) {
            return transportUrl;
        }
        if (com.openexchange.java.Strings.isEmpty(transportServer)) {
            return null;
        }
        final String protocol = transportSecure ? transportProtocol + 's' : transportProtocol;
        try {
            return transportUrl = URITools.generateURI(protocol, IDNA.toASCII(transportServer), transportPort).toString();
        } catch (final URISyntaxException e) {
            final StringBuilder sb = new StringBuilder(32);
            sb.append(transportProtocol);
            if (transportSecure) {
                sb.append('s');
            }
            throw MailAccountExceptionCodes.INVALID_HOST_NAME.create(e, sb.append("://").append(transportServer).append(':').append(transportPort).toString());
        }
    }

    /**
     * Gets the ID.
     *
     * @return The ID or <code>-1</code> if not set
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the account name.
     *
     * @return The account name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the personal.
     *
     * @return The personal
     */
    public String getPersonal() {
        return personal;
    }

    /**
     * Gets the primary email address.
     *
     * @return The primary email address
     */
    public String getPrimaryAddress() {
        return primaryAddress;
    }

    /**
     * Gets the reply-to address
     *
     * @return The reply-to address
     */
    public String getReplyTo() {
        return replyTo;
    }

    /**
     * Gets the transport authentication information
     *
     * @return The transport authentication information
     */
    public TransportAuth getTransportAuth() {
        return transportAuth;
    }

    /**
     * Gets the transport login.
     *
     * @return The transport login
     */
    public String getTransportLogin() {
        return login;
    }

    /**
     * Gets the transport password.
     *
     * @return The transport password
     */
    public String getTransportPassword() {
        return password;
    }

    /**
     * Gets the transport server port.
     *
     * @return The transport server port
     */
    public int getTransportPort() {
        return transportPort;
    }

    /**
     * Gets the transport server protocol.
     *
     * @return The transport server protocol
     */
    public String getTransportProtocol() {
        return transportProtocol;
    }

    /**
     * Gets the transport server name.
     * <p>
     * The transport server name can either be a machine name, such as "<code>java.sun.com</code>", or a textual representation of its IP
     * address.
     *
     * @return The transport server name
     */
    public String getTransportServer() {
        return transportServer;
    }

    /**
     * Checks if STARTTLS should be used to connect to transport server
     * 
     * @return
     */
    public boolean isTransportStartTls() {
        return transportStartTls;
    }
}
