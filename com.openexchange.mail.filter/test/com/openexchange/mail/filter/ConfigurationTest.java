package com.openexchange.mail.filter;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.mailfilter.exceptions.MailFilterExceptionCode;
import com.openexchange.mailfilter.osgi.Activator;


public class ConfigurationTest extends Activator {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testNoPasswordSourceAndNoMasterPassword() throws Exception {
        Common.prepare("", "");
        try {
            checkConfigfile();
            Assert.fail("No exception thrown");
        } catch (final OXException e) {
            Assert.assertTrue(MailFilterExceptionCode.NO_VALID_PASSWORDSOURCE.equals(e));
        }
    }

    @Test
    public void testPasswordSourceAndNoMasterPassword() throws Exception {
        Common.prepare("session", "");
        checkConfigfile();
    }

    @Test
    public void testPasswordSourceGlobalAndNoMasterPassword() throws Exception {
        Common.prepare("global", "");
        try {
            checkConfigfile();
            Assert.fail("No exception thrown");
        } catch (final OXException e) {
            Assert.assertTrue(MailFilterExceptionCode.NO_MASTERPASSWORD_SET.equals(e));
        }
    }

    @Test
    public void testPasswordSourceGlobalAndMasterPassword() throws Exception {
        Common.prepare("global", "secret");
        checkConfigfile();
    }

}
