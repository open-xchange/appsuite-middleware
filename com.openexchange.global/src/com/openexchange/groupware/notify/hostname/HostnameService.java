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

package com.openexchange.groupware.notify.hostname;

import com.openexchange.osgi.annotation.SingletonService;

/**
 * An optional service providing the host name part in generated links to internal objects, e.g. for notifications:
 *
 * <pre>
 * http://[hostname]/[uiwebpath]#m=[module]&i=[object]&f=[folder]
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface HostnameService {

    /**
     * The key under which {@link HostData} instances are stored within generic parameter maps.
     *
     * @type <code>com.openexchange.groupware.notify.hostname.HostData</code>
     */
    public static final String PARAM_HOST_DATA = "com.openexchange.groupware.hostdata";

    /**
     * Returns the host name part used in generated links to internal objects; meaning the replacement for &quot;[hostname]&quot; in URL
     * template defined by property &quot;object_link&quot; in properties file &quot;notification.properties&quot;. Additionally this
     * service may be used for the host name when generating direct links into the UI.
     *
     * @param userId The user ID or a value less than/equal to zero if not available
     * @param contextId The context ID or a value less than/equal to zero if not available
     * @return The host name part used in generated links to internal objects or <code>null</code> (if user ID and/or context ID could not
     *         be resolved or any error occurred).
     */
    String getHostname(int userId, int contextId);

    /**
     * Returns the host name part used in generated links to internal objects for guest user accounts; meaning the replacement for
     * &quot;[hostname]&quot; in URL template defined by property &quot;object_link&quot; in properties file
     * &quot;notification.properties&quot;. Additionally this service may be used for the host name when generating direct links into
     * the UI.
     *
     * @param userId The user ID or a value less than/equal to zero if not available
     * @param contextId The context ID or a value less than/equal to zero if not available
     * @return The host name part used in generated links to internal objects or <code>null</code> (if user ID and/or context ID could not
     *         be resolved or any error occurred).
     */
    String getGuestHostname(int userId, int contextId);

}
