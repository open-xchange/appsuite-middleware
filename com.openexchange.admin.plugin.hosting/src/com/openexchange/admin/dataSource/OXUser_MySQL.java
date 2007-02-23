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
package com.openexchange.admin.dataSource;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.contexts.ContextException;
import com.openexchange.groupware.delete.DeleteFailedException;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.server.DBPoolingException;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.dataSource.impl.OXUser;
import com.openexchange.admin.exceptions.PoolException;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.AdminDaemonTools;
import com.openexchange.admin.tools.PropertyHandler;
import com.openexchange.groupware.IDGenerator;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.contact.Contacts;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.tools.oxfolder.OXFolderAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class OXUser_MySQL {
    
    private AdminCache      cache   = null;
    private static Log log = LogFactory.getLog(OXUser_MySQL.class);
    private PropertyHandler prop    = null;
    
    private static Hashtable<String,Integer> CONTACT_HT = new Hashtable<String, Integer>();
    static {
        CONTACT_HT.put(I_OXUser.STREET_BUSINESS, 523);
        CONTACT_HT.put(I_OXUser.POSTAL_CODE_BUSINESS, 525);
        CONTACT_HT.put(I_OXUser.CITY_BUSINESS, 526);
        CONTACT_HT.put(I_OXUser.STATE_BUSINESS, 527);
        CONTACT_HT.put(I_OXUser.COUNTRY_BUSINESS, 528);
        CONTACT_HT.put(I_OXUser.BUSINESS_CATEGORY, 534);
        CONTACT_HT.put(I_OXUser.TELEPHONE_BUSINESS1, 542);
        CONTACT_HT.put(I_OXUser.TELEPHONE_BUSINESS2, 543);
        CONTACT_HT.put(I_OXUser.FAX_BUSINESS, 544);
        CONTACT_HT.put(I_OXUser.STREET_OTHER, 538);
        CONTACT_HT.put(I_OXUser.CITY_OTHER, 539);
        CONTACT_HT.put(I_OXUser.POSTAL_CODE_OTHER, 540);
        CONTACT_HT.put(I_OXUser.COUNTRY_OTHER, 541);
        CONTACT_HT.put(I_OXUser.TELEPHONE_OTHER, 553);
        CONTACT_HT.put(I_OXUser.FAX_OTHER, 554);
        CONTACT_HT.put(I_OXUser.STATE_OTHER, 598);
        CONTACT_HT.put(I_OXUser.GIVEN_NAME, 501);
        CONTACT_HT.put(I_OXUser.SUR_NAME, 502);
        CONTACT_HT.put(I_OXUser.DISPLAY_NAME, 500);
        CONTACT_HT.put(I_OXUser.MIDDLE_NAME, 503);
        CONTACT_HT.put(I_OXUser.SUFFIX, 504);
        CONTACT_HT.put(I_OXUser.TITLE, 505);
        CONTACT_HT.put(I_OXUser.STREET_HOME, 506);
        CONTACT_HT.put(I_OXUser.POSTAL_CODE_HOME, 507);
        CONTACT_HT.put(I_OXUser.CITY_HOME, 508);
        CONTACT_HT.put(I_OXUser.STATE_HOME, 509);
        CONTACT_HT.put(I_OXUser.COUNTRY_HOME, 510);
        CONTACT_HT.put(I_OXUser.BIRTHDAY, 511);
        CONTACT_HT.put(I_OXUser.MARITAL_STATUS, 512);
        CONTACT_HT.put(I_OXUser.NUMBER_OF_CHILDREN, 513);
        CONTACT_HT.put(I_OXUser.PROFESSION, 514);
        CONTACT_HT.put(I_OXUser.NICKNAME, 515);
        CONTACT_HT.put(I_OXUser.SPOUSE_NAME, 516);
        CONTACT_HT.put(I_OXUser.ANNIVERSARY, 517);
        CONTACT_HT.put(I_OXUser.NOTE, 518);
        CONTACT_HT.put(I_OXUser.DEPARTMENT, 519);
        CONTACT_HT.put(I_OXUser.POSITION, 520);
        CONTACT_HT.put(I_OXUser.EMPLOYEE_TYPE, 521);
        CONTACT_HT.put(I_OXUser.ROOM_NUMBER, 522);
        CONTACT_HT.put(I_OXUser.NUMBER_OF_EMPLOYEE, 529);
        CONTACT_HT.put(I_OXUser.SALES_VOLUME, 530);
        CONTACT_HT.put(I_OXUser.TAX_ID, 531);
        CONTACT_HT.put(I_OXUser.COMMERCIAL_REGISTER, 532);
        CONTACT_HT.put(I_OXUser.BRANCHES, 533);
        CONTACT_HT.put(I_OXUser.INFO, 535);
        CONTACT_HT.put(I_OXUser.MANAGER_NAME, 536);
        CONTACT_HT.put(I_OXUser.ASSISTANT_NAME, 537);
        CONTACT_HT.put(I_OXUser.TELEPHONE_CALLBACK, 545);
        CONTACT_HT.put(I_OXUser.TELEPHONE_CAR, 546);
        CONTACT_HT.put(I_OXUser.TELEPHONE_COMPANY, 547);
        CONTACT_HT.put(I_OXUser.TELEPHONE_HOME1, 548);
        CONTACT_HT.put(I_OXUser.TELEPHONE_HOME2, 549);
        CONTACT_HT.put(I_OXUser.FAX_HOME, 550);
        CONTACT_HT.put(I_OXUser.CELLULAR_TELEPHONE1, 551);
        CONTACT_HT.put(I_OXUser.CELLULAR_TELEPHONE2, 552);
        CONTACT_HT.put(I_OXUser.EMAIL1, 555);
        CONTACT_HT.put(I_OXUser.EMAIL2, 556);
        CONTACT_HT.put(I_OXUser.EMAIL3, 557);
        CONTACT_HT.put(I_OXUser.URL, 558);
        CONTACT_HT.put(I_OXUser.TELEPHONE_ISDN, 559);
        CONTACT_HT.put(I_OXUser.TELEPHONE_PAGER, 560);
        CONTACT_HT.put(I_OXUser.TELEPHONE_PRIMARY, 561);
        CONTACT_HT.put(I_OXUser.TELEPHONE_RADIO, 562);
        CONTACT_HT.put(I_OXUser.TELEPHONE_TELEX, 563);
        CONTACT_HT.put(I_OXUser.TELEPHONE_TTYTDD, 564);
        CONTACT_HT.put(I_OXUser.INSTANT_MESSENGER1, 565);
        CONTACT_HT.put(I_OXUser.INSTANT_MESSENGER2, 566);
        CONTACT_HT.put(I_OXUser.TELEPHONE_IP, 567);
        CONTACT_HT.put(I_OXUser.TELEPHONE_ASSISTANT, 568);
        CONTACT_HT.put(I_OXUser.COMPANY, 569);
        CONTACT_HT.put(I_OXUser.USERFIELD01, 571);
        CONTACT_HT.put(I_OXUser.USERFIELD02, 572);
        CONTACT_HT.put(I_OXUser.USERFIELD03, 573);
        CONTACT_HT.put(I_OXUser.USERFIELD04, 574);
        CONTACT_HT.put(I_OXUser.USERFIELD05, 575);
        CONTACT_HT.put(I_OXUser.USERFIELD06, 576);
        CONTACT_HT.put(I_OXUser.USERFIELD07, 577);
        CONTACT_HT.put(I_OXUser.USERFIELD08, 578);
        CONTACT_HT.put(I_OXUser.USERFIELD09, 579);
        CONTACT_HT.put(I_OXUser.USERFIELD10, 580);
        CONTACT_HT.put(I_OXUser.USERFIELD11, 581);
        CONTACT_HT.put(I_OXUser.USERFIELD12, 582);
        CONTACT_HT.put(I_OXUser.USERFIELD13, 583);
        CONTACT_HT.put(I_OXUser.USERFIELD14, 584);
        CONTACT_HT.put(I_OXUser.USERFIELD15, 585);
        CONTACT_HT.put(I_OXUser.USERFIELD16, 586);
        CONTACT_HT.put(I_OXUser.USERFIELD17, 587);
        CONTACT_HT.put(I_OXUser.USERFIELD18, 588);
        CONTACT_HT.put(I_OXUser.USERFIELD19, 589);
        CONTACT_HT.put(I_OXUser.USERFIELD20, 590);
    }
    private static final String [] CONTACT_FIELDS = {
        I_OXUser.STREET_BUSINESS,
        I_OXUser.POSTAL_CODE_BUSINESS,
        I_OXUser.CITY_BUSINESS,
        I_OXUser.STATE_BUSINESS,
        I_OXUser.COUNTRY_BUSINESS,
        I_OXUser.BUSINESS_CATEGORY,
        I_OXUser.TELEPHONE_BUSINESS1,
        I_OXUser.TELEPHONE_BUSINESS2,
        I_OXUser.FAX_BUSINESS,
        I_OXUser.STREET_OTHER,
        I_OXUser.CITY_OTHER,
        I_OXUser.POSTAL_CODE_OTHER,
        I_OXUser.COUNTRY_OTHER,
        I_OXUser.TELEPHONE_OTHER,
        I_OXUser.FAX_OTHER,
        I_OXUser.STATE_OTHER,
        I_OXUser.GIVEN_NAME,
        I_OXUser.SUR_NAME,
        I_OXUser.DISPLAY_NAME,
        I_OXUser.MIDDLE_NAME,
        I_OXUser.SUFFIX,
        I_OXUser.TITLE,
        I_OXUser.STREET_HOME,
        I_OXUser.POSTAL_CODE_HOME,
        I_OXUser.CITY_HOME,
        I_OXUser.STATE_HOME,
        I_OXUser.COUNTRY_HOME,
        I_OXUser.BIRTHDAY,
        I_OXUser.MARITAL_STATUS,
        I_OXUser.NUMBER_OF_CHILDREN,
        I_OXUser.PROFESSION,
        I_OXUser.NICKNAME,
        I_OXUser.SPOUSE_NAME,
        I_OXUser.ANNIVERSARY,
        I_OXUser.NOTE,
        I_OXUser.DEPARTMENT,
        I_OXUser.POSITION,
        I_OXUser.EMPLOYEE_TYPE,
        I_OXUser.ROOM_NUMBER,
        I_OXUser.NUMBER_OF_EMPLOYEE,
        I_OXUser.SALES_VOLUME,
        I_OXUser.TAX_ID,
        I_OXUser.COMMERCIAL_REGISTER,
        I_OXUser.BRANCHES,
        I_OXUser.INFO,
        I_OXUser.MANAGER_NAME,
        I_OXUser.ASSISTANT_NAME,
        I_OXUser.TELEPHONE_CALLBACK,
        I_OXUser.TELEPHONE_CAR,
        I_OXUser.TELEPHONE_COMPANY,
        I_OXUser.TELEPHONE_HOME1,
        I_OXUser.TELEPHONE_HOME2,
        I_OXUser.FAX_HOME,
        I_OXUser.CELLULAR_TELEPHONE1,
        I_OXUser.CELLULAR_TELEPHONE2,
        I_OXUser.EMAIL1,
        I_OXUser.EMAIL2,
        I_OXUser.EMAIL3,
        I_OXUser.URL,
        I_OXUser.TELEPHONE_ISDN,
        I_OXUser.TELEPHONE_PAGER,
        I_OXUser.TELEPHONE_PRIMARY,
        I_OXUser.TELEPHONE_RADIO,
        I_OXUser.TELEPHONE_TELEX,
        I_OXUser.TELEPHONE_TTYTDD,
        I_OXUser.INSTANT_MESSENGER1,
        I_OXUser.INSTANT_MESSENGER2,
        I_OXUser.TELEPHONE_IP,
        I_OXUser.TELEPHONE_ASSISTANT,
        I_OXUser.COMPANY,
        I_OXUser.USERFIELD01,
        I_OXUser.USERFIELD02,
        I_OXUser.USERFIELD03,
        I_OXUser.USERFIELD04,
        I_OXUser.USERFIELD05,
        I_OXUser.USERFIELD06,
        I_OXUser.USERFIELD07,
        I_OXUser.USERFIELD08,
        I_OXUser.USERFIELD09,
        I_OXUser.USERFIELD10,
        I_OXUser.USERFIELD11,
        I_OXUser.USERFIELD12,
        I_OXUser.USERFIELD13,
        I_OXUser.USERFIELD14,
        I_OXUser.USERFIELD15,
        I_OXUser.USERFIELD16,
        I_OXUser.USERFIELD17,
        I_OXUser.USERFIELD18,
        I_OXUser.USERFIELD19,
        I_OXUser.USERFIELD20
    };
    
    public OXUser_MySQL() {
        try {
            cache   = ClientAdminThread.cache;
            prop    = cache.getProperties();
            //log.info( "class loaded: " + this.getClass().getName() );
        } catch ( Exception e ) {
            log.error("Error init",e);
        }
    }
    
    private int[] getGroupsForUser(int context_id,int user_id,Connection read_ox_con) throws SQLException{
        
        PreparedStatement prep = null;
        try{
            prep = read_ox_con.prepareStatement("SELECT id FROM groups_member WHERE cid = ? AND member = ?");
            prep.setInt(1,context_id);
            prep.setInt(2,user_id);
            ResultSet rs = prep.executeQuery();
            
            Vector<String> groups = new Vector<String>();
            // add colubrids ALL_GROUPS_AND_USERS group to the group
            groups.add("0");
            while(rs.next()){
                String id = rs.getString(1);
                
                groups.add(id);
                
            }
            int [] all_groups = new int[groups.size()];
            for(int a = 0;a<groups.size();a++){
                all_groups[a] = Integer.parseInt(""+groups.get(a));
            }
            prep.close();
            return all_groups;
        }finally{
            try {
                if(prep!=null){
                    prep.close();
                }
            } catch (Exception e) {
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,e);
            }
        }
    }
    
    public Vector<Object> getUserModuleAccess(int context_id, int user_id) throws PoolException, SQLException, DBPoolingException  {
        Vector<Object> v = new Vector<Object>();
        Connection read_ox_con = null;
        try{
            read_ox_con = cache.getREADConnectionForContext(context_id);
            int [] bla = getGroupsForUser(context_id,user_id,read_ox_con);
            UserConfiguration user = UserConfiguration.loadUserConfiguration(user_id,bla,context_id,read_ox_con);
            Hashtable<String, Boolean> access = new Hashtable<String, Boolean>();
            if(user.hasCalendar()){
                access.put(I_OXUser.ACCESS_CALENDAR,Boolean.TRUE);
            }else{
                access.put(I_OXUser.ACCESS_CALENDAR,Boolean.FALSE);
            }
            if(user.hasContact()){
                access.put(I_OXUser.ACCESS_CONTACTS,Boolean.TRUE);
            }else{
                access.put(I_OXUser.ACCESS_CONTACTS,Boolean.FALSE);
            }
            if(user.hasForum()){
                access.put(I_OXUser.ACCESS_FORUM,Boolean.TRUE);
            }else{
                access.put(I_OXUser.ACCESS_FORUM,Boolean.FALSE);
            }
            if(user.hasFullPublicFolderAccess()){
                access.put(I_OXUser.ACCESS_EDIT_PUBLIC_FOLDERS,Boolean.TRUE);
            }else{
                access.put(I_OXUser.ACCESS_EDIT_PUBLIC_FOLDERS,Boolean.FALSE);
            }
            if(user.hasFullSharedFolderAccess()){
                access.put(I_OXUser.ACCESS_READ_CREATE_SHARED_FOLDERS,Boolean.TRUE);
            }else{
                access.put(I_OXUser.ACCESS_READ_CREATE_SHARED_FOLDERS,Boolean.FALSE);
            }
            if(user.hasICal()){
                access.put(I_OXUser.ACCESS_ICAL,Boolean.TRUE);
            }else{
                access.put(I_OXUser.ACCESS_ICAL,Boolean.FALSE);
            }
            if(user.hasInfostore()){
                access.put(I_OXUser.ACCESS_INFOSSTORE,Boolean.TRUE);
            }else{
                access.put(I_OXUser.ACCESS_INFOSSTORE,Boolean.FALSE);
            }
            if(user.hasPinboardWriteAccess()){
                access.put(I_OXUser.ACCESS_PINBOARD_WRITE_ACCESS,Boolean.TRUE);
            }else{
                access.put(I_OXUser.ACCESS_PINBOARD_WRITE_ACCESS,Boolean.FALSE);
            }
            if(user.hasProject()){
                access.put(I_OXUser.ACCESS_PROJECTS,Boolean.TRUE);
            }else{
                access.put(I_OXUser.ACCESS_PROJECTS,Boolean.FALSE);
            }
            if(user.hasRSSBookmarks()){
                access.put(I_OXUser.ACCESS_RSS_BOOKMARKS,Boolean.TRUE);
            }else{
                access.put(I_OXUser.ACCESS_RSS_BOOKMARKS,Boolean.FALSE);
            }
            if(user.hasRSSPortal()){
                access.put(I_OXUser.ACCESS_RSS_PORTAL,Boolean.TRUE);
            }else{
                access.put(I_OXUser.ACCESS_RSS_PORTAL,Boolean.FALSE);
            }
            if(user.hasSyncML()){
                access.put(I_OXUser.ACCESS_SYNCML,Boolean.TRUE);
            }else{
                access.put(I_OXUser.ACCESS_SYNCML,Boolean.FALSE);
            }
            if(user.hasTask()){
                access.put(I_OXUser.ACCESS_TASKS,Boolean.TRUE);
            }else{
                access.put(I_OXUser.ACCESS_TASKS,Boolean.FALSE);
            }
            if(user.hasVCard()){
                access.put(I_OXUser.ACCESS_VCARD,Boolean.TRUE);
            }else{
                access.put(I_OXUser.ACCESS_VCARD,Boolean.FALSE);
            }
            if(user.hasWebDAV()){
                access.put(I_OXUser.ACCESS_WEBDAV,Boolean.TRUE);
            }else{
                access.put(I_OXUser.ACCESS_WEBDAV,Boolean.FALSE);
            }
            if(user.hasWebDAVXML()){
                access.put(I_OXUser.ACCESS_WEBDAV_XML,Boolean.TRUE);
            }else{
                access.put(I_OXUser.ACCESS_WEBDAV_XML,Boolean.FALSE);
            }
            if(user.hasWebMail()){
                access.put(I_OXUser.ACCESS_WEBMAIL,Boolean.TRUE);
            }else{
                access.put(I_OXUser.ACCESS_WEBMAIL,Boolean.FALSE);
            }
            if(user.canDelegateTasks()){
                access.put(I_OXUser.ACCESS_DELEGATE_TASKS,Boolean.TRUE);
            }else{
                access.put(I_OXUser.ACCESS_DELEGATE_TASKS,Boolean.FALSE);
            }
            v.add("OK");
            v.add(access);
        }finally{
            try{
                if(read_ox_con!=null){
                    cache.pushOXDBRead(context_id,read_ox_con);
                }
            }catch(Exception exp){
                log.error("Error pushing ox read connection to pool!",exp);
            }
        }
        return v;
    }
    
    private Vector<String> myChangeInsertModuleAccess(int context_id, int user_id, Hashtable moduleAccess,boolean insert_or_update,Connection read_ox_con,Connection write_ox_con,int[] groups)
    throws SQLException, DBPoolingException{
        
        Vector<String> v = new Vector<String>();
        
        UserConfiguration user = UserConfiguration.loadUserConfiguration(user_id,groups,context_id,read_ox_con);
        if(moduleAccess!=null){            
            if(moduleAccess.containsKey(I_OXUser.ACCESS_CALENDAR)){
                user.setCalendar(Boolean.valueOf(""+moduleAccess.get(I_OXUser.ACCESS_CALENDAR)).booleanValue());                
            }else{
                user.setCalendar(false);
            }
            if(moduleAccess.containsKey(I_OXUser.ACCESS_CONTACTS)){
                user.setContact(Boolean.valueOf(""+moduleAccess.get(I_OXUser.ACCESS_CONTACTS)).booleanValue());                
            }else{
                user.setContact(false);
            }
            if(moduleAccess.containsKey(I_OXUser.ACCESS_FORUM)){
                user.setForum(Boolean.valueOf(""+moduleAccess.get(I_OXUser.ACCESS_FORUM)).booleanValue());                
            }else{
                user.setForum(false);
            }
            if(moduleAccess.containsKey(I_OXUser.ACCESS_EDIT_PUBLIC_FOLDERS)){
                user.setFullPublicFolderAccess(Boolean.valueOf(""+moduleAccess.get(I_OXUser.ACCESS_EDIT_PUBLIC_FOLDERS)).booleanValue());                
            }else{
                user.setFullPublicFolderAccess(false);
            }
            if(moduleAccess.containsKey(I_OXUser.ACCESS_READ_CREATE_SHARED_FOLDERS)){
                user.setFullSharedFolderAccess(Boolean.valueOf(""+moduleAccess.get(I_OXUser.ACCESS_READ_CREATE_SHARED_FOLDERS)).booleanValue());
            }else{
                user.setFullSharedFolderAccess(false);
            }
            if(moduleAccess.containsKey(I_OXUser.ACCESS_ICAL)){
                user.setICal(Boolean.valueOf(""+moduleAccess.get(I_OXUser.ACCESS_ICAL)).booleanValue());
            }else{
                user.setICal(false);
            }
            if(moduleAccess.containsKey(I_OXUser.ACCESS_INFOSSTORE)){
                user.setInfostore(Boolean.valueOf(""+moduleAccess.get(I_OXUser.ACCESS_INFOSSTORE)).booleanValue());
            }else{
                user.setInfostore(false);
            }
            if(moduleAccess.containsKey(I_OXUser.ACCESS_PINBOARD_WRITE_ACCESS)){
                user.setPinboardWriteAccess(Boolean.valueOf(""+moduleAccess.get(I_OXUser.ACCESS_PINBOARD_WRITE_ACCESS)).booleanValue());
            }else{
                user.setPinboardWriteAccess(false);
            }
            if(moduleAccess.containsKey(I_OXUser.ACCESS_PROJECTS)){
                user.setProject(Boolean.valueOf(""+moduleAccess.get(I_OXUser.ACCESS_PROJECTS)).booleanValue());
            }else{
                user.setProject(false);
            }
            if(moduleAccess.containsKey(I_OXUser.ACCESS_RSS_BOOKMARKS)){
                user.setRSSBookmarks(Boolean.valueOf(""+moduleAccess.get(I_OXUser.ACCESS_RSS_BOOKMARKS)).booleanValue());
            }else{
                user.setRSSBookmarks(false);
            }
            if(moduleAccess.containsKey(I_OXUser.ACCESS_RSS_PORTAL)){
                user.setRSSPortal(Boolean.valueOf(""+moduleAccess.get(I_OXUser.ACCESS_RSS_PORTAL)).booleanValue());
            }else{
                user.setRSSPortal(false);
            }
            if(moduleAccess.containsKey(I_OXUser.ACCESS_SYNCML)){
                user.setSyncML(Boolean.valueOf(""+moduleAccess.get(I_OXUser.ACCESS_SYNCML)).booleanValue());
            }else{
                user.setSyncML(false);
            }
            if(moduleAccess.containsKey(I_OXUser.ACCESS_TASKS)){
                user.setTask(Boolean.valueOf(""+moduleAccess.get(I_OXUser.ACCESS_TASKS)).booleanValue());
            }else{
                user.setTask(false);
            }
            if(moduleAccess.containsKey(I_OXUser.ACCESS_VCARD)){
                user.setVCard(Boolean.valueOf(""+moduleAccess.get(I_OXUser.ACCESS_VCARD)).booleanValue());
            }else{
                user.setVCard(false);
            }
            if(moduleAccess.containsKey(I_OXUser.ACCESS_WEBDAV)){
                user.setWebDAV(Boolean.valueOf(""+moduleAccess.get(I_OXUser.ACCESS_WEBDAV)).booleanValue());
            }else{
                user.setWebDAV(false);
            }
            if(moduleAccess.containsKey(I_OXUser.ACCESS_WEBDAV_XML)){
                user.setWebDAVXML(Boolean.valueOf(""+moduleAccess.get(I_OXUser.ACCESS_WEBDAV_XML)).booleanValue());
            }else{
                user.setWebDAVXML(false);
            }
            if(moduleAccess.containsKey(I_OXUser.ACCESS_WEBMAIL)){
                user.setWebMail(Boolean.valueOf(""+moduleAccess.get(I_OXUser.ACCESS_WEBMAIL)).booleanValue());
            }else{
                user.setWebMail(false);
            }
            if(moduleAccess.containsKey(I_OXUser.ACCESS_DELEGATE_TASKS)){
                user.setDelegateTasks(Boolean.valueOf(""+moduleAccess.get(I_OXUser.ACCESS_DELEGATE_TASKS)).booleanValue());
            }else{
                user.setDelegateTasks(false);
            }
        }else{
            user.setCalendar(false);
            user.setContact(false);
            user.setDelegateTasks(false);
            user.setForum(false);
            user.setFullPublicFolderAccess(false);
            user.setFullSharedFolderAccess(false);
            user.setICal(false);
            user.setInfostore(false);
            user.setPinboardWriteAccess(false);
            user.setProject(false);
            user.setRSSBookmarks(false);
            user.setRSSPortal(false);
            user.setSyncML(false);
            user.setTask(false);
            user.setVCard(false);
            user.setWebDAV(false);
            user.setWebDAVXML(false);
            user.setWebMail(false);
        }
        
        UserConfiguration.saveUserConfiguration(user,insert_or_update,write_ox_con);
        v.add( "OK" );
        
        return v;
    }
    
    
    public Vector<String> changeUserModuleAccess( int context_id, int user_id, Hashtable moduleAccess ) throws PoolException, SQLException, DBPoolingException {
        Vector<String> v = new Vector<String>();
        
        Connection read_ox_con = null;
        Connection write_ox_con = null;
        
        try {
            read_ox_con = cache.getREADConnectionForContext(context_id);
            write_ox_con = cache.getWRITEConnectionForContext(context_id);
            
            // first get all groups the user is in
            int [] all_groups = getGroupsForUser(context_id,user_id,read_ox_con);
            // update last modified column
            changeLastModifiedOnUser(user_id,context_id,write_ox_con);
            v =  myChangeInsertModuleAccess(context_id,user_id,moduleAccess,false,read_ox_con,write_ox_con,all_groups);
        }finally{
            try{
                if(read_ox_con!=null){
                    cache.pushOXDBRead(context_id,read_ox_con);
                }
            }catch(Exception exp){
                log.error("Error pushing ox read connection to pool!",exp);
            }
            try{
                if(write_ox_con!=null){
                    cache.pushOXDBWrite(context_id,write_ox_con);
                }
            }catch(Exception exp){
                log.error("Error pushing ox write connection to pool!",exp);
            }
        }
        
        return v;
        
    }
    
    
    public Vector<Object> getUserData(int context_id, String uid) throws PoolException, SQLException {
        Vector<Object> v = new Vector<Object>();
        Connection read_ox_con = null;
        PreparedStatement prep_check = null;
        // get id of user via login2user table
        try{
            read_ox_con = cache.getREADConnectionForContext(context_id);
            prep_check = read_ox_con.prepareStatement( "SELECT id FROM login2user WHERE cid = ? AND uid = ?" );
            prep_check.setInt( 1, context_id );
            prep_check.setString( 2, uid );
            ResultSet rs = prep_check.executeQuery();
            if(rs.next()){
                v = getUserData(context_id,rs.getInt("id"));
            }
        }finally{
            try {
                prep_check.close();
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
            try{
                if(read_ox_con!=null){
                    cache.pushOXDBRead(context_id,read_ox_con);
                }
            }catch(Exception exp){
                log.error("Error pushing ox read connection to pool!",exp);
            }
        }
        return v;
    }
    
    
    public Vector<Object> getUserData(int context_id, int user_id) throws SQLException, PoolException {
        Vector<Object> retValue = new Vector<Object>();
        
        String [] DB_FIELDS = {
            "user.cid",
            "user.smtpserver",
            "user.imapserver",
            "user.id",
            "user.timezone",
            "user.preferredLanguage",
            "user.mail",
            "user.mailEnabled",
            "user.shadowLastChange",
            "login2user.uid"
        };
        String [] API_FIELDS = {
            I_OXContext.CONTEXT_ID,
            I_OXUser.SMTP_SERVER,
            I_OXUser.IMAP_SERVER,
            I_OXUser.UID_NUMBER,
            I_OXUser.TIMEZONE,
            I_OXUser.LANGUAGE,
            I_OXUser.PRIMARY_MAIL,
            I_OXUser.ENABLED,
            I_OXUser.PASSWORD_EXPIRED,
            I_OXUser.UID
        };
        
        StringBuffer query = new StringBuffer();
        query.append("SELECT ");
        for(int a = 0;a<DB_FIELDS.length;a++){
            query.append(DB_FIELDS[a]+",");
        }
        
        for(int a = 0;a<CONTACT_FIELDS.length;a++){
            int cfield = CONTACT_HT.get(CONTACT_FIELDS[a]);
            if( Contacts.mapping[cfield] != null ) {
                String field_name = Contacts.mapping[cfield].getDBFieldName();
                if(a==CONTACT_FIELDS.length-1){
                    query.append(field_name);
                }else{
                    query.append(field_name+",");
                }
            }
        }
        query.append(" FROM user JOIN login2user USING (cid,id) " +
                " JOIN prg_contacts " +
                " ON (user.cid=prg_contacts.cid " +
                " AND user.id=prg_contacts.userid) ");
        query.append("WHERE user.id = ? ");
        query.append("AND user.cid = ? ");
        
        Connection read_ox_con = null;
        PreparedStatement stmt = null;
        try {
            Hashtable<String, Object> user = new Hashtable<String, Object>();
            read_ox_con = cache.getREADConnectionForContext(context_id);
            stmt = read_ox_con.prepareStatement(query.toString());
            stmt.setInt(1,user_id);
            stmt.setInt(2,context_id);
            ResultSet rs3 = stmt.executeQuery();
            if(rs3.next()){
                int count = 0;
                for(count = 0; count<DB_FIELDS.length; count++){
                    if(rs3.getString(count+1)!=null){
                        if( API_FIELDS[count].equals(I_OXUser.ENABLED) ){
                            user.put(API_FIELDS[count],rs3.getBoolean(count+1));
                        } else if(API_FIELDS[count].equals(I_OXUser.PASSWORD_EXPIRED) ) {
                            int expired = rs3.getInt(count+1);
                            if( expired == 0 ) {
                                user.put(I_OXUser.PASSWORD_EXPIRED, true);
                            } else {
                                user.put(I_OXUser.PASSWORD_EXPIRED, false);
                            }
                        } else if(API_FIELDS[count].equals(I_OXUser.UID_NUMBER) ) {
                            user.put(API_FIELDS[count],new Integer(rs3.getInt(count+1)));
                        } else {
                            user.put(API_FIELDS[count],rs3.getString(count+1));
                        }
                    }
                }
                for(int c = 0; c<CONTACT_FIELDS.length; c++){
                    String val = rs3.getString(count+c+1);
                    if(val != null ){
                        int cfield = CONTACT_HT.get(CONTACT_FIELDS[c]);
                        if(CONTACT_FIELDS[c].equals(I_OXUser.BIRTHDAY)||
                                CONTACT_FIELDS[c].equals(I_OXUser.ANNIVERSARY)) {
                            user.put(CONTACT_FIELDS[c],
                                    (java.util.Date)Contacts.mapping[cfield].getData(rs3, count+c+1));
                        } else {
                            user.put(CONTACT_FIELDS[c],
                                    (String)Contacts.mapping[cfield].getData(rs3, count+c+1));
                        }
                    }
                }
            }
            rs3.close();
            stmt.close();
            
            
            
            stmt = read_ox_con.prepareStatement("SELECT value FROM user_attribute WHERE cid = ? and id = ? AND name = \"" +
                    I_OXUser.ALIAS + "\"");
            stmt.setInt(1, context_id);
            stmt.setInt(2, user_id);
            rs3 = stmt.executeQuery();
            ArrayList<String> a = new ArrayList<String>();
            while( rs3.next() ) {
                a.add(rs3.getString("value"));
            }
            String aliases[] = new String[a.size()];
            for(int i=0; i<a.size(); i++) {
                aliases[i] = a.get(i);
            }
            user.put(I_OXUser.ALIAS, aliases);
            rs3.close();
            stmt.close();
            
            stmt = read_ox_con.prepareStatement("SELECT std_trash,std_sent,std_drafts,std_spam FROM user_setting_mail WHERE cid = ? and user = ?");
            stmt.setInt(1, context_id);
            stmt.setInt(2, user_id);
            rs3 = stmt.executeQuery();
            if( rs3.next() ) {
                user.put(I_OXUser.MAIL_FOLDER_DRAFTS, rs3.getString("std_drafts"));
                user.put(I_OXUser.MAIL_FOLDER_SENT,   rs3.getString("std_sent"));
                user.put(I_OXUser.MAIL_FOLDER_SPAM,   rs3.getString("std_spam"));
                user.put(I_OXUser.MAIL_FOLDER_TRASH,  rs3.getString("std_trash"));
            }
            rs3.close();
            stmt.close();
            
            retValue.add( "OK" );
            retValue.add(user);
        }finally{
            try {
                if(stmt!=null){
                    stmt.close();
                }
            } catch (Exception e) {
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,e);
            }
            try{
                cache.pushOXDBRead(context_id,read_ox_con);
            }catch(Exception exp){
                log.error("Error pushing ox read connection to pool!",exp);
            }
        }
        
        return retValue;
    }
    
    
    public Vector<String> changeUserData(int context_id, int user_id, Hashtable user_data) throws SQLException, PoolException {
        
        Vector<String> retValue = new Vector<String>();
        Connection write_ox_con = null;
        PreparedStatement stmt = null;
        PreparedStatement folder_update = null;
        try {
            
            Vector<String> v = new Vector<String>();
            
            // first fill the user_data hash to update user table
            if(user_data.containsKey(I_OXUser.PRIMARY_MAIL)){
                v.add(I_OXUser.PRIMARY_MAIL);
            }
            if(user_data.containsKey(I_OXUser.LANGUAGE)){
                v.add(I_OXUser.LANGUAGE);
            }
            if(user_data.containsKey(I_OXUser.TIMEZONE)){
                v.add(I_OXUser.TIMEZONE);
            }
            
            if(user_data.containsKey(I_OXUser.ENABLED)){
                v.add(I_OXUser.ENABLED);
            }
            
            if(user_data.containsKey(I_OXUser.PASSWORD_EXPIRED)){
                v.add(I_OXUser.PASSWORD_EXPIRED);
            }
            
            if(user_data.containsKey(I_OXUser.IMAP_SERVER)){
                v.add(I_OXUser.IMAP_SERVER);
            }
            
            if(user_data.containsKey(I_OXUser.SMTP_SERVER)){
                v.add(I_OXUser.SMTP_SERVER);
            }
            
            write_ox_con = cache.getWRITEConnectionForContext(context_id);
            write_ox_con.setAutoCommit(false);
            
            // fill up statement for user data update
            if(v.size()>0){
                StringBuffer sb = new StringBuffer();
                sb.append("UPDATE ");
                sb.append("user ");
                sb.append("SET ");
                for(int i = 0;i<v.size();i++){
                    if(i==v.size()-1){
                        sb.append(v.get(i)+" = ? ");
                    }else{
                        sb.append(v.get(i)+" = ?, ");
                    }
                }
                sb.append("WHERE ");
                sb.append("cid = ? ");
                sb.append("AND id = ? ");
                
                stmt = write_ox_con.prepareStatement(sb.toString());
                for(int i = 0;i<v.size();i++){
                    if(((String)v.get(i)).equals(I_OXUser.ENABLED)){
                        Boolean enabled = Boolean.TRUE;
                        try{
                            enabled = (Boolean)user_data.get(v.get(i));
                        }catch(Exception exp){
                            log.error("error in data",exp);
                            
                        }
                        stmt.setBoolean(i+1,enabled);
                    }else if(((String)v.get(i)).equals(I_OXUser.PASSWORD_EXPIRED)){
                        int password_expired = -1; // not expired
                        try{
                            Boolean b = (Boolean)user_data.get(v.get(i));
                            if(b){
                                password_expired = 0;
                            }
                            stmt.setInt(i+1,password_expired);
                        }catch(Exception ep){
                            stmt.setString(i+1,""+user_data.get(""+v.get(i)));
                        }
                    }else{
                        stmt.setString(i+1,""+user_data.get(""+v.get(i)));
                    }
                    
                }
                stmt.setInt(v.size()+1,context_id);
                stmt.setInt(v.size()+2,user_id);
                stmt.executeUpdate();
                stmt.close();
                
            }
            
            if( user_data.containsKey(I_OXUser.ALIAS)) {
                stmt = write_ox_con.prepareStatement("DELETE FROM user_attribute WHERE cid = ? AND id = ?" +
                        " AND name = \"" + I_OXUser.ALIAS + "\"");
                stmt.setInt(1,context_id);
                stmt.setInt(2,user_id);
                stmt.executeUpdate();
                stmt.close();
                String aliases[] = (String[])user_data.get(I_OXUser.ALIAS);
                for(int a=0; a<aliases.length; a++) {
                    stmt = write_ox_con.prepareStatement("INSERT INTO user_attribute (cid,id,name,value) VALUES (?,?,?,?)");
                    stmt.setInt(1,context_id);
                    stmt.setInt(2,user_id);
                    stmt.setString(3, I_OXUser.ALIAS);
                    stmt.setString(4, aliases[a]);
                    stmt.executeUpdate();
                    stmt.close();
                    
                }
            }
            
            
            boolean contacts_changed = false;
            String contact_query = "UPDATE prg_contacts SET ";
            // fill up statement for prg_contacts update
            for(int f=0; f<CONTACT_FIELDS.length; f++) {
                if( user_data.containsKey(CONTACT_FIELDS[f]) ) {
                    if( contacts_changed ) {
                        contact_query += ", ";
                    }
                    contacts_changed = true;
                    int cfield = CONTACT_HT.get(CONTACT_FIELDS[f]);
                    contact_query += Contacts.mapping[cfield].getDBFieldName() + " = ?";
                }
            }
            contact_query += " WHERE cid = ? AND userid = ?";
            
            if(contacts_changed){
                
                stmt = write_ox_con.prepareStatement(contact_query);
                int count = 1;
                
                for(int f=0; f<CONTACT_FIELDS.length; f++) {
                    if( user_data.containsKey(CONTACT_FIELDS[f]) ) {
                        int cfield = CONTACT_HT.get(CONTACT_FIELDS[f]);
                        if( CONTACT_FIELDS[f].equals(I_OXUser.ANNIVERSARY) ||
                                CONTACT_FIELDS[f].equals(I_OXUser.BIRTHDAY) ) {
                            Contacts.mapping[cfield].fillPreparedStatement(stmt, count++, new java.sql.Date(((java.util.Date)user_data.get(CONTACT_FIELDS[f])).getTime()) );
                        } else {
                            Contacts.mapping[cfield].fillPreparedStatement(stmt, count++, user_data.get(CONTACT_FIELDS[f]));
                        }
                        log.debug("*******************  " + user_data.get(CONTACT_FIELDS[f]).toString() +
                                " / " + Contacts.mapping[cfield].getDBFieldName() +
                                " / " + cfield);
                    }
                }
                
                stmt.setInt(count++,context_id);
                stmt.setInt(count++,user_id);
                stmt.executeUpdate();
                stmt.close();
                
            }
            
            
            // update the mailfolder mapping
            
            if(user_data.containsKey(I_OXUser.MAIL_FOLDER_DRAFTS)){
                folder_update = write_ox_con.prepareStatement("UPDATE user_setting_mail SET std_drafts = ? WHERE cid = ? AND user = ?");
                folder_update.setString(1,user_data.get(I_OXUser.MAIL_FOLDER_DRAFTS).toString());
                folder_update.setInt(2, context_id);
                folder_update.setInt(3, user_id);
                folder_update.executeUpdate();
                folder_update.close();
            }
            if(user_data.containsKey(I_OXUser.MAIL_FOLDER_SENT)){
                folder_update = write_ox_con.prepareStatement("UPDATE user_setting_mail SET std_sent = ? WHERE cid = ? AND user = ?");
                folder_update.setString(1,user_data.get(I_OXUser.MAIL_FOLDER_SENT).toString());
                folder_update.setInt(2, context_id);
                folder_update.setInt(3, user_id);
                folder_update.executeUpdate();
                folder_update.close();
            }
            if(user_data.containsKey(I_OXUser.MAIL_FOLDER_SPAM)){
                folder_update = write_ox_con.prepareStatement("UPDATE user_setting_mail SET std_spam = ? WHERE cid = ? AND user = ?");
                folder_update.setString(1,user_data.get(I_OXUser.MAIL_FOLDER_SPAM).toString());
                folder_update.setInt(2, context_id);
                folder_update.setInt(3, user_id);
                folder_update.executeUpdate();
                folder_update.close();
            }
            if(user_data.containsKey(I_OXUser.MAIL_FOLDER_TRASH)){
                folder_update = write_ox_con.prepareStatement("UPDATE user_setting_mail SET std_trash = ? WHERE cid = ? AND user = ?");
                folder_update.setString(1,user_data.get(I_OXUser.MAIL_FOLDER_TRASH).toString());
                folder_update.setInt(2, context_id);
                folder_update.setInt(3, user_id);
                folder_update.executeUpdate();
                folder_update.close();
            }
            
            if(folder_update!=null){
                folder_update.close();
            }
            
            
            // update last modified column
            changeLastModifiedOnUser(user_id,context_id,write_ox_con);
            
            // fire up
            write_ox_con.commit();
            retValue.add( "OK" );
        } catch (SQLException e ) {
            log.error("Error processing changeOXResource",e);
            try{
                write_ox_con.rollback();
            }catch(Exception e2){
                log.error("Error doing rollback",e2);
            }
            throw e;
        }finally{
            try {
                if(folder_update!=null){
                    folder_update.close();
                }
            } catch (Exception e) {
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,e);
            }
            try{
                if(stmt!=null){
                    stmt.close();
                }
            }catch(Exception e){
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,e);
            }
            
            try{
                if(write_ox_con!=null){
                    cache.pushOXDBWrite(context_id,write_ox_con);
                }
            }catch(Exception exp){
                log.error("Error pushing ox write connection to pool!",exp);
            }
        }
        
        return retValue;
    }
    
    public static void changeLastModifiedOnUser(int user_id,int context_id,Connection write_ox_con) throws SQLException{
        PreparedStatement prep_edit_user = null;
        try {
            prep_edit_user = write_ox_con.prepareStatement( "UPDATE prg_contacts SET changing_date=? WHERE cid=? AND userid=?;" );
            prep_edit_user.setLong(1,System.currentTimeMillis());
            prep_edit_user.setInt(2,context_id);
            prep_edit_user.setInt(3,user_id);
            prep_edit_user.executeUpdate();
        }finally{
            try {
                if(prep_edit_user!=null){
                    prep_edit_user.close();
                }
            } catch (Exception ex) {
                log.error("Error closing statement!",ex);
            }
        }
    }
    
    private static String makeDBPasswd(String raw) throws NoSuchAlgorithmException {
        MessageDigest md;
        
        md = MessageDigest.getInstance("SHA-1");
        
        byte[] salt = {};
        
        md.reset();
        try {
            md.update( raw.getBytes( "UTF-8" ) );
        } catch( UnsupportedEncodingException e ) {
            log.error("Error Encoding Password",e);
            
        }
        md.update( salt );
        
        byte[] pwhash = md.digest();
        String ret = ( new sun.misc.BASE64Encoder().encode( pwhash ) );
        
        return ret;
        
    }
    
    public int createUser(int context_id, Hashtable userData, Hashtable module_access,Connection write_ox_con,int internal_user_id,int contact_id) throws SQLException, NoSuchAlgorithmException, DBPoolingException, OXException{
        // this method is needed for rollback at context creation
        PreparedStatement ps = null;
        PreparedStatement return_db_id = null;
        
        try{
            
            String new_user = userData.get(I_OXUser.UID).toString();
            
            
            ps = write_ox_con.prepareStatement("SELECT user FROM user_setting_admin WHERE cid=?");
            ps.setInt(1, context_id);
            ResultSet rs = ps.executeQuery();
            int admin_id = 0;
            boolean mustMapAdmin = false;
            if(rs.next()) {
                admin_id = rs.getInt("user");
            } else {
                admin_id = internal_user_id;
                mustMapAdmin = true;
            }
            rs.close();
            
            // get next user id
            // int user_id = cache.getNextUserID(context_id);
            
            String username = userData.get(I_OXUser.UID).toString();
            String passwd = makeDBPasswd(userData.get(I_OXUser.PASSWORD).toString());
            
            //write_ox_con.setAutoCommit(false);
            PreparedStatement stmt = null;
            try{
                
                stmt = write_ox_con.prepareStatement("INSERT INTO user (cid,id,userPassword,shadowLastChange,mail,timeZone,preferredLanguage,mailEnabled,imapserver,smtpserver,contactId) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
                stmt.setInt(1,context_id);
                stmt.setInt(2,internal_user_id);
                stmt.setString(3,passwd);
                
                if(userData.containsKey(I_OXUser.PASSWORD_EXPIRED)){
                    int password_expired = -1; // not expired
                    try{
                        Boolean b = (Boolean)userData.get(I_OXUser.PASSWORD_EXPIRED);
                        if(b){
                            password_expired = 0;
                        }
                        stmt.setInt(4,password_expired);
                    }catch(Exception ep){
                        stmt.setInt(4,-1);
                    }
                }else{
                    stmt.setInt(4,-1);
                }
                
                stmt.setString(5,(String)userData.get(I_OXUser.PRIMARY_MAIL));
                
                if(userData.containsKey(I_OXUser.TIMEZONE)){
                    stmt.setString(6,(String)userData.get(I_OXUser.TIMEZONE));
                }else{
                    stmt.setString(6,"Europe/Berlin");
                }
                
                String lang = OXUser.getLanguage(userData);
                stmt.setString(7,lang);
                
                // mailenabled
                Boolean enabled = Boolean.TRUE;
                if(userData.containsKey(I_OXUser.ENABLED)){
                    try{
                        enabled = (Boolean)userData.get(I_OXUser.ENABLED);
                    }catch(Exception exp){
                        log.error("invalid user data",exp);
                    }
                    stmt.setBoolean(8,enabled);
                }else{
                    stmt.setInt(8,1);
                }
                
                
                // imap and smtp server
                String def_imap = "localhost";
                if(userData.containsKey(I_OXUser.IMAP_SERVER)){
                    def_imap = (String)userData.get(I_OXUser.IMAP_SERVER);
                }
                stmt.setString(9,def_imap);
                String def_smtp = "localhost";
                if(userData.containsKey(I_OXUser.SMTP_SERVER)){
                    def_smtp = (String)userData.get(I_OXUser.SMTP_SERVER);
                }
                stmt.setString(10,def_smtp);
                //Context ctx = new ContextImpl(context_id);
                //int contact_id = SqlHandler.getNextContactID(context_id);
                stmt.setInt(11,contact_id);
                stmt.executeUpdate();
                stmt.close();
                
                // we MUST set a display name, OXUser.getDisplayName is doing that
                // if not already done, it adds that field to userData
                String display_name = OXUser.getDisplayName(userData);
                
                String contact_query = "INSERT INTO prg_contacts (cid,userid,creating_date,created_from,changing_date,";
                String values = "VALUES (?,?,?,?,?,";
                for(int f=0; f<CONTACT_FIELDS.length; f++) {
                    if( userData.containsKey(CONTACT_FIELDS[f]) ) {
                        int cfield = CONTACT_HT.get(CONTACT_FIELDS[f]);
                        values += "?,";
                        contact_query += Contacts.mapping[cfield].getDBFieldName() + ",";
                    }
                }
                
                values += "?,6)";
                contact_query += Contacts.mapping[ContactObject.OBJECT_ID].getDBFieldName() + ",fid)";
                
                String complete_query = contact_query + " " + values;
                // insert into prg_contacts the contact informatin of the user
                stmt = write_ox_con.prepareStatement(complete_query);
                stmt.setInt(1,context_id);
                stmt.setInt(2,internal_user_id);
                stmt.setLong(3,System.currentTimeMillis());
                stmt.setLong(4, admin_id);
                stmt.setLong(5,System.currentTimeMillis());
                
                int count = 6;
                for(int f=0; f<CONTACT_FIELDS.length; f++) {
                    if( userData.containsKey(CONTACT_FIELDS[f]) ) {
                        int cfield = CONTACT_HT.get(CONTACT_FIELDS[f]);
                        if( CONTACT_FIELDS[f].equals(I_OXUser.ANNIVERSARY) ||
                                CONTACT_FIELDS[f].equals(I_OXUser.BIRTHDAY) ) {
                            Contacts.mapping[cfield].fillPreparedStatement(stmt, count++, new java.sql.Date(((java.util.Date)userData.get(CONTACT_FIELDS[f])).getTime()) );
                        } else {
                            Contacts.mapping[cfield].fillPreparedStatement(stmt, count++, userData.get(CONTACT_FIELDS[f]));
                        }
                        log.debug("*******************  " + userData.get(CONTACT_FIELDS[f]).toString() +
                                " / " + Contacts.mapping[cfield].getDBFieldName() +
                                " / " + cfield);
                    }
                }
                
                // contact_id == intfield01, == last element
                stmt.setInt(count,contact_id);
                stmt.executeUpdate();
                stmt.close();
                
                // get mailfolder
                String std_mail_folder_sent = prop.getUserProp("SENT_MAILFOLDER_"+lang.toUpperCase(),"Sent");
                if(userData.containsKey(I_OXUser.MAIL_FOLDER_SENT)){
                    std_mail_folder_sent = (String)userData.get(I_OXUser.MAIL_FOLDER_SENT);
                }
                
                String std_mail_folder_trash = prop.getUserProp("TRASH_MAILFOLDER_"+lang.toUpperCase(),"Trash");
                if(userData.containsKey(I_OXUser.MAIL_FOLDER_TRASH)){
                    std_mail_folder_trash = (String)userData.get(I_OXUser.MAIL_FOLDER_TRASH);
                }
                
                String std_mail_folder_drafts = prop.getUserProp("DRAFTS_MAILFOLDER_"+lang.toUpperCase(),"Drafts");
                if(userData.containsKey(I_OXUser.MAIL_FOLDER_DRAFTS)){
                    std_mail_folder_drafts = (String)userData.get(I_OXUser.MAIL_FOLDER_DRAFTS);
                }
                
                String std_mail_folder_spam = prop.getUserProp("SPAM_MAILFOLDER_"+lang.toUpperCase(),"Spam");
                if(userData.containsKey(I_OXUser.MAIL_FOLDER_SPAM)){
                    std_mail_folder_spam = (String)userData.get(I_OXUser.MAIL_FOLDER_SPAM);
                }
                
                
                
                // insert all multi valued attribs to the user_attribute table,
                // here we fill the alias attribute in it
                String aliases[] = (String[])userData.get(I_OXUser.ALIAS);
                if(aliases!=null){
                    for(int a = 0; a < aliases.length;a++){
                        stmt = write_ox_con.prepareStatement("INSERT INTO user_attribute (cid,id,name,value) VALUES (?,?,?,?)");
                        stmt.setInt(1,context_id);
                        stmt.setInt(2,internal_user_id);
                        stmt.setString(3,I_OXUser.ALIAS);
                        stmt.setString(4,aliases[a]);
                        stmt.executeUpdate();
                        stmt.close();
                    }
                }
                
                
                // add user to login2user table with the internal id
                stmt = write_ox_con.prepareStatement("INSERT INTO login2user (cid,id,uid) VALUES (?,?,?)");
                stmt.setInt(1,context_id);
                stmt.setInt(2,internal_user_id);
                stmt.setString(3,username);
                stmt.executeUpdate();
                stmt.close();
                
                // add user to group members table of his group
                long def_group_id = AdminDaemonTools.getDefaultGroupForContext(context_id, write_ox_con);
                if(userData.containsKey(I_OXUser.DEFAULT_GROUP)){
                    def_group_id = Long.parseLong(""+userData.get(I_OXUser.DEFAULT_GROUP));
                }
                stmt = write_ox_con.prepareStatement("INSERT INTO groups_member (cid,id,member) VALUES (?,?,?)");
                stmt.setInt(1,context_id);
                stmt.setLong(2,def_group_id);
                stmt.setInt(3,internal_user_id);
                stmt.executeUpdate();
                stmt.close();
                
                if(mustMapAdmin) {
                    stmt = write_ox_con.prepareStatement("INSERT INTO user_setting_admin (cid,user) VALUES (?,?)");
                    stmt.setInt(1,context_id);
                    stmt.setInt(2,admin_id);
                    stmt.executeUpdate();
                    stmt.close();
                }
                
                // add the module access rights to the db
                int[] all_groups = getGroupsForUser(context_id,internal_user_id,write_ox_con);
                
                
                myChangeInsertModuleAccess(context_id,internal_user_id,module_access,true,write_ox_con,write_ox_con,all_groups);
                
                // add users standard mail settings                   
                stmt = write_ox_con.prepareStatement("INSERT INTO user_setting_mail (cid,user,std_trash,std_sent,std_drafts,std_spam,send_addr,bits) VALUES (?,?,?,?,?,?,?,?)");
                stmt.setInt(1,context_id);
                stmt.setInt(2,internal_user_id);
                stmt.setString(3,std_mail_folder_trash);
                stmt.setString(4,std_mail_folder_sent);
                stmt.setString(5,std_mail_folder_drafts);
                stmt.setString(6,std_mail_folder_spam);
                stmt.setString(7,(String)userData.get(I_OXUser.PRIMARY_MAIL));
                // set the flag for "receiving notifications" in the ox, was bug #5336
                stmt.setInt(8,768);
                stmt.executeUpdate();
                stmt.close();
                
                
                // only when user is NOT the admin user, then invoke the ox api colubrid directly, else
                // a context is currently in creation and we would get an error by the ox api
                if( internal_user_id != admin_id ) {                    
                    OXFolderAction oxa = new OXFolderAction();
                    // hier muss jenachdem was der user nun hat also premium oder standard anderer wert an thorebn gegeben werden
                    oxa.addUserToOXFolders(internal_user_id,display_name,lang,context_id,write_ox_con);
                }
                
            }finally{
                try {
                    if(stmt!=null){
                        stmt.close();
                    }
                } catch (Exception e) {
                    log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,e);
                }
            }
            
            // return the client the id to work with the user in the system
            return_db_id = write_ox_con.prepareStatement("SELECT id FROM login2user WHERE cid = ? AND uid = ?");
            return_db_id.setInt(1,context_id);
            return_db_id.setString(2,new_user);
            rs = return_db_id.executeQuery();
            int id_for_client = -1;
            if(rs.next()){
                id_for_client = rs.getInt("id");
            }
            log.info("User "+id_for_client+" created!");
            return id_for_client;
            
        }finally{
            try {
                if(return_db_id!=null){
                    return_db_id.close();
                }
            } catch (Exception e) {
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,e);
            }
            try {
                if(ps!=null){
                    ps.close();
                }
            } catch (Exception e) {
                log.error(OXContext_MySQL.LOG_ERROR_CLOSING_STATEMENT,e);
            }
            
            
            
        }
    }
    
    public Vector<Object> createUser(int context_id, Hashtable userData, Hashtable module_access)
    throws PoolException, SQLException, DBPoolingException, OXException, NoSuchAlgorithmException {
        Vector<Object> v = new Vector<Object>();
        Connection write_ox_con = null;
        try{
            write_ox_con  = cache.getWRITEConnectionForContext(context_id);
            write_ox_con.setAutoCommit(false);
            
            int internal_user_id = IDGenerator.getId(context_id,com.openexchange.groupware.Types.PRINCIPAL,write_ox_con);
            write_ox_con.commit();
            
            int contact_id = IDGenerator.getId(context_id,com.openexchange.groupware.Types.CONTACT,write_ox_con);
            write_ox_con.commit();
            
            int ret = createUser(context_id,userData,module_access,write_ox_con,internal_user_id,contact_id);
            write_ox_con.commit();
            v.add("OK");
            v.add(new Integer(ret));
        }catch(SQLException sql){
            // rollback operations on ox db connection
            try{
                write_ox_con.rollback();
                log.debug("Rollback successfull for ox db write connection");
            }catch(Exception ecp){
                log.error("Error rollback ox db write connection",ecp);
            }
            throw sql;
        }catch(NoSuchAlgorithmException nsae){
            try{
                write_ox_con.rollback();
                log.debug("Rollback successfull for ox db write connection");
            }catch(Exception nsae2){
                log.error("Error rollback ox db write connection",nsae2);
            }
            throw nsae;
        }catch(DBPoolingException pex){
            try{
                if(write_ox_con!=null){
                    write_ox_con.rollback();
                }
                log.debug("Rollback successfull for ox db write connection");
            }catch(Exception pex2){
                log.error("Error rollback ox db write connection",pex2);
            }
            throw pex;
        }catch(OXException oxi){
            try{
                if(write_ox_con!=null){
                    write_ox_con.rollback();
                }
                log.debug("Rollback successfull for ox db write connection");
            }catch(Exception pex3){
                log.error("Error rollback ox db write connection",pex3);
            }
            throw oxi;
        }finally{
            try {
                cache.pushOXDBWrite(context_id,write_ox_con);
            } catch (Exception ex) {
                log.error("Error pushing ox write connection to pool!",ex);
            }
        }
        
        return v;
    }
    
    public Vector<Object> getAllUsers(int context_id) throws PoolException, SQLException {
        Vector<Object> retValue = new Vector<Object>();
        
        Connection read_ox_con = null;
        PreparedStatement stmt = null;
        try {
            Vector<Integer> users = new Vector<Integer>();
            read_ox_con = cache.getREADConnectionForContext(context_id);
            stmt = read_ox_con.prepareStatement("SELECT con.userid,con.field01,con.field02,con.field03,lu.uid FROM prg_contacts con JOIN login2user lu  ON con.userid = lu.id WHERE con.cid = ? AND con.cid = lu.cid AND (lu.uid LIKE '%' OR con.field01 LIKE '%');");
            
            stmt.setInt(1,context_id);
            ResultSet rs3 = stmt.executeQuery();
            while(rs3.next()){
                int user_id = rs3.getInt("userid");
                users.add(new Integer(user_id));
            }
            rs3.close();
            retValue.add( "OK" );
            retValue.add(users);
        }finally{
            try{
                if(stmt!=null){
                    stmt.close();
                }
            }catch(Exception e){
                log.error("Error closing statement!",e);
            }
            try{
                cache.pushOXDBRead(context_id,read_ox_con);
            }catch(Exception exp){
                log.error("Error pushing ox read connection to pool!",exp);
            }
        }
        
        return retValue;
    }
    
    public void deleteUser(int context_id, int user_id,Connection write_ox_con) throws PoolException, SQLException, ContextException, DeleteFailedException, LdapException, DBPoolingException{
        PreparedStatement stmt = null;
        try{          
            
            log.debug("Start delete user "+user_id+" in context "+context_id);
            
            log.debug("Delete user "+user_id+"("+context_id+") via OX API...");
            
            
            DeleteEvent delev = new DeleteEvent(this,user_id,DeleteEvent.TYPE_USER,context_id);
            AdminCache.delreg.fireDeleteEvent(delev,write_ox_con,write_ox_con);            
            
            
            log.debug("Delete user "+user_id+"("+context_id+") from login2user...");
            stmt = write_ox_con.prepareStatement("DELETE FROM login2user WHERE cid = ? AND id = ?");
            stmt.setInt(1,context_id);
            stmt.setInt(2,user_id);
            stmt.executeUpdate();
            stmt.close();
            
            log.debug("Delete user "+user_id+"("+context_id+") from groups member...");
            stmt = write_ox_con.prepareStatement("DELETE FROM groups_member WHERE cid = ? AND member = ?");
            stmt.setInt(1,context_id);
            stmt.setInt(2,user_id);
            stmt.executeUpdate();
            stmt.close();
            
            log.debug("Delete user "+user_id+"("+context_id+") from user attribute ...");
            stmt = write_ox_con.prepareStatement("DELETE FROM user_attribute WHERE cid = ? AND id = ?");
            stmt.setInt(1,context_id);
            stmt.setInt(2,user_id);
            stmt.executeUpdate();
            stmt.close();            

            log.debug("Delete user "+user_id+"("+context_id+") from user mail setting...");
            stmt = write_ox_con.prepareStatement("DELETE FROM user_setting_mail WHERE cid = ? AND user = ?");
            stmt.setInt(1,context_id);
            stmt.setInt(2,user_id);
            stmt.executeUpdate();
            stmt.close();
            
            // delete from user_setting_admin if user is mailadmin            
            if(user_id==AdminDaemonTools.getAdminForContext(context_id,write_ox_con)){
                stmt = write_ox_con.prepareStatement("DELETE FROM user_setting_admin WHERE cid = ? AND user = ?");
                stmt.setInt(1,context_id);
                stmt.setInt(2,user_id);
                stmt.executeUpdate();
                stmt.close();
            }
            
            // when table ready, enable this
            createRecoveryData(user_id,context_id,write_ox_con);
            
            log.debug("Delete user "+user_id+"("+context_id+") from user ...");
            stmt = write_ox_con.prepareStatement("DELETE FROM user WHERE cid = ? AND id = ?");
            stmt.setInt(1,context_id);
            stmt.setInt(2,user_id);
            stmt.executeUpdate();
            stmt.close();
            
            log.debug("Delete user "+user_id+"("+context_id+") from contacts ...");
            stmt = write_ox_con.prepareStatement("DELETE FROM prg_contacts WHERE cid = ? AND userid = ?");
            stmt.setInt(1,context_id);
            stmt.setInt(2,user_id);
            stmt.executeUpdate();
            stmt.close();            
            
            
            
        }finally{
            try {
                if(stmt!=null){
                    stmt.close();
                }
            } catch (Exception e) {
                log.error("Error closing statement on ox write connection!",e);
            }
        }
    }
    
    public Vector<Object> deleteUser(int context_id, int user_id) throws PoolException, SQLException, ContextException, DeleteFailedException, LdapException, DBPoolingException {
        Vector<Object> retValue = new Vector<Object>();
        
        Connection write_ox_con = null;        
        
        try{
            
            write_ox_con = cache.getWRITEConnectionForContext(context_id);            
            write_ox_con.setAutoCommit(false);
            
            deleteUser(context_id,user_id,write_ox_con);
            
            write_ox_con.commit();
            
            retValue.add("OK");
            retValue.add("USER REMOVED");
        }catch(SQLException sql){            
            try{
                write_ox_con.rollback();
            }catch(Exception ex){
                log.error("Error rollback ox db write connection",ex);
            }
            throw sql;
        }catch(PoolException pexp){
            try{
                write_ox_con.rollback();
            }catch(Exception ex){
                log.error("Error rollback ox db write connection",ex);
            }
            throw pexp;
        }catch(DBPoolingException pexp2){
            try{
                write_ox_con.rollback();
            }catch(Exception ex){
                log.error("Error rollback ox db write connection",ex);
            }
            throw pexp2;
        }catch(ContextException pexp3){
            try{
                write_ox_con.rollback();
            }catch(Exception ex){
                log.error("Error rollback ox db write connection",ex);
            }
            throw pexp3;
        }catch(DeleteFailedException pexp4){
            try{
                write_ox_con.rollback();
            }catch(Exception ex){
                log.error("Error rollback ox db write connection",ex);
            }
            throw pexp4;
        }catch(LdapException pexp5){
            try{
                write_ox_con.rollback();
            }catch(Exception ex){
                log.error("Error rollback ox db write connection",ex);
            }
            throw pexp5;
        }finally{            
            try{
                cache.pushOXDBWrite(context_id,write_ox_con);
            }catch(Exception aexp){
                log.error("Error pushing ox write connection to pool!",aexp);
            }
            
        }
        
        return retValue;
    }

    public static void createRecoveryData(int user_id, int context_id, Connection write_ox_con) throws SQLException {
        // move user to del_user table if table is ready        
        PreparedStatement del_st = null;
        ResultSet rs = null;
        try{
            del_st = write_ox_con.prepareStatement(
                    "SELECT " +
                    "imapServer,smtpServer,imapLogin,mail,mailDomain,mailEnabled,preferredLanguage," +
                    "shadowLastChange,timeZone,contactId,userPassword " +
                    "FROM user " +
                    "WHERE " +
                    "id = ? " +
                    "AND " +
                    "cid = ?");
            del_st.setInt(1,user_id);
            del_st.setInt(2,context_id);
            rs = del_st.executeQuery();
            
            String iserver = null;
            String sserver = null;
            String ilogin = null;
            String mail = null;
            String maildomain = null;
            int menabled = -1;
            String preflang = null;
            int shadowlastschange = -1;
            String tzone = null;
            int contactid = -1;
            String passwd = null;
            
            if(rs.next()){
              iserver = rs.getString("imapServer"); 
              sserver = rs.getString("smtpServer");
              ilogin = rs.getString("imapLogin");
              mail = rs.getString("mail");
              maildomain = rs.getString("maildomain");
              menabled = rs.getInt("mailEnabled");
              preflang = rs.getString("preferredLanguage");
              shadowlastschange = rs.getInt("shadowLastChange");
              tzone = rs.getString("timeZone");
              contactid = rs.getInt("contactId");
              passwd = rs.getString("userPassword");
            }
            del_st.close();
            rs.close();
            
            
            del_st = write_ox_con.prepareStatement("" +
                    "INSERT " +
                    "into del_user " +
                    "(id,cid,lastModified,imapServer,smtpServer,imapLogin,mail,maildomain," +
                    "mailEnabled,preferredLanguage,shadowLastChange,timeZone,contactId,userPassword) " +
                    "VALUES " +
                    "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            del_st.setInt(1,user_id);
            del_st.setInt(2,context_id);
            del_st.setLong(3,System.currentTimeMillis());
            if(iserver!=null){
                del_st.setString(4,iserver);
            }else{
                del_st.setNull(4,Types.VARCHAR);
            }
            if(sserver!=null){
                del_st.setString(5,sserver);
            }else{
                del_st.setNull(5,Types.VARCHAR);
            }
            if(ilogin!=null){
                del_st.setString(6,ilogin);
            }else{
                del_st.setNull(6,Types.VARCHAR);
            }
            if(mail!=null){
                del_st.setString(7,mail);
            }else{
                del_st.setNull(7,Types.VARCHAR);
            }
            if(maildomain!=null){
                del_st.setString(8,maildomain);
            }else{
                del_st.setNull(8,Types.VARCHAR);
            }
            if(menabled!=-1){
                del_st.setInt(9,menabled);
            }else{
                del_st.setNull(9,Types.INTEGER);
            }
            if(preflang!=null){
                del_st.setString(10,preflang);
            }else{
                del_st.setNull(10,Types.VARCHAR);
            }
            
            del_st.setInt(11,shadowlastschange);
            
            if(tzone!=null){
                del_st.setString(12,tzone);
            }else{
                del_st.setNull(12,Types.VARCHAR);
            }
            if(contactid!=-1){
                del_st.setInt(13,contactid);
            }else{
                del_st.setNull(13,Types.INTEGER);
            }
            if(passwd!=null){
                del_st.setString(14,passwd);
            }else{
                del_st.setNull(14,Types.VARCHAR);
            }
            del_st.executeUpdate();
        }finally{
            try {
                if(del_st!=null){
                    del_st.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
        }
    }
    
    public static void deleteRecoveryData(int user_id, int context_id, Connection con) throws SQLException {
        // delete from del_user table
        PreparedStatement del_st = null;
        try{
            del_st = con.prepareStatement("" +
                    "DELETE " +
                    "from del_user " +
                    "WHERE " +
                    "id = ? " +
                    "AND " +
                    "cid = ?");
            del_st.setInt(1,user_id);
            del_st.setInt(2,context_id);            
            del_st.executeUpdate();
        }finally{
            try {
                if(del_st!=null){
                    del_st.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
        }
    }
    
    public static void deleteAllRecoveryData(int context_id, Connection con) throws SQLException {
        // delete from del_user table
        PreparedStatement del_st = null;
        try{
            del_st = con.prepareStatement("" +
                    "DELETE " +
                    "from del_user " +
                    "WHERE " +
                    "cid = ?");            
            del_st.setInt(1,context_id);            
            del_st.executeUpdate();
        }finally{
            try {
                if(del_st!=null){
                    del_st.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
        }
    }
    
    
    
}
