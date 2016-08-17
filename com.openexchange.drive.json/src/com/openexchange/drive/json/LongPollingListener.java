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

package com.openexchange.drive.json;

import java.util.List;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.drive.DriveSession;
import com.openexchange.drive.events.DriveEvent;
import com.openexchange.exception.OXException;

/**
 * {@link LongPollingListener}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface LongPollingListener {

    /**
     * Waits for a result carrying the drive actions resulting from an occurred event for the polling client.
     *
     * @param timeout The maximum timeout in milliseconds to wait for the result
     * @return The result
     * @throws OXException
     */
    AJAXRequestResult await(long timeout) throws OXException;

    /**
     * Notifies the listener about an incoming drive event.
     *
     * @param event The drive event
     */
    void onEvent(DriveEvent event);

    /**
     * Gets the drive session associated with this listener.
     *
     * @return The drive session
     */
    DriveSession getSession();

    /**
     * Gets a value indicating whether this listener's push token matches the supplied token value, trying to match either the token
     * itself or the md5 checksum of the token.
     *
     * @param tokenRef The push token reference to match
     * @return <code>true</code> if this listener's token or the md5 checksum of this listener's token matches, <code>false</code>,
     *         otherwise
     */
    boolean matches(String tokenRef);

    /**
     * Gets the root folder identifiers monitored by this listener.
     *
     * @return The root folder identifiers
     */
    List<String> getRootFolderIDs();

}
