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
            if (json.has("folder")) {
                Set<String> folders = new HashSet<String>();
                if (json.get("folder").getClass().equals(JSONArray.class)) {
                    folders.addAll(Arrays.asList(DataParser.parseJSONStringArray(json, "folder")));
                } else {
                    folders.add(DataParser.parseString(json, "folder"));
                }
                searchObject.setFolders(folders);
            }
            if (json.has(EXCLUDE_FOLDERS_FIELD)) {
                Set<String> excludeFolders = new HashSet<String>();
                if (json.get(EXCLUDE_FOLDERS_FIELD).getClass().equals(JSONArray.class)) {
                    excludeFolders.addAll(Arrays.asList(DataParser.parseJSONStringArray(json, EXCLUDE_FOLDERS_FIELD)));
                } else {
                    excludeFolders.add(DataParser.parseString(json, EXCLUDE_FOLDERS_FIELD));
                }
                searchObject.setExcludeFolders(excludeFolders);
            }
            if (null != searchObject.getFolders() && false == searchObject.getFolders().isEmpty() &&
                null != searchObject.getExcludeFolders() && false == searchObject.getExcludeFolders().isEmpty()) {
                throw AjaxExceptionCodes.UNEXPECTED_ERROR.create("'folder' and 'excludedFolders' are mutual exclusive'");
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
            searchObject.setCatgories(DataParser.parseString(json, ContactFields.CATEGORIES));
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_READ_ERROR.create(e);
        }

        return searchObject;
    }

}
