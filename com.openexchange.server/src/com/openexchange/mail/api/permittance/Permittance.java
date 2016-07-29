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

package com.openexchange.mail.api.permittance;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.config.MailReloadable;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;


/**
 * {@link Permittance}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Permittance {

    private static volatile Integer accessPermits;
    private static int accessPermits() {
        Integer tmp = accessPermits;
        if (null == tmp) {
            synchronized (Permittance.class) {
                tmp = accessPermits;
                if (null == tmp) {
                    int defaultValue = 0;
                    ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    if (null == service) {
                        return defaultValue;
                    }
                    tmp = Integer.valueOf(service.getIntProperty("com.openexchange.mail.accessPermits", defaultValue));
                    accessPermits = tmp;
                }
            }
        }
        return tmp.intValue();
    }

    static {
        MailReloadable.getInstance().addReloadable(new Reloadable() {

            @SuppressWarnings("synthetic-access")
            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                accessPermits = null;
            }

            @Override
            public Interests getInterests() {
                return Reloadables.interestsForProperties("com.openexchange.mail.accessPermits");
            }
        });
    }

    // -----------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link Permittance}.
     */
    private Permittance() {
        super();
    }

    private static final ConcurrentMap<PermitterKey, PermitterImpl> SYNCHRONIZER = new ConcurrentHashMap<PermitterKey, PermitterImpl>(256, 0.9f, 1);

    /**
     * Acquires a permitter for given key
     *
     * @param accountId The account identifier
     * @param session The user session
     * @return The permitter instance
     */
    public static PermitterImpl acquireFor(int accountId, Session session) {
        int permits = accessPermits();
        return permits > 0 ? acquireFor0(PermitterKey.keyFor(accountId, session), permits) : null;
    }

    /**
     * Acquires a permitter for given key
     *
     * @param key The key
     * @return The permitter instance
     */
    public static PermitterImpl acquireFor(PermitterKey key) {
        int permits = accessPermits();
        return permits > 0 ? acquireFor0(key, permits) : null;
    }

    private static PermitterImpl acquireFor0(PermitterKey key, int permits) {
        PermitterImpl permitter = SYNCHRONIZER.get(key);
        if (null == permitter) {
            PermitterImpl newSemaphore = new PermitterImpl(permits, key);
            permitter = SYNCHRONIZER.putIfAbsent(key, newSemaphore);
            if (null == permitter) {
                permitter = newSemaphore;
            }
        }
        return permitter;
    }

    /**
     * Releases the permitter instance
     *
     * @param key The key
     */
    public static void release(Permitter permitter) {
        if (null != permitter) {
            releaseFor(permitter.getKey());
        }
    }

    /**
     * Releases the permitter instance associated with given key.
     *
     * @param key The permitter key
     */
    public static void releaseFor(PermitterKey key) {
        if (null != key) {
            SYNCHRONIZER.remove(key);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------------------------

    /**
     * A permitter.
     */
    private static final class PermitterImpl implements Permitter {

        private int permits;
        private int waits;
        private final PermitterKey key;

        PermitterImpl(int permits, PermitterKey key) {
            super();
            this.permits = permits;
            this.key = key;
        }

        @Override
        public PermitterKey getKey() {
            return key;
        }

        @Override
        public synchronized void acquire() throws OXException {
            while (permits <= 0) {
                waits++;
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw MailExceptionCode.INTERRUPT_ERROR.create(e, e.getMessage());
                } finally {
                    waits--;
                }
            }
            permits--;
        }

        @Override
        public synchronized boolean release() {
            permits++;
            this.notify();
            return waits <= 0;
        }

        @Override
        public synchronized int getWaits() {
            return waits;
        }
    }

}
