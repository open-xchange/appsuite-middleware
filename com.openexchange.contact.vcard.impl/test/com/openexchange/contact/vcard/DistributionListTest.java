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

package com.openexchange.contact.vcard;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import ezvcard.VCard;
import ezvcard.property.Email;
import ezvcard.property.Member;

/**
 * {@link DistributionListTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DistributionListTest extends VCardTest {

    /**
     * Initializes a new {@link DistributionListTest}.
     */
    public DistributionListTest() {
        super();
    }

    @Test
    public void testExportDistributionList() throws OXException {
        /*
         * create test contact
         */
        Contact contact = new Contact();
        contact.setDisplayName("Liste");
        List<DistributionListEntryObject> distributionList = new ArrayList<>();
        distributionList.add(new DistributionListEntryObject("Otto", "otto@example.com", DistributionListEntryObject.INDEPENDENT));
        distributionList.add(new DistributionListEntryObject("Horst", "horst@example.com", DistributionListEntryObject.INDEPENDENT));
        DistributionListEntryObject herbert = new DistributionListEntryObject("Herbert", "herbert@example.com", DistributionListEntryObject.INDEPENDENT);
        herbert.setFolderID(3242);
        herbert.setEntryID(253463);
        herbert.setEmailfield(DistributionListEntryObject.EMAILFIELD1);
        distributionList.add(herbert);
        distributionList.add(new DistributionListEntryObject("Peter", "peter@example.com", DistributionListEntryObject.INDEPENDENT));
        distributionList.add(new DistributionListEntryObject("Klaus", "klaus@example.com", DistributionListEntryObject.INDEPENDENT));
        contact.setDistributionList(distributionList.toArray(new DistributionListEntryObject[distributionList.size()]));
        /*
         * export to new vCard
         */
        VCard vCard = getMapper().exportContact(contact, null, null, null);
        /*
         * verify vCard
         */
        assertNotNull("no vCard exported", vCard);
        assertNotNull("no KIND exported", vCard.getKind());
        assertTrue(vCard.getKind().isGroup());
        List<Member> members = vCard.getMembers();
        assertTrue("no MEMBERs exported", null != members && 0 < members.size());
        Assert.assertEquals(distributionList.size(), members.size());
        for (DistributionListEntryObject entry : distributionList) {
            Member matchingMember = null;
            for (Member member : members) {
                if (("mailto:" + entry.getEmailaddress()).equals(member.getValue())) {
                    matchingMember = member;
                    break;
                }
            }
            assertNotNull(entry.getEmailaddress(), matchingMember);
        }
    }

    @Test
    public void testImportDistributionList() {
        /*
         * import vCard
         */
        String vCardString =
            "BEGIN:VCARD\r\n" +
            "VERSION:3.0\r\n" +
            "FN:Liste\r\n" +
            "KIND:group\r\n" +
            "MEMBER;X-OX-FN=Otto:mailto:otto@example.com\r\n" +
            "MEMBER;X-OX-FN=Horst:mailto:horst@example.com\r\n" +
            "MEMBER;X-OX-FN=Herbert:mailto:herbert@example.com\r\n" +
            "MEMBER;X-OX-FN=Peter:mailto:peter@example.com\r\n" +
            "MEMBER;X-OX-FN=Klaus:mailto:klaus@example.com\r\n" +
            "PRODID:-//Open-Xchange//<unknown version>//EN\r\n" +
            "END:VCARD\r\n"
        ;
        Contact contact = getMapper().importVCard(parse(vCardString), null, null, null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        Assert.assertEquals("Liste", contact.getDisplayName());
        assertTrue(contact.getMarkAsDistribtuionlist());
        DistributionListEntryObject[] distributionList = contact.getDistributionList();
        assertNotNull(distributionList);
        Assert.assertEquals(5, distributionList.length);
        for (String member : new String[] { "otto@example.com", "horst@example.com", "herbert@example.com", "peter@example.com", "klaus@example.com" }) {
            DistributionListEntryObject matchingEntry = null;
            for (DistributionListEntryObject entry : distributionList) {
                if (member.equals(entry.getEmailaddress())) {
                    matchingEntry = entry;
                }
            }
            assertNotNull(matchingEntry);
        }
    }

    @Test
    public void testImportOldDistributionList() {
        /*
         * import vCard
         */
        String vCardString =
            "BEGIN:VCARD\r\n" +
            "VERSION:3.0\r\n" +
            "PRODID:OPEN-XCHANGE\r\n" +
            "FN:Liste\r\n" +
            "N:;;;;\r\n" +
            "X-OPEN-XCHANGE-CTYPE:dlist\r\n" +
            "EMAIL;TYPE=INTERNET:otto@example.com\r\n" +
            "EMAIL;TYPE=INTERNET:horst@example.com\r\n" +
            "EMAIL;TYPE=INTERNET:herbert@example.com\r\n" +
            "EMAIL;TYPE=INTERNET:peter@example.com\r\n" +
            "EMAIL;TYPE=INTERNET:klaus@example.com\r\n" +
            "END:VCARD"
        ;
        Contact contact = getMapper().importVCard(parse(vCardString), null, null, null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        Assert.assertEquals("Liste", contact.getDisplayName());
        assertTrue(contact.getMarkAsDistribtuionlist());
        DistributionListEntryObject[] distributionList = contact.getDistributionList();
        assertNotNull(distributionList);
        Assert.assertEquals(5, distributionList.length);
        for (String member : new String[] { "otto@example.com", "horst@example.com", "herbert@example.com", "peter@example.com", "klaus@example.com" }) {
            DistributionListEntryObject matchingEntry = null;
            for (DistributionListEntryObject entry : distributionList) {
                if (member.equals(entry.getEmailaddress())) {
                    matchingEntry = entry;
                }
            }
            assertNotNull(matchingEntry);
        }
    }

    @Test
    public void testMergeWithOldDistributionList() {
        /*
         * import original vCard
         */
        String vCardString =
            "BEGIN:VCARD\r\n" +
            "VERSION:3.0\r\n" +
            "PRODID:OPEN-XCHANGE\r\n" +
            "FN:Liste\r\n" +
            "N:;;;;\r\n" +
            "X-OPEN-XCHANGE-CTYPE:dlist\r\n" +
            "EMAIL;TYPE=INTERNET:otto@example.com\r\n" +
            "EMAIL;TYPE=INTERNET:horst@example.com\r\n" +
            "EMAIL;TYPE=INTERNET:herbert@example.com\r\n" +
            "EMAIL;TYPE=INTERNET:peter@example.com\r\n" +
            "EMAIL;TYPE=INTERNET:klaus@example.com\r\n" +
            "END:VCARD"
        ;
        VCard orignalVCard = parse(vCardString);
        Contact contact = getMapper().importVCard(orignalVCard, null, null, null);
        /*
         * export vCard again & verify written properties
         */
        VCard vCard = getMapper().exportContact(contact, orignalVCard, null, null);
        orignalVCard = parse(vCardString);
        assertNotNull("no vCard exported", vCard);
        assertNull(vCard.getExtendedProperty("X-OPEN-XCHANGE-CTYPE"));
        assertTrue(null == vCard.getEmails() || 0 == vCard.getEmails().size());
        assertNotNull("no KIND exported", vCard.getKind());
        assertTrue(vCard.getKind().isGroup());
        List<Member> members = vCard.getMembers();
        assertTrue("no MEMBERs exported", null != members && 0 < members.size());
        Assert.assertEquals(orignalVCard.getEmails().size(), members.size());
        for (Email email : orignalVCard.getEmails()) {
            Member matchingMember = null;
            for (Member member : members) {
                if (("mailto:" + email.getValue()).equals(member.getValue())) {
                    matchingMember = member;
                    break;
                }
            }
            assertNotNull(email.getValue(), matchingMember);
        }
    }

}
