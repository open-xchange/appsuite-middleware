package com.openexchange.mail.messagestorage;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.HeaderCollection;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mail.permission.MailPermission;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.sessiond.impl.SessionObject;

/**
 * An abstract class which contains methods used for alle MessageStorage based tests
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public abstract class MessageStorageTest extends AbstractMailTest {

    public static final String[] NON_MATCHING_HEADERS = {
        MessageHeaders.HDR_FROM, MessageHeaders.HDR_TO, MessageHeaders.HDR_CC, MessageHeaders.HDR_BCC, MessageHeaders.HDR_DISP_NOT_TO,
        MessageHeaders.HDR_REPLY_TO, MessageHeaders.HDR_SUBJECT, MessageHeaders.HDR_DATE, MessageHeaders.HDR_IMPORTANCE, MessageHeaders.HDR_X_PRIORITY,
        MessageHeaders.HDR_MESSAGE_ID, MessageHeaders.HDR_IN_REPLY_TO, MessageHeaders.HDR_REFERENCES, MessageHeaders.HDR_X_OX_VCARD,
        MessageHeaders.HDR_X_OX_NOTIFICATION };

    protected static final MailField[] FIELDS_ID = { MailField.ID };

//    private static final MailField[] RELEVANT_FIELD = { MailField.ID, MailField.FOLDER_ID, MailField.CONTENT_TYPE, MailField.FROM, MailField.TO, MailField.CC,
//        MailField.BCC, MailField.SUBJECT, MailField.SIZE, MailField.SENT_DATE, MailField.RECEIVED_DATE, MailField.FLAGS, MailField.THREAD_LEVEL,
//        MailField.DISPOSITION_NOTIFICATION_TO, MailField.PRIORITY, MailField.COLOR_LABEL, MailField.HEADERS, MailField.BODY };

    protected static final MailField[] FIELDS_MORE = { MailField.ID, MailField.CONTENT_TYPE, MailField.FLAGS, MailField.BODY };

    protected MailAccess<?, ?> mailAccess = null;

    protected MailMessage[] testmessages = null;

    /**
     *
     */
    protected MessageStorageTest() {
        super();
    }

    protected MessageStorageTest(final String name) {
        super(name);
    }


    protected static MailField[][] generateVariations() {
        final MailField[] values = MailField.values();
        final int number = 1 << values.length ;
        final MailField[][] retval = new MailField[number][];
        int[] indices;
        int t = 0;
        for (int o = 1; o <= values.length; o++) {
            final CombinationGenerator x = new CombinationGenerator(values.length, o);
            while (x.hasMore()) {
                indices = x.getNext();
                retval[t] = new MailField[indices.length];
                for (int i = 0; i < indices.length; i++) {
                    retval[t][i] = values[indices[i]];
                }
                t++;
            }
        }
        return retval;
    }

    private static long fac(final long l) {
        long retval = 1;
        for (long i = 1; i < l; i++) {
            retval *= i;
        }
        return retval;
    }

    private static boolean isValidAddressHeader(final MailMessage mail, final String name) {
        final String addressStr = mail.getHeader(name, ',');
        if (null == addressStr || addressStr.length() == 0) {
            return true;
        }
        try {
            InternetAddress.parse(MimeMessageUtility.decodeMultiEncodedHeader(addressStr), true);
            // Valid addresses
            return true;
        } catch (final Exception e) {
            // No valid addresses
            return false;
        }
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
    protected void compareMailMessages(final MailMessage mail1, final MailMessage mail2, final MailField[] fields1, final MailField[] fields2, final String mail1name, final String mail2name, final boolean firstvalueParsed) {
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
            if (isValidAddressHeader(mail1, "From")) {
                check("From", mail1.getFrom(), mail2.getFrom(), mail1name, mail2name);
            }
        }
        if (comparefields.contains(MailField.TO) || comparefields.contains(MailField.FULL)) {
            if (isValidAddressHeader(mail1, "To")) {
                check("To", mail1.getTo(), mail2.getTo(), mail1name, mail2name);
            }
        }
        if (comparefields.contains(MailField.DISPOSITION_NOTIFICATION_TO) || comparefields.contains(MailField.FULL)) {
            if (isValidAddressHeader(mail1, "Disposition-Notification-To")) {
                check("Disposition-Notification-To", mail1.getDispositionNotification(), mail2.getDispositionNotification(), mail1name, mail2name);
            }
        }
        if (comparefields.contains(MailField.COLOR_LABEL) || comparefields.contains(MailField.FULL)) {
            check("Color label", mail1.getColorLabel(), mail2.getColorLabel(), mail1name, mail2name);
        }
        if (comparefields.contains(MailField.HEADERS) || comparefields.contains(MailField.FULL)) {
            // Checking complete equality of headers makes no sense
            // TODO: Define headers to check not occurring in MIMEMessageConverter.NON_MATCHING_HEADERS
            //check("Headers", hc1, hc2, mail1name, mail2name);
        }
        if (comparefields.contains(MailField.SUBJECT) || comparefields.contains(MailField.FULL)) {
            check("Subject", mail1.getSubject(), mail2.getSubject(), mail1name, mail2name);
        }
        if (comparefields.contains(MailField.THREAD_LEVEL) || comparefields.contains(MailField.FULL)) {
            check("Thread level", mail1.getThreadLevel(), mail2.getThreadLevel(), mail1name, mail2name);
        }
/*
 * The size test cannot be done, because the right size value is depending on the underlying mail system
 * and the way how the underlying mail system stored the messages (with CRLF line termination or with LF
 * line termination etc.)
 */
/*-        if (comparefields.contains(MailField.SIZE) || comparefields.contains(MailField.FULL)) {
 *           check("Size", mail1.getSize(), mail2.getSize(), mail1name, mail2name);
 *         }
*/
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
            if (isValidAddressHeader(mail1, "Cc")) {
                check("CC", mail1.getCc(), mail2.getCc(), mail1name, mail2name);
            }
        }
        if (comparefields.contains(MailField.BCC) || comparefields.contains(MailField.FULL)) {
            if (isValidAddressHeader(mail1, "Bcc")) {
                check("BCC", mail1.getBcc(), mail2.getBcc(), mail1name, mail2name);
            }
        }
        if (comparefields.contains(MailField.FOLDER_ID) || comparefields.contains(MailField.FULL)) {
            check("Folder id", mail1.getFolder(), mail2.getFolder(), mail1name, mail2name);
        }

    }

    /**
     * @param session
     * @param mailAccess
     * @return The name of the temporary folder which is created
     * @throws OXException
     */
    protected String createTemporaryFolder(final SessionObject session, final MailAccess<?, ?> mailAccess) throws OXException {
        return createTemporaryFolderAndGetFullname(session, mailAccess, "TemporaryFolder");
    }

    /**
     * Creates a folder with tempFolderName under the inbox
     *
     * @param session
     * @param mailAccess
     * @param tempFolderName
     * @return
     * @throws OXException
     */
    protected String createTemporaryFolderAndGetFullname(final SessionObject session, final MailAccess<?, ?> mailAccess, final String tempFolderName) throws OXException {
        final String fullname;
        final MailFolder inbox = mailAccess.getFolderStorage().getFolder("INBOX");
        final String parentFullname;
        if (inbox.isHoldsFolders()) {
            fullname = new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append(
                tempFolderName).toString();
            parentFullname = "INBOX";
        } else {
            fullname = tempFolderName;
            parentFullname = MailFolder.DEFAULT_FOLDER_ID;
        }

        final MailFolderDescription mfd = new MailFolderDescription();
        mfd.setExists(false);
        mfd.setParentFullname(parentFullname);
        mfd.setSeparator(inbox.getSeparator());
        mfd.setName(tempFolderName);

        mfd.addPermission(getPermission(session));
        mailAccess.getFolderStorage().createFolder(mfd);
        return fullname;
    }

    protected MailAccess<?, ?> getMailAccess() throws OXException {
        final SessionObject session = getSession();

        final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
        mailAccess.connect();
        return mailAccess;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.testmessages = getMessages(getTestMailDir(), -1);
        this.mailAccess = getMailAccess();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mailAccess.close(false);
    }

    private void check(final String string, final HeaderCollection value1, final HeaderCollection value2, final String mail1name, final String mail2name) {
        final StringBuilder sb = new StringBuilder();
        sb.append(string);
        sb.append(" of ");
        sb.append(mail1name);
        sb.append(": ``");
        sb.append(value1);
        sb.append("'' is not equal with ");
        sb.append(mail2name);
        sb.append(": ``");
        sb.append(value2).append("''");
        assertTrue(sb.toString(), equalsCheckWithNull(value1, value2));
    }


    private void check(final String string, final int value1, final int value2, final String mail1name, final String mail2name) {
        final StringBuilder sb = new StringBuilder();
        sb.append(string);
        sb.append(" of ");
        sb.append(mail1name);
        sb.append(": ``");
        sb.append(value1);
        sb.append("'' is not equal with ");
        sb.append(mail2name);
        sb.append(": ``");
        sb.append(value2);
        sb.append("''");
        assertTrue(sb.toString(), value1 == value2);
    }
    private void check(final String string, final InternetAddress[] address1, final InternetAddress[] address2, final String mail1name, final String mail2name) {
        final StringBuilder sb = new StringBuilder(128);
        sb.append(string);
        sb.append(" of ");
        sb.append(mail1name);
        sb.append(": ``");
        sb.append(Arrays.toString(address1));
        sb.append("'' are not equal with ");
        sb.append(mail2name);
        sb.append(": ``");
        sb.append(Arrays.toString(address2));
        sb.append("''");
        assertTrue(sb.toString(), equals(address1, address2));
    }

    private boolean equals(final InternetAddress[] a, final InternetAddress[] a2) {
        if (a==a2) {
            return true;
        }
        if (a==null || a2==null) {
            return false;
        }
        int length = a.length;
        if (a2.length != length) {
            return false;
        }
        for (int i=0; i<length; i++) {
            InternetAddress o1 = a[i];
            InternetAddress o2 = a2[i];
            if (!(o1==null ? o2==null : o1.getAddress().equals(o2.getAddress()))) {
                return false;
            }
        }
        return true;
    }

    private void check(final String string, final long value1, final long value2, final String mail1name, final String mail2name) {
        final StringBuilder sb = new StringBuilder();
        sb.append(string);
        sb.append(" of ");
        sb.append(mail1name);
        sb.append(": ``");
        sb.append(value1);
        sb.append("'' are not equal with ");
        sb.append(mail2name);
        sb.append(": ``");
        sb.append(value2);
        sb.append("''");
        assertTrue(sb.toString(), value1 == value2);
    }

    private void check(final String string, final Object value1, final Object value2, final String mail1name, final String mail2name) {
        final StringBuilder sb = new StringBuilder();
        sb.append(string);
        sb.append(" of ");
        sb.append(mail1name);
        sb.append(": ``");
        sb.append(value1);
        sb.append("'' is not equal with ");
        sb.append(mail2name);
        sb.append(": ``");
        sb.append(value2).append("''");
        assertTrue(sb.toString(), equalsCheckWithNull(value1, value2));
    }

    private void check(final String string, final String value1, final String value2, final String mail1name, final String mail2name) {
        final StringBuilder sb = new StringBuilder();
        sb.append(string);
        sb.append(" of ");
        sb.append(mail1name);
        sb.append(": ``");
        sb.append(value1);
        sb.append("'' is not equal with ");
        sb.append(mail2name);
        sb.append(": ``");
        sb.append(value2).append("''");
        assertTrue(sb.toString(), equalsCheckWithNull(value1, value2));
    }

    private void checkFieldsSet(final MailMessage mail, final Set<MailField> set, final String mailname, final boolean parsed) {
        final boolean full = set.contains(MailField.FULL);
        if (full || set.contains(MailField.ID)) {
            assertTrue("Missing mail ID in " + mailname, mail.getMailId() != null);
        }
        if (full || set.contains(MailField.FLAGS)) {
            assertTrue("Missing flags in " + mailname, mail.containsFlags());
        } else {
            if (parsed) {
                assertTrue(
                    "Flags set in " + mailname + " although not requested",
                    !mail.containsFlags() || (mail.containsFlags() && mail.getFlags() == 0));
            }
            // Don't complain about additional information
            // else {
            // assertTrue("Flags set in " + mailname + " although not requested", !mail.containsFlags());
            // }
        }
        final boolean headers = set.contains(MailField.HEADERS);
        if (full || headers) {
            assertTrue("Missing headers in " + mailname, mail.containsHeaders());
        }
        //else {
        //    assertTrue("Headers set in " + mailname + " although not requested", !mail.containsHeaders());
        //}
        // If headers are requested the from part is automatically filled so we handle the request for headers like
        // a request for the from field
        if (full || set.contains(MailField.FROM) || headers) {
            assertTrue("Missing From in " + mailname, mail.containsFrom());
        }
        // If headers are requested the to part is automatically filled so we handle the request for headers like
        // a request for the to field
        if (full || set.contains(MailField.TO) || headers) {
            assertTrue("Missing To in " + mailname, mail.containsTo());
        }
        // Don't complain about an extra field
        // else {
        //    assertTrue("To set in " + mailname + " although not requested", !mail.containsTo());
        // }

        // If headers are requested there's no need to check the contains methods because they will
        // completely depend on the values which are stored in the message headers, and for the
        // following headers which are not mandatory we can't say for sure
        if (!headers) {
            if (full || set.contains(MailField.CC)) {
                assertTrue("Missing Cc in " + mailname, mail.containsCc());
            }
            if (full || set.contains(MailField.BCC)) {
                assertTrue("Missing Bcc in " + mailname, mail.containsBcc());
            }
            if (full || set.contains(MailField.CONTENT_TYPE)) {
                assertTrue("Missing content type in " + mailname, mail.containsContentType());
            }
            if (full || set.contains(MailField.DISPOSITION_NOTIFICATION_TO)) {
                assertTrue("Missing Disposition-Notification-To in " + mailname, mail.containsDispositionNotification());
            }
            if (full || set.contains(MailField.PRIORITY)) {
                assertTrue("Missing priority in " + mailname, mail.containsPriority());
            }
        }
        if (full || set.contains(MailField.COLOR_LABEL)) {
            assertTrue("Missing color label in " + mailname, mail.containsColorLabel());
        }
        // Don't complain about additional information
        // else {
        // assertTrue("Color label set in " + mailname + " although not requested", !mail.containsColorLabel());
        // }
        if (full || set.contains(MailField.SUBJECT) || headers) { // As Subject is a part of the headers it will be automatically fetched
                                                                  // when headers are requested
            assertTrue("Missing subject in " + mailname, mail.containsSubject());
        }
        // Don't complain about additional information
        // else {
        // assertTrue("Subject set in " + mailname + " although not requested", !mail.containsSubject());
        // }
        if (full || set.contains(MailField.THREAD_LEVEL)) {
            assertTrue("Missing thread level in " + mailname, mail.containsThreadLevel());
        }
        // Don't complain about additional information
        // else {
        // assertTrue("Thread level set in " + mailname + " although not requested", !mail.containsThreadLevel());
        // }
        if (full || set.contains(MailField.SIZE)) {
            assertTrue("Missing size in " + mailname, mail.containsSize());
        }
        // Don't complain about additional information
        // else {
        // assertTrue("Size set in " + mailname + " although not requested", !mail.containsSize());
        // }
        if (full || set.contains(MailField.SENT_DATE) || headers) { // As sent date is a part of the headers it will be automatically
                                                                    // fetched when headers are requested
            assertTrue("Missing sent date in " + mailname, mail.containsSentDate());
        }
        // Don't complain about additional information
        // else {
        // assertTrue("Sent date set in " + mailname + " although not requested", !mail.containsSentDate());
        // }
        if (full || set.contains(MailField.RECEIVED_DATE)) {
            assertTrue("Missing received date in " + mailname, mail.containsReceivedDate());
        }
        // Don't complain about additional information
        // else {
        // if (parsed) {
        // assertTrue("Received date set in " + mailname + " although not requested", !mail.containsReceivedDate() ||
        // (mail.containsReceivedDate() && null == mail.getReceivedDate()));
        // } else {
        // assertTrue("Received date set in " + mailname + " although not requested", !mail.containsReceivedDate());
        // }
        // }
        if (full || set.contains(MailField.FOLDER_ID)) {
            assertTrue("Missing folder fullname in " + mailname, mail.containsFolder());
        }
        // Don't complain about additional information
        // else {
        // assertTrue("Folder fullname set in " + mailname + " although not requested", !mail.containsFolder());
        // }
    }

    private boolean equalsCheckWithNull(final HeaderCollection a, final HeaderCollection b) {
        if (null == a) {
            return null == b;
        } else {
            // Here we have to remove the X-OX-Marker header because this is only used internally and should
            // not be included in the test
            // Because the HeaderCollection of the mails is read-only, we have to duplicate the collections
            // in order to remove this headers...
            final HeaderCollection headerCollectiona = new HeaderCollection(a);
            final HeaderCollection headerCollectionb = new HeaderCollection(b);
            if (a.containsHeader("X-OX-Marker")) {
                headerCollectiona.removeHeader("X-OX-Marker");
            } else if (b.containsHeader("X-OX-Marker")) {
                headerCollectionb.removeHeader("X-OX-Marker");
            }
            return headerCollectiona.equals(headerCollectionb);
        }
    }

    private boolean equalsCheckWithNull(final Object a, final Object b) {
        if (null == a) {
            return null == b;
        } else {
            return a.equals(b);
        }
    }

    private boolean equalsCheckWithNull(final String a, final String b) {
        if (null == a) {
            return null == b;
        } else {
            return a.trim().equals(b.trim());
        }
    }

    private String getFullFolderName(final MailFolder inbox, final String tempFolderName) {
        return new StringBuilder(inbox.getFullname()).append(inbox.getSeparator()).append(
        		tempFolderName).toString();
    }

    private MailPermission getPermission(final SessionObject session) throws OXException {
        final MailPermission p = MailProviderRegistry.getMailProviderBySession(session, MailAccount.DEFAULT_ID)
        		.createNewMailPermission(session, MailAccount.DEFAULT_ID);
        p.setEntity(getUser());
        p.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION,
        		OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        p.setFolderAdmin(true);
        p.setGroupPermission(false);
        return p;
    }

}
