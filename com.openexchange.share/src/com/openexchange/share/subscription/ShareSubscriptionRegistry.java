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

package com.openexchange.share.subscription;

import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link ShareSubscriptionRegistry}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
@SingletonService
public interface ShareSubscriptionRegistry {

    /**
     * Analyzes the given link
     *
     * @param session The session representing the acting user
     * @param shareLink The link to a share or rather subscription
     * @return A result indicating the action that can be performed for the link
     * @throws OXException In case of an error
     */
    ShareLinkAnalyzeResult analyze(Session session, String shareLink) throws OXException;

    /**
     * Subscribes to a share represented by the link. If the share is unknown a new
     * account for the share will be mounted, too.
     *
     * @param session The user session
     * @param shareLink The share link to subscribe
     * @param shareName The name to set for the share, or <code>null</code> to use a default value
     * @param password The optional password for the share, can be <code>null</code>
     * @return The information about the mount
     * @throws OXException In case the share can't be subscribed
     */
    ShareSubscriptionInformation subscribe(Session session, String shareLink, String shareName, String password) throws OXException;

    /**
     * Updates a mounted object
     *
     * @param session The user session
     * @param shareLink The share link to identify the mounted object
     * @param shareName The optional name to set for the share, or <code>null</code> to keep the existing one
     * @param password The password to set for the object
     * @return The information about the mount
     * @throws OXException In case the underlying account can't be updated or rather remounted
     */
    ShareSubscriptionInformation resubscribe(Session session, String shareLink, String shareName, String password) throws OXException;

    /**
     * Unsubscribes a share. This however will not delete the underlying account.
     * <p>
     * To delete the underlying account use the account API.
     *
     * @param session The user session
     * @param shareLink The share link to identify the subscription
     * @throws OXException In case the unsubscribe fails
     */
    void unsubscribe(Session session, String shareLink) throws OXException;
}
