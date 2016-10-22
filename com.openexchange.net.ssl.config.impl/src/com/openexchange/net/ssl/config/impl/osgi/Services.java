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

package com.openexchange.net.ssl.config.impl.osgi;

import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.server.ServiceLookup;

/**
 * {@link Services} - Provides static access to tracked services.
 *
 * @since v7.8.3
 */
public class Services {

    /**
     * Initializes a new {@link Services}.
     */
    private Services() {
        super();
    }

    private static final AtomicReference<ServiceLookup> REF = new AtomicReference<>();

    /**
     * Sets the service lookup.
     *
     * @param serviceLookup The service lookup or <code>null</code>
     */
    public static void setServiceLookup(final ServiceLookup serviceLookup) {
        REF.set(serviceLookup);
    }

    /**
     * Gets the service lookup.
     *
     * @return The service lookup or <code>null</code>
     */
    public static ServiceLookup getServiceLookup() {
        return REF.get();
    }

    /**
     * Gets the service of specified type
     *
     * @param clazz The service's class
     * @return The service
     * @throws IllegalStateException If an error occurs while returning the demanded service
     */
    public static <S extends Object> S getService(final Class<? extends S> clazz) {
        final com.openexchange.server.ServiceLookup serviceLookup = REF.get();
        if (null == serviceLookup) {
            throw new IllegalStateException("Missing ServiceLookup instance. Bundle \"com.openexchange.net.ssl.config.impl\" not started?");
        }
        return serviceLookup.getService(clazz);
    }

    /**
     * (Optionally) Gets the service of specified type
     *
     * @param clazz The service's class
     * @return The service or <code>null</code> if absent
     */
    public static <S extends Object> S optService(final Class<? extends S> clazz) {
        try {
            return getService(clazz);
        } catch (final IllegalStateException e) {
            return null;
        }
    }

}
