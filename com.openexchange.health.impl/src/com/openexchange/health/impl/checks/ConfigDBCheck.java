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

package com.openexchange.health.impl.checks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.health.NodeHealthCheck;
import com.openexchange.health.NodeHealthCheckResponse;
import com.openexchange.health.impl.NodeHealthCheckResponseImpl;
import com.openexchange.server.ServiceLookup;


/**
 * {@link ConfigDBCheck}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
public class ConfigDBCheck implements NodeHealthCheck {

    private final static String NAME = "configDB";

    private final ServiceLookup services;

    public ConfigDBCheck(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public NodeHealthCheckResponse call() {
        DatabaseService dbService = services.getService(DatabaseService.class);
        boolean status = true;
        Map<String, Object> data = new HashMap<>();

        {
            Connection con = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                long start = System.currentTimeMillis();
                con = dbService.getReadOnly();
                stmt = con.prepareStatement("SELECT 1");
                rs = stmt.executeQuery();
                long end = System.currentTimeMillis();
                data.put("readConnectionRoundTripTime", end - start + "ms");
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
                long start = System.currentTimeMillis();
                con = dbService.getWritable();
                stmt = con.prepareStatement("SELECT 1");
                rs = stmt.executeQuery();
                long end = System.currentTimeMillis();
                data.put("writeConnectionRoundTripTime", end - start + "ms");
            } catch (OXException | SQLException e) {
                status = false;
                data.put("writeConnectionError", e.getMessage());
            } finally {
                Databases.closeSQLStuff(rs, stmt);
                dbService.backWritableAfterReading(con);
            }
        }

        return new NodeHealthCheckResponseImpl(NAME, data, status);
    }

}
