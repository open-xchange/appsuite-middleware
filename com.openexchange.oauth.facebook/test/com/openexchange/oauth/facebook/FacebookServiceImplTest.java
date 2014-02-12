package com.openexchange.oauth.facebook;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.util.TimeZones;

/**
 * Unit tests for {@link FacebookServiceImpl}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4.2
 */
@RunWith(PowerMockRunner.class)
public class FacebookServiceImplTest {

    @InjectMocks
    private FacebookServiceImpl facebookServiceImpl;

    private Contact contact;

    @Before
    public void setUp() throws Exception {
        this.contact = new Contact();
    }

    @Test
    public void testSetBirthday_correctPatternButNoValidEntry_doNotSaveBirthday() {
        String birthday = "00/00/0000";

        facebookServiceImpl.setBirthday(contact, birthday);

        Assert.assertNull(contact.getBirthday());
    }

    @Test
    public void testSetBirthday_noYear_doNotSaveBirthday() {
        String birthday = "00/00";

        facebookServiceImpl.setBirthday(contact, birthday);

        Assert.assertNull(contact.getBirthday());
    }

    @Test
    public void testSetBirthday_noYearButMonthAndDayValid_doNotSaveBirthday() {
        String birthday = "05/05";

        facebookServiceImpl.setBirthday(contact, birthday);

        Assert.assertNull(contact.getBirthday());
    }

    @Test
    public void testSetBirthday_validEntry_saveBirthday() throws ParseException {
        String birthday = "01/11/1982";

        facebookServiceImpl.setBirthday(contact, birthday);

        final Calendar cal = Calendar.getInstance(TimeZones.UTC);
        cal.clear();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        simpleDateFormat.setTimeZone(TimeZones.UTC);
        cal.setTime(simpleDateFormat.parse(birthday));

        Assert.assertEquals("Birthday not as expected!", cal.getTime(), contact.getBirthday());
    }

    @Test
    public void testSetBirthday_validEntry2_saveBirthday() throws ParseException {
        String birthday = "04/01/1982";

        facebookServiceImpl.setBirthday(contact, birthday);

        final Calendar cal = Calendar.getInstance(TimeZones.UTC);
        cal.clear();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        simpleDateFormat.setTimeZone(TimeZones.UTC);
        cal.setTime(simpleDateFormat.parse(birthday));

        Assert.assertEquals("Birthday not as expected!", cal.getTime(), contact.getBirthday());
    }

    @Test
    public void testSetBirthday_validEntry3_saveBirthday() throws ParseException {
        String birthday = "02/29/1982";

        facebookServiceImpl.setBirthday(contact, birthday);

        final Calendar cal = Calendar.getInstance(TimeZones.UTC);
        cal.clear();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        simpleDateFormat.setTimeZone(TimeZones.UTC);
        cal.setTime(simpleDateFormat.parse(birthday));

        Assert.assertEquals("Birthday not as expected!", cal.getTime(), contact.getBirthday());
    }

    @Test
    public void testSetEmail_mailIsNullString_doNotSaveMail() {
        String email = "null";

        facebookServiceImpl.setEmail(contact, email);

        Assert.assertNull(contact.getEmail1());
    }

    @Test
    public void testSetEmail_noDomain_doNotSaveMail() {
        String email = "martin.schneider";

        facebookServiceImpl.setEmail(contact, email);

        Assert.assertNull(contact.getEmail1());
    }

    @Test
    public void testSetEmail_onlyCompleteDomain_doNotSaveMail() {
        String email = "@open-xchange.de";

        facebookServiceImpl.setEmail(contact, email);

        Assert.assertNull(contact.getEmail1());
    }

    @Test
    public void testSetEmail_noTopLevelDomain_saveMail() {
        String email = "martin.schneider@open-xchange";

        facebookServiceImpl.setEmail(contact, email);

        Assert.assertEquals("Saved mail address differs from stored", email, contact.getEmail1());
    }

    @Test
    public void testSetEmail_validMailAddress_saveMailAddress() {
        String email = "martin.schneider@open-xchange.de";

        facebookServiceImpl.setEmail(contact, email);

        Assert.assertEquals("Saved mail address differs from stored", email, contact.getEmail1());
    }
}
