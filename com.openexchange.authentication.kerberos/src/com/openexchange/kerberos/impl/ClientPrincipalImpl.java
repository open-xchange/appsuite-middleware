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

import javax.security.auth.Subject;
import com.openexchange.kerberos.ClientPrincipal;
import com.openexchange.kerberos.KerberosUtils;

/**
 * {@link ClientPrincipalImpl}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class ClientPrincipalImpl implements ClientPrincipal {

    private static final long serialVersionUID = 4408781422388672382L;

    private Subject clientSubject;
    private Subject delegateSubject;
    private byte[] clientTicket;
    private byte[] responseTicket;

    public ClientPrincipalImpl() {
        super();
    }

    void setClientTicket(byte[] clientTicket) {
        this.clientTicket = clientTicket;
    }

    void setResponseTicket(byte[] responseTicket) {
        this.responseTicket = responseTicket;
    }

    void setClientSubject(Subject clientSubject) {
        this.clientSubject = clientSubject;
    }

    void setDelegateSubject(Subject delegateSubject) {
        this.delegateSubject = delegateSubject;
    }

    @Override
    public String getName() {
        return KerberosUtils.getFirst(clientSubject.getPrincipals()).getName();
    }

    @Override
    public Subject getDelegateSubject() {
        return delegateSubject;
    }

    @Override
    public byte[] getResponseTicket() {
        return responseTicket;
    }

    @Override
    public Subject getClientSubject() {
        return clientSubject;
    }

    @Override
    public byte[] getClientTicket() {
        return clientTicket;
    }

    @Override
    public boolean isSPNEGO() {
        return null != clientTicket;
    }
}
