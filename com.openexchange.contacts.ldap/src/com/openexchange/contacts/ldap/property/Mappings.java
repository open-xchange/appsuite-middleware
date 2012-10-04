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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.contacts.ldap.property;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.openexchange.contacts.ldap.exceptions.LdapConfigurationExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;

/**
 * This class contains the mapping properties, specifying what LDAP attributes
 * correspond to which groupware attributes
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class Mappings {

    private enum Parameters {
        anniversary("anniversary"),
        assistant_name("assistant_name"),
        birthday("birthday"),
        branches("branches"),
        business_category("business_category"),
        categories("categories"),
        cellular_telephone1("cellular_telephone1"),
        cellular_telephone2("cellular_telephone2"),
        city_business("city_business"),
        city_home("city_home"),
        city_other("city_other"),
        commercial_register("commercial_register"),
        company("company"),
        country_business("country_business"),
        country_home("country_home"),
        country_other("country_other"),
        creationdate("creationdate"),
        defaultaddress("defaultaddress"),
        department("department"),
        displayname("displayname"),
        distributionlistname("distributionlistname"),
        distributionuid("distributionuid"),
        email1("email1"),
        email2("email2"),
        email3("email3"),
        employeetype("employeetype"),
        fax_business("fax_business"),
        fax_home("fax_home"),
        fax_other("fax_other"),
        givenname("givenname"),
        info("info"),
        instant_messenger1("instant_messenger1"),
        instant_messenger2("instant_messenger2"),
        lastmodified("lastmodified"),
        manager_name("manager_name"),
        marital_status("marital_status"),
        middle_name("middle_name"),
        nickname("nickname"),
        note("note"),
        number_of_children("number_of_children"),
        number_of_employee("number_of_employee"),
        position("position"),
        postal_code_business("postal_code_business"),
        postal_code_home("postal_code_home"),
        postal_code_other("postal_code_other"),
        profession("profession"),
        room_number("room_number"),
        sales_volume("sales_volume"),
        spouse_name("spouse_name"),
        state_business("state_business"),
        state_home("state_home"),
        state_other("state_other"),
        street_business("street_business"),
        street_home("street_home"),
        street_other("street_other"),
        suffix("suffix"),
        surname("surname"),
        tax_id("tax_id"),
        telephone_assistant("telephone_assistant"),
        telephone_business1("telephone_business1"),
        telephone_business2("telephone_business2"),
        telephone_callback("telephone_callback"),
        telephone_car("telephone_car"),
        telephone_company("telephone_company"),
        telephone_home1("telephone_home1"),
        telephone_home2("telephone_home2"),
        telephone_ip("telephone_ip"),
        telephone_isdn("telephone_isdn"),
        telephone_other("telephone_other"),
        telephone_pager("telephone_pager"),
        telephone_primary("telephone_primary"),
        telephone_radio("telephone_radio"),
        telephone_telex("telephone_telex"),
        telephone_ttytdd("telephone_ttytdd"),
        title("title"),
        // Here we begin with the mapping entries
        uniqueid("uniqueid"),
        url("url"),
        userfield01("userfield01"),
        userfield02("userfield02"),
        userfield03("userfield03"),
        userfield04("userfield04"),
        userfield05("userfield05"),
        userfield06("userfield06"),
        userfield07("userfield07"),
        userfield08("userfield08"),
        userfield09("userfield09"),
        userfield10("userfield10"),
        userfield11("userfield11"),
        userfield12("userfield12"),
        userfield13("userfield13"),
        userfield14("userfield14"),
        userfield15("userfield15"),
        userfield16("userfield16"),
        userfield17("userfield17"),
        userfield18("userfield18"),
        userfield19("userfield19"),
        userfield20("userfield20");

        private final String name;

        private static volatile String prefix;

        private Parameters(final String name) {
            this.name = name;
        }

        public String getName() {
            return prefix + '.' + name;
        }

        public static void setPrefix(final String pref) {
            prefix = pref;
        }
    }

    private final Map<ContactField, String> ox2ldap;

    private String anniversary;
    private String assistant_name;
    private String birthday;
    private String branches;
    private String business_category;
    private String categories;
    private String cellular_telephone1;
    private String cellular_telephone2;
    private String city_business;
    private String city_home;
    private String city_other;
    private String commercial_register;
    private String company;
    private String country_business;
    private String country_home;
    private String country_other;
    private String creationdate;
    private String defaultaddress;
    private String department;
    private String displayname;
    private String distributionlistname;
    private String distributionuid;
    private String email1;
    private String email2;
    private String email3;
    private String employeetype;
    private String fax_business;
    private String fax_home;
    private String fax_other;
    private String givenname;
    private String info;
    private String instant_messenger1;
    private String instant_messenger2;
    private String lastmodified;
    private String manager_name;
    private String marital_status;
    private String middle_name;
    private String nickname;
    private String note;
    private String number_of_children;
    private String number_of_employee;
    private String position;
    private String postal_code_business;
    private String postal_code_home;
    private String postal_code_other;
    private String profession;
    private String room_number;
    private String sales_volume;
    private String spouse_name;
    private String state_business;
    private String state_home;
    private String state_other;
    private String street_business;
    private String street_home;
    private String street_other;
    private String suffix;
    private String surname;
    private String tax_id;
    private String telephone_assistant;
    private String telephone_business1;
    private String telephone_business2;
    private String telephone_callback;
    private String telephone_car;
    private String telephone_company;
    private String telephone_home1;
    private String telephone_home2;
    private String telephone_ip;
    private String telephone_isdn;
    private String telephone_other;
    private String telephone_pager;
    private String telephone_primary;
    private String telephone_radio;
    private String telephone_telex;
    private String telephone_ttytdd;
    private String title;
    private String uniqueid;
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

    public static Mappings getMappingsFromProperties(final Properties props, final String prefix, final String mappingfile) throws OXException {
        final Mappings retval = new Mappings(props, prefix, mappingfile);
        Parameters.setPrefix(prefix);

        final CheckStringPropertyParameter parameterObject = new CheckStringPropertyParameter(props, mappingfile);


        // TODO: Differentiate between optional and non-optional settings
        retval.setDisplayname(checkStringPropertyOptional(parameterObject, Parameters.displayname));

        retval.setGivenname(checkStringPropertyOptional(parameterObject, Parameters.givenname));

        retval.setSurname(checkStringPropertyOptional(parameterObject, Parameters.surname));

        retval.setEmail1(checkStringPropertyOptional(parameterObject, Parameters.email1));

        retval.setDepartment(checkStringPropertyOptional(parameterObject, Parameters.department));

        retval.setCompany(checkStringPropertyOptional(parameterObject, Parameters.company));

        retval.setDistributionuid(checkStringProperty(parameterObject, Parameters.distributionuid));

        retval.setBirthday(checkStringPropertyOptional(parameterObject, Parameters.birthday));

        retval.setAnniversary(checkStringPropertyOptional(parameterObject, Parameters.anniversary));

        retval.setBranches(checkStringPropertyOptional(parameterObject, Parameters.branches));

        retval.setBusiness_category(checkStringPropertyOptional(parameterObject, Parameters.business_category));

        retval.setPostal_code_business(checkStringPropertyOptional(parameterObject, Parameters.postal_code_business));

        retval.setState_business(checkStringPropertyOptional(parameterObject, Parameters.state_business));

        retval.setStreet_business(checkStringPropertyOptional(parameterObject, Parameters.street_business));

        retval.setTelephone_callback(checkStringPropertyOptional(parameterObject, Parameters.telephone_callback));

        retval.setCity_home(checkStringPropertyOptional(parameterObject, Parameters.city_home));

        retval.setCommercial_register(checkStringPropertyOptional(parameterObject, Parameters.commercial_register));

        retval.setCountry_home(checkStringPropertyOptional(parameterObject, Parameters.country_home));

        retval.setEmail2(checkStringPropertyOptional(parameterObject, Parameters.email2));

        retval.setEmail3(checkStringPropertyOptional(parameterObject, Parameters.email3));

        retval.setEmployeetype(checkStringPropertyOptional(parameterObject, Parameters.employeetype));

        retval.setFax_business(checkStringPropertyOptional(parameterObject, Parameters.fax_business));

        retval.setFax_home(checkStringPropertyOptional(parameterObject, Parameters.fax_home));

        retval.setFax_other(checkStringPropertyOptional(parameterObject, Parameters.fax_other));

        retval.setInstant_messenger1(checkStringPropertyOptional(parameterObject, Parameters.instant_messenger1));

        retval.setInstant_messenger2(checkStringPropertyOptional(parameterObject, Parameters.instant_messenger2));

        retval.setTelephone_ip(checkStringPropertyOptional(parameterObject, Parameters.telephone_ip));

        retval.setTelephone_isdn(checkStringPropertyOptional(parameterObject, Parameters.telephone_isdn));

        retval.setManager_name(checkStringPropertyOptional(parameterObject, Parameters.manager_name));

        retval.setMarital_status(checkStringPropertyOptional(parameterObject, Parameters.marital_status));

        retval.setCellular_telephone1(checkStringPropertyOptional(parameterObject, Parameters.cellular_telephone1));

        retval.setCellular_telephone2(checkStringPropertyOptional(parameterObject, Parameters.cellular_telephone2));

        retval.setInfo(checkStringPropertyOptional(parameterObject, Parameters.info));

        retval.setNickname(checkStringPropertyOptional(parameterObject, Parameters.nickname));

        retval.setNumber_of_children(checkStringPropertyOptional(parameterObject, Parameters.number_of_children));

        retval.setNote(checkStringPropertyOptional(parameterObject, Parameters.note));

        retval.setNumber_of_employee(checkStringPropertyOptional(parameterObject, Parameters.number_of_employee));

        retval.setTelephone_pager(checkStringPropertyOptional(parameterObject, Parameters.telephone_pager));

        retval.setTelephone_assistant(checkStringPropertyOptional(parameterObject, Parameters.telephone_assistant));

        retval.setTelephone_business1(checkStringPropertyOptional(parameterObject, Parameters.telephone_business1));

        retval.setTelephone_business2(checkStringPropertyOptional(parameterObject, Parameters.telephone_business2));

        retval.setTelephone_car(checkStringPropertyOptional(parameterObject, Parameters.telephone_car));

        retval.setTelephone_company(checkStringPropertyOptional(parameterObject, Parameters.telephone_company));

        retval.setTelephone_home1(checkStringPropertyOptional(parameterObject, Parameters.telephone_home1));

        retval.setTelephone_home2(checkStringPropertyOptional(parameterObject, Parameters.telephone_home2));

        retval.setTelephone_other(checkStringPropertyOptional(parameterObject, Parameters.telephone_other));

        retval.setPostal_code_home(checkStringPropertyOptional(parameterObject, Parameters.postal_code_home));

        retval.setProfession(checkStringPropertyOptional(parameterObject, Parameters.profession));

        retval.setTelephone_radio(checkStringPropertyOptional(parameterObject, Parameters.telephone_radio));

        retval.setRoom_number(checkStringPropertyOptional(parameterObject, Parameters.room_number));

        retval.setSales_volume(checkStringPropertyOptional(parameterObject, Parameters.sales_volume));

        retval.setCity_other(checkStringPropertyOptional(parameterObject, Parameters.city_other));

        retval.setCountry_other(checkStringPropertyOptional(parameterObject, Parameters.country_other));

        retval.setMiddle_name(checkStringPropertyOptional(parameterObject, Parameters.middle_name));

        retval.setPostal_code_other(checkStringPropertyOptional(parameterObject, Parameters.postal_code_other));

        retval.setState_other(checkStringPropertyOptional(parameterObject, Parameters.state_other));

        retval.setStreet_other(checkStringPropertyOptional(parameterObject, Parameters.street_other));

        retval.setSpouse_name(checkStringPropertyOptional(parameterObject, Parameters.spouse_name));

        retval.setState_home(checkStringPropertyOptional(parameterObject, Parameters.state_home));

        retval.setStreet_home(checkStringPropertyOptional(parameterObject, Parameters.street_home));

        retval.setSuffix(checkStringPropertyOptional(parameterObject, Parameters.suffix));

        retval.setTax_id(checkStringPropertyOptional(parameterObject, Parameters.tax_id));

        retval.setTelephone_telex(checkStringPropertyOptional(parameterObject, Parameters.telephone_telex));

        retval.setTelephone_ttytdd(checkStringPropertyOptional(parameterObject, Parameters.telephone_ttytdd));

        retval.setUrl(checkStringPropertyOptional(parameterObject, Parameters.url));

        retval.setUserfield01(checkStringPropertyOptional(parameterObject, Parameters.userfield01));

        retval.setUserfield02(checkStringPropertyOptional(parameterObject, Parameters.userfield02));

        retval.setUserfield03(checkStringPropertyOptional(parameterObject, Parameters.userfield03));

        retval.setUserfield04(checkStringPropertyOptional(parameterObject, Parameters.userfield04));

        retval.setUserfield05(checkStringPropertyOptional(parameterObject, Parameters.userfield05));

        retval.setUserfield06(checkStringPropertyOptional(parameterObject, Parameters.userfield06));

        retval.setUserfield07(checkStringPropertyOptional(parameterObject, Parameters.userfield07));

        retval.setUserfield08(checkStringPropertyOptional(parameterObject, Parameters.userfield08));

        retval.setUserfield09(checkStringPropertyOptional(parameterObject, Parameters.userfield09));

        retval.setUserfield10(checkStringPropertyOptional(parameterObject, Parameters.userfield10));

        retval.setUserfield10(checkStringPropertyOptional(parameterObject, Parameters.userfield10));

        retval.setUserfield11(checkStringPropertyOptional(parameterObject, Parameters.userfield11));

        retval.setUserfield12(checkStringPropertyOptional(parameterObject, Parameters.userfield12));

        retval.setUserfield13(checkStringPropertyOptional(parameterObject, Parameters.userfield13));

        retval.setUserfield14(checkStringPropertyOptional(parameterObject, Parameters.userfield14));

        retval.setUserfield15(checkStringPropertyOptional(parameterObject, Parameters.userfield15));

        retval.setUserfield16(checkStringPropertyOptional(parameterObject, Parameters.userfield16));

        retval.setUserfield17(checkStringPropertyOptional(parameterObject, Parameters.userfield17));

        retval.setUserfield18(checkStringPropertyOptional(parameterObject, Parameters.userfield18));

        retval.setUserfield19(checkStringPropertyOptional(parameterObject, Parameters.userfield19));

        retval.setUserfield20(checkStringPropertyOptional(parameterObject, Parameters.userfield20));

        retval.setCity_business(checkStringPropertyOptional(parameterObject, Parameters.city_business));

        retval.setCountry_business(checkStringPropertyOptional(parameterObject, Parameters.country_business));

        retval.setAssistant_name(checkStringPropertyOptional(parameterObject, Parameters.assistant_name));

        retval.setTelephone_primary(checkStringPropertyOptional(parameterObject, Parameters.telephone_primary));

        retval.setCategories(checkStringPropertyOptional(parameterObject, Parameters.categories));

        retval.setDefaultaddress(checkStringPropertyOptional(parameterObject, Parameters.defaultaddress));

        retval.setTitle(checkStringPropertyOptional(parameterObject, Parameters.title));

        retval.setPosition(checkStringPropertyOptional(parameterObject, Parameters.position));

        retval.setLastmodified(checkStringPropertyOptional(parameterObject, Parameters.lastmodified));

        retval.setCreationdate(checkStringPropertyOptional(parameterObject, Parameters.creationdate));

        retval.setUniqueid(checkStringPropertyOptional(parameterObject, Parameters.uniqueid));

        return retval;
    }



    public static class CheckStringPropertyParameter {

        private final Properties m_props;

        private final String m_mappingfile;

        public CheckStringPropertyParameter(final Properties props, final String mappingfile) {
            m_props = props;
            m_mappingfile = mappingfile;
        }

        public Properties getProps() {
            return m_props;
        }

        public String getMappingfile() {
            return m_mappingfile;
        }
    }


    private static String checkStringPropertyOptional(final CheckStringPropertyParameter parameterObject, final Parameters param) {
        final String name = param.getName();
        final String checkStringProperty = PropertyHandler.checkStringProperty(parameterObject.getProps(), name);
        if (null != checkStringProperty) {
            return checkStringProperty;
        } else {
            return null;
        }
    }

    private static String checkStringProperty(final CheckStringPropertyParameter parameterObject, final Parameters param) throws OXException {
        final String name = param.getName();
        final String checkStringProperty = PropertyHandler.checkStringProperty(parameterObject.getProps(), name);
        if (null != checkStringProperty && 0 != checkStringProperty.length()) {
            return checkStringProperty;
        } else {
            throw LdapConfigurationExceptionCode.PARAMETER_NOT_SET.create(name, parameterObject.getMappingfile());
        }
    }


    private static List<String> NOT_SEARCHABLE = Arrays.asList(
        "defaultaddress",
        "distributionlistname",
        "distributionuid",
        "uniqueid",
        "mail_folder_drafts_name",
        "mail_folder_sent_name",
        "mail_folder_spam_name",
        "mail_folder_trash_name");

    public Mappings(final Properties props, final String prefix, final String mappingfile) throws OXException {
        // Initializes the mapper which is used for searching
        ox2ldap = new HashMap<ContactField, String>();
        final Set<Object> prefixedOxFields = props.keySet();
        for (final Object tmp : prefixedOxFields) {
            final String ldapName = (String) props.get(tmp);
            if (ldapName == null) {
                continue;
            }
            final String[] tmp2 = ((String) tmp).split(prefix.endsWith(".") ? prefix : prefix + ".");
            if (tmp2.length != 2) {
                continue;
            }
            final String oxname = tmp2[1];

            if ("distributionlistname".equals(oxname))
             {
                distributionlistname = ldapName; // difference between OX and LDAP: distri lists and contacts use the same field. Needs
            // special handling.
            }

            if (NOT_SEARCHABLE.contains(oxname)) {
                continue;
            }

            final ContactField field = getBySimilarity(oxname);
            if (field == null) {
                continue;
            }
            ox2ldap.put(field, ldapName);
        }
    }

    public static ContactField getBySimilarity(final String value) {
        final String needle = value.replaceAll("[_\\. ]", "").toLowerCase();
        for (final ContactField field : ContactField.values()) {
            final List<String> haystack = Arrays.asList(new String[] {
                field.getAjaxName().replaceAll("[_\\. ]", "").toLowerCase(),
                field.getReadableName().replaceAll("[_\\. ]", "").toLowerCase(), field.getDbName().replaceAll("[_\\. ]", "").toLowerCase() });
            if (haystack.contains(needle)) {
                return field;
            }
        }
        return null;
    }

    public final String get(final ContactField field) {
        // displayname", "givenname", "surname", "employeetype", "lastmodified", "creationdate"
        if (ox2ldap.containsKey(field)) {
            return ox2ldap.get(field);
        }

        return null;
    }

    public final String getAnniversary() {
        return anniversary;
    }


    public final String getAssistant_name() {
        return assistant_name;
    }


    public final String getBirthday() {
        return birthday;
    }


    public final String getBranches() {
        return branches;
    }


    public final String getBusiness_category() {
        return business_category;
    }


    public final String getCategories() {
        return categories;
    }


    public final String getCellular_telephone1() {
        return cellular_telephone1;
    }


    public final String getCellular_telephone2() {
        return cellular_telephone2;
    }


    public final String getCity_business() {
        return city_business;
    }


    public final String getCity_home() {
        return city_home;
    }


    public final String getCity_other() {
        return city_other;
    }


    public final String getCommercial_register() {
        return commercial_register;
    }


    public final String getCompany() {
        return company;
    }


    public final String getCountry_business() {
        return country_business;
    }


    public final String getCountry_home() {
        return country_home;
    }


    public final String getCountry_other() {
        return country_other;
    }


    public final String getCreationdate() {
        return creationdate;
    }


    public final String getDefaultaddress() {
        return defaultaddress;
    }


    public final String getDepartment() {
        return department;
    }


    public final String getDisplayname() {
        return displayname;
    }

    /**
     * Gets the distributionlistname
     *
     * @return The distributionlistname
     */
    public final String getDistributionlistname() {
        return distributionlistname;
    }

    /**
     * Gets the distributionuid
     *
     * @return The distributionuid
     */
    public final String getDistributionuid() {
        return distributionuid;
    }

    public final String getEmail1() {
        return email1;
    }


    public final String getEmail2() {
        return email2;
    }


    public final String getEmail3() {
        return email3;
    }


    public final String getEmployeetype() {
        return employeetype;
    }


    public final String getFax_business() {
        return fax_business;
    }


    public final String getFax_home() {
        return fax_home;
    }


    public final String getFax_other() {
        return fax_other;
    }


    public final String getGivenname() {
        return givenname;
    }


    public final String getInfo() {
        return info;
    }


    public final String getInstant_messenger1() {
        return instant_messenger1;
    }


    public final String getInstant_messenger2() {
        return instant_messenger2;
    }


    public final String getLastmodified() {
        return lastmodified;
    }


    public final String getManager_name() {
        return manager_name;
    }


    public final String getMarital_status() {
        return marital_status;
    }


    public final String getMiddle_name() {
        return middle_name;
    }


    public final String getNickname() {
        return nickname;
    }


    public final String getNote() {
        return note;
    }


    public final String getNumber_of_children() {
        return number_of_children;
    }


    public final String getNumber_of_employee() {
        return number_of_employee;
    }


    public final String getPosition() {
        return position;
    }


    public final String getPostal_code_business() {
        return postal_code_business;
    }


    public final String getPostal_code_home() {
        return postal_code_home;
    }


    public final String getPostal_code_other() {
        return postal_code_other;
    }


    public final String getProfession() {
        return profession;
    }


    public final String getRoom_number() {
        return room_number;
    }


    public final String getSales_volume() {
        return sales_volume;
    }


    public final String getSpouse_name() {
        return spouse_name;
    }


    public final String getState_business() {
        return state_business;
    }


    public final String getState_home() {
        return state_home;
    }


    public final String getState_other() {
        return state_other;
    }


    public final String getStreet_business() {
        return street_business;
    }


    public final String getStreet_home() {
        return street_home;
    }


    public final String getStreet_other() {
        return street_other;
    }


    public final String getSuffix() {
        return suffix;
    }


    public final String getSurname() {
        return surname;
    }


    public final String getTax_id() {
        return tax_id;
    }


    public final String getTelephone_assistant() {
        return telephone_assistant;
    }


    public final String getTelephone_business1() {
        return telephone_business1;
    }


    public final String getTelephone_business2() {
        return telephone_business2;
    }


    public final String getTelephone_callback() {
        return telephone_callback;
    }


    public final String getTelephone_car() {
        return telephone_car;
    }


    public final String getTelephone_company() {
        return telephone_company;
    }


    public final String getTelephone_home1() {
        return telephone_home1;
    }


    public final String getTelephone_home2() {
        return telephone_home2;
    }


    public final String getTelephone_ip() {
        return telephone_ip;
    }


    public final String getTelephone_isdn() {
        return telephone_isdn;
    }


    public final String getTelephone_other() {
        return telephone_other;
    }


    public final String getTelephone_pager() {
        return telephone_pager;
    }


    public final String getTelephone_primary() {
        return telephone_primary;
    }


    public final String getTelephone_radio() {
        return telephone_radio;
    }


    public final String getTelephone_telex() {
        return telephone_telex;
    }


    public final String getTelephone_ttytdd() {
        return telephone_ttytdd;
    }


    public final String getTitle() {
        return title;
    }


    public final String getUniqueid() {
        return uniqueid;
    }


    public final String getUrl() {
        return url;
    }


    public final String getUserfield01() {
        return userfield01;
    }


    public final String getUserfield02() {
        return userfield02;
    }


    public final String getUserfield03() {
        return userfield03;
    }


    public final String getUserfield04() {
        return userfield04;
    }


    public final String getUserfield05() {
        return userfield05;
    }


    public final String getUserfield06() {
        return userfield06;
    }


    public final String getUserfield07() {
        return userfield07;
    }


    public final String getUserfield08() {
        return userfield08;
    }


    public final String getUserfield09() {
        return userfield09;
    }


    public final String getUserfield10() {
        return userfield10;
    }


    public final String getUserfield11() {
        return userfield11;
    }


    public final String getUserfield12() {
        return userfield12;
    }


    public final String getUserfield13() {
        return userfield13;
    }


    public final String getUserfield14() {
        return userfield14;
    }


    public final String getUserfield15() {
        return userfield15;
    }


    public final String getUserfield16() {
        return userfield16;
    }


    public final String getUserfield17() {
        return userfield17;
    }


    public final String getUserfield18() {
        return userfield18;
    }


    public final String getUserfield19() {
        return userfield19;
    }


    public final String getUserfield20() {
        return userfield20;
    }

    private final void setAnniversary(final String anniversary) {
        this.anniversary = anniversary;
    }

    private final void setAssistant_name(final String assistant_name) {
        this.assistant_name = assistant_name;
    }

    private final void setBirthday(final String birthday) {
        this.birthday = birthday;
    }

    private final void setBranches(final String branches) {
        this.branches = branches;
    }

    private final void setBusiness_category(final String business_category) {
        this.business_category = business_category;
    }

    private final void setCategories(final String categories) {
        this.categories = categories;
    }

    private final void setCellular_telephone1(final String cellular_telephone1) {
        this.cellular_telephone1 = cellular_telephone1;
    }

    private final void setCellular_telephone2(final String cellular_telephone2) {
        this.cellular_telephone2 = cellular_telephone2;
    }

    private final void setCity_business(final String city_business) {
        this.city_business = city_business;
    }

    private final void setCity_home(final String city_home) {
        this.city_home = city_home;
    }

    private final void setCity_other(final String city_other) {
        this.city_other = city_other;
    }

    private final void setCommercial_register(final String commercial_register) {
        this.commercial_register = commercial_register;
    }

    private final void setCompany(final String company) {
        this.company = company;
    }

    private final void setCountry_business(final String country_business) {
        this.country_business = country_business;
    }

    private final void setCountry_home(final String country_home) {
        this.country_home = country_home;
    }

    private final void setCountry_other(final String country_other) {
        this.country_other = country_other;
    }

    private final void setCreationdate(final String creationdate) {
        this.creationdate = creationdate;
    }

    private final void setDefaultaddress(final String defaultaddress) {
        this.defaultaddress = defaultaddress;
    }

    private final void setDepartment(final String department) {
        this.department = department;
    }

    private final void setDisplayname(final String displayname) {
        this.displayname = displayname;
    }

    /**
     * Sets the distributionuid
     *
     * @param distributionuid The distributionuid to set
     */
    private final void setDistributionuid(final String distributionuid) {
        this.distributionuid = distributionuid;
    }

    private final void setEmail1(final String email1) {
        this.email1 = email1;
    }

    private final void setEmail2(final String email2) {
        this.email2 = email2;
    }

    private final void setEmail3(final String email3) {
        this.email3 = email3;
    }

    private final void setEmployeetype(final String employeetype) {
        this.employeetype = employeetype;
    }

    private final void setFax_business(final String fax_business) {
        this.fax_business = fax_business;
    }

    private final void setFax_home(final String fax_home) {
        this.fax_home = fax_home;
    }

    private final void setFax_other(final String fax_other) {
        this.fax_other = fax_other;
    }

    private final void setGivenname(final String givenname) {
        this.givenname = givenname;
    }

    private final void setInfo(final String info) {
        this.info = info;
    }

    private final void setInstant_messenger1(final String instant_messenger1) {
        this.instant_messenger1 = instant_messenger1;
    }

    private final void setInstant_messenger2(final String instant_messenger2) {
        this.instant_messenger2 = instant_messenger2;
    }

    private final void setLastmodified(final String lastmodified) {
        this.lastmodified = lastmodified;
    }

    private final void setManager_name(final String manager_name) {
        this.manager_name = manager_name;
    }

    private final void setMarital_status(final String marital_status) {
        this.marital_status = marital_status;
    }

    private final void setMiddle_name(final String middle_name) {
        this.middle_name = middle_name;
    }

    private final void setNickname(final String nickname) {
        this.nickname = nickname;
    }

    private final void setNote(final String note) {
        this.note = note;
    }

    private final void setNumber_of_children(final String number_of_children) {
        this.number_of_children = number_of_children;
    }

    private final void setNumber_of_employee(final String number_of_employee) {
        this.number_of_employee = number_of_employee;
    }

    private final void setPosition(final String position) {
        this.position = position;
    }

    private final void setPostal_code_business(final String postal_code_business) {
        this.postal_code_business = postal_code_business;
    }

    private final void setPostal_code_home(final String postal_code_home) {
        this.postal_code_home = postal_code_home;
    }

    private final void setPostal_code_other(final String postal_code_other) {
        this.postal_code_other = postal_code_other;
    }

    private final void setProfession(final String profession) {
        this.profession = profession;
    }

    private final void setRoom_number(final String room_number) {
        this.room_number = room_number;
    }

    private final void setSales_volume(final String sales_volume) {
        this.sales_volume = sales_volume;
    }

    private final void setSpouse_name(final String spouse_name) {
        this.spouse_name = spouse_name;
    }

    private final void setState_business(final String state_business) {
        this.state_business = state_business;
    }

    private final void setState_home(final String state_home) {
        this.state_home = state_home;
    }

    private final void setState_other(final String state_other) {
        this.state_other = state_other;
    }

    private final void setStreet_business(final String street_business) {
        this.street_business = street_business;
    }

    private final void setStreet_home(final String street_home) {
        this.street_home = street_home;
    }

    private final void setStreet_other(final String street_other) {
        this.street_other = street_other;
    }

    private final void setSuffix(final String suffix) {
        this.suffix = suffix;
    }

    private final void setSurname(final String surname) {
        this.surname = surname;
    }

    private final void setTax_id(final String tax_id) {
        this.tax_id = tax_id;
    }

    private final void setTelephone_assistant(final String telephone_assistant) {
        this.telephone_assistant = telephone_assistant;
    }

    private final void setTelephone_business1(final String telephone_business1) {
        this.telephone_business1 = telephone_business1;
    }

    private final void setTelephone_business2(final String telephone_business2) {
        this.telephone_business2 = telephone_business2;
    }

    private final void setTelephone_callback(final String telephone_callback) {
        this.telephone_callback = telephone_callback;
    }

    private final void setTelephone_car(final String telephone_car) {
        this.telephone_car = telephone_car;
    }

    private final void setTelephone_company(final String telephone_company) {
        this.telephone_company = telephone_company;
    }

    private final void setTelephone_home1(final String telephone_home1) {
        this.telephone_home1 = telephone_home1;
    }

    private final void setTelephone_home2(final String telephone_home2) {
        this.telephone_home2 = telephone_home2;
    }

    private final void setTelephone_ip(final String telephone_ip) {
        this.telephone_ip = telephone_ip;
    }

    private final void setTelephone_isdn(final String telephone_isdn) {
        this.telephone_isdn = telephone_isdn;
    }

    private final void setTelephone_other(final String telephone_other) {
        this.telephone_other = telephone_other;
    }

    private final void setTelephone_pager(final String telephone_pager) {
        this.telephone_pager = telephone_pager;
    }

    private final void setTelephone_primary(final String telephone_primary) {
        this.telephone_primary = telephone_primary;
    }

    private final void setTelephone_radio(final String telephone_radio) {
        this.telephone_radio = telephone_radio;
    }

    private final void setTelephone_telex(final String telephone_telex) {
        this.telephone_telex = telephone_telex;
    }

    private final void setTelephone_ttytdd(final String telephone_ttytdd) {
        this.telephone_ttytdd = telephone_ttytdd;
    }

    private final void setTitle(final String title) {
        this.title = title;
    }

    private final void setUniqueid(final String uniqueid) {
        this.uniqueid = uniqueid;
    }

    private final void setUrl(final String url) {
        this.url = url;
    }

    private final void setUserfield01(final String userfield01) {
        this.userfield01 = userfield01;
    }

    private final void setUserfield02(final String userfield02) {
        this.userfield02 = userfield02;
    }

    private final void setUserfield03(final String userfield03) {
        this.userfield03 = userfield03;
    }

    private final void setUserfield04(final String userfield04) {
        this.userfield04 = userfield04;
    }

    private final void setUserfield05(final String userfield05) {
        this.userfield05 = userfield05;
    }

    private final void setUserfield06(final String userfield06) {
        this.userfield06 = userfield06;
    }

    private final void setUserfield07(final String userfield07) {
        this.userfield07 = userfield07;
    }

    private final void setUserfield08(final String userfield08) {
        this.userfield08 = userfield08;
    }

    private final void setUserfield09(final String userfield09) {
        this.userfield09 = userfield09;
    }

    private final void setUserfield10(final String userfield10) {
        this.userfield10 = userfield10;
    }

    private final void setUserfield11(final String userfield11) {
        this.userfield11 = userfield11;
    }

    private final void setUserfield12(final String userfield12) {
        this.userfield12 = userfield12;
    }

    private final void setUserfield13(final String userfield13) {
        this.userfield13 = userfield13;
    }

    private final void setUserfield14(final String userfield14) {
        this.userfield14 = userfield14;
    }

    private final void setUserfield15(final String userfield15) {
        this.userfield15 = userfield15;
    }

    private final void setUserfield16(final String userfield16) {
        this.userfield16 = userfield16;
    }

    private final void setUserfield17(final String userfield17) {
        this.userfield17 = userfield17;
    }

    private final void setUserfield18(final String userfield18) {
        this.userfield18 = userfield18;
    }

    private final void setUserfield19(final String userfield19) {
        this.userfield19 = userfield19;
    }


    private final void setUserfield20(final String userfield20) {
        this.userfield20 = userfield20;
    }
}
