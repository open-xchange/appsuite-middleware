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
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.QuotaFileStorageService;
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
        "END:VCARD\n" +
        "";

    private final int CONTEXT_ID = 111;
    private final String FILE_STORAGE_ID = "hashed/cf/92/75/cbfd33f804f649738fea4a9e7cf31e3b";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(FileStorages.class);
        PowerMockito.when(FileStorages.getQuotaFileStorageService()).thenReturn(quotaFileStorageService);
        PowerMockito.when(quotaFileStorageService.getQuotaFileStorage(Matchers.anyInt())).thenReturn(quotaFileStorage);
    }

    @Test(expected = OXException.class)
    public void testSaveVCard_errorOccured_throwException() throws OXException {
        PowerMockito.when(FileStorages.getQuotaFileStorageService()).thenReturn(null);

        service.saveVCard(IOUtils.toInputStream(vCard), CONTEXT_ID);
    }

    @Test
    public void testSaveVCard_happyFlow_returnVCardId() throws OXException {
        DefaultVCardStorageService tempVCardStorageService = Mockito.spy(new DefaultVCardStorageService());
        SaveFileAction saveFileAction = Mockito.mock(SaveFileAction.class);
        Mockito.doReturn(saveFileAction).when(tempVCardStorageService).createFileAction((InputStream) Matchers.any(), Matchers.anyInt());
        Mockito.when(saveFileAction.getFileStorageID()).thenReturn(FILE_STORAGE_ID);

        String vCardId = tempVCardStorageService.saveVCard(IOUtils.toInputStream(vCard), CONTEXT_ID);
        assertEquals(FILE_STORAGE_ID, vCardId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveVCard_vCardNull_doNotPersist() throws OXException
    {
        service.saveVCard(null, CONTEXT_ID);
    }

    @Test
    public void testDeleteVCard_vCardIdNull_doNothingAndReturnFalse() throws OXException {
        boolean deleteVCard = service.deleteVCard(null, CONTEXT_ID);
        assertFalse(deleteVCard);
    }

    @Test
    public void testDeleteVCard_fileStorageReturnsFalse_returnFalse() throws OXException {
        Mockito.doReturn(false).when(quotaFileStorage).deleteFile(Matchers.anyString());

        boolean deleteVCard = service.deleteVCard(FILE_STORAGE_ID, CONTEXT_ID);
        assertFalse(deleteVCard);
    }

    @Test
    public void testDeleteVCard_fileStorageReturnsTrue_returnTrue() throws OXException {
        Mockito.doReturn(true).when(quotaFileStorage).deleteFile(Matchers.anyString());

        boolean deleteVCard = service.deleteVCard(FILE_STORAGE_ID, CONTEXT_ID);
        assertTrue(deleteVCard);
    }

    @Test(expected = OXException.class)
    public void testDeleteVCard_fileStorageThrowsException_rethrow() throws OXException {
        Mockito.doThrow(new OXException(77)).when(quotaFileStorage).deleteFile(Matchers.anyString());

        service.deleteVCard(FILE_STORAGE_ID, CONTEXT_ID);
    }

    @Test
    public void testGetVCard_identifierNull_returnNull() throws OXException {
        InputStream lVCard = service.getVCard(null, CONTEXT_ID);
        assertNull(lVCard);
    }

    @Test
    public void testGetVCard_fileStorageReturnsNull_returnNull() throws OXException {
        Mockito.doReturn(null).when(quotaFileStorage).getFile(Matchers.anyString());
        InputStream lVCard = service.getVCard(FILE_STORAGE_ID, CONTEXT_ID);
        assertNull(lVCard);
    }

    @Test(expected = OXException.class)
    public void testGetVCard_fileStorageThrowsException_rethrow() throws OXException {
        Mockito.doThrow(new OXException(77)).when(quotaFileStorage).getFile(Matchers.anyString());

        service.getVCard(FILE_STORAGE_ID, CONTEXT_ID);
    }

    @Test
    public void testGetVCard_happyFlow_returnInputStream() throws OXException {
        InputStream mock = Mockito.mock(InputStream.class);
        Mockito.doReturn(mock).when(quotaFileStorage).getFile(Matchers.anyString());

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
