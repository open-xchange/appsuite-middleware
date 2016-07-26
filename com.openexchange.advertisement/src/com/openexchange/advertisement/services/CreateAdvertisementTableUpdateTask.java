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

package com.openexchange.advertisement.services;

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.advertisement.osgi.Services;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link CreateAdvertisementTableUpdateTask}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class CreateAdvertisementTableUpdateTask extends UpdateTaskAdapter {

    private final static String CREATE_MAPPING_TABLE = "create table advertisement_mapping (" +
        "    reseller VARCHAR(128) NOT NULL," + 
        "    package VARCHAR(128) NOT NULL," + 
        "    configId int NOT NULL," + "    PRIMARY KEY (reseller, package)" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    private final static String CREATE_CONFIG_TABLE = "create table advertisement_config (" + "    configId int NOT NULL AUTO_INCREMENT," +
        "    config text NOT NULL," + 
        "    PRIMARY KEY (configId)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    @Override
    public void perform(PerformParameters params) throws OXException {
        final DatabaseService dbService = Services.getService(DatabaseService.class);
        if (dbService == null) {
            throw UpdateExceptionCodes.UNEXPECTED_ERROR.create(ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getSimpleName()));
        }
        Connection con = dbService.getForUpdateTask();
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(CREATE_CONFIG_TABLE);
            stmt.execute();
            stmt.close();
            stmt = con.prepareStatement(CREATE_MAPPING_TABLE);
            stmt.execute();

        } catch (SQLException e) {
            rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
            autocommit(con);
            dbService.backForUpdateTask(con);
        }

    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

}
