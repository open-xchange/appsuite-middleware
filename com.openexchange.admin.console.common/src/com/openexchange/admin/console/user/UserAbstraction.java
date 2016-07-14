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

package com.openexchange.admin.console.user;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import au.com.bytecode.opencsv.CSVReader;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.ObjectNamingAbstraction;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;

public abstract class UserAbstraction extends ObjectNamingAbstraction {

    private interface MethodDateClosure {
        public void callMethod(final Date value) throws ParseException;
    }

    protected interface MethodStringClosure {
        public void callMethod(final String value) throws ParseException, InvalidDataException;
    }

    public interface CSVConstants {

        public int getIndex();

        public String getString();

        public boolean isRequired();

        public void setRequired(final boolean required);

    }

    protected class MethodAndNames {
        private Method method = null;

        private String name = null;

        private String returntype = null;

        /**
         * @param method
         * @param name
         */
        public MethodAndNames(final Method method, final String name, final String returntype) {
            super();
            this.method = method;
            this.name = name;
            this.returntype = returntype;
        }

        public Method getMethod() {
            return this.method;
        }

        public void setMethod(final Method method) {
            this.method = method;
        }

        public String getName() {
            return this.name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public final void setReturntype(final String returntype) {
            this.returntype = returntype;
        }

        public final String getReturntype() {
            return this.returntype;
        }

    }

    protected class OptionAndMethod {
        private Method method = null;

        private CLIOption option = null;

        private String returntype = null;

        public final Method getMethod() {
            return this.method;
        }

        public final void setMethod(final Method method) {
            this.method = method;
        }

        public final CLIOption getOption() {
            return this.option;
        }

        public final void setOption(final CLIOption option) {
            this.option = option;
        }

        /**
         * @param method
         * @param option
         */
        public OptionAndMethod(final Method method, final CLIOption option, final String returntype) {
            super();
            this.method = method;
            this.option = option;
            this.returntype = returntype;
        }

        public final String getReturntype() {
            return this.returntype;
        }

        public final void setReturntype(final String returntype) {
            this.returntype = returntype;
        }

    }

    public enum AccessCombinations implements CSVConstants {
        ACCESS_COMBI_NAME(0, OPT_ACCESSRIGHTS_COMBINATION_NAME, false),
        accessCalendar(1, OPT_ACCESS_CALENDAR, false),
        accessContacts(2, OPT_ACCESS_CONTACTS, false),
        accessDelegatetasks(3, OPT_ACCESS_DELEGATE_TASKS, false),
        accessEditPublicFolder(4, OPT_ACCESS_EDIT_PUBLIC_FOLDERS, false),
        accessIcal(6, OPT_ACCESS_ICAL, false),
        accessInfostore(7, OPT_ACCESS_INFOSTORE, false),
        accessReadCreateSharedFolders(10, OPT_ACCESS_READCREATE_SHARED_FOLDERS, false),
        accessSyncML(13, OPT_ACCESS_SYNCML, false),
        accessTasks(14, OPT_ACCESS_TASKS, false),
        accessVcard(15, OPT_ACCESS_VCARD, false),
        accessWebdav(16, OPT_ACCESS_WEBDAV, false),
        accessWebdavxml(17, OPT_ACCESS_WEBDAV_XML, false),
        accessWebmail(18, OPT_ACCESS_WEBMAIL, false),
        accessEditgroup(19, OPT_ACCESS_EDIT_GROUP, false),
        accessEditresource(20, OPT_ACCESS_EDIT_RESOURCE, false),
        accessEditpassword(21, OPT_ACCESS_EDIT_PASSWORD, false),
        accessCollectemailaddresses(22, OPT_ACCESS_COLLECT_EMAIL_ADDRESSES, false),
        accessMultiplemailaccounts(23, OPT_ACCESS_MULTIPLE_MAIL_ACCOUNTS, false),
        accessSubscription(24, OPT_ACCESS_SUBSCRIPTION, false),
        accessPublication(25, OPT_ACCESS_PUBLICATION, false),
        accessActiveSync(26, OPT_ACCESS_ACTIVE_SYNC, false),
        accessUsm(27, OPT_ACCESS_USM, false),
        accessOlox20(28, OPT_ACCESS_OLOX20, false),
        accessDeniedPortal(29, OPT_ACCESS_DENIED_PORTAL, false);

        private final String string;

        private final int index;

        private boolean required;

        private AccessCombinations(final int index, final String string, final boolean required) {
            this.index = index;
            this.string = string;
            this.required = required;
        }

        @Override
        public String getString() {
            return string;
        }


        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public boolean isRequired() {
            return required;
        }

        @Override
        public void setRequired(final boolean required) {
            this.required = required;
        }
    }

    static int INITIAL_CONSTANTS_VALUE = AccessCombinations.values().length;

    public enum Constants implements CSVConstants {
        CONTEXTID(OPT_NAME_CONTEXT_LONG, true),
        adminuser(OPT_NAME_ADMINUSER_LONG, false),
        adminpass(OPT_NAME_ADMINPASS_LONG, false),
        USERNAME(OPT_USERNAME_LONG, true),
        DISPLAYNAME(OPT_DISPLAYNAME_LONG, true),
        GIVENNAME(OPT_GIVENNAME_LONG, true),
        SURNAME(OPT_SURNAME_LONG, true),
        PASSWORD(OPT_PASSWORD_LONG, true),
        EMAIL(OPT_PRIMARY_EMAIL_LONG, true),
        LANGUAGE(OPT_LANGUAGE_LONG, false),
        timezone(OPT_TIMEZONE_LONG, false),
        THEME(OPT_ADD_GUI_SETTING_LONG, false),
        department(OPT_DEPARTMENT_LONG, false),
        company(OPT_COMPANY_LONG, false),
        MAILALIAS(OPT_ALIASES_LONG, false),
        EMAIL1(OPT_EMAIL1_LONG, false),
        mailenabled(OPT_MAILENABLED_LONG, false),
        birthday(OPT_BIRTHDAY_LONG, false),
        anniversary(OPT_ANNIVERSARY_LONG, false),
        branches(OPT_BRANCHES_LONG, false),
        business_category(OPT_BUSINESS_CATEGORY_LONG, false),
        postal_code_business(OPT_POSTAL_CODE_BUSINESS_LONG, false),
        state_business(OPT_STATE_BUSINESS_LONG, false),
        street_business(OPT_STREET_BUSINESS_LONG, false),
        telephone_callback(OPT_TELEPHONE_CALLBACK_LONG, false),
        city_home(OPT_CITY_HOME_LONG, false),
        commercial_register(OPT_COMMERCIAL_REGISTER_LONG, false),
        country_home(OPT_COUNTRY_HOME_LONG, false),
        email2(OPT_EMAIL2_LONG, false),
        email3(OPT_EMAIL3_LONG, false),
        employeetype(OPT_EMPLOYEETYPE_LONG, false),
        fax_business(OPT_FAX_BUSINESS_LONG, false),
        fax_home(OPT_FAX_HOME_LONG, false),
        fax_other(OPT_FAX_OTHER_LONG, false),
        imapserver(OPT_IMAPSERVER_LONG, false),
        imaplogin(OPT_IMAPLOGIN_LONG, false),
        smtpserver(OPT_SMTPSERVER_LONG, false),
        instant_messenger1(OPT_INSTANT_MESSENGER1_LONG, false),
        instant_messenger2(OPT_INSTANT_MESSENGER2_LONG, false),
        telephone_ip(OPT_TELEPHONE_IP_LONG, false),
        telephone_isdn(OPT_TELEPHONE_ISDN_LONG, false),
        mail_folder_drafts_name(OPT_MAIL_FOLDER_DRAFTS_NAME_LONG, false),
        mail_folder_sent_name(OPT_MAIL_FOLDER_SENT_NAME_LONG, false),
        mail_folder_spam_name(OPT_MAIL_FOLDER_SPAM_NAME_LONG, false),
        mail_folder_trash_name(OPT_MAIL_FOLDER_TRASH_NAME_LONG, false),
        mail_folder_archive_full_name(OPT_MAIL_FOLDER_ARCHIVE_FULL_NAME_LONG, false),
        manager_name(OPT_MANAGER_NAME_LONG, false),
        marital_status(OPT_MARITAL_STATUS_LONG, false),
        cellular_telephone1(OPT_CELLULAR_TELEPHONE1_LONG, false),
        cellular_telephone2(OPT_CELLULAR_TELEPHONE2_LONG, false),
        info(OPT_INFO_LONG, false),
        nickname(OPT_NICKNAME_LONG, false),
        number_of_children(OPT_NUMBER_OF_CHILDREN_LONG, false),
        note(OPT_NOTE_LONG, false),
        number_of_employee(OPT_NUMBER_OF_EMPLOYEE_LONG, false),
        telephone_pager(OPT_TELEPHONE_PAGER_LONG, false),
        password_expired(OPT_PASSWORD_EXPIRED_LONG, false),
        telephone_assistant(OPT_TELEPHONE_ASSISTANT_LONG, false),
        telephone_business1(OPT_TELEPHONE_BUSINESS1_LONG, false),
        telephone_business2(OPT_TELEPHONE_BUSINESS2_LONG, false),
        telephone_car(OPT_TELEPHONE_CAR_LONG, false),
        telephone_company(OPT_TELEPHONE_COMPANY_LONG, false),
        telephone_home1(OPT_TELEPHONE_HOME1_LONG, false),
        telephone_home2(OPT_TELEPHONE_HOME2_LONG, false),
        telephone_other(OPT_TELEPHONE_OTHER_LONG, false),
        position(OPT_POSITION_LONG, false),
        postal_code_home(OPT_POSTAL_CODE_HOME_LONG, false),
        profession(OPT_PROFESSION_LONG, false),
        telephone_radio(OPT_TELEPHONE_RADIO_LONG, false),
        room_number(OPT_ROOM_NUMBER_LONG, false),
        sales_volume(OPT_SALES_VOLUME_LONG, false),
        city_other(OPT_CITY_OTHER_LONG, false),
        country_other(OPT_COUNTRY_OTHER_LONG, false),
        middle_name(OPT_MIDDLE_NAME_LONG, false),
        postal_code_other(OPT_POSTAL_CODE_OTHER_LONG, false),
        state_other(OPT_STATE_OTHER_LONG, false),
        street_other(OPT_STREET_OTHER_LONG, false),
        spouse_name(OPT_SPOUSE_NAME_LONG, false),
        state_home(OPT_STATE_HOME_LONG, false),
        street_home(OPT_STREET_HOME_LONG, false),
        suffix(OPT_SUFFIX_LONG, false),
        tax_id(OPT_TAX_ID_LONG, false),
        telephone_telex(OPT_TELEPHONE_TELEX_LONG, false),
        title(OPT_TITLE_LONG, false),
        telephone_ttytdd(OPT_TELEPHONE_TTYTDD_LONG, false),
        UPLOADFILESIZELIMIT(OPT_UPLOADFILESIZELIMIT_LONG, false),
        uploadfilesizelimitperfile(OPT_UPLOADFILESIZELIMITPERFILE_LONG, false),
        url(OPT_URL_LONG, false),
        userfield01(OPT_USERFIELD01_LONG, false),
        userfield02(OPT_USERFIELD02_LONG, false),
        userfield03(OPT_USERFIELD03_LONG, false),
        userfield04(OPT_USERFIELD04_LONG, false),
        userfield05(OPT_USERFIELD05_LONG, false),
        userfield06(OPT_USERFIELD06_LONG, false),
        userfield07(OPT_USERFIELD07_LONG, false),
        userfield08(OPT_USERFIELD08_LONG, false),
        userfield09(OPT_USERFIELD09_LONG, false),
        userfield10(OPT_USERFIELD10_LONG, false),
        userfield11(OPT_USERFIELD11_LONG, false),
        userfield12(OPT_USERFIELD12_LONG, false),
        userfield13(OPT_USERFIELD13_LONG, false),
        userfield14(OPT_USERFIELD14_LONG, false),
        userfield15(OPT_USERFIELD15_LONG, false),
        userfield16(OPT_USERFIELD16_LONG, false),
        userfield17(OPT_USERFIELD17_LONG, false),
        userfield18(OPT_USERFIELD18_LONG, false),
        userfield19(OPT_USERFIELD19_LONG, false),
        userfield20(OPT_USERFIELD20_LONG, false),
        city_business(OPT_CITY_BUSINESS_LONG, false),
        country_business(OPT_COUNTRY_BUSINESS_LONG, false),
        assistant_name(OPT_ASSISTANT_NAME_LONG, false),
        telephone_primary(OPT_TELEPHONE_PRIMARY_LONG, false),
        categories(OPT_CATEGORIES_LONG, false),
        PASSWORDMECH(OPT_PASSWORDMECH_LONG, false),
        mail_folder_confirmed_ham_name(OPT_MAIL_FOLDER_CONFIRMED_HAM_NAME_LONG, false),
        mail_folder_confirmed_spam_name(OPT_MAIL_FOLDER_CONFIRMED_SPAM_NAME_LONG, false),
        DEFAULTSENDERADDRESS(OPT_DEFAULTSENDERADDRESS_LONG, false),
        gui_spam_filter_capabilities_enabled(OPT_GUI_LONG, false),
        add_config(OPT_CONFIG_LONG, false),
        remove_config(OPT_REMOVE_CONFIG_LONG, false),
        ;

        private final String string;
        private final int index;
        private boolean required;

        private Constants(String string, boolean required) {
            this.index = INITIAL_CONSTANTS_VALUE + ordinal();
            this.string = string;
            this.required = required;
        }

        @Override
        public String getString() {
            return string;
        }


        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public boolean isRequired() {
            return required;
        }

        @Override
        public void setRequired(final boolean required) {
            this.required = required;
        }

    }

    final static protected UserModuleAccess NO_RIGHTS_ACCESS = new UserModuleAccess();

    protected static final String ACCESS_COMBINATION_NAME_AND_ACCESS_RIGHTS_DETECTED_ERROR = "You must not specify access combination name AND single access attributes simultaneously!";

    protected static final char OPT_ID_SHORT = 'i';
    protected static final String OPT_ID_LONG = "userid";
    protected static final char OPT_USERNAME_SHORT = 'u';
    protected static final String OPT_USERNAME_LONG = "username";
    protected static final char OPT_DISPLAYNAME_SHORT = 'd';
    protected static final String OPT_DISPLAYNAME_LONG = "displayname";
    protected static final char OPT_PASSWORD_SHORT = 'p';
    protected static final String OPT_PASSWORD_LONG = "password";
    protected static final char OPT_GIVENNAME_SHORT = 'g';
    protected static final String OPT_GIVENNAME_LONG = "givenname";
    protected static final char OPT_SURNAME_SHORT = 's';
    protected static final String OPT_SURNAME_LONG = "surname";
    protected static final char OPT_LANGUAGE_SHORT = 'l';
    protected static final String OPT_LANGUAGE_LONG = "language";
    protected static final char OPT_TIMEZONE_SHORT = 't';
    protected static final String OPT_TIMEZONE_LONG = "timezone";
    protected static final char OPT_PRIMARY_EMAIL_SHORT = 'e';
    protected static final String OPT_PRIMARY_EMAIL_LONG = "email";
    protected static final char OPT_DEPARTMENT_SHORT = 'x';
    protected static final String OPT_DEPARTMENT_LONG = "department";
    protected static final char OPT_COMPANY_SHORT = 'z';
    protected static final String OPT_COMPANY_LONG = "company";
    protected static final char OPT_ALIASES_SHORT = 'a';
    protected static final String OPT_ALIASES_LONG = "aliases";

    protected static final String OPT_ACCESSRIGHTS_COMBINATION_NAME = "access-combination-name";

    protected static final String OPT_CAPABILITIES_TO_ADD = "capabilities-to-add";
    protected static final String OPT_CAPABILITIES_TO_REMOVE = "capabilities-to-remove";
    protected static final String OPT_CAPABILITIES_TO_DROP = "capabilities-to-drop";

    protected static final String OPT_PERSONAL = "personal";

    protected static final String OPT_QUOTA_MODULE = "quota-module";
    protected static final String OPT_QUOTA_VALUE = "quota-value";

    protected static final String OPT_ACCESS_CALENDAR = "access-calendar";
    protected static final String OPT_ACCESS_CONTACTS = "access-contacts";
    protected static final String OPT_ACCESS_DELEGATE_TASKS = "access-delegate-tasks";
    protected static final String OPT_ACCESS_EDIT_PUBLIC_FOLDERS = "access-edit-public-folder";

    protected static final String OPT_ACCESS_ICAL = "access-ical";
    protected static final String OPT_ACCESS_INFOSTORE = "access-infostore";
    protected static final String OPT_ACCESS_READCREATE_SHARED_FOLDERS = "access-read-create-shared-Folders";
    protected static final String OPT_ACCESS_SYNCML = "access-syncml";
    protected static final String OPT_ACCESS_TASKS = "access-tasks";
    protected static final String OPT_ACCESS_VCARD = "access-vcard";
    protected static final String OPT_ACCESS_WEBDAV = "access-webdav";
    protected static final String OPT_ACCESS_WEBDAV_XML = "access-webdav-xml";
    protected static final String OPT_ACCESS_WEBMAIL = "access-webmail";
    protected static final String OPT_ACCESS_EDIT_GROUP = "access-edit-group";
    protected static final String OPT_ACCESS_EDIT_RESOURCE = "access-edit-resource";
    protected static final String OPT_ACCESS_EDIT_PASSWORD = "access-edit-password";
    protected static final String OPT_ACCESS_COLLECT_EMAIL_ADDRESSES = "access-collect-email-addresses";
    protected static final String OPT_ACCESS_MULTIPLE_MAIL_ACCOUNTS = "access-multiple-mail-accounts";
    protected static final String OPT_ACCESS_SUBSCRIPTION = "access-subscription";
    protected static final String OPT_ACCESS_PUBLICATION = "access-publication";
    protected static final String OPT_ACCESS_ACTIVE_SYNC = "access-active-sync";
    protected static final String OPT_ACCESS_USM = "access-usm";
    protected static final String OPT_ACCESS_OLOX20 = "access-olox20";
    protected static final String OPT_ACCESS_DENIED_PORTAL = "access-denied-portal";
    protected static final String OPT_DISABLE_GAB = "access-global-address-book-disabled";
    protected static final String OPT_ACCESS_PUBLIC_FOLDER_EDITABLE = "access-public-folder-editable";
    protected static final String OPT_GUI_LONG = "gui_spam_filter_capabilities_enabled";
    protected static final String OPT_CSV_IMPORT = "csv-import";

    // extended options
    protected static final String OPT_EMAIL1_LONG = "email1";
    protected static final String OPT_MAILENABLED_LONG = "mailenabled";
    protected static final String OPT_BIRTHDAY_LONG = "birthday";
    protected static final String OPT_ANNIVERSARY_LONG = "anniversary";
    protected static final String OPT_BRANCHES_LONG = "branches";
    protected static final String OPT_BUSINESS_CATEGORY_LONG = "business_category";
    protected static final String OPT_POSTAL_CODE_BUSINESS_LONG = "postal_code_business";
    protected static final String OPT_STATE_BUSINESS_LONG = "state_business";
    protected static final String OPT_STREET_BUSINESS_LONG = "street_business";
    protected static final String OPT_TELEPHONE_CALLBACK_LONG = "telephone_callback";
    protected static final String OPT_CITY_HOME_LONG = "city_home";
    protected static final String OPT_COMMERCIAL_REGISTER_LONG = "commercial_register";
    protected static final String OPT_COUNTRY_HOME_LONG = "country_home";
    protected static final String OPT_EMAIL2_LONG = "email2";
    protected static final String OPT_EMAIL3_LONG = "email3";
    protected static final String OPT_EMPLOYEETYPE_LONG = "employeetype";
    protected static final String OPT_FAX_BUSINESS_LONG = "fax_business";
    protected static final String OPT_FAX_HOME_LONG = "fax_home";
    protected static final String OPT_FAX_OTHER_LONG = "fax_other";
    protected static final String OPT_IMAPSERVER_LONG = "imapserver";
    protected static final String OPT_IMAPLOGIN_LONG = "imaplogin";
    protected static final String OPT_SMTPSERVER_LONG = "smtpserver";
    protected static final String OPT_INSTANT_MESSENGER1_LONG = "instant_messenger1";
    protected static final String OPT_INSTANT_MESSENGER2_LONG = "instant_messenger2";
    protected static final String OPT_TELEPHONE_IP_LONG = "telephone_ip";
    protected static final String OPT_TELEPHONE_ISDN_LONG = "telephone_isdn";
    protected static final String OPT_MAIL_FOLDER_DRAFTS_NAME_LONG = "mail_folder_drafts_name";
    protected static final String OPT_MAIL_FOLDER_SENT_NAME_LONG = "mail_folder_sent_name";
    protected static final String OPT_MAIL_FOLDER_SPAM_NAME_LONG = "mail_folder_spam_name";
    protected static final String OPT_MAIL_FOLDER_TRASH_NAME_LONG = "mail_folder_trash_name";
    protected static final String OPT_MAIL_FOLDER_ARCHIVE_FULL_NAME_LONG = "mail_folder_archive_full_name";
    protected static final String OPT_MANAGER_NAME_LONG = "manager_name";
    protected static final String OPT_MARITAL_STATUS_LONG = "marital_status";
    protected static final String OPT_CELLULAR_TELEPHONE1_LONG = "cellular_telephone1";
    protected static final String OPT_CELLULAR_TELEPHONE2_LONG = "cellular_telephone2";
    protected static final String OPT_INFO_LONG = "info";
    protected static final String OPT_NICKNAME_LONG = "nickname";
    protected static final String OPT_NUMBER_OF_CHILDREN_LONG = "number_of_children";
    protected static final String OPT_NOTE_LONG = "note";
    protected static final String OPT_NUMBER_OF_EMPLOYEE_LONG = "number_of_employee";
    protected static final String OPT_TELEPHONE_PAGER_LONG = "telephone_pager";
    protected static final String OPT_PASSWORD_EXPIRED_LONG = "password_expired";
    protected static final String OPT_TELEPHONE_ASSISTANT_LONG = "telephone_assistant";
    protected static final String OPT_TELEPHONE_BUSINESS1_LONG = "telephone_business1";
    protected static final String OPT_TELEPHONE_BUSINESS2_LONG = "telephone_business2";
    protected static final String OPT_TELEPHONE_CAR_LONG = "telephone_car";
    protected static final String OPT_TELEPHONE_COMPANY_LONG = "telephone_company";
    protected static final String OPT_TELEPHONE_HOME1_LONG = "telephone_home1";
    protected static final String OPT_TELEPHONE_HOME2_LONG = "telephone_home2";
    protected static final String OPT_TELEPHONE_OTHER_LONG = "telephone_other";
    protected static final String OPT_POSTAL_CODE_HOME_LONG = "postal_code_home";
    protected static final String OPT_PROFESSION_LONG = "profession";
    protected static final String OPT_TELEPHONE_RADIO_LONG = "telephone_radio";
    protected static final String OPT_ROOM_NUMBER_LONG = "room_number";
    protected static final String OPT_SALES_VOLUME_LONG = "sales_volume";
    protected static final String OPT_CITY_OTHER_LONG = "city_other";
    protected static final String OPT_COUNTRY_OTHER_LONG = "country_other";
    protected static final String OPT_MIDDLE_NAME_LONG = "middle_name";
    protected static final String OPT_POSTAL_CODE_OTHER_LONG = "postal_code_other";
    protected static final String OPT_STATE_OTHER_LONG = "state_other";
    protected static final String OPT_STREET_OTHER_LONG = "street_other";
    protected static final String OPT_SPOUSE_NAME_LONG = "spouse_name";
    protected static final String OPT_STATE_HOME_LONG = "state_home";
    protected static final String OPT_STREET_HOME_LONG = "street_home";
    protected static final String OPT_SUFFIX_LONG = "suffix";
    protected static final String OPT_TAX_ID_LONG = "tax_id";
    protected static final String OPT_TELEPHONE_TELEX_LONG = "telephone_telex";
    protected static final String OPT_TELEPHONE_TTYTDD_LONG = "telephone_ttytdd";
    protected static final String OPT_UPLOADFILESIZELIMIT_LONG = "uploadfilesizelimit";
    protected static final String OPT_UPLOADFILESIZELIMITPERFILE_LONG = "uploadfilesizelimitperfile";
    protected static final String OPT_URL_LONG = "url";
    protected static final String OPT_USERFIELD01_LONG = "userfield01";
    protected static final String OPT_USERFIELD02_LONG = "userfield02";
    protected static final String OPT_USERFIELD03_LONG = "userfield03";
    protected static final String OPT_USERFIELD04_LONG = "userfield04";
    protected static final String OPT_USERFIELD05_LONG = "userfield05";
    protected static final String OPT_USERFIELD06_LONG = "userfield06";
    protected static final String OPT_USERFIELD07_LONG = "userfield07";
    protected static final String OPT_USERFIELD08_LONG = "userfield08";
    protected static final String OPT_USERFIELD09_LONG = "userfield09";
    protected static final String OPT_USERFIELD10_LONG = "userfield10";
    protected static final String OPT_USERFIELD11_LONG = "userfield11";
    protected static final String OPT_USERFIELD12_LONG = "userfield12";
    protected static final String OPT_USERFIELD13_LONG = "userfield13";
    protected static final String OPT_USERFIELD14_LONG = "userfield14";
    protected static final String OPT_USERFIELD15_LONG = "userfield15";
    protected static final String OPT_USERFIELD16_LONG = "userfield16";
    protected static final String OPT_USERFIELD17_LONG = "userfield17";
    protected static final String OPT_USERFIELD18_LONG = "userfield18";
    protected static final String OPT_USERFIELD19_LONG = "userfield19";
    protected static final String OPT_USERFIELD20_LONG = "userfield20";
    protected static final String OPT_CITY_BUSINESS_LONG = "city_business";
    protected static final String OPT_ASSISTANT_NAME_LONG = "assistant_name";
    protected static final String OPT_TELEPHONE_PRIMARY_LONG = "telephone_primary";
    protected static final String OPT_CATEGORIES_LONG = "categories";
    protected static final String OPT_PASSWORDMECH_LONG = "passwordmech";
    protected static final String OPT_MAIL_FOLDER_CONFIRMED_HAM_NAME_LONG = "mail_folder_confirmed_ham_name";
    protected static final String OPT_MAIL_FOLDER_CONFIRMED_SPAM_NAME_LONG = "mail_folder_confirmed_spam_name";
    protected static final String OPT_DEFAULTSENDERADDRESS_LONG = "defaultsenderaddress";
    protected static final String OPT_COUNTRY_BUSINESS_LONG = "country_business";
    protected static final String OPT_FOLDERTREE_LONG = "foldertree";
    protected static final String OPT_TITLE_LONG = "title";
    protected static final String OPT_POSITION_LONG = "position";



    protected static final String JAVA_UTIL_TIME_ZONE = "java.util.TimeZone";
    protected static final String PASSWORDMECH_CLASS = "com.openexchange.admin.rmi.dataobjects.User$PASSWORDMECH";
    protected static final String JAVA_UTIL_HASH_SET = "java.util.HashSet";
    protected static final String JAVA_UTIL_MAP = "java.util.Map";
    protected static final String JAVA_UTIL_DATE = "java.util.Date";
    protected static final String JAVA_LANG_BOOLEAN = "java.lang.Boolean";
    protected static final String JAVA_LANG_INTEGER = "java.lang.Integer";
    protected static final String JAVA_UTIL_ARRAY_LIST = "java.util.ArrayList";
    protected static final String JAVA_UTIL_LOCALE = "java.util.Locale";
    protected static final String JAVA_LANG_LONG = "java.lang.Long";
    protected static final String JAVA_LANG_STRING = "java.lang.String";
    protected static final String SIMPLE_INT = "int";
    protected static final String OPT_IMAPONLY_LONG = "imaponly";
    protected static final String OPT_DBONLY_LONG = "dbonly";

    public static final ArrayList<OptionAndMethod> optionsandmethods = new ArrayList<OptionAndMethod>();

    static {
        NO_RIGHTS_ACCESS.disableAll();
    }

    protected CLIOption userNameOption = null;
    protected CLIOption displayNameOption = null;
    protected CLIOption givenNameOption = null;
    protected CLIOption surNameOption = null;
    protected CLIOption passwordOption = null;
    protected CLIOption primaryMailOption = null;
    protected CLIOption languageOption = null;
    protected CLIOption timezoneOption = null;
    protected CLIOption departmentOption = null;
    protected CLIOption companyOption = null;
    protected CLIOption aliasesOption = null;
    protected CLIOption idOption = null;
    protected CLIOption imapOnlyOption = null;
    protected CLIOption dbOnlyOption = null;
    protected CLIOption extendedOption = null;
    protected CLIOption imapQuotaOption = null;
    protected CLIOption inetMailAccessOption = null;
    protected CLIOption spamFilterOption = null;

    protected CLIOption accessRightsCombinationName = null;

    protected CLIOption capsToAdd = null;
    protected CLIOption capsToRemove = null;
    protected CLIOption capsToDrop = null;

    protected CLIOption personal = null;

    protected CLIOption quotaModule = null;
    protected CLIOption quotaValue = null;

    // access to modules
    protected CLIOption accessCalendarOption = null;
    protected CLIOption accessContactOption = null;
    protected CLIOption accessDelegateTasksOption = null;
    protected CLIOption accessEditPublicFolderOption = null;
    protected CLIOption accessIcalOption = null;
    protected CLIOption accessInfostoreOption = null;
    protected CLIOption accessReadCreateSharedFolderOption = null;
    protected CLIOption accessSyncmlOption = null;
    protected CLIOption accessTasksOption = null;
    protected CLIOption accessVcardOption = null;
    protected CLIOption accessWebdavOption = null;
    protected CLIOption accessWebdavXmlOption = null;
    protected CLIOption accessWebmailOption = null;
    protected CLIOption accessEditGroupOption = null;
    protected CLIOption accessEditResourceOption = null;
    protected CLIOption accessEditPasswordOption = null;
    protected CLIOption accessCollectEmailAddresses = null;
    protected CLIOption accessMultipleMailAccounts = null;
    protected CLIOption accessPublication = null;
    protected CLIOption accessSubscription = null;
    protected CLIOption accessActiveSync = null;
    protected CLIOption accessUSM = null;
    protected CLIOption accessOLOX20 = null;
    protected CLIOption accessDeniedPortal = null;
    protected CLIOption accessGAB = null;
    protected CLIOption accessPublicFolderEditable = null;


    // non-generic extended option
    protected CLIOption addGUISettingOption = null;
    protected CLIOption removeGUISettingOption = null;
    protected static final String OPT_ADD_GUI_SETTING_LONG = "addguipreferences";
    protected static final String OPT_REMOVE_GUI_SETTING_LONG = "removeguipreferences";

    // The CLIOption instances for "config" and "remove-config" options are only for the purpose to mention these options for a "--help" invocation
    // Their values are set through AdminParser's dynamic options!
    protected CLIOption configOption_NO_READ = null;
    protected CLIOption removeConfigOption_NO_READ = null;
    protected static final String OPT_CONFIG_LONG = "config";
    protected static final String OPT_REMOVE_CONFIG_LONG = "remove-config";

    // For right error output
    protected String username = null;
    protected String displayName = null;
    protected Integer userid = null;
    private CLIOption email1Option;
    private CLIOption mailenabledOption;
    private CLIOption birthdayOption;
    private CLIOption anniversaryOption;
    private CLIOption branchesOption;
    private CLIOption business_categoryOption;
    private CLIOption postal_code_businessOption;
    private CLIOption state_businessOption;
    private CLIOption street_businessOption;
    private CLIOption telephone_callbackOption;
    private CLIOption city_homeOption;
    private CLIOption commercial_registerOption;
    private CLIOption country_homeOption;
    private CLIOption email2Option;
    private CLIOption email3Option;
    private CLIOption employeetypeOption;
    private CLIOption fax_businessOption;
    private CLIOption fax_homeOption;
    private CLIOption fax_otherOption;
    private CLIOption imapserverOption;
    private CLIOption imaploginOption;
    private CLIOption smtpserverOption;
    private CLIOption instant_messenger1Option;
    private CLIOption instant_messenger2Option;
    private CLIOption telephone_ipOption;
    private CLIOption telephone_isdnOption;
    private CLIOption mail_folder_drafts_nameOption;
    private CLIOption mail_folder_sent_nameOption;
    private CLIOption mail_folder_spam_nameOption;
    private CLIOption mail_folder_trash_nameOption;
    private CLIOption mail_folder_archive_full_nameOption;
    private CLIOption manager_nameOption;
    private CLIOption marital_statusOption;
    private CLIOption cellular_telephone1Option;
    private CLIOption cellular_telephone2Option;
    private CLIOption infoOption;
    private CLIOption nicknameOption;
    private CLIOption number_of_childrenOption;
    private CLIOption noteOption;
    private CLIOption number_of_employeeOption;
    private CLIOption telephone_pagerOption;
    private CLIOption password_expiredOption;
    private CLIOption telephone_assistantOption;
    private CLIOption telephone_business1Option;
    private CLIOption telephone_business2Option;
    private CLIOption telephone_carOption;
    private CLIOption telephone_companyOption;
    private CLIOption telephone_home1Option;
    private CLIOption telephone_home2Option;
    private CLIOption telephone_otherOption;
    private CLIOption postal_code_homeOption;
    private CLIOption professionOption;
    private CLIOption telephone_radioOption;
    private CLIOption room_numberOption;
    private CLIOption sales_volumeOption;
    private CLIOption city_otherOption;
    private CLIOption country_otherOption;
    private CLIOption middle_nameOption;
    private CLIOption postal_code_otherOption;
    private CLIOption state_otherOption;
    private CLIOption street_otherOption;
    private CLIOption spouse_nameOption;
    private CLIOption state_homeOption;
    private CLIOption street_homeOption;
    private CLIOption suffixOption;
    private CLIOption tax_idOption;
    private CLIOption telephone_telexOption;
    private CLIOption telephone_ttytddOption;
    private CLIOption uploadfilesizelimitOption;
    private CLIOption uploadfilesizelimitperfileOption;
    private CLIOption urlOption;
    private CLIOption userfield01Option;
    private CLIOption userfield02Option;
    private CLIOption userfield03Option;
    private CLIOption userfield06Option;
    private CLIOption userfield04Option;
    private CLIOption userfield05Option;
    private CLIOption userfield07Option;
    private CLIOption userfield08Option;
    private CLIOption userfield09Option;
    private CLIOption userfield10Option;
    private CLIOption userfield11Option;
    private CLIOption userfield12Option;
    private CLIOption userfield13Option;
    private CLIOption userfield14Option;
    private CLIOption userfield15Option;
    private CLIOption userfield16Option;
    private CLIOption userfield17Option;
    private CLIOption userfield18Option;
    private CLIOption userfield19Option;
    private CLIOption userfield20Option;
    private CLIOption city_businessOption;
    private CLIOption assistant_nameOption;
    private CLIOption telephone_primaryOption;
    private CLIOption categoriesOption;
    private CLIOption passwordmechOption;
    private CLIOption mail_folder_confirmed_ham_nameOption;
    private CLIOption mail_folder_confirmed_spam_nameOption;
    private CLIOption defaultsenderaddressOption;
    private CLIOption country_businessOption;
    private CLIOption foldertreeOption;
    private CLIOption titleOption;
    private CLIOption positionOption;

    protected HashMap<String, CSVConstants> constantsMap;

    protected static UserModuleAccess getUserModuleAccess(final String[] nextLine, final int[] idarray) {
        final UserModuleAccess moduleaccess = new UserModuleAccess();
        moduleaccess.disableAll();
        final int i = idarray[AccessCombinations.accessActiveSync.getIndex()];
        if (-1 != i) {
            if (nextLine[i].length() > 0) {
                moduleaccess.setActiveSync(stringToBool(nextLine[i]));
            }
        }
        final int j = idarray[AccessCombinations.accessCalendar.getIndex()];
        if (-1 != j) {
            if (nextLine[j].length() > 0) {
                moduleaccess.setCalendar(stringToBool(nextLine[j]));
            }
        }
        final int j2 = idarray[AccessCombinations.accessCollectemailaddresses.getIndex()];
        if (-1 != j2) {
            if (nextLine[j2].length() > 0) {
                moduleaccess.setCollectEmailAddresses(stringToBool(nextLine[j2]));
            }
        }
        final int k = idarray[AccessCombinations.accessContacts.getIndex()];
        if (-1 != k) {
            if (nextLine[k].length() > 0) {
                moduleaccess.setContacts(stringToBool(nextLine[k]));
            }
        }
        final int k2 = idarray[AccessCombinations.accessDelegatetasks.getIndex()];
        if (-1 != k2) {
            if (nextLine[k2].length() > 0) {
                moduleaccess.setDelegateTask(stringToBool(nextLine[k2]));
            }
        }
        final int l = idarray[AccessCombinations.accessEditgroup.getIndex()];
        if (-1 != l) {
            if (nextLine[l].length() > 0) {
                moduleaccess.setEditGroup(stringToBool(nextLine[l]));
            }
        }
        final int l2 = idarray[AccessCombinations.accessEditpassword.getIndex()];
        if (-1 != l2) {
            if (nextLine[l2].length() > 0) {
                moduleaccess.setEditPassword(stringToBool(nextLine[l2]));
            }
        }
        final int m = idarray[AccessCombinations.accessEditPublicFolder.getIndex()];
        if (-1 != m) {
            if (nextLine[m].length() > 0) {
                moduleaccess.setEditPublicFolders(stringToBool(nextLine[m]));
            }
        }
        final int m2 = idarray[AccessCombinations.accessEditresource.getIndex()];
        if (-1 != m2) {
            if (nextLine[m2].length() > 0) {
                moduleaccess.setEditResource(stringToBool(nextLine[m2]));
            }
        }
        final int n2 = idarray[AccessCombinations.accessIcal.getIndex()];
        if (-1 != n2) {
            if (nextLine[n2].length() > 0) {
                moduleaccess.setIcal(stringToBool(nextLine[n2]));
            }
        }
        final int o = idarray[AccessCombinations.accessInfostore.getIndex()];
        if (-1 != o) {
            if (nextLine[o].length() > 0) {
                moduleaccess.setInfostore(stringToBool(nextLine[o]));
            }
        }
        final int o2 = idarray[AccessCombinations.accessMultiplemailaccounts.getIndex()];
        if (-1 != o2) {
            if (nextLine[o2].length() > 0) {
                moduleaccess.setMultipleMailAccounts(stringToBool(nextLine[o2]));
            }
        }
        final int q = idarray[AccessCombinations.accessPublication.getIndex()];
        if (-1 != q) {
            if (nextLine[q].length() > 0) {
                moduleaccess.setPublication(stringToBool(nextLine[q]));
            }
        }
        final int q2 = idarray[AccessCombinations.accessReadCreateSharedFolders.getIndex()];
        if (-1 != q2) {
            if (nextLine[q2].length() > 0) {
                moduleaccess.setReadCreateSharedFolders(stringToBool(nextLine[q2]));
            }
        }
        final int s = idarray[AccessCombinations.accessSubscription.getIndex()];
        if (-1 != s) {
            if (nextLine[s].length() > 0) {
                moduleaccess.setSubscription(stringToBool(nextLine[s]));
            }
        }
        final int s2 = idarray[AccessCombinations.accessSyncML.getIndex()];
        if (-1 != s2) {
            if (nextLine[s2].length() > 0) {
                moduleaccess.setSyncml(stringToBool(nextLine[s2]));
            }
        }
        final int t = idarray[AccessCombinations.accessTasks.getIndex()];
        if (-1 != t) {
            if (nextLine[t].length() > 0) {
                moduleaccess.setTasks(stringToBool(nextLine[t]));
            }
        }
        final int t2 = idarray[AccessCombinations.accessUsm.getIndex()];
        if (-1 != t2) {
            if (nextLine[t2].length() > 0) {
                moduleaccess.setUSM(stringToBool(nextLine[t2]));
            }
        }
        final int u = idarray[AccessCombinations.accessUsm.getIndex()];
        if (-1 != u) {
            if (nextLine[u].length() > 0) {
                moduleaccess.setUSM(stringToBool(nextLine[u]));
            }
        }
        final int olox20 = idarray[AccessCombinations.accessOlox20.getIndex()];
        if (-1 != olox20) {
            if (nextLine[olox20].length() > 0) {
                moduleaccess.setOLOX20(stringToBool(nextLine[olox20]));
            }
        }
        final int portal = idarray[AccessCombinations.accessDeniedPortal.getIndex()];
        if (-1 != portal) {
            if (nextLine[portal].length() > 0) {
                moduleaccess.setDeniedPortal(stringToBool(nextLine[portal]));
            }
        }
        final int u2 = idarray[AccessCombinations.accessVcard.getIndex()];
        if (-1 != u2) {
            if (nextLine[u2].length() > 0) {
                moduleaccess.setVcard(stringToBool(nextLine[u2]));
            }
        }
        final int v = idarray[AccessCombinations.accessWebdav.getIndex()];
        if (-1 != v) {
            if (nextLine[v].length() > 0) {
                moduleaccess.setWebdav(stringToBool(nextLine[v]));
            }
        }
        final int v2 = idarray[AccessCombinations.accessWebdavxml.getIndex()];
        if (-1 != v2) {
            if (nextLine[v2].length() > 0) {
                moduleaccess.setWebdavXml(stringToBool(nextLine[v2]));
            }
        }
        final int w = idarray[AccessCombinations.accessWebmail.getIndex()];
        if (-1 != w) {
            if (nextLine[w].length() > 0) {
                moduleaccess.setWebmail(stringToBool(nextLine[w]));
            }
        }
        return moduleaccess;
    }

    protected static boolean stringToBool(final String string) {
        return parseBool(string);
    }

    private static final Set<String> BOOL_VALS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("true","1","yes","y","on")));

    private static boolean parseBool(final String parameter) {
        return (null != parameter) && BOOL_VALS.contains(com.openexchange.java.Strings.toLowerCase(parameter.trim()));
    }

    protected static Credentials getCreds(final String[] nextLine, final int[] idarray) {
        final Credentials credentials = new Credentials();
        final int i = idarray[Constants.adminuser.getIndex()];
        if (-1 != i) {
            if (nextLine[i].length() > 0) {
                credentials.setLogin(nextLine[i]);
            }
        }
        final int j = idarray[Constants.adminpass.getIndex()];
        if (-1 != j) {
            if (nextLine[j].length() > 0) {
                credentials.setPassword(nextLine[j]);
            }
        }
        return credentials;
    }

    protected Context getContext(final String[] nextLine, final int[] idarray) throws InvalidDataException, ParseException {
        final Context context = new Context();
        setValue(nextLine, idarray, Constants.CONTEXTID, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) throws InvalidDataException {
                try {
                    context.setId(Integer.valueOf(value));
                } catch (final NumberFormatException e) {
                    throw new InvalidDataException("Value in field " + Constants.CONTEXTID.getString() + " is no integer");
                }
            }
        });

        return context;
    }

    protected User getUser(final String[] nextLine, final int[] idarray) throws InvalidDataException, ParseException {
        final User user = new User();
        setValue(nextLine, idarray, Constants.USERNAME, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setName(value);
            }
        });
        setValue(nextLine, idarray, Constants.PASSWORD, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setPassword(value);
            }
        });
        setValue(nextLine, idarray, Constants.EMAIL, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setPrimaryEmail(value);
                user.setEmail1(value);
            }
        });
        setValue(nextLine, idarray, Constants.DISPLAYNAME, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setDisplay_name(value);
            }
        });
        setValue(nextLine, idarray, Constants.SURNAME, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setSur_name(value);
            }
        });
        setValue(nextLine, idarray, Constants.GIVENNAME, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setGiven_name(value);
            }
        });
        setValue(nextLine, idarray, Constants.LANGUAGE, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setLanguage(value);
            }
        });
        setValue(nextLine, idarray, Constants.timezone, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setTimezone(value);
            }
        });
        final int m3 = idarray[Constants.THEME.getIndex()];
        if (m3 >= 0) {
            String theme = nextLine[m3];
            if (theme.length() > 0) {
                final String addguival = theme.trim();
                if (addguival.length() == 0) {
                    throw new InvalidDataException("Argument for " + OPT_ADD_GUI_SETTING_LONG + "is wrong (empty value)");
                }
                int idx = addguival.indexOf('=');
                if (idx <= 0) {
                    throw new InvalidDataException("Argument for " + OPT_ADD_GUI_SETTING_LONG + "is wrong (not key = value)");
                }
                String key = addguival.substring(0, idx).trim();
                String val = addguival.substring(idx + 1, addguival.length()).trim();
                if (key.length() == 0 || val.length() == 0) {
                    throw new InvalidDataException("Argument for " + OPT_ADD_GUI_SETTING_LONG + "is wrong (key or val empty)");
                }
                user.addGuiPreferences(key, val);
            } else {
                if (Constants.THEME.isRequired()) {
                    throw new InvalidDataException("Field " + Constants.THEME.getString() + " required but not set.");
                }
            }
        }
        setValue(nextLine, idarray, Constants.department, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setDepartment(value);
            }
        });
        setValue(nextLine, idarray, Constants.company, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setCompany(value);
            }
        });
        setValue(nextLine, idarray, Constants.EMAIL1, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setEmail1(value);
            }
        });
        setValue(nextLine, idarray, Constants.mailenabled, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setMailenabled(Boolean.valueOf(stringToBool(value)));
            }
        });
        setValue(nextLine, idarray, Constants.birthday, new MethodDateClosure() {
            @Override
            public void callMethod(final Date value) {
                user.setBirthday(value);
            }
        });
        setValue(nextLine, idarray, Constants.anniversary, new MethodDateClosure() {
            @Override
            public void callMethod(final Date value) {
                user.setAnniversary(value);
            }
        });
        setValue(nextLine, idarray, Constants.branches, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setBranches(value);
            }
        });
        setValue(nextLine, idarray, Constants.business_category, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setBusiness_category(value);
            }
        });
        setValue(nextLine, idarray, Constants.postal_code_business, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setPostal_code_business(value);
            }
        });
        setValue(nextLine, idarray, Constants.state_business, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setState_business(value);
            }
        });
        setValue(nextLine, idarray, Constants.street_business, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setStreet_business(value);
            }
        });
        setValue(nextLine, idarray, Constants.telephone_callback, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setTelephone_callback(value);
            }
        });
        setValue(nextLine, idarray, Constants.city_home, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setCity_home(value);
            }
        });
        setValue(nextLine, idarray, Constants.commercial_register, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setCommercial_register(value);
            }
        });
        setValue(nextLine, idarray, Constants.country_home, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setCountry_home(value);
            }
        });
        setValue(nextLine, idarray, Constants.email2, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setEmail2(value);
            }
        });
        setValue(nextLine, idarray, Constants.email3, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setEmail3(value);
            }
        });
        setValue(nextLine, idarray, Constants.employeetype, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setEmployeeType(value);
            }
        });
        setValue(nextLine, idarray, Constants.fax_business, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setFax_business(value);
            }
        });
        setValue(nextLine, idarray, Constants.fax_home, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setFax_home(value);
            }
        });
        setValue(nextLine, idarray, Constants.fax_other, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setFax_other(value);
            }
        });
        setValue(nextLine, idarray, Constants.imapserver, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setImapServer(value);
            }
        });
        setValue(nextLine, idarray, Constants.imaplogin, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setImapLogin(value);
            }
        });
        setValue(nextLine, idarray, Constants.smtpserver, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setSmtpServer(value);
            }
        });
        setValue(nextLine, idarray, Constants.instant_messenger1, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setInstant_messenger1(value);
            }
        });
        setValue(nextLine, idarray, Constants.instant_messenger2, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setInstant_messenger2(value);
            }
        });
        setValue(nextLine, idarray, Constants.telephone_ip, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setTelephone_ip(value);
            }
        });
        setValue(nextLine, idarray, Constants.telephone_isdn, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setTelephone_isdn(value);
            }
        });
        setValue(nextLine, idarray, Constants.mail_folder_drafts_name, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setMail_folder_drafts_name(value);
            }
        });
        setValue(nextLine, idarray, Constants.mail_folder_sent_name, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setMail_folder_sent_name(value);
            }
        });
        setValue(nextLine, idarray, Constants.mail_folder_spam_name, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setMail_folder_spam_name(value);
            }
        });
        setValue(nextLine, idarray, Constants.mail_folder_trash_name, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setMail_folder_trash_name(value);
            }
        });
        setValue(nextLine, idarray, Constants.mail_folder_archive_full_name, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setMail_folder_archive_full_name(value);
            }
        });
        setValue(nextLine, idarray, Constants.manager_name, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setManager_name(value);
            }
        });
        setValue(nextLine, idarray, Constants.marital_status, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setMarital_status(value);
            }
        });
        setValue(nextLine, idarray, Constants.cellular_telephone1, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setCellular_telephone1(value);
            }
        });
        setValue(nextLine, idarray, Constants.cellular_telephone2, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setCellular_telephone2(value);
            }
        });
        setValue(nextLine, idarray, Constants.info, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setInfo(value);
            }
        });
        setValue(nextLine, idarray, Constants.nickname, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setNickname(value);
            }
        });
        setValue(nextLine, idarray, Constants.number_of_children, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setNumber_of_children(value);
            }
        });
        setValue(nextLine, idarray, Constants.note, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setNote(value);
            }
        });
        setValue(nextLine, idarray, Constants.number_of_employee, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setNumber_of_employee(value);
            }
        });
        setValue(nextLine, idarray, Constants.telephone_pager, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setTelephone_pager(value);
            }
        });
        setValue(nextLine, idarray, Constants.password_expired, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setPassword_expired(Boolean.valueOf(stringToBool(value)));
            }
        });
        setValue(nextLine, idarray, Constants.telephone_assistant, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setTelephone_assistant(value);
            }
        });
        setValue(nextLine, idarray, Constants.telephone_business1, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setTelephone_business1(value);
            }
        });
        setValue(nextLine, idarray, Constants.telephone_business2, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setTelephone_business2(value);
            }
        });
        setValue(nextLine, idarray, Constants.telephone_car, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setTelephone_car(value);
            }
        });
        setValue(nextLine, idarray, Constants.telephone_company, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setTelephone_company(value);
            }
        });
        setValue(nextLine, idarray, Constants.telephone_home1, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setTelephone_home1(value);
            }
        });
        setValue(nextLine, idarray, Constants.telephone_home2, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setTelephone_home2(value);
            }
        });
        setValue(nextLine, idarray, Constants.telephone_other, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setTelephone_other(value);
            }
        });
        setValue(nextLine, idarray, Constants.position, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setPosition(value);
            }
        });
        setValue(nextLine, idarray, Constants.postal_code_home, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setPostal_code_home(value);
            }
        });
        setValue(nextLine, idarray, Constants.profession, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setProfession(value);
            }
        });
        setValue(nextLine, idarray, Constants.telephone_radio, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setTelephone_radio(value);
            }
        });
        setValue(nextLine, idarray, Constants.room_number, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setRoom_number(value);
            }
        });
        setValue(nextLine, idarray, Constants.sales_volume, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setSales_volume(value);
            }
        });
        setValue(nextLine, idarray, Constants.city_other, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setCity_other(value);
            }
        });
        setValue(nextLine, idarray, Constants.country_other, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setCountry_other(value);
            }
        });
        setValue(nextLine, idarray, Constants.middle_name, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setMiddle_name(value);
            }
        });
        setValue(nextLine, idarray, Constants.postal_code_other, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setPostal_code_other(value);
            }
        });
        setValue(nextLine, idarray, Constants.state_other, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setState_other(value);
            }
        });
        setValue(nextLine, idarray, Constants.street_other, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setStreet_other(value);
            }
        });
        setValue(nextLine, idarray, Constants.spouse_name, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setSpouse_name(value);
            }
        });
        setValue(nextLine, idarray, Constants.state_home, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setState_home(value);
            }
        });
        setValue(nextLine, idarray, Constants.street_home, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setStreet_home(value);
            }
        });
        setValue(nextLine, idarray, Constants.suffix, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setSuffix(value);
            }
        });
        setValue(nextLine, idarray, Constants.tax_id, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setTax_id(value);
            }
        });
        setValue(nextLine, idarray, Constants.telephone_telex, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setTelephone_telex(value);
            }
        });
        setValue(nextLine, idarray, Constants.title, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setTitle(value);
            }
        });
        setValue(nextLine, idarray, Constants.telephone_ttytdd, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setTelephone_ttytdd(value);
            }
        });
        setValue(nextLine, idarray, Constants.UPLOADFILESIZELIMIT, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) throws InvalidDataException {
                try {
                    user.setUploadFileSizeLimit(Integer.valueOf(value));
                } catch (final NumberFormatException e) {
                    throw new InvalidDataException("Value in field " + Constants.UPLOADFILESIZELIMIT.getString() + " is no integer");
                }
            }
        });
        setValue(nextLine, idarray, Constants.uploadfilesizelimitperfile, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) throws InvalidDataException {
                try {
                    user.setUploadFileSizeLimitPerFile(Integer.valueOf(value));
                } catch (final NumberFormatException e) {
                    throw new InvalidDataException("Value in field " + Constants.uploadfilesizelimitperfile.getString() + " is no integer");
                }
            }
        });
        setValue(nextLine, idarray, Constants.url, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setUrl(value);
            }
        });
        setValue(nextLine, idarray, Constants.userfield01, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setUserfield01(value);
            }
        });
        setValue(nextLine, idarray, Constants.userfield02, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setUserfield02(value);
            }
        });
        setValue(nextLine, idarray, Constants.userfield03, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setUserfield03(value);
            }
        });
        setValue(nextLine, idarray, Constants.userfield04, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setUserfield04(value);
            }
        });
        setValue(nextLine, idarray, Constants.userfield05, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setUserfield05(value);
            }
        });
        setValue(nextLine, idarray, Constants.userfield06, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setUserfield06(value);
            }
        });
        setValue(nextLine, idarray, Constants.userfield07, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setUserfield07(value);
            }
        });
        setValue(nextLine, idarray, Constants.userfield08, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setUserfield08(value);
            }
        });
        setValue(nextLine, idarray, Constants.userfield09, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setUserfield09(value);
            }
        });
        setValue(nextLine, idarray, Constants.userfield10, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setUserfield10(value);
            }
        });
        setValue(nextLine, idarray, Constants.userfield11, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setUserfield11(value);
            }
        });
        setValue(nextLine, idarray, Constants.userfield12, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setUserfield12(value);
            }
        });
        setValue(nextLine, idarray, Constants.userfield13, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setUserfield13(value);
            }
        });
        setValue(nextLine, idarray, Constants.userfield14, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setUserfield14(value);
            }
        });
        setValue(nextLine, idarray, Constants.userfield15, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setUserfield15(value);
            }
        });
        setValue(nextLine, idarray, Constants.userfield16, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setUserfield16(value);
            }
        });
        setValue(nextLine, idarray, Constants.userfield17, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setUserfield17(value);
            }
        });
        setValue(nextLine, idarray, Constants.userfield18, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setUserfield18(value);
            }
        });
        setValue(nextLine, idarray, Constants.userfield19, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setUserfield19(value);
            }
        });
        setValue(nextLine, idarray, Constants.userfield20, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setUserfield20(value);
            }
        });
        setValue(nextLine, idarray, Constants.city_business, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setCity_business(value);
            }
        });
        setValue(nextLine, idarray, Constants.country_business, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setCountry_business(value);
            }
        });
        setValue(nextLine, idarray, Constants.assistant_name, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setAssistant_name(value);
            }
        });
        setValue(nextLine, idarray, Constants.telephone_primary, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setTelephone_primary(value);
            }
        });
        setValue(nextLine, idarray, Constants.categories, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setCategories(value);
            }
        });
        setValue(nextLine, idarray, Constants.PASSWORDMECH, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setPasswordMech(value);
            }
        });
        setValue(nextLine, idarray, Constants.mail_folder_confirmed_ham_name, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setMail_folder_confirmed_ham_name(value);
            }
        });
        setValue(nextLine, idarray, Constants.mail_folder_confirmed_spam_name, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setMail_folder_confirmed_spam_name(value);
            }
        });
        setValue(nextLine, idarray, Constants.DEFAULTSENDERADDRESS, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setDefaultSenderAddress(value);
            }
        });
        setValue(nextLine, idarray, Constants.gui_spam_filter_capabilities_enabled, new MethodStringClosure() {
            @Override
            public void callMethod(final String value) {
                user.setGui_spam_filter_enabled(Boolean.valueOf(stringToBool(value)));
            }
        });
        final int m2 = idarray[Constants.MAILALIAS.getIndex()];
        if (m2 >= 0) {
            String mailAlias = nextLine[m2];
            if (mailAlias.length() > 0) {
                HashSet<String> aliases = new HashSet<String>(Arrays.asList(mailAlias.split(" *, *")));
                String primaryEmail = user.getPrimaryEmail();
                if (null != primaryEmail) {
                    aliases.add(primaryEmail);
                }
                String email1 = user.getEmail1();
                if (null != email1) {
                    aliases.add(email1);
                }
                user.setAliases(aliases);
            } else {
                if (Constants.MAILALIAS.isRequired()) {
                    throw new InvalidDataException("Field " + Constants.MAILALIAS.getString() + " required but not set.");
                }
            }
        }


        return user;
    }

    protected static void setValue(String[] nextLine, int[] idarray, CSVConstants constant, MethodStringClosure closure) throws InvalidDataException, ParseException {
        int i = idarray[constant.getIndex()];
        if (i >= 0) {
            String value = nextLine[i];
            if (value.length() > 0) {
                closure.callMethod(value);
            } else {
                if (constant.isRequired()) {
                    throw new InvalidDataException("Field " + constant.getString() + " required but not set.");
                }
            }
        }
    }

    private void setValue(final String[] nextLine, final int[] idarray, final CSVConstants constant, final MethodDateClosure closure) throws InvalidDataException, ParseException {
        final int i = idarray[constant.getIndex()];
        if (i >= 0) {
            String value = nextLine[i];
            if (value.length() > 0) {
                final Date stringToDate = stringToDate(value);
                if (null != stringToDate) {
                    closure.callMethod(stringToDate);
                }
            } else {
                if (constant.isRequired()) {
                    throw new InvalidDataException("Field " + constant.getString() + " required but not set.");
                }
            }
        }
    }

    private Date stringToDate(final String string) throws java.text.ParseException {
        final SimpleDateFormat sdf = new SimpleDateFormat(COMMANDLINE_DATEFORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone(COMMANDLINE_TIMEZONE));
        final Date value = sdf.parse(string);
        return value;
    }

    protected final void setIdOption(final AdminParser admp){
        this.idOption =  setShortLongOpt(admp,OPT_ID_SHORT,OPT_ID_LONG,"Id of the user", true, NeededQuadState.eitheror);
    }

    protected final void setUsernameOption(final AdminParser admp, final NeededQuadState needed) {
        this.userNameOption = setShortLongOpt(admp,OPT_USERNAME_SHORT,OPT_USERNAME_LONG,"Username of the user", true, needed);
    }

    protected final void setDisplayNameOption(final AdminParser admp, final NeededQuadState needed) {
        this.displayNameOption = setShortLongOpt(admp,OPT_DISPLAYNAME_SHORT,OPT_DISPLAYNAME_LONG,"Display name of the user", true, needed);
    }

    protected final void setPasswordOption(final AdminParser admp, final NeededQuadState needed) {
        this.passwordOption =  setShortLongOpt(admp,OPT_PASSWORD_SHORT,OPT_PASSWORD_LONG,"Password for the user", true, needed);
    }

    protected final void setGivenNameOption(final AdminParser admp, final NeededQuadState needed) {
        this.givenNameOption =  setShortLongOpt(admp,OPT_GIVENNAME_SHORT,OPT_GIVENNAME_LONG,"Given name for the user", true, needed);
    }

    protected final void setSurNameOption(final AdminParser admp, final NeededQuadState needed){
        this.surNameOption =  setShortLongOpt(admp,OPT_SURNAME_SHORT,OPT_SURNAME_LONG,"Sur name for the user", true, needed);
    }

    protected final void setLanguageOption(final AdminParser admp){
        this.languageOption =  setShortLongOpt(admp,OPT_LANGUAGE_SHORT,OPT_LANGUAGE_LONG,"Language for the user (de_DE,en_US)", true, NeededQuadState.notneeded);
    }

    protected final void setTimezoneOption(final AdminParser admp){
        this.timezoneOption =  setShortLongOpt(admp,OPT_TIMEZONE_SHORT,OPT_TIMEZONE_LONG,"Timezone of the user (Europe/Berlin)", true, NeededQuadState.notneeded);
    }

    protected final void setPrimaryMailOption(final AdminParser admp, final NeededQuadState needed){
        this.primaryMailOption =  setShortLongOpt(admp,OPT_PRIMARY_EMAIL_SHORT,OPT_PRIMARY_EMAIL_LONG,"Primary mail address", true, needed);
    }

    protected final void setDepartmentOption(final AdminParser admp){
        this.departmentOption = setShortLongOpt(admp,OPT_DEPARTMENT_SHORT,OPT_DEPARTMENT_LONG,"Department of the user", true, NeededQuadState.notneeded);
    }

    protected final void setCompanyOption(final AdminParser admp){
        this.companyOption = setShortLongOpt(admp,OPT_COMPANY_SHORT,OPT_COMPANY_LONG,"Company of the user", true, NeededQuadState.notneeded);
    }

    protected void setAddAccessRightCombinationNameOption(final AdminParser parser) {
        this.accessRightsCombinationName = setLongOpt(parser,OPT_ACCESSRIGHTS_COMBINATION_NAME,"Access combination name", true, false,false);
    }

    protected void setPersonal(final AdminParser parser) {
        this.personal = setLongOpt(parser,OPT_PERSONAL,"The personal of user's mail address or special value \"NULL\" to drop the personal (if any)", true, false, false);
    }

    protected void setCapsToAdd(final AdminParser parser) {
        this.capsToAdd = setLongOpt(parser,OPT_CAPABILITIES_TO_ADD,"The capabilities to add as a comma-separated string; e.g. \"portal, -autologin\"", true, false,false);
    }

    protected void setCapsToRemove(final AdminParser parser) {
        this.capsToRemove = setLongOpt(parser,OPT_CAPABILITIES_TO_REMOVE,"The capabilities to remove as a comma-separated string; e.g. \"cap2, cap2\"", true, false,false);
    }

    protected void setCapsToDrop(final AdminParser parser) {
        this.capsToDrop = setLongOpt(parser,OPT_CAPABILITIES_TO_DROP,"The capabilities to drop (clean from storage) as a comma-separated string; e.g. \"cap2, cap2\"", true, false,false);
    }

    protected void setQuotaModule(final AdminParser parser) {
        this.quotaModule = setLongOpt(parser,OPT_QUOTA_MODULE,"The (comma-separated) list of identifiers for those modules to which to apply the quota value; currently supported values: [task, calendar, contact, infostore, attachment]", true, false,false);
    }

    protected void setQuotaValue(final AdminParser parser) {
        this.quotaValue = setLongOpt(parser,OPT_QUOTA_VALUE,"The numeric quota value specifying the max. number of items allowed for context. Zero is unlimited. A value less than zero deletes the quota entry (and falls back to configured behavior)", true, false,false);
    }

    protected final void setAliasesOption(final AdminParser admp){
        this.aliasesOption = setShortLongOpt(admp,OPT_ALIASES_SHORT,OPT_ALIASES_LONG,"Comma separated list of the email aliases of the user", true, NeededQuadState.notneeded);
    }

    protected final void setImapOnlyOption(final AdminParser admp){
        this.imapOnlyOption =  setLongOpt(admp,OPT_IMAPONLY_LONG,"Do this operation only for the IMAP account of the user", false, false);
    }

    protected final void setDBOnlyOption(final AdminParser admp){
        this.dbOnlyOption =  setLongOpt(admp,OPT_DBONLY_LONG,"Do this operation only in Database system (parameters which apply to extensions will be ignored)", false, false);
    }

    protected final void setAddGuiSettingOption(final AdminParser admp){
        this.addGUISettingOption = setLongOpt(admp,OPT_ADD_GUI_SETTING_LONG,"Add a GUI setting (key=value)", true, false);
    }

    protected final void setRemoveGuiSettingOption(final AdminParser admp){
        this.removeGUISettingOption = setLongOpt(admp,OPT_REMOVE_GUI_SETTING_LONG,"Remove a GUI setting", true, false);
    }

    protected final void setConfigOption(final AdminParser adminParser){
        // The CLIOption instances for "config" and "remove-config" options are only for the purpose to mention these options for a "--help" invocation
        // Their values are set through AdminParser's dynamic options!
        this.configOption_NO_READ = setLongOpt(adminParser, OPT_CONFIG_LONG, "Add user/context specific configuration, e. g. '--config/com.openexchange.oauth.twitter=false|true'", false, false);
    }

    protected final void setRemoveConfigOption(final AdminParser adminParser){
        // The CLIOption instances for "config" and "remove-config" options are only for the purpose to mention these options for a "--help" invocation
        // Their values are set through AdminParser's dynamic options!
        this.removeConfigOption_NO_READ = setLongOpt(adminParser, OPT_REMOVE_CONFIG_LONG, "Remove user/context specific configuration, e. g. '--remove-config/com.openexchange.oauth.twitter'", false, false);
    }

    /**
     * @param theMethods
     * @param notallowedOrReplace Here we define the methods we don't want or want to replace. The name is the name of method without the prefix.
     * get or is. If the value of the map contains a string with length > 0, then this string will be used as columnname
     * @return
     */
    protected final ArrayList<MethodAndNames> getGetters(final Method[] theMethods, final Map<String, String> notallowedOrReplace) {
        // Define the returntypes we search for
        final HashSet<String> returntypes = new HashSet<String>();
        returntypes.add(JAVA_LANG_STRING);
        returntypes.add(JAVA_LANG_INTEGER);
        returntypes.add(JAVA_LANG_LONG);
        returntypes.add(JAVA_LANG_BOOLEAN);
        returntypes.add(JAVA_UTIL_DATE);
        returntypes.add(JAVA_UTIL_HASH_SET);
        returntypes.add(JAVA_UTIL_MAP);
        returntypes.add(JAVA_UTIL_TIME_ZONE);
        returntypes.add(JAVA_UTIL_LOCALE);
        returntypes.add(PASSWORDMECH_CLASS);
        returntypes.add(SIMPLE_INT);

        return getGetterGeneral(theMethods, notallowedOrReplace, returntypes);
    }

    private final ArrayList<MethodAndNames> getGetterGeneral(final Method[] theMethods, final Map<String, String> notallowedOrReplace, final HashSet<String> returntypes) {
        final ArrayList<MethodAndNames> retlist = new ArrayList<MethodAndNames>();
        // First we get all the getters of the user data class
        for (final Method method : theMethods) {
            // Getters shouldn't need parameters
            if(method.getParameterTypes().length > 0) {
                continue;
            }
            final String methodname = method.getName();

            if (methodname.startsWith("get")) {
                final String methodnamewithoutprefix = methodname.substring(3);
                if (!notallowedOrReplace.containsKey(methodnamewithoutprefix)) {
                    final String returntype = method.getReturnType().getName();
                    if (returntypes.contains(returntype)) {
                        retlist.add(new MethodAndNames(method, methodnamewithoutprefix, returntype));
                    }
                } else if (0 != notallowedOrReplace.get(methodnamewithoutprefix).length()) {
                    final String returntype = method.getReturnType().getName();
                    if (returntypes.contains(returntype)) {
                        retlist.add(new MethodAndNames(method, notallowedOrReplace.get(methodnamewithoutprefix), returntype));
                    }
                }
            } else if (methodname.startsWith("is")) {
                final String methodnamewithoutprefix = methodname.substring(2);
                if (!notallowedOrReplace.containsKey(methodnamewithoutprefix)) {
                    final String returntype = method.getReturnType().getName();
                    if (returntypes.contains(returntype)) {
                        retlist.add(new MethodAndNames(method, methodnamewithoutprefix, returntype));
                    }
                } else if (0 != notallowedOrReplace.get(methodnamewithoutprefix).length()) {
                    final String returntype = method.getReturnType().getName();
                    if (returntypes.contains(returntype)) {
                        retlist.add(new MethodAndNames(method, notallowedOrReplace.get(methodnamewithoutprefix), returntype));
                    }
                }
            }
        }
        return retlist;
    }

    public String parseAndSetAccessCombinationName(final AdminParser parser) {
        return (String) parser.getOptionValue(this.accessRightsCombinationName);
    }

    public String parseAndSetQuotaModule(final AdminParser parser) {
        if (null == quotaModule) {
            setQuotaModule(parser);
        }
        final Object object = parser.getOptionValue(quotaModule);
        if (null == object) {
            return null;
        }
        final String tmp = object.toString().trim();
        return com.openexchange.java.Strings.isEmpty(tmp) ? null : tmp;
    }

    public Long parseAndSetQuotaValue(final AdminParser parser) throws InvalidDataException {
        if (null == quotaValue) {
            setQuotaValue(parser);
        }
        final Object object = parser.getOptionValue(quotaValue);
        if (null == object) {
            return null;
        }
        try {
            return Long.valueOf(object.toString().trim());
        } catch (NumberFormatException e) {
            throw new InvalidDataException("Quota value must be a number.");
        }
    }

    public String parseAndSetPersonal(final AdminParser parser) {
        if (null == personal) {
            setPersonal(parser);
        }
        Object object = parser.getOptionValue(personal);
        if (null == object) {
            return null;
        }
        return object.toString().trim();
    }

    public Set<String> parseAndSetCapabilitiesToAdd(final AdminParser parser) {
        if (null == capsToAdd) {
            setCapsToAdd(parser);
        }
        return parseAndSetCapabilities(capsToAdd, parser);
    }

    public Set<String> parseAndSetCapabilitiesToRemove(final AdminParser parser) {
        if (null == capsToRemove) {
            setCapsToRemove(parser);
        }
        return parseAndSetCapabilities(capsToRemove, parser);
    }

    public Set<String> parseAndSetCapabilitiesToDrop(final AdminParser parser) {
        if (null == capsToDrop) {
            setCapsToDrop(parser);
        }
        return parseAndSetCapabilities(capsToDrop, parser);
    }

    private Set<String> parseAndSetCapabilities(final CLIOption cliOption, final AdminParser parser) {
        String s = (String) parser.getOptionValue(cliOption);
        if (com.openexchange.java.Strings.isEmpty(s)) {
            return Collections.emptySet();
        }
        s = s.trim();
        if ('"' == s.charAt(0)) {
            if (s.length() <= 1) {
                return Collections.emptySet();
            }
            s = s.substring(1);
            if (com.openexchange.java.Strings.isEmpty(s)) {
                return Collections.emptySet();
            }
        }
        if ('"' == s.charAt(s.length() - 1)) {
            if (s.length() <= 1) {
                return Collections.emptySet();
            }
            s = s.substring(0, s.length() - 1);
            if (com.openexchange.java.Strings.isEmpty(s)) {
                return Collections.emptySet();
            }
        }
        // Split
        final String[] arr = s.split(" *, *", 0);
        final Set<String> set = new HashSet<String>(arr.length);
        for (String element : arr) {
            final String cap = element;
            if (!com.openexchange.java.Strings.isEmpty(cap)) {
                set.add(com.openexchange.java.Strings.toLowerCase(cap));
            }
        }
        return set;
    }

    /**
     * Get the mandatory options from the command line and set's them in the user object
     *
     * @param parser The parser object
     * @param usr User object which will be changed
     */
    protected final void parseAndSetMandatoryOptionsinUser(final AdminParser parser, final User usr) {
        parseAndSetUsername(parser, usr);
        parseAndSetMandatoryOptionsWithoutUsernameInUser(parser, usr);
    }

    protected void parseAndSetUsername(final AdminParser parser, final User usr) {
        this.username = (String) parser.getOptionValue(this.userNameOption);
        if (null != this.username) {
            usr.setName(this.username);
        }
    }

    protected void parseAndSetDisplayName(final AdminParser parser, final User usr) {
        this.displayName = (String) parser.getOptionValue(this.displayNameOption);
        if (null != this.displayName) {
            usr.setDisplay_name(displayName);
        }
    }

    protected final void parseAndSetMandatoryOptionsWithoutUsernameInUser(final AdminParser parser, final User usr) {
        String optionValue2 = (String) parser.getOptionValue(this.displayNameOption);
        if (null != optionValue2) {
            if ("".equals(optionValue2)) { optionValue2 = null; }
            usr.setDisplay_name(optionValue2);
        }

        String optionValue3 = (String) parser.getOptionValue(this.givenNameOption);
        if (null != optionValue3) {
            if ("".equals(optionValue3)) { optionValue3 = null; }
            usr.setGiven_name(optionValue3);
        }

        String optionValue4 = (String) parser.getOptionValue(this.surNameOption);
        if (null != optionValue4) {
            if ("".equals(optionValue4)) { optionValue4 = null; }
            usr.setSur_name(optionValue4);
        }

        String optionValue5 = null;
        if( NEW_USER_PASSWORD != null ) {
            optionValue5 = NEW_USER_PASSWORD;
        } else {
            optionValue5 = (String) parser.getOptionValue(this.passwordOption);
        }
        if (null != optionValue5) {
            usr.setPassword(optionValue5);
        }

        final String optionValue6 = (String) parser.getOptionValue(this.primaryMailOption);
        if (null != optionValue6) {
            usr.setPrimaryEmail(optionValue6);
            usr.setEmail1(optionValue6);
        }
    }

    protected void prepareConstantsMap() {
        HashMap<String, CSVConstants> constantsMap = new HashMap<String, CSVConstants>();
        this.constantsMap = constantsMap;
        for (final Constants value : Constants.values()) {
            constantsMap.put(value.getString(), value);
        }
        for (final AccessCombinations value : AccessCombinations.values()) {
            constantsMap.put(value.getString(), value);
        }
    }

    /**
     * Apply module access rights given from command line to the given module access object.
     *
     * @param parser The parser object
     * @param usr User object which will be changed
     */
    protected final void setModuleAccessOptionsinUserCreate(final AdminParser parser, final UserModuleAccess access) {
        access.setCalendar(accessOption2BooleanCreate(parser,this.accessCalendarOption));
        access.setContacts(accessOption2BooleanCreate(parser,this.accessContactOption));
        access.setDelegateTask(accessOption2BooleanCreate(parser,this.accessDelegateTasksOption));
        access.setEditPublicFolders(accessOption2BooleanCreate(parser,this.accessEditPublicFolderOption));
        access.setIcal(accessOption2BooleanCreate(parser,this.accessIcalOption));
        access.setInfostore(accessOption2BooleanCreate(parser,this.accessInfostoreOption));
        access.setReadCreateSharedFolders(accessOption2BooleanCreate(parser,this.accessReadCreateSharedFolderOption));
        access.setSyncml(accessOption2BooleanCreate(parser,this.accessSyncmlOption));
        access.setTasks(accessOption2BooleanCreate(parser,this.accessTasksOption));
        access.setVcard(accessOption2BooleanCreate(parser,this.accessVcardOption));
        access.setWebdav(accessOption2BooleanCreate(parser,this.accessWebdavOption));
        access.setWebdavXml(accessOption2BooleanCreate(parser,this.accessWebdavXmlOption));
        access.setWebmail(accessOption2BooleanCreate(parser,this.accessWebmailOption));
        access.setEditGroup(accessOption2BooleanCreate(parser,this.accessEditGroupOption));
        access.setEditResource(accessOption2BooleanCreate(parser,this.accessEditResourceOption));
        access.setEditPassword(accessOption2BooleanCreate(parser,this.accessEditPasswordOption));
        access.setCollectEmailAddresses(accessOption2BooleanCreate(parser,this.accessCollectEmailAddresses));
        access.setMultipleMailAccounts(accessOption2BooleanCreate(parser,this.accessMultipleMailAccounts));
        access.setSubscription(accessOption2BooleanCreate(parser,this.accessSubscription));
        access.setPublication(accessOption2BooleanCreate(parser,this.accessPublication));
        access.setActiveSync(accessOption2BooleanCreate(parser,this.accessActiveSync));
        access.setUSM(accessOption2BooleanCreate(parser, this.accessUSM));
        access.setOLOX20(accessOption2BooleanCreate(parser, this.accessOLOX20));
        access.setDeniedPortal(accessOption2BooleanCreate(parser, this.accessDeniedPortal));
        access.setGlobalAddressBookDisabled(accessOption2BooleanCreate(parser, this.accessGAB));
        access.setPublicFolderEditable(accessOption2BooleanCreate(parser, this.accessPublicFolderEditable));
    }

    protected final boolean accessOption2BooleanCreate(final AdminParser parser,final CLIOption accessOption){
        // option was set, check what text was sent
        final String optionValue = (String) parser.getOptionValue(accessOption);
        if (optionValue == null) {
            // option was not set in create. we return true, because default is
            // on
            return true;
        }
        return stringToBool(optionValue);
    }

    /**
     * @param parser
     * @param access
     * @return true if options have been specified, false if not
     */
    protected final boolean setModuleAccessOptions(final AdminParser parser, final UserModuleAccess access) {
        boolean changed = false;
        if ((String) parser.getOptionValue(this.accessCalendarOption) != null) {
            access.setCalendar(accessOption2BooleanCreate(parser, this.accessCalendarOption));
            changed = true;
        }
        if ((String) parser.getOptionValue(this.accessContactOption) != null) {
            access.setContacts(accessOption2BooleanCreate(parser, this.accessContactOption));
            changed = true;
        }
        if ((String) parser.getOptionValue(this.accessDelegateTasksOption) != null) {
            access.setDelegateTask(accessOption2BooleanCreate(parser, this.accessDelegateTasksOption));
            changed = true;
        }
        if ((String) parser.getOptionValue(this.accessEditPublicFolderOption) != null) {
            access.setEditPublicFolders(accessOption2BooleanCreate(parser, this.accessEditPublicFolderOption));
            changed = true;
        }
        if ((String) parser.getOptionValue(this.accessIcalOption) != null) {
            access.setIcal(accessOption2BooleanCreate(parser, this.accessIcalOption));
            changed = true;
        }
        if ((String) parser.getOptionValue(this.accessInfostoreOption) != null) {
            access.setInfostore(accessOption2BooleanCreate(parser, this.accessInfostoreOption));
            changed = true;
        }
        if ((String) parser.getOptionValue(this.accessReadCreateSharedFolderOption) != null) {
            access.setReadCreateSharedFolders(accessOption2BooleanCreate(parser, this.accessReadCreateSharedFolderOption));
            changed = true;
        }
        if ((String) parser.getOptionValue(this.accessSyncmlOption) != null) {
            access.setSyncml(accessOption2BooleanCreate(parser, this.accessSyncmlOption));
            changed = true;
        }
        if ((String) parser.getOptionValue(this.accessTasksOption) != null) {
            access.setTasks(accessOption2BooleanCreate(parser, this.accessTasksOption));
            changed = true;
        }
        if ((String) parser.getOptionValue(this.accessVcardOption) != null) {
            access.setVcard(accessOption2BooleanCreate(parser, this.accessVcardOption));
            changed = true;
        }
        if ((String) parser.getOptionValue(this.accessWebdavOption) != null) {
            access.setWebdav(accessOption2BooleanCreate(parser, this.accessWebdavOption));
            changed = true;
        }
        if ((String) parser.getOptionValue(this.accessWebdavXmlOption) != null) {
            access.setWebdavXml(accessOption2BooleanCreate(parser, this.accessWebdavXmlOption));
            changed = true;
        }
        if ((String) parser.getOptionValue(this.accessWebmailOption) != null) {
            access.setWebmail(accessOption2BooleanCreate(parser, this.accessWebmailOption));
            changed = true;
        }
        if ((String) parser.getOptionValue(this.accessEditGroupOption) != null) {
            access.setEditGroup(accessOption2BooleanCreate(parser, this.accessEditGroupOption));
            changed = true;
        }
        if ((String) parser.getOptionValue(this.accessEditResourceOption) != null) {
            access.setEditResource(accessOption2BooleanCreate(parser, this.accessEditResourceOption));
            changed = true;
        }
        if ((String) parser.getOptionValue(this.accessEditPasswordOption) != null) {
            access.setEditPassword(accessOption2BooleanCreate(parser, this.accessEditPasswordOption));
            changed = true;
        }
        if ((String) parser.getOptionValue(this.accessCollectEmailAddresses) != null) {
            access.setCollectEmailAddresses(accessOption2BooleanCreate(parser, this.accessCollectEmailAddresses));
            changed = true;
        }
        if ((String) parser.getOptionValue(this.accessMultipleMailAccounts) != null) {
            access.setMultipleMailAccounts(accessOption2BooleanCreate(parser, this.accessMultipleMailAccounts));
            changed = true;
        }
        if ((String) parser.getOptionValue(this.accessSubscription) != null) {
            access.setSubscription(accessOption2BooleanCreate(parser, this.accessSubscription));
            changed = true;
        }
        if ((String) parser.getOptionValue(this.accessPublication) != null) {
            access.setPublication(accessOption2BooleanCreate(parser, this.accessPublication));
            changed = true;
        }
        if((String) parser.getOptionValue(this.accessActiveSync) != null) {
            access.setActiveSync(accessOption2BooleanCreate(parser, this.accessActiveSync));
            changed = true;
        }
        if((String) parser.getOptionValue(this.accessUSM) != null) {
            access.setUSM(accessOption2BooleanCreate(parser, this.accessUSM));
            changed = true;
        }
        if((String) parser.getOptionValue(this.accessOLOX20) != null) {
            access.setOLOX20(accessOption2BooleanCreate(parser, this.accessOLOX20));
            changed = true;
        }
        if((String) parser.getOptionValue(this.accessDeniedPortal) != null) {
            access.setDeniedPortal(accessOption2BooleanCreate(parser, this.accessDeniedPortal));
            changed = true;
        }
        if((String) parser.getOptionValue(this.accessGAB) != null) {
            access.setGlobalAddressBookDisabled(accessOption2BooleanCreate(parser, this.accessGAB));
            changed = true;
        }
        if((String) parser.getOptionValue(this.accessPublicFolderEditable) != null) {
            access.setPublicFolderEditable(accessOption2BooleanCreate(parser, this.accessPublicFolderEditable));
            changed = true;
        }
        return changed;
    }

    protected final boolean accessOption2BooleanChange(final AdminParser parser, final CLIOption accessOption) {
        // option was set, check what text was sent
        final String optionValue = (String) parser.getOptionValue(accessOption);
        return stringToBool(optionValue);
    }

    /**
     * Get the optional options from the command line and set's them in the user object
     *
     * @param parser The parser object
     * @param usr User object which will be changed
     * @throws InvalidDataException
     */
    protected final void parseAndSetOptionalOptionsinUser(final AdminParser parser, final User usr) throws InvalidDataException {
        final String optionValue = (String) parser.getOptionValue(this.companyOption);
        if (null != optionValue) {
            usr.setCompany(optionValue);
        }

        final String optionValue2 = (String) parser.getOptionValue(this.departmentOption);
        if (null != optionValue2) {
            usr.setDepartment(optionValue2);
        }

        final String optionValue3 = (String) parser.getOptionValue(this.languageOption);
        if (null != optionValue3) {
            usr.setLanguage(optionValue3);
        }

        final String optionValue4 = (String) parser.getOptionValue(this.timezoneOption);
        if (null != optionValue4) {
            if (!Arrays.asList(TimeZone.getAvailableIDs()).contains(optionValue4)) {
                throw new InvalidDataException("The given timezone is invalid");
            }
            usr.setTimezone(optionValue4);
        }

        final String aliasOpt = (String) parser.getOptionValue(this.aliasesOption);
        if (null != aliasOpt) {
            final HashSet<String> aliases = new HashSet<String>();
            for (final String alias : aliasOpt.split(",")) {
                aliases.add(alias.trim());
            }
            usr.setAliases(aliases);
        }
    }


    protected final void setModuleAccessOptions(final AdminParser admp) {
        setModuleAccessOptions(admp, false, true);
    }

    protected void setModuleAccessOptions(final AdminParser admp, final boolean required, final boolean extended) {
        // TODO: The default values should be dynamically generated from the setting in the core
        this.accessCalendarOption = setLongOpt(admp, OPT_ACCESS_CALENDAR,"on/off","Calendar module (Default is off)", true, false,true);
        this.accessContactOption = setLongOpt(admp, OPT_ACCESS_CONTACTS,"on/off","Contact module access (Default is on)", true, false,true);
        this.accessDelegateTasksOption = setLongOpt(admp, OPT_ACCESS_DELEGATE_TASKS,"on/off","Delegate tasks access (Default is off)", true, false,true);
        this.accessEditPublicFolderOption = setLongOpt(admp, OPT_ACCESS_EDIT_PUBLIC_FOLDERS,"on/off","Edit public folder access (Default is off)", true, false,true);
        this.accessIcalOption = setLongOpt(admp, OPT_ACCESS_ICAL,"on/off","Ical module access (Default is off)", true, false,true);
        this.accessInfostoreOption = setLongOpt(admp, OPT_ACCESS_INFOSTORE,"on/off","Infostore module access (Default is off)", true, false,true);
        this.accessReadCreateSharedFolderOption = setLongOpt(admp, OPT_ACCESS_READCREATE_SHARED_FOLDERS,"on/off","Read create shared folder access (Default is off)", true, false,true);
        this.accessSyncmlOption = setLongOpt(admp, OPT_ACCESS_SYNCML,"on/off","Syncml access (Default is off)", true, false,true);
        this.accessTasksOption = setLongOpt(admp, OPT_ACCESS_TASKS,"on/off","Tasks access (Default is off)", true, false,true);
        this.accessVcardOption = setLongOpt(admp, OPT_ACCESS_VCARD,"on/off","Vcard access (Default is off)", true, false,true);
        this.accessWebdavOption = setLongOpt(admp, OPT_ACCESS_WEBDAV,"on/off","Webdav access (Default is off)", true, false,true);
        this.accessWebdavXmlOption = setLongOpt(admp, OPT_ACCESS_WEBDAV_XML,"on/off","Webdav-Xml access (Default is off)", true, false,true);
        this.accessWebmailOption = setLongOpt(admp, OPT_ACCESS_WEBMAIL,"on/off","Webmail access (Default is on)", true, false,true);
        this.accessEditGroupOption = setLongOpt(admp, OPT_ACCESS_EDIT_GROUP,"on/off","Edit Group access (Default is off)", true, false,true);
        this.accessEditResourceOption = setLongOpt(admp, OPT_ACCESS_EDIT_RESOURCE,"on/off","Edit Resource access (Default is off)", true, false,true);
        this.accessEditPasswordOption = setLongOpt(admp, OPT_ACCESS_EDIT_PASSWORD,"on/off","Edit Password access (Default is off)", true, false,true);
        this.accessCollectEmailAddresses = setLongOpt(admp, OPT_ACCESS_COLLECT_EMAIL_ADDRESSES,"on/off","Collect Email Addresses access (Default is off)", true, false,true);
        this.accessMultipleMailAccounts = setLongOpt(admp, OPT_ACCESS_MULTIPLE_MAIL_ACCOUNTS,"on/off","Multiple Mail Accounts access (Default is off)", true, false,true);
        this.accessSubscription = setLongOpt(admp, OPT_ACCESS_SUBSCRIPTION,"on/off","Subscription access (Default is off)", true, false,true);
        this.accessPublication = setLongOpt(admp, OPT_ACCESS_PUBLICATION,"on/off","Publication access (Default is off)", true, false,true);
        this.accessActiveSync = setLongOpt(admp, OPT_ACCESS_ACTIVE_SYNC, "on/off", "Exchange Active Sync access (Default is off)", true, false, true);
        this.accessUSM = setLongOpt(admp, OPT_ACCESS_USM, "on/off", "Universal Sync access (Default is off)", true, false, true);
        this.accessOLOX20 = setLongOpt(admp, OPT_ACCESS_OLOX20, "on/off", "OLOX v2.0 access (Default is off)", true, false, true);
        this.accessDeniedPortal = setLongOpt(admp, OPT_ACCESS_DENIED_PORTAL, "on/off", "Denies portal access (Default is off)", true, false, true);
        this.accessGAB = setLongOpt(admp, OPT_DISABLE_GAB, "on/off", "Disable Global Address Book access (Default is off)", true, false, true);
        this.accessPublicFolderEditable = setLongOpt(admp, OPT_ACCESS_PUBLIC_FOLDER_EDITABLE, "on/off", "Whether public folder(s) is/are editable (Default is off). Applies only to context admin user.", true, false, true);
    }

    protected final void setMandatoryOptions(final AdminParser parser) {
        setUsernameOption(parser, NeededQuadState.needed);
        setMandatoryOptionsWithoutUsername(parser, NeededQuadState.needed);
    }

    protected void setMandatoryOptionsWithoutUsername(final AdminParser parser, final NeededQuadState needed) {
        setDisplayNameOption(parser, needed);
        setGivenNameOption(parser, needed);
        setSurNameOption(parser, needed);
        // if password of new user is supplied in environment, do not insist on password option
        if( NEW_USER_PASSWORD != null ) {
            setPasswordOption(parser, NeededQuadState.notneeded);
        } else {
            setPasswordOption(parser, needed);
        }
        setPrimaryMailOption(parser, needed);
    }

    protected final void setOptionalOptions(final AdminParser parser) {
        setLanguageOption(parser);
        setTimezoneOption(parser);
        setDepartmentOption(parser);
        setCompanyOption(parser);
        setAliasesOption(parser);
    }

    protected void setExtendedOptions(final AdminParser parser) {
        setAddGuiSettingOption(parser);

        if( this.getClass().getName().endsWith("Change") ) {
            setRemoveGuiSettingOption(parser);
        }

        setConfigOption(parser);
        setRemoveConfigOption(parser);

        this.email1Option = setLongOpt(parser, OPT_EMAIL1_LONG, "stringvalue", "Email1", true, false, true);
        this.mailenabledOption = setSettableBooleanLongOpt(parser, OPT_MAILENABLED_LONG, "true / false", "Mailenabled", true, false, true);
        this.birthdayOption = setLongOpt(parser, OPT_BIRTHDAY_LONG, "datevalue", "Birthday", true, false, true);
        this.anniversaryOption = setLongOpt(parser, OPT_ANNIVERSARY_LONG, "datevalue", "Anniversary", true, false, true);
        this.branchesOption = setLongOpt(parser, OPT_BRANCHES_LONG, "stringvalue", "Branches", true, false, true);
        this.business_categoryOption = setLongOpt(parser, OPT_BUSINESS_CATEGORY_LONG, "stringvalue", "Business_category", true, false, true);
        this.postal_code_businessOption = setLongOpt(parser, OPT_POSTAL_CODE_BUSINESS_LONG, "stringvalue", "Postal_code_business", true, false, true);
        this.state_businessOption = setLongOpt(parser, OPT_STATE_BUSINESS_LONG, "stringvalue", "State_business", true, false, true);
        this.street_businessOption = setLongOpt(parser, OPT_STREET_BUSINESS_LONG, "stringvalue", "Street_business", true, false, true);
        this.telephone_callbackOption = setLongOpt(parser, OPT_TELEPHONE_CALLBACK_LONG, "stringvalue", "Telephone_callback", true, false, true);
        this.city_homeOption = setLongOpt(parser, OPT_CITY_HOME_LONG, "stringvalue", "City_home", true, false, true);
        this.commercial_registerOption = setLongOpt(parser, OPT_COMMERCIAL_REGISTER_LONG, "stringvalue", "Commercial_register", true, false, true);
        this.country_homeOption = setLongOpt(parser, OPT_COUNTRY_HOME_LONG, "stringvalue", "Country_home", true, false, true);
        this.email2Option = setLongOpt(parser, OPT_EMAIL2_LONG, "stringvalue", "Email2", true, false, true);
        this.email3Option = setLongOpt(parser, OPT_EMAIL3_LONG, "stringvalue", "Email3", true, false, true);
        this.employeetypeOption = setLongOpt(parser, OPT_EMPLOYEETYPE_LONG, "stringvalue", "EmployeeType", true, false, true);
        this.fax_businessOption = setLongOpt(parser, OPT_FAX_BUSINESS_LONG, "stringvalue", "Fax_business", true, false, true);
        this.fax_homeOption = setLongOpt(parser, OPT_FAX_HOME_LONG, "stringvalue", "Fax_home", true, false, true);
        this.fax_otherOption = setLongOpt(parser, OPT_FAX_OTHER_LONG, "stringvalue", "Fax_other", true, false, true);
        this.imapserverOption = setLongOpt(parser, OPT_IMAPSERVER_LONG, "stringvalue", "ImapServer", true, false, true);
        this.imaploginOption = setLongOpt(parser, OPT_IMAPLOGIN_LONG, "stringvalue", "ImapLogin", true, false, true);
        this.smtpserverOption = setLongOpt(parser, OPT_SMTPSERVER_LONG, "stringvalue", "SmtpServer", true, false, true);
        this.instant_messenger1Option = setLongOpt(parser, OPT_INSTANT_MESSENGER1_LONG, "stringvalue", "Instant_messenger1", true, false, true);
        this.instant_messenger2Option = setLongOpt(parser, OPT_INSTANT_MESSENGER2_LONG, "stringvalue", "Instant_messenger2", true, false, true);
        this.telephone_ipOption = setLongOpt(parser, OPT_TELEPHONE_IP_LONG, "stringvalue", "Telephone_ip", true, false, true);
        this.telephone_isdnOption = setLongOpt(parser, OPT_TELEPHONE_ISDN_LONG, "stringvalue", "Telephone_isdn", true, false, true);
        this.mail_folder_drafts_nameOption = setLongOpt(parser, OPT_MAIL_FOLDER_DRAFTS_NAME_LONG, "stringvalue", "Mail_folder_drafts_name", true, false, true);
        this.mail_folder_sent_nameOption = setLongOpt(parser, OPT_MAIL_FOLDER_SENT_NAME_LONG, "stringvalue", "Mail_folder_sent_name", true, false, true);
        this.mail_folder_spam_nameOption = setLongOpt(parser, OPT_MAIL_FOLDER_SPAM_NAME_LONG, "stringvalue", "Mail_folder_spam_name", true, false, true);
        this.mail_folder_trash_nameOption = setLongOpt(parser, OPT_MAIL_FOLDER_TRASH_NAME_LONG, "stringvalue", "Mail_folder_trash_name", true, false, true);
        this.mail_folder_archive_full_nameOption = setLongOpt(parser, OPT_MAIL_FOLDER_ARCHIVE_FULL_NAME_LONG, "stringvalue", "Mail_folder_archive_full_name", true, false, true);
        this.manager_nameOption = setLongOpt(parser, OPT_MANAGER_NAME_LONG, "stringvalue", "Manager_name", true, false, true);
        this.marital_statusOption = setLongOpt(parser, OPT_MARITAL_STATUS_LONG, "stringvalue", "Marital_status", true, false, true);
        this.cellular_telephone1Option = setLongOpt(parser, OPT_CELLULAR_TELEPHONE1_LONG, "stringvalue", "Cellular_telephone1", true, false, true);
        this.cellular_telephone2Option = setLongOpt(parser, OPT_CELLULAR_TELEPHONE2_LONG, "stringvalue", "Cellular_telephone2", true, false, true);
        this.infoOption = setLongOpt(parser, OPT_INFO_LONG, "stringvalue", "Info", true, false, true);
        this.nicknameOption = setLongOpt(parser, OPT_NICKNAME_LONG, "stringvalue", "Nickname", true, false, true);
        this.number_of_childrenOption = setLongOpt(parser, OPT_NUMBER_OF_CHILDREN_LONG, "stringvalue", "Number_of_children", true, false, true);
        this.noteOption = setLongOpt(parser, OPT_NOTE_LONG, "stringvalue", "Note", true, false, true);
        this.number_of_employeeOption = setLongOpt(parser, OPT_NUMBER_OF_EMPLOYEE_LONG, "stringvalue", "Number_of_employee", true, false, true);
        this.telephone_pagerOption = setLongOpt(parser, OPT_TELEPHONE_PAGER_LONG, "stringvalue", "Telephone_pager", true, false, true);
        this.password_expiredOption = setSettableBooleanLongOpt(parser, OPT_PASSWORD_EXPIRED_LONG, "true / false", "Password_expired", true, false, true);
        this.telephone_assistantOption = setLongOpt(parser, OPT_TELEPHONE_ASSISTANT_LONG, "stringvalue", "Telephone_assistant", true, false, true);
        this.telephone_business1Option = setLongOpt(parser, OPT_TELEPHONE_BUSINESS1_LONG, "stringvalue", "Telephone_business1", true, false, true);
        this.telephone_business2Option = setLongOpt(parser, OPT_TELEPHONE_BUSINESS2_LONG, "stringvalue", "Telephone_business2", true, false, true);
        this.telephone_carOption = setLongOpt(parser, OPT_TELEPHONE_CAR_LONG, "stringvalue", "Telephone_car", true, false, true);
        this.telephone_companyOption = setLongOpt(parser, OPT_TELEPHONE_COMPANY_LONG, "stringvalue", "Telephone_company", true, false, true);
        this.telephone_home1Option = setLongOpt(parser, OPT_TELEPHONE_HOME1_LONG, "stringvalue", "Telephone_home1", true, false, true);
        this.telephone_home2Option = setLongOpt(parser, OPT_TELEPHONE_HOME2_LONG, "stringvalue", "Telephone_home2", true, false, true);
        this.telephone_otherOption = setLongOpt(parser, OPT_TELEPHONE_OTHER_LONG, "stringvalue", "Telephone_other", true, false, true);
        this.positionOption = setLongOpt(parser, OPT_POSITION_LONG, "stringvalue", "Position", true, false, true);
        this.postal_code_homeOption = setLongOpt(parser, OPT_POSTAL_CODE_HOME_LONG, "stringvalue", "Postal_code_home", true, false, true);
        this.professionOption = setLongOpt(parser, OPT_PROFESSION_LONG, "stringvalue", "Profession", true, false, true);
        this.telephone_radioOption = setLongOpt(parser, OPT_TELEPHONE_RADIO_LONG, "stringvalue", "Telephone_radio", true, false, true);
        this.room_numberOption = setLongOpt(parser, OPT_ROOM_NUMBER_LONG, "stringvalue", "Room_number", true, false, true);
        this.sales_volumeOption = setLongOpt(parser, OPT_SALES_VOLUME_LONG, "stringvalue", "Sales_volume", true, false, true);
        this.city_otherOption = setLongOpt(parser, OPT_CITY_OTHER_LONG, "stringvalue", "City_other", true, false, true);
        this.country_otherOption = setLongOpt(parser, OPT_COUNTRY_OTHER_LONG, "stringvalue", "Country_other", true, false, true);
        this.middle_nameOption = setLongOpt(parser, OPT_MIDDLE_NAME_LONG, "stringvalue", "Middle_name", true, false, true);
        this.postal_code_otherOption = setLongOpt(parser, OPT_POSTAL_CODE_OTHER_LONG, "stringvalue", "Postal_code_other", true, false, true);
        this.state_otherOption = setLongOpt(parser, OPT_STATE_OTHER_LONG, "stringvalue", "State_other", true, false, true);
        this.street_otherOption = setLongOpt(parser, OPT_STREET_OTHER_LONG, "stringvalue", "Street_other", true, false, true);
        this.spouse_nameOption = setLongOpt(parser, OPT_SPOUSE_NAME_LONG, "stringvalue", "Spouse_name", true, false, true);
        this.state_homeOption = setLongOpt(parser, OPT_STATE_HOME_LONG, "stringvalue", "State_home", true, false, true);
        this.street_homeOption = setLongOpt(parser, OPT_STREET_HOME_LONG, "stringvalue", "Street_home", true, false, true);
        this.suffixOption = setLongOpt(parser, OPT_SUFFIX_LONG, "stringvalue", "Suffix", true, false, true);
        this.tax_idOption = setLongOpt(parser, OPT_TAX_ID_LONG, "stringvalue", "Tax_id", true, false, true);
        this.telephone_telexOption = setLongOpt(parser, OPT_TELEPHONE_TELEX_LONG, "stringvalue", "Telephone_telex", true, false, true);
        this.titleOption = setLongOpt(parser, OPT_TITLE_LONG, "stringvalue", "Title", true, false, true);
        this.telephone_ttytddOption = setLongOpt(parser, OPT_TELEPHONE_TTYTDD_LONG, "stringvalue", "Telephone_ttytdd", true, false, true);
        this.uploadfilesizelimitOption = setIntegerLongOpt(parser, OPT_UPLOADFILESIZELIMIT_LONG, "intvalue", "UploadFileSizeLimit", true, false, true);
        this.uploadfilesizelimitperfileOption = setIntegerLongOpt(parser, OPT_UPLOADFILESIZELIMITPERFILE_LONG, "intvalue", "UploadFileSizeLimitPerFile", true, false, true);
        this.urlOption = setLongOpt(parser, OPT_URL_LONG, "stringvalue", "Url", true, false, true);
        this.userfield01Option = setLongOpt(parser, OPT_USERFIELD01_LONG, "stringvalue", "Userfield01", true, false, true);
        this.userfield02Option = setLongOpt(parser, OPT_USERFIELD02_LONG, "stringvalue", "Userfield02", true, false, true);
        this.userfield03Option = setLongOpt(parser, OPT_USERFIELD03_LONG, "stringvalue", "Userfield03", true, false, true);
        this.userfield04Option = setLongOpt(parser, OPT_USERFIELD04_LONG, "stringvalue", "Userfield04", true, false, true);
        this.userfield05Option = setLongOpt(parser, OPT_USERFIELD05_LONG, "stringvalue", "Userfield05", true, false, true);
        this.userfield06Option = setLongOpt(parser, OPT_USERFIELD06_LONG, "stringvalue", "Userfield06", true, false, true);
        this.userfield07Option = setLongOpt(parser, OPT_USERFIELD07_LONG, "stringvalue", "Userfield07", true, false, true);
        this.userfield08Option = setLongOpt(parser, OPT_USERFIELD08_LONG, "stringvalue", "Userfield08", true, false, true);
        this.userfield09Option = setLongOpt(parser, OPT_USERFIELD09_LONG, "stringvalue", "Userfield09", true, false, true);
        this.userfield10Option = setLongOpt(parser, OPT_USERFIELD10_LONG, "stringvalue", "Userfield10", true, false, true);
        this.userfield11Option = setLongOpt(parser, OPT_USERFIELD11_LONG, "stringvalue", "Userfield11", true, false, true);
        this.userfield12Option = setLongOpt(parser, OPT_USERFIELD12_LONG, "stringvalue", "Userfield12", true, false, true);
        this.userfield13Option = setLongOpt(parser, OPT_USERFIELD13_LONG, "stringvalue", "Userfield13", true, false, true);
        this.userfield14Option = setLongOpt(parser, OPT_USERFIELD14_LONG, "stringvalue", "Userfield14", true, false, true);
        this.userfield15Option = setLongOpt(parser, OPT_USERFIELD15_LONG, "stringvalue", "Userfield15", true, false, true);
        this.userfield16Option = setLongOpt(parser, OPT_USERFIELD16_LONG, "stringvalue", "Userfield16", true, false, true);
        this.userfield17Option = setLongOpt(parser, OPT_USERFIELD17_LONG, "stringvalue", "Userfield17", true, false, true);
        this.userfield18Option = setLongOpt(parser, OPT_USERFIELD18_LONG, "stringvalue", "Userfield18", true, false, true);
        this.userfield19Option = setLongOpt(parser, OPT_USERFIELD19_LONG, "stringvalue", "Userfield19", true, false, true);
        this.userfield20Option = setLongOpt(parser, OPT_USERFIELD20_LONG, "stringvalue", "Userfield20", true, false, true);
        this.city_businessOption = setLongOpt(parser, OPT_CITY_BUSINESS_LONG, "stringvalue", "City_business", true, false, true);
        this.country_businessOption = setLongOpt(parser, OPT_COUNTRY_BUSINESS_LONG, "stringvalue", "Country_business", true, false, true);
        this.assistant_nameOption = setLongOpt(parser, OPT_ASSISTANT_NAME_LONG, "stringvalue", "Assistant_name", true, false, true);
        this.telephone_primaryOption = setLongOpt(parser, OPT_TELEPHONE_PRIMARY_LONG, "stringvalue", "Telephone_primary", true, false, true);
        this.categoriesOption = setLongOpt(parser, OPT_CATEGORIES_LONG, "stringvalue", "Categories", true, false, true);
        this.passwordmechOption = setLongOpt(parser, OPT_PASSWORDMECH_LONG, "stringvalue", "PasswordMech", true, false, true);
        this.mail_folder_confirmed_ham_nameOption = setLongOpt(parser, OPT_MAIL_FOLDER_CONFIRMED_HAM_NAME_LONG, "stringvalue", "Mail_folder_confirmed_ham_name", true, false, true);
        this.mail_folder_confirmed_spam_nameOption = setLongOpt(parser, OPT_MAIL_FOLDER_CONFIRMED_SPAM_NAME_LONG, "stringvalue", "Mail_folder_confirmed_spam_name", true, false, true);
        this.defaultsenderaddressOption = setLongOpt(parser, OPT_DEFAULTSENDERADDRESS_LONG, "stringvalue", "DefaultSenderAddress", true, false, true);
        this.foldertreeOption = setIntegerLongOpt(parser, OPT_FOLDERTREE_LONG, "intvalue", "FolderTree", true, false, true);

        setGui_Spam_option(parser);
        setModuleAccessOptions(parser);
    }

    protected final void setGui_Spam_option(final AdminParser admp){
        this.spamFilterOption =  setSettableBooleanLongOpt(admp, OPT_GUI_LONG, "true / false", "GUI_Spam_filter_capabilities_enabled", true, false, true);
    }

    protected final void setCsvImport(final AdminParser admp) {
        admp.setCsvImportOption(setLongOpt(admp, OPT_CSV_IMPORT, "CSV file","Full path to CSV file with user data to import. This option makes \r\n" +
            "                                                   mandatory command line options obsolete, except credential options (if\r\n" +
            "                                                   needed). But they have to be set in the CSV file.", true, false, false));
    }

    /**
     * This method goes through the dynamically created options, and sets the corresponding values
     * in the user object.
     *
     * Attention the user object given as parameter is changed
     *
     * @param parser
     * @param usr
     * @throws IllegalArgumentException
     * @throws InvalidDataException
     */
    protected final void applyExtendedOptionsToUser(final AdminParser parser, final User usr) throws IllegalArgumentException, InvalidDataException {

        String addguival    = (String)parser.getOptionValue(this.addGUISettingOption);
        if( addguival != null ) {
            addguival = addguival.trim();
            if( addguival.length() == 0 ) {
                throw new InvalidDataException("Argument for " + OPT_ADD_GUI_SETTING_LONG + "is wrong (empty value)");
            }
            if( addguival.indexOf('=') < 0 ) {
                throw new InvalidDataException("Argument for " + OPT_ADD_GUI_SETTING_LONG + "is wrong (not key = value)");
            }
            final int idx = addguival.indexOf('=');
            final String key = addguival.substring(0, idx).trim();
            final String val = addguival.substring(idx+1, addguival.length()).trim();
            if(key.length() == 0 || val.length() == 0) {
                throw new InvalidDataException("Argument for " + OPT_ADD_GUI_SETTING_LONG + "is wrong (key or val empty)");
            }
            usr.addGuiPreferences(key, val);
        }
        if( this.getClass().getName().endsWith("Change") ) {
            String removeguival = (String)parser.getOptionValue(this.removeGUISettingOption);
            if( removeguival != null ) {
                removeguival = removeguival.trim();
                if( removeguival.length() == 0 ) {
                    throw new InvalidDataException("Argument for " + OPT_REMOVE_GUI_SETTING_LONG + "is wrong (empty value)");
                }
                usr.removeGuiPreferences(removeguival);
            }
        }

        {
            Boolean spamfilter = (Boolean)parser.getOptionValue(this.spamFilterOption);
            if (null != spamfilter) {
                usr.setGui_spam_filter_enabled(spamfilter);
            }
        }

        {
            String value = (String)parser.getOptionValue(email1Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if (value.length() == 0) {
                    value = null;
                }
                usr.setEmail1(value);
            }
        }
        {
            final Boolean value = (Boolean)parser.getOptionValue(mailenabledOption);
            if (null != value) {
                usr.setMailenabled(value);
            }
        }
        {
            final SimpleDateFormat sdf = new SimpleDateFormat(COMMANDLINE_DATEFORMAT);
            sdf.setTimeZone(TimeZone.getTimeZone(COMMANDLINE_TIMEZONE));
            try {
                final String date = (String) parser.getOptionValue(birthdayOption);
                if (null != date) {
                    if ("null".equalsIgnoreCase(date) || 0 == date.length()) {
                        usr.setBirthday(null);
                    } else {
                        usr.setBirthday(sdf.parse(date));
                    }
                }
            } catch (final ParseException e) {
                throw new InvalidDataException("Wrong dateformat, use \"" + sdf.toPattern() + "\"", e);
            }
        }
        {
            final SimpleDateFormat sdf = new SimpleDateFormat(COMMANDLINE_DATEFORMAT);
            sdf.setTimeZone(TimeZone.getTimeZone(COMMANDLINE_TIMEZONE));
            try {
                final String date = (String)parser.getOptionValue(anniversaryOption);
                if( date != null ) {
                    final Date value = sdf.parse(date);
                    if (null != value) {
                        usr.setAnniversary(value);
                    }
                }
            } catch (final ParseException e) {
                throw new InvalidDataException("Wrong dateformat, use \"" + sdf.toPattern() + "\"");
            }
        }
        {
            String value = (String)parser.getOptionValue(branchesOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setBranches(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(business_categoryOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setBusiness_category(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(postal_code_businessOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setPostal_code_business(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(state_businessOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setState_business(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(street_businessOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setStreet_business(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(telephone_callbackOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setTelephone_callback(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(city_homeOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setCity_home(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(commercial_registerOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setCommercial_register(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(country_homeOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setCountry_home(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(email2Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setEmail2(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(email3Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setEmail3(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(employeetypeOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setEmployeeType(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(fax_businessOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setFax_business(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(fax_homeOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setFax_home(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(fax_otherOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setFax_other(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(imapserverOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setImapServer(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(imaploginOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setImapLogin(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(smtpserverOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setSmtpServer(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(instant_messenger1Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setInstant_messenger1(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(instant_messenger2Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setInstant_messenger2(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(telephone_ipOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setTelephone_ip(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(telephone_isdnOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setTelephone_isdn(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(mail_folder_drafts_nameOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setMail_folder_drafts_name(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(mail_folder_sent_nameOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setMail_folder_sent_name(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(mail_folder_spam_nameOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setMail_folder_spam_name(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(mail_folder_trash_nameOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setMail_folder_trash_name(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(mail_folder_archive_full_nameOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setMail_folder_archive_full_name(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(manager_nameOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setManager_name(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(marital_statusOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setMarital_status(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(cellular_telephone1Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setCellular_telephone1(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(cellular_telephone2Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setCellular_telephone2(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(infoOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setInfo(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(nicknameOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setNickname(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(number_of_childrenOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setNumber_of_children(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(noteOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setNote(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(number_of_employeeOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setNumber_of_employee(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(telephone_pagerOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setTelephone_pager(value);
            }
        }
        {
            final Boolean value = (Boolean)parser.getOptionValue(password_expiredOption);
            if (null != value) {
                usr.setPassword_expired(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(telephone_assistantOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setTelephone_assistant(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(telephone_business1Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setTelephone_business1(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(telephone_business2Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setTelephone_business2(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(telephone_carOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setTelephone_car(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(telephone_companyOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setTelephone_company(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(telephone_home1Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setTelephone_home1(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(telephone_home2Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setTelephone_home2(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(telephone_otherOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setTelephone_other(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(postal_code_homeOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setPostal_code_home(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(professionOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setProfession(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(telephone_radioOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setTelephone_radio(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(room_numberOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setRoom_number(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(sales_volumeOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setSales_volume(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(city_otherOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setCity_other(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(country_otherOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setCountry_other(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(middle_nameOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setMiddle_name(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(postal_code_otherOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setPostal_code_other(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(state_otherOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setState_other(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(street_otherOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setStreet_other(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(spouse_nameOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setSpouse_name(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(state_homeOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setState_home(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(street_homeOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setStreet_home(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(suffixOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setSuffix(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(tax_idOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setTax_id(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(telephone_telexOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setTelephone_telex(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(telephone_ttytddOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setTelephone_ttytdd(value);
            }
        }
        {
            final Integer value = (Integer)parser.getOptionValue(uploadfilesizelimitOption);
            if (null != value) {
                usr.setUploadFileSizeLimit(value);
            }
        }
        {
            final Integer value = (Integer)parser.getOptionValue(uploadfilesizelimitperfileOption);
            if (null != value) {
                usr.setUploadFileSizeLimitPerFile(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(urlOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setUrl(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(userfield01Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setUserfield01(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(userfield02Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setUserfield02(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(userfield03Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setUserfield03(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(userfield04Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setUserfield04(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(userfield05Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setUserfield05(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(userfield06Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setUserfield06(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(userfield07Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setUserfield07(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(userfield08Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setUserfield08(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(userfield09Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setUserfield09(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(userfield10Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setUserfield10(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(userfield11Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setUserfield11(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(userfield12Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setUserfield12(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(userfield13Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setUserfield13(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(userfield14Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setUserfield14(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(userfield15Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setUserfield15(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(userfield16Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setUserfield16(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(userfield17Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setUserfield17(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(userfield18Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setUserfield18(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(userfield19Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setUserfield19(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(userfield20Option);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setUserfield20(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(city_businessOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setCity_business(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(assistant_nameOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setAssistant_name(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(telephone_primaryOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setTelephone_primary(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(categoriesOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setCategories(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(passwordmechOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setPasswordMech(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(mail_folder_confirmed_ham_nameOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setMail_folder_confirmed_ham_name(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(mail_folder_confirmed_spam_nameOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setMail_folder_confirmed_spam_name(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(defaultsenderaddressOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setDefaultSenderAddress(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(country_businessOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setCountry_business(value);
            }
        }
        {
            final Integer value = (Integer)parser.getOptionValue(foldertreeOption);
            if (null != value) {
                usr.setFolderTree(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(titleOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setTitle(value);
            }
        }
        {
            String value = (String)parser.getOptionValue(positionOption);
            if (null != value) {
                // On the command line an empty string can be used to clear that specific attribute.
                if ("".equals(value)) { value = null; }
                usr.setPosition(value);
            }
        }

        //        for (final OptionAndMethod optionAndMethod : optionsandmethods) {
        //            if (optionAndMethod.getReturntype().equals(JAVA_LANG_STRING)) {
        //                String value = (String)parser.getOptionValue(optionAndMethod.getOption());
        //                if (null != value) {
        //                    // On the command line an empty string can be used to clear that specific attribute.
        //                    if ("".equals(value)) { value = null; }
        //                    optionAndMethod.getMethod().invoke(usr, value);
        //                }
        //            } else if (optionAndMethod.getReturntype().equals(JAVA_LANG_INTEGER)) {
        //                final Integer value = (Integer)parser.getOptionValue(optionAndMethod.getOption());
        //                if (null != value) {
        //                    optionAndMethod.getMethod().invoke(usr, value);
        //                }
        //            } else if (optionAndMethod.getReturntype().equals(JAVA_LANG_BOOLEAN)) {
        //                final Boolean value = (Boolean)parser.getOptionValue(optionAndMethod.getOption());
        //                if (null != value) {
        //                    optionAndMethod.getMethod().invoke(usr, value);
        //                }
        //            } else if (optionAndMethod.getReturntype().equals(JAVA_UTIL_DATE)) {
        //                final SimpleDateFormat sdf = new SimpleDateFormat(COMMANDLINE_DATEFORMAT);
        //                sdf.setTimeZone(TimeZone.getTimeZone(COMMANDLINE_TIMEZONE));
        //                try {
        //                    final String date = (String)parser.getOptionValue(optionAndMethod.getOption());
        //                    if( date != null ) {
        //                        final Date value = sdf.parse(date);
        //                        if (null != value) {
        //                            optionAndMethod.getMethod().invoke(usr, value);
        //                        }
        //                    }
        //                } catch (final ParseException e) {
        //                    throw new InvalidDataException("Wrong dateformat, use \"" + sdf.toPattern() + "\"");
        //                }
        //            } else if (optionAndMethod.getReturntype().equals(JAVA_UTIL_HASH_SET)) {
        //                final HashSet<?> value = (HashSet<?>)parser.getOptionValue(optionAndMethod.getOption());
        //                if (null != value) {
        //                    optionAndMethod.getMethod().invoke(usr, value);
        //                }
        //            }
        //        }
    }

    protected void applyDynamicOptionsToUser(final AdminParser parser, final User usr) {
        final Map<String, Map<String, String>> dynamicArguments = parser.getDynamicArguments();
        for(final Map.Entry<String, Map<String, String>> namespaced : dynamicArguments.entrySet()) {
            final String namespace = namespaced.getKey();
            for(final Map.Entry<String, String> pair : namespaced.getValue().entrySet()) {
                final String name = pair.getKey();
                final String value = pair.getValue();

                usr.setUserAttribute(namespace, name, value);
            }
        }
    }


    protected final OXUserInterface getUserInterface() throws NotBoundException, MalformedURLException, RemoteException {
        return (OXUserInterface) Naming.lookup(RMI_HOSTNAME + OXUserInterface.RMI_NAME);
    }

    @Override
    protected String getObjectName() {
        return "user";
    }

    protected void parseAndSetUserId(final AdminParser parser, final User usr) {
        final String optionValue = (String) parser.getOptionValue(this.idOption);
        if (null != optionValue) {
            userid = Integer.valueOf(optionValue);
            usr.setId(userid);
        }
    }

    protected CSVConstants getConstantFromString(final String string) {
        return this.constantsMap.get(string);
    }

    /**
     * Checks if required columns are set
     *
     * @param idarray
     * @throws InvalidDataException
     */
    protected void checkRequired(final int[] idarray) throws InvalidDataException {
        for (final Constants value : Constants.values()) {
            if (value.isRequired()) {
                if (-1 == idarray[value.getIndex()]) {
                    throw new InvalidDataException("The required column \"" + value.getString() + "\" is missing");
                }
            }
        }
        for (final AccessCombinations value : AccessCombinations.values()) {
            if (value.isRequired()) {
                if (-1 == idarray[value.getIndex()]) {
                    throw new InvalidDataException("The required column \"" + value.getString() + "\" is missing");
                }
            }
        }
    }

    protected final int[] csvParsingCommon(final CSVReader reader) throws IOException, InvalidDataException {
        prepareConstantsMap();

        int[] idarray = new int[this.constantsMap.size()];
        for (int i = idarray.length; i-- > 0;) {
            idarray[i] = -1;
        }

        // First read the column names, we will use them later on like the parameter names for the clts
        String [] nextLine = reader.readNext();
        if (null == nextLine) {
            throw new InvalidDataException("no columnnames found");
        }

        for (int i = 0; i < nextLine.length; i++) {
            final CSVConstants constant = getConstantFromString(nextLine[i]);
            if (null != constant) {
                idarray[constant.getIndex()] = i;
            }
        }

        checkRequired(idarray);
        return idarray;
    }
}
