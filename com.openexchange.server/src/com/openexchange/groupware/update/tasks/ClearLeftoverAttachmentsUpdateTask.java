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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.java.Autoboxing.I;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.log.LogFactory;
import com.openexchange.tools.file.FileStorage;

public class ClearLeftoverAttachmentsUpdateTask implements UpdateTask {

    private final ThreadLocal<Map<Integer, FileStorage>> filestorages = new ThreadLocal<Map<Integer,FileStorage>>();

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ClearLeftoverAttachmentsUpdateTask.class));

    @Override
    public int addedWithVersion() {
        return 11;
    }

    @Override
    public int getPriority() {
        return UpdateTaskPriority.NORMAL.priority;
    }

    @Override
    public void perform(final Schema schema, final int contextId) throws OXException {
        try {
            filestorages.set(new HashMap<Integer, FileStorage>());
            for(final LeftoverAttachment att : getLeftoverAttachmentsInSchema(contextId)){
                removeFile(att.getFileId(), att.getContextId()); //FIXME will not work during update
                try {
                    removeDatabaseEntry(att.getId(),att.getContextId());
                } catch (final SQLException e) {
                    throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
                }
            }
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            filestorages.set(null);
        }
    }

    private void removeDatabaseEntry(final int id, final int contextId) throws OXException, SQLException {
        update(contextId, "DELETE FROM prg_attachment WHERE id = ? and cid = ?", I(id), I(contextId));
    }

    private void update(final int contextId, final String sql, final Object...args) throws OXException, SQLException {
        Connection writeCon = null;
        PreparedStatement stmt = null;

        try {
            writeCon = Database.get(contextId, true);
            writeCon.setAutoCommit(false);
            stmt = writeCon.prepareStatement(sql);
            for(int i = 0; i < args.length; i++) {
                stmt.setObject(i+1, args[i]);
            }
            stmt.executeUpdate();


        } catch (final SQLException x) {
            try {
                writeCon.rollback();
            } catch (final SQLException x2) {
                LOG.error("Can't execute rollback.", x2);
            }
            throw x;
        } finally {
            if(stmt != null) {
                try {
                    stmt.close();
                } catch (final SQLException x) {
                    LOG.warn("Couldn't close statement", x);
                }
            }

            if(writeCon != null) {
                try {
                    writeCon.setAutoCommit(true);
                } catch (final SQLException x){
                    LOG.warn("Can't reset auto commit", x);
                }

                Database.back(contextId, true, writeCon);
            }
        }
    }

    private void removeFile(final String fileId,final int ctx_id) throws SQLException, OXException, OXException, OXException {
        // We have to use the local file storage to bypass quota handling, which must remain
        // unaffected by these operations

        FileStorage fs = filestorages.get().get(I(ctx_id));
        if(fs == null) {
            final URI uri = createURI(ctx_id);
            if (uri == null) {
                throw UpdateExceptionCodes.OTHER_PROBLEM.create("Can not determine filestore for context " + ctx_id + ".");
            }

            //fs = new LocalFileStorage(I(3),I(256),uri);  //FIXME: It's very dangerous to just copy these values (3 and 256)!
            fs = FileStorage.getInstance(uri);
            filestorages.get().put(I(ctx_id), fs);
        }
        try {
            fs.deleteFile(fileId);
        } catch (final OXException x) {
            LOG.warn("Could not delete "+fileId+ "in context "+ctx_id+". The file might be gone already.");
        }
    }

    private URI createURI(final int ctx_id) throws OXException, SQLException {
        // We need to select the filestore URI and the context subpath from the DB
        // We can't use the API, because the ContextStorage will throw exceptions
        // when we try to load a Context during the update process;
        Connection readCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            readCon = Database.get(false);
            stmt = readCon.prepareStatement("SELECT filestore_id, filestore_name FROM context WHERE cid = ?");
            stmt.setInt(1, ctx_id);

            rs = stmt.executeQuery();

            if(!rs.next()) {
                LOG.error("Context "+ctx_id+" doesn't seem to have a proper filestore");
                return null;
            }

            final String filestore_name = rs.getString(2);

            final int filestore_id = rs.getInt(1);

            rs.close();
            stmt.close();

            stmt = readCon.prepareStatement("SELECT uri FROM filestore WHERE id = ?");
            stmt.setInt(1,filestore_id);

            rs = stmt.executeQuery();

            if(!rs.next()) {
                LOG.error("Context "+ctx_id+" doesn't seem to have a proper filestore");
                return null;
            }
            final String uri_string = rs.getString(1);
            final URI uri = new URI(uri_string);

            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath()
                + '/' + filestore_name, uri.getQuery(),
                uri.getFragment());


        } catch (final URISyntaxException e) {
            LOG.error(e.getMessage(), e);
            return null;
        } finally {
            if(stmt != null) {
                try {
                    stmt.close();
                } catch (final SQLException x) {
                    LOG.warn("Couldn't close statement", x);
                }
            }

            if(rs != null) {
                try {
                    rs.close();
                } catch (final SQLException x) {
                    LOG.warn("Couldn't close result set");
                }
            }

            if(readCon != null) {
                if(readCon != null) {
                    Database.back(false, readCon);
                }
            }
        }
    }

    private List<LeftoverAttachment> getLeftoverAttachmentsInSchema(final int contextId) throws SQLException, OXException {

        final String query = "SELECT prg_attachment.cid, prg_attachment.id, prg_attachment.file_id FROM prg_attachment " +
                "JOIN sequence_attachment ON prg_attachment.cid = sequence_attachment.cid  WHERE prg_attachment.id > sequence_attachment.id";

        final List<LeftoverAttachment> attachments = new ArrayList<LeftoverAttachment>();

        Connection readCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            readCon = Database.get(contextId, false);
            stmt = readCon.prepareStatement(query);
            rs = stmt.executeQuery();
            while(rs.next()) {
                attachments.add(new LeftoverAttachment(rs.getString(3), rs.getInt(2), rs.getInt(1)));
            }

            return attachments;
        } finally {
            if(stmt != null) {
                try {
                    stmt.close();
                } catch (final SQLException x) {
                    LOG.warn("Couldn't close statement", x);
                }
            }

            if(rs != null) {
                try {
                    rs.close();
                } catch (final SQLException x) {
                    LOG.warn("Couldn't close result set");
                }
            }

            if(readCon != null) {
                Database.back(contextId, false, readCon);
            }
        }
    }

    private class LeftoverAttachment {
        String fileId;
        int id;
        int contextId;

        LeftoverAttachment(final String fileId, final int id, final int contextId) {
            this.fileId = fileId;
            this.id = id;
            this.contextId = contextId;
        }

        public String getFileId() {
            return fileId;
        }

        public int getId() {
            return id;
        }

        public int getContextId(){
            return contextId;
        }

    }
}
