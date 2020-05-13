
package com.openexchange.imap.threader.references;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.mockito.Mockito;
import com.openexchange.mail.dataobjects.MailMessage;

public class ConversationsTest {

    @Test
    public void test_fold() {
        // Create normal list
        List<Conversation> toFold = new ArrayList<>();
        {
            // add some examples
            toFold.add(new Conversation(mockMailMessage("A", "B")));
            toFold.add(new Conversation(mockMailMessage("B", "C")));
            toFold.add(new Conversation(mockMailMessage("C", "D")));

            toFold.add(new Conversation(mockMailMessage("A1", "B1")));
            toFold.add(new Conversation(mockMailMessage("B1", "C1")));
            toFold.add(new Conversation(mockMailMessage("C1", "D1")));

        }

        Conversations.fold(toFold);
        compareWithExpectedResult(2, toFold, createExpectedStrings(stringSet("A", "B", "C"), stringSet("A1", "B1" , "C1")));
    }

    @Test
    public void test_fold_2() {
        // Create normal list
        List<Conversation> toFold = new ArrayList<>();
        {
            // add some examples
            toFold.add(new Conversation(mockMailMessage("C1", "D")));
            toFold.add(new Conversation(mockMailMessage("C", "D")));
            toFold.add(new Conversation(mockMailMessage("B", "C")));
            toFold.add(new Conversation(mockMailMessage("A", "B")));
            toFold.add(new Conversation(mockMailMessage("B1", "C1")));
            toFold.add(new Conversation(mockMailMessage("A1", "B1")));

        }

        Conversations.fold(toFold);
        compareWithExpectedResult(1, toFold, createExpectedStrings(stringSet("A", "B", "C", "A1", "B1", "C1")));
    }
    
    @Test
    public void test_fold_3() {
        // Create normal list
        List<Conversation> toFold = new ArrayList<>();
        {
            // add some examples
            toFold.add(new Conversation(mockMailMessage("A", "B")));
            toFold.add(new Conversation(mockMailMessage("A1", "B1")));
            toFold.add(new Conversation(mockMailMessage("B", "C")));
            toFold.add(new Conversation(mockMailMessage("B1", "C1")));
            toFold.add(new Conversation(mockMailMessage("C", "D")));
            toFold.add(new Conversation(mockMailMessage("C1", "D")));

        }

        Conversations.fold(toFold);
        compareWithExpectedResult(1, toFold, createExpectedStrings(stringSet("A", "B", "C", "A1", "B1", "C1")));
    }
    
    private void compareWithExpectedResult(int size, List<Conversation> toFold, ArrayList<Set<String>> expectecMessageIdsLookup) {
        // convert to list for easier lookup
        ArrayList<Set<String>> messageIdsLookup = new ArrayList<>();
        for (Conversation conversation : toFold) {
            messageIdsLookup.add(conversation.getMessageIds());
        }

        assertThat("Expected IDs: " + expectecMessageIdsLookup + "\n" + "FoundIDs: " + messageIdsLookup, toFold, hasSize(size));
 
        
        for (Iterator<Set<String>> expectedIterator = expectecMessageIdsLookup.iterator(); expectedIterator.hasNext();) {
            Set<String> expected = expectedIterator.next();
            Iterator<Set<String>> iterator = messageIdsLookup.iterator();
            boolean found = false;
            while (!found && iterator.hasNext()) {
                Set<String> cur = iterator.next();
                if (expected.equals(cur)) {
                    iterator.remove();
                    expectedIterator.remove();
                    found = true;
                }
            }
        }


        if (!expectecMessageIdsLookup.isEmpty() || !messageIdsLookup.isEmpty()) {
            fail("Expected IDs: " + expectecMessageIdsLookup + "\n" + "FoundIDs: " + messageIdsLookup);
        }

    }

    private Set<String> stringSet(String... strings) {
        HashSet<String> stringSet = new HashSet<>();
        for (String string : strings) {
            stringSet.add(string);
        }
        return stringSet;
    }

    @SafeVarargs
    private final ArrayList<Set<String>> createExpectedStrings(Set<String>... sets) {
        ArrayList<Set<String>> expectecMessageIdsLookup = new ArrayList<>();
        for (Set<String> set : sets) {

            expectecMessageIdsLookup.add(set);
        }
        return expectecMessageIdsLookup;
    }

    private MailMessage mockMailMessage(String messageId, String... references) {
        MailMessage mailMessage = Mockito.mock(MailMessage.class);
        Mockito.when(mailMessage.getMailId()).thenReturn(messageId); // should be fine to use the messageId
        Mockito.when(mailMessage.getMessageId()).thenReturn(messageId);
        Mockito.when(mailMessage.getFolder()).thenReturn("example");
        Mockito.when(mailMessage.getReferences()).thenReturn(references);
        return mailMessage;
    }

    @SuppressWarnings("unused")
    private MailMessage mockMailMessageWithFolder(String messageId, String folder, String... references) {
        MailMessage mailMessage = Mockito.mock(MailMessage.class);
        Mockito.when(mailMessage.getMailId()).thenReturn(messageId); // should be fine to use the messageId
        Mockito.when(mailMessage.getMessageId()).thenReturn(messageId);
        Mockito.when(mailMessage.getFolder()).thenReturn(folder);
        Mockito.when(mailMessage.getReferences()).thenReturn(references);
        return mailMessage;
    }

}
