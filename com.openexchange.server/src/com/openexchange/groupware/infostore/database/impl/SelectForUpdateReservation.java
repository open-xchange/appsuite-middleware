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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.groupware.infostore.database.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.apache.commons.logging.Log;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.StaticDBPoolProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.InfostoreFilenameReservation;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.log.LogFactory;

/**
 * {@link SelectForUpdateReservation}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SelectForUpdateReservation implements InfostoreFilenameReservation {

    private static final String REMOVE_RESERVATION_SQL = "DELETE FROM infostoreReservedPaths WHERE cid = ? AND folder = ? AND name = ?";

    private static final String LOCK_FOLDER_SQL = "SELECT fuid FROM oxfolder_tree WHERE cid = ? AND fuid = ? FOR UPDATE ";

    private static final String RESERVE_NAME_SQL = "INSERT INTO infostoreReservedPaths (uuid, cid, folder, name) VALUES (?, ?, ?, ?)";

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SelectForUpdateReservation.class));

    private final String fileName;

    private final long folderId;

    private final int id;

    private final Context ctx;

    private final DBProvider provider;

    private Connection con;

    private boolean reserved;

	private boolean wasAdjusted;

    public SelectForUpdateReservation(final String fileName, final long folderId, final int id, final Context context, final DBProvider provider) {
        super();
        this.fileName = fileName;
        this.folderId = folderId;
        this.id = id;
        this.ctx = context;
        this.provider = provider;
    }

    @Override
    public void destroySilently() {
        try {
            openConnection();
            removeReservation();
        } catch (final Exception x) {
            LOG.error(x.getMessage(), x);
        } finally {
            releaseConnection();
        }
    }

    private void releaseConnection() {
        provider.releaseWriteConnection(ctx, con);
    }

    private void openConnection() throws OXException {
        con = provider.getWriteConnection(ctx);
    }

    public boolean reserve() throws SQLException, OXException {
        if (reserved) {
            return true;
        }
        if (!mustReserveName()) {
            return true;
        }
        boolean free = false;
        boolean rollback = false;
        try {
            startTransaction();
            rollback = true;
            lockFolder();
            free = checkFree();
            if (free) {
                reserveFilename();
            }
            commit();
            rollback = false;
        } catch (final SQLException x) {
            throw x;
        } finally {
            if (rollback) {
                rollback();
            }
            finishTransaction();
        }
        return reserved = free;
    }

    protected boolean mustReserveName() {
        return !Strings.isEmpty(fileName);
    }

    private void finishTransaction() {
        Databases.autocommit(con);
        releaseConnection();
    }

    private void rollback() {
        Databases.rollback(con);
    }

    private void commit() throws SQLException {
        con.commit();
    }

    private void reserveFilename() throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(RESERVE_NAME_SQL);
            int pos = 1;
            stmt.setBytes(pos++, UUIDs.toByteArray(UUID.randomUUID()));
            stmt.setLong(pos++, ctx.getContextId());
            stmt.setLong(pos++, folderId);
            stmt.setString(pos, fileName);
            stmt.executeUpdate();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private boolean checkFree() throws OXException, SQLException {

        InfostoreIterator iter = null;
        try {
            iter = InfostoreIterator.documentsByFilename(
                folderId,
                fileName,
                new Metadata[] { Metadata.ID_LITERAL, Metadata.TITLE_LITERAL },
                new StaticDBPoolProvider(con),
                ctx);
            while (iter.hasNext()) {
                final DocumentMetadata dm = iter.next();
                if (dm.getId() != id) {
                    return false;
                }
            }
        } finally {
            if (iter != null) {
                iter.close();
            }
        }

        return !hasResult(
            "SELECT 1 FROM infostoreReservedPaths WHERE cid = ? AND folder = ? AND name = ?",
            ctx.getContextId(),
            folderId,
            fileName);
    }

    private void lockFolder() throws SQLException {
        exec(LOCK_FOLDER_SQL, ctx.getContextId(), folderId);
    }

    private void removeReservation() throws SQLException {
        if(!reserved) {
            return;
        }
        exec(REMOVE_RESERVATION_SQL, ctx.getContextId(), folderId, fileName);
    }

    private void exec(final String sql, final Object... replacements) throws SQLException {
        final PreparedStatement stmt = con.prepareStatement(sql);
        try {
            for (int i = 0; i < replacements.length; i++) {
                stmt.setObject(i + 1, replacements[i]);
            }
            stmt.execute();
        } finally {
            stmt.close();
        }
    }

    private boolean hasResult(final String sql, final Object... replacements) throws SQLException {
        final PreparedStatement stmt = con.prepareStatement(sql);
        ResultSet rs = null;
        try {
            for (int i = 0; i < replacements.length; i++) {
                stmt.setObject(i + 1, replacements[i]);
            }
            rs = stmt.executeQuery();
            return rs.next();
        } finally {
            stmt.close();
            if (rs != null) {
                rs.close();
            }
        }
    }

    private void startTransaction() throws OXException, SQLException {
        openConnection();
        Databases.startTransaction(con);
    }


    @Override
    public String getFilename() {
        return fileName;
    }

	@Override
	public boolean wasAdjusted() {
		return wasAdjusted;
	}

	@Override
    public void setWasAdjusted(boolean wasAdjusted) {
		this.wasAdjusted = wasAdjusted;
	}



}
