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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.service;

import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.quota.Quota;

/**
 * {@link CalendarServiceUtilities}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
@SingletonService
public interface CalendarServiceUtilities {

    /**
     * Resolves an UID to the identifier of an existing event. The lookup is performed case-sensitive and context-wise, independently of
     * the current session user's access rights. If an event series with change exceptions is matched, the identifier of the recurring
     * <i>master</i> event is returned.
     *
     * @param session The calendar session
     * @param uid The UID to resolve
     * @return The identifier of the resolved event, or <code>null</code> if not found
     */
    String resolveByUID(CalendarSession session, String uid) throws OXException;

    /**
     * Resolves a resource filename to the identifier of an existing event. The lookup is performed context-wise, independently of the
     * current session user's access rights. If an event series with change exceptions is matched, the identifier of the recurring
     * <i>master</i> event is returned.
     *
     * @param session The calendar session
     * @param filename The filename to resolve
     * @return The identifier of the resolved event, or <code>null</code> if not found
     */
    String resolveByFilename(CalendarSession session, String filename) throws OXException;

    /**
     * Gets a value indicating whether a specific folder contains events that were not created by the current session's user.
     *
     * @param session The calendar session
     * @param folderId The identifier of the folder to check the contained events in
     * @return <code>true</code> if there's at least one event located in the folder that is not created by the user, <code>false</code>, otherwise
     */
    boolean containsForeignEvents(CalendarSession session, String folderId) throws OXException;

    /**
     * Gets the number of events in a folder, which includes the sum of all non-recurring events, the series master events, and the
     * overridden exceptional occurrences from event series. Distinct object access permissions (e.g. <i>read own</i>) are not considered.
     *
     * @param session The calendar session
     * @param folderId The identifier of the folder to count the events in
     * @return The number of events contained in the folder, or <code>0</code> if there are none
     */
    long countEvents(CalendarSession session, String folderId) throws OXException;

    /**
     * Get the configured quotas and their actual usages of the underlying calendar account.
     *
     * @param session The calendar session
     * @return The configured quotas and the actual usages
     */
    Quota[] getQuotas(CalendarSession session) throws OXException;

}
