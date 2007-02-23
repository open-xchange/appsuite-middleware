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
package com.openexchange.admin.container;

import com.openexchange.admin.dataSource.I_OXContext;
import com.openexchange.admin.dataSource.I_OXUser;

import java.util.Date;
import java.util.Hashtable;

/**
 *
 * @author cutmasta
 */
public class User {
    
    private long id = -1;
    private long contextId = -1;
    private String username = null;
    private boolean enabled = false;
    
    
    private String [] alias = null;
    private Date anniversary = null;    
    private String assistantsName = null;
    private Date birthDay = null;
    private String branches = null;
    private String businessCategory = null;
    private String businessCity = null;
    private String businessCountry = null;
    private String businessPostalCode = null;
    private String businessState = null;
    private String businessStreet = null;
    private String callBack = null;
    private String city = null;
    private String commercialRegister = null;
    private String country = null;
    private String company = null;
    private long defaultGroup = -1;
    private String department = null;
    private String displayName = null;
    private String privateEmail1 = null;
    private String privateEmail2 = null;
    private String privateEmail3 = null;
    private String employeeType = null;
    private String faxBusiness = null;
    private String faxHome = null;
    private String faxOther = null;
    private String firstName = null;
    private String imapServer  = null;
    private String smtpServer = null;
    private String instantMessenger = null;
    private String instantMessenger2 = null;
    private String ipPhone = null;
    private String isdn = null;
    private String language = null;
    private String lastName = null;
    private String mailFolderDrafts = null;
    private String mailFolderSent = null;
    private String mailFolderSpam = null;
    private String mailFolderTrash = null;
    private String managersName = null;
    private String maritalStatus = null;
    private String mobile1 = null;
    private String mobile2 = null;
    private String moreInfo = null;
    private String nickName = null;
    private String numberOfChildren = null;
    private String note = null;
    private String numberOfEmployee = null;
    private String pager = null;
    private String password = null;
    private boolean passwordExpired = false;
    private String phoneAssistant = null;
    private String phoneBusiness = null;
    private String phoneBusiness2 = null;
    private String phoneCar = null;
    private String phoneCompany = null;
    private String phoneHome = null;
    private String phoneHome2 = null;
    private String phoneOther = null;
    private String position = null;
    private String postalCode = null;
    private String primaryEMail = null;
    private String profession = null;
    private String radio = null;
    private String roomNumber = null;
    private String salesVolume = null;
    private String secondCity = null;
    private String secondCountry = null;
    private String secondName = null;
    private String secondPostalCode = null;
    private String secondState = null;
    private String secondStreet = null;
    private String spouseName = null;
    private String state = null;
    private String street = null;
    private String suffix = null;
    private String taxId = null;
    private String telex = null;
    private String timeZone = null;
    private String title = null;
    private String ttyTdd = null;
   // private long uidNumber = -1;
    private String url = null;
    private String userfield01 = null;
    private String userfield02 = null;
    private String userfield03 = null;
    private String userfield04 = null;
    private String userfield05 = null;
    private String userfield06 = null;
    private String userfield07 = null;
    private String userfield08 = null;
    private String userfield09 = null;
    private String userfield10 = null;
    private String userfield11 = null;
    private String userfield12 = null;
    private String userfield13 = null;
    private String userfield14 = null;
    private String userfield15 = null;
    private String userfield16 = null;
    private String userfield17 = null;
    private String userfield18 = null;
    private String userfield19 = null;
    private String userfield20 = null;
    
    
    // rights
    private boolean accessCalendar = false;
    private boolean accessContacts = false;
    private boolean accessDelegateTasks = false;
    private boolean accessEditPublicFolders = false;
    private boolean accessForum = false;
    private boolean accessIcal = false;
    private boolean accessInfostore = false;
    private boolean accessPinboardWrite = false;
    private boolean accessProjects = false;
    private boolean accessReadCreateSharedFolders = false;
    private boolean accessRssBookmarks = false;
    private boolean accessRssPortal = false;
    private boolean accessSyncml = false;
    private boolean accessTasks = false;
    private boolean accessVcard = false;
    private boolean accessWebdav = false;
    private boolean accessWebdavXml = false;
    private boolean accessWebmail = false;
    
    public User() {
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public long getContextId() {
        return contextId;
    }
    
    public void setContextId(long contextId) {
        this.contextId = contextId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String[] getAlias() {
        return alias;
    }
    
    public void setAlias(String[] alias) {
        this.alias = alias;
    }
    
    public Date getAnniversary() {
        return anniversary;
    }
    
    public void setAnniversary(Date anniversary) {
        this.anniversary = anniversary;
    }
    
    public String getAssistantsName() {
        return assistantsName;
    }
    
    public void setAssistantsName(String assistantsName) {
        this.assistantsName = assistantsName;
    }
    
    public Date getBirthDay() {
        return birthDay;
    }
    
    public void setBirthDay(Date birthDay) {
        this.birthDay = birthDay;
    }
    
    public String getBranches() {
        return branches;
    }
    
    public void setBranches(String branches) {
        this.branches = branches;
    }
    
    public String getBusinessCategory() {
        return businessCategory;
    }
    
    public void setBusinessCategory(String businessCategory) {
        this.businessCategory = businessCategory;
    }
    
    public String getBusinessCity() {
        return businessCity;
    }
    
    public void setBusinessCity(String businessCity) {
        this.businessCity = businessCity;
    }
    
    public String getBusinessCountry() {
        return businessCountry;
    }
    
    public void setBusinessCountry(String businessCountry) {
        this.businessCountry = businessCountry;
    }
    
    public String getBusinessPostalCode() {
        return businessPostalCode;
    }
    
    public void setBusinessPostalCode(String businessPostalCode) {
        this.businessPostalCode = businessPostalCode;
    }
    
    public String getBusinessState() {
        return businessState;
    }
    
    public void setBusinessState(String businessState) {
        this.businessState = businessState;
    }
    
    public String getBusinessStreet() {
        return businessStreet;
    }
    
    public void setBusinessStreet(String businessStreet) {
        this.businessStreet = businessStreet;
    }
    
    public String getCallBack() {
        return callBack;
    }
    
    public void setCallBack(String callBack) {
        this.callBack = callBack;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getCommercialRegister() {
        return commercialRegister;
    }
    
    public void setCommercialRegister(String commercialRegister) {
        this.commercialRegister = commercialRegister;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getCompany() {
        return company;
    }
    
    public void setCompany(String company) {
        this.company = company;
    }
    
    public long getDefaultGroup() {
        return defaultGroup;
    }
    
    public void setDefaultGroup(long defaultGroup) {
        this.defaultGroup = defaultGroup;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getPrivateEmail1() {
        return privateEmail1;
    }
    
    public void setPrivateEmail1(String privateEmail1) {
        this.privateEmail1 = privateEmail1;
    }
    
    public String getPrivateEmail2() {
        return privateEmail2;
    }
    
    public void setPrivateEmail2(String privateEmail2) {
        this.privateEmail2 = privateEmail2;
    }
    
    public String getPrivateEmail3() {
        return privateEmail3;
    }
    
    public void setPrivateEmail3(String privateEmail3) {
        this.privateEmail3 = privateEmail3;
    }
    
    public String getEmployeeType() {
        return employeeType;
    }
    
    public void setEmployeeType(String employeeType) {
        this.employeeType = employeeType;
    }
    
    public String getFaxBusiness() {
        return faxBusiness;
    }
    
    public void setFaxBusiness(String faxBusiness) {
        this.faxBusiness = faxBusiness;
    }
    
    public String getFaxHome() {
        return faxHome;
    }
    
    public void setFaxHome(String faxHome) {
        this.faxHome = faxHome;
    }
    
    public String getFaxOther() {
        return faxOther;
    }
    
    public void setFaxOther(String faxOther) {
        this.faxOther = faxOther;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }   
    
    public String getImapServer() {
        return imapServer;
    }
    
    public void setImapServer(String imapServer) {
        this.imapServer = imapServer;
    }
    
    public String getSmtpServer() {
        return smtpServer;
    }
    
    public void setSmtpServer(String smtpServer) {
        this.smtpServer = smtpServer;
    }
    
    public String getInstantMessenger() {
        return instantMessenger;
    }
    
    public void setInstantMessenger(String instantMessenger) {
        this.instantMessenger = instantMessenger;
    }
    
    public String getInstantMessenger2() {
        return instantMessenger2;
    }
    
    public void setInstantMessenger2(String instantMessenger2) {
        this.instantMessenger2 = instantMessenger2;
    }
    
    public String getIpPhone() {
        return ipPhone;
    }
    
    public void setIpPhone(String ipPhone) {
        this.ipPhone = ipPhone;
    }
    
    public String getIsdn() {
        return isdn;
    }
    
    public void setIsdn(String isdn) {
        this.isdn = isdn;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getMailFolderDrafts() {
        return mailFolderDrafts;
    }
    
    public void setMailFolderDrafts(String mailFolderDrafts) {
        this.mailFolderDrafts = mailFolderDrafts;
    }
    
    public String getMailFolderSent() {
        return mailFolderSent;
    }
    
    public void setMailFolderSent(String mailFolderSent) {
        this.mailFolderSent = mailFolderSent;
    }
    
    public String getMailFolderSpam() {
        return mailFolderSpam;
    }
    
    public void setMailFolderSpam(String mailFolderSpam) {
        this.mailFolderSpam = mailFolderSpam;
    }
    
    public String getMailFolderTrash() {
        return mailFolderTrash;
    }
    
    public void setMailFolderTrash(String mailFolderTrash) {
        this.mailFolderTrash = mailFolderTrash;
    }
    
    public String getManagersName() {
        return managersName;
    }
    
    public void setManagersName(String managersName) {
        this.managersName = managersName;
    }
    
    public String getMaritalStatus() {
        return maritalStatus;
    }
    
    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }
    
    public String getMobile1() {
        return mobile1;
    }
    
    public void setMobile1(String mobile1) {
        this.mobile1 = mobile1;
    }
    
    public String getMobile2() {
        return mobile2;
    }
    
    public void setMobile2(String mobile2) {
        this.mobile2 = mobile2;
    }
    
    public String getMoreInfo() {
        return moreInfo;
    }
    
    public void setMoreInfo(String moreInfo) {
        this.moreInfo = moreInfo;
    }
    
    public String getNickName() {
        return nickName;
    }
    
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
    
    public String getNumberOfChildren() {
        return numberOfChildren;
    }
    
    public void setNumberOfChildren(String numberOfChildren) {
        this.numberOfChildren = numberOfChildren;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public String getNumberOfEmployee() {
        return numberOfEmployee;
    }
    
    public void setNumberOfEmployee(String numberOfEmployee) {
        this.numberOfEmployee = numberOfEmployee;
    }
    
    public String getPager() {
        return pager;
    }
    
    public void setPager(String pager) {
        this.pager = pager;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public boolean isPasswordExpired() {
        return passwordExpired;
    }
    
    public void setPasswordExpired(boolean passwordExpired) {
        this.passwordExpired = passwordExpired;
    }
    
    public String getPhoneAssistant() {
        return phoneAssistant;
    }
    
    public void setPhoneAssistant(String phoneAssistant) {
        this.phoneAssistant = phoneAssistant;
    }
    
    public String getPhoneBusiness() {
        return phoneBusiness;
    }
    
    public void setPhoneBusiness(String phoneBusiness) {
        this.phoneBusiness = phoneBusiness;
    }
    
    public String getPhoneBusiness2() {
        return phoneBusiness2;
    }
    
    public void setPhoneBusiness2(String phoneBusiness2) {
        this.phoneBusiness2 = phoneBusiness2;
    }
    
    public String getPhoneCar() {
        return phoneCar;
    }
    
    public void setPhoneCar(String phoneCar) {
        this.phoneCar = phoneCar;
    }
    
    public String getPhoneCompany() {
        return phoneCompany;
    }
    
    public void setPhoneCompany(String phoneCompany) {
        this.phoneCompany = phoneCompany;
    }
    
    public String getPhoneHome() {
        return phoneHome;
    }
    
    public void setPhoneHome(String phoneHome) {
        this.phoneHome = phoneHome;
    }
    
    public String getPhoneHome2() {
        return phoneHome2;
    }
    
    public void setPhoneHome2(String phoneHome2) {
        this.phoneHome2 = phoneHome2;
    }
    
    public String getPhoneOther() {
        return phoneOther;
    }
    
    public void setPhoneOther(String phoneOther) {
        this.phoneOther = phoneOther;
    }
    
    public String getPosition() {
        return position;
    }
    
    public void setPosition(String position) {
        this.position = position;
    }
    
    public String getPostalCode() {
        return postalCode;
    }
    
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    
    public String getPrimaryEMail() {
        return primaryEMail;
    }
    
    public void setPrimaryEMail(String primaryEMail) {
        this.primaryEMail = primaryEMail;
    }
    
    public String getProfession() {
        return profession;
    }
    
    public void setProfession(String profession) {
        this.profession = profession;
    }
    
    public String getRadio() {
        return radio;
    }
    
    public void setRadio(String radio) {
        this.radio = radio;
    }
    
    public String getRoomNumber() {
        return roomNumber;
    }
    
    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
    
    public String getSalesVolume() {
        return salesVolume;
    }
    
    public void setSalesVolume(String salesVolume) {
        this.salesVolume = salesVolume;
    }
    
    public String getSecondCity() {
        return secondCity;
    }
    
    public void setSecondCity(String secondCity) {
        this.secondCity = secondCity;
    }
    
    public String getSecondCountry() {
        return secondCountry;
    }
    
    public void setSecondCountry(String secondCountry) {
        this.secondCountry = secondCountry;
    }
    
    public String getSecondName() {
        return secondName;
    }
    
    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }
    
    public String getSecondPostalCode() {
        return secondPostalCode;
    }
    
    public void setSecondPostalCode(String secondPostalCode) {
        this.secondPostalCode = secondPostalCode;
    }
    
    public String getSecondState() {
        return secondState;
    }
    
    public void setSecondState(String secondState) {
        this.secondState = secondState;
    }
    
    public String getSecondStreet() {
        return secondStreet;
    }
    
    public void setSecondStreet(String secondStreet) {
        this.secondStreet = secondStreet;
    }
    
    public String getSpouseName() {
        return spouseName;
    }
    
    public void setSpouseName(String spouseName) {
        this.spouseName = spouseName;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getStreet() {
        return street;
    }
    
    public void setStreet(String street) {
        this.street = street;
    }
    
    public String getSuffix() {
        return suffix;
    }
    
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
    
    public String getTaxId() {
        return taxId;
    }
    
    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }
    
    public String getTelex() {
        return telex;
    }
    
    public void setTelex(String telex) {
        this.telex = telex;
    }
    
    public String getTimeZone() {
        return timeZone;
    }
    
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getTtyTdd() {
        return ttyTdd;
    }
    
    public void setTtyTdd(String ttyTdd) {
        this.ttyTdd = ttyTdd;
    }
    
//    public long getUidNumber() {
//        return uidNumber;
//    }
//    
//    public void setUidNumber(long uidNumber) {
//        this.uidNumber = uidNumber;
//    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getUserfield01() {
        return userfield01;
    }
    
    public void setUserfield01(String userfield01) {
        this.userfield01 = userfield01;
    }
    
    public String getUserfield02() {
        return userfield02;
    }
    
    public void setUserfield02(String userfield02) {
        this.userfield02 = userfield02;
    }
    
    public String getUserfield03() {
        return userfield03;
    }
    
    public void setUserfield03(String userfield03) {
        this.userfield03 = userfield03;
    }
    
    public String getUserfield04() {
        return userfield04;
    }
    
    public void setUserfield04(String userfield04) {
        this.userfield04 = userfield04;
    }
    
    public String getUserfield05() {
        return userfield05;
    }
    
    public void setUserfield05(String userfield05) {
        this.userfield05 = userfield05;
    }
    
    public String getUserfield06() {
        return userfield06;
    }
    
    public void setUserfield06(String userfield06) {
        this.userfield06 = userfield06;
    }
    
    public String getUserfield07() {
        return userfield07;
    }
    
    public void setUserfield07(String userfield07) {
        this.userfield07 = userfield07;
    }
    
    public String getUserfield08() {
        return userfield08;
    }
    
    public void setUserfield08(String userfield08) {
        this.userfield08 = userfield08;
    }
    
    public String getUserfield09() {
        return userfield09;
    }
    
    public void setUserfield09(String userfield09) {
        this.userfield09 = userfield09;
    }
    
    public String getUserfield10() {
        return userfield10;
    }
    
    public void setUserfield10(String userfield10) {
        this.userfield10 = userfield10;
    }
    
    public String getUserfield11() {
        return userfield11;
    }
    
    public void setUserfield11(String userfield11) {
        this.userfield11 = userfield11;
    }
    
    public String getUserfield12() {
        return userfield12;
    }
    
    public void setUserfield12(String userfield12) {
        this.userfield12 = userfield12;
    }
    
    public String getUserfield13() {
        return userfield13;
    }
    
    public void setUserfield13(String userfield13) {
        this.userfield13 = userfield13;
    }
    
    public String getUserfield14() {
        return userfield14;
    }
    
    public void setUserfield14(String userfield14) {
        this.userfield14 = userfield14;
    }
    
    public String getUserfield15() {
        return userfield15;
    }
    
    public void setUserfield15(String userfield15) {
        this.userfield15 = userfield15;
    }
    
    public String getUserfield16() {
        return userfield16;
    }
    
    public void setUserfield16(String userfield16) {
        this.userfield16 = userfield16;
    }
    
    public String getUserfield17() {
        return userfield17;
    }
    
    public void setUserfield17(String userfield17) {
        this.userfield17 = userfield17;
    }
    
    public String getUserfield18() {
        return userfield18;
    }
    
    public void setUserfield18(String userfield18) {
        this.userfield18 = userfield18;
    }
    
    public String getUserfield19() {
        return userfield19;
    }
    
    public void setUserfield19(String userfield19) {
        this.userfield19 = userfield19;
    }
    
    public String getUserfield20() {
        return userfield20;
    }
    
    public void setUserfield20(String userfield20) {
        this.userfield20 = userfield20;
    }
    
    public boolean isAccessCalendar() {
        return accessCalendar;
    }
    
    public void setAccessCalendar(boolean accessCalendar) {
        this.accessCalendar = accessCalendar;
    }
    
    public boolean isAccessContacts() {
        return accessContacts;
    }
    
    public void setAccessContacts(boolean accessContacts) {
        this.accessContacts = accessContacts;
    }
    
    public boolean isAccessDelegateTasks() {
        return accessDelegateTasks;
    }
    
    public void setAccessDelegateTasks(boolean accessDelegateTasks) {
        this.accessDelegateTasks = accessDelegateTasks;
    }
    
    public boolean isAccessEditPublicFolders() {
        return accessEditPublicFolders;
    }
    
    public void setAccessEditPublicFolders(boolean accessEditPublicFolders) {
        this.accessEditPublicFolders = accessEditPublicFolders;
    }
    
    public boolean isAccessForum() {
        return accessForum;
    }
    
    public void setAccessForum(boolean accessForum) {
        this.accessForum = accessForum;
    }
    
    public boolean isAccessIcal() {
        return accessIcal;
    }
    
    public void setAccessIcal(boolean accessIcal) {
        this.accessIcal = accessIcal;
    }
    
    public boolean isAccessInfostore() {
        return accessInfostore;
    }
    
    public void setAccessInfostore(boolean accessInfostore) {
        this.accessInfostore = accessInfostore;
    }
    
    public boolean isAccessPinboardWrite() {
        return accessPinboardWrite;
    }
    
    public void setAccessPinboardWrite(boolean accessPinboardWrite) {
        this.accessPinboardWrite = accessPinboardWrite;
    }
    
    public boolean isAccessProjects() {
        return accessProjects;
    }
    
    public void setAccessProjects(boolean accessProjects) {
        this.accessProjects = accessProjects;
    }
    
    public boolean isAccessReadCreateSharedFolders() {
        return accessReadCreateSharedFolders;
    }
    
    public void setAccessReadCreateSharedFolders(boolean accessReadCreateSharedFolders) {
        this.accessReadCreateSharedFolders = accessReadCreateSharedFolders;
    }
    
    public boolean isAccessRssBookmarks() {
        return accessRssBookmarks;
    }
    
    public void setAccessRssBookmarks(boolean accessRssBookmarks) {
        this.accessRssBookmarks = accessRssBookmarks;
    }
    
    public boolean isAccessRssPortal() {
        return accessRssPortal;
    }
    
    public void setAccessRssPortal(boolean accessRssPortal) {
        this.accessRssPortal = accessRssPortal;
    }
    
    public boolean isAccessSyncml() {
        return accessSyncml;
    }
    
    public void setAccessSyncml(boolean accessSyncml) {
        this.accessSyncml = accessSyncml;
    }
    
    public boolean isAccessTasks() {
        return accessTasks;
    }
    
    public void setAccessTasks(boolean accessTasks) {
        this.accessTasks = accessTasks;
    }
    
    public boolean isAccessVcard() {
        return accessVcard;
    }
    
    public void setAccessVcard(boolean accessVcard) {
        this.accessVcard = accessVcard;
    }
    
    public boolean isAccessWebdav() {
        return accessWebdav;
    }
    
    public void setAccessWebdav(boolean accessWebdav) {
        this.accessWebdav = accessWebdav;
    }
    
    public boolean isAccessWebdavXml() {
        return accessWebdavXml;
    }
    
    public void setAccessWebdavXml(boolean accessWebdavXml) {
        this.accessWebdavXml = accessWebdavXml;
    }
    
    public boolean isAccessWebmail() {
        return accessWebmail;
    }
    
    public void setAccessWebmail(boolean accessWebmail) {
        this.accessWebmail = accessWebmail;
    }
    
    public Hashtable xForm2AccessData(){
        
        Hashtable access = new Hashtable();
        
        access.put(I_OXUser.ACCESS_CALENDAR,new Boolean(this.isAccessCalendar()));
        access.put(I_OXUser.ACCESS_CONTACTS,new Boolean(this.isAccessContacts()));
        access.put(I_OXUser.ACCESS_DELEGATE_TASKS,new Boolean(this.isAccessDelegateTasks()));
        access.put(I_OXUser.ACCESS_EDIT_PUBLIC_FOLDERS,new Boolean(this.isAccessEditPublicFolders()));
        access.put(I_OXUser.ACCESS_FORUM,new Boolean(this.isAccessForum()));
        access.put(I_OXUser.ACCESS_ICAL,new Boolean(this.isAccessIcal()));
        access.put(I_OXUser.ACCESS_INFOSSTORE,new Boolean(this.isAccessInfostore()));
        access.put(I_OXUser.ACCESS_PINBOARD_WRITE_ACCESS,new Boolean(this.isAccessPinboardWrite()));
        access.put(I_OXUser.ACCESS_PROJECTS,new Boolean(this.isAccessProjects()));
        access.put(I_OXUser.ACCESS_READ_CREATE_SHARED_FOLDERS,new Boolean(this.isAccessReadCreateSharedFolders()));
        access.put(I_OXUser.ACCESS_RSS_BOOKMARKS,new Boolean(this.isAccessRssBookmarks()));
        access.put(I_OXUser.ACCESS_RSS_PORTAL,new Boolean(this.isAccessRssPortal()));
        access.put(I_OXUser.ACCESS_SYNCML,new Boolean(this.isAccessSyncml()));
        access.put(I_OXUser.ACCESS_TASKS,new Boolean(this.isAccessTasks()));
        access.put(I_OXUser.ACCESS_VCARD,new Boolean(this.isAccessVcard()));
        access.put(I_OXUser.ACCESS_WEBDAV,new Boolean(this.isAccessWebdav()));
        access.put(I_OXUser.ACCESS_WEBDAV_XML,new Boolean(this.isAccessWebdavXml()));
        access.put(I_OXUser.ACCESS_WEBMAIL,new Boolean(this.isAccessWebmail()));
        
        return access;
    }
    
    public void giveAllAccessRights(){
        this.setAccessCalendar(true);
        this.setAccessContacts(true);
        this.setAccessDelegateTasks(true);
        this.setAccessEditPublicFolders(true);
        this.setAccessForum(true);
        this.setAccessIcal(true);
        this.setAccessInfostore(true);
        this.setAccessPinboardWrite(true);
        this.setAccessProjects(true);
        this.setAccessReadCreateSharedFolders(true);
        this.setAccessRssBookmarks(true);
        this.setAccessRssPortal(true);
        this.setAccessSyncml(true);
        this.setAccessTasks(true);
        this.setAccessVcard(true);
        this.setAccessWebdav(true);
        this.setAccessWebdavXml(true);
        this.setAccessWebmail(true);
    }
    
    public void removeAllAccessRights(){
        this.setAccessCalendar(false);
        this.setAccessContacts(false);
        this.setAccessDelegateTasks(false);
        this.setAccessEditPublicFolders(false);
        this.setAccessForum(false);
        this.setAccessIcal(false);
        this.setAccessInfostore(false);
        this.setAccessPinboardWrite(false);
        this.setAccessProjects(false);
        this.setAccessReadCreateSharedFolders(false);
        this.setAccessRssBookmarks(false);
        this.setAccessRssPortal(false);
        this.setAccessSyncml(false);
        this.setAccessTasks(false);
        this.setAccessVcard(false);
        this.setAccessWebdav(false);
        this.setAccessWebdavXml(false);
        this.setAccessWebmail(false);
    }
    
    public Hashtable xForm2Userdata(){
        Hashtable user = new Hashtable();
        
        if(this.getAlias()!=null){
            user.put(I_OXUser.ALIAS,this.getAlias());
        }
        if(this.getAnniversary()!=null){
            user.put(I_OXUser.ANNIVERSARY,this.getAnniversary());
        }
        
        if(this.getAssistantsName()!=null){
            user.put(I_OXUser.ASSISTANT_NAME,this.getAssistantsName());
        }
        if(this.getBirthDay()!=null){
            user.put(I_OXUser.BIRTHDAY,this.getBirthDay());
        }
        if(this.getBranches()!=null){
            user.put(I_OXUser.BRANCHES,this.getBranches());
        }
        if(this.getBusinessCategory()!=null){
            user.put(I_OXUser.BUSINESS_CATEGORY,this.getBusinessCategory());
        }
        if(this.getBusinessCity()!=null){
            user.put(I_OXUser.CITY_BUSINESS,this.getBusinessCity());
        }
        if(this.getBusinessCountry()!=null){
            user.put(I_OXUser.COUNTRY_BUSINESS,this.getBusinessCountry());
        }
        if(this.getBusinessPostalCode()!=null){
            user.put(I_OXUser.POSTAL_CODE_BUSINESS,this.getBusinessPostalCode());
        }
        if(this.getBusinessState()!=null){
            user.put(I_OXUser.STATE_BUSINESS,this.getBusinessState());
        }
        if(this.getBusinessStreet()!=null){
            user.put(I_OXUser.STREET_BUSINESS,this.getBusinessStreet());
        }
        if(this.getCallBack()!=null){
            user.put(I_OXUser.TELEPHONE_CALLBACK,this.getCallBack());
        }
        
        if(this.getCity()!=null){
            user.put(I_OXUser.CITY_HOME,this.getCity());
        }
        if(this.getCommercialRegister()!=null){
            user.put(I_OXUser.COMMERCIAL_REGISTER,this.getCommercialRegister());
        }
        if(this.getCompany()!=null){
            user.put(I_OXUser.COMPANY,this.getCompany());
        }
        if(this.getCountry()!=null){
            user.put(I_OXUser.COUNTRY_HOME,this.getCountry());
        }
        if(this.getDefaultGroup()!=-1){
            user.put(I_OXUser.DEFAULT_GROUP,this.getDefaultGroup());
        }
        if(this.getDepartment()!=null){
            user.put(I_OXUser.DEPARTMENT,this.getDepartment());
        }
        if(this.getDisplayName()!=null){
            user.put(I_OXUser.DISPLAY_NAME,this.getDisplayName());
        }
        if(this.getPrivateEmail1()!=null){
            user.put(I_OXUser.EMAIL1,this.getPrivateEmail1());
        }
        if(this.getPrivateEmail2()!=null){
            user.put(I_OXUser.EMAIL2,this.getPrivateEmail2());
        }
        if(this.getPrivateEmail3()!=null){
            user.put(I_OXUser.EMAIL3,this.getPrivateEmail3());
        }
        if(this.getEmployeeType()!=null){
            user.put(I_OXUser.EMPLOYEE_TYPE,this.getEmployeeType());
        }
        //if(this.isEnabled()!=null){
            user.put(I_OXUser.ENABLED,this.isEnabled());
        //}
        if(this.getFaxBusiness()!=null){
            user.put(I_OXUser.FAX_BUSINESS,this.getFaxBusiness());
        }
        if(this.getFaxHome()!=null){
            user.put(I_OXUser.FAX_HOME,this.getFaxHome());
        }
        if(this.getFaxOther()!=null){
            user.put(I_OXUser.FAX_OTHER,this.getFaxOther());
        }
        if(this.getFirstName()!=null){
            user.put(I_OXUser.GIVEN_NAME,this.getFirstName());
        }
        if(this.getImapServer()!=null){
            user.put(I_OXUser.IMAP_SERVER,this.getImapServer());
        }
        if(this.getInstantMessenger()!=null){
            user.put(I_OXUser.INSTANT_MESSENGER1,this.getInstantMessenger());
        }
        if(this.getInstantMessenger2()!=null){
            user.put(I_OXUser.INSTANT_MESSENGER2,this.getInstantMessenger2());
        }
        if(this.getIpPhone()!=null){
            user.put(I_OXUser.TELEPHONE_IP,this.getIpPhone());
        }
        if(this.getIsdn()!=null){
            user.put(I_OXUser.TELEPHONE_ISDN,this.getIsdn());
        }
        if(this.getLanguage()!=null){
            user.put(I_OXUser.LANGUAGE,this.getLanguage());
        }
        if(this.getLastName()!=null){
            user.put(I_OXUser.SUR_NAME,this.getLastName());
        }
        if(this.getMailFolderDrafts()!=null){
            user.put(I_OXUser.MAIL_FOLDER_DRAFTS,this.getMailFolderDrafts());
        }
        if(this.getMailFolderSent()!=null){
            user.put(I_OXUser.MAIL_FOLDER_SENT,this.getMailFolderSent());
        }
        if(this.getMailFolderSpam()!=null){
            user.put(I_OXUser.MAIL_FOLDER_SPAM,this.getMailFolderSpam());
        }
        if(this.getMailFolderTrash()!=null){
            user.put(I_OXUser.MAIL_FOLDER_TRASH,this.getMailFolderTrash());
        }
        if(this.getManagersName()!=null){
            user.put(I_OXUser.MANAGER_NAME,this.getManagersName());
        }
        if(this.getMaritalStatus()!=null){
            user.put(I_OXUser.MARITAL_STATUS,this.getMaritalStatus());
        }
        if(this.getMobile1()!=null){
            user.put(I_OXUser.CELLULAR_TELEPHONE1,this.getMobile1());
        }
        if(this.getMobile2()!=null){
            user.put(I_OXUser.CELLULAR_TELEPHONE2,this.getMobile2());
        }
            
        if(this.getMoreInfo()!=null){
            user.put(I_OXUser.INFO,this.getMoreInfo());
        }
        if(this.getNickName()!=null){
            user.put(I_OXUser.NICKNAME,this.getNickName());
        }
        if(this.getNote()!=null){
            user.put(I_OXUser.NOTE,this.getNote());
        }
        if(this.getNumberOfChildren()!=null){
            user.put(I_OXUser.NUMBER_OF_CHILDREN,this.getNumberOfChildren());
        }
        if(this.getNumberOfEmployee()!=null){
            user.put(I_OXUser.NUMBER_OF_EMPLOYEE,this.getNumberOfEmployee());
        }
        if(this.getPager()!=null){
            user.put(I_OXUser.TELEPHONE_PAGER,this.getPager());
        }
        if(this.getPassword()!=null){
            user.put(I_OXUser.PASSWORD,this.getPassword());
        }
//        if(this.isPasswordExpired()!=null){
            user.put(I_OXUser.PASSWORD_EXPIRED,this.isPasswordExpired());
//        }
        if(this.getPhoneAssistant()!=null){
            user.put(I_OXUser.TELEPHONE_ASSISTANT,this.getPhoneAssistant());
        }
        if(this.getPhoneBusiness()!=null){
            user.put(I_OXUser.TELEPHONE_BUSINESS1,this.getPhoneBusiness());
        }
        if(this.getPhoneBusiness2()!=null){
            user.put(I_OXUser.TELEPHONE_BUSINESS2,this.getPhoneBusiness2());
        }
        if(this.getPhoneCar()!=null){
            user.put(I_OXUser.TELEPHONE_CAR,this.getPhoneCar());
        }
        if(this.getPhoneCompany()!=null){
            user.put(I_OXUser.TELEPHONE_COMPANY,this.getPhoneCompany());
        }
        if(this.getPhoneHome()!=null){
            user.put(I_OXUser.TELEPHONE_HOME1,this.getPhoneHome());
        }
        if(this.getPhoneHome2()!=null){
            user.put(I_OXUser.TELEPHONE_HOME2,this.getPhoneHome2());
        }
        if(this.getPhoneOther()!=null){
            user.put(I_OXUser.TELEPHONE_OTHER,this.getPhoneOther());
        }
        if(this.getPosition()!=null){
            user.put(I_OXUser.POSITION,this.getPosition());
        }
        if(this.getPostalCode()!=null){
            user.put(I_OXUser.POSTAL_CODE_HOME,this.getPostalCode());
        }
        if(this.getPrimaryEMail()!=null){
            user.put(I_OXUser.PRIMARY_MAIL,this.getPrimaryEMail());
        }
        if(this.getProfession()!=null){
            user.put(I_OXUser.PROFESSION,this.getProfession());
        }
        if(this.getRadio()!=null){
            user.put(I_OXUser.TELEPHONE_RADIO,this.getRadio());
        }
        if(this.getRoomNumber()!=null){
            user.put(I_OXUser.ROOM_NUMBER,this.getRoomNumber());
        }
        if(this.getSalesVolume()!=null){
            user.put(I_OXUser.SALES_VOLUME,this.getSalesVolume());
        }
        if(this.getSecondCity()!=null){
            user.put(I_OXUser.CITY_OTHER,this.getSecondCity());
        }
        if(this.getSecondCountry()!=null){
            user.put(I_OXUser.COUNTRY_OTHER,this.getSecondCountry());
        }
        if(this.getSecondName()!=null){
            user.put(I_OXUser.MIDDLE_NAME,this.getSecondName());
        }
        if(this.getSecondPostalCode()!=null){
            user.put(I_OXUser.POSTAL_CODE_OTHER,this.getSecondPostalCode());
        }
        if(this.getSecondState()!=null){
            user.put(I_OXUser.STATE_OTHER,this.getSecondState());
        }
        if(this.getSecondStreet()!=null){
            user.put(I_OXUser.STREET_OTHER,this.getSecondStreet());
        }
        if(this.getSmtpServer()!=null){
            user.put(I_OXUser.SMTP_SERVER,this.getSmtpServer());
        }
        if(this.getSpouseName()!=null){
            user.put(I_OXUser.SPOUSE_NAME,this.getSpouseName());
        }
           // CHECK  
        if(this.getState()!=null){
            user.put(I_OXUser.STATE_HOME,this.getState());
        }
        if(this.getStreet()!=null){
            user.put(I_OXUser.STREET_HOME,this.getStreet());
        }
        if(this.getSuffix()!=null){
            user.put(I_OXUser.SUFFIX,this.getSuffix());
        }
        
        if(this.getTaxId()!=null){
            user.put(I_OXUser.TAX_ID,this.getTaxId());
        }
        if(this.getTelex()!=null){
            user.put(I_OXUser.TELEPHONE_TELEX,this.getTelex());
        }
        if(this.getTimeZone()!=null){
            user.put(I_OXUser.TIMEZONE,this.getTimeZone());
        }
        if(this.getTitle()!=null){
            user.put(I_OXUser.TITLE,this.getTitle());
        }
        if(this.getTtyTdd()!=null){
            user.put(I_OXUser.TELEPHONE_TTYTDD,this.getTtyTdd());
        }
        if(this.getId()!=-1){
            user.put(I_OXUser.UID_NUMBER,this.getId());
        }
        if(this.getUsername()!=null){
            user.put(I_OXUser.UID,this.getUsername());
        }
        if(this.getUrl()!=null){
            user.put(I_OXUser.URL,this.getUrl());
        }
        if(this.getUserfield01()!=null){
            user.put(I_OXUser.USERFIELD01,this.getUserfield01());
        }
        if(this.getUserfield02()!=null){
            user.put(I_OXUser.USERFIELD02,this.getUserfield02());
        }
        if(this.getUserfield03()!=null){
            user.put(I_OXUser.USERFIELD03,this.getUserfield03());
        }
        if(this.getUserfield04()!=null){
            user.put(I_OXUser.USERFIELD04,this.getUserfield04());
        }
        if(this.getUserfield05()!=null){
            user.put(I_OXUser.USERFIELD05,this.getUserfield05());
        }
        if(this.getUserfield06()!=null){
            user.put(I_OXUser.USERFIELD06,this.getUserfield06());
        }
        if(this.getUserfield07()!=null){
            user.put(I_OXUser.USERFIELD07,this.getUserfield07());
        }
        if(this.getUserfield08()!=null){
            user.put(I_OXUser.USERFIELD08,this.getUserfield08());
        }
        if(this.getUserfield09()!=null){
            user.put(I_OXUser.USERFIELD09,this.getUserfield09());
        }
        if(this.getUserfield10()!=null){
            user.put(I_OXUser.USERFIELD10,this.getUserfield10());
        }
        if(this.getUserfield11()!=null){
            user.put(I_OXUser.USERFIELD11,this.getUserfield11());
        }
        if(this.getUserfield12()!=null){
            user.put(I_OXUser.USERFIELD12,this.getUserfield12());
        }
        if(this.getUserfield13()!=null){
            user.put(I_OXUser.USERFIELD13,this.getUserfield13());
        }
        if(this.getUserfield14()!=null){
            user.put(I_OXUser.USERFIELD14,this.getUserfield14());
        }
        if(this.getUserfield15()!=null){
            user.put(I_OXUser.USERFIELD15,this.getUserfield15());
        }
        if(this.getUserfield16()!=null){
            user.put(I_OXUser.USERFIELD16,this.getUserfield16());
        }
        if(this.getUserfield17()!=null){
            user.put(I_OXUser.USERFIELD17,this.getUserfield17());
        }
        if(this.getUserfield18()!=null){
            user.put(I_OXUser.USERFIELD18,this.getUserfield18());
        }
        if(this.getUserfield19()!=null){
            user.put(I_OXUser.USERFIELD19,this.getUserfield19());
        }
        if(this.getUserfield20()!=null){
            user.put(I_OXUser.USERFIELD20,this.getUserfield20());
        }
        
        return user;
    }

    public void xForm2Object(Hashtable data) {
               
        if(data.containsKey(I_OXUser.UID)){
            this.setUsername(data.get(I_OXUser.UID).toString());
        }
        if(data.containsKey(I_OXUser.UID_NUMBER)){
            this.setId(Long.parseLong(""+data.get(I_OXUser.UID_NUMBER)));
        }
        if(data.containsKey(I_OXContext.CONTEXT_ID)){
            this.setContextId(Long.parseLong(""+data.get(I_OXContext.CONTEXT_ID)));
        }
        if(data.containsKey(I_OXUser.ENABLED)){
            this.setEnabled(((Boolean)data.get(I_OXUser.ENABLED)).booleanValue());
        }
        if(data.containsKey(I_OXUser.ALIAS)){
            this.setAlias((String[])data.get(I_OXUser.ALIAS));
        }
        if(data.containsKey(I_OXUser.ANNIVERSARY)){
            this.setAnniversary((Date)data.get(I_OXUser.ANNIVERSARY));
        }
        
        if(data.containsKey(I_OXUser.ASSISTANT_NAME)){
            this.setAssistantsName((String)data.get(I_OXUser.ASSISTANT_NAME));
        }
        if(data.containsKey(I_OXUser.BIRTHDAY)){
            this.setBirthDay((Date)data.get(I_OXUser.BIRTHDAY));
        }
        if(data.containsKey(I_OXUser.BRANCHES)){
            this.setBranches((String)data.get(I_OXUser.BRANCHES));
        }
        if(data.containsKey(I_OXUser.BUSINESS_CATEGORY)){
            this.setBusinessCategory((String)data.get(I_OXUser.BUSINESS_CATEGORY));
        }
        if(data.containsKey(I_OXUser.CITY_BUSINESS)){
            this.setBusinessCity((String)data.get(I_OXUser.CITY_BUSINESS));
        }
        if(data.containsKey(I_OXUser.COUNTRY_BUSINESS)){
            this.setBusinessCountry((String)data.get(I_OXUser.COUNTRY_BUSINESS));
        }
        if(data.containsKey(I_OXUser.POSTAL_CODE_BUSINESS)){
            this.setBusinessPostalCode((String)data.get(I_OXUser.POSTAL_CODE_BUSINESS));
        }
        if(data.containsKey(I_OXUser.STATE_BUSINESS)){
            this.setBusinessState((String)data.get(I_OXUser.STATE_BUSINESS));
        }
        if(data.containsKey(I_OXUser.STREET_BUSINESS)){
            this.setBusinessStreet((String)data.get(I_OXUser.STREET_BUSINESS));
        }
        if(data.containsKey(I_OXUser.TELEPHONE_CALLBACK)){
            this.setCallBack((String)data.get(I_OXUser.TELEPHONE_CALLBACK));
        }
       
        if(data.containsKey(I_OXUser.CITY_BUSINESS)){
            this.setCity((String)data.get(I_OXUser.CITY_BUSINESS));
        }
        if(data.containsKey(I_OXUser.COMMERCIAL_REGISTER)){
            this.setCommercialRegister((String)data.get(I_OXUser.COMMERCIAL_REGISTER));
        }
        if(data.containsKey(I_OXUser.COMPANY)){
            this.setCompany((String)data.get(I_OXUser.COMPANY));
        }
        if(data.containsKey(I_OXUser.COUNTRY_HOME)){
            this.setCountry((String)data.get(I_OXUser.COUNTRY_HOME));
        }
        if(data.containsKey(I_OXUser.DEFAULT_GROUP)){
            this.setDefaultGroup(Long.parseLong(""+data.get(I_OXUser.DEFAULT_GROUP)));
        }
        if(data.containsKey(I_OXUser.DEPARTMENT)){
            this.setDepartment((String)data.get(I_OXUser.DEPARTMENT));
        }
        if(data.containsKey(I_OXUser.DISPLAY_NAME)){
            this.setDisplayName((String)data.get(I_OXUser.DISPLAY_NAME));
        }
        if(data.containsKey(I_OXUser.EMPLOYEE_TYPE)){
            this.setEmployeeType((String)data.get(I_OXUser.EMPLOYEE_TYPE));
        }
        if(data.containsKey(I_OXUser.FAX_BUSINESS)){
            this.setFaxBusiness((String)data.get(I_OXUser.FAX_BUSINESS));
        }
        if(data.containsKey(I_OXUser.FAX_HOME)){
            this.setFaxHome((String)data.get(I_OXUser.FAX_HOME));
        }
        if(data.containsKey(I_OXUser.FAX_OTHER)){
            this.setFaxOther((String)data.get(I_OXUser.FAX_OTHER));
        }
        if(data.containsKey(I_OXUser.GIVEN_NAME)){
            this.setFirstName((String)data.get(I_OXUser.GIVEN_NAME));
        }
        
        if(data.containsKey(I_OXUser.IMAP_SERVER)){
            this.setImapServer((String)data.get(I_OXUser.IMAP_SERVER));
        }
        if(data.containsKey(I_OXUser.INSTANT_MESSENGER1)){
            this.setInstantMessenger((String)data.get(I_OXUser.INSTANT_MESSENGER1));
        }
        if(data.containsKey(I_OXUser.INSTANT_MESSENGER2)){
            this.setInstantMessenger2((String)data.get(I_OXUser.INSTANT_MESSENGER2));
        }
        if(data.containsKey(I_OXUser.TELEPHONE_IP)){
            this.setIpPhone((String)data.get(I_OXUser.TELEPHONE_IP));
        }
        if(data.containsKey(I_OXUser.TELEPHONE_ISDN)){
            this.setIsdn((String)data.get(I_OXUser.TELEPHONE_ISDN));
        }
        if(data.containsKey(I_OXUser.LANGUAGE)){
            this.setLanguage((String)data.get(I_OXUser.LANGUAGE));
        }
        if(data.containsKey(I_OXUser.SUR_NAME)){
            this.setLastName((String)data.get(I_OXUser.SUR_NAME));
        }
        if(data.containsKey(I_OXUser.MAIL_FOLDER_DRAFTS)){
            this.setMailFolderDrafts((String)data.get(I_OXUser.MAIL_FOLDER_DRAFTS));
        }
        if(data.containsKey(I_OXUser.MAIL_FOLDER_SENT)){
            this.setMailFolderSent((String)data.get(I_OXUser.MAIL_FOLDER_SENT));
        }
        if(data.containsKey(I_OXUser.MAIL_FOLDER_SPAM)){
            this.setMailFolderSpam((String)data.get(I_OXUser.MAIL_FOLDER_SPAM));
        }
        if(data.containsKey(I_OXUser.MAIL_FOLDER_TRASH)){
            this.setMailFolderTrash((String)data.get(I_OXUser.MAIL_FOLDER_TRASH));
        }
        if(data.containsKey(I_OXUser.MANAGER_NAME)){
            this.setManagersName((String)data.get(I_OXUser.MANAGER_NAME));
        }
        if(data.containsKey(I_OXUser.MARITAL_STATUS)){
            this.setMaritalStatus((String)data.get(I_OXUser.MARITAL_STATUS));
        }
        if(data.containsKey(I_OXUser.CELLULAR_TELEPHONE1)){
            this.setMobile1((String)data.get(I_OXUser.CELLULAR_TELEPHONE1));
        }
        if(data.containsKey(I_OXUser.CELLULAR_TELEPHONE2)){
            this.setMobile2((String)data.get(I_OXUser.CELLULAR_TELEPHONE2));
        }
        if(data.containsKey(I_OXUser.INFO)){
            this.setMoreInfo((String)data.get(I_OXUser.INFO));
        }
        if(data.containsKey(I_OXUser.NICKNAME)){
            this.setNickName((String)data.get(I_OXUser.NICKNAME));
        }
        if(data.containsKey(I_OXUser.NOTE)){
            this.setNote((String)data.get(I_OXUser.NOTE));
        }
        if(data.containsKey(I_OXUser.NUMBER_OF_CHILDREN)){
            this.setNumberOfChildren((String)data.get(I_OXUser.NUMBER_OF_CHILDREN));
        }
        if(data.containsKey(I_OXUser.NUMBER_OF_EMPLOYEE)){
            this.setNumberOfEmployee((String)data.get(I_OXUser.NUMBER_OF_EMPLOYEE));
        }
        if(data.containsKey(I_OXUser.TELEPHONE_PAGER)){
            this.setPager((String)data.get(I_OXUser.TELEPHONE_PAGER));
        }
        if(data.containsKey(I_OXUser.PASSWORD)){
            this.setPassword((String)data.get(I_OXUser.PASSWORD));
        }
        if(data.containsKey(I_OXUser.PASSWORD_EXPIRED)){
            this.setPasswordExpired(((Boolean)data.get(I_OXUser.PASSWORD_EXPIRED)).booleanValue());
        }
        if(data.containsKey(I_OXUser.TELEPHONE_ASSISTANT)){
            this.setPhoneAssistant((String)data.get(I_OXUser.TELEPHONE_ASSISTANT));
        }
        if(data.containsKey(I_OXUser.TELEPHONE_BUSINESS1)){
            this.setPhoneBusiness((String)data.get(I_OXUser.TELEPHONE_BUSINESS1));
        }
        if(data.containsKey(I_OXUser.TELEPHONE_BUSINESS2)){
            this.setPhoneBusiness2((String)data.get(I_OXUser.TELEPHONE_BUSINESS2));
        }
        if(data.containsKey(I_OXUser.TELEPHONE_CAR)){
            this.setPhoneCar((String)data.get(I_OXUser.TELEPHONE_CAR));
        }
        if(data.containsKey(I_OXUser.TELEPHONE_COMPANY)){
            this.setPhoneCompany((String)data.get(I_OXUser.TELEPHONE_COMPANY));
        }
        if(data.containsKey(I_OXUser.TELEPHONE_HOME1)){
            this.setPhoneHome((String)data.get(I_OXUser.TELEPHONE_HOME1));
        }
        if(data.containsKey(I_OXUser.TELEPHONE_HOME2)){
            this.setPhoneHome2((String)data.get(I_OXUser.TELEPHONE_HOME2));
        }
        if(data.containsKey(I_OXUser.TELEPHONE_OTHER)){
            this.setPhoneOther((String)data.get(I_OXUser.TELEPHONE_OTHER));
        }
        if(data.containsKey(I_OXUser.POSITION)){
            this.setPosition((String)data.get(I_OXUser.POSITION));
        }
        if(data.containsKey(I_OXUser.POSTAL_CODE_HOME)){
            this.setPostalCode((String)data.get(I_OXUser.POSTAL_CODE_HOME));
        }
        if(data.containsKey(I_OXUser.PRIMARY_MAIL)){
            this.setPrimaryEMail((String)data.get(I_OXUser.PRIMARY_MAIL));
        }
        if(data.containsKey(I_OXUser.EMAIL1)){
            this.setPrivateEmail1((String)data.get(I_OXUser.EMAIL1));
        }
        if(data.containsKey(I_OXUser.EMAIL2)){
            this.setPrivateEmail2((String)data.get(I_OXUser.EMAIL2));
        }
        if(data.containsKey(I_OXUser.EMAIL3)){
            this.setPrivateEmail3((String)data.get(I_OXUser.EMAIL3));
        }
        if(data.containsKey(I_OXUser.PROFESSION)){
            this.setProfession((String)data.get(I_OXUser.PROFESSION));
        }
        if(data.containsKey(I_OXUser.TELEPHONE_RADIO)){
            this.setRadio((String)data.get(I_OXUser.TELEPHONE_RADIO));
        }
        if(data.containsKey(I_OXUser.ROOM_NUMBER)){
            this.setRoomNumber((String)data.get(I_OXUser.ROOM_NUMBER));
        }
        if(data.containsKey(I_OXUser.SALES_VOLUME)){
            this.setSalesVolume((String)data.get(I_OXUser.SALES_VOLUME));
        }
        if(data.containsKey(I_OXUser.CITY_OTHER)){
            this.setSecondCity((String)data.get(I_OXUser.CITY_OTHER));
        }
        if(data.containsKey(I_OXUser.COUNTRY_OTHER)){
            this.setSecondCountry((String)data.get(I_OXUser.COUNTRY_OTHER));
        }
        if(data.containsKey(I_OXUser.MIDDLE_NAME)){
            this.setSecondName((String)data.get(I_OXUser.MIDDLE_NAME));
        }
        if(data.containsKey(I_OXUser.POSTAL_CODE_OTHER)){
            this.setSecondPostalCode((String)data.get(I_OXUser.POSTAL_CODE_OTHER));
        }
        if(data.containsKey(I_OXUser.STATE_OTHER)){
            this.setSecondState((String)data.get(I_OXUser.STATE_OTHER));
        }
        if(data.containsKey(I_OXUser.STREET_OTHER)){
            this.setSecondStreet((String)data.get(I_OXUser.STREET_OTHER));
        }
        if(data.containsKey(I_OXUser.SMTP_SERVER)){
            this.setSmtpServer((String)data.get(I_OXUser.SMTP_SERVER));
        }
        if(data.containsKey(I_OXUser.SPOUSE_NAME)){
            this.setSpouseName((String)data.get(I_OXUser.SPOUSE_NAME));
        }
        if(data.containsKey(I_OXUser.STATE_HOME)){
            this.setState((String)data.get(I_OXUser.STATE_HOME));
        }
        if(data.containsKey(I_OXUser.STREET_HOME)){
            this.setStreet((String)data.get(I_OXUser.STREET_HOME));
        }
        if(data.containsKey(I_OXUser.SUFFIX)){
            this.setSuffix((String)data.get(I_OXUser.SUFFIX));
        }
        
        if(data.containsKey(I_OXUser.TAX_ID)){
            this.setTaxId((String)data.get(I_OXUser.TAX_ID));
        }
        if(data.containsKey(I_OXUser.TELEPHONE_TELEX)){
            this.setTelex((String)data.get(I_OXUser.TELEPHONE_TELEX));
        }
        if(data.containsKey(I_OXUser.TIMEZONE)){
            this.setTimeZone((String)data.get(I_OXUser.TIMEZONE));
        }
        if(data.containsKey(I_OXUser.TITLE)){
            this.setTitle((String)data.get(I_OXUser.TITLE));
        }
         if(data.containsKey(I_OXUser.TELEPHONE_TTYTDD)){
            this.setTtyTdd((String)data.get(I_OXUser.TELEPHONE_TTYTDD));
        }
         if(data.containsKey(I_OXUser.URL)){
            this.setUrl((String)data.get(I_OXUser.URL));
        }
        if(data.containsKey(I_OXUser.USERFIELD01)){
            this.setUserfield01((String)data.get(I_OXUser.USERFIELD01));
        }
        if(data.containsKey(I_OXUser.USERFIELD02)){
            this.setUserfield02((String)data.get(I_OXUser.USERFIELD02));
        }
        if(data.containsKey(I_OXUser.USERFIELD03)){
            this.setUserfield03((String)data.get(I_OXUser.USERFIELD03));
        }
        if(data.containsKey(I_OXUser.USERFIELD04)){
            this.setUserfield04((String)data.get(I_OXUser.USERFIELD04));
        }
        if(data.containsKey(I_OXUser.USERFIELD05)){
            this.setUserfield05((String)data.get(I_OXUser.USERFIELD05));
        }
        if(data.containsKey(I_OXUser.USERFIELD06)){
            this.setUserfield06((String)data.get(I_OXUser.USERFIELD06));
        }
        if(data.containsKey(I_OXUser.USERFIELD07)){
            this.setUserfield07((String)data.get(I_OXUser.USERFIELD07));
        }
        if(data.containsKey(I_OXUser.USERFIELD08)){
            this.setUserfield08((String)data.get(I_OXUser.USERFIELD08));
        }
        if(data.containsKey(I_OXUser.USERFIELD09)){
            this.setUserfield09((String)data.get(I_OXUser.USERFIELD09));
        }
        if(data.containsKey(I_OXUser.USERFIELD10)){
            this.setUserfield10((String)data.get(I_OXUser.USERFIELD10));
        }
        if(data.containsKey(I_OXUser.USERFIELD11)){
            this.setUserfield11((String)data.get(I_OXUser.USERFIELD11));
        }
        if(data.containsKey(I_OXUser.USERFIELD12)){
            this.setUserfield12((String)data.get(I_OXUser.USERFIELD12));
        }
        if(data.containsKey(I_OXUser.USERFIELD13)){
            this.setUserfield13((String)data.get(I_OXUser.USERFIELD13));
        }
        if(data.containsKey(I_OXUser.USERFIELD14)){
            this.setUserfield14((String)data.get(I_OXUser.USERFIELD14));
        }
        if(data.containsKey(I_OXUser.USERFIELD15)){
            this.setUserfield15((String)data.get(I_OXUser.USERFIELD15));
        }
        if(data.containsKey(I_OXUser.USERFIELD16)){
            this.setUserfield16((String)data.get(I_OXUser.USERFIELD16));
        }
        if(data.containsKey(I_OXUser.USERFIELD17)){
            this.setUserfield17((String)data.get(I_OXUser.USERFIELD17));
        }
        if(data.containsKey(I_OXUser.USERFIELD18)){
            this.setUserfield18((String)data.get(I_OXUser.USERFIELD18));
        }
        if(data.containsKey(I_OXUser.USERFIELD19)){
            this.setUserfield19((String)data.get(I_OXUser.USERFIELD19));
        }
        if(data.containsKey(I_OXUser.USERFIELD20)){
            this.setUserfield20((String)data.get(I_OXUser.USERFIELD20));
        }
    }

    public void xForm2ObjectAccess(Hashtable accessdata) {
        if(accessdata.containsKey(I_OXUser.ACCESS_CALENDAR)){
            this.setAccessCalendar(((Boolean)accessdata.get(I_OXUser.ACCESS_CALENDAR)).booleanValue());
        }
        if(accessdata.containsKey(I_OXUser.ACCESS_CONTACTS)){
            this.setAccessContacts(((Boolean)accessdata.get(I_OXUser.ACCESS_CONTACTS)).booleanValue());
        }
        if(accessdata.containsKey(I_OXUser.ACCESS_DELEGATE_TASKS)){
            this.setAccessDelegateTasks(((Boolean)accessdata.get(I_OXUser.ACCESS_DELEGATE_TASKS)).booleanValue());
        }
        if(accessdata.containsKey(I_OXUser.ACCESS_EDIT_PUBLIC_FOLDERS)){
            this.setAccessEditPublicFolders(((Boolean)accessdata.get(I_OXUser.ACCESS_EDIT_PUBLIC_FOLDERS)).booleanValue());
        }
        if(accessdata.containsKey(I_OXUser.ACCESS_FORUM)){
            this.setAccessForum(((Boolean)accessdata.get(I_OXUser.ACCESS_FORUM)).booleanValue());
        }
        if(accessdata.containsKey(I_OXUser.ACCESS_ICAL)){
            this.setAccessIcal(((Boolean)accessdata.get(I_OXUser.ACCESS_ICAL)).booleanValue());
        }
        if(accessdata.containsKey(I_OXUser.ACCESS_INFOSSTORE)){
            this.setAccessInfostore(((Boolean)accessdata.get(I_OXUser.ACCESS_INFOSSTORE)).booleanValue());
        }
        if(accessdata.containsKey(I_OXUser.ACCESS_PINBOARD_WRITE_ACCESS)){
            this.setAccessPinboardWrite(((Boolean)accessdata.get(I_OXUser.ACCESS_PINBOARD_WRITE_ACCESS)).booleanValue());
        }
        if(accessdata.containsKey(I_OXUser.ACCESS_PROJECTS)){
            this.setAccessProjects(((Boolean)accessdata.get(I_OXUser.ACCESS_PROJECTS)).booleanValue());
        }
        if(accessdata.containsKey(I_OXUser.ACCESS_READ_CREATE_SHARED_FOLDERS)){
            this.setAccessReadCreateSharedFolders(((Boolean)accessdata.get(I_OXUser.ACCESS_READ_CREATE_SHARED_FOLDERS)).booleanValue());
        }
        if(accessdata.containsKey(I_OXUser.ACCESS_RSS_BOOKMARKS)){
            this.setAccessRssBookmarks(((Boolean)accessdata.get(I_OXUser.ACCESS_RSS_BOOKMARKS)).booleanValue());
        }
        if(accessdata.containsKey(I_OXUser.ACCESS_RSS_PORTAL)){
            this.setAccessRssPortal(((Boolean)accessdata.get(I_OXUser.ACCESS_RSS_PORTAL)).booleanValue());
        }
        if(accessdata.containsKey(I_OXUser.ACCESS_SYNCML)){
            this.setAccessSyncml(((Boolean)accessdata.get(I_OXUser.ACCESS_SYNCML)).booleanValue());
        }
        if(accessdata.containsKey(I_OXUser.ACCESS_TASKS)){
            this.setAccessTasks(((Boolean)accessdata.get(I_OXUser.ACCESS_TASKS)).booleanValue());
        }
        if(accessdata.containsKey(I_OXUser.ACCESS_VCARD)){
            this.setAccessVcard(((Boolean)accessdata.get(I_OXUser.ACCESS_VCARD)).booleanValue());
        }
        if(accessdata.containsKey(I_OXUser.ACCESS_WEBDAV)){
            this.setAccessWebdav(((Boolean)accessdata.get(I_OXUser.ACCESS_WEBDAV)).booleanValue());
        }
        if(accessdata.containsKey(I_OXUser.ACCESS_WEBDAV_XML)){
            this.setAccessWebdavXml(((Boolean)accessdata.get(I_OXUser.ACCESS_WEBDAV_XML)).booleanValue());
        }
        if(accessdata.containsKey(I_OXUser.ACCESS_WEBMAIL)){
            this.setAccessWebmail(((Boolean)accessdata.get(I_OXUser.ACCESS_WEBMAIL)).booleanValue());
        }
    }
    
}
