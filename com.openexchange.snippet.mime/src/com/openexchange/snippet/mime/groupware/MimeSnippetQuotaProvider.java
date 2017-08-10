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

package com.openexchange.snippet.mime.groupware;

import static com.openexchange.snippet.mime.MimeSnippetManagement.getFsType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.cascade.ConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.Info;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.quota.AccountQuota;
import com.openexchange.quota.DefaultAccountQuota;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.quota.QuotaType;
import com.openexchange.quota.groupware.AmountQuotas;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.snippet.SnippetExceptionCodes;
import com.openexchange.snippet.mime.MimeSnippetManagement;
import com.openexchange.snippet.mime.Services;

/**
 * {@link MimeSnippetQuotaProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class MimeSnippetQuotaProvider implements QuotaProvider {

    private static final String MODULE_ID = "mime_snippet";
    private static final String PROP_AMOUNT_LIMIT = "com.openexchange.snippet.quota.limit";
    private static final String PROP_SIZE_LIMIT = "com.openexchange.snippet.filestore.quota.perUserLimit";

    /**
     * Initializes a new {@link MimeSnippetQuotaProvider}.
     */
    public MimeSnippetQuotaProvider() {
        super();
    }

    @Override
    public String getModuleID() {
        return MODULE_ID;
    }

    @Override
    public String getDisplayName() {
        return "Snippet";
    }

    private Quota getAmountQuota(Session session, ConfigViewFactory viewFactory) throws OXException {
        long limit = AmountQuotas.getConfiguredLimitByPropertyName(session, PROP_AMOUNT_LIMIT, viewFactory);
        if (limit <= Quota.UNLIMITED) {
            return Quota.UNLIMITED_AMOUNT;
        }

        long usage = getAmountUsage(session);
        return new Quota(QuotaType.AMOUNT, limit, usage);
    }

    private Quota optSizeQuota(Session session, ConfigViewFactory viewFactory) throws OXException {
        if (false == hasSizeQuota()) {
            // No storage quota
            return null;
        }

        // Get size limit property
        long limit;
        {
            ConfigView configView = viewFactory.getView(session.getUserId(), session.getContextId());
            ConfigProperty<String> property = configView.property(PROP_SIZE_LIMIT, String.class);
            long def = 5242880;
            if (property.isDefined()) {
                limit = ConfigTools.parseBytes(property.get());
            } else {
                limit = def;
            }
        }
        if (limit <= Quota.UNLIMITED) {
            return Quota.UNLIMITED_SIZE;
        }

        // Retrieve usage as well and return
        long usage = getSizeUsage(session);
        return new Quota(QuotaType.SIZE, limit, usage);
    }

    @Override
    public AccountQuota getFor(Session session, String accountID) throws OXException {
        if (!accountID.equals("0")) {
            throw QuotaExceptionCodes.UNKNOWN_ACCOUNT.create(accountID, MODULE_ID);
        }

        ConfigViewFactory viewFactory = Services.getService(ConfigViewFactory.class);
        if (viewFactory == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ConfigViewFactory.class.getName());
        }

        Quota amountQuota = getAmountQuota(session, viewFactory);
        DefaultAccountQuota accountQuota = new DefaultAccountQuota(accountID, getDisplayName()).addQuota(amountQuota);
        Quota sizeQuota = optSizeQuota(session, viewFactory);
        if (null != sizeQuota) {
            accountQuota.addQuota(sizeQuota);
        }
        return accountQuota;
    }

    @Override
    public List<AccountQuota> getFor(Session session) throws OXException {
        return Collections.singletonList(getFor(session, "0"));
    }

    // --------------------------------------------------------------------------------------------------------------

    /**
     * Checks if size quota is enabled.
     *
     * @return <code>true</code> if size quota is enabled; otherwise <code>false</code>
     */
    private boolean hasSizeQuota() {
        return QuotaMode.DEDICATED.equals(MimeSnippetManagement.getMode());
    }

    /**
     * Retrieves the current storage usage.
     *
     * @return The usage in bytes
     * @throws OXException If usage cannot be retrieved
     */
    private long getSizeUsage(Session session) throws OXException {
        DatabaseService databaseService = Services.optService(DatabaseService.class);
        if (null == databaseService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }

        int userId = session.getUserId();
        int contextId = session.getContextId();

        Map<String, String> reload = null;
        long currentUsage = 0;
        {
            Connection con = databaseService.getReadOnly(contextId);
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = con.prepareStatement("SELECT id, size, refId FROM snippet WHERE cid=? AND user=? AND refType=" + getFsType());
                int pos = 0;
                stmt.setInt(++pos, contextId);
                stmt.setInt(++pos, userId);
                rs = stmt.executeQuery();
                if (false == rs.next()) {
                    return 0;
                }

                do {
                    long size = rs.getLong(2);
                    if (rs.wasNull()) {
                        if (reload == null) {
                            reload = new HashMap<>();
                        }
                        String snippetId = rs.getString(1);
                        String fileStoreLocation = rs.getString(3);
                        reload.put(snippetId, fileStoreLocation);
                    } else {
                        currentUsage += size;
                    }
                } while (rs.next());
            } catch (final SQLException e) {
                throw SnippetExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(rs, stmt);
                databaseService.backReadOnly(contextId, con);
            }
        }

        // Check if there are entries, which are required to be reloaded
        if (null != reload) {
            QuotaFileStorage quotaFileStorage = FileStorages.getQuotaFileStorageService().getQuotaFileStorage(contextId, Info.general());

            Connection writeCon = databaseService.getWritable(contextId);
            boolean onlyRead = true;
            PreparedStatement stmt = null;
            try {
                stmt = writeCon.prepareStatement("UPDATE snippet SET size=? WHERE cid=? AND user=? AND id=? AND refType=" + getFsType());
                stmt.setInt(2, contextId);
                stmt.setInt(3, userId);

                for (Map.Entry<String, String> entry : reload.entrySet()) {
                    String id = entry.getKey();
                    String fileId = entry.getValue();

                    // Get current file size from file-storage & add to batch statement
                    long fileSize = quotaFileStorage.getFileSize(fileId);
                    stmt.setLong(1, fileSize);
                    stmt.setString(4, id);
                    stmt.addBatch();
                    onlyRead = false;

                    // Add to current usage as well
                    currentUsage += fileSize;
                }

                stmt.executeBatch();
            } catch (final SQLException e) {
                throw SnippetExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(stmt);
                if (onlyRead) {
                    databaseService.backWritableAfterReading(contextId, writeCon);
                } else {
                    databaseService.backWritable(contextId, writeCon);
                }
            }
        }

        return currentUsage;
    }

    private int getAmountUsage(Session session) throws OXException {
        DatabaseService databaseService = Services.optService(DatabaseService.class);
        if (null == databaseService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }

        int contextId = session.getContextId();
        Connection con = databaseService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT COUNT(id) FROM snippet WHERE cid=? AND user=? AND refType=" + getFsType());
            int pos = 0;
            stmt.setInt(++pos, contextId);
            stmt.setInt(++pos, session.getUserId());
            rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (final SQLException e) {
            throw SnippetExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(contextId, con);
        }
    }

}
