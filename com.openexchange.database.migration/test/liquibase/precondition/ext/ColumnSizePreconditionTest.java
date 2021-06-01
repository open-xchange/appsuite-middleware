/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package liquibase.precondition.ext;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import com.openexchange.java.Autoboxing;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomPreconditionErrorException;
import liquibase.exception.CustomPreconditionFailedException;

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

    private final String expectedSize = "255";

    private final String columnName = "columnName";

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
        Mockito.when(I(resultSetMock.getInt("COLUMN_SIZE"))).thenReturn(new Integer(expectedSize));

        Mockito.when(databaseMetaData.getColumns(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(resultSetMock);
        Mockito.when(databaseMetaData.getColumns(ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(resultSetMock);

        Mockito.when(Autoboxing.valueOf(resultSetMock.next())).thenReturn(Boolean.TRUE).thenReturn(Boolean.TRUE).thenReturn(Boolean.FALSE);
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
        Mockito.when(I(resultSetMock.getInt("COLUMN_SIZE"))).thenReturn(I(128));
        columnSizePrecondition.check(database);
    }

    @Test(expected = CustomPreconditionErrorException.class)
    public void testCheck_columnNotFound_throwException() throws CustomPreconditionFailedException, CustomPreconditionErrorException, SQLException {
        Mockito.when(resultSetMock.getString("COLUMN_NAME")).thenReturn("NotExistingColumn");

        columnSizePrecondition.check(database);
    }

    @Test(expected = CustomPreconditionErrorException.class)
    public void testCheck_tableNotFound_throwException() throws CustomPreconditionFailedException, CustomPreconditionErrorException, SQLException {
        Mockito.when(Autoboxing.valueOf(resultSetMock.next())).thenReturn(Boolean.FALSE);

        columnSizePrecondition.check(database);
    }
}
