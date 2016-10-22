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

package com.openexchange.net.ssl.config;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link UserAwareSSLConfigurationService}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
@SingletonService
public interface UserAwareSSLConfigurationService {

    public static final String USER_CONFIG_ENABLED_PROPERTY = "com.openexchange.net.ssl.user.configuration.enabled";

    /**
     * Name of the user attribute the configuration made by the user is handled for.
     */
    public static final String USER_ATTRIBUTE_NAME = "acceptUntrustedCertificates";

    /**
     * Returns if the user is allowed to define the trust level for external connections.
     * 
     * @param userId The id of the user to check
     * @param contextId The id of the context the user is associated to
     * @return <code>true</code> if the user is allowed to define the trust level; otherwise <code>false</code>
     */
    boolean isAllowedToDefineTrustLevel(int userId, int contextId);

    /**
     * Returns if the given user configured to trust all external connections.
     * 
     * @see #setTrustAll(int, Context, boolean)
     * @param userId The id of the user to check
     * @param contextId The id of the context the user is associated to
     * @return <code>true</code> if the user has configured to trust all connections; otherwise <code>false</code>
     */
    boolean isTrustAll(int userId, int contextId);

    /**
     * Sets if the given user would like to trust all external connections without taking the server configuration into account.
     * 
     * @param userId The id of the user to check
     * @param contextId The context the user is associated to
     * @param trustAll <code>true</code> to set if the user would like to trust all connections; otherwise <code>false</code>
     */
    void setTrustAll(int userId, Context context, boolean trustAll);

}
