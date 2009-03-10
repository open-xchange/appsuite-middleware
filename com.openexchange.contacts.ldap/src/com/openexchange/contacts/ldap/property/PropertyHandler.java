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

package com.openexchange.contacts.ldap.property;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.config.ConfigurationService;
import com.openexchange.contacts.ldap.exceptions.LdapConfigurationException;
import com.openexchange.contacts.ldap.exceptions.LdapConfigurationException.Code;
import com.openexchange.contacts.ldap.osgi.ServiceRegistry;

/**
 * A class which will deal with all property related actions.
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class PropertyHandler {
    
    
    public class ContextDetails {
        
        private final String foldername;
        
        /**
         * Initializes a new {@link ContextDetails}.
         * @param foldername
         */
        private ContextDetails(String foldername) {
            this.foldername = foldername;
        }
        
        public final String getFoldername() {
            return foldername;
        }
    }

    private static final String bundlename = "com.openexchange.contacts.ldap.";
    
    private enum Parameters {
        uri("uri"),
        baseDN("baseDN"),
        AdminDN("AdminDN"),
        AdminBindPW("AdminBindPW"),
        searchScope("searchScope"),
        authtype("authtype"),
        contexts("contexts"),
        // Here we begin with the mapping entries
        uniqueid("uniqueid"),
        displayname("displayname"),
        givenname("givenname"),
        surname("surname"),
        email1("email1"),
        department("department"),
        company("company"),
        
        birthday("birthday"),
        anniversary("anniversary"),
        branches("branches"),
        business_category("business_category"),
        postal_code_business("postal_code_business"),
        state_business("state_business"),
        street_business("street_business"),
        telephone_callback("telephone_callback"),
        city_home("city_home"),
        commercial_register("commercial_register"),
        country_home("country_home"),
        email2("email2"),
        email3("email3"),
        employeetype("employeetype"),
        fax_business("fax_business"),
        fax_home("fax_home"),
        fax_other("fax_other"),
        instant_messenger1("instant_messenger1"),
        instant_messenger2("instant_messenger2"),
        telephone_ip("telephone_ip"),
        telephone_isdn("telephone_isdn"),
        manager_name("manager_name"),
        marital_status("marital_status"),
        cellular_telephone1("cellular_telephone1"),
        cellular_telephone2("cellular_telephone2"),
        info("info"),
        nickname("nickname"),
        number_of_children("number_of_children"),
        note("note"),
        number_of_employee("number_of_employee"),
        telephone_pager("telephone_pager"),
        telephone_assistant("telephone_assistant"),
        telephone_business1("telephone_business1"),
        telephone_business2("telephone_business2"),
        telephone_car("telephone_car"),
        telephone_company("telephone_company"),
        telephone_home1("telephone_home1"),
        telephone_home2("telephone_home2"),
        telephone_other("telephone_other"),
        postal_code_home("postal_code_home"),
        profession("profession"),
        telephone_radio("telephone_radio"),
        room_number("room_number"),
        sales_volume("sales_volume"),
        city_other("city_other"),
        country_other("country_other"),
        middle_name("middle_name"),
        postal_code_other("postal_code_other"),
        state_other("state_other"),
        street_other("street_other"),
        spouse_name("spouse_name"),
        state_home("state_home"),
        street_home("street_home"),
        suffix("suffix"),
        tax_id("tax_id"),
        telephone_telex("telephone_telex"),
        telephone_ttytdd("telephone_ttytdd"),
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
        userfield20("userfield20"),
        city_business("city_business"),
        country_business("country_business"),
        assistant_name("assistant_name"),
        telephone_primary("telephone_primary"),
        categories("categories"),
        defaultaddress("defaultaddress"),
        title("title"),
        position("position");
        
        private final String name;
        
        private Parameters(final String name) {
            this.name = name;
        }

        
        public final String getName() {
            return bundlename + name;
        }
        
    }
    
    public enum AuthType {
        anonymous("anonymous"),
        AdminDN("AdminDN"),
        user("user");
        
        private final String type;
        
        private AuthType(final String type) {
            this.type = type;
        }

        
        public final String getType() {
            return type;
        }
        
    }
    
    public enum SearchScope {
        sub("sub"),
        base("base"),
        one("one");
        
        private final String type;
        
        private SearchScope(final String type) {
            this.type = type;
        }

        public final String getType() {
            return type;
        }
        
    }
    
    private AtomicBoolean loaded = new AtomicBoolean();

    private Properties properties;
    
    private Map<Integer, ContextDetails> contextdetails = new ConcurrentHashMap<Integer, ContextDetails>();
    
    private String uri;
    
    private String baseDN;

    private String adminDN;
    
    private String adminBindPW;
    
    private SearchScope searchScope;
    
    private AuthType authtype;
    
    private String uniqueid;
    
    private String displayname;
    
    private String givenname;

    private String surname;
    
    private String email1;
    
    private String department;

    private String company;

    private String birthday;

    private String anniversary;

    private String branches;

    private String business_category;

    private String postal_code_business;

    private String state_business;

    private String street_business;

    private String telephone_callback;

    private String city_home;

    private String commercial_register;

    private String country_home;

    private String email2;

    private String email3;

    private String employeetype;

    private String fax_business;

    private String fax_home;

    private String fax_other;

    private String instant_messenger1;

    private String instant_messenger2;

    private String telephone_ip;

    private String telephone_isdn;

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

    private String telephone_assistant;

    private String telephone_business1;

    private String telephone_business2;

    private String telephone_car;

    private String telephone_company;

    private String telephone_home1;

    private String telephone_home2;

    private String telephone_other;

    private String postal_code_home;

    private String profession;

    private String telephone_radio;

    private String room_number;

    private String sales_volume;

    private String city_other;

    private String country_other;

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

    private String city_business;

    private String country_business;

    private String assistant_name;

    private String telephone_primary;

    private String categories;

    private String defaultaddress;

    private String title;

    private String position;

    private List<Integer> contexts;
    
    
    private final static String PROPFILE = "contacts-ldap.properties";
    
    private static PropertyHandler singleton = new PropertyHandler();
    
    public static PropertyHandler getInstance() {
        return singleton;
    }

    public void loadProperties() throws LdapConfigurationException {
        final ConfigurationService configuration = ServiceRegistry.getInstance().getService(ConfigurationService.class);
        this.properties = configuration.getFile(PROPFILE);
        
        // Here we iterate over all properties...
        this.uri = checkStringProperty(Parameters.uri.getName());
        
        this.baseDN = checkStringProperty(Parameters.baseDN.getName());
        
        this.adminDN = checkStringProperty(Parameters.AdminDN.getName());
        
        this.adminBindPW = checkStringProperty(Parameters.AdminBindPW.getName());
        
        final String searchScopeString = checkStringProperty(Parameters.searchScope.getName());
        try {
            this.searchScope = SearchScope.valueOf(searchScopeString);
        } catch (final IllegalArgumentException e) {
            throw new LdapConfigurationException(Code.SEARCH_SCOPE_WRONG, searchScopeString);
        }
        
        final String authstring = checkStringProperty(Parameters.authtype.getName());
        try {
            this.authtype  = AuthType.valueOf(authstring);
        } catch (final IllegalArgumentException e) {
            throw new LdapConfigurationException(Code.AUTH_TYPE_WRONG, authstring);
        }
        
        this.contexts = getContexts(Parameters.contexts.getName());
        
        this.uniqueid = checkStringProperty(Parameters.uniqueid.getName());
        
        this.displayname = checkStringProperty(Parameters.displayname.getName());
        
        this.givenname = checkStringProperty(Parameters.givenname.getName());
        
        this.surname = checkStringProperty(Parameters.surname.getName());

        this.email1 = checkStringProperty(Parameters.email1.getName());
        
        this.department = checkStringProperty(Parameters.department.getName());
        
        this.company = checkStringProperty(Parameters.company.getName());

        this.birthday = checkStringProperty(Parameters.birthday.getName());
        
        this.anniversary = checkStringProperty(Parameters.anniversary.getName());
        
        this.branches = checkStringProperty(Parameters.branches.getName());
        
        this.business_category = checkStringProperty(Parameters.business_category.getName());
        
        this.postal_code_business = checkStringProperty(Parameters.postal_code_business.getName());
        
        this.state_business = checkStringProperty(Parameters.state_business.getName());
        
        this.street_business = checkStringProperty(Parameters.street_business.getName());
        
        this.telephone_callback = checkStringProperty(Parameters.telephone_callback.getName());
        
        this.city_home = checkStringProperty(Parameters.city_home.getName());
        
        this.commercial_register = checkStringProperty(Parameters.commercial_register.getName());
        
        this.country_home = checkStringProperty(Parameters.country_home.getName());
        
        this.email2 = checkStringProperty(Parameters.email2.getName());
        
        this.email3 = checkStringProperty(Parameters.email3.getName());
        
        this.employeetype = checkStringProperty(Parameters.employeetype.getName());
        
        this.fax_business = checkStringProperty(Parameters.fax_business.getName());
        
        this.fax_home = checkStringProperty(Parameters.fax_home.getName());
        
        this.fax_other = checkStringProperty(Parameters.fax_other.getName());
        
        this.instant_messenger1 = checkStringProperty(Parameters.instant_messenger1.getName());
        
        this.instant_messenger2 = checkStringProperty(Parameters.instant_messenger2.getName());
        
        this.telephone_ip = checkStringProperty(Parameters.telephone_ip.getName());
        
        this.telephone_isdn = checkStringProperty(Parameters.telephone_isdn.getName());
        
        this.manager_name = checkStringProperty(Parameters.manager_name.getName());
        
        this.marital_status = checkStringProperty(Parameters.marital_status.getName());
        
        this.cellular_telephone1 = checkStringProperty(Parameters.cellular_telephone1.getName());
        
        this.cellular_telephone2 = checkStringProperty(Parameters.cellular_telephone2.getName());
        
        this.info = checkStringProperty(Parameters.info.getName());
        
        this.nickname = checkStringProperty(Parameters.nickname.getName());
        
        this.number_of_children = checkStringProperty(Parameters.number_of_children.getName());
        
        this.note = checkStringProperty(Parameters.note.getName());
        
        this.number_of_employee = checkStringProperty(Parameters.number_of_employee.getName());
        
        this.telephone_pager = checkStringProperty(Parameters.telephone_pager.getName());
        
        this.telephone_assistant = checkStringProperty(Parameters.telephone_assistant.getName());
        
        this.telephone_business1 = checkStringProperty(Parameters.telephone_business1.getName());
        
        this.telephone_business2 = checkStringProperty(Parameters.telephone_business2.getName());
        
        this.telephone_car = checkStringProperty(Parameters.telephone_car.getName());
        
        this.telephone_company = checkStringProperty(Parameters.telephone_company.getName());
        
        this.telephone_home1 = checkStringProperty(Parameters.telephone_home1.getName());
        
        this.telephone_home2 = checkStringProperty(Parameters.telephone_home2.getName());
        
        this.telephone_other = checkStringProperty(Parameters.telephone_other.getName());
        
        this.postal_code_home = checkStringProperty(Parameters.postal_code_home.getName());
        
        this.profession = checkStringProperty(Parameters.profession.getName());
        
        this.telephone_radio = checkStringProperty(Parameters.telephone_radio.getName());
        
        this.room_number = checkStringProperty(Parameters.room_number.getName());
        
        this.sales_volume = checkStringProperty(Parameters.sales_volume.getName());
        
        this.city_other = checkStringProperty(Parameters.city_other.getName());
        
        this.country_other = checkStringProperty(Parameters.country_other.getName());
        
        this.middle_name = checkStringProperty(Parameters.middle_name.getName());
        
        this.postal_code_other = checkStringProperty(Parameters.postal_code_other.getName());
        
        this.state_other = checkStringProperty(Parameters.state_other.getName());
        
        this.street_other = checkStringProperty(Parameters.street_other.getName());
        
        this.spouse_name = checkStringProperty(Parameters.spouse_name.getName());
        
        this.state_home = checkStringProperty(Parameters.state_home.getName());
        
        this.street_home = checkStringProperty(Parameters.street_home.getName());
        
        this.suffix = checkStringProperty(Parameters.suffix.getName());

        this.tax_id = checkStringProperty(Parameters.tax_id.getName());
        
        this.telephone_telex = checkStringProperty(Parameters.telephone_telex.getName());
        
        this.telephone_ttytdd = checkStringProperty(Parameters.telephone_ttytdd.getName());
        
        this.url = checkStringProperty(Parameters.url.getName());
        
        this.userfield01 = checkStringProperty(Parameters.userfield01.getName());
        
        this.userfield02 = checkStringProperty(Parameters.userfield02.getName());
        
        this.userfield03 = checkStringProperty(Parameters.userfield03.getName());
        
        this.userfield04 = checkStringProperty(Parameters.userfield04.getName());
        
        this.userfield05 = checkStringProperty(Parameters.userfield05.getName());
        
        this.userfield06 = checkStringProperty(Parameters.userfield06.getName());
        
        this.userfield07 = checkStringProperty(Parameters.userfield07.getName());
        
        this.userfield08 = checkStringProperty(Parameters.userfield08.getName());
        
        this.userfield09 = checkStringProperty(Parameters.userfield09.getName());
        
        this.userfield10 = checkStringProperty(Parameters.userfield10.getName());
        
        this.userfield10 = checkStringProperty(Parameters.userfield10.getName());
        
        this.userfield11 = checkStringProperty(Parameters.userfield11.getName());
        
        this.userfield12 = checkStringProperty(Parameters.userfield12.getName());
        
        this.userfield13 = checkStringProperty(Parameters.userfield13.getName());
        
        this.userfield14 = checkStringProperty(Parameters.userfield14.getName());
        
        this.userfield15 = checkStringProperty(Parameters.userfield15.getName());
        
        this.userfield16 = checkStringProperty(Parameters.userfield16.getName());
        
        this.userfield17 = checkStringProperty(Parameters.userfield17.getName());
        
        this.userfield18 = checkStringProperty(Parameters.userfield18.getName());
        
        this.userfield19 = checkStringProperty(Parameters.userfield19.getName());
        
        this.userfield20 = checkStringProperty(Parameters.userfield20.getName());
        
        this.city_business = checkStringProperty(Parameters.city_business.getName());
        
        this.country_business = checkStringProperty(Parameters.country_business.getName());
        
        this.assistant_name = checkStringProperty(Parameters.assistant_name.getName());
        
        this.telephone_primary = checkStringProperty(Parameters.telephone_primary.getName());
        
        this.categories = checkStringProperty(Parameters.categories.getName());
        
        this.defaultaddress = checkStringProperty(Parameters.defaultaddress.getName());
        
        this.title = checkStringProperty(Parameters.title.getName());

        this.position = checkStringProperty(Parameters.position.getName());
        
        for (final Integer ctx : this.contexts) {
            final String stringctx = String.valueOf(ctx);
            final Properties file = configuration.getFile(stringctx + ".properties");
            final String parameter = bundlename + "context" + stringctx + ".foldername";
            final String foldername = file.getProperty(parameter);
            if (null != foldername) {
                this.contextdetails.put(ctx, new ContextDetails(foldername));
            } else {
                throw new LdapConfigurationException(Code.PARAMETER_NOT_SET, parameter);
            }
        }
//        configuration.getPropertiesInFolder(folderName)
        this.loaded.set(true);
    }
    
    private List<Integer> getContexts(String name) throws LdapConfigurationException {
        final String property = this.properties.getProperty(name);
        if (null != property) {
            final List<Integer> retval = new ArrayList<Integer>();
            final String[] split = property.split(",");
            for (final String ctx : split) {
                try {
                    retval.add(Integer.parseInt(ctx));
                } catch (final NumberFormatException e) {
                    throw new LdapConfigurationException(Code.NO_INTEGER_VALUE, ctx);
                }
            }
            return retval;
        } else {
            throw new LdapConfigurationException(Code.PARAMETER_NOT_SET, name);
        }
    }

    public void reloadProperties() {
        
    }
    
    public String getBaseDN() {
        return this.baseDN;
    }

    
    public final String getUri() {
        return uri;
    }

    public final String getAdminDN() {
        return adminDN;
    }

    
    public final SearchScope getSearchScope() {
        return searchScope;
    }

    
    public final AuthType getAuthtype() {
        return authtype;
    }

    
    public final String getAdminBindPW() {
        return adminBindPW;
    }
    
    public static final String getBundlename() {
        return bundlename;
    }

    
    public final String getUniqueid() {
        return uniqueid;
    }

    
    public final String getDisplayname() {
        return displayname;
    }

    
    public final String getGivenname() {
        return givenname;
    }

    
    public final String getSurname() {
        return surname;
    }

    
    public final String getEmail1() {
        return email1;
    }

    public final String getDepartment() {
        return department;
    }

    
    public final String getCompany() {
        return company;
    }

    
    public final String getBirthday() {
        return birthday;
    }

    public final String getAnniversary() {
        return anniversary;
    }

    public final String getBranches() {
        return branches;
    }

    public final String getBusiness_category() {
        return business_category;
    }

    public final String getPostal_code_business() {
        return postal_code_business;
    }

    public final String getState_business() {
        return state_business;
    }

    public final String getStreet_business() {
        return street_business;
    }
    
    public final String getTelephone_callback() {
        return telephone_callback;
    }
    
    public final String getCity_home() {
        return city_home;
    }

    public final String getCommercial_register() {
        return commercial_register;
    }
    
    public final String getCountry_home() {
        return country_home;
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

    public final String getInstant_messenger1() {
        return instant_messenger1;
    }
    
    public final String getInstant_messenger2() {
        return instant_messenger2;
    }
    
    public final String getTelephone_ip() {
        return telephone_ip;
    }
    
    public final String getTelephone_isdn() {
        return telephone_isdn;
    }
    
    public final String getManager_name() {
        return manager_name;
    }

    public final String getMarital_status() {
        return marital_status;
    }

    public final String getCellular_telephone1() {
        return cellular_telephone1;
    }

    public final String getCellular_telephone2() {
        return cellular_telephone2;
    }

    public final String getInfo() {
        return info;
    }

    public final String getNickname() {
        return nickname;
    }
    
    public final String getNumber_of_children() {
        return number_of_children;
    }
    
    public final String getNote() {
        return note;
    }
    
    public final String getNumber_of_employee() {
        return number_of_employee;
    }
    
    public final String getTelephone_pager() {
        return telephone_pager;
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
    
    public final String getTelephone_other() {
        return telephone_other;
    }
    
    public final String getPostal_code_home() {
        return postal_code_home;
    }
    
    public final String getProfession() {
        return profession;
    }
    
    public final String getTelephone_radio() {
        return telephone_radio;
    }
    
    public final String getRoom_number() {
        return room_number;
    }
    
    public final String getSales_volume() {
        return sales_volume;
    }
    
    public final String getCity_other() {
        return city_other;
    }
    
    public final String getCountry_other() {
        return country_other;
    }
    
    public final String getMiddle_name() {
        return middle_name;
    }
    
    public final String getPostal_code_other() {
        return postal_code_other;
    }
    
    public final String getState_other() {
        return state_other;
    }

    public final String getStreet_other() {
        return street_other;
    }
    
    public final String getSpouse_name() {
        return spouse_name;
    }
    
    public final String getState_home() {
        return state_home;
    }
    
    public final String getStreet_home() {
        return street_home;
    }
    
    public final String getSuffix() {
        return suffix;
    }

    
    public final String getTax_id() {
        return tax_id;
    }

    
    public final String getTelephone_telex() {
        return telephone_telex;
    }

    
    public final String getTelephone_ttytdd() {
        return telephone_ttytdd;
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

    
    
    public final String getCity_business() {
        return city_business;
    }

    
    public final String getCountry_business() {
        return country_business;
    }

    
    public final String getAssistant_name() {
        return assistant_name;
    }

    
    public final String getTelephone_primary() {
        return telephone_primary;
    }

    
    public final String getCategories() {
        return categories;
    }

    
    public final String getDefaultaddress() {
        return defaultaddress;
    }

    
    public final String getTitle() {
        return title;
    }

    
    public final String getPosition() {
        return position;
    }

    public static final PropertyHandler getSingleton() {
        return singleton;
    }

    public static final String getPROPFILE() {
        return PROPFILE;
    }
    
    private String checkStringProperty(final String name) throws LdapConfigurationException {
        final String property = this.properties.getProperty(name);
        if (null == property) {
            throw new LdapConfigurationException(Code.PARAMETER_NOT_SET, name);
        } else {
            return property;
        }
        
    }
}
