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

import java.sql.Connection;
import com.openexchange.chronos.CalendarSession;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;

/**
 * {@link ReadOperation}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class ReadOperation<T> {

    private final CalendarSession session;

    /**
     * Initializes a new {@link ReadOperation}.
     *
     * @param session The server session
     */
    public ReadOperation(CalendarSession session) {
        super();
        this.session = session;
    }

    /**
     * Executes the storage operation in a transaction.
     *
     * @return The result
     */
    public T execute() throws OXException {
        DatabaseService dbService = Services.getService(DatabaseService.class);
        CalendarStorageFactory storageFactory = Services.getService(CalendarStorageFactory.class);
        Connection readConnection = null;
        try {
            readConnection = dbService.getReadOnly(session.getContext());
            SimpleDBProvider dbProvider = new SimpleDBProvider(readConnection, null);
            CalendarStorage storage = storageFactory.create(session.getContext(), dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
            return execute(new CalendarReader(session, storage));
        } finally {
            if (null != readConnection) {
                dbService.backReadOnly(session.getContext(), readConnection);
            }
        }
    }

    /**
     * Executes the storage operation.
     *
     * @param writer The calendar reader to use
     * @return The result
     */
    protected abstract T execute(CalendarReader writer) throws OXException;

}

