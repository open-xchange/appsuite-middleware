/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.regional.impl.storage;

import static com.openexchange.java.Autoboxing.i;
import java.sql.Connection;
import java.sql.Types;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.regional.RegionalSettings;
import com.openexchange.server.ServiceLookup;

/**
 * {@link SQLRegionalSettingStorage}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public class SQLRegionalSettingStorage extends AbstractRegionalSettingStorage implements RegionalSettingStorage {

    /**
     * Initialises a new {@link SQLRegionalSettingStorage}.
     * 
     * @param databaseService The {@link DatabaseService}
     */
    public SQLRegionalSettingStorage(ServiceLookup services) {
        super(services);
    }

    @Override
    public RegionalSettings get(int contextId, int userId) throws OXException {
        return query(contextId, SQLStatements.SELECT, (statement) -> {
            int arg = 1;
            statement.setInt(arg++, contextId);
            statement.setInt(arg++, userId);
        });
    }

    @Override
    public void upsert(int contextId, int userId, RegionalSettings settings) throws OXException {
        update(contextId, SQLStatements.UPSERT, (statement) -> {
            int arg = 1;
            statement.setInt(arg++, contextId);
            statement.setInt(arg++, userId);
            statement.setString(arg++, settings.getTimeFormat());
            statement.setString(arg++, settings.getTimeFormatLong());
            statement.setString(arg++, settings.getDateFormat());
            statement.setString(arg++, settings.getDateFormatShort());
            statement.setString(arg++, settings.getDateFormatMedium());
            statement.setString(arg++, settings.getDateFormatLong());
            statement.setString(arg++, settings.getDateFormatFull());
            statement.setString(arg++, settings.getNumberFormat());
            if (null == settings.getFirstDayOfWeek()) {
                statement.setNull(arg++, Types.INTEGER);
            } else {
                statement.setInt(arg++, i(settings.getFirstDayOfWeek()));
            }
            if (null == settings.getFirstDayOfYear()) {
                statement.setNull(arg++, Types.INTEGER);
            } else {
                statement.setInt(arg++, i(settings.getFirstDayOfYear()));
            }

            statement.setString(arg++, settings.getTimeFormat());
            statement.setString(arg++, settings.getTimeFormatLong());
            statement.setString(arg++, settings.getDateFormat());
            statement.setString(arg++, settings.getDateFormatShort());
            statement.setString(arg++, settings.getDateFormatMedium());
            statement.setString(arg++, settings.getDateFormatLong());
            statement.setString(arg++, settings.getDateFormatFull());
            statement.setString(arg++, settings.getNumberFormat());
            if (null == settings.getFirstDayOfWeek()) {
                statement.setNull(arg++, Types.INTEGER);
            } else {
                statement.setInt(arg++, i(settings.getFirstDayOfWeek()));
            }
            if (null == settings.getFirstDayOfYear()) {
                statement.setNull(arg++, Types.INTEGER);
            } else {
                statement.setInt(arg++, i(settings.getFirstDayOfYear()));
            }
        });
    }

    @Override
    public void delete(int contextId, int userId) throws OXException {
        delete(contextId, userId, null);
    }

    @Override
    public void delete(int contextId, int userId, Connection writeCon) throws OXException {
        update(contextId, SQLStatements.DELETE_USER, (statement) -> {
            int arg = 1;
            statement.setInt(arg++, contextId);
            statement.setInt(arg++, userId);
        }, writeCon);
    }

    @Override
    public void delete(int contextId) throws OXException {
        delete(contextId, null);
    }

    @Override
    public void delete(int contextId, Connection writeCon) throws OXException {
        update(contextId, SQLStatements.DELETE_CONTEXT, (statement) -> statement.setInt(1, contextId), writeCon);
    }
}
