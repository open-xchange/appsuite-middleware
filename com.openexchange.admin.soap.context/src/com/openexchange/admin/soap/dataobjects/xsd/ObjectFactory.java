
package com.openexchange.admin.soap.dataobjects.xsd;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.openexchange.admin.soap.dataobjects.xsd package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ContextEnabled_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "enabled");
    private final static QName _ContextId_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "id");
    private final static QName _ContextWriteDatabase_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "writeDatabase");
    private final static QName _ContextFilestoreId_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "filestoreId");
    private final static QName _ContextName_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "name");
    private final static QName _ContextUsedQuota_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "usedQuota");
    private final static QName _ContextMaxQuota_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "maxQuota");
    private final static QName _ContextReadDatabase_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "readDatabase");
    private final static QName _ContextFilestoreName_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "filestore_name");
    private final static QName _ContextAverageSize_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "average_size");
    private final static QName _ContextUserAttributes_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "userAttributes");
    private final static QName _EntryValue_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "value");
    private final static QName _EntryKey_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "key");
    private final static QName _UserTelephoneCallback_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "telephone_callback");
    private final static QName _UserNickname_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "nickname");
    private final static QName _UserMiddleName_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "middle_name");
    private final static QName _UserPostalCodeBusiness_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "postal_code_business");
    private final static QName _UserMailFolderSpamName_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "mail_folder_spam_name");
    private final static QName _UserDisplayName_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "display_name");
    private final static QName _UserSpouseName_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "spouse_name");
    private final static QName _UserLanguage_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "language");
    private final static QName _UserUploadFileSizeLimit_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "uploadFileSizeLimit");
    private final static QName _UserProfession_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "profession");
    private final static QName _UserNumberOfEmployee_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "number_of_employee");
    private final static QName _UserImapServer_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "imapServer");
    private final static QName _UserStateHome_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "state_home");
    private final static QName _UserInfo_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "info");
    private final static QName _UserSurName_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "sur_name");
    private final static QName _UserUploadFileSizeLimitPerFile_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "uploadFileSizeLimitPerFile");
    private final static QName _UserTelephoneIsdn_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "telephone_isdn");
    private final static QName _UserPostalCodeOther_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "postal_code_other");
    private final static QName _UserSmtpServer_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "smtpServer");
    private final static QName _UserFaxHome_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "fax_home");
    private final static QName _UserMaritalStatus_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "marital_status");
    private final static QName _UserCellularTelephone1_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "cellular_telephone1");
    private final static QName _UserCellularTelephone2_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "cellular_telephone2");
    private final static QName _UserMailenabled_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "mailenabled");
    private final static QName _UserMailFolderDraftsName_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "mail_folder_drafts_name");
    private final static QName _UserBranches_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "branches");
    private final static QName _UserTelephoneHome1_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "telephone_home1");
    private final static QName _UserNumberOfChildren_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "number_of_children");
    private final static QName _UserUserfield16_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "userfield16");
    private final static QName _UserUserfield17_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "userfield17");
    private final static QName _UserUserfield18_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "userfield18");
    private final static QName _UserMailFolderConfirmedSpamName_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "mail_folder_confirmed_spam_name");
    private final static QName _UserUserfield19_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "userfield19");
    private final static QName _UserUserfield12_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "userfield12");
    private final static QName _UserUserfield13_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "userfield13");
    private final static QName _UserUserfield14_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "userfield14");
    private final static QName _UserUserfield15_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "userfield15");
    private final static QName _UserTelephoneHome2_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "telephone_home2");
    private final static QName _UserUserfield11_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "userfield11");
    private final static QName _UserCountryBusiness_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "country_business");
    private final static QName _UserPasswordExpired_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "password_expired");
    private final static QName _UserUserfield10_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "userfield10");
    private final static QName _UserCountryOther_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "country_other");
    private final static QName _UserTelephoneIp_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "telephone_ip");
    private final static QName _UserTelephoneOther_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "telephone_other");
    private final static QName _UserManagerName_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "manager_name");
    private final static QName _UserDefaultSenderAddress_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "defaultSenderAddress");
    private final static QName _UserTelephoneTtytdd_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "telephone_ttytdd");
    private final static QName _UserPosition_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "position");
    private final static QName _UserSalesVolume_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "sales_volume");
    private final static QName _UserUserfield09_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "userfield09");
    private final static QName _UserUserfield07_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "userfield07");
    private final static QName _UserUserfield08_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "userfield08");
    private final static QName _UserUserfield05_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "userfield05");
    private final static QName _UserUserfield06_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "userfield06");
    private final static QName _UserUserfield03_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "userfield03");
    private final static QName _UserUserfield04_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "userfield04");
    private final static QName _UserUserfield01_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "userfield01");
    private final static QName _UserPasswordMech_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "passwordMech");
    private final static QName _UserUserfield02_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "userfield02");
    private final static QName _UserTitle_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "title");
    private final static QName _UserMailFolderSentName_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "mail_folder_sent_name");
    private final static QName _UserTaxId_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "tax_id");
    private final static QName _UserNote_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "note");
    private final static QName _UserCityOther_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "city_other");
    private final static QName _UserEmployeeType_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "employeeType");
    private final static QName _UserUrl_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "url");
    private final static QName _UserPrimaryEmail_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "primaryEmail");
    private final static QName _UserAssistantName_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "assistant_name");
    private final static QName _UserCityBusiness_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "city_business");
    private final static QName _UserCompany_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "company");
    private final static QName _UserSmtpSchema_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "smtpSchema");
    private final static QName _UserTelephoneCar_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "telephone_car");
    private final static QName _UserTelephoneAssistant_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "telephone_assistant");
    private final static QName _UserPassword_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "password");
    private final static QName _UserGuiPreferencesForSoap_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "guiPreferencesForSoap");
    private final static QName _UserCityHome_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "city_home");
    private final static QName _UserStateOther_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "state_other");
    private final static QName _UserTimezone_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "timezone");
    private final static QName _UserMailFolderConfirmedHamName_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "mail_folder_confirmed_ham_name");
    private final static QName _UserStreetHome_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "street_home");
    private final static QName _UserSmtpServerString_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "smtpServerString");
    private final static QName _UserTelephoneRadio_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "telephone_radio");
    private final static QName _UserCountryHome_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "country_home");
    private final static QName _UserMailFolderTrashName_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "mail_folder_trash_name");
    private final static QName _UserUserfield20_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "userfield20");
    private final static QName _UserTelephoneBusiness2_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "telephone_business2");
    private final static QName _UserRoomNumber_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "room_number");
    private final static QName _UserTelephoneBusiness1_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "telephone_business1");
    private final static QName _UserAnniversary_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "anniversary");
    private final static QName _UserEmail1_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "email1");
    private final static QName _UserSuffix_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "suffix");
    private final static QName _UserStreetOther_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "street_other");
    private final static QName _UserEmail3_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "email3");
    private final static QName _UserEmail2_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "email2");
    private final static QName _UserImapLogin_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "imapLogin");
    private final static QName _UserFaxBusiness_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "fax_business");
    private final static QName _UserDefaultGroup_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "default_group");
    private final static QName _UserPostalCodeHome_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "postal_code_home");
    private final static QName _UserStreetBusiness_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "street_business");
    private final static QName _UserImapSchema_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "imapSchema");
    private final static QName _UserStateBusiness_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "state_business");
    private final static QName _UserTelephonePrimary_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "telephone_primary");
    private final static QName _UserCategories_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "categories");
    private final static QName _UserCommercialRegister_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "commercial_register");
    private final static QName _UserInstantMessenger1_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "instant_messenger1");
    private final static QName _UserBirthday_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "birthday");
    private final static QName _UserTelephonePager_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "telephone_pager");
    private final static QName _UserInstantMessenger2_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "instant_messenger2");
    private final static QName _UserBusinessCategory_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "business_category");
    private final static QName _UserDepartment_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "department");
    private final static QName _UserTelephoneTelex_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "telephone_telex");
    private final static QName _UserImapServerString_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "imapServerString");
    private final static QName _UserGivenName_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "given_name");
    private final static QName _UserGuiSpamFilterEnabled_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "gui_spam_filter_enabled");
    private final static QName _UserFolderTree_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "folderTree");
    private final static QName _UserTelephoneCompany_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "telephone_company");
    private final static QName _UserFaxOther_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "fax_other");
    private final static QName _GroupDisplayname_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "displayname");
    private final static QName _UserModuleAccessRssPortal_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "rssPortal");
    private final static QName _UserModuleAccessWebmail_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "webmail");
    private final static QName _UserModuleAccessEditGroup_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "editGroup");
    private final static QName _UserModuleAccessTasks_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "tasks");
    private final static QName _UserModuleAccessPublication_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "publication");
    private final static QName _UserModuleAccessVcard_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "vcard");
    private final static QName _UserModuleAccessProjects_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "projects");
    private final static QName _UserModuleAccessSyncml_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "syncml");
    private final static QName _UserModuleAccessPublicFolderEditable_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "publicFolderEditable");
    private final static QName _UserModuleAccessCollectEmailAddresses_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "collectEmailAddresses");
    private final static QName _UserModuleAccessIcal_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "ical");
    private final static QName _UserModuleAccessEditPublicFolders_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "editPublicFolders");
    private final static QName _UserModuleAccessDelegateTask_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "delegateTask");
    private final static QName _UserModuleAccessUSM_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "USM");
    private final static QName _UserModuleAccessForum_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "forum");
    private final static QName _UserModuleAccessGlobalAddressBookDisabled_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "globalAddressBookDisabled");
    private final static QName _UserModuleAccessMultipleMailAccounts_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "multipleMailAccounts");
    private final static QName _UserModuleAccessRssBookmarks_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "rssBookmarks");
    private final static QName _UserModuleAccessWebdav_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "webdav");
    private final static QName _UserModuleAccessEditPassword_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "editPassword");
    private final static QName _UserModuleAccessContacts_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "contacts");
    private final static QName _UserModuleAccessWebdavXml_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "webdavXml");
    private final static QName _UserModuleAccessEditResource_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "editResource");
    private final static QName _UserModuleAccessInfostore_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "infostore");
    private final static QName _UserModuleAccessOLOX20_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "OLOX20");
    private final static QName _UserModuleAccessReadCreateSharedFolders_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "readCreateSharedFolders");
    private final static QName _UserModuleAccessSubscription_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "subscription");
    private final static QName _UserModuleAccessPinboardWrite_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "pinboardWrite");
    private final static QName _UserModuleAccessActiveSync_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "activeSync");
    private final static QName _UserModuleAccessCalendar_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "calendar");
    private final static QName _UserModuleAccessDeniedPortal_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "deniedPortal");
    private final static QName _DatabaseMasterId_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "masterId");
    private final static QName _DatabaseMaxUnits_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "maxUnits");
    private final static QName _DatabasePoolMax_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "poolMax");
    private final static QName _DatabaseCurrentUnits_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "currentUnits");
    private final static QName _DatabaseReadId_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "read_id");
    private final static QName _DatabaseMaster_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "master");
    private final static QName _DatabaseScheme_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "scheme");
    private final static QName _DatabasePoolInitial_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "poolInitial");
    private final static QName _DatabaseDriver_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "driver");
    private final static QName _DatabaseLogin_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "login");
    private final static QName _DatabasePoolHardLimit_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "poolHardLimit");
    private final static QName _DatabaseClusterWeight_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "clusterWeight");
    private final static QName _FilestoreMaxContexts_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "maxContexts");
    private final static QName _FilestoreReserved_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "reserved");
    private final static QName _FilestoreUsed_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "used");
    private final static QName _FilestoreCurrentContexts_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "currentContexts");
    private final static QName _FilestoreSize_QNAME = new QName("http://dataobjects.soap.admin.openexchange.com/xsd", "size");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.openexchange.admin.soap.dataobjects.xsd
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link User }
     * 
     */
    public User createUser() {
        return new User();
    }

    /**
     * Create an instance of {@link Filestore }
     * 
     */
    public Filestore createFilestore() {
        return new Filestore();
    }

    /**
     * Create an instance of {@link Entry }
     * 
     */
    public Entry createEntry() {
        return new Entry();
    }

    /**
     * Create an instance of {@link SOAPStringMap }
     * 
     */
    public SOAPStringMap createSOAPStringMap() {
        return new SOAPStringMap();
    }

    /**
     * Create an instance of {@link Context }
     * 
     */
    public Context createContext() {
        return new Context();
    }

    /**
     * Create an instance of {@link SOAPStringMapMap }
     * 
     */
    public SOAPStringMapMap createSOAPStringMapMap() {
        return new SOAPStringMapMap();
    }

    /**
     * Create an instance of {@link Database }
     * 
     */
    public Database createDatabase() {
        return new Database();
    }

    /**
     * Create an instance of {@link SOAPMapEntry }
     * 
     */
    public SOAPMapEntry createSOAPMapEntry() {
        return new SOAPMapEntry();
    }

    /**
     * Create an instance of {@link UserModuleAccess }
     * 
     */
    public UserModuleAccess createUserModuleAccess() {
        return new UserModuleAccess();
    }

    /**
     * Create an instance of {@link Group }
     * 
     */
    public Group createGroup() {
        return new Group();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "enabled", scope = Context.class)
    public JAXBElement<Boolean> createContextEnabled(Boolean value) {
        return new JAXBElement<Boolean>(_ContextEnabled_QNAME, Boolean.class, Context.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "id", scope = Context.class)
    public JAXBElement<Integer> createContextId(Integer value) {
        return new JAXBElement<Integer>(_ContextId_QNAME, Integer.class, Context.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Database }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "writeDatabase", scope = Context.class)
    public JAXBElement<Database> createContextWriteDatabase(Database value) {
        return new JAXBElement<Database>(_ContextWriteDatabase_QNAME, Database.class, Context.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "filestoreId", scope = Context.class)
    public JAXBElement<Integer> createContextFilestoreId(Integer value) {
        return new JAXBElement<Integer>(_ContextFilestoreId_QNAME, Integer.class, Context.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "name", scope = Context.class)
    public JAXBElement<String> createContextName(String value) {
        return new JAXBElement<String>(_ContextName_QNAME, String.class, Context.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "usedQuota", scope = Context.class)
    public JAXBElement<Long> createContextUsedQuota(Long value) {
        return new JAXBElement<Long>(_ContextUsedQuota_QNAME, Long.class, Context.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "maxQuota", scope = Context.class)
    public JAXBElement<Long> createContextMaxQuota(Long value) {
        return new JAXBElement<Long>(_ContextMaxQuota_QNAME, Long.class, Context.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Database }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "readDatabase", scope = Context.class)
    public JAXBElement<Database> createContextReadDatabase(Database value) {
        return new JAXBElement<Database>(_ContextReadDatabase_QNAME, Database.class, Context.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "filestore_name", scope = Context.class)
    public JAXBElement<String> createContextFilestoreName(String value) {
        return new JAXBElement<String>(_ContextFilestoreName_QNAME, String.class, Context.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "average_size", scope = Context.class)
    public JAXBElement<Long> createContextAverageSize(Long value) {
        return new JAXBElement<Long>(_ContextAverageSize_QNAME, Long.class, Context.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SOAPStringMapMap }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "userAttributes", scope = Context.class)
    public JAXBElement<SOAPStringMapMap> createContextUserAttributes(SOAPStringMapMap value) {
        return new JAXBElement<SOAPStringMapMap>(_ContextUserAttributes_QNAME, SOAPStringMapMap.class, Context.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "value", scope = Entry.class)
    public JAXBElement<String> createEntryValue(String value) {
        return new JAXBElement<String>(_EntryValue_QNAME, String.class, Entry.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "key", scope = Entry.class)
    public JAXBElement<String> createEntryKey(String value) {
        return new JAXBElement<String>(_EntryKey_QNAME, String.class, Entry.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "telephone_callback", scope = User.class)
    public JAXBElement<String> createUserTelephoneCallback(String value) {
        return new JAXBElement<String>(_UserTelephoneCallback_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "nickname", scope = User.class)
    public JAXBElement<String> createUserNickname(String value) {
        return new JAXBElement<String>(_UserNickname_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "middle_name", scope = User.class)
    public JAXBElement<String> createUserMiddleName(String value) {
        return new JAXBElement<String>(_UserMiddleName_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "postal_code_business", scope = User.class)
    public JAXBElement<String> createUserPostalCodeBusiness(String value) {
        return new JAXBElement<String>(_UserPostalCodeBusiness_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "mail_folder_spam_name", scope = User.class)
    public JAXBElement<String> createUserMailFolderSpamName(String value) {
        return new JAXBElement<String>(_UserMailFolderSpamName_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "display_name", scope = User.class)
    public JAXBElement<String> createUserDisplayName(String value) {
        return new JAXBElement<String>(_UserDisplayName_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "spouse_name", scope = User.class)
    public JAXBElement<String> createUserSpouseName(String value) {
        return new JAXBElement<String>(_UserSpouseName_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "language", scope = User.class)
    public JAXBElement<String> createUserLanguage(String value) {
        return new JAXBElement<String>(_UserLanguage_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "uploadFileSizeLimit", scope = User.class)
    public JAXBElement<Integer> createUserUploadFileSizeLimit(Integer value) {
        return new JAXBElement<Integer>(_UserUploadFileSizeLimit_QNAME, Integer.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "profession", scope = User.class)
    public JAXBElement<String> createUserProfession(String value) {
        return new JAXBElement<String>(_UserProfession_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "number_of_employee", scope = User.class)
    public JAXBElement<String> createUserNumberOfEmployee(String value) {
        return new JAXBElement<String>(_UserNumberOfEmployee_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "imapServer", scope = User.class)
    public JAXBElement<String> createUserImapServer(String value) {
        return new JAXBElement<String>(_UserImapServer_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "state_home", scope = User.class)
    public JAXBElement<String> createUserStateHome(String value) {
        return new JAXBElement<String>(_UserStateHome_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "info", scope = User.class)
    public JAXBElement<String> createUserInfo(String value) {
        return new JAXBElement<String>(_UserInfo_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "sur_name", scope = User.class)
    public JAXBElement<String> createUserSurName(String value) {
        return new JAXBElement<String>(_UserSurName_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "uploadFileSizeLimitPerFile", scope = User.class)
    public JAXBElement<Integer> createUserUploadFileSizeLimitPerFile(Integer value) {
        return new JAXBElement<Integer>(_UserUploadFileSizeLimitPerFile_QNAME, Integer.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "telephone_isdn", scope = User.class)
    public JAXBElement<String> createUserTelephoneIsdn(String value) {
        return new JAXBElement<String>(_UserTelephoneIsdn_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "postal_code_other", scope = User.class)
    public JAXBElement<String> createUserPostalCodeOther(String value) {
        return new JAXBElement<String>(_UserPostalCodeOther_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "smtpServer", scope = User.class)
    public JAXBElement<String> createUserSmtpServer(String value) {
        return new JAXBElement<String>(_UserSmtpServer_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "fax_home", scope = User.class)
    public JAXBElement<String> createUserFaxHome(String value) {
        return new JAXBElement<String>(_UserFaxHome_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "marital_status", scope = User.class)
    public JAXBElement<String> createUserMaritalStatus(String value) {
        return new JAXBElement<String>(_UserMaritalStatus_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "cellular_telephone1", scope = User.class)
    public JAXBElement<String> createUserCellularTelephone1(String value) {
        return new JAXBElement<String>(_UserCellularTelephone1_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "cellular_telephone2", scope = User.class)
    public JAXBElement<String> createUserCellularTelephone2(String value) {
        return new JAXBElement<String>(_UserCellularTelephone2_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "mailenabled", scope = User.class)
    public JAXBElement<Boolean> createUserMailenabled(Boolean value) {
        return new JAXBElement<Boolean>(_UserMailenabled_QNAME, Boolean.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "mail_folder_drafts_name", scope = User.class)
    public JAXBElement<String> createUserMailFolderDraftsName(String value) {
        return new JAXBElement<String>(_UserMailFolderDraftsName_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "branches", scope = User.class)
    public JAXBElement<String> createUserBranches(String value) {
        return new JAXBElement<String>(_UserBranches_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "telephone_home1", scope = User.class)
    public JAXBElement<String> createUserTelephoneHome1(String value) {
        return new JAXBElement<String>(_UserTelephoneHome1_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "number_of_children", scope = User.class)
    public JAXBElement<String> createUserNumberOfChildren(String value) {
        return new JAXBElement<String>(_UserNumberOfChildren_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "userfield16", scope = User.class)
    public JAXBElement<String> createUserUserfield16(String value) {
        return new JAXBElement<String>(_UserUserfield16_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "userfield17", scope = User.class)
    public JAXBElement<String> createUserUserfield17(String value) {
        return new JAXBElement<String>(_UserUserfield17_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "userfield18", scope = User.class)
    public JAXBElement<String> createUserUserfield18(String value) {
        return new JAXBElement<String>(_UserUserfield18_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "mail_folder_confirmed_spam_name", scope = User.class)
    public JAXBElement<String> createUserMailFolderConfirmedSpamName(String value) {
        return new JAXBElement<String>(_UserMailFolderConfirmedSpamName_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "userfield19", scope = User.class)
    public JAXBElement<String> createUserUserfield19(String value) {
        return new JAXBElement<String>(_UserUserfield19_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "userfield12", scope = User.class)
    public JAXBElement<String> createUserUserfield12(String value) {
        return new JAXBElement<String>(_UserUserfield12_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "userfield13", scope = User.class)
    public JAXBElement<String> createUserUserfield13(String value) {
        return new JAXBElement<String>(_UserUserfield13_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "userfield14", scope = User.class)
    public JAXBElement<String> createUserUserfield14(String value) {
        return new JAXBElement<String>(_UserUserfield14_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "userfield15", scope = User.class)
    public JAXBElement<String> createUserUserfield15(String value) {
        return new JAXBElement<String>(_UserUserfield15_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "telephone_home2", scope = User.class)
    public JAXBElement<String> createUserTelephoneHome2(String value) {
        return new JAXBElement<String>(_UserTelephoneHome2_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "userfield11", scope = User.class)
    public JAXBElement<String> createUserUserfield11(String value) {
        return new JAXBElement<String>(_UserUserfield11_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "country_business", scope = User.class)
    public JAXBElement<String> createUserCountryBusiness(String value) {
        return new JAXBElement<String>(_UserCountryBusiness_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "password_expired", scope = User.class)
    public JAXBElement<Boolean> createUserPasswordExpired(Boolean value) {
        return new JAXBElement<Boolean>(_UserPasswordExpired_QNAME, Boolean.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "userfield10", scope = User.class)
    public JAXBElement<String> createUserUserfield10(String value) {
        return new JAXBElement<String>(_UserUserfield10_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "country_other", scope = User.class)
    public JAXBElement<String> createUserCountryOther(String value) {
        return new JAXBElement<String>(_UserCountryOther_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "telephone_ip", scope = User.class)
    public JAXBElement<String> createUserTelephoneIp(String value) {
        return new JAXBElement<String>(_UserTelephoneIp_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "telephone_other", scope = User.class)
    public JAXBElement<String> createUserTelephoneOther(String value) {
        return new JAXBElement<String>(_UserTelephoneOther_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "manager_name", scope = User.class)
    public JAXBElement<String> createUserManagerName(String value) {
        return new JAXBElement<String>(_UserManagerName_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "defaultSenderAddress", scope = User.class)
    public JAXBElement<String> createUserDefaultSenderAddress(String value) {
        return new JAXBElement<String>(_UserDefaultSenderAddress_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "telephone_ttytdd", scope = User.class)
    public JAXBElement<String> createUserTelephoneTtytdd(String value) {
        return new JAXBElement<String>(_UserTelephoneTtytdd_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "position", scope = User.class)
    public JAXBElement<String> createUserPosition(String value) {
        return new JAXBElement<String>(_UserPosition_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "sales_volume", scope = User.class)
    public JAXBElement<String> createUserSalesVolume(String value) {
        return new JAXBElement<String>(_UserSalesVolume_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "userfield09", scope = User.class)
    public JAXBElement<String> createUserUserfield09(String value) {
        return new JAXBElement<String>(_UserUserfield09_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "userfield07", scope = User.class)
    public JAXBElement<String> createUserUserfield07(String value) {
        return new JAXBElement<String>(_UserUserfield07_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "userfield08", scope = User.class)
    public JAXBElement<String> createUserUserfield08(String value) {
        return new JAXBElement<String>(_UserUserfield08_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "userfield05", scope = User.class)
    public JAXBElement<String> createUserUserfield05(String value) {
        return new JAXBElement<String>(_UserUserfield05_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "userfield06", scope = User.class)
    public JAXBElement<String> createUserUserfield06(String value) {
        return new JAXBElement<String>(_UserUserfield06_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "userfield03", scope = User.class)
    public JAXBElement<String> createUserUserfield03(String value) {
        return new JAXBElement<String>(_UserUserfield03_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "userfield04", scope = User.class)
    public JAXBElement<String> createUserUserfield04(String value) {
        return new JAXBElement<String>(_UserUserfield04_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "userfield01", scope = User.class)
    public JAXBElement<String> createUserUserfield01(String value) {
        return new JAXBElement<String>(_UserUserfield01_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "passwordMech", scope = User.class)
    public JAXBElement<String> createUserPasswordMech(String value) {
        return new JAXBElement<String>(_UserPasswordMech_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "userfield02", scope = User.class)
    public JAXBElement<String> createUserUserfield02(String value) {
        return new JAXBElement<String>(_UserUserfield02_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "title", scope = User.class)
    public JAXBElement<String> createUserTitle(String value) {
        return new JAXBElement<String>(_UserTitle_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "mail_folder_sent_name", scope = User.class)
    public JAXBElement<String> createUserMailFolderSentName(String value) {
        return new JAXBElement<String>(_UserMailFolderSentName_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "tax_id", scope = User.class)
    public JAXBElement<String> createUserTaxId(String value) {
        return new JAXBElement<String>(_UserTaxId_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SOAPStringMapMap }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "userAttributes", scope = User.class)
    public JAXBElement<SOAPStringMapMap> createUserUserAttributes(SOAPStringMapMap value) {
        return new JAXBElement<SOAPStringMapMap>(_ContextUserAttributes_QNAME, SOAPStringMapMap.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "note", scope = User.class)
    public JAXBElement<String> createUserNote(String value) {
        return new JAXBElement<String>(_UserNote_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "city_other", scope = User.class)
    public JAXBElement<String> createUserCityOther(String value) {
        return new JAXBElement<String>(_UserCityOther_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "employeeType", scope = User.class)
    public JAXBElement<String> createUserEmployeeType(String value) {
        return new JAXBElement<String>(_UserEmployeeType_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "url", scope = User.class)
    public JAXBElement<String> createUserUrl(String value) {
        return new JAXBElement<String>(_UserUrl_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "primaryEmail", scope = User.class)
    public JAXBElement<String> createUserPrimaryEmail(String value) {
        return new JAXBElement<String>(_UserPrimaryEmail_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "assistant_name", scope = User.class)
    public JAXBElement<String> createUserAssistantName(String value) {
        return new JAXBElement<String>(_UserAssistantName_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "city_business", scope = User.class)
    public JAXBElement<String> createUserCityBusiness(String value) {
        return new JAXBElement<String>(_UserCityBusiness_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "company", scope = User.class)
    public JAXBElement<String> createUserCompany(String value) {
        return new JAXBElement<String>(_UserCompany_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "smtpSchema", scope = User.class)
    public JAXBElement<String> createUserSmtpSchema(String value) {
        return new JAXBElement<String>(_UserSmtpSchema_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "telephone_car", scope = User.class)
    public JAXBElement<String> createUserTelephoneCar(String value) {
        return new JAXBElement<String>(_UserTelephoneCar_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "telephone_assistant", scope = User.class)
    public JAXBElement<String> createUserTelephoneAssistant(String value) {
        return new JAXBElement<String>(_UserTelephoneAssistant_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "password", scope = User.class)
    public JAXBElement<String> createUserPassword(String value) {
        return new JAXBElement<String>(_UserPassword_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SOAPStringMap }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "guiPreferencesForSoap", scope = User.class)
    public JAXBElement<SOAPStringMap> createUserGuiPreferencesForSoap(SOAPStringMap value) {
        return new JAXBElement<SOAPStringMap>(_UserGuiPreferencesForSoap_QNAME, SOAPStringMap.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "city_home", scope = User.class)
    public JAXBElement<String> createUserCityHome(String value) {
        return new JAXBElement<String>(_UserCityHome_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "state_other", scope = User.class)
    public JAXBElement<String> createUserStateOther(String value) {
        return new JAXBElement<String>(_UserStateOther_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "timezone", scope = User.class)
    public JAXBElement<String> createUserTimezone(String value) {
        return new JAXBElement<String>(_UserTimezone_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "mail_folder_confirmed_ham_name", scope = User.class)
    public JAXBElement<String> createUserMailFolderConfirmedHamName(String value) {
        return new JAXBElement<String>(_UserMailFolderConfirmedHamName_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "street_home", scope = User.class)
    public JAXBElement<String> createUserStreetHome(String value) {
        return new JAXBElement<String>(_UserStreetHome_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "smtpServerString", scope = User.class)
    public JAXBElement<String> createUserSmtpServerString(String value) {
        return new JAXBElement<String>(_UserSmtpServerString_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "telephone_radio", scope = User.class)
    public JAXBElement<String> createUserTelephoneRadio(String value) {
        return new JAXBElement<String>(_UserTelephoneRadio_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "country_home", scope = User.class)
    public JAXBElement<String> createUserCountryHome(String value) {
        return new JAXBElement<String>(_UserCountryHome_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "mail_folder_trash_name", scope = User.class)
    public JAXBElement<String> createUserMailFolderTrashName(String value) {
        return new JAXBElement<String>(_UserMailFolderTrashName_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "userfield20", scope = User.class)
    public JAXBElement<String> createUserUserfield20(String value) {
        return new JAXBElement<String>(_UserUserfield20_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "telephone_business2", scope = User.class)
    public JAXBElement<String> createUserTelephoneBusiness2(String value) {
        return new JAXBElement<String>(_UserTelephoneBusiness2_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "room_number", scope = User.class)
    public JAXBElement<String> createUserRoomNumber(String value) {
        return new JAXBElement<String>(_UserRoomNumber_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "telephone_business1", scope = User.class)
    public JAXBElement<String> createUserTelephoneBusiness1(String value) {
        return new JAXBElement<String>(_UserTelephoneBusiness1_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "anniversary", scope = User.class)
    public JAXBElement<XMLGregorianCalendar> createUserAnniversary(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_UserAnniversary_QNAME, XMLGregorianCalendar.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "email1", scope = User.class)
    public JAXBElement<String> createUserEmail1(String value) {
        return new JAXBElement<String>(_UserEmail1_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "suffix", scope = User.class)
    public JAXBElement<String> createUserSuffix(String value) {
        return new JAXBElement<String>(_UserSuffix_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "street_other", scope = User.class)
    public JAXBElement<String> createUserStreetOther(String value) {
        return new JAXBElement<String>(_UserStreetOther_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "email3", scope = User.class)
    public JAXBElement<String> createUserEmail3(String value) {
        return new JAXBElement<String>(_UserEmail3_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "email2", scope = User.class)
    public JAXBElement<String> createUserEmail2(String value) {
        return new JAXBElement<String>(_UserEmail2_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "imapLogin", scope = User.class)
    public JAXBElement<String> createUserImapLogin(String value) {
        return new JAXBElement<String>(_UserImapLogin_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "fax_business", scope = User.class)
    public JAXBElement<String> createUserFaxBusiness(String value) {
        return new JAXBElement<String>(_UserFaxBusiness_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Group }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "default_group", scope = User.class)
    public JAXBElement<Group> createUserDefaultGroup(Group value) {
        return new JAXBElement<Group>(_UserDefaultGroup_QNAME, Group.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "postal_code_home", scope = User.class)
    public JAXBElement<String> createUserPostalCodeHome(String value) {
        return new JAXBElement<String>(_UserPostalCodeHome_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "street_business", scope = User.class)
    public JAXBElement<String> createUserStreetBusiness(String value) {
        return new JAXBElement<String>(_UserStreetBusiness_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "imapSchema", scope = User.class)
    public JAXBElement<String> createUserImapSchema(String value) {
        return new JAXBElement<String>(_UserImapSchema_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "state_business", scope = User.class)
    public JAXBElement<String> createUserStateBusiness(String value) {
        return new JAXBElement<String>(_UserStateBusiness_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "telephone_primary", scope = User.class)
    public JAXBElement<String> createUserTelephonePrimary(String value) {
        return new JAXBElement<String>(_UserTelephonePrimary_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "categories", scope = User.class)
    public JAXBElement<String> createUserCategories(String value) {
        return new JAXBElement<String>(_UserCategories_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "commercial_register", scope = User.class)
    public JAXBElement<String> createUserCommercialRegister(String value) {
        return new JAXBElement<String>(_UserCommercialRegister_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "instant_messenger1", scope = User.class)
    public JAXBElement<String> createUserInstantMessenger1(String value) {
        return new JAXBElement<String>(_UserInstantMessenger1_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "birthday", scope = User.class)
    public JAXBElement<XMLGregorianCalendar> createUserBirthday(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_UserBirthday_QNAME, XMLGregorianCalendar.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "telephone_pager", scope = User.class)
    public JAXBElement<String> createUserTelephonePager(String value) {
        return new JAXBElement<String>(_UserTelephonePager_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "instant_messenger2", scope = User.class)
    public JAXBElement<String> createUserInstantMessenger2(String value) {
        return new JAXBElement<String>(_UserInstantMessenger2_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "business_category", scope = User.class)
    public JAXBElement<String> createUserBusinessCategory(String value) {
        return new JAXBElement<String>(_UserBusinessCategory_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "department", scope = User.class)
    public JAXBElement<String> createUserDepartment(String value) {
        return new JAXBElement<String>(_UserDepartment_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "telephone_telex", scope = User.class)
    public JAXBElement<String> createUserTelephoneTelex(String value) {
        return new JAXBElement<String>(_UserTelephoneTelex_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "imapServerString", scope = User.class)
    public JAXBElement<String> createUserImapServerString(String value) {
        return new JAXBElement<String>(_UserImapServerString_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "given_name", scope = User.class)
    public JAXBElement<String> createUserGivenName(String value) {
        return new JAXBElement<String>(_UserGivenName_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "gui_spam_filter_enabled", scope = User.class)
    public JAXBElement<Boolean> createUserGuiSpamFilterEnabled(Boolean value) {
        return new JAXBElement<Boolean>(_UserGuiSpamFilterEnabled_QNAME, Boolean.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "id", scope = User.class)
    public JAXBElement<Integer> createUserId(Integer value) {
        return new JAXBElement<Integer>(_ContextId_QNAME, Integer.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "name", scope = User.class)
    public JAXBElement<String> createUserName(String value) {
        return new JAXBElement<String>(_ContextName_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "folderTree", scope = User.class)
    public JAXBElement<Integer> createUserFolderTree(Integer value) {
        return new JAXBElement<Integer>(_UserFolderTree_QNAME, Integer.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "telephone_company", scope = User.class)
    public JAXBElement<String> createUserTelephoneCompany(String value) {
        return new JAXBElement<String>(_UserTelephoneCompany_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "fax_other", scope = User.class)
    public JAXBElement<String> createUserFaxOther(String value) {
        return new JAXBElement<String>(_UserFaxOther_QNAME, String.class, User.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "id", scope = Group.class)
    public JAXBElement<Integer> createGroupId(Integer value) {
        return new JAXBElement<Integer>(_ContextId_QNAME, Integer.class, Group.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "name", scope = Group.class)
    public JAXBElement<String> createGroupName(String value) {
        return new JAXBElement<String>(_ContextName_QNAME, String.class, Group.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "displayname", scope = Group.class)
    public JAXBElement<String> createGroupDisplayname(String value) {
        return new JAXBElement<String>(_GroupDisplayname_QNAME, String.class, Group.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SOAPStringMap }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "value", scope = SOAPMapEntry.class)
    public JAXBElement<SOAPStringMap> createSOAPMapEntryValue(SOAPStringMap value) {
        return new JAXBElement<SOAPStringMap>(_EntryValue_QNAME, SOAPStringMap.class, SOAPMapEntry.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "key", scope = SOAPMapEntry.class)
    public JAXBElement<String> createSOAPMapEntryKey(String value) {
        return new JAXBElement<String>(_EntryKey_QNAME, String.class, SOAPMapEntry.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "rssPortal", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessRssPortal(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessRssPortal_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "webmail", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessWebmail(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessWebmail_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "editGroup", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessEditGroup(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessEditGroup_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "tasks", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessTasks(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessTasks_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "publication", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessPublication(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessPublication_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "vcard", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessVcard(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessVcard_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "projects", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessProjects(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessProjects_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "syncml", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessSyncml(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessSyncml_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "publicFolderEditable", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessPublicFolderEditable(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessPublicFolderEditable_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "collectEmailAddresses", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessCollectEmailAddresses(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessCollectEmailAddresses_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "ical", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessIcal(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessIcal_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "editPublicFolders", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessEditPublicFolders(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessEditPublicFolders_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "delegateTask", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessDelegateTask(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessDelegateTask_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "USM", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessUSM(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessUSM_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "forum", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessForum(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessForum_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "globalAddressBookDisabled", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessGlobalAddressBookDisabled(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessGlobalAddressBookDisabled_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "multipleMailAccounts", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessMultipleMailAccounts(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessMultipleMailAccounts_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "rssBookmarks", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessRssBookmarks(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessRssBookmarks_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "webdav", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessWebdav(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessWebdav_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "editPassword", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessEditPassword(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessEditPassword_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "contacts", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessContacts(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessContacts_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "webdavXml", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessWebdavXml(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessWebdavXml_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "editResource", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessEditResource(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessEditResource_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "infostore", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessInfostore(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessInfostore_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "OLOX20", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessOLOX20(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessOLOX20_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "readCreateSharedFolders", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessReadCreateSharedFolders(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessReadCreateSharedFolders_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "subscription", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessSubscription(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessSubscription_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "pinboardWrite", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessPinboardWrite(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessPinboardWrite_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "activeSync", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessActiveSync(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessActiveSync_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "calendar", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessCalendar(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessCalendar_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "deniedPortal", scope = UserModuleAccess.class)
    public JAXBElement<Boolean> createUserModuleAccessDeniedPortal(Boolean value) {
        return new JAXBElement<Boolean>(_UserModuleAccessDeniedPortal_QNAME, Boolean.class, UserModuleAccess.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "masterId", scope = Database.class)
    public JAXBElement<Integer> createDatabaseMasterId(Integer value) {
        return new JAXBElement<Integer>(_DatabaseMasterId_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "maxUnits", scope = Database.class)
    public JAXBElement<Integer> createDatabaseMaxUnits(Integer value) {
        return new JAXBElement<Integer>(_DatabaseMaxUnits_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "poolMax", scope = Database.class)
    public JAXBElement<Integer> createDatabasePoolMax(Integer value) {
        return new JAXBElement<Integer>(_DatabasePoolMax_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "currentUnits", scope = Database.class)
    public JAXBElement<Integer> createDatabaseCurrentUnits(Integer value) {
        return new JAXBElement<Integer>(_DatabaseCurrentUnits_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "password", scope = Database.class)
    public JAXBElement<String> createDatabasePassword(String value) {
        return new JAXBElement<String>(_UserPassword_QNAME, String.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "read_id", scope = Database.class)
    public JAXBElement<Integer> createDatabaseReadId(Integer value) {
        return new JAXBElement<Integer>(_DatabaseReadId_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "url", scope = Database.class)
    public JAXBElement<String> createDatabaseUrl(String value) {
        return new JAXBElement<String>(_UserUrl_QNAME, String.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "master", scope = Database.class)
    public JAXBElement<Boolean> createDatabaseMaster(Boolean value) {
        return new JAXBElement<Boolean>(_DatabaseMaster_QNAME, Boolean.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "id", scope = Database.class)
    public JAXBElement<Integer> createDatabaseId(Integer value) {
        return new JAXBElement<Integer>(_ContextId_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "scheme", scope = Database.class)
    public JAXBElement<String> createDatabaseScheme(String value) {
        return new JAXBElement<String>(_DatabaseScheme_QNAME, String.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "name", scope = Database.class)
    public JAXBElement<String> createDatabaseName(String value) {
        return new JAXBElement<String>(_ContextName_QNAME, String.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "poolInitial", scope = Database.class)
    public JAXBElement<Integer> createDatabasePoolInitial(Integer value) {
        return new JAXBElement<Integer>(_DatabasePoolInitial_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "driver", scope = Database.class)
    public JAXBElement<String> createDatabaseDriver(String value) {
        return new JAXBElement<String>(_DatabaseDriver_QNAME, String.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "login", scope = Database.class)
    public JAXBElement<String> createDatabaseLogin(String value) {
        return new JAXBElement<String>(_DatabaseLogin_QNAME, String.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "poolHardLimit", scope = Database.class)
    public JAXBElement<Integer> createDatabasePoolHardLimit(Integer value) {
        return new JAXBElement<Integer>(_DatabasePoolHardLimit_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "clusterWeight", scope = Database.class)
    public JAXBElement<Integer> createDatabaseClusterWeight(Integer value) {
        return new JAXBElement<Integer>(_DatabaseClusterWeight_QNAME, Integer.class, Database.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "id", scope = Filestore.class)
    public JAXBElement<Integer> createFilestoreId(Integer value) {
        return new JAXBElement<Integer>(_ContextId_QNAME, Integer.class, Filestore.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "maxContexts", scope = Filestore.class)
    public JAXBElement<Integer> createFilestoreMaxContexts(Integer value) {
        return new JAXBElement<Integer>(_FilestoreMaxContexts_QNAME, Integer.class, Filestore.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "reserved", scope = Filestore.class)
    public JAXBElement<Long> createFilestoreReserved(Long value) {
        return new JAXBElement<Long>(_FilestoreReserved_QNAME, Long.class, Filestore.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "used", scope = Filestore.class)
    public JAXBElement<Long> createFilestoreUsed(Long value) {
        return new JAXBElement<Long>(_FilestoreUsed_QNAME, Long.class, Filestore.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "currentContexts", scope = Filestore.class)
    public JAXBElement<Integer> createFilestoreCurrentContexts(Integer value) {
        return new JAXBElement<Integer>(_FilestoreCurrentContexts_QNAME, Integer.class, Filestore.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "size", scope = Filestore.class)
    public JAXBElement<Long> createFilestoreSize(Long value) {
        return new JAXBElement<Long>(_FilestoreSize_QNAME, Long.class, Filestore.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://dataobjects.soap.admin.openexchange.com/xsd", name = "url", scope = Filestore.class)
    public JAXBElement<String> createFilestoreUrl(String value) {
        return new JAXBElement<String>(_UserUrl_QNAME, String.class, Filestore.class, value);
    }

}
