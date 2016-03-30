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

package com.openexchange.user.copy.internal.usersettings;

import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.user.copy.internal.CopyTools.getIntOrNegative;
import static com.openexchange.user.copy.internal.CopyTools.setIntOrNull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.copy.CopyUserTaskService;
import com.openexchange.user.copy.ObjectMapping;
import com.openexchange.user.copy.UserCopyExceptionCodes;
import com.openexchange.user.copy.internal.CopyTools;
import com.openexchange.user.copy.internal.connection.ConnectionFetcherTask;
import com.openexchange.user.copy.internal.context.ContextLoadTask;
import com.openexchange.user.copy.internal.folder.FolderCopyTask;
import com.openexchange.user.copy.internal.user.UserCopyTask;


/**
 * {@link UserSettingsCopyTask} - Copies all values from the user_settings table for the user to move.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class UserSettingsCopyTask implements CopyUserTaskService {

    private static final String SELECT_SQL = "SELECT path_id, value FROM user_setting WHERE cid = ? AND user_id = ?";

    private static final String INSERT_SQL = "INSERT INTO user_setting (cid, user_id, path_id, value) VALUES (?, ?, ?, ?)";

    private static final String SELECT_SERVER_SQL = "SELECT contact_collect_folder, contact_collect_enabled, defaultStatusPrivate, defaultStatusPublic, contactCollectOnMailTransport, contactCollectOnMailAccess, folderTree, uuid FROM user_setting_server WHERE cid = ? AND user = ?";

    private static final String INSERT_SERVER_SQL = "INSERT INTO user_setting_server (cid, user, contact_collect_folder, contact_collect_enabled, defaultStatusPrivate, defaultStatusPublic, contactCollectOnMailTransport, contactCollectOnMailAccess, folderTree, uuid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


    /**
     * Initializes a new {@link UserSettingsCopyTask}.
     */
    public UserSettingsCopyTask() {
        super();
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#getAlreadyCopied()
     */
    @Override
    public String[] getAlreadyCopied() {
        return new String[] {
            UserCopyTask.class.getName(),
            ContextLoadTask.class.getName(),
            ConnectionFetcherTask.class.getName(),
            FolderCopyTask.class.getName()
        };
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#getObjectName()
     */
    @Override
    public String getObjectName() {
        return Setting.class.getName();
    }

    /**
     * @see com.openexchange.user.copy.CopyUserTaskService#copyUser(java.util.Map)
     */
    @Override
    public ObjectMapping<?> copyUser(final Map<String, ObjectMapping<?>> copied) throws OXException {
        final CopyTools copyTools = new CopyTools(copied);
        final Integer srcCtxId = copyTools.getSourceContextId();
        final Integer dstCtxId = copyTools.getDestinationContextId();
        final Integer srcUsrId = copyTools.getSourceUserId();
        final Integer dstUsrId = copyTools.getDestinationUserId();
        final Connection srcCon = copyTools.getSourceConnection();
        final Connection dstCon = copyTools.getDestinationConnection();
        final Context srcCtx = copyTools.getSourceContext();
        final Context dstCtx = copyTools.getDestinationContext();
        final ObjectMapping<FolderObject> folderMapping = copyTools.getFolderMapping();

        /*
         * user_setting
         */
        final List<UserSettingEntry> settings = loadSettingsFromDB(srcCon, i(srcCtxId), i(srcUsrId));
        writeSettingsToDB(dstCon, settings, i(dstCtxId), i(dstUsrId));

        /*
         * user_setting_mail
         */
        final UserSettingMailStorage usmStorage = UserSettingMailStorage.getInstance();
        final UserSettingMail srcMailSettings = usmStorage.getUserSettingMail(i(srcUsrId), srcCtx, srcCon);
        try {
            usmStorage.saveUserSettingMail(srcMailSettings, i(dstUsrId), dstCtx, dstCon);
        } catch (final OXException e) {
            throw UserCopyExceptionCodes.SAVE_MAIL_SETTINGS_PROBLEM.create(e);
        }

        /*
         * user_setting_server
         */
        final ServerSetting serverSetting = loadServerSettingFromDB(srcCon, i(srcCtxId), i(srcUsrId));
        if (serverSetting != null) {
            final int srcCollectionFolderId = serverSetting.getFolder();
            int dstCollectionFolderId = -1;
            if (srcCollectionFolderId != -1) {
                final FolderObject srcCollectionFolder = folderMapping.getSource(srcCollectionFolderId);
                if (srcCollectionFolder != null) {
                    final FolderObject dstCollectionFolder = folderMapping.getDestination(srcCollectionFolder);
                    dstCollectionFolderId = dstCollectionFolder.getObjectID();
                }
            }

            serverSetting.setFolder(dstCollectionFolderId);
            writeServerSettingToDB(dstCon, i(dstCtxId), i(dstUsrId), serverSetting);
        }

        return null;
    }

    private ServerSetting loadServerSettingFromDB(final Connection con, final int cid, final int uid) throws OXException {
        ServerSetting setting = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SELECT_SERVER_SQL);
            stmt.setInt(1, cid);
            stmt.setInt(2, uid);

            rs = stmt.executeQuery();
            if (rs.next()) {
                int i = 1;
                setting = new ServerSetting();
                setting.setFolder(getIntOrNegative(i++, rs));
                setting.setEnabled(getIntOrNegative(i++, rs) == 1 ? true : false);
                setting.setDefaultStatusPrivate(getIntOrNegative(i++, rs));
                setting.setDefaultStatusPublic(getIntOrNegative(i++, rs));
                setting.setContactCollectOnMailTransport(getIntOrNegative(i++, rs) == 1 ? true : false);
                setting.setContactCollectOnMailAccess(getIntOrNegative(i++, rs) == 1 ? true : false);
                setting.setFolderTree(getIntOrNegative(i++, rs));
                setting.setUuidBinary(rs.getBytes("uuid"));
            }
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }

        return setting;
    }

    private void writeServerSettingToDB(final Connection con, final int cid, final int uid, final ServerSetting setting) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(INSERT_SERVER_SQL);
            int i = 1;
            stmt.setInt(i++, cid);
            stmt.setInt(i++, uid);
            setIntOrNull(i++, stmt, setting.getFolder());
            stmt.setInt(i++, setting.isEnabled() ? 1 : 0);
            setIntOrNull(i++, stmt, setting.getDefaultStatusPrivate());
            setIntOrNull(i++, stmt, setting.getDefaultStatusPublic());
            stmt.setInt(i++, setting.isContactCollectOnMailTransport() ? 1 : 0);
            stmt.setInt(i++, setting.isContactCollectOnMailAccess() ? 1 : 0);
            setIntOrNull(i++, stmt, setting.getFolderTree());
            stmt.setBytes(i++, setting.getUuidBinary());
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    private List<UserSettingEntry> loadSettingsFromDB(final Connection con, final int cid, final int uid) throws OXException {
        final List<UserSettingEntry> settings = new ArrayList<UserSettingEntry>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SELECT_SQL);
            stmt.setInt(1, cid);
            stmt.setInt(2, uid);

            rs = stmt.executeQuery();
            while (rs.next()) {
                final UserSettingEntry setting = new UserSettingEntry();
                setting.setPathId(rs.getInt(1));
                setting.setValue(rs.getString(2));

                settings.add(setting);
            }
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }

        return settings;
    }

    private void writeSettingsToDB(final Connection con, final List<UserSettingEntry> settings, final int cid, final int uid) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(INSERT_SQL);
            for (final UserSettingEntry setting : settings) {
                stmt.setInt(1, cid);
                stmt.setInt(2, uid);
                stmt.setInt(3, setting.getPathId());

                final String value = setting.getValue();
                if (value == null) {
                    stmt.setNull(4, java.sql.Types.VARCHAR);
                } else {
                    stmt.setString(4, value);
                }

                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (final SQLException e) {
            throw UserCopyExceptionCodes.SQL_PROBLEM.create(e);
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    @Override
    public void done(final Map<String, ObjectMapping<?>> copied, final boolean failed) {

    }
}
