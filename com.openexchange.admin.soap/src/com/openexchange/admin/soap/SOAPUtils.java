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
package com.openexchange.admin.soap;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import com.openexchange.admin.soap.dataobjects.Context;
import com.openexchange.admin.soap.dataobjects.Database;
import com.openexchange.admin.soap.dataobjects.Filestore;
import com.openexchange.admin.soap.dataobjects.Group;
import com.openexchange.admin.soap.dataobjects.Resource;
import com.openexchange.admin.soap.dataobjects.SOAPStringMap;
import com.openexchange.admin.soap.dataobjects.SOAPStringMapMap;
import com.openexchange.admin.soap.dataobjects.Server;
import com.openexchange.admin.soap.dataobjects.User;
import com.openexchange.admin.soap.dataobjects.UserModuleAccess;


public final class SOAPUtils {

    public static void moduleAccess2SoapModuleAccess(com.openexchange.admin.rmi.dataobjects.UserModuleAccess in, UserModuleAccess out) {
        out.setEditGroup(in.getEditGroup());
        out.setEditResource(in.getEditResource());
        out.setEditPassword(in.getEditPassword());
        out.setCollectEmailAddresses(in.isCollectEmailAddresses());
        out.setMultipleMailAccounts(in.isMultipleMailAccounts());
        out.setSubscription(in.isSubscription());
        out.setPublication(in.isPublication());
        out.setCalendar(in.getCalendar());
        out.setContacts(in.getContacts());
        out.setDelegateTask(in.getDelegateTask());
        out.setEditPublicFolders(in.getEditPublicFolders());
        out.setForum(in.getForum());
        out.setIcal(in.getIcal());
        out.setInfostore(in.getInfostore());
        out.setPinboardWrite(in.getPinboardWrite());
        out.setReadCreateSharedFolders(in.getReadCreateSharedFolders());
        out.setRssBookmarks(in.getRssBookmarks());
        out.setRssPortal(in.getRssPortal());
        out.setSyncml(in.getSyncml());
        out.setTasks(in.getTasks());
        out.setVcard(in.getVcard());
        out.setWebdav(in.getWebdav());
        out.setWebdavXml(in.getWebdavXml());
        out.setWebmail(in.getWebmail());
        out.setActiveSync(in.isActiveSync());
        out.setUSM(in.isUSM());
        out.setOLOX20(in.isOLOX20());
        out.setDeniedPortal(in.isDeniedPortal());
        out.setGlobalAddressBookDisabled(in.isGlobalAddressBookDisabled());
        out.setPublicFolderEditable(in.isPublicFolderEditable());
    }

    public static com.openexchange.admin.rmi.dataobjects.UserModuleAccess soapModuleAccess2ModuleAccess(UserModuleAccess access) {
        com.openexchange.admin.rmi.dataobjects.UserModuleAccess ret = new com.openexchange.admin.rmi.dataobjects.UserModuleAccess();
        if( null != access.getEditGroup() ) {
            ret.setEditGroup(access.getEditGroup());
        }
        if( null != access.getEditResource() ) {
            ret.setEditResource(access.getEditResource());
        }
        if( null != access.getEditPassword() ) {
            ret.setEditPassword(access.getEditPassword());
        }
        if( null != access.getCollectEmailAddresses() ) {
            ret.setCollectEmailAddresses(access.getCollectEmailAddresses());
        }
        if( null != access.getMultipleMailAccounts() ) {
            ret.setMultipleMailAccounts(access.getMultipleMailAccounts());
        }
        if( null != access.getSubscription() ) {
            ret.setSubscription(access.getSubscription());
        }
        if( null != access.getPublication() ) {
            ret.setPublication(access.getPublication());
        }
        if( null != access.getCalendar() ) {
            ret.setCalendar(access.getCalendar());
        }
        if( null != access.getContacts() ) {
            ret.setContacts(access.getContacts());
        }
        if( null != access.getDelegateTask() ) {
            ret.setDelegateTask(access.getDelegateTask());
        }
        if( null != access.getEditPublicFolders() ) {
            ret.setEditPublicFolders(access.getEditPublicFolders());
        }
        if( null != access.getForum() ) {
            ret.setForum(access.getForum());
        }
        if( null != access.getIcal() ) {
            ret.setIcal(access.getIcal());
        }
        if( null != access.getInfostore() ) {
            ret.setInfostore(access.getInfostore());
        }
        if( null != access.getPinboardWrite() ) {
            ret.setPinboardWrite(access.getPinboardWrite());
        }
        if( null != access.getReadCreateSharedFolders() ) {
            ret.setReadCreateSharedFolders(access.getReadCreateSharedFolders());
        }
        if( null != access.getRssBookmarks() ) {
            ret.setRssBookmarks(access.getRssBookmarks());
        }
        if( null != access.getRssPortal() ) {
            ret.setRssPortal(access.getRssPortal());
        }
        if( null != access.getSyncml() ) {
            ret.setSyncml(access.getSyncml());
        }
        if( null != access.getTasks() ) {
            ret.setTasks(access.getTasks());
        }
        if( null != access.getVcard() ) {
            ret.setVcard(access.getVcard());
        }
        if( null != access.getWebdav() ) {
            ret.setWebdav(access.getWebdav());
        }
        if( null != access.getWebdavXml() ) {
            ret.setWebdavXml(access.getWebdavXml());
        }
        if( null != access.getWebmail() ) {
            ret.setWebmail(access.getWebmail());
        }
        if( null != access.getActiveSync() ) {
            ret.setActiveSync(access.getActiveSync());
        }
        if( null != access.getUSM() ) {
            ret.setUSM(access.getUSM());
        }
        if( null != access.getOLOX20() ) {
            ret.setOLOX20(access.getOLOX20());
        }
        if( null != access.getDeniedPortal() ) {
            ret.setDeniedPortal(access.getDeniedPortal());
        }
        if( null != access.getGlobalAddressBookDisabled() ) {
            ret.setGlobalAddressBookDisabled(access.getGlobalAddressBookDisabled());
        }
        if( null != access.getPublicFolderEditable() ) {
            ret.setPublicFolderEditable(access.getPublicFolderEditable());
        }
        return ret;
    }

    /**
     * @param ctx
     * @return
     */
    public static com.openexchange.admin.rmi.dataobjects.Context soapContext2Context(Context ctx) {
        com.openexchange.admin.rmi.dataobjects.Context ret = new com.openexchange.admin.rmi.dataobjects.Context();
        ret.setId(ctx.getId());
        ret.setAverage_size(ctx.getAverage_size());
        ret.setEnabled(ctx.getEnabled());
        ret.setFilestore_name(ctx.getFilestore_name());
        ret.setFilestoreId(ctx.getFilestoreId());
        String[] lmappings = ctx.getLoginMappings();
        if( null != lmappings && lmappings.length > 0 ) {
            HashSet<String> lmh = new HashSet<String>();
            for(final String l : lmappings) {
                lmh.add(l);
            }
            ret.setLoginMappings(lmh);
        }
        ret.setMaxQuota(ctx.getMaxQuota());
        ret.setName(ctx.getName());
        ret.setReadDatabase(soapDatabase2Database(ctx.getReadDatabase()));
        ret.setUsedQuota(ctx.getUsedQuota());
        ret.setWriteDatabase(soapDatabase2Database(ctx.getWriteDatabase()));
        SOAPStringMapMap userattrs = ctx.getUserAttributes();
        if( null != userattrs ) {
            ret.setUserAttributes(SOAPStringMapMap.convertToMapMap(userattrs));
        }
        return ret;
    }

    /**
     * @param ctx
     * @return
     */
    public static Context[] contexts2SoapContexts(com.openexchange.admin.rmi.dataobjects.Context ctx[]) {
        Context []ret = new Context[ctx.length];
        for(int i=0; i<ctx.length; i++) {
            ret[i] = new Context(ctx[i]);
        }
        return ret;
    }

    /**
     * @param g
     * @return
     */
    public static com.openexchange.admin.rmi.dataobjects.Group soapGroup2Group(Group g) {
        com.openexchange.admin.rmi.dataobjects.Group ret = new com.openexchange.admin.rmi.dataobjects.Group();
        ret.setId(g.getId());
        ret.setName(g.getName());
        ret.setDisplayname(g.getDisplayname());
        ret.setMembers(g.getMembers());
        return ret;
    }

    /**
     * @param grps
     * @return
     */
    public static com.openexchange.admin.rmi.dataobjects.Group[] soapGroups2Groups(Group grps[]) {
        com.openexchange.admin.rmi.dataobjects.Group []ret = new com.openexchange.admin.rmi.dataobjects.Group[grps.length];
        for(int i=0; i<grps.length; i++) {
            ret[i] = soapGroup2Group(grps[i]);
        }
        return ret;
    }

    /**
     * @param grps
     * @return
     */
    public static Group[] groups2SoapGroups(com.openexchange.admin.rmi.dataobjects.Group grps[]) {
        Group []ret = new Group[grps.length];
        for(int i=0; i<grps.length; i++) {
            ret[i] = new Group(grps[i]);
        }
        return ret;
    }

    public static void user2SoapUser(com.openexchange.admin.rmi.dataobjects.User in, User out) {
        HashSet<String> aliases = in.getAliases();
        if( null != aliases && aliases.size() > 0 ) {
            out.setAliases(aliases.toArray(new String[aliases.size()]));
        }
        if( null != in.getId() ) {
            out.setId(in.getId());
        }
        if( null != in.getName() ) {
            out.setName(in.getName());
        }
        if( null != in.getPassword() ) {
            out.setPassword(in.getPassword());
        }
        if( null != in.getPrimaryEmail() ) {
            out.setPrimaryEmail(in.getPrimaryEmail());
        }
        if( null != in.getSur_name() ) {
            out.setSur_name(in.getSur_name());
        }
        if( null != in.getGiven_name() ) {
            out.setGiven_name(in.getGiven_name());
        }
        if( null != in.getMailenabled() ) {
            out.setMailenabled(in.getMailenabled());
        }
        if( null != in.getBirthday() ) {
            out.setBirthday(in.getBirthday());
        }
        if( null != in.getAnniversary() ) {
            out.setAnniversary(in.getAnniversary());
        }
        if( null != in.getBranches() ) {
            out.setBranches(in.getBranches());
        }
        if( null != in.getBusiness_category() ) {
            out.setBusiness_category(in.getBusiness_category());
        }
        if( null != in.getPostal_code_business() ) {
            out.setPostal_code_business(in.getPostal_code_business());
        }
        if( null != in.getState_business() ) {
            out.setState_business(in.getState_business());
        }
        if( null != in.getStreet_business() ) {
            out.setStreet_business(in.getStreet_business());
        }
        if( null != in.getTelephone_callback() ) {
            out.setTelephone_callback(in.getTelephone_callback());
        }
        if( null != in.getCity_home() ) {
            out.setCity_home(in.getCity_home());
        }
        if( null != in.getCommercial_register() ) {
            out.setCommercial_register(in.getCommercial_register());
        }
        if( null != in.getCountry_home() ) {
            out.setCountry_home(in.getCountry_home());
        }
        if( null != in.getCompany() ) {
            out.setCompany(in.getCompany());
        }
        if( null != in.getDefault_group() ) {
            out.setDefault_group(new Group(in.getDefault_group()));
        }
        if( null != in.getDepartment() ) {
            out.setDepartment(in.getDepartment());
        }
        if( null != in.getDisplay_name() ) {
            out.setDisplay_name(in.getDisplay_name());
        }
        if( null != in.getEmail2() ) {
            out.setEmail2(in.getEmail2());
        }
        if( null != in.getEmail3() ) {
            out.setEmail3(in.getEmail3());
        }
        if( null != in.getEmployeeType() ) {
            out.setEmployeeType(in.getEmployeeType());
        }
        if( null != in.getFax_business() ) {
            out.setFax_business(in.getFax_business());
        }
        if( null != in.getFax_home() ) {
            out.setFax_home(in.getFax_home());
        }
        if( null != in.getFax_other() ) {
            out.setFax_other(in.getFax_other());
        }
        if( null != in.getImapServerString() ) {
            out.setImapServer(in.getImapServerString());
        }
        if( null != in.getImapLogin() ) {
            out.setImapLogin(in.getImapLogin());
        }
        if( null != in.getSmtpServerString() ) {
            out.setSmtpServer(in.getSmtpServerString());
        }
        if( null != in.getInstant_messenger1() ) {
            out.setInstant_messenger1(in.getInstant_messenger1());
        }
        if( null != in.getInstant_messenger2() ) {
            out.setInstant_messenger2(in.getInstant_messenger2());
        }
        if( null != in.getTelephone_ip() ) {
            out.setTelephone_ip(in.getTelephone_ip());
        }
        if( null != in.getTelephone_isdn() ) {
            out.setTelephone_isdn(in.getTelephone_isdn());
        }
        if( null != in.getLanguage() ) {
            out.setLanguage(in.getLanguage());
        }
        if( null != in.getMail_folder_drafts_name() ) {
            out.setMail_folder_drafts_name(in.getMail_folder_drafts_name());
        }
        if( null != in.getMail_folder_sent_name() ) {
            out.setMail_folder_sent_name(in.getMail_folder_sent_name());
        }
        if( null != in.getMail_folder_spam_name() ) {
            out.setMail_folder_spam_name(in.getMail_folder_spam_name());
        }
        if( null != in.getMail_folder_trash_name() ) {
            out.setMail_folder_trash_name(in.getMail_folder_trash_name());
        }
        if( null != in.getManager_name() ) {
            out.setManager_name(in.getManager_name());
        }
        if( null != in.getMarital_status() ) {
            out.setMarital_status(in.getMarital_status());
        }
        if( null != in.getCellular_telephone1() ) {
            out.setCellular_telephone1(in.getCellular_telephone1());
        }
        if( null != in.getCellular_telephone2() ) {
            out.setCellular_telephone2(in.getCellular_telephone2());
        }
        if( null != in.getInfo() ) {
            out.setInfo(in.getInfo());
        }
        if( null != in.getNickname() ) {
            out.setNickname(in.getNickname());
        }
        if( null != in.getNumber_of_children() ) {
            out.setNumber_of_children(in.getNumber_of_children());
        }
        if( null != in.getNote() ) {
            out.setNote(in.getNote());
        }
        if( null != in.getNumber_of_employee() ) {
            out.setNumber_of_employee(in.getNumber_of_employee());
        }
        if( null != in.getTelephone_pager() ) {
            out.setTelephone_pager(in.getTelephone_pager());
        }
        if( null != in.getPassword_expired() ) {
            out.setPassword_expired(in.getPassword_expired());
        }
        if( null != in.getTelephone_assistant() ) {
            out.setTelephone_assistant(in.getTelephone_assistant());
        }
        if( null != in.getTelephone_business1() ) {
            out.setTelephone_business1(in.getTelephone_business1());
        }
        if( null != in.getTelephone_business2() ) {
            out.setTelephone_business2(in.getTelephone_business2());
        }
        if( null != in.getTelephone_car() ) {
            out.setTelephone_car(in.getTelephone_car());
        }
        if( null != in.getTelephone_company() ) {
            out.setTelephone_company(in.getTelephone_company());
        }
        if( null != in.getTelephone_home1() ) {
            out.setTelephone_home1(in.getTelephone_home1());
        }
        if( null != in.getTelephone_home2() ) {
            out.setTelephone_home2(in.getTelephone_home2());
        }
        if( null != in.getTelephone_other() ) {
            out.setTelephone_other(in.getTelephone_other());
        }
        if( null != in.getPosition() ) {
            out.setPosition(in.getPosition());
        }
        if( null != in.getPostal_code_home() ) {
            out.setPostal_code_home(in.getPostal_code_home());
        }
        if( null != in.getProfession() ) {
            out.setProfession(in.getProfession());
        }
        if( null != in.getTelephone_radio() ) {
            out.setTelephone_radio(in.getTelephone_radio());
        }
        if( null != in.getRoom_number() ) {
            out.setRoom_number(in.getRoom_number());
        }
        if( null != in.getSales_volume() ) {
            out.setSales_volume(in.getSales_volume());
        }
        if( null != in.getCity_other() ) {
            out.setCity_other(in.getCity_other());
        }
        if( null != in.getCountry_other() ) {
            out.setCountry_other(in.getCountry_other());
        }
        if( null != in.getMiddle_name() ) {
            out.setMiddle_name(in.getMiddle_name());
        }
        if( null != in.getPostal_code_other() ) {
            out.setPostal_code_other(in.getPostal_code_other());
        }
        if( null != in.getState_other() ) {
            out.setState_other(in.getState_other());
        }
        if( null != in.getStreet_other() ) {
            out.setStreet_other(in.getStreet_other());
        }
        if( null != in.getSpouse_name() ) {
            out.setSpouse_name(in.getSpouse_name());
        }
        if( null != in.getState_home() ) {
            out.setState_home(in.getState_home());
        }
        if( null != in.getStreet_home() ) {
            out.setStreet_home(in.getStreet_home());
        }
        if( null != in.getSuffix() ) {
            out.setSuffix(in.getSuffix());
        }
        if( null != in.getTax_id() ) {
            out.setTax_id(in.getTax_id());
        }
        if( null != in.getTelephone_telex() ) {
            out.setTelephone_telex(in.getTelephone_telex());
        }
        if( null != in.getTimezone() ) {
            out.setTimezone(in.getTimezone());
        }
        if( null != in.getTitle() ) {
            out.setTitle(in.getTitle());
        }
        if( null != in.getTelephone_ttytdd() ) {
            out.setTelephone_ttytdd(in.getTelephone_ttytdd());
        }
        if( null != in.getUploadFileSizeLimit() ) {
            out.setUploadFileSizeLimit(in.getUploadFileSizeLimit());
        }
        if( null != in.getUploadFileSizeLimitPerFile() ) {
            out.setUploadFileSizeLimitPerFile(in.getUploadFileSizeLimitPerFile());
        }
        if( null != in.getUrl() ) {
            out.setUrl(in.getUrl());
        }
        if( null != in.getUserfield01() ) {
            out.setUserfield01(in.getUserfield01());
        }
        if( null != in.getUserfield02() ) {
            out.setUserfield02(in.getUserfield02());
        }
        if( null != in.getUserfield03() ) {
            out.setUserfield03(in.getUserfield03());
        }
        if( null != in.getUserfield04() ) {
            out.setUserfield04(in.getUserfield04());
        }
        if( null != in.getUserfield05() ) {
            out.setUserfield05(in.getUserfield05());
        }
        if( null != in.getUserfield06() ) {
            out.setUserfield06(in.getUserfield06());
        }
        if( null != in.getUserfield07() ) {
            out.setUserfield07(in.getUserfield07());
        }
        if( null != in.getUserfield08() ) {
            out.setUserfield08(in.getUserfield08());
        }
        if( null != in.getUserfield09() ) {
            out.setUserfield09(in.getUserfield09());
        }
        if( null != in.getUserfield10() ) {
            out.setUserfield10(in.getUserfield10());
        }
        if( null != in.getUserfield11() ) {
            out.setUserfield11(in.getUserfield11());
        }
        if( null != in.getUserfield12() ) {
            out.setUserfield12(in.getUserfield12());
        }
        if( null != in.getUserfield13() ) {
            out.setUserfield13(in.getUserfield13());
        }
        if( null != in.getUserfield14() ) {
            out.setUserfield14(in.getUserfield14());
        }
        if( null != in.getUserfield15() ) {
            out.setUserfield15(in.getUserfield15());
        }
        if( null != in.getUserfield16() ) {
            out.setUserfield16(in.getUserfield16());
        }
        if( null != in.getUserfield17() ) {
            out.setUserfield17(in.getUserfield17());
        }
        if( null != in.getUserfield18() ) {
            out.setUserfield18(in.getUserfield18());
        }
        if( null != in.getUserfield19() ) {
            out.setUserfield19(in.getUserfield19());
        }
        if( null != in.getUserfield20() ) {
            out.setUserfield20(in.getUserfield20());
        }
        if( null != in.getCity_business() ) {
            out.setCity_business(in.getCity_business());
        }
        if( null != in.getCountry_business() ) {
            out.setCountry_business(in.getCountry_business());
        }
        if( null != in.getAssistant_name() ) {
            out.setAssistant_name(in.getAssistant_name());
        }
        if( null != in.getTelephone_primary() ) {
            out.setTelephone_primary(in.getTelephone_primary());
        }
        if( null != in.getCategories() ) {
            out.setCategories(in.getCategories());
        }
        if( null != in.getEmail1() ) {
            out.setEmail1(in.getEmail1());
        }
        if( null != in.getPasswordMech() ) {
            out.setPasswordMech(in.getPasswordMech());
        }
        if( null != in.getMail_folder_confirmed_ham_name() ) {
            out.setMail_folder_confirmed_ham_name(in.getMail_folder_confirmed_ham_name());
        }
        if( null != in.getMail_folder_confirmed_spam_name() ) {
            out.setMail_folder_confirmed_spam_name(in.getMail_folder_confirmed_spam_name());
        }
        if( null != in.getGui_spam_filter_enabled() ) {
            out.setGui_spam_filter_enabled(in.getGui_spam_filter_enabled());
        }
        if( null != in.getDefaultSenderAddress() ) {
            out.setDefaultSenderAddress(in.getDefaultSenderAddress());
        }
        if( null != in.getFolderTree() ) {
            out.setFolderTree(in.getFolderTree());
        }
        if( null != in.getGuiPreferences() ) {
            out.setGuiPreferencesForSoap(SOAPStringMap.convertFromMap(in.getGuiPreferences()));
        }
        if( null != in.getUserAttributes() ) {
            out.setUserAttributes(SOAPStringMapMap.convertFromMapMap(in.getUserAttributes()));
        }
        out.setContextadmin(in.isContextadmin());
    }

    /**
     * @param u
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static com.openexchange.admin.rmi.dataobjects.User soapUser2User(User u) {
        com.openexchange.admin.rmi.dataobjects.User ret = new com.openexchange.admin.rmi.dataobjects.User();
        String[] aliases = u.getAliases();
        if( null != aliases && aliases.length > 0 ) {
            HashSet<String> ah = new HashSet<String>();
            for(final String a : aliases) {
                ah.add(a);
            }
            ret.setAliases(ah);
        }
        if( null != u.getId() ) {
            ret.setId(u.getId());
        }
        if( null != u.getName() ) {
            ret.setName(u.getName());
        }
        if( null != u.getPassword() ) {
            ret.setPassword(u.getPassword());
        }
        if( null != u.getPrimaryEmail() ) {
            ret.setPrimaryEmail(u.getPrimaryEmail());
        }
        if( null != u.getSur_name() ) {
            ret.setSur_name(u.getSur_name());
        }
        if( null != u.getGiven_name() ) {
            ret.setGiven_name(u.getGiven_name());
        }
        if( null != u.getMailenabled() ) {
            ret.setMailenabled(u.getMailenabled());
        }
        if( null != u.getBirthday() ) {
            ret.setBirthday(u.getBirthday());
        }
        if( null != u.getAnniversary() ) {
            ret.setAnniversary(u.getAnniversary());
        }
        if( null != u.getBranches() ) {
            ret.setBranches(u.getBranches());
        }
        if( null != u.getBusiness_category() ) {
            ret.setBusiness_category(u.getBusiness_category());
        }
        if( null != u.getPostal_code_business() ) {
            ret.setPostal_code_business(u.getPostal_code_business());
        }
        if( null != u.getState_business() ) {
            ret.setState_business(u.getState_business());
        }
        if( null != u.getStreet_business() ) {
            ret.setStreet_business(u.getStreet_business());
        }
        if( null != u.getTelephone_callback() ) {
            ret.setTelephone_callback(u.getTelephone_callback());
        }
        if( null != u.getCity_home() ) {
            ret.setCity_home(u.getCity_home());
        }
        if( null != u.getCommercial_register() ) {
            ret.setCommercial_register(u.getCommercial_register());
        }
        if( null != u.getCountry_home() ) {
            ret.setCountry_home(u.getCountry_home());
        }
        if( null != u.getCompany() ) {
            ret.setCompany(u.getCompany());
        }
        if( null != u.getDefault_group() ) {
            ret.setDefault_group(soapGroup2Group(u.getDefault_group()));
        }
        if( null != u.getDepartment() ) {
            ret.setDepartment(u.getDepartment());
        }
        if( null != u.getDisplay_name() ) {
            ret.setDisplay_name(u.getDisplay_name());
        }
        if( null != u.getEmail2() ) {
            ret.setEmail2(u.getEmail2());
        }
        if( null != u.getEmail3() ) {
            ret.setEmail3(u.getEmail3());
        }
        if( null != u.getEmployeeType() ) {
            ret.setEmployeeType(u.getEmployeeType());
        }
        if( null != u.getFax_business() ) {
            ret.setFax_business(u.getFax_business());
        }
        if( null != u.getFax_home() ) {
            ret.setFax_home(u.getFax_home());
        }
        if( null != u.getFax_other() ) {
            ret.setFax_other(u.getFax_other());
        }
        if( null != u.getImapServerString() ) {
            ret.setImapServer(u.getImapServerString());
        }
        if( null != u.getImapLogin() ) {
            ret.setImapLogin(u.getImapLogin());
        }
        if( null != u.getSmtpServerString() ) {
            ret.setSmtpServer(u.getSmtpServerString());
        }
        if( null != u.getInstant_messenger1() ) {
            ret.setInstant_messenger1(u.getInstant_messenger1());
        }
        if( null != u.getInstant_messenger2() ) {
            ret.setInstant_messenger2(u.getInstant_messenger2());
        }
        if( null != u.getTelephone_ip() ) {
            ret.setTelephone_ip(u.getTelephone_ip());
        }
        if( null != u.getTelephone_isdn() ) {
            ret.setTelephone_isdn(u.getTelephone_isdn());
        }
        if( null != u.getLanguage() ) {
            ret.setLanguage(u.getLanguage());
        }
        if( null != u.getMail_folder_drafts_name() ) {
            ret.setMail_folder_drafts_name(u.getMail_folder_drafts_name());
        }
        if( null != u.getMail_folder_sent_name() ) {
            ret.setMail_folder_sent_name(u.getMail_folder_sent_name());
        }
        if( null != u.getMail_folder_spam_name() ) {
            ret.setMail_folder_spam_name(u.getMail_folder_spam_name());
        }
        if( null != u.getMail_folder_trash_name() ) {
            ret.setMail_folder_trash_name(u.getMail_folder_trash_name());
        }
        if( null != u.getManager_name() ) {
            ret.setManager_name(u.getManager_name());
        }
        if( null != u.getMarital_status() ) {
            ret.setMarital_status(u.getMarital_status());
        }
        if( null != u.getCellular_telephone1() ) {
            ret.setCellular_telephone1(u.getCellular_telephone1());
        }
        if( null != u.getCellular_telephone2() ) {
            ret.setCellular_telephone2(u.getCellular_telephone2());
        }
        if( null != u.getInfo() ) {
            ret.setInfo(u.getInfo());
        }
        if( null != u.getNickname() ) {
            ret.setNickname(u.getNickname());
        }
        if( null != u.getNumber_of_children() ) {
            ret.setNumber_of_children(u.getNumber_of_children());
        }
        if( null != u.getNote() ) {
            ret.setNote(u.getNote());
        }
        if( null != u.getNumber_of_employee() ) {
            ret.setNumber_of_employee(u.getNumber_of_employee());
        }
        if( null != u.getTelephone_pager() ) {
            ret.setTelephone_pager(u.getTelephone_pager());
        }
        if( null != u.getPassword_expired() ) {
            ret.setPassword_expired(u.getPassword_expired());
        }
        if( null != u.getTelephone_assistant() ) {
            ret.setTelephone_assistant(u.getTelephone_assistant());
        }
        if( null != u.getTelephone_business1() ) {
            ret.setTelephone_business1(u.getTelephone_business1());
        }
        if( null != u.getTelephone_business2() ) {
            ret.setTelephone_business2(u.getTelephone_business2());
        }
        if( null != u.getTelephone_car() ) {
            ret.setTelephone_car(u.getTelephone_car());
        }
        if( null != u.getTelephone_company() ) {
            ret.setTelephone_company(u.getTelephone_company());
        }
        if( null != u.getTelephone_home1() ) {
            ret.setTelephone_home1(u.getTelephone_home1());
        }
        if( null != u.getTelephone_home2() ) {
            ret.setTelephone_home2(u.getTelephone_home2());
        }
        if( null != u.getTelephone_other() ) {
            ret.setTelephone_other(u.getTelephone_other());
        }
        if( null != u.getPosition() ) {
            ret.setPosition(u.getPosition());
        }
        if( null != u.getPostal_code_home() ) {
            ret.setPostal_code_home(u.getPostal_code_home());
        }
        if( null != u.getProfession() ) {
            ret.setProfession(u.getProfession());
        }
        if( null != u.getTelephone_radio() ) {
            ret.setTelephone_radio(u.getTelephone_radio());
        }
        if( null != u.getRoom_number() ) {
            ret.setRoom_number(u.getRoom_number());
        }
        if( null != u.getSales_volume() ) {
            ret.setSales_volume(u.getSales_volume());
        }
        if( null != u.getCity_other() ) {
            ret.setCity_other(u.getCity_other());
        }
        if( null != u.getCountry_other() ) {
            ret.setCountry_other(u.getCountry_other());
        }
        if( null != u.getMiddle_name() ) {
            ret.setMiddle_name(u.getMiddle_name());
        }
        if( null != u.getPostal_code_other() ) {
            ret.setPostal_code_other(u.getPostal_code_other());
        }
        if( null != u.getState_other() ) {
            ret.setState_other(u.getState_other());
        }
        if( null != u.getStreet_other() ) {
            ret.setStreet_other(u.getStreet_other());
        }
        if( null != u.getSpouse_name() ) {
            ret.setSpouse_name(u.getSpouse_name());
        }
        if( null != u.getState_home() ) {
            ret.setState_home(u.getState_home());
        }
        if( null != u.getStreet_home() ) {
            ret.setStreet_home(u.getStreet_home());
        }
        if( null != u.getSuffix() ) {
            ret.setSuffix(u.getSuffix());
        }
        if( null != u.getTax_id() ) {
            ret.setTax_id(u.getTax_id());
        }
        if( null != u.getTelephone_telex() ) {
            ret.setTelephone_telex(u.getTelephone_telex());
        }
        if( null != u.getTimezone() ) {
            ret.setTimezone(u.getTimezone());
        }
        if( null != u.getTitle() ) {
            ret.setTitle(u.getTitle());
        }
        if( null != u.getTelephone_ttytdd() ) {
            ret.setTelephone_ttytdd(u.getTelephone_ttytdd());
        }
        if( null != u.getUploadFileSizeLimit() ) {
            ret.setUploadFileSizeLimit(u.getUploadFileSizeLimit());
        }
        if( null != u.getUploadFileSizeLimitPerFile() ) {
            ret.setUploadFileSizeLimitPerFile(u.getUploadFileSizeLimitPerFile());
        }
        if( null != u.getUrl() ) {
            ret.setUrl(u.getUrl());
        }
        if( null != u.getUserfield01() ) {
            ret.setUserfield01(u.getUserfield01());
        }
        if( null != u.getUserfield02() ) {
            ret.setUserfield02(u.getUserfield02());
        }
        if( null != u.getUserfield03() ) {
            ret.setUserfield03(u.getUserfield03());
        }
        if( null != u.getUserfield04() ) {
            ret.setUserfield04(u.getUserfield04());
        }
        if( null != u.getUserfield05() ) {
            ret.setUserfield05(u.getUserfield05());
        }
        if( null != u.getUserfield06() ) {
            ret.setUserfield06(u.getUserfield06());
        }
        if( null != u.getUserfield07() ) {
            ret.setUserfield07(u.getUserfield07());
        }
        if( null != u.getUserfield08() ) {
            ret.setUserfield08(u.getUserfield08());
        }
        if( null != u.getUserfield09() ) {
            ret.setUserfield09(u.getUserfield09());
        }
        if( null != u.getUserfield10() ) {
            ret.setUserfield10(u.getUserfield10());
        }
        if( null != u.getUserfield11() ) {
            ret.setUserfield11(u.getUserfield11());
        }
        if( null != u.getUserfield12() ) {
            ret.setUserfield12(u.getUserfield12());
        }
        if( null != u.getUserfield13() ) {
            ret.setUserfield13(u.getUserfield13());
        }
        if( null != u.getUserfield14() ) {
            ret.setUserfield14(u.getUserfield14());
        }
        if( null != u.getUserfield15() ) {
            ret.setUserfield15(u.getUserfield15());
        }
        if( null != u.getUserfield16() ) {
            ret.setUserfield16(u.getUserfield16());
        }
        if( null != u.getUserfield17() ) {
            ret.setUserfield17(u.getUserfield17());
        }
        if( null != u.getUserfield18() ) {
            ret.setUserfield18(u.getUserfield18());
        }
        if( null != u.getUserfield19() ) {
            ret.setUserfield19(u.getUserfield19());
        }
        if( null != u.getUserfield20() ) {
            ret.setUserfield20(u.getUserfield20());
        }
        if( null != u.getCity_business() ) {
            ret.setCity_business(u.getCity_business());
        }
        if( null != u.getCountry_business() ) {
            ret.setCountry_business(u.getCountry_business());
        }
        if( null != u.getAssistant_name() ) {
            ret.setAssistant_name(u.getAssistant_name());
        }
        if( null != u.getTelephone_primary() ) {
            ret.setTelephone_primary(u.getTelephone_primary());
        }
        if( null != u.getCategories() ) {
            ret.setCategories(u.getCategories());
        }
        if( null != u.getEmail1() ) {
            ret.setEmail1(u.getEmail1());
        }
        if( null != u.getPasswordMech() ) {
            ret.setPasswordMech(u.getPasswordMech());
        }
        if( null != u.getMail_folder_confirmed_ham_name() ) {
            ret.setMail_folder_confirmed_ham_name(u.getMail_folder_confirmed_ham_name());
        }
        if( null != u.getMail_folder_confirmed_spam_name() ) {
            ret.setMail_folder_confirmed_spam_name(u.getMail_folder_confirmed_spam_name());
        }
        if( null != u.getGui_spam_filter_enabled() ) {
            ret.setGui_spam_filter_enabled(u.getGui_spam_filter_enabled());
        }
        if( null != u.getDefaultSenderAddress() ) {
            ret.setDefaultSenderAddress(u.getDefaultSenderAddress());
        }
        if( null != u.getFolderTree() ) {
            ret.setFolderTree(u.getFolderTree());
        }
        if( null != u.getGuiPreferencesForSoap() ) {
            ret.setGuiPreferences(SOAPStringMap.convertToMap(u.getGuiPreferencesForSoap()));
        }
        if( null != u.getUserAttributes() ) {
            ret.setUserAttributes(SOAPStringMapMap.convertToMapMap(u.getUserAttributes()));
        }
        ret.setContextadmin(u.isContextadmin());
        return ret;
    }

    /**
     * @param users
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static com.openexchange.admin.rmi.dataobjects.User[] soapUsers2Users(User users[]) {
        com.openexchange.admin.rmi.dataobjects.User rusers[] = new com.openexchange.admin.rmi.dataobjects.User[users.length];
        for(int i=0; i<users.length; i++) {
            rusers[i] = SOAPUtils.soapUser2User(users[i]);
        }
        return rusers;
    }

    /**
     * @param users
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static User[] users2SoapUsers(com.openexchange.admin.rmi.dataobjects.User users[]) {
        User rusers[] = new User[users.length];
        for(int i=0; i<users.length; i++) {
            rusers[i] = new User(users[i]);
        }
        return rusers;
    }

    /**
     * @param res
     * @return
     */
    public static com.openexchange.admin.rmi.dataobjects.Resource soapResource2Resource(Resource res) {
        com.openexchange.admin.rmi.dataobjects.Resource ret = new com.openexchange.admin.rmi.dataobjects.Resource();
        ret.setId(res.getId());
        ret.setName(res.getName());
        ret.setDisplayname(res.getDisplayname());
        ret.setDescription(res.getDescription());
        ret.setEmail(res.getEmail());
        ret.setAvailable(res.getAvailable());
        return ret;
    }

    /**
     * @param resources
     * @return
     */
    public static com.openexchange.admin.rmi.dataobjects.Resource[] soapResources2Resources(Resource resources[]) {
        com.openexchange.admin.rmi.dataobjects.Resource ret[] = new com.openexchange.admin.rmi.dataobjects.Resource[resources.length];
        for(int i=0; i<resources.length; i++) {
            ret[i] = soapResource2Resource(resources[i]);
        }
        return ret;
    }

    /**
     * @param resources
     * @return
     */
    public static Resource[] resources2SoapResources(com.openexchange.admin.rmi.dataobjects.Resource resources[]) {
        Resource ret[] = new Resource[resources.length];
        for(int i=0; i<resources.length; i++) {
            ret[i] = new Resource(resources[i]);
        }
        return ret;
    }

    public static com.openexchange.admin.rmi.dataobjects.Database soapDatabase2Database(Database db) {
        if( null == db ) {
            return null;
        }
        com.openexchange.admin.rmi.dataobjects.Database ret = new com.openexchange.admin.rmi.dataobjects.Database();
        ret.setId(db.getId());
        ret.setClusterWeight(db.getClusterWeight());
        ret.setCurrentUnits(db.getCurrentUnits());
        ret.setMaxUnits(db.getMaxUnits());
        ret.setPoolHardLimit(db.getPoolHardLimit());
        ret.setPoolInitial(db.getPoolInitial());
        ret.setPoolMax(db.getPoolMax());
        ret.setDriver(db.getDriver());
        ret.setLogin(db.getLogin());
        ret.setMaster(db.getMaster());
        ret.setMasterId(db.getMasterId());
        ret.setName(db.getName());
        ret.setPassword(db.getPassword());
        ret.setUrl(db.getUrl());
        ret.setRead_id(db.getRead_id());
        ret.setScheme(db.getScheme());
        return ret;
    }

    public static com.openexchange.admin.rmi.dataobjects.Filestore soapFilestore2Filestore(Filestore fs) {
        com.openexchange.admin.rmi.dataobjects.Filestore ret = new com.openexchange.admin.rmi.dataobjects.Filestore();
        ret.setId(fs.getId());
        ret.setCurrentContexts(fs.getCurrentContexts());
        ret.setMaxContexts(fs.getMaxContexts());
        ret.setReserved(fs.getReserved());
        ret.setSize(fs.getSize());
        ret.setUrl(fs.getUrl());
        ret.setUsed(fs.getUsed());
        return ret;
    }

    public static Database[] databases2SoapDatabases(com.openexchange.admin.rmi.dataobjects.Database []dbs) {
        Database ret[] = new Database[dbs.length];
        for(int i=0; i<dbs.length; i++) {
            ret[i] = new Database(dbs[i]);
        }
        return ret;
    }

    public static Filestore[] filestores2SoapFilestores(com.openexchange.admin.rmi.dataobjects.Filestore []fss) {
        Filestore ret[] = new Filestore[fss.length];
        for(int i=0; i<fss.length; i++) {
            ret[i] = new Filestore(fss[i]);
        }
        return ret;
    }

    public static Server[] servers2SoapServers(com.openexchange.admin.rmi.dataobjects.Server []srvs) {
        Server ret[] = new Server[srvs.length];
        for(int i=0; i<srvs.length; i++) {
            ret[i] = new Server(srvs[i]);
        }
        return ret;
    }

    public static com.openexchange.admin.rmi.dataobjects.Server soapServer2Server(Server s) {
        com.openexchange.admin.rmi.dataobjects.Server ret = new com.openexchange.admin.rmi.dataobjects.Server();
        ret.setId(s.getId());
        ret.setName(s.getName());
        return ret;
    }
}
