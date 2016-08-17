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

package com.openexchange.ajax.requesthandler.converters.preview.cache;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.startsWith;
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
    @SuppressWarnings("unchecked")
    private void testFileIsDeletedOnRollback(Class<? extends Exception> exceptionClass) throws Exception {
        PreparedStatement statement = mock(PreparedStatement.class);
        when(statement.executeUpdate()).thenThrow(exceptionClass);
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
        when(FileStoreResourceCacheImpl.class, "getFileStorage", anyInt(), anyBoolean()).thenReturn(fileStorage);

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
