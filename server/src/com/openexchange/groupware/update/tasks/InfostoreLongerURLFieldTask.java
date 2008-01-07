package com.openexchange.groupware.update.tasks;

import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.exception.Classes;
import com.openexchange.groupware.update.exception.SchemaExceptionFactory;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXThrows;
import com.openexchange.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
@OXExceptionSource(
	    classId = Classes.UPDATE_TASK,
	    component = Component.UPDATE
	)
public class InfostoreLongerURLFieldTask  implements UpdateTask {

    private final Log LOG = LogFactory.getLog(InfostoreLongerURLFieldTask.class);
    private static final SchemaExceptionFactory EXCEPTIONS =
        new SchemaExceptionFactory(InfostoreLongerURLFieldTask.class);

    public int addedWithVersion() {
        return 12;
    }

    public int getPriority() {
        return UpdateTask.UpdateTaskPriority.NORMAL.priority;
    }
    @OXThrows(
            category = AbstractOXException.Category.CODE_ERROR,
            desc = "",
            msg = "Error in SQL Statement",
            exceptionId = 1
    )
    public void perform(Schema schema, int contextId) throws AbstractOXException {
        Connection writeCon = null;
        PreparedStatement stmt = null;
        PreparedStatement checkAvailable = null;
        ResultSet rs = null;

        try {
            writeCon = Database.get(contextId, true);
            writeCon.setAutoCommit(false);
            stmt = writeCon.prepareStatement("ALTER TABLE infostore_document MODIFY url varchar(256)");
            stmt.executeUpdate();
            stmt.close();
            stmt = writeCon.prepareStatement("ALTER TABLE del_infostore_document MODIFY url varchar(256)");
            stmt.executeUpdate();
            writeCon.commit();
        } catch (SQLException x) {
            try {
                writeCon.rollback();
            } catch (SQLException x2) {
                LOG.error("Can't execute rollback.", x2);
            }
            EXCEPTIONS.create(1, x);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException x) {
                    LOG.warn("Couldn't close statement", x);
                }
            }

            if (null != rs) {
                try {
                    rs.close();
                } catch (SQLException x) {
                    LOG.warn("Couldn't close result set", x);
                }
            }

            if (writeCon != null) {
                try {
                    writeCon.setAutoCommit(true);
                } catch (SQLException x) {
                    LOG.warn("Can't reset auto commit", x);
                }

                if (writeCon != null) {
                    Database.back(contextId, true, writeCon);
                }
            }
        }
    }
}
