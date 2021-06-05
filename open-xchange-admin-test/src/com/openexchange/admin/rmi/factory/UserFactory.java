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

package com.openexchange.admin.rmi.factory;

import java.util.Calendar;
import java.util.HashSet;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.java.util.TimeZones;

/**
 * {@link UserFactory}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class UserFactory {

    /**
     * Creates the default 'oxadmin' context admin with 'secret' as password
     * 
     * @return The newly created {@link User} object
     */
    public static User createContextAdmin() {
        return createContextAdmin("oxadmin", "secret");
    }

    /**
     * Creates a new {@link Context} admin {@link User} object
     * 
     * @return The newly created {@link User} object
     */
    public static User createContextAdmin(String name, String password) {
        return createUser(name, password, name, name, name, name + "@example.com");
    }

    /**
     * Creates a new user
     */
    public static User createUser(String name, String passwd, String displayName, String givenName, String surname, String email) {
        User user = new User();
        user.setName(name);
        user.setPassword(passwd);
        user.setDisplay_name(displayName);
        user.setGiven_name(givenName);
        user.setSur_name(surname);
        user.setPrimaryEmail(email);
        user.setEmail1(email);
        return user;
    }

    /**
     * Creates a new {@link User} with only the mandatory fields
     * 
     * @param ident The user identity
     * @param password the password
     * @param domain The domain
     * @return The {@link User} object
     */
    public static User createUser(String ident, String password, String domain) {
        User user = createUser(ident, password, "Displayname " + ident, ident, "Lastname " + ident, "primaryemail-" + ident + "@" + domain);
        user.setMailenabled(true);
        return user;
    }

    /**
     * Creates a new user
     */
    public static User createUser(String ident, String password, String domain, Context context) {
        return createUser(ident, password, domain, context, true);
    }

    /**
     * Creates a new {@link User} object with all fields set
     */
    public static User createUser(String ident, String password, String domain, Context context, boolean dedicatedFilestore) {
        User usr = createUser(ident, password, domain);
        usr.setLanguage("de_DE");
        // new for testing

        usr.setEmail1("primaryemail-" + ident + "@" + domain);
        usr.setEmail2("email2-" + ident + "@" + domain);
        usr.setEmail3("email3-" + ident + "@" + domain);

        if (context != null && dedicatedFilestore) {
            usr.setFilestoreId(context.getFilestoreId());
            usr.setFilestore_name(context.getFilestore_name());
        }

        HashSet<String> aliase = new HashSet<String>();
        aliase.add("alias1-" + ident + "@" + domain);
        aliase.add("alias2-" + ident + "@" + domain);
        aliase.add("alias3-" + ident + "@" + domain);
        aliase.add("email2-" + ident + "@" + domain);
        aliase.add("email3-" + ident + "@" + domain);
        aliase.add("primaryemail-" + ident + "@" + domain);
        usr.setAliases(aliase);

        Calendar cal = Calendar.getInstance(TimeZones.UTC);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        usr.setBirthday(cal.getTime());
        usr.setAnniversary(cal.getTime());

        usr.setAssistant_name("assistants name");

        usr.setBranches("Branches");
        usr.setBusiness_category("Business Category");
        usr.setCity_business("Business City");
        usr.setCountry_business("Business Country");
        usr.setPostal_code_business("BusinessPostalCode");
        usr.setState_business("BusinessState");
        usr.setStreet_business("BusinessStreet");
        usr.setTelephone_callback("callback");
        usr.setCity_home("City");
        usr.setCommercial_register("CommercialRegister");
        usr.setCompany("Company");
        usr.setCountry_home("Country");
        usr.setDepartment("Department");
        usr.setEmployeeType("EmployeeType");
        usr.setFax_business("FaxBusiness");
        usr.setFax_home("FaxHome");
        usr.setFax_other("FaxOther");
        usr.setImapServer("imap://localhost:143");
        usr.setInstant_messenger1("InstantMessenger");
        usr.setInstant_messenger2("InstantMessenger2");
        usr.setTelephone_ip("IpPhone");
        usr.setTelephone_isdn("Isdn");
        usr.setMail_folder_drafts_name("MailFolderDrafts");
        usr.setMail_folder_sent_name("MailFolderSent");
        usr.setMail_folder_spam_name("MailFolderSpam");
        usr.setMail_folder_trash_name("MailFolderTrash");
        usr.setMail_folder_archive_full_name("MailFolderArchive");
        usr.setManager_name("ManagersName");
        usr.setMarital_status("MaritalStatus");
        usr.setCellular_telephone1("Mobile1");
        usr.setCellular_telephone2("Mobile2");
        usr.setInfo("MoreInfo");
        usr.setNickname("NickName");
        usr.setNote("Note");
        usr.setNumber_of_children("NumberOfChildren");
        usr.setNumber_of_employee("NumberOfEmployee");
        usr.setTelephone_pager("Pager");
        usr.setPassword_expired(false);
        usr.setTelephone_assistant("PhoneAssistant");
        usr.setTelephone_business1("PhoneBusiness");
        usr.setTelephone_business2("PhoneBusiness2");
        usr.setTelephone_car("PhoneCar");
        usr.setTelephone_company("PhoneCompany");
        usr.setTelephone_home1("PhoneHome");
        usr.setTelephone_home2("PhoneHome2");
        usr.setTelephone_other("PhoneOther");
        usr.setPosition("Position");
        usr.setPostal_code_home("PostalCode");
        usr.setEmail2("Privateemail2-" + ident + "@" + domain);
        usr.setEmail3("Privateemail3-" + ident + "@" + domain);
        usr.setProfession("Profession");
        usr.setTelephone_radio("Radio");
        usr.setRoom_number("1337");
        usr.setSales_volume("SalesVolume");
        usr.setCity_other("SecondCity");
        usr.setCountry_other("SecondCountry");
        usr.setMiddle_name("SecondName");
        usr.setPostal_code_other("SecondPostalCode");
        usr.setState_other("SecondState");
        usr.setStreet_other("SecondStreet");
        usr.setSmtpServer("smtp://localhost:25");
        usr.setSpouse_name("SpouseName");
        usr.setState_home("State");
        usr.setStreet_home("Street");
        usr.setSuffix("Suffix");
        usr.setTax_id("TaxId");
        usr.setTelephone_telex("Telex");
        usr.setTimezone("Europe/Berlin");
        usr.setTitle("Title");
        usr.setTelephone_ttytdd("TtyTdd");
        usr.setUrl("url");
        usr.setUserfield01("Userfield01");
        usr.setUserfield02("Userfield02");
        usr.setUserfield03("Userfield03");
        usr.setUserfield04("Userfield04");
        usr.setUserfield05("Userfield05");
        usr.setUserfield06("Userfield06");
        usr.setUserfield07("Userfield07");
        usr.setUserfield08("Userfield08");
        usr.setUserfield09("Userfield09");
        usr.setUserfield10("Userfield10");
        usr.setUserfield11("Userfield11");
        usr.setUserfield12("Userfield12");
        usr.setUserfield13("Userfield13");
        usr.setUserfield14("Userfield14");
        usr.setUserfield15("Userfield15");
        usr.setUserfield16("Userfield16");
        usr.setUserfield17("Userfield17");
        usr.setUserfield18("Userfield18");
        usr.setUserfield19("Userfield19");
        usr.setUserfield20("Userfield20");

        usr.setUserAttribute("com.openexchange.test", "simpleValue", "12");
        usr.setUserAttribute("com.openexchange.test", "staticValue", "42");
        usr.setUserAttribute("com.openexchange.test", "deleteMe", "23");

        return usr;
    }
}
