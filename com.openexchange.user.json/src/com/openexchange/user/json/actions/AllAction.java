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
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.groupware.search.Order;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;
import com.openexchange.user.json.UserContact;
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
