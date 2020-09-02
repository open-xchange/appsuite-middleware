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

import com.openexchange.annotation.NonNull;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.Ranked;
import com.openexchange.session.Session;

/**
 * {@link ShareSubscriptionProvider} - A provider that handles CRUD operations for s single module and a certain kind of share
 * 
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public interface ShareSubscriptionProvider extends Ranked {

    /** The ranking for the remote OX to OX file storage */
    final static int XOX_REMOTE_RANK = 55;

    /**
     * Gets a value indicating whether e.g. the given combination of module,
     * folder and item extracted from the share link is supported by this provider.
     *
     * @param shareLink The share link to support
     * @return <code>true</code> in case this provider can be used for subsequent calls, <code>false</code> if not
     */
    boolean isSupported(String shareLink);

    /**
     * Get the unique identifier of the manager
     *
     * @return The unique ID
     */
    @NonNull
    String getId();

    /**
     * Analyzes the given share link
     *
     * @param session The session representing the acting user
     * @param shareLink The share link to access
     * @return A result indicating the action that can be performed, or <code>null</code> if not applicable
     * @throws OXException In case of an error
     */
    ShareLinkAnalyzeResult analyze(Session session, String shareLink) throws OXException;

    /**
     * Mounts a share represented by the link efficiently subscribing the share
     *
     * @param session The user session to bind the share to
     * @param shareLink The share link to mount
     * @param shareName The name to set for the share to mount
     * @param password The optional password for the share
     * @return The information about the mount
     * @throws OXException In case of error
     */
    ShareSubscriptionInformation mount(Session session, String shareLink, String shareName, String password) throws OXException;

    /**
     * Updates a mounted object that currently can't be used
     *
     * @param session The user session
     * @param shareLink The share link to identify the mounted object
     * @param shareName The optional name to set for the share
     * @param password The password to set for the object
     * @return The information about the mount
     * @throws OXException In case of error
     */
    ShareSubscriptionInformation remount(Session session, String shareLink, String shareName, String password) throws OXException;

    /**
     * Unmouts a share or rather deactivates the subscription
     *
     * @param session The user session
     * @param shareLink The share link to identify the mounted object
     * @throws OXException In case of error
     */
    void unmount(Session session, String shareLink) throws OXException;

}
