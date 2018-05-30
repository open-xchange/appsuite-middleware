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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.storage.rdb.groupware;

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.rollback;
import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.rdb.RdbCalendarStorage;
import com.openexchange.context.ContextService;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.server.ServiceLookup;

/**
 * {@link CalendarAlarmRepairShiftedTriggersTask}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarAlarmRepairShiftedTriggersTask extends UpdateTaskAdapter {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link CalendarAlarmRepairShiftedTriggersTask}.
     *
     * @param services A service lookup reference
     */
    public CalendarAlarmRepairShiftedTriggersTask(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public String[] getDependencies() {
        return new String[] { "com.openexchange.chronos.storage.rdb.migration.ChronosStorageMigrationTask" };
    }

    /**
     * Optionally gets an entity resolver for the supplied context.
     *
     * @param contextId The identifier of the context to get the entity resolver for
     * @return The entity resolver, or <code>null</code> if not available
     */
    private EntityResolver optEntityResolver(int contextId) {
        CalendarUtilities calendarUtilities = services.getOptionalService(CalendarUtilities.class);
        if (null != calendarUtilities) {
            try {
                return calendarUtilities.getEntityResolver(contextId);
            } catch (OXException e) {
                org.slf4j.LoggerFactory.getLogger(CalendarAlarmRepairShiftedTriggersTask.class).warn(
                    "Error getting entity resolver for context {}: {}", I(contextId), e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        ContextService contextService = services.getService(ContextService.class);
        int[] contextIds = params.getContextsInSameSchema();
        Connection connection = params.getConnection();
        boolean rollback = false;
        try {
            connection.setAutoCommit(false);
            rollback = true;
            DBProvider dbProvider = new SimpleDBProvider(connection, connection);
            for (int contextId : contextIds) {
                /*
                 * check for obviously wrong trigger durations in alarms
                 */
                Collection<String> eventIds = repairShiftedTriggerDurations(connection, contextId);
                if (null != eventIds && 0 < eventIds.size()) {
                    /*
                     * repair trigger durations and re-init affected triggers
                     */
                    Context context = contextService.loadContext(contextId);
                    RdbCalendarStorage calendarStorage = new com.openexchange.chronos.storage.rdb.RdbCalendarStorage(context, 0, optEntityResolver(contextId), dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
                    for (String eventId : eventIds) {
                        Event event = calendarStorage.getEventStorage().loadEvent(eventId, null);
                        event = calendarStorage.getUtilities().loadAdditionalEventData(-1, event, null);
                        Map<Integer, List<Alarm>> alarmsPerUserId = calendarStorage.getAlarmStorage().loadAlarms(event);
                        calendarStorage.getAlarmTriggerStorage().deleteTriggers(eventId);
                        calendarStorage.getAlarmTriggerStorage().insertTriggers(event, alarmsPerUserId);
                    }
                }
            }
            connection.commit();
            rollback = false;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(connection);
            }
            autocommit(connection);
        }
    }

    private static final String[] SHIFTED_15_MIN = new String[] { "-PT1455M", "-P7DT15M", "-PT10095M", "-P14DT15M", "-PT20175M", "-P21DT15M", "-PT30255M", "-P28DT15M", "-PT40335M", "-P6DT23H15M" };
    private static final String[] SHIFTED_30_MIN = new String[] { "-PT1470M", "-P7DT30M", "-PT10110M", "-P14DT30M", "-PT20190M", "-P21DT30M", "-PT30270M", "-P28DT30M", "-PT40350M" };

    private Set<String> repairShiftedTriggerDurations(Connection connection, int contextId) throws SQLException {
        Set<String> retval = new HashSet<>();

        String s = Databases.getIN("SELECT event FROM calendar_alarm WHERE cid=? AND account=0 AND triggerDuration IN (", SHIFTED_15_MIN.length + SHIFTED_30_MIN.length);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement(s);
            int i = 1;
            stmt.setInt(i++, contextId);
            for (String shifted : SHIFTED_15_MIN) {
                stmt.setString(i++, shifted);
            }
            for (String shifted : SHIFTED_30_MIN) {
                stmt.setString(i++, shifted);
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                retval.add(rs.getString("event"));
            }

            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            s = Databases.getIN("UPDATE calendar_alarm SET triggerDuration = ? WHERE cid=? AND account=0 AND triggerDuration IN (", SHIFTED_15_MIN.length);
            stmt = connection.prepareStatement(s);
            i = 1;
            stmt.setString(i++, "-PT15M");
            stmt.setInt(i++, contextId);
            for (String shifted : SHIFTED_15_MIN) {
                stmt.setString(i++, shifted);
            }
            stmt.executeUpdate();

            Databases.closeSQLStuff(stmt);
            stmt = null;

            s = Databases.getIN("UPDATE calendar_alarm SET triggerDuration = ? WHERE cid=? AND account=0 AND triggerDuration IN (", SHIFTED_30_MIN.length);
            stmt = connection.prepareStatement(s);
            i = 1;
            stmt.setString(i++, "-PT30M");
            stmt.setInt(i++, contextId);
            for (String shifted : SHIFTED_30_MIN) {
                stmt.setString(i++, shifted);
            }
            stmt.executeUpdate();

            Databases.closeSQLStuff(stmt);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }

        return retval;
    }

}
