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

package com.openexchange.ajax.writer;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.DistributionListFields;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.datasource.ContactImageDataSource;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.image.ImageLocation;
import com.openexchange.session.Session;
import com.openexchange.tools.TimeZoneUtils;

/**
 * {@link ContactWriter} - The writer for contacts
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
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
        utc = TimeZoneUtils.getTimeZone("utc");
    }

    public void writeArray(final Contact contactobject, final int cols[], final JSONArray jsonArray, final Session session) throws JSONException {
        for (int a = 0; a < cols.length; a++) {
            write(cols[a], contactobject, jsonArray, session);
        }
    }

    public void writeContact(final Contact contact, final JSONObject json, final Session session) throws JSONException {
        writeCommonFields(contact, json, session);
        /* TODO: Refactoring - this can be done with ContactGetter rather easily. sadly not now when 50% of our tests are broken due to the big HttpUnit/HttpClient rewrite
        EXAMPLE:

        Iterator<String> keys = json.keys();
        ContactSetter cs = new ContactSetter();
        //extend: We'll need to nest several specialized setters here, e.g. one for dates
        while(keys.hasNext()){
        	String jsonKey = keys.next();
        	ContactField field = ContactField.getByAjaxName(jsonKey);
        	field.doSwitch(cs, contact, json.get(jsonKey));
        }
        */

        writeParameter(ContactFields.LAST_NAME, contact.getSurName(), json);
        writeParameter(ContactFields.FIRST_NAME, contact.getGivenName(), json);
        writeParameter(ContactFields.ANNIVERSARY, contact.getAnniversary(), json);
        writeParameter(ContactFields.ASSISTANT_NAME, contact.getAssistantName(), json);
        writeParameter(ContactFields.BIRTHDAY, contact.getBirthday(), json);
        writeParameter(ContactFields.BRANCHES, contact.getBranches(), json);
        writeParameter(ContactFields.BUSINESS_CATEGORY, contact.getBusinessCategory(), json);
        writeParameter(ContactFields.CELLULAR_TELEPHONE1, contact.getCellularTelephone1(), json);
        writeParameter(ContactFields.CELLULAR_TELEPHONE2, contact.getCellularTelephone2(), json);
        writeParameter(ContactFields.CITY_HOME, contact.getCityHome(), json);
        writeParameter(ContactFields.CITY_BUSINESS, contact.getCityBusiness(), json);
        writeParameter(ContactFields.CITY_OTHER, contact.getCityOther(), json);
        writeParameter(ContactFields.COMMERCIAL_REGISTER, contact.getCommercialRegister(), json);
        writeParameter(ContactFields.COMPANY, contact.getCompany(), json);
        writeParameter(ContactFields.COUNTRY_HOME, contact.getCountryHome(), json);
        writeParameter(ContactFields.COUNTRY_BUSINESS, contact.getCountryBusiness(), json);
        writeParameter(ContactFields.COUNTRY_OTHER, contact.getCountryOther(), json);
        writeParameter(ContactFields.DEFAULT_ADDRESS, contact.getDefaultAddress(), json, contact.containsDefaultAddress());
        writeParameter(ContactFields.DEPARTMENT, contact.getDepartment(), json);
        writeParameter(ContactFields.DISPLAY_NAME, contact.getDisplayName(), json);
        writeParameter(ContactFields.EMAIL1, contact.getEmail1(), json);
        writeParameter(ContactFields.EMAIL2, contact.getEmail2(), json);
        writeParameter(ContactFields.EMAIL3, contact.getEmail3(), json);
        writeParameter(ContactFields.EMPLOYEE_TYPE, contact.getEmployeeType(), json);
        writeParameter(ContactFields.FAX_BUSINESS, contact.getFaxBusiness(), json);
        writeParameter(ContactFields.FAX_HOME, contact.getFaxHome(), json);
        writeParameter(ContactFields.FAX_OTHER, contact.getFaxOther(), json);
        writeParameter(ContactFields.NUMBER_OF_IMAGES, contact.getNumberOfImages(), json);
        if (contact.containsImage1()) {
            final byte[] imageData = contact.getImage1();
            if (imageData != null && null != session && contact.getObjectID() > 0) {
                try {
                    final ContactImageDataSource imgSource = ContactImageDataSource.getInstance();
                    final ImageLocation imageLocation =
                        new ImageLocation.Builder().folder(Integer.toString(contact.getParentFolderID())).id(
                            Integer.toString(contact.getObjectID())).build();
                    final String imageURL = imgSource.generateUrl(imageLocation, session);
                    writeParameter(ContactFields.IMAGE1_URL, imageURL, json);
                } catch (final OXException e) {
                    org.slf4j.LoggerFactory.getLogger(ContactWriter.class).warn("Contact image URL could not be generated.", e);
                }
            }
        }
        // write image1 at least when setting it to null
        if (contact.containsImage1() && null == contact.getImage1()) {
        	json.put(ContactFields.IMAGE1, JSONObject.NULL);
        }
        // writeParameter(ContactFields.IMAGE1, contactobject.getImage1());
        writeParameter(ContactFields.INFO, contact.getInfo(), json);
        writeParameter(ContactFields.NOTE, contact.getNote(), json);
        writeParameter(ContactFields.INSTANT_MESSENGER1, contact.getInstantMessenger1(), json);
        writeParameter(ContactFields.INSTANT_MESSENGER2, contact.getInstantMessenger2(), json);
        writeParameter(ContactFields.MARITAL_STATUS, contact.getMaritalStatus(), json);
        writeParameter(ContactFields.MANAGER_NAME, contact.getManagerName(), json);
        writeParameter(ContactFields.SECOND_NAME, contact.getMiddleName(), json);
        writeParameter(ContactFields.NICKNAME, contact.getNickname(), json);
        writeParameter(ContactFields.NUMBER_OF_CHILDREN, contact.getNumberOfChildren(), json);
        writeParameter(ContactFields.NUMBER_OF_EMPLOYEE, contact.getNumberOfEmployee(), json);
        writeParameter(ContactFields.POSITION, contact.getPosition(), json);
        writeParameter(ContactFields.POSTAL_CODE_HOME, contact.getPostalCodeHome(), json);
        writeParameter(ContactFields.POSTAL_CODE_BUSINESS, contact.getPostalCodeBusiness(), json);
        writeParameter(ContactFields.POSTAL_CODE_OTHER, contact.getPostalCodeOther(), json);
        writeParameter(ContactFields.PROFESSION, contact.getProfession(), json);
        writeParameter(ContactFields.ROOM_NUMBER, contact.getRoomNumber(), json);
        writeParameter(ContactFields.SALES_VOLUME, contact.getSalesVolume(), json);
        writeParameter(ContactFields.SPOUSE_NAME, contact.getSpouseName(), json);
        writeParameter(ContactFields.STATE_HOME, contact.getStateHome(), json);
        writeParameter(ContactFields.STATE_BUSINESS, contact.getStateBusiness(), json);
        writeParameter(ContactFields.STATE_OTHER, contact.getStateOther(), json);
        writeParameter(ContactFields.STREET_HOME, contact.getStreetHome(), json);
        writeParameter(ContactFields.STREET_BUSINESS, contact.getStreetBusiness(), json);
        writeParameter(ContactFields.STREET_OTHER, contact.getStreetOther(), json);
        writeParameter(ContactFields.SUFFIX, contact.getSuffix(), json);
        writeParameter(ContactFields.TAX_ID, contact.getTaxID(), json);
        writeParameter(ContactFields.TELEPHONE_ASSISTANT, contact.getTelephoneAssistant(), json);
        writeParameter(ContactFields.TELEPHONE_BUSINESS1, contact.getTelephoneBusiness1(), json);
        writeParameter(ContactFields.TELEPHONE_BUSINESS2, contact.getTelephoneBusiness2(), json);
        writeParameter(ContactFields.TELEPHONE_CALLBACK, contact.getTelephoneCallback(), json);
        writeParameter(ContactFields.TELEPHONE_CAR, contact.getTelephoneCar(), json);
        writeParameter(ContactFields.TELEPHONE_COMPANY, contact.getTelephoneCompany(), json);
        writeParameter(ContactFields.TELEPHONE_HOME1, contact.getTelephoneHome1(), json);
        writeParameter(ContactFields.TELEPHONE_HOME2, contact.getTelephoneHome2(), json);
        writeParameter(ContactFields.TELEPHONE_IP, contact.getTelephoneIP(), json);
        writeParameter(ContactFields.TELEPHONE_ISDN, contact.getTelephoneISDN(), json);
        writeParameter(ContactFields.TELEPHONE_OTHER, contact.getTelephoneOther(), json);
        writeParameter(ContactFields.TELEPHONE_PAGER, contact.getTelephonePager(), json);
        writeParameter(ContactFields.TELEPHONE_PRIMARY, contact.getTelephonePrimary(), json);
        writeParameter(ContactFields.TELEPHONE_RADIO, contact.getTelephoneRadio(), json);
        writeParameter(ContactFields.TELEPHONE_TELEX, contact.getTelephoneTelex(), json);
        writeParameter(ContactFields.TELEPHONE_TTYTDD, contact.getTelephoneTTYTTD(), json);
        writeParameter(ContactFields.TITLE, contact.getTitle(), json);
        writeParameter(ContactFields.URL, contact.getURL(), json);
        writeParameter(ContactFields.USERFIELD01, contact.getUserField01(), json);
        writeParameter(ContactFields.USERFIELD02, contact.getUserField02(), json);
        writeParameter(ContactFields.USERFIELD03, contact.getUserField03(), json);
        writeParameter(ContactFields.USERFIELD04, contact.getUserField04(), json);
        writeParameter(ContactFields.USERFIELD05, contact.getUserField05(), json);
        writeParameter(ContactFields.USERFIELD06, contact.getUserField06(), json);
        writeParameter(ContactFields.USERFIELD07, contact.getUserField07(), json);
        writeParameter(ContactFields.USERFIELD08, contact.getUserField08(), json);
        writeParameter(ContactFields.USERFIELD09, contact.getUserField09(), json);
        writeParameter(ContactFields.USERFIELD10, contact.getUserField10(), json);
        writeParameter(ContactFields.USERFIELD11, contact.getUserField11(), json);
        writeParameter(ContactFields.USERFIELD12, contact.getUserField12(), json);
        writeParameter(ContactFields.USERFIELD13, contact.getUserField13(), json);
        writeParameter(ContactFields.USERFIELD14, contact.getUserField14(), json);
        writeParameter(ContactFields.USERFIELD15, contact.getUserField15(), json);
        writeParameter(ContactFields.USERFIELD16, contact.getUserField16(), json);
        writeParameter(ContactFields.USERFIELD17, contact.getUserField17(), json);
        writeParameter(ContactFields.USERFIELD18, contact.getUserField18(), json);
        writeParameter(ContactFields.USERFIELD19, contact.getUserField19(), json);
        writeParameter(ContactFields.USERFIELD20, contact.getUserField20(), json);
        writeParameter(ContactFields.USER_ID, contact.getInternalUserId(), json, contact.containsInternalUserId());
        writeParameter(
            ContactFields.MARK_AS_DISTRIBUTIONLIST,
            contact.getMarkAsDistribtuionlist(),
            json,
            contact.containsMarkAsDistributionlist());
        writeParameter(ContactFields.USE_COUNT, contact.getUseCount(), json, contact.containsInternalUserId());
        writeParameter(ContactFields.FILE_AS, contact.getFileAs(), json, contact.containsFileAs());
        writeParameter(ContactFields.YOMI_FIRST_NAME, contact.getYomiFirstName(), json);
        writeParameter(ContactFields.YOMI_LAST_NAME, contact.getYomiLastName(), json);
        writeParameter(ContactFields.YOMI_COMPANY, contact.getYomiCompany(), json);
        writeParameter(ContactFields.ADDRESS_BUSINESS, contact.getAddressBusiness(), json);
        writeParameter(ContactFields.ADDRESS_HOME, contact.getAddressHome(), json);
        writeParameter(ContactFields.ADDRESS_OTHER, contact.getAddressOther(), json);
        writeParameter(CommonFields.UID, contact.getUid(), json);

        final JSONArray jsonDistributionListArray = getDistributionListAsJSONArray(contact);
        if (jsonDistributionListArray != null) {
            json.put(ContactFields.DISTRIBUTIONLIST, jsonDistributionListArray);
        }
    }

    static final JSONArray getDistributionListAsJSONArray(final Contact contactobject) throws JSONException {
        final DistributionListEntryObject[] distributionlist = contactobject.getDistributionList();

        if (distributionlist == null) {
            return null;
        }
        final JSONArray jsonArray = new JSONArray();

        for (int a = 0; a < distributionlist.length; a++) {
            final JSONObject jsonDListObj = new JSONObject();
            final int emailField = distributionlist[a].getEmailfield();

            if (!(emailField == DistributionListEntryObject.INDEPENDENT)) {
                writeParameter(DataFields.ID, distributionlist[a].getEntryID(), jsonDListObj);
            }

            writeParameter(DistributionListFields.MAIL, distributionlist[a].getEmailaddress(), jsonDListObj);
            writeParameter(ContactFields.DISPLAY_NAME, distributionlist[a].getDisplayname(), jsonDListObj);
            writeParameter(DistributionListFields.MAIL_FIELD, emailField, jsonDListObj);

            jsonArray.put(jsonDListObj);
        }
        return jsonArray;
    }

    public void write(final int field, final Contact contactobject, final JSONArray jsonArray, final Session session) throws JSONException {
        final ContactFieldWriter writer = WRITER_MAP.get(field);
        if (writer != null) {
            writer.write(contactobject, jsonArray, session);
            return;
        }
        /*
         * No appropriate static writer found, write manually
         */
        switch (field) {
        case DataObject.CREATION_DATE:
            writeValue(contactobject.getCreationDate(), timeZone, jsonArray);
            break;
        case DataObject.LAST_MODIFIED:
            writeValue(contactobject.getLastModified(), timeZone, jsonArray);
            break;
        case DataObject.LAST_MODIFIED_UTC:
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
         * @param session TODO
         * @throws JSONException If writing to JSON array fails
         */
        public void write(Contact contactObject, JSONArray jsonArray, Session session) throws JSONException;
    }

    /*-
     * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * ++++++++++++++++++++ INITIALIZATION OF FIELD WRITERS ++++++++++++++++++++
     * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     */

    private static final TIntObjectMap<ContactFieldWriter> WRITER_MAP;

    static {
        final TIntObjectMap<ContactFieldWriter> m = new TIntObjectHashMap<ContactFieldWriter>(128);

        m.put(DataObject.OBJECT_ID, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getObjectID(), jsonArray, contactObject.containsObjectID());
            }
        });

        m.put(DataObject.CREATED_BY, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getCreatedBy(), jsonArray, contactObject.containsCreatedBy());
            }
        });

        m.put(DataObject.MODIFIED_BY, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getModifiedBy(), jsonArray, contactObject.containsModifiedBy());
            }
        });

        m.put(FolderChildObject.FOLDER_ID, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getParentFolderID(), jsonArray, contactObject.containsParentFolderID());
            }
        });

        m.put(CommonObject.PRIVATE_FLAG, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getPrivateFlag(), jsonArray, contactObject.containsPrivateFlag());
            }
        });

        m.put(Contact.SUR_NAME, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getSurName(), jsonArray);
            }
        });

        m.put(Contact.YOMI_FIRST_NAME, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getYomiFirstName(), jsonArray);
            }
        });

        m.put(Contact.ADDRESS_BUSINESS, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getAddressBusiness(), jsonArray);
            }
        });

        m.put(Contact.ADDRESS_HOME, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getAddressHome(), jsonArray);
            }
        });

        m.put(Contact.ADDRESS_OTHER, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getAddressOther(), jsonArray);
            }
        });

        m.put(CommonObject.UID, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getUid(), jsonArray);
            }
        });

        m.put(Contact.GIVEN_NAME, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getGivenName(), jsonArray);
            }
        });

        m.put(Contact.YOMI_LAST_NAME, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getYomiLastName(), jsonArray);
            }
        });

        m.put(Contact.ANNIVERSARY, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getAnniversary(), jsonArray);
            }
        });

        m.put(Contact.ASSISTANT_NAME, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getAssistantName(), jsonArray);
            }
        });

        m.put(Contact.BIRTHDAY, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getBirthday(), jsonArray);
            }
        });

        m.put(Contact.BRANCHES, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getBranches(), jsonArray);
            }
        });

        m.put(Contact.BUSINESS_CATEGORY, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getBusinessCategory(), jsonArray);
            }
        });

        m.put(CommonObject.CATEGORIES, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getCategories(), jsonArray);
            }
        });

        m.put(Contact.CELLULAR_TELEPHONE1, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getCellularTelephone1(), jsonArray);
            }
        });

        m.put(Contact.CELLULAR_TELEPHONE2, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getCellularTelephone2(), jsonArray);
            }
        });

        m.put(Contact.CITY_HOME, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getCityHome(), jsonArray);
            }
        });

        m.put(Contact.CITY_BUSINESS, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getCityBusiness(), jsonArray);
            }
        });

        m.put(Contact.CITY_OTHER, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getCityOther(), jsonArray);
            }
        });

        m.put(CommonObject.COLOR_LABEL, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getLabel(), jsonArray, contactObject.containsLabel());
            }
        });

        m.put(Contact.COMMERCIAL_REGISTER, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getCommercialRegister(), jsonArray);
            }
        });

        m.put(Contact.COMPANY, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getCompany(), jsonArray);
            }
        });

        m.put(Contact.YOMI_COMPANY, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getYomiCompany(), jsonArray);
            }
        });

        m.put(Contact.COUNTRY_HOME, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getCountryHome(), jsonArray);
            }
        });

        m.put(Contact.COUNTRY_BUSINESS, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getCountryBusiness(), jsonArray);
            }
        });

        m.put(Contact.COUNTRY_OTHER, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getCountryOther(), jsonArray);
            }
        });

        m.put(Contact.DEFAULT_ADDRESS, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getDefaultAddress(), jsonArray, contactObject.containsDefaultAddress());
            }
        });

        m.put(Contact.DEPARTMENT, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getDepartment(), jsonArray);
            }
        });

        m.put(Contact.DISPLAY_NAME, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getDisplayName(), jsonArray);
            }
        });

        m.put(Contact.MARK_AS_DISTRIBUTIONLIST, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getMarkAsDistribtuionlist(), jsonArray, contactObject.containsMarkAsDistributionlist());
            }
        });

        m.put(Contact.EMAIL1, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getEmail1(), jsonArray);
            }
        });

        m.put(Contact.EMAIL2, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getEmail2(), jsonArray);
            }
        });

        m.put(Contact.EMAIL3, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getEmail3(), jsonArray);
            }
        });

        m.put(Contact.EMPLOYEE_TYPE, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getEmployeeType(), jsonArray);
            }
        });

        m.put(Contact.FAX_BUSINESS, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getFaxBusiness(), jsonArray);
            }
        });

        m.put(Contact.FAX_HOME, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getFaxHome(), jsonArray);
            }
        });

        m.put(Contact.FAX_OTHER, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getFaxOther(), jsonArray);
            }
        });

        m.put(Contact.IMAGE1, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                final byte[] imageData = contactObject.getImage1();
                if (imageData == null) {
                    writeValueNull(jsonArray);
                } else {
                    writeValue(new String(imageData), jsonArray);
                }
            }
        });

        m.put(Contact.IMAGE1_URL, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                if (contactObject.containsContextId()) {
                    final byte[] imageData2 = contactObject.getImage1();
                    if (imageData2 == null) {
                        writeValueNull(jsonArray);
                    } else {
                        try {
                            final ContactImageDataSource imgSource = ContactImageDataSource.getInstance();
                            final ImageLocation imageLocation =
                                new ImageLocation.Builder().folder(Integer.toString(contactObject.getParentFolderID())).id(
                                    Integer.toString(contactObject.getObjectID())).build();
                            final String imageURL = imgSource.generateUrl(imageLocation, session);
                            writeValue(imageURL, jsonArray);
                        } catch (final OXException e) {
                            org.slf4j.LoggerFactory.getLogger(ContactWriter.class).warn("Contact image URL could not be generated.", e);
                            writeValueNull(jsonArray);
                        }
                    }
                } else {
                    writeValueNull(jsonArray);
                }
            }
        });
        m.put(Contact.NUMBER_OF_IMAGES, new ContactFieldWriter() {
            @Override
            public void write(final Contact contact, final JSONArray json, final Session session) {
                writeValue(contact.getNumberOfImages(), json);
            }
        });
        m.put(Contact.INFO, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getInfo(), jsonArray);
            }
        });

        m.put(Contact.INSTANT_MESSENGER1, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getInstantMessenger1(), jsonArray);
            }
        });

        m.put(Contact.INSTANT_MESSENGER2, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getInstantMessenger2(), jsonArray);
            }
        });

        m.put(Contact.INTERNAL_USERID, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getInternalUserId(), jsonArray, contactObject.containsInternalUserId());
            }
        });

        m.put(Contact.MANAGER_NAME, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getManagerName(), jsonArray);
            }
        });

        m.put(Contact.MARITAL_STATUS, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getMaritalStatus(), jsonArray);
            }
        });

        m.put(Contact.MIDDLE_NAME, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getMiddleName(), jsonArray);
            }
        });

        m.put(Contact.NICKNAME, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getNickname(), jsonArray);
            }
        });

        m.put(Contact.NOTE, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getNote(), jsonArray);
            }
        });

        m.put(Contact.NUMBER_OF_CHILDREN, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getNumberOfChildren(), jsonArray);
            }
        });

        m.put(Contact.NUMBER_OF_EMPLOYEE, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getNumberOfEmployee(), jsonArray);
            }
        });

        m.put(Contact.POSITION, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getPosition(), jsonArray);
            }
        });

        m.put(Contact.POSTAL_CODE_HOME, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getPostalCodeHome(), jsonArray);
            }
        });

        m.put(Contact.POSTAL_CODE_BUSINESS, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getPostalCodeBusiness(), jsonArray);
            }
        });

        m.put(Contact.POSTAL_CODE_OTHER, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getPostalCodeOther(), jsonArray);
            }
        });

        m.put(Contact.PROFESSION, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getProfession(), jsonArray);
            }
        });

        m.put(Contact.ROOM_NUMBER, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getRoomNumber(), jsonArray);
            }
        });

        m.put(Contact.SALES_VOLUME, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getSalesVolume(), jsonArray);
            }
        });

        m.put(Contact.SPOUSE_NAME, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getSpouseName(), jsonArray);
            }
        });

        m.put(Contact.STATE_HOME, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getStateHome(), jsonArray);
            }
        });

        m.put(Contact.STATE_BUSINESS, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getStateBusiness(), jsonArray);
            }
        });

        m.put(Contact.STATE_OTHER, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getStateOther(), jsonArray);
            }
        });

        m.put(Contact.STREET_HOME, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getStreetHome(), jsonArray);
            }
        });

        m.put(Contact.STREET_BUSINESS, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getStreetBusiness(), jsonArray);
            }
        });

        m.put(Contact.STREET_OTHER, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getStreetOther(), jsonArray);
            }
        });

        m.put(Contact.SUFFIX, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getSuffix(), jsonArray);
            }
        });

        m.put(Contact.TAX_ID, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getTaxID(), jsonArray);
            }
        });

        m.put(Contact.TELEPHONE_ASSISTANT, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getTelephoneAssistant(), jsonArray);
            }
        });

        m.put(Contact.TELEPHONE_BUSINESS1, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getTelephoneBusiness1(), jsonArray);
            }
        });

        m.put(Contact.TELEPHONE_BUSINESS2, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getTelephoneBusiness2(), jsonArray);
            }
        });

        m.put(Contact.TELEPHONE_CALLBACK, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getTelephoneCallback(), jsonArray);
            }
        });

        m.put(Contact.TELEPHONE_CAR, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getTelephoneCar(), jsonArray);
            }
        });

        m.put(Contact.TELEPHONE_COMPANY, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getTelephoneCompany(), jsonArray);
            }
        });

        m.put(Contact.TELEPHONE_HOME1, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getTelephoneHome1(), jsonArray);
            }
        });

        m.put(Contact.TELEPHONE_HOME2, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getTelephoneHome2(), jsonArray);
            }
        });

        m.put(Contact.TELEPHONE_IP, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getTelephoneIP(), jsonArray);
            }
        });

        m.put(Contact.TELEPHONE_ISDN, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getTelephoneISDN(), jsonArray);
            }
        });

        m.put(Contact.TELEPHONE_OTHER, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getTelephoneOther(), jsonArray);
            }
        });

        m.put(Contact.TELEPHONE_PAGER, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getTelephonePager(), jsonArray);
            }
        });

        m.put(Contact.TELEPHONE_PRIMARY, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getTelephonePrimary(), jsonArray);
            }
        });

        m.put(Contact.TELEPHONE_RADIO, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getTelephoneRadio(), jsonArray);
            }
        });

        m.put(Contact.TELEPHONE_TELEX, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getTelephoneTelex(), jsonArray);
            }
        });

        m.put(Contact.TELEPHONE_TTYTDD, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getTelephoneTTYTTD(), jsonArray);
            }
        });

        m.put(Contact.TITLE, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getTitle(), jsonArray);
            }
        });

        m.put(Contact.URL, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getURL(), jsonArray);
            }
        });

        m.put(Contact.USERFIELD01, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getUserField01(), jsonArray);
            }
        });

        m.put(Contact.USERFIELD02, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getUserField02(), jsonArray);
            }
        });

        m.put(Contact.USERFIELD03, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getUserField03(), jsonArray);
            }
        });

        m.put(Contact.USERFIELD04, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getUserField04(), jsonArray);
            }
        });

        m.put(Contact.USERFIELD05, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getUserField05(), jsonArray);
            }
        });

        m.put(Contact.USERFIELD06, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getUserField06(), jsonArray);
            }
        });

        m.put(Contact.USERFIELD07, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getUserField07(), jsonArray);
            }
        });

        m.put(Contact.USERFIELD08, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getUserField08(), jsonArray);
            }
        });

        m.put(Contact.USERFIELD09, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getUserField09(), jsonArray);
            }
        });

        m.put(Contact.USERFIELD10, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getUserField10(), jsonArray);
            }
        });

        m.put(Contact.USERFIELD11, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getUserField11(), jsonArray);
            }
        });

        m.put(Contact.USERFIELD12, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getUserField12(), jsonArray);
            }
        });

        m.put(Contact.USERFIELD13, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getUserField13(), jsonArray);
            }
        });

        m.put(Contact.USERFIELD14, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getUserField14(), jsonArray);
            }
        });

        m.put(Contact.USERFIELD15, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getUserField15(), jsonArray);
            }
        });

        m.put(Contact.USERFIELD16, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getUserField16(), jsonArray);
            }
        });

        m.put(Contact.USERFIELD17, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getUserField17(), jsonArray);
            }
        });

        m.put(Contact.USERFIELD18, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getUserField18(), jsonArray);
            }
        });

        m.put(Contact.USERFIELD19, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getUserField19(), jsonArray);
            }
        });

        m.put(Contact.USERFIELD20, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getUserField20(), jsonArray);
            }
        });

        m.put(CommonObject.NUMBER_OF_ATTACHMENTS, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getNumberOfAttachments(), jsonArray, contactObject.containsNumberOfAttachments());
            }
        });
        m.put(CommonObject.LAST_MODIFIED_OF_NEWEST_ATTACHMENT, new ContactFieldWriter() {
            @Override
            public void write(final Contact contact, final JSONArray json, final Session session) {
                writeValue(contact.getLastModifiedOfNewestAttachment(), json, contact.containsLastModifiedOfNewestAttachment());
            }
        });
        m.put(Contact.NUMBER_OF_DISTRIBUTIONLIST, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getNumberOfDistributionLists(), jsonArray, contactObject.containsNumberOfDistributionLists());
            }
        });

        m.put(Contact.IMAGE_LAST_MODIFIED, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getImageLastModified(), jsonArray);
            }
        });
        m.put(Contact.FILE_AS, new ContactFieldWriter() {
            @Override
            public void write(final Contact contact, final JSONArray json, final Session session) {
                writeValue(contact.getFileAs(), json, contact.containsFileAs());
            }
        });
        m.put(Contact.IMAGE1_CONTENT_TYPE, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getImageContentType(), jsonArray);
            }
        });

        m.put(Contact.USE_COUNT, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getUseCount(), jsonArray);
            }
        });

        m.put(Contact.DISTRIBUTIONLIST, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) throws JSONException {
                final JSONArray jsonDistributionListArray = getDistributionListAsJSONArray(contactObject);
                if (jsonDistributionListArray == null) {
                    jsonArray.put(JSONObject.NULL);
                } else {
                    jsonArray.put(jsonDistributionListArray);
                }
            }
        });

        m.put(Contact.YOMI_FIRST_NAME, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getYomiFirstName(), jsonArray);
            }
        });

        m.put(Contact.YOMI_LAST_NAME, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getYomiLastName(), jsonArray);
            }
        });

        m.put(Contact.YOMI_COMPANY, new ContactFieldWriter() {

            @Override
            public void write(final Contact contactObject, final JSONArray jsonArray, final Session session) {
                writeValue(contactObject.getYomiCompany(), jsonArray);
            }
        });

        WRITER_MAP = m;
    }
}
