package com.openexchange.groupware.update.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.databaseold.Database;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.exception.Classes;
import com.openexchange.groupware.update.exception.SchemaExceptionFactory;
@OXExceptionSource(
	    classId = Classes.UPDATE_TASK,
	    component = EnumComponent.UPDATE
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
    public void perform(final Schema schema, final int contextId) throws AbstractOXException {
        Connection writeCon = null;
        PreparedStatement stmt = null;
        final PreparedStatement checkAvailable = null;
        final ResultSet rs = null;

        try {
            writeCon = Database.get(contextId, true);
            writeCon.setAutoCommit(false);
            stmt = writeCon.prepareStatement("ALTER TABLE infostore_document MODIFY url varchar(256)");
            stmt.executeUpdate();
            stmt.close();
            stmt = writeCon.prepareStatement("ALTER TABLE del_infostore_document MODIFY url varchar(256)");
            stmt.executeUpdate();
            writeCon.commit();
        } catch (final SQLException x) {
            try {
                writeCon.rollback();
            } catch (final SQLException x2) {
                LOG.error("Can't execute rollback.", x2);
            }
            EXCEPTIONS.create(1, x);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (final SQLException x) {
                    LOG.warn("Couldn't close statement", x);
                }
            }

            if (null != rs) {
                try {
                    rs.close();
                } catch (final SQLException x) {
                    LOG.warn("Couldn't close result set", x);
                }
            }

            if (writeCon != null) {
                try {
                    writeCon.setAutoCommit(true);
                } catch (final SQLException x) {
                    LOG.warn("Can't reset auto commit", x);
                }

                if (writeCon != null) {
                    Database.back(contextId, true, writeCon);
                }
            }
        }
    }
}
