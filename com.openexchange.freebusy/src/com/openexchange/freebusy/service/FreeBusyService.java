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

package com.openexchange.freebusy.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.freebusy.FreeBusyData;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link FreeBusyService}
 *
 * The free/busy service.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@SingletonService
public interface FreeBusyService {

    /**
     * Gets the available free/busy data for a list of participants. The data is pre-processed and sorted by time, so that any
     * overlapping intervals each of the participants' free/busy data are merged implicitly to the most conflicting busy times.
     *
     * @param session The session
     * @param participants A list of participants, identified either by their internal user-/group/-resource-ID or e-mail address.
     * @param from The lower (inclusive) limit of the requested time-range
     * @param until The upper (exclusive) limit of the requested time-range
     * @return A map of free/busy data, with each entry representing the free/busy data of one requested participant
     * @throws OXException
     */
    Map<String, FreeBusyData> getMergedFreeBusy(Session session, List<String> participants, Date from, Date until) throws OXException;

    /**
     * Gets the available free/busy data for a list of participants.
     *
     * @param session The session
     * @param participants A list of participants, identified either by their internal user-/group-/resource-ID or e-mail address.
     * @param from The lower (inclusive) limit of the requested time-range
     * @param until The upper (exclusive) limit of the requested time-range
     * @return A map of free/busy data, with each entry representing the free/busy data of one requested participant
     * @throws OXException
     */
    Map<String, FreeBusyData> getFreeBusy(Session session, List<String> participants, Date from, Date until) throws OXException;

    /**
     * Gets the available free/busy data for a participant. The data is pre-processed and sorted by time, so that any
     * overlapping intervals in the participants' free/busy data are merged implicitly to the most conflicting busy times.
     *
     * @param session The session
     * @param participant A participant, identified either by its internal user-/group-/resource-ID or e-mail address.
     * @param from The lower (inclusive) limit of the requested time-range
     * @param until The upper (exclusive) limit of the requested time-range
     * @return The free/busy data
     * @throws OXException
     */
    FreeBusyData getMergedFreeBusy(Session session, String participant, Date from, Date until) throws OXException;

    /**
     * Gets the available free/busy data for a participant.
     *
     * @param session The session
     * @param participant A participant, identified either by its internal user-/group-/resource-ID or e-mail address.
     * @param from The lower (inclusive) limit of the requested time-range
     * @param until The upper (exclusive) limit of the requested time-range
     * @return The free/busy data
     * @throws OXException
     */
    FreeBusyData getFreeBusy(Session session, String participant, Date from, Date until) throws OXException;

}
