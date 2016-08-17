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

package com.openexchange.oauth.provider.authorizationserver.client;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;


/**
 * {@link Client} - Represents an OAuth client.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public interface Client extends Serializable {

    /**
     * Gets the clients public identifier
     *
     * @return The public identifier
     */
    String getId();

    /**
     * Gets the clients name
     *
     * @return The name
     */
    String getName();

    /**
     * Gets the clients description
     *
     * @return The description
     */
    String getDescription();

    /**
     * Gets the icon
     *
     * @return The icon
     */
    Icon getIcon();

    /**
     * Gets the date of this clients registration.
     *
     * @return The date
     */
    Date getRegistrationDate();

    /**
     * Gets the contact address
     *
     * @return The address
     */
    String getContactAddress();

    /**
     * Gets the website
     *
     * @return The website
     */
    String getWebsite();

    /**
     * Gets the default scope that should be applied when an authorization request does not
     * specify a certain scope.
     *
     * @return The default scope
     */
    Scope getDefaultScope();

    /**
     * Gets the clients secret identifier
     *
     * @return The secret identifier
     */
    String getSecret();

    /**
     * Gets the redirect URIs
     *
     * @return The URIs
     */
    List<String> getRedirectURIs();

    /**
     * Checks if given redirect URI is contained in registered redirect URIs for this client
     *
     * @param uri The URI to check
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    boolean hasRedirectURI(String uri);

    /**
     * Returns whether this client is enabled or not.
     *
     * @return <code>true</code> if the client is enabled
     */
    boolean isEnabled();

}
