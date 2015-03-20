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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.saml.state;

import java.util.List;
import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;


/**
 * The state management is used to assign authentication responses to previously generated requests
 * and to cache responses to check for replay attacks.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public interface StateManagement {

    /**
     * Adds an {@link AuthnRequestInfo} to the distributed state management.
     *
     * @param requestInfo The request info
     * @param ttl The time to live
     * @param timeUnit The time unit of <code>ttl</code>
     * @return The ID via which the stored request can be retrieved later on
     */
    String addAuthnRequestInfo(AuthnRequestInfo requestInfo, long ttl, TimeUnit timeUnit) throws OXException;

    /**
     * Gets an {@link AuthnRequestInfo} by its ID and removes it from the state management.
     *
     * @param id The ID
     * @return The request info or <code>null</code> if the ID is invalid or the time to live is already expired
     */
    AuthnRequestInfo removeAuthnRequestInfo(String id) throws OXException;

    void addAuthnResponse(String responseID, long timeout, TimeUnit timeUnit) throws OXException;

    boolean hasAuthnResponse(String responseID) throws OXException;

    List<String> removeSessionIds(List<String> keys);

    /**
     * Adds a {@link LogoutRequestInfo} to the distributed state management.
     *
     * @param requestInfo The request info
     * @param ttl The time to live
     * @param timeUnit The time unit of <code>ttl</code>
     * @return The ID via which the stored request can be retrieved later on
     */
    String addLogoutRequestInfo(LogoutRequestInfo requestInfo, long ttl, TimeUnit timeUnit) throws OXException;

    /**
     * Gets a {@link LogoutRequestInfo} by its ID and removes it from the state management.
     *
     * @param id The ID
     * @return The request info or <code>null</code> if the ID is invalid or the time to live is already expired
     */
    LogoutRequestInfo removeLogoutRequestInfo(String id) throws OXException;

}
