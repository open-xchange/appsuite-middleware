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

package com.openexchange.kerberos;

import java.security.Principal;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.kerberos.KerberosTicket;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;
import com.openexchange.exception.OXException;

/**
 * Contains helpful methods for handling Kerberos ticket stuff.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class KerberosUtils {

    /**
     * Renew tickets 1 minute before they expire.
     */
    private static final int MIN_LIFETIME_SECONDS = 1 * 60;

    public static final String SESSION_SUBJECT = "kerberosSubject";
    public static final String SESSION_PRINCIPAL = "kerberosPrincipal";

    /**
     * This event is emitted if a session gets a new Kerberos ticket. This happens if a session is migrated between backend hosts or if the
     * ticket lifetime expired.
     */
    public static final String TOPIC_TICKET_READDED = "com/openexchange/authentication/kerberos/ticket/readded";

    public static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(KerberosUtils.class);

    public static CallbackHandler getCallbackHandler(final String user, final String password) {
        return new CallbackHandler() {
            @Override
            public void handle(final Callback[] callbacks) {
                for (Callback callback : callbacks) {
                    if (callback instanceof NameCallback && null != user ) {
                        final NameCallback nameCallback = (NameCallback) callback;
                        nameCallback.setName(user);
                    } else if (callback instanceof PasswordCallback && null != password ) {
                        final PasswordCallback passCallback = (PasswordCallback) callback;
                        passCallback.setPassword(password.toCharArray());
                    } else {
                        LOG.error("Unknown callback class: {}#{}", callback.getClass().getName(), callback);
                    }
                }
            }
        };
    }

    /**
     * Calculate renewal time for ticket in given subject.
     *
     * @param subject Subject containing some private credentials.
     * @return the time in seconds when the ticket needs a renewal.
     * @throws GSSException if fetching the remaining life time from the ticket fails.
     * @throws OXException if the subject does not contain any tickets.
     */
    public static int calculateRenewalTime(Subject subject) throws OXException {
        final Set<GSSCredential> credentials = subject.getPrivateCredentials(GSSCredential.class);
        for (GSSCredential credential : credentials) {
            int remaining;
            try {
                remaining = credential.getRemainingLifetime();
            } catch (GSSException e) {
                throw KerberosExceptionCodes.UNKNOWN.create(e, e.getMessage());
            }
            // if lifetime to small, renew NOW
            if (remaining <= MIN_LIFETIME_SECONDS) {
                return 0;
            }
            return remaining - MIN_LIFETIME_SECONDS;
        }
        final Set<KerberosTicket> tickets = subject.getPrivateCredentials(KerberosTicket.class);
        for (KerberosTicket ticket : tickets) {
            int remaining = (int) ((ticket.getEndTime().getTime() - System.currentTimeMillis()) / 1000);
            if (remaining <= MIN_LIFETIME_SECONDS) {
                return 0;
            }
            return remaining - MIN_LIFETIME_SECONDS;
        }
        throw KerberosExceptionCodes.NO_CREDENTIALS.create(getName(subject));
    }

    public static void logSubject(Subject subject) {
        StringBuilder sb = new StringBuilder();
        sb.append("Subject:\n");
        sb.append("\nPrincipals:\n");
        for (final Principal principal : subject.getPrincipals()) {
            sb.append(principal.getClass().getName());
            sb.append(':');
            sb.append(principal.toString());
            sb.append('\n');
        }
        sb.append("Public credentials:\n");
        for (final Object obj : subject.getPublicCredentials()) {
            sb.append(obj.getClass().getName());
            sb.append(':');
            sb.append(obj.toString());
            sb.append('\n');
        }
        sb.append("Private credentials:\n");
        for (Object obj : subject.getPrivateCredentials()) {
            sb.append(obj.getClass().getName());
            sb.append(':');
            sb.append(obj.toString());
            sb.append('\n');
        }
        LOG.info(sb.toString());
    }

    public static void disposeSubject(Subject subject) {
        if (null != subject) {
            for (final GSSCredential credential : subject.getPrivateCredentials(GSSCredential.class)) {
                try {
                    credential.dispose();
                } catch (GSSException e) {
                    LOG.error("", e);
                }
            }
        }
    }

    public static Oid getKerberosName() throws GSSException {
        return new Oid("1.2.840.113554.1.2.2.1");
    }

    public static Oid getKerberosMechanism() throws GSSException {
        return new Oid("1.2.840.113554.1.2.2");
    }

    public static final Principal getFirst(Set<Principal> principals) {
        Principal[] array = principals.toArray(new Principal[principals.size()]);
        return array[0];
    }

    public static final String getName(Subject subject) {
        return getFirst(subject.getPrincipals()).getName();
    }

    private KerberosUtils() {
        super();
    }
}
