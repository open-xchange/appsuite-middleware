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

package com.openexchange.geolocation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.annotation.Nullable;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link AbstractGeoLocationSQLStorage} - Provides abstract and common logic for SQL storage
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public abstract class AbstractGeoLocationSQLStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGeoLocationSQLStorage.class);

    protected static int EARTH_RADIUS_KM = 6371;
    protected static int EARTH_RADIUS_MILES = 3956;

    private ServiceLookup services;

    /**
     * Initialises a new {@link AbstractGeoLocationSQLStorage}.
     * 
     * @param services The {@link ServiceLookup} instance
     */
    public AbstractGeoLocationSQLStorage(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Retrieves the {@link GeoInformation} for the specified ipAddress
     * 
     * @param session The groupware session
     * @param ipAddress The ipAddress
     * @param query the SQL query to use for querying the database
     * @return The found {@link GeoInformation} or <code>null</code> if no location could be determined.
     * @throws OXException if an error is occurred
     */
    protected @Nullable GeoInformation getGeoInformation(Session session, int ipAddress, String query) throws OXException {
        DatabaseService databaseService = services.getServiceSafe(DatabaseService.class);
        Connection connection = databaseService.getReadOnlyForGlobal(session.getContextId());
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement(query);
            stmt.setInt(1, ipAddress);
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return null;
            }
            return parseResultSet(rs);
        } catch (SQLException e) {
            throw GeoLocationStorageExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseService.backReadOnlyForGlobal(session.getContextId(), connection);
        }
    }

    /**
     * Retrieves the {@link GeoInformation} of the location which is closest to the point of origin (latitude/longitude) and within
     * the specified radius
     * 
     * @param session The groupware session
     * @param latitude The latitude
     * @param longitude The longitude
     * @param radius The radius to search
     * @return The found {@link GeoInformation} or <code>null</code> if no location could be found within the specified radius.
     * @throws OXException if an error is occurred
     */
    protected @Nullable GeoInformation getGeoInformation(Session session, String query, double latitude, double longitude, int radius) throws OXException {
        DatabaseService databaseService = services.getServiceSafe(DatabaseService.class);
        Connection connection = databaseService.getReadOnlyForGlobal(session.getContextId());
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            int parameterIndex = 1;
            stmt = connection.prepareStatement(query);
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
            throw GeoLocationStorageExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseService.backReadOnlyForGlobal(session.getContextId(), connection);
        }
    }

    /**
     * Parses the specified {@link ResultSet} to a {@link GeoInformation}
     * 
     * @param resultSet The {@link ResultSet} to parse
     * @return The parsed {@link GeoInformation}
     * @throws SQLException if an SQL error is occurred
     */
    protected abstract GeoInformation parseResultSet(ResultSet resultSet) throws SQLException;
}
