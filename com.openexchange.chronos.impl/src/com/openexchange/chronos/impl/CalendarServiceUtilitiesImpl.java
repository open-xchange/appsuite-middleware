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

package com.openexchange.chronos.impl;

import static com.openexchange.chronos.impl.Utils.getFolder;
import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.L;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.impl.performer.CountEventsPerformer;
import com.openexchange.chronos.impl.performer.ForeignEventsPerformer;
import com.openexchange.chronos.impl.performer.ResolveFilenamePerformer;
import com.openexchange.chronos.impl.performer.ResolveUidPerformer;
import com.openexchange.chronos.service.CalendarServiceUtilities;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.quota.Quota;

/**
 * {@link CalendarServiceUtilitiesImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarServiceUtilitiesImpl implements CalendarServiceUtilities {

    private static final CalendarServiceUtilities INSTANCE = new CalendarServiceUtilitiesImpl();

    /**
     * Gets the calendar service utilities instance.
     *
     * @return The calendar service utilities
     */
    public static CalendarServiceUtilities getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link CalendarServiceUtilitiesImpl}.
     */
    private CalendarServiceUtilitiesImpl() {
        super();
    }

    @Override
    public boolean containsForeignEvents(CalendarSession session, final String folderId) throws OXException {
        return new InternalCalendarStorageOperation<Boolean>(session) {

            @Override
            protected Boolean execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return B(new ForeignEventsPerformer(session, storage).perform(getFolder(session, folderId)));
            }
        }.executeQuery().booleanValue();
    }

    @Override
    public long countEvents(CalendarSession session, final String folderId) throws OXException {
        return new InternalCalendarStorageOperation<Long>(session) {

            @Override
            protected Long execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return L(new CountEventsPerformer(session, storage).perform(getFolder(session, folderId)));
            }
        }.executeQuery().intValue();
    }

    @Override
    public String resolveByUID(CalendarSession session, final String uid) throws OXException {
        return new InternalCalendarStorageOperation<String>(session) {

            @Override
            protected String execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new ResolveUidPerformer(storage).perform(uid);
            }
        }.executeQuery();
    }

    @Override
    public String resolveByFilename(CalendarSession session, final String filename) throws OXException {
        return new InternalCalendarStorageOperation<String>(session) {

            @Override
            protected String execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new ResolveFilenamePerformer(storage).perform(filename);
            }
        }.executeQuery();
    }

    @Override
    public Quota[] getQuotas(CalendarSession session) throws OXException {
        return new InternalCalendarStorageOperation<Quota[]>(session) {

            @Override
            protected Quota[] execute(CalendarSession session, CalendarStorage storage) throws OXException {
                return new Quota[] { Utils.getQuota(session, storage) };
            }
        }.executeQuery();
    }

    @Override
    public Event allocateAlarm(CalendarSession session, String alarmId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }
}
