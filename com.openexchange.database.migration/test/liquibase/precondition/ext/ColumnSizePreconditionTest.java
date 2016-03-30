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

package liquibase.precondition.ext;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomPreconditionErrorException;
import liquibase.exception.CustomPreconditionFailedException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * {@link ColumnSizePreconditionTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class ColumnSizePreconditionTest {

    @InjectMocks
    private ColumnSizePrecondition columnSizePrecondition;

    @Mock
    private Database database;

    @Mock
    private JdbcConnection jdbcConnection;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData databaseMetaData;

    private ResultSet resultSetMock;

    private String expectedSize = "255";

    private String columnName = "columnName";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        columnSizePrecondition.setExpectedSize(expectedSize);
        columnSizePrecondition.setColumnName(columnName);

        Mockito.when(database.getConnection()).thenReturn(jdbcConnection);
        Mockito.when(jdbcConnection.getUnderlyingConnection()).thenReturn(connection);
        Mockito.when(connection.getMetaData()).thenReturn(databaseMetaData);

        resultSetMock = Mockito.mock(ResultSet.class);
        Mockito.when(resultSetMock.getString("COLUMN_NAME")).thenReturn(columnName);
        Mockito.when(resultSetMock.getInt("COLUMN_SIZE")).thenReturn(new Integer(expectedSize));

        Mockito.when(databaseMetaData.getColumns(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.anyString())).thenReturn(resultSetMock);

        Mockito.when(resultSetMock.next()).thenReturn(true).thenReturn(true).thenReturn(false);
    }

    @Test(expected = CustomPreconditionErrorException.class)
    public void testCheck_databaseFromLiquibaseNull_throwException() throws CustomPreconditionFailedException, CustomPreconditionErrorException {
        columnSizePrecondition.check(null);
    }

    @Test(expected = CustomPreconditionErrorException.class)
    public void testCheck_underlyingConnectionFromWrongType_throwException() throws CustomPreconditionFailedException, CustomPreconditionErrorException {
        Mockito.when(database.getConnection()).thenReturn(null);
        columnSizePrecondition.check(database);
    }

    @Test(expected = CustomPreconditionFailedException.class)
    public void testCheck_columnFoundSizeAlreadyCorrect_throwExceptionToMarkExecuted() throws CustomPreconditionFailedException, CustomPreconditionErrorException {
        columnSizePrecondition.check(database);
    }

    @Test
    public void testCheck_columnFoundSizeDifferent_nothingTodoSoThatExecutionIsTriggeredByLiquibase() throws CustomPreconditionFailedException, CustomPreconditionErrorException, NumberFormatException, SQLException {
        Mockito.when(resultSetMock.getInt("COLUMN_SIZE")).thenReturn(128);
        columnSizePrecondition.check(database);
    }

    @Test(expected = CustomPreconditionErrorException.class)
    public void testCheck_columnNotFound_throwException() throws CustomPreconditionFailedException, CustomPreconditionErrorException, SQLException {
        Mockito.when(resultSetMock.getString("COLUMN_NAME")).thenReturn("NotExistingColumn");

        columnSizePrecondition.check(database);
    }

    @Test(expected = CustomPreconditionErrorException.class)
    public void testCheck_tableNotFound_throwException() throws CustomPreconditionFailedException, CustomPreconditionErrorException, SQLException {
        Mockito.when(resultSetMock.next()).thenReturn(false);

        columnSizePrecondition.check(database);
    }
}
