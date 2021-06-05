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

package com.openexchange.health.impl.checks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.health.AbstractCachingMWHealthCheck;
import com.openexchange.health.MWHealthCheckResponse;
import com.openexchange.health.impl.MWHealthCheckResponseImpl;
import com.openexchange.server.ServiceLookup;


/**
 * {@link ConfigDBCheck}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
public class ConfigDBCheck extends AbstractCachingMWHealthCheck {

    private final static String NAME = "configDB";
    private final static long TIMEOUT = 15000L;

    private final ServiceLookup services;

    public ConfigDBCheck(ServiceLookup services) {
        super(5000);
        this.services = services;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public long getTimeout() {
        return TIMEOUT;
    }

    @Override
    protected MWHealthCheckResponse doCall() {
        DatabaseService dbService = services.getService(DatabaseService.class);
        boolean status = true;
        Map<String, Object> data = new HashMap<>();

        {
            Connection con = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                long start = System.nanoTime();
                con = dbService.getReadOnly();
                stmt = con.prepareStatement("SELECT 1");
                rs = stmt.executeQuery();
                long end = System.nanoTime();
                data.put("readConnectionRoundTripTime", formatDuration(start, end) + "ms");
            } catch (OXException | SQLException e) {
                status = false;
                data.put("readConnectionError", e.getMessage());
            } finally {
                Databases.closeSQLStuff(rs, stmt);
                dbService.backReadOnly(con);
            }
        }

        {
            Connection con = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                long start = System.nanoTime();
                con = dbService.getWritable();
                stmt = con.prepareStatement("SELECT 1");
                rs = stmt.executeQuery();
                long end = System.nanoTime();
                data.put("writeConnectionRoundTripTime", formatDuration(start, end) + "ms");
            } catch (OXException | SQLException e) {
                status = false;
                data.put("writeConnectionError", e.getMessage());
            } finally {
                Databases.closeSQLStuff(rs, stmt);
                dbService.backWritableAfterReading(con);
            }
        }

        return new MWHealthCheckResponseImpl(NAME, data, status);
    }

    private String formatDuration(long start, long end) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMaximumFractionDigits(3);
        nf.setMinimumFractionDigits(3);
        return nf.format((end - start) / 1000000F);
    }

}
