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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Vector;


/**
 * The interface class <code>I_OXUser</code> defines the Open-Xchange
 * API for creating and manipulating OX Users within a given OX
 * context.
 *
 */
public interface I_OXUser extends Remote {
    
    /**
     * RMI name to be used in RMI URL
     */
    public static final String RMI_NAME             = "OX_AdminDaemon_OXUser";
    
    
    
    
    /**
     * Key representing the user identifier, value is a <code>String</code>
     */
    public static final String UID                  = "identifier";
    
    
    /**
     * Key representing the user identifier number, value must be <code>Integer</code>
     */
    public static final String UID_NUMBER           = "id";
    
    /*****************************************************************************
     *
     * User setup parameters
     *
     *****************************************************************************/
    
    /**
     * Key representing if user is enabled or not , value is a <code>Boolean</code>
     */
    public static final String ENABLED           = "mailEnabled";
    
    /**
     * Use this to set the password expired. Value is a <code>boolean</code>
     */
    public static final String PASSWORD_EXPIRED          = "shadowLastChange";
    
    /**
     * Represents the password, value is a <code>String</code>
     */
    public static final String PASSWORD             = "password";
    
    /**
     * Represents the aliases of an user, value is a <code>String[]</code>
     */
    public static final String ALIAS                = "alias";
    
    /**
     * Represents the imapserver where the user is located, value is a <code>String</code>
     */
    public static final String IMAP_SERVER                = "imapserver";
    
    /**
     * Represents the smtpserver the user must use, value is a <code>String</code>
     */
    public static final String SMTP_SERVER                = "smtpserver";
    
    /**
     * Represents the timezone of the user, value is a <code>String</code>
     */
    public static final String TIMEZONE                = "timezone";
    
    /**
     * Represents the language of the user, value is a <code>String</code>. For example: de_DE,en_US
     */
    public static final String LANGUAGE                = "preferredlanguage";
    
    /**
     * Group id of the default group when creating an user, value is an <code>int</code>
     * If not set , the default context group is used.
     *
     */
    public static final String DEFAULT_GROUP              = "ox_default_group";
    
    /**
     * Primary mail address, value is a <code>String</code>
     */
    public static final String PRIMARY_MAIL = "mail";
    
    /*****************************************************************************
     *
     * START user data from contact interface
     *
     *****************************************************************************/
    
    /*
     * Missing fields:
     * Gender
     * Instant messenger (other)
     */
    
    
    public static final String STREET_BUSINESS = "street_business";
    public static final String POSTAL_CODE_BUSINESS = "postal_code_business";
    public static final String CITY_BUSINESS = "city_business";
    public static final String STATE_BUSINESS = "state_business";
    public static final String COUNTRY_BUSINESS = "country_business";
    public static final String BUSINESS_CATEGORY = "business_category";
    public static final String TELEPHONE_BUSINESS1 = "telephone_business1";
    public static final String TELEPHONE_BUSINESS2 = "telephone_business2";
    public static final String FAX_BUSINESS = "fax_business";
    
    public static final String STREET_OTHER = "street_other";
    public static final String CITY_OTHER = "city_other";
    public static final String POSTAL_CODE_OTHER = "postal_code_other";
    public static final String COUNTRY_OTHER = "country_other";
    public static final String TELEPHONE_OTHER = "telephone_other";
    public static final String FAX_OTHER = "fax_other";
    public static final String STATE_OTHER = "state_other";
    
    
    /**
     * First name
     */
    public static final String GIVEN_NAME = "given_name";
    
    /**
     * Last name
     */
    public static final String SUR_NAME = "sur_name";
    
    /**
     * Display name
     */
    public static final String DISPLAY_NAME = "display_name";
    /**
     * Second name
     */
    public static final String MIDDLE_NAME = "middle_name";
    public static final String SUFFIX = "suffix";
    public static final String TITLE = "title";
    public static final String STREET_HOME = "street_home";
    public static final String POSTAL_CODE_HOME = "postal_code_home";
    public static final String CITY_HOME = "city_home";
    public static final String STATE_HOME = "state_home";
    public static final String COUNTRY_HOME = "country_home";
    /**
     * Birthday, Type: <code>java.util.Date</code>
     */
    public static final String BIRTHDAY = "birthday";
    public static final String MARITAL_STATUS = "marital_status";
    public static final String NUMBER_OF_CHILDREN = "number_of_children";
    public static final String PROFESSION = "profession";
    public static final String NICKNAME = "nickname";
    public static final String SPOUSE_NAME = "spouse_name";
    /**
     * Anniversary, Type: <code>java.util.Date</code>
     */
    public static final String ANNIVERSARY = "anniversary";
    public static final String NOTE = "note";
    public static final String DEPARTMENT = "department";
    public static final String POSITION = "position";
    /**
     * Job title
     */
    public static final String EMPLOYEE_TYPE = "employee_type";
    public static final String ROOM_NUMBER = "room_number";
    /**
     * Employee ID
     */
    public static final String NUMBER_OF_EMPLOYEE = "number_of_employee";
    public static final String SALES_VOLUME = "sales_volume";
    public static final String TAX_ID = "tax_id";
    public static final String COMMERCIAL_REGISTER = "commercial_register";
    public static final String BRANCHES = "branches";
    public static final String INFO = "info";
    public static final String MANAGER_NAME = "manager_name";
    public static final String ASSISTANT_NAME = "assistant_name";
    public static final String TELEPHONE_CALLBACK = "telephone_callback";
    public static final String TELEPHONE_CAR = "telephone_car";
    public static final String TELEPHONE_COMPANY = "telephone_company";
    /**
     * Phone (home)
     */
    public static final String TELEPHONE_HOME1 = "telephone_home1";
    /**
     * Phone (home 2)
     */
    public static final String TELEPHONE_HOME2 = "telephone_home2";
    public static final String FAX_HOME = "fax_home";
    /**
     * Mobile
     */
    public static final String CELLULAR_TELEPHONE1 = "cellular_telephone1";
    /**
     * Mobile 2
     */
    public static final String CELLULAR_TELEPHONE2 = "cellular_telephone2";
    /**
     * Email (business)
     */
    public static final String EMAIL1 = "email1";
    /**
     * Email (home)
     */
    public static final String EMAIL2 = "email2";
    /**
     * Email (other)
     */
    public static final String EMAIL3 = "email3";
    public static final String URL = "url";
    public static final String TELEPHONE_ISDN = "telephone_isdn";
    public static final String TELEPHONE_PAGER = "telephone_pager";
    public static final String TELEPHONE_PRIMARY = "telephone_primary";
    public static final String TELEPHONE_RADIO = "telephone_radio";
    public static final String TELEPHONE_TELEX = "telephone_telex";
    public static final String TELEPHONE_TTYTDD = "telephone_ttytdd";
    /**
     * Instant messenger (business)
     */
    public static final String INSTANT_MESSENGER1 = "instant_messenger1";
    /**
     * Instant messenger (home)
     */
    public static final String INSTANT_MESSENGER2 = "instant_messenger2";
    /**
     * IP-phone
     */
    public static final String TELEPHONE_IP = "telephone_ip";
    public static final String TELEPHONE_ASSISTANT = "telephone_assistant";
    public static final String COMPANY = "company";
    
    public static final String USERFIELD01 = "userfield01";
    public static final String USERFIELD02 = "userfield02";
    public static final String USERFIELD03 = "userfield03";
    public static final String USERFIELD04 = "userfield04";
    public static final String USERFIELD05 = "userfield05";
    public static final String USERFIELD06 = "userfield06";
    public static final String USERFIELD07 = "userfield07";
    public static final String USERFIELD08 = "userfield08";
    public static final String USERFIELD09 = "userfield09";
    public static final String USERFIELD10 = "userfield10";
    public static final String USERFIELD11 = "userfield11";
    public static final String USERFIELD12 = "userfield12";
    public static final String USERFIELD13 = "userfield13";
    public static final String USERFIELD14 = "userfield14";
    public static final String USERFIELD15 = "userfield15";
    public static final String USERFIELD16 = "userfield16";
    public static final String USERFIELD17 = "userfield17";
    public static final String USERFIELD18 = "userfield18";
    public static final String USERFIELD19 = "userfield19";
    public static final String USERFIELD20 = "userfield20";
    
    /*****************************************************************************
     *
     * START user data from contact interface
     *
     *****************************************************************************/
    
    
    /**
     * Key representing module access to webmail  , value is a <code>Boolean</code>
     */
    public static final String ACCESS_WEBMAIL = "module_access_webmail";
    
    /**
     * Key representing module access to calendar  , value is a <code>Boolean</code>
     */
    public static final String ACCESS_CALENDAR = "module_access_calendar";
    
    /**
     * Key representing module access to contacts  , value is a <code>Boolean</code>
     */
    public static final String ACCESS_CONTACTS = "module_access_contacts";
    
    /**
     * Key representing module access to tasks  , value is a <code>Boolean</code>
     */
    public static final String ACCESS_TASKS = "module_access_tasks";
    
    /**
     * Key representing module access to infostore  , value is a <code>Boolean</code>
     */
    public static final String ACCESS_INFOSSTORE = "module_access_infostore";
    
    /**
     * Key representing module access to projects  , value is a <code>Boolean</code>
     */
    public static final String ACCESS_PROJECTS = "module_access_projects";
    
    /**
     * Key representing module access to forum/board  , value is a <code>Boolean</code>
     */
    public static final String ACCESS_FORUM = "module_access_forum";
    
    /**
     * Key representing module access for write access to pinboard  , value is a <code>Boolean</code>
     */
    public static final String ACCESS_PINBOARD_WRITE_ACCESS = "module_access_pinboard_write_access";
    
    /**
     * Key representing module access to webdav xml inteface  , value is a <code>Boolean</code>
     */
    public static final String ACCESS_WEBDAV_XML = "module_access_webdav_xml";
    
    /**
     * Key representing module access to webdav  , value is a <code>Boolean</code>
     */
    public static final String ACCESS_WEBDAV = "module_access_webdav";
    
    /**
     * Key representing module access to ical interface  , value is a <code>Boolean</code>
     */
    public static final String ACCESS_ICAL = "module_access_ical";
    
    /**
     * Key representing module access to vcard  , value is a <code>Boolean</code>
     */
    public static final String ACCESS_VCARD = "module_access_vcard";
    
    /**
     * Key representing module access to rss bookmarks  , value is a <code>Boolean</code>
     */
    public static final String ACCESS_RSS_BOOKMARKS = "module_access_rss_bookmarks";
    
    /**
     * Key representing module access to rss portal  , value is a <code>Boolean</code>
     */
    public static final String ACCESS_RSS_PORTAL = "module_access_rss_portal";
    
    /**
     * Key representing module access to syncml interface  , value is a <code>Boolean</code>
     */
    public static final String ACCESS_SYNCML = "module_access_syncml";
    
    /**
     * Key representing module access to edit public folders  , value is a <code>Boolean</code>
     */
    public static final String ACCESS_EDIT_PUBLIC_FOLDERS = "module_access_edit_public_folders";
    
    /**
     * Key representing module access to read create shared folders, value is a <code>Boolean</code>
     */
    public static final String ACCESS_READ_CREATE_SHARED_FOLDERS = "module_access_read_create_shared_folders";
    
    /**
     * Key representing module access to delegate tasks  , value is a <code>Boolean</code>
     */
    public static final String ACCESS_DELEGATE_TASKS = "module_access_delegate_tasks";
    
    /**
     * Key representing the mailfolder "Spam"  , value is a <code>String</code>
     */
    public static final String MAIL_FOLDER_SPAM = "mail_folder_spam_name";
    
    /**
     * Key representing the mailfolder "Sent"  , value is a <code>String</code>
     */
    public static final String MAIL_FOLDER_SENT = "mail_folder_sent_name";
    
    /**
     * Key representing the mailfolder "Trash"  , value is a <code>String</code>
     */
    public static final String MAIL_FOLDER_TRASH = "mail_folder_trash_name";
    
    /**
     * Key representing the mailfolder "Drafts"  , value is a <code>String</code>
     */
    public static final String MAIL_FOLDER_DRAFTS = "mail_folder_drafts_name";
    
    /**
     * String array containing fields required to create a user
     * @see #UID
     * @see #DISPLAY_NAME
     * @see #PASSWORD
     * @see #GIVEN_NAME
     * @see #SUR_NAME
     * @see #PRIMARY_MAIL
     */
    public static final String REQUIRED_KEYS_CREATE[]   = { UID, DISPLAY_NAME, PASSWORD,GIVEN_NAME,SUR_NAME,PRIMARY_MAIL };
    
    
    
    public static final String POSSIBLE_ACCESS_RIGHTS[] = {
        ACCESS_CALENDAR,
        ACCESS_CONTACTS,
        ACCESS_DELEGATE_TASKS,
        ACCESS_EDIT_PUBLIC_FOLDERS,
        ACCESS_FORUM,
        ACCESS_ICAL,
        ACCESS_INFOSSTORE,
        ACCESS_PINBOARD_WRITE_ACCESS,
        ACCESS_PROJECTS,
        ACCESS_READ_CREATE_SHARED_FOLDERS,
        ACCESS_RSS_BOOKMARKS,
        ACCESS_RSS_PORTAL,
        ACCESS_SYNCML,
        ACCESS_TASKS,
        ACCESS_VCARD,
        ACCESS_WEBDAV,
        ACCESS_WEBDAV_XML,
        ACCESS_WEBMAIL
    };
    
    
    
    /**
     * Creates a new user within the given context. The
     * <code>Hashtable userData</code> contains every element a newly
     * created user may or must contain.
     *
     * <p><blockquote><pre>
     *      Hashtable newUser = new Hashtable();
     *      newUser.put(I_OXUser.UID, "myuser");
     *      newUser.put(I_OXUser.DISPLAY_NAME, "My fine new user");
     *      newUser.put(I_OXUser.PASSWORD, "mySecretPassword");
     *      newUser.put(I_OXUser.GIVEN_NAME, "John");
     *      newUser.put(I_OXUser.SUR_NAME, "Smith");
     *      newUser.put(I_OXUser.PRIMARY_MAIL, "myuser@domain.tld");
     *
     *      // set access rights to modules
     *      Hashtable access = new Hashtable();
     *
     *      access.put(I_OXUser.ACCESS_CALENDAR,new Boolean(true));
     *      access.put(I_OXUser.ACCESS_CONTACTS,new Boolean(true));
     *      access.put(I_OXUser.ACCESS_DELEGATE_TASKS,new Boolean(true));
     *      access.put(I_OXUser.ACCESS_EDIT_PUBLIC_FOLDERS,new Boolean(true));
     *      access.put(I_OXUser.ACCESS_FORUM,new Boolean(true));
     *      access.put(I_OXUser.ACCESS_ICAL,new Boolean(true));
     *      access.put(I_OXUser.ACCESS_INFOSSTORE,new Boolean(true));
     *      access.put(I_OXUser.ACCESS_PINBOARD_WRITE_ACCESS,new Boolean(true));
     *      access.put(I_OXUser.ACCESS_PROJECTS,new Boolean(true));
     *      access.put(I_OXUser.ACCESS_READ_CREATE_SHARED_FOLDERS,new Boolean(true));
     *      access.put(I_OXUser.ACCESS_RSS_BOOKMARKS,new Boolean(true));
     *      access.put(I_OXUser.ACCESS_RSS_PORTAL,new Boolean(true));
     *      access.put(I_OXUser.ACCESS_SYNCML,new Boolean(true));
     *      access.put(I_OXUser.ACCESS_TASKS,new Boolean(true));
     *      access.put(I_OXUser.ACCESS_VCARD,new Boolean(true));
     *      access.put(I_OXUser.ACCESS_WEBDAV,new Boolean(true));
     *      access.put(I_OXUser.ACCESS_WEBDAV_XML,new Boolean(true));
     *      access.put(I_OXUser.ACCESS_WEBMAIL,new Boolean(true));
     *
     *      Vector result = rmi_oxuser.createUser(context_id, newUser, access);
     * </pre></blockquote></p>
     *
     * @param context_id numerical context identifier
     * @param userData Hashtable containing user data
     * @param access Hashtable containing module access for the user
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.<br>
     *         2nd Object is of type <code>Integer</code> and contains the id to work with this user in the system.
     *         <p>
     *
     * @throws RemoteException
     */
    public Vector createUser( int context_id, Hashtable userData, Hashtable access ) throws RemoteException;
    
    
    /**
     * Manipulate user data within the given context.
     *
     * <p><blockquote><pre>
     *      Hashtable changeUser = new Hashtable();
     *      changeUser.put(I_OXUser.PRIMARY_MAIL, "myuser@domain.tld");
     *      changeUser.put(I_OXUser.DISPLAY_NAME, "My new fine new user");
     *
     *      Vector result = rmi_oxuser.changeUserData(context_id, user_id, changeUser);
     * </pre></blockquote></p>
     *
     * @param context_id numerical context identifier
     * @param user_id numerical user identifier
     * @param userData Hash containing user data
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     *
     * @throws RemoteException
     */
    public Vector changeUserData( int context_id, int user_id, Hashtable userData ) throws RemoteException;
    
    
    /**
     * Delete user from given context.
     *
     * @param context_id numerical context identifier
     * @param user_id numerical user identifier
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     *
     * @throws RemoteException
     */
    public Vector deleteUser( int context_id, int user_id ) throws RemoteException;
    
    /**
     * Retrieve the ModuleAccess for an user.
     *
     * @param context_id numerical context identifier
     * @param user_id numerical user identifier
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.<br>
     *         2nd Object is of type <code>Hashtable</code> containing module access rights per user
     *         <p>
     *
     * @throws RemoteException
     * @see #ACCESS_CALENDAR
     * @see #ACCESS_CONTACTS 
     * @see #ACCESS_DELEGATE_TASKS 
     * @see #ACCESS_EDIT_PUBLIC_FOLDERS 
     * @see #ACCESS_FORUM 
     * @see #ACCESS_ICAL 
     * @see #ACCESS_INFOSSTORE 
     * @see #ACCESS_PINBOARD_WRITE_ACCESS 
     * @see #ACCESS_PROJECTS 
     * @see #ACCESS_READ_CREATE_SHARED_FOLDERS 
     * @see #ACCESS_RSS_BOOKMARKS 
     * @see #ACCESS_RSS_PORTAL 
     * @see #ACCESS_SYNCML 
     * @see #ACCESS_TASKS 
     * @see #ACCESS_VCARD 
     * @see #ACCESS_WEBDAV
     * @see #ACCESS_ICAL
     * @see #ACCESS_WEBDAV_XML
     * @see #ACCESS_WEBMAIL 
     *
     */
    public Vector getUserModuleAccess( int context_id, int user_id ) throws RemoteException;
    
    /**
     * Manipulate user module access within the given context.
     *
     * <p><blockquote><pre>
     *      Hashtable changeUser = new Hashtable();
     *      changeUser.put(rmi_oxuser.ACCESS_WEBMAIL, new Boolean(false));
     *      Vector result = rmi_oxuser.changeUserModuleAccess(context_id, user_id, changeUser);
     * </pre></blockquote></p>
     *
     * @param context_id numerical context identifier
     * @param user_id numerical user identifier
     * @param moduleAccess Hash containing module access data
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     *
     * @throws RemoteException
     * @see #ACCESS_CALENDAR
     * @see #ACCESS_CONTACTS 
     * @see #ACCESS_DELEGATE_TASKS 
     * @see #ACCESS_EDIT_PUBLIC_FOLDERS 
     * @see #ACCESS_FORUM 
     * @see #ACCESS_ICAL 
     * @see #ACCESS_INFOSSTORE 
     * @see #ACCESS_PINBOARD_WRITE_ACCESS 
     * @see #ACCESS_PROJECTS 
     * @see #ACCESS_READ_CREATE_SHARED_FOLDERS 
     * @see #ACCESS_RSS_BOOKMARKS 
     * @see #ACCESS_RSS_PORTAL 
     * @see #ACCESS_SYNCML 
     * @see #ACCESS_TASKS 
     * @see #ACCESS_VCARD 
     * @see #ACCESS_WEBDAV
     * @see #ACCESS_ICAL
     * @see #ACCESS_WEBDAV_XML
     * @see #ACCESS_WEBMAIL 
     */
    public Vector changeUserModuleAccess( int context_id, int user_id, Hashtable moduleAccess ) throws RemoteException;
    
    /**
     * Retrieve all user ids for a given context.
     *
     * @param context_id numerical context identifier
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.<br>
     *         2nd Object is of type <code>Vector</code> containing the user ids.
     *         <p>
     *
     * @throws RemoteException
     */
    public Vector getAllUsers(int context_id) throws RemoteException;
    
    /**
     * Retrieve user data for an user.
     *
     * @param context_id numerical context identifier
     * @param user_id numerical user identifier
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.<br>
     *         2nd Object is of type <code>Hashtable</code> containing the user data
     *         <p>
     *
     * @throws RemoteException
     */
    public Vector getUserData(int context_id, int user_id) throws RemoteException;
    
    /**
     * Retrieve user data for an user.
     *
     * @param context_id numerical context identifier
     * @param uid users uid (I_OXUser.UID)
     * @return Vector containing return code and/or result objects
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         2nd Object is of type <code>Hashtable</code> containing the user data
     *         <p>
     *
     * @throws RemoteException
     */
    public Vector getUserData(int context_id, String uid) throws RemoteException;
    
}
