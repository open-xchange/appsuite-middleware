/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.contact.vcard;

import java.util.ArrayList;
import java.util.List;
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

    public void testExportDistributionList() throws OXException {
        /*
         * create test contact
         */
        Contact contact = new Contact();
        contact.setDisplayName("Liste");
        List<DistributionListEntryObject> distributionList = new ArrayList<DistributionListEntryObject>();
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
        assertEquals(distributionList.size(), members.size());
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

    public void testImportDistributionList() throws OXException {
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
        assertEquals("Liste", contact.getDisplayName());
        assertTrue(contact.getMarkAsDistribtuionlist());
        DistributionListEntryObject[] distributionList = contact.getDistributionList();
        assertNotNull(distributionList);
        assertEquals(5, distributionList.length);
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

    public void testImportOldDistributionList() throws OXException {
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
        assertEquals("Liste", contact.getDisplayName());
        assertTrue(contact.getMarkAsDistribtuionlist());
        DistributionListEntryObject[] distributionList = contact.getDistributionList();
        assertNotNull(distributionList);
        assertEquals(5, distributionList.length);
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

    public void testMergeWithOldDistributionList() throws OXException {
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
        assertEquals(orignalVCard.getEmails().size(), members.size());
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
