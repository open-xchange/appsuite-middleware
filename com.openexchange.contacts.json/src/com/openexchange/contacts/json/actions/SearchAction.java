/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.contacts.json.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.common.collect.ImmutableSet;
import com.openexchange.ajax.fields.ContactFields;
import com.openexchange.ajax.fields.SearchFields;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.contact.provider.composition.IDBasedContactsAccess;
import com.openexchange.contacts.json.ContactRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.search.ContactsSearchObject;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 * {@link SearchAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@RestrictedAction(module = IDBasedContactAction.MODULE_NAME, type = RestrictedAction.Type.READ)
public class SearchAction extends IDBasedContactAction {

    private static final Set<String> OPTIONAL_PARAMETERS = ImmutableSet.of(PARAM_FIELDS, PARAM_ORDER, PARAM_ORDER_BY, PARAM_LEFT_HAND_LIMIT, PARAM_RIGHT_HAND_LIMIT, PARAM_COLLATION);

    private static final String EXCLUDE_FOLDERS_FIELD = "exclude_folders";

    /**
     * Initializes a new {@link SearchAction}.
     *
     * @param serviceLookup
     */
    public SearchAction(ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(IDBasedContactsAccess access, ContactRequest request) throws OXException {
        JSONObject jsonObject = request.getJSONData();
        List<Contact> contacts = access.searchContacts(createContactsSearchObject(jsonObject));
        return new AJAXRequestResult(sortIfNeeded(request, contacts), getLatestTimestamp(contacts), "contact");
    }

    @Override
    protected Set<String> getOptionalParameters() {
        return OPTIONAL_PARAMETERS;
    }


    /**
     * Creates a {@link ContactSearchObject} based on the specifed json
     *
     * @param json The json object
     * @return The {@link ContactSearchObject}
     */
    private static ContactsSearchObject createContactsSearchObject(JSONObject json) throws OXException {
        ContactsSearchObject searchObject = null;
        try {
            searchObject = new ContactsSearchObject();
            {
                Object oFolders = json.opt("folder");
                if (oFolders != null && !JSONObject.NULL.equals(oFolders)) {
                    Set<String> folders;
                    if (oFolders instanceof JSONArray) {
                        folders = new HashSet<String>(Arrays.asList(DataParser.parseJSONStringArray((JSONArray) oFolders)));
                    } else {
                        folders = new HashSet<String>(2);
                        folders.add(oFolders.toString());
                    }
                    searchObject.setFolders(folders);
                }
            }
            {
                Object oExcludedFolders = json.opt(EXCLUDE_FOLDERS_FIELD);
                if (oExcludedFolders != null && !JSONObject.NULL.equals(oExcludedFolders)) {
                    Set<String> excludeFolders;
                    if (oExcludedFolders instanceof JSONArray) {
                        excludeFolders = new HashSet<String>(Arrays.asList(DataParser.parseJSONStringArray((JSONArray) oExcludedFolders)));
                    } else {
                        excludeFolders = new HashSet<String>(2);
                        excludeFolders.add(oExcludedFolders.toString());
                    }
                    searchObject.setExcludeFolders(excludeFolders);
                }
            }
            if (null != searchObject.getFolders() && false == searchObject.getFolders().isEmpty() &&
                null != searchObject.getExcludeFolders() && false == searchObject.getExcludeFolders().isEmpty()) {
                throw AjaxExceptionCodes.UNEXPECTED_ERROR.create("'folder' and 'excludedFolders' are mutual exclusive'");
            }
            if (json.has(SearchFields.PATTERN)) {
                searchObject.setPattern(DataParser.parseString(json, SearchFields.PATTERN));
            }
            if (json.optBoolean("startletter", false)) {
                searchObject.setStartLetter(true);
            }
            if (json.optBoolean("emailAutoComplete", false)) {
                searchObject.setEmailAutoComplete(true);
            }
            if (json.optBoolean("orSearch", false)) {
                searchObject.setOrSearch(true);
            }
            if (json.optBoolean("exactMatch", false)) {
                searchObject.setExactMatch(true);
            }
            searchObject.setSurname(DataParser.parseString(json, ContactFields.LAST_NAME));
            searchObject.setDisplayName(DataParser.parseString(json, ContactFields.DISPLAY_NAME));
            searchObject.setGivenName(DataParser.parseString(json, ContactFields.FIRST_NAME));
            searchObject.setCompany(DataParser.parseString(json, ContactFields.COMPANY));
            searchObject.setEmail1(DataParser.parseString(json, ContactFields.EMAIL1));
            searchObject.setEmail2(DataParser.parseString(json, ContactFields.EMAIL2));
            searchObject.setEmail3(DataParser.parseString(json, ContactFields.EMAIL3));
            searchObject.setCatgories(DataParser.parseString(json, ContactFields.CATEGORIES));
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e);
        }

        return searchObject;
    }

}
