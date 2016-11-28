
package com.openexchange.admin.soap.user.dataobjects;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java-Klasse f\u00fcr User complex type.
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
 *         &lt;element name="filestoreId" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="filestore_name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
 *         &lt;element name="maxQuota" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
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
 *         &lt;element name="usedQuota" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
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
 *         &lt;element name="primaryAccountName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "filestoreId",
    "filestoreName",
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
    "mailFolderArchiveFullName",
    "mailenabled",
    "managerName",
    "maritalStatus",
    "maxQuota",
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
    "usedQuota",
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
    "userfield20",
    "primaryAccountName"
})
public class User {

    @XmlElement(nillable = true)
    protected List<String> aliases;
    @XmlElement(nillable = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar anniversary;
    @XmlElement(name = "assistant_name", nillable = true)
    protected String assistantName;
    @XmlElement(nillable = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar birthday;
    @XmlElement(nillable = true)
    protected String branches;
    @XmlElement(name = "business_category", nillable = true)
    protected String businessCategory;
    @XmlElement(nillable = true)
    protected String categories;
    @XmlElement(name = "cellular_telephone1", nillable = true)
    protected String cellularTelephone1;
    @XmlElement(name = "cellular_telephone2", nillable = true)
    protected String cellularTelephone2;
    @XmlElement(name = "city_business", nillable = true)
    protected String cityBusiness;
    @XmlElement(name = "city_home", nillable = true)
    protected String cityHome;
    @XmlElement(name = "city_other", nillable = true)
    protected String cityOther;
    @XmlElement(name = "commercial_register", nillable = true)
    protected String commercialRegister;
    @XmlElement(nillable = true)
    protected String company;
    protected Boolean contextadmin;
    @XmlElement(name = "country_business", nillable = true)
    protected String countryBusiness;
    @XmlElement(name = "country_home", nillable = true)
    protected String countryHome;
    @XmlElement(name = "country_other", nillable = true)
    protected String countryOther;
    @XmlElement(nillable = true)
    protected String defaultSenderAddress;
    @XmlElement(name = "default_group", nillable = true)
    protected Group defaultGroup;
    @XmlElement(nillable = true)
    protected String department;
    @XmlElement(name = "display_name", nillable = true)
    protected String displayName;
    @XmlElement(nillable = true)
    protected String email1;
    @XmlElement(nillable = true)
    protected String email2;
    @XmlElement(nillable = true)
    protected String email3;
    @XmlElement(nillable = true)
    protected String employeeType;
    @XmlElement(name = "fax_business", nillable = true)
    protected String faxBusiness;
    @XmlElement(name = "fax_home", nillable = true)
    protected String faxHome;
    @XmlElement(name = "fax_other", nillable = true)
    protected String faxOther;
    @XmlElement(nillable = true)
    protected Integer filestoreId;
    @XmlElement(name = "filestore_name", nillable = true)
    protected String filestoreName;
    @XmlElement(nillable = true)
    protected Integer folderTree;
    @XmlElement(name = "given_name", nillable = true)
    protected String givenName;
    @XmlElement(nillable = true)
    protected SOAPStringMap guiPreferencesForSoap;
    @XmlElement(name = "gui_spam_filter_enabled", nillable = true)
    protected Boolean guiSpamFilterEnabled;
    @XmlElement(nillable = true)
    protected Integer id;
    @XmlElement(nillable = true)
    protected String imapLogin;
    protected Integer imapPort;
    @XmlElement(nillable = true)
    protected String imapSchema;
    @XmlElement(nillable = true)
    protected String imapServer;
    @XmlElement(nillable = true)
    protected String imapServerString;
    @XmlElement(nillable = true)
    protected String info;
    @XmlElement(name = "instant_messenger1", nillable = true)
    protected String instantMessenger1;
    @XmlElement(name = "instant_messenger2", nillable = true)
    protected String instantMessenger2;
    @XmlElement(nillable = true)
    protected String language;
    @XmlElement(name = "mail_folder_confirmed_ham_name", nillable = true)
    protected String mailFolderConfirmedHamName;
    @XmlElement(name = "mail_folder_confirmed_spam_name", nillable = true)
    protected String mailFolderConfirmedSpamName;
    @XmlElement(name = "mail_folder_drafts_name", nillable = true)
    protected String mailFolderDraftsName;
    @XmlElement(name = "mail_folder_sent_name", nillable = true)
    protected String mailFolderSentName;
    @XmlElement(name = "mail_folder_spam_name", nillable = true)
    protected String mailFolderSpamName;
    @XmlElement(name = "mail_folder_trash_name", nillable = true)
    protected String mailFolderTrashName;
    @XmlElement(name = "mail_folder_archive_full_name", nillable = true)
    protected String mailFolderArchiveFullName;
    @XmlElement(nillable = true)
    protected Boolean mailenabled;
    @XmlElement(name = "manager_name", nillable = true)
    protected String managerName;
    @XmlElement(name = "marital_status", nillable = true)
    protected String maritalStatus;
    @XmlElement(nillable = true)
    protected Long maxQuota;
    @XmlElement(name = "middle_name", nillable = true)
    protected String middleName;
    @XmlElement(nillable = true)
    protected String name;
    @XmlElement(nillable = true)
    protected String nickname;
    @XmlElement(nillable = true)
    protected String note;
    @XmlElement(name = "number_of_children", nillable = true)
    protected String numberOfChildren;
    @XmlElement(name = "number_of_employee", nillable = true)
    protected String numberOfEmployee;
    @XmlElement(nillable = true)
    protected String password;
    @XmlElement(nillable = true)
    protected String passwordMech;
    @XmlElement(name = "password_expired", nillable = true)
    protected Boolean passwordExpired;
    @XmlElement(nillable = true)
    protected String position;
    @XmlElement(name = "postal_code_business", nillable = true)
    protected String postalCodeBusiness;
    @XmlElement(name = "postal_code_home", nillable = true)
    protected String postalCodeHome;
    @XmlElement(name = "postal_code_other", nillable = true)
    protected String postalCodeOther;
    @XmlElement(nillable = true)
    protected String primaryEmail;
    @XmlElement(nillable = true)
    protected String profession;
    @XmlElement(name = "room_number", nillable = true)
    protected String roomNumber;
    @XmlElement(name = "sales_volume", nillable = true)
    protected String salesVolume;
    protected Integer smtpPort;
    @XmlElement(nillable = true)
    protected String smtpSchema;
    @XmlElement(nillable = true)
    protected String smtpServer;
    @XmlElement(nillable = true)
    protected String smtpServerString;
    @XmlElement(name = "spouse_name", nillable = true)
    protected String spouseName;
    @XmlElement(name = "state_business", nillable = true)
    protected String stateBusiness;
    @XmlElement(name = "state_home", nillable = true)
    protected String stateHome;
    @XmlElement(name = "state_other", nillable = true)
    protected String stateOther;
    @XmlElement(name = "street_business", nillable = true)
    protected String streetBusiness;
    @XmlElement(name = "street_home", nillable = true)
    protected String streetHome;
    @XmlElement(name = "street_other", nillable = true)
    protected String streetOther;
    @XmlElement(nillable = true)
    protected String suffix;
    @XmlElement(name = "sur_name", nillable = true)
    protected String surName;
    @XmlElement(name = "tax_id", nillable = true)
    protected String taxId;
    @XmlElement(name = "telephone_assistant", nillable = true)
    protected String telephoneAssistant;
    @XmlElement(name = "telephone_business1", nillable = true)
    protected String telephoneBusiness1;
    @XmlElement(name = "telephone_business2", nillable = true)
    protected String telephoneBusiness2;
    @XmlElement(name = "telephone_callback", nillable = true)
    protected String telephoneCallback;
    @XmlElement(name = "telephone_car", nillable = true)
    protected String telephoneCar;
    @XmlElement(name = "telephone_company", nillable = true)
    protected String telephoneCompany;
    @XmlElement(name = "telephone_home1", nillable = true)
    protected String telephoneHome1;
    @XmlElement(name = "telephone_home2", nillable = true)
    protected String telephoneHome2;
    @XmlElement(name = "telephone_ip", nillable = true)
    protected String telephoneIp;
    @XmlElement(name = "telephone_isdn", nillable = true)
    protected String telephoneIsdn;
    @XmlElement(name = "telephone_other", nillable = true)
    protected String telephoneOther;
    @XmlElement(name = "telephone_pager", nillable = true)
    protected String telephonePager;
    @XmlElement(name = "telephone_primary", nillable = true)
    protected String telephonePrimary;
    @XmlElement(name = "telephone_radio", nillable = true)
    protected String telephoneRadio;
    @XmlElement(name = "telephone_telex", nillable = true)
    protected String telephoneTelex;
    @XmlElement(name = "telephone_ttytdd", nillable = true)
    protected String telephoneTtytdd;
    @XmlElement(nillable = true)
    protected String timezone;
    @XmlElement(nillable = true)
    protected String title;
    @XmlElement(nillable = true)
    protected Integer uploadFileSizeLimit;
    @XmlElement(nillable = true)
    protected Integer uploadFileSizeLimitPerFile;
    @XmlElement(nillable = true)
    protected String url;
    @XmlElement(nillable = true)
    protected Long usedQuota;
    @XmlElement(nillable = true)
    protected SOAPStringMapMap userAttributes;
    @XmlElement(nillable = true)
    protected String userfield01;
    @XmlElement(nillable = true)
    protected String userfield02;
    @XmlElement(nillable = true)
    protected String userfield03;
    @XmlElement(nillable = true)
    protected String userfield04;
    @XmlElement(nillable = true)
    protected String userfield05;
    @XmlElement(nillable = true)
    protected String userfield06;
    @XmlElement(nillable = true)
    protected String userfield07;
    @XmlElement(nillable = true)
    protected String userfield08;
    @XmlElement(nillable = true)
    protected String userfield09;
    @XmlElement(nillable = true)
    protected String userfield10;
    @XmlElement(nillable = true)
    protected String userfield11;
    @XmlElement(nillable = true)
    protected String userfield12;
    @XmlElement(nillable = true)
    protected String userfield13;
    @XmlElement(nillable = true)
    protected String userfield14;
    @XmlElement(nillable = true)
    protected String userfield15;
    @XmlElement(nillable = true)
    protected String userfield16;
    @XmlElement(nillable = true)
    protected String userfield17;
    @XmlElement(nillable = true)
    protected String userfield18;
    @XmlElement(nillable = true)
    protected String userfield19;
    @XmlElement(nillable = true)
    protected String userfield20;
    @XmlElement(nillable = true)
    private String primaryAccountName;

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
        return this.aliases;
    }

    /**
     * Ruft den Wert der anniversary-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public XMLGregorianCalendar getAnniversary() {
        return anniversary;
    }

    /**
     * Legt den Wert der anniversary-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public void setAnniversary(final XMLGregorianCalendar value) {
        this.anniversary = value;
    }

    /**
     * Ruft den Wert der assistantName-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAssistantName() {
        return assistantName;
    }

    /**
     * Legt den Wert der assistantName-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAssistantName(final String value) {
        this.assistantName = value;
    }

    /**
     * Ruft den Wert der birthday-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public XMLGregorianCalendar getBirthday() {
        return birthday;
    }

    /**
     * Legt den Wert der birthday-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *
     */
    public void setBirthday(final XMLGregorianCalendar value) {
        this.birthday = value;
    }

    /**
     * Ruft den Wert der branches-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getBranches() {
        return branches;
    }

    /**
     * Legt den Wert der branches-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setBranches(final String value) {
        this.branches = value;
    }

    /**
     * Ruft den Wert der businessCategory-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getBusinessCategory() {
        return businessCategory;
    }

    /**
     * Legt den Wert der businessCategory-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setBusinessCategory(final String value) {
        this.businessCategory = value;
    }

    /**
     * Ruft den Wert der categories-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCategories() {
        return categories;
    }

    /**
     * Legt den Wert der categories-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCategories(final String value) {
        this.categories = value;
    }

    /**
     * Ruft den Wert der cellularTelephone1-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCellularTelephone1() {
        return cellularTelephone1;
    }

    /**
     * Legt den Wert der cellularTelephone1-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCellularTelephone1(final String value) {
        this.cellularTelephone1 = value;
    }

    /**
     * Ruft den Wert der cellularTelephone2-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCellularTelephone2() {
        return cellularTelephone2;
    }

    /**
     * Legt den Wert der cellularTelephone2-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCellularTelephone2(final String value) {
        this.cellularTelephone2 = value;
    }

    /**
     * Ruft den Wert der cityBusiness-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCityBusiness() {
        return cityBusiness;
    }

    /**
     * Legt den Wert der cityBusiness-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCityBusiness(final String value) {
        this.cityBusiness = value;
    }

    /**
     * Ruft den Wert der cityHome-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCityHome() {
        return cityHome;
    }

    /**
     * Legt den Wert der cityHome-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCityHome(final String value) {
        this.cityHome = value;
    }

    /**
     * Ruft den Wert der cityOther-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCityOther() {
        return cityOther;
    }

    /**
     * Legt den Wert der cityOther-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCityOther(final String value) {
        this.cityOther = value;
    }

    /**
     * Ruft den Wert der commercialRegister-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCommercialRegister() {
        return commercialRegister;
    }

    /**
     * Legt den Wert der commercialRegister-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCommercialRegister(final String value) {
        this.commercialRegister = value;
    }

    /**
     * Ruft den Wert der company-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCompany() {
        return company;
    }

    /**
     * Legt den Wert der company-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCompany(final String value) {
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
    public void setContextadmin(final Boolean value) {
        this.contextadmin = value;
    }

    /**
     * Ruft den Wert der countryBusiness-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCountryBusiness() {
        return countryBusiness;
    }

    /**
     * Legt den Wert der countryBusiness-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCountryBusiness(final String value) {
        this.countryBusiness = value;
    }

    /**
     * Ruft den Wert der countryHome-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCountryHome() {
        return countryHome;
    }

    /**
     * Legt den Wert der countryHome-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCountryHome(final String value) {
        this.countryHome = value;
    }

    /**
     * Ruft den Wert der countryOther-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCountryOther() {
        return countryOther;
    }

    /**
     * Legt den Wert der countryOther-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCountryOther(final String value) {
        this.countryOther = value;
    }

    /**
     * Ruft den Wert der defaultSenderAddress-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDefaultSenderAddress() {
        return defaultSenderAddress;
    }

    /**
     * Legt den Wert der defaultSenderAddress-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDefaultSenderAddress(final String value) {
        this.defaultSenderAddress = value;
    }

    /**
     * Ruft den Wert der defaultGroup-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Group }
     *
     */
    public Group getDefaultGroup() {
        return defaultGroup;
    }

    /**
     * Legt den Wert der defaultGroup-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Group }
     *
     */
    public void setDefaultGroup(final Group value) {
        this.defaultGroup = value;
    }

    /**
     * Ruft den Wert der department-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDepartment() {
        return department;
    }

    /**
     * Legt den Wert der department-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDepartment(final String value) {
        this.department = value;
    }

    /**
     * Ruft den Wert der displayName-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Legt den Wert der displayName-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDisplayName(final String value) {
        this.displayName = value;
    }

    /**
     * Ruft den Wert der email1-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getEmail1() {
        return email1;
    }

    /**
     * Legt den Wert der email1-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setEmail1(final String value) {
        this.email1 = value;
    }

    /**
     * Ruft den Wert der email2-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getEmail2() {
        return email2;
    }

    /**
     * Legt den Wert der email2-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setEmail2(final String value) {
        this.email2 = value;
    }

    /**
     * Ruft den Wert der email3-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getEmail3() {
        return email3;
    }

    /**
     * Legt den Wert der email3-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setEmail3(final String value) {
        this.email3 = value;
    }

    /**
     * Ruft den Wert der employeeType-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getEmployeeType() {
        return employeeType;
    }

    /**
     * Legt den Wert der employeeType-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setEmployeeType(final String value) {
        this.employeeType = value;
    }

    /**
     * Ruft den Wert der faxBusiness-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFaxBusiness() {
        return faxBusiness;
    }

    /**
     * Legt den Wert der faxBusiness-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFaxBusiness(final String value) {
        this.faxBusiness = value;
    }

    /**
     * Ruft den Wert der faxHome-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFaxHome() {
        return faxHome;
    }

    /**
     * Legt den Wert der faxHome-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFaxHome(final String value) {
        this.faxHome = value;
    }

    /**
     * Ruft den Wert der faxOther-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFaxOther() {
        return faxOther;
    }

    /**
     * Legt den Wert der faxOther-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFaxOther(final String value) {
        this.faxOther = value;
    }

    /**
     * Ruft den Wert der filestoreId-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getFilestoreId() {
        return filestoreId;
    }

    /**
     * Legt den Wert der filestoreId-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setFilestoreId(Integer value) {
        this.filestoreId = value;
    }

    /**
     * Ruft den Wert der filestoreName-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFilestoreName() {
        return filestoreName;
    }

    /**
     * Legt den Wert der filestoreName-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFilestoreName(String value) {
        this.filestoreName = value;
    }

    /**
     * Ruft den Wert der folderTree-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getFolderTree() {
        return folderTree;
    }

    /**
     * Legt den Wert der folderTree-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setFolderTree(final Integer value) {
        this.folderTree = value;
    }

    /**
     * Ruft den Wert der givenName-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getGivenName() {
        return givenName;
    }

    /**
     * Legt den Wert der givenName-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setGivenName(final String value) {
        this.givenName = value;
    }

    /**
     * Ruft den Wert der guiPreferencesForSoap-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link SOAPStringMap }
     *
     */
    public SOAPStringMap getGuiPreferencesForSoap() {
        return guiPreferencesForSoap;
    }

    /**
     * Legt den Wert der guiPreferencesForSoap-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link SOAPStringMap }
     *
     */
    public void setGuiPreferencesForSoap(final SOAPStringMap value) {
        this.guiPreferencesForSoap = value;
    }

    /**
     * Ruft den Wert der guiSpamFilterEnabled-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isGuiSpamFilterEnabled() {
        return guiSpamFilterEnabled;
    }

    /**
     * Legt den Wert der guiSpamFilterEnabled-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setGuiSpamFilterEnabled(final Boolean value) {
        this.guiSpamFilterEnabled = value;
    }

    /**
     * Sets the aliases
     *
     * @param aliases The aliases to set
     */
    public void setAliases(final List<String> aliases) {
        this.aliases = aliases;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setId(final Integer value) {
        this.id = value;
    }

    /**
     * Ruft den Wert der imapLogin-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getImapLogin() {
        return imapLogin;
    }

    /**
     * Legt den Wert der imapLogin-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setImapLogin(final String value) {
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
    public void setImapPort(final Integer value) {
        this.imapPort = value;
    }

    /**
     * Ruft den Wert der imapSchema-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getImapSchema() {
        return imapSchema;
    }

    /**
     * Legt den Wert der imapSchema-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setImapSchema(final String value) {
        this.imapSchema = value;
    }

    /**
     * Ruft den Wert der imapServer-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getImapServer() {
        return null == imapServer ? imapServerString : imapServer;
    }

    /**
     * Legt den Wert der imapServer-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setImapServer(final String value) {
        this.imapServer = value;
    }

    /**
     * Ruft den Wert der imapServerString-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getImapServerString() {
        return imapServerString;
    }

    /**
     * Legt den Wert der imapServerString-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setImapServerString(final String value) {
        this.imapServerString = value;
    }

    /**
     * Ruft den Wert der info-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getInfo() {
        return info;
    }

    /**
     * Legt den Wert der info-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setInfo(final String value) {
        this.info = value;
    }

    /**
     * Ruft den Wert der instantMessenger1-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getInstantMessenger1() {
        return instantMessenger1;
    }

    /**
     * Legt den Wert der instantMessenger1-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setInstantMessenger1(final String value) {
        this.instantMessenger1 = value;
    }

    /**
     * Ruft den Wert der instantMessenger2-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getInstantMessenger2() {
        return instantMessenger2;
    }

    /**
     * Legt den Wert der instantMessenger2-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setInstantMessenger2(final String value) {
        this.instantMessenger2 = value;
    }

    /**
     * Ruft den Wert der language-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Legt den Wert der language-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setLanguage(final String value) {
        this.language = value;
    }

    /**
     * Ruft den Wert der mailFolderConfirmedHamName-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMailFolderConfirmedHamName() {
        return mailFolderConfirmedHamName;
    }

    /**
     * Legt den Wert der mailFolderConfirmedHamName-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMailFolderConfirmedHamName(final String value) {
        this.mailFolderConfirmedHamName = value;
    }

    /**
     * Ruft den Wert der mailFolderConfirmedSpamName-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMailFolderConfirmedSpamName() {
        return mailFolderConfirmedSpamName;
    }

    /**
     * Legt den Wert der mailFolderConfirmedSpamName-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMailFolderConfirmedSpamName(final String value) {
        this.mailFolderConfirmedSpamName = value;
    }

    /**
     * Ruft den Wert der mailFolderDraftsName-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMailFolderDraftsName() {
        return mailFolderDraftsName;
    }

    /**
     * Legt den Wert der mailFolderDraftsName-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMailFolderDraftsName(final String value) {
        this.mailFolderDraftsName = value;
    }

    /**
     * Ruft den Wert der mailFolderSentName-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMailFolderSentName() {
        return mailFolderSentName;
    }

    /**
     * Legt den Wert der mailFolderSentName-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMailFolderSentName(final String value) {
        this.mailFolderSentName = value;
    }

    /**
     * Ruft den Wert der mailFolderSpamName-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMailFolderSpamName() {
        return mailFolderSpamName;
    }

    /**
     * Legt den Wert der mailFolderSpamName-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMailFolderSpamName(final String value) {
        this.mailFolderSpamName = value;
    }

    /**
     * Ruft den Wert der mailFolderTrashName-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMailFolderTrashName() {
        return mailFolderTrashName;
    }

    /**
     * Legt den Wert der mailFolderTrashName-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMailFolderTrashName(final String value) {
        this.mailFolderTrashName = value;
    }

    /**
     * Ruft den Wert der mailFolderArchiveFullName-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMailFolderArchiveFullName() {
        return mailFolderArchiveFullName;
    }

    /**
     * Legt den Wert der mailFolderArchiveFullName-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMailFolderArchiveFullName(String value) {
        this.mailFolderArchiveFullName = value;
    }

    /**
     * Ruft den Wert der mailenabled-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isMailenabled() {
        return mailenabled;
    }

    /**
     * Legt den Wert der mailenabled-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setMailenabled(final Boolean value) {
        this.mailenabled = value;
    }

    /**
     * Ruft den Wert der managerName-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getManagerName() {
        return managerName;
    }

    /**
     * Legt den Wert der managerName-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setManagerName(final String value) {
        this.managerName = value;
    }

    /**
     * Ruft den Wert der maritalStatus-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMaritalStatus() {
        return maritalStatus;
    }

    /**
     * Legt den Wert der maritalStatus-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMaritalStatus(final String value) {
        this.maritalStatus = value;
    }

    /**
     * Ruft den Wert der maxQuota-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public Long getMaxQuota() {
        return maxQuota;
    }

    /**
     * Legt den Wert der maxQuota-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setMaxQuota(Long value) {
        this.maxQuota = value;
    }

    /**
     * Ruft den Wert der middleName-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getMiddleName() {
        return middleName;
    }

    /**
     * Legt den Wert der middleName-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMiddleName(final String value) {
        this.middleName = value;
    }

    /**
     * Ruft den Wert der name-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Legt den Wert der name-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setName(final String value) {
        this.name = value;
    }

    /**
     * Ruft den Wert der nickname-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Legt den Wert der nickname-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setNickname(final String value) {
        this.nickname = value;
    }

    /**
     * Ruft den Wert der note-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getNote() {
        return note;
    }

    /**
     * Legt den Wert der note-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setNote(final String value) {
        this.note = value;
    }

    /**
     * Ruft den Wert der numberOfChildren-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getNumberOfChildren() {
        return numberOfChildren;
    }

    /**
     * Legt den Wert der numberOfChildren-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setNumberOfChildren(final String value) {
        this.numberOfChildren = value;
    }

    /**
     * Ruft den Wert der numberOfEmployee-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getNumberOfEmployee() {
        return numberOfEmployee;
    }

    /**
     * Legt den Wert der numberOfEmployee-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setNumberOfEmployee(final String value) {
        this.numberOfEmployee = value;
    }

    /**
     * Ruft den Wert der password-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPassword() {
        return password;
    }

    /**
     * Legt den Wert der password-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPassword(final String value) {
        this.password = value;
    }

    /**
     * Ruft den Wert der passwordMech-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPasswordMech() {
        return passwordMech;
    }

    /**
     * Legt den Wert der passwordMech-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPasswordMech(final String value) {
        this.passwordMech = value;
    }

    /**
     * Ruft den Wert der passwordExpired-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isPasswordExpired() {
        return passwordExpired;
    }

    /**
     * Legt den Wert der passwordExpired-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setPasswordExpired(final Boolean value) {
        this.passwordExpired = value;
    }

    /**
     * Ruft den Wert der position-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPosition() {
        return position;
    }

    /**
     * Legt den Wert der position-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPosition(final String value) {
        this.position = value;
    }

    /**
     * Ruft den Wert der postalCodeBusiness-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPostalCodeBusiness() {
        return postalCodeBusiness;
    }

    /**
     * Legt den Wert der postalCodeBusiness-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPostalCodeBusiness(final String value) {
        this.postalCodeBusiness = value;
    }

    /**
     * Ruft den Wert der postalCodeHome-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPostalCodeHome() {
        return postalCodeHome;
    }

    /**
     * Legt den Wert der postalCodeHome-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPostalCodeHome(final String value) {
        this.postalCodeHome = value;
    }

    /**
     * Ruft den Wert der postalCodeOther-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPostalCodeOther() {
        return postalCodeOther;
    }

    /**
     * Legt den Wert der postalCodeOther-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPostalCodeOther(final String value) {
        this.postalCodeOther = value;
    }

    /**
     * Ruft den Wert der primaryEmail-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getPrimaryEmail() {
        return primaryEmail;
    }

    /**
     * Legt den Wert der primaryEmail-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setPrimaryEmail(final String value) {
        this.primaryEmail = value;
    }

    /**
     * Ruft den Wert der profession-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getProfession() {
        return profession;
    }

    /**
     * Legt den Wert der profession-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setProfession(final String value) {
        this.profession = value;
    }

    /**
     * Ruft den Wert der roomNumber-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getRoomNumber() {
        return roomNumber;
    }

    /**
     * Legt den Wert der roomNumber-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setRoomNumber(final String value) {
        this.roomNumber = value;
    }

    /**
     * Ruft den Wert der salesVolume-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSalesVolume() {
        return salesVolume;
    }

    /**
     * Legt den Wert der salesVolume-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSalesVolume(final String value) {
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
    public void setSmtpPort(final Integer value) {
        this.smtpPort = value;
    }

    /**
     * Ruft den Wert der smtpSchema-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSmtpSchema() {
        return smtpSchema;
    }

    /**
     * Legt den Wert der smtpSchema-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSmtpSchema(final String value) {
        this.smtpSchema = value;
    }

    /**
     * Ruft den Wert der smtpServer-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSmtpServer() {
        return null == smtpServer ? smtpServerString : smtpServer;
    }

    /**
     * Legt den Wert der smtpServer-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSmtpServer(final String value) {
        this.smtpServer = value;
    }

    private static final Pattern URL_PATTERN = Pattern.compile("^(.*?://)?(.*?)(:(.*?))?$");

    private String getSmtpServer0() {
        if (this.smtpServer != null) {
            final Matcher matcher = URL_PATTERN.matcher(smtpServerString);
            if (matcher.matches() && null != matcher.group(2)) {
                return matcher.group(2);
            }
        }
        return null;
    }

    private String getSmtpSchema0() {
        if (this.smtpServer != null) {
            final Matcher matcher = URL_PATTERN.matcher(smtpServerString);
            if (matcher.matches() && null != matcher.group(1)) {
                return matcher.group(1);
            }
        }
        return "smtp://";
    }

    /**
     * Ruft den Wert der smtpServerString-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSmtpServerString() {
        if (smtpServerString == null) {
            return null;
        }
        int port = 25;
        final Matcher matcher = URL_PATTERN.matcher(smtpServerString);
        if (matcher.matches() && null != matcher.group(4)) {
            try {
                port = Integer.parseInt(matcher.group(4));
            } catch (final NumberFormatException e) {
                port = 25;
            }
        }
        if (port == 143) {
            smtpServerString = getSmtpSchema0() + getSmtpServer0() + ':' + "25";
        } else if (port == 993) {
            smtpServerString = getSmtpSchema0() + getSmtpServer0() + ':' + "465";
        }
        return smtpServerString;
    }

    /**
     * Legt den Wert der smtpServerString-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSmtpServerString(final String value) {
        this.smtpServerString = value;
    }

    /**
     * Ruft den Wert der spouseName-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSpouseName() {
        return spouseName;
    }

    /**
     * Legt den Wert der spouseName-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSpouseName(final String value) {
        this.spouseName = value;
    }

    /**
     * Ruft den Wert der stateBusiness-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getStateBusiness() {
        return stateBusiness;
    }

    /**
     * Legt den Wert der stateBusiness-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setStateBusiness(final String value) {
        this.stateBusiness = value;
    }

    /**
     * Ruft den Wert der stateHome-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getStateHome() {
        return stateHome;
    }

    /**
     * Legt den Wert der stateHome-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setStateHome(final String value) {
        this.stateHome = value;
    }

    /**
     * Ruft den Wert der stateOther-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getStateOther() {
        return stateOther;
    }

    /**
     * Legt den Wert der stateOther-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setStateOther(final String value) {
        this.stateOther = value;
    }

    /**
     * Ruft den Wert der streetBusiness-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getStreetBusiness() {
        return streetBusiness;
    }

    /**
     * Legt den Wert der streetBusiness-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setStreetBusiness(final String value) {
        this.streetBusiness = value;
    }

    /**
     * Ruft den Wert der streetHome-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getStreetHome() {
        return streetHome;
    }

    /**
     * Legt den Wert der streetHome-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setStreetHome(final String value) {
        this.streetHome = value;
    }

    /**
     * Ruft den Wert der streetOther-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getStreetOther() {
        return streetOther;
    }

    /**
     * Legt den Wert der streetOther-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setStreetOther(final String value) {
        this.streetOther = value;
    }

    /**
     * Ruft den Wert der suffix-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * Legt den Wert der suffix-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSuffix(final String value) {
        this.suffix = value;
    }

    /**
     * Ruft den Wert der surName-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSurName() {
        return surName;
    }

    /**
     * Legt den Wert der surName-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSurName(final String value) {
        this.surName = value;
    }

    /**
     * Ruft den Wert der taxId-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTaxId() {
        return taxId;
    }

    /**
     * Legt den Wert der taxId-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTaxId(final String value) {
        this.taxId = value;
    }

    /**
     * Ruft den Wert der telephoneAssistant-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTelephoneAssistant() {
        return telephoneAssistant;
    }

    /**
     * Legt den Wert der telephoneAssistant-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTelephoneAssistant(final String value) {
        this.telephoneAssistant = value;
    }

    /**
     * Ruft den Wert der telephoneBusiness1-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTelephoneBusiness1() {
        return telephoneBusiness1;
    }

    /**
     * Legt den Wert der telephoneBusiness1-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTelephoneBusiness1(final String value) {
        this.telephoneBusiness1 = value;
    }

    /**
     * Ruft den Wert der telephoneBusiness2-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTelephoneBusiness2() {
        return telephoneBusiness2;
    }

    /**
     * Legt den Wert der telephoneBusiness2-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTelephoneBusiness2(final String value) {
        this.telephoneBusiness2 = value;
    }

    /**
     * Ruft den Wert der telephoneCallback-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTelephoneCallback() {
        return telephoneCallback;
    }

    /**
     * Legt den Wert der telephoneCallback-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTelephoneCallback(final String value) {
        this.telephoneCallback = value;
    }

    /**
     * Ruft den Wert der telephoneCar-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTelephoneCar() {
        return telephoneCar;
    }

    /**
     * Legt den Wert der telephoneCar-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTelephoneCar(final String value) {
        this.telephoneCar = value;
    }

    /**
     * Ruft den Wert der telephoneCompany-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTelephoneCompany() {
        return telephoneCompany;
    }

    /**
     * Legt den Wert der telephoneCompany-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTelephoneCompany(final String value) {
        this.telephoneCompany = value;
    }

    /**
     * Ruft den Wert der telephoneHome1-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTelephoneHome1() {
        return telephoneHome1;
    }

    /**
     * Legt den Wert der telephoneHome1-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTelephoneHome1(final String value) {
        this.telephoneHome1 = value;
    }

    /**
     * Ruft den Wert der telephoneHome2-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTelephoneHome2() {
        return telephoneHome2;
    }

    /**
     * Legt den Wert der telephoneHome2-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTelephoneHome2(final String value) {
        this.telephoneHome2 = value;
    }

    /**
     * Ruft den Wert der telephoneIp-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTelephoneIp() {
        return telephoneIp;
    }

    /**
     * Legt den Wert der telephoneIp-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTelephoneIp(final String value) {
        this.telephoneIp = value;
    }

    /**
     * Ruft den Wert der telephoneIsdn-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTelephoneIsdn() {
        return telephoneIsdn;
    }

    /**
     * Legt den Wert der telephoneIsdn-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTelephoneIsdn(final String value) {
        this.telephoneIsdn = value;
    }

    /**
     * Ruft den Wert der telephoneOther-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTelephoneOther() {
        return telephoneOther;
    }

    /**
     * Legt den Wert der telephoneOther-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTelephoneOther(final String value) {
        this.telephoneOther = value;
    }

    /**
     * Ruft den Wert der telephonePager-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTelephonePager() {
        return telephonePager;
    }

    /**
     * Legt den Wert der telephonePager-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTelephonePager(final String value) {
        this.telephonePager = value;
    }

    /**
     * Ruft den Wert der telephonePrimary-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTelephonePrimary() {
        return telephonePrimary;
    }

    /**
     * Legt den Wert der telephonePrimary-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTelephonePrimary(final String value) {
        this.telephonePrimary = value;
    }

    /**
     * Ruft den Wert der telephoneRadio-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTelephoneRadio() {
        return telephoneRadio;
    }

    /**
     * Legt den Wert der telephoneRadio-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTelephoneRadio(final String value) {
        this.telephoneRadio = value;
    }

    /**
     * Ruft den Wert der telephoneTelex-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTelephoneTelex() {
        return telephoneTelex;
    }

    /**
     * Legt den Wert der telephoneTelex-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTelephoneTelex(final String value) {
        this.telephoneTelex = value;
    }

    /**
     * Ruft den Wert der telephoneTtytdd-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTelephoneTtytdd() {
        return telephoneTtytdd;
    }

    /**
     * Legt den Wert der telephoneTtytdd-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTelephoneTtytdd(final String value) {
        this.telephoneTtytdd = value;
    }

    /**
     * Ruft den Wert der timezone-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTimezone() {
        return timezone;
    }

    /**
     * Legt den Wert der timezone-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTimezone(final String value) {
        this.timezone = value;
    }

    /**
     * Ruft den Wert der title-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getTitle() {
        return title;
    }

    /**
     * Legt den Wert der title-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setTitle(final String value) {
        this.title = value;
    }

    /**
     * Ruft den Wert der uploadFileSizeLimit-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getUploadFileSizeLimit() {
        return uploadFileSizeLimit;
    }

    /**
     * Legt den Wert der uploadFileSizeLimit-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setUploadFileSizeLimit(final Integer value) {
        this.uploadFileSizeLimit = value;
    }

    /**
     * Ruft den Wert der uploadFileSizeLimitPerFile-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public Integer getUploadFileSizeLimitPerFile() {
        return uploadFileSizeLimitPerFile;
    }

    /**
     * Legt den Wert der uploadFileSizeLimitPerFile-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setUploadFileSizeLimitPerFile(final Integer value) {
        this.uploadFileSizeLimitPerFile = value;
    }

    /**
     * Ruft den Wert der url-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUrl() {
        return url;
    }

    /**
     * Legt den Wert der url-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUrl(final String value) {
        this.url = value;
    }

    /**
     * Ruft den Wert der usedQuota-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    public Long getUsedQuota() {
        return usedQuota;
    }

    /**
     * Legt den Wert der usedQuota-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setUsedQuota(Long value) {
        this.usedQuota = value;
    }

    /**
     * Ruft den Wert der userAttributes-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link SOAPStringMapMap }
     *
     */
    public SOAPStringMapMap getUserAttributes() {
        return userAttributes;
    }

    /**
     * Legt den Wert der userAttributes-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link SOAPStringMapMap }
     *
     */
    public void setUserAttributes(final SOAPStringMapMap value) {
        this.userAttributes = value;
    }

    /**
     * Ruft den Wert der userfield01-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserfield01() {
        return userfield01;
    }

    /**
     * Legt den Wert der userfield01-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserfield01(final String value) {
        this.userfield01 = value;
    }

    /**
     * Ruft den Wert der userfield02-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserfield02() {
        return userfield02;
    }

    /**
     * Legt den Wert der userfield02-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserfield02(final String value) {
        this.userfield02 = value;
    }

    /**
     * Ruft den Wert der userfield03-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserfield03() {
        return userfield03;
    }

    /**
     * Legt den Wert der userfield03-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserfield03(final String value) {
        this.userfield03 = value;
    }

    /**
     * Ruft den Wert der userfield04-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserfield04() {
        return userfield04;
    }

    /**
     * Legt den Wert der userfield04-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserfield04(final String value) {
        this.userfield04 = value;
    }

    /**
     * Ruft den Wert der userfield05-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserfield05() {
        return userfield05;
    }

    /**
     * Legt den Wert der userfield05-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserfield05(final String value) {
        this.userfield05 = value;
    }

    /**
     * Ruft den Wert der userfield06-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserfield06() {
        return userfield06;
    }

    /**
     * Legt den Wert der userfield06-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserfield06(final String value) {
        this.userfield06 = value;
    }

    /**
     * Ruft den Wert der userfield07-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserfield07() {
        return userfield07;
    }

    /**
     * Legt den Wert der userfield07-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserfield07(final String value) {
        this.userfield07 = value;
    }

    /**
     * Ruft den Wert der userfield08-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserfield08() {
        return userfield08;
    }

    /**
     * Legt den Wert der userfield08-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserfield08(final String value) {
        this.userfield08 = value;
    }

    /**
     * Ruft den Wert der userfield09-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserfield09() {
        return userfield09;
    }

    /**
     * Legt den Wert der userfield09-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserfield09(final String value) {
        this.userfield09 = value;
    }

    /**
     * Ruft den Wert der userfield10-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserfield10() {
        return userfield10;
    }

    /**
     * Legt den Wert der userfield10-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserfield10(final String value) {
        this.userfield10 = value;
    }

    /**
     * Ruft den Wert der userfield11-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserfield11() {
        return userfield11;
    }

    /**
     * Legt den Wert der userfield11-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserfield11(final String value) {
        this.userfield11 = value;
    }

    /**
     * Ruft den Wert der userfield12-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserfield12() {
        return userfield12;
    }

    /**
     * Legt den Wert der userfield12-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserfield12(final String value) {
        this.userfield12 = value;
    }

    /**
     * Ruft den Wert der userfield13-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserfield13() {
        return userfield13;
    }

    /**
     * Legt den Wert der userfield13-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserfield13(final String value) {
        this.userfield13 = value;
    }

    /**
     * Ruft den Wert der userfield14-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserfield14() {
        return userfield14;
    }

    /**
     * Legt den Wert der userfield14-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserfield14(final String value) {
        this.userfield14 = value;
    }

    /**
     * Ruft den Wert der userfield15-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserfield15() {
        return userfield15;
    }

    /**
     * Legt den Wert der userfield15-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserfield15(final String value) {
        this.userfield15 = value;
    }

    /**
     * Ruft den Wert der userfield16-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserfield16() {
        return userfield16;
    }

    /**
     * Legt den Wert der userfield16-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserfield16(final String value) {
        this.userfield16 = value;
    }

    /**
     * Ruft den Wert der userfield17-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserfield17() {
        return userfield17;
    }

    /**
     * Legt den Wert der userfield17-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserfield17(final String value) {
        this.userfield17 = value;
    }

    /**
     * Ruft den Wert der userfield18-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserfield18() {
        return userfield18;
    }

    /**
     * Legt den Wert der userfield18-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserfield18(final String value) {
        this.userfield18 = value;
    }

    /**
     * Ruft den Wert der userfield19-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserfield19() {
        return userfield19;
    }

    /**
     * Legt den Wert der userfield19-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserfield19(final String value) {
        this.userfield19 = value;
    }

    /**
     * Ruft den Wert der userfield20-Eigenschaft ab.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUserfield20() {
        return userfield20;
    }

    /**
     * Legt den Wert der userfield20-Eigenschaft fest.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUserfield20(final String value) {
        this.userfield20 = value;
    }

    /**
     * Gets the primaryAccountName
     *
     * @return The primaryAccountName
     */
    public String getPrimaryAccountName() {
        return primaryAccountName;
    }

    /**
     * Sets the primaryAccountName
     *
     * @param primaryAccountName The primaryAccountName to set
     */
    public void setPrimaryAccountName(String primaryAccountName) {
        this.primaryAccountName = primaryAccountName;
    }

}
