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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.api2.OXException;
import com.openexchange.database.DBPoolingException;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.StaticDBPoolProvider;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreException;
import com.openexchange.groupware.infostore.database.InfostoreFilenameReservation;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.tools.iterator.SearchIteratorException;

/**
 * {@link SelectForUpdateReservation}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SelectForUpdateReservation implements InfostoreFilenameReservation {

    private static final String REMOVE_RESERVATION_SQL = "DELETE FROM infostoreReservedPaths WHERE cid = ? AND folder = ? AND name = ?";

    private static final String LOCK_FOLDER_SQL = "SELECT fuid FROM oxfolder_tree WHERE cid = ? AND fuid = ? FOR UPDATE ";

    private static final String RESERVE_NAME_SQL = "INSERT INTO infostoreReservedPaths (cid, folder, name) VALUES (?, ?, ?)";

    private static final Log LOG = com.openexchange.exception.Log.valueOf(LogFactory.getLog(SelectForUpdateReservation.class));

    private String fileName;

    private long folderId;

    private int id;

    private Context ctx;

    private DBProvider provider;

    private Connection con;

    private boolean reserved;

    public SelectForUpdateReservation(String fileName, long folderId, int id, Context context, DBProvider provider) {
        super();
        this.fileName = fileName;
        this.folderId = folderId;
        this.id = id;
        this.ctx = context;
        this.provider = provider;
    }

    public void destroySilently() {
        try {
            openConnection();
            removeReservation();
        } catch (Exception x) {
            LOG.error(x.getMessage(), x);
        } finally {
            releaseConnection();
        }
    }

    private void releaseConnection() {
        provider.releaseWriteConnection(ctx, con);
    }

    private void openConnection() throws DBPoolingException {
        con = provider.getWriteConnection(ctx);
    }

    public boolean reserve() throws SQLException, OXException {
        if(reserved) {
            return true;
        }
        if (!mustReserveName()) {
            return true;
        }
        boolean free = false;
        try {
            startTransaction();
            lockFolder();
            free = checkFree();
            if (free) {
                reserveFilename();
            }
            commit();
        } catch (SQLException x) {
            rollback();
            throw x;
        } catch (DBPoolingException e) {
            throw new InfostoreException(e);
        } finally {
            finishTransaction();
        }
        return reserved = free;
    }

    protected boolean mustReserveName() {
        if (null == fileName) {
            return false;
        }
        if ("".equals(fileName.trim())) {
            return false;
        }
        return true;
    }

    private void finishTransaction() throws SQLException {
        con.setAutoCommit(true);
        releaseConnection();
    }

    private void rollback() throws SQLException {
        con.rollback();
    }

    private void commit() throws SQLException {
        con.commit();
    }

    private void reserveFilename() throws SQLException {
        exec(RESERVE_NAME_SQL, ctx.getContextId(), folderId, fileName);
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
        } catch (final SearchIteratorException e) {
            throw new InfostoreException(e);
        } finally {
            if (iter != null) {
                try {
                    iter.close();
                } catch (final SearchIteratorException e) {
                    throw new InfostoreException(e);
                }
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

    private void exec(String sql, Object... replacements) throws SQLException {
        PreparedStatement stmt = con.prepareStatement(sql);
        try {
            for (int i = 0; i < replacements.length; i++) {
                stmt.setObject(i + 1, replacements[i]);
            }
            stmt.execute();
        } finally {
            stmt.close();
        }
    }

    private boolean hasResult(String sql, Object... replacements) throws SQLException {
        PreparedStatement stmt = con.prepareStatement(sql);
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

    private void startTransaction() throws DBPoolingException, SQLException {
        openConnection();
        con.setAutoCommit(false);
    }
    
    
    public String getFilename() {
        return fileName;
    }

}
