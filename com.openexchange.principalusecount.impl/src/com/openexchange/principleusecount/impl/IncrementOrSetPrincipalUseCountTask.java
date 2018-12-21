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
 * {@link IncrementOrSetPrincipalUseCountTask} increments or sets the principal use count for a given user
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.2
 */
class IncrementOrSetPrincipalUseCountTask extends AbstractTask<Void> {

    private static final Logger LOG = LoggerFactory.getLogger(IncrementOrSetPrincipalUseCountTask.class);
    private static final String SQL_INCREMENT = "INSERT INTO principalUseCount (cid, user, principal, value) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE value=value+1";
    private static final String SQL_SET = "UPDATE principalUseCount SET value = ? WHERE cid = ? AND user = ? AND principal = ?";

    private final Session session;
    private final int principal;
    private final Integer value;

    /**
     * Initializes a new {@link IncrementOrSetPrincipalUseCountTask}.
     */
    IncrementOrSetPrincipalUseCountTask(Session session, int principalId) {
        super();
        this.principal = principalId;
        this.session = session;
        this.value = null;
    }

    /**
     * Initializes a new {@link IncrementOrSetPrincipalUseCountTask}.
     */
    IncrementOrSetPrincipalUseCountTask(Session session, int principalId, Integer value) {
        super();
        this.principal = principalId;
        this.session = session;
        this.value = value;
    }

    @Override
    public Void call() throws OXException {
        DatabaseService dbService = Services.getService(DatabaseService.class);
        if (null == dbService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class);
        }
        Connection con = dbService.getWritable(session.getContextId());
        try {

            if (value == null) {
                try (PreparedStatement stmt = con.prepareStatement(SQL_INCREMENT)) {
                    stmt.setInt(1, session.getContextId());
                    stmt.setInt(2, session.getUserId());
                    stmt.setInt(3, principal);
                    stmt.setInt(4, 1);
                    stmt.executeUpdate();
                    LOG.debug("Incremented principal use count for user {}, principal {} in context {}.", session.getUserId(), principal, session.getContextId());
                } catch (SQLException e) {
                    throw PrincipalUseCountExceptionCode.SQL_ERROR.create(e, e.getMessage());
                }
            } else {
                try (PreparedStatement stmt = con.prepareStatement(SQL_SET)) {
                    stmt.setInt(1, value);
                    stmt.setInt(2, session.getContextId());
                    stmt.setInt(3, session.getUserId());
                    stmt.setInt(4, principal);
                    stmt.executeUpdate();
                    LOG.debug("Changed principal use count for user {}, principal {} in context {} to value {}.", session.getUserId(), principal, session.getContextId(), value);
                } catch (SQLException e) {
                    throw PrincipalUseCountExceptionCode.SQL_ERROR.create(e, e.getMessage());
                }
            }
        } finally {
            dbService.backWritable(session.getContextId(), con);
        }
        return null;
    }
}