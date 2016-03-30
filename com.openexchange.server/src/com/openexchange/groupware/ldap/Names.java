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



package com.openexchange.groupware.ldap;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein </a>
 */
public final class Names {

    /**
     * Private Constructor prevents instantiation of this class.
     */
    private Names() {
        super();
    }

    /*
     * Implementation class names
     */

    /**
     * Implementation for the user storage.
     */
    public static final String CONTACTSTORAGE_IMPL = "ContactStorageImpl";

    /**
     * Implementation for the mail configuration storage.
     */
    public static final String MAILCONFIGURATION_IMPL = "MailConfigurationImpl";

    /**
     * Implementation for the resource storage.
     */
    public static final String RESOURCESTORAGE_IMPL = "ResourceStorageImpl";

    /**
     * Implementation for the user storage.
     */
    public static final String USERSTORAGE_IMPL = "UserStorageImpl";

   /*
    * Object DNs
    */

   public static final String ADDRESSADMINS_DN = "AddressAdminsDN";

   public static final String DN_FOR_DEFAULT_MAIL = "DNForDefaultMail";

   public static final String GLOBALADDRESSBOOKENTRY_DN =
      "GlobalAddressBookEntryDN";

   public static final String GROUP_DN = "GroupDN";

   public static final String RESOURCEGROUP_DN = "ResourceGroupDN";

   public static final String RESOURCE_DN = "ResourceDN";

   public static final String USER_DN = "UserDN";

   public static final String USERADDRESSBOOKENTRY_DN =
      "UserAddressBookEntryDN";

   /*
    * Search Base DNs
    */

   public static final String GLOBALADDRESSBOOK_SEARCH_BASE_DN =
      "globalAddressBookBaseDN";

   public static final String GROUP_SEARCH_BASE_DN = "groupBaseDN";

   public static final String RESOURCEGROUP_SEARCH_BASE_DN =
      "resourceGroupBaseDN";

   public static final String RESOURCE_SEARCH_BASE_DN = "resourceBaseDN";

   public static final String USER_SEARCH_BASE_DN = "userBaseDN";

   /*
    * Search scopes.
    */

   public static final String GLOBALADDRESSBOOK_SEARCH_SCOPE =
      "GlobalAddressBookSearchScope";

   public static final String GROUP_SEARCH_SCOPE = "GroupSearchScope";

   public static final String RESOURCEGROUP_SEARCH_SCOPE =
      "ResourceGroupSearchScope";

   public static final String RESOURCE_SEARCH_SCOPE = "ResourceSearchScope";

   /**
    * The scope with that users will be searched in ldap.
    */
   public static final String USER_SEARCH_SCOPE = "UserSearchScope";

    /*
     * Search filter
     */

    /**
     * Filter for searching groups with a pattern.
     */
    public static final String GROUPS_PATTERN_SEARCH_FILTER =
        "GroupsPatternSearchFilter";

    /**
     * Filter for search users.
     */
    public static final String USERS_DISPLAYNAME_PATTERN_SEARCH_FILTER =
        "UsersDisplayNamePatternSearchFilter";

    /**
     * Filter for search users.
     */
    public static final String USERS_FORESURENAMEUID_PATTERN_SEARCH_FILTER =
        "UsersForeSureNameUIDPatternSearchFilter";

   public static final String GROUP_SEARCH_FILTER = "GroupSearchFilter";

   /**
    * This filter searches for groups that have a specific user as member.
    */
   public static final String GROUPS_WITH_MEMBER_SEARCH_FILTER =
      "GroupsWithMemberSearchFilter";

   public static final String INTERNALUSERS_FORESURENAMEUIDPATTERN_SEARCH_FILTER
      = "InternalUsersForeSureNameUIDPatternSearchFilter";

   public static final String INTERNALUSERS_FORDLIST_SEARCH_FILTER
      = "InternalUsersForDlistSearchFilter";

   public static final String INTERNALUSERS_STARTINGLETTER_SEARCH_FILTER =
      "InternalUsersStartingLetterSearchFilter";

   public static final String RESOURCEGROUP_SEARCH_FILTER =
      "ResourceGroupSearchFilter";

   public static final String RESOURCEGROUP_PATTERN_SEARCH_FILTER =
      "ResourceGroupPatternSearchFilter";

   public static final String RESOURCE_PATTERN_SEARCH_FILTER =
      "ResourcePatternSearchFilter";

   public static final String USER_SEARCH_FILTER = "UserSearchFilter";

   public static final String USERS_PATTERN_SEARCH_FILTER =
      "UsersPatternSearchFilter";

   /**
    * Property name that contains comma seperated objectClasses for a search for
    * users.
    */
   public static final String USERS_SEARCH_OBJECTCLASSES =
      "UsersPatternSearchObjectClasses";

   /*
    * Object attribute names.
    */

    /**
     * Name of the property containing the name of the attribute that contains
     * the aliases.
     */
    public static final String USER_ATTRIBUTE_ALIAS = "UserAttributeAlias";

    /**
     * Name of the property containing the name of the attribute that contains
     * the appointmentDays.
     */
    public static final String USER_ATTRIBUTE_APPOINTMENTDAYS =
        "UserAttributeAppointmentDays";

    /**
     * Name of the property containing the name of the attribute that contains
     * the country.
     */
    public static final String USER_ATTRIBUTE_COUNTRY =
        "UserAttributeCountry";

    /**
     * Name of the property containing the attribute name that contains the
     * second country of the user.
     */
    public static final String USER_ATTRIBUTE_COUNTRY2 =
        "UserAttributeCountry2";

    /**
     * Name of the property containing the name of the attribute that contains
     * the description.
     */
    public static final String USER_ATTRIBUTE_DESCRIPTION =
        "UserAttributeDescription";

    /**
     * Name of the property containing the name of the attribute that contains
     * the display name.
     */
    public static final String USER_ATTRIBUTE_DISPLAYNAME =
        "UserAttributeDisplayName";

    /**
     * Name of the property containing the name of the attribute that contains
     * the facsimile telephone number.
     */
    public static final String USER_ATTRIBUTE_FACSIMILE =
        "UserAttributeFacsimile";

    /**
     * Name of the property containing the attribute name that contains the
     * given name of the user.
     */
    public static final String USER_ATTRIBUTE_GIVENNAME =
        "UserAttributeGivenName";

    /**
     * Name of the property containing the name of the attribute that contains
     * the unique database identifier.
     */
    public static final String USER_ATTRIBUTE_IDENTIFIER =
        "UserAttributeIdentifier";

    /**
     * Name of the property containing the attribute name that contains the imap
     * server host name.
     */
    public static final String USER_ATTRIBUTE_IMAPSERVER =
        "UserAttributeImapServer";

    /**
     * Name of the property that value names the attribute of the user that
     * contains the JDBC URL to a user specific database.
     */
    public static final String USER_ATTRIBUTE_JDBCDATABASEURL =
        "UserAttributeJDBCDatabaseURL";

    /**
     * Name of the property that value names the attribute of the user that
     * contains the driver class name for a user specific database.
     */
    public static final String USER_ATTRIBUTE_JDBCDRIVERCLASSNAME =
        "UserAttributeJDBCDriverClassName";

    /**
     * Name of the property that value names the attribute of the user that
     * contains the database login for a user specific database.
     */
    public static final String USER_ATTRIBUTE_JDBCLOGIN =
        "UserAttributeJDBCLogin";

    /**
     * Name of the property that value names the attribute of the user that
     * contains the password for a user specific database.
     */
    public static final String USER_ATTRIBUTE_JDBCPASSWORD =
        "UserAttributeJDBCPassword";

    /**
     * Name of the property containing the attribute name that contains the
     * labeled URI.
     */
    public static final String USER_ATTRIBUTE_LABELEDURI =
        "UserAttributeLabeledURI";

    /**
     * Name of the property containing the name of the attribute that contains
     * the locality.
     */
    public static final String USER_ATTRIBUTE_LOCALITY =
        "UserAttributeLocality";

    /**
     * Name of the property containing the name of the attribute that contains
     * the mail.
     */
    public static final String USER_ATTRIBUTE_MAIL = "UserAttributeMail";

    /**
     * Name of the property containing the name of the attribute that contains
     * the mail domain.
     */
    public static final String USER_ATTRIBUTE_MAILDOMAIN =
        "UserAttributeMailDomain";

    /**
     * Name of the property containing the name of the attribute that contains
     * if the user is enabled or disabled.
     */
    public static final String USER_ATTRIBUTE_MAILENABLED =
        "UserAttributeEnabled";

    /**
     * Name of the property containing the name of the attribute that contains
     * the mobile telephone number.
     */
    public static final String USER_ATTRIBUTE_MOBILE =
        "UserAttributeMobile";

    /**
     * Name of the property containing the name of the attribute that contains
     * the timestamp of the last modification.
     */
    public static final String USER_ATTRIBUTE_MODIFYTIMESTAMP =
       "UserAttributeModifyTimestamp";

    /**
     * Name of the property containing the attribute name that contains the
     * organization.
     */
    public static final String USER_ATTRIBUTE_ORGANIZATION =
        "UserAttributeOrganization";

    /**
     * Name of the property containing the attribute name that contains the
     * organizational unit.
     */
    public static final String USER_ATTRIBUTE_ORGANIZATIONALUNIT =
        "UserAttributeOrganizationalUnit";

    /**
     * Name of the property containing the attribute name that contains the
     * pager number.
     */
    public static final String USER_ATTRIBUTE_PAGER =
        "UserAttributePager";

    /**
     * Name of the property containing the attribute name that contains the
     * password of the user.
     */
    public static final String USER_ATTRIBUTE_PASSWORD =
        "UserAttributePassword";

    /**
     * Name of the property containing the attribute name that contains the
     * postal code.
     */
    public static final String USER_ATTRIBUTE_POSTALCODE =
        "UserAttributePostalCode";

    /**
     * Name of the property containing the name of the attribute that contains
     * the preferred language.
     */
    public static final String USER_ATTRIBUTE_PREFERREDLANGUAGE =
        "UserAttributePreferredLanguage";

    /**
     * Name of the property that value names the attribute of the user that
     * contains the value when the user changed his password last. The value of
     * the last change of the password is measured in days since 1970-01-01.
     */
    public static final String USER_ATTRIBUTE_SHADOWLASTCHANGE =
        "UserAttributeShadowLastChange";

    /**
     * Name of the property containing the attribute name that contains the smtp
     * server host name.
     */
    public static final String USER_ATTRIBUTE_SMTPSERVER =
        "UserAttributeSmtpServer";

    /**
     * Name of the property containing the attribute name that contains the
     * state.
     */
    public static final String USER_ATTRIBUTE_STATE =
        "UserAttributeState";

    /**
     * Name of the property containing the attribute name that contains the
     * street of the user.
     */
    public static final String USER_ATTRIBUTE_STREET =
        "UserAttributeStreet";

    /**
     * Name of the property containing the attribute name that contains the sure
     * name of the user.
     */
    public static final String USER_ATTRIBUTE_SURENAME =
        "UserAttributeSureName";

    /**
     * Name of the property containing the attribute name that contains the
     * task days of the user.
     */
    public static final String USER_ATTRIBUTE_TASKDAYS =
        "UserAttributeTaskDays";

    /**
     * Name of the property containing the attribute name that contains the
     * telephone number of the user.
     */
    public static final String USER_ATTRIBUTE_TELEPHONENUMBER =
        "UserAttributeTelephoneNumber";

    /**
     * Name of the property containing the attribute name that contains the
     * timezone of the user.
     */
    public static final String USER_ATTRIBUTE_TIMEZONE =
        "UserAttributeTimeZone";

    /**
     * Name of the property containing the attribute name that contains the
     * title of the user.
     */
    public static final String USER_ATTRIBUTE_TITLE =
        "UserAttributeTitle";

    /**
     * Name of the property containing the attribute name that contains the
     * identifier of the user.
     */
    public static final String USER_ATTRIBUTE_UID =
        "UserAttributeUid";

   public static final String INETORGPERSON_ATTRIBUTE_BUSINESSCATEGORY_NAME =
      "inetOrgPersonAttributebusinessCategoryName";

   public static final String INETORGPERSON_ATTRIBUTE_CN_NAME =
      "inetOrgPersonAttributecnName";

   public static final String INETORGPERSON_ATTRIBUTE_EMPLOYEENUMBER_NAME =
      "inetOrgPersonAttributeemployeeNumberName";

   public static final String INETORGPERSON_ATTRIBUTE_EMPLOYEETYPE_NAME =
      "inetOrgPersonAttributeemployeeTypeName";

   public static final String INETORGPERSON_ATTRIBUTE_HOMEPHONE_NAME =
     "inetOrgPersonAttributehomePhoneName";

   public static final String INETORGPERSON_ATTRIBUTE_HOMEPOSTALADDRESS_NAME =
      "inetOrgPersonAttributehomePostalAddressName";

   public static final String INETORGPERSON_ATTRIBUTE_JPEGPHOTO_NAME =
      "inetOrgPersonAttributejpegPhotoName";

   public static final String INETORGPERSON_ATTRIBUTE_INITIALS_NAME =
      "inetOrgPersonAttributeinitialsName";

   public static final String
      INETORGPERSON_ATTRIBUTE_INTERNATIONALISDNNUMBER_NAME =
         "inetOrgPersonAttributeinternationaliSDNNumberName";

   public static final String INETORGPERSON_ATTRIBUTE_ROOMNUMBER_NAME =
      "inetOrgPersonAttributeroomNumberName";

   public static final String INETORGPERSON_ATTRIBUTE_TELEXNUMBER_NAME =
      "inetOrgPersonAttributetelexNumberName";

   public static final String
      OXRESOURCEGROUP_ATTRIBUTE_RESOURCEGROUPAVAILABLE_NAME =
         "OXResourceGroupAttributeresourceGroupAvailableName";

   public static final String OXRESOURCEGROUP_ATTRIBUTE_RESOURCEGROUPNAME_NAME =
      "OXResourceGroupAttributeresourceGroupNameName";

   public static final String OXRESOURCEGROUP_ATTRIBUTE_RESOURCEGROUPMEMBER_NAME
      = "OXResourceGroupAttributeresourceGroupMemberName";

   public static final String OXRESOURCE_ATTRIBUTE_RESOURCENAME_NAME =
      "OXResourceAttributeresourceNameName";

   public static final String OXUSEROBJECT_ATTRIBUTE_ANNIVERSARY_NAME =
      "OXUserObjectAttributeAnniversaryName";

   public static final String OXUSEROBJECT_ATTRIBUTE_BIRTHDAY_NAME =
      "OXUserObjectAttributebirthDayName";

   public static final String OXUSEROBJECT_ATTRIBUTE_BRANCHES_NAME =
      "OXUserObjectAttributeBranchesName";

   public static final String OXUSEROBJECT_ATTRIBUTE_CATEGORIES_NAME =
      "OXUserObjectAttributeCategoriesName";

   public static final String OXUSEROBJECT_ATTRIBUTE_CHILDREN_NAME =
      "OXUserObjectAttributeChildrenName";

   public static final String OXUSEROBJECT_ATTRIBUTE_CITY_NAME =
      "OXUserObjectAttributeCityName";

   public static final String OXUSEROBJECT_ATTRIBUTE_CO_NAME =
      "OXUserObjectAttributecoName";

   public static final String OXUSEROBJECT_ATTRIBUTE_COMMENT_NAME =
      "OXUserObjectAttributeCommentName";

   public static final String OXUSEROBJECT_ATTRIBUTE_COMREG_NAME =
      "OXUserObjectAttributeComRegName";

   public static final String OXUSEROBJECT_ATTRIBUTE_DAYVIEWENDTIME_NAME =
      "OXUserObjectAttributeDayViewEndTimeName";

   public static final String OXUSEROBJECT_ATTRIBUTE_DAYVIEWINTERVAL_NAME =
      "OXUserObjectAttributeDayViewIntervalName";

   public static final String OXUSEROBJECT_ATTRIBUTE_DAYVIEWSTARTTIME_NAME =
      "OXUserObjectAttributeDayViewStartTimeName";

   public static final String OXUSEROBJECT_ATTRIBUTE_DISTRIBUTIONLIST_NAME =
      "OXUserObjectAttributeDistributionListName";

   public static final String OXUSEROBJECT_ATTRIBUTE_EMAIL2_NAME =
      "OXUserObjectAttributeEmail2Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_EMAIL3_NAME =
      "OXUserObjectAttributeEmail3Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_INFO_NAME =
      "OXUserObjectAttributeInfoName";

   public static final String OXUSEROBJECT_ATTRIBUTE_INSTANTMESSENGER_NAME =
      "OXUserObjectAttributeInstantMessengerName";

   public static final String OXUSEROBJECT_ATTRIBUTE_INSTANTMESSENGER2_NAME =
      "OXUserObjectAttributeInstantMessenger2Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_IPPHONE_NAME =
      "OXUserObjectAttributeIPPhoneName";

   public static final String OXUSEROBJECT_ATTRIBUTE_MARITALSTATUS_NAME =
      "OXUserObjectAttributeMaritalStatusName";

   public static final String OXUSEROBJECT_ATTRIBUTE_NICKNAME_NAME =
      "OXUserObjectAttributeNickNameName";

   public static final String OXUSEROBJECT_ATTRIBUTE_OTHERCITY_NAME =
      "OXUserObjectAttributeOtherCityName";

   public static final String OXUSEROBJECT_ATTRIBUTE_OTHERCOUNTRY_NAME =
      "OXUserObjectAttributeOtherCountryName";

   public static final String OXUSEROBJECT_ATTRIBUTE_OTHERFACSIMILE_NAME =
      "OXUserObjectAttributeotherfacsimiletelephonenumberName";

   public static final String OXUSEROBJECT_ATTRIBUTE_OTHERPOSTALCODE_NAME =
      "OXUserObjectAttributeOtherPostalCodeName";

   public static final String OXUSEROBJECT_ATTRIBUTE_OTHERSTATE_NAME =
      "OXUserObjectAttributeOtherStateName";

   public static final String OXUSEROBJECT_ATTRIBUTE_OTHERSTREET_NAME =
      "OXUserObjectAttributeOtherStreetName";

   public static final String OXUSEROBJECT_ATTRIBUTE_POSITION_NAME =
      "OXUserObjectAttributePositionName";

   public static final String OXUSEROBJECT_ATTRIBUTE_POSTALCODE_NAME =
      "OXUserObjectAttributePostalCodeName";

   public static final String OXUSEROBJECT_ATTRIBUTE_PROFESSION_NAME =
      "OXUserObjectAttributeProfessionName";

   public static final String OXUSEROBJECT_ATTRIBUTE_SALESVOLUME_NAME =
      "OXUserObjectAttributeSalesVolumeName";

   public static final String OXUSEROBJECT_ATTRIBUTE_SPOUSENAME_NAME =
      "OXUserObjectAttributeSpouseNameName";

   public static final String OXUSEROBJECT_ATTRIBUTE_SUFFIX_NAME =
      "OXUserObjectAttributesuffixName";

   public static final String OXUSEROBJECT_ATTRIBUTE_STATE_NAME =
      "OXUserObjectAttributeStateName";

   public static final String OXUSEROBJECT_ATTRIBUTE_TAXID_NAME =
     "OXUserObjectAttributeTaxIDName";

   public static final String OXUSEROBJECT_ATTRIBUTE_TELEASSISTANT_NAME =
      "OXUserObjectAttributeTeleAssistantName";

   public static final String OXUSEROBJECT_ATTRIBUTE_TELEBUSINESS2_NAME =
      "OXUserObjectAttributeTeleBusiness2Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_TELECALLBACK_NAME =
      "OXUserObjectAttributeTeleCallbackName";

   public static final String OXUSEROBJECT_ATTRIBUTE_TELECAR_NAME =
      "OXUserObjectAttributeTeleCarName";

   public static final String OXUSEROBJECT_ATTRIBUTE_TELECOMPANY_NAME =
      "OXUserObjectAttributeTeleCompanyName";

   public static final String OXUSEROBJECT_ATTRIBUTE_TELEFAX2_NAME =
      "OXUserObjectAttributeTeleFax2Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_TELEHOME2_NAME =
      "OXUserObjectAttributeTeleHome2Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_TELEMOBILE2_NAME =
      "OXUserObjectAttributeTeleMobile2Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_TELEOTHER_NAME =
      "OXUserObjectAttributeTeleOtherName";

   public static final String OXUSEROBJECT_ATTRIBUTE_TELEPRIMARY_NAME =
      "OXUserObjectAttributeTelePrimaryName";

   public static final String OXUSEROBJECT_ATTRIBUTE_TELERADIO_NAME =
      "OXUserObjectAttributeTeleRadioName";

   public static final String OXUSEROBJECT_ATTRIBUTE_TELETTY_NAME =
      "OXUserObjectAttributeTeleTTYName";

   public static final String OXUSEROBJECT_ATTRIBUTE_UNDEF01_NAME =
      "OXUserObjectAttributeUserUndef01Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_UNDEF02_NAME =
      "OXUserObjectAttributeUserUndef02Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_UNDEF03_NAME =
      "OXUserObjectAttributeUserUndef03Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_UNDEF04_NAME =
      "OXUserObjectAttributeUserUndef04Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_UNDEF05_NAME =
      "OXUserObjectAttributeUserUndef05Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_UNDEF06_NAME =
      "OXUserObjectAttributeUserUndef06Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_UNDEF07_NAME =
      "OXUserObjectAttributeUserUndef07Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_UNDEF08_NAME =
      "OXUserObjectAttributeUserUndef08Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_UNDEF09_NAME =
      "OXUserObjectAttributeUserUndef09Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_UNDEF10_NAME =
      "OXUserObjectAttributeUserUndef10Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_UNDEF11_NAME =
      "OXUserObjectAttributeUserUndef11Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_UNDEF12_NAME =
      "OXUserObjectAttributeUserUndef12Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_UNDEF13_NAME =
      "OXUserObjectAttributeUserUndef13Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_UNDEF14_NAME =
      "OXUserObjectAttributeUserUndef14Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_UNDEF15_NAME =
      "OXUserObjectAttributeUserUndef15Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_UNDEF16_NAME =
      "OXUserObjectAttributeUserUndef16Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_UNDEF17_NAME =
      "OXUserObjectAttributeUserUndef17Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_UNDEF18_NAME =
      "OXUserObjectAttributeUserUndef18Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_UNDEF19_NAME =
      "OXUserObjectAttributeUserUndef19Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_UNDEF20_NAME =
      "OXUserObjectAttributeUserUndef20Name";

   public static final String OXUSEROBJECT_ATTRIBUTE_URL_NAME =
      "OXUserObjectAttributeurlName";

   public static final String POSIXACCOUNT_ATTRIBUTE_CN_NAME =
      "posixAccountAttributecnName";

   /*
    * Object class names
    */

   public static final String INETORGPERSON_CLASS_NAME =
      "inetOrgPersonClassName";

   public static final String OXUSEROBJECT_CLASS_NAME =
      "OXUserObjectClassName";

   /*
    * Options
    */

   /**
    * The value of this property defines if contacts are written to the
    * directory service or not. Possible values are <code>true</code> and
    * <code>false</code>.
    */
   public static final String CONTACTS_DISABLED = "ContactsDisabled";

   /**
    * The value of this property defines if users are spread all over the
    * directory service and their DN must be searched for.
    */
   public static final String USER_FULL_DYNAMIC = "UserFullDynamic";

    /**
     * The value of this property defines if caching is used.
     */
    public static final String CACHING = "Caching";

    /*
     * Other constants
     */

    /**
     * The mailEnabled attribute contains this value if the user is enabled.
     */
    public static final String MAILENABLED_OK = "MailEnabledOK";
}
