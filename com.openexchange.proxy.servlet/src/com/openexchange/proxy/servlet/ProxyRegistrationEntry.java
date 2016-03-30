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

package com.openexchange.proxy.servlet;

import com.openexchange.proxy.ProxyRegistration;

/**
 * {@link ProxyRegistrationEntry} - A registration entry which wraps a {@link ProxyRegistration registration} and provides registration time
 * stamp and time-to-live.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ProxyRegistrationEntry {

    private final ProxyRegistration proxyRegistration;

    private final long ttl;

    private final long timestamp;

    /**
     * Initializes a new {@link ProxyRegistrationEntry} bound to session.
     *
     * @param proxyRegistration The proxy registration
     */
    public ProxyRegistrationEntry(final ProxyRegistration proxyRegistration) {
        this(proxyRegistration, -1L);
    }

    /**
     * Initializes a new {@link ProxyRegistrationEntry} with given time-to-live.
     *
     * @param proxyRegistration The proxy registration
     * @param ttl The time-to-live value; a negative value means bound to session life time.
     */
    public ProxyRegistrationEntry(final ProxyRegistration proxyRegistration, final long ttl) {
        super();
        this.ttl = ttl;
        this.proxyRegistration = proxyRegistration;
        timestamp = System.currentTimeMillis();
    }

    /**
     * Gets the time-to-live for this entry. A negative value means this entry is bound to associated session's life time.
     *
     * @return The time-to-live or a negative value
     */
    public long getTTL() {
        return ttl;
    }

    /**
     * Gets the proxy registration
     *
     * @return The proxy registration
     */
    public ProxyRegistration getProxyRegistration() {
        return proxyRegistration;
    }

    /**
     * Gets the time stamp when this entry was registered.
     *
     * @return The register time stamp
     */
    public long getTimestamp() {
        return timestamp;
    }

}
