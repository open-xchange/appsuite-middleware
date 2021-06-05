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

package com.openexchange.ajax.requesthandler.converters.preview.cache;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.ajax.requesthandler.cache.CachedResource;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link FileStoreResourceCacheImplTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ FileStoreResourceCacheImpl.class, ServerServiceRegistry.class })
public class FileStoreResourceCacheImplTest {

    @Test
    public void testFileIsDeletedOnDataTruncation() throws Exception {
        testFileIsDeletedOnRollback(DataTruncation.class);
    }

    @Test
    public void testFileIsDeletedOnSqlException() throws Exception {
        testFileIsDeletedOnRollback(SQLException.class);
    }

    /**
     * Verify that a created file is deleted if the subsequent db transaction throws a given exception
     */
    private void testFileIsDeletedOnRollback(Class<? extends Exception> exceptionClass) throws Exception {
        PreparedStatement statement = mock(PreparedStatement.class);
        when(I(statement.executeUpdate())).thenThrow(exceptionClass);
        when(statement.executeQuery()).thenReturn(mock(ResultSet.class));
        Connection connection = mock(Connection.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(connection.createStatement()).thenReturn(mock(Statement.class));
        DatabaseService databaseService = mock(DatabaseService.class);
        when(databaseService.getWritable(anyInt())).thenReturn(connection);
        ConfigurationService configMock = mock(ConfigurationService.class);
        when(configMock.getProperty(anyString(), anyString())).thenReturn("-1");
        ServiceLookup serviceLookupMock = mock(ServiceLookup.class);
        when(serviceLookupMock.getService(DatabaseService.class)).thenReturn(databaseService);
        when(serviceLookupMock.getService(ConfigurationService.class)).thenReturn(configMock);

        FileStoreResourceCacheImpl cache = spy(new FileStoreResourceCacheImpl(serviceLookupMock));
        mockStatic(FileStoreResourceCacheImpl.class);
        com.openexchange.filestore.FileStorage fileStorage = mock(com.openexchange.filestore.FileStorage.class);
        String fileId = "12345";
        when(fileStorage.saveNewFile(any(InputStream.class))).thenReturn(fileId);
        when(FileStoreResourceCacheImpl.class, "getFileStorage", I(anyInt()), B(anyBoolean())).thenReturn(fileStorage);

        boolean exceptionThrown = false;
        try {
            byte[] bytes = new byte[100];
            CachedResource resource = new CachedResource(bytes, "some_image.jpg", "image/jpeg", 100);
            cache.save("resource-id", resource, 1, 1);
        } catch (OXException e) {
            exceptionThrown = true;
            Assert.assertTrue(exceptionClass.isInstance(e.getCause()));
        }
        Assert.assertTrue(exceptionThrown);
        Mockito.verify(databaseService, Mockito.times(1)).getWritable(anyInt());
        InOrder autoCommitOrder = Mockito.inOrder(connection);
        autoCommitOrder.verify(connection).setAutoCommit(false);
        autoCommitOrder.verify(connection).setAutoCommit(true);
        Mockito.verify(connection).prepareStatement(startsWith("INSERT INTO"));
        Mockito.verify(statement).executeUpdate();
        Mockito.verify(connection).rollback();
        Mockito.verify(fileStorage).deleteFile(fileId);
    }

}
