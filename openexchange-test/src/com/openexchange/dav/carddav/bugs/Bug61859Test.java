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

package com.openexchange.dav.carddav.bugs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.List;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.VCardResource;
import com.openexchange.groupware.container.Contact;
import net.sourceforge.cardme.vcard.arch.VCardTypeName;
import net.sourceforge.cardme.vcard.types.EmailType;
import net.sourceforge.cardme.vcard.types.params.EmailParamType;
import net.sourceforge.cardme.vcard.types.params.ExtendedParamType;

/**
 * {@link Bug61859Test}
 *
 * CardDAV: weird / unexpected behaviour when entering / syncing CardDAV addresses
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.2
 */
public class Bug61859Test extends CardDAVTest {

    @Test
    public void testCreateWithFourEmails() throws Exception {
        /*
         * prepare vCard
         */
        String uid = randomUID();
        String vCard = // @formatter:off
            "BEGIN:VCARD" + "\r\n" +
            "VERSION:3.0" + "\r\n" +
            "FN:Test From Android" + "\r\n" +
            "N:;Test From Android;;;" + "\r\n" +
            "EMAIL:other2@mail.de" + "\r\n" +
            "EMAIL;TYPE=WORK:work@mail.de" + "\r\n" +
            "EMAIL:other1@mail.de" + "\r\n" +
            "EMAIL;TYPE=HOME:home@mail.de" + "\r\n" +
            "PRODID:-//dmfs.org//mimedir.vcard//EN" + "\r\n" +
            "REV:20190111T150424Z" + "\r\n" +
            "UID:" + uid + "\r\n" +
            "END:VCARD" + "\r\n"
        ; // @formatter:on
        /*
         * create vCard resource on server
         */
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putVCard(uid, vCard));
        /*
         * get & verify created contact on server
         */
        Contact contact = getContact(uid);
        rememberForCleanUp(contact);
        assertEquals("uid wrong", uid, contact.getUid());
        assertEquals("email1 wrong", "work@mail.de", contact.getEmail1());
        assertEquals("email2 wrong", "home@mail.de", contact.getEmail2());
        assertEquals("email3 wrong", "other2@mail.de", contact.getEmail3());
        /*
         * get & verify created vCard from server
         */
        VCardResource vCardResource = getVCard(uid);
        EmailType emailOther2 = null;
        EmailType emailWork = null;
        EmailType emailOther1 = null;
        EmailType emailHome = null;
        List<EmailType> emails = vCardResource.getVCard().getEmails();
        for (EmailType email : emails) {
            if ("other2@mail.de".equals(email.getEmail())) {
                emailOther2 = email;
            } else if ("work@mail.de".equals(email.getEmail())) {
                emailWork = email;
            } else if ("other1@mail.de".equals(email.getEmail())) {
                emailOther1 = email;
            } else if ("home@mail.de".equals(email.getEmail())) {
                emailHome = email;
            } else {
                fail("unexepcted email" + email.getEmail());
            }
        }
        assertNotNull(emailOther2);
        emailOther2.getExtendedParams().contains(new ExtendedParamType("x-other", VCardTypeName.EMAIL));
        assertNotNull(emailWork);
        emailWork.getParams().contains(EmailParamType.WORK);
        assertNotNull(emailHome);
        emailHome.getParams().contains(EmailParamType.HOME);
        assertNotNull(emailOther1);
        emailOther1.getExtendedParams().contains(new ExtendedParamType("x-other", VCardTypeName.EMAIL));
    }

    @Test
    public void testCreateWithFourEmailsTypeInternet() throws Exception {
        /*
         * prepare vCard
         */
        String uid = randomUID();
        String vCard = // @formatter:off
            "BEGIN:VCARD" + "\r\n" +
            "VERSION:3.0" + "\r\n" +
            "FN:Test From Android" + "\r\n" +
            "N:;Test From Android;;;" + "\r\n" +
            "EMAIL;TYPE=INTERNET:other2@mail.de" + "\r\n" +
            "EMAIL;TYPE=WORK;TYPE=INTERNET:work@mail.de" + "\r\n" +
            "EMAIL;TYPE=INTERNET:other1@mail.de" + "\r\n" +
            "EMAIL;TYPE=HOME;TYPE=INTERNET:home@mail.de" + "\r\n" +
            "PRODID:-//dmfs.org//mimedir.vcard//EN" + "\r\n" +
            "REV:20190111T150424Z" + "\r\n" +
            "UID:" + uid + "\r\n" +
            "END:VCARD" + "\r\n"
        ; // @formatter:on
        /*
         * create vCard resource on server
         */
        assertEquals("response code wrong", StatusCodes.SC_CREATED, putVCard(uid, vCard));
        /*
         * get & verify created contact on server
         */
        Contact contact = getContact(uid);
        rememberForCleanUp(contact);
        assertEquals("uid wrong", uid, contact.getUid());
        assertEquals("email1 wrong", "work@mail.de", contact.getEmail1());
        assertEquals("email2 wrong", "home@mail.de", contact.getEmail2());
        assertEquals("email3 wrong", "other2@mail.de", contact.getEmail3());
        /*
         * get & verify created vCard from server
         */
        VCardResource vCardResource = getVCard(uid);
        EmailType emailOther2 = null;
        EmailType emailWork = null;
        EmailType emailOther1 = null;
        EmailType emailHome = null;
        List<EmailType> emails = vCardResource.getVCard().getEmails();
        for (EmailType email : emails) {
            if ("other2@mail.de".equals(email.getEmail())) {
                emailOther2 = email;
            } else if ("work@mail.de".equals(email.getEmail())) {
                emailWork = email;
            } else if ("other1@mail.de".equals(email.getEmail())) {
                emailOther1 = email;
            } else if ("home@mail.de".equals(email.getEmail())) {
                emailHome = email;
            } else {
                fail("unexepcted email" + email.getEmail());
            }
        }
        assertNotNull(emailOther2);
        assertTrue(emailOther2.getParams().contains(EmailParamType.INTERNET));
        emailOther2.getExtendedParams().contains(new ExtendedParamType("x-other", VCardTypeName.EMAIL));

        assertNotNull(emailWork);
        assertTrue(emailWork.getParams().contains(EmailParamType.INTERNET));
        emailWork.getParams().contains(EmailParamType.WORK);

        assertNotNull(emailHome);
        assertTrue(emailHome.getParams().contains(EmailParamType.INTERNET));
        emailHome.getParams().contains(EmailParamType.HOME);

        assertNotNull(emailOther1);
        assertTrue(emailOther1.getParams().contains(EmailParamType.INTERNET));
        emailOther1.getExtendedParams().contains(new ExtendedParamType("x-other", VCardTypeName.EMAIL));
    }

}
