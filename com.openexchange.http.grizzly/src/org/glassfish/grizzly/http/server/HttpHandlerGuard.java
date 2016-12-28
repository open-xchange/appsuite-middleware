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
 * {@link HttpHandlerGuard} - A guard that controls the number of threads that are allowed to concurrently process a HTTP request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class HttpHandlerGuard {

    private final Semaphore semaphore;

    /**
     * Initializes a new {@link HttpHandlerGuard}.
     *
     * @param permits The initial number of permits available. Must not be equal to or less than zero.
     * @throws IllegalArgumentException If permits is negative or equal to zero
     */
    public HttpHandlerGuard(int permits) {
        super();
        if (permits <= 0) {
            throw new IllegalArgumentException();
        }
        semaphore = new Semaphore(permits);
    }

    /**
     * Acquires a permit from this guard, only if one is available at the time of invocation.
     * <p>
     * Acquires a permit, if one is available and returns immediately, with the value {@code true},
     * reducing the number of available permits by one.
     * <p>
     * If no permit is available then this method will return immediately with the value {@code false}.
     *
     * @return {@code true} if a permit was acquired and {@code false} otherwise
     */
    public boolean tryAcquire() {
        return semaphore.tryAcquire();
    }

    /**
     * Releases a permit, returning it to this guard.
     * <p>
     * Thus increasing the number of available permits by one.
     */
    public void release() {
        semaphore.release();
    }

}
