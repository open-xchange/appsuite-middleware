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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.CalendarUtils.getFields;
import static com.openexchange.chronos.common.CalendarUtils.getSearchTerm;
import static com.openexchange.chronos.impl.Utils.getFolder;
import static com.openexchange.chronos.impl.Utils.getFolderIdTerm;
import static com.openexchange.java.Autoboxing.I;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;

/**
 * {@link ForeignEventsPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ForeignEventsPerformer extends AbstractQueryPerformer {

    /**
     * Initializes a new {@link ForeignEventsPerformer}.
     *
     * @param session The calendar session
     * @param storage The underlying calendar storage
     */
    public ForeignEventsPerformer(CalendarSession session, CalendarStorage storage) {
        super(session, storage);
    }

    /**
     * Performs the operation.
     *
     * @param folderId The identifier of the folder to check the contained events creator's in
     */
    public boolean perform(String folderId) throws OXException {
        CalendarFolder folder = getFolder(session, folderId);
        SearchTerm<?> searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
            .addSearchTerm(getFolderIdTerm(session, folder))
            .addSearchTerm(getSearchTerm(EventField.CREATED_BY, SingleOperation.NOT_EQUALS, I(session.getUserId())))
        ;
        SearchOptions searchOptions = new SearchOptions().setLimits(0, 1);
        EventField[] fields = getFields(new EventField[0], EventField.CREATED_BY);
        return 0 < storage.getEventStorage().searchEvents(searchTerm, searchOptions, fields).size();
    }

}
