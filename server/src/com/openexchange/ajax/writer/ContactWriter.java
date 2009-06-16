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

package com.openexchange.ajax.writer;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.ajax.fields.DistributionListFields;
import com.openexchange.conversion.DataArguments;
import com.openexchange.groupware.contact.datasource.ContactImageDataSource;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.LinkEntryObject;
import com.openexchange.image.ImageService;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link ContactWriter} - The writer for contacts
 * 
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ContactWriter extends CommonWriter {

    private final TimeZone utc;

    /**
     * Initializes a new {@link ContactWriter}
     * 
     * @param timeZone The user time zone
     * @param ctx The context
     */
    public ContactWriter(final TimeZone timeZone) {
        super(timeZone, null);
        utc = TimeZone.getTimeZone("utc");
    }

    public void writeArray(final ContactObject contactobject, final int cols[], final JSONArray jsonArray) throws JSONException {
        for (int a = 0; a < cols.length; a++) {
            write(cols[a], contactobject, jsonArray);
        }
    }

    public void writeContact(final ContactObject contactobject, final JSONObject jsonObj) throws JSONException {
        writeCommonFields(contactobject, jsonObj);

        writeParameter(ContactFields.LAST_NAME, contactobject.getSurName(), jsonObj);
        writeParameter(ContactFields.FIRST_NAME, contactobject.getGivenName(), jsonObj);
        writeParameter(ContactFields.ANNIVERSARY, contactobject.getAnniversary(), jsonObj);
        writeParameter(ContactFields.ASSISTANT_NAME, contactobject.getAssistantName(), jsonObj);
        writeParameter(ContactFields.BIRTHDAY, contactobject.getBirthday(), jsonObj);
        writeParameter(ContactFields.BRANCHES, contactobject.getBranches(), jsonObj);
        writeParameter(ContactFields.BUSINESS_CATEGORY, contactobject.getBusinessCategory(), jsonObj);
        writeParameter(ContactFields.CELLULAR_TELEPHONE1, contactobject.getCellularTelephone1(), jsonObj);
        writeParameter(ContactFields.CELLULAR_TELEPHONE2, contactobject.getCellularTelephone2(), jsonObj);
        writeParameter(ContactFields.CITY_HOME, contactobject.getCityHome(), jsonObj);
        writeParameter(ContactFields.CITY_BUSINESS, contactobject.getCityBusiness(), jsonObj);
        writeParameter(ContactFields.CITY_OTHER, contactobject.getCityOther(), jsonObj);
        writeParameter(ContactFields.COMMERCIAL_REGISTER, contactobject.getCommercialRegister(), jsonObj);
        writeParameter(ContactFields.COMPANY, contactobject.getCompany(), jsonObj);
        writeParameter(ContactFields.COUNTRY_HOME, contactobject.getCountryHome(), jsonObj);
        writeParameter(ContactFields.COUNTRY_BUSINESS, contactobject.getCountryBusiness(), jsonObj);
        writeParameter(ContactFields.COUNTRY_OTHER, contactobject.getCountryOther(), jsonObj);
        writeParameter(ContactFields.DEFAULT_ADDRESS, contactobject.getDefaultAddress(), jsonObj, contactobject.containsDefaultAddress());
        writeParameter(ContactFields.DEPARTMENT, contactobject.getDepartment(), jsonObj);
        writeParameter(ContactFields.DISPLAY_NAME, contactobject.getDisplayName(), jsonObj);
        writeParameter(ContactFields.EMAIL1, contactobject.getEmail1(), jsonObj);
        writeParameter(ContactFields.EMAIL2, contactobject.getEmail2(), jsonObj);
        writeParameter(ContactFields.EMAIL3, contactobject.getEmail3(), jsonObj);
        writeParameter(ContactFields.EMPLOYEE_TYPE, contactobject.getEmployeeType(), jsonObj);
        writeParameter(ContactFields.FAX_BUSINESS, contactobject.getFaxBusiness(), jsonObj);
        writeParameter(ContactFields.FAX_HOME, contactobject.getFaxHome(), jsonObj);
        writeParameter(ContactFields.FAX_OTHER, contactobject.getFaxOther(), jsonObj);
        if (contactobject.containsImage1()) {
            writeParameter(ContactFields.CONTAINS_IMAGE1, contactobject.getNumberOfImages(), jsonObj);
            if (contactobject.containsContextId()) {
                final byte[] imageData = contactobject.getImage1();
                if (imageData != null) {
                    final String imageURL;
                    {
                        final ContactImageDataSource imgSource = new ContactImageDataSource();
                        final DataArguments args = new DataArguments();
                        final String[] argsNames = imgSource.getRequiredArguments();
                        args.put(argsNames[0], String.valueOf(contactobject.getParentFolderID()));
                        args.put(argsNames[1], String.valueOf(contactobject.getObjectID()));
                        imageURL = ServerServiceRegistry.getInstance().getService(ImageService.class).addImageData(
                            contactobject.getContextId(),
                            imgSource,
                            args).getImageURL();
                    }
                    writeParameter(ContactFields.IMAGE1_URL, imageURL, jsonObj);
                }
            }
        }
        // writeParameter(ContactFields.IMAGE1, contactobject.getImage1());
        writeParameter(ContactFields.INFO, contactobject.getInfo(), jsonObj);
        writeParameter(ContactFields.NOTE, contactobject.getNote(), jsonObj);
        writeParameter(ContactFields.INSTANT_MESSENGER1, contactobject.getInstantMessenger1(), jsonObj);
        writeParameter(ContactFields.INSTANT_MESSENGER2, contactobject.getInstantMessenger2(), jsonObj);
        writeParameter(ContactFields.MARITAL_STATUS, contactobject.getMaritalStatus(), jsonObj);
        writeParameter(ContactFields.MANAGER_NAME, contactobject.getManagerName(), jsonObj);
        writeParameter(ContactFields.SECOND_NAME, contactobject.getMiddleName(), jsonObj);
        writeParameter(ContactFields.NICKNAME, contactobject.getNickname(), jsonObj);
        writeParameter(ContactFields.NUMBER_OF_CHILDREN, contactobject.getNumberOfChildren(), jsonObj);
        writeParameter(ContactFields.NUMBER_OF_EMPLOYEE, contactobject.getNumberOfEmployee(), jsonObj);
        writeParameter(ContactFields.POSITION, contactobject.getPosition(), jsonObj);
        writeParameter(ContactFields.POSTAL_CODE_HOME, contactobject.getPostalCodeHome(), jsonObj);
        writeParameter(ContactFields.POSTAL_CODE_BUSINESS, contactobject.getPostalCodeBusiness(), jsonObj);
        writeParameter(ContactFields.POSTAL_CODE_OTHER, contactobject.getPostalCodeOther(), jsonObj);
        writeParameter(ContactFields.PROFESSION, contactobject.getProfession(), jsonObj);
        writeParameter(ContactFields.ROOM_NUMBER, contactobject.getRoomNumber(), jsonObj);
        writeParameter(ContactFields.SALES_VOLUME, contactobject.getSalesVolume(), jsonObj);
        writeParameter(ContactFields.SPOUSE_NAME, contactobject.getSpouseName(), jsonObj);
        writeParameter(ContactFields.STATE_HOME, contactobject.getStateHome(), jsonObj);
        writeParameter(ContactFields.STATE_BUSINESS, contactobject.getStateBusiness(), jsonObj);
        writeParameter(ContactFields.STATE_OTHER, contactobject.getStateOther(), jsonObj);
        writeParameter(ContactFields.STREET_HOME, contactobject.getStreetHome(), jsonObj);
        writeParameter(ContactFields.STREET_BUSINESS, contactobject.getStreetBusiness(), jsonObj);
        writeParameter(ContactFields.STREET_OTHER, contactobject.getStreetOther(), jsonObj);
        writeParameter(ContactFields.SUFFIX, contactobject.getSuffix(), jsonObj);
        writeParameter(ContactFields.TAX_ID, contactobject.getTaxID(), jsonObj);
        writeParameter(ContactFields.TELEPHONE_ASSISTANT, contactobject.getTelephoneAssistant(), jsonObj);
        writeParameter(ContactFields.TELEPHONE_BUSINESS1, contactobject.getTelephoneBusiness1(), jsonObj);
        writeParameter(ContactFields.TELEPHONE_BUSINESS2, contactobject.getTelephoneBusiness2(), jsonObj);
        writeParameter(ContactFields.TELEPHONE_CALLBACK, contactobject.getTelephoneCallback(), jsonObj);
        writeParameter(ContactFields.TELEPHONE_CAR, contactobject.getTelephoneCar(), jsonObj);
        writeParameter(ContactFields.TELEPHONE_COMPANY, contactobject.getTelephoneCompany(), jsonObj);
        writeParameter(ContactFields.TELEPHONE_HOME1, contactobject.getTelephoneHome1(), jsonObj);
        writeParameter(ContactFields.TELEPHONE_HOME2, contactobject.getTelephoneHome2(), jsonObj);
        writeParameter(ContactFields.TELEPHONE_IP, contactobject.getTelephoneIP(), jsonObj);
        writeParameter(ContactFields.TELEPHONE_ISDN, contactobject.getTelephoneISDN(), jsonObj);
        writeParameter(ContactFields.TELEPHONE_OTHER, contactobject.getTelephoneOther(), jsonObj);
        writeParameter(ContactFields.TELEPHONE_PAGER, contactobject.getTelephonePager(), jsonObj);
        writeParameter(ContactFields.TELEPHONE_PRIMARY, contactobject.getTelephonePrimary(), jsonObj);
        writeParameter(ContactFields.TELEPHONE_RADIO, contactobject.getTelephoneRadio(), jsonObj);
        writeParameter(ContactFields.TELEPHONE_TELEX, contactobject.getTelephoneTelex(), jsonObj);
        writeParameter(ContactFields.TELEPHONE_TTYTDD, contactobject.getTelephoneTTYTTD(), jsonObj);
        writeParameter(ContactFields.TITLE, contactobject.getTitle(), jsonObj);
        writeParameter(ContactFields.URL, contactobject.getURL(), jsonObj);
        writeParameter(ContactFields.USERFIELD01, contactobject.getUserField01(), jsonObj);
        writeParameter(ContactFields.USERFIELD02, contactobject.getUserField02(), jsonObj);
        writeParameter(ContactFields.USERFIELD03, contactobject.getUserField03(), jsonObj);
        writeParameter(ContactFields.USERFIELD04, contactobject.getUserField04(), jsonObj);
        writeParameter(ContactFields.USERFIELD05, contactobject.getUserField05(), jsonObj);
        writeParameter(ContactFields.USERFIELD06, contactobject.getUserField06(), jsonObj);
        writeParameter(ContactFields.USERFIELD07, contactobject.getUserField07(), jsonObj);
        writeParameter(ContactFields.USERFIELD08, contactobject.getUserField08(), jsonObj);
        writeParameter(ContactFields.USERFIELD09, contactobject.getUserField09(), jsonObj);
        writeParameter(ContactFields.USERFIELD10, contactobject.getUserField10(), jsonObj);
        writeParameter(ContactFields.USERFIELD11, contactobject.getUserField11(), jsonObj);
        writeParameter(ContactFields.USERFIELD12, contactobject.getUserField12(), jsonObj);
        writeParameter(ContactFields.USERFIELD13, contactobject.getUserField13(), jsonObj);
        writeParameter(ContactFields.USERFIELD14, contactobject.getUserField14(), jsonObj);
        writeParameter(ContactFields.USERFIELD15, contactobject.getUserField15(), jsonObj);
        writeParameter(ContactFields.USERFIELD16, contactobject.getUserField16(), jsonObj);
        writeParameter(ContactFields.USERFIELD17, contactobject.getUserField17(), jsonObj);
        writeParameter(ContactFields.USERFIELD18, contactobject.getUserField18(), jsonObj);
        writeParameter(ContactFields.USERFIELD19, contactobject.getUserField19(), jsonObj);
        writeParameter(ContactFields.USERFIELD20, contactobject.getUserField20(), jsonObj);
        writeParameter(ContactFields.USER_ID, contactobject.getInternalUserId(), jsonObj);
        writeParameter(
            ContactFields.MARK_AS_DISTRIBUTIONLIST,
            contactobject.getMarkAsDistribtuionlist(),
            jsonObj,
            contactobject.containsMarkAsDistributionlist());
        writeParameter(ContactFields.USE_COUNT, contactobject.getUseCount(), jsonObj);

        final JSONArray jsonLinkArray = getLinksAsJSONArray(contactobject);
        if (jsonLinkArray != null) {
            jsonObj.put(ContactFields.LINKS, jsonLinkArray);
        }

        final JSONArray jsonDistributionListArray = getDistributionListAsJSONArray(contactobject);
        if (jsonDistributionListArray != null) {
            jsonObj.put(ContactFields.DISTRIBUTIONLIST, jsonDistributionListArray);
        }
    }

    static final JSONArray getLinksAsJSONArray(final ContactObject contactobject) throws JSONException {
        final LinkEntryObject[] linkentries = contactobject.getLinks();

        if (linkentries != null) {
            final JSONArray jsonArray = new JSONArray();

            for (int a = 0; a < linkentries.length; a++) {
                final JSONObject jsonLinkObject = new JSONObject();
                writeParameter(ContactFields.ID, linkentries[a].getLinkID(), jsonLinkObject, linkentries[a].containsLinkID());
                writeParameter(ContactFields.DISPLAY_NAME, linkentries[a].getLinkDisplayname(), jsonLinkObject);
                jsonArray.put(jsonLinkObject);
            }
            return jsonArray;
        }
        return null;
    }

    static final JSONArray getDistributionListAsJSONArray(final ContactObject contactobject) throws JSONException {
        final DistributionListEntryObject[] distributionlist = contactobject.getDistributionList();

        if (distributionlist == null) {
            return null;
        }
        final JSONArray jsonArray = new JSONArray();

        for (int a = 0; a < distributionlist.length; a++) {
            final JSONObject jsonDListObj = new JSONObject();
            final int emailField = distributionlist[a].getEmailfield();

            if (!(emailField == DistributionListEntryObject.INDEPENDENT)) {
                writeParameter(DistributionListFields.ID, distributionlist[a].getEntryID(), jsonDListObj);
            }

            writeParameter(DistributionListFields.MAIL, distributionlist[a].getEmailaddress(), jsonDListObj);
            writeParameter(DistributionListFields.DISPLAY_NAME, distributionlist[a].getDisplayname(), jsonDListObj);
            writeParameter(DistributionListFields.MAIL_FIELD, emailField, jsonDListObj);

            jsonArray.put(jsonDListObj);
        }
        return jsonArray;
    }

    public void write(final int field, final ContactObject contactobject, final JSONArray jsonArray) throws JSONException {
        final ContactFieldWriter writer = WRITER_MAP.get(Integer.valueOf(field));
        if (writer != null) {
            writer.write(contactobject, jsonArray);
            return;
        }
        /*
         * No appropriate static writer found, write manually
         */
        switch (field) {
        case ContactObject.CREATION_DATE:
            writeValue(contactobject.getCreationDate(), timeZone, jsonArray);
            break;
        case ContactObject.LAST_MODIFIED:
            writeValue(contactobject.getLastModified(), timeZone, jsonArray);
            break;
        case ContactObject.LAST_MODIFIED_UTC:
            writeValue(contactobject.getLastModified(), utc, jsonArray);
            break;
        default:
            throw new JSONException("missing field in mapping: " + field);
        }
    }

    private static interface ContactFieldWriter {

        /**
         * Writes this writer's value taken from specified contact object to given JSON array
         * 
         * @param contactObject The contact object
         * @param jsonArray The JSON array
         * @throws JSONException If writing to JSON array fails
         */
        public void write(ContactObject contactObject, JSONArray jsonArray) throws JSONException;
    }

    /*-
     * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * ++++++++++++++++++++ INITIALIZATION OF FIELD WRITERS ++++++++++++++++++++
     * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     */

    private static final Map<Integer, ContactFieldWriter> WRITER_MAP;

    static {
        final Map<Integer, ContactFieldWriter> m = new HashMap<Integer, ContactFieldWriter>(128);

        m.put(Integer.valueOf(ContactObject.OBJECT_ID), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getObjectID(), jsonArray, contactObject.containsObjectID());
            }
        });

        m.put(Integer.valueOf(ContactObject.CREATED_BY), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getCreatedBy(), jsonArray, contactObject.containsCreatedBy());
            }
        });

        m.put(Integer.valueOf(ContactObject.MODIFIED_BY), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getModifiedBy(), jsonArray, contactObject.containsModifiedBy());
            }
        });

        m.put(Integer.valueOf(ContactObject.FOLDER_ID), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getParentFolderID(), jsonArray, contactObject.containsParentFolderID());
            }
        });

        m.put(Integer.valueOf(ContactObject.PRIVATE_FLAG), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getPrivateFlag(), jsonArray, contactObject.containsPrivateFlag());
            }
        });

        m.put(Integer.valueOf(ContactObject.SUR_NAME), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getSurName(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.GIVEN_NAME), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getGivenName(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.ANNIVERSARY), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getAnniversary(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.ASSISTANT_NAME), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getAssistantName(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.BIRTHDAY), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getBirthday(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.BRANCHES), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getBranches(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.BUSINESS_CATEGORY), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getBusinessCategory(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.CATEGORIES), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getCategories(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.CELLULAR_TELEPHONE1), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getCellularTelephone1(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.CELLULAR_TELEPHONE2), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getCellularTelephone2(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.CITY_HOME), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getCityHome(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.CITY_BUSINESS), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getCityBusiness(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.CITY_OTHER), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getCityOther(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.COLOR_LABEL), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getLabel(), jsonArray, contactObject.containsLabel());
            }
        });

        m.put(Integer.valueOf(ContactObject.COMMERCIAL_REGISTER), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getCommercialRegister(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.COMPANY), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getCompany(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.COUNTRY_HOME), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getCountryHome(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.COUNTRY_BUSINESS), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getCountryBusiness(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.COUNTRY_OTHER), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getCountryOther(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.DEFAULT_ADDRESS), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getDefaultAddress(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.DEPARTMENT), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getDepartment(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.DISPLAY_NAME), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getDisplayName(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.MARK_AS_DISTRIBUTIONLIST), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getMarkAsDistribtuionlist(), jsonArray, contactObject.containsMarkAsDistributionlist());
            }
        });

        m.put(Integer.valueOf(ContactObject.EMAIL1), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getEmail1(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.EMAIL2), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getEmail2(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.EMAIL3), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getEmail3(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.EMPLOYEE_TYPE), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getEmployeeType(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.FAX_BUSINESS), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getFaxBusiness(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.FAX_HOME), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getFaxHome(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.FAX_OTHER), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getFaxOther(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.IMAGE1), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                final byte[] imageData = contactObject.getImage1();
                if (imageData == null) {
                    writeValueNull(jsonArray);
                } else {
                    writeValue(new String(imageData), jsonArray);
                }
            }
        });

        m.put(Integer.valueOf(ContactObject.IMAGE1_URL), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                if (contactObject.containsContextId()) {
                    final byte[] imageData2 = contactObject.getImage1();
                    if (imageData2 == null) {
                        writeValueNull(jsonArray);
                    } else {
                        final String imageURL;
                        {
                            final ContactImageDataSource imgSource = new ContactImageDataSource();
                            final DataArguments args = new DataArguments();
                            final String[] argsNames = imgSource.getRequiredArguments();
                            args.put(argsNames[0], String.valueOf(contactObject.getParentFolderID()));
                            args.put(argsNames[1], String.valueOf(contactObject.getObjectID()));
                            imageURL = ServerServiceRegistry.getInstance().getService(ImageService.class).addImageData(
                                contactObject.getContextId(),
                                imgSource,
                                args).getImageURL();
                        }
                        writeValue(imageURL, jsonArray);
                    }
                } else {
                    writeValueNull(jsonArray);
                }
            }
        });

        m.put(Integer.valueOf(ContactObject.NUMBER_OF_IMAGES), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getNumberOfImages(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.INFO), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getInfo(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.INSTANT_MESSENGER1), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getInstantMessenger1(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.INSTANT_MESSENGER2), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getInstantMessenger2(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.INTERNAL_USERID), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getInternalUserId(), jsonArray, contactObject.containsInternalUserId());
            }
        });

        m.put(Integer.valueOf(ContactObject.MANAGER_NAME), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getManagerName(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.MARITAL_STATUS), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getMaritalStatus(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.MIDDLE_NAME), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getMiddleName(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.NICKNAME), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getNickname(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.NOTE), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getNote(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.NUMBER_OF_CHILDREN), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getNumberOfChildren(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.NUMBER_OF_EMPLOYEE), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getNumberOfEmployee(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.POSITION), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getPosition(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.POSTAL_CODE_HOME), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getPostalCodeHome(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.POSTAL_CODE_BUSINESS), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getPostalCodeBusiness(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.POSTAL_CODE_OTHER), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getPostalCodeOther(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.PROFESSION), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getProfession(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.ROOM_NUMBER), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getRoomNumber(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.SALES_VOLUME), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getSalesVolume(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.SPOUSE_NAME), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getSpouseName(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.STATE_HOME), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getStateHome(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.STATE_BUSINESS), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getStateBusiness(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.STATE_OTHER), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getStateOther(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.STREET_HOME), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getStreetHome(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.STREET_BUSINESS), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getStreetBusiness(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.STREET_OTHER), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getStreetOther(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.SUFFIX), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getSuffix(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.TAX_ID), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getTaxID(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.TELEPHONE_ASSISTANT), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getTelephoneAssistant(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.TELEPHONE_BUSINESS1), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getTelephoneBusiness1(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.TELEPHONE_BUSINESS2), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getTelephoneBusiness2(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.TELEPHONE_CALLBACK), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getTelephoneCallback(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.TELEPHONE_CAR), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getTelephoneCar(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.TELEPHONE_COMPANY), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getTelephoneCompany(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.TELEPHONE_HOME1), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getTelephoneHome1(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.TELEPHONE_HOME2), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getTelephoneHome2(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.TELEPHONE_IP), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getTelephoneIP(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.TELEPHONE_ISDN), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getTelephoneISDN(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.TELEPHONE_OTHER), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getTelephoneOther(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.TELEPHONE_PAGER), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getTelephonePager(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.TELEPHONE_PRIMARY), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getTelephonePrimary(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.TELEPHONE_RADIO), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getTelephoneRadio(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.TELEPHONE_TELEX), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getTelephoneTelex(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.TELEPHONE_TTYTDD), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getTelephoneTTYTTD(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.TITLE), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getTitle(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.URL), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getURL(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.USERFIELD01), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getUserField01(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.USERFIELD02), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getUserField02(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.USERFIELD03), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getUserField03(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.USERFIELD04), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getUserField04(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.USERFIELD05), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getUserField05(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.USERFIELD06), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getUserField06(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.USERFIELD07), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getUserField07(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.USERFIELD08), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getUserField08(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.USERFIELD09), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getUserField09(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.USERFIELD10), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getUserField10(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.USERFIELD11), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getUserField11(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.USERFIELD12), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getUserField12(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.USERFIELD13), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getUserField13(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.USERFIELD14), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getUserField14(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.USERFIELD15), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getUserField15(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.USERFIELD16), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getUserField16(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.USERFIELD17), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getUserField17(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.USERFIELD18), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getUserField18(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.USERFIELD19), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getUserField19(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.USERFIELD20), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getUserField20(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.NUMBER_OF_ATTACHMENTS), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getNumberOfAttachments(), jsonArray, contactObject.containsNumberOfAttachments());
            }
        });

        m.put(Integer.valueOf(ContactObject.NUMBER_OF_LINKS), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getNumberOfLinks(), jsonArray, contactObject.containsNumberOfLinks());
            }
        });

        m.put(Integer.valueOf(ContactObject.NUMBER_OF_DISTRIBUTIONLIST), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getNumberOfDistributionLists(), jsonArray, contactObject.containsNumberOfDistributionLists());
            }
        });

        m.put(Integer.valueOf(ContactObject.IMAGE_LAST_MODIFIED), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getImageLastModified(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.FILE_AS), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getFileAs(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.IMAGE1_CONTENT_TYPE), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getImageContentType(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.USE_COUNT), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) {
                writeValue(contactObject.getUseCount(), jsonArray);
            }
        });

        m.put(Integer.valueOf(ContactObject.LINKS), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) throws JSONException {
                final JSONArray jsonLinksArray = getLinksAsJSONArray(contactObject);
                if (jsonLinksArray == null) {
                    jsonArray.put(JSONObject.NULL);
                } else {
                    jsonArray.put(jsonLinksArray);
                }
            }
        });

        m.put(Integer.valueOf(ContactObject.DISTRIBUTIONLIST), new ContactFieldWriter() {

            public void write(final ContactObject contactObject, final JSONArray jsonArray) throws JSONException {
                final JSONArray jsonDistributionListArray = getDistributionListAsJSONArray(contactObject);
                if (jsonDistributionListArray == null) {
                    jsonArray.put(JSONObject.NULL);
                } else {
                    jsonArray.put(jsonDistributionListArray);
                }
            }
        });

        WRITER_MAP = m;
    }
}
