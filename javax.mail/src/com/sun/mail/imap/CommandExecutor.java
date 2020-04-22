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

package com.sun.mail.imap;

import java.io.IOException;
import java.util.Optional;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.Protocol;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.iap.ResponseInterceptor;
import com.sun.mail.imap.protocol.IMAPProtocol;

/**
 * {@link CommandExecutor} - Is responsible for executing commands and reading responses.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public interface CommandExecutor {

    /**
     * Protocol property that denotes whether this executor is called for a primary mail account.
     * If so, the property returns the string {@code true}. Otherwise {@code null} or any string different
     * from {@code true}.
     */
    static final String PROP_PRIMARY_ACCOUNT = "mail.imap.primary";

    /**
     * Checks if this executor is applicable to given protocol instance
     *
     * @param protocol The protocol instance
     * @return <code>true</code> if applicable; otherwise <code>false</code>
     */
    boolean isApplicable(Protocol protocol);

    /**
     * Executes given command with given arguments using specified protocol instance.
     *
     * @param command The command
     * @param args The arguments
     * @param optionalInterceptor The optional interceptor
     * @param protocol The protocol instance
     * @return The response array
     */
    default Response[] executeCommand(String command, Argument args, Optional<ResponseInterceptor> optionalInterceptor, Protocol protocol) {
        return protocol.executeCommand(command, args, optionalInterceptor);
    }

    /**
     * Reads a response using specified protocol instance.
     *
     * @param protocol The protocol instance
     * @return The response
     * @throws IOException If an I/O error occurs
     */
    default Response readResponse(Protocol protocol) throws IOException {
        try {
            return protocol.readResponse();
        } catch (ProtocolException e) {
            // Cannot occur
            throw new IOException(e);
        }
    }

    /**
     * Issues given command and returns its unique tag identifier.
     * <p>
     * Example: <code>"A1 LOGIN bob secret"</code>
     *
     * @param command The command
     * @param args The arguments
     * @param protocol The protocol instance
     * @return The tag identifier
     * @throws IOException If an I/O error occurs
     * @throws ProtocolException If a protocol error occurs
     */
    default String writeCommand(String command, Argument args, Protocol protocol) throws IOException, ProtocolException {
        return protocol.writeCommand(command, args);
    }

    /**
     * Issues the AUTHENTICATE command with AUTH=LOGIN authenticate scheme.
     *
     * @param u The user name
     * @param p The password
     * @param protocol The protocol instance
     * @throws ProtocolException If a protocol error occurs
     */
    default void authlogin(String u, String p, Protocol protocol) throws ProtocolException {
        if (!(protocol instanceof IMAPProtocol)) {
            throw new ProtocolException("Invalid protocol instance: " + protocol.getClass().getName());
        }
        ((IMAPProtocol) protocol).authlogin(u, p);
    }

    /**
     * Issues the AUTHENTICATE command with AUTH=PLAIN authentication scheme.
     *
     * @param authzid The authorization identifier
     * @param u The user name
     * @param p The password
     * @param protocol The protocol instance
     * @throws ProtocolException If a protocol error occurs
     */
    default void authplain(String authzid, String u, String p, Protocol protocol) throws ProtocolException {
        if (!(protocol instanceof IMAPProtocol)) {
            throw new ProtocolException("Invalid protocol instance: " + protocol.getClass().getName());
        }
        ((IMAPProtocol) protocol).authplain(authzid, u, p);
    }

    /**
     * Issues the AUTHENTICATE command with AUTH=NTLM authentication scheme.
     *
     * @param authzid The authorization identifier
     * @param u The user name
     * @param p The password
     * @param protocol The protocol instance
     * @throws ProtocolException If a protocol error occurs
     */
    default void authntlm(String authzid, String u, String p, Protocol protocol) throws ProtocolException {
        if (!(protocol instanceof IMAPProtocol)) {
            throw new ProtocolException("Invalid protocol instance: " + protocol.getClass().getName());
        }
        ((IMAPProtocol) protocol).authntlm(authzid, u, p);
    }

    /**
     * Issues the AUTHENTICATE command with AUTH=XOAUTH2 authenticate scheme.
     *
     * @param u The user name
     * @param p The password
     * @param protocol The protocol instance
     * @throws ProtocolException If a protocol error occurs
     */
    default void authoauth2(String u, String p, Protocol protocol) throws ProtocolException {
        if (!(protocol instanceof IMAPProtocol)) {
            throw new ProtocolException("Invalid protocol instance: " + protocol.getClass().getName());
        }
        ((IMAPProtocol) protocol).authoauth2(u, p);
    }

    /**
     * Issues the AUTHENTICATE command with AUTH=OAUTHBEARER authenticate scheme.
     *
     * @param u The user name
     * @param p The password
     * @param protocol The protocol instance
     * @throws ProtocolException If a protocol error occurs
     */
    default void authoauthbearer(String u, String p, Protocol protocol) throws ProtocolException {
        if (!(protocol instanceof IMAPProtocol)) {
            throw new ProtocolException("Invalid protocol instance: " + protocol.getClass().getName());
        }
        ((IMAPProtocol) protocol).authoauthbearer(u, p);
    }

    /**
     * Gets the ranking for this command executor.
     * <p>
     * The higher the ranking, the more likely the executor will be invoked in preference over others.
     *
     * @return The ranking
     */
    default int getRanking() {
        return 0;
    }
}
