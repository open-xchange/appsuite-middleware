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

import static com.openexchange.contact.ContactIDUtil.createContactID;
import static com.openexchange.contact.common.ContactsParameters.PARAMETER_COLLATION;
import static com.openexchange.contact.common.ContactsParameters.PARAMETER_FIELDS;
import static com.openexchange.contact.common.ContactsParameters.PARAMETER_LEFT_HAND_LIMIT;
import static com.openexchange.contact.common.ContactsParameters.PARAMETER_ORDER;
import static com.openexchange.contact.common.ContactsParameters.PARAMETER_ORDER_BY;
import static com.openexchange.contact.common.ContactsParameters.PARAMETER_RIGHT_HAND_LIMIT;
import static com.openexchange.java.Autoboxing.I;
import java.util.AbstractMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.contact.ContactID;
import com.openexchange.contact.provider.composition.IDBasedContactsAccess;
import com.openexchange.contact.provider.composition.IDBasedContactsAccessFactory;
import com.openexchange.contacts.json.ContactRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link IDBasedContactAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class IDBasedContactAction extends ContactAction {

    public static final String MODULE_NAME = "contacts";

    public static final int[] COLUMNS_ALIAS_ALL = new int[] { 20, 1, 5, 2, 602 };
    public static final int[] COLUMNS_ALIAS_LIST = new int[] { 20, 1, 5, 2, 500, 501, 502, 505, 523, 525, 526, 527, 542, 555, 102, 602, 592, 101, 551, 552, 543, 547, 548, 549, 556, 569 };

    // Mapping to ContactsParameters parameters
    protected static final String PARAM_FIELDS = "columns";
    protected static final String PARAM_ORDER_BY = "sort";
    protected static final String PARAM_ORDER = "order";
    protected static final String PARAM_UPDATE_CACHE = "updateCache";
    protected static final String PARAM_LEFT_HAND_LIMIT = "left_hand_limit";
    protected static final String PARAM_RIGHT_HAND_LIMIT = "right_hand_limit";
    protected static final String PARAM_COLLATION = "collation";

    /**
     * Contact fields that are not persistent.
     */
    public static final EnumSet<ContactField> VIRTUAL_FIELDS = EnumSet.of(ContactField.IMAGE1_URL, ContactField.LAST_MODIFIED_UTC, ContactField.SORT_NAME);

    /**
     * Initializes a new {@link IDBasedContactAction}.
     *
     * @param serviceLookup The service lookup to use
     */
    public IDBasedContactAction(ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    public AJAXRequestResult perform(ContactRequest contactRequest) throws OXException {
        IDBasedContactsAccess access = initContactsAccess(contactRequest);
        AJAXRequestResult result = null;
        boolean comitted = false;
        try {
            access.startTransaction();
            result = perform(access, contactRequest);
            access.commit();
            comitted = true;
        } finally {
            if (false == comitted) {
                access.rollback();
            }
            access.finish();
        }
        List<OXException> warnings = access.getWarnings();
        if (null != warnings && 0 < warnings.size()) {
            result.addWarnings(warnings);
        }
        return result;
    }

    /**
     * Initialises the contacts access based on the specified contacts request
     *
     * @param requestData The request data
     * @return The initialised {@link IDBasedContactsAccess}
     */
    protected IDBasedContactsAccess initContactsAccess(ContactRequest contactRequest) throws OXException {
        IDBasedContactsAccess access = requireService(IDBasedContactsAccessFactory.class).createAccess(contactRequest.getSession());
        Set<String> requiredParameters = getMandatoryParameters();
        Set<String> optionalParameters = getOptionalParameters();
        Set<String> parameters = new HashSet<>();
        parameters.addAll(requiredParameters);
        parameters.addAll(optionalParameters);
        for (String parameter : parameters) {
            Entry<String, ?> entry = parseParameter(contactRequest, parameter, requiredParameters.contains(parameter));
            if (null != entry) {
                access.set(entry.getKey(), entry.getValue());
            }
        }
        return access;
    }

    /**
     * Retrieves the given parameter as an Entry object
     *
     * @param request The request
     * @param parameter The parameter name
     * @param required Defines if the parameter is required
     * @return The parameter or null if it isn't required
     * @throws OXException if the parameter is required and can't be found or if the parameter can't be parsed
     */
    @SuppressWarnings("deprecation")
    private Entry<String, ?> parseParameter(ContactRequest request, String parameter, boolean required) throws OXException {
        if (false == request.getRequest().containsParameter(parameter)) {
            if (false == required) {
                return null;
            }
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(parameter);
        }
        switch (parameter) {
            case PARAM_ORDER_BY:
                return request.isInternalSort() ? null : new AbstractMap.SimpleEntry<>(PARAMETER_ORDER_BY, ContactField.getByValue(request.getSort()));
            case PARAM_ORDER:
                return request.isInternalSort() ? null : new AbstractMap.SimpleEntry<>(PARAMETER_ORDER, request.getOrder());
            case PARAM_FIELDS:
                return new AbstractMap.SimpleEntry<>(PARAMETER_FIELDS, getFields(request));
            case PARAM_LEFT_HAND_LIMIT:
                return request.isInternalSort() ? null : new AbstractMap.SimpleEntry<>(PARAMETER_LEFT_HAND_LIMIT, I(request.getLeftHandLimit()));
            case PARAM_RIGHT_HAND_LIMIT:
                return request.isInternalSort() ? null : new AbstractMap.SimpleEntry<>(PARAMETER_RIGHT_HAND_LIMIT, I(request.getRightHandLimit()));
            case PARAM_COLLATION:
                return new AbstractMap.SimpleEntry<>(PARAMETER_COLLATION, request.getCollation());
            default:
                return null;
        }
    }

    /**
     * Performs the request.
     *
     * @param access The {@link IDBasedContactsAccess}
     * @param request The contacts {@link Request}
     * @return The AJAX result
     */
    protected abstract AJAXRequestResult perform(IDBasedContactsAccess access, ContactRequest request) throws OXException;

    /**
     * Gets a list of mandatory parameter names that will be evaluated. If missing in the request, an appropriate exception is thrown. By
     * default, an empty list is returned.
     *
     * @return The list of mandatory parameters
     */
    protected Set<String> getMandatoryParameters() {
        return ImmutableSet.of();
    }

    /**
     * Gets a list of parameter names that will be evaluated if set, but are not required to fulfil the request. By default, an empty
     * list is returned.
     *
     * @return The list of optional parameters
     */
    protected Set<String> getOptionalParameters() {
        return ImmutableSet.of();
    }

    /**
     * Returns the request's {@link ContactField}s
     *
     * @param request the contact request
     * @return The {@link ContactField}s
     */
    protected ContactField[] getFields(ContactRequest request) throws OXException {
        return request.getFields();
    }

    /**
     * Sorts the contacts if needed
     *
     * @param request The contact request
     * @param contacts The contacts to sort
     * @return The (optionally) sorted contacts
     */
    protected List<Contact> sortIfNeeded(ContactRequest request, List<Contact> contacts) throws OXException {
        List<Contact> ret = contacts;
        if (request.sortInternalIfNeeded(contacts)) {
            ret = request.slice(contacts);
        }
        return ret;
    }

    /**
     * Sorts the contacts if needed by the specified {@link ContactField}
     *
     * @param request The contact request
     * @param contacts The contacts to sort
     * @param byField The {@link ContactField} to use for sorting
     * @return The (optionally) sorted contacts
     */
    protected List<Contact> sortIfNeeded(ContactRequest request, List<Contact> contacts, ContactField byField) throws OXException {
        List<Contact> ret = contacts;
        if (request.sortInternalIfNeeded(contacts, byField, request.getStart())) {
            ret = request.slice(contacts);
        }
        return ret;
    }

    /**
     * Constructs a full contact identifier from the supplied folder and object identifiers.
     *
     * @param folderId The folder identifier
     * @param objectId The object identifier
     * @return The full contact identifier
     */
    protected ContactID getContactID(String folderId, String objectId) {
        return createContactID(folderId, objectId);
    }
}
