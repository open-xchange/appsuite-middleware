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

package com.openexchange.chronos.storage.rdb.legacy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.chronos.Conference;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.ConferenceStorage;
import com.openexchange.chronos.storage.rdb.RdbStorage;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public class RdbConferenceStorage extends RdbStorage implements ConferenceStorage {

    /**
     * Initializes a new {@link RdbConferenceStorage}.
     *
     * @param context The context
     * @param dbProvider The database provider to use
     * @param txPolicy The transaction policy
     */
    public RdbConferenceStorage(Context context, DBProvider dbProvider, DBTransactionPolicy txPolicy) {
        super(context, dbProvider, txPolicy);
    }

    @Override
    public int nextId() throws OXException {
        return 0; // no unique identifiers required
    }

    @Override
    public List<Conference> loadConferences(String eventId) throws OXException {
        return Collections.emptyList();
    }

    @Override
    public Map<String, List<Conference>> loadConferences(String[] eventIds) throws OXException {
        return Collections.emptyMap();
    }

    @Override
    public Set<String> hasConferences(String[] eventIds) throws OXException {
        return Collections.emptySet();
    }

    @Override
    public void deleteConferences(String eventId) throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    @Override
    public void deleteConferences(List<String> eventIds) throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    @Override
    public void deleteConferences(String eventId, int[] conferencesIds) throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    @Override
    public boolean deleteAllConferences() throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    @Override
    public void insertConferences(String eventId, List<Conference> conferences) throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    @Override
    public void updateConferences(String eventId, List<Conference> conferences) throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

}
