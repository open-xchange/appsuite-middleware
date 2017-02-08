
package com.openexchange.ajax.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import javax.mail.internet.InternetAddress;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.mail.actions.AllRequest;
import com.openexchange.ajax.mail.actions.AllResponse;
import com.openexchange.ajax.mail.actions.SendRequest;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 *
 * {@link AllRequestAndResponseTest} - tests the AllRequest and -Response
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 *
 */
public class AllRequestAndResponseTest extends AbstractMailTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AllTest.class);
    protected String folder;
    String mailObject_25kb;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        folder = getSentFolder();
        /*
         * Create JSON mail object
         */
        mailObject_25kb = createSelfAddressed25KBMailObject().toString();
        clearFolder(folder);
    }

    @After
    public void tearDown() throws Exception {
        try {
        clearFolder(folder);
        } finally {
            super.tearDown();
        }
    }

    @Test
    public void testAllResponseGetMailObjects() throws Exception {

        /*
         * Insert <numOfMails> mails through a send request
         */
        final int numOfMails = 1;
        LOG.info("Sending " + numOfMails + " mails to fill emptied INBOX");
        for (int i = 0; i < numOfMails; i++) {
            getClient().execute(new SendRequest(mailObject_25kb));
            LOG.info("Sent " + (i + 1) + ". mail of " + numOfMails);
        }

        AllResponse allR = Executor.execute(getSession(), new AllRequest(getInboxFolder(), COLUMNS_DEFAULT_LIST, 0, null, true));
        if (allR.hasError()) {
            fail(allR.getException().toString());
        }
        MailMessage[] mailMessages = allR.getMailMessages(COLUMNS_DEFAULT_LIST);
        for (MailMessage mailMessage : mailMessages) {
            assertEquals("From is not equal", new InternetAddress(getSendAddress()), mailMessage.getFrom()[0]);
            assertEquals("Subject is not equal", MAIL_SUBJECT, mailMessage.getSubject());
        }
    }

}
