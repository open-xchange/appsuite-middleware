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
import com.openexchange.exception.OXException;
import com.openexchange.kerberos.ClientPrincipal;
import com.openexchange.kerberos.KerberosExceptionCodes;

/**
 * Creates a delegation ticket for the client to contact a backend service.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
final class DelegateTicketRenewer implements PrivilegedExceptionAction<ClientPrincipal> {

    private final GSSManager manager;
    private final GSSCredential credential;

    public DelegateTicketRenewer(GSSManager manager, GSSCredential credential) {
        super();
        this.manager = manager;
        this.credential = credential;
    }

    @Override
    public ClientPrincipal run() throws GSSException, OXException {
        // create a security context between the client and the service
        final GSSContext context = manager.createContext(credential);
        ClientPrincipalImpl principal = new ClientPrincipalImpl();
        try {
            final byte[] empty = new byte[0];
            // context.requestLifetime(60);
            context.requestCredDeleg(true);
            final byte[] tokenForClient = context.acceptSecContext(empty, 0, empty.length);

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
            throw KerberosExceptionCodes.COMM_FAILED.create(e, e.getMessage());
        } finally {
            context.dispose();
        }
        return principal;
    }
}
