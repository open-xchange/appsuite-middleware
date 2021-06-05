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

package com.openexchange.contact.vcard.storage.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.Info;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.java.Charsets;
import com.openexchange.tools.file.SaveFileAction;

/**
 * {@link DefaultVCardStorageServiceTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ FileStorages.class })
public class DefaultVCardStorageServiceTest {

    @InjectMocks
    private DefaultVCardStorageService service;

    @Mock
    private QuotaFileStorageService quotaFileStorageService;

    @Mock
    private QuotaFileStorage quotaFileStorage;

    // @formatter:off
    private final String vCard = "BEGIN:VCARD\n" + 
        "VERSION:3.0\n" + 
        "PRODID:-//Apple Inc.//Mac OS X 10.10.3//EN\n" + 
        "N:Allison;Christian;;;\n" + 
        "FN:Christian Allison\n" + 
        "EMAIL;type=INTERNET;type=WORK;type=pref:knightmedina@gushkool.com\n" + 
        "EMAIL;type=INTERNET;type=HOME:barkerharmon@orbean.com\n" +
        "item1.EMAIL;type=INTERNET:terrynorton@netbook.com\n" +
        "item1.X-ABLabel:_$!<Other>!$_\n" + 
        "TEL;type=HOME;type=VOICE;type=pref:(951) 498-2926\n" + 
        "TEL;type=HOME;type=VOICE:(829) 455-2780\n" + 
        "TEL;type=CELL;type=VOICE:8294552781\n" + 
        "NOTE:laboris\n" + 
        "REV:2015-06-11T13:54:57Z\n" + 
        "UID:f047e394-f638-4578-ad15-cd17e52ecce3\n" + 
        "END:VCARD\n" + "";
    // @formatter:on

    private final int CONTEXT_ID = 111;
    private final String FILE_STORAGE_ID = "hashed/cf/92/75/cbfd33f804f649738fea4a9e7cf31e3b";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(FileStorages.class);
        PowerMockito.when(FileStorages.getQuotaFileStorageService()).thenReturn(quotaFileStorageService);
        PowerMockito.when(quotaFileStorageService.getQuotaFileStorage(ArgumentMatchers.anyInt(), Info.class.cast(ArgumentMatchers.any()))).thenReturn(quotaFileStorage);
    }

    @Test(expected = OXException.class)
    public void testSaveVCard_errorOccured_throwException() throws OXException {
        PowerMockito.when(FileStorages.getQuotaFileStorageService()).thenReturn(null);

        service.saveVCard(IOUtils.toInputStream(vCard, Charsets.UTF_8), CONTEXT_ID);
    }

    @Test
    public void testSaveVCard_happyFlow_returnVCardId() throws OXException {
        DefaultVCardStorageService tempVCardStorageService = Mockito.spy(new DefaultVCardStorageService());
        SaveFileAction saveFileAction = Mockito.mock(SaveFileAction.class);
        Mockito.doReturn(saveFileAction).when(tempVCardStorageService).createFileAction((InputStream) ArgumentMatchers.any(), ArgumentMatchers.anyInt());
        Mockito.when(saveFileAction.getFileStorageID()).thenReturn(FILE_STORAGE_ID);

        String vCardId = tempVCardStorageService.saveVCard(IOUtils.toInputStream(vCard, Charsets.UTF_8), CONTEXT_ID);
        assertEquals(FILE_STORAGE_ID, vCardId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveVCard_vCardNull_doNotPersist() throws OXException {
        service.saveVCard(null, CONTEXT_ID);
    }

    @Test
    public void testDeleteVCard_vCardIdNull_doNothingAndReturnFalse() throws OXException {
        boolean deleteVCard = service.deleteVCard(null, CONTEXT_ID);
        assertFalse(deleteVCard);
    }

    @Test
    public void testDeleteVCard_fileStorageReturnsFalse_returnFalse() throws OXException {
        Mockito.doReturn(Boolean.FALSE).when(quotaFileStorage).deleteFile(ArgumentMatchers.anyString());

        boolean deleteVCard = service.deleteVCard(FILE_STORAGE_ID, CONTEXT_ID);
        assertFalse(deleteVCard);
    }

    @Test
    public void testDeleteVCard_fileStorageReturnsTrue_returnTrue() throws OXException {
        Mockito.doReturn(Boolean.TRUE).when(quotaFileStorage).deleteFile(ArgumentMatchers.anyString());

        boolean deleteVCard = service.deleteVCard(FILE_STORAGE_ID, CONTEXT_ID);
        assertTrue(deleteVCard);
    }

    @Test(expected = OXException.class)
    public void testDeleteVCard_fileStorageThrowsException_rethrow() throws OXException {
        Mockito.doThrow(new OXException(77)).when(quotaFileStorage).deleteFile(ArgumentMatchers.anyString());

        service.deleteVCard(FILE_STORAGE_ID, CONTEXT_ID);
    }

    @Test
    public void testGetVCard_identifierNull_returnNull() throws OXException {
        InputStream lVCard = service.getVCard(null, CONTEXT_ID);
        assertNull(lVCard);
    }

    @Test
    public void testGetVCard_fileStorageReturnsNull_returnNull() throws OXException {
        Mockito.doReturn(null).when(quotaFileStorage).getFile(ArgumentMatchers.anyString());
        InputStream lVCard = service.getVCard(FILE_STORAGE_ID, CONTEXT_ID);
        assertNull(lVCard);
    }

    @Test(expected = OXException.class)
    public void testGetVCard_fileStorageThrowsException_rethrow() throws OXException {
        Mockito.doThrow(new OXException(77)).when(quotaFileStorage).getFile(ArgumentMatchers.anyString());

        service.getVCard(FILE_STORAGE_ID, CONTEXT_ID);
    }

    @Test
    public void testGetVCard_happyFlow_returnInputStream() throws OXException {
        InputStream mock = Mockito.mock(InputStream.class);
        Mockito.doReturn(mock).when(quotaFileStorage).getFile(ArgumentMatchers.anyString());

        InputStream lVCard = service.getVCard(FILE_STORAGE_ID, CONTEXT_ID);
        assertNotNull(lVCard);
        assertEquals(mock, lVCard);
    }

    @Test
    public void testGetFileStorage_ok_storageNotNull() throws OXException {
        FileStorage storage = service.getFileStorage(CONTEXT_ID);
        assertNotNull(storage);
    }

    @Test
    public void testGetFileStorage_ok_correctServiceReturned() throws OXException {
        FileStorage storage = service.getFileStorage(CONTEXT_ID);
        assertEquals(quotaFileStorage, storage);
    }

    @Test(expected = OXException.class)
    public void testGetFileStorage_serviceNull_throwException() throws OXException {
        PowerMockito.when(FileStorages.getQuotaFileStorageService()).thenReturn(null);

        service.getFileStorage(CONTEXT_ID);
    }
}
