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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.chronos.storage.rdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.chronos.CalendarAvailability;
import com.openexchange.chronos.CalendarFreeSlot;
import com.openexchange.chronos.FieldAware;
import com.openexchange.chronos.storage.CalendarAvailabilityStorage;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapper;

/**
 * {@link RdbCalendarAvailabilityStorage}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class RdbCalendarAvailabilityStorage extends RdbStorage implements CalendarAvailabilityStorage {

    private static final CalendarAvailabilityMapper calendarAvailabilityMapper = CalendarAvailabilityMapper.getInstance();
    private static final FreeSlotMapper freeSlotMapper = FreeSlotMapper.getInstance();
    private int accountId;
    private static final String CA_TABLE_NAME = "calendar_availability";
    private static final String CA_FREE_SLOT_NAME = "calendar_free_slot";

    /**
     * Initialises a new {@link RdbCalendarAvailabilityStorage}.
     * 
     * @param context The context
     * @param dbProvider The database provider to use
     * @param txPolicy The transaction policy
     */
    public RdbCalendarAvailabilityStorage(Context context, int accountId, DBProvider dbProvider, DBTransactionPolicy txPolicy) {
        super(context, dbProvider, txPolicy);
        this.accountId = accountId;

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.storage.CalendarAvailabilityStorage#insertCalendarAvailability(com.openexchange.chronos.CalendarAvailability)
     */
    @Override
    public void insertCalendarAvailability(CalendarAvailability calendarAvailability) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            int caAmount = insertCalendarAvailabilityItem(calendarAvailability, calendarAvailabilityMapper, CA_TABLE_NAME, connection);
            int freeSlotsCount = 0;
            for (CalendarFreeSlot calendarFreeSlot : calendarAvailability.getCalendarFreeSlots()) {
                freeSlotsCount += insertCalendarAvailabilityItem(calendarFreeSlot, freeSlotMapper, CA_FREE_SLOT_NAME, connection);
            }
            txPolicy.commit(connection);
            LOG.debug("Inserted {} availability block(s and {} free slot(s) for user {} in context {}.", caAmount, freeSlotsCount, calendarAvailability.getCalendarUser(), context.getContextId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inserts the specified item to the storage
     * 
     * @param item The item to insert
     * @param mapper The database mapper
     * @param tableName The table name
     * @param connection The wrtieable connection to the storage
     * @return The amount of affected rows
     * @throws OXException if an error is occurred
     * @throws SQLException if an SQL error is occurred
     */
    private <O extends FieldAware, E extends Enum<E>> int insertCalendarAvailabilityItem(O item, DefaultDbMapper<O, E> mapper, String tableName, Connection connection) throws OXException, SQLException {
        E[] mappedFields = mapper.getMappedFields();
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(tableName);
        sb.append("(cid,account,").append(mapper.getColumns(mappedFields)).append(");");
        sb.append("VALUES (?,?,").append(mapper.getParameters(mappedFields)).append(");");
        String sql = sb.toString();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, context.getContextId());
            stmt.setInt(parameterIndex++, accountId);
            parameterIndex = mapper.setParameters(stmt, parameterIndex, item, mappedFields);
            return logExecuteUpdate(stmt);
        }
    }
}
