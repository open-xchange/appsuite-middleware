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
package com.openexchange.admin;

import com.openexchange.admin.container.Context;
import com.openexchange.admin.container.User;
import com.openexchange.admin.dataSource.I_OXUser;

import java.rmi.Naming;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

/**
 *
 * @author cutmasta
 */
public class UserTest extends AbstractAdminTest{
    
    // list of chars that must be valid
    private static String VALID_CHAR_TESTUSER = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-+.%$@";
    // global setting for stored password
    private static String pass = "foo-user-pass";
    
	public void testAddUser() throws Exception {
        User urs = getTestUserObject();
        urs.giveAllAccessRights();
        long id = addUser(urs,getRMIHost());
        assertTrue("object id > 0 expected",id>0);
    }
    
    public void testDeleteUser() throws Exception {

        long contextid = getContextID();
        long id = addUser(getTestUserObject(VALID_CHAR_TESTUSER,pass,contextid),getRMIHost());
        User urs = new User();
        urs.setContextId(contextid);
        urs.setId(id);
        deleteUser(urs,getRMIHost());
        try{
            loadUser(urs,getRMIHost());
            fail("user not exists expected");
        }catch(Exception ecp){
            if(ecp.toString().toLowerCase().indexOf("user does not exist")!=-1){
                // this exception MUST happen, if not, test MUST fail :)
                assertTrue(true);
            }
        }
    }
    
    public void testGetUserData() throws Exception {
        User us = getTestUserObject();
        long load_id = addUser(us,getRMIHost());
        us.setId(load_id);
        compareUser(us,loadUser(us,getRMIHost()));
    }
    
    public void testGetUserDataByUid() throws Exception {
        long contextid = getContextID();
        User us = getTestUserObject(VALID_CHAR_TESTUSER,pass, contextid);
        long load_id = addUser(us,getRMIHost());
        us.setId(load_id);
        compareUser(us,loadUserByUid(us,getRMIHost()));
    }
    
    public void testLoadUserModuleAccess() throws Exception {
        long contextid = getContextID();
        
        User us = getTestUserObject(VALID_CHAR_TESTUSER,pass,contextid);
        long load_id = addUser(us,getRMIHost());
        us.setId(load_id);
        us.setContextId(contextid);
        compareUserAccess(us,loadUserModuleAccess(us,getRMIHost()));
    }
    
    public void testChangeUserModuleAccess() throws Exception {
        long contextid = getContextID();
        
        
        User allrightsfalse = getTestUserObject(VALID_CHAR_TESTUSER,pass,contextid);
        long load_id = addUser(allrightsfalse,getRMIHost());
        allrightsfalse.setId(load_id);
        
        // set all true
        allrightsfalse.giveAllAccessRights();
        changeUserModuleAccess(allrightsfalse,getRMIHost());
        
        // load now the rights from the server
        User loadedfromserver = getTestUserObject(VALID_CHAR_TESTUSER,pass,contextid);
        loadedfromserver.setId(load_id);
        loadedfromserver = loadUserModuleAccess(loadedfromserver,getRMIHost());
        
        // this must be ok, else the rights were not updated
        compareUserAccess(allrightsfalse,loadedfromserver);
    }
    
    public void testLoadAllUsers() throws Exception {
        long contextid = getContextID();
        long[] user_ids = loadAllUsers(contextid,getRMIHost());
        long added_user_id = addUser(getTestUserObject(VALID_CHAR_TESTUSER,pass,contextid),getRMIHost());                
        long[] new_user_ids = loadAllUsers(contextid,getRMIHost());
        
        // now check if all old ids are in the new search result
        int not_inlist_counter = 0;
        for(int a = 0;a< user_ids.length;a++){
            long old_id = user_ids[a];
            if(!oldIsInNewArray(old_id,new_user_ids)){
                not_inlist_counter++;
            }
        }
        
        // now check if new added user is in the search result
        boolean foundnewuser = false;
        for(int a = 0;a < new_user_ids.length;a++){
            if(new_user_ids[a]==added_user_id){
                foundnewuser = true;
            }
            
        }
        assertTrue("found not all users,"+not_inlist_counter+" users are missing after loading all users for context "+contextid,not_inlist_counter==0);
        assertTrue("user id "+added_user_id+" not found after loading all users for context "+contextid,foundnewuser);
    }
    
    public void testChangeUserData() throws Exception {
        long contextid = getContextID();

        User us = getTestUserObject(VALID_CHAR_TESTUSER,pass,contextid);
        long load_id = addUser(us,getRMIHost());       
        us.setId(load_id);
        User edit_data = loadUser(us,getRMIHost());
        compareUser(us,edit_data);
        
        // create data to change
        createChangeUserData(edit_data);
        
        // change data
        changeUserData(edit_data,getRMIHost());
        
        // load from sevrer 
        User rem = loadUser(edit_data,getRMIHost());
        
        // compare with 
        compareUser(edit_data,rem);
    }
    
    public static void changeUserData(User urs,String host) throws Exception   {
        I_OXUser xres = (I_OXUser)Naming.lookup(checkHost(host)+I_OXUser.RMI_NAME);
        Vector v = xres.changeUserData((int)urs.getContextId(),(int)urs.getId(),urs.xForm2Userdata());
        parseResponse(v);
    }
    
    public static long[] loadAllUsers(long context_id,String host)  throws Exception {
        I_OXUser xres = (I_OXUser)Naming.lookup(checkHost(host)+I_OXUser.RMI_NAME);
        Vector v = xres.getAllUsers((int)context_id);
        parseResponse(v);
        Vector ids = (Vector)v.get(1);
        long [] ret = new long[ids.size()];
        for(int a =0;a<ids.size();a++){
            ret[a] = Long.parseLong(ids.get(a).toString());
        }
        return ret;
    }
    
    
    public static long addUser(User urs,String host) throws Exception {
        I_OXUser xres = (I_OXUser)Naming.lookup(checkHost(host)+I_OXUser.RMI_NAME);
        Vector v = xres.createUser((int)urs.getContextId(),urs.xForm2Userdata(),urs.xForm2AccessData());
        parseResponse(v);
        return Long.parseLong(v.get(1).toString());
    }
    
    public static void deleteUser(User urs,String host)throws Exception{
        I_OXUser xres = (I_OXUser)Naming.lookup(checkHost(host)+I_OXUser.RMI_NAME);
        Vector v = xres.deleteUser((int)urs.getContextId(),(int)urs.getId());
        parseResponse(v);
    }
    
    public static User loadUser(long userid,long contextid,String host)throws Exception {
        User vla = new User();
        vla.setId(userid);
        vla.setContextId(contextid);
        return loadUser(vla,host);
    }
    
    public static User loadUser(User usr,String host)throws Exception {
        I_OXUser xres = (I_OXUser)Naming.lookup(checkHost(host)+I_OXUser.RMI_NAME);
        Vector v = xres.getUserData((int)usr.getContextId(),(int)usr.getId());
        parseResponse(v);
        Hashtable data = (Hashtable)v.get(1);
        User ret = new User();
        ret.xForm2Object(data);
        return ret;
    }
    
    public static User loadUserByUid(User usr,String host)throws Exception {
        I_OXUser xres = (I_OXUser)Naming.lookup(checkHost(host)+I_OXUser.RMI_NAME);
        Vector v = xres.getUserData((int)usr.getContextId(),usr.getUsername());
        parseResponse(v);
        Hashtable data = (Hashtable)v.get(1);
        User ret = new User();
        ret.xForm2Object(data);
        return ret;
    }
    
    public static User loadUserModuleAccess(User usr,String host) throws Exception {
        I_OXUser xres = (I_OXUser)Naming.lookup(checkHost(host)+I_OXUser.RMI_NAME);
        Vector v = xres.getUserModuleAccess((int)usr.getContextId(),(int)usr.getId());
        parseResponse(v);
        Hashtable accessdata = (Hashtable)v.get(1);
        User ret = new User();
        ret.xForm2ObjectAccess(accessdata);
        ret.setId(usr.getId());
        ret.setContextId(usr.getContextId());
        return ret;
    }
    
    public static void changeUserModuleAccess(User usr,String host) throws Exception {
        I_OXUser xres = (I_OXUser)Naming.lookup(checkHost(host)+I_OXUser.RMI_NAME);
        Vector v = xres.changeUserModuleAccess((int)usr.getContextId(),(int)usr.getId(),usr.xForm2AccessData());
        parseResponse(v);
    }
    
    public static User getTestUserObject(String ident,String password,long contextid){
        User usr = new User();
        usr.setUsername(ident);
        usr.setPassword(password);
        usr.setContextId(contextid);
        usr.setEnabled(true);
        usr.setPrimaryEMail("primaryemail-"+ident+"@"+AbstractAdminTest.TEST_DOMAIN);
        usr.setDisplayName("Displayname "+ident);
        usr.setFirstName(ident);
        usr.setLastName("Lastname "+ident);
        usr.setLanguage("de_DE");
        // new for testing
        
        usr.setPrivateEmail1("primaryemail-"+ident+"@"+AbstractAdminTest.TEST_DOMAIN);
        usr.setPrivateEmail2("email2-"+ident+"@"+AbstractAdminTest.TEST_DOMAIN);
        usr.setPrivateEmail3("email3-"+ident+"@"+AbstractAdminTest.TEST_DOMAIN);
        
        String [] aliase = {"alias1-"+ident+"@"+AbstractAdminTest.TEST_DOMAIN,
                            "alias2-"+ident+"@"+AbstractAdminTest.TEST_DOMAIN,
                            "alias3-"+ident+"@"+AbstractAdminTest.TEST_DOMAIN,
                            "email2-"+ident+"@"+AbstractAdminTest.TEST_DOMAIN,
                            "email3-"+ident+"@"+AbstractAdminTest.TEST_DOMAIN,
                            "primaryemail-"+ident+"@"+AbstractAdminTest.TEST_DOMAIN};
        usr.setAlias(aliase);
        
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        usr.setBirthDay(cal.getTime());
        usr.setAnniversary(cal.getTime());
        
        usr.setAssistantsName("assistants name");
        
        
        usr.setBranches("Branches");
        usr.setBusinessCategory("Business Category");
        usr.setBusinessCity("Business City");
        usr.setBusinessCountry("Business Country");
        usr.setBusinessPostalCode("BusinessPostalCode");
        usr.setBusinessState("BusinessState");
        usr.setBusinessStreet("BusinessStreet");
        usr.setCallBack("callback");
        usr.setCity("City");
        usr.setCommercialRegister("CommercialRegister");
        usr.setCompany("Company");
        usr.setCountry("Country");
        usr.setDepartment("Department");
        usr.setEmployeeType("EmployeeType");
        usr.setFaxBusiness("FaxBusiness");
        usr.setFaxHome("FaxHome");
        usr.setFaxOther("FaxOther");
        usr.setImapServer("imapserver.de");
        usr.setInstantMessenger("InstantMessenger");
        usr.setInstantMessenger2("InstantMessenger2");
        usr.setIpPhone("IpPhone");
        usr.setIsdn("Isdn");
        usr.setMailFolderDrafts("MailFolderDrafts");
        usr.setMailFolderSent("MailFolderSent");
        usr.setMailFolderSpam("MailFolderSpam");
        usr.setMailFolderTrash("MailFolderTrash");
        usr.setManagersName("ManagersName");
        usr.setMaritalStatus("MaritalStatus");
        usr.setMobile1("Mobile1");
        usr.setMobile2("Mobile2");
        usr.setMoreInfo("MoreInfo");
        usr.setNickName("NickName");
        usr.setNote("Note");
        usr.setNumberOfChildren("NumberOfChildren");
        usr.setNumberOfEmployee("NumberOfEmployee");
        usr.setPager("Pager");
        usr.setPasswordExpired(false);
        usr.setPhoneAssistant("PhoneAssistant");
        usr.setPhoneBusiness("PhoneBusiness");
        usr.setPhoneBusiness2("PhoneBusiness2");
        usr.setPhoneCar("PhoneCar");
        usr.setPhoneCompany("PhoneCompany");
        usr.setPhoneHome("PhoneHome");
        usr.setPhoneHome2("PhoneHome2");
        usr.setPhoneOther("PhoneOther");
        usr.setPosition("Position");
        usr.setPostalCode("PostalCode");
        usr.setPrivateEmail2("Privateemail2-"+ident+"@"+AbstractAdminTest.TEST_DOMAIN);
        usr.setPrivateEmail3("Privateemail3-"+ident+"@"+AbstractAdminTest.TEST_DOMAIN);
        usr.setProfession("Profession");
        usr.setRadio("Radio");
        usr.setRoomNumber("1337");
        usr.setSalesVolume("SalesVolume");
        usr.setSecondCity("SecondCity");
        usr.setSecondCountry("SecondCountry");
        usr.setSecondName("SecondName");
        usr.setSecondPostalCode("SecondPostalCode");
        usr.setSecondState("SecondState");
        usr.setSecondStreet("SecondStreet");
        usr.setSmtpServer("SmtpServer");
        usr.setSpouseName("SpouseName");
        usr.setState("State");
        usr.setStreet("Street");
        usr.setSuffix("Suffix");
        usr.setTaxId("TaxId");
        usr.setTelex("Telex");
        usr.setTimeZone("Timezone");
        usr.setTitle("Title");
        usr.setTtyTdd("TtyTdd");
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
    
    public static User getTestUserObject() throws Exception{
        return getTestUserObject(VALID_CHAR_TESTUSER,"open-xchange",getContextID());
    }
    
    private static String getRMIHost(){
        return "localhost";
    }
    
    private static long getContextID() throws Exception{
        Context ctx = ContextTest.getTestContextObject(ContextTest.createNewContextID(),10);
        long id = ContextTest.addContext(ctx,getRMIHost());
        return id;
    }
    
    private void compareUser(User a, User b) {
        assertEquals("context id not equal",a.getContextId(),b.getContextId());
        assertEquals("username not equal",a.getUsername(),b.getUsername());
        assertEquals("enabled not equal",a.isEnabled(),b.isEnabled());
        // NOT MORE POSSIBLE assertEquals("primary email not equal",a.getPrimaryEMail(),b.getPrimaryEMail());
        assertEquals("display name not equal",a.getDisplayName(),b.getDisplayName());
        assertEquals("firtname not equal",a.getFirstName(),b.getFirstName());
        assertEquals("lastname not equal",a.getLastName(),b.getLastName());
        assertEquals("language not equal",a.getLanguage(),b.getLanguage());
        // test aliasing comparing the content of the string[]
        compareStringArray(a.getAlias(),b.getAlias());
        assertEquals("aniversary not equal",a.getAnniversary(),b.getAnniversary());
        assertEquals("assistants name not equal",a.getAssistantsName(),b.getAssistantsName());
        assertEquals("birthday not equal",a.getBirthDay(),b.getBirthDay());
        assertEquals("branches not equal",a.getBranches(),b.getBranches());
        assertEquals("BusinessCategory not equal",a.getBusinessCategory(),b.getBusinessCategory());
        assertEquals("BusinessCity not equal",a.getBusinessCity(),b.getBusinessCity());
        assertEquals("BusinessCountry not equal",a.getBusinessCountry(),b.getBusinessCountry());
        assertEquals("BusinessPostalCode not equal",a.getBusinessPostalCode(),b.getBusinessPostalCode());
        assertEquals("BusinessState not equal",a.getBusinessState(),b.getBusinessState());
        assertEquals("BusinessStreet not equal",a.getBusinessStreet(),b.getBusinessStreet());
        assertEquals("callback not equal",a.getCallBack(),b.getCallBack());
        assertEquals("CommercialRegister not equal",a.getCommercialRegister(),b.getCommercialRegister());
        assertEquals("Company not equal",a.getCompany(),b.getCompany());
        assertEquals("Country not equal",a.getCountry(),b.getCountry());
        assertEquals("Department not equal",a.getDepartment(),b.getDepartment());
        assertEquals("EmployeeType not equal",a.getEmployeeType(),b.getEmployeeType());
        assertEquals("FaxBusiness not equal",a.getFaxBusiness(),b.getFaxBusiness());
        assertEquals("FaxHome not equal",a.getFaxHome(),b.getFaxHome());
        assertEquals("FaxOther not equal",a.getFaxOther(),b.getFaxOther());
        assertEquals("ImapServer not equal",a.getImapServer(),b.getImapServer());
        assertEquals("InstantMessenger not equal",a.getInstantMessenger(),b.getInstantMessenger());
        assertEquals("InstantMessenger2 not equal",a.getInstantMessenger2(),b.getInstantMessenger2());
        assertEquals("IpPhone not equal",a.getIpPhone(),b.getIpPhone());
        assertEquals("Isdn not equal",a.getIsdn(),b.getIsdn());
        assertEquals("MailFolderDrafts not equal",a.getMailFolderDrafts(),b.getMailFolderDrafts());
        assertEquals("MailFolderSent not equal",a.getMailFolderSent(),b.getMailFolderSent());
        assertEquals("MailFolderSpam not equal",a.getMailFolderSpam(),b.getMailFolderSpam());
        assertEquals("MailFolderTrash not equal",a.getMailFolderTrash(),b.getMailFolderTrash());
        assertEquals("ManagersName not equal",a.getManagersName(),b.getManagersName());
        assertEquals("MaritalStatus not equal",a.getMaritalStatus(),b.getMaritalStatus());
        assertEquals("Mobile1 not equal",a.getMobile1(),b.getMobile1());
        assertEquals("Mobile2 not equal",a.getMobile2(),b.getMobile2());
        assertEquals("MoreInfo not equal",a.getMoreInfo(),b.getMoreInfo());
        assertEquals("NickName not equal",a.getNickName(),b.getNickName());
        assertEquals("Note not equal",a.getNote(),b.getNote());
        assertEquals("NumberOfChildren not equal",a.getNumberOfChildren(),b.getNumberOfChildren());
        assertEquals("NumberOfEmployee not equal",a.getNumberOfEmployee(),b.getNumberOfEmployee());
        assertEquals("Pager not equal",a.getPager(),b.getPager());
        assertEquals("PasswordExpired not equal",a.isPasswordExpired(),b.isPasswordExpired());
        assertEquals("PhoneAssistant not equal",a.getPhoneAssistant(),b.getPhoneAssistant());
        assertEquals("PhoneBusiness not equal",a.getPhoneBusiness(),b.getPhoneBusiness());
        assertEquals("PhoneBusiness2 not equal",a.getPhoneBusiness2(),b.getPhoneBusiness2());
        assertEquals("PhoneCar not equal",a.getPhoneCar(),b.getPhoneCar());
        assertEquals("PhoneCompany not equal",a.getPhoneCompany(),b.getPhoneCompany());
        assertEquals("PhoneHome not equal",a.getPhoneHome(),b.getPhoneHome());
        assertEquals("PhoneHome2 not equal",a.getPhoneHome2(),b.getPhoneHome2());
        assertEquals("PhoneOther not equal",a.getPhoneOther(),b.getPhoneOther());
        assertEquals("Position not equal",a.getPosition(),b.getPosition());
        assertEquals("PostalCode not equal",a.getPostalCode(),b.getPostalCode());
        assertEquals("Email1 not equal",a.getPrivateEmail1(),b.getPrivateEmail1());
        assertEquals("Email2 not equal",a.getPrivateEmail2(),b.getPrivateEmail2());
        assertEquals("Email3 not equal",a.getPrivateEmail3(),b.getPrivateEmail3());
        assertEquals("Profession not equal",a.getProfession(),b.getProfession());
        assertEquals("Radio not equal",a.getRadio(),b.getRadio());
        assertEquals("RoomNumber not equal",a.getRoomNumber(),b.getRoomNumber());
        assertEquals("SalesVolume not equal",a.getSalesVolume(),b.getSalesVolume());
        assertEquals("SecondCity not equal",a.getSecondCity(),b.getSecondCity());
        assertEquals("SecondCountry not equal",a.getSecondCountry(),b.getSecondCountry());
        assertEquals("SecondName not equal",a.getSecondName(),b.getSecondName());
        assertEquals("SecondPostalCode not equal",a.getSecondPostalCode(),b.getSecondPostalCode());
        assertEquals("SecondState not equal",a.getSecondState(),b.getSecondState());
        assertEquals("SecondStreet not equal",a.getSecondStreet(),b.getSecondStreet());
        assertEquals("SmtpServer not equal",a.getSmtpServer(),b.getSmtpServer());
        assertEquals("SpouseName not equal",a.getSpouseName(),b.getSpouseName());
        assertEquals("State not equal",a.getState(),b.getState());
        assertEquals("Street not equal",a.getStreet(),b.getStreet());
        assertEquals("Suffix not equal",a.getSuffix(),b.getSuffix());
        assertEquals("TaxId not equal",a.getTaxId(),b.getTaxId());
        assertEquals("Telex not equal",a.getTelex(),b.getTelex());
        assertEquals("Timezone not equal",a.getTimeZone(),b.getTimeZone());
        assertEquals("Title not equal",a.getTitle(),b.getTitle());
        assertEquals("TtyTdd not equal",a.getTtyTdd(),b.getTtyTdd());
        assertEquals("Url not equal",a.getUrl(),b.getUrl());
        assertEquals("Userfield01 not equal",a.getUserfield01(),b.getUserfield01());
        assertEquals("Userfield02 not equal",a.getUserfield02(),b.getUserfield02());
        assertEquals("Userfield03 not equal",a.getUserfield03(),b.getUserfield03());
        assertEquals("Userfield04 not equal",a.getUserfield04(),b.getUserfield04());
        assertEquals("Userfield05 not equal",a.getUserfield05(),b.getUserfield05());
        assertEquals("Userfield06 not equal",a.getUserfield06(),b.getUserfield06());
        assertEquals("Userfield07 not equal",a.getUserfield07(),b.getUserfield07());
        assertEquals("Userfield08 not equal",a.getUserfield08(),b.getUserfield08());
        assertEquals("Userfield09 not equal",a.getUserfield09(),b.getUserfield09());
        assertEquals("Userfield10 not equal",a.getUserfield10(),b.getUserfield10());
        assertEquals("Userfield11 not equal",a.getUserfield11(),b.getUserfield11());
        assertEquals("Userfield12 not equal",a.getUserfield12(),b.getUserfield12());
        assertEquals("Userfield13 not equal",a.getUserfield13(),b.getUserfield13());
        assertEquals("Userfield14 not equal",a.getUserfield14(),b.getUserfield14());
        assertEquals("Userfield15 not equal",a.getUserfield15(),b.getUserfield15());
        assertEquals("Userfield16 not equal",a.getUserfield16(),b.getUserfield16());
        assertEquals("Userfield17 not equal",a.getUserfield17(),b.getUserfield17());
        assertEquals("Userfield18 not equal",a.getUserfield18(),b.getUserfield18());
        assertEquals("Userfield19 not equal",a.getUserfield19(),b.getUserfield19());
        assertEquals("Userfield20 not equal",a.getUserfield20(),b.getUserfield20());
    }
    
    private void compareUserAccess(User a, User b) {
        assertEquals("access calendar not equal",a.isAccessCalendar(),b.isAccessCalendar());
        assertEquals("access contacts not equal",a.isAccessContacts(),b.isAccessContacts());
        assertEquals("access delegatetasks not equal",a.isAccessDelegateTasks(),b.isAccessDelegateTasks());
        assertEquals("access edit public folders not equal",a.isAccessEditPublicFolders(),b.isAccessEditPublicFolders());
        assertEquals("access forum not equal",a.isAccessForum(),b.isAccessForum());
        assertEquals("access ical not equal",a.isAccessIcal(),b.isAccessIcal());
        assertEquals("access infostore not equal",a.isAccessInfostore(),b.isAccessInfostore());
        assertEquals("access pinboard write not equal",a.isAccessPinboardWrite(),b.isAccessPinboardWrite());
        assertEquals("access projects not equal",a.isAccessProjects(),b.isAccessProjects());
        assertEquals("access ReadCreateSharedFolders not equal",a.isAccessReadCreateSharedFolders(),b.isAccessReadCreateSharedFolders());
        assertEquals("access rss bookmarks not equal",a.isAccessRssBookmarks(),b.isAccessRssBookmarks());
        assertEquals("access rss portal not equal",a.isAccessRssPortal(),b.isAccessRssPortal());
        assertEquals("access syncml not equal",a.isAccessSyncml(),b.isAccessSyncml());
        assertEquals("access tasks not equal",a.isAccessTasks(),b.isAccessTasks());
        assertEquals("access vcard not equal",a.isAccessVcard(),b.isAccessVcard());
        assertEquals("access webdav not equal",a.isAccessWebdav(),b.isAccessWebdav());
        assertEquals("access webdav xml not equal",a.isAccessWebdavXml(),b.isAccessWebdavXml());
        assertEquals("access webmail not equal",a.isAccessWebmail(),b.isAccessWebmail());
    }

    private boolean oldIsInNewArray(long old_id,long[] new_ids) {
        boolean ret = false;
        for(int a = 0;a<new_ids.length;a++){
            if(new_ids[a]==old_id){
                ret = true;
            }
        }
        return ret;
    }
    
    private void createChangeUserData(User usr){
        // change all fields of the user
        if(usr.isEnabled()){
            usr.setEnabled(false);
        }else{
            usr.setEnabled(true);
        }
        usr.setPrimaryEMail(usr.getPrivateEmail1()+change_suffix);
        usr.setPrivateEmail1(usr.getPrivateEmail1()+change_suffix);
        usr.setPrivateEmail2(usr.getPrivateEmail2()+change_suffix);
        usr.setPrivateEmail3(usr.getPrivateEmail3()+change_suffix);
        
        usr.setDisplayName(usr.getDisplayName()+change_suffix);
        usr.setFirstName(usr.getFirstName()+change_suffix);
        usr.setLastName(usr.getLastName()+change_suffix);
        usr.setLanguage("en_US");
        // new for testing
        
        String [] aliase = usr.getAlias();
        List<String> lAliases = new ArrayList();
        for(int a = 0;a<aliase.length;a++){
            lAliases.add(change_suffix+"_"+aliase[a]);            
        }
        lAliases.add(usr.getPrimaryEMail());
        lAliases.add(usr.getPrivateEmail1());
        usr.setAlias((String[])lAliases.toArray(new String[lAliases.size()]));
        
        
        // set the dates to the acutal + 1 day
        usr.setBirthDay(new Date(usr.getBirthDay().getTime()+(24*60*60*1000)));
        usr.setAnniversary(new Date(usr.getAnniversary().getTime()+(24*60*60*1000)));
        
        usr.setAssistantsName(usr.getAssistantsName()+change_suffix);
        
        
        usr.setBranches(usr.getBranches()+change_suffix);
        usr.setBusinessCategory(usr.getBusinessCategory()+change_suffix);
        usr.setBusinessCity(usr.getBusinessCity()+change_suffix);
        usr.setBusinessCountry(usr.getBusinessCountry()+change_suffix);
        usr.setBusinessPostalCode(usr.getBusinessPostalCode()+change_suffix);
        usr.setBusinessState(usr.getBusinessState()+change_suffix);
        usr.setBusinessStreet(usr.getBusinessStreet()+change_suffix);
        usr.setCallBack(usr.getCallBack()+change_suffix);
        usr.setCity(usr.getCity()+change_suffix);
        usr.setCommercialRegister(usr.getCommercialRegister()+change_suffix);
        usr.setCompany(usr.getCompany()+change_suffix);
        usr.setCountry(usr.getCountry()+change_suffix);
        usr.setDepartment(usr.getDepartment()+change_suffix);
        usr.setEmployeeType(usr.getEmployeeType()+change_suffix);
        usr.setFaxBusiness(usr.getFaxBusiness()+change_suffix);
        usr.setFaxHome(usr.getFaxHome()+change_suffix);
        usr.setFaxOther(usr.getFaxOther()+change_suffix);
        usr.setImapServer(usr.getImapServer()+change_suffix);
        usr.setInstantMessenger(usr.getInstantMessenger()+change_suffix);
        usr.setInstantMessenger2(usr.getInstantMessenger2()+change_suffix);
        usr.setIpPhone(usr.getIpPhone()+change_suffix);
        usr.setIsdn(usr.getIsdn()+change_suffix);
        usr.setMailFolderDrafts(usr.getMailFolderDrafts()+change_suffix);
        usr.setMailFolderSent(usr.getMailFolderSent()+change_suffix);
        usr.setMailFolderSpam(usr.getMailFolderSpam()+change_suffix);
        usr.setMailFolderTrash(usr.getMailFolderTrash()+change_suffix);
        usr.setManagersName(usr.getManagersName()+change_suffix);
        usr.setMaritalStatus(usr.getMaritalStatus()+change_suffix);
        usr.setMobile1(usr.getMobile1()+change_suffix);
        usr.setMobile2(usr.getMobile2()+change_suffix);
        usr.setMoreInfo(usr.getMoreInfo()+change_suffix);
        usr.setNickName(usr.getNickName()+change_suffix);
        usr.setNote(usr.getNote()+change_suffix);
        usr.setNumberOfChildren(usr.getNumberOfChildren()+change_suffix);
        usr.setNumberOfEmployee(usr.getNumberOfEmployee()+change_suffix);
        usr.setPager(usr.getPager()+change_suffix);
        if(usr.isPasswordExpired()){
            usr.setPasswordExpired(false);
        }else{
            usr.setPasswordExpired(true);
        }
        usr.setPhoneAssistant(usr.getPhoneAssistant()+change_suffix);
        usr.setPhoneBusiness(usr.getPhoneBusiness()+change_suffix);
        usr.setPhoneBusiness2(usr.getPhoneBusiness2()+change_suffix);
        usr.setPhoneCar(usr.getPhoneCar()+change_suffix);
        usr.setPhoneCompany(usr.getPhoneCompany()+change_suffix);
        usr.setPhoneHome(usr.getPhoneHome()+change_suffix);
        usr.setPhoneHome2(usr.getPhoneHome2()+change_suffix);
        usr.setPhoneOther(usr.getPhoneOther()+change_suffix);
        usr.setPosition(usr.getPosition()+change_suffix);
        usr.setPostalCode(usr.getPostalCode()+change_suffix);
        
        usr.setProfession(usr.getProfession()+change_suffix);
        usr.setRadio(usr.getRadio()+change_suffix);
        usr.setRoomNumber(usr.getRoomNumber()+change_suffix);
        usr.setSalesVolume(usr.getSalesVolume()+change_suffix);
        usr.setSecondCity(usr.getSecondCity()+change_suffix);
        usr.setSecondCountry(usr.getSecondCountry()+change_suffix);
        usr.setSecondName(usr.getSecondName()+change_suffix);
        usr.setSecondPostalCode(usr.getSecondPostalCode()+change_suffix);
        usr.setSecondState(usr.getSecondState()+change_suffix);
        usr.setSecondStreet(usr.getSecondStreet()+change_suffix);
        usr.setSmtpServer(usr.getSmtpServer()+change_suffix);
        usr.setSpouseName(usr.getSpouseName()+change_suffix);
        usr.setState(usr.getState()+change_suffix);
        usr.setStreet(usr.getStreet()+change_suffix);
        usr.setSuffix(usr.getSuffix()+change_suffix);
        usr.setTaxId(usr.getTaxId()+change_suffix);
        usr.setTelex(usr.getTelex()+change_suffix);
        usr.setTimeZone(usr.getTimeZone()+change_suffix);
        usr.setTitle(usr.getTitle()+change_suffix);
        usr.setTtyTdd(usr.getTtyTdd()+change_suffix);
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
