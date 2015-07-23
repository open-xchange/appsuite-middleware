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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.drive.share;

import java.util.List;
import java.util.Map;
import com.openexchange.drive.DriveSession;
import com.openexchange.exception.OXException;
import com.openexchange.share.recipient.ShareRecipient;


/**
 * {@link DriveShareService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface DriveShareService {

    /**
     * Gets all shares for a specific target.
     *
     * @param session The session
     * @param target The target to get the shares for
     * @return The shares, or an empty list if there are none
     */
    List<DriveShareInfo> getShares(DriveSession session, DriveShareTarget target) throws OXException;

    /**
     * Adds a share to a single target for a specific recipient. An appropriate guest user is created implicitly as needed.
     * <p/>
     * <b>Remarks:</b>
     * <ul>
     * <li>Associated permissions of the guest user on the share target are updated implicitly via corresponding target proxies
     * automatically</li>
     * <li>Permissions checks are performed implicitly during the update of the referenced target</li>
     * </ul>
     *
     * @param session The session
     * @param target The share target to add
     * @param recipient The recipient for the share
     * @param meta Additional metadata to store along with the created share(s), or <code>null</code> if not needed
     * @return The created share
     */
    DriveShareInfo addShare(DriveSession session, DriveShareTarget target, ShareRecipient recipient, Map<String, Object> meta) throws OXException;

}
