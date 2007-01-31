package com.openexchange.ajax.config;

import static com.openexchange.ajax.config.ConfigTools.getTimeZone;
import static com.openexchange.ajax.config.ConfigTools.readSetting;

import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.ajax.AbstractAJAXTest;

/**
 * Tests resulting from bug reports.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class BugTests extends AbstractAJAXTest {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(CurrentTimeTest.class);

    /**
     * Path to the configuration parameter.
     */
    private static final String DRAFTS_PATH = "mail/folder/drafts";

    /**
     * Path to the configuration parameter.
     */
    private static final String SENT_PATH = "mail/folder/sent";

    /**
     * Path to the configuration parameter.
     */
    private static final String SPAM_PATH = "mail/folder/spam";

    /**
     * Path to the configuration parameter.
     */
    private static final String TRASH_PATH = "mail/folder/trash";

    /**
     * Default constructor.
     * @param name Name of the test.
     */
    public BugTests(final String name) {
        super(name);
    }

    /**
     * Tests if the mail folder are sent correctly to the GUI.
     */
    public void testBug5607() throws Throwable {
        final String drafts = readSetting(getWebConversation(), getHostName(),
            getSessionId(), DRAFTS_PATH);
        assertNotNull("Can't get drafts folder.", drafts);

        final String sent = readSetting(getWebConversation(), getHostName(),
            getSessionId(), SENT_PATH);
        assertNotNull("Can't get sent folder.", sent);

        final String spam = readSetting(getWebConversation(), getHostName(),
            getSessionId(), SPAM_PATH);
        assertNotNull("Can't get spam folder.", spam);

        final String trash = readSetting(getWebConversation(), getHostName(),
            getSessionId(), TRASH_PATH);
        assertNotNull("Can't get trash folder.", trash);
    }

}
