package com.openexchange.contacts.ldap.property;

import java.util.Properties;
import com.openexchange.contacts.ldap.exceptions.LdapConfigurationException;

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
        
        private static String prefix;
        
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

    public static Mappings getMappingsFromProperties(final Properties props, final String prefix, final String mappingfile) throws LdapConfigurationException {
        final Mappings retval = new Mappings();
        Parameters.setPrefix(prefix);
        
        retval.setDisplayname(PropertyHandler.checkStringProperty(props, Parameters.displayname.getName(), mappingfile));
        
        retval.setGivenname(PropertyHandler.checkStringProperty(props, Parameters.givenname.getName(), mappingfile));
        
        retval.setSurname(PropertyHandler.checkStringProperty(props, Parameters.surname.getName(), mappingfile));

        retval.setEmail1(PropertyHandler.checkStringProperty(props, Parameters.email1.getName(), mappingfile));
        
        retval.setDepartment(PropertyHandler.checkStringProperty(props, Parameters.department.getName(), mappingfile));
        
        retval.setCompany(PropertyHandler.checkStringProperty(props, Parameters.company.getName(), mappingfile));

        retval.setBirthday(PropertyHandler.checkStringProperty(props, Parameters.birthday.getName(), mappingfile));
        
        retval.setAnniversary(PropertyHandler.checkStringProperty(props, Parameters.anniversary.getName(), mappingfile));
        
        retval.setBranches(PropertyHandler.checkStringProperty(props, Parameters.branches.getName(), mappingfile));
        
        retval.setBusiness_category(PropertyHandler.checkStringProperty(props, Parameters.business_category.getName(), mappingfile));
        
        retval.setPostal_code_business(PropertyHandler.checkStringProperty(props, Parameters.postal_code_business.getName(), mappingfile));
        
        retval.setState_business(PropertyHandler.checkStringProperty(props, Parameters.state_business.getName(), mappingfile));
        
        retval.setStreet_business(PropertyHandler.checkStringProperty(props, Parameters.street_business.getName(), mappingfile));
        
        retval.setTelephone_callback(PropertyHandler.checkStringProperty(props, Parameters.telephone_callback.getName(), mappingfile));
        
        retval.setCity_home(PropertyHandler.checkStringProperty(props, Parameters.city_home.getName(), mappingfile));
        
        retval.setCommercial_register(PropertyHandler.checkStringProperty(props, Parameters.commercial_register.getName(), mappingfile));
        
        retval.setCountry_home(PropertyHandler.checkStringProperty(props, Parameters.country_home.getName(), mappingfile));
        
        retval.setEmail2(PropertyHandler.checkStringProperty(props, Parameters.email2.getName(), mappingfile));
        
        retval.setEmail3(PropertyHandler.checkStringProperty(props, Parameters.email3.getName(), mappingfile));
        
        retval.setEmployeetype(PropertyHandler.checkStringProperty(props, Parameters.employeetype.getName(), mappingfile));
        
        retval.setFax_business(PropertyHandler.checkStringProperty(props, Parameters.fax_business.getName(), mappingfile));
        
        retval.setFax_home(PropertyHandler.checkStringProperty(props, Parameters.fax_home.getName(), mappingfile));
        
        retval.setFax_other(PropertyHandler.checkStringProperty(props, Parameters.fax_other.getName(), mappingfile));
        
        retval.setInstant_messenger1(PropertyHandler.checkStringProperty(props, Parameters.instant_messenger1.getName(), mappingfile));
        
        retval.setInstant_messenger2(PropertyHandler.checkStringProperty(props, Parameters.instant_messenger2.getName(), mappingfile));
        
        retval.setTelephone_ip(PropertyHandler.checkStringProperty(props, Parameters.telephone_ip.getName(), mappingfile));
        
        retval.setTelephone_isdn(PropertyHandler.checkStringProperty(props, Parameters.telephone_isdn.getName(), mappingfile));
        
        retval.setManager_name(PropertyHandler.checkStringProperty(props, Parameters.manager_name.getName(), mappingfile));
        
        retval.setMarital_status(PropertyHandler.checkStringProperty(props, Parameters.marital_status.getName(), mappingfile));
        
        retval.setCellular_telephone1(PropertyHandler.checkStringProperty(props, Parameters.cellular_telephone1.getName(), mappingfile));
        
        retval.setCellular_telephone2(PropertyHandler.checkStringProperty(props, Parameters.cellular_telephone2.getName(), mappingfile));
        
        retval.setInfo(PropertyHandler.checkStringProperty(props, Parameters.info.getName(), mappingfile));
        
        retval.setNickname(PropertyHandler.checkStringProperty(props, Parameters.nickname.getName(), mappingfile));
        
        retval.setNumber_of_children(PropertyHandler.checkStringProperty(props, Parameters.number_of_children.getName(), mappingfile));
        
        retval.setNote(PropertyHandler.checkStringProperty(props, Parameters.note.getName(), mappingfile));
        
        retval.setNumber_of_employee(PropertyHandler.checkStringProperty(props, Parameters.number_of_employee.getName(), mappingfile));
        
        retval.setTelephone_pager(PropertyHandler.checkStringProperty(props, Parameters.telephone_pager.getName(), mappingfile));
        
        retval.setTelephone_assistant(PropertyHandler.checkStringProperty(props, Parameters.telephone_assistant.getName(), mappingfile));
        
        retval.setTelephone_business1(PropertyHandler.checkStringProperty(props, Parameters.telephone_business1.getName(), mappingfile));
        
        retval.setTelephone_business2(PropertyHandler.checkStringProperty(props, Parameters.telephone_business2.getName(), mappingfile));
        
        retval.setTelephone_car(PropertyHandler.checkStringProperty(props, Parameters.telephone_car.getName(), mappingfile));
        
        retval.setTelephone_company(PropertyHandler.checkStringProperty(props, Parameters.telephone_company.getName(), mappingfile));
        
        retval.setTelephone_home1(PropertyHandler.checkStringProperty(props, Parameters.telephone_home1.getName(), mappingfile));
        
        retval.setTelephone_home2(PropertyHandler.checkStringProperty(props, Parameters.telephone_home2.getName(), mappingfile));
        
        retval.setTelephone_other(PropertyHandler.checkStringProperty(props, Parameters.telephone_other.getName(), mappingfile));
        
        retval.setPostal_code_home(PropertyHandler.checkStringProperty(props, Parameters.postal_code_home.getName(), mappingfile));
        
        retval.setProfession(PropertyHandler.checkStringProperty(props, Parameters.profession.getName(), mappingfile));
        
        retval.setTelephone_radio(PropertyHandler.checkStringProperty(props, Parameters.telephone_radio.getName(), mappingfile));
        
        retval.setRoom_number(PropertyHandler.checkStringProperty(props, Parameters.room_number.getName(), mappingfile));
        
        retval.setSales_volume(PropertyHandler.checkStringProperty(props, Parameters.sales_volume.getName(), mappingfile));
        
        retval.setCity_other(PropertyHandler.checkStringProperty(props, Parameters.city_other.getName(), mappingfile));
        
        retval.setCountry_other(PropertyHandler.checkStringProperty(props, Parameters.country_other.getName(), mappingfile));
        
        retval.setMiddle_name(PropertyHandler.checkStringProperty(props, Parameters.middle_name.getName(), mappingfile));
        
        retval.setPostal_code_other(PropertyHandler.checkStringProperty(props, Parameters.postal_code_other.getName(), mappingfile));
        
        retval.setState_other(PropertyHandler.checkStringProperty(props, Parameters.state_other.getName(), mappingfile));
        
        retval.setStreet_other(PropertyHandler.checkStringProperty(props, Parameters.street_other.getName(), mappingfile));
        
        retval.setSpouse_name(PropertyHandler.checkStringProperty(props, Parameters.spouse_name.getName(), mappingfile));
        
        retval.setState_home(PropertyHandler.checkStringProperty(props, Parameters.state_home.getName(), mappingfile));
        
        retval.setStreet_home(PropertyHandler.checkStringProperty(props, Parameters.street_home.getName(), mappingfile));
        
        retval.setSuffix(PropertyHandler.checkStringProperty(props, Parameters.suffix.getName(), mappingfile));

        retval.setTax_id(PropertyHandler.checkStringProperty(props, Parameters.tax_id.getName(), mappingfile));
        
        retval.setTelephone_telex(PropertyHandler.checkStringProperty(props, Parameters.telephone_telex.getName(), mappingfile));
        
        retval.setTelephone_ttytdd(PropertyHandler.checkStringProperty(props, Parameters.telephone_ttytdd.getName(), mappingfile));
        
        retval.setUrl(PropertyHandler.checkStringProperty(props, Parameters.url.getName(), mappingfile));
        
        retval.setUserfield01(PropertyHandler.checkStringProperty(props, Parameters.userfield01.getName(), mappingfile));
        
        retval.setUserfield02(PropertyHandler.checkStringProperty(props, Parameters.userfield02.getName(), mappingfile));
        
        retval.setUserfield03(PropertyHandler.checkStringProperty(props, Parameters.userfield03.getName(), mappingfile));
        
        retval.setUserfield04(PropertyHandler.checkStringProperty(props, Parameters.userfield04.getName(), mappingfile));
        
        retval.setUserfield05(PropertyHandler.checkStringProperty(props, Parameters.userfield05.getName(), mappingfile));
        
        retval.setUserfield06(PropertyHandler.checkStringProperty(props, Parameters.userfield06.getName(), mappingfile));
        
        retval.setUserfield07(PropertyHandler.checkStringProperty(props, Parameters.userfield07.getName(), mappingfile));
        
        retval.setUserfield08(PropertyHandler.checkStringProperty(props, Parameters.userfield08.getName(), mappingfile));
        
        retval.setUserfield09(PropertyHandler.checkStringProperty(props, Parameters.userfield09.getName(), mappingfile));
        
        retval.setUserfield10(PropertyHandler.checkStringProperty(props, Parameters.userfield10.getName(), mappingfile));
        
        retval.setUserfield10(PropertyHandler.checkStringProperty(props, Parameters.userfield10.getName(), mappingfile));
        
        retval.setUserfield11(PropertyHandler.checkStringProperty(props, Parameters.userfield11.getName(), mappingfile));
        
        retval.setUserfield12(PropertyHandler.checkStringProperty(props, Parameters.userfield12.getName(), mappingfile));
        
        retval.setUserfield13(PropertyHandler.checkStringProperty(props, Parameters.userfield13.getName(), mappingfile));
        
        retval.setUserfield14(PropertyHandler.checkStringProperty(props, Parameters.userfield14.getName(), mappingfile));
        
        retval.setUserfield15(PropertyHandler.checkStringProperty(props, Parameters.userfield15.getName(), mappingfile));
        
        retval.setUserfield16(PropertyHandler.checkStringProperty(props, Parameters.userfield16.getName(), mappingfile));
        
        retval.setUserfield17(PropertyHandler.checkStringProperty(props, Parameters.userfield17.getName(), mappingfile));
        
        retval.setUserfield18(PropertyHandler.checkStringProperty(props, Parameters.userfield18.getName(), mappingfile));
        
        retval.setUserfield19(PropertyHandler.checkStringProperty(props, Parameters.userfield19.getName(), mappingfile));
        
        retval.setUserfield20(PropertyHandler.checkStringProperty(props, Parameters.userfield20.getName(), mappingfile));
        
        retval.setCity_business(PropertyHandler.checkStringProperty(props, Parameters.city_business.getName(), mappingfile));
        
        retval.setCountry_business(PropertyHandler.checkStringProperty(props, Parameters.country_business.getName(), mappingfile));
        
        retval.setAssistant_name(PropertyHandler.checkStringProperty(props, Parameters.assistant_name.getName(), mappingfile));
        
        retval.setTelephone_primary(PropertyHandler.checkStringProperty(props, Parameters.telephone_primary.getName(), mappingfile));
        
        retval.setCategories(PropertyHandler.checkStringProperty(props, Parameters.categories.getName(), mappingfile));
        
        retval.setDefaultaddress(PropertyHandler.checkStringProperty(props, Parameters.defaultaddress.getName(), mappingfile));
        
        retval.setTitle(PropertyHandler.checkStringProperty(props, Parameters.title.getName(), mappingfile));

        retval.setPosition(PropertyHandler.checkStringProperty(props, Parameters.position.getName(), mappingfile));
        
        retval.setLastmodified(PropertyHandler.checkStringProperty(props, Parameters.lastmodified.getName(), mappingfile));
        
        retval.setCreationdate(PropertyHandler.checkStringProperty(props, Parameters.creationdate.getName(), mappingfile));

        retval.setUniqueid(PropertyHandler.checkStringProperty(props, Parameters.uniqueid.getName(), mappingfile));

        return retval;
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

    private final void setAnniversary(String anniversary) {
        this.anniversary = anniversary;
    }

    private final void setAssistant_name(String assistant_name) {
        this.assistant_name = assistant_name;
    }

    private final void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    private final void setBranches(String branches) {
        this.branches = branches;
    }

    private final void setBusiness_category(String business_category) {
        this.business_category = business_category;
    }

    private final void setCategories(String categories) {
        this.categories = categories;
    }

    private final void setCellular_telephone1(String cellular_telephone1) {
        this.cellular_telephone1 = cellular_telephone1;
    }

    private final void setCellular_telephone2(String cellular_telephone2) {
        this.cellular_telephone2 = cellular_telephone2;
    }

    private final void setCity_business(String city_business) {
        this.city_business = city_business;
    }

    private final void setCity_home(String city_home) {
        this.city_home = city_home;
    }

    private final void setCity_other(String city_other) {
        this.city_other = city_other;
    }

    private final void setCommercial_register(String commercial_register) {
        this.commercial_register = commercial_register;
    }

    private final void setCompany(String company) {
        this.company = company;
    }

    private final void setCountry_business(String country_business) {
        this.country_business = country_business;
    }

    private final void setCountry_home(String country_home) {
        this.country_home = country_home;
    }

    private final void setCountry_other(String country_other) {
        this.country_other = country_other;
    }

    private final void setCreationdate(String creationdate) {
        this.creationdate = creationdate;
    }

    private final void setDefaultaddress(String defaultaddress) {
        this.defaultaddress = defaultaddress;
    }

    private final void setDepartment(String department) {
        this.department = department;
    }

    private final void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    private final void setEmail1(String email1) {
        this.email1 = email1;
    }

    private final void setEmail2(String email2) {
        this.email2 = email2;
    }

    private final void setEmail3(String email3) {
        this.email3 = email3;
    }

    private final void setEmployeetype(String employeetype) {
        this.employeetype = employeetype;
    }

    private final void setFax_business(String fax_business) {
        this.fax_business = fax_business;
    }

    private final void setFax_home(String fax_home) {
        this.fax_home = fax_home;
    }

    private final void setFax_other(String fax_other) {
        this.fax_other = fax_other;
    }

    private final void setGivenname(String givenname) {
        this.givenname = givenname;
    }

    private final void setInfo(String info) {
        this.info = info;
    }

    private final void setInstant_messenger1(String instant_messenger1) {
        this.instant_messenger1 = instant_messenger1;
    }

    private final void setInstant_messenger2(String instant_messenger2) {
        this.instant_messenger2 = instant_messenger2;
    }

    private final void setLastmodified(String lastmodified) {
        this.lastmodified = lastmodified;
    }

    private final void setManager_name(String manager_name) {
        this.manager_name = manager_name;
    }

    private final void setMarital_status(String marital_status) {
        this.marital_status = marital_status;
    }

    private final void setMiddle_name(String middle_name) {
        this.middle_name = middle_name;
    }

    private final void setNickname(String nickname) {
        this.nickname = nickname;
    }

    private final void setNote(String note) {
        this.note = note;
    }

    private final void setNumber_of_children(String number_of_children) {
        this.number_of_children = number_of_children;
    }

    private final void setNumber_of_employee(String number_of_employee) {
        this.number_of_employee = number_of_employee;
    }

    private final void setPosition(String position) {
        this.position = position;
    }

    private final void setPostal_code_business(String postal_code_business) {
        this.postal_code_business = postal_code_business;
    }

    private final void setPostal_code_home(String postal_code_home) {
        this.postal_code_home = postal_code_home;
    }

    private final void setPostal_code_other(String postal_code_other) {
        this.postal_code_other = postal_code_other;
    }

    private final void setProfession(String profession) {
        this.profession = profession;
    }

    private final void setRoom_number(String room_number) {
        this.room_number = room_number;
    }

    private final void setSales_volume(String sales_volume) {
        this.sales_volume = sales_volume;
    }

    private final void setSpouse_name(String spouse_name) {
        this.spouse_name = spouse_name;
    }

    private final void setState_business(String state_business) {
        this.state_business = state_business;
    }

    private final void setState_home(String state_home) {
        this.state_home = state_home;
    }

    private final void setState_other(String state_other) {
        this.state_other = state_other;
    }

    private final void setStreet_business(String street_business) {
        this.street_business = street_business;
    }

    private final void setStreet_home(String street_home) {
        this.street_home = street_home;
    }

    private final void setStreet_other(String street_other) {
        this.street_other = street_other;
    }

    private final void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    private final void setSurname(String surname) {
        this.surname = surname;
    }

    private final void setTax_id(String tax_id) {
        this.tax_id = tax_id;
    }

    private final void setTelephone_assistant(String telephone_assistant) {
        this.telephone_assistant = telephone_assistant;
    }

    private final void setTelephone_business1(String telephone_business1) {
        this.telephone_business1 = telephone_business1;
    }

    private final void setTelephone_business2(String telephone_business2) {
        this.telephone_business2 = telephone_business2;
    }

    private final void setTelephone_callback(String telephone_callback) {
        this.telephone_callback = telephone_callback;
    }

    private final void setTelephone_car(String telephone_car) {
        this.telephone_car = telephone_car;
    }

    private final void setTelephone_company(String telephone_company) {
        this.telephone_company = telephone_company;
    }

    private final void setTelephone_home1(String telephone_home1) {
        this.telephone_home1 = telephone_home1;
    }

    private final void setTelephone_home2(String telephone_home2) {
        this.telephone_home2 = telephone_home2;
    }

    private final void setTelephone_ip(String telephone_ip) {
        this.telephone_ip = telephone_ip;
    }

    private final void setTelephone_isdn(String telephone_isdn) {
        this.telephone_isdn = telephone_isdn;
    }

    private final void setTelephone_other(String telephone_other) {
        this.telephone_other = telephone_other;
    }

    private final void setTelephone_pager(String telephone_pager) {
        this.telephone_pager = telephone_pager;
    }

    private final void setTelephone_primary(String telephone_primary) {
        this.telephone_primary = telephone_primary;
    }

    private final void setTelephone_radio(String telephone_radio) {
        this.telephone_radio = telephone_radio;
    }

    private final void setTelephone_telex(String telephone_telex) {
        this.telephone_telex = telephone_telex;
    }

    private final void setTelephone_ttytdd(String telephone_ttytdd) {
        this.telephone_ttytdd = telephone_ttytdd;
    }

    private final void setTitle(String title) {
        this.title = title;
    }

    private final void setUniqueid(String uniqueid) {
        this.uniqueid = uniqueid;
    }

    private final void setUrl(String url) {
        this.url = url;
    }

    private final void setUserfield01(String userfield01) {
        this.userfield01 = userfield01;
    }

    private final void setUserfield02(String userfield02) {
        this.userfield02 = userfield02;
    }

    private final void setUserfield03(String userfield03) {
        this.userfield03 = userfield03;
    }

    private final void setUserfield04(String userfield04) {
        this.userfield04 = userfield04;
    }

    private final void setUserfield05(String userfield05) {
        this.userfield05 = userfield05;
    }

    private final void setUserfield06(String userfield06) {
        this.userfield06 = userfield06;
    }

    private final void setUserfield07(String userfield07) {
        this.userfield07 = userfield07;
    }

    private final void setUserfield08(String userfield08) {
        this.userfield08 = userfield08;
    }

    private final void setUserfield09(String userfield09) {
        this.userfield09 = userfield09;
    }

    private final void setUserfield10(String userfield10) {
        this.userfield10 = userfield10;
    }

    private final void setUserfield11(String userfield11) {
        this.userfield11 = userfield11;
    }

    private final void setUserfield12(String userfield12) {
        this.userfield12 = userfield12;
    }

    private final void setUserfield13(String userfield13) {
        this.userfield13 = userfield13;
    }

    private final void setUserfield14(String userfield14) {
        this.userfield14 = userfield14;
    }

    private final void setUserfield15(String userfield15) {
        this.userfield15 = userfield15;
    }

    private final void setUserfield16(String userfield16) {
        this.userfield16 = userfield16;
    }

    private final void setUserfield17(String userfield17) {
        this.userfield17 = userfield17;
    }

    private final void setUserfield18(String userfield18) {
        this.userfield18 = userfield18;
    }

    private final void setUserfield19(String userfield19) {
        this.userfield19 = userfield19;
    }


    private final void setUserfield20(String userfield20) {
        this.userfield20 = userfield20;
    }
}
