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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.guest.impl.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.guest.GuestAssignment;
import com.openexchange.guest.impl.storage.GuestStorage;
import com.openexchange.test.mock.MockUtils;

/**
 * {@link RdbGuestStorageTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
@RunWith(PowerMockRunner.class)
public class RdbGuestStorageTest {

    @InjectMocks
    private RdbGuestStorage rdbGuestStorage;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private static final String GUEST_MAIL_ADDRESS = "hotte@example.com";

    private static final long GUEST_ID = 77;
    private static final int CONTEXT_ID = 1;
    private static final int USER_ID = 11;
    private static final String GROUP_ID = "default";
    private static final String GUEST_PASSWORD = "myToppiPasswordi";
    private static final String GUEST_PASSWORD_MECH = "{BCRYPT}";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        Mockito.when(connection.prepareStatement(Matchers.anyString())).thenReturn(preparedStatement);
        Mockito.when(connection.prepareStatement(Matchers.anyString(), Matchers.anyInt())).thenReturn(preparedStatement);
        Mockito.when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
    }

    @Test
    public void testAddGuest_guestAdded_returnNewGuestId() throws OXException, SQLException {
        Mockito.when(preparedStatement.executeUpdate()).thenReturn(1);
        Mockito.when(resultSet.next()).thenReturn(true, false);
        Mockito.when(resultSet.getLong(Matchers.anyInt())).thenReturn(GUEST_ID);

        long addGuest = rdbGuestStorage.addGuest(GUEST_MAIL_ADDRESS, GROUP_ID, connection);

        Assert.assertEquals(GUEST_ID, addGuest);
        Mockito.verify(preparedStatement, Mockito.times(1)).executeUpdate();
        Mockito.verify(connection, Mockito.times(1)).prepareStatement(RdbGuestStorage.INSERT_GUEST, Statement.RETURN_GENERATED_KEYS);
    }

    @Test(expected = OXException.class)
    public void testAddGuest_noRowAffected_throwOxException() throws OXException, SQLException {
        Mockito.when(preparedStatement.executeUpdate()).thenReturn(0);

        rdbGuestStorage.addGuest(GUEST_MAIL_ADDRESS, GROUP_ID, connection);
    }

    @Test
    public void testGetGuestAssignments_noResultFound_returnEmptyList() throws OXException, SQLException {
        Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);

        List<GuestAssignment> guestAssignments = rdbGuestStorage.getGuestAssignments(GUEST_ID, connection);

        Assert.assertEquals(0, guestAssignments.size());

        Mockito.verify(preparedStatement, Mockito.times(1)).setLong(1, GUEST_ID);
        Mockito.verify(preparedStatement, Mockito.times(1)).executeQuery();
        Mockito.verify(connection, Mockito.times(1)).prepareStatement(RdbGuestStorage.RESOLVE_GUEST_ASSIGNMENTS);
    }

    @Test
    public void testGetGuestAssignments_foundTwoResults_returnList() throws OXException, SQLException {
        Mockito.when(resultSet.next()).thenReturn(true, true, false);
        Mockito.when(resultSet.next()).thenReturn(true, true, false);
        Mockito.when(resultSet.getInt(1)).thenReturn(1, 2);
        Mockito.when(resultSet.getInt(2)).thenReturn(11, 12);

        Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);

        List<GuestAssignment> guestAssignments = rdbGuestStorage.getGuestAssignments(GUEST_ID, connection);

        Assert.assertEquals(2, guestAssignments.size());
        Assert.assertEquals(1, guestAssignments.get(0).getContextId());
        Assert.assertEquals(11, guestAssignments.get(0).getUserId());
        Assert.assertEquals(2, guestAssignments.get(1).getContextId());
        Assert.assertEquals(12, guestAssignments.get(1).getUserId());

        Mockito.verify(preparedStatement, Mockito.times(1)).setLong(1, GUEST_ID);
        Mockito.verify(preparedStatement, Mockito.times(1)).executeQuery();
        Mockito.verify(connection, Mockito.times(1)).prepareStatement(RdbGuestStorage.RESOLVE_GUEST_ASSIGNMENTS);
    }

    @Test(expected = OXException.class)
    public void testAddGuestAssignment_connectionNull_throwException() throws OXException, SQLException {
        rdbGuestStorage.addGuestAssignment(new GuestAssignment(GUEST_ID, CONTEXT_ID, USER_ID, GUEST_PASSWORD, GUEST_PASSWORD_MECH), null);
    }

    @Test
    public void testAddGuestAssignment_assignmentAdded() throws OXException, SQLException {
        Mockito.when(preparedStatement.executeUpdate()).thenReturn(1);

        rdbGuestStorage.addGuestAssignment(new GuestAssignment(GUEST_ID, CONTEXT_ID, USER_ID, GUEST_PASSWORD, GUEST_PASSWORD_MECH), connection);

        Mockito.verify(preparedStatement, Mockito.times(1)).setLong(1, GUEST_ID);
        Mockito.verify(preparedStatement, Mockito.times(1)).setInt(2, CONTEXT_ID);
        Mockito.verify(preparedStatement, Mockito.times(1)).setInt(3, USER_ID);
    }

    @Test
    public void testGetGuestIdViaMail_guestNotFound_returnNotFound() throws OXException, SQLException {
        Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(false);

        long guestId = rdbGuestStorage.getGuestId(GUEST_MAIL_ADDRESS, GROUP_ID, connection);

        Assert.assertEquals(GuestStorage.NOT_FOUND, guestId);

        Mockito.verify(preparedStatement, Mockito.times(1)).setString(1, GUEST_MAIL_ADDRESS);
        Mockito.verify(connection, Mockito.times(1)).prepareStatement(RdbGuestStorage.RESOLVE_GUEST_ID_BY_MAIL);
    }

    @Test
    public void testGetGuestIdViaMail_guestFound_returnId() throws OXException, SQLException {
        Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(true);
        Mockito.when(resultSet.getLong(1)).thenReturn(GUEST_ID);

        long guestId = rdbGuestStorage.getGuestId(GUEST_MAIL_ADDRESS, GROUP_ID, connection);

        Assert.assertEquals(GUEST_ID, guestId);

        Mockito.verify(preparedStatement, Mockito.times(1)).setString(1, GUEST_MAIL_ADDRESS);
        Mockito.verify(connection, Mockito.times(1)).prepareStatement(RdbGuestStorage.RESOLVE_GUEST_ID_BY_MAIL);
    }

    @Test(expected = OXException.class)
    public void testGetNumberOfAssignments_conntectionsNull_throwExcpetion() throws OXException, SQLException {
        Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(false);

        rdbGuestStorage.getNumberOfAssignments(GUEST_ID, null);
    }

    @Test
    public void testGetNumberOfAssignments_noAssignmentFound_returnZero() throws OXException, SQLException {
        Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(false);

        long numberOfAssignments = rdbGuestStorage.getNumberOfAssignments(GUEST_ID, connection);

        Assert.assertEquals(0, numberOfAssignments);

        Mockito.verify(preparedStatement, Mockito.times(1)).setLong(1, GUEST_ID);
        Mockito.verify(connection, Mockito.times(1)).prepareStatement(RdbGuestStorage.RESOLVE_NUMBER_OF_GUEST_ASSIGNMENTS_BY_GUESTID);
    }

    @Test
    public void testGetNumberOfAssignments_assignmentsFound_returnAssignmentsNo() throws OXException, SQLException {
        Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(true);
        Mockito.when(resultSet.getInt(1)).thenReturn(10);

        long numberOfAssignments = rdbGuestStorage.getNumberOfAssignments(GUEST_ID, connection);

        Assert.assertEquals(10, numberOfAssignments);

        Mockito.verify(preparedStatement, Mockito.times(1)).setLong(1, GUEST_ID);
        Mockito.verify(resultSet, Mockito.times(1)).getInt(1);
        Mockito.verify(connection, Mockito.times(1)).prepareStatement(RdbGuestStorage.RESOLVE_NUMBER_OF_GUEST_ASSIGNMENTS_BY_GUESTID);
    }

    @Test (expected=OXException.class)
    public void testIsAssignmentExisting_connectionNull_throwException() throws OXException, SQLException {
        Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(false);

        rdbGuestStorage.isAssignmentExisting(GUEST_ID, CONTEXT_ID, USER_ID, null);
    }

    @Test
    public void testIsAssignmentExisting_noAssignmentFound_returnFalse() throws OXException, SQLException {
        Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(false);

        boolean assignmentExisting = rdbGuestStorage.isAssignmentExisting(GUEST_ID, CONTEXT_ID, USER_ID, connection);

        Assert.assertFalse(assignmentExisting);

        Mockito.verify(connection, Mockito.times(1)).prepareStatement(RdbGuestStorage.RESOLVE_GUEST_ASSIGNMENT);
        Mockito.verify(preparedStatement, Mockito.times(1)).setInt(1, CONTEXT_ID);
        Mockito.verify(preparedStatement, Mockito.times(1)).setInt(2, USER_ID);
        Mockito.verify(preparedStatement, Mockito.times(1)).setLong(3, GUEST_ID);
    }

    @Test
    public void testIsAssignmentExisting_assignmentFound_returnTrue() throws OXException, SQLException {
        Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(true);

        boolean assignmentExisting = rdbGuestStorage.isAssignmentExisting(GUEST_ID, CONTEXT_ID, USER_ID, connection);

        Assert.assertTrue(assignmentExisting);

        Mockito.verify(connection, Mockito.times(1)).prepareStatement(RdbGuestStorage.RESOLVE_GUEST_ASSIGNMENT);
        Mockito.verify(preparedStatement, Mockito.times(1)).setInt(1, CONTEXT_ID);
        Mockito.verify(preparedStatement, Mockito.times(1)).setInt(2, USER_ID);
        Mockito.verify(preparedStatement, Mockito.times(1)).setLong(3, GUEST_ID);
    }

    @Test(expected = OXException.class)
    public void testRemoveGuest_moreThanOneRowDeleted_throwException() throws OXException, SQLException {
        Mockito.when(preparedStatement.executeUpdate()).thenReturn(3);

        rdbGuestStorage.removeGuest(GUEST_ID, connection);

        Mockito.verify(connection, Mockito.times(1)).prepareStatement(RdbGuestStorage.DELETE_GUEST);
    }

    @Test
    public void testRemoveGuest_oneGuestDeleted_return() throws OXException, SQLException {
        Mockito.when(preparedStatement.executeUpdate()).thenReturn(1);

        rdbGuestStorage.removeGuest(GUEST_ID, connection);

        Mockito.verify(connection, Mockito.times(1)).prepareStatement(RdbGuestStorage.DELETE_GUEST);
        Mockito.verify(preparedStatement, Mockito.times(1)).executeUpdate();
    }

    @Test
    public void testRemoveGuest_guestAlreadyDeleted_return() throws OXException, SQLException {
        Mockito.when(preparedStatement.executeUpdate()).thenReturn(0);

        rdbGuestStorage.removeGuest(GUEST_ID, connection);

        Mockito.verify(connection, Mockito.times(1)).prepareStatement(RdbGuestStorage.DELETE_GUEST);
        Mockito.verify(preparedStatement, Mockito.times(1)).executeUpdate();
    }

    @Test
    public void testRemoveGuestAssignment_noChange_logUnexpectedReturnValue() throws OXException, SQLException {
        Logger log = PowerMockito.mock(org.slf4j.Logger.class);
        MockUtils.injectValueIntoPrivateField(RdbGuestStorage.class, "LOG", log);

        Mockito.when(preparedStatement.executeUpdate()).thenReturn(0);

        rdbGuestStorage.removeGuestAssignment(GUEST_ID, CONTEXT_ID, USER_ID, connection);

        Mockito.verify(log, Mockito.times(1)).error(Matchers.anyString());
        Mockito.verify(connection, Mockito.times(1)).prepareStatement(RdbGuestStorage.DELETE_GUEST_ASSIGNMENT);
        Mockito.verify(preparedStatement, Mockito.times(1)).executeUpdate();
    }

    @Test(expected = OXException.class)
    public void testRemoveGuests_connectionNull_throwException() throws OXException, SQLException {
        rdbGuestStorage.removeGuestAssignments(CONTEXT_ID, null);
    }

    @Test
    public void testRemoveGuests_fine_removeGuestAssignments() throws OXException, SQLException {
        Mockito.when(preparedStatement.executeUpdate()).thenReturn(11);

        rdbGuestStorage.removeGuestAssignments(CONTEXT_ID, connection);

        Mockito.verify(preparedStatement, Mockito.times(1)).executeUpdate();
    }

    @Test
    public void testResolveGuests_notYetImplemented() throws OXException, SQLException {
        Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);
        Mockito.when(resultSet.next()).thenReturn(true, false);
        Mockito.when(resultSet.getLong(Matchers.anyInt())).thenReturn(GUEST_ID);

        List<Long> resolveGuestAssignments = rdbGuestStorage.resolveGuestAssignments(CONTEXT_ID, connection);

        Assert.assertEquals(GUEST_ID, resolveGuestAssignments.get(0).intValue());
    }

    @Test (expected=OXException.class)
    public void testUpdateGuestAssignment_connectionNull_throwException() throws OXException, SQLException {
        Mockito.when(preparedStatement.executeUpdate()).thenReturn(1);
        Mockito.when(resultSet.next()).thenReturn(true, false);
        Mockito.when(resultSet.getLong(Matchers.anyInt())).thenReturn(GUEST_ID);

        rdbGuestStorage.updateGuestAssignment(new GuestAssignment(GUEST_ID, CONTEXT_ID, USER_ID, GUEST_PASSWORD, GUEST_PASSWORD_MECH), null);
    }

    @Test
    public void testUpdateGuestAssignment_updateGuest_fine() throws OXException, SQLException {
        Mockito.when(preparedStatement.executeUpdate()).thenReturn(1);
        Mockito.when(resultSet.next()).thenReturn(true, false);
        Mockito.when(resultSet.getLong(Matchers.anyInt())).thenReturn(GUEST_ID);

        rdbGuestStorage.updateGuestAssignment(new GuestAssignment(GUEST_ID, CONTEXT_ID, USER_ID, GUEST_PASSWORD, GUEST_PASSWORD_MECH), connection);

        Mockito.verify(preparedStatement, Mockito.times(1)).executeUpdate();
        Mockito.verify(connection, Mockito.times(1)).prepareStatement(RdbGuestStorage.UPDATE_GUEST_PASSWORD);
    }

    @Test
    public void testGetIdsAsString() {
        List<Long> groupIds = Arrays.asList(3L, 5L, 10L, 13L);
        String commaSeperatedString = rdbGuestStorage.getIdsAsString(groupIds);

        Assert.assertEquals("3, 5, 10, 13".replaceAll("\\s",""), commaSeperatedString);
    }
}
