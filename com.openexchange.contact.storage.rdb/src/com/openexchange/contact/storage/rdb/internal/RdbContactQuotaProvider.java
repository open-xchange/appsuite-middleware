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

package com.openexchange.contact.storage.rdb.internal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.contact.storage.rdb.sql.Executor;
import com.openexchange.contact.storage.rdb.sql.Table;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.quota.AccountQuota;
import com.openexchange.quota.DefaultAccountQuota;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.quota.QuotaType;
import com.openexchange.quota.groupware.AmountQuotas;
import com.openexchange.session.Session;

/**
 * {@link RdbContactQuotaProvider}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class RdbContactQuotaProvider implements QuotaProvider {

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RdbContactQuotaProvider.class);

    private static final String MODULE_ID = "contact";

    public RdbContactQuotaProvider() {
        super();
    }

    @Override
    public String getModuleID() {
        return "contact";
    }

    @Override
    public String getDisplayName() {
        return "Contacts";
    }

    static Quota getAmountQuota(Session session, Executor executor, Connection connection) throws SQLException, OXException {
        long limit = AmountQuotas.getLimit(session, MODULE_ID,
            RdbServiceLookup.getService(ConfigViewFactory.class), connection);
        if (limit <= Quota.UNLIMITED) {
            return Quota.UNLIMITED_AMOUNT;
        }
        long usage = executor.count(connection, Table.CONTACTS, session.getContextId());
        return new Quota(QuotaType.AMOUNT, limit, usage);
    }

    @Override
    public AccountQuota getFor(Session session, String accountID) throws OXException {
        if ("0".equals(accountID)) {
            DatabaseService dbService = RdbServiceLookup.getService(DatabaseService.class);
            Connection connection = null;
            try {
                connection = dbService.getReadOnly(session.getContextId());
                return new DefaultAccountQuota(accountID, getDisplayName()).addQuota(getAmountQuota(session, new Executor(), connection));
            } catch (SQLException e) {
                throw QuotaExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } finally {
                if (null != connection) {
                    dbService.backReadOnly(session.getContextId(), connection);
                }
            }
        } else {
            throw QuotaExceptionCodes.UNKNOWN_ACCOUNT.create(accountID, MODULE_ID);
        }
    }

    @Override
    public List<AccountQuota> getFor(Session session) throws OXException {
        return Collections.singletonList(getFor(session, "0"));
    }

}
