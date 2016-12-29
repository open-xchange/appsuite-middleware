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

package org.glassfish.grizzly.http.server;

import java.util.concurrent.Semaphore;

/**
 * {@link HttpRequestGuard} - A guard that controls the number of threads that are allowed to concurrently process a HTTP request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public abstract class HttpRequestGuard {

    /** The effectively no-op HTTP request guard */
    public static final HttpRequestGuard NO_GUARD = new HttpRequestGuard() {

        @Override
        public boolean mayHandle(Request newRequest) {
            return true;
        }

        @Override
        public String toString() {
            return "allow all";
        }
    };

    /**
     * Gets the appropriate guard for specified initial permits.
     *
     * @param permits The initial permits
     * @return The guard
     */
    public static HttpRequestGuard guardFor(int permits) {
        return permits > 0 ? new SemaphoreHttpRequestGuard(permits) : NO_GUARD;
    }

    // -----------------------------------------------------------------------------------------------------------


    /**
     * Initializes a new {@link HttpRequestGuard}.
     *
     * @param permits The initial number of permits available. Must not be equal to or less than zero.
     * @throws IllegalArgumentException If permits is negative or equal to zero
     */
    protected HttpRequestGuard() {
        super();
    }

    /**
     * Checks if this guard allows that the specified <i>new</i> HTTP request should be handled.
     *
     * @return <code>true</code> if given HTTP request is allowed being handled and <code>false</code> otherwise
     */
    public abstract boolean mayHandle(Request newRequest);

    // -----------------------------------------------------------------------------------------------------------

    private static final class SemaphoreHttpRequestGuard extends HttpRequestGuard {

        private final Semaphore semaphore;
        private final AfterServiceListener afterServiceListener;

        /**
         * Initializes a new {@link SemaphoreHttpRequestGuard}.
         *
         * @param permits The initial number of permits available. Must not be equal to or less than zero.
         * @throws IllegalArgumentException If permits is negative or equal to zero
         */
        SemaphoreHttpRequestGuard(int permits) {
            super();
            if (permits <= 0) {
                throw new IllegalArgumentException();
            }

            final Semaphore semaphore = new Semaphore(permits);
            this.semaphore = semaphore;
            afterServiceListener = new AfterServiceListener() {

                @Override
                public void onAfterService(Request request) {
                    semaphore.release();
                }
            };
        }

        @Override
        public boolean mayHandle(Request newRequest) {
            boolean allowed = semaphore.tryAcquire();
            if (allowed) {
                newRequest.addAfterServiceListener(afterServiceListener);
            }
            return allowed;
        }

        @Override
        public String toString() {
            return semaphore.toString();
        }
    }

}
