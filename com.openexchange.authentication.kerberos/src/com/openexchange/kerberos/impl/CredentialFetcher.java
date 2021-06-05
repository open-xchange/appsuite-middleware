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
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.Oid;

/**
 * {@link CredentialFetcher}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class CredentialFetcher implements PrivilegedExceptionAction<GSSCredential> {

    private final GSSManager manager;

    public CredentialFetcher(GSSManager manager) {
        super();
        this.manager = manager;
    }

    @Override
    public GSSCredential run() throws GSSException {
        return manager.createCredential(null, GSSCredential.INDEFINITE_LIFETIME, getSPNEGOOid(), GSSCredential.ACCEPT_ONLY);
    }

    private static Oid getSPNEGOOid() throws GSSException {
        return new Oid("1.3.6.1.5.5.2");
    }
}
