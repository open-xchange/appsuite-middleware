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

package com.openexchange.ajax.parser;

import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.CommonFields;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.DistributionListFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 * Parses JSON to contact objects.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class ContactParser extends CommonParser {

    public ContactParser() {
        super();
    }

    public ContactParser(final boolean parseAll, final TimeZone timeZone) {
        super(parseAll, timeZone);
    }

    public void parse(final Contact contactobject, final JSONObject jsonobject) throws OXException, OXException {
        try {
            parseElementContact(contactobject, jsonobject);
        } catch (final JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e);
        }
    }

    protected void parseElementContact(final Contact contactobject, final JSONObject jsonobject) throws JSONException, OXException, OXException {
        for (int i = 0; i < mapping.length; i++) {
            if (mapping[i].jsonObjectContains(jsonobject)) {
                mapping[i].setObject(contactobject, jsonobject);
            }
        }
        if (jsonobject.has(ContactFields.DISTRIBUTIONLIST)) {
            parseDistributionList(contactobject, jsonobject);
        }
        parseElementCommon(contactobject, jsonobject);
    }

    protected void parseDistributionList(final Contact oxobject, final JSONObject jsonobject) throws JSONException, OXException, OXException {
        final JSONArray jdistributionlist = jsonobject.getJSONArray(ContactFields.DISTRIBUTIONLIST);
        final DistributionListEntryObject[] distributionlist = new DistributionListEntryObject[jdistributionlist.length()];
        for (int a = 0; a < jdistributionlist.length(); a++) {
            final JSONObject entry = jdistributionlist.getJSONObject(a);
            distributionlist[a] = new DistributionListEntryObject();
            if (entry.has(DataFields.ID)) {
                distributionlist[a].setEntryID(parseInt(entry, DataFields.ID));
            }

            if (entry.has(FolderChildFields.FOLDER_ID)) {
                distributionlist[a].setFolderID(parseInt(entry, FolderChildFields.FOLDER_ID));
            }

            if (entry.has(ContactFields.FIRST_NAME)) {
                distributionlist[a].setFirstname(parseString(entry, ContactFields.FIRST_NAME));
            }

            if (entry.has(ContactFields.LAST_NAME)) {
                distributionlist[a].setLastname(parseString(entry, ContactFields.LAST_NAME));
            }

            distributionlist[a].setDisplayname(parseString(entry, ContactFields.DISPLAY_NAME));
            distributionlist[a].setEmailaddress(parseString(entry, DistributionListFields.MAIL));
            distributionlist[a].setEmailfield(parseInt(entry, DistributionListFields.MAIL_FIELD));
        }
        oxobject.setDistributionList(distributionlist);
    }

    private interface JSONAttributeMapper {
        boolean jsonObjectContains(JSONObject jsonobject);
        void setObject(Contact contactobject, JSONObject jsonobject) throws JSONException, OXException;
    }

    private final JSONAttributeMapper[] mapping = new JSONAttributeMapper[] {
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.LAST_NAME);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setSurName(parseString(jsonobject, ContactFields.LAST_NAME));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.TITLE);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setTitle(parseString(jsonobject, ContactFields.TITLE));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.FIRST_NAME);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setGivenName (parseString(jsonobject, ContactFields.FIRST_NAME));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.MARITAL_STATUS);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setMaritalStatus(parseString(jsonobject, ContactFields.MARITAL_STATUS));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.ANNIVERSARY);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setAnniversary(parseDate(jsonobject, ContactFields.ANNIVERSARY));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.ASSISTANT_NAME);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setAssistantName(parseString(jsonobject, ContactFields.ASSISTANT_NAME));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.BIRTHDAY);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setBirthday(parseDate(jsonobject, ContactFields.BIRTHDAY));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.BRANCHES);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setBranches(parseString(jsonobject, ContactFields.BRANCHES));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.BUSINESS_CATEGORY);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setBusinessCategory(parseString(jsonobject, ContactFields.BUSINESS_CATEGORY));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.CELLULAR_TELEPHONE1);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setCellularTelephone1(parseString(jsonobject, ContactFields.CELLULAR_TELEPHONE1));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.CELLULAR_TELEPHONE2);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setCellularTelephone2(parseString(jsonobject, ContactFields.CELLULAR_TELEPHONE2));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.CITY_HOME);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setCityHome(parseString(jsonobject, ContactFields.CITY_HOME));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.CITY_BUSINESS);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setCityBusiness(parseString(jsonobject, ContactFields.CITY_BUSINESS));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.CITY_OTHER);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setCityOther(parseString(jsonobject, ContactFields.CITY_OTHER));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.COMMERCIAL_REGISTER);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setCommercialRegister(parseString(jsonobject, ContactFields.COMMERCIAL_REGISTER));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.COMPANY);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setCompany(parseString(jsonobject, ContactFields.COMPANY));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.COUNTRY_HOME);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setCountryHome(parseString(jsonobject, ContactFields.COUNTRY_HOME));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.COUNTRY_BUSINESS);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setCountryBusiness(parseString(jsonobject, ContactFields.COUNTRY_BUSINESS));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.COUNTRY_OTHER);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setCountryOther(parseString(jsonobject, ContactFields.COUNTRY_OTHER));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.DEFAULT_ADDRESS);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException, OXException {
                contactobject.setDefaultAddress(parseInt(jsonobject, ContactFields.DEFAULT_ADDRESS));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.DEPARTMENT);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setDepartment(parseString(jsonobject, ContactFields.DEPARTMENT));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.DISPLAY_NAME);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setDisplayName(parseString(jsonobject, ContactFields.DISPLAY_NAME));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.MARK_AS_DISTRIBUTIONLIST);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException {
                contactobject.setMarkAsDistributionlist(parseBoolean(jsonobject, ContactFields.MARK_AS_DISTRIBUTIONLIST));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.EMAIL1);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setEmail1(parseString(jsonobject, ContactFields.EMAIL1));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.EMAIL2);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setEmail2(parseString(jsonobject, ContactFields.EMAIL2));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.EMAIL3);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setEmail3(parseString(jsonobject, ContactFields.EMAIL3));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.EMPLOYEE_TYPE);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setEmployeeType(parseString(jsonobject, ContactFields.EMPLOYEE_TYPE));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.FAX_BUSINESS);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setFaxBusiness(parseString(jsonobject, ContactFields.FAX_BUSINESS));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.FAX_HOME);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setFaxHome(parseString(jsonobject, ContactFields.FAX_HOME));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.FAX_OTHER);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setFaxOther(parseString(jsonobject, ContactFields.FAX_OTHER));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.IMAGE1);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                final String image = parseString(jsonobject, ContactFields.IMAGE1);
                if (image != null) {
                    contactobject.setImage1(image.getBytes());
                } else {
                    contactobject.setImage1(null);
                }
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.NOTE);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setNote(parseString(jsonobject, ContactFields.NOTE));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.INFO);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setInfo(parseString(jsonobject, ContactFields.INFO));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.INSTANT_MESSENGER1);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setInstantMessenger1(parseString(jsonobject, ContactFields.INSTANT_MESSENGER1));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.INSTANT_MESSENGER2);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setInstantMessenger2(parseString(jsonobject, ContactFields.INSTANT_MESSENGER2));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.MANAGER_NAME);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setManagerName(parseString(jsonobject, ContactFields.MANAGER_NAME));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.SECOND_NAME);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setMiddleName(parseString(jsonobject, ContactFields.SECOND_NAME));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.NICKNAME);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setNickname(parseString(jsonobject, ContactFields.NICKNAME));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.NUMBER_OF_CHILDREN);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setNumberOfChildren(parseString(jsonobject, ContactFields.NUMBER_OF_CHILDREN));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.NUMBER_OF_EMPLOYEE);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setNumberOfEmployee(parseString(jsonobject, ContactFields.NUMBER_OF_EMPLOYEE));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.POSITION);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setPosition(parseString(jsonobject, ContactFields.POSITION));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.POSTAL_CODE_HOME);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setPostalCodeHome(parseString(jsonobject, ContactFields.POSTAL_CODE_HOME));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.POSTAL_CODE_BUSINESS);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setPostalCodeBusiness(parseString(jsonobject, ContactFields.POSTAL_CODE_BUSINESS));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.POSTAL_CODE_OTHER);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setPostalCodeOther(parseString(jsonobject, ContactFields.POSTAL_CODE_OTHER));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.PROFESSION);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setProfession(parseString(jsonobject, ContactFields.PROFESSION));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.ROOM_NUMBER);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setRoomNumber(parseString(jsonobject, ContactFields.ROOM_NUMBER));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.SALES_VOLUME);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setSalesVolume(parseString(jsonobject, ContactFields.SALES_VOLUME));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.SPOUSE_NAME);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setSpouseName(parseString(jsonobject, ContactFields.SPOUSE_NAME));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.STATE_HOME);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setStateHome(parseString(jsonobject, ContactFields.STATE_HOME));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.STATE_BUSINESS);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setStateBusiness(parseString(jsonobject, ContactFields.STATE_BUSINESS));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.STATE_OTHER);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setStateOther(parseString(jsonobject, ContactFields.STATE_OTHER));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.STREET_HOME);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setStreetHome(parseString(jsonobject, ContactFields.STREET_HOME));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.STREET_BUSINESS);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setStreetBusiness(parseString(jsonobject, ContactFields.STREET_BUSINESS));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.STREET_OTHER);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setStreetOther(parseString(jsonobject, ContactFields.STREET_OTHER));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.SUFFIX);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setSuffix(parseString(jsonobject, ContactFields.SUFFIX));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.TAX_ID);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setTaxID(parseString(jsonobject, ContactFields.TAX_ID));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.TELEPHONE_ASSISTANT);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setTelephoneAssistant(parseString(jsonobject, ContactFields.TELEPHONE_ASSISTANT));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.TELEPHONE_BUSINESS1);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setTelephoneBusiness1(parseString(jsonobject, ContactFields.TELEPHONE_BUSINESS1));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.TELEPHONE_BUSINESS2);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setTelephoneBusiness2(parseString(jsonobject, ContactFields.TELEPHONE_BUSINESS2));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.TELEPHONE_CALLBACK);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setTelephoneCallback(parseString(jsonobject, ContactFields.TELEPHONE_CALLBACK));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.TELEPHONE_CAR);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setTelephoneCar(parseString(jsonobject, ContactFields.TELEPHONE_CAR));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.TELEPHONE_COMPANY);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setTelephoneCompany(parseString(jsonobject, ContactFields.TELEPHONE_COMPANY));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.TELEPHONE_HOME1);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setTelephoneHome1(parseString(jsonobject, ContactFields.TELEPHONE_HOME1));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.TELEPHONE_HOME2);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setTelephoneHome2(parseString(jsonobject, ContactFields.TELEPHONE_HOME2));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.TELEPHONE_IP);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setTelephoneIP(parseString(jsonobject, ContactFields.TELEPHONE_IP));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.TELEPHONE_ISDN);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setTelephoneISDN(parseString(jsonobject, ContactFields.TELEPHONE_ISDN));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.TELEPHONE_OTHER);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setTelephoneOther(parseString(jsonobject, ContactFields.TELEPHONE_OTHER));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.TELEPHONE_PAGER);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setTelephonePager(parseString(jsonobject, ContactFields.TELEPHONE_PAGER));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.TELEPHONE_PRIMARY);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setTelephonePrimary(parseString(jsonobject, ContactFields.TELEPHONE_PRIMARY));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.TELEPHONE_RADIO);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setTelephoneRadio(parseString(jsonobject, ContactFields.TELEPHONE_RADIO));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.TELEPHONE_TELEX);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setTelephoneTelex(parseString(jsonobject, ContactFields.TELEPHONE_TELEX));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.TELEPHONE_TTYTDD);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setTelephoneTTYTTD(parseString(jsonobject, ContactFields.TELEPHONE_TTYTDD));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.URL);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setURL(parseString(jsonobject, ContactFields.URL));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.USE_COUNT);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException, OXException {
                contactobject.setUseCount(parseInt(jsonobject, ContactFields.USE_COUNT));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.USERFIELD01);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setUserField01(parseString(jsonobject, ContactFields.USERFIELD01));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.USERFIELD02);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setUserField02(parseString(jsonobject, ContactFields.USERFIELD02));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.USERFIELD03);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setUserField03(parseString(jsonobject, ContactFields.USERFIELD03));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.USERFIELD04);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setUserField04(parseString(jsonobject, ContactFields.USERFIELD04));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.USERFIELD05);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setUserField05(parseString(jsonobject, ContactFields.USERFIELD05));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.USERFIELD06);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setUserField06(parseString(jsonobject, ContactFields.USERFIELD06));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.USERFIELD07);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setUserField07(parseString(jsonobject, ContactFields.USERFIELD07));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.USERFIELD08);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setUserField08(parseString(jsonobject, ContactFields.USERFIELD08));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.USERFIELD09);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setUserField09(parseString(jsonobject, ContactFields.USERFIELD09));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.USERFIELD10);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setUserField10(parseString(jsonobject, ContactFields.USERFIELD10));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.USERFIELD11);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setUserField11(parseString(jsonobject, ContactFields.USERFIELD11));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.USERFIELD12);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setUserField12(parseString(jsonobject, ContactFields.USERFIELD12));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.USERFIELD13);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setUserField13(parseString(jsonobject, ContactFields.USERFIELD13));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.USERFIELD14);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setUserField14(parseString(jsonobject, ContactFields.USERFIELD14));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.USERFIELD15);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setUserField15(parseString(jsonobject, ContactFields.USERFIELD15));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.USERFIELD16);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setUserField16(parseString(jsonobject, ContactFields.USERFIELD16));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.USERFIELD17);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setUserField17(parseString(jsonobject, ContactFields.USERFIELD17));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.USERFIELD18);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setUserField18(parseString(jsonobject, ContactFields.USERFIELD18));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.USERFIELD19);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setUserField19(parseString(jsonobject, ContactFields.USERFIELD19));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.USERFIELD20);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setUserField20(parseString(jsonobject, ContactFields.USERFIELD20));
            }
        },
        new JSONAttributeMapper() {
        	@Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.FILE_AS);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setFileAs(parseString(jsonobject, ContactFields.FILE_AS));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.YOMI_FIRST_NAME);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setYomiFirstName(parseString(jsonobject, ContactFields.YOMI_FIRST_NAME));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.YOMI_LAST_NAME);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setYomiLastName(parseString(jsonobject, ContactFields.YOMI_LAST_NAME));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.YOMI_COMPANY);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setYomiCompany(parseString(jsonobject, ContactFields.YOMI_COMPANY));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.ADDRESS_HOME);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setAddressHome(parseString(jsonobject, ContactFields.ADDRESS_HOME));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.ADDRESS_BUSINESS);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setAddressBusiness(parseString(jsonobject, ContactFields.ADDRESS_BUSINESS));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.ADDRESS_OTHER);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setAddressOther(parseString(jsonobject, ContactFields.ADDRESS_OTHER));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(CommonFields.UID);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) {
                contactobject.setUid(parseString(jsonobject, CommonFields.UID));
            }
        },
        new JSONAttributeMapper() {
            @Override
            public boolean jsonObjectContains(final JSONObject jsonobject) {
                return jsonobject.has(ContactFields.USER_ID);
            }
            @Override
            public void setObject(final Contact contactobject, final JSONObject jsonobject) throws JSONException, OXException {
                contactobject.setInternalUserId(parseInt(jsonobject, ContactFields.USER_ID));
            }
        }

    };
}
