package com.openexchange.groupware.update.tasks;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.tools.update.Tools;

/**
 * The class <code>MakeFolderIdPrimaryForDelContactsTableTest</code> contains tests for the class {@link <code>MakeFolderIdPrimaryForDelContactsTable</code>}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Database.class, Tools.class })
public class MakeFolderIdPrimaryForDelContactsTableTest {

    @InjectMocks
    private MakeFolderIdPrimaryForDelContactsTable makeFolderIdPrimaryForDelContactsTable;

    @Mock
    private PerformParameters mockParams;

    @Mock
    private Connection mockConnection;

    int contextId = 1;

    @Before
    public void setUp() throws OXException {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(Database.class);
        PowerMockito.when(Database.getNoTimeout(contextId, true)).thenReturn(mockConnection);

        Mockito.when(mockParams.getContextId()).thenReturn(contextId);
    }

    /**
     * Run the void perform(PerformParameters) method test
     * 
     * @throws OXException
     */
    @Test(expected = IllegalArgumentException.class)
     public void testPerform_paramsNull_throwExpception() throws OXException {
        makeFolderIdPrimaryForDelContactsTable.perform(null);
    }

    /**
     * Run the void perform(PerformParameters) method test
     * 
     * @throws OXException
     */
    @Test(expected = OXException.class)
     public void testPerform_connectionNotProper_throwException() throws OXException {
        makeFolderIdPrimaryForDelContactsTable.perform(mockParams);
    }

    /**
     * Run the void perform(PerformParameters) method test
     * 
     * @throws OXException
     * @throws SQLException
     */
     @Test
     public void testPerform_tableHasNoPrimaryKey_createPrimaryKeyWithoutDroppingBefore() throws OXException, SQLException {
        PowerMockito.mockStatic(Tools.class);
        PowerMockito.when(
            Tools.hasPrimaryKey(
                this.mockConnection,
                com.openexchange.groupware.update.tasks.MakeFolderIdPrimaryForDelContactsTable.DEL_CONTACTS)).thenReturn(false);
        PowerMockito.when(
            Tools.hasPrimaryKey(
                this.mockConnection,
                com.openexchange.groupware.update.tasks.MakeFolderIdPrimaryForDelContactsTable.PRG_CONTACTS)).thenReturn(false);

        makeFolderIdPrimaryForDelContactsTable.perform(mockParams);

        PowerMockito.verifyStatic(Mockito.never());
        Tools.dropPrimaryKey(mockConnection, com.openexchange.groupware.update.tasks.MakeFolderIdPrimaryForDelContactsTable.DEL_CONTACTS);

        PowerMockito.verifyStatic(Mockito.times(1));
        Tools.hasPrimaryKey(mockConnection, com.openexchange.groupware.update.tasks.MakeFolderIdPrimaryForDelContactsTable.DEL_CONTACTS);
        Tools.hasPrimaryKey(mockConnection, com.openexchange.groupware.update.tasks.MakeFolderIdPrimaryForDelContactsTable.PRG_CONTACTS);

        Tools.createPrimaryKey(
            mockConnection,
            com.openexchange.groupware.update.tasks.MakeFolderIdPrimaryForDelContactsTable.DEL_CONTACTS,
            new String[] { "cid", "intfield01", "fid" });
        Tools.createPrimaryKey(
            mockConnection,
            com.openexchange.groupware.update.tasks.MakeFolderIdPrimaryForDelContactsTable.PRG_CONTACTS,
            new String[] { "cid", "intfield01", "fid" });
    }

    /**
     * Run the void perform(PerformParameters) method test
     * 
     * @throws OXException
     * @throws SQLException
     */
     @Test
     public void testPerform_tableHasNoPrimaryKey_createPrimaryKeyWithDroppingBefore() throws OXException, SQLException {
        PowerMockito.mockStatic(Tools.class);
        PowerMockito.when(
            Tools.hasPrimaryKey(
                this.mockConnection,
                com.openexchange.groupware.update.tasks.MakeFolderIdPrimaryForDelContactsTable.DEL_CONTACTS)).thenReturn(true);
        PowerMockito.when(
            Tools.hasPrimaryKey(
                this.mockConnection,
                com.openexchange.groupware.update.tasks.MakeFolderIdPrimaryForDelContactsTable.PRG_CONTACTS)).thenReturn(true);

        makeFolderIdPrimaryForDelContactsTable.perform(mockParams);

        PowerMockito.verifyStatic(Mockito.times(1));
        Tools.dropPrimaryKey(mockConnection, com.openexchange.groupware.update.tasks.MakeFolderIdPrimaryForDelContactsTable.DEL_CONTACTS);
        Tools.dropPrimaryKey(mockConnection, com.openexchange.groupware.update.tasks.MakeFolderIdPrimaryForDelContactsTable.PRG_CONTACTS);

        Tools.hasPrimaryKey(mockConnection, com.openexchange.groupware.update.tasks.MakeFolderIdPrimaryForDelContactsTable.DEL_CONTACTS);
        Tools.hasPrimaryKey(mockConnection, com.openexchange.groupware.update.tasks.MakeFolderIdPrimaryForDelContactsTable.PRG_CONTACTS);

        Tools.createPrimaryKey(
            mockConnection,
            com.openexchange.groupware.update.tasks.MakeFolderIdPrimaryForDelContactsTable.DEL_CONTACTS,
            new String[] { "cid", "intfield01", "fid" });
        Tools.createPrimaryKey(
            mockConnection,
            com.openexchange.groupware.update.tasks.MakeFolderIdPrimaryForDelContactsTable.PRG_CONTACTS,
            new String[] { "cid", "intfield01", "fid" });
    }
}