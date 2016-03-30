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
