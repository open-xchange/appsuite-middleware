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

package com.openexchange.geolocation.maxmind;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.geolocation.AbstractGeoLocationSQLStorage;
import com.openexchange.geolocation.DefaultGeoInformation;
import com.openexchange.geolocation.GeoInformation;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link MaxMindSQLStorage}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
final class MaxMindSQLStorage extends AbstractGeoLocationSQLStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(MaxMindSQLStorage.class);
    private final ServiceLookup services;

    /**
     * Initialises a new {@link MaxMindSQLStorage}.
     */
    public MaxMindSQLStorage(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * 
     * @param session
     * @param ipAddress
     * @return
     * @throws OXException
     */
    GeoInformation getGeoInformation(Session session, int ipAddress) throws OXException {
        DatabaseService databaseService = services.getServiceSafe(DatabaseService.class);
        Connection connection = databaseService.getReadOnlyForGlobal(session.getContextId());
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement(SQLStatements.SELECT_BY_IP_ADDRESS);
            stmt.setInt(1, ipAddress);
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return null;
            }
            return parseResultSet(rs);
        } catch (SQLException e) {
            throw new OXException(31145, "SQL error", e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseService.backReadOnlyForGlobal(session.getContextId(), connection);
        }
    }

    /**
     * 
     * @param session
     * @param latitude
     * @param longitude
     * @param radius
     * @return
     * @throws OXException
     */
    GeoInformation getGeoInformation(Session session, double latitude, double longitude, int radius) throws OXException {
        DatabaseService databaseService = services.getServiceSafe(DatabaseService.class);
        Connection connection = databaseService.getReadOnlyForGlobal(session.getContextId());
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            int parameterIndex = 1;
            stmt = connection.prepareStatement(SQLStatements.SELECT_BY_GPS_COORDINATES);
            stmt.setInt(parameterIndex++, EARTH_RADIUS_KM);
            stmt.setDouble(parameterIndex++, latitude);
            stmt.setDouble(parameterIndex++, longitude);
            stmt.setDouble(parameterIndex++, latitude);
            stmt.setInt(parameterIndex++, radius);
            stmt.setInt(parameterIndex++, 1); //return the first result
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                LOGGER.debug("No locations could be found from point {},{} within a radius of '{}'.", latitude, longitude, radius);
                return null;
            }
            return parseResultSet(rs);
        } catch (SQLException e) {
            throw new OXException(31145, "SQL error", e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseService.backReadOnlyForGlobal(session.getContextId(), connection);
        }
    }

    /**
     * 
     * @param resultSet
     * @return
     * @throws SQLException
     */
    private GeoInformation parseResultSet(ResultSet resultSet) throws SQLException {
        //@formatter:off
        return DefaultGeoInformation.builder()
            .city(resultSet.getString("city_name"))
            .continent(resultSet.getString("continent_name"))
            .country(resultSet.getString("country_name"))
            .postalCode(resultSet.getInt("postal_code"))
            .build();
        //@formatter:on
    }
}
