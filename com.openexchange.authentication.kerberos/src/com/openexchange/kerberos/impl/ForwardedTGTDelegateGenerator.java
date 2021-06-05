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

import java.security.Principal;
import java.security.PrivilegedExceptionAction;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openexchange.exception.OXException;
import com.openexchange.kerberos.ClientPrincipal;
import com.openexchange.kerberos.KerberosExceptionCodes;
import com.openexchange.tools.encoding.Base64;

/**
 * Creates a delegation ticket for the client to contact a backend service.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
final class ForwardedTGTDelegateGenerator implements PrivilegedExceptionAction<ClientPrincipal> {

	private static final Logger LOG = LoggerFactory.getLogger(ForwardedTGTDelegateGenerator.class);

    private final GSSManager manager;
    private final byte[] forwardedTicket;

    public ForwardedTGTDelegateGenerator(GSSManager manager, byte[] forwardedTicket) {
        super();
        this.manager = manager;
        this.forwardedTicket = forwardedTicket;
    }

    @Override
    public ClientPrincipal run() throws GSSException, OXException {
        // create a security context between the client and the service
        final GSSContext context = manager.createContext((GSSCredential) null);
        final ClientPrincipalImpl principal = new ClientPrincipalImpl();
        principal.setClientTicket(forwardedTicket);
        try {
            // context.requestLifetime(60);
            context.requestCredDeleg(true);
            final byte[] tokenForClient = context.acceptSecContext(forwardedTicket, 0, forwardedTicket.length);
            principal.setResponseTicket(tokenForClient);

            final GSSName clientGSSName = context.getSrcName();
            final String clientName = clientGSSName.toString();
            final Subject clientSubject = new Subject();
            principal.setClientSubject(clientSubject);
            clientSubject.getPrincipals().add(new KerberosPrincipal(clientName));
            final Subject delegateSubject = new Subject();
            principal.setDelegateSubject(delegateSubject);
            if (context.getCredDelegState()) {
                final GSSCredential delegateCredential = context.getDelegCred();
                final GSSName delegateName = delegateCredential.getName();
                final Principal delegatePrincipal = new KerberosPrincipal(delegateName.toString());
                delegateSubject.getPrincipals().add(delegatePrincipal);
                delegateSubject.getPrivateCredentials().add(delegateCredential);
            } else {
                throw KerberosExceptionCodes.DELEGATE_FAILED.create(clientName);
            }
        } catch (GSSException e) {
            final byte[] forwardedTicket = this.forwardedTicket;
            Object ticketLog = new Object() {
                @Override
                public String toString() {
                    return Base64.encode(forwardedTicket);
                }
            };
            LOG.error("Unable to process delegated ticket: {}", ticketLog, e);
            throw KerberosExceptionCodes.COMM_FAILED.create(e, e.getMessage());
        } finally {
            context.dispose();
        }
//        KerberosUtils.logSubject(principal.getClientSubject());
//        KerberosUtils.logSubject(principal.getDelegateSubject());
        return principal;
    }
}
