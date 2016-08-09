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

package com.openexchange.advertisement.rmi.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.advertisement.AdvertisementExceptionCodes;
import com.openexchange.advertisement.RemoteAdvertisementService;
import com.openexchange.advertisement.osgi.Services;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link RemoteAdvertisementServiceImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class RemoteAdvertisementServiceImpl implements RemoteAdvertisementService {

    private static final String SQL_DELETE_MAPPING_ALL = "DELETE FROM advertisement_mapping;";
    private static final String SQL_DELETE_CONFIG_ALL = "DELETE FROM advertisement_config;";

    private static final String SQL_DELETE_MAPPING = "DELETE FROM advertisement_mapping WHERE reseller=?;";
    private static final String SQL_DELETE_CONFIG = "DELETE FROM advertisement_config WHERE reseller=?;";

    @Override
    public void removeConfigurations(String reseller) throws OXException {
        
        DatabaseService dbService = Services.getService(DatabaseService.class);
        Connection con = dbService.getWritable();
        PreparedStatement stmt = null;
        boolean isReadOnly = true;
        try {
            if (reseller == null) {
                stmt = con.prepareStatement(SQL_DELETE_CONFIG_ALL);
                if (stmt.executeUpdate() > 0) {
                    isReadOnly = false;
                }
                DBUtils.closeSQLStuff(stmt);
                stmt = con.prepareStatement(SQL_DELETE_MAPPING_ALL);
                if (stmt.executeUpdate() > 0) {
                    isReadOnly = false;
                }
            } else {
                stmt = con.prepareStatement(SQL_DELETE_CONFIG);
                stmt.setString(1, reseller);
                if (stmt.executeUpdate() > 0) {
                    isReadOnly = false;
                }
                DBUtils.closeSQLStuff(stmt);
                stmt = con.prepareStatement(SQL_DELETE_MAPPING);
                stmt.setString(1, reseller);
                if (stmt.executeUpdate() > 0) {
                    isReadOnly = false;
                }
            }
        } catch (SQLException e) {
            throw AdvertisementExceptionCodes.UNEXPECTED_DATABASE_ERROR.create(e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
            if (con != null) {
                if (isReadOnly) {
                    dbService.backReadOnly(con);
                } else {
                    dbService.backWritable(con);
                }
            }
        }
            
            
    }

}
