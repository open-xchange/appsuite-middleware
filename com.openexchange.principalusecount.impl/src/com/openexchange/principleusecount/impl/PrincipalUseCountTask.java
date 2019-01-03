package com.openexchange.principleusecount.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.principleusecount.impl.osgi.Services;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.threadpool.AbstractTask;

/**
 *
 * {@link PrincipalUseCountTask} increments, sets or deletes the principal use count for a given user
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
class PrincipalUseCountTask extends AbstractTask<Void> {

    enum TaskType {
        INCREMENT,
        SET,
        DELETE
    }

    private static final Logger LOG = LoggerFactory.getLogger(PrincipalUseCountTask.class);
    private static final String SQL_INCREMENT = "INSERT INTO principalUseCount (cid, user, principal, value) VALUES (?, ?, ?, 1) ON DUPLICATE KEY UPDATE value=value+1";
    private static final String SQL_SET = "UPDATE principalUseCount SET value = ? WHERE cid = ? AND user = ? AND principal = ?";
    private static final String SQL_DELETE = "DELETE FROM principalUseCount WHERE cid = ? AND user = ? AND principal = ?";

    private final Session session;
    private final int principal;
    private final Integer value;
    private final TaskType type;

    /**
     * Initializes a new {@link PrincipalUseCountTask}.
     */
    PrincipalUseCountTask(Session session, int principalId, TaskType type) {
        super();
        this.principal = principalId;
        this.session = session;
        this.value = null;
        this.type = TaskType.INCREMENT.equals(type) ? TaskType.INCREMENT : TaskType.DELETE;
    }

    /**
     * Initializes a new {@link PrincipalUseCountTask}.
     */
    PrincipalUseCountTask(Session session, int principalId, Integer value) {
        super();
        this.principal = principalId;
        this.session = session;
        this.value = value;
        this.type = TaskType.SET;
    }

    @Override
    public Void call() throws OXException {
        DatabaseService dbService = Services.getService(DatabaseService.class);
        if (null == dbService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class);
        }
        Connection con = dbService.getWritable(session.getContextId());
        try {
            int index = 1;
            switch (type) {
                case DELETE:
                    try (PreparedStatement stmt = con.prepareStatement(SQL_DELETE)) {
                        stmt.setInt(index++, session.getContextId());
                        stmt.setInt(index++, session.getUserId());
                        stmt.setInt(index++, principal);
                        stmt.executeUpdate();
                        LOG.debug("Removed principal use count for user {}, principal {} in context {}.", session.getUserId(), principal, session.getContextId(), value);
                    } catch (SQLException e) {
                        throw PrincipalUseCountExceptionCode.SQL_ERROR.create(e, e.getMessage());
                    }
                    break;
                case SET:
                    try (PreparedStatement stmt = con.prepareStatement(SQL_SET)) {
                        stmt.setInt(index++, value);
                        stmt.setInt(index++, session.getContextId());
                        stmt.setInt(index++, session.getUserId());
                        stmt.setInt(index++, principal);
                        stmt.executeUpdate();
                        LOG.debug("Changed principal use count for user {}, principal {} in context {} to value {}.", session.getUserId(), principal, session.getContextId(), value);
                    } catch (SQLException e) {
                        throw PrincipalUseCountExceptionCode.SQL_ERROR.create(e, e.getMessage());
                    }
                    break;
                default:
                case INCREMENT:
                    try (PreparedStatement stmt = con.prepareStatement(SQL_INCREMENT)) {
                        stmt.setInt(index++, session.getContextId());
                        stmt.setInt(index++, session.getUserId());
                        stmt.setInt(index++, principal);
                        stmt.executeUpdate();
                        LOG.debug("Incremented principal use count for user {}, principal {} in context {}.", session.getUserId(), principal, session.getContextId());
                    } catch (SQLException e) {
                        throw PrincipalUseCountExceptionCode.SQL_ERROR.create(e, e.getMessage());
                    }
                    break;
            }
        } finally {
            dbService.backWritable(session.getContextId(), con);
        }
        return null;
    }
}