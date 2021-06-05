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
package com.openexchange.unifiedinbox.converters;

import static com.openexchange.java.Autoboxing.B;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolder.DefaultFolderType;
import com.openexchange.session.Session;
import com.openexchange.unifiedinbox.UnifiedInboxAccess;

/**
 * {@link UnifiedInboxFolderConverterTest}
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.8.4
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ MailAccess.class, Session.class })
public class UnifiedInboxFolderConverterTest {

    private final int accountId = 1;

    @Mock
    private IMailFolderStorage folderStorage;
    @Mock
    private Session session;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void testMergeAccountDefaultFolders_AccountFoldersEmpty() {
        MailFolder[] folders = UnifiedInboxFolderConverter.mergeAccountDefaultFolders(new ArrayList<int[][]>(), new String[0], new String[0]);
        assertTrue("Folders are not empty",folders.length == 0);
    }

    @Test
    public void testMergeAccountDefaultFolders_OneAccountFolderEmpty() {
        ArrayList<int[][]> accountFolders = new ArrayList<int[][]>();
        int[][] firstFolder = {{0,0,0,0}};
        int[][] secondFolder = {{1,2,3,4}};
        accountFolders.add(firstFolder);
        accountFolders.add(secondFolder);
        String[] fullNames = new String[] {UnifiedInboxAccess.INBOX};
        String[] localizedNames = new String[] {"LocalizedName"};
        MailFolder[] resultFolders = UnifiedInboxFolderConverter.mergeAccountDefaultFolders(accountFolders, fullNames, localizedNames);
        assertTrue("Result folders are not as many as predicted", resultFolders.length == 1);
        MailFolder folder = resultFolders[0];
        assertTrue("Wrong message count", folder.getMessageCount() == secondFolder[0][0] && folder.getUnreadMessageCount() == secondFolder[0][1] && folder.getDeletedMessageCount() == secondFolder[0][2] && folder.getNewMessageCount() == secondFolder[0][3]);
        assertTrue("Wrong fullname", folder.getDefaultFolderType().equals(DefaultFolderType.INBOX));
    }

    @Test
    public void testMergeAccountDefaultFolders_OneAccount() {
        ArrayList<int[][]> accountFolders = new ArrayList<int[][]>();
        int[][] storedFolder = {{1,2,3,4}};
        accountFolders.add(storedFolder);
        String[] fullNames = new String[] {"Fullname"};
        String[] localizedNames = new String[] {"LocalizedName"};
        MailFolder[] resultFolders = UnifiedInboxFolderConverter.mergeAccountDefaultFolders(accountFolders, fullNames, localizedNames);
        assertTrue("Result folders are not as many as predicted", resultFolders.length == 1);
        MailFolder folder = resultFolders[0];
        assertTrue("Wrong message count", folder.getMessageCount() == storedFolder[0][0] && folder.getUnreadMessageCount() == storedFolder[0][1] && folder.getDeletedMessageCount() == storedFolder[0][2] && folder.getNewMessageCount() == storedFolder[0][3]);
        assertTrue("Wrong fullname", folder.getDefaultFolderType().equals(DefaultFolderType.NONE));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testGetAccountDefaultFolders_MissingFolders() throws Exception {
        MailAccess mailAccess = PowerMockito.mock(MailAccess.class, Mockito.CALLS_REAL_METHODS);
        PowerMockito.mockStatic(MailAccess.class);
        PowerMockito.when(MailAccess.getInstance(session, accountId)).thenReturn(mailAccess);
        PowerMockito.doNothing().when(mailAccess).connect();
        PowerMockito.doNothing().when(mailAccess).close(true);
        String[] FULLNAMES = {
            UnifiedInboxAccess.INBOX, UnifiedInboxAccess.DRAFTS, UnifiedInboxAccess.SENT, UnifiedInboxAccess.SPAM, UnifiedInboxAccess.TRASH };
        PowerMockito.when(mailAccess.getFolderStorage()).thenReturn(folderStorage);
        PowerMockito.when(B(folderStorage.exists(UnifiedInboxAccess.INBOX))).thenReturn(Boolean.TRUE);
        PowerMockito.when(B(folderStorage.exists(UnifiedInboxAccess.DRAFTS))).thenReturn(Boolean.FALSE);
        PowerMockito.when(B(folderStorage.exists(UnifiedInboxAccess.SENT))).thenReturn(Boolean.FALSE);
        PowerMockito.when(B(folderStorage.exists(UnifiedInboxAccess.SPAM))).thenReturn(Boolean.FALSE);
        PowerMockito.when(B(folderStorage.exists(UnifiedInboxAccess.TRASH))).thenReturn(Boolean.TRUE);
        PowerMockito.when(folderStorage.getTrashFolder()).thenReturn(UnifiedInboxAccess.TRASH);
        MailFolder inbox = new MailFolder();
        inbox.setMessageCount(1);
        inbox.setUnreadMessageCount(2);
        inbox.setDeletedMessageCount(3);
        inbox.setNewMessageCount(4);
        MailFolder trash = new MailFolder();
        trash.setMessageCount(1);
        trash.setUnreadMessageCount(2);
        trash.setDeletedMessageCount(3);
        trash.setNewMessageCount(4);
        PowerMockito.when(folderStorage.getFolder(UnifiedInboxAccess.INBOX)).thenReturn(inbox);
        PowerMockito.when(folderStorage.getFolder(UnifiedInboxAccess.TRASH)).thenReturn(trash);
        int[][] folders = UnifiedInboxFolderConverter.getAccountDefaultFolders(1, session, FULLNAMES);
        assertTrue("Wrong number of default folders.", notNullLength(folders) == 2);
    }

    private int notNullLength(int[][] array) {
        int length = 0;
        for (int[] is : array) {
            if (is != null) {
                length++;
            }
        }
        return length;
    }
}
