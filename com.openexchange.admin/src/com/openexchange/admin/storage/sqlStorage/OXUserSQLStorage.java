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

package com.openexchange.admin.storage.sqlStorage;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import com.openexchange.admin.storage.interfaces.OXUserStorageInterface;

/**
 * This class implements the global storage interface and creates a layer between the abstract storage definition and a storage in a SQL
 * accessible database
 *
 * @author d7
 * @author cutmasta
 */
public abstract class OXUserSQLStorage extends OXUserStorageInterface {

    /**
     * This class provides a mapper which maps the name of the field in the user object to the name of the field in the database and the
     * other way around
     */
    protected static class Mapper {

        public final static String PASSWORD_EXPIRED = "Password_expired";

        public final static Hashtable<String, String> method2field = new Hashtable<String, String>(99);

        public final static Hashtable<String, String> field2method = new Hashtable<String, String>(99);

        public final static HashSet<String> notallowed = new HashSet<String>(9);

        static {
            // Define all those fields which are contained in the user table
            notallowed.add("Id");
            notallowed.add("Password");
            notallowed.add("PasswordMech");
            notallowed.add("PrimaryEmail");
            notallowed.add("Timezone");
            notallowed.add("Mailenabled");
            notallowed.add("ImapServer");
            notallowed.add("ImapLogin");
            notallowed.add("SmtpServer");
            notallowed.add(PASSWORD_EXPIRED);
            notallowed.add("Language");
            notallowed.add("MaxQuota");
            notallowed.add("Filestore_name");
            notallowed.add("FilestoreOwner");
            notallowed.add("FilestoreId");

            // For the user table
            method2field.put("Id", "id");
            method2field.put("PrimaryEmail", "mail");
            method2field.put("Language", "preferredlanguage");
            method2field.put("Timezone", "timezone");
            method2field.put("Mailenabled", "mailEnabled");
            method2field.put(PASSWORD_EXPIRED, "shadowLastChange");
            method2field.put("ImapServer", "imapserver");
            method2field.put("ImapLogin", "imapLogin");
            method2field.put("SmtpServer", "smtpserver");
            method2field.put("PasswordMech", "passwordMech");
            method2field.put("MaxQuota", "quota_max");
            method2field.put("Filestore_name", "filestore_name");
            method2field.put("FilestoreOwner", "filestore_owner");
            method2field.put("FilestoreId", "filestore_id");

            method2field.put("Display_name", "field01");
            method2field.put("Sur_name", "field02");
            method2field.put("Given_name", "field03");
            method2field.put("Middle_name", "field04");
            method2field.put("Suffix", "field05");
            method2field.put("Title", "field06");
            method2field.put("Street_home", "field07");
            method2field.put("Postal_code_home", "field08");
            method2field.put("City_home", "field09");
            method2field.put("State_home", "field10");
            method2field.put("Country_home", "field11");
            method2field.put("Marital_status", "field12");
            method2field.put("Number_of_children", "field13");
            method2field.put("Profession", "field14");
            method2field.put("Nickname", "field15");
            method2field.put("Spouse_name", "field16");
            method2field.put("Note", "field17");
            method2field.put("Company", "field18");
            method2field.put("Department", "field19");
            method2field.put("Position", "field20");
            method2field.put("EmployeeType", "field21");
            method2field.put("Room_number", "field22");
            method2field.put("Street_business", "field23");
            method2field.put("Postal_code_business", "field24");
            method2field.put("City_business", "field25");
            method2field.put("State_business", "field26");
            method2field.put("Country_business", "field27");
            method2field.put("Number_of_employee", "field28");
            method2field.put("Sales_volume", "field29");
            method2field.put("Tax_id", "field30");
            method2field.put("Commercial_register", "field31");
            method2field.put("Branches", "field32");
            method2field.put("Business_category", "field33");
            method2field.put("Info", "field34");
            method2field.put("Manager_name", "field35");
            method2field.put("Assistant_name", "field36");
            method2field.put("Street_other", "field37");
            method2field.put("Postal_code_other", "field38");
            method2field.put("City_other", "field39");
            method2field.put("State_other", "field40");
            method2field.put("Country_other", "field41");
            method2field.put("Telephone_assistant", "field42");
            method2field.put("Telephone_business1", "field43");
            method2field.put("Telephone_business2", "field44");
            method2field.put("Fax_business", "field45");
            method2field.put("Telephone_callback", "field46");
            method2field.put("Telephone_car", "field47");
            method2field.put("Telephone_company", "field48");
            method2field.put("Telephone_home1", "field49");
            method2field.put("Telephone_home2", "field50");
            method2field.put("Fax_home", "field51");
            method2field.put("Telephone_isdn", "field52");
            method2field.put("Cellular_telephone1", "field53");
            method2field.put("Cellular_telephone2", "field54");
            method2field.put("Telephone_other", "field55");
            method2field.put("Fax_other", "field56");
            method2field.put("Telephone_pager", "field57");
            method2field.put("Telephone_primary", "field58");
            method2field.put("Telephone_radio", "field59");
            method2field.put("Telephone_telex", "field60");
            method2field.put("Telephone_ttytdd", "field61");
            method2field.put("Instant_messenger1", "field62");
            method2field.put("Instant_messenger2", "field63");
            method2field.put("Telephone_ip", "field64");
            method2field.put("Email1", "field65");
            method2field.put("Email2", "field66");
            method2field.put("Email3", "field67");
            method2field.put("Url", "field68");
            method2field.put("Categories", "field69");
            method2field.put("Userfield01", "field70");
            method2field.put("Userfield02", "field71");
            method2field.put("Userfield03", "field72");
            method2field.put("Userfield04", "field73");
            method2field.put("Userfield05", "field74");
            method2field.put("Userfield06", "field75");
            method2field.put("Userfield07", "field76");
            method2field.put("Userfield08", "field77");
            method2field.put("Userfield09", "field78");
            method2field.put("Userfield10", "field79");
            method2field.put("Userfield11", "field80");
            method2field.put("Userfield12", "field81");
            method2field.put("Userfield13", "field82");
            method2field.put("Userfield14", "field83");
            method2field.put("Userfield15", "field84");
            method2field.put("Userfield16", "field85");
            method2field.put("Userfield17", "field86");
            method2field.put("Userfield18", "field87");
            method2field.put("Userfield19", "field88");
            method2field.put("Userfield20", "field89");
            method2field.put("Birthday", "timestampfield01");
            method2field.put("Anniversary", "timestampfield02");

            // For the user table
            final Enumeration<String> keys = method2field.keys();
            final Enumeration<String> values = method2field.elements();

            while (keys.hasMoreElements()) {
                final String key = keys.nextElement();
                final String value = values.nextElement();
                field2method.put(value, key);
            }
        }
    }
}
