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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
package com.openexchange.admin.rmi;

import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

import java.rmi.Naming;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 
 * @author cutmasta
 * @author d7
 */
public class UserTest extends AbstractTest {

    // list of chars that must be valid
    private static final String VALID_CHAR_TESTUSER = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-+.%$@";

    // global setting for stored password
    private static final String pass = "foo-user-pass";

    private static OXUserInterface getUserClient() throws Exception{
        return (OXUserInterface) Naming.lookup(getRMIHostUrl()+ OXUserInterface.RMI_NAME);
    }
    
    private Context getTestContext() throws Exception{
        return new Context(1);
    }
    
    public void testCreate() throws Exception {        
        
        // get context to create an user
        Context ctx = getTestContext();
        
        // create new user
        OXUserInterface oxu = getUserClient();
        final Credentials cred = DummyCredentials();
        final UserModuleAccess access = new UserModuleAccess();    
        final User urs = getTestUserObject(VALID_CHAR_TESTUSER+System.currentTimeMillis(), pass);
        urs.setId(oxu.create(ctx,urs,access,cred));        
        int[] id = {urs.getId()};
        
        // now load user from server and check if data is correct, else fail
        User[] srv_response = oxu.getData(ctx,id,cred);
        User srv_loaded = srv_response[0];
        if(urs.getId().equals(srv_loaded.getId())){
            //verify data
            compareUser(urs,srv_loaded);
        }else{
            fail("Expected to get user data for added user");
        }
    }

    public void testDelete() throws Exception {
        
        // get context to create an user
        Context ctx = getTestContext();
        
        // create new user
        OXUserInterface oxu = getUserClient();
        final UserModuleAccess access = new UserModuleAccess();
        final Credentials cred = DummyCredentials();
        final User urs = getTestUserObject(VALID_CHAR_TESTUSER+System.currentTimeMillis(), pass);
        urs.setId(oxu.create(ctx,urs,access,cred));             
        
        // delete user
        oxu.delete(ctx,new int[]{urs.getId()},cred);
        
        // try to load user, this MUST fail       
        try {
            oxu.getData(ctx,new int[]{urs.getId()},cred);
            fail("user not exists expected");
        } catch (final InvalidDataException ecp) {            
            // this exception MUST happen, if not, test MUST fail :)
            assertTrue(true);
        }
    }

    public void testGetData() throws Exception {
        
        // get context to create an user
        Context ctx = getTestContext();
        
        // create new user
        OXUserInterface oxu = getUserClient();
        final UserModuleAccess access = new UserModuleAccess();   
        final Credentials cred = DummyCredentials();
        final User urs = getTestUserObject(VALID_CHAR_TESTUSER+System.currentTimeMillis(), pass);
        urs.setId(oxu.create(ctx,urs,access,cred));     
        
        // now load user from server and check if data is correct, else fail
        User[] srv_response = oxu.getData(ctx,new int[]{urs.getId()},cred);
        User srv_loaded = srv_response[0];
        if(urs.getId().equals(srv_loaded.getId())){
            //verify data
            compareUser(urs,srv_loaded);
        }else{
            fail("Expected to get user data");
        }       
    }

    public void testGetDataByUsername() throws Exception {
        // get context to create an user
        Context ctx = getTestContext();
        
        // create new user
        OXUserInterface oxu = getUserClient();
        final UserModuleAccess access = new UserModuleAccess();
        final Credentials cred = DummyCredentials();
        final User urs = getTestUserObject(VALID_CHAR_TESTUSER+System.currentTimeMillis(), pass);
        urs.setId(oxu.create(ctx,urs,access,cred));     
        
        // now load user from server and check if data is correct, else fail
        User[] srv_response = oxu.getData(ctx,new User[]{urs},cred);
        User srv_loaded = srv_response[0];
        if(urs.getId().equals(srv_loaded.getId())){
            //verify data
            compareUser(urs,srv_loaded);
        }else{
            fail("Expected to get user data");
        }    
    }

    public void testGetModuleAccess() throws Exception {        
        
        // get context to create an user
        Context ctx = getTestContext();
        
        // create new user
        OXUserInterface oxu = getUserClient();
        final UserModuleAccess client_access = new UserModuleAccess(); 
        final Credentials cred = DummyCredentials();
        final User urs = getTestUserObject(VALID_CHAR_TESTUSER+System.currentTimeMillis(), pass);
        urs.setId(oxu.create(ctx,urs,client_access,cred));
        
        // get module access 
        UserModuleAccess srv_response = oxu.getModuleAccess(ctx,urs.getId(),cred);
        
        // test if module access was set correctly
        compareUserAccess(client_access,srv_response);        
        
    }

    public void testChangeModuleAccess() throws Exception {
        
        // get context to create an user
        Context ctx = getTestContext();
        
        // create new user
        OXUserInterface oxu = getUserClient();
        final UserModuleAccess client_access = new UserModuleAccess();
        final Credentials cred = DummyCredentials();
        final User urs = getTestUserObject(VALID_CHAR_TESTUSER+System.currentTimeMillis(), pass);
        urs.setId(oxu.create(ctx,urs,client_access,cred));
        
        // get module access 
        UserModuleAccess srv_response = oxu.getModuleAccess(ctx,urs.getId(),cred);
        
        // test if module access was set correctly
        compareUserAccess(client_access,srv_response);  
        
        // now change server loaded module access and submit changes to the server
        srv_response.setCalendar(!srv_response.getCalendar());
        srv_response.setContacts(!srv_response.getContacts());
        srv_response.setDelegateTask(!srv_response.getDelegateTask());
        srv_response.setEditPublicFolders(!srv_response.getEditPublicFolders());
        srv_response.setForum(!srv_response.getForum());
        srv_response.setIcal(!srv_response.getIcal());
        srv_response.setInfostore(!srv_response.getInfostore());
        srv_response.setPinboardWrite(!srv_response.getPinboardWrite());
        srv_response.setProjects(!srv_response.getProjects());
        srv_response.setReadCreateSharedFolders(!srv_response.getReadCreateSharedFolders());
        srv_response.setRssBookmarks(!srv_response.getRssBookmarks());
        srv_response.setRssPortal(!srv_response.getRssPortal());
        srv_response.setSyncml(!srv_response.getSyncml());
        srv_response.setTasks(!srv_response.getTasks());
        srv_response.setVcard(!srv_response.getVcard());
        srv_response.setWebdav(!srv_response.getWebdav());
        srv_response.setWebdavXml(!srv_response.getWebdavXml());
        srv_response.setWebmail(!srv_response.getWebmail());        
        
        // submit changes
        oxu.changeModuleAccess(ctx,urs.getId(),srv_response,cred);        
        
        // load again and verify
        UserModuleAccess srv_response_changed = oxu.getModuleAccess(ctx,urs.getId(),cred);
        
        // test if module access was set correctly
        compareUserAccess(srv_response,srv_response_changed);          
        
    }

    public void testGetAll() throws Exception {
        
        // get context to create an user
        Context ctx = getTestContext();
        
        // create new user
        OXUserInterface oxu = getUserClient();
        final UserModuleAccess client_access = new UserModuleAccess();
        final Credentials cred = DummyCredentials();
        final User urs = getTestUserObject(VALID_CHAR_TESTUSER+System.currentTimeMillis(), pass);
        urs.setId(oxu.create(ctx,urs,client_access,cred));
        
        int[] srv_response = oxu.getAll(ctx,cred);
        
        assertTrue("Expected list size > 0 ",srv_response.length>0);
        
        boolean founduser = false;
        for(int element: srv_response){
            if(element==urs.getId()){
                founduser = true;
            }
        }
       
        assertTrue("Expected to find added user in user list",founduser);
    }

    public void testChange() throws Exception {
        
        // get context to create an user
        Context ctx = getTestContext();
        
        // create new user
        OXUserInterface oxu = getUserClient();
        final Credentials cred = DummyCredentials();
        final UserModuleAccess access = new UserModuleAccess();    
        final User urs = getTestUserObject(VALID_CHAR_TESTUSER+System.currentTimeMillis(), pass);
        urs.setId(oxu.create(ctx,urs,access,cred));     
        
        // now load user from server and check if data is correct, else fail
        User[] srv_response = oxu.getData(ctx,new int[]{urs.getId()},cred);
        User srv_loaded = srv_response[0];
        if(urs.getId().equals(srv_loaded.getId())){
            //verify data
            compareUser(urs,srv_loaded);
        }else{
            fail("Expected to get user data");
        } 
        
        
        // now change data
        createChangeUserData(srv_loaded);
        
        // submit changes
        oxu.change(ctx,srv_loaded,cred);
        
        // load again
        srv_response = oxu.getData(ctx,new int[]{srv_loaded.getId()},cred);
        User user_changed_loaded = srv_response[0];
        if(srv_loaded.getId().equals(user_changed_loaded.getId())){
            //verify data
            compareUser(srv_loaded,user_changed_loaded);
        }else{
            fail("Expected to get correct changed user data");
        } 
    }

    
   

    public static User getTestUserObject(final String ident, final String password) {
        final User usr = new User();
        usr.setUsername(ident);
        usr.setPassword(password);
        usr.setEnabled(true);
        usr.setPrimaryEmail("primaryemail-" + ident + "@" + AbstractTest.TEST_DOMAIN);
        usr.setDisplay_name("Displayname " + ident);
        usr.setGiven_name(ident);
        usr.setSur_name("Lastname " + ident);
        usr.setLanguage(Locale.GERMANY);
        // new for testing

        usr.setEmail1("primaryemail-"+ident+"@"+AbstractTest.TEST_DOMAIN);
        usr.setEmail2("email2-" + ident + "@" + AbstractTest.TEST_DOMAIN);
        usr.setEmail3("email3-" + ident + "@" + AbstractTest.TEST_DOMAIN);

        HashSet<String> aliase = new HashSet<String>();
        aliase.add("alias1-" + ident + "@" + AbstractTest.TEST_DOMAIN);
        aliase.add("alias2-" + ident + "@" + AbstractTest.TEST_DOMAIN);
        aliase.add("alias3-" + ident + "@" + AbstractTest.TEST_DOMAIN);
        aliase.add("email2-" + ident + "@" + AbstractTest.TEST_DOMAIN);
        aliase.add("email3-" + ident + "@" + AbstractTest.TEST_DOMAIN);
        aliase.add("primaryemail-" + ident + "@" + AbstractTest.TEST_DOMAIN);
        usr.setAliases(aliase);

        final Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
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
        usr.setImapServer("localhost");
        usr.setInstant_messenger1("InstantMessenger");
        usr.setInstant_messenger2("InstantMessenger2");
        usr.setTelephone_ip("IpPhone");
        usr.setTelephone_isdn("Isdn");
        usr.setMail_folder_drafts_name("MailFolderDrafts");
        usr.setMail_folder_sent_name("MailFolderSent");
        usr.setMail_folder_spam_name("MailFolderSpam");
        usr.setMail_folder_trash_name("MailFolderTrash");
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
        usr.setEmail2("Privateemail2-" + ident + "@" + AbstractTest.TEST_DOMAIN);
        usr.setEmail3("Privateemail3-" + ident + "@" + AbstractTest.TEST_DOMAIN);
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
        usr.setSmtpServer("SmtpServer");
        usr.setSpouse_name("SpouseName");
        usr.setState_home("State");
        usr.setStreet_home("Street");
        usr.setSuffix("Suffix");
        usr.setTax_id("TaxId");
        usr.setTelephone_telex("Telex");
        usr.setTimezone(TimeZone.getDefault());
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
        return usr;
    }

    public static User getTestUserObject() throws Exception {
        return getTestUserObject(VALID_CHAR_TESTUSER, "open-xchange");
    }

//    private static int getContextID() throws Exception {
//        final Credentials cred = DummyCredentials();
//        final Context ctx = ContextTest.getTestContextObject(ContextTest.createNewContextID(cred), 10);
//        final int id = ContextTest.addContext(ctx, getRMIHostUrl(), cred);
//        return id;
//    }

    private void compareUser(final User a, final User b) {
        System.out.println("USERA" + a.toString());
        System.out.println("USERB" + b.toString());
        
        assertEquals("username not equal", a.getUsername(), b.getUsername());
        assertEquals("enabled not equal", a.isEnabled(), b.isEnabled());
        assertEquals("primaryemail not equal",a.getPrimaryEmail(),b.getPrimaryEmail());        
        assertEquals("display name not equal", a.getDisplay_name(), b.getDisplay_name());
        assertEquals("firtname not equal", a.getGiven_name(), b.getGiven_name());
        assertEquals("lastname not equal", a.getSur_name(), b.getSur_name());
        assertEquals("language not equal", a.getLanguage(), b.getLanguage());
        // test aliasing comparing the content of the hashset
        assertEquals(a.getAliases(), b.getAliases());
        assertEquals("aniversary not equal", a.getAnniversary(), b.getAnniversary());
        assertEquals("assistants name not equal", a.getAssistant_name(), b.getAssistant_name());
        assertEquals("birthday not equal", a.getBirthday(), b.getBirthday());
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
        assertEquals("ImapServer not equal", a.getImapServer(), b.getImapServer());
        assertEquals("InstantMessenger not equal", a.getInstant_messenger1(), b.getInstant_messenger1());
        assertEquals("InstantMessenger2 not equal", a.getInstant_messenger2(), b.getInstant_messenger2());
        assertEquals("IpPhone not equal", a.getTelephone_ip(), b.getTelephone_ip());
        assertEquals("Isdn not equal", a.getTelephone_isdn(), b.getTelephone_isdn());
        assertEquals("MailFolderDrafts not equal", a.getMail_folder_drafts_name(), b.getMail_folder_drafts_name());
        assertEquals("MailFolderSent not equal", a.getMail_folder_sent_name(), b.getMail_folder_sent_name());
        assertEquals("MailFolderSpam not equal", a.getMail_folder_spam_name(), b.getMail_folder_spam_name());
        assertEquals("MailFolderTrash not equal", a.getMail_folder_trash_name(), b.getMail_folder_trash_name());
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
        assertEquals("SmtpServer not equal", a.getSmtpServer(), b.getSmtpServer());
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
    }

    private void compareUserAccess(final UserModuleAccess a, final UserModuleAccess b) {
        assertEquals("access calendar not equal", a.getCalendar(), b.getCalendar());
        assertEquals("access contacts not equal", a.getContacts(), b.getContacts());
        assertEquals("access delegatetasks not equal", a.getDelegateTask(), b.getDelegateTask());
        assertEquals("access edit public folders not equal", a.getEditPublicFolders(), b.getEditPublicFolders());
        assertEquals("access forum not equal", a.getForum(), b.getForum());
        assertEquals("access ical not equal", a.getIcal(), b.getIcal());
        assertEquals("access infostore not equal", a.getInfostore(), b.getInfostore());
        assertEquals("access pinboard write not equal", a.getPinboardWrite(), b.getPinboardWrite());
        assertEquals("access projects not equal", a.getProjects(), b.getProjects());
        assertEquals("access ReadCreateSharedFolders not equal", a.getReadCreateSharedFolders(), b.getReadCreateSharedFolders());
        assertEquals("access rss bookmarks not equal", a.getRssBookmarks(), b.getRssBookmarks());
        assertEquals("access rss portal not equal", a.getRssPortal(), b.getRssPortal());
        assertEquals("access syncml not equal", a.getSyncml(), b.getSyncml());
        assertEquals("access tasks not equal", a.getTasks(), b.getTasks());
        assertEquals("access vcard not equal", a.getVcard(), b.getVcard());
        assertEquals("access webdav not equal", a.getWebdav(), b.getWebdav());
        assertEquals("access webdav xml not equal", a.getWebdavXml(), b.getWebdavXml());
        assertEquals("access webmail not equal", a.getWebmail(), b.getWebmail());
    }
    
    public static int addUser(Context ctx,User usr,UserModuleAccess access) throws Exception{
        // create new user
        OXUserInterface oxu = getUserClient();        
        return oxu.create(ctx,usr,access,DummyCredentials());
    }
    

    private void createChangeUserData(final User usr){
        // change all fields of the user
       
        usr.setEnabled(!usr.isEnabled());        
        usr.setPrimaryEmail(usr.getPrimaryEmail()+change_suffix);
        usr.setEmail1(usr.getEmail1()+change_suffix);
        usr.setEmail2(usr.getEmail2()+change_suffix);
        usr.setEmail3(usr.getEmail3()+change_suffix);
        
        usr.setDisplay_name(usr.getDisplay_name()+change_suffix);
        usr.setGiven_name(usr.getGiven_name()+change_suffix);
        usr.setSur_name(usr.getSur_name()+change_suffix);
        usr.setLanguage(Locale.US);
        // new for testing
        
        final HashSet<String> aliase = usr.getAliases();
        final HashSet<String> lAliases = new HashSet<String>();
        for (final String element : aliase) {
            lAliases.add(element + "_" + change_suffix);            
        }
        lAliases.add(usr.getPrimaryEmail());
        lAliases.add(usr.getEmail2());
        
        usr.setAliases(lAliases);        
        
        // set the dates to the acutal + 1 day
        usr.setBirthday(new Date(usr.getBirthday().getTime()+(24*60*60*1000)));
        usr.setAnniversary(new Date(usr.getAnniversary().getTime()+(24*60*60*1000)));        
        usr.setAssistant_name(usr.getAssistant_name()+change_suffix);        
        usr.setBranches(usr.getBranches()+change_suffix);
        usr.setBusiness_category(usr.getBusiness_category()+change_suffix);
        usr.setCity_business(usr.getCity_business()+change_suffix);
        usr.setCountry_business(usr.getCountry_business()+change_suffix);
        usr.setPostal_code_business(usr.getPostal_code_business()+change_suffix);
        usr.setState_business(usr.getState_business()+change_suffix);
        usr.setStreet_business(usr.getStreet_business()+change_suffix);
        usr.setTelephone_callback(usr.getTelephone_callback()+change_suffix);
        usr.setCity_home(usr.getCity_home()+change_suffix);
        usr.setCommercial_register(usr.getCommercial_register()+change_suffix);
        usr.setCompany(usr.getCompany()+change_suffix);
        usr.setCountry_home(usr.getCountry_home()+change_suffix);
        usr.setDepartment(usr.getDepartment()+change_suffix);
        usr.setEmployeeType(usr.getEmployeeType()+change_suffix);
        usr.setFax_business(usr.getFax_business()+change_suffix);
        usr.setFax_home(usr.getFax_home()+change_suffix);
        usr.setFax_other(usr.getFax_other()+change_suffix);
        usr.setImapServer(usr.getImapServer()+change_suffix);
        usr.setInstant_messenger1(usr.getInstant_messenger1()+change_suffix);
        usr.setInstant_messenger2(usr.getInstant_messenger2()+change_suffix);
        usr.setTelephone_ip(usr.getTelephone_ip()+change_suffix);
        usr.setTelephone_isdn(usr.getTelephone_isdn()+change_suffix);
        usr.setMail_folder_drafts_name(usr.getMail_folder_drafts_name()+change_suffix);
        usr.setMail_folder_sent_name(usr.getMail_folder_sent_name()+change_suffix);
        usr.setMail_folder_spam_name(usr.getMail_folder_spam_name()+change_suffix);
        usr.setMail_folder_trash_name(usr.getMail_folder_trash_name()+change_suffix);
        usr.setManager_name(usr.getManager_name()+change_suffix);
        usr.setMarital_status(usr.getMarital_status()+change_suffix);
        usr.setCellular_telephone1(usr.getCellular_telephone1()+change_suffix);
        usr.setCellular_telephone2(usr.getCellular_telephone2()+change_suffix);
        usr.setInfo(usr.getInfo()+change_suffix);
        usr.setNickname(usr.getNickname()+change_suffix);
        usr.setNote(usr.getNote()+change_suffix);
        usr.setNumber_of_children(usr.getNumber_of_children()+change_suffix);
        usr.setNumber_of_employee(usr.getNumber_of_employee()+change_suffix);
        usr.setTelephone_pager(usr.getTelephone_pager()+change_suffix);        
        usr.setPassword_expired(!usr.getPassword_expired());       
        usr.setTelephone_assistant(usr.getTelephone_assistant()+change_suffix);
        usr.setTelephone_business1(usr.getTelephone_business1()+change_suffix);
        usr.setTelephone_business2(usr.getTelephone_business2()+change_suffix);
        usr.setTelephone_car(usr.getTelephone_car()+change_suffix);
        usr.setTelephone_company(usr.getTelephone_company()+change_suffix);
        usr.setTelephone_home1(usr.getTelephone_home1()+change_suffix);
        usr.setTelephone_home2(usr.getTelephone_home2()+change_suffix);
        usr.setTelephone_other(usr.getTelephone_other()+change_suffix);
        usr.setPosition(usr.getPosition()+change_suffix);
        usr.setPostal_code_home(usr.getPostal_code_home()+change_suffix);        
        usr.setProfession(usr.getProfession()+change_suffix);
        usr.setTelephone_radio(usr.getTelephone_radio()+change_suffix);
        usr.setRoom_number(usr.getRoom_number()+change_suffix);
        usr.setSales_volume(usr.getSales_volume()+change_suffix);
        usr.setCity_other(usr.getCity_other()+change_suffix);
        usr.setCountry_other(usr.getCountry_other()+change_suffix);
        usr.setMiddle_name(usr.getMiddle_name()+change_suffix);
        usr.setPostal_code_other(usr.getPostal_code_other()+change_suffix);
        usr.setState_other(usr.getState_other()+change_suffix);
        usr.setStreet_other(usr.getStreet_other()+change_suffix);
        usr.setSmtpServer(usr.getSmtpServer()+change_suffix);
        usr.setSpouse_name(usr.getSpouse_name()+change_suffix);
        usr.setState_home(usr.getState_home()+change_suffix);
        usr.setStreet_home(usr.getStreet_home()+change_suffix);
        usr.setSuffix(usr.getSuffix()+change_suffix);
        usr.setTax_id(usr.getTax_id()+change_suffix);
        usr.setTelephone_telex(usr.getTelephone_telex()+change_suffix);
        usr.setTimezone(usr.getTimezone());
        usr.setTitle(usr.getTitle()+change_suffix);
        usr.setTelephone_ttytdd(usr.getTelephone_ttytdd()+change_suffix);
        usr.setUrl(usr.getUrl()+change_suffix);
        usr.setUserfield01(usr.getUserfield01()+change_suffix);
        usr.setUserfield02(usr.getUserfield02()+change_suffix);
        usr.setUserfield03(usr.getUserfield03()+change_suffix);
        usr.setUserfield04(usr.getUserfield04()+change_suffix);
        usr.setUserfield05(usr.getUserfield05()+change_suffix);
        usr.setUserfield06(usr.getUserfield06()+change_suffix);
        usr.setUserfield07(usr.getUserfield07()+change_suffix);
        usr.setUserfield08(usr.getUserfield08()+change_suffix);
        usr.setUserfield09(usr.getUserfield09()+change_suffix);
        usr.setUserfield10(usr.getUserfield10()+change_suffix);
        usr.setUserfield11(usr.getUserfield11()+change_suffix);
        usr.setUserfield12(usr.getUserfield12()+change_suffix);
        usr.setUserfield13(usr.getUserfield13()+change_suffix);
        usr.setUserfield14(usr.getUserfield14()+change_suffix);
        usr.setUserfield15(usr.getUserfield15()+change_suffix);
        usr.setUserfield16(usr.getUserfield16()+change_suffix);
        usr.setUserfield17(usr.getUserfield17()+change_suffix);
        usr.setUserfield18(usr.getUserfield18()+change_suffix);
        usr.setUserfield19(usr.getUserfield19()+change_suffix);
        usr.setUserfield20(usr.getUserfield20()+change_suffix);
    }
}
