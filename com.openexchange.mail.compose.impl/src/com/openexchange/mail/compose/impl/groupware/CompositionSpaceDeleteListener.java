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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.compose.impl.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCode;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.mail.compose.AttachmentStorageType;
import com.openexchange.mail.compose.impl.attachment.FileStorageAttachmentStorage;
import com.openexchange.mail.compose.impl.security.FileStorageCompositionSpaceKeyStorage;

/**
 * {@link CompositionSpaceDeleteListener}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class CompositionSpaceDeleteListener implements DeleteListener {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(CompositionSpaceDeleteListener.class);
    }

    /**
     * Initializes a new {@link CompositionSpaceDeleteListener}.
     */
    public CompositionSpaceDeleteListener() {
        super();
    }

    @Override
    public void deletePerformed(DeleteEvent event, Connection readCon, Connection writeCon) throws OXException {
        if (DeleteEvent.TYPE_USER == event.getType()) {
            deleteCompositionSpacesFromUser(event.getId(), event.getContext().getContextId(), writeCon);
        } else if (DeleteEvent.TYPE_CONTEXT == event.getType()) {
            deleteCompositionSpacesFromContext(event.getContext().getContextId(), writeCon);
        }
    }

    private void deleteCompositionSpacesFromContext(int contextId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("DELETE FROM compositionSpaceAttachmentBinary WHERE cid=?");
            stmt.setInt(1, contextId);
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;

            QuotaFileStorage fileStorage = FileStorageAttachmentStorage.optFileStorage(contextId);
            if (null != fileStorage) {
                stmt = con.prepareStatement("SELECT refId FROM compositionSpaceAttachmentMeta WHERE cid=? AND refType=?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, AttachmentStorageType.FILE_STORAGE.getType());
                rs = stmt.executeQuery();
                Set<String> storageIdentifers = null;
                if (rs.next()) {
                    storageIdentifers = new HashSet<String>();
                    do {
                        storageIdentifers.add(rs.getString(1));
                    } while (rs.next());
                }
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;

                if (null != storageIdentifers) {
                    try {
                        Set<String> undeletedFiles = fileStorage.deleteFiles(storageIdentifers.toArray(new String[storageIdentifers.size()]));
                        if (null != undeletedFiles && !undeletedFiles.isEmpty()) {
                            LoggerHolder.LOG.warn("Failed to delete the following files on filestore: {}", undeletedFiles);
                        }
                    } catch (Exception e) {
                        LoggerHolder.LOG.warn("Failed to delete files on filestore", e);
                    }
                }
            }

            stmt = con.prepareStatement("DELETE FROM compositionSpaceAttachmentMeta WHERE cid=?");
            stmt.setInt(1, contextId);
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;

            stmt = con.prepareStatement("DELETE FROM compositionSpace WHERE cid=?");
            stmt.setInt(1, contextId);
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;

            if (null != fileStorage) {
                FileStorageCompositionSpaceKeyStorage keyStorage = FileStorageCompositionSpaceKeyStorage.getInstance();
                if (null != keyStorage) {
                    keyStorage.clearCache();
                }

                stmt = con.prepareStatement("SELECT refId FROM compositionSpaceKeyStorage WHERE cid=?");
                stmt.setInt(1, contextId);
                rs = stmt.executeQuery();
                Set<String> storageIdentifers = null;
                if (rs.next()) {
                    storageIdentifers = new HashSet<String>();
                    do {
                        storageIdentifers.add(rs.getString(1));
                    } while (rs.next());
                }
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;

                if (null != storageIdentifers) {
                    try {
                        Set<String> undeletedFiles = fileStorage.deleteFiles(storageIdentifers.toArray(new String[storageIdentifers.size()]));
                        if (null != undeletedFiles && !undeletedFiles.isEmpty()) {
                            LoggerHolder.LOG.warn("Failed to delete the following files on filestore: {}", undeletedFiles);
                        }
                    } catch (Exception e) {
                        LoggerHolder.LOG.warn("Failed to delete files on filestore", e);
                    }
                }
            }

            stmt = con.prepareStatement("DELETE FROM compositionSpaceKeyStorage WHERE cid=?");
            stmt.setInt(1, contextId);
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;
        } catch (SQLException e) {
            throw DeleteFailedExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private void deleteCompositionSpacesFromUser(int userId, int contextId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("DELETE FROM compositionSpaceAttachmentBinary WHERE cid=? AND user=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;

            QuotaFileStorage fileStorage = FileStorageAttachmentStorage.optFileStorage(contextId);
            if (null != fileStorage) {
                stmt = con.prepareStatement("SELECT refId FROM compositionSpaceAttachmentMeta WHERE cid=? AND user=? AND refType=?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.setInt(3, AttachmentStorageType.FILE_STORAGE.getType());
                rs = stmt.executeQuery();
                Set<String> storageIdentifers = null;
                if (rs.next()) {
                    storageIdentifers = new HashSet<String>();
                    do {
                        storageIdentifers.add(rs.getString(1));
                    } while (rs.next());
                }
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;

                if (null != storageIdentifers) {
                    try {
                        Set<String> undeletedFiles = fileStorage.deleteFiles(storageIdentifers.toArray(new String[storageIdentifers.size()]));
                        if (null != undeletedFiles && !undeletedFiles.isEmpty()) {
                            LoggerHolder.LOG.warn("Failed to delete the following files on filestore: {}", undeletedFiles);
                        }
                    } catch (Exception e) {
                        LoggerHolder.LOG.warn("Failed to delete files on filestore", e);
                    }
                }
            }

            stmt = con.prepareStatement("DELETE FROM compositionSpaceAttachmentMeta WHERE cid=? AND user=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;

            stmt = con.prepareStatement("DELETE FROM compositionSpace WHERE cid=? AND user=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;

            if (null != fileStorage) {
                FileStorageCompositionSpaceKeyStorage keyStorage = FileStorageCompositionSpaceKeyStorage.getInstance();
                if (null != keyStorage) {
                    keyStorage.clearCache();
                }

                stmt = con.prepareStatement("SELECT refId FROM compositionSpaceKeyStorage WHERE cid=? AND user=?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                rs = stmt.executeQuery();
                Set<String> storageIdentifers = null;
                if (rs.next()) {
                    storageIdentifers = new HashSet<String>();
                    do {
                        storageIdentifers.add(rs.getString(1));
                    } while (rs.next());
                }
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;

                if (null != storageIdentifers) {
                    try {
                        Set<String> undeletedFiles = fileStorage.deleteFiles(storageIdentifers.toArray(new String[storageIdentifers.size()]));
                        if (null != undeletedFiles && !undeletedFiles.isEmpty()) {
                            LoggerHolder.LOG.warn("Failed to delete the following files on filestore: {}", undeletedFiles);
                        }
                    } catch (Exception e) {
                        LoggerHolder.LOG.warn("Failed to delete files on filestore", e);
                    }
                }
            }

            stmt = con.prepareStatement("DELETE FROM compositionSpaceKeyStorage WHERE cid=? AND user=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;
        } catch (SQLException e) {
            throw DeleteFailedExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

}
