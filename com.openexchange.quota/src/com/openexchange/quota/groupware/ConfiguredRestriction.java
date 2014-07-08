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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.quota.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.config.cascade.ConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.session.Session;

/**
 * {@link ConfiguredRestriction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class ConfiguredRestriction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConfiguredRestriction.class);

    public static long getAmountLimit(Session session, String moduleID, ConfigViewFactory configViewFactory, Connection connection) throws OXException {
        final Long quotaFromDB = getQuotaFromDB(connection, session.getContextId(), moduleID);
        if (null != quotaFromDB) {
            return quotaFromDB.longValue();
        }
        final ConfigView configView = configViewFactory.getView(session.getUserId(), session.getContextId());
        // Get property
        ConfigProperty<String> property = configView.property("com.openexchange.quota." + moduleID, String.class);
        if (!property.isDefined()) {
            return Quota.UNLIMITED;
        }
        try {
            return Long.parseLong(property.get().trim());
        } catch (final RuntimeException e) {
            LOG.warn("Couldn't detect quota for {} (user={}, context={})", moduleID, session.getUserId(), session.getContextId(), e);
            return Quota.UNLIMITED;
        }
    }

    public static long getAmountLimit(Session session, String moduleID, ConfigViewFactory configViewFactory, DatabaseService dbService) throws OXException {
        Connection connection = null;
        try {
            connection = dbService.getReadOnly(session.getContextId());
            return getAmountLimit(session, moduleID, configViewFactory, connection);
        } finally {
            if (null != connection) {
                dbService.backReadOnly(session.getContextId(), connection);
            }
        }
    }

    private static Long getQuotaFromDB(Connection connection, int contextId, String moduleID) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement("SELECT value FROM quota_context WHERE cid=? AND module=?");
            stmt.setLong(1, contextId);
            stmt.setString(2, moduleID);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }
            final long retval = rs.getLong(1);
            if (rs.wasNull()) {
                return null;
            }
            return Long.valueOf(retval <= 0 ? Quota.UNLIMITED : retval);
        } catch (final SQLException e) {
            throw QuotaExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw QuotaExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

}
