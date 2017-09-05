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

package com.openexchange.chronos.provider.quota;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.internal.Services;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.quota.AccountQuota;
import com.openexchange.quota.DefaultAccountQuota;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.quota.QuotaType;
import com.openexchange.quota.groupware.AmountQuotas;
import com.openexchange.session.Session;

/**
 * {@link ChronosQuotaProvider}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class ChronosQuotaProvider implements QuotaProvider {

    /** Statement to count context quota */
    private final static String COUNT_CONTEXT = "SELECT COUNT(cid=?) AS quota FROM calendar_event";

    /** Logger for this class */
    private final static Logger LOGGER = LoggerFactory.getLogger(ChronosQuotaProvider.class);

    /** The unique module ID of this {@link QuotaProvider}. See {@link #getModuleID()} */
    private final static String MODULE_ID = "calendar.chronos";

    /** The unique and localizable module display name of this {@link QuotaProvider}. See {@link #getDisplayName()} */
    private final static String DISPLAY_NAME = "Calendar";

    /**
     * Initializes a new {@link ChronosQuotaProvider}.
     * 
     */
    public ChronosQuotaProvider() {
        super();
    }

    @Override
    public String getModuleID() {
        return MODULE_ID;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public AccountQuota getFor(Session session, String accountID) throws OXException {
        if ("0".equals(accountID)) {
            return new DefaultAccountQuota("0", DISPLAY_NAME).addQuota(getAmountQuota(session));
        }
        LOGGER.debug("Checking quota for external calendar account.");
        // TODO external case
        return null;
    }

    @Override
    public List<AccountQuota> getFor(Session session) throws OXException {
        return Collections.singletonList(new DefaultAccountQuota("0", DISPLAY_NAME).addQuota(getAmountQuota(session)));
    }

    /**
     * Get the Quota for given account
     * 
     * @param session The users {@link Session}
     * @param context The {@link Context} of the user
     * @param account The specific {@link CalendarAccount} to get quota for
     * @param connection A readable {@link Connection} to load quota from DB.
     * @return The {@link Quota}
     * @throws OXException Various reasons
     */
    private Quota getAmountQuota(Session session) throws OXException {
        DatabaseService dbService = Services.getService(DatabaseService.class, true);
        Connection connection = null;
        try {
            connection = dbService.getReadOnly(session.getContextId());
            long limit = AmountQuotas.getLimit(session, MODULE_ID, Services.getService(ConfigViewFactory.class, true), connection);
            if (limit <= Quota.UNLIMITED) {
                return Quota.UNLIMITED_AMOUNT;
            }
            return new Quota(QuotaType.AMOUNT, limit, countEventForContext(session.getContextId(), connection));
        } finally {
            if (null != connection) {
                dbService.backReadOnly(session.getContextId(), connection);
            }
        }
    }

    /**
     * Count all events known to the DB for a specific context
     * 
     * @param contextId The identifier of the context
     * @param connection The readable {@link Connection} to use
     * @return The quota for the context
     * @throws OXException If the quota can't be get
     */
    private long countEventForContext(int contextId, Connection connection) throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = connection.prepareStatement(COUNT_CONTEXT);
            stmt.setInt(1, contextId);

            result = stmt.executeQuery();

            // There should always be a result
            result.next();
            return result.getLong(1);

        } catch (SQLException e) {
            LOGGER.debug("Failed to count quota!", e);
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getCause());
        } finally {
            Databases.closeSQLStuff(result, stmt);
        }
    }
}
