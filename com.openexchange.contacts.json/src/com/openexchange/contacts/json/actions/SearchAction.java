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

package com.openexchange.contacts.json.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.ajax.fields.SearchFields;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.contacts.json.ContactActionFactory;
import com.openexchange.contacts.json.ContactRequest;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.Type;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;


/**
 * {@link SearchAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@Action(method = RequestMethod.PUT, name = "search", description = "Search contact.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "columns", description = "The requested fields."),
    @Parameter(name = "sort", optional=true, description = "The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified. In case of use of column 609 (use count depending order for collected contacts with global address book) the parameter \"order\" ist NOT necessary and will be ignored."),
    @Parameter(name = "order", optional=true, description = "\"asc\" if the response entires should be sorted in the ascending order, \"desc\" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified."),
    @Parameter(name = "collation", description = "(preliminary, since 6.20) - allows you to specify a collation to sort the contacts by. As of 6.20, only supports \"gbk\" and \"gb2312\", not needed for other languages. Parameter sort should be set for this to work."),
    @Parameter(name = "admin", optional=true, type=Type.BOOLEAN, description = "(preliminary, since 7.4.2) - whether to include the contact representing the admin in the result or not. Defaults to \"true\".")
}, requestBody = "An Object as described in Search contacts.",
responseDescription = "An array with contact data. Each array element describes one contact and is itself an array. The elements of each array contain the information specified by the corresponding identifiers in the columns parameter.")
@OAuthAction(ContactActionFactory.OAUTH_READ_SCOPE)
public class SearchAction extends ContactAction {

    /**
     * Initializes a new {@link SearchAction}.
     * @param serviceLookup
     */
    public SearchAction(ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(ContactRequest request) throws OXException {
    	JSONObject jsonObject = request.getJSONData();
        ContactSearchObject contactSearch = createContactSearchObject(jsonObject);
        boolean excludeAdmin = request.isExcludeAdmin();
        int excludedAdminID = excludeAdmin ? request.getSession().getContext().getMailadmin() : -1;
        ContactField[] fields = excludeAdmin ? request.getFields(ContactField.INTERNAL_USERID) : request.getFields();
        List<Contact> contacts = new ArrayList<Contact>();
        Date lastModified = addContacts(contacts, getContactService().searchContacts(
            request.getSession(), contactSearch, fields, request.getSortOptions()), excludedAdminID);
        if (request.sortInternalIfNeeded(contacts)) {
            contacts = request.slice(contacts);
        }
        return new AJAXRequestResult(contacts, lastModified, "contact");
    }

    private static ContactSearchObject createContactSearchObject(JSONObject json) throws OXException {
        ContactSearchObject searchObject = null;
        try {
            searchObject = new ContactSearchObject();
            if (json.has("folder")) {
                if (json.get("folder").getClass().equals(JSONArray.class)) {
                    for (int folder : DataParser.parseJSONIntArray(json, "folder")) {
                        searchObject.addFolder(folder);
                    }
                } else {
                    searchObject.addFolder(DataParser.parseInt(json, "folder"));
                }
            }
            if (json.has(SearchFields.PATTERN)) {
                searchObject.setPattern(DataParser.parseString(json, SearchFields.PATTERN));
            }
            if (json.has("startletter")) {
                searchObject.setStartLetter(DataParser.parseBoolean(json, "startletter"));
            }
            if (json.has("emailAutoComplete") && json.getBoolean("emailAutoComplete")) {
                searchObject.setEmailAutoComplete(true);
            }
            if (json.has("orSearch") && json.getBoolean("orSearch")) {
                searchObject.setOrSearch(true);
            }
            if (json.has("exactMatch") && json.getBoolean("exactMatch")) {
                searchObject.setExactMatch(true);
            }

            searchObject.setSurname(DataParser.parseString(json, ContactFields.LAST_NAME));
            searchObject.setDisplayName(DataParser.parseString(json, ContactFields.DISPLAY_NAME));
            searchObject.setGivenName(DataParser.parseString(json, ContactFields.FIRST_NAME));
            searchObject.setCompany(DataParser.parseString(json, ContactFields.COMPANY));
            searchObject.setEmail1(DataParser.parseString(json, ContactFields.EMAIL1));
            searchObject.setEmail2(DataParser.parseString(json, ContactFields.EMAIL2));
            searchObject.setEmail3(DataParser.parseString(json, ContactFields.EMAIL3));
            searchObject.setDepartment(DataParser.parseString(json, ContactFields.DEPARTMENT));
            searchObject.setStreetBusiness(DataParser.parseString(json, ContactFields.STREET_BUSINESS));
            searchObject.setCityBusiness(DataParser.parseString(json, ContactFields.CITY_BUSINESS));
            searchObject.setDynamicSearchField(DataParser.parseJSONIntArray(json, "dynamicsearchfield"));
            searchObject.setDynamicSearchFieldValue(DataParser.parseJSONStringArray(json, "dynamicsearchfieldvalue"));
            searchObject.setPrivatePostalCodeRange(DataParser.parseJSONStringArray(json, "privatepostalcoderange"));
            searchObject.setBusinessPostalCodeRange(DataParser.parseJSONStringArray(json, "businesspostalcoderange"));
            searchObject.setPrivatePostalCodeRange(DataParser.parseJSONStringArray(json, "privatepostalcoderange"));
            searchObject.setOtherPostalCodeRange(DataParser.parseJSONStringArray(json, "otherpostalcoderange"));
            searchObject.setBirthdayRange(DataParser.parseJSONDateArray(json, "birthdayrange"));
            searchObject.setAnniversaryRange(DataParser.parseJSONDateArray(json, "anniversaryrange"));
            searchObject.setNumberOfEmployeesRange(DataParser.parseJSONStringArray(json, "numberofemployee"));
            searchObject.setSalesVolumeRange(DataParser.parseJSONStringArray(json, "salesvolumerange"));
            searchObject.setCreationDateRange(DataParser.parseJSONDateArray(json, "creationdaterange"));
            searchObject.setLastModifiedRange(DataParser.parseJSONDateArray(json, "lastmodifiedrange"));
            searchObject.setCatgories(DataParser.parseString(json, "categories"));
            searchObject.setSubfolderSearch(DataParser.parseBoolean(json, "subfoldersearch"));
            searchObject.setYomiCompany(DataParser.parseString(json, ContactFields.YOMI_COMPANY));
            searchObject.setYomiFirstname(DataParser.parseString(json, ContactFields.YOMI_FIRST_NAME));
            searchObject.setYomiLastName(DataParser.parseString(json, ContactFields.YOMI_LAST_NAME));
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e);
        }

        return searchObject;
    }

}
