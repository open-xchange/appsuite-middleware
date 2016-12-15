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

package com.openexchange.admin.rmi.dataobjects;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.admin.rmi.extensions.OXCommonExtension;
import com.openexchange.admin.rmi.extensions.OXUserExtensionInterface;
import com.openexchange.admin.rmi.utils.URIDefaults;
import com.openexchange.admin.rmi.utils.URIParser;

/**
 * Class representing a user.
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 */
public class User extends ExtendableDataObject implements NameAndIdObject, PasswordMechObject {
    /**
     * For serialization
     */
    private static final long serialVersionUID = -4492376747507390066L;

    private boolean contextadmin = false;

    private Integer id;

    private boolean idset = false;

    private String name;

    private boolean nameset = false;

    private String password;

    private boolean passwordset = false;

    private String passwordMech;

    private boolean passwordMechset = false;

    private String primaryEmail;

    private boolean primaryEmailset = false;

    private String email1;

    private boolean email1set = false;

    private String email2;

    private boolean email2set = false;

    private String email3;

    private boolean email3set = false;

    private HashSet<String> aliases;

    private boolean aliasesset = false;

    private String sur_name;

    private boolean sur_nameset = false;

    private String given_name;

    private boolean given_nameset = false;

    private Boolean mailenabled;

    private boolean mailenabledset = false;

    // ------------------------ File storage members ------------------------

    private Integer filestore_id;

    private boolean filestore_idset;

    private Integer filestore_owner;

    private boolean filestore_ownerset;

    private String filestore_name;

    private boolean filestore_nameset;

    private Long maxQuota;

    private boolean maxQuotaset;

    private Long usedQuota;

    private boolean usedQuotaset;

    // -----------------------------------------------------------------------

    private Date birthday;

    private boolean birthdayset = false;

    private Date anniversary;

    private boolean anniversaryset = false;

    private String branches;

    private boolean branchesset = false;

    private String business_category;

    private boolean business_categoryset = false;

    private String categories;

    private boolean categoriesset = false;

    private String postal_code_business;

    private boolean postal_code_businessset = false;

    private String state_business;

    private boolean state_businessset = false;

    private String street_business;

    private boolean street_businessset = false;

    private String telephone_callback;

    private boolean telephone_callbackset = false;

    private String city_home;

    private boolean city_homeset = false;

    private String commercial_register;

    private boolean commercial_registerset = false;

    private String country_home;

    private boolean country_homeset = false;

    private String company;

    private boolean companyset = false;

    private Group default_group;

    private boolean default_groupset = false;

    private String department;

    private boolean departmentset = false;

    private String display_name;

    private boolean display_nameset = false;

    private String employeeType;

    private boolean employeeTypeset = false;

    private String fax_business;

    private boolean fax_businessset = false;

    private String fax_home;

    private boolean fax_homeset = false;

    private String fax_other;

    private boolean fax_otherset = false;

    private String imapServer;

    private boolean imapServerset = false;

    private String smtpServer;

    private boolean smtpServerset = false;

    private String imapLogin;

    private boolean imapLoginset = false;

    private String instant_messenger1;

    private boolean instant_messenger1set = false;

    private String instant_messenger2;

    private boolean instant_messenger2set = false;

    private String telephone_ip;

    private boolean telephone_ipset = false;

    private String telephone_isdn;

    private boolean telephone_isdnset = false;

    private String language;

    private boolean languageset = false;

    private String mail_folder_drafts_name;

    private boolean mail_folder_drafts_nameset = false;

    private String mail_folder_sent_name;

    private boolean mail_folder_sent_nameset = false;

    private String mail_folder_spam_name;

    private boolean mail_folder_spam_nameset = false;

    private String mail_folder_trash_name;

    private boolean mail_folder_trash_nameset = false;

    private String mail_folder_archive_full_name;

    private boolean mail_folder_archive_full_nameset = false;

    private String mail_folder_confirmed_spam_name;

    private final boolean mail_folder_confirmed_spam_nameset = false;

    private String mail_folder_confirmed_ham_name;

    private boolean mail_folder_confirmed_ham_nameset = false;

    private Boolean gui_spam_filter_enabled;

    private boolean gui_spam_filter_enabledset = false;

    private String manager_name;

    private boolean manager_nameset = false;

    private String marital_status;

    private boolean marital_statusset = false;

    private String cellular_telephone1;

    private boolean cellular_telephone1set = false;

    private String cellular_telephone2;

    private boolean cellular_telephone2set = false;

    private String info;

    private boolean infoset = false;

    private String nickname;

    private boolean nicknameset = false;

    private String number_of_children;

    private boolean number_of_childrenset = false;

    private String note;

    private boolean noteset = false;

    private String number_of_employee;

    private boolean number_of_employeeset = false;

    private String telephone_pager;

    private boolean telephone_pagerset = false;

    private Boolean password_expired;

    private boolean password_expiredset = false;

    private String telephone_assistant;

    private boolean telephone_assistantset = false;

    private String assistant_name;

    private boolean assistant_nameset = false;

    private String telephone_business1;

    private boolean telephone_business1set = false;

    private String telephone_business2;

    private boolean telephone_business2set = false;

    private String telephone_car;

    private boolean telephone_carset = false;

    private String telephone_company;

    private boolean telephone_companyset = false;

    private String telephone_home1;

    private boolean telephone_home1set = false;

    private String telephone_home2;

    private boolean telephone_home2set = false;

    private String telephone_other;

    private boolean telephone_otherset = false;

    private String telephone_primary;

    private boolean telephone_primaryset = false;

    private String position;

    private boolean positionset = false;

    private String postal_code_home;

    private boolean postal_code_homeset = false;

    private String profession;

    private boolean professionset = false;

    private String telephone_radio;

    private boolean telephone_radioset = false;

    private String room_number;

    private boolean room_numberset = false;

    private String sales_volume;

    private boolean sales_volumeset = false;

    private String city_other;

    private boolean city_otherset = false;

    private String city_business;

    private boolean city_businessset = false;

    private String country_other;

    private boolean country_otherset = false;

    private String country_business;

    private boolean country_businessset = false;

    private String middle_name;

    private boolean middle_nameset = false;

    private String postal_code_other;

    private boolean postal_code_otherset = false;

    private String state_other;

    private boolean state_otherset = false;

    private String street_other;

    private boolean street_otherset = false;

    private String spouse_name;

    private boolean spouse_nameset = false;

    private String state_home;

    private boolean state_homeset = false;

    private String street_home;

    private boolean street_homeset = false;

    private String suffix;

    private boolean suffixset = false;

    private String tax_id;

    private boolean tax_idset = false;

    private String telephone_telex;

    private boolean telephone_telexset = false;

    private String timezone;

    private boolean timezoneset = false;

    private String title;

    private boolean titleset = false;

    private String telephone_ttytdd;

    private boolean telephone_ttytddset = false;

    private Integer uploadFileSizeLimit;

    private boolean uploadFileSizeLimitset = false;

    private Integer uploadFileSizeLimitPerFile;

    private boolean uploadFileSizeLimitPerFileset = false;

    private String url;

    private boolean urlset = false;

    private String userfield01;

    private boolean userfield01set = false;

    private String userfield02;

    private boolean userfield02set = false;

    private String userfield03;

    private boolean userfield03set = false;

    private String userfield04;

    private boolean userfield04set = false;

    private String userfield05;

    private boolean userfield05set = false;

    private String userfield06;

    private boolean userfield06set = false;

    private String userfield07;

    private boolean userfield07set = false;

    private String userfield08;

    private boolean userfield08set = false;

    private String userfield09;

    private boolean userfield09set = false;

    private String userfield10;

    private boolean userfield10set = false;

    private String userfield11;

    private boolean userfield11set = false;

    private String userfield12;

    private boolean userfield12set = false;

    private String userfield13;

    private boolean userfield13set = false;

    private String userfield14;

    private boolean userfield14set = false;

    private String userfield15;

    private boolean userfield15set = false;

    private String userfield16;

    private boolean userfield16set = false;

    private String userfield17;

    private boolean userfield17set = false;

    private String userfield18;

    private boolean userfield18set = false;

    private String userfield19;

    private boolean userfield19set = false;

    private String userfield20;

    private boolean userfield20set = false;

    private String defaultSenderAddress;

    private boolean defaultSenderAddressset = false;

    private Integer folderTree;

    private boolean folderTreeSet = false;
    
    private String defaultFolderMode;
    
    private boolean defaultFolderModeSet = false;

    private Map<String, String> guiPreferences;

    private boolean guiPreferencesset = false;

    private Map<String, Map<String, String>> userAttributes = null;

    private boolean userAttribtuesset;

    // -----------------------------------------------------------------------

    private String primaryAccountName;

    private boolean primaryAccountNameSet = false;

    /**
     * Instantiates a new empty user object
     */
    public User() {
        super();
        init();
    }

    /**
     * Instantiates a new user object with the given id set
     *
     * @param id An {@code int} value
     */
    public User(final int id) {
        super();
        init();
        this.id = Integer.valueOf(id);
    }

    /**
     * Returns the id of the user
     *
     * @return Returns the id of the user as a long.
     */
    @Override
    final public Integer getId() {
        return id;
    }

    /**
     * Used to check if the aliases field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isAliasesset() {
        return aliasesset;
    }

    /**
     * Used to check if the anniversary field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isAnniversaryset() {
        return anniversaryset;
    }

    /**
     * Used to check if the assistant_name field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isAssistant_nameset() {
        return assistant_nameset;
    }

    /**
     * Used to check if the birthday field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isBirthdayset() {
        return birthdayset;
    }

    /**
     * Used to check if the branches field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isBranchesset() {
        return branchesset;
    }

    /**
     * Used to check if the business_category field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isBusiness_categoryset() {
        return business_categoryset;
    }

    /**
     * Used to check if the categories field of this user user has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isCategoriesset() {
        return categoriesset;
    }

    /**
     * Used to check if the cellular_telephone1 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isCellular_telephone1set() {
        return cellular_telephone1set;
    }

    /**
     * Used to check if the cellular_telephone2 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isCellular_telephone2set() {
        return cellular_telephone2set;
    }

    /**
     * Used to check if the city_business field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isCity_businessset() {
        return city_businessset;
    }

    /**
     * Used to check if the city_home field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isCity_homeset() {
        return city_homeset;
    }

    /**
     * Used to check if the city_other field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isCity_otherset() {
        return city_otherset;
    }

    /**
     * Used to check if the commercial_register field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isCommercial_registerset() {
        return commercial_registerset;
    }

    /**
     * Used to check if the company field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isCompanyset() {
        return companyset;
    }

    /**
     * Used to check if the country_business field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isCountry_businessset() {
        return country_businessset;
    }

    /**
     * Used to check if the country_home field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isCountry_homeset() {
        return country_homeset;
    }

    /**
     * Used to check if the country_other field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isCountry_otherset() {
        return country_otherset;
    }

    /**
     * Used to check if the default_group field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isDefault_groupset() {
        return default_groupset;
    }

    /**
     * Used to check if the department field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isDepartmentset() {
        return departmentset;
    }

    /**
     * Used to check if the display_name field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isDisplay_nameset() {
        return display_nameset;
    }

    /**
     * Used to check if the email1 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isEmail1set() {
        return email1set;
    }

    /**
     * Used to check if the email2 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isEmail2set() {
        return email2set;
    }

    /**
     * Used to check if the email3 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isEmail3set() {
        return email3set;
    }

    /**
     * Used to check if the employeetype field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isEmployeeTypeset() {
        return employeeTypeset;
    }

    /**
     * Currently not used
     *
     * @return true if set; false if not
     */
    final public Boolean getMailenabled() {
        return mailenabled;
    }

    /**
     * Currently not used
     *
     * @return true if set; false if not
     **/
    final public boolean isMailenabledset() {
        return mailenabledset;
    }

    /**
     * Used to check if the fax_business field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isFax_businessset() {
        return fax_businessset;
    }

    /**
     * Used to check if the fax_home field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isFax_homeset() {
        return fax_homeset;
    }

    /**
     * Used to check if the fax_other field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isFax_otherset() {
        return fax_otherset;
    }

    /**
     * Used to check if the given_name field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isGiven_nameset() {
        return given_nameset;
    }

    /**
     * Used to check if the id field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isIdset() {
        return idset;
    }

    /**
     * Used to check if the imapserver field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isImapServerset() {
        return imapServerset;
    }

    /**
     * Used to check if the imaplogin field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isImapLoginset() {
        return imapLoginset;
    }

    /**
     * Used to check if the info field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isInfoset() {
        return infoset;
    }

    /**
     * Used to check if the instant_messenger1 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isInstant_messenger1set() {
        return instant_messenger1set;
    }

    /**
     * Used to check if the instant_messenger2 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isInstant_messenger2set() {
        return instant_messenger2set;
    }

    /**
     * Used to check if the language field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isLanguageset() {
        return languageset;
    }

    /**
     * Used to check if the mail_folder_drafts_name field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isMail_folder_drafts_nameset() {
        return mail_folder_drafts_nameset;
    }

    /**
     * Used to check if the mail_folder_sent_name field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isMail_folder_sent_nameset() {
        return mail_folder_sent_nameset;
    }

    /**
     * Used to check if the mail_folder_spam_name field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isMail_folder_spam_nameset() {
        return mail_folder_spam_nameset;
    }

    /**
     * Used to check if the mail_folder_trash_name field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isMail_folder_trash_nameset() {
        return mail_folder_trash_nameset;
    }

    /**
     * Used to check if the mail_folder_archive_name field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isMail_folder_archive_full_nameset() {
        return mail_folder_archive_full_nameset;
    }

    /**
     * Used to check if the manager_name field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isManager_nameset() {
        return manager_nameset;
    }

    /**
     * Used to check if the marital_status field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isMarital_statusset() {
        return marital_statusset;
    }

    /**
     * Used to check if the middle_name field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isMiddle_nameset() {
        return middle_nameset;
    }

    /**
     * Used to check if the nickname field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isNicknameset() {
        return nicknameset;
    }

    /**
     * Used to check if the note field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isNoteset() {
        return noteset;
    }

    /**
     * Used to check if the number_of_children field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isNumber_of_childrenset() {
        return number_of_childrenset;
    }

    /**
     * Used to check if the number_of_employee field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isNumber_of_employeeset() {
        return number_of_employeeset;
    }

    /**
     * Used to check if the password_expired field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isPassword_expiredset() {
        return password_expiredset;
    }

    /**
     * Used to check if the passwordmech field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isPasswordMechset() {
        return passwordMechset;
    }

    /**
     * Used to check if the password field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isPasswordset() {
        return passwordset;
    }

    /**
     * Used to check if the position field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isPositionset() {
        return positionset;
    }

    /**
     * Used to check if the postal_code_business field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isPostal_code_businessset() {
        return postal_code_businessset;
    }

    /**
     * Used to check if the postal_code_home field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isPostal_code_homeset() {
        return postal_code_homeset;
    }

    /**
     * Used to check if the postal_code_other field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isPostal_code_otherset() {
        return postal_code_otherset;
    }

    /**
     * Used to check if the primaryemail field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isPrimaryEmailset() {
        return primaryEmailset;
    }

    /**
     * Used to check if the profession field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isProfessionset() {
        return professionset;
    }

    /**
     * Used to check if the room_number field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isRoom_numberset() {
        return room_numberset;
    }

    /**
     * Used to check if the sales_volume field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isSales_volumeset() {
        return sales_volumeset;
    }

    /**
     * Used to check if the smtpserver field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isSmtpServerset() {
        return smtpServerset;
    }

    /**
     * Used to check if the spouse_name field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isSpouse_nameset() {
        return spouse_nameset;
    }

    /**
     * Used to check if the state_business field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isState_businessset() {
        return state_businessset;
    }

    /**
     * Used to check if the state_home field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isState_homeset() {
        return state_homeset;
    }

    /**
     * Used to check if the state_other field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isState_otherset() {
        return state_otherset;
    }

    /**
     * Used to check if the street_business field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isStreet_businessset() {
        return street_businessset;
    }

    /**
     * Used to check if the street_home field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isStreet_homeset() {
        return street_homeset;
    }

    /**
     * Used to check if the street_other field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isStreet_otherset() {
        return street_otherset;
    }

    /**
     * Used to check if the suffix field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isSuffixset() {
        return suffixset;
    }

    /**
     * Used to check if the sur_name field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isSur_nameset() {
        return sur_nameset;
    }

    /**
     * Used to check if the tax_id field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isTax_idset() {
        return tax_idset;
    }

    /**
     * Used to check if the telephone_assistant field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isTelephone_assistantset() {
        return telephone_assistantset;
    }

    /**
     * Used to check if the telephone_business1 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isTelephone_business1set() {
        return telephone_business1set;
    }

    /**
     * Used to check if the telephone_business2 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isTelephone_business2set() {
        return telephone_business2set;
    }

    /**
     * Used to check if the telephone_callback field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isTelephone_callbackset() {
        return telephone_callbackset;
    }

    /**
     * Used to check if the telephone_car field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isTelephone_carset() {
        return telephone_carset;
    }

    /**
     * Used to check if the telephone_company field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isTelephone_companyset() {
        return telephone_companyset;
    }

    /**
     * Used to check if the telephone_home1 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isTelephone_home1set() {
        return telephone_home1set;
    }

    /**
     * Used to check if the telephone_home2 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isTelephone_home2set() {
        return telephone_home2set;
    }

    /**
     * Used to check if the telephone_ip field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isTelephone_ipset() {
        return telephone_ipset;
    }

    /**
     * Used to check if the telephone_isdn field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isTelephone_isdnset() {
        return telephone_isdnset;
    }

    /**
     * Used to check if the telephone_other field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isTelephone_otherset() {
        return telephone_otherset;
    }

    /**
     * Used to check if the telephone_pager field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isTelephone_pagerset() {
        return telephone_pagerset;
    }

    /**
     * Used to check if the telephone_primary field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isTelephone_primaryset() {
        return telephone_primaryset;
    }

    /**
     * Used to check if the telephone_radio field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isTelephone_radioset() {
        return telephone_radioset;
    }

    /**
     * Used to check if the telephone_telex field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isTelephone_telexset() {
        return telephone_telexset;
    }

    /**
     * Used to check if the telephone_ttytdd field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isTelephone_ttytddset() {
        return telephone_ttytddset;
    }

    /**
     * Used to check if the timezone field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isTimezoneset() {
        return timezoneset;
    }

    /**
     * Used to check if the title field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isTitleset() {
        return titleset;
    }

    /**
     * Used to check if the upload quota of this user object has been changed
     *
     * @return true if set; false if not
     */
    public boolean isUploadFileSizeLimitset() {
        return uploadFileSizeLimitset;
    }


    /**
     * Used to check if the upload file size limit per file of this user object has been changed
     *
     * @return true if set; false if not
     */
    public boolean isUploadFileSizeLimitPerFileset() {
        return uploadFileSizeLimitPerFileset;
    }

    /**
     * Used to check if the url field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isUrlset() {
        return urlset;
    }

    /**
     * Used to check if the userfield01 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isUserfield01set() {
        return userfield01set;
    }

    /**
     * Used to check if the userfield02 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isUserfield02set() {
        return userfield02set;
    }

    /**
     * Used to check if the userfield03 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isUserfield03set() {
        return userfield03set;
    }

    /**
     * Used to check if the userfield04 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isUserfield04set() {
        return userfield04set;
    }

    /**
     * Used to check if the userfield05 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isUserfield05set() {
        return userfield05set;
    }

    /**
     * Used to check if the userfield06 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isUserfield06set() {
        return userfield06set;
    }

    /**
     * Used to check if the userfield07 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isUserfield07set() {
        return userfield07set;
    }

    /**
     * Used to check if the userfield08 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isUserfield08set() {
        return userfield08set;
    }

    /**
     * Used to check if the userfield09 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isUserfield09set() {
        return userfield09set;
    }

    /**
     * Used to check if the userfield10 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isUserfield10set() {
        return userfield10set;
    }

    /**
     * Used to check if the userfield11 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isUserfield11set() {
        return userfield11set;
    }

    /**
     * Used to check if the userfield12 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isUserfield12set() {
        return userfield12set;
    }

    /**
     * Used to check if the userfield13 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isUserfield13set() {
        return userfield13set;
    }

    /**
     * Used to check if the userfield14 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isUserfield14set() {
        return userfield14set;
    }

    /**
     * Used to check if the userfield15 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isUserfield15set() {
        return userfield15set;
    }

    /**
     * Used to check if the userfield16 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isUserfield16set() {
        return userfield16set;
    }

    /**
     * Used to check if the userfield17 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isUserfield17set() {
        return userfield17set;
    }

    /**
     * Used to check if the userfield18 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isUserfield18set() {
        return userfield18set;
    }

    /**
     * Used to check if the userfield19 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isUserfield19set() {
        return userfield19set;
    }

    /**
     * Used to check if the userfield20 field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isUserfield20set() {
        return userfield20set;
    }

    /**
     * Used to check if the name field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isNameset() {
        return nameset;
    }

    /**
     * Sets the numeric user id
     *
     * @param userid An {@link Integer} containing the user id
     */
    @Override
    final public void setId(final Integer userid) {
        if (null == userid) {
            this.idset = true;
        }
        this.id = userid;
    }

    @Override
    final public String getName() {
        return name;
    }

    /**
     * Sets the symbolic user identifier
     *
     * @param username A {@link String} containing the user name
     */
    @Override
    final public void setName(final String username) {
        if (null == username) {
            this.nameset = true;
        }
        this.name = username;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.PasswordMechObject#getPassword()
     */
    @Override
    final public String getPassword() {
        return password;
    }

    /**
     * Sets the password for this user object. The users password must be plaintext
     *
     * @param passwd A {@link String} containing the password
     * @see setPasswordMech
     */
    final public void setPassword(final String passwd) {
        if (null == passwd) {
            this.passwordset = true;
        }
        this.password = passwd;
    }

    /**
     * Returns the primary E-Mail address of this user object
     *
     * @return A {@link String} containing the primary E-Mail address
     */
    final public String getPrimaryEmail() {
        return primaryEmail;
    }

    /**
     * Sets the primary mail address of this user object. Primary mail address is the default email
     * address of the user
     *
     * @param primaryEmail A {@link String} containing the primary E-Mail address
     */
    final public void setPrimaryEmail(final String primaryEmail) {
        if (null == primaryEmail) {
            this.primaryEmailset = true;
        }
        this.primaryEmail = primaryEmail;
    }

    /**
     * Return the last name of this user object
     *
     * @return A {@link String} containing the last name
     */
    final public String getSur_name() {
        return sur_name;
    }

    /**
     * Sets the last name of user object
     *
     * @param sur_name A {@link String} containing the last name
     */
    final public void setSur_name(final String sur_name) {
        if (null == sur_name) {
            this.sur_nameset = true;
        }
        this.sur_name = sur_name;
    }

    /**
     * Returns the the first name of user
     *
     * @return A {@link String} containing the first name
     */
    final public String getGiven_name() {
        return given_name;
    }

    /**
     * Sets the first name of user
     *
     * @param given_name A {@link String} containing the first name
     */
    final public void setGiven_name(final String given_name) {
        if (null == given_name) {
            this.given_nameset = true;
        }
        this.given_name = given_name;
    }

    /**
     * Currently not used
     *
     * @param enabled A {@link Boolean} to activate/deactivate
     */
    final public void setMailenabled(final Boolean enabled) {
        if (null == enabled) {
            this.mailenabledset = true;
        }
        this.mailenabled = enabled;
    }

    // -----------------------------------------------------------------------------------------

    public final Integer getFilestoreId() {
        return filestore_id;
    }

    public final void setFilestoreId(final Integer filestore_id) {
        this.filestore_id = filestore_id;
        this.filestore_idset = true;
    }

    public boolean isFilestore_idset() {
        return filestore_idset;
    }

    public final Integer getFilestoreOwner() {
        return filestore_owner;
    }

    public final void setFilestoreOwner(final Integer filestore_owner) {
        this.filestore_owner = filestore_owner;
        this.filestore_ownerset = true;
    }

    public boolean isFilestore_ownerset() {
        return filestore_ownerset;
    }

    /**
     * @return max Quota (in MB)
     */
    public final Long getMaxQuota() {
        return maxQuota;
    }

    /**
     *
     * @param maxQuota (in MB)
     */
    public final void setMaxQuota(final Long maxQuota) {
        this.maxQuota = maxQuota;
        this.maxQuotaset = true;
    }

    public boolean isMaxQuotaset() {
        return maxQuotaset;
    }

    /**
     * @return used Quota (in MB)
     */
    public final Long getUsedQuota() {
        return usedQuota;
    }

    public final void setUsedQuota(final Long usedQuota) {
        this.usedQuota = usedQuota;
        this.usedQuotaset = true;
    }

    public final String getFilestore_name() {
        return filestore_name;
    }

    public final void setFilestore_name(final String filestore_name) {
        this.filestore_name = filestore_name;
        this.filestore_nameset = true;
    }

    public boolean isFilestore_nameset() {
        return filestore_nameset;
    }

    // -----------------------------------------------------------------------------------------

    /**
     * Returns the birthday of this user object
     *
     * @return A {@link Date} containing the birthday
     */
    final public Date getBirthday() {
        return birthday;
    }

    /**
     * Sets the birthday for this user object
     *
     * @param birthday A {@link Date} containing the birthday
     */
    final public void setBirthday(final Date birthday) {
        if (null == birthday) {
            this.birthdayset = true;
        }
        this.birthday = birthday;
    }

    /**
     * Returns the anniversary of this user object
     *
     * @return A {@link Date} containing the anniversary
     */
    final public Date getAnniversary() {
        return anniversary;
    }

    /**
     * Sets the anniversary for this user object
     *
     * @param anniversary A {@link Date} containing the anniversary
     */
    public final void setAnniversary(final Date anniversary) {
        if (null == anniversary) {
            this.anniversaryset = true;
        }
        this.anniversary = anniversary;
    }

    /**
     * Returns the branches of this user object
     *
     * @return A {@link String} containing the branches
     */
    final public String getBranches() {
        return branches;
    }

    /**
     * Sets the branches for this user object
     *
     * @param branches A {@link String} containing the branches
     */
    final public void setBranches(final String branches) {
        if (null == branches) {
            this.branchesset = true;
        }
        this.branches = branches;
    }

    /**
     * Returns the business_category of this user object
     *
     * @return A {@link String} containing the business_category
     */
    final public String getBusiness_category() {
        return business_category;
    }

    /**
     * Sets the business_category for this user object
     *
     * @param business_category A {@link String} containing the business_category
     */
    final public void setBusiness_category(final String business_category) {
        if (null == business_category) {
            this.business_categoryset = true;
        }
        this.business_category = business_category;
    }

    /**
     * Returns the postal_code_otherset of this user object
     *
     * @return A {@link String} containing the postal_code_otherset
     */
    final public String getPostal_code_business() {
        return postal_code_business;
    }

    /**
     * Sets the postal_code_business for this user object
     *
     * @param postal_code_business A {@link String} containing the postal_code_otherset
     */
    final public void setPostal_code_business(final String postal_code_business) {
        if (null == postal_code_business) {
            this.postal_code_businessset = true;
        }
        this.postal_code_business = postal_code_business;
    }

    /**
     * Returns the state of the business of this user object
     *
     * @return A {@link String} containing the state of the business
     */
    final public String getState_business() {
        return state_business;
    }

    /**
     * Sets the state of the business for this user object
     *
     * @param state_business A {@link String} containing the state of the business
     */
    final public void setState_business(final String state_business) {
        if (null == state_business) {
            this.state_businessset = true;
        }
        this.state_business = state_business;
    }

    /**
     * Returns the street of the business of this user object
     *
     * @return A {@link String} containing the street of the business
     */
    final public String getStreet_business() {
        return street_business;
    }

    /**
     * Sets the street of the business for this user object
     *
     * @param street_business A {@link String} containing the street of the business
     */
    final public void setStreet_business(final String street_business) {
        if (null == street_business) {
            this.street_businessset = true;
        }
        this.street_business = street_business;
    }

    /**
     * Returns the telephone_callback of this user object
     *
     * @return A {@link String} containing the telephone_callback
     */
    final public String getTelephone_callback() {
        return telephone_callback;
    }

    /**
     * Sets the telephone_callback for this user object
     *
     * @param telephone_callback A {@link String} containing the telephone_callback
     */
    final public void setTelephone_callback(final String telephone_callback) {
        if (null == telephone_callback) {
            this.telephone_callbackset = true;
        }
        this.telephone_callback = telephone_callback;
    }

    /**
     * Returns the city for the home location of this user object
     *
     * @return A {@link String} containing the city for the home location
     */
    final public String getCity_home() {
        return city_home;
    }

    /**
     * Sets the city for the home location for this user object
     *
     * @param city_home A {@link String} containing the city for the home location
     */
    final public void setCity_home(final String city_home) {
        if (null == city_home) {
            this.city_homeset = true;
        }
        this.city_home = city_home;
    }

    /**
     * Returns the commercial_register of this user object
     *
     * @return A {@link String} containing commercial_register
     */
    final public String getCommercial_register() {
        return commercial_register;
    }

    /**
     * Sets the commercial_register for this user object
     *
     * @param commercial_register A {@link String} containing commercial_register
     */
    final public void setCommercial_register(final String commercial_register) {
        if (null == commercial_register) {
            this.commercial_registerset = true;
        }
        this.commercial_register = commercial_register;
    }

    /**
     * Returns the country of the home location of this user object
     *
     * @return A {@link String} containing the country of the home location
     */
    final public String getCountry_home() {
        return country_home;
    }

    /**
     * Sets the country of the home location for this user object
     *
     * @param country_home A {@link String} containing the country of the home location
     */
    final public void setCountry_home(final String country_home) {
        if (null == country_home) {
            this.country_homeset = true;
        }
        this.country_home = country_home;
    }

    /**
     * Returns the company of this user object
     *
     * @return A {@link String} containing the company
     */
    final public String getCompany() {
        return company;
    }

    /**
     * Sets the company for this user object
     *
     * @param company A {@link String} containing the company
     */
    final public void setCompany(final String company) {
        if (null == company) {
            this.companyset = true;
        }
        this.company = company;
    }

    /**
     * Returns the default group of this user object
     *
     * @return A {@link Group} object containing the default group
     */
    final public Group getDefault_group() {
        return default_group;
    }

    /**
     * The default group when creating an user. If not supplied, a default group
     * is used.
     *
     * @param default_group A {@link Group} object containing the default group
     */
    final public void setDefault_group(final Group default_group) {
        if (null == default_group) {
            this.default_groupset = true;
        }
        this.default_group = default_group;
    }

    /**
     * Returns the department of this user object
     *
     * @return A {@link String} containing the department
     */
    final public String getDepartment() {
        return department;
    }

    /**
     * Sets the department for this user object
     *
     * @param department A {@link String} containing the department
     */
    final public void setDepartment(final String department) {
        if (null == department) {
            this.departmentset = true;
        }
        this.department = department;
    }

    /**
     * Returns the display name of this user object
     *
     * @return A {@link String} containing the display name
     */
    final public String getDisplay_name() {
        return display_name;
    }

    /**
     * Sets the display name for this user object
     *
     * @param display_name A {@link String} containing the display name
     */
    final public void setDisplay_name(final String display_name) {
        if (null == display_name) {
            this.display_nameset = true;
        }
        this.display_name = display_name;
    }

    /**
     * Returns the home E-Mail of this user object
     *
     * @return A {@link String} containing the home E-Mail
     */
    final public String getEmail2() {
        return email2;
    }

    /**
     * Sets the home E-Mail for this user object
     *
     * @param email2 A {@link String} containing the home E-Mail
     */
    final public void setEmail2(final String email2) {
        if (null == email2) {
            this.email2set = true;
        }
        this.email2 = email2;
    }

    /**
     * Returns the E-Mail (other) of this user object
     *
     * @return A {@link String} containing the E-Mail (other)
     */
    final public String getEmail3() {
        return email3;
    }

    /**
     * Sets the E-Mail (other) for this user object
     *
     * @param email3 A {@link String} containing the E-Mail (other)
     */
    final public void setEmail3(final String email3) {
        if (null == email3) {
            this.email3set = true;
        }
        this.email3 = email3;
    }

    /**
     * Returns the job title of this user object
     *
     * @return A {@link String} containing the job title
     */
    final public String getEmployeeType() {
        return employeeType;
    }

    /**
     * Sets the job title for this user object
     *
     * @param employeeType A {@link String} containing the job title
     */
    final public void setEmployeeType(final String employeeType) {
        if (null == employeeType) {
            this.employeeTypeset = true;
        }
        this.employeeType = employeeType;
    }

    /**
     * Returns the fax number for the business location of this user object
     *
     * @return A {@link String} containing the fax number for the business location
     */
    final public String getFax_business() {
        return fax_business;
    }

    /**
     * Sets the fax number for the business location for this user object
     *
     * @param fax_business A {@link String} containing the fax number for the business location
     */
    final public void setFax_business(final String fax_business) {
        if (null == fax_business) {
            this.fax_businessset = true;
        }
        this.fax_business = fax_business;
    }

    /**
     * Returns the fax number for the home location of this user object
     *
     * @return A {@link String} containing the fax number for the home location
     */
    final public String getFax_home() {
        return fax_home;
    }

    /**
     * Sets the fax number for the home location for this user object
     *
     * @param fax_home A {@link String} containing the fax number for the home location
     */
    final public void setFax_home(final String fax_home) {
        if (null == fax_home) {
            this.fax_homeset = true;
        }
        this.fax_home = fax_home;
    }

    /**
     * Returns the fax number for a further location of this user object
     *
     * @return A {@link String} containing the fax number for a further location
     */
    final public String getFax_other() {
        return fax_other;
    }

    /**
     * Sets the fax number for a further location for this user object
     *
     * @param fax_other A {@link String} containing the fax number for a further location
     */
    final public void setFax_other(final String fax_other) {
        if (null == fax_other) {
            this.fax_otherset = true;
        }
        this.fax_other = fax_other;
    }

    /**
     * Returns the port of the imap server of this user object
     *
     * @return An {@link int} containing the port number
     * @deprecated since 6.20. Use {@link #getImapServerString()} instead.
     */
    @Deprecated
    final public int getImapPort() {
        if (this.imapServer != null) {
            try {
                return URIParser.parse(imapServer, URIDefaults.IMAP).getPort();
            } catch (final URISyntaxException e) {
                // Ignore
            }
        }
        return 143;
    }

    /**
     * Returns the hostname for the imap server of this user object
     *
     * @return A {@link String} containing the hostname for the imap server
     * @deprecated since 6.20. Use {@link #getImapServerString()} instead.
     */
    @Deprecated
    final public String getImapServer() {
        if (this.imapServer != null) {
            try {
                return URIParser.parse(imapServer, URIDefaults.IMAP).getHost();
            } catch (final URISyntaxException e) {
                // Ignore
            }
        }
        return null;
    }

    /**
     * Returns the URL of the IMAP server of this user object. This method will once be replaced by the {@link #getImapServer()} method to
     * get a Bean style conform API.
     *
     * @return A {@link String} containing the URL for the IMAP server.
     */
    public final String getImapServerString() {
        return imapServer;
    }

    /**
     * Returns the schema part of the imap server url of this user object
     *
     * @return A {@link String} containing the schema of the imap server url
     * @deprecated since 6.20. Use {@link #getImapServerString()} instead.
     */
    @Deprecated
    final public String getImapSchema() {
        if (this.imapServer != null) {
            try {
                final String scheme = URIParser.parse(imapServer, URIDefaults.IMAP).getScheme();
                return null == scheme ? "imap://" : scheme + "://";
            } catch (final URISyntaxException e) {
                // Ignore
            }
        }
        return "imap://";
    }

    /**
     * Sets the users imap server if not localhost should be used. Syntax of
     * imap server String: HOSTNAME[:PORT] if PORT is omitted, the default port
     * is used
     *
     * Note: to get used imap server and port, the methods getImapPort and
     * getImapServer are used
     *
     * @param imapServer A {@link String} containing the imap server
     */
    final public void setImapServer(final String imapServer) {
        if (null == imapServer) {
            this.imapServerset = true;
        }
        this.imapServer = imapServer;
    }

    /**
     * Sets the login for the imap server for this user object
     *
     * @param imapLogin A {@link String} containing the login
     */
    final public void setImapLogin(final String imapLogin) {
        if (null == imapLogin) {
            this.imapLoginset = true;
        }
        this.imapLogin = imapLogin;
    }

    /**
     * Returns the login for the imap server of this user object
     *
     * @return A {@link String} containing the login
     */
    final public String getImapLogin() {
        return this.imapLogin;
    }

    /**
     * Returns the hostname for the smtp server of this user object
     *
     * @return A {@link String} containing the hostname for the smtp server
     * @deprecated since 6.20. Use {@link #getSmtpServerString()} instead.
     */
    @Deprecated
    final public String getSmtpServer() {
        if (this.smtpServer != null) {
            try {
                return URIParser.parse(smtpServer, URIDefaults.SMTP).getHost();
            } catch (final URISyntaxException e) {
                // Ignore
            }
        }
        return null;
    }

    /**
     * Returns the schema part of the smtp server url of this user object
     *
     * @return A {@link String} containing the schema of the smtp server url
     * @deprecated since 6.20. Use {@link #getSmtpServerString()} instead.
     */
    @Deprecated
    final public String getSmtpSchema() {
        if (this.smtpServer != null) {
            try {
                final String scheme = URIParser.parse(smtpServer, URIDefaults.SMTP).getScheme();
                return null == scheme ? "smtp://" : scheme + "://";
            } catch (final URISyntaxException e) {
                // Ignore
            }
        }
        return "smtp://";
    }

    /**
     * Returns the URL of the SMTP server of this user object. This method will once be replaced by the {@link #getImapServer()} method to
     * get a Bean style conform API.
     *
     * @return A {@link String} containing the URL for the SMTP server.
     */
    public final String getSmtpServerString() {
        return smtpServer;
    }

    /**
     * Set the users smtp server if not localhost should be used. Syntax of
     * smtp server String: HOSTNAME[:PORT] if PORT is omitted, the default port
     * is used
     *
     * Note: to get used smtp server and port, the methods getSmtpPort and
     * getSmtpServer are used
     *
     * @param smtpServer A {@link String} containting the smtp server
     */
    final public void setSmtpServer(final String smtpServer) {
        if (null == smtpServer) {
            this.smtpServerset = true;
        }
        this.smtpServer = smtpServer;
    }

    /**
     * Returns the port for the smtp server of this user object
     *
     * @return An {@link int} containing the port for the smtp server
     * @deprecated since 6.20. Use {@link #getSmtpServerString()} instead.
     */
    @Deprecated
    final public int getSmtpPort() {
        if (this.smtpServer != null) {
            try {
                return URIParser.parse(smtpServer, URIDefaults.SMTP).getPort();
            } catch (final URISyntaxException e) {
                // Ignore
            }
        }
        return 25;
    }

    /**
     * Returns the instant messenger address for the business location of this user object
     *
     * @return A {@link String} containing the instant messenger address
     */
    final public String getInstant_messenger1() {
        return instant_messenger1;
    }

    /**
     * Sets the instant messenger address for the business location for this user object
     *
     * @param instant_messenger1 A {@link String} containing the instant messenger address
     */
    final public void setInstant_messenger1(final String instant_messenger1) {
        if (null == instant_messenger1) {
            this.instant_messenger1set = true;
        }
        this.instant_messenger1 = instant_messenger1;
    }

    /**
     * Returns the instant messenger address for the private location of this user object
     *
     * @return A {@link String} containing the instant messenger address for the private location
     */
    final public String getInstant_messenger2() {
        return instant_messenger2;
    }

    /**
     * Sets the instant messenger address for the private location for this user object
     *
     * @param instant_messenger2 A {@link String} containing the instant messenger address for the private location
     */
    final public void setInstant_messenger2(final String instant_messenger2) {
        if (null == instant_messenger2) {
            this.instant_messenger2set = true;
        }
        this.instant_messenger2 = instant_messenger2;
    }

    /**
     * Returns the telephone number for ip telephony of this user object
     *
     * @return A {@link String} containing the telephone number for ip telephony
     */
    final public String getTelephone_ip() {
        return telephone_ip;
    }

    /**
     * Sets the telephone number for ip telephony for this user object
     *
     * @param telephone_ip A {@link String} containing the telephone number for ip telephony
     */
    final public void setTelephone_ip(final String telephone_ip) {
        if (null == telephone_ip) {
            this.telephone_ipset = true;
        }
        this.telephone_ip = telephone_ip;
    }

    /**
     * Returns the telephone number for isdn telephony of this user object
     *
     * @return A {@link String} containing the telephone number for isdn telephony
     */
    final public String getTelephone_isdn() {
        return telephone_isdn;
    }

    /**
     * Sets the telephone number for isdn telephony for this user object
     *
     * @param telephone_isdn A {@link String} containing the telephone number for isdn telephony
     */
    final public void setTelephone_isdn(final String telephone_isdn) {
        if (null == telephone_isdn) {
            this.telephone_isdnset = true;
        }
        this.telephone_isdn = telephone_isdn;
    }

    /**
     * Returns the language setting of this user object
     *
     * @return A {@link String} object containing the language setting
     */
    final public String getLanguage() {
        return language;
    }

    /**
     * Sets the language for this user object. Note: Language must be constructed like
     *   <language>_<COUNTRYCODE>
     * See
     *  http://www.loc.gov/standards/iso639-2/englangn.html for possible values of <language> and
     *  http://www.iso.org/iso/country_codes/iso_3166_code_lists/english_country_names_and_code_elements.htm
     *  for possible values of <COUNTRYCODE>
     *  NOTE: Of course not all variants are supported by OX
     *
     * @param language A {@link String} object containing the language setting
     */
    final public void setLanguage(final String language) {
        if (null == language) {
            this.languageset = true;
        }
        this.language = language;
    }

    /**
     * Returns the mail folder name of the drafts folder of this user object
     *
     * @return A {@link String} containing the mail folder name of the drafts folder
     */
    final public String getMail_folder_drafts_name() {
        return mail_folder_drafts_name;
    }

    /**
     * Sets the mail folder name of the drafts folder for this user object
     *
     * @param mail_folder_drafts_name A {@link String} containing the mail folder name of the drafts folder
     */
    final public void setMail_folder_drafts_name(final String mail_folder_drafts_name) {
        if (null == mail_folder_drafts_name) {
            this.mail_folder_drafts_nameset = true;
        }
        this.mail_folder_drafts_name = mail_folder_drafts_name;
    }

    /**
     * Returns the mail folder name of the sent folder of this user object
     *
     * @return A {@link String} containing the mail folder name of the sent folder
     */
    final public String getMail_folder_sent_name() {
        return mail_folder_sent_name;
    }

    /**
     * Sets the mail folder name of the sent folder for this user object
     *
     * @param mail_folder_sent_name A {@link String} containing the mail folder name of the sent folder
     */
    final public void setMail_folder_sent_name(final String mail_folder_sent_name) {
        if (null == mail_folder_sent_name) {
            this.mail_folder_sent_nameset = true;
        }
        this.mail_folder_sent_name = mail_folder_sent_name;
    }

    /**
     * Returns the mail folder name of the spam folder of this user object
     *
     * @return A {@link String} containing the mail folder name of the spam folder
     */
    final public String getMail_folder_spam_name() {
        return mail_folder_spam_name;
    }

    /**
     * Sets the name of the users SPAM folder where detected SPAM mail will be
     * moved into.
     *
     * @param mail_folder_spam_name A {@link String} containing the mail folder name of the spam folder
     */
    final public void setMail_folder_spam_name(final String mail_folder_spam_name) {
        if (null == mail_folder_spam_name) {
            this.mail_folder_spam_nameset = true;
        }
        this.mail_folder_spam_name = mail_folder_spam_name;
    }

    /**
     * Returns the mail folder name of the trash folder of this user object
     *
     * @return A {@link String} containing the mail folder name of the trash folder
     */
    final public String getMail_folder_trash_name() {
        return mail_folder_trash_name;
    }

    /**
     * Sets the mail folder name of the trash folder of this user object
     *
     * @param mail_folder_trash_name A {@link String} containing the mail folder name of the trash folder
     */
    final public void setMail_folder_trash_name(final String mail_folder_trash_name) {
        if (null == mail_folder_trash_name) {
            this.mail_folder_trash_nameset = true;
        }
        this.mail_folder_trash_name = mail_folder_trash_name;
    }

    /**
     * Returns the mail folder full-name of the archive folder of this user object
     *
     * @return A {@link String} containing the mail folder full-name of the archive folder
     */
    final public String getMail_folder_archive_full_name() {
        return mail_folder_archive_full_name;
    }

    /**
     * Sets the mail folder full-name of the archive folder of this user object
     *
     * @param mail_folder_archive_full_name A {@link String} containing the mail folder full-name of the archive folder
     */
    final public void setMail_folder_archive_full_name(final String mail_folder_archive_full_name) {
        if (null == mail_folder_archive_full_name) {
            this.mail_folder_archive_full_nameset = true;
        }
        this.mail_folder_archive_full_name = mail_folder_archive_full_name;
    }

    /**
     * Returns the name of the manager of this user object
     *
     * @return A {@link String} containing the name of the manager
     */
    final public String getManager_name() {
        return manager_name;
    }

    /**
     * Sets the name of the manager for this user object
     *
     * @param manager_name A {@link String} containing the name of the manager
     */
    final public void setManager_name(final String manager_name) {
        if (null == manager_name) {
            this.manager_nameset = true;
        }
        this.manager_name = manager_name;
    }

    /**
     * Returns the marital status of this user object
     *
     * @return A {@link String} containing the marital status
     */
    final public String getMarital_status() {
        return marital_status;
    }

    /**
     * Sets the marital status for this user object
     *
     * @param marital_status A {@link String} containing the marital status
     */
    final public void setMarital_status(final String marital_status) {
        if (null == marital_status) {
            this.marital_statusset = true;
        }
        this.marital_status = marital_status;
    }

    /**
     * Returns the mobile phone number of this user object
     *
     * @return A {@link String} containing the mobile phone number
     */
    final public String getCellular_telephone1() {
        return cellular_telephone1;
    }

    /**
     * Sets the mobile phone number for this user object
     *
     * @param cellular_telephone1 A {@link String} containing the mobile phone number
     */
    final public void setCellular_telephone1(final String cellular_telephone1) {
        if (null == cellular_telephone1) {
            this.cellular_telephone1set = true;
        }
        this.cellular_telephone1 = cellular_telephone1;
    }

    /**
     * Returns the second mobile phone number of this user object
     *
     * @return A {@link String} containing the second mobile phone number
     */
    final public String getCellular_telephone2() {
        return cellular_telephone2;
    }

    /**
     * Sets the second mobile phone number for this user object
     *
     * @param cellular_telephone2 A {@link String} containing the second mobile phone number
     */
    final public void setCellular_telephone2(final String cellular_telephone2) {
        if (null == cellular_telephone2) {
            this.cellular_telephone2set = true;
        }
        this.cellular_telephone2 = cellular_telephone2;
    }

    /**
     * Returns the info field of this user object
     *
     * @return A {@link String} containing the info field
     */
    final public String getInfo() {
        return info;
    }

    /**
     * Sets the info field for this user object
     *
     * @param info A {@link String} containing the info field
     */
    final public void setInfo(final String info) {
        if (null == info) {
            this.infoset = true;
        }
        this.info = info;
    }

    /**
     * Returns the nickname of this user object
     *
     * @return A {@link String} containing the nickname
     */
    final public String getNickname() {
        return nickname;
    }

    /**
     * Sets the nickname of this user object
     *
     * @param nickname A {@link String} containing the nickname
     */
    final public void setNickname(final String nickname) {
        if (null == nickname) {
            this.nicknameset = true;
        }
        this.nickname = nickname;
    }

    /**
     * Returns the number of children of this user object
     *
     * @return A {@link String} containing the number of children
     */
    final public String getNumber_of_children() {
        return number_of_children;
    }

    /**
     * Sets the number of children for this user object
     *
     * @param number_of_children A {@link String} containing the number of children
     */
    final public void setNumber_of_children(final String number_of_children) {
        if (null == number_of_children) {
            this.number_of_childrenset = true;
        }
        this.number_of_children = number_of_children;
    }

    /**
     * Returns the note field of this user object
     *
     * @return A {@link String} containing the note field
     */
    final public String getNote() {
        return note;
    }

    /**
     * Sets the note field for this user object
     *
     * @param note A {@link String} containing the note field
     */
    final public void setNote(final String note) {
        if (null == note) {
            this.noteset = true;
        }
        this.note = note;
    }

    /**
     * Returns the employee identifier of this user object
     *
     * @return A {@link String} containing the employee identifier
     */
    final public String getNumber_of_employee() {
        return number_of_employee;
    }

    /**
     * Sets the employee identifier for this user object
     *
     * @param A {@link String} containing the employee identifier
     */
    final public void setNumber_of_employee(final String number_of_employee) {
        if (null == number_of_employee) {
            this.number_of_employeeset = true;
        }
        this.number_of_employee = number_of_employee;
    }

    /**
     * Returns the number of the telephone pager of this user object
     *
     * @return A {@link String} containing the number of the telephone pager
     */
    final public String getTelephone_pager() {
        return telephone_pager;
    }

    /**
     * Sets the number of the telephone pager of this user object
     *
     * @param telephone_pager A {@link String} containing the number of the telephone pager
     */
    final public void setTelephone_pager(final String telephone_pager) {
        if (null == telephone_pager) {
            this.telephone_pagerset = true;
        }
        this.telephone_pager = telephone_pager;
    }

    /**
     * Returns if the password of this user object is expired
     *
     * @return true if the password is expired; false otherwise
     */
    final public Boolean getPassword_expired() {
        return password_expired;
    }

    /**
     * Sets if the password for this user object is expired
     *
     * @param password_expired A {@link Boolean} containing the value
     */
    final public void setPassword_expired(final Boolean password_expired) {
        if (null == password_expired) {
            this.password_expiredset = true;
        }
        this.password_expired = password_expired;
    }

    /**
     * Returns the telephone number of the assistant of this user object
     *
     * @return A {@link String} containing the telephone number of the assistant
     */
    final public String getTelephone_assistant() {
        return telephone_assistant;
    }

    /**
     * Sets the telephone number of the assistant for this user object
     *
     * @param telephone_assistant A {@link String} containing the telephone number of the assistant
     */
    final public void setTelephone_assistant(final String telephone_assistant) {
        if (null == telephone_assistant) {
            this.telephone_assistantset = true;
        }
        this.telephone_assistant = telephone_assistant;
    }

    /**
     * Returns the telephone number of the business location of this user object
     *
     * @return A {@link String} containing the telephone number of the business location
     */
    final public String getTelephone_business1() {
        return telephone_business1;
    }

    /**
     * Sets the telephone number of the business location for this user object
     *
     * @param telephone_business1 A {@link String} containing the telephone number of the business location
     */
    final public void setTelephone_business1(final String telephone_business1) {
        if (null == telephone_business1) {
            this.telephone_business1set = true;
        }
        this.telephone_business1 = telephone_business1;
    }

    /**
     * Returns the second telephone number of the business location of this user object
     *
     * @return A {@link String} containing the second telephone number of the business location
     */
    final public String getTelephone_business2() {
        return telephone_business2;
    }

    /**
     * Sets the second telephone number of the business location for this user object
     *
     * @param telephone_business2 A {@link String} containing the second telephone number of the business location
     */
    final public void setTelephone_business2(final String telephone_business2) {
        if (null == telephone_business2) {
            this.telephone_business2set = true;
        }
        this.telephone_business2 = telephone_business2;
    }

    /**
     * Returns the telephone number for the car phone of this user object
     *
     * @return A {@link String} containing the telephone number for the car phone
     */
    final public String getTelephone_car() {
        return telephone_car;
    }

    /**
     * Sets the telephone number for the car phone for this user object
     *
     * @param telephone_car A {@link String} containing the telephone number for the car phone
     */
    final public void setTelephone_car(final String telephone_car) {
        if (null == telephone_car) {
            this.telephone_carset = true;
        }
        this.telephone_car = telephone_car;
    }

    /**
     * Returns the telephone number of the company of this user object
     *
     * @return A {@link String} containing the telephone number of the company
     */
    final public String getTelephone_company() {
        return telephone_company;
    }

    /**
     * Sets the telephone number of the company for this user object
     *
     * @param telephone_company A {@link String} containing the telephone number of the company
     */
    final public void setTelephone_company(final String telephone_company) {
        if (null == telephone_company) {
            this.telephone_companyset = true;
        }
        this.telephone_company = telephone_company;
    }

    /**
     * Returns the first telephone number of the home location of this user object
     *
     * @return A {@link String} containing the first telephone number of the home location
     */
    final public String getTelephone_home1() {
        return telephone_home1;
    }

    /**
     * Sets the first telephone number of the home location for this user object
     *
     * @param telephone_home1 A {@link String} containing the first telephone number of the home location
     */
    final public void setTelephone_home1(final String telephone_home1) {
        if (null == telephone_home1) {
            this.telephone_home1set = true;
        }
        this.telephone_home1 = telephone_home1;
    }

    /**
     * Returns the second telephone number of the home location of this user object
     *
     * @return A {@link String} containing the second telephone number of the home location
     */
    final public String getTelephone_home2() {
        return telephone_home2;
    }

    /**
     * Sets the second telephone number of the home location for this user object
     *
     * @param telephone_home2 A {@link String} containing the second telephone number of the home location
     */
    final public void setTelephone_home2(final String telephone_home2) {
        if (null == telephone_home2) {
            this.telephone_home2set = true;
        }
        this.telephone_home2 = telephone_home2;
    }

    /**
     * Returns a further specified telephone number of this user object
     *
     * @return A {@link String} containing the number
     */
    final public String getTelephone_other() {
        return telephone_other;
    }

    /**
     * Sets the further telephone number for this user object
     *
     * @param telephone_other A {@link String} containing the further telephone number
     */
    final public void setTelephone_other(final String telephone_other) {
        if (null == telephone_other) {
            this.telephone_otherset = true;
        }
        this.telephone_other = telephone_other;
    }

    /**
     * Returns the position field of this user object
     *
     * @return A {@link String} containing the position
     */
    final public String getPosition() {
        return position;
    }

    /**
     * Sets the position field for this user object
     *
     * @param position A {@link String} containing the position field
     */
    final public void setPosition(final String position) {
        if (null == position) {
            this.positionset = true;
        }
        this.position = position;
    }

    /**
     * Returns the postal code of the home location of this user object
     *
     * @return A {@link String} containing the postal code of the home location
     */
    final public String getPostal_code_home() {
        return postal_code_home;
    }

    /**
     * Sets the postal code of the home location for this user object
     *
     * @param postal_code_home A {@link String} containing the postal code of the home location
     */
    final public void setPostal_code_home(final String postal_code_home) {
        if (null == postal_code_home) {
            this.postal_code_homeset = true;
        }
        this.postal_code_home = postal_code_home;
    }

    /**
     * Returns the profession of this user object
     *
     * @return A {@link String} containing the profession
     */
    final public String getProfession() {
        return profession;
    }

    /**
     * Sets the profession for this user object
     *
     * @param profession A {@link String} containing the profession
     */
    final public void setProfession(final String profession) {
        if (null == profession) {
            this.professionset = true;
        }
        this.profession = profession;
    }

    /**
     * Currently not used
     *
     * @return A {@link String} containing the field content
     */
    final public String getTelephone_radio() {
        return telephone_radio;
    }

    /**
     * Currently not used
     *
     * @param test2 A {@link String} containing the field content
     */
    final public void setTelephone_radio(final String telephone_radio) {
        if (null == telephone_radio) {
            this.telephone_radioset = true;
        }
        this.telephone_radio = telephone_radio;
    }

    /**
     * Returns the room number of this user object
     *
     * @return A {@link String} containing the room number
     */
    final public String getRoom_number() {
        return room_number;
    }

    /**
     * Sets the room number for this user object
     *
     * @param room_number A {@link String} containing the room number
     */
    final public void setRoom_number(final String room_number) {
        if (null == room_number) {
            this.room_numberset = true;
        }
        this.room_number = room_number;
    }

    /**
     * Returns the sales volume of this user object
     *
     * @return A {@link String} containing the sales volume
     */
    final public String getSales_volume() {
        return sales_volume;
    }

    /**
     * Sets the sales volume for this user object
     *
     * @param sales_volume A {@link String} containing the sales volume
     */
    final public void setSales_volume(final String sales_volume) {
        if (null == sales_volume) {
            this.sales_volumeset = true;
        }
        this.sales_volume = sales_volume;
    }

    /**
     * Returns the city for a further location of this user object
     *
     * @return A {@link String} containing the city for a further location
     */
    final public String getCity_other() {
        return city_other;
    }

    /**
     * Sets the city for a further location for this user object
     *
     * @param city_other A {@link String} containing the city for a further location
     */
    final public void setCity_other(final String city_other) {
        if (null == city_other) {
            this.city_otherset = true;
        }
        this.city_other = city_other;
    }

    /**
     * Returns the country for a further location of this user object
     *
     * @return A {@link String} containing the country for a further location
     */
    final public String getCountry_other() {
        return country_other;
    }

    /**
     * Sets the country for a further location for this user object
     *
     * @param country_other A {@link String} containing the country for a further location
     */
    final public void setCountry_other(final String country_other) {
        if (null == country_other) {
            this.country_otherset = true;
        }
        this.country_other = country_other;
    }

    /**
     * Returns the middle name of this user object
     *
     * @return A {@link String} containing the middle name
     */
    final public String getMiddle_name() {
        return middle_name;
    }

    /**
     * Sets the middle name for this user object
     *
     * @param middle_name A {@link String} containing the middle name
     */
    final public void setMiddle_name(final String middle_name) {
        if (null == middle_name) {
            this.middle_nameset = true;
        }
        this.middle_name = middle_name;
    }

    /**
     * Returns the postal code for a further location of this user object
     *
     * @return A {@link String} containing the postal code for a further location
     */
    final public String getPostal_code_other() {
        return postal_code_other;
    }

    /**
     * Sets the postal code for a further location for this user object
     *
     * @param postal_code_other A {@link String} containing the postal code for a further location
     */
    final public void setPostal_code_other(final String postal_code_other) {
        if (null == postal_code_other) {
            this.postal_code_otherset = true;
        }
        this.postal_code_other = postal_code_other;
    }

    /**
     * Returns the state for a further location of this user object
     *
     * @return A {@link String} containing the state for a further location
     */
    final public String getState_other() {
        return state_other;
    }

    /**
     * Sets the state for a further location for this user object
     *
     * @param state_other A {@link String} containing the state for a further location
     */
    final public void setState_other(final String state_other) {
        if (null == state_other) {
            this.state_otherset = true;
        }
        this.state_other = state_other;
    }

    /**
     * Returns the street for a further location of this user object
     *
     * @return A {@link String} containing the street for a further location
     */
    final public String getStreet_other() {
        return street_other;
    }

    /**
     * Sets the street for a further location for this user object
     *
     * @param street_other A {@link String} containing the street for a further location
     */
    final public void setStreet_other(final String street_other) {
        if (null == street_other) {
            this.street_otherset = true;
        }
        this.street_other = street_other;
    }

    /**
     * Returns the name of the spouse of this user object
     *
     * @return A {@link String} containing the name of the spouse
     */
    final public String getSpouse_name() {
        return spouse_name;
    }

    /**
     * Sets the name of the spouse for this user object
     *
     * @param spouse_name A {@link String} containing the name of the spouse
     */
    final public void setSpouse_name(final String spouse_name) {
        if (null == spouse_name) {
            this.spouse_nameset = true;
        }
        this.spouse_name = spouse_name;
    }

    /**
     * Returns the state of the home location of this user object
     *
     * @return A {@link String} containing the state of the home location
     */
    final public String getState_home() {
        return state_home;
    }

    /**
     * Sets the state of the home location for this user object
     *
     * @param state_home A {@link String} containing the state of the home location
     */
    final public void setState_home(final String state_home) {
        if (null == state_home) {
            this.state_homeset = true;
        }
        this.state_home = state_home;
    }

    /**
     * Returns the street of the home location of this user object
     *
     * @return A {@link String} containing the street of the home location
     */
    final public String getStreet_home() {
        return street_home;
    }

    /**
     * Sets the street of the home location for this user object
     *
     * @param street_home A {@link String} containing the street of the home location
     */
    final public void setStreet_home(final String street_home) {
        if (null == street_home) {
            this.street_homeset = true;
        }
        this.street_home = street_home;
    }

    /**
     * Returns the suffix of this user object
     *
     * @return A {@link String} containing the suffix
     */
    final public String getSuffix() {
        return suffix;
    }

    /**
     * Sets the suffix for this user object
     *
     * @param suffix A {@link String} containing the suffix
     */
    final public void setSuffix(final String suffix) {
        if (null == suffix) {
            this.suffixset = true;
        }
        this.suffix = suffix;
    }

    /**
     * Returns the tax id of this user object
     *
     * @return A {@link String} containing the tax id
     */
    final public String getTax_id() {
        return tax_id;
    }

    /**
     * Sets the tax id for this user object
     *
     * @param tax_id A {@link String} containing the tax id
     */
    final public void setTax_id(final String tax_id) {
        if (null == tax_id) {
            this.tax_idset = true;
        }
        this.tax_id = tax_id;
    }

    /**
     * Returns the telephone number for the telex of this user object
     *
     * @return A {@link String} containing the telephone number for the telex
     */
    final public String getTelephone_telex() {
        return telephone_telex;
    }

    /**
     * Sets the telephone number for the telex for this user object
     *
     * @param telephone_telex A {@link String} containing the telephone number for the telex
     */
    final public void setTelephone_telex(final String telephone_telex) {
        if (null == telephone_telex) {
            this.telephone_telexset = true;
        }
        this.telephone_telex = telephone_telex;
    }

    /**
     * Returns the timezone of this user object
     *
     * @return A {@link String} containing the timezone
     */
    public final String getTimezone() {
        return timezone;
    }

    /**
     * Sets the timezone for this user object.
     * See
     * http://java.sun.com/j2se/1.5.0/docs/api/java/util/TimeZone.html
     * for possible Timezone Strings
     *
     * @param timezone A {@link String} containing the timezone
     */
    final public void setTimezone(final String timezone) {
        if (null == timezone) {
            this.timezoneset = true;
        }
        this.timezone = timezone;
    }

    /**
     * Returns the title of this user object
     *
     * @return A {@link String} containing the title
     */
    final public String getTitle() {
        return title;
    }

    /**
     * Sets the title for this user object
     *
     * @param title A {@link String} containing the title
     */
    final public void setTitle(final String title) {
        if (null == title) {
            this.titleset = true;
        }
        this.title = title;
    }

    /**
     * Returns the telephone number for the TTY/TDD of this user object
     *
     * @return A {@link String} containing the telephone number for the TTY/TDD
     */
    final public String getTelephone_ttytdd() {
        return telephone_ttytdd;
    }

    /**
     * Sets the telephone number for the TTY/TDD for this user object
     *
     * @param telephone_ttytdd A {@link String} containing the telephone number for the TTY/TDD
     */
    final public void setTelephone_ttytdd(final String telephone_ttytdd) {
        if (null == telephone_ttytdd) {
            this.telephone_ttytddset = true;
        }
        this.telephone_ttytdd = telephone_ttytdd;
    }

    /**
     * Returns the upload file size limit of this user object
     *
     * @return An {@link int} containing the upload file size limit
     */
    final public Integer getUploadFileSizeLimit() {
        return uploadFileSizeLimit;
    }

    /**
     * Sets the upload file size limit for this user object
     *
     * @param upload_quota An {@link int} containing the upload file size limit
     */
    final public void setUploadFileSizeLimit(final Integer upload_quota) {
        if (null == upload_quota) {
            this.uploadFileSizeLimitset = true;
        }
        this.uploadFileSizeLimit = upload_quota;
    }

    /**
     * Returns the upload file size limit per file of this user object
     *
     * @return An {@link int} containing the upload file size limit per file
     */
    final public Integer getUploadFileSizeLimitPerFile() {
        return uploadFileSizeLimitPerFile;
    }

    /**
     * Sets the upload file size limit per file for this user object
     *
     * @param upload_quota_per_file An {@link int} containing the upload file size limit per file
     */
    final public void setUploadFileSizeLimitPerFile(final Integer upload_quota_per_file) {
        if (null == upload_quota_per_file) {
            this.uploadFileSizeLimitPerFileset = true;
        }
        this.uploadFileSizeLimitPerFile = upload_quota_per_file;
    }

    /**
     * Returns the URL of this user object
     *
     * @return A {@link String} containing the URL
     */
    final public String getUrl() {
        return url;
    }

    /**
     * Sets the URL for this user object
     *
     * @param url A {@link String} containing the URL
     */
    final public void setUrl(final String url) {
        if (null == url) {
            this.urlset = true;
        }
        this.url = url;
    }

    /**
     * Returns the dynamic field no 1 of this user object
     *
     * @return A {@link String} containing the dynamic field no 1
     */
    final public String getUserfield01() {
        return userfield01;
    }

    /**
     * Sets the dynamic field no 1 for this user object
     *
     * @param userfield01 A {@link String} containing the dynamic field no 1
     */
    final public void setUserfield01(final String userfield01) {
        if (null == userfield01) {
            this.userfield01set = true;
        }
        this.userfield01 = userfield01;
    }

    /**
     * Returns the dynamic field no 2 of this user object
     *
     * @return A {@link String} containing the dynamic field no 2
     */
    final public String getUserfield02() {
        return userfield02;
    }

    /**
     * Sets the dynamic field no 2 for this user object
     *
     * @param userfield02 A {@link String} containing the dynamic field no 2
     */
    final public void setUserfield02(final String userfield02) {
        if (null == userfield02) {
            this.userfield02set = true;
        }
        this.userfield02 = userfield02;
    }

    /**
     * Returns the dynamic field no 3 of this user object
     *
     * @return A {@link String} containing the dynamic field no 3
     */
    final public String getUserfield03() {
        return userfield03;
    }

    /**
     * Sets the dynamic field no 3 for this user object
     *
     * @param userfield03 A {@link String} containing the dynamic field no 3
     */
    final public void setUserfield03(final String userfield03) {
        if (null == userfield03) {
            this.userfield03set = true;
        }
        this.userfield03 = userfield03;
    }

    /**
     * Returns the dynamic field no 4 of this user object
     *
     * @return A {@link String} containing the dynamic field no 4
     */
    final public String getUserfield04() {
        return userfield04;
    }

    /**
     * Sets the dynamic field no 4 for this user object
     *
     * @param userfield04 A {@link String} containing the dynamic field no 4
     */
    final public void setUserfield04(final String userfield04) {
        if (null == userfield04) {
            this.userfield04set = true;
        }
        this.userfield04 = userfield04;
    }

    /**
     * Returns the dynamic field no 5 of this user object
     *
     * @return A {@link String} containing the dynamic field no 5
     */
    final public String getUserfield05() {
        return userfield05;
    }

    /**
     * Sets the dynamic field no 5 for this user object
     *
     * @param userfield05 A {@link String} containing the dynamic field no 5
     */
    final public void setUserfield05(final String userfield05) {
        if (null == userfield05) {
            this.userfield05set = true;
        }
        this.userfield05 = userfield05;
    }

    /**
     * Returns the dynamic field no 6 of this user object
     *
     * @return A {@link String} containing the dynamic field no 6
     */
    final public String getUserfield06() {
        return userfield06;
    }

    /**
     * Sets the dynamic field no 6 for this user object
     *
     * @param userfield06 A {@link String} containing the dynamic field no 6
     */
    final public void setUserfield06(final String userfield06) {
        if (null == userfield06) {
            this.userfield06set = true;
        }
        this.userfield06 = userfield06;
    }

    /**
     * Returns the dynamic field no 7 of this user object
     *
     * @return A {@link String} containing the dynamic field no 7
     */
    final public String getUserfield07() {
        return userfield07;
    }

    /**
     * Sets the dynamic field no 7 for this user object
     *
     * @param userfield07 A {@link String} containing the dynamic field no 7
     */
    final public void setUserfield07(final String userfield07) {
        if (null == userfield07) {
            this.userfield07set = true;
        }
        this.userfield07 = userfield07;
    }

    /**
     * Returns the dynamic field no 8 of this user object
     *
     * @return A {@link String} containing the dynamic field no 8
     */
    final public String getUserfield08() {
        return userfield08;
    }

    /**
     * Sets the dynamic field no 8 for this user object
     *
     * @param userfield08 A {@link String} containing the dynamic field no 8
     */
    final public void setUserfield08(final String userfield08) {
        if (null == userfield08) {
            this.userfield08set = true;
        }
        this.userfield08 = userfield08;
    }

    /**
     * Returns the dynamic field no 9 of this user object
     *
     * @return A {@link String} containing the dynamic field no 9
     */
    final public String getUserfield09() {
        return userfield09;
    }

    /**
     * Sets the dynamic field no 9 for this user object
     *
     * @param userfield09 A {@link String} containing the dynamic field no 9
     */
    final public void setUserfield09(final String userfield09) {
        if (null == userfield09) {
            this.userfield09set = true;
        }
        this.userfield09 = userfield09;
    }

    /**
     * Returns the dynamic field no 10 of this user object
     *
     * @return A {@link String} containing the dynamic field no 10
     */
    final public String getUserfield10() {
        return userfield10;
    }

    /**
     * Sets the dynamic field no 10 for this user object
     *
     * @param userfield10 A {@link String} containing the dynamic field no 10
     */
    final public void setUserfield10(final String userfield10) {
        if (null == userfield10) {
            this.userfield10set = true;
        }
        this.userfield10 = userfield10;
    }

    /**
     * Returns the dynamic field no 11 of this user object
     *
     * @return A {@link String} containing the dynamic field no 11
     */
    final public String getUserfield11() {
        return userfield11;
    }

    /**
     * Sets the dynamic field no 11 for this user object
     *
     * @param userfield11 A {@link String} containing the dynamic field no 11
     */
    final public void setUserfield11(final String userfield11) {
        if (null == userfield11) {
            this.userfield11set = true;
        }
        this.userfield11 = userfield11;
    }

    /**
     * Returns the dynamic field no 12 of this user object
     *
     * @return A {@link String} containing the dynamic field no 12
     */
    final public String getUserfield12() {
        return userfield12;
    }

    /**
     * Sets the dynamic field no 12 for this user object
     *
     * @param userfield12 A {@link String} containing the dynamic field no 12
     */
    final public void setUserfield12(final String userfield12) {
        if (null == userfield12) {
            this.userfield12set = true;
        }
        this.userfield12 = userfield12;
    }

    /**
     * Returns the dynamic field no 13 of this user object
     *
     * @return A {@link String} containing the dynamic field no 13
     */
    final public String getUserfield13() {
        return userfield13;
    }

    /**
     * Sets the dynamic field no 13 for this user object
     *
     * @param userfield13 A {@link String} containing the dynamic field no 13
     */
    final public void setUserfield13(final String userfield13) {
        if (null == userfield13) {
            this.userfield13set = true;
        }
        this.userfield13 = userfield13;
    }

    /**
     * Returns the dynamic field no 14 of this user object
     *
     * @return A {@link String} containing the dynamic field no 14
     */
    final public String getUserfield14() {
        return userfield14;
    }

    /**
     * Sets the dynamic field no 14 for this user object
     *
     * @param userfield14 A {@link String} containing the dynamic field no 14
     */
    final public void setUserfield14(final String userfield14) {
        if (null == userfield14) {
            this.userfield14set = true;
        }
        this.userfield14 = userfield14;
    }

    /**
     * Returns the dynamic field no 15 of this user object
     *
     * @return A {@link String} containing the dynamic field no 15
     */
    final public String getUserfield15() {
        return userfield15;
    }

    /**
     * Sets the dynamic field no 15 for this user object
     *
     * @param userfield15 A {@link String} containing the dynamic field no 15
     */
    final public void setUserfield15(final String userfield15) {
        if (null == userfield15) {
            this.userfield15set = true;
        }
        this.userfield15 = userfield15;
    }

    /**
     * Returns the dynamic field no 16 of this user object
     *
     * @return A {@link String} containing the dynamic field no 16
     */
    final public String getUserfield16() {
        return userfield16;
    }

    /**
     * Sets the dynamic field no 16 for this user object
     *
     * @param userfield16 A {@link String} containing the dynamic field no 16
     */
    final public void setUserfield16(final String userfield16) {
        if (null == userfield16) {
            this.userfield16set = true;
        }
        this.userfield16 = userfield16;
    }

    /**
     * Returns the dynamic field no 17 of this user object
     *
     * @return A {@link String} containing the dynamic field no 17
     */
    final public String getUserfield17() {
        return userfield17;
    }

    /**
     * Sets the dynamic field no 17 for this user object
     *
     * @param userfield17 A {@link String} containing the dynamic field no 17
     */
    final public void setUserfield17(final String userfield17) {
        if (null == userfield17) {
            this.userfield17set = true;
        }
        this.userfield17 = userfield17;
    }

    /**
     * Returns the dynamic field no 18 of this user object
     *
     * @return A {@link String} containing the dynamic field no 18
     */
    final public String getUserfield18() {
        return userfield18;
    }

    /**
     * Sets the dynamic field no 18 for this user object
     *
     * @param userfield18 A {@link String} containing the dynamic field no 18
     */
    final public void setUserfield18(final String userfield18) {
        if (null == userfield18) {
            this.userfield18set = true;
        }
        this.userfield18 = userfield18;
    }

    /**
     * Returns the dynamic field no 19 of this user object
     *
     * @return A {@link String} containing the dynamic field no 19
     */
    final public String getUserfield19() {
        return userfield19;
    }

    /**
     * Sets the dynamic field no 19 for this user object
     *
     * @param userfield19 A {@link String} containing the dynamic field no 19
     */
    final public void setUserfield19(final String userfield19) {
        if (null == userfield19) {
            this.userfield19set = true;
        }
        this.userfield19 = userfield19;
    }

    /**
     * Returns the dynamic field no 20 of this user object
     *
     * @return A {@link String} containing the dynamic field no 20
     */
    final public String getUserfield20() {
        return userfield20;
    }

    /**
     * Sets the dynamic field no 20 for this user object
     *
     * @param userfield20 A {@link String} containing the dynamic field no 20
     */
    final public void setUserfield20(final String userfield20) {
        if (null == userfield20) {
            this.userfield20set = true;
        }
        this.userfield20 = userfield20;
    }

    /**
     * Return the name of the primary mail account
     * @return A {@link String} containing the name of the primary mail account
     */
    final public String getPrimaryAccountName(){
        return primaryAccountName;
    }

    /**
     * Sets the name of the primary mail account for this user object
     *
     * @param primaryAccountName A {@link String} containing the name of the primary mail account
     */
    final public void setPrimaryAccountName(final String primaryAccountName) {
        if (null == this.primaryAccountName) {
            this.primaryAccountNameSet = true;
        }
        this.primaryAccountName = primaryAccountName;
    }

    final public boolean isPrimaryAccountNameSet() {
        return primaryAccountNameSet;
    }

    /**
     * Sets the E-Mail aliases for this user object
     *
     * @param aliases A {@link HashSet} containing the E-Mail aliases
     */
    final public void setAliases(final HashSet<String> aliases) {
        if (null == aliases) {
            this.aliasesset = true;
        }
        this.aliases = aliases;
    }

    /**
     * Adds an E-Mail alias to the current list of aliases of this user object
     *
     * @param alias A {@link String} containing the E-Mail alias to add
     */
    final public void addAlias(final String alias) {
        if (this.aliases == null) {
            this.aliases = new HashSet<String>();
        }
        this.aliases.add(alias);
    }

    /**
     * Removes the specified E-Mail alias from the list of this user object
     *
     * @param alias A {@link String} containing the E-Mail alias to be removed
     * @return true if removing was successful; false otherwise
     */
    public final boolean removeAlias(String alias) {
        return null != aliases && aliases.remove(alias);
    }

    /**
     * Returns the complete E-mail aliases of this user object
     *
     * @return A {@link HashSet} containing the complete E-mail aliases
     */
    final public HashSet<String> getAliases() {
        return this.aliases;
    }


    final public void setAliasesForSOAP(List<String> aliases) {
        if(aliases != null) {
            this.aliases = new HashSet<String>(aliases);
        }
    }

    final public List<String> getAliasesForSOAP() {
        if(this.aliases == null) {
            return null;
        }
        return new LinkedList<String>(this.aliases);
    }

    /**
     * Returns the city for the business location of this user object
     *
     * @return A {@link String} containing the city for the business location
     */
    final public String getCity_business() {
        return city_business;
    }

    /**
     * Sets the city for the business location for this user object
     *
     * @param city_business A {@link String} containing the city for the business location
     */
    final public void setCity_business(final String city_business) {
        if (null == city_business) {
            this.city_businessset = true;
        }
        this.city_business = city_business;
    }

    /**
     * Returns the country for the business location of this user object
     *
     * @return A {@link String} containing the country for the business location
     */
    final public String getCountry_business() {
        return country_business;
    }

    /**
     * Sets the country for the business location for this user object
     *
     * @param country_business A {@link String} containing the country for the business location
     */
    final public void setCountry_business(final String country_business) {
        if (null == country_business) {
            this.country_businessset = true;
        }
        this.country_business = country_business;
    }

    /**
     * Returns the name of the assistant of this user object
     *
     * @return A {@link String} containing the name of the assistant
     */
    final public String getAssistant_name() {
        return assistant_name;
    }

    /**
     * Sets the name of the assistant for this user object
     *
     * @param assistant_name A {@link String} containing the name of the assistant
     */
    final public void setAssistant_name(final String assistant_name) {
        if (null == assistant_name) {
            this.assistant_nameset = true;
        }
        this.assistant_name = assistant_name;
    }

    /**
     * Currently not used
     *
     * @return A {@link String} containing the fields value
     */
    final public String getTelephone_primary() {
        return telephone_primary;
    }

    /**
     * Currently not used
     *
     * @param telephone_primary A {@link String} containing the fields value
     */
    final public void setTelephone_primary(final String telephone_primary) {
        if (null == telephone_primary) {
            this.telephone_primaryset = true;
        }
        this.telephone_primary = telephone_primary;
    }

    /**
     * Returns the categories of this user object
     *
     * @return A {@link String} containing the categories
     */
    final public String getCategories() {
        return categories;
    }

    /**
     * Sets the categories for this user object
     *
     * @param categories A {@link String} containing the categories
     */
    final public void setCategories(final String categories) {
        if (null == categories) {
            this.categoriesset = true;
        }
        this.categories = categories;
    }

    final public String getEmail1() {
        return email1;
    }

    /**
     * Sets the E-Mail for the business location for this user object
     *
     * @param email1 A {@link String} containing the E-Mail for the business location
     */
    final public void setEmail1(final String email1) {
        if (null == email1) {
            this.email1set = true;
        }
        this.email1 = email1;
    }

    /**
     * @param ht
     * @return key/value pairs in Hashtable as a User Object
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    final public static User hashmapToUser(HashMap<String, Object> hm) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        User u = new User();
        for (Method m : u.getClass().getMethods()) {
            String mname = m.getName();
            if (mname.startsWith("set")) {
                String keyName = mname.substring(3).toLowerCase();
                if (hm.containsKey(keyName)) {
                    m.invoke(u, hm.get(keyName));
                }
            }
        }

        return u;
    }

    /**
     * A wrapper function to get fields of this user object as key/value pairs in a {@link Hashtable}
     *
     * @return The {@link Hashtable} containing the keys/values of this user object
     */
    final public Hashtable<String, Object> toHashtable() {
        final Hashtable<String, Object> ht = new Hashtable<String, Object>();

        for (final Field f : this.getClass().getDeclaredFields()) {
            try {
                final Object ob = f.get(this);
                final String tname = f.getName();
                if (ob != null && !tname.equals("serialVersionUID") && !tname.equals("extensions") && !tname.endsWith("set")) {
                    ht.put(tname, ob);
                }
            } catch (final IllegalArgumentException e) {
                // ignore
            } catch (final IllegalAccessException e) {
                // ignore
            }
        }
        return ht;
    }

    @Override
    public String toString() {
        final StringBuilder ret = new StringBuilder();
        ret.append("[ \n");
        ret.append(super.toString());

        for (final Field f : this.getClass().getDeclaredFields()) {
            try {
                final Object ob = f.get(this);
                final String tname = f.getName();
                if (ob != null && !tname.equals("serialVersionUID") && !tname.equals("extensions") && !tname.endsWith("set") && !tname.equalsIgnoreCase("password")) {
                    ret.append("  ");
                    ret.append(tname);
                    ret.append(": ");
                    ret.append(ob);
                    ret.append("\n");
                }
            } catch (final IllegalArgumentException e) {
                ret.append("IllegalArgument\n");
            } catch (final IllegalAccessException e) {
                ret.append("IllegalAccessException\n");
            }
        }
        ret.append("]");
        return ret.toString();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final User object = (User) super.clone();
        if( this.aliases != null ) {
            object.aliases = new HashSet<String>(this.aliases);
        }
        if(this.userAttributes != null) {
            object.userAttributes = new HashMap<String, Map<String, String>>();
            for(Map.Entry<String, Map<String, String>> map : userAttributes.entrySet()) {
                object.userAttributes.put(map.getKey(), new HashMap<String, String>(map.getValue()));
            }
        }
        if (null != this.birthday) {
            object.birthday = (Date) this.birthday.clone();
        }
        if (null != this.anniversary) {
            object.anniversary = (Date) this.anniversary.clone();
        }
        return object;
    }


    private void init() {
        initExtendable();
        this.id = null;
        this.name = null;
        this.password = null;
        this.passwordMech = null;
        this.primaryEmail = null;
        this.email1 = null;
        this.email2 = null;
        this.email3 = null;
        this.aliases = null;
        this.sur_name = null;
        this.given_name = null;
        this.mailenabled = null;

        this.filestore_id = null;
        this.filestore_owner = null;
        this.filestore_name = null;
        this.maxQuota = null;
        this.usedQuota = null;

        this.birthday = null;
        this.anniversary = null;
        this.branches = null;
        this.business_category = null;
        this.categories = null;
        this.postal_code_business = null;
        this.state_business = null;
        this.street_business = null;
        this.telephone_callback = null;
        this.city_home = null;
        this.commercial_register = null;
        this.country_home = null;
        this.company = null;
        this.default_group = null;
        this.department = null;
        this.display_name = null;
        this.employeeType = null;
        this.fax_business = null;
        this.fax_home = null;
        this.fax_other = null;
        this.imapServer = null;
        this.smtpServer = null;
        this.instant_messenger1 = null;
        this.instant_messenger2 = null;
        this.telephone_ip = null;
        this.telephone_isdn = null;
        this.language = null;
        this.mail_folder_drafts_name = null;
        this.mail_folder_sent_name = null;
        this.mail_folder_spam_name = null;
        this.mail_folder_trash_name = null;
        this.mail_folder_archive_full_name = null;
        this.manager_name = null;
        this.marital_status = null;
        this.cellular_telephone1 = null;
        this.cellular_telephone2 = null;
        this.info = null;
        this.nickname = null;
        this.number_of_children = null;
        this.note = null;
        this.number_of_employee = null;
        this.telephone_pager = null;
        this.password_expired = null;
        this.telephone_assistant = null;
        this.assistant_name = null;
        this.telephone_business1 = null;
        this.telephone_business2 = null;
        this.telephone_car = null;
        this.telephone_company = null;
        this.telephone_home1 = null;
        this.telephone_home2 = null;
        this.telephone_other = null;
        this.telephone_primary = null;
        this.position = null;
        this.postal_code_home = null;
        this.profession = null;
        this.telephone_radio = null;
        this.room_number = null;
        this.sales_volume = null;
        this.city_other = null;
        this.city_business = null;
        this.country_other = null;
        this.country_business = null;
        this.middle_name = null;
        this.postal_code_other = null;
        this.state_other = null;
        this.street_other = null;
        this.spouse_name = null;
        this.state_home = null;
        this.street_home = null;
        this.suffix = null;
        this.tax_id = null;
        this.telephone_telex = null;
        this.timezone = null;
        this.title = null;
        this.telephone_ttytdd = null;
        this.url = null;
        this.userfield01 = null;
        this.userfield02 = null;
        this.userfield03 = null;
        this.userfield04 = null;
        this.userfield05 = null;
        this.userfield06 = null;
        this.userfield07 = null;
        this.userfield08 = null;
        this.userfield09 = null;
        this.userfield10 = null;
        this.userfield11 = null;
        this.userfield12 = null;
        this.userfield13 = null;
        this.userfield14 = null;
        this.userfield15 = null;
        this.userfield16 = null;
        this.userfield17 = null;
        this.userfield18 = null;
        this.userfield19 = null;
        this.userfield20 = null;
        this.defaultSenderAddress = null;
        folderTree = null;
        defaultFolderMode = null;
        this.guiPreferences = null;
        this.userAttributes = new HashMap<String, Map<String, String>>();
        this.primaryAccountName = null;
    }

    /**
     * @param extension
     * @deprecated
     */
    @Deprecated
    final public void addExtension(final OXUserExtensionInterface extension) {
        getAllExtensionsAsHash().put(extension.getClass().getName(), (OXCommonExtension) extension);
    }

    /**
     * @return
     * @deprecated
     */
    @Deprecated
    final public ArrayList<OXUserExtensionInterface> getExtensions() {
        final ArrayList<OXUserExtensionInterface> retval = new ArrayList<OXUserExtensionInterface>();
        for (final OXCommonExtension commoninterface : getAllExtensionsAsHash().values()) {
            retval.add((OXUserExtensionInterface) commoninterface);
        }
        return retval;
    }

    /**
     * @param o
     * @return
     * @deprecated
     */
    @Deprecated
    final public boolean removeExtension(final OXUserExtensionInterface o) {
        return null != getAllExtensionsAsHash().remove(o.getClass().getName());
    }

    /**
     * @param index
     * @return
     * @deprecated
     */
    @Deprecated
    final public OXUserExtensionInterface removeExtensionByIndex(final int index) {
        final ArrayList<OXCommonExtension> retval = new ArrayList<OXCommonExtension>(getAllExtensionsAsHash().values());
        final OXCommonExtension commonExtensionInterface = retval.get(index);
        return (OXUserExtensionInterface) getAllExtensionsAsHash().remove(commonExtensionInterface.getClass().getName());
    }

    /**
     * This method is used to get the extensions through the name of the
     * extension. An Array with all extensions where the name fits will be
     * returned, or an empty array if no fitting extension was found.
     *
     * @param extname
     *                a String for the extension
     * @return the ArrayList of {@link OXUserExtensionInterface} with extname
     * @deprecated
     */
    @Deprecated
    final public ArrayList<OXUserExtensionInterface> getExtensionbyName(final String extname) {
        final ArrayList<OXUserExtensionInterface> retval = new ArrayList<OXUserExtensionInterface>();
        for (final OXCommonExtension ext : getAllExtensionsAsHash().values()) {
            if (extname.equals(ext.getClass().getName())) {
                retval.add((OXUserExtensionInterface) ext);
            }
        }
        return retval;
    }

    /**
     * A convenience method for getting the first extension in a list of equal
     * extension names. The use of this method is not recommended because you
     * won't get notifications how many extensions of the same name exist.
     *
     * @param extname
     * @return
     * @deprecated
     */
    @Deprecated
    final public OXUserExtensionInterface getFirstExtensionbyName(final String extname) {
        final ArrayList<OXUserExtensionInterface> list = getExtensionbyName(extname);
        if (!list.isEmpty() && list.size() == 1) {
            return list.get(0);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.PasswordMechObject#getPasswordMech()
     */
    @Override
    final public String getPasswordMech() {
        return passwordMech;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.PasswordMechObject#setPasswordMech(java.lang.String)
     */
    @Override
    final public void setPasswordMech(final String passwordMech) {
        if (null == passwordMech) {
            this.passwordMechset = true;
        }
        // NOTE: Check is done in OXUser
        this.passwordMech = passwordMech;
    }

    /**
     * @return the mail_folder_confirmed_ham_name
     */
    final public String getMail_folder_confirmed_ham_name() {
        return mail_folder_confirmed_ham_name;
    }

    /**
     * Set the name of the folder where mail should be copied, that should be
     * learned as HAM using whatever mechanism on the server side
     */
    final public void setMail_folder_confirmed_ham_name(String mail_folder_confirmed_ham_name) {
        this.mail_folder_confirmed_ham_nameset = true;
        this.mail_folder_confirmed_ham_name = mail_folder_confirmed_ham_name;
    }

    /**
     * @return the mail_folder_confirmed_spam_name
     */
    final public String getMail_folder_confirmed_spam_name() {
        return mail_folder_confirmed_spam_name;
    }

    /**
     * Set the name of the folder where mail should be moved, that should be
     * learned as SPAM using whatever mechanism on the server side
     */
    final public void setMail_folder_confirmed_spam_name(String mail_folder_confirmed_spam_name) {
        this.mail_folder_confirmed_ham_nameset = true;
        this.mail_folder_confirmed_spam_name = mail_folder_confirmed_spam_name;
    }

    /**
     * Used to check if the mail_folder_confirmed_spam field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isMail_folder_confirmed_spamset() {
        return mail_folder_confirmed_spam_nameset;
    }

    /**
     * Used to check if the mail_folder_confirmed_ham field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isMail_folder_confirmed_hamset() {
        return mail_folder_confirmed_ham_nameset;
    }

    /**
     * Used to check if the gui_spam_filter_capabilities_enabled field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    final public boolean isGui_spam_filter_enabledset() {
        return gui_spam_filter_enabledset;
    }

    /**
     * Used to check if the gui_spam_filter_capabilities_enabled field of this user object has been changed
     *
     * @return true if set; false if not
     * @deprecated use {@link #isGui_spam_filter_enabledset()} instead
     **/
    @Deprecated
    final public boolean isGUI_Spam_filter_capabilities_enabledset() {
        return gui_spam_filter_enabledset;
    }

    /**
     * @return the gui_spam_filter_enabled
     */
    final public Boolean getGui_spam_filter_enabled() {
        return gui_spam_filter_enabled;
    }

    /**
     * @return the gui_spam_filter_enabled
     * @deprecated use {@link #getGui_spam_filter_enabled()} instead
     */
    @Deprecated
    final public Boolean getGUI_Spam_filter_capabilities_enabled() {
        return gui_spam_filter_enabled;
    }

    /**
     * @return
     * @deprecated use {@link #getGui_spam_filter_enabled()} instead
     */
    @Deprecated
    final public Boolean getSpam_filter_enabled() {
        return gui_spam_filter_enabled;
    }

    /**
     * @param gui_spam_filter_enabled
     *                the gui_spam_filter_enabled to set
     */
    public final void setGui_spam_filter_enabled(Boolean gui_spam_filter_enabled) {
        this.gui_spam_filter_enabledset = true;
        this.gui_spam_filter_enabled = gui_spam_filter_enabled;
    }

    /**
     * @param gui_spam_filter_enabled
     *                the gui_spam_filter_enabled to set
     * @deprecated use {@link #setGui_spam_filter_enabled(Boolean)} instead
     */
    @Deprecated
    public final void setGUI_Spam_filter_capabilities_enabled(Boolean gui_spam_filter_enabled) {
        this.gui_spam_filter_enabledset = true;
        this.gui_spam_filter_enabled = gui_spam_filter_enabled;
    }

    /**
     * Used to check if the contextadmin field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    public final boolean isContextadmin() {
        return contextadmin;
    }

    /**
     * Note: calling this setter on a user object has no influence on the user
     * stored within the ox subsystem, as it is not possible to change the admin
     * status of a user.
     */
    public final void setContextadmin(boolean contextadmin) {
        this.contextadmin = contextadmin;
    }

    /**
     * Returns the default sender address of this user object
     *
     * @return A {@link String} containing the default sender address
     */
    public final String getDefaultSenderAddress() {
        return defaultSenderAddress;
    }

    /**
     * Sets the default sender address for this user object
     *
     * @param defaultSenderAddress A {@link String} containing the default sender address
     */
    public final void setDefaultSenderAddress(final String defaultSenderAddress) {
        this.defaultSenderAddressset = true;
        this.defaultSenderAddress = defaultSenderAddress;
    }

    /**
     * Used to check if the contextadmin field of this user object has been changed
     *
     * @return true if set; false if not
     **/
    public final boolean isDefaultSenderAddressset() {
        return defaultSenderAddressset;
    }

    public Integer getFolderTree() {
        return folderTree;
    }

    public void setFolderTree(Integer folderTree) {
        folderTreeSet = true;
        this.folderTree = folderTree;
    }

    public boolean isFolderTreeSet() {
        return folderTreeSet;
    }
    
    
    public String getDefaultFolderMode() {
        return defaultFolderMode;
    }
    
    public void setDefaultFolderMode(String defaultFolderMode) {
        this.defaultFolderModeSet = true;
        this.defaultFolderMode = defaultFolderMode;
    }
    
    public boolean isDefaultFolderModeSet() {
        return defaultFolderModeSet;
    }
    
    /**
     * Sets a generic user attribute
     */
    public void setUserAttribute(String namespace, String name, String value) {
        getNamespace(namespace).put(name, value);
        userAttribtuesset = true;
    }

    /**
     * Read a generic user attribute
     */
    public String getUserAttribute(String namespace, String name) {
        return getNamespace(namespace).get(name);
    }

    public Map<String, Map<String, String>> getUserAttributes() {
        if(userAttributes == null) {
            userAttributes = new HashMap<String, Map<String, String>>();
        }
        return userAttributes;
    }

    public void setUserAttributes(Map<String, Map<String, String>> userAttributes) {
        this.userAttribtuesset = true;
        this.userAttributes = userAttributes;
    }

    public Map<String, String> getNamespace(String namespace) {
        if(userAttributes == null) {
            userAttributes = new HashMap<String, Map<String, String>>();
        }
        Map<String, String> ns = userAttributes.get(namespace);
        if(ns == null) {
            ns = new HashMap<String, String>();
            userAttributes.put(namespace, ns);
        }
        return ns;
    }

    /**
     * Used to check if the user attributes have been modified
     */
    public boolean isUserAttributesset() {
        return userAttribtuesset;
    }

    /**
     * @return true if set; false if not
     */
    public final boolean isGuiPreferencesset() {
        return guiPreferencesset;
    }


    /**
     * @return the guiPreferences
     */
    public final Map<String, String> getGuiPreferences() {
        return guiPreferences;
    }

    /**
     * add a path/value pair to gui settings
     *
     * @param path
     * @param guiValue
     */
    public final void addGuiPreferences(final String path, final String guiValue) {
        if( guiPreferences == null ) {
            guiPreferences = new HashMap<String, String>();
        }
        guiPreferences.put(path, guiValue);
    }

    /**
     * remove a path/value pair from gui settings
     *
     * @param path
     */
    public final void removeGuiPreferences(final String path) {
        if( guiPreferences != null ) {
            guiPreferences.remove(path);
        }
    }

    /**
     * @param guiPreferences the guiPreferences to set
     */
   public final void setGuiPreferences(final Map<String, String> guiPreferences) {
        if( guiPreferences != null ) {
            this.guiPreferencesset = true;
        }
        this.guiPreferences = guiPreferences;
    }

    /**
     * At the moment {@link #setName}, {@link #setDisplay_name}, {@link #setPassword(String)},
     * {@link #setGiven_name(String)}, {@link #setSur_name(String)} and {@link #setPrimaryEmail(String)}
     * are defined here
     */
    @Override
    public final String[] getMandatoryMembersCreate() {
        return new String[]{ "name", "display_name", "password", "given_name", "sur_name", "primaryEmail", "email1" };
    }

    /**
     * At the moment no fields are defined here
     */
    @Override
    public final String[] getMandatoryMembersChange() {
        return null;
    }

    /**
     * At the moment no fields are defined here
     */
    @Override
    public String[] getMandatoryMembersDelete() {
        return null;
    }

    /**
     * At the moment no fields are defined here
     */
    @Override
    public String[] getMandatoryMembersRegister() {
        return null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((aliases == null) ? 0 : aliases.hashCode());
        result = prime * result + (aliasesset ? 1231 : 1237);
        result = prime * result + ((anniversary == null) ? 0 : anniversary.hashCode());
        result = prime * result + (anniversaryset ? 1231 : 1237);
        result = prime * result + ((assistant_name == null) ? 0 : assistant_name.hashCode());
        result = prime * result + (assistant_nameset ? 1231 : 1237);
        result = prime * result + ((birthday == null) ? 0 : birthday.hashCode());
        result = prime * result + (birthdayset ? 1231 : 1237);
        result = prime * result + ((branches == null) ? 0 : branches.hashCode());
        result = prime * result + (branchesset ? 1231 : 1237);
        result = prime * result + ((business_category == null) ? 0 : business_category.hashCode());
        result = prime * result + (business_categoryset ? 1231 : 1237);
        result = prime * result + ((categories == null) ? 0 : categories.hashCode());
        result = prime * result + (categoriesset ? 1231 : 1237);
        result = prime * result + ((cellular_telephone1 == null) ? 0 : cellular_telephone1.hashCode());
        result = prime * result + (cellular_telephone1set ? 1231 : 1237);
        result = prime * result + ((cellular_telephone2 == null) ? 0 : cellular_telephone2.hashCode());
        result = prime * result + (cellular_telephone2set ? 1231 : 1237);
        result = prime * result + ((city_business == null) ? 0 : city_business.hashCode());
        result = prime * result + (city_businessset ? 1231 : 1237);
        result = prime * result + ((city_home == null) ? 0 : city_home.hashCode());
        result = prime * result + (city_homeset ? 1231 : 1237);
        result = prime * result + ((city_other == null) ? 0 : city_other.hashCode());
        result = prime * result + (city_otherset ? 1231 : 1237);
        result = prime * result + ((commercial_register == null) ? 0 : commercial_register.hashCode());
        result = prime * result + (commercial_registerset ? 1231 : 1237);
        result = prime * result + ((company == null) ? 0 : company.hashCode());
        result = prime * result + (companyset ? 1231 : 1237);
        result = prime * result + (contextadmin ? 1231 : 1237);
        result = prime * result + ((country_business == null) ? 0 : country_business.hashCode());
        result = prime * result + (country_businessset ? 1231 : 1237);
        result = prime * result + ((country_home == null) ? 0 : country_home.hashCode());
        result = prime * result + (country_homeset ? 1231 : 1237);
        result = prime * result + ((country_other == null) ? 0 : country_other.hashCode());
        result = prime * result + (country_otherset ? 1231 : 1237);
        result = prime * result + ((defaultSenderAddress == null) ? 0 : defaultSenderAddress.hashCode());
        result = prime * result + (defaultSenderAddressset ? 1231 : 1237);
        result = prime * result + ((folderTree == null) ? 0 : folderTree.hashCode());
        result = prime * result + (folderTreeSet ? 1231 : 1237);
        result = prime * result + ((defaultFolderMode == null) ? 0 : defaultFolderMode.hashCode());
        result = prime * result + (defaultFolderModeSet ? 1231 :1237);
        result = prime * result + ((default_group == null) ? 0 : default_group.hashCode());
        result = prime * result + (default_groupset ? 1231 : 1237);
        result = prime * result + ((department == null) ? 0 : department.hashCode());
        result = prime * result + (departmentset ? 1231 : 1237);
        result = prime * result + ((display_name == null) ? 0 : display_name.hashCode());
        result = prime * result + (display_nameset ? 1231 : 1237);
        result = prime * result + ((email1 == null) ? 0 : email1.hashCode());
        result = prime * result + (email1set ? 1231 : 1237);
        result = prime * result + ((email2 == null) ? 0 : email2.hashCode());
        result = prime * result + (email2set ? 1231 : 1237);
        result = prime * result + ((email3 == null) ? 0 : email3.hashCode());
        result = prime * result + (email3set ? 1231 : 1237);
        result = prime * result + ((employeeType == null) ? 0 : employeeType.hashCode());
        result = prime * result + (employeeTypeset ? 1231 : 1237);
        result = prime * result + ((fax_business == null) ? 0 : fax_business.hashCode());
        result = prime * result + (fax_businessset ? 1231 : 1237);
        result = prime * result + ((fax_home == null) ? 0 : fax_home.hashCode());
        result = prime * result + (fax_homeset ? 1231 : 1237);
        result = prime * result + ((fax_other == null) ? 0 : fax_other.hashCode());
        result = prime * result + (fax_otherset ? 1231 : 1237);
        result = prime * result + ((given_name == null) ? 0 : given_name.hashCode());
        result = prime * result + (given_nameset ? 1231 : 1237);
        result = prime * result + ((guiPreferences == null) ? 0 : guiPreferences.hashCode());
        result = prime * result + ((gui_spam_filter_enabled == null) ? 0 : gui_spam_filter_enabled.hashCode());
        result = prime * result + (gui_spam_filter_enabledset ? 1231 : 1237);
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (idset ? 1231 : 1237);
        result = prime * result + ((imapLogin == null) ? 0 : imapLogin.hashCode());
        result = prime * result + (imapLoginset ? 1231 : 1237);
        result = prime * result + ((imapServer == null) ? 0 : imapServer.hashCode());
        result = prime * result + (imapServerset ? 1231 : 1237);
        result = prime * result + ((info == null) ? 0 : info.hashCode());
        result = prime * result + (infoset ? 1231 : 1237);
        result = prime * result + ((instant_messenger1 == null) ? 0 : instant_messenger1.hashCode());
        result = prime * result + (instant_messenger1set ? 1231 : 1237);
        result = prime * result + ((instant_messenger2 == null) ? 0 : instant_messenger2.hashCode());
        result = prime * result + (instant_messenger2set ? 1231 : 1237);
        result = prime * result + ((language == null) ? 0 : language.hashCode());
        result = prime * result + (languageset ? 1231 : 1237);
        result = prime * result + ((mail_folder_confirmed_ham_name == null) ? 0 : mail_folder_confirmed_ham_name.hashCode());
        result = prime * result + (mail_folder_confirmed_ham_nameset ? 1231 : 1237);
        result = prime * result + ((mail_folder_confirmed_spam_name == null) ? 0 : mail_folder_confirmed_spam_name.hashCode());
        result = prime * result + (mail_folder_confirmed_spam_nameset ? 1231 : 1237);
        result = prime * result + ((mail_folder_drafts_name == null) ? 0 : mail_folder_drafts_name.hashCode());
        result = prime * result + (mail_folder_drafts_nameset ? 1231 : 1237);
        result = prime * result + ((mail_folder_sent_name == null) ? 0 : mail_folder_sent_name.hashCode());
        result = prime * result + (mail_folder_sent_nameset ? 1231 : 1237);
        result = prime * result + ((mail_folder_spam_name == null) ? 0 : mail_folder_spam_name.hashCode());
        result = prime * result + (mail_folder_spam_nameset ? 1231 : 1237);
        result = prime * result + ((mail_folder_trash_name == null) ? 0 : mail_folder_trash_name.hashCode());
        result = prime * result + (mail_folder_trash_nameset ? 1231 : 1237);
        result = prime * result + ((mail_folder_archive_full_name == null) ? 0 : mail_folder_archive_full_name.hashCode());
        result = prime * result + (mail_folder_archive_full_nameset ? 1231 : 1237);
        result = prime * result + ((mailenabled == null) ? 0 : mailenabled.hashCode());
        result = prime * result + (mailenabledset ? 1231 : 1237);
        result = prime * result + ((manager_name == null) ? 0 : manager_name.hashCode());
        result = prime * result + (manager_nameset ? 1231 : 1237);
        result = prime * result + ((marital_status == null) ? 0 : marital_status.hashCode());
        result = prime * result + (marital_statusset ? 1231 : 1237);
        result = prime * result + ((middle_name == null) ? 0 : middle_name.hashCode());
        result = prime * result + (middle_nameset ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (nameset ? 1231 : 1237);
        result = prime * result + ((nickname == null) ? 0 : nickname.hashCode());
        result = prime * result + (nicknameset ? 1231 : 1237);
        result = prime * result + ((note == null) ? 0 : note.hashCode());
        result = prime * result + (noteset ? 1231 : 1237);
        result = prime * result + ((number_of_children == null) ? 0 : number_of_children.hashCode());
        result = prime * result + (number_of_childrenset ? 1231 : 1237);
        result = prime * result + ((number_of_employee == null) ? 0 : number_of_employee.hashCode());
        result = prime * result + (number_of_employeeset ? 1231 : 1237);
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((passwordMech == null) ? 0 : passwordMech.hashCode());
        result = prime * result + (passwordMechset ? 1231 : 1237);
        result = prime * result + ((password_expired == null) ? 0 : password_expired.hashCode());
        result = prime * result + (password_expiredset ? 1231 : 1237);
        result = prime * result + (passwordset ? 1231 : 1237);
        result = prime * result + ((position == null) ? 0 : position.hashCode());
        result = prime * result + (positionset ? 1231 : 1237);
        result = prime * result + ((postal_code_business == null) ? 0 : postal_code_business.hashCode());
        result = prime * result + (postal_code_businessset ? 1231 : 1237);
        result = prime * result + ((postal_code_home == null) ? 0 : postal_code_home.hashCode());
        result = prime * result + (postal_code_homeset ? 1231 : 1237);
        result = prime * result + ((postal_code_other == null) ? 0 : postal_code_other.hashCode());
        result = prime * result + (postal_code_otherset ? 1231 : 1237);
        result = prime * result + ((primaryEmail == null) ? 0 : primaryEmail.hashCode());
        result = prime * result + (primaryEmailset ? 1231 : 1237);
        result = prime * result + ((profession == null) ? 0 : profession.hashCode());
        result = prime * result + (professionset ? 1231 : 1237);
        result = prime * result + ((room_number == null) ? 0 : room_number.hashCode());
        result = prime * result + (room_numberset ? 1231 : 1237);
        result = prime * result + ((sales_volume == null) ? 0 : sales_volume.hashCode());
        result = prime * result + (sales_volumeset ? 1231 : 1237);
        result = prime * result + ((smtpServer == null) ? 0 : smtpServer.hashCode());
        result = prime * result + (smtpServerset ? 1231 : 1237);
        result = prime * result + ((spouse_name == null) ? 0 : spouse_name.hashCode());
        result = prime * result + (spouse_nameset ? 1231 : 1237);
        result = prime * result + ((state_business == null) ? 0 : state_business.hashCode());
        result = prime * result + (state_businessset ? 1231 : 1237);
        result = prime * result + ((state_home == null) ? 0 : state_home.hashCode());
        result = prime * result + (state_homeset ? 1231 : 1237);
        result = prime * result + ((state_other == null) ? 0 : state_other.hashCode());
        result = prime * result + (state_otherset ? 1231 : 1237);
        result = prime * result + ((street_business == null) ? 0 : street_business.hashCode());
        result = prime * result + (street_businessset ? 1231 : 1237);
        result = prime * result + ((street_home == null) ? 0 : street_home.hashCode());
        result = prime * result + (street_homeset ? 1231 : 1237);
        result = prime * result + ((street_other == null) ? 0 : street_other.hashCode());
        result = prime * result + (street_otherset ? 1231 : 1237);
        result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
        result = prime * result + (suffixset ? 1231 : 1237);
        result = prime * result + ((sur_name == null) ? 0 : sur_name.hashCode());
        result = prime * result + (sur_nameset ? 1231 : 1237);
        result = prime * result + ((tax_id == null) ? 0 : tax_id.hashCode());
        result = prime * result + (tax_idset ? 1231 : 1237);
        result = prime * result + ((telephone_assistant == null) ? 0 : telephone_assistant.hashCode());
        result = prime * result + (telephone_assistantset ? 1231 : 1237);
        result = prime * result + ((telephone_business1 == null) ? 0 : telephone_business1.hashCode());
        result = prime * result + (telephone_business1set ? 1231 : 1237);
        result = prime * result + ((telephone_business2 == null) ? 0 : telephone_business2.hashCode());
        result = prime * result + (telephone_business2set ? 1231 : 1237);
        result = prime * result + ((telephone_callback == null) ? 0 : telephone_callback.hashCode());
        result = prime * result + (telephone_callbackset ? 1231 : 1237);
        result = prime * result + ((telephone_car == null) ? 0 : telephone_car.hashCode());
        result = prime * result + (telephone_carset ? 1231 : 1237);
        result = prime * result + ((telephone_company == null) ? 0 : telephone_company.hashCode());
        result = prime * result + (telephone_companyset ? 1231 : 1237);
        result = prime * result + ((telephone_home1 == null) ? 0 : telephone_home1.hashCode());
        result = prime * result + (telephone_home1set ? 1231 : 1237);
        result = prime * result + ((telephone_home2 == null) ? 0 : telephone_home2.hashCode());
        result = prime * result + (telephone_home2set ? 1231 : 1237);
        result = prime * result + ((telephone_ip == null) ? 0 : telephone_ip.hashCode());
        result = prime * result + (telephone_ipset ? 1231 : 1237);
        result = prime * result + ((telephone_isdn == null) ? 0 : telephone_isdn.hashCode());
        result = prime * result + (telephone_isdnset ? 1231 : 1237);
        result = prime * result + ((telephone_other == null) ? 0 : telephone_other.hashCode());
        result = prime * result + (telephone_otherset ? 1231 : 1237);
        result = prime * result + ((telephone_pager == null) ? 0 : telephone_pager.hashCode());
        result = prime * result + (telephone_pagerset ? 1231 : 1237);
        result = prime * result + ((telephone_primary == null) ? 0 : telephone_primary.hashCode());
        result = prime * result + (telephone_primaryset ? 1231 : 1237);
        result = prime * result + ((telephone_radio == null) ? 0 : telephone_radio.hashCode());
        result = prime * result + (telephone_radioset ? 1231 : 1237);
        result = prime * result + ((telephone_telex == null) ? 0 : telephone_telex.hashCode());
        result = prime * result + (telephone_telexset ? 1231 : 1237);
        result = prime * result + ((telephone_ttytdd == null) ? 0 : telephone_ttytdd.hashCode());
        result = prime * result + (telephone_ttytddset ? 1231 : 1237);
        result = prime * result + ((timezone == null) ? 0 : timezone.hashCode());
        result = prime * result + (timezoneset ? 1231 : 1237);
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        result = prime * result + (titleset ? 1231 : 1237);
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        result = prime * result + (urlset ? 1231 : 1237);
        result = prime * result + ((userfield01 == null) ? 0 : userfield01.hashCode());
        result = prime * result + (userfield01set ? 1231 : 1237);
        result = prime * result + ((userfield02 == null) ? 0 : userfield02.hashCode());
        result = prime * result + (userfield02set ? 1231 : 1237);
        result = prime * result + ((userfield03 == null) ? 0 : userfield03.hashCode());
        result = prime * result + (userfield03set ? 1231 : 1237);
        result = prime * result + ((userfield04 == null) ? 0 : userfield04.hashCode());
        result = prime * result + (userfield04set ? 1231 : 1237);
        result = prime * result + ((userfield05 == null) ? 0 : userfield05.hashCode());
        result = prime * result + (userfield05set ? 1231 : 1237);
        result = prime * result + ((userfield06 == null) ? 0 : userfield06.hashCode());
        result = prime * result + (userfield06set ? 1231 : 1237);
        result = prime * result + ((userfield07 == null) ? 0 : userfield07.hashCode());
        result = prime * result + (userfield07set ? 1231 : 1237);
        result = prime * result + ((userfield08 == null) ? 0 : userfield08.hashCode());
        result = prime * result + (userfield08set ? 1231 : 1237);
        result = prime * result + ((userfield09 == null) ? 0 : userfield09.hashCode());
        result = prime * result + (userfield09set ? 1231 : 1237);
        result = prime * result + ((userfield10 == null) ? 0 : userfield10.hashCode());
        result = prime * result + (userfield10set ? 1231 : 1237);
        result = prime * result + ((userfield11 == null) ? 0 : userfield11.hashCode());
        result = prime * result + (userfield11set ? 1231 : 1237);
        result = prime * result + ((userfield12 == null) ? 0 : userfield12.hashCode());
        result = prime * result + (userfield12set ? 1231 : 1237);
        result = prime * result + ((userfield13 == null) ? 0 : userfield13.hashCode());
        result = prime * result + (userfield13set ? 1231 : 1237);
        result = prime * result + ((userfield14 == null) ? 0 : userfield14.hashCode());
        result = prime * result + (userfield14set ? 1231 : 1237);
        result = prime * result + ((userfield15 == null) ? 0 : userfield15.hashCode());
        result = prime * result + (userfield15set ? 1231 : 1237);
        result = prime * result + ((userfield16 == null) ? 0 : userfield16.hashCode());
        result = prime * result + (userfield16set ? 1231 : 1237);
        result = prime * result + ((userfield17 == null) ? 0 : userfield17.hashCode());
        result = prime * result + (userfield17set ? 1231 : 1237);
        result = prime * result + ((userfield18 == null) ? 0 : userfield18.hashCode());
        result = prime * result + (userfield18set ? 1231 : 1237);
        result = prime * result + ((userfield19 == null) ? 0 : userfield19.hashCode());
        result = prime * result + (userfield19set ? 1231 : 1237);
        result = prime * result + ((userfield20 == null) ? 0 : userfield20.hashCode());
        result = prime * result + (userfield20set ? 1231 : 1237);
        result = prime * result + ((primaryAccountName == null) ? 0 : primaryAccountName.hashCode());
        result = prime * result + (primaryAccountNameSet ? 1231 : 1237);
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof User)) {
            return false;
        }
        final User other = (User) obj;
        if (aliases == null) {
            if (other.aliases != null) {
                return false;
            }
        } else if (!aliases.equals(other.aliases)) {
            return false;
        }
        if (aliasesset != other.aliasesset) {
            return false;
        }
        if (anniversary == null) {
            if (other.anniversary != null) {
                return false;
            }
        } else if (!anniversary.equals(other.anniversary)) {
            return false;
        }
        if (anniversaryset != other.anniversaryset) {
            return false;
        }
        if (assistant_name == null) {
            if (other.assistant_name != null) {
                return false;
            }
        } else if (!assistant_name.equals(other.assistant_name)) {
            return false;
        }
        if (assistant_nameset != other.assistant_nameset) {
            return false;
        }
        if (birthday == null) {
            if (other.birthday != null) {
                return false;
            }
        } else if (!birthday.equals(other.birthday)) {
            return false;
        }
        if (birthdayset != other.birthdayset) {
            return false;
        }
        if (branches == null) {
            if (other.branches != null) {
                return false;
            }
        } else if (!branches.equals(other.branches)) {
            return false;
        }
        if (branchesset != other.branchesset) {
            return false;
        }
        if (business_category == null) {
            if (other.business_category != null) {
                return false;
            }
        } else if (!business_category.equals(other.business_category)) {
            return false;
        }
        if (business_categoryset != other.business_categoryset) {
            return false;
        }
        if (categories == null) {
            if (other.categories != null) {
                return false;
            }
        } else if (!categories.equals(other.categories)) {
            return false;
        }
        if (categoriesset != other.categoriesset) {
            return false;
        }
        if (cellular_telephone1 == null) {
            if (other.cellular_telephone1 != null) {
                return false;
            }
        } else if (!cellular_telephone1.equals(other.cellular_telephone1)) {
            return false;
        }
        if (cellular_telephone1set != other.cellular_telephone1set) {
            return false;
        }
        if (cellular_telephone2 == null) {
            if (other.cellular_telephone2 != null) {
                return false;
            }
        } else if (!cellular_telephone2.equals(other.cellular_telephone2)) {
            return false;
        }
        if (cellular_telephone2set != other.cellular_telephone2set) {
            return false;
        }
        if (city_business == null) {
            if (other.city_business != null) {
                return false;
            }
        } else if (!city_business.equals(other.city_business)) {
            return false;
        }
        if (city_businessset != other.city_businessset) {
            return false;
        }
        if (city_home == null) {
            if (other.city_home != null) {
                return false;
            }
        } else if (!city_home.equals(other.city_home)) {
            return false;
        }
        if (city_homeset != other.city_homeset) {
            return false;
        }
        if (city_other == null) {
            if (other.city_other != null) {
                return false;
            }
        } else if (!city_other.equals(other.city_other)) {
            return false;
        }
        if (city_otherset != other.city_otherset) {
            return false;
        }
        if (commercial_register == null) {
            if (other.commercial_register != null) {
                return false;
            }
        } else if (!commercial_register.equals(other.commercial_register)) {
            return false;
        }
        if (commercial_registerset != other.commercial_registerset) {
            return false;
        }
        if (company == null) {
            if (other.company != null) {
                return false;
            }
        } else if (!company.equals(other.company)) {
            return false;
        }
        if (companyset != other.companyset) {
            return false;
        }
        if (contextadmin != other.contextadmin) {
            return false;
        }
        if (country_business == null) {
            if (other.country_business != null) {
                return false;
            }
        } else if (!country_business.equals(other.country_business)) {
            return false;
        }
        if (country_businessset != other.country_businessset) {
            return false;
        }
        if (country_home == null) {
            if (other.country_home != null) {
                return false;
            }
        } else if (!country_home.equals(other.country_home)) {
            return false;
        }
        if (country_homeset != other.country_homeset) {
            return false;
        }
        if (country_other == null) {
            if (other.country_other != null) {
                return false;
            }
        } else if (!country_other.equals(other.country_other)) {
            return false;
        }
        if (country_otherset != other.country_otherset) {
            return false;
        }
        if (defaultSenderAddress == null) {
            if (other.defaultSenderAddress != null) {
                return false;
            }
        } else if (!defaultSenderAddress.equals(other.defaultSenderAddress)) {
            return false;
        }
        if (defaultSenderAddressset != other.defaultSenderAddressset) {
            return false;
        }
        if (folderTree == null) {
            if (other.folderTree != null) {
                return false;
            }
        } else if (!folderTree.equals(other.folderTree)) {
            return false;
        }
        if (folderTreeSet != other.folderTreeSet) {
            return false;
        }
        if (defaultFolderMode == null) {
            if (other.defaultFolderMode != null) {
                return false;
            }
        } else if (!defaultFolderMode.equals(other.defaultFolderMode)) {
            return false;
        }
        if (defaultFolderModeSet != other.defaultFolderModeSet) {
            return false;
        }
        if (default_group == null) {
            if (other.default_group != null) {
                return false;
            }
        } else if (!default_group.equals(other.default_group)) {
            return false;
        }
        if (default_groupset != other.default_groupset) {
            return false;
        }
        if (department == null) {
            if (other.department != null) {
                return false;
            }
        } else if (!department.equals(other.department)) {
            return false;
        }
        if (departmentset != other.departmentset) {
            return false;
        }
        if (display_name == null) {
            if (other.display_name != null) {
                return false;
            }
        } else if (!display_name.equals(other.display_name)) {
            return false;
        }
        if (display_nameset != other.display_nameset) {
            return false;
        }
        if (email1 == null) {
            if (other.email1 != null) {
                return false;
            }
        } else if (!email1.equals(other.email1)) {
            return false;
        }
        if (email1set != other.email1set) {
            return false;
        }
        if (email2 == null) {
            if (other.email2 != null) {
                return false;
            }
        } else if (!email2.equals(other.email2)) {
            return false;
        }
        if (email2set != other.email2set) {
            return false;
        }
        if (email3 == null) {
            if (other.email3 != null) {
                return false;
            }
        } else if (!email3.equals(other.email3)) {
            return false;
        }
        if (email3set != other.email3set) {
            return false;
        }
        if (employeeType == null) {
            if (other.employeeType != null) {
                return false;
            }
        } else if (!employeeType.equals(other.employeeType)) {
            return false;
        }
        if (employeeTypeset != other.employeeTypeset) {
            return false;
        }
        if (fax_business == null) {
            if (other.fax_business != null) {
                return false;
            }
        } else if (!fax_business.equals(other.fax_business)) {
            return false;
        }
        if (fax_businessset != other.fax_businessset) {
            return false;
        }
        if (fax_home == null) {
            if (other.fax_home != null) {
                return false;
            }
        } else if (!fax_home.equals(other.fax_home)) {
            return false;
        }
        if (fax_homeset != other.fax_homeset) {
            return false;
        }
        if (fax_other == null) {
            if (other.fax_other != null) {
                return false;
            }
        } else if (!fax_other.equals(other.fax_other)) {
            return false;
        }
        if (fax_otherset != other.fax_otherset) {
            return false;
        }
        if (given_name == null) {
            if (other.given_name != null) {
                return false;
            }
        } else if (!given_name.equals(other.given_name)) {
            return false;
        }
        if (given_nameset != other.given_nameset) {
            return false;
        }
        if (guiPreferences == null) {
            if (other.guiPreferences != null) {
                return false;
            }
        } else if (!guiPreferences.equals(other.guiPreferences)) {
            return false;
        }
        if (guiPreferencesset != other.guiPreferencesset) {
            return false;
        }
        if (gui_spam_filter_enabled == null) {
            if (other.gui_spam_filter_enabled != null) {
                return false;
            }
        } else if (!gui_spam_filter_enabled.equals(other.gui_spam_filter_enabled)) {
            return false;
        }
        if (gui_spam_filter_enabledset != other.gui_spam_filter_enabledset) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (idset != other.idset) {
            return false;
        }
        if (imapLogin == null) {
            if (other.imapLogin != null) {
                return false;
            }
        } else if (!imapLogin.equals(other.imapLogin)) {
            return false;
        }
        if (imapLoginset != other.imapLoginset) {
            return false;
        }
        if (imapServer == null) {
            if (other.imapServer != null) {
                return false;
            }
        } else if (!imapServer.equals(other.imapServer)) {
            return false;
        }
        if (imapServerset != other.imapServerset) {
            return false;
        }
        if (info == null) {
            if (other.info != null) {
                return false;
            }
        } else if (!info.equals(other.info)) {
            return false;
        }
        if (infoset != other.infoset) {
            return false;
        }
        if (instant_messenger1 == null) {
            if (other.instant_messenger1 != null) {
                return false;
            }
        } else if (!instant_messenger1.equals(other.instant_messenger1)) {
            return false;
        }
        if (instant_messenger1set != other.instant_messenger1set) {
            return false;
        }
        if (instant_messenger2 == null) {
            if (other.instant_messenger2 != null) {
                return false;
            }
        } else if (!instant_messenger2.equals(other.instant_messenger2)) {
            return false;
        }
        if (instant_messenger2set != other.instant_messenger2set) {
            return false;
        }
        if (language == null) {
            if (other.language != null) {
                return false;
            }
        } else if (!language.equals(other.language)) {
            return false;
        }
        if (languageset != other.languageset) {
            return false;
        }
        if (mail_folder_confirmed_ham_name == null) {
            if (other.mail_folder_confirmed_ham_name != null) {
                return false;
            }
        } else if (!mail_folder_confirmed_ham_name.equals(other.mail_folder_confirmed_ham_name)) {
            return false;
        }
        if (mail_folder_confirmed_ham_nameset != other.mail_folder_confirmed_ham_nameset) {
            return false;
        }
        if (mail_folder_confirmed_spam_name == null) {
            if (other.mail_folder_confirmed_spam_name != null) {
                return false;
            }
        } else if (!mail_folder_confirmed_spam_name.equals(other.mail_folder_confirmed_spam_name)) {
            return false;
        }
        if (mail_folder_confirmed_spam_nameset != other.mail_folder_confirmed_spam_nameset) {
            return false;
        }
        if (mail_folder_drafts_name == null) {
            if (other.mail_folder_drafts_name != null) {
                return false;
            }
        } else if (!mail_folder_drafts_name.equals(other.mail_folder_drafts_name)) {
            return false;
        }
        if (mail_folder_drafts_nameset != other.mail_folder_drafts_nameset) {
            return false;
        }
        if (mail_folder_sent_name == null) {
            if (other.mail_folder_sent_name != null) {
                return false;
            }
        } else if (!mail_folder_sent_name.equals(other.mail_folder_sent_name)) {
            return false;
        }
        if (mail_folder_sent_nameset != other.mail_folder_sent_nameset) {
            return false;
        }
        if (mail_folder_spam_name == null) {
            if (other.mail_folder_spam_name != null) {
                return false;
            }
        } else if (!mail_folder_spam_name.equals(other.mail_folder_spam_name)) {
            return false;
        }
        if (mail_folder_spam_nameset != other.mail_folder_spam_nameset) {
            return false;
        }
        if (mail_folder_trash_name == null) {
            if (other.mail_folder_trash_name != null) {
                return false;
            }
        } else if (!mail_folder_trash_name.equals(other.mail_folder_trash_name)) {
            return false;
        }
        if (mail_folder_trash_nameset != other.mail_folder_trash_nameset) {
            return false;
        }
        if (mail_folder_archive_full_name == null) {
            if (other.mail_folder_archive_full_name != null) {
                return false;
            }
        } else if (!mail_folder_archive_full_name.equals(other.mail_folder_archive_full_name)) {
            return false;
        }
        if (mail_folder_archive_full_nameset != other.mail_folder_archive_full_nameset) {
            return false;
        }
        if (mailenabled == null) {
            if (other.mailenabled != null) {
                return false;
            }
        } else if (!mailenabled.equals(other.mailenabled)) {
            return false;
        }
        if (mailenabledset != other.mailenabledset) {
            return false;
        }
        if (manager_name == null) {
            if (other.manager_name != null) {
                return false;
            }
        } else if (!manager_name.equals(other.manager_name)) {
            return false;
        }
        if (manager_nameset != other.manager_nameset) {
            return false;
        }
        if (marital_status == null) {
            if (other.marital_status != null) {
                return false;
            }
        } else if (!marital_status.equals(other.marital_status)) {
            return false;
        }
        if (marital_statusset != other.marital_statusset) {
            return false;
        }
        if (middle_name == null) {
            if (other.middle_name != null) {
                return false;
            }
        } else if (!middle_name.equals(other.middle_name)) {
            return false;
        }
        if (middle_nameset != other.middle_nameset) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (nameset != other.nameset) {
            return false;
        }
        if (nickname == null) {
            if (other.nickname != null) {
                return false;
            }
        } else if (!nickname.equals(other.nickname)) {
            return false;
        }
        if (nicknameset != other.nicknameset) {
            return false;
        }
        if (note == null) {
            if (other.note != null) {
                return false;
            }
        } else if (!note.equals(other.note)) {
            return false;
        }
        if (noteset != other.noteset) {
            return false;
        }
        if (number_of_children == null) {
            if (other.number_of_children != null) {
                return false;
            }
        } else if (!number_of_children.equals(other.number_of_children)) {
            return false;
        }
        if (number_of_childrenset != other.number_of_childrenset) {
            return false;
        }
        if (number_of_employee == null) {
            if (other.number_of_employee != null) {
                return false;
            }
        } else if (!number_of_employee.equals(other.number_of_employee)) {
            return false;
        }
        if (number_of_employeeset != other.number_of_employeeset) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        if (passwordMech == null) {
            if (other.passwordMech != null) {
                return false;
            }
        } else if (!passwordMech.equals(other.passwordMech)) {
            return false;
        }
        if (passwordMechset != other.passwordMechset) {
            return false;
        }
        if (password_expired == null) {
            if (other.password_expired != null) {
                return false;
            }
        } else if (!password_expired.equals(other.password_expired)) {
            return false;
        }
        if (password_expiredset != other.password_expiredset) {
            return false;
        }
        if (passwordset != other.passwordset) {
            return false;
        }
        if (position == null) {
            if (other.position != null) {
                return false;
            }
        } else if (!position.equals(other.position)) {
            return false;
        }
        if (positionset != other.positionset) {
            return false;
        }
        if (postal_code_business == null) {
            if (other.postal_code_business != null) {
                return false;
            }
        } else if (!postal_code_business.equals(other.postal_code_business)) {
            return false;
        }
        if (postal_code_businessset != other.postal_code_businessset) {
            return false;
        }
        if (postal_code_home == null) {
            if (other.postal_code_home != null) {
                return false;
            }
        } else if (!postal_code_home.equals(other.postal_code_home)) {
            return false;
        }
        if (postal_code_homeset != other.postal_code_homeset) {
            return false;
        }
        if (postal_code_other == null) {
            if (other.postal_code_other != null) {
                return false;
            }
        } else if (!postal_code_other.equals(other.postal_code_other)) {
            return false;
        }
        if (postal_code_otherset != other.postal_code_otherset) {
            return false;
        }
        if (primaryEmail == null) {
            if (other.primaryEmail != null) {
                return false;
            }
        } else if (!primaryEmail.equals(other.primaryEmail)) {
            return false;
        }
        if (primaryEmailset != other.primaryEmailset) {
            return false;
        }
        if (profession == null) {
            if (other.profession != null) {
                return false;
            }
        } else if (!profession.equals(other.profession)) {
            return false;
        }
        if (professionset != other.professionset) {
            return false;
        }
        if (room_number == null) {
            if (other.room_number != null) {
                return false;
            }
        } else if (!room_number.equals(other.room_number)) {
            return false;
        }
        if (room_numberset != other.room_numberset) {
            return false;
        }
        if (sales_volume == null) {
            if (other.sales_volume != null) {
                return false;
            }
        } else if (!sales_volume.equals(other.sales_volume)) {
            return false;
        }
        if (sales_volumeset != other.sales_volumeset) {
            return false;
        }
        if (smtpServer == null) {
            if (other.smtpServer != null) {
                return false;
            }
        } else if (!smtpServer.equals(other.smtpServer)) {
            return false;
        }
        if (smtpServerset != other.smtpServerset) {
            return false;
        }
        if (spouse_name == null) {
            if (other.spouse_name != null) {
                return false;
            }
        } else if (!spouse_name.equals(other.spouse_name)) {
            return false;
        }
        if (spouse_nameset != other.spouse_nameset) {
            return false;
        }
        if (state_business == null) {
            if (other.state_business != null) {
                return false;
            }
        } else if (!state_business.equals(other.state_business)) {
            return false;
        }
        if (state_businessset != other.state_businessset) {
            return false;
        }
        if (state_home == null) {
            if (other.state_home != null) {
                return false;
            }
        } else if (!state_home.equals(other.state_home)) {
            return false;
        }
        if (state_homeset != other.state_homeset) {
            return false;
        }
        if (state_other == null) {
            if (other.state_other != null) {
                return false;
            }
        } else if (!state_other.equals(other.state_other)) {
            return false;
        }
        if (state_otherset != other.state_otherset) {
            return false;
        }
        if (street_business == null) {
            if (other.street_business != null) {
                return false;
            }
        } else if (!street_business.equals(other.street_business)) {
            return false;
        }
        if (street_businessset != other.street_businessset) {
            return false;
        }
        if (street_home == null) {
            if (other.street_home != null) {
                return false;
            }
        } else if (!street_home.equals(other.street_home)) {
            return false;
        }
        if (street_homeset != other.street_homeset) {
            return false;
        }
        if (street_other == null) {
            if (other.street_other != null) {
                return false;
            }
        } else if (!street_other.equals(other.street_other)) {
            return false;
        }
        if (street_otherset != other.street_otherset) {
            return false;
        }
        if (suffix == null) {
            if (other.suffix != null) {
                return false;
            }
        } else if (!suffix.equals(other.suffix)) {
            return false;
        }
        if (suffixset != other.suffixset) {
            return false;
        }
        if (sur_name == null) {
            if (other.sur_name != null) {
                return false;
            }
        } else if (!sur_name.equals(other.sur_name)) {
            return false;
        }
        if (sur_nameset != other.sur_nameset) {
            return false;
        }
        if (tax_id == null) {
            if (other.tax_id != null) {
                return false;
            }
        } else if (!tax_id.equals(other.tax_id)) {
            return false;
        }
        if (tax_idset != other.tax_idset) {
            return false;
        }
        if (telephone_assistant == null) {
            if (other.telephone_assistant != null) {
                return false;
            }
        } else if (!telephone_assistant.equals(other.telephone_assistant)) {
            return false;
        }
        if (telephone_assistantset != other.telephone_assistantset) {
            return false;
        }
        if (telephone_business1 == null) {
            if (other.telephone_business1 != null) {
                return false;
            }
        } else if (!telephone_business1.equals(other.telephone_business1)) {
            return false;
        }
        if (telephone_business1set != other.telephone_business1set) {
            return false;
        }
        if (telephone_business2 == null) {
            if (other.telephone_business2 != null) {
                return false;
            }
        } else if (!telephone_business2.equals(other.telephone_business2)) {
            return false;
        }
        if (telephone_business2set != other.telephone_business2set) {
            return false;
        }
        if (telephone_callback == null) {
            if (other.telephone_callback != null) {
                return false;
            }
        } else if (!telephone_callback.equals(other.telephone_callback)) {
            return false;
        }
        if (telephone_callbackset != other.telephone_callbackset) {
            return false;
        }
        if (telephone_car == null) {
            if (other.telephone_car != null) {
                return false;
            }
        } else if (!telephone_car.equals(other.telephone_car)) {
            return false;
        }
        if (telephone_carset != other.telephone_carset) {
            return false;
        }
        if (telephone_company == null) {
            if (other.telephone_company != null) {
                return false;
            }
        } else if (!telephone_company.equals(other.telephone_company)) {
            return false;
        }
        if (telephone_companyset != other.telephone_companyset) {
            return false;
        }
        if (telephone_home1 == null) {
            if (other.telephone_home1 != null) {
                return false;
            }
        } else if (!telephone_home1.equals(other.telephone_home1)) {
            return false;
        }
        if (telephone_home1set != other.telephone_home1set) {
            return false;
        }
        if (telephone_home2 == null) {
            if (other.telephone_home2 != null) {
                return false;
            }
        } else if (!telephone_home2.equals(other.telephone_home2)) {
            return false;
        }
        if (telephone_home2set != other.telephone_home2set) {
            return false;
        }
        if (telephone_ip == null) {
            if (other.telephone_ip != null) {
                return false;
            }
        } else if (!telephone_ip.equals(other.telephone_ip)) {
            return false;
        }
        if (telephone_ipset != other.telephone_ipset) {
            return false;
        }
        if (telephone_isdn == null) {
            if (other.telephone_isdn != null) {
                return false;
            }
        } else if (!telephone_isdn.equals(other.telephone_isdn)) {
            return false;
        }
        if (telephone_isdnset != other.telephone_isdnset) {
            return false;
        }
        if (telephone_other == null) {
            if (other.telephone_other != null) {
                return false;
            }
        } else if (!telephone_other.equals(other.telephone_other)) {
            return false;
        }
        if (telephone_otherset != other.telephone_otherset) {
            return false;
        }
        if (telephone_pager == null) {
            if (other.telephone_pager != null) {
                return false;
            }
        } else if (!telephone_pager.equals(other.telephone_pager)) {
            return false;
        }
        if (telephone_pagerset != other.telephone_pagerset) {
            return false;
        }
        if (telephone_primary == null) {
            if (other.telephone_primary != null) {
                return false;
            }
        } else if (!telephone_primary.equals(other.telephone_primary)) {
            return false;
        }
        if (telephone_primaryset != other.telephone_primaryset) {
            return false;
        }
        if (telephone_radio == null) {
            if (other.telephone_radio != null) {
                return false;
            }
        } else if (!telephone_radio.equals(other.telephone_radio)) {
            return false;
        }
        if (telephone_radioset != other.telephone_radioset) {
            return false;
        }
        if (telephone_telex == null) {
            if (other.telephone_telex != null) {
                return false;
            }
        } else if (!telephone_telex.equals(other.telephone_telex)) {
            return false;
        }
        if (telephone_telexset != other.telephone_telexset) {
            return false;
        }
        if (telephone_ttytdd == null) {
            if (other.telephone_ttytdd != null) {
                return false;
            }
        } else if (!telephone_ttytdd.equals(other.telephone_ttytdd)) {
            return false;
        }
        if (telephone_ttytddset != other.telephone_ttytddset) {
            return false;
        }
        if (timezone == null) {
            if (other.timezone != null) {
                return false;
            }
        } else if (!timezone.equals(other.timezone)) {
            return false;
        }
        if (timezoneset != other.timezoneset) {
            return false;
        }
        if (title == null) {
            if (other.title != null) {
                return false;
            }
        } else if (!title.equals(other.title)) {
            return false;
        }
        if (titleset != other.titleset) {
            return false;
        }
        if (url == null) {
            if (other.url != null) {
                return false;
            }
        } else if (!url.equals(other.url)) {
            return false;
        }
        if (urlset != other.urlset) {
            return false;
        }
        if (userfield01 == null) {
            if (other.userfield01 != null) {
                return false;
            }
        } else if (!userfield01.equals(other.userfield01)) {
            return false;
        }
        if (userfield01set != other.userfield01set) {
            return false;
        }
        if (userfield02 == null) {
            if (other.userfield02 != null) {
                return false;
            }
        } else if (!userfield02.equals(other.userfield02)) {
            return false;
        }
        if (userfield02set != other.userfield02set) {
            return false;
        }
        if (userfield03 == null) {
            if (other.userfield03 != null) {
                return false;
            }
        } else if (!userfield03.equals(other.userfield03)) {
            return false;
        }
        if (userfield03set != other.userfield03set) {
            return false;
        }
        if (userfield04 == null) {
            if (other.userfield04 != null) {
                return false;
            }
        } else if (!userfield04.equals(other.userfield04)) {
            return false;
        }
        if (userfield04set != other.userfield04set) {
            return false;
        }
        if (userfield05 == null) {
            if (other.userfield05 != null) {
                return false;
            }
        } else if (!userfield05.equals(other.userfield05)) {
            return false;
        }
        if (userfield05set != other.userfield05set) {
            return false;
        }
        if (userfield06 == null) {
            if (other.userfield06 != null) {
                return false;
            }
        } else if (!userfield06.equals(other.userfield06)) {
            return false;
        }
        if (userfield06set != other.userfield06set) {
            return false;
        }
        if (userfield07 == null) {
            if (other.userfield07 != null) {
                return false;
            }
        } else if (!userfield07.equals(other.userfield07)) {
            return false;
        }
        if (userfield07set != other.userfield07set) {
            return false;
        }
        if (userfield08 == null) {
            if (other.userfield08 != null) {
                return false;
            }
        } else if (!userfield08.equals(other.userfield08)) {
            return false;
        }
        if (userfield08set != other.userfield08set) {
            return false;
        }
        if (userfield09 == null) {
            if (other.userfield09 != null) {
                return false;
            }
        } else if (!userfield09.equals(other.userfield09)) {
            return false;
        }
        if (userfield09set != other.userfield09set) {
            return false;
        }
        if (userfield10 == null) {
            if (other.userfield10 != null) {
                return false;
            }
        } else if (!userfield10.equals(other.userfield10)) {
            return false;
        }
        if (userfield10set != other.userfield10set) {
            return false;
        }
        if (userfield11 == null) {
            if (other.userfield11 != null) {
                return false;
            }
        } else if (!userfield11.equals(other.userfield11)) {
            return false;
        }
        if (userfield11set != other.userfield11set) {
            return false;
        }
        if (userfield12 == null) {
            if (other.userfield12 != null) {
                return false;
            }
        } else if (!userfield12.equals(other.userfield12)) {
            return false;
        }
        if (userfield12set != other.userfield12set) {
            return false;
        }
        if (userfield13 == null) {
            if (other.userfield13 != null) {
                return false;
            }
        } else if (!userfield13.equals(other.userfield13)) {
            return false;
        }
        if (userfield13set != other.userfield13set) {
            return false;
        }
        if (userfield14 == null) {
            if (other.userfield14 != null) {
                return false;
            }
        } else if (!userfield14.equals(other.userfield14)) {
            return false;
        }
        if (userfield14set != other.userfield14set) {
            return false;
        }
        if (userfield15 == null) {
            if (other.userfield15 != null) {
                return false;
            }
        } else if (!userfield15.equals(other.userfield15)) {
            return false;
        }
        if (userfield15set != other.userfield15set) {
            return false;
        }
        if (userfield16 == null) {
            if (other.userfield16 != null) {
                return false;
            }
        } else if (!userfield16.equals(other.userfield16)) {
            return false;
        }
        if (userfield16set != other.userfield16set) {
            return false;
        }
        if (userfield17 == null) {
            if (other.userfield17 != null) {
                return false;
            }
        } else if (!userfield17.equals(other.userfield17)) {
            return false;
        }
        if (userfield17set != other.userfield17set) {
            return false;
        }
        if (userfield18 == null) {
            if (other.userfield18 != null) {
                return false;
            }
        } else if (!userfield18.equals(other.userfield18)) {
            return false;
        }
        if (userfield18set != other.userfield18set) {
            return false;
        }
        if (userfield19 == null) {
            if (other.userfield19 != null) {
                return false;
            }
        } else if (!userfield19.equals(other.userfield19)) {
            return false;
        }
        if (userfield19set != other.userfield19set) {
            return false;
        }
        if (userfield20 == null) {
            if (other.userfield20 != null) {
                return false;
            }
        } else if (!userfield20.equals(other.userfield20)) {
            return false;
        }
        if (userfield20set != other.userfield20set) {
            return false;
        }
        if(!primaryAccountName.equals(other.primaryAccountName)){
            return false;
        }
        if(primaryAccountNameSet!=other.primaryAccountNameSet){
            return false;
        }
        return true;
    }

}
