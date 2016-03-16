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

package com.openexchange.user.json.parser;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.user.json.field.DistributionListField;
import com.openexchange.user.json.field.UserField;

/**
 * {@link UserParser} - Parses a user from JSON data.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UserParser {

    /**
     * Initializes a new {@link UserParser}.
     */
    private UserParser() {
        super();
    }

    /**
     * Parses a user data from given JSON object.
     *
     * @param userJSONObject The JSON object containing user data
     * @param userId The user ID
     * @return The parsed user
     * @throws OXException If parsing user data fails
     */
    public static ParsedUser parseUserData(final JSONObject userJSONObject, final int userId) throws OXException {
        try {
            final ParsedUser user = new ParsedUser();
            if (userJSONObject.has(UserField.LOCALE.getName())) {
                user.setLocale(parseLocaleString(userJSONObject.getString(UserField.LOCALE.getName())));
            }
            if (userJSONObject.has(UserField.TIME_ZONE.getName())) {
                user.setTimeZone(userJSONObject.getString(UserField.TIME_ZONE.getName()));
            }
            user.setId(userId);
            return user;
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create( e, e.getMessage());
        }
    }

    /**
     * Parses a user contact from given JSON object.
     *
     * @param userJSONObject The JSON object containing user contact data
     * @param timeZone The time zone of requesting session's user
     * @return The parsed user contact
     * @throws OXException If parsing user contact fails
     */
    public static Contact parseUserContact(final JSONObject userJSONObject, final TimeZone timeZone) throws OXException {
        try {
            final Contact contact = new Contact();
            if (null != userJSONObject) {
                for (final JSONAttributeMapper element : mapping) {
                    if (element.jsonObjectContains(userJSONObject)) {
                        element.setObject(contact, userJSONObject);
                    }
                }
                if (userJSONObject.has(UserField.DISTRIBUTIONLIST.getName())) {
                    parseDistributionList(contact, userJSONObject);
                }
                if (userJSONObject.has(UserField.CATEGORIES.getName())) {
                    contact.setCategories(parseString(userJSONObject, UserField.CATEGORIES.getName()));
                }
                if (userJSONObject.has(UserField.COLOR_LABEL.getName())) {
                    contact.setLabel(parseInt(userJSONObject, UserField.COLOR_LABEL.getName()));
                }
                if (userJSONObject.has(UserField.PRIVATE_FLAG.getName())) {
                    contact.setPrivateFlag(parseBoolean(userJSONObject, UserField.PRIVATE_FLAG.getName()));
                }
                if (userJSONObject.has(UserField.NUMBER_OF_ATTACHMENTS.getName())) {
                    contact.setNumberOfAttachments(parseInt(userJSONObject, UserField.NUMBER_OF_ATTACHMENTS.getName()));
                }
                if (userJSONObject.has(UserField.FOLDER_ID.getName())) {
                    contact.setParentFolderID(parseInt(userJSONObject, UserField.FOLDER_ID.getName()));
                }
                if (userJSONObject.has(UserField.CONTACT_ID.getName())) {
                    contact.setObjectID(parseInt(userJSONObject, UserField.CONTACT_ID.getName()));
                }
                if (userJSONObject.has(UserField.INTERNAL_USERID.getName())) {
                    contact.setInternalUserId(parseInt(userJSONObject, UserField.INTERNAL_USERID.getName()));
                }
                if (userJSONObject.has(UserField.CREATED_BY.getName())) {
                    contact.setCreatedBy(parseInt(userJSONObject, UserField.CREATED_BY.getName()));
                }
                if (userJSONObject.has(UserField.CREATION_DATE.getName())) {
                    contact.setCreationDate(parseTime(userJSONObject, UserField.CREATION_DATE.getName(), timeZone));
                }
                if (userJSONObject.has(UserField.MODIFIED_BY.getName())) {
                    contact.setModifiedBy(parseInt(userJSONObject, UserField.MODIFIED_BY.getName()));
                }
                if (userJSONObject.has(UserField.LAST_MODIFIED.getName())) {
                    contact.setLastModified(parseTime(userJSONObject, UserField.LAST_MODIFIED.getName(), timeZone));
                }
                if (userJSONObject.has(UserField.NUMBER_OF_IMAGES.getName())) {
                    contact.setNumberOfImages(parseInt(userJSONObject, UserField.NUMBER_OF_IMAGES.getName()));
                }
                if (userJSONObject.has(UserField.IMAGE1_CONTENT_TYPE.getName())) {
                    contact.setImageContentType(parseString(userJSONObject, UserField.IMAGE1_CONTENT_TYPE.getName()));
                }
            }
            return contact;
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create( e, e.getMessage());
        }
    }

    private static void parseDistributionList(final Contact oxobject, final JSONObject jsonobject) throws JSONException, OXException {
        final JSONArray jdistributionlist = jsonobject.getJSONArray(ContactFields.DISTRIBUTIONLIST);
        final DistributionListEntryObject[] distributionlist = new DistributionListEntryObject[jdistributionlist.length()];
        for (int a = 0; a < jdistributionlist.length(); a++) {
            final JSONObject entry = jdistributionlist.getJSONObject(a);
            distributionlist[a] = new DistributionListEntryObject();
            if (entry.has(DataFields.ID)) {
                distributionlist[a].setEntryID(parseInt(entry, DataFields.ID));
            }

            if (entry.has(UserField.FIRST_NAME.getName())) {
                distributionlist[a].setFirstname(parseString(entry, UserField.FIRST_NAME.getName()));
            }

            if (entry.has(UserField.LAST_NAME.getName())) {
                distributionlist[a].setLastname(parseString(entry, UserField.LAST_NAME.getName()));
            }

            distributionlist[a].setDisplayname(parseString(entry, UserField.DISPLAY_NAME.getName()));
            distributionlist[a].setEmailaddress(parseString(entry, DistributionListField.MAIL.getName()));
            distributionlist[a].setEmailfield(parseInt(entry, DistributionListField.MAIL_FIELD.getName()));
        }
        oxobject.setDistributionList(distributionlist);
    }

    /*-
     * #################################### MAPPERS ####################################
     */

    private static final Pattern identifierPattern = Pattern.compile("(\\p{Lower}{2})(?:_(\\p{Upper}{2}))?(?:_([a-zA-Z]{2}))?");

    /**
     * Parses given locale string into an instance of {@link Locale}
     *
     * @param localeStr The locale string to parse
     * @return The parsed instance of {@link Locale}
     * @throws OXException If locale string is invalid
     */
    private static Locale parseLocaleString(final String localeStr) throws OXException {
        final Matcher match = identifierPattern.matcher(localeStr);
        Locale retval = null;
        if (match.matches()) {
            final String country = match.group(2);
            final String variant = match.group(3);
            retval = new Locale(match.group(1), country == null ? "" : country, variant == null ? "" : variant);
        }
        return retval;
    }

    private static interface JSONAttributeMapper {

        boolean jsonObjectContains(JSONObject jsonobject);

        void setObject(Contact contactobject, JSONObject jsonobject) throws JSONException;
    }

    private static final JSONAttributeMapper[] mapping = new JSONAttributeMapper[] { new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.LAST_NAME);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setSurName(parseString(jsonobject, ContactFields.LAST_NAME));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.TITLE);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setTitle(parseString(jsonobject, ContactFields.TITLE));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.FIRST_NAME);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setGivenName(parseString(jsonobject, ContactFields.FIRST_NAME));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.MARITAL_STATUS);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setMaritalStatus(parseString(jsonobject, ContactFields.MARITAL_STATUS));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.ANNIVERSARY);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setAnniversary(parseDate(jsonobject, ContactFields.ANNIVERSARY));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.ASSISTANT_NAME);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setAssistantName(parseString(jsonobject, ContactFields.ASSISTANT_NAME));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.BIRTHDAY);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setBirthday(parseDate(jsonobject, ContactFields.BIRTHDAY));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.BRANCHES);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setBranches(parseString(jsonobject, ContactFields.BRANCHES));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.BUSINESS_CATEGORY);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setBusinessCategory(parseString(jsonobject, ContactFields.BUSINESS_CATEGORY));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.CELLULAR_TELEPHONE1);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setCellularTelephone1(parseString(jsonobject, ContactFields.CELLULAR_TELEPHONE1));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.CELLULAR_TELEPHONE2);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setCellularTelephone2(parseString(jsonobject, ContactFields.CELLULAR_TELEPHONE2));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.CITY_HOME);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setCityHome(parseString(jsonobject, ContactFields.CITY_HOME));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.CITY_BUSINESS);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setCityBusiness(parseString(jsonobject, ContactFields.CITY_BUSINESS));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.CITY_OTHER);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setCityOther(parseString(jsonobject, ContactFields.CITY_OTHER));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.COMMERCIAL_REGISTER);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setCommercialRegister(parseString(jsonobject, ContactFields.COMMERCIAL_REGISTER));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.COMPANY);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setCompany(parseString(jsonobject, ContactFields.COMPANY));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.COUNTRY_HOME);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setCountryHome(parseString(jsonobject, ContactFields.COUNTRY_HOME));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.COUNTRY_BUSINESS);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setCountryBusiness(parseString(jsonobject, ContactFields.COUNTRY_BUSINESS));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.COUNTRY_OTHER);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setCountryOther(parseString(jsonobject, ContactFields.COUNTRY_OTHER));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.DEFAULT_ADDRESS);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setDefaultAddress(parseInt(jsonobject, ContactFields.DEFAULT_ADDRESS));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.DEPARTMENT);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setDepartment(parseString(jsonobject, ContactFields.DEPARTMENT));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.DISPLAY_NAME);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setDisplayName(parseString(jsonobject, ContactFields.DISPLAY_NAME));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.MARK_AS_DISTRIBUTIONLIST);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setMarkAsDistributionlist(parseBoolean(jsonobject, ContactFields.MARK_AS_DISTRIBUTIONLIST));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.EMAIL1);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setEmail1(parseString(jsonobject, ContactFields.EMAIL1));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.EMAIL2);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setEmail2(parseString(jsonobject, ContactFields.EMAIL2));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.EMAIL3);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setEmail3(parseString(jsonobject, ContactFields.EMAIL3));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.EMPLOYEE_TYPE);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setEmployeeType(parseString(jsonobject, ContactFields.EMPLOYEE_TYPE));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.FAX_BUSINESS);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setFaxBusiness(parseString(jsonobject, ContactFields.FAX_BUSINESS));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.FAX_HOME);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setFaxHome(parseString(jsonobject, ContactFields.FAX_HOME));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.FAX_OTHER);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setFaxOther(parseString(jsonobject, ContactFields.FAX_OTHER));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.IMAGE1);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            final String image = parseString(jsonobject, ContactFields.IMAGE1);
            if (image != null) {
                contactobject.setImage1(image.getBytes());
            } else {
                contactobject.setImage1(null);
            }
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.NOTE);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setNote(parseString(jsonobject, ContactFields.NOTE));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.INFO);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setInfo(parseString(jsonobject, ContactFields.INFO));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.INSTANT_MESSENGER1);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setInstantMessenger1(parseString(jsonobject, ContactFields.INSTANT_MESSENGER1));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.INSTANT_MESSENGER2);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setInstantMessenger2(parseString(jsonobject, ContactFields.INSTANT_MESSENGER2));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.MANAGER_NAME);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setManagerName(parseString(jsonobject, ContactFields.MANAGER_NAME));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.SECOND_NAME);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setMiddleName(parseString(jsonobject, ContactFields.SECOND_NAME));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.NICKNAME);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setNickname(parseString(jsonobject, ContactFields.NICKNAME));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.NUMBER_OF_CHILDREN);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setNumberOfChildren(parseString(jsonobject, ContactFields.NUMBER_OF_CHILDREN));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.NUMBER_OF_EMPLOYEE);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setNumberOfEmployee(parseString(jsonobject, ContactFields.NUMBER_OF_EMPLOYEE));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.POSITION);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setPosition(parseString(jsonobject, ContactFields.POSITION));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.POSTAL_CODE_HOME);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setPostalCodeHome(parseString(jsonobject, ContactFields.POSTAL_CODE_HOME));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.POSTAL_CODE_BUSINESS);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setPostalCodeBusiness(parseString(jsonobject, ContactFields.POSTAL_CODE_BUSINESS));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.POSTAL_CODE_OTHER);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setPostalCodeOther(parseString(jsonobject, ContactFields.POSTAL_CODE_OTHER));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.PROFESSION);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setProfession(parseString(jsonobject, ContactFields.PROFESSION));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.ROOM_NUMBER);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setRoomNumber(parseString(jsonobject, ContactFields.ROOM_NUMBER));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.SALES_VOLUME);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setSalesVolume(parseString(jsonobject, ContactFields.SALES_VOLUME));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.SPOUSE_NAME);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setSpouseName(parseString(jsonobject, ContactFields.SPOUSE_NAME));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.STATE_HOME);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setStateHome(parseString(jsonobject, ContactFields.STATE_HOME));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.STATE_BUSINESS);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setStateBusiness(parseString(jsonobject, ContactFields.STATE_BUSINESS));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.STATE_OTHER);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setStateOther(parseString(jsonobject, ContactFields.STATE_OTHER));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.STREET_HOME);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setStreetHome(parseString(jsonobject, ContactFields.STREET_HOME));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.STREET_BUSINESS);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setStreetBusiness(parseString(jsonobject, ContactFields.STREET_BUSINESS));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.STREET_OTHER);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setStreetOther(parseString(jsonobject, ContactFields.STREET_OTHER));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.SUFFIX);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setSuffix(parseString(jsonobject, ContactFields.SUFFIX));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.TAX_ID);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setTaxID(parseString(jsonobject, ContactFields.TAX_ID));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.TELEPHONE_ASSISTANT);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setTelephoneAssistant(parseString(jsonobject, ContactFields.TELEPHONE_ASSISTANT));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.TELEPHONE_BUSINESS1);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setTelephoneBusiness1(parseString(jsonobject, ContactFields.TELEPHONE_BUSINESS1));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.TELEPHONE_BUSINESS2);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setTelephoneBusiness2(parseString(jsonobject, ContactFields.TELEPHONE_BUSINESS2));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.TELEPHONE_CALLBACK);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setTelephoneCallback(parseString(jsonobject, ContactFields.TELEPHONE_CALLBACK));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.TELEPHONE_CAR);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setTelephoneCar(parseString(jsonobject, ContactFields.TELEPHONE_CAR));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.TELEPHONE_COMPANY);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setTelephoneCompany(parseString(jsonobject, ContactFields.TELEPHONE_COMPANY));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.TELEPHONE_HOME1);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setTelephoneHome1(parseString(jsonobject, ContactFields.TELEPHONE_HOME1));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.TELEPHONE_HOME2);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setTelephoneHome2(parseString(jsonobject, ContactFields.TELEPHONE_HOME2));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.TELEPHONE_IP);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setTelephoneIP(parseString(jsonobject, ContactFields.TELEPHONE_IP));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.TELEPHONE_ISDN);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setTelephoneISDN(parseString(jsonobject, ContactFields.TELEPHONE_ISDN));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.TELEPHONE_OTHER);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setTelephoneOther(parseString(jsonobject, ContactFields.TELEPHONE_OTHER));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.TELEPHONE_PAGER);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setTelephonePager(parseString(jsonobject, ContactFields.TELEPHONE_PAGER));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.TELEPHONE_PRIMARY);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setTelephonePrimary(parseString(jsonobject, ContactFields.TELEPHONE_PRIMARY));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.TELEPHONE_RADIO);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setTelephoneRadio(parseString(jsonobject, ContactFields.TELEPHONE_RADIO));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.TELEPHONE_TELEX);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setTelephoneTelex(parseString(jsonobject, ContactFields.TELEPHONE_TELEX));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.TELEPHONE_TTYTDD);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setTelephoneTTYTTD(parseString(jsonobject, ContactFields.TELEPHONE_TTYTDD));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.URL);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setURL(parseString(jsonobject, ContactFields.URL));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.USE_COUNT);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setUseCount(parseInt(jsonobject, ContactFields.USE_COUNT));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.USERFIELD01);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setUserField01(parseString(jsonobject, ContactFields.USERFIELD01));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.USERFIELD02);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setUserField02(parseString(jsonobject, ContactFields.USERFIELD02));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.USERFIELD03);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setUserField03(parseString(jsonobject, ContactFields.USERFIELD03));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.USERFIELD04);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setUserField04(parseString(jsonobject, ContactFields.USERFIELD04));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.USERFIELD05);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setUserField05(parseString(jsonobject, ContactFields.USERFIELD05));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.USERFIELD06);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setUserField06(parseString(jsonobject, ContactFields.USERFIELD06));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.USERFIELD07);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setUserField07(parseString(jsonobject, ContactFields.USERFIELD07));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.USERFIELD08);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setUserField08(parseString(jsonobject, ContactFields.USERFIELD08));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.USERFIELD09);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setUserField09(parseString(jsonobject, ContactFields.USERFIELD09));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.USERFIELD10);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setUserField10(parseString(jsonobject, ContactFields.USERFIELD10));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.USERFIELD11);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setUserField11(parseString(jsonobject, ContactFields.USERFIELD11));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.USERFIELD12);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setUserField12(parseString(jsonobject, ContactFields.USERFIELD12));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.USERFIELD13);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setUserField13(parseString(jsonobject, ContactFields.USERFIELD13));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.USERFIELD14);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setUserField14(parseString(jsonobject, ContactFields.USERFIELD14));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.USERFIELD15);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setUserField15(parseString(jsonobject, ContactFields.USERFIELD15));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.USERFIELD16);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setUserField16(parseString(jsonobject, ContactFields.USERFIELD16));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.USERFIELD17);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setUserField17(parseString(jsonobject, ContactFields.USERFIELD17));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.USERFIELD18);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setUserField18(parseString(jsonobject, ContactFields.USERFIELD18));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.USERFIELD19);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setUserField19(parseString(jsonobject, ContactFields.USERFIELD19));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.USERFIELD20);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setUserField20(parseString(jsonobject, ContactFields.USERFIELD20));
        }
    }, new JSONAttributeMapper() {

        @Override
        public boolean jsonObjectContains(final JSONObject jsonobject) {
            return jsonobject.has(ContactFields.UID);
        }

        @Override
        public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
            contactobject.setUid(parseString(jsonobject, ContactFields.UID));
        }
    } };

    /**
     * Parses optional field out of specified JSON object.
     *
     * @param jsonObj The JSON object to parse
     * @param name The optional field name
     * @return The optional field's value or <code>null</code> if there's no such field
     * @throws JSONException If a JSON error occurs
     */
    static String parseString(final JSONObject jsonObj, final String name) throws JSONException {
        String retval = null;
        if (jsonObj.hasAndNotNull(name)) {
            final String test = jsonObj.getString(name);
            if (0 != test.length()) {
                retval = test;
            }
        }
        return retval;
    }

    static int parseInt(final JSONObject jsonObj, final String name) throws JSONException {
        if (!jsonObj.has(name)) {
            return 0;
        }

        final String tmp = jsonObj.getString(name);
        if (tmp == null || jsonObj.isNull(name) || tmp.length() == 0) {
            return 0;
        }
        try {
            return Integer.parseInt(tmp);
        } catch (final NumberFormatException exc) {
            final StringBuilder sb = new StringBuilder(32).append("Attribute \"");
            sb.append(name).append("\" is not a number: ").append(tmp);
            throw new JSONException(sb.toString());
        }
    }

    static boolean parseBoolean(final JSONObject jsonObj, final String name) throws JSONException {
        if (!jsonObj.has(name)) {
            return false;
        }

        return jsonObj.getBoolean(name);
    }

    static Date parseDate(final JSONObject jsonObj, final String name) throws JSONException {
        if (!jsonObj.has(name)) {
            return null;
        }

        final String tmp = parseString(jsonObj, name);
        if (tmp == null) {
            return null;
        }
        try {
            return new Date(Long.parseLong(tmp));
        } catch (final NumberFormatException e) {
            final StringBuilder sb = new StringBuilder(64).append("Attribute \"");
            sb.append(name).append("\" does not denote date's milliseconds since January 1, 1970, 00:00:00 GMT: ").append(tmp);
            throw new JSONException(sb.toString());
        }
    }

    static Date parseTime(final JSONObject jsonObj, final String name, final TimeZone timeZone) throws JSONException {
        final Date d = parseDate(jsonObj, name);
        if (d == null) {
            return null;
        }

        final int offset = timeZone.getOffset(d.getTime());
        d.setTime(d.getTime() - offset);
        return d;
    }

}
