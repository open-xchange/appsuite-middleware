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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.contact.AutocompleteParameters;
import com.openexchange.contact.SortOptions;
import com.openexchange.contacts.json.ContactActionFactory;
import com.openexchange.contacts.json.ContactRequest;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.Type;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.UseCountComparator;
import com.openexchange.groupware.container.Contact;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link AutocompleteAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
@Action(method = RequestMethod.PUT, name = "autocomplete", description = "Auto-complete contacts.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "query", description = "The query to search for."),
    @Parameter(name = "folder", optional=true, description = "Object ID of the parent folder that is searched. If not set, all visible folders are used."),
    @Parameter(name = "email", optional=true, type=Type.BOOLEAN, description = "Whether to only include contacts with at least one e-mail address. Defaults to \"true\"."),
    @Parameter(name = "columns", description = "The requested fields."),
    @Parameter(name = "sort", optional=true, description = "The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified. In case of use of column 609 (use count depending order for collected contacts with global address book) the parameter \"order\" ist NOT necessary and will be ignored."),
    @Parameter(name = "order", optional=true, description = "\"asc\" if the response entires should be sorted in the ascending order, \"desc\" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified."),
    @Parameter(name = "collation", description = "Allows you to specify a collation to sort the contacts by. As of 6.20, only supports \"gbk\" and \"gb2312\", not needed for other languages. Parameter sort should be set for this to work."),
    @Parameter(name = "admin", optional=true, type=Type.BOOLEAN, description = "Whether to include the contact representing the admin in the result or not. Defaults to \"true\".")
}, responseDescription = "An array with contact data. Each array element describes one contact and is itself an array. The elements of each array contain the information specified by the corresponding identifiers in the columns parameter.")
@OAuthAction(ContactActionFactory.OAUTH_READ_SCOPE)
public class AutocompleteAction extends ContactAction {

    /**
     * Initializes a new {@link AutocompleteAction}.
     *
     * @param serviceLookup The service lookup to use
     */
    public AutocompleteAction(ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(ContactRequest request) throws OXException {
        /*
         * extract parameters
         */
        Boolean requireEmail = Boolean.valueOf(request.isRequireEmail());
        String query = request.getQuery();
        boolean excludeAdmin = request.isExcludeAdmin();
        int excludedAdminID = excludeAdmin ? request.getSession().getContext().getMailadmin() : -1;
        ContactField[] fields = excludeAdmin ? request.getFields(ContactField.INTERNAL_USERID) : request.getFields();
        String folderID = request.optFolderID();
        /*
         * perform search
         */
        AutocompleteParameters parameters = AutocompleteParameters.newInstance();
        parameters.put(AutocompleteParameters.REQUIRE_EMAIL, requireEmail);
        SearchIterator<Contact> searchIterator;
        SortOptions sortOptions = request.getSortOptions();
        if (null != folderID) {
            searchIterator = getContactService().autocompleteContacts(request.getSession(), Collections.singletonList(folderID),
                query, parameters, fields, sortOptions);
        } else {
            searchIterator = getContactService().autocompleteContacts(request.getSession(), query, parameters, fields, sortOptions);
        }
        /*
         * construct result
         */
        List<Contact> contacts = new ArrayList<Contact>();
        Date lastModified = addContacts(contacts, searchIterator, excludedAdminID);
        Collections.sort(contacts, new UseCountComparator(request.getSession().getUser().getLocale()));
        return new AJAXRequestResult(contacts, lastModified, "contact");
    }

}
