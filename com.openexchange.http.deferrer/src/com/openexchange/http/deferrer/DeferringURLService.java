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

package com.openexchange.http.deferrer;

import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link DeferringURLService} - The service to create a deferring URL.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface DeferringURLService {

    /**
     * Generates a deferred URL for specified URL. Useful for a multi-domain setup to allow certain operations to jump to an extra step in a
     * singular domain; e.g. certain OAuth provider require a single domain for call-back actions.
     * <p>
     * If a single domain is configured through <code>com.openexchange.http.deferrer.url</code> property (<i>deferrer.properties</i>), the
     * resulting URL looks like:
     * <p>
     * &lt;deferrer-url&gt; + <code>"ajax/defer?redirect="</code> + <i>URLENC</i>(&lt;url&gt;)
     * <p>
     * If no such property is set, passed URL is returned unchanged
     *
     * @param url The URL to defer
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The deferred URL
     */
    String getDeferredURL(String url, int userId, int contextId);

    /**
     * Generates a deferred URL for specified URL using given <code>domain</code>.
     *
     * @param url The URL to defer
     * @param domain The singular domain to use
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The deferred URL
     */
    String deferredURLUsing(String url, String domain, int userId, int contextId);

    /**
     * Gets the basic defer URL.
     * <p>
     * If a single domain is configured through <code>com.openexchange.http.deferrer.url</code> property (<i>deferrer.properties</i>), the
     * resulting basic URL looks like:
     * <p>
     * &lt;deferrer-url&gt; + <code>"ajax/defer"</code>
     * <p>
     * If no such property is set, return value is a relative one according to <code>"ajax/defer"</code>.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The basic defer URL
     */
    String getBasicDeferrerURL(int userId, int contextId);

    /**
     * Performs a check if passed URL seems to be deferred.
     *
     * @param url The URL to check if deferred
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if deferred; otherwise <code>false</code>
     */
    boolean seemsDeferred(String url, int userId, int contextId);

    /**
     * Signals if a deferred URL is available.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if a deferred URL is available; otherwise <code>false</code>
     */
    boolean isDeferrerURLAvailable(int userId, int contextId);

}
