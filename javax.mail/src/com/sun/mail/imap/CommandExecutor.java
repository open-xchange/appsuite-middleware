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

package com.sun.mail.imap;

import java.io.IOException;
import java.util.Optional;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.Protocol;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.iap.ResponseInterceptor;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.util.SocketConnector;

/**
 * {@link CommandExecutor} - Is responsible for executing commands and reading responses.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public interface CommandExecutor extends SocketConnector {

    /**
     * Protocol property that denotes whether this executor is called for a primary mail account.
     * If so, the property returns the string {@code true}. Otherwise {@code null} or any string different
     * from {@code true}.
     */
    static final String PROP_PRIMARY_ACCOUNT = "mail.imap.primary";

    /**
     * Checks if this executor is applicable to given protocol instance
     *
     * @param protocolAccess The protocol access
     * @return <code>true</code> if applicable; otherwise <code>false</code>
     */
    boolean isApplicable(ProtocolAccess protocolAccess);

    /**
     * Executes given command with given arguments using specified protocol instance.
     *
     * @param command The command
     * @param args The arguments
     * @param optionalInterceptor The optional interceptor
     * @param protocolAccess The protocol access
     * @return The response array
     */
    default Response[] executeCommand(String command, Argument args, Optional<ResponseInterceptor> optionalInterceptor, ProtocolAccess protocolAccess) {
        Protocol protocol = protocolAccess.getProtocol();
        return protocol.executeCommand(command, args, optionalInterceptor);
    }

    /**
     * Reads a response using specified protocol instance.
     *
     * @param protocolAccess The protocol access
     * @return The response
     * @throws IOException If an I/O error occurs
     */
    default Response readResponse(ProtocolAccess protocolAccess) throws IOException {
        try {
            Protocol protocol = protocolAccess.getProtocol();
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
     * @param protocolAccess The protocol access
     * @return The tag identifier
     * @throws IOException If an I/O error occurs
     * @throws ProtocolException If a protocol error occurs
     */
    default String writeCommand(String command, Argument args, ProtocolAccess protocolAccess) throws IOException, ProtocolException {
        Protocol protocol = protocolAccess.getProtocol();
        return protocol.writeCommand(command, args);
    }

    /**
     * Issues the AUTHENTICATE command with AUTH=LOGIN authenticate scheme.
     *
     * @param u The user name
     * @param p The password
     * @param protocolAccess The protocol access
     * @throws ProtocolException If a protocol error occurs
     */
    default void authlogin(String u, String p, ProtocolAccess protocolAccess) throws ProtocolException {
        Protocol protocol = protocolAccess.getProtocol();
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
     * @param protocolAccess The protocol access
     * @throws ProtocolException If a protocol error occurs
     */
    default void authplain(String authzid, String u, String p, ProtocolAccess protocolAccess) throws ProtocolException {
        Protocol protocol = protocolAccess.getProtocol();
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
     * @param protocolAccess The protocol access
     * @throws ProtocolException If a protocol error occurs
     */
    default void authntlm(String authzid, String u, String p, ProtocolAccess protocolAccess) throws ProtocolException {
        Protocol protocol = protocolAccess.getProtocol();
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
     * @param protocolAccess The protocol access
     * @throws ProtocolException If a protocol error occurs
     */
    default void authoauth2(String u, String p, ProtocolAccess protocolAccess) throws ProtocolException {
        Protocol protocol = protocolAccess.getProtocol();
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
     * @param protocolAccess The protocol access
     * @throws ProtocolException If a protocol error occurs
     */
    default void authoauthbearer(String u, String p, ProtocolAccess protocolAccess) throws ProtocolException {
        Protocol protocol = protocolAccess.getProtocol();
        if (!(protocol instanceof IMAPProtocol)) {
            throw new ProtocolException("Invalid protocol instance: " + protocol.getClass().getName());
        }
        ((IMAPProtocol) protocol).authoauthbearer(u, p);
    }

    /**
     * Issues the SASL-based login.
     *
     * @param allowed The SASL mechanisms we're allowed to use
     * @param realm The SASL realm
     * @param authzid The authorization identifier
     * @param u The user name
     * @param p The password
     * @param protocolAccess The protocol access
     * @throws ProtocolException If a protocol error occurs
     */
    default void authsasl(String[] allowed, String realm, String authzid, String u, String p, ProtocolAccess protocolAccess) throws ProtocolException {
        Protocol protocol = protocolAccess.getProtocol();
        if (!(protocol instanceof IMAPProtocol)) {
            throw new ProtocolException("Invalid protocol instance: " + protocol.getClass().getName());
        }
        ((IMAPProtocol) protocol).sasllogin(allowed, realm, authzid, u, p);
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
