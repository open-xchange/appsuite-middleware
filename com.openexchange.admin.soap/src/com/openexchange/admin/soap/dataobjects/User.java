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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.admin.soap.dataobjects;

import java.util.Date;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.admin.soap.SOAPUtils;

/**
 * Class representing a user.
 *
 */
public class User {

    private static final Pattern URL_PATTERN = Pattern.compile("^(.*?://)?(.*?)(:(.*?))?$");

    private boolean contextadmin = false;

    private Integer id;

    private String name;

    private String password;

    private String passwordMech;

    private String primaryEmail;

    private String email1;

    private String email2;

    private String email3;

    private String[] aliases;

    private String sur_name;

    private String given_name;

    private Boolean mailenabled;

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

    private Group default_group;

    private String department;

    private String display_name;

    private String employeeType;

    private String fax_business;

    private String fax_home;

    private String fax_other;

    private String imapServer;

    private String smtpServer;

    private String imapLogin;

    private String instant_messenger1;

    private String instant_messenger2;

    private String telephone_ip;

    private String telephone_isdn;

    private String language;

    private String mail_folder_drafts_name;

    private String mail_folder_sent_name;

    private String mail_folder_spam_name;

    private String mail_folder_trash_name;

    private String mail_folder_confirmed_spam_name;

    private String mail_folder_confirmed_ham_name;

    private Boolean gui_spam_filter_enabled;

    private String manager_name;

    private String marital_status;

    private String cellular_telephone1;

    private String cellular_telephone2;

    private String info;

    private String nickname;

    private String number_of_children;

    private String note;

    private String number_of_employee;

    private String telephone_pager;

    private Boolean password_expired;

    private String telephone_assistant;

    private String assistant_name;

    private String telephone_business1;

    private String telephone_business2;

    private String telephone_car;

    private String telephone_company;

    private String telephone_home1;

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

    private String timezone;

    private String title;

    private String telephone_ttytdd;

    private Integer uploadFileSizeLimit;

    private Integer uploadFileSizeLimitPerFile;

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

    private String defaultSenderAddress;

    private Integer folderTree;

    private SOAPStringMap guiPreferencesForSoap;

    private SOAPStringMapMap userAttributes;


    /**
     * @return the userAttributes
     */
    public final SOAPStringMapMap getUserAttributes() {
        return userAttributes;
    }


    /**
     * @param userAttributes the userAttributes to set
     */
    public final void setUserAttributes(SOAPStringMapMap userAttributes) {
        this.userAttributes = userAttributes;
    }

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

    public User(com.openexchange.admin.rmi.dataobjects.User u) {
        super();
        SOAPUtils.user2SoapUser(u, this);
    }

    /**
     * Returns the id of the user
     *
     * @return Returns the id of the user as a long.
     */
    final public Integer getId() {
        return id;
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
     * Sets the numeric user id
     *
     * @param userid An {@link Integer} containing the user id
     */
    final public void setId(final Integer userid) {
        this.id = userid;
    }

    final public String getName() {
        return name;
    }

    /**
     * Sets the symbolic user identifier
     *
     * @param username A {@link String} containing the user name
     */
    final public void setName(final String username) {
        this.name = username;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.PasswordMechObject#getPassword()
     */
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
        this.given_name = given_name;
    }

    /**
     * Currently not used
     *
     * @param enabled A {@link Boolean} to activate/deactivate
     */
    final public void setMailenabled(final Boolean enabled) {
        this.mailenabled = enabled;
    }

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
    final public void setAnniversary(final Date anniversary) {
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
        this.fax_other = fax_other;
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
        this.imapServer = imapServer;
    }

    /**
     * Sets the login for the imap server for this user object
     *
     * @param imapLogin A {@link String} containing the login
     */
    final public void setImapLogin(final String imapLogin) {
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
        this.smtpServer = smtpServer;
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
        this.mail_folder_trash_name = mail_folder_trash_name;
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
        this.userfield20 = userfield20;
    }

    /**
     * Sets the E-Mail aliases for this user object
     *
     * @param aliases A {@link HashSet} containing the E-Mail aliases
     */
    final public void setAliases(final String []aliases) {
        this.aliases = aliases;
    }

    /**
     * Returns the complete E-mail aliases of this user object
     *
     * @return A {@link HashSet} containing the complete E-mail aliases
     */
    final public String[] getAliases() {
        return this.aliases;
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
        this.email1 = email1;
    }

    private void init() {
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
        folderTree = null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.PasswordMechObject#getPasswordMech()
     */
    final public String getPasswordMech() {
        return passwordMech;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.PasswordMechObject#setPasswordMech(java.lang.String)
     */
    final public void setPasswordMech(final String passwordMech) {
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
        this.mail_folder_confirmed_spam_name = mail_folder_confirmed_spam_name;
    }

    /**
     * @return the gui_spam_filter_enabled
     */
    final public Boolean getGui_spam_filter_enabled() {
        return gui_spam_filter_enabled;
    }

    /**
     * @param gui_spam_filter_enabled
     *                the gui_spam_filter_enabled to set
     */
    public final void setGui_spam_filter_enabled(Boolean gui_spam_filter_enabled) {
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
        this.defaultSenderAddress = defaultSenderAddress;
    }

    public Integer getFolderTree() {
        return folderTree;
    }

    public void setFolderTree(Integer folderTree) {
        this.folderTree = folderTree;
    }

    /**
     * @param guiPreferences the guiPreferences to set
     */
    public final void setGuiPreferencesForSoap(final SOAPStringMap guiPreferences) {
        this.guiPreferencesForSoap = guiPreferences;
    }

    public final SOAPStringMap getGuiPreferencesForSoap() {
        return this.guiPreferencesForSoap;
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
            final Matcher matcher = URL_PATTERN.matcher(this.imapServer);
            if (matcher.matches() && null != matcher.group(4)) {
                try {
                    return Integer.parseInt(matcher.group(4));
                } catch (final NumberFormatException e) {
                    return 143;
                }
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
            final Matcher matcher = URL_PATTERN.matcher(this.imapServer);
            if (matcher.matches() && null != matcher.group(2)) {
                return matcher.group(2);
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
            final Matcher matcher = URL_PATTERN.matcher(this.imapServer);
            if (matcher.matches() && null != matcher.group(1)) {
                return matcher.group(1);
            }
        }
        return "imap://";
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
            final Matcher matcher = URL_PATTERN.matcher(this.smtpServer);
            if (matcher.matches() && null != matcher.group(4)) {
                try {
                    return Integer.parseInt(matcher.group(4));
                } catch (final NumberFormatException e) {
                    return 25;
                }
            }
        }
        return 25;
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
            final Matcher matcher = URL_PATTERN.matcher(this.smtpServer);
            if (matcher.matches() && null != matcher.group(2)) {
                return matcher.group(2);
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
            final Matcher matcher = URL_PATTERN.matcher(this.smtpServer);
            if (matcher.matches() && null != matcher.group(1)) {
                return matcher.group(1);
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

}
