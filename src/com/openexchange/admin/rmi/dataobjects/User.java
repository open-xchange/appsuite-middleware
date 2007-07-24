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
package com.openexchange.admin.rmi.dataobjects;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.TimeZone;

import com.openexchange.admin.rmi.extensions.OXCommonExtensionInterface;
import com.openexchange.admin.rmi.extensions.OXUserExtensionInterface;

/**
 * Class representing a user
 * 
 * @author cutmasta
 * @author d7
 */
public class User extends ExtendableDataObject {
    /**
     * For serialization
     */
    private static final long serialVersionUID = -4492376747507390066L;

    private boolean contextadmin = false;

    private Integer id;

    private boolean idset = false;

    private String username;

    private boolean usernameset = false;

    private String password;

    private boolean passwordset = false;

    private PASSWORDMECH passwordMech;

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

    private Boolean enabled;

    private boolean enabledset = false;

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

    private Locale language;

    private boolean languageset = false;

    private String mail_folder_drafts_name;

    private boolean mail_folder_drafts_nameset = false;

    private String mail_folder_sent_name;

    private boolean mail_folder_sent_nameset = false;

    private String mail_folder_spam_name;

    private boolean mail_folder_spam_nameset = false;

    private String mail_folder_trash_name;

    private boolean mail_folder_trash_nameset = false;

    private String mail_folder_confirmed_spam_name;

    private boolean mail_folder_confirmed_spam_nameset = false;

    private String mail_folder_confirmed_ham_name;

    private boolean mail_folder_confirmed_ham_nameset = false;

    private Boolean spam_filter_enabled;

    private boolean spam_filter_enabledset = false;

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

    private TimeZone timezone;

    private boolean timezoneset = false;

    private String title;

    private boolean titleset = false;

    private String telephone_ttytdd;

    private boolean telephone_ttytddset = false;

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

    /**
     * Creates a new instance of user
     */
    public User() {
        super();
        init();
    }

    /**
     * @param id
     */
    public User(final int id) {
        super();
        init();
        this.id = id;
    }

    public enum PASSWORDMECH {
        CRYPT, SHA
    }

    /**
     * Returns the String representation of password mechanism as used in the
     * data store as SQL or LDAP
     * 
     * @return String representation of PASSWORDMECH
     */
    public String getPasswordMech2String() {
        if (null != this.passwordMech) {
            switch (this.passwordMech) {
            case CRYPT:
                return "{CRYPT}";
            case SHA:
                return "{SHA}";

            default:
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Returns the id of the user
     * 
     * @return Returns the id of the user as a long.
     */
    public Integer getId() {
        return id;
    }

    public boolean isAliasesset() {
        return aliasesset;
    }

    public boolean isAnniversaryset() {
        return anniversaryset;
    }

    public boolean isAssistant_nameset() {
        return assistant_nameset;
    }

    public boolean isBirthdayset() {
        return birthdayset;
    }

    public boolean isBranchesset() {
        return branchesset;
    }

    public boolean isBusiness_categoryset() {
        return business_categoryset;
    }

    public boolean isCategoriesset() {
        return categoriesset;
    }

    public boolean isCellular_telephone1set() {
        return cellular_telephone1set;
    }

    public boolean isCellular_telephone2set() {
        return cellular_telephone2set;
    }

    public boolean isCity_businessset() {
        return city_businessset;
    }

    public boolean isCity_homeset() {
        return city_homeset;
    }

    public boolean isCity_otherset() {
        return city_otherset;
    }

    public boolean isCommercial_registerset() {
        return commercial_registerset;
    }

    public boolean isCompanyset() {
        return companyset;
    }

    public boolean isCountry_businessset() {
        return country_businessset;
    }

    public boolean isCountry_homeset() {
        return country_homeset;
    }

    public boolean isCountry_otherset() {
        return country_otherset;
    }

    public boolean isDefault_groupset() {
        return default_groupset;
    }

    public boolean isDepartmentset() {
        return departmentset;
    }

    public boolean isDisplay_nameset() {
        return display_nameset;
    }

    public boolean isEmail1set() {
        return email1set;
    }

    public boolean isEmail2set() {
        return email2set;
    }

    public boolean isEmail3set() {
        return email3set;
    }

    public boolean isEmployeeTypeset() {
        return employeeTypeset;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public boolean isEnabledset() {
        return enabledset;
    }

    public boolean isFax_businessset() {
        return fax_businessset;
    }

    public boolean isFax_homeset() {
        return fax_homeset;
    }

    public boolean isFax_otherset() {
        return fax_otherset;
    }

    public boolean isGiven_nameset() {
        return given_nameset;
    }

    public boolean isIdset() {
        return idset;
    }

    public boolean isImapServerset() {
        return imapServerset;
    }

    public boolean isImapLoginset() {
        return imapLoginset;
    }

    public boolean isInfoset() {
        return infoset;
    }

    public boolean isInstant_messenger1set() {
        return instant_messenger1set;
    }

    public boolean isInstant_messenger2set() {
        return instant_messenger2set;
    }

    public boolean isLanguageset() {
        return languageset;
    }

    public boolean isMail_folder_drafts_nameset() {
        return mail_folder_drafts_nameset;
    }

    public boolean isMail_folder_sent_nameset() {
        return mail_folder_sent_nameset;
    }

    public boolean isMail_folder_spam_nameset() {
        return mail_folder_spam_nameset;
    }

    public boolean isMail_folder_trash_nameset() {
        return mail_folder_trash_nameset;
    }

    public boolean isManager_nameset() {
        return manager_nameset;
    }

    public boolean isMarital_statusset() {
        return marital_statusset;
    }

    public boolean isMiddle_nameset() {
        return middle_nameset;
    }

    public boolean isNicknameset() {
        return nicknameset;
    }

    public boolean isNoteset() {
        return noteset;
    }

    public boolean isNumber_of_childrenset() {
        return number_of_childrenset;
    }

    public boolean isNumber_of_employeeset() {
        return number_of_employeeset;
    }

    public boolean isPassword_expiredset() {
        return password_expiredset;
    }

    public boolean isPasswordMechset() {
        return passwordMechset;
    }

    public boolean isPasswordset() {
        return passwordset;
    }

    public boolean isPositionset() {
        return positionset;
    }

    public boolean isPostal_code_businessset() {
        return postal_code_businessset;
    }

    public boolean isPostal_code_homeset() {
        return postal_code_homeset;
    }

    public boolean isPostal_code_otherset() {
        return postal_code_otherset;
    }

    public boolean isPrimaryEmailset() {
        return primaryEmailset;
    }

    public boolean isProfessionset() {
        return professionset;
    }

    public boolean isRoom_numberset() {
        return room_numberset;
    }

    public boolean isSales_volumeset() {
        return sales_volumeset;
    }

    public boolean isSmtpServerset() {
        return smtpServerset;
    }

    public boolean isSpouse_nameset() {
        return spouse_nameset;
    }

    public boolean isState_businessset() {
        return state_businessset;
    }

    public boolean isState_homeset() {
        return state_homeset;
    }

    public boolean isState_otherset() {
        return state_otherset;
    }

    public boolean isStreet_businessset() {
        return street_businessset;
    }

    public boolean isStreet_homeset() {
        return street_homeset;
    }

    public boolean isStreet_otherset() {
        return street_otherset;
    }

    public boolean isSuffixset() {
        return suffixset;
    }

    public boolean isSur_nameset() {
        return sur_nameset;
    }

    public boolean isTax_idset() {
        return tax_idset;
    }

    public boolean isTelephone_assistantset() {
        return telephone_assistantset;
    }

    public boolean isTelephone_business1set() {
        return telephone_business1set;
    }

    public boolean isTelephone_business2set() {
        return telephone_business2set;
    }

    public boolean isTelephone_callbackset() {
        return telephone_callbackset;
    }

    public boolean isTelephone_carset() {
        return telephone_carset;
    }

    public boolean isTelephone_companyset() {
        return telephone_companyset;
    }

    public boolean isTelephone_home1set() {
        return telephone_home1set;
    }

    public boolean isTelephone_home2set() {
        return telephone_home2set;
    }

    public boolean isTelephone_ipset() {
        return telephone_ipset;
    }

    public boolean isTelephone_isdnset() {
        return telephone_isdnset;
    }

    public boolean isTelephone_otherset() {
        return telephone_otherset;
    }

    public boolean isTelephone_pagerset() {
        return telephone_pagerset;
    }

    public boolean isTelephone_primaryset() {
        return telephone_primaryset;
    }

    public boolean isTelephone_radioset() {
        return telephone_radioset;
    }

    public boolean isTelephone_telexset() {
        return telephone_telexset;
    }

    public boolean isTelephone_ttytddset() {
        return telephone_ttytddset;
    }

    public boolean isTimezoneset() {
        return timezoneset;
    }

    public boolean isTitleset() {
        return titleset;
    }

    public boolean isUrlset() {
        return urlset;
    }

    public boolean isUserfield01set() {
        return userfield01set;
    }

    public boolean isUserfield02set() {
        return userfield02set;
    }

    public boolean isUserfield03set() {
        return userfield03set;
    }

    public boolean isUserfield04set() {
        return userfield04set;
    }

    public boolean isUserfield05set() {
        return userfield05set;
    }

    public boolean isUserfield06set() {
        return userfield06set;
    }

    public boolean isUserfield07set() {
        return userfield07set;
    }

    public boolean isUserfield08set() {
        return userfield08set;
    }

    public boolean isUserfield09set() {
        return userfield09set;
    }

    public boolean isUserfield10set() {
        return userfield10set;
    }

    public boolean isUserfield11set() {
        return userfield11set;
    }

    public boolean isUserfield12set() {
        return userfield12set;
    }

    public boolean isUserfield13set() {
        return userfield13set;
    }

    public boolean isUserfield14set() {
        return userfield14set;
    }

    public boolean isUserfield15set() {
        return userfield15set;
    }

    public boolean isUserfield16set() {
        return userfield16set;
    }

    public boolean isUserfield17set() {
        return userfield17set;
    }

    public boolean isUserfield18set() {
        return userfield18set;
    }

    public boolean isUserfield19set() {
        return userfield19set;
    }

    public boolean isUserfield20set() {
        return userfield20set;
    }

    public boolean isUsernameset() {
        return usernameset;
    }

    /**
     * Set user numeric user id
     * 
     * @param userid
     */
    public void setId(final Integer userid) {
        if (null == userid) {
            this.idset = true;
        }
        this.id = userid;
    }

    public String getUsername() {
        return username;
    }

    /**
     * Set symbolic user identifier
     */
    public void setUsername(final String username) {
        if (null == username) {
            this.usernameset = true;
        }
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    /**
     * The users password must be plaintext
     * 
     * @param passwd
     * @see setPasswordMech
     */
    public void setPassword(final String passwd) {
        if (null == password) {
            this.passwordset = true;
        }
        this.password = passwd;
    }

    public String getPrimaryEmail() {
        return primaryEmail;
    }

    /**
     * Primary mail address is the default email address of the user
     */
    public void setPrimaryEmail(final String primaryEmail) {
        if (null == primaryEmail) {
            this.primaryEmailset = true;
        }
        this.primaryEmail = primaryEmail;
    }

    public String getSur_name() {
        return sur_name;
    }

    /**
     * Last name of user
     */
    public void setSur_name(final String sur_name) {
        if (null == sur_name) {
            this.sur_nameset = true;
        }
        this.sur_name = sur_name;
    }

    public String getGiven_name() {
        return given_name;
    }

    /**
     * First name of user
     */
    public void setGiven_name(final String given_name) {
        if (null == given_name) {
            this.given_nameset = true;
        }
        this.given_name = given_name;
    }

    public void setEnabled(final Boolean enabled) {
        if (null == enabled) {
            this.enabledset = true;
        }
        this.enabled = enabled;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(final Date birthday) {
        if (null == birthday) {
            this.birthdayset = true;
        }
        this.birthday = birthday;
    }

    public Date getAnniversary() {
        return anniversary;
    }

    public void setAnniversary(final Date anniversary) {
        if (null == anniversary) {
            this.anniversaryset = true;
        }
        this.anniversary = anniversary;
    }

    public String getBranches() {
        return branches;
    }

    public void setBranches(final String branches) {
        if (null == branches) {
            this.branchesset = true;
        }
        this.branches = branches;
    }

    public String getBusiness_category() {
        return business_category;
    }

    public void setBusiness_category(final String business_category) {
        if (null == business_category) {
            this.business_categoryset = true;
        }
        this.business_category = business_category;
    }

    public String getPostal_code_business() {
        return postal_code_business;
    }

    public void setPostal_code_business(final String postal_code_business) {
        if (null == postal_code_business) {
            this.postal_code_businessset = true;
        }
        this.postal_code_business = postal_code_business;
    }

    public String getState_business() {
        return state_business;
    }

    public void setState_business(final String state_business) {
        if (null == state_business) {
            this.state_businessset = true;
        }
        this.state_business = state_business;
    }

    public String getStreet_business() {
        return street_business;
    }

    public void setStreet_business(final String street_business) {
        if (null == street_business) {
            this.street_businessset = true;
        }
        this.street_business = street_business;
    }

    public String getTelephone_callback() {
        return telephone_callback;
    }

    public void setTelephone_callback(final String telephone_callback) {
        if (null == telephone_callback) {
            this.telephone_callbackset = true;
        }
        this.telephone_callback = telephone_callback;
    }

    public String getCity_home() {
        return city_home;
    }

    public void setCity_home(final String city_home) {
        if (null == city_home) {
            this.city_homeset = true;
        }
        this.city_home = city_home;
    }

    public String getCommercial_register() {
        return commercial_register;
    }

    public void setCommercial_register(final String commercial_register) {
        if (null == commercial_register) {
            this.commercial_registerset = true;
        }
        this.commercial_register = commercial_register;
    }

    public String getCountry_home() {
        return country_home;
    }

    public void setCountry_home(final String country_home) {
        if (null == country_home) {
            this.country_homeset = true;
        }
        this.country_home = country_home;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(final String company) {
        if (null == company) {
            this.companyset = true;
        }
        this.company = company;
    }

    public Group getDefault_group() {
        return default_group;
    }

    /**
     * The default group when creating an user. If not supplied, a default group
     * is used.
     */
    public void setDefault_group(final Group default_group) {
        if (null == default_group) {
            this.default_groupset = true;
        }
        this.default_group = default_group;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(final String department) {
        if (null == department) {
            this.departmentset = true;
        }
        this.department = department;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(final String display_name) {
        if (null == display_name) {
            this.display_nameset = true;
        }
        this.display_name = display_name;
    }

    public String getEmail2() {
        return email2;
    }

    /**
     * Email (home)
     */
    public void setEmail2(final String email2) {
        if (null == email2) {
            this.email2set = true;
        }
        this.email2 = email2;
    }

    public String getEmail3() {
        return email3;
    }

    /**
     * Email (other)
     */
    public void setEmail3(final String email3) {
        if (null == email3) {
            this.email3set = true;
        }
        this.email3 = email3;
    }

    public String getEmployeeType() {
        return employeeType;
    }

    /**
     * Job title
     */
    public void setEmployeeType(final String employeeType) {
        if (null == employeeType) {
            this.employeeTypeset = true;
        }
        this.employeeType = employeeType;
    }

    public String getFax_business() {
        return fax_business;
    }

    public void setFax_business(final String fax_business) {
        if (null == fax_business) {
            this.fax_businessset = true;
        }
        this.fax_business = fax_business;
    }

    public String getFax_home() {
        return fax_home;
    }

    public void setFax_home(final String fax_home) {
        if (null == fax_home) {
            this.fax_homeset = true;
        }
        this.fax_home = fax_home;
    }

    public String getFax_other() {
        return fax_other;
    }

    public void setFax_other(final String fax_other) {
        if (null == fax_other) {
            this.fax_otherset = true;
        }
        this.fax_other = fax_other;
    }

    public int getImapPort() {
        // we should be open to the future and accept values like
        // hostname:port
        if (this.imapServer != null && this.imapServer.contains(":")) {
            final String[] sp = imapServer.split(":");
            if (sp.length > 1 && sp[1].trim().length() > 0) {
                return Integer.parseInt(sp[1]);
            }
        }
        return 143;
    }

    public String getImapServer() {
        // we should be open to the future and accept values like
        // hostname:port
        if (this.imapServer == null) {
            return null;
        }
        if (this.imapServer.contains(":")) {
            return this.imapServer.split(":")[0];
        } else {
            return imapServer;
        }
    }

    public void setImapServer(final String imapServer) {
        if (null == imapServer) {
            this.imapServerset = true;
        }
        this.imapServer = imapServer;
    }

    public void setImapLogin(final String imapLogin) {
        if (null == imapLogin) {
            this.imapLoginset = true;
        }
        this.imapLogin = imapLogin;
    }

    public String getImapLogin() {
        return this.imapLogin;
    }

    public String getSmtpServer() {
        return smtpServer;
    }

    public void setSmtpServer(final String smtpServer) {
        if (null == smtpServer) {
            this.smtpServerset = true;
        }
        this.smtpServer = smtpServer;
    }

    public int getSmtpPort() {
        // we should be open to the future and accept values like
        // hostname:port
        if (this.smtpServer != null && this.smtpServer.contains(":")) {
            final String[] sp = smtpServer.split(":");
            if (sp.length > 1 && sp[1].trim().length() > 0) {
                return Integer.parseInt(sp[1]);
            }
        }
        return 25;
    }

    public String getInstant_messenger1() {
        return instant_messenger1;
    }

    /**
     * Instant messenger (business)
     */
    public void setInstant_messenger1(final String instant_messenger1) {
        if (null == instant_messenger1) {
            this.instant_messenger1set = true;
        }
        this.instant_messenger1 = instant_messenger1;
    }

    public String getInstant_messenger2() {
        return instant_messenger2;
    }

    public void setInstant_messenger2(final String instant_messenger2) {
        if (null == instant_messenger2) {
            this.instant_messenger2set = true;
        }
        this.instant_messenger2 = instant_messenger2;
    }

    public String getTelephone_ip() {
        return telephone_ip;
    }

    public void setTelephone_ip(final String telephone_ip) {
        if (null == telephone_ip) {
            this.telephone_ipset = true;
        }
        this.telephone_ip = telephone_ip;
    }

    public String getTelephone_isdn() {
        return telephone_isdn;
    }

    public void setTelephone_isdn(final String telephone_isdn) {
        if (null == telephone_isdn) {
            this.telephone_isdnset = true;
        }
        this.telephone_isdn = telephone_isdn;
    }

    public Locale getLanguage() {
        return language;
    }

    public void setLanguage(final Locale language) {
        if (null == language) {
            this.languageset = true;
        }
        this.language = language;
    }

    public String getMail_folder_drafts_name() {
        return mail_folder_drafts_name;
    }

    public void setMail_folder_drafts_name(final String mail_folder_drafts_name) {
        if (null == mail_folder_drafts_name) {
            this.mail_folder_drafts_nameset = true;
        }
        this.mail_folder_drafts_name = mail_folder_drafts_name;
    }

    public String getMail_folder_sent_name() {
        return mail_folder_sent_name;
    }

    public void setMail_folder_sent_name(final String mail_folder_sent_name) {
        if (null == mail_folder_sent_name) {
            this.mail_folder_sent_nameset = true;
        }
        this.mail_folder_sent_name = mail_folder_sent_name;
    }

    public String getMail_folder_spam_name() {
        return mail_folder_spam_name;
    }

    public void setMail_folder_spam_name(final String mail_folder_spam_name) {
        if (null == mail_folder_spam_name) {
            this.mail_folder_spam_nameset = true;
        }
        this.mail_folder_spam_name = mail_folder_spam_name;
    }

    public String getMail_folder_trash_name() {
        return mail_folder_trash_name;
    }

    public void setMail_folder_trash_name(final String mail_folder_trash_name) {
        if (null == mail_folder_trash_name) {
            this.mail_folder_trash_nameset = true;
        }
        this.mail_folder_trash_name = mail_folder_trash_name;
    }

    public String getManager_name() {
        return manager_name;
    }

    public void setManager_name(final String manager_name) {
        if (null == manager_name) {
            this.manager_nameset = true;
        }
        this.manager_name = manager_name;
    }

    public String getMarital_status() {
        return marital_status;
    }

    public void setMarital_status(final String marital_status) {
        if (null == marital_status) {
            this.marital_statusset = true;
        }
        this.marital_status = marital_status;
    }

    public String getCellular_telephone1() {
        return cellular_telephone1;
    }

    /**
     * Mobile
     */
    public void setCellular_telephone1(final String cellular_telephone1) {
        if (null == cellular_telephone1) {
            this.cellular_telephone1set = true;
        }
        this.cellular_telephone1 = cellular_telephone1;
    }

    public String getCellular_telephone2() {
        return cellular_telephone2;
    }

    /**
     * Mobile 2
     */
    public void setCellular_telephone2(final String cellular_telephone2) {
        if (null == cellular_telephone2) {
            this.cellular_telephone2set = true;
        }
        this.cellular_telephone2 = cellular_telephone2;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(final String info) {
        if (null == info) {
            this.infoset = true;
        }
        this.info = info;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(final String nickname) {
        if (null == nickname) {
            this.nicknameset = true;
        }
        this.nickname = nickname;
    }

    public String getNumber_of_children() {
        return number_of_children;
    }

    public void setNumber_of_children(final String number_of_children) {
        if (null == number_of_children) {
            this.number_of_childrenset = true;
        }
        this.number_of_children = number_of_children;
    }

    public String getNote() {
        return note;
    }

    public void setNote(final String note) {
        if (null == note) {
            this.noteset = true;
        }
        this.note = note;
    }

    public String getNumber_of_employee() {
        return number_of_employee;
    }

    /**
     * Employee ID
     */
    public void setNumber_of_employee(final String number_of_employee) {
        if (null == number_of_employee) {
            this.number_of_employeeset = true;
        }
        this.number_of_employee = number_of_employee;
    }

    public String getTelephone_pager() {
        return telephone_pager;
    }

    public void setTelephone_pager(final String telephone_pager) {
        if (null == telephone_pager) {
            this.telephone_pagerset = true;
        }
        this.telephone_pager = telephone_pager;
    }

    public Boolean getPassword_expired() {
        return password_expired;
    }

    public void setPassword_expired(final Boolean password_expired) {
        if (null == password_expired) {
            this.password_expiredset = true;
        }
        this.password_expired = password_expired;
    }

    public String getTelephone_assistant() {
        return telephone_assistant;
    }

    public void setTelephone_assistant(final String telephone_assistant) {
        if (null == telephone_assistant) {
            this.telephone_assistantset = true;
        }
        this.telephone_assistant = telephone_assistant;
    }

    public String getTelephone_business1() {
        return telephone_business1;
    }

    public void setTelephone_business1(final String telephone_business1) {
        if (null == telephone_business1) {
            this.telephone_business1set = true;
        }
        this.telephone_business1 = telephone_business1;
    }

    public String getTelephone_business2() {
        return telephone_business2;
    }

    public void setTelephone_business2(final String telephone_business2) {
        if (null == telephone_business2) {
            this.telephone_business2set = true;
        }
        this.telephone_business2 = telephone_business2;
    }

    public String getTelephone_car() {
        return telephone_car;
    }

    public void setTelephone_car(final String telephone_car) {
        if (null == telephone_car) {
            this.telephone_carset = true;
        }
        this.telephone_car = telephone_car;
    }

    public String getTelephone_company() {
        return telephone_company;
    }

    public void setTelephone_company(final String telephone_company) {
        if (null == telephone_company) {
            this.telephone_companyset = true;
        }
        this.telephone_company = telephone_company;
    }

    public String getTelephone_home1() {
        return telephone_home1;
    }

    public void setTelephone_home1(final String telephone_home1) {
        if (null == telephone_home1) {
            this.telephone_home1set = true;
        }
        this.telephone_home1 = telephone_home1;
    }

    public String getTelephone_home2() {
        return telephone_home2;
    }

    public void setTelephone_home2(final String telephone_home2) {
        if (null == telephone_home2) {
            this.telephone_home2set = true;
        }
        this.telephone_home2 = telephone_home2;
    }

    public String getTelephone_other() {
        return telephone_other;
    }

    public void setTelephone_other(final String telephone_other) {
        if (null == telephone_other) {
            this.telephone_otherset = true;
        }
        this.telephone_other = telephone_other;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(final String position) {
        if (null == position) {
            this.positionset = true;
        }
        this.position = position;
    }

    public String getPostal_code_home() {
        return postal_code_home;
    }

    public void setPostal_code_home(final String postal_code_home) {
        if (null == postal_code_home) {
            this.postal_code_homeset = true;
        }
        this.postal_code_home = postal_code_home;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(final String profession) {
        if (null == profession) {
            this.professionset = true;
        }
        this.profession = profession;
    }

    public String getTelephone_radio() {
        return telephone_radio;
    }

    public void setTelephone_radio(final String telephone_radio) {
        if (null == telephone_radio) {
            this.telephone_radioset = true;
        }
        this.telephone_radio = telephone_radio;
    }

    public String getRoom_number() {
        return room_number;
    }

    public void setRoom_number(final String room_number) {
        if (null == room_number) {
            this.room_numberset = true;
        }
        this.room_number = room_number;
    }

    public String getSales_volume() {
        return sales_volume;
    }

    public void setSales_volume(final String sales_volume) {
        if (null == sales_volume) {
            this.sales_volumeset = true;
        }
        this.sales_volume = sales_volume;
    }

    public String getCity_other() {
        return city_other;
    }

    public void setCity_other(final String city_other) {
        if (null == city_other) {
            this.city_otherset = true;
        }
        this.city_other = city_other;
    }

    public String getCountry_other() {
        return country_other;
    }

    public void setCountry_other(final String country_other) {
        if (null == country_other) {
            this.country_otherset = true;
        }
        this.country_other = country_other;
    }

    public String getMiddle_name() {
        return middle_name;
    }

    /**
     * Second name
     */
    public void setMiddle_name(final String middle_name) {
        if (null == middle_name) {
            this.middle_nameset = true;
        }
        this.middle_name = middle_name;
    }

    public String getPostal_code_other() {
        return postal_code_other;
    }

    public void setPostal_code_other(final String postal_code_other) {
        if (null == postal_code_other) {
            this.postal_code_otherset = true;
        }
        this.postal_code_other = postal_code_other;
    }

    public String getState_other() {
        return state_other;
    }

    public void setState_other(final String state_other) {
        if (null == state_other) {
            this.state_otherset = true;
        }
        this.state_other = state_other;
    }

    public String getStreet_other() {
        return street_other;
    }

    public void setStreet_other(final String street_other) {
        if (null == street_other) {
            this.street_otherset = true;
        }
        this.street_other = street_other;
    }

    public String getSpouse_name() {
        return spouse_name;
    }

    public void setSpouse_name(final String spouse_name) {
        if (null == spouse_name) {
            this.spouse_nameset = true;
        }
        this.spouse_name = spouse_name;
    }

    public String getState_home() {
        return state_home;
    }

    public void setState_home(final String state_home) {
        if (null == state_home) {
            this.state_homeset = true;
        }
        this.state_home = state_home;
    }

    public String getStreet_home() {
        return street_home;
    }

    public void setStreet_home(final String street_home) {
        if (null == street_home) {
            this.street_homeset = true;
        }
        this.street_home = street_home;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(final String suffix) {
        if (null == suffix) {
            this.suffixset = true;
        }
        this.suffix = suffix;
    }

    public String getTax_id() {
        return tax_id;
    }

    public void setTax_id(final String tax_id) {
        if (null == tax_id) {
            this.tax_idset = true;
        }
        this.tax_id = tax_id;
    }

    public String getTelephone_telex() {
        return telephone_telex;
    }

    public void setTelephone_telex(final String telephone_telex) {
        if (null == telephone_telex) {
            this.telephone_telexset = true;
        }
        this.telephone_telex = telephone_telex;
    }

    public TimeZone getTimezone() {
        return timezone;
    }

    public void setTimezone(final TimeZone timezone) {
        if (null == timezone) {
            this.timezoneset = true;
        }
        this.timezone = timezone;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        if (null == title) {
            this.titleset = true;
        }
        this.title = title;
    }

    public String getTelephone_ttytdd() {
        return telephone_ttytdd;
    }

    public void setTelephone_ttytdd(final String telephone_ttytdd) {
        if (null == telephone_ttytdd) {
            this.telephone_ttytddset = true;
        }
        this.telephone_ttytdd = telephone_ttytdd;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        if (null == url) {
            this.urlset = true;
        }
        this.url = url;
    }

    public String getUserfield01() {
        return userfield01;
    }

    public void setUserfield01(final String userfield01) {
        if (null == userfield01) {
            this.userfield01set = true;
        }
        this.userfield01 = userfield01;
    }

    public String getUserfield02() {
        return userfield02;
    }

    public void setUserfield02(final String userfield02) {
        if (null == userfield02) {
            this.userfield02set = true;
        }
        this.userfield02 = userfield02;
    }

    public String getUserfield03() {
        return userfield03;
    }

    public void setUserfield03(final String userfield03) {
        if (null == userfield03) {
            this.userfield03set = true;
        }
        this.userfield03 = userfield03;
    }

    public String getUserfield04() {
        return userfield04;
    }

    public void setUserfield04(final String userfield04) {
        if (null == userfield04) {
            this.userfield04set = true;
        }
        this.userfield04 = userfield04;
    }

    public String getUserfield05() {
        return userfield05;
    }

    public void setUserfield05(final String userfield05) {
        if (null == userfield05) {
            this.userfield05set = true;
        }
        this.userfield05 = userfield05;
    }

    public String getUserfield06() {
        return userfield06;
    }

    public void setUserfield06(final String userfield06) {
        if (null == userfield06) {
            this.userfield06set = true;
        }
        this.userfield06 = userfield06;
    }

    public String getUserfield07() {
        return userfield07;
    }

    public void setUserfield07(final String userfield07) {
        if (null == userfield07) {
            this.userfield07set = true;
        }
        this.userfield07 = userfield07;
    }

    public String getUserfield08() {
        return userfield08;
    }

    public void setUserfield08(final String userfield08) {
        if (null == userfield08) {
            this.userfield08set = true;
        }
        this.userfield08 = userfield08;
    }

    public String getUserfield09() {
        return userfield09;
    }

    public void setUserfield09(final String userfield09) {
        if (null == userfield09) {
            this.userfield09set = true;
        }
        this.userfield09 = userfield09;
    }

    public String getUserfield10() {
        return userfield10;
    }

    public void setUserfield10(final String userfield10) {
        if (null == userfield10) {
            this.userfield10set = true;
        }
        this.userfield10 = userfield10;
    }

    public String getUserfield11() {
        return userfield11;
    }

    public void setUserfield11(final String userfield11) {
        if (null == userfield11) {
            this.userfield11set = true;
        }
        this.userfield11 = userfield11;
    }

    public String getUserfield12() {
        return userfield12;
    }

    public void setUserfield12(final String userfield12) {
        if (null == userfield12) {
            this.userfield12set = true;
        }
        this.userfield12 = userfield12;
    }

    public String getUserfield13() {
        return userfield13;
    }

    public void setUserfield13(final String userfield13) {
        if (null == userfield13) {
            this.userfield13set = true;
        }
        this.userfield13 = userfield13;
    }

    public String getUserfield14() {
        return userfield14;
    }

    public void setUserfield14(final String userfield14) {
        if (null == userfield14) {
            this.userfield14set = true;
        }
        this.userfield14 = userfield14;
    }

    public String getUserfield15() {
        return userfield15;
    }

    public void setUserfield15(final String userfield15) {
        if (null == userfield15) {
            this.userfield15set = true;
        }
        this.userfield15 = userfield15;
    }

    public String getUserfield16() {
        return userfield16;
    }

    public void setUserfield16(final String userfield16) {
        if (null == userfield16) {
            this.userfield16set = true;
        }
        this.userfield16 = userfield16;
    }

    public String getUserfield17() {
        return userfield17;
    }

    public void setUserfield17(final String userfield17) {
        if (null == userfield17) {
            this.userfield17set = true;
        }
        this.userfield17 = userfield17;
    }

    public String getUserfield18() {
        return userfield18;
    }

    public void setUserfield18(final String userfield18) {
        if (null == userfield18) {
            this.userfield18set = true;
        }
        this.userfield18 = userfield18;
    }

    public String getUserfield19() {
        return userfield19;
    }

    public void setUserfield19(final String userfield19) {
        if (null == userfield19) {
            this.userfield19set = true;
        }
        this.userfield19 = userfield19;
    }

    public String getUserfield20() {
        return userfield20;
    }

    public void setUserfield20(final String userfield20) {
        if (null == userfield20) {
            this.userfield20set = true;
        }
        this.userfield20 = userfield20;
    }

    public void setAliases(final HashSet<String> aliases) {
        if (null == aliases) {
            this.aliasesset = true;
        }
        this.aliases = aliases;
    }

    public void addAlias(final String alias) {
        if (this.aliases == null) {
            this.aliases = new HashSet<String>();
        }
        this.aliases.add(alias);
    }

    public boolean removeAlias(final String alias) {
        if (null != this.aliases) {
            return this.aliases.remove(alias);
        } else {
            return false;
        }
    }

    public HashSet<String> getAliases() {
        return this.aliases;
    }

    @Override
    protected final String[] getMandatoryMembersCreate() {
        return new String[]{ "username", "display_name", "password", "given_name", "sur_name", "primaryEmail" };
    }
    
    @Override
    protected final String[] getMandatoryMembersChange() {
        return null;
    }

    public String getCity_business() {
        return city_business;
    }

    public void setCity_business(final String city_business) {
        if (null == city_business) {
            this.city_businessset = true;
        }
        this.city_business = city_business;
    }

    public String getCountry_business() {
        return country_business;
    }

    public void setCountry_business(final String country_business) {
        if (null == country_business) {
            this.country_businessset = true;
        }
        this.country_business = country_business;
    }

    public String getAssistant_name() {
        return assistant_name;
    }

    public void setAssistant_name(final String assistant_name) {
        if (null == assistant_name) {
            this.assistant_nameset = true;
        }
        this.assistant_name = assistant_name;
    }

    public String getTelephone_primary() {
        return telephone_primary;
    }

    public void setTelephone_primary(final String telephone_primary) {
        if (null == telephone_primary) {
            this.telephone_primaryset = true;
        }
        this.telephone_primary = telephone_primary;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(final String categories) {
        if (null == categories) {
            this.categoriesset = true;
        }
        this.categories = categories;
    }

    public String getEmail1() {
        return email1;
    }

    /**
     * Email (business)
     */
    public void setEmail1(final String email1) {
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
    public static User hashmapToUser(HashMap<String, Object> hm) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
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
     * @return this user Objects members as key/value pairs in a hashtable
     */
    public Hashtable<String, Object> toHashtable() {
        final Hashtable<String, Object> ht = new Hashtable<String, Object>();

        for (final Field f : this.getClass().getDeclaredFields()) {
            try {
                final Object ob = f.get(this);
                final String tname = f.getName();
                if (ob != null && !tname.equals("serialVersionUID") && !tname.equals("extensions") && !tname.endsWith("set")) {
                    ht.put(tname, ob);
                }
            } catch (final IllegalArgumentException e) {
            } catch (final IllegalAccessException e) {
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
        if (null != this.birthday) {
            object.birthday = (Date) this.birthday.clone();
        }
        if (null != this.anniversary) {
            object.anniversary = (Date) this.anniversary.clone();
        }
        if (null != this.language) {
            object.language = (Locale) this.language.clone();
        }
        if (null != this.timezone) {
            // object.default_group
            object.timezone = (TimeZone) this.timezone.clone();
        }
        return object;
    }

    private void init() {
        initExtendable();
        this.id = null;
        this.username = null;
        this.password = null;
        this.passwordMech = null;
        this.primaryEmail = null;
        this.email1 = null;
        this.email2 = null;
        this.email3 = null;
        this.aliases = null;
        this.sur_name = null;
        this.given_name = null;
        this.enabled = null;
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
    }

    /**
     * @param extension
     * @deprecated Please remove the usage of this method as fast as you can because it used a dangerous downcast.
     * This method will go away with the next update
     */
    public void addExtension(final OXUserExtensionInterface extension) {
        getAllExtensionsAsHash().put(extension.getExtensionName(), extension);
    }

    /**
     * @return
     * @deprecated Please remove the usage of this method as fast as you can
     *             because it used a dangerous downcast. This method will go
     *             away with the next update
     */
    @Deprecated
    public ArrayList<OXUserExtensionInterface> getExtensions() {
        final ArrayList<OXUserExtensionInterface> retval = new ArrayList<OXUserExtensionInterface>();
        for (final OXCommonExtensionInterface commoninterface : getAllExtensionsAsHash().values()) {
            retval.add((OXUserExtensionInterface) commoninterface);
        }
        return retval;
    }
    
    /**
     * @param o
     * @return
     * @deprecated Will be removed with next version. Use removeExtension(final OXCommonExtensionInterface o) instead
     */
    public boolean removeExtension(final OXUserExtensionInterface o) {
        if (null == getAllExtensionsAsHash().remove(o.getExtensionName())) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * @param index
     * @return
     * @deprecated Please remove the usage of this method as fast as you can
     *             because it used a dangerous downcast. This method will go
     *             away with the next update
     */
    public OXUserExtensionInterface removeExtensionByIndex(final int index) {
        final ArrayList<OXCommonExtensionInterface> retval = new ArrayList<OXCommonExtensionInterface>(getAllExtensionsAsHash().values());
        final OXCommonExtensionInterface commonExtensionInterface = retval.get(index);
        return (OXUserExtensionInterface) getAllExtensionsAsHash().remove(commonExtensionInterface);
    }

    /**
     * This method is used to get the extensions through the name of the
     * extension. An Array with all extensions where the name fits will be
     * returned, or an empty array if no fitting extension was found.
     * 
     * @param extname
     *                a String for the extension
     * @return the ArrayList of {@link OXUserExtensionInterface} with extname
     * @deprecated Please remove the usage of this method as fast as you can
     *             because it used a dangerous downcast. This method will go
     *             away with the next update
     */
    public ArrayList<OXUserExtensionInterface> getExtensionbyName(final String extname) {
        final ArrayList<OXUserExtensionInterface> retval = new ArrayList<OXUserExtensionInterface>();
        for (final OXCommonExtensionInterface ext : getAllExtensionsAsHash().values()) {
            if (ext.getExtensionName().equals(extname)) {
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
     * @deprecated Please remove the usage of this method as fast as you can
     *             because it used a dangerous downcast. This method will go
     *             away with the next update
     */
    public OXUserExtensionInterface getFirstExtensionbyName(final String extname) {
        final ArrayList<OXUserExtensionInterface> list = getExtensionbyName(extname);
        if (!list.isEmpty() && list.size() == 1) {
            return list.get(0);
        } else {
            return null;
        }
    }

    /**
     * @return the passwordMech
     */
    public PASSWORDMECH getPasswordMech() {
        return passwordMech;
    }

    /**
     * Represents the password encryption mechanism, value is a password
     * mechanism
     * 
     * @param passwordMech
     *                the passwordMech to set
     */
    public void setPasswordMech(final PASSWORDMECH passwordMech) {
        if (null == passwordMech) {
            this.passwordMechset = true;
        }
        this.passwordMech = passwordMech;
    }

    /**
     * @return the mail_folder_confirmed_ham_name
     */
    public final String getMail_folder_confirmed_ham_name() {
        return mail_folder_confirmed_ham_name;
    }

    /**
     * @param mail_folder_confirmed_ham_name
     *                the mail_folder_confirmed_ham_name to set
     */
    public final void setMail_folder_confirmed_ham_name(String mail_folder_confirmed_ham_name) {
        this.mail_folder_confirmed_ham_nameset = true;
        this.mail_folder_confirmed_ham_name = mail_folder_confirmed_ham_name;
    }

    /**
     * @return the mail_folder_confirmed_spam_name
     */
    public final String getMail_folder_confirmed_spam_name() {
        return mail_folder_confirmed_spam_name;
    }

    /**
     * @param mail_folder_confirmed_spam_name
     *                the mail_folder_confirmed_spam_name to set
     */
    public final void setMail_folder_confirmed_spam_name(String mail_folder_confirmed_spam_name) {
        this.mail_folder_confirmed_ham_nameset = true;
        this.mail_folder_confirmed_spam_name = mail_folder_confirmed_spam_name;
    }

    public boolean isMail_folder_confirmed_spamset() {
        return mail_folder_confirmed_spam_nameset;
    }

    public boolean isMail_folder_confirmed_hamset() {
        return mail_folder_confirmed_ham_nameset;
    }

    public boolean isSpam_filter_enabledset() {
        return spam_filter_enabledset;
    }

    /**
     * @return the spam_filter_enabled
     */
    public final Boolean getSpam_filter_enabled() {
        return spam_filter_enabled;
    }

    /**
     * @param spam_filter_enabled
     *                the spam_filter_enabled to set
     */
    public final void setGUI_Spam_filter_capabilities_enabled(Boolean spam_filter_enabled) {
        this.spam_filter_enabledset = true;
        this.spam_filter_enabled = spam_filter_enabled;
    }

    public final boolean isContextadmin() {
        return contextadmin;
    }

    public final void setContextadmin(boolean contextadmin) {
        this.contextadmin = contextadmin;
    }

    /**
     * @return the defaultSenderAddress
     */
    public final String getDefaultSenderAddress() {
        return defaultSenderAddress;
    }

    /**
     * @param defaultSenderAddress
     *                the defaultSenderAddress to set
     */
    public final void setDefaultSenderAddress(String defaultSenderAddress) {
        this.defaultSenderAddressset = true;
        this.defaultSenderAddress = defaultSenderAddress;
    }

    public boolean isDefaultSenderAddressset() {
        return defaultSenderAddressset;
    }

}
