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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.TimeZone;

import com.openexchange.admin.rmi.extensions.OXUserExtensionInterface;

/**
 * This object represents a user of OX.
 * 
 * @author cutmasta
 */
public class User implements Serializable {
    /**
     * For serialization
     */
    private static final long serialVersionUID = -4492376747507390066L;

    private OXUserExtensionInterface[] extensions = null;

    /**
     * Key representing the user identifier number, value must be
     * <code>Integer</code>
     */
    private Integer id;

    /**
     * Key representing the user identifier, value is a <code>String</code>
     */
    private String username;

    /**
     * Represents the password, value is a <code>String</code>
     */
    private String password;

    /**
     * Represents the password encryption mechanism, value is a
     * <code>String</code> Syntax: {MECH}
     */
    private String passwordMech;

    /**
     * Primary mail address, value is a <code>String</code> Data is stored in
     * user table not in prg_contacts. Fieldname is mail
     */
    private String primaryEmail;

    /**
     * Email (business)
     */
    private String email1;

    /**
     * Email (home)
     */
    private String email2;

    /**
     * Email (other)
     */
    private String email3;

    /**
     * Represents the aliases of an user, value is a <code>HashSet[]</code>
     */
    private HashSet<String> aliases;

    /**
     * Last name. Data is stored in field02
     */
    private String sur_name;

    /**
     * First name. Data is stored in field03
     */
    private String given_name;

    /**
     * Key representing if user is enabled or not , value is a
     * <code>Boolean</code>
     */
    private Boolean enabled;

    private Date birthday;

    private Date anniversary;

    private String branches;

    private String business_category;

    private String categories;

    private String postal_code_business;

    private String state_business;

    private String street_business;

    private String telephone_callback;

    private String city_home;

    private String commercial_register;

    private String country_home;

    private String company;

    /**
     * The default group when creating an user, value is a <code>Group</code>-
     * object If not set , the default context group is used.
     */
    private Group default_group;

    private String department;

    /**
     * Display name
     */
    private String display_name;

    /**
     * Job title
     */
    private String employeeType;

    private String fax_business;

    private String fax_home;

    private String fax_other;

    /**
     * Represents the imapserver where the user is located, value is a
     * <code>String</code>. Data is stored in user table not in prg_contacts
     */
    private String imapServer;

    /**
     * Represents the smtpserver the user must use, value is a
     * <code>String</code>. Data is stored in user table not in prg_contacts
     */
    private String smtpServer;

    /**
     * Instant messenger (business)
     */
    private String instant_messenger1;

    private String instant_messenger2;

    /**
     * IP-phone
     */
    private String telephone_ip;

    private String telephone_isdn;

    /**
     * Represents the language of the user, value is <code>Locale</code>. For
     * example: de_DE,en_US Data is stored in user table not in prg_contacts.
     * Fieldname is preferredlanguage
     */
    private Locale language;

    private String mail_folder_drafts_name;

    private String mail_folder_sent_name;

    private String mail_folder_spam_name;

    private String mail_folder_trash_name;

    private String manager_name;

    private String marital_status;

    /**
     * Mobile
     */
    private String cellular_telephone1;

    /**
     * Mobile 2
     */
    private String cellular_telephone2;

    private String info;

    private String nickname;

    private String number_of_children;

    private String note;

    /**
     * Employee ID
     */
    private String number_of_employee;

    private String telephone_pager;

    /**
     * Use this to set the password expired. Value is a <code>Boolean</code>.
     * shadowLastChange in DB
     */
    private Boolean password_expired;

    private String telephone_assistant;

    private String assistant_name;

    private String telephone_business1;

    private String telephone_business2;

    private String telephone_car;

    private String telephone_company;

    /**
     * Phone (home)
     */
    private String telephone_home1;

    /**
     * Phone (home 2)
     */
    private String telephone_home2;

    private String telephone_other;

    private String telephone_primary;

    private String position;

    private String postal_code_home;

    private String profession;

    private String telephone_radio;

    private String room_number;

    private String sales_volume;

    private String city_other;

    private String city_business;

    private String country_other;

    private String country_business;

    /**
     * Second name
     */
    private String middle_name;

    private String postal_code_other;

    private String state_other;

    private String street_other;

    private String spouse_name;

    private String state_home;

    private String street_home;

    private String suffix;

    private String tax_id;

    private String telephone_telex;

    /**
     * Represents the timezone of the user, value is <code>TimeZone</code>
     * Data is stored in user table not in prg_contacts
     */
    private TimeZone timezone;

    private String title;

    private String telephone_ttytdd;

    private String url;

    private String userfield01;

    private String userfield02;

    private String userfield03;

    private String userfield04;

    private String userfield05;

    private String userfield06;

    private String userfield07;

    private String userfield08;

    private String userfield09;

    private String userfield10;

    private String userfield11;

    private String userfield12;

    private String userfield13;

    private String userfield14;

    private String userfield15;

    private String userfield16;

    private String userfield17;

    private String userfield18;

    private String userfield19;

    private String userfield20;

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
    public User(int id) {
        super();
        init();
        this.id = id;
    }

    /**
     * Returns the id of the user as a long
     * 
     * @return Returns the id of the user as a long.
     */
    public Integer getId() {
        return id;
    }

    /**
     * 
     * @param val
     */
    public void setId(Integer userid) {
        this.id = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String passwd) {
        this.password = passwd;
    }

    public String getPrimaryEmail() {
        return primaryEmail;
    }

    public void setPrimaryEmail(String val) {
        this.primaryEmail = val;
    }

    public String getSur_name() {
        return sur_name;
    }

    public void setSur_name(String val) {
        this.sur_name = val;
    }

    public String getGiven_name() {
        return given_name;
    }

    public void setGiven_name(String val) {
        this.given_name = val;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean val) {
        this.enabled = val;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date val) {
        this.birthday = val;
    }

    public Date getAnniversary() {
        return anniversary;
    }

    public void setAnniversary(Date val) {
        this.anniversary = val;
    }

    public String getBranches() {
        return branches;
    }

    public void setBranches(String val) {
        this.branches = val;
    }

    public String getBusiness_category() {
        return business_category;
    }

    public void setBusiness_category(String val) {
        this.business_category = val;
    }

    public String getPostal_code_business() {
        return postal_code_business;
    }

    public void setPostal_code_business(String val) {
        this.postal_code_business = val;
    }

    public String getState_business() {
        return state_business;
    }

    public void setState_business(String val) {
        this.state_business = val;
    }

    public String getStreet_business() {
        return street_business;
    }

    public void setStreet_business(String val) {
        this.street_business = val;
    }

    public String getTelephone_callback() {
        return telephone_callback;
    }

    public void setTelephone_callback(String val) {
        this.telephone_callback = val;
    }

    public String getCity_home() {
        return city_home;
    }

    public void setCity_home(String val) {
        this.city_home = val;
    }

    public String getCommercial_register() {
        return commercial_register;
    }

    public void setCommercial_register(String val) {
        this.commercial_register = val;
    }

    public String getCountry_home() {
        return country_home;
    }

    public void setCountry_home(String val) {
        this.country_home = val;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String val) {
        this.company = val;
    }

    public Group getDefault_group() {
        return default_group;
    }

    public void setDefault_group(Group val) {
        this.default_group = val;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String val) {
        this.department = val;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String val) {
        this.display_name = val;
    }

    public String getEmail2() {
        return email2;
    }

    public void setEmail2(String val) {
        this.email2 = val;
    }

    public String getEmail3() {
        return email3;
    }

    public void setEmail3(String val) {
        this.email3 = val;
    }

    public String getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(String val) {
        this.employeeType = val;
    }

    public String getFax_business() {
        return fax_business;
    }

    public void setFax_business(String val) {
        this.fax_business = val;
    }

    public String getFax_home() {
        return fax_home;
    }

    public void setFax_home(String val) {
        this.fax_home = val;
    }

    public String getFax_other() {
        return fax_other;
    }

    public void setFax_other(String val) {
        this.fax_other = val;
    }

    public int getImapPort() {
        // we should be open to the future and accept values like
        // hostname:port
        if (this.imapServer != null && this.imapServer.contains(":")) {
            String[] sp = imapServer.split(":");
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

    public void setImapServer(String val) {
        this.imapServer = val;
    }

    public String getSmtpServer() {
        return smtpServer;
    }

    public void setSmtpServer(String val) {
        this.smtpServer = val;
    }

    public String getInstant_messenger1() {
        return instant_messenger1;
    }

    public void setInstant_messenger1(String val) {
        this.instant_messenger1 = val;
    }

    public String getInstant_messenger2() {
        return instant_messenger2;
    }

    public void setInstant_messenger2(String val) {
        this.instant_messenger2 = val;
    }

    public String getTelephone_ip() {
        return telephone_ip;
    }

    public void setTelephone_ip(String val) {
        this.telephone_ip = val;
    }

    public String getTelephone_isdn() {
        return telephone_isdn;
    }

    public void setTelephone_isdn(String val) {
        this.telephone_isdn = val;
    }

    public Locale getLanguage() {
        return language;
    }

    public void setLanguage(Locale val) {
        this.language = val;
    }

    public String getMail_folder_drafts_name() {
        return mail_folder_drafts_name;
    }

    public void setMail_folder_drafts_name(String val) {
        this.mail_folder_drafts_name = val;
    }

    public String getMail_folder_sent_name() {
        return mail_folder_sent_name;
    }

    public void setMail_folder_sent_name(String val) {
        this.mail_folder_sent_name = val;
    }

    public String getMail_folder_spam_name() {
        return mail_folder_spam_name;
    }

    public void setMail_folder_spam_name(String val) {
        this.mail_folder_spam_name = val;
    }

    public String getMail_folder_trash_name() {
        return mail_folder_trash_name;
    }

    public void setMail_folder_trash_name(String val) {
        this.mail_folder_trash_name = val;
    }

    public String getManager_name() {
        return manager_name;
    }

    public void setManager_name(String val) {
        this.manager_name = val;
    }

    public String getMarital_status() {
        return marital_status;
    }

    public void setMarital_status(String val) {
        this.marital_status = val;
    }

    public String getCellular_telephone1() {
        return cellular_telephone1;
    }

    public void setCellular_telephone1(String val) {
        this.cellular_telephone1 = val;
    }

    public String getCellular_telephone2() {
        return cellular_telephone2;
    }

    public void setCellular_telephone2(String val) {
        this.cellular_telephone2 = val;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String val) {
        this.info = val;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String val) {
        this.nickname = val;
    }

    public String getNumber_of_children() {
        return number_of_children;
    }

    public void setNumber_of_children(String val) {
        this.number_of_children = val;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String val) {
        this.note = val;
    }

    public String getNumber_of_employee() {
        return number_of_employee;
    }

    public void setNumber_of_employee(String val) {
        this.number_of_employee = val;
    }

    public String getTelephone_pager() {
        return telephone_pager;
    }

    public void setTelephone_pager(String val) {
        this.telephone_pager = val;
    }

    public Boolean getPassword_expired() {
        return password_expired;
    }

    public void setPassword_expired(Boolean val) {
        this.password_expired = val;
    }

    public String getTelephone_assistant() {
        return telephone_assistant;
    }

    public void setTelephone_assistant(String val) {
        this.telephone_assistant = val;
    }

    public String getTelephone_business1() {
        return telephone_business1;
    }

    public void setTelephone_business1(String val) {
        this.telephone_business1 = val;
    }

    public String getTelephone_business2() {
        return telephone_business2;
    }

    public void setTelephone_business2(String val) {
        this.telephone_business2 = val;
    }

    public String getTelephone_car() {
        return telephone_car;
    }

    public void setTelephone_car(String val) {
        this.telephone_car = val;
    }

    public String getTelephone_company() {
        return telephone_company;
    }

    public void setTelephone_company(String val) {
        this.telephone_company = val;
    }

    public String getTelephone_home1() {
        return telephone_home1;
    }

    public void setTelephone_home1(String val) {
        this.telephone_home1 = val;
    }

    public String getTelephone_home2() {
        return telephone_home2;
    }

    public void setTelephone_home2(String val) {
        this.telephone_home2 = val;
    }

    public String getTelephone_other() {
        return telephone_other;
    }

    public void setTelephone_other(String val) {
        this.telephone_other = val;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String val) {
        this.position = val;
    }

    public String getPostal_code_home() {
        return postal_code_home;
    }

    public void setPostal_code_home(String val) {
        this.postal_code_home = val;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String val) {
        this.profession = val;
    }

    public String getTelephone_radio() {
        return telephone_radio;
    }

    public void setTelephone_radio(String val) {
        this.telephone_radio = val;
    }

    public String getRoom_number() {
        return room_number;
    }

    public void setRoom_number(String val) {
        this.room_number = val;
    }

    public String getSales_volume() {
        return sales_volume;
    }

    public void setSales_volume(String val) {
        this.sales_volume = val;
    }

    public String getCity_other() {
        return city_other;
    }

    public void setCity_other(String val) {
        this.city_other = val;
    }

    public String getCountry_other() {
        return country_other;
    }

    public void setCountry_other(String val) {
        this.country_other = val;
    }

    public String getMiddle_name() {
        return middle_name;
    }

    public void setMiddle_name(String val) {
        this.middle_name = val;
    }

    public String getPostal_code_other() {
        return postal_code_other;
    }

    public void setPostal_code_other(String val) {
        this.postal_code_other = val;
    }

    public String getState_other() {
        return state_other;
    }

    public void setState_other(String val) {
        this.state_other = val;
    }

    public String getStreet_other() {
        return street_other;
    }

    public void setStreet_other(String val) {
        this.street_other = val;
    }

    public String getSpouse_name() {
        return spouse_name;
    }

    public void setSpouse_name(String val) {
        this.spouse_name = val;
    }

    public String getState_home() {
        return state_home;
    }

    public void setState_home(String val) {
        this.state_home = val;
    }

    public String getStreet_home() {
        return street_home;
    }

    public void setStreet_home(String val) {
        this.street_home = val;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String val) {
        this.suffix = val;
    }

    public String getTax_id() {
        return tax_id;
    }

    public void setTax_id(String val) {
        this.tax_id = val;
    }

    public String getTelephone_telex() {
        return telephone_telex;
    }

    public void setTelephone_telex(String val) {
        this.telephone_telex = val;
    }

    public TimeZone getTimezone() {
        return timezone;
    }

    public void setTimezone(TimeZone val) {
        this.timezone = val;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String val) {
        this.title = val;
    }

    public String getTelephone_ttytdd() {
        return telephone_ttytdd;
    }

    public void setTelephone_ttytdd(String val) {
        this.telephone_ttytdd = val;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String val) {
        this.url = val;
    }

    public String getUserfield01() {
        return userfield01;
    }

    public void setUserfield01(String val) {
        this.userfield01 = val;
    }

    public String getUserfield02() {
        return userfield02;
    }

    public void setUserfield02(String val) {
        this.userfield02 = val;
    }

    public String getUserfield03() {
        return userfield03;
    }

    public void setUserfield03(String val) {
        this.userfield03 = val;
    }

    public String getUserfield04() {
        return userfield04;
    }

    public void setUserfield04(String val) {
        this.userfield04 = val;
    }

    public String getUserfield05() {
        return userfield05;
    }

    public void setUserfield05(String val) {
        this.userfield05 = val;
    }

    public String getUserfield06() {
        return userfield06;
    }

    public void setUserfield06(String val) {
        this.userfield06 = val;
    }

    public String getUserfield07() {
        return userfield07;
    }

    public void setUserfield07(String val) {
        this.userfield07 = val;
    }

    public String getUserfield08() {
        return userfield08;
    }

    public void setUserfield08(String val) {
        this.userfield08 = val;
    }

    public String getUserfield09() {
        return userfield09;
    }

    public void setUserfield09(String val) {
        this.userfield09 = val;
    }

    public String getUserfield10() {
        return userfield10;
    }

    public void setUserfield10(String val) {
        this.userfield10 = val;
    }

    public String getUserfield11() {
        return userfield11;
    }

    public void setUserfield11(String val) {
        this.userfield11 = val;
    }

    public String getUserfield12() {
        return userfield12;
    }

    public void setUserfield12(String val) {
        this.userfield12 = val;
    }

    public String getUserfield13() {
        return userfield13;
    }

    public void setUserfield13(String val) {
        this.userfield13 = val;
    }

    public String getUserfield14() {
        return userfield14;
    }

    public void setUserfield14(String val) {
        this.userfield14 = val;
    }

    public String getUserfield15() {
        return userfield15;
    }

    public void setUserfield15(String val) {
        this.userfield15 = val;
    }

    public String getUserfield16() {
        return userfield16;
    }

    public void setUserfield16(String val) {
        this.userfield16 = val;
    }

    public String getUserfield17() {
        return userfield17;
    }

    public void setUserfield17(String val) {
        this.userfield17 = val;
    }

    public String getUserfield18() {
        return userfield18;
    }

    public void setUserfield18(String val) {
        this.userfield18 = val;
    }

    public String getUserfield19() {
        return userfield19;
    }

    public void setUserfield19(String val) {
        this.userfield19 = val;
    }

    public String getUserfield20() {
        return userfield20;
    }

    public void setUserfield20(String val) {
        this.userfield20 = val;
    }

    public void setAliases(HashSet<String> aliases) {
        this.aliases = aliases;
    }

    public void addAlias(String alias) {
        if (this.aliases == null) {
            this.aliases = new HashSet<String>();
        }
        this.aliases.add(alias);
    }

    public boolean removeAlias(String alias) {
        if (null != this.aliases) {
            return this.aliases.remove(alias);
        } else {
            return false;
        }
    }

    public HashSet<String> getAliases() {
        return this.aliases;
    }

    public boolean attributesforcreateset() {
        if (null != this.username && null != this.display_name
                && null != this.password && null != this.given_name
                && null != this.sur_name && null != this.primaryEmail) {
            return true;
        } else {
            return false;
        }
    }

    public String getCity_business() {
        return city_business;
    }

    public void setCity_business(String businessCity) {
        this.city_business = businessCity;
    }

    public String getCountry_business() {
        return country_business;
    }

    public void setCountry_business(String businessCountry) {
        this.country_business = businessCountry;
    }

    public String getAssistant_name() {
        return assistant_name;
    }

    public void setAssistant_name(String nameAssistant) {
        this.assistant_name = nameAssistant;
    }

    public String getTelephone_primary() {
        return telephone_primary;
    }

    public void setTelephone_primary(String phonePrimary) {
        this.telephone_primary = phonePrimary;
    }

    public String getCategories() {
        return categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public String getEmail1() {
        return email1;
    }

    public void setEmail1(String privateEmail1) {
        this.email1 = privateEmail1;
    }

    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("[ \n");
        for (final Field f : this.getClass().getDeclaredFields()) {
            try {
                Object ob = f.get(this);
                String tname = f.getName();
                if (ob != null && !tname.equals("serialVersionUID")) {
                    ret.append("  ");
                    ret.append(tname);
                    ret.append(": ");
                    ret.append(ob);
                    ret.append("\n");
                }
            } catch (IllegalArgumentException e) {
                ret.append("IllegalArgument\n");
            } catch (IllegalAccessException e) {
                ret.append("IllegalAccessException\n");
            }
        }
        ret.append("]");
        return ret.toString();
    }

    private void init() {
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
    }

    public void addExtension(final OXUserExtensionInterface extension) {

    }

    public OXUserExtensionInterface[] getExtensions() {
        return this.extensions;
    }

    /**
     * @return the passwordMech
     */
    public String getPasswordMech() {
        return passwordMech;
    }

    /**
     * @param passwordMech
     *            the passwordMech to set
     */
    public void setPasswordMech(String passwordMech) {
        this.passwordMech = passwordMech;
    }
}
