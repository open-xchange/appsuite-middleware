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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.guest.storage;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.guest.GuestAssignment;
import com.openexchange.guest.GuestExceptionCodes;

/**
 *
 * Database storage implementation for guests
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class RdbGuestStorage extends GuestStorage {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RdbGuestStorage.class);

    /**
     * SQL statement for resolving the internal unique id based on the mail address
     */
    private static final String RESOLVE_GUEST_ID_BY_MAIL = "SELECT id FROM guest WHERE mail_address=?";

    /**
     * SQL statement for resolving the internal unique id based on context and user id
     */
    private static final String RESOLVE_GUEST_ID_BY_CONTEXT_AND_USER = "SELECT guest_id FROM guest2context WHERE cid=? AND uid=?";

    /**
     * SQL statement for getting one assignment made for a user (resolved by mail address, context and user id)<br>
     * <br>
     * Checks if exactly the same user is existing.
     */
    private static final String RESOLVE_GUEST_ASSIGNMENT = "SELECT * FROM guest2context WHERE cid=? AND uid=? AND guest_id=?";

    /**
     * SQL statement for getting assignments made for a user based on the mail address<br>
     * <br>
     * Checks if the given user has assignments.
     */
    private static final String RESOLVE_GUEST_ASSIGNMENTS = "SELECT cid,uid FROM guest2context WHERE guest_id=?";

    /**
     * SQL statement to count assignments that currently exist.
     */
    private static final String RESOLVE_NUMBER_OF_GUEST_ASSIGNMENTS_BY_GUESTID = "SELECT COUNT(*) FROM guest2context WHERE guest_id=?";

    /**
     * SQL statement to insert a new assignment for an existing guest
     */
    private static final String INSERT_GUEST_ASSIGNMENT = "INSERT INTO guest2context (guest_id, cid, uid) VALUES (?, ?, ?)";

    /**
     * SQL statement to insert a new guest for an unknown mail address
     */
    private static final String INSERT_GUEST = "INSERT INTO guest (mail_address) VALUES (?)";

    /**
     * SQL statement for deleting a guest assignment based on the context and user id.
     */
    private static final String DELETE_GUEST_ASSIGNMENT = "DELETE FROM guest2context where guest_id=? AND cid=? AND uid=?";

    /**
     * SQL statement for deleting a guest based on its internal guest id.
     */
    private static final String DELETE_GUEST = "DELETE FROM guest where id=?";

    private final DatabaseService databaseService;

    /**
     * Default constructor.
     */
    public RdbGuestStorage(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int addGuest(String mailAddress) throws OXException {
        Connection connection = null;
        try {
            connection = this.databaseService.getWritable();

            return addGuest(mailAddress, connection);
        } catch (final OXException e) {
            throw GuestExceptionCodes.NO_CONNECTION.create(e);
        } finally {
            databaseService.backWritable(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int addGuest(String mailAddress, Connection connection) throws OXException {
        if (connection == null) {
            return addGuest(mailAddress);
        }

        PreparedStatement statement = null;
        int guestId = NOT_FOUND;
        try {
            statement = connection.prepareStatement(INSERT_GUEST, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, mailAddress);
            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw GuestExceptionCodes.SQL_ERROR.create("Not able to create guest with mail address '" + mailAddress + "' as desired!");
            }
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    guestId = generatedKeys.getInt(1);
                } else {
                    throw GuestExceptionCodes.SQL_ERROR.create("Creating guest with mail address '" + mailAddress + "' failed, no ID obtained!");
                }
            }
        } catch (final SQLException e) {
            throw GuestExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(statement);
        }

        return guestId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addGuestAssignment(int guestId, int contextId, int userId) throws OXException {
        Connection connection = null;
        try {
            connection = this.databaseService.getWritable();

            addGuestAssignment(guestId, contextId, userId, connection);
        } catch (final OXException e) {
            throw GuestExceptionCodes.NO_CONNECTION.create(e);
        } finally {
            databaseService.backWritable(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addGuestAssignment(int guestId, int contextId, int userId, Connection connection) throws OXException {
        if (connection == null) {
            addGuestAssignment(guestId, contextId, userId);
            return;
        }

        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(INSERT_GUEST_ASSIGNMENT);
            statement.setInt(1, guestId);
            statement.setInt(2, contextId);
            statement.setInt(3, userId);

            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                LOG.error("There have been " + affectedRows + " changes for adding guest assignment but there should only be 1. Executed SQL: " + statement.toString());
            }
        } catch (final SQLException e) {
            throw GuestExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(statement);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGuestId(String mailAddress) throws OXException {
        final Connection connection;
        try {
            connection = this.databaseService.getReadOnly();
        } catch (final OXException e) {
            throw GuestExceptionCodes.NO_CONNECTION.create(e);
        }
        PreparedStatement statement = null;
        ResultSet result = null;
        int guestId = NOT_FOUND;
        try {
            statement = connection.prepareStatement(RESOLVE_GUEST_ID_BY_MAIL);
            statement.setString(1, mailAddress);
            result = statement.executeQuery();
            if (result.next()) {
                guestId = result.getInt(1);
            }
        } catch (final SQLException e) {
            throw GuestExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, statement);
            databaseService.backReadOnly(connection);
        }

        return guestId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGuestId(int contextId, int userId) throws OXException {
        final Connection connection;
        try {
            connection = this.databaseService.getReadOnly();
        } catch (final OXException e) {
            throw GuestExceptionCodes.NO_CONNECTION.create(e);
        }
        PreparedStatement statement = null;
        ResultSet result = null;
        int guestId = NOT_FOUND;
        try {
            statement = connection.prepareStatement(RESOLVE_GUEST_ID_BY_CONTEXT_AND_USER);
            statement.setInt(1, contextId);
            statement.setInt(2, userId);
            result = statement.executeQuery();
            if (result.next()) {
                guestId = result.getInt(1);
            }
        } catch (final SQLException e) {
            throw GuestExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, statement);
            databaseService.backReadOnly(connection);
        }

        return guestId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeGuestAssignment(int guestId, int contextId, int userId) throws OXException {
        final Connection connection;
        try {
            connection = this.databaseService.getWritable();
        } catch (final OXException e) {
            throw GuestExceptionCodes.NO_CONNECTION.create(e);
        }

        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(DELETE_GUEST_ASSIGNMENT);
            statement.setInt(1, guestId);
            statement.setInt(2, contextId);
            statement.setInt(3, userId);
            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                LOG.error("There have been " + affectedRows + " changes for removing a guest assignment but there should only be 1. Executed SQL: " + statement.toString());
            }
        } catch (final SQLException e) {
            throw GuestExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(statement);
            databaseService.backWritable(connection);
        }
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public void removeGuest(int guestId) throws OXException {
        final Connection connection;
        try {
            connection = this.databaseService.getWritable();
        } catch (final OXException e) {
            throw GuestExceptionCodes.NO_CONNECTION.create(e);
        }
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(DELETE_GUEST);
            statement.setInt(1, guestId);
            int affectedRows = statement.executeUpdate();

            if (affectedRows != 1) {
                LOG.error("There have been " + affectedRows + " changes for removing a guest but there should only be 1. Executed SQL: " + statement.toString());
            }
        } catch (final SQLException e) {
            throw GuestExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(statement);
            databaseService.backWritable(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfAssignments(int guestId) throws OXException {
        final Connection connection;
        try {
            connection = this.databaseService.getReadOnly();
        } catch (final OXException e) {
            throw GuestExceptionCodes.NO_CONNECTION.create(e);
        }

        int guestAssignments = 0;
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            statement = connection.prepareStatement(RESOLVE_NUMBER_OF_GUEST_ASSIGNMENTS_BY_GUESTID);
            statement.setInt(1, guestId);
            result = statement.executeQuery();
            if (result.next()) {
                guestAssignments = result.getInt(1);
            }
        } catch (final SQLException e) {
            throw GuestExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, statement);
            databaseService.backReadOnly(connection);
        }

        return guestAssignments;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAssignmentExisting(int guestId, int contextId, int userId) throws OXException {
        final Connection connection;
        try {
            connection = this.databaseService.getReadOnly();
        } catch (final OXException e) {
            throw GuestExceptionCodes.NO_CONNECTION.create(e);
        }
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            statement = connection.prepareStatement(RESOLVE_GUEST_ASSIGNMENT);
            statement.setInt(1, contextId);
            statement.setInt(2, userId);
            statement.setInt(3, guestId);
            result = statement.executeQuery();
            if (result.next()) {
                // already existing guest in this context
                return true;
            }
        } catch (final SQLException e) {
            throw GuestExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, statement);
            databaseService.backReadOnly(connection);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Serializable> getGuestAssignments(int guestId) throws OXException {
        final Connection connection;
        try {
            connection = this.databaseService.getReadOnly();
        } catch (final OXException e) {
            throw GuestExceptionCodes.NO_CONNECTION.create(e);
        }

        final List<Serializable> guestAssignments = new ArrayList<Serializable>();
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            statement = connection.prepareStatement(RESOLVE_GUEST_ASSIGNMENTS);
            statement.setInt(1, guestId);
            result = statement.executeQuery();
            while (result.next()) {
                int cid = result.getInt(1);
                int uid = result.getInt(2);
                guestAssignments.add(new GuestAssignment(guestId, cid, uid));
            }
        } catch (final SQLException e) {
            throw GuestExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, statement);
            databaseService.backReadOnly(connection);
        }

        return guestAssignments;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void shutDown() {
        // Nothing to do.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startUp() {
        // Nothing to do.
    }
}
