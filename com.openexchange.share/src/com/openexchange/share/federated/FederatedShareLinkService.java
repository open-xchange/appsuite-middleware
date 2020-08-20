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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.share.federated;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link FederatedShareLinkService}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public interface FederatedShareLinkService {

    /**
     * Analyzes the given share link
     *
     * @param session The session representing the acting user
     * @param shareLink The share link to access
     * @return A result indicating the action that can be performed, or <code>null</code> if not applicable
     * @throws OXException In case of an error
     */
    ShareLinkAnalyzeResult analyzeLink(Session session, String shareLink) throws OXException;

    /**
     * Binds a share link to the user by e.g. creating a storage account for the share
     *
     * @param session The user session to bind the share to
     * @param serviceId The service ID of the service that shall bind the link
     * @param shareLink The share link to add or rather bind
     * @param password The optional password for the share
     * @param shareName The name to set for the binded object
     * @return The ID of the created object
     * @throws OXException In case of error
     */
    String bindShare(Session session, String serviceId, String shareLink, String password, String shareName) throws OXException;

    /**
     * Updates a bound share link
     *
     * @param session The user session
     * @param serviceId The service ID of the service that shall bind the link
     * @param shareLink The share link to identify the bound object
     * @param password The password
     * @throws OXException In case of error
     */
    void update(Session session, String serviceId,String shareLink, String password) throws OXException;

    /**
     * Unbinds a share and the associated resources of the share.
     *
     * @param session The user session
     * @param serviceId The service ID of the service that shall bind the link
     * @param shareLink The share link to remove
     * @throws OXException In case of error
     */
    void unbindShare(Session session, String serviceId,String shareLink) throws OXException;
}
