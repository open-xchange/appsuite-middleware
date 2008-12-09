package com.openexchange.mail.messagestorage;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import javax.mail.internet.InternetAddress;

import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailField;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.sessiond.impl.SessionObject;

/**
 * An abstract class which contains methods used for alle MessageStorage based tests
 * 
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public abstract class MessageStorageTest extends AbstractMailTest {

    protected static final MailField[] FIELDS_ID = { MailField.ID };

    protected static final MailField[] FIELDS_MORE = { MailField.ID, MailField.CONTENT_TYPE, MailField.FLAGS, MailField.BODY };

    private static final MailField[] RELEVANT_FIELD = { MailField.ID, MailField.FOLDER_ID, MailField.CONTENT_TYPE, MailField.FROM, MailField.TO, MailField.CC, 
        MailField.BCC, MailField.SUBJECT, MailField.SIZE, MailField.SENT_DATE, MailField.RECEIVED_DATE, MailField.FLAGS, MailField.THREAD_LEVEL,
        MailField.DISPOSITION_NOTIFICATION_TO, MailField.PRIORITY, MailField.COLOR_LABEL, MailField.HEADERS, MailField.BODY };
    
    /**
     * 
     */
    protected MessageStorageTest() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param name
     */
    protected MessageStorageTest(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }

    /**
     * Compares if two MailMessage object are equal. Only the fields specified
     * are checked
     * 
     * @param mail1
     * @param mail2
     * @param fields1
     * @param fields2
     *            TODO
     * @param mail1name
     *            TODO
     * @param mail2name
     *            TODO
     * @param firstvalueParsed Definies if the first mail message object comes out of the parser, this is essential because in this case
     *                         some part behave different. So e.g. the mailid is contained but null, same for the receive date
     */
    protected void compareMailMessages(final MailMessage mail1, final MailMessage mail2, final MailField[] fields1, final MailField[] fields2, final String mail1name, final String mail2name, boolean firstvalueParsed) {
        // First check if all field of the two objects are set...
        final Set<MailField> set1 = EnumSet.copyOf(Arrays.asList(fields1));
        final Set<MailField> set2 = EnumSet.copyOf(Arrays.asList(fields2));
        checkFieldsSet(mail1, set1, mail1name, firstvalueParsed);
        checkFieldsSet(mail2, set2, mail2name, false);

        final Set<MailField> comparefields;
        
        if (set1.contains(MailField.FULL) && !set2.contains(MailField.FULL)) {
            comparefields = set2;
        } else if (set2.contains(MailField.FULL) && !set1.contains(MailField.FULL)) {
            comparefields = set1;
        } else if (set1.size() > set2.size()){
            comparefields = EnumSet.copyOf(set1); 
            comparefields.retainAll(set2);
        } else {
            comparefields = EnumSet.copyOf(set2);
            set2.retainAll(set1);
        }
        
        if (comparefields.contains(MailField.ID) || comparefields.contains(MailField.FULL)) {
            check("Mail ID", mail1.getMailId(), mail2.getMailId(), mail1name, mail2name);
        }
        if (comparefields.contains(MailField.CONTENT_TYPE) || comparefields.contains(MailField.FULL)) {
            check("Content type", mail1.getContentType(), mail2.getContentType(), mail1name, mail2name);
        }
        if (comparefields.contains(MailField.FLAGS) || comparefields.contains(MailField.FULL)) {
            check("Flags", mail1.getFlags(), mail2.getFlags(), mail1name, mail2name);
        }
        if (comparefields.contains(MailField.FROM) || comparefields.contains(MailField.FULL)) {
            check("From", mail1.getFrom(), mail2.getFrom(), mail1name, mail2name);
        }
        if (comparefields.contains(MailField.TO) || comparefields.contains(MailField.FULL)) {
            check("To", mail1.getTo(), mail2.getTo(), mail1name, mail2name);
        }
        if (comparefields.contains(MailField.DISPOSITION_NOTIFICATION_TO) || comparefields.contains(MailField.FULL)) {
            check("Disposition-Notification-To", mail1.getDispositionNotification(), mail2.getDispositionNotification(), mail1name, mail2name);
        }
        if (comparefields.contains(MailField.COLOR_LABEL) || comparefields.contains(MailField.FULL)) {
            check("Color label", mail1.getColorLabel(), mail2.getColorLabel(), mail1name, mail2name);
        }
// TODO: Disable until server is fixed
//        if (comparefields.contains(MailField.HEADERS) || comparefields.contains(MailField.FULL)) {
//            check("Headers", mail1.getHeaders(), mail2.getHeaders(), mail1name, mail2name);
//        }
        if (comparefields.contains(MailField.SUBJECT) || comparefields.contains(MailField.FULL)) {
            check("Subject", mail1.getSubject(), mail2.getSubject(), mail1name, mail2name);
        }
        if (comparefields.contains(MailField.THREAD_LEVEL) || comparefields.contains(MailField.FULL)) {
            check("Thread level", mail1.getThreadLevel(), mail2.getThreadLevel(), mail1name, mail2name);
        }
// TODO: The size test cannot be done at the moment, because the right size value has to be defined first
// On the on hand we have the problem that java mail gives the size without the size of the header on the 
// other hand some implementations make \r\n out of \n. And last but not least we have the problem that other
// implementations add new headers to the mails
//        if (comparefields.contains(MailField.SIZE) || comparefields.contains(MailField.FULL)) {
//            check("Size", mail1.getSize(), mail2.getSize(), mail1name, mail2name);
//        }
        if (comparefields.contains(MailField.PRIORITY) || comparefields.contains(MailField.FULL)) {
            check("Priority", mail1.getPriority(), mail2.getPriority(), mail1name, mail2name);
        }
        if (comparefields.contains(MailField.SENT_DATE) || comparefields.contains(MailField.FULL)) {
            check("Sent date", mail1.getSentDate(), mail2.getSentDate(), mail1name, mail2name);
        }
        if (comparefields.contains(MailField.RECEIVED_DATE) || comparefields.contains(MailField.FULL)) {
            check("Received date", mail1.getReceivedDate(), mail2.getReceivedDate(), mail1name, mail2name);
        }
        if (comparefields.contains(MailField.CC) || comparefields.contains(MailField.FULL)) {
            check("CC", mail1.getCc(), mail2.getCc(), mail1name, mail2name);
        }
        if (comparefields.contains(MailField.BCC) || comparefields.contains(MailField.FULL)) {
            check("BCC", mail1.getBcc(), mail2.getBcc(), mail1name, mail2name);
        }
        if (comparefields.contains(MailField.FOLDER_ID) || comparefields.contains(MailField.FULL)) {
            check("Folder id", mail1.getFolder(), mail2.getFolder(), mail1name, mail2name);
        }
        
    }
    
    protected MailAccess<?, ?> getMailAccess() throws MailException {
        final SessionObject session = getSession();

        final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
        mailAccess.connect();
        return mailAccess;
    }
    
    private void check(final String string, final int value1, final int value2, final String mail1name, final String mail2name) {
        assertTrue(string + " of " + mail1name + ":" + value1 + " are not equal with " + mail2name + ":" + value2, value1 == value2);
    }

    private void check(final String string, final InternetAddress[] address1, final InternetAddress[] address2, final String mail1name, final String mail2name) {
        assertTrue(string + " of " + mail1name + ":" + Arrays.toString(address1) + " is not equal with " + mail2name + ":" + Arrays.toString(address2),
                Arrays.equals(address1, address2));
    }
    
    private void check(final String string, final long value1, final long value2, final String mail1name, final String mail2name) {
        assertTrue(string + " of " + mail1name + ":" + value1 + " are not equal with " + mail2name + ":" + value2, value1 == value2);
    }
    
    private void check(final String string, final Object value1, final Object value2, final String mail1name, final String mail2name) {
        assertTrue(string + " of " + mail1name + ":" + value1 + " is not equal with " + mail2name + ":" + value2, equalsCheckWithNull(value1, value2));
    }

    private void check(final String string, final byte[] value1, final byte[] value2, final String mail1name, final String mail2name) {
        assertTrue(string + " of " + mail1name + ":" + value1 + " is not equal with " + mail2name + ":" + value2, Arrays.equals(value1, value2));
    }
    
    private void checkFieldsSet(final MailMessage mail, final Set<MailField> set, final String mailname, boolean parsed) {
        if (set.contains(MailField.ID) || set.contains(MailField.FULL)) {
            assertTrue("Missing mail ID in " + mailname, mail.getMailId() != -1);
        } else {
            assertTrue("Mail ID set in " + mailname + " although not requested", mail.getMailId() == -1);
        }
        if (set.contains(MailField.CONTENT_TYPE) || set.contains(MailField.FULL)) {
            assertTrue("Missing content type in " + mailname, mail.containsContentType());
        } else {
            assertTrue("Content type set in " + mailname + " although not requested", !mail.containsContentType());
        }
        if (set.contains(MailField.FLAGS) || set.contains(MailField.FULL)) {
            assertTrue("Missing flags in " + mailname, mail.containsFlags());
        } else {
            if (parsed) {
                assertTrue("Flags set in " + mailname + " although not requested", !mail.containsFlags() || (mail.containsFlags() && mail.getFlags() == 0));
            } else {
                assertTrue("Flags set in " + mailname + " although not requested", !mail.containsFlags());
            }
        }
        if (set.contains(MailField.FROM) || set.contains(MailField.FULL)) {
            assertTrue("Missing From in " + mailname, mail.containsFrom());
        } else {
            assertTrue("From set in " + mailname + " although not requested", !mail.containsFrom());
        }
        if (set.contains(MailField.TO) || set.contains(MailField.FULL)) {
            assertTrue("Missing To in " + mailname, mail.containsTo());
        } else {
            assertTrue("To set in " + mailname + " although not requested", !mail.containsTo());
        }
        if (set.contains(MailField.DISPOSITION_NOTIFICATION_TO) || set.contains(MailField.FULL)) {
            assertTrue("Missing Disposition-Notification-To in " + mailname, mail.containsDispositionNotification());
        } else {
            assertTrue("Disposition-Notification-To set in " + mailname + " although not requested", !mail.containsDispositionNotification());
        }
        if (set.contains(MailField.COLOR_LABEL) || set.contains(MailField.FULL)) {
            assertTrue("Missing color label in " + mailname, mail.containsColorLabel());
        } else {
            assertTrue("Color label set in " + mailname + " although not requested", !mail.containsColorLabel());
        }
        if (set.contains(MailField.HEADERS) || set.contains(MailField.FULL)) {
            assertTrue("Missing headers in " + mailname, mail.containsHeaders());
        } else {
            assertTrue("Headers set in " + mailname + " although not requested", !mail.containsHeaders());
        }
        if (set.contains(MailField.SUBJECT) || set.contains(MailField.FULL)) {
            assertTrue("Missing subject in " + mailname, mail.containsSubject());
        } else {
            assertTrue("Subject set in " + mailname + " although not requested", !mail.containsSubject());
        }
        if (set.contains(MailField.THREAD_LEVEL) || set.contains(MailField.FULL)) {
            assertTrue("Missing thread level in " + mailname, mail.containsThreadLevel());
        } else {
            assertTrue("Thread level set in " + mailname + " although not requested", !mail.containsThreadLevel());
        }
        if (set.contains(MailField.SIZE) || set.contains(MailField.FULL)) {
            assertTrue("Missing size in " + mailname, mail.containsSize());
        } else {
            assertTrue("Size set in " + mailname + " although not requested", !mail.containsSize());
        }
        if (set.contains(MailField.PRIORITY) || set.contains(MailField.FULL)) {
            assertTrue("Missing priority in " + mailname, mail.containsPriority());
        } else {
            assertTrue("Priority set in " + mailname + " although not requested", !mail.containsPriority());
        }
        if (set.contains(MailField.SENT_DATE) || set.contains(MailField.FULL)) {
            assertTrue("Missing sent date in " + mailname, mail.containsSentDate());
        } else {
            assertTrue("Sent date set in " + mailname + " although not requested", !mail.containsSentDate());
        }
        if (set.contains(MailField.RECEIVED_DATE) || set.contains(MailField.FULL)) {
            assertTrue("Missing received date in " + mailname, mail.containsReceivedDate());
        } else {
            if (parsed) {
                assertTrue("Received date set in " + mailname + " although not requested", !mail.containsReceivedDate() || (mail.containsReceivedDate() && null == mail.getReceivedDate()));
            } else {
                assertTrue("Received date set in " + mailname + " although not requested", !mail.containsReceivedDate());
            }
        }
        if (set.contains(MailField.CC) || set.contains(MailField.FULL)) {
            assertTrue("Missing Cc in " + mailname, mail.containsCc());
        } else {
            assertTrue("Cc set in " + mailname + " although not requested", !mail.containsCc());
        }
        if (set.contains(MailField.BCC) || set.contains(MailField.FULL)) {
            assertTrue("Missing Bcc in " + mailname, mail.containsBcc());
        } else {
            assertTrue("Bcc set in " + mailname + " although not requested", !mail.containsBcc());
        }
        if (set.contains(MailField.FOLDER_ID) || set.contains(MailField.FULL)) {
            assertTrue("Missing folder fullname in " + mailname, mail.containsFolder());
        } else {
            assertTrue("Folder fullname set in " + mailname + " although not requested", !mail.containsFolder());
        }
    }

    private boolean equalsCheckWithNull(final Object a, final Object b) {
        if (null == a) {
            if (null == b) {
                return true;
            } else {
                return false;
            }
        } else {
            return a.equals(b);
        }
    }
    
    protected MailField[][] generateVariations() {
        int number = 0;
        for (int i = 0; i < RELEVANT_FIELD.length; i++) {
            number += fac(RELEVANT_FIELD.length)/(fac(i)*fac(RELEVANT_FIELD.length-i));
        }
        final MailField[][] retval = new MailField[number][];
        int[] indices;
        int t = 0;
        for (int o = 1; o <= RELEVANT_FIELD.length; o++) {
            CombinationGenerator x = new CombinationGenerator(RELEVANT_FIELD.length, o);
//            StringBuffer combination;
            while (x.hasMore()) {
//                combination = new StringBuffer();
                indices = x.getNext();
                retval[t] = new MailField[indices.length];
                for (int i = 0; i < indices.length; i++) {
                    retval[t][i] = RELEVANT_FIELD[indices[i]];
//                    combination.append(RELEVANT_FIELD[indices[i]]);
//                    combination.append(",");
                }
                t++;
//                System.out.println(combination.toString());
            }
        }
        //        for (int o = 0; o < RELEVANT_FIELD.length; o++) {
//            retval[i] = new MailField[1];
//            retval[i][0] = RELEVANT_FIELD[o];
//            i++;
//        }
//        for (int o = 0; o < RELEVANT_FIELD.length; o++) {
//            for (int t = o + 1; t < RELEVANT_FIELD.length; t++) {
//                retval[i] = new MailField[2];
//                retval[i][0] = RELEVANT_FIELD[o];
//                retval[i][1] = RELEVANT_FIELD[t];
//            }
//        }
        // And finally the whole:
//        retval[i] = RELEVANT_FIELD;
        return retval;
    }

    private long fac(long l) {
        long retval = 1;
        for (long i = 1; i < l; i++) {
            retval *= i;
        }
        return retval;
    }

}
