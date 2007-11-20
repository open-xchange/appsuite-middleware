package com.openexchange.groupware.update.tasks;

import com.openexchange.database.Database;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.exception.Classes;
import com.openexchange.groupware.update.exception.SchemaExceptionFactory;
import com.openexchange.groupware.update.exception.SchemaException;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.tools.file.FileStorageException;
import com.openexchange.tools.file.LocalFileStorage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

@OXExceptionSource(
	    classId = Classes.UPDATE_TASK,
	    component = Component.UPDATE
	)
public class ClearLeftoverAttachmentsUpdateTask implements UpdateTask {

    private ThreadLocal<Map<Integer,LocalFileStorage>> filestorages = new ThreadLocal<Map<Integer,LocalFileStorage>>();

    private static final Log LOG = LogFactory.getLog(ClearLeftoverAttachmentsUpdateTask.class);
    private static final SchemaExceptionFactory EXCEPTIONS =
        new SchemaExceptionFactory(ClearLeftoverAttachmentsUpdateTask.class);

    public int addedWithVersion() {
        return 11;
    }

    public int getPriority() {
        return UpdateTaskPriority.NORMAL.priority;
    }

    @OXThrowsMultiple(category = { AbstractOXException.Category.CODE_ERROR,AbstractOXException.Category.SETUP_ERROR }, desc = { "" }, exceptionId = { 1,2 }, msg = { "An SQL error occurred while performing task ClearLeftoverAttachmentsUpdateTask: %1$s.", "Can't resolve filestore." })
    public void perform(Schema schema, int contextId) throws AbstractOXException {
        try {
            filestorages.set(new HashMap<Integer,LocalFileStorage>());
            for(LeftoverAttachment att : getLeftoverAttachmentsInSchema(contextId, schema)){
                removeFile(att.getFileId(), att.getContextId()); //FIXME will not work during update
                try {
                    removeDatabaseEntry(att.getId(),att.getContextId());
                } catch (SQLException e) {
                    throw EXCEPTIONS.create(1, e, e.getMessage());
                }
            }
        } catch (SQLException e) {
            throw EXCEPTIONS.create(1, e, e.getMessage());
        } finally {
            filestorages.set(null);
        }
    }

    private void removeDatabaseEntry(int id, int contextId) throws DBPoolingException, SQLException {
        update(contextId, "DELETE FROM prg_attachment WHERE id = ? and cid = ?", id, contextId);
    }

    private void update(int contextId, String sql, Object...args) throws DBPoolingException, SQLException {
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


		} catch (SQLException x) {
			try {
				writeCon.rollback();
			} catch (SQLException x2) {
				LOG.error("Can't execute rollback.", x2);
			}
			throw x;
		} finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException x) {
					LOG.warn("Couldn't close statement", x);
				}
			}

			if(writeCon != null) {
				try {
					writeCon.setAutoCommit(true);
				} catch (SQLException x){
					LOG.warn("Can't reset auto commit", x);
				}

				if(writeCon != null) {
					Database.back(contextId, true, writeCon);
				}
			}   
        }
    }

    private void removeFile(String fileId,int ctx_id) throws SQLException, DBPoolingException, FileStorageException, SchemaException {
        // We have to use the local file storage to bypass quota handling, which must remain
        // unaffected by these operations

        LocalFileStorage fs = filestorages.get().get(ctx_id);
        if(fs == null) {
            URI uri = createURI(ctx_id);
            if(uri == null) {
                throw EXCEPTIONS.create(2);
            }

            fs = new LocalFileStorage(3,256,uri);  //FIXME: It's very dangerous to just copy these values (3 and 256)!
            filestorages.get().put(ctx_id, fs);
        }
        try {
            fs.deleteFile(fileId);
        } catch (FileStorageException x) {
            LOG.warn("Could not delete "+fileId+ "in context "+ctx_id+". The file might be gone already.");
        }
    }

    private URI createURI(int ctx_id) throws DBPoolingException, SQLException {
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

            String filestore_name = rs.getString(2);

            int filestore_id = rs.getInt(1);

            rs.close();
            stmt.close();

            stmt = readCon.prepareStatement("SELECT uri FROM filestore WHERE id = ?");
            stmt.setInt(1,filestore_id);

            rs = stmt.executeQuery();

            if(!rs.next()) {
                LOG.error("Context "+ctx_id+" doesn't seem to have a proper filestore");
                return null;
            }
            String uri_string = rs.getString(1);
            URI uri = new URI(uri_string);

            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath()
                + '/' + filestore_name, uri.getQuery(),
                uri.getFragment());


        } catch (URISyntaxException e) {
            LOG.error(e);
            return null;
        } finally {
			if(stmt != null) {
				try {
					stmt.close();
				} catch (SQLException x) {
					LOG.warn("Couldn't close statement", x);
				}
			}

            if(rs != null) {
                try {
                    rs.close();
                } catch (SQLException x) {
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

    private List<LeftoverAttachment> getLeftoverAttachmentsInSchema(int contextId, Schema schema) throws SQLException, DBPoolingException {

        String query = "SELECT prg_attachment.cid, prg_attachment.id, prg_attachment.file_id FROM prg_attachment " +
                "JOIN sequence_attachment ON prg_attachment.cid = sequence_attachment.cid  WHERE prg_attachment.id > sequence_attachment.id";

        List<LeftoverAttachment> attachments = new ArrayList<LeftoverAttachment>();
        
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
				} catch (SQLException x) {
					LOG.warn("Couldn't close statement", x);
				}
			}

            if(rs != null) {
                try {
                    rs.close();
                } catch (SQLException x) {
                    LOG.warn("Couldn't close result set");
                }
            }

            if(readCon != null) {
				if(readCon != null) {
					Database.back(contextId, false, readCon);
				}
			}
        }
    }

    private class LeftoverAttachment {
        String fileId;
        int id;
        int contextId;

        private LeftoverAttachment(String fileId, int id, int contextId) {
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
