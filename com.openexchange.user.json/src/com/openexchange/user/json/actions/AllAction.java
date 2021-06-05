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

package com.openexchange.user.json.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.SortOrder;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.groupware.search.Order;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;
import com.openexchange.user.UserService;
import com.openexchange.user.json.dto.UserContact;
import com.openexchange.user.json.field.UserField;
import com.openexchange.user.json.filter.UserCensorship;
import com.openexchange.user.json.mapping.UserMapper;

/**
 * {@link AllAction} - Maps the action to an <tt>all</tt> action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class AllAction extends AbstractUserAction {

    /**
     * The <tt>all</tt> action string.
     */
    public static final String ACTION = AJAXServlet.ACTION_ALL;

    /**
     * Initializes a new {@link AllAction}.
     */
    public AllAction(ServiceLookup services) {
        super(services);
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
        /*
         * Parse parameters
         */
        final int[] columns = parseIntArrayParameter(AJAXServlet.PARAMETER_COLUMNS, request);
        final int orderBy = parseIntParameter(AJAXServlet.PARAMETER_SORT, request);
        final Order order = OrderFields.parse(request.getParameter(AJAXServlet.PARAMETER_ORDER));
        final int leftHandLimit = parseIntParameter(AJAXServlet.LEFT_HAND_LIMIT, request);
        final int rightHandLimit = parseIntParameter(AJAXServlet.RIGHT_HAND_LIMIT, request);
        /*
         * Determine sort options
         */
        final int lhl = leftHandLimit < 0 ? 0 : leftHandLimit;
        final int rhl = rightHandLimit <= 0 ? 50000 : rightHandLimit;
        final SortOptions sortOptions = new SortOptions(lhl,  rhl - lhl);
        final UserField orderByUserField = UserMapper.getInstance().getMappedField(orderBy);
        if (null == orderByUserField) {
        	final ContactField orderByContactField = ContactMapper.getInstance().getMappedField(orderBy);
        	if (null != orderByContactField) {
        		// Sort field is a contact field: pass as it is
        		sortOptions.setOrderBy(new SortOrder[] { SortOptions.Order(orderByContactField, order) });
        	}
        }
        /*
         * Get contacts and users
         */
        Date lastModified = new Date(0);
        final List<UserContact> userContacts = new ArrayList<UserContact>();
        final ContactService contactService = services.getService(ContactService.class);
        final ContactField[] contactFields = ContactMapper.getInstance().getFields(columns,
        		ContactField.INTERNAL_USERID, ContactField.LAST_MODIFIED);
        final UserService userService = services.getService(UserService.class);
        UserField[] userFields = UserMapper.getInstance().getFields(columns);
        boolean needsUserData = null != userFields && 0 < userFields.length;
        UserCensorship censorship = needsUserData ? getUserCensorship(session) : null;
        SearchIterator<Contact> searchIterator = null;
        try {
        	searchIterator = contactService.getAllUsers(session, contactFields, sortOptions);
        	/*
        	 * Process results
        	 */
            while (searchIterator.hasNext()) {
            	final Contact contact = searchIterator.next();
            	/*
            	 * Check last modified
            	 */
            	if (contact.getLastModified().after(lastModified)) {
            		lastModified = contact.getLastModified();
            	}
            	/*
            	 * Get corresponding user & apply censorship if needed
            	 */
            	User user;
            	if (needsUserData && 0 < contact.getInternalUserId()) {
            	    user = userService.getUser(contact.getInternalUserId(), session.getContext());
                    if (null != censorship && session.getUserId() != user.getId()) {
                        user = censorship.censor(user);
                    }
            	} else {
            	    UserImpl placeholder = new UserImpl();
            	    placeholder.setId(contact.getInternalUserId());
            	    user = placeholder;
            	}
                userContacts.add(new UserContact(contact, user));
            }
        } finally {
            SearchIterators.close(searchIterator);
        }
        /*
         * Sort by users if a user field was denoted by sort field
         */
        if (1 < userContacts.size() && null != orderByUserField) {
        	Collections.sort(userContacts, UserContact.getComparator(
        			orderByUserField, session.getUser().getLocale(), Order.DESCENDING.equals(order)));
        }
        /*
         * Return appropriate result
         */
        return new AJAXRequestResult(userContacts, lastModified, "usercontact");
    }

}
