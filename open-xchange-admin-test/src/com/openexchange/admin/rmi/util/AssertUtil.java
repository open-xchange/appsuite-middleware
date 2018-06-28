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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.admin.rmi.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.extensions.OXCommonExtensionInterface;
import com.openexchange.java.util.TimeZones;

/**
 * {@link AssertUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class AssertUtil {

    private static final Logger LOG = LoggerFactory.getLogger(AssertUtil.class);

    /**
     * Asserts that the <code>expected</code> user is equal to the <code>actual</code>.
     * Only the mandatory fields are asserted.
     * 
     * @param expected The expected {@link User}
     * @param actual The actual {@link User}
     */
    public static void assertUserEquals(User expected, User actual) {
        assertEquals("Name should match", expected.getName(), actual.getName());
        assertEquals("Display name should match", expected.getDisplay_name(), actual.getDisplay_name());
        assertEquals("Given name should match", expected.getGiven_name(), actual.getGiven_name());
        assertEquals("Surname should match", expected.getSur_name(), actual.getSur_name());
        assertEquals("Primary E-Mail should match", expected.getPrimaryEmail(), actual.getPrimaryEmail());
        assertEquals("E-Mail #1 should match", expected.getEmail1(), actual.getEmail1());
    }

    /**
     * Compares the {@link User} A with {@link User} B
     * 
     * @param a The {@link User} A
     * @param b The {@link User} B
     */
    public static void assertUser(User a, User b) {
        LOG.info("USER A: {}", a.toString());
        LOG.info("USER B: {}", b.toString());

        assertEquals("username not equal", a.getName(), b.getName());
        assertEquals("enabled not equal", a.getMailenabled(), b.getMailenabled());
        assertEquals("primaryemail not equal", a.getPrimaryEmail(), b.getPrimaryEmail());
        assertEquals("display name not equal", a.getDisplay_name(), b.getDisplay_name());
        assertEquals("firtname not equal", a.getGiven_name(), b.getGiven_name());
        assertEquals("lastname not equal", a.getSur_name(), b.getSur_name());
        assertEquals("language not equal", a.getLanguage(), b.getLanguage());
        // test aliasing comparing the content of the hashset
        assertEquals(a.getAliases(), b.getAliases());
        AssertUtil.compareNonCriticFields(a, b);
    }

    /**
     * Asserts that the groups are equal
     * 
     * @param expected The expected {@link Group}
     * @param actual The actual {@link Group}
     */
    public static void assertGroupEquals(Group expected, Group actual) {
        assertEquals("Display name should match", expected.getDisplayname(), actual.getDisplayname());
        assertEquals("Name should match", expected.getName(), actual.getName());
    }

    /**
     * Asserts that the expected {@link Resource} is equal the actual {@link Resource}
     * 
     * @param expected The expected {@link Resource}
     * @param actual The actual {@link Resource}
     */
    public static void assertResourceEquals(Resource expected, Resource actual) {
        assertEquals("Display name should match", expected.getDisplayname(), actual.getDisplayname());
        assertEquals("Name should match", expected.getName(), actual.getName());
        assertEquals("E-Mail should match", expected.getEmail(), actual.getEmail());
    }

    /**
     * Compares two user arrays by retrieving all the IDs they contain
     * an checking if they match. Ignores duplicate entries, ignores
     * users without an ID at all.
     * 
     * @param arr1 the first array
     * @param arr2 the second array
     */
    public static void assertIDsAreEqual(User[] arr1, User[] arr2) {
        Set<Integer> set1 = new HashSet<Integer>();
        for (User element : arr1) {
            set1.add(element.getId());
        }
        Set<Integer> set2 = new HashSet<Integer>();
        for (int i = 0; i < arr1.length; i++) {
            set2.add(arr2[i].getId());
        }

        assertEquals("Both arrays should return the same IDs", set1, set2);
    }

    /**
     * Asserts that both {@link UserModuleAccess} objects are equal
     * 
     * @param a The {@link UserModuleAccess} a
     * @param b The {@link UserModuleAccess} b
     */
    public static void compareUserAccess(UserModuleAccess a, UserModuleAccess b) {
        assertEquals("access calendar not equal", a.getCalendar(), b.getCalendar());
        assertEquals("access contacts not equal", a.getContacts(), b.getContacts());
        assertEquals("access delegatetasks not equal", a.getDelegateTask(), b.getDelegateTask());
        assertEquals("access edit public folders not equal", a.getEditPublicFolders(), b.getEditPublicFolders());
        assertEquals("access ical not equal", a.getIcal(), b.getIcal());
        assertEquals("access infostore not equal", a.getInfostore(), b.getInfostore());
        assertEquals("access ReadCreateSharedFolders not equal", a.getReadCreateSharedFolders(), b.getReadCreateSharedFolders());
        assertEquals("access syncml not equal", a.getSyncml(), b.getSyncml());
        assertEquals("access tasks not equal", a.getTasks(), b.getTasks());
        assertEquals("access vcard not equal", a.getVcard(), b.getVcard());
        assertEquals("access webdav not equal", a.getWebdav(), b.getWebdav());
        assertEquals("access webdav xml not equal", a.getWebdavXml(), b.getWebdavXml());
        assertEquals("access webmail not equal", a.getWebmail(), b.getWebmail());
    }

    /**
     * Asserts that the {@link User} <code>b</code> does not have any of
     * its mandatory fields set to <code>null</code> and then asserts
     * that is equal to {@link User} <code>a</code>
     * 
     * @param a The {@link User} a
     * @param b The {@link User} b
     */
    public static void compareUserSpecialForNulledAttributes(User a, User b) {
        LOG.info("USER A: {}", a.toString());
        LOG.info("USER B: {}", b.toString());

        // all these attributes cannot be null | cannot changed by server to null/empty
        assertNotNull("username cannot be null", b.getName());
        assertNotNull("enabled cannot be null", b.getMailenabled());
        assertNotNull("primaryemail cannot be null", b.getPrimaryEmail());
        assertNotNull("display name cannot be null", b.getDisplay_name());
        assertNotNull("firstname name cannot be null", b.getGiven_name());
        assertNotNull("lastname name cannot be null", b.getSur_name());
        assertNotNull("language name cannot be null", b.getLanguage());

        // can alias be null?
        //assertEquals(a.getAliases(), b.getAliases());
        AssertUtil.compareNonCriticFields(a, b);
    }

    private static void compareNonCriticFields(User a, User b) {
        assertDatesAreEqualsAtYMD("aniversary not equal", a.getAnniversary(), b.getAnniversary());
        assertEquals("assistants name not equal", a.getAssistant_name(), b.getAssistant_name());
        assertDatesAreEqualsAtYMD("birthday not equal", a.getBirthday(), b.getBirthday());
        assertEquals("branches not equal", a.getBranches(), b.getBranches());
        assertEquals("BusinessCategory not equal", a.getBusiness_category(), b.getBusiness_category());
        assertEquals("BusinessCity not equal", a.getCity_business(), b.getCity_business());
        assertEquals("BusinessCountry not equal", a.getCountry_business(), b.getCountry_business());
        assertEquals("BusinessPostalCode not equal", a.getPostal_code_business(), b.getPostal_code_business());
        assertEquals("BusinessState not equal", a.getState_business(), b.getState_business());
        assertEquals("BusinessStreet not equal", a.getStreet_business(), b.getStreet_business());
        assertEquals("callback not equal", a.getTelephone_callback(), b.getTelephone_callback());
        assertEquals("CommercialRegister not equal", a.getCommercial_register(), b.getCommercial_register());
        assertEquals("Company not equal", a.getCompany(), b.getCompany());
        assertEquals("Country not equal", a.getCountry_home(), b.getCountry_home());
        assertEquals("Department not equal", a.getDepartment(), b.getDepartment());
        assertEquals("EmployeeType not equal", a.getEmployeeType(), b.getEmployeeType());
        assertEquals("FaxBusiness not equal", a.getFax_business(), b.getFax_business());
        assertEquals("FaxHome not equal", a.getFax_home(), b.getFax_home());
        assertEquals("FaxOther not equal", a.getFax_other(), b.getFax_other());
        assertEquals("ImapServer not equal", a.getImapServerString(), b.getImapServerString());
        assertEquals("InstantMessenger not equal", a.getInstant_messenger1(), b.getInstant_messenger1());
        assertEquals("InstantMessenger2 not equal", a.getInstant_messenger2(), b.getInstant_messenger2());
        assertEquals("IpPhone not equal", a.getTelephone_ip(), b.getTelephone_ip());
        assertEquals("Isdn not equal", a.getTelephone_isdn(), b.getTelephone_isdn());
        assertEquals("MailFolderDrafts not equal", a.getMail_folder_drafts_name(), b.getMail_folder_drafts_name());
        assertEquals("MailFolderSent not equal", a.getMail_folder_sent_name(), b.getMail_folder_sent_name());
        assertEquals("MailFolderSpam not equal", a.getMail_folder_spam_name(), b.getMail_folder_spam_name());
        assertEquals("MailFolderTrash not equal", a.getMail_folder_trash_name(), b.getMail_folder_trash_name());
        assertEquals("MailFolderArchiveFull not equal", a.getMail_folder_archive_full_name(), b.getMail_folder_archive_full_name());
        assertEquals("ManagersName not equal", a.getManager_name(), b.getManager_name());
        assertEquals("MaritalStatus not equal", a.getMarital_status(), b.getMarital_status());
        assertEquals("Mobile1 not equal", a.getCellular_telephone1(), b.getCellular_telephone1());
        assertEquals("Mobile2 not equal", a.getCellular_telephone2(), b.getCellular_telephone2());
        assertEquals("MoreInfo not equal", a.getInfo(), b.getInfo());
        assertEquals("NickName not equal", a.getNickname(), b.getNickname());
        assertEquals("Note not equal", a.getNote(), b.getNote());
        assertEquals("NumberOfChildren not equal", a.getNumber_of_children(), b.getNumber_of_children());
        assertEquals("NumberOfEmployee not equal", a.getNumber_of_employee(), b.getNumber_of_employee());
        assertEquals("Pager not equal", a.getTelephone_pager(), b.getTelephone_pager());
        assertEquals("PasswordExpired not equal", a.getPassword_expired(), b.getPassword_expired());
        assertEquals("PhoneAssistant not equal", a.getTelephone_assistant(), b.getTelephone_assistant());
        assertEquals("PhoneBusiness not equal", a.getTelephone_business1(), b.getTelephone_business1());
        assertEquals("PhoneBusiness2 not equal", a.getTelephone_business2(), b.getTelephone_business2());
        assertEquals("PhoneCar not equal", a.getTelephone_car(), b.getTelephone_car());
        assertEquals("PhoneCompany not equal", a.getTelephone_company(), b.getTelephone_company());
        assertEquals("PhoneHome not equal", a.getTelephone_home1(), b.getTelephone_home1());
        assertEquals("PhoneHome2 not equal", a.getTelephone_home2(), b.getTelephone_home2());
        assertEquals("PhoneOther not equal", a.getTelephone_other(), b.getTelephone_other());
        assertEquals("Position not equal", a.getPosition(), b.getPosition());
        assertEquals("PostalCode not equal", a.getPostal_code_home(), b.getPostal_code_home());
        assertEquals("Email2 not equal", a.getEmail2(), b.getEmail2());
        assertEquals("Email3 not equal", a.getEmail3(), b.getEmail3());
        assertEquals("Profession not equal", a.getProfession(), b.getProfession());
        assertEquals("Radio not equal", a.getTelephone_radio(), b.getTelephone_radio());
        assertEquals("RoomNumber not equal", a.getRoom_number(), b.getRoom_number());
        assertEquals("SalesVolume not equal", a.getSales_volume(), b.getSales_volume());
        assertEquals("SecondCity not equal", a.getCity_other(), b.getCity_other());
        assertEquals("SecondCountry not equal", a.getCountry_other(), b.getCountry_other());
        assertEquals("SecondName not equal", a.getMiddle_name(), b.getMiddle_name());
        assertEquals("SecondPostalCode not equal", a.getPostal_code_other(), b.getPostal_code_other());
        assertEquals("SecondState not equal", a.getState_other(), b.getState_other());
        assertEquals("SecondStreet not equal", a.getStreet_other(), b.getStreet_other());
        assertEquals("SmtpServer not equal", a.getSmtpServerString(), b.getSmtpServerString());
        assertEquals("SpouseName not equal", a.getSpouse_name(), b.getSpouse_name());
        assertEquals("State not equal", a.getState_home(), b.getState_home());
        assertEquals("Street not equal", a.getStreet_home(), b.getStreet_home());
        assertEquals("Suffix not equal", a.getSuffix(), b.getSuffix());
        assertEquals("TaxId not equal", a.getTax_id(), b.getTax_id());
        assertEquals("Telex not equal", a.getTelephone_telex(), b.getTelephone_telex());
        assertEquals("Timezone not equal", a.getTimezone(), b.getTimezone());
        assertEquals("Title not equal", a.getTitle(), b.getTitle());
        assertEquals("TtyTdd not equal", a.getTelephone_ttytdd(), b.getTelephone_ttytdd());
        assertEquals("Url not equal", a.getUrl(), b.getUrl());
        assertEquals("Userfield01 not equal", a.getUserfield01(), b.getUserfield01());
        assertEquals("Userfield02 not equal", a.getUserfield02(), b.getUserfield02());
        assertEquals("Userfield03 not equal", a.getUserfield03(), b.getUserfield03());
        assertEquals("Userfield04 not equal", a.getUserfield04(), b.getUserfield04());
        assertEquals("Userfield05 not equal", a.getUserfield05(), b.getUserfield05());
        assertEquals("Userfield06 not equal", a.getUserfield06(), b.getUserfield06());
        assertEquals("Userfield07 not equal", a.getUserfield07(), b.getUserfield07());
        assertEquals("Userfield08 not equal", a.getUserfield08(), b.getUserfield08());
        assertEquals("Userfield09 not equal", a.getUserfield09(), b.getUserfield09());
        assertEquals("Userfield10 not equal", a.getUserfield10(), b.getUserfield10());
        assertEquals("Userfield11 not equal", a.getUserfield11(), b.getUserfield11());
        assertEquals("Userfield12 not equal", a.getUserfield12(), b.getUserfield12());
        assertEquals("Userfield13 not equal", a.getUserfield13(), b.getUserfield13());
        assertEquals("Userfield14 not equal", a.getUserfield14(), b.getUserfield14());
        assertEquals("Userfield15 not equal", a.getUserfield15(), b.getUserfield15());
        assertEquals("Userfield16 not equal", a.getUserfield16(), b.getUserfield16());
        assertEquals("Userfield17 not equal", a.getUserfield17(), b.getUserfield17());
        assertEquals("Userfield18 not equal", a.getUserfield18(), b.getUserfield18());
        assertEquals("Userfield19 not equal", a.getUserfield19(), b.getUserfield19());
        assertEquals("Userfield20 not equal", a.getUserfield20(), b.getUserfield20());
        Hashtable<String, OXCommonExtensionInterface> aexts = a.getAllExtensionsAsHash();
        Hashtable<String, OXCommonExtensionInterface> bexts = b.getAllExtensionsAsHash();
        if (aexts.size() == bexts.size()) {
            assertTrue("Extensions not equal: " + aexts.toString() + ",\n" + bexts.toString(), aexts.values().containsAll(bexts.values()));
            for (int i = 0; i < aexts.size(); i++) {
                OXCommonExtensionInterface aext = aexts.get(i);
                OXCommonExtensionInterface bext = bexts.get(i);
                assertTrue("Extensions not equal: " + aext.toString() + ",\n" + bext.toString(), aext.equals(bext));
            }
        }

        assertEquals("User Attributes not equal", a.getUserAttributes(), b.getUserAttributes());
    }

    private static void assertDatesAreEqualsAtYMD(String message, Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance(TimeZones.UTC);
        Calendar cal2 = Calendar.getInstance(TimeZones.UTC);
        if (date1 != null && date2 != null) {
            cal1.setTime(date1);
            cal2.setTime(date2);
            assertEquals(message, cal1.get(Calendar.YEAR), cal2.get(Calendar.YEAR));
            assertEquals(message, cal1.get(Calendar.MONTH), cal2.get(Calendar.MONTH));
            assertEquals(message, cal1.get(Calendar.DAY_OF_MONTH), cal2.get(Calendar.DAY_OF_MONTH));
        }
    }
}
