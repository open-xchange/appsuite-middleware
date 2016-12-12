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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.chronos.impl.Utils.getCalendarUser;
import static com.openexchange.chronos.impl.Utils.getFields;
import static com.openexchange.chronos.impl.Utils.getFolderIdTerm;
import static com.openexchange.chronos.impl.Utils.getSearchTerm;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.READ_ALL_OBJECTS;
import static com.openexchange.folderstorage.Permission.READ_FOLDER;
import static com.openexchange.folderstorage.Permission.READ_OWN_OBJECTS;
import static com.openexchange.java.Autoboxing.I;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ColumnFieldOperand;

/**
 * {@link ChangeExceptionsPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ChangeExceptionsPerformer extends AbstractQueryPerformer {

    /**
     * Initializes a new {@link ChangeExceptionsPerformer}.
     *
     * @param session The calendar session
     * @param storage The underlying calendar storage
     */
    public ChangeExceptionsPerformer(CalendarSession session, CalendarStorage storage) throws OXException {
        super(session, storage);
    }

    /**
     * Performs the operation.
     *
     * @param seriesID The series identifier to get the change exceptions for
     * @return The change exceptions
     */
    public List<Event> perform(UserizedFolder folder, int seriesID) throws OXException {
        /*
         * load series master first
         */
        Event event = storage.getEventStorage().loadEvent(seriesID, getFields(session));
        if (null == event) {
            throw CalendarExceptionCodes.EVENT_NOT_FOUND.create(I(seriesID));
        }
        if (session.getUser().getId() != event.getCreatedBy()) {
            requireCalendarPermission(folder, READ_FOLDER, READ_ALL_OBJECTS, NO_PERMISSIONS, NO_PERMISSIONS);
        } else {
            requireCalendarPermission(folder, READ_FOLDER, READ_OWN_OBJECTS, NO_PERMISSIONS, NO_PERMISSIONS);
        }
        /*
         * construct search term to lookup all change exceptions
         */
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
            .addSearchTerm(getFolderIdTerm(folder))
            .addSearchTerm(getSearchTerm(EventField.SERIES_ID, SingleOperation.EQUALS, I(seriesID)))
            .addSearchTerm(getSearchTerm(EventField.ID, SingleOperation.NOT_EQUALS, new ColumnFieldOperand<EventField>(EventField.SERIES_ID)))
        ;
        /*
         * perform search & filter the results based on user's access permissions
         */
        List<Event> changeExceptions = storage.getEventStorage().searchEvents(searchTerm, null, getFields(session));
        if (null == changeExceptions || 0 == changeExceptions.size()) {
            return Collections.emptyList();
        }
        readAdditionalEventData(changeExceptions, getCalendarUser(folder).getId(), getFields(session));
        for (Iterator<Event> iterator = changeExceptions.iterator(); iterator.hasNext();) {
            if (false == Utils.isInFolder(iterator.next(), folder)) {
                iterator.remove();
            }
        }
        return postProcess(changeExceptions, folder, true);
    }

}
