
package com.openexchange.admin.soap.dataobjects.xsd;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java-Klasse für User complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="User">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="aliases" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="anniversary" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="assistant_name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="birthday" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/>
 *         &lt;element name="branches" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="business_category" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="categories" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cellular_telephone1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cellular_telephone2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="city_business" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="city_home" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="city_other" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="commercial_register" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="company" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="contextadmin" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="country_business" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="country_home" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="country_other" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="defaultSenderAddress" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="default_group" type="{http://dataobjects.soap.admin.openexchange.com/xsd}Group" minOccurs="0"/>
 *         &lt;element name="department" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="display_name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="email1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="email2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="email3" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="employeeType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="fax_business" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="fax_home" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="fax_other" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="folderTree" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="given_name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="guiPreferencesForSoap" type="{http://dataobjects.soap.admin.openexchange.com/xsd}SOAPStringMap" minOccurs="0"/>
 *         &lt;element name="gui_spam_filter_enabled" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="imapLogin" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="imapPort" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="imapSchema" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="imapServer" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="imapServerString" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="info" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="instant_messenger1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="instant_messenger2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="language" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mail_folder_confirmed_ham_name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mail_folder_confirmed_spam_name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mail_folder_drafts_name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mail_folder_sent_name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mail_folder_spam_name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mail_folder_trash_name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mailenabled" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="manager_name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="marital_status" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="middle_name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="nickname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="note" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="number_of_children" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="number_of_employee" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="password" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="passwordMech" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="password_expired" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="position" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="postal_code_business" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="postal_code_home" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="postal_code_other" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="primaryEmail" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="profession" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="room_number" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sales_volume" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="smtpPort" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="smtpSchema" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="smtpServer" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="smtpServerString" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="spouse_name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="state_business" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="state_home" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="state_other" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="street_business" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="street_home" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="street_other" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="suffix" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sur_name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tax_id" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="telephone_assistant" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="telephone_business1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="telephone_business2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="telephone_callback" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="telephone_car" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="telephone_company" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="telephone_home1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="telephone_home2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="telephone_ip" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="telephone_isdn" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="telephone_other" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="telephone_pager" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="telephone_primary" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="telephone_radio" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="telephone_telex" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="telephone_ttytdd" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="timezone" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="title" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="uploadFileSizeLimit" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="uploadFileSizeLimitPerFile" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="url" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userAttributes" type="{http://dataobjects.soap.admin.openexchange.com/xsd}SOAPStringMapMap" minOccurs="0"/>
 *         &lt;element name="userfield01" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userfield02" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userfield03" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userfield04" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userfield05" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userfield06" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userfield07" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userfield08" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userfield09" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userfield10" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userfield11" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userfield12" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userfield13" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userfield14" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userfield15" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userfield16" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userfield17" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userfield18" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userfield19" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userfield20" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "User", propOrder = {
    "aliases",
    "anniversary",
    "assistantName",
    "birthday",
    "branches",
    "businessCategory",
    "categories",
    "cellularTelephone1",
    "cellularTelephone2",
    "cityBusiness",
    "cityHome",
    "cityOther",
    "commercialRegister",
    "company",
    "contextadmin",
    "countryBusiness",
    "countryHome",
    "countryOther",
    "defaultSenderAddress",
    "defaultGroup",
    "department",
    "displayName",
    "email1",
    "email2",
    "email3",
    "employeeType",
    "faxBusiness",
    "faxHome",
    "faxOther",
    "folderTree",
    "givenName",
    "guiPreferencesForSoap",
    "guiSpamFilterEnabled",
    "id",
    "imapLogin",
    "imapPort",
    "imapSchema",
    "imapServer",
    "imapServerString",
    "info",
    "instantMessenger1",
    "instantMessenger2",
    "language",
    "mailFolderConfirmedHamName",
    "mailFolderConfirmedSpamName",
    "mailFolderDraftsName",
    "mailFolderSentName",
    "mailFolderSpamName",
    "mailFolderTrashName",
    "mailenabled",
    "managerName",
    "maritalStatus",
    "middleName",
    "name",
    "nickname",
    "note",
    "numberOfChildren",
    "numberOfEmployee",
    "password",
    "passwordMech",
    "passwordExpired",
    "position",
    "postalCodeBusiness",
    "postalCodeHome",
    "postalCodeOther",
    "primaryEmail",
    "profession",
    "roomNumber",
    "salesVolume",
    "smtpPort",
    "smtpSchema",
    "smtpServer",
    "smtpServerString",
    "spouseName",
    "stateBusiness",
    "stateHome",
    "stateOther",
    "streetBusiness",
    "streetHome",
    "streetOther",
    "suffix",
    "surName",
    "taxId",
    "telephoneAssistant",
    "telephoneBusiness1",
    "telephoneBusiness2",
    "telephoneCallback",
    "telephoneCar",
    "telephoneCompany",
    "telephoneHome1",
    "telephoneHome2",
    "telephoneIp",
    "telephoneIsdn",
    "telephoneOther",
    "telephonePager",
    "telephonePrimary",
    "telephoneRadio",
    "telephoneTelex",
    "telephoneTtytdd",
    "timezone",
    "title",
    "uploadFileSizeLimit",
    "uploadFileSizeLimitPerFile",
    "url",
    "userAttributes",
    "userfield01",
    "userfield02",
    "userfield03",
    "userfield04",
    "userfield05",
    "userfield06",
    "userfield07",
    "userfield08",
    "userfield09",
    "userfield10",
    "userfield11",
    "userfield12",
    "userfield13",
    "userfield14",
    "userfield15",
    "userfield16",
    "userfield17",
    "userfield18",
    "userfield19",
    "userfield20"
})
public class User {

    @XmlElement(nillable = true)
    protected List<String> aliases;
    @XmlElementRef(name = "anniversary", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<XMLGregorianCalendar> anniversary;
    @XmlElementRef(name = "assistant_name", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> assistantName;
    @XmlElementRef(name = "birthday", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<XMLGregorianCalendar> birthday;
    @XmlElementRef(name = "branches", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> branches;
    @XmlElementRef(name = "business_category", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> businessCategory;
    @XmlElementRef(name = "categories", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> categories;
    @XmlElementRef(name = "cellular_telephone1", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> cellularTelephone1;
    @XmlElementRef(name = "cellular_telephone2", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> cellularTelephone2;
    @XmlElementRef(name = "city_business", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> cityBusiness;
    @XmlElementRef(name = "city_home", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> cityHome;
    @XmlElementRef(name = "city_other", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> cityOther;
    @XmlElementRef(name = "commercial_register", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> commercialRegister;
    @XmlElementRef(name = "company", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> company;
    protected Boolean contextadmin;
    @XmlElementRef(name = "country_business", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> countryBusiness;
    @XmlElementRef(name = "country_home", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> countryHome;
    @XmlElementRef(name = "country_other", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> countryOther;
    @XmlElementRef(name = "defaultSenderAddress", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> defaultSenderAddress;
    @XmlElementRef(name = "default_group", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Group> defaultGroup;
    @XmlElementRef(name = "department", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> department;
    @XmlElementRef(name = "display_name", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> displayName;
    @XmlElementRef(name = "email1", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> email1;
    @XmlElementRef(name = "email2", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> email2;
    @XmlElementRef(name = "email3", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> email3;
    @XmlElementRef(name = "employeeType", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> employeeType;
    @XmlElementRef(name = "fax_business", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> faxBusiness;
    @XmlElementRef(name = "fax_home", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> faxHome;
    @XmlElementRef(name = "fax_other", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> faxOther;
    @XmlElementRef(name = "folderTree", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Integer> folderTree;
    @XmlElementRef(name = "given_name", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> givenName;
    @XmlElementRef(name = "guiPreferencesForSoap", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<SOAPStringMap> guiPreferencesForSoap;
    @XmlElementRef(name = "gui_spam_filter_enabled", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> guiSpamFilterEnabled;
    @XmlElementRef(name = "id", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Integer> id;
    @XmlElementRef(name = "imapLogin", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> imapLogin;
    protected Integer imapPort;
    @XmlElementRef(name = "imapSchema", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> imapSchema;
    @XmlElementRef(name = "imapServer", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> imapServer;
    @XmlElementRef(name = "imapServerString", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> imapServerString;
    @XmlElementRef(name = "info", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> info;
    @XmlElementRef(name = "instant_messenger1", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> instantMessenger1;
    @XmlElementRef(name = "instant_messenger2", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> instantMessenger2;
    @XmlElementRef(name = "language", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> language;
    @XmlElementRef(name = "mail_folder_confirmed_ham_name", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> mailFolderConfirmedHamName;
    @XmlElementRef(name = "mail_folder_confirmed_spam_name", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> mailFolderConfirmedSpamName;
    @XmlElementRef(name = "mail_folder_drafts_name", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> mailFolderDraftsName;
    @XmlElementRef(name = "mail_folder_sent_name", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> mailFolderSentName;
    @XmlElementRef(name = "mail_folder_spam_name", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> mailFolderSpamName;
    @XmlElementRef(name = "mail_folder_trash_name", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> mailFolderTrashName;
    @XmlElementRef(name = "mailenabled", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> mailenabled;
    @XmlElementRef(name = "manager_name", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> managerName;
    @XmlElementRef(name = "marital_status", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> maritalStatus;
    @XmlElementRef(name = "middle_name", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> middleName;
    @XmlElementRef(name = "name", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> name;
    @XmlElementRef(name = "nickname", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> nickname;
    @XmlElementRef(name = "note", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> note;
    @XmlElementRef(name = "number_of_children", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> numberOfChildren;
    @XmlElementRef(name = "number_of_employee", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> numberOfEmployee;
    @XmlElementRef(name = "password", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> password;
    @XmlElementRef(name = "passwordMech", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> passwordMech;
    @XmlElementRef(name = "password_expired", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Boolean> passwordExpired;
    @XmlElementRef(name = "position", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> position;
    @XmlElementRef(name = "postal_code_business", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> postalCodeBusiness;
    @XmlElementRef(name = "postal_code_home", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> postalCodeHome;
    @XmlElementRef(name = "postal_code_other", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> postalCodeOther;
    @XmlElementRef(name = "primaryEmail", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> primaryEmail;
    @XmlElementRef(name = "profession", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> profession;
    @XmlElementRef(name = "room_number", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> roomNumber;
    @XmlElementRef(name = "sales_volume", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> salesVolume;
    protected Integer smtpPort;
    @XmlElementRef(name = "smtpSchema", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> smtpSchema;
    @XmlElementRef(name = "smtpServer", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> smtpServer;
    @XmlElementRef(name = "smtpServerString", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> smtpServerString;
    @XmlElementRef(name = "spouse_name", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> spouseName;
    @XmlElementRef(name = "state_business", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> stateBusiness;
    @XmlElementRef(name = "state_home", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> stateHome;
    @XmlElementRef(name = "state_other", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> stateOther;
    @XmlElementRef(name = "street_business", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> streetBusiness;
    @XmlElementRef(name = "street_home", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> streetHome;
    @XmlElementRef(name = "street_other", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> streetOther;
    @XmlElementRef(name = "suffix", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> suffix;
    @XmlElementRef(name = "sur_name", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> surName;
    @XmlElementRef(name = "tax_id", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> taxId;
    @XmlElementRef(name = "telephone_assistant", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> telephoneAssistant;
    @XmlElementRef(name = "telephone_business1", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> telephoneBusiness1;
    @XmlElementRef(name = "telephone_business2", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> telephoneBusiness2;
    @XmlElementRef(name = "telephone_callback", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> telephoneCallback;
    @XmlElementRef(name = "telephone_car", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> telephoneCar;
    @XmlElementRef(name = "telephone_company", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> telephoneCompany;
    @XmlElementRef(name = "telephone_home1", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> telephoneHome1;
    @XmlElementRef(name = "telephone_home2", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> telephoneHome2;
    @XmlElementRef(name = "telephone_ip", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> telephoneIp;
    @XmlElementRef(name = "telephone_isdn", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> telephoneIsdn;
    @XmlElementRef(name = "telephone_other", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> telephoneOther;
    @XmlElementRef(name = "telephone_pager", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> telephonePager;
    @XmlElementRef(name = "telephone_primary", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> telephonePrimary;
    @XmlElementRef(name = "telephone_radio", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> telephoneRadio;
    @XmlElementRef(name = "telephone_telex", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> telephoneTelex;
    @XmlElementRef(name = "telephone_ttytdd", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> telephoneTtytdd;
    @XmlElementRef(name = "timezone", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> timezone;
    @XmlElementRef(name = "title", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> title;
    @XmlElementRef(name = "uploadFileSizeLimit", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Integer> uploadFileSizeLimit;
    @XmlElementRef(name = "uploadFileSizeLimitPerFile", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<Integer> uploadFileSizeLimitPerFile;
    @XmlElementRef(name = "url", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> url;
    @XmlElementRef(name = "userAttributes", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<SOAPStringMapMap> userAttributes;
    @XmlElementRef(name = "userfield01", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> userfield01;
    @XmlElementRef(name = "userfield02", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> userfield02;
    @XmlElementRef(name = "userfield03", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> userfield03;
    @XmlElementRef(name = "userfield04", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> userfield04;
    @XmlElementRef(name = "userfield05", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> userfield05;
    @XmlElementRef(name = "userfield06", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> userfield06;
    @XmlElementRef(name = "userfield07", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> userfield07;
    @XmlElementRef(name = "userfield08", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> userfield08;
    @XmlElementRef(name = "userfield09", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> userfield09;
    @XmlElementRef(name = "userfield10", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> userfield10;
    @XmlElementRef(name = "userfield11", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> userfield11;
    @XmlElementRef(name = "userfield12", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> userfield12;
    @XmlElementRef(name = "userfield13", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> userfield13;
    @XmlElementRef(name = "userfield14", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> userfield14;
    @XmlElementRef(name = "userfield15", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> userfield15;
    @XmlElementRef(name = "userfield16", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> userfield16;
    @XmlElementRef(name = "userfield17", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> userfield17;
    @XmlElementRef(name = "userfield18", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> userfield18;
    @XmlElementRef(name = "userfield19", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> userfield19;
    @XmlElementRef(name = "userfield20", namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", type = JAXBElement.class)
    protected JAXBElement<String> userfield20;

    /**
     * Gets the value of the aliases property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the aliases property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAliases().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getAliases() {
        if (aliases == null) {
            aliases = new ArrayList<String>();
        }
        return this.aliases;
    }

    /**
     * Ruft den Wert der anniversary-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public JAXBElement<XMLGregorianCalendar> getAnniversary() {
        return anniversary;
    }

    /**
     * Legt den Wert der anniversary-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public void setAnniversary(JAXBElement<XMLGregorianCalendar> value) {
        this.anniversary = value;
    }

    /**
     * Ruft den Wert der assistantName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getAssistantName() {
        return assistantName;
    }

    /**
     * Legt den Wert der assistantName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setAssistantName(JAXBElement<String> value) {
        this.assistantName = value;
    }

    /**
     * Ruft den Wert der birthday-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public JAXBElement<XMLGregorianCalendar> getBirthday() {
        return birthday;
    }

    /**
     * Legt den Wert der birthday-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public void setBirthday(JAXBElement<XMLGregorianCalendar> value) {
        this.birthday = value;
    }

    /**
     * Ruft den Wert der branches-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getBranches() {
        return branches;
    }

    /**
     * Legt den Wert der branches-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setBranches(JAXBElement<String> value) {
        this.branches = value;
    }

    /**
     * Ruft den Wert der businessCategory-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getBusinessCategory() {
        return businessCategory;
    }

    /**
     * Legt den Wert der businessCategory-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setBusinessCategory(JAXBElement<String> value) {
        this.businessCategory = value;
    }

    /**
     * Ruft den Wert der categories-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getCategories() {
        return categories;
    }

    /**
     * Legt den Wert der categories-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setCategories(JAXBElement<String> value) {
        this.categories = value;
    }

    /**
     * Ruft den Wert der cellularTelephone1-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getCellularTelephone1() {
        return cellularTelephone1;
    }

    /**
     * Legt den Wert der cellularTelephone1-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setCellularTelephone1(JAXBElement<String> value) {
        this.cellularTelephone1 = value;
    }

    /**
     * Ruft den Wert der cellularTelephone2-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getCellularTelephone2() {
        return cellularTelephone2;
    }

    /**
     * Legt den Wert der cellularTelephone2-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setCellularTelephone2(JAXBElement<String> value) {
        this.cellularTelephone2 = value;
    }

    /**
     * Ruft den Wert der cityBusiness-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getCityBusiness() {
        return cityBusiness;
    }

    /**
     * Legt den Wert der cityBusiness-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setCityBusiness(JAXBElement<String> value) {
        this.cityBusiness = value;
    }

    /**
     * Ruft den Wert der cityHome-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getCityHome() {
        return cityHome;
    }

    /**
     * Legt den Wert der cityHome-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setCityHome(JAXBElement<String> value) {
        this.cityHome = value;
    }

    /**
     * Ruft den Wert der cityOther-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getCityOther() {
        return cityOther;
    }

    /**
     * Legt den Wert der cityOther-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setCityOther(JAXBElement<String> value) {
        this.cityOther = value;
    }

    /**
     * Ruft den Wert der commercialRegister-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getCommercialRegister() {
        return commercialRegister;
    }

    /**
     * Legt den Wert der commercialRegister-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setCommercialRegister(JAXBElement<String> value) {
        this.commercialRegister = value;
    }

    /**
     * Ruft den Wert der company-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getCompany() {
        return company;
    }

    /**
     * Legt den Wert der company-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setCompany(JAXBElement<String> value) {
        this.company = value;
    }

    /**
     * Ruft den Wert der contextadmin-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isContextadmin() {
        return contextadmin;
    }

    /**
     * Legt den Wert der contextadmin-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setContextadmin(Boolean value) {
        this.contextadmin = value;
    }

    /**
     * Ruft den Wert der countryBusiness-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getCountryBusiness() {
        return countryBusiness;
    }

    /**
     * Legt den Wert der countryBusiness-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setCountryBusiness(JAXBElement<String> value) {
        this.countryBusiness = value;
    }

    /**
     * Ruft den Wert der countryHome-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getCountryHome() {
        return countryHome;
    }

    /**
     * Legt den Wert der countryHome-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setCountryHome(JAXBElement<String> value) {
        this.countryHome = value;
    }

    /**
     * Ruft den Wert der countryOther-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getCountryOther() {
        return countryOther;
    }

    /**
     * Legt den Wert der countryOther-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setCountryOther(JAXBElement<String> value) {
        this.countryOther = value;
    }

    /**
     * Ruft den Wert der defaultSenderAddress-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getDefaultSenderAddress() {
        return defaultSenderAddress;
    }

    /**
     * Legt den Wert der defaultSenderAddress-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setDefaultSenderAddress(JAXBElement<String> value) {
        this.defaultSenderAddress = value;
    }

    /**
     * Ruft den Wert der defaultGroup-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Group }{@code >}
     *     
     */
    public JAXBElement<Group> getDefaultGroup() {
        return defaultGroup;
    }

    /**
     * Legt den Wert der defaultGroup-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Group }{@code >}
     *     
     */
    public void setDefaultGroup(JAXBElement<Group> value) {
        this.defaultGroup = value;
    }

    /**
     * Ruft den Wert der department-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getDepartment() {
        return department;
    }

    /**
     * Legt den Wert der department-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setDepartment(JAXBElement<String> value) {
        this.department = value;
    }

    /**
     * Ruft den Wert der displayName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getDisplayName() {
        return displayName;
    }

    /**
     * Legt den Wert der displayName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setDisplayName(JAXBElement<String> value) {
        this.displayName = value;
    }

    /**
     * Ruft den Wert der email1-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getEmail1() {
        return email1;
    }

    /**
     * Legt den Wert der email1-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setEmail1(JAXBElement<String> value) {
        this.email1 = value;
    }

    /**
     * Ruft den Wert der email2-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getEmail2() {
        return email2;
    }

    /**
     * Legt den Wert der email2-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setEmail2(JAXBElement<String> value) {
        this.email2 = value;
    }

    /**
     * Ruft den Wert der email3-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getEmail3() {
        return email3;
    }

    /**
     * Legt den Wert der email3-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setEmail3(JAXBElement<String> value) {
        this.email3 = value;
    }

    /**
     * Ruft den Wert der employeeType-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getEmployeeType() {
        return employeeType;
    }

    /**
     * Legt den Wert der employeeType-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setEmployeeType(JAXBElement<String> value) {
        this.employeeType = value;
    }

    /**
     * Ruft den Wert der faxBusiness-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getFaxBusiness() {
        return faxBusiness;
    }

    /**
     * Legt den Wert der faxBusiness-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setFaxBusiness(JAXBElement<String> value) {
        this.faxBusiness = value;
    }

    /**
     * Ruft den Wert der faxHome-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getFaxHome() {
        return faxHome;
    }

    /**
     * Legt den Wert der faxHome-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setFaxHome(JAXBElement<String> value) {
        this.faxHome = value;
    }

    /**
     * Ruft den Wert der faxOther-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getFaxOther() {
        return faxOther;
    }

    /**
     * Legt den Wert der faxOther-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setFaxOther(JAXBElement<String> value) {
        this.faxOther = value;
    }

    /**
     * Ruft den Wert der folderTree-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getFolderTree() {
        return folderTree;
    }

    /**
     * Legt den Wert der folderTree-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setFolderTree(JAXBElement<Integer> value) {
        this.folderTree = value;
    }

    /**
     * Ruft den Wert der givenName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getGivenName() {
        return givenName;
    }

    /**
     * Legt den Wert der givenName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setGivenName(JAXBElement<String> value) {
        this.givenName = value;
    }

    /**
     * Ruft den Wert der guiPreferencesForSoap-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link SOAPStringMap }{@code >}
     *     
     */
    public JAXBElement<SOAPStringMap> getGuiPreferencesForSoap() {
        return guiPreferencesForSoap;
    }

    /**
     * Legt den Wert der guiPreferencesForSoap-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link SOAPStringMap }{@code >}
     *     
     */
    public void setGuiPreferencesForSoap(JAXBElement<SOAPStringMap> value) {
        this.guiPreferencesForSoap = value;
    }

    /**
     * Ruft den Wert der guiSpamFilterEnabled-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getGuiSpamFilterEnabled() {
        return guiSpamFilterEnabled;
    }

    /**
     * Legt den Wert der guiSpamFilterEnabled-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setGuiSpamFilterEnabled(JAXBElement<Boolean> value) {
        this.guiSpamFilterEnabled = value;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setId(JAXBElement<Integer> value) {
        this.id = value;
    }

    /**
     * Ruft den Wert der imapLogin-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getImapLogin() {
        return imapLogin;
    }

    /**
     * Legt den Wert der imapLogin-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setImapLogin(JAXBElement<String> value) {
        this.imapLogin = value;
    }

    /**
     * Ruft den Wert der imapPort-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getImapPort() {
        return imapPort;
    }

    /**
     * Legt den Wert der imapPort-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setImapPort(Integer value) {
        this.imapPort = value;
    }

    /**
     * Ruft den Wert der imapSchema-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getImapSchema() {
        return imapSchema;
    }

    /**
     * Legt den Wert der imapSchema-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setImapSchema(JAXBElement<String> value) {
        this.imapSchema = value;
    }

    /**
     * Ruft den Wert der imapServer-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getImapServer() {
        return imapServer;
    }

    /**
     * Legt den Wert der imapServer-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setImapServer(JAXBElement<String> value) {
        this.imapServer = value;
    }

    /**
     * Ruft den Wert der imapServerString-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getImapServerString() {
        return imapServerString;
    }

    /**
     * Legt den Wert der imapServerString-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setImapServerString(JAXBElement<String> value) {
        this.imapServerString = value;
    }

    /**
     * Ruft den Wert der info-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getInfo() {
        return info;
    }

    /**
     * Legt den Wert der info-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setInfo(JAXBElement<String> value) {
        this.info = value;
    }

    /**
     * Ruft den Wert der instantMessenger1-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getInstantMessenger1() {
        return instantMessenger1;
    }

    /**
     * Legt den Wert der instantMessenger1-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setInstantMessenger1(JAXBElement<String> value) {
        this.instantMessenger1 = value;
    }

    /**
     * Ruft den Wert der instantMessenger2-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getInstantMessenger2() {
        return instantMessenger2;
    }

    /**
     * Legt den Wert der instantMessenger2-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setInstantMessenger2(JAXBElement<String> value) {
        this.instantMessenger2 = value;
    }

    /**
     * Ruft den Wert der language-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getLanguage() {
        return language;
    }

    /**
     * Legt den Wert der language-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setLanguage(JAXBElement<String> value) {
        this.language = value;
    }

    /**
     * Ruft den Wert der mailFolderConfirmedHamName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getMailFolderConfirmedHamName() {
        return mailFolderConfirmedHamName;
    }

    /**
     * Legt den Wert der mailFolderConfirmedHamName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setMailFolderConfirmedHamName(JAXBElement<String> value) {
        this.mailFolderConfirmedHamName = value;
    }

    /**
     * Ruft den Wert der mailFolderConfirmedSpamName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getMailFolderConfirmedSpamName() {
        return mailFolderConfirmedSpamName;
    }

    /**
     * Legt den Wert der mailFolderConfirmedSpamName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setMailFolderConfirmedSpamName(JAXBElement<String> value) {
        this.mailFolderConfirmedSpamName = value;
    }

    /**
     * Ruft den Wert der mailFolderDraftsName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getMailFolderDraftsName() {
        return mailFolderDraftsName;
    }

    /**
     * Legt den Wert der mailFolderDraftsName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setMailFolderDraftsName(JAXBElement<String> value) {
        this.mailFolderDraftsName = value;
    }

    /**
     * Ruft den Wert der mailFolderSentName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getMailFolderSentName() {
        return mailFolderSentName;
    }

    /**
     * Legt den Wert der mailFolderSentName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setMailFolderSentName(JAXBElement<String> value) {
        this.mailFolderSentName = value;
    }

    /**
     * Ruft den Wert der mailFolderSpamName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getMailFolderSpamName() {
        return mailFolderSpamName;
    }

    /**
     * Legt den Wert der mailFolderSpamName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setMailFolderSpamName(JAXBElement<String> value) {
        this.mailFolderSpamName = value;
    }

    /**
     * Ruft den Wert der mailFolderTrashName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getMailFolderTrashName() {
        return mailFolderTrashName;
    }

    /**
     * Legt den Wert der mailFolderTrashName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setMailFolderTrashName(JAXBElement<String> value) {
        this.mailFolderTrashName = value;
    }

    /**
     * Ruft den Wert der mailenabled-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getMailenabled() {
        return mailenabled;
    }

    /**
     * Legt den Wert der mailenabled-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setMailenabled(JAXBElement<Boolean> value) {
        this.mailenabled = value;
    }

    /**
     * Ruft den Wert der managerName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getManagerName() {
        return managerName;
    }

    /**
     * Legt den Wert der managerName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setManagerName(JAXBElement<String> value) {
        this.managerName = value;
    }

    /**
     * Ruft den Wert der maritalStatus-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getMaritalStatus() {
        return maritalStatus;
    }

    /**
     * Legt den Wert der maritalStatus-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setMaritalStatus(JAXBElement<String> value) {
        this.maritalStatus = value;
    }

    /**
     * Ruft den Wert der middleName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getMiddleName() {
        return middleName;
    }

    /**
     * Legt den Wert der middleName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setMiddleName(JAXBElement<String> value) {
        this.middleName = value;
    }

    /**
     * Ruft den Wert der name-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getName() {
        return name;
    }

    /**
     * Legt den Wert der name-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setName(JAXBElement<String> value) {
        this.name = value;
    }

    /**
     * Ruft den Wert der nickname-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getNickname() {
        return nickname;
    }

    /**
     * Legt den Wert der nickname-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setNickname(JAXBElement<String> value) {
        this.nickname = value;
    }

    /**
     * Ruft den Wert der note-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getNote() {
        return note;
    }

    /**
     * Legt den Wert der note-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setNote(JAXBElement<String> value) {
        this.note = value;
    }

    /**
     * Ruft den Wert der numberOfChildren-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getNumberOfChildren() {
        return numberOfChildren;
    }

    /**
     * Legt den Wert der numberOfChildren-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setNumberOfChildren(JAXBElement<String> value) {
        this.numberOfChildren = value;
    }

    /**
     * Ruft den Wert der numberOfEmployee-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getNumberOfEmployee() {
        return numberOfEmployee;
    }

    /**
     * Legt den Wert der numberOfEmployee-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setNumberOfEmployee(JAXBElement<String> value) {
        this.numberOfEmployee = value;
    }

    /**
     * Ruft den Wert der password-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getPassword() {
        return password;
    }

    /**
     * Legt den Wert der password-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setPassword(JAXBElement<String> value) {
        this.password = value;
    }

    /**
     * Ruft den Wert der passwordMech-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getPasswordMech() {
        return passwordMech;
    }

    /**
     * Legt den Wert der passwordMech-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setPasswordMech(JAXBElement<String> value) {
        this.passwordMech = value;
    }

    /**
     * Ruft den Wert der passwordExpired-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public JAXBElement<Boolean> getPasswordExpired() {
        return passwordExpired;
    }

    /**
     * Legt den Wert der passwordExpired-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Boolean }{@code >}
     *     
     */
    public void setPasswordExpired(JAXBElement<Boolean> value) {
        this.passwordExpired = value;
    }

    /**
     * Ruft den Wert der position-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getPosition() {
        return position;
    }

    /**
     * Legt den Wert der position-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setPosition(JAXBElement<String> value) {
        this.position = value;
    }

    /**
     * Ruft den Wert der postalCodeBusiness-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getPostalCodeBusiness() {
        return postalCodeBusiness;
    }

    /**
     * Legt den Wert der postalCodeBusiness-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setPostalCodeBusiness(JAXBElement<String> value) {
        this.postalCodeBusiness = value;
    }

    /**
     * Ruft den Wert der postalCodeHome-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getPostalCodeHome() {
        return postalCodeHome;
    }

    /**
     * Legt den Wert der postalCodeHome-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setPostalCodeHome(JAXBElement<String> value) {
        this.postalCodeHome = value;
    }

    /**
     * Ruft den Wert der postalCodeOther-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getPostalCodeOther() {
        return postalCodeOther;
    }

    /**
     * Legt den Wert der postalCodeOther-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setPostalCodeOther(JAXBElement<String> value) {
        this.postalCodeOther = value;
    }

    /**
     * Ruft den Wert der primaryEmail-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getPrimaryEmail() {
        return primaryEmail;
    }

    /**
     * Legt den Wert der primaryEmail-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setPrimaryEmail(JAXBElement<String> value) {
        this.primaryEmail = value;
    }

    /**
     * Ruft den Wert der profession-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getProfession() {
        return profession;
    }

    /**
     * Legt den Wert der profession-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setProfession(JAXBElement<String> value) {
        this.profession = value;
    }

    /**
     * Ruft den Wert der roomNumber-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getRoomNumber() {
        return roomNumber;
    }

    /**
     * Legt den Wert der roomNumber-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setRoomNumber(JAXBElement<String> value) {
        this.roomNumber = value;
    }

    /**
     * Ruft den Wert der salesVolume-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getSalesVolume() {
        return salesVolume;
    }

    /**
     * Legt den Wert der salesVolume-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setSalesVolume(JAXBElement<String> value) {
        this.salesVolume = value;
    }

    /**
     * Ruft den Wert der smtpPort-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSmtpPort() {
        return smtpPort;
    }

    /**
     * Legt den Wert der smtpPort-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSmtpPort(Integer value) {
        this.smtpPort = value;
    }

    /**
     * Ruft den Wert der smtpSchema-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getSmtpSchema() {
        return smtpSchema;
    }

    /**
     * Legt den Wert der smtpSchema-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setSmtpSchema(JAXBElement<String> value) {
        this.smtpSchema = value;
    }

    /**
     * Ruft den Wert der smtpServer-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getSmtpServer() {
        return smtpServer;
    }

    /**
     * Legt den Wert der smtpServer-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setSmtpServer(JAXBElement<String> value) {
        this.smtpServer = value;
    }

    /**
     * Ruft den Wert der smtpServerString-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getSmtpServerString() {
        return smtpServerString;
    }

    /**
     * Legt den Wert der smtpServerString-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setSmtpServerString(JAXBElement<String> value) {
        this.smtpServerString = value;
    }

    /**
     * Ruft den Wert der spouseName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getSpouseName() {
        return spouseName;
    }

    /**
     * Legt den Wert der spouseName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setSpouseName(JAXBElement<String> value) {
        this.spouseName = value;
    }

    /**
     * Ruft den Wert der stateBusiness-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getStateBusiness() {
        return stateBusiness;
    }

    /**
     * Legt den Wert der stateBusiness-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setStateBusiness(JAXBElement<String> value) {
        this.stateBusiness = value;
    }

    /**
     * Ruft den Wert der stateHome-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getStateHome() {
        return stateHome;
    }

    /**
     * Legt den Wert der stateHome-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setStateHome(JAXBElement<String> value) {
        this.stateHome = value;
    }

    /**
     * Ruft den Wert der stateOther-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getStateOther() {
        return stateOther;
    }

    /**
     * Legt den Wert der stateOther-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setStateOther(JAXBElement<String> value) {
        this.stateOther = value;
    }

    /**
     * Ruft den Wert der streetBusiness-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getStreetBusiness() {
        return streetBusiness;
    }

    /**
     * Legt den Wert der streetBusiness-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setStreetBusiness(JAXBElement<String> value) {
        this.streetBusiness = value;
    }

    /**
     * Ruft den Wert der streetHome-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getStreetHome() {
        return streetHome;
    }

    /**
     * Legt den Wert der streetHome-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setStreetHome(JAXBElement<String> value) {
        this.streetHome = value;
    }

    /**
     * Ruft den Wert der streetOther-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getStreetOther() {
        return streetOther;
    }

    /**
     * Legt den Wert der streetOther-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setStreetOther(JAXBElement<String> value) {
        this.streetOther = value;
    }

    /**
     * Ruft den Wert der suffix-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getSuffix() {
        return suffix;
    }

    /**
     * Legt den Wert der suffix-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setSuffix(JAXBElement<String> value) {
        this.suffix = value;
    }

    /**
     * Ruft den Wert der surName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getSurName() {
        return surName;
    }

    /**
     * Legt den Wert der surName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setSurName(JAXBElement<String> value) {
        this.surName = value;
    }

    /**
     * Ruft den Wert der taxId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTaxId() {
        return taxId;
    }

    /**
     * Legt den Wert der taxId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTaxId(JAXBElement<String> value) {
        this.taxId = value;
    }

    /**
     * Ruft den Wert der telephoneAssistant-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTelephoneAssistant() {
        return telephoneAssistant;
    }

    /**
     * Legt den Wert der telephoneAssistant-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTelephoneAssistant(JAXBElement<String> value) {
        this.telephoneAssistant = value;
    }

    /**
     * Ruft den Wert der telephoneBusiness1-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTelephoneBusiness1() {
        return telephoneBusiness1;
    }

    /**
     * Legt den Wert der telephoneBusiness1-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTelephoneBusiness1(JAXBElement<String> value) {
        this.telephoneBusiness1 = value;
    }

    /**
     * Ruft den Wert der telephoneBusiness2-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTelephoneBusiness2() {
        return telephoneBusiness2;
    }

    /**
     * Legt den Wert der telephoneBusiness2-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTelephoneBusiness2(JAXBElement<String> value) {
        this.telephoneBusiness2 = value;
    }

    /**
     * Ruft den Wert der telephoneCallback-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTelephoneCallback() {
        return telephoneCallback;
    }

    /**
     * Legt den Wert der telephoneCallback-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTelephoneCallback(JAXBElement<String> value) {
        this.telephoneCallback = value;
    }

    /**
     * Ruft den Wert der telephoneCar-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTelephoneCar() {
        return telephoneCar;
    }

    /**
     * Legt den Wert der telephoneCar-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTelephoneCar(JAXBElement<String> value) {
        this.telephoneCar = value;
    }

    /**
     * Ruft den Wert der telephoneCompany-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTelephoneCompany() {
        return telephoneCompany;
    }

    /**
     * Legt den Wert der telephoneCompany-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTelephoneCompany(JAXBElement<String> value) {
        this.telephoneCompany = value;
    }

    /**
     * Ruft den Wert der telephoneHome1-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTelephoneHome1() {
        return telephoneHome1;
    }

    /**
     * Legt den Wert der telephoneHome1-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTelephoneHome1(JAXBElement<String> value) {
        this.telephoneHome1 = value;
    }

    /**
     * Ruft den Wert der telephoneHome2-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTelephoneHome2() {
        return telephoneHome2;
    }

    /**
     * Legt den Wert der telephoneHome2-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTelephoneHome2(JAXBElement<String> value) {
        this.telephoneHome2 = value;
    }

    /**
     * Ruft den Wert der telephoneIp-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTelephoneIp() {
        return telephoneIp;
    }

    /**
     * Legt den Wert der telephoneIp-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTelephoneIp(JAXBElement<String> value) {
        this.telephoneIp = value;
    }

    /**
     * Ruft den Wert der telephoneIsdn-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTelephoneIsdn() {
        return telephoneIsdn;
    }

    /**
     * Legt den Wert der telephoneIsdn-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTelephoneIsdn(JAXBElement<String> value) {
        this.telephoneIsdn = value;
    }

    /**
     * Ruft den Wert der telephoneOther-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTelephoneOther() {
        return telephoneOther;
    }

    /**
     * Legt den Wert der telephoneOther-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTelephoneOther(JAXBElement<String> value) {
        this.telephoneOther = value;
    }

    /**
     * Ruft den Wert der telephonePager-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTelephonePager() {
        return telephonePager;
    }

    /**
     * Legt den Wert der telephonePager-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTelephonePager(JAXBElement<String> value) {
        this.telephonePager = value;
    }

    /**
     * Ruft den Wert der telephonePrimary-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTelephonePrimary() {
        return telephonePrimary;
    }

    /**
     * Legt den Wert der telephonePrimary-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTelephonePrimary(JAXBElement<String> value) {
        this.telephonePrimary = value;
    }

    /**
     * Ruft den Wert der telephoneRadio-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTelephoneRadio() {
        return telephoneRadio;
    }

    /**
     * Legt den Wert der telephoneRadio-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTelephoneRadio(JAXBElement<String> value) {
        this.telephoneRadio = value;
    }

    /**
     * Ruft den Wert der telephoneTelex-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTelephoneTelex() {
        return telephoneTelex;
    }

    /**
     * Legt den Wert der telephoneTelex-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTelephoneTelex(JAXBElement<String> value) {
        this.telephoneTelex = value;
    }

    /**
     * Ruft den Wert der telephoneTtytdd-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTelephoneTtytdd() {
        return telephoneTtytdd;
    }

    /**
     * Legt den Wert der telephoneTtytdd-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTelephoneTtytdd(JAXBElement<String> value) {
        this.telephoneTtytdd = value;
    }

    /**
     * Ruft den Wert der timezone-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTimezone() {
        return timezone;
    }

    /**
     * Legt den Wert der timezone-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTimezone(JAXBElement<String> value) {
        this.timezone = value;
    }

    /**
     * Ruft den Wert der title-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getTitle() {
        return title;
    }

    /**
     * Legt den Wert der title-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setTitle(JAXBElement<String> value) {
        this.title = value;
    }

    /**
     * Ruft den Wert der uploadFileSizeLimit-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getUploadFileSizeLimit() {
        return uploadFileSizeLimit;
    }

    /**
     * Legt den Wert der uploadFileSizeLimit-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setUploadFileSizeLimit(JAXBElement<Integer> value) {
        this.uploadFileSizeLimit = value;
    }

    /**
     * Ruft den Wert der uploadFileSizeLimitPerFile-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getUploadFileSizeLimitPerFile() {
        return uploadFileSizeLimitPerFile;
    }

    /**
     * Legt den Wert der uploadFileSizeLimitPerFile-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setUploadFileSizeLimitPerFile(JAXBElement<Integer> value) {
        this.uploadFileSizeLimitPerFile = value;
    }

    /**
     * Ruft den Wert der url-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUrl() {
        return url;
    }

    /**
     * Legt den Wert der url-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUrl(JAXBElement<String> value) {
        this.url = value;
    }

    /**
     * Ruft den Wert der userAttributes-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link SOAPStringMapMap }{@code >}
     *     
     */
    public JAXBElement<SOAPStringMapMap> getUserAttributes() {
        return userAttributes;
    }

    /**
     * Legt den Wert der userAttributes-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link SOAPStringMapMap }{@code >}
     *     
     */
    public void setUserAttributes(JAXBElement<SOAPStringMapMap> value) {
        this.userAttributes = value;
    }

    /**
     * Ruft den Wert der userfield01-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUserfield01() {
        return userfield01;
    }

    /**
     * Legt den Wert der userfield01-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUserfield01(JAXBElement<String> value) {
        this.userfield01 = value;
    }

    /**
     * Ruft den Wert der userfield02-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUserfield02() {
        return userfield02;
    }

    /**
     * Legt den Wert der userfield02-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUserfield02(JAXBElement<String> value) {
        this.userfield02 = value;
    }

    /**
     * Ruft den Wert der userfield03-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUserfield03() {
        return userfield03;
    }

    /**
     * Legt den Wert der userfield03-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUserfield03(JAXBElement<String> value) {
        this.userfield03 = value;
    }

    /**
     * Ruft den Wert der userfield04-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUserfield04() {
        return userfield04;
    }

    /**
     * Legt den Wert der userfield04-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUserfield04(JAXBElement<String> value) {
        this.userfield04 = value;
    }

    /**
     * Ruft den Wert der userfield05-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUserfield05() {
        return userfield05;
    }

    /**
     * Legt den Wert der userfield05-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUserfield05(JAXBElement<String> value) {
        this.userfield05 = value;
    }

    /**
     * Ruft den Wert der userfield06-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUserfield06() {
        return userfield06;
    }

    /**
     * Legt den Wert der userfield06-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUserfield06(JAXBElement<String> value) {
        this.userfield06 = value;
    }

    /**
     * Ruft den Wert der userfield07-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUserfield07() {
        return userfield07;
    }

    /**
     * Legt den Wert der userfield07-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUserfield07(JAXBElement<String> value) {
        this.userfield07 = value;
    }

    /**
     * Ruft den Wert der userfield08-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUserfield08() {
        return userfield08;
    }

    /**
     * Legt den Wert der userfield08-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUserfield08(JAXBElement<String> value) {
        this.userfield08 = value;
    }

    /**
     * Ruft den Wert der userfield09-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUserfield09() {
        return userfield09;
    }

    /**
     * Legt den Wert der userfield09-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUserfield09(JAXBElement<String> value) {
        this.userfield09 = value;
    }

    /**
     * Ruft den Wert der userfield10-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUserfield10() {
        return userfield10;
    }

    /**
     * Legt den Wert der userfield10-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUserfield10(JAXBElement<String> value) {
        this.userfield10 = value;
    }

    /**
     * Ruft den Wert der userfield11-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUserfield11() {
        return userfield11;
    }

    /**
     * Legt den Wert der userfield11-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUserfield11(JAXBElement<String> value) {
        this.userfield11 = value;
    }

    /**
     * Ruft den Wert der userfield12-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUserfield12() {
        return userfield12;
    }

    /**
     * Legt den Wert der userfield12-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUserfield12(JAXBElement<String> value) {
        this.userfield12 = value;
    }

    /**
     * Ruft den Wert der userfield13-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUserfield13() {
        return userfield13;
    }

    /**
     * Legt den Wert der userfield13-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUserfield13(JAXBElement<String> value) {
        this.userfield13 = value;
    }

    /**
     * Ruft den Wert der userfield14-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUserfield14() {
        return userfield14;
    }

    /**
     * Legt den Wert der userfield14-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUserfield14(JAXBElement<String> value) {
        this.userfield14 = value;
    }

    /**
     * Ruft den Wert der userfield15-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUserfield15() {
        return userfield15;
    }

    /**
     * Legt den Wert der userfield15-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUserfield15(JAXBElement<String> value) {
        this.userfield15 = value;
    }

    /**
     * Ruft den Wert der userfield16-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUserfield16() {
        return userfield16;
    }

    /**
     * Legt den Wert der userfield16-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUserfield16(JAXBElement<String> value) {
        this.userfield16 = value;
    }

    /**
     * Ruft den Wert der userfield17-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUserfield17() {
        return userfield17;
    }

    /**
     * Legt den Wert der userfield17-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUserfield17(JAXBElement<String> value) {
        this.userfield17 = value;
    }

    /**
     * Ruft den Wert der userfield18-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUserfield18() {
        return userfield18;
    }

    /**
     * Legt den Wert der userfield18-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUserfield18(JAXBElement<String> value) {
        this.userfield18 = value;
    }

    /**
     * Ruft den Wert der userfield19-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUserfield19() {
        return userfield19;
    }

    /**
     * Legt den Wert der userfield19-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUserfield19(JAXBElement<String> value) {
        this.userfield19 = value;
    }

    /**
     * Ruft den Wert der userfield20-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUserfield20() {
        return userfield20;
    }

    /**
     * Legt den Wert der userfield20-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUserfield20(JAXBElement<String> value) {
        this.userfield20 = value;
    }

}
