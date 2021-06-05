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

import static com.openexchange.kerberos.KerberosUtils.getKerberosMechanism;
import java.security.Principal;
import java.security.PrivilegedExceptionAction;
import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import com.openexchange.exception.OXException;
import com.openexchange.kerberos.KerberosExceptionCodes;

/**
 * Creates a delegation ticket for the client to contact a backend service.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
final class ConstrainedDelegateGenerator implements PrivilegedExceptionAction<Subject> {

    private final GSSManager manager;
    private final GSSName clientName;
    private final GSSName backendService;

    public ConstrainedDelegateGenerator(GSSManager manager, GSSName clientName, GSSName backendService) {
        super();
        this.manager = manager;
        this.clientName = clientName;
        this.backendService = backendService;
    }

    @Override
    public Subject run() throws GSSException, OXException {
        GSSCredential clientCred = manager.createCredential(
            clientName,
            GSSContext.DEFAULT_LIFETIME,
            getKerberosMechanism(),
            GSSCredential.INITIATE_ONLY);
        GSSContext context = manager.createContext(backendService, getKerberosMechanism(), clientCred, GSSContext.DEFAULT_LIFETIME);
        // create a security context between the client and the service
        final Subject delegateSubject;
        /* final byte[] serviceTicket; */
        try {
            context.requestMutualAuth(true);
            context.requestCredDeleg(true);
            /* serviceTicket = */context.initSecContext(new byte[0], 0, 0);

            delegateSubject = new Subject();
            if (context.getCredDelegState()) {
                final GSSCredential delegateCredential = context.getDelegCred();
                final GSSName delegateGSSName = delegateCredential.getName();
                final Principal delegatePrincipal = new KerberosPrincipal(delegateGSSName.toString());
                delegateSubject.getPrincipals().add(delegatePrincipal);
                delegateSubject.getPrivateCredentials().add(delegateCredential);
            } else {
                throw KerberosExceptionCodes.DELEGATE_FAILED.create(clientName);
            }
        } catch (GSSException e) {
            throw KerberosExceptionCodes.COMM_FAILED.create(e, e.getMessage());
        } finally {
            context.dispose();
        }
        return delegateSubject;
    }
}
