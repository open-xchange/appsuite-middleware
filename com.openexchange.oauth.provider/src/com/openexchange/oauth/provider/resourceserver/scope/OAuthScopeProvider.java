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

package com.openexchange.oauth.provider.resourceserver.scope;

import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.exception.OXException;


/**
 * Provides information and validation methods for an OAuth scope token. Such a token can
 * be seen as a protection domain that includes a dedicated set of accessible modules and
 * actions. For every token that is defined on an action to prevent unauthorized access
 * a {@link OAuthScopeProvider} must be registered as OSGi service. For your convenience,
 * {@link AbstractScopeProvider} allows you to specify token and description and implement
 * only the {@link #canBeGranted(CapabilitySet)} method.
 *
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 * @see Scope
 * @see AbstractScopeProvider
 */
public interface OAuthScopeProvider {

    /**
     * Gets the scope token. Must be unique within the whole application Allowed characters are
     * %x21 / %x23-5B / %x5D-7E.
     *
     * @return The token
     */
    String getToken();

    /**
     * A localizable string that describes the impact of granting the denoted scope
     * to an external application. The string is shown to the user requesting OAuth
     * access.
     *
     * Example:
     * Application 'example' requires the following permissions:
     * - Read your contacts
     * - Create / modify appointments
     *
     * @return The description
     */
    String getDescription();

    /**
     * Checks whether the denoted scope can be granted for the passed session users
     * capabilities.
     *
     * @param capabilities The capabilities to check
     * @return <code>true</code> if the scope can be granted, <code>false</code> if not.
     * @throws OXException If an error occurs during the permission check.
     */
    boolean canBeGranted(CapabilitySet capabilities);

}
