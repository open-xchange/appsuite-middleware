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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.rollback;
import static com.openexchange.java.Autoboxing.I;
import static org.slf4j.LoggerFactory.getLogger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.IntStream;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.user.UserExceptionCode;

/**
 * {@link CorrectInvalidTimezoneIdsTask}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v8.0.0
 */
public class CorrectInvalidTimezoneIdsTask implements UpdateTaskV2 {

    @Override
    public String[] getDependencies() {
        return new String[] { UserAddGuestCreatedByTask.class.getName() };
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(UpdateConcurrency.BLOCKING);
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection connection = params.getConnection();
        int rollback = 0;
        try {
            connection.setAutoCommit(false);
            /*
             * lookup all invalid timezone identifiers in schema
             */
            Set<String> invalidTimezoneIds = getInvalidTimezoneIds(connection);
            if (invalidTimezoneIds.isEmpty()) {
                getLogger(CorrectInvalidTimezoneIdsTask.class).info("No users with invalid timezone id found on schema {}.", connection.getCatalog());
                return;
            }
            /*
             * update the invalid records to configured DEFAULT_TIMEZONE
             */
            rollback = 1;
            int updated = updateInvalidTimezoneIds(connection, invalidTimezoneIds, getDefaultTimezone());
            getLogger(CorrectInvalidTimezoneIdsTask.class).info("Successfully updated {} invalid timezone ids for {} users on schema {}.", 
                I(invalidTimezoneIds.size()), I(updated), connection.getCatalog());
            connection.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (0 < rollback) {
                if (1 == rollback) {
                    rollback(connection);
                }
                autocommit(connection);
            }
        }
    }

    private static int updateInvalidTimezoneIds(Connection connection, Set<String> invalidTimezoneIds, String newTimezoneId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE user SET timezone=? WHERE timezone=?;")) {
            stmt.setString(1, newTimezoneId);
            for (String invalidTimezoneId : invalidTimezoneIds) {
                stmt.setString(2, invalidTimezoneId);
                stmt.addBatch();
            }
            return IntStream.of(stmt.executeBatch()).sum();
        }
    }

    private static Set<String> getInvalidTimezoneIds(Connection connection) throws SQLException, OXException {
        Set<String> invalidTimezoneIds = new HashSet<String>();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT DISTINCT timezone FROM user;")) {
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    String timezoneId = resultSet.getString(1);
                    if (false == isValidTimezoneId(timezoneId)) {
                        invalidTimezoneIds.add(resultSet.getString(1));
                    }
                }
            }
        }
        return invalidTimezoneIds;
    }

    private static String getDefaultTimezone() {
        String defaultValue = "Europe/Berlin";
        try {
            ConfigurationService configurationService = ServerServiceRegistry.getServize(ConfigurationService.class, true);
            String value = configurationService.getProperty("DEFAULT_TIMEZONE", defaultValue);
            if (false == isValidTimezoneId(value)) {
                throw UserExceptionCode.INVALID_TIMEZONE.create(value);
            }
            return value;
        } catch (OXException e) {
            getLogger(CorrectInvalidTimezoneIdsTask.class).warn("Error getting configured \"DEFAULT_TIMEZONE\", falling back to {}", defaultValue);
            return defaultValue;
        }
    }

    private static boolean isValidTimezoneId(String timezoneId) {
        if (null != timezoneId) {
            TimeZone timeZone = TimeZone.getTimeZone(timezoneId);
            return null != timeZone && timeZone.getID().equals(timezoneId);
        }
        return false;
    }

}
