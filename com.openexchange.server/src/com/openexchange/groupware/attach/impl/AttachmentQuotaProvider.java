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

package com.openexchange.groupware.attach.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.Info;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.quota.AccountQuota;
import com.openexchange.quota.AccountQuotas;
import com.openexchange.quota.DefaultAccountQuota;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.quota.QuotaType;
import com.openexchange.quota.groupware.AmountQuotas;
import com.openexchange.session.Session;


/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class AttachmentQuotaProvider implements QuotaProvider {

    private static final String MODULE_ID = "attachment";

    private final DatabaseService dbService;

    private final ContextService contextService;

    private final ConfigViewFactory viewFactory;

    private final QuotaFileStorageService fsFactory;


    public AttachmentQuotaProvider(DatabaseService dbService, ContextService contextService, ConfigViewFactory viewFactory, QuotaFileStorageService fsFactory) {
        super();
        this.dbService = dbService;
        this.contextService = contextService;
        this.viewFactory = viewFactory;
        this.fsFactory = fsFactory;
    }

    @Override
    public String getModuleID() {
        return MODULE_ID;
    }

    @Override
    public String getDisplayName() {
        return AttachmentStrings.ATTACHMENTS;
    }

    Quota getAmountQuota(Session session) throws OXException {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            connection = dbService.getReadOnly(session.getContextId());
            long limit = AmountQuotas.getLimit(session, MODULE_ID, viewFactory, connection);
            if (limit <= Quota.UNLIMITED) {
                return Quota.UNLIMITED_AMOUNT;
            }

            stmt = connection.prepareStatement("SELECT count(id) FROM prg_attachment WHERE cid=?");
            stmt.setInt(1, session.getContextId());
            rs = stmt.executeQuery();
            long usage = rs.next() ? rs.getLong(1) : 0;
            return new Quota(QuotaType.AMOUNT, limit, usage);
        } catch (SQLException e) {
            throw QuotaExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (null != connection) {
                dbService.backReadOnly(session.getContextId(), connection);
            }
        }
    }

    Quota getSizeQuota(Session session) throws OXException {
        QuotaFileStorage quotaFileStorage = fsFactory.getQuotaFileStorage(session.getContextId(), Info.general());
        long limit = quotaFileStorage.getQuota();
        long usage = quotaFileStorage.getUsage();
        return new Quota(QuotaType.SIZE, limit, usage);
    }

    @Override
    public AccountQuota getFor(Session session, String accountID) throws OXException {
        if (false == "0".equals(accountID)) {
            throw QuotaExceptionCodes.UNKNOWN_ACCOUNT.create(accountID, MODULE_ID);
        }

        return new DefaultAccountQuota(accountID, getDisplayName()).addQuota(getAmountQuota(session)).addQuota(getSizeQuota(session));
    }

    @Override
    public AccountQuotas getFor(Session session) throws OXException {
        return new AccountQuotas(getFor(session, "0"));
    }

}
