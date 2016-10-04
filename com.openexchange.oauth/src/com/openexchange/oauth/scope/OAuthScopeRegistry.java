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

package com.openexchange.oauth.scope;

import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.API;

/**
 * {@link OAuthScopeRegistry} - Central access point for all available {@link OAuthScope}s for all supported
 * OAuth {@link API}s
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface OAuthScopeRegistry {

    /**
     * Registers the specified {@link OAuthScope} to the specified OAuth {@link API}
     * 
     * @param api The OAuth {@link API}
     * @param scope The {@link OAuthScope} to register
     */
    void registerScope(API api, OAuthScope scope);

    /**
     * Registers the specified {@link OAuthScope}s to the specified OAuth {@link API}
     * 
     * @param api The OAuth {@link API}
     * @param scopes The {@link OAuthScope}s to register
     */
    void registerScopes(API api, OAuthScope... scopes);

    /**
     * Unregisters the {@link OAuthScope} that is associated with the specified {@link OXScope} and OAuth {@link API}
     * 
     * @param api The OAuth {@link API}
     * @param module The {@link OXScope}
     */
    void unregisterScope(API api, OXScope module);

    /**
     * Unregisters all {@link OAuthScope}s that are associated with the specified OAuth {@link API}
     * 
     * @param api The OAuth {@link API}
     */
    void unregisterScopes(API api);

    /**
     * Purges the registry
     */
    void purge();

    /**
     * Returns an unmodifiable {@link Set} with all available {@link OAuthScope}s the specified OAuth {@link API} provides
     * 
     * @param api The OAuth {@link API} for which to get all available {@link OAuthScope}s
     * @return a unmodifiable {@link Set} with all available {@link OAuthScope}s
     * @throws OXException if there is no such OAuth {@link API} known to the registry
     */
    Set<OAuthScope> getAvailableScopes(API api) throws OXException;

    /**
     * <p>Returns an unmodifiable {@link Set} with all available legacy {@link OAuthScope}s of the specified OAuth {@link API}.</p>
     * <p>The legacy scopes include:</p>
     * <ul>
     * <li>{@link OXScope#drive}</li>
     * <li>{@link OXScope#calendar_ro}</li>
     * <li>{@link OXScope#contacts_ro}</li>
     * <li>{@link OXScope#generic}</li>
     * </ul>
     * 
     * @param api The OAuth {@link API} for which to get all available {@link OAuthScope}s
     * @return a unmodifiable {@link Set} with all available {@link OAuthScope}s
     * @throws OXException if there is no such OAuth {@link API} known to the registry
     */
    Set<OAuthScope> getLegacyScopes(API api) throws OXException;

    /**
     * Returns an unmodifiable {@link Set} with all available {@link OAuthScope}s that are associated with the specified
     * OAuth {@link API} and {@link OXScope}. If there is no {@link OAuthScope} associated with one of the specified
     * OAuth {@link API} and {@link OXScope}s, an {@link OXException} will be thrown
     * 
     * @param api The OAuth {@link API}
     * @param modules The {@link OXScope}s
     * @return An unmodifiable {@link Set} with all available {@link OAuthScope}s
     * @throws OXException if there is no {@link OAuthScope} associated with the specified OAuth {@link API} and {@link OXScope}
     */
    Set<OAuthScope> getAvailableScopes(API api, OXScope... modules) throws OXException;

    /**
     * Returns the {@link OAuthScope} associated with the specified {@link API} and {@link OXScope}
     * 
     * @param api The {@link API}
     * @param module The {@link OXScope}
     * @return the {@link OAuthScope} associated with the specified {@link API} and {@link OXScope}
     * @throws OXException if there is no {@link OAuthScope} associated with the specified OAuth {@link API} and {@link OXScope}
     */
    OAuthScope getScope(API api, OXScope module) throws OXException;
}
