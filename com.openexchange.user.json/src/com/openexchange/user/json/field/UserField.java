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

package com.openexchange.user.json.field;

import java.util.EnumSet;

/**
 * {@link UserField} - Enumeration for user fields.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum UserField {

    /**
     * The user identifier
     */
    ID(1, "id"),
    /**
     * The entity which created this user
     */
    CREATED_BY(2, "created_by"),
    /**
     * The entity which modified this user last time
     */
    MODIFIED_BY(3, "modified_by"),
    /**
     * The creation time stamp in requesting session's user time zone
     */
    CREATION_DATE(4, "creation_date"),
    /**
     * The last-modified time stamp in requesting session's user time zone
     */
    LAST_MODIFIED(5, "last_modified"),
    /**
     * The last-modified time stamp in UTC
     */
    LAST_MODIFIED_UTC(6, "last_modified_utc"),
    /**
     * The user's parent folder identifier
     */
    FOLDER_ID(20, "folder_id"),

    // ######################### COMMON ATTRIBUTES ###########################################

    /**
     * Categories
     */
    CATEGORIES(100, "categories"),
    /**
     * Private flag
     */
    PRIVATE_FLAG(101, "private_flag"),
    /**
     * Private flag
     */
    COLOR_LABEL(103, "color_label"),
    /**
     * Private flag
     */
    NUMBER_OF_ATTACHMENTS(104, "number_of_attachments"),

    // ######################### CONTACT ATTRIBUTES ###########################################

    /**
     * The display name
     */
    DISPLAY_NAME(500, "display_name"),
    /**
     * The first name
     */
    FIRST_NAME(501, "first_name"),
    /**
     * The last name
     */
    LAST_NAME(502, "last_name"),
    /**
     * The second name
     */
    SECOND_NAME(503, "second_name"),
    /**
     * The suffix
     */
    SUFFIX(504, "suffix"),
    /**
     * The title
     */
    TITLE(505, "title"),
    /**
     * Street home
     */
    STREET_HOME(506, "street_home"),
    /**
     * Postal code home
     */
    POSTAL_CODE_HOME(507, "postal_code_home"),
    /**
     * City home
     */
    CITY_HOME(508, "city_home"),
    /**
     * State home
     */
    STATE_HOME(509, "state_home"),
    /**
     * Country home
     */
    COUNTRY_HOME(510, "country_home"),
    /**
     * Birthday
     */
    BIRTHDAY(511, "birthday"),
    /**
     * Marital status
     */
    MARITAL_STATUS(512, "marital_status"),
    /**
     * Number of children
     */
    NUMBER_OF_CHILDREN(513, "number_of_children"),
    /**
     * Profession
     */
    PROFESSION(514, "profession"),
    /**
     * Nickname
     */
    NICKNAME(515, "nickname"),
    /**
     * Spouse name
     */
    SPOUSE_NAME(516, "spouse_name"),
    /**
     * Anniversary
     */
    ANNIVERSARY(517, "anniversary"),
    /**
     * Note
     */
    NOTE(518, "note"),
    /**
     * Default address
     */
    DEFAULT_ADDRESS(605, "default_address"),
    /**
     * Department
     */
    DEPARTMENT(519, "department"),
    /**
     * Position
     */
    POSITION(520, "position"),
    /**
     * Employee type
     */
    EMPLOYEE_TYPE(521, "employee_type"),
    /**
     * Room number
     */
    ROOM_NUMBER(522, "room_number"),
    /**
     * Street business
     */
    STREET_BUSINESS(523, "street_business"),
    /**
     * Internal user ID
     */
    INTERNAL_USERID(524, "user_id"),
    /**
     * Postal code business
     */
    POSTAL_CODE_BUSINESS(525, "postal_code_business"),
    /**
     * City business
     */
    CITY_BUSINESS(526, "city_business"),
    /**
     * State business
     */
    STATE_BUSINESS(527, "state_business"),
    /**
     * Country business
     */
    COUNTRY_BUSINESS(528, "country_business"),
    /**
     * Number of employees
     */
    NUMBER_OF_EMPLOYEE(529, "number_of_employees"),
    /**
     * Sales volume
     */
    SALES_VOLUME(530, "sales_volume"),
    /**
     * Tax ID
     */
    TAX_ID(531, "tax_id"),
    /**
     * Commercial register
     */
    COMMERCIAL_REGISTER(532, "commercial_register"),
    /**
     * Commercial register
     */
    BRANCHES(533, "branches"),
    /**
     * Business category TODO: Typo?!
     */
    BUSINESS_CATEGORY(534, "busines_categorie"),
    /**
     * Commercial register
     */
    INFO(535, "commercial_register"),
    /**
     * Manager name
     */
    MANAGER_NAME(536, "manager_name"),
    /**
     * Assistant name
     */
    ASSISTANT_NAME(537, "assistant_name"),
    /**
     * Street other
     */
    STREET_OTHER(538, "street_other"),
    /**
     * Street other
     */
    CITY_OTHER(539, "city_other"),
    /**
     * State other
     */
    STATE_OTHER(598, "state_other"),
    /**
     * Street other
     */
    POSTAL_CODE_OTHER(540, "postal_code_other"),
    /**
     * Country other
     */
    COUNTRY_OTHER(541, "country_other"),
    /**
     * Telephone business1
     */
    TELEPHONE_BUSINESS1(542, "telephone_business1"),
    /**
     * Telephone business2
     */
    TELEPHONE_BUSINESS2(543, "telephone_business2"),
    /**
     * FAX business
     */
    FAX_BUSINESS(544, "fax_business"),
    /**
     * Telephone callback
     */
    TELEPHONE_CALLBACK(545, "telephone_callback"),
    /**
     * Telephone car
     */
    TELEPHONE_CAR(546, "telephone_car"),
    /**
     * Telephone company
     */
    TELEPHONE_COMPANY(547, "telephone_company"),
    /**
     * Telephone home1
     */
    TELEPHONE_HOME1(548, "telephone_home1"),
    /**
     * Telephone home2
     */
    TELEPHONE_HOME2(549, "telephone_home2"),
    /**
     * FAX home
     */
    FAX_HOME(550, "fax_home"),
    /**
     * FAX home
     */
    CELLULAR_TELEPHONE1(551, "cellular_telephone1"),
    /**
     * FAX home
     */
    CELLULAR_TELEPHONE2(552, "cellular_telephone2"),
    /**
     * Telephone other
     */
    TELEPHONE_OTHER(553, "telephone_other"),
    /**
     * FAX other
     */
    FAX_OTHER(554, "fax_other"),
    /**
     * Business email address.
     */
    EMAIL1(555, "email1"),
    /**
     * Private email address.
     */
    EMAIL2(556, "email2"),
    /**
     * Email3
     */
    EMAIL3(557, "email3"),
    /**
     * URL
     */
    URL(558, "url"),
    /**
     * Telephone ISDN
     */
    TELEPHONE_ISDN(559, "telephone_isdn"),
    /**
     * Telephone pager
     */
    TELEPHONE_PAGER(560, "telephone_pager"),
    /**
     * Telephone primary
     */
    TELEPHONE_PRIMARY(561, "telephone_primary"),
    /**
     * Telephone radio
     */
    TELEPHONE_RADIO(562, "telephone_radio"),
    /**
     * Telephone telex
     */
    TELEPHONE_TELEX(563, "telephone_telex"),
    /**
     * Telephone TTYTDD
     */
    TELEPHONE_TTYTDD(564, "telephone_ttytdd"),
    /**
     * Instant messenger2
     */
    INSTANT_MESSENGER1(565, "instant_messenger1"),
    /**
     * Instant messenger1
     */
    INSTANT_MESSENGER2(566, "instant_messenger2"),
    /**
     * Telephone IP
     */
    TELEPHONE_IP(567, "telephone_ip"),
    /**
     * Telephone Assistant
     */
    TELEPHONE_ASSISTANT(568, "telephone_assistant"),
    /**
     * Company
     */
    COMPANY(569, "company"),
    /**
     * Image1
     */
    IMAGE1(570, "image1"),
    /**
     * User field01
     */
    USERFIELD01(571, "userfield01"),
    /**
     * User field02
     */
    USERFIELD02(572, "userfield02"),
    /**
     * User field03
     */
    USERFIELD03(573, "userfield03"),
    /**
     * User field04
     */
    USERFIELD04(574, "userfield04"),
    /**
     * User field05
     */
    USERFIELD05(575, "userfield05"),
    /**
     * User field06
     */
    USERFIELD06(576, "userfield06"),
    /**
     * User field07
     */
    USERFIELD07(577, "userfield07"),
    /**
     * User field08
     */
    USERFIELD08(578, "userfield08"),
    /**
     * User field09
     */
    USERFIELD09(579, "userfield09"),
    /**
     * User field10
     */
    USERFIELD10(580, "userfield10"),
    /**
     * User field11
     */
    USERFIELD11(581, "userfield11"),
    /**
     * User field12
     */
    USERFIELD12(582, "userfield12"),
    /**
     * User field13
     */
    USERFIELD13(583, "userfield13"),
    /**
     * User field14
     */
    USERFIELD14(584, "userfield14"),
    /**
     * User field15
     */
    USERFIELD15(585, "userfield15"),
    /**
     * User field16
     */
    USERFIELD16(586, "userfield16"),
    /**
     * User field17
     */
    USERFIELD17(587, "userfield17"),
    /**
     * User field18
     */
    USERFIELD18(588, "userfield18"),
    /**
     * User field19
     */
    USERFIELD19(589, "userfield19"),
    /**
     * User field20
     */
    USERFIELD20(590, "userfield20"),
    /**
     * User field20
     */
    LINKS(591, "links"),
    /**
     * Distribution list
     */
    DISTRIBUTIONLIST(592, "distribution_list"),
    /**
     * Context ID
     */
    CONTEXTID(593, "context_id"),
    /**
     * Number of distribution list
     */
    NUMBER_OF_DISTRIBUTIONLIST(594, "number_of_distribution_list"),
    /**
     * Number of links
     */
    NUMBER_OF_LINKS(595, "number_of_links"),
    /**
     * Number of images
     */
    NUMBER_OF_IMAGES(596, "number_of_images"),
    /**
     * Image last-modified
     */
    IMAGE_LAST_MODIFIED(597, "number_of_distribution_list"),
    /**
     * Contains image1
     */
    CONTAINS_IMAGE1(-1, "contains_image1"),
    /**
     * Number of distribution list
     */
    FILE_AS(599, "file_as"),
    /**
     * Number of distribution list
     */
    IMAGE1_CONTENT_TYPE(601, "image1_content_type"),
    /**
     * Mark as distribution list
     */
    MARK_AS_DISTRIBUTIONLIST(602, "mark_as_distributionlist"),
    /**
     * Image1 URL
     */
    IMAGE1_URL(606, "image1_url"),
    /**
     * Special sorting.
     * <p>
     * This attribute identifier has only a sorting purpose. This does not represent a contact attribute. This identifier can be specified
     * only for the sorting column. The sorting is the done the following way: Use one of {@link #LAST_NAME}, {@link #DISPLAY_NAME},
     * {@link #COMPANY}, {@link #EMAIL1} or {@link #EMAIL2} in this order whichever is first not null. Use the selected value for sorting
     * with the AlphanumComparator.
     */
    SPECIAL_SORTING(607, null),
    /**
     * Use count
     */
    USE_COUNT(608, "useCount"),
    /**
     * The same as {@link #USE_COUNT}, but with respect to the global address book (only for searching purpose).
     */
    USE_COUNT_GLOBAL_FIRST(609, null),

    // ########################### USER ATTRIBUTES #########################################

    /**
     * Aliases
     */
    ALIASES(610, "aliases"),
    /**
     * Time zone
     */
    TIME_ZONE(611, "timezone"),
    /**
     * Locale
     */
    LOCALE(612, "locale"),
    /**
     * The user's groups
     */
    GROUPS(613, "groups"),
    /**
     * The contact ID
     */
    CONTACT_ID(614, "contact_id"),
    /**
     * The login info
     */
    LOGIN_INFO(615, "login_info"),
    /**
     * The ID of the user who created the guest
     */
    GUEST_CREATED_BY(616, "guest_created_by"),

    ;

    private final int column;

    private final String name;

    private UserField(final int column, final String name) {
        this.column = column;
        this.name = name;
    }

    /**
     * Gets the column or <code>-1</code> if none available.
     *
     * @return The column or <code>-1</code> if none available
     */
    public int getColumn() {
        return column;
    }

    /**
     * Gets the name or <code>null</code> if none available.
     *
     * @return The name or <code>null</code> if none available
     */
    public String getName() {
        return name;
    }

    private static final EnumSet<UserField> USER_ONLY_FIELDS = EnumSet.of(
        ALIASES, TIME_ZONE, LOCALE, GROUPS, CONTACT_ID, LOGIN_INFO, GUEST_CREATED_BY);

    /**
     * Gets the user-only field corresponding to given field number.
     *
     * @param field The field number
     * @return The user-only field or <code>null</code>
     */
    public static UserField getUserOnlyField(final int field) {
        for (final UserField uf : USER_ONLY_FIELDS) {
            if (uf.getColumn() == field) {
                return uf;
            }
        }
        return null;
    }

    public static boolean isUserOnlyField(final int field) {
        return null != getUserOnlyField(field);
    }

    public static final EnumSet<UserField> UNPROTECTED_FIELDS = EnumSet.of(
        ID, DISPLAY_NAME, FIRST_NAME, LAST_NAME, SECOND_NAME, SUFFIX, GUEST_CREATED_BY);

    public static boolean isProtected(final int field) {
        for (final UserField uf : UNPROTECTED_FIELDS) {
            if(uf.getColumn() == field) {
                return false;
            }
        }
        return true;
    }

    /**
     * The constant describing all fields of a user object.
     */
    public static final UserField[] ALL_FIELDS =
        {
            // From ContactObject itself
            DISPLAY_NAME, FIRST_NAME, LAST_NAME, SECOND_NAME, SUFFIX, TITLE, STREET_HOME, POSTAL_CODE_HOME, CITY_HOME, STATE_HOME,
            COUNTRY_HOME, BIRTHDAY, MARITAL_STATUS, NUMBER_OF_CHILDREN, PROFESSION, NICKNAME, SPOUSE_NAME, ANNIVERSARY, NOTE, DEPARTMENT,
            POSITION, EMPLOYEE_TYPE, ROOM_NUMBER, STREET_BUSINESS, POSTAL_CODE_BUSINESS, CITY_BUSINESS, STATE_BUSINESS, COUNTRY_BUSINESS,
            NUMBER_OF_EMPLOYEE, SALES_VOLUME, TAX_ID, COMMERCIAL_REGISTER, BRANCHES, BUSINESS_CATEGORY, INFO, MANAGER_NAME, ASSISTANT_NAME,
            STREET_OTHER, POSTAL_CODE_OTHER, CITY_OTHER, STATE_OTHER, COUNTRY_OTHER, TELEPHONE_BUSINESS1, TELEPHONE_BUSINESS2,
            FAX_BUSINESS, TELEPHONE_CALLBACK, TELEPHONE_CAR, TELEPHONE_COMPANY, TELEPHONE_HOME1, TELEPHONE_HOME2, FAX_HOME,
            CELLULAR_TELEPHONE1, CELLULAR_TELEPHONE2, TELEPHONE_OTHER, FAX_OTHER, EMAIL1, EMAIL2, EMAIL3, URL, TELEPHONE_ISDN,
            TELEPHONE_PAGER, TELEPHONE_PRIMARY, TELEPHONE_RADIO, TELEPHONE_TELEX, TELEPHONE_TTYTDD, INSTANT_MESSENGER1, INSTANT_MESSENGER2,
            TELEPHONE_IP, TELEPHONE_ASSISTANT, COMPANY, IMAGE1, USERFIELD01, USERFIELD02, USERFIELD03, USERFIELD04, USERFIELD05,
            USERFIELD06, USERFIELD07, USERFIELD08, USERFIELD09, USERFIELD10, USERFIELD11, USERFIELD12, USERFIELD13, USERFIELD14,
            USERFIELD15, USERFIELD16, USERFIELD17, USERFIELD18, USERFIELD19, USERFIELD20, LINKS, DISTRIBUTIONLIST, INTERNAL_USERID,
            // Produces error: missing field in mapping: 593 (ContactWriter.java:603)// CONTEXTID,
            NUMBER_OF_DISTRIBUTIONLIST, NUMBER_OF_LINKS, // NUMBER_OF_IMAGES,
            // IMAGE_LAST_MODIFIED, FILE_AS,
            // Produces a MySQLDataException// ATTACHMENT,
            // IMAGE1_CONTENT_TYPE, MARK_AS_DISTRIBUTIONLIST,
            DEFAULT_ADDRESS,
            // IMAGE1_URL,
            USE_COUNT,
            // From CommonObject
            // Left out as it is unclear what these are for and they produce an error//LABEL_NONE, LABEL_1, LABEL_2, LABEL_3, LABEL_4,
            // LABEL_5, LABEL_6, LABEL_7, LABEL_8, LABEL_9, LABEL_10,
            CATEGORIES, PRIVATE_FLAG, COLOR_LABEL, NUMBER_OF_ATTACHMENTS,
            // From FolderChildObject
            FOLDER_ID,
            // From DataObject
            ID, CREATED_BY, MODIFIED_BY, CREATION_DATE, LAST_MODIFIED, LAST_MODIFIED_UTC,
            // From user
            ALIASES, TIME_ZONE, LOCALE, GROUPS, CONTACT_ID, LOGIN_INFO, GUEST_CREATED_BY };

}
