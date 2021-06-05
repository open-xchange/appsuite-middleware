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

package com.openexchange.kerberos.session;

import static com.openexchange.kerberos.KerberosUtils.SESSION_PRINCIPAL;
import static com.openexchange.kerberos.KerberosUtils.SESSION_SUBJECT;
import com.openexchange.exception.OXException;
import com.openexchange.kerberos.ClientPrincipal;
import com.openexchange.kerberos.KerberosService;
import com.openexchange.session.Session;
import com.openexchange.session.SessionSerializationInterceptor;

/**
 * Fetches a new TGT for migrated sessions that have been created using login and password.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @since 7.6.0
 */
public final class KerberosSessionSerialization implements SessionSerializationInterceptor {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(KerberosSessionSerialization.class);

    private final KerberosService kerberosService;

    public KerberosSessionSerialization(KerberosService kerberosService) {
        super();
        this.kerberosService = kerberosService;
    }

    @Override
    public void serialize(Session session) {
        // Nothing to do.
    }

    @Override
    public void deserialize(Session session) {
        if (!"".equals(session.getPassword())) {
            // Sessions created with a forwarded TGT from the client do not contain any password.
            try {
                ClientPrincipal principal = kerberosService.authenticate(session.getLogin(), session.getPassword());
                session.setParameter(SESSION_SUBJECT, principal.getDelegateSubject());
                session.setParameter(SESSION_PRINCIPAL, principal);
            } catch (OXException e) {
                LOG.error("Session migration for session created with login and password failed.", e);
            }
        }
    }
}
