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

import static com.openexchange.ajax.AJAXServlet.PARAMETER_FILTER;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.common.collect.ImmutableSet;
import com.openexchange.ajax.fields.SearchTermFields;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.parser.SearchTermParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contact.provider.composition.IDBasedContactsAccess;
import com.openexchange.contacts.json.ContactRequest;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Strings;
import com.openexchange.search.Operand;
import com.openexchange.search.SearchExceptionMessages;
import com.openexchange.search.SearchTerm;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 * {@link AdvancedSearchAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
@RestrictedAction(module = IDBasedContactAction.MODULE_NAME, type = RestrictedAction.Type.READ)
public class AdvancedSearchAction extends IDBasedContactAction {

    private static final Set<String> OPTIONAL_PARAMETERS = ImmutableSet.of(PARAM_FIELDS, PARAM_ORDER, PARAM_ORDER_BY, PARAM_COLLATION, PARAM_LEFT_HAND_LIMIT, PARAM_RIGHT_HAND_LIMIT);

    private static final SearchTermParser PARSER = new ContactSearchTermParser();

    /**
     * Initializes a new {@link AdvancedSearchAction}.
     *
     * @param serviceLookup
     */
    public AdvancedSearchAction(ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected Set<String> getOptionalParameters() {
        return OPTIONAL_PARAMETERS;
    }

    @Override
    protected AJAXRequestResult perform(IDBasedContactsAccess access, ContactRequest request) throws OXException {
        /*
         * parse search term and folders
         */
        JSONObject data = request.getJSONData();
        SearchTerm<?> searchTerm = parseSearchTerm(data);
        List<String> folderIds = parseFolderIds(data);
        /*
         * perform search & prepare result
         */
        List<Contact> contacts = access.searchContacts(folderIds, searchTerm);
        return new AJAXRequestResult(sortIfNeeded(request, contacts), getLatestTimestamp(contacts), "contact");
    }

    private static List<String> parseFolderIds(JSONObject data) throws OXException {
        try {
            String[] folders = DataParser.parseJSONStringArray(data, "folders");
            return null != folders ? Arrays.asList(folders) : null;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
        }
    }

    private static SearchTerm<?> parseSearchTerm(JSONObject data) throws OXException {
        JSONArray filterArray = data.optJSONArray(PARAMETER_FILTER);
        if (null == filterArray) {
            throw OXJSONExceptionCodes.MISSING_FIELD.create(PARAMETER_FILTER);
        }
        return PARSER.parseSearchTerm(filterArray);
    }

    /**
     * {@link ContactSearchTermParser}
     *
     * Custom {@link SearchTermParser} producing {@link ContactFieldOperand}s from ajax names and checking their validity.
     */
    private static class ContactSearchTermParser extends SearchTermParser {

        ContactSearchTermParser() {
            super();
        }

        @Override
        protected Operand<?> parseOperand(JSONObject operand) throws OXException {
            String fieldName = operand.optString(SearchTermFields.FIELD);
            if (Strings.isEmpty(fieldName)) {
                throw SearchExceptionMessages.PARSING_FAILED_MISSING_FIELD.create(SearchTermFields.FIELD);
            }
            ContactField field = ContactMapper.getInstance().getMappedField(fieldName);
            if (null == field || false == isSupported(field)) {
                throw SearchExceptionMessages.PARSING_FAILED_UNSUPPORTED_OPERAND.create(fieldName);
            }
            return new ContactFieldOperand(field);
        }

        private static boolean isSupported(ContactField field) {
            switch (field) {
                case FOLDER_ID:
                case IMAGE1:
                case IMAGE1_CONTENT_TYPE:
                case IMAGE_LAST_MODIFIED:
                case IMAGE1_URL:
                    return false;
                default:
                    return true;
            }
        }
    }

}
