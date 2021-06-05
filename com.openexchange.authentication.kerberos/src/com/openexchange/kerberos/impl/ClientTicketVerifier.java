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

package com.openexchange.kerberos.impl;

import java.security.PrivilegedExceptionAction;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import com.openexchange.kerberos.ClientPrincipal;

/**
 * {@link ClientTicketVerifier}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class ClientTicketVerifier implements PrivilegedExceptionAction<ClientPrincipal> {

    private final GSSManager manager;
    private final byte[] serviceTicket;

    /**
     * Initializes a new {@link ClientTicketVerifier}.
     */
    public ClientTicketVerifier(GSSManager manager, byte[] serviceTicket) {
        super();
        this.manager = manager;
        this.serviceTicket = serviceTicket;
    }

    @Override
    public ClientPrincipal run() throws GSSException {
        // create a security context for decrypting the service ticket
        final GSSContext context = manager.createContext((GSSCredential) null);
        try {
            final ClientPrincipalImpl principal = new ClientPrincipalImpl();
            principal.setClientTicket(serviceTicket);
            // decrypt the service ticket
            byte[] tokenForClient = context.acceptSecContext(serviceTicket, 0, serviceTicket.length);
            principal.setResponseTicket(tokenForClient);
            // get client name
            final GSSName clientGSSName = context.getSrcName();
            final String clientName = clientGSSName.toString();
            final Subject clientSubject = new Subject();
            clientSubject.getPrincipals().add(new KerberosPrincipal(clientName));
            principal.setClientSubject(clientSubject);
            return principal;
        } finally {
            context.dispose();
        }
    }
}
