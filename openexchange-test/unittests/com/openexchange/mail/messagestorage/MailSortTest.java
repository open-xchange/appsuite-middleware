package com.openexchange.mail.messagestorage;

import java.util.Arrays;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.dataobjects.MailMessage;


public class MailSortTest extends MessageStorageTest {

    public void testSortMails() throws Exception {
        // This test must be run in a clear folder so we create a new one with a timestamp
        final String tempFolderName = "TempFolder" + System.currentTimeMillis();
        final String tempFolder = createTemporaryFolderAndGetFullname(getSession(), mailAccess, tempFolderName);
        try {
            final String[] appendMessages = this.mailAccess.getMessageStorage().appendMessages(tempFolder, testmessages);
            final MailMessage[] searchMessages = mailAccess.getMessageStorage().searchMessages(tempFolder, null, MailSortField.SUBJECT, OrderDirection.ASC, null, FIELDS_ID);
            final String[] sortedids = new String[testmessages.length];
            for (int i = 0; i < searchMessages.length; i++) {
                sortedids[i] = searchMessages[i].getMailId();
            }
            final String[] rightsorteduids = new String[]{appendMessages[14], appendMessages[11], appendMessages[8], appendMessages[7], appendMessages[2], 
                appendMessages[13], appendMessages[6], appendMessages[4], appendMessages[5], appendMessages[12], appendMessages[15], appendMessages[0], 
                appendMessages[10], appendMessages[1], appendMessages[9], appendMessages[3]};
            for (final MailMessage msg : searchMessages) {
                System.out.println(msg.getSubject());
            }
            assertTrue("The sorting of the messages is not correct. Is: \n" + Arrays.toString(sortedids) + " but should be: \n" + Arrays.toString(rightsorteduids), Arrays.equals(sortedids, rightsorteduids));
        } finally {
            mailAccess.getFolderStorage().deleteFolder(tempFolder, true);
        }

    }
    
}
