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
