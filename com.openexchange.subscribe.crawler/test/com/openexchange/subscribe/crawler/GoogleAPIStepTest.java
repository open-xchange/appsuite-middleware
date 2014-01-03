package com.openexchange.subscribe.crawler;

import java.util.Calendar;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.groupware.container.Contact;

/**
 * Unit tests for {@link GoogleAPIStep}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4.2
 */
@RunWith(PowerMockRunner.class)
public class GoogleAPIStepTest {

    @InjectMocks
    private GoogleAPIStep googleAPIStep;

    private Contact contact;

    @Before
    public void setUp() throws Exception {
        this.contact = new Contact();
    }

    @Test
    public void testSetBirthday_setCorrectly_contactHasCorrectBirthday() {
        String birthday = "1995-12-10";

        googleAPIStep.setBirthday(contact, birthday);

        final Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(1995, 11, 10);
        Assert.assertEquals("Returned birthday not as expected.", cal.getTime(), contact.getBirthday());
    }
}
