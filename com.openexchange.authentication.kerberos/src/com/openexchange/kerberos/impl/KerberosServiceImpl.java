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

import java.security.PrivilegedActionException;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import com.openexchange.exception.OXException;
import com.openexchange.kerberos.ClientPrincipal;
import com.openexchange.kerberos.KerberosExceptionCodes;
import com.openexchange.kerberos.KerberosService;

/**
 * {@link KerberosServiceImpl}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class KerberosServiceImpl implements KerberosService {

    private static final GSSManager MANAGER = GSSManager.getInstance();
    private final String moduleName;
    private final String userModuleName;
    private Subject serviceSubject;
    private LoginContext lc;

    public KerberosServiceImpl(String moduleName, String userModuleName) {
        super();
        this.moduleName = moduleName;
        this.userModuleName = userModuleName;
    }

    /*
     * The LoginContext is kept for the whole uptime of the backend. If the encapsulated ticket times out, a new ticket is fetched
     * automatically.
     */

    public void login() throws OXException {
        try {
            lc = new LoginContext(moduleName);
            // login (effectively populating the Subject)
            lc.login();
            // get the Subject that represents the service
            serviceSubject = lc.getSubject();
        } catch (javax.security.auth.login.LoginException e) {
            throw KerberosExceptionCodes.LOGIN_FAILED.create(e, e.getMessage());
        }
    }

    public void logout() throws OXException {
        try {
            lc.logout();
        } catch (LoginException e) {
            throw KerberosExceptionCodes.LOGOUT_FAILED.create(e, e.getMessage());
        } finally {
            lc = null;
        }
    }

    /**
     * This is a try to just verify the client ticket. The delegation ticket should be fetched in a second step. But due to bad described
     * API we are not able to separate those steps. It is completely unclear how a {@link GSSContext} should be initialized to do certain
     * steps in communication with the Kerberos server.
     *
     * @param ticket
     * @return
     * @throws OXException
     */
    public ClientPrincipal verifyTicket(byte[] ticket) throws OXException {
        final ClientTicketVerifier decoder = new ClientTicketVerifier(MANAGER, ticket);
        final ClientPrincipal principal;
        try {
            principal = Subject.doAs(serviceSubject, decoder);
        } catch (PrivilegedActionException e) {
            final Exception nested = e.getException();
            if (nested instanceof GSSException) {
                final GSSException ge = (GSSException) nested;
                throw KerberosExceptionCodes.TICKET_WRONG.create(ge, ge.getMessage());
            }
            throw KerberosExceptionCodes.UNKNOWN.create(e, e.getMessage());
        }
        return principal;
    }

    @Override
    public ClientPrincipal verifyAndDelegate(byte[] ticket) throws OXException {
        final ForwardedTGTDelegateGenerator generator = new ForwardedTGTDelegateGenerator(MANAGER, ticket);
        final ClientPrincipal principal;
        try {
            principal = Subject.doAs(serviceSubject, generator);
        } catch (PrivilegedActionException e) {
            final Exception nested = e.getException();
            if (nested instanceof OXException) {
                throw (OXException) nested;
            }
            if (nested instanceof GSSException) {
                final GSSException ge = (GSSException) nested;
                throw KerberosExceptionCodes.TICKET_WRONG.create(ge, ge.getMessage());
            }
            throw KerberosExceptionCodes.UNKNOWN.create(e, e.getMessage());
        }
        return principal;
    }

    @Override
    public ClientPrincipal authenticate(String username, String password) throws OXException {
        final ClientPrincipalImpl principal;
        Subject mysubject = new Subject();
        LoginContext userLc;
        try {
            userLc = new LoginContext(userModuleName, mysubject, new KerberosCallbackHandler(username, password));
            userLc.login();
            principal = new ClientPrincipalImpl();
            principal.setClientSubject(userLc.getSubject());
            principal.setDelegateSubject(userLc.getSubject());
        } catch (LoginException e) {
            // If an exception is caused here, it's likely the ~/.java.login.config file is wrong
            throw KerberosExceptionCodes.UNKNOWN.create(e, e.getMessage());
        }
        return principal;
    }

    /**
     * This is a try to just renew the delegation ticket. Just the delegation ticket should be renewed. But due to bad described
     * API we are not able to separate those steps. It is completely unclear how a {@link GSSContext} should be initialized to do certain
     * steps in communication with the Kerberos server.
     *
     * @param ticket
     * @return
     * @throws OXException
     */
    @Override
    public ClientPrincipal renewDelegateTicket(Subject subject) throws OXException {
        Set<GSSCredential> credentials = subject.getPrivateCredentials(GSSCredential.class);
        for (GSSCredential credential : credentials) {
            final DelegateTicketRenewer renewer = new DelegateTicketRenewer(MANAGER, credential);
            try {
                return Subject.doAs(subject, renewer);
            } catch (PrivilegedActionException e) {
                final Exception nested = e.getException();
                if (nested instanceof OXException) {
                    throw (OXException) nested;
                }
                if (nested instanceof GSSException) {
                    final GSSException ge = (GSSException) nested;
                    throw KerberosExceptionCodes.TICKET_WRONG.create(ge, ge.getMessage());
                }
                throw KerberosExceptionCodes.UNKNOWN.create(e, e.getMessage());
            }
        }
        return null;
    }
}
