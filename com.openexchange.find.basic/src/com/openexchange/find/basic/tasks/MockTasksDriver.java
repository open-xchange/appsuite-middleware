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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.find.basic.tasks;

import static com.openexchange.find.basic.tasks.Constants.FIELD_ATTACHMENT_NAME;
import static com.openexchange.find.basic.tasks.Constants.FIELD_DESCRIPTION;
import static com.openexchange.find.basic.tasks.Constants.FIELD_LOCATION;
import static com.openexchange.find.basic.tasks.Constants.FIELD_STATUS;
import static com.openexchange.find.basic.tasks.Constants.FIELD_SUBJECT;
import static com.openexchange.find.basic.tasks.Constants.FIELD_TYPE;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.exception.OXException;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.Document;
import com.openexchange.find.Module;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.basic.AbstractContactFacetingModuleSearchDriver;
import com.openexchange.find.common.ContactDisplayItem;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.FieldFacet;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.mail.MailFacetType;
import com.openexchange.find.tasks.TaskStatusDisplayItem;
import com.openexchange.find.tasks.TaskTypeDisplayItem;
import com.openexchange.find.tasks.TasksDocument;
import com.openexchange.find.tasks.TasksFacetType;
import com.openexchange.find.tasks.TasksStrings;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MockTasksDriver}
 *
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 * @since 7.6.0
 */
public class MockTasksDriver extends AbstractContactFacetingModuleSearchDriver {

    // --------------------------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link MockTasksDriver}.
     */
    public MockTasksDriver() {
        super();
    }

    @Override
    public Module getModule() {
        return Module.TASKS;
    }

    @Override
    public boolean isValidFor(ServerSession session) throws OXException {
        return session.getUserConfiguration().hasTask();
    }

    public void getConfiguration(ServerSession session) throws OXException {
        List<Facet> staticFacets = new ArrayList<Facet>(7);

        // subject or task name
        {
            final Facet facet = new FieldFacet(TasksFacetType.TASK_SUBJECT, FIELD_SUBJECT);
            staticFacets.add(facet);
        }
        // description
        {
            final Facet facet = new FieldFacet(TasksFacetType.TASK_DESCRIPTION, FIELD_DESCRIPTION);
            staticFacets.add(facet);
        }
        // location
        {
            final Facet facet = new FieldFacet(TasksFacetType.TASK_LOCATION, FIELD_LOCATION);
            staticFacets.add(facet);
        }
        // attachment name
        {

            final Facet facet = new FieldFacet(TasksFacetType.TASK_ATTACHMENT_NAME, FIELD_ATTACHMENT_NAME);
            staticFacets.add(facet);
        }
        // Status
        {
            final List<FacetValue> staticFacetValues = new ArrayList<FacetValue>(5);
            staticFacetValues.add(new FacetValue(TaskStatusDisplayItem.Type.NOT_STARTED.getIdentifier(), new TaskStatusDisplayItem(
                TasksStrings.TASK_TYPE_NOT_STARTED,
                TaskStatusDisplayItem.Type.NOT_STARTED), FacetValue.UNKNOWN_COUNT, new Filter(
                Collections.singletonList(FIELD_STATUS),
                TaskStatusDisplayItem.Type.NOT_STARTED.getIdentifier())));
            staticFacetValues.add(new FacetValue(TaskStatusDisplayItem.Type.IN_PROGRESS.getIdentifier(), new TaskStatusDisplayItem(
                TasksStrings.TASK_TYPE_IN_PROGRESS,
                TaskStatusDisplayItem.Type.IN_PROGRESS), FacetValue.UNKNOWN_COUNT, new Filter(
                Collections.singletonList(Constants.FIELD_STATUS),
                TaskStatusDisplayItem.Type.IN_PROGRESS.getIdentifier())));
            staticFacetValues.add(new FacetValue(TaskStatusDisplayItem.Type.DONE.getIdentifier(), new TaskStatusDisplayItem(
                TasksStrings.TASK_TYPE_DONE,
                TaskStatusDisplayItem.Type.DONE), FacetValue.UNKNOWN_COUNT, new Filter(
                Collections.singletonList(Constants.FIELD_STATUS),
                TaskStatusDisplayItem.Type.DONE.getIdentifier())));
            staticFacetValues.add(new FacetValue(TaskStatusDisplayItem.Type.WAITING.getIdentifier(), new TaskStatusDisplayItem(
                TasksStrings.TASK_TYPE_WAITING,
                TaskStatusDisplayItem.Type.WAITING), FacetValue.UNKNOWN_COUNT, new Filter(
                Collections.singletonList(Constants.FIELD_STATUS),
                TaskStatusDisplayItem.Type.WAITING.getIdentifier())));
            staticFacetValues.add(new FacetValue(TaskStatusDisplayItem.Type.DEFERRED.getIdentifier(), new TaskStatusDisplayItem(
                TasksStrings.TASK_TYPE_DEFERRED,
                TaskStatusDisplayItem.Type.DEFERRED), FacetValue.UNKNOWN_COUNT, new Filter(
                Collections.singletonList(Constants.FIELD_STATUS),
                TaskStatusDisplayItem.Type.DEFERRED.getIdentifier())));
            final Facet statusFacet = new Facet(TasksFacetType.TASK_STATUS, staticFacetValues);
            staticFacets.add(statusFacet);
        }
        // Folder
        {
            final Facet folderTypeFacet = buildFolderTypeFacet();
            staticFacets.add(folderTypeFacet);
        }
        // Type
        {
            final List<FacetValue> staticFacetValues = new ArrayList<FacetValue>(2);
            staticFacetValues.add(new FacetValue(TaskTypeDisplayItem.Type.SINGLE_TASK.getIdentifier(), new TaskTypeDisplayItem(
                TasksStrings.TASK_STATUS_SINGLE_TASK,
                TaskTypeDisplayItem.Type.SINGLE_TASK), FacetValue.UNKNOWN_COUNT, new Filter(
                Collections.singletonList(FIELD_TYPE),
                TaskTypeDisplayItem.Type.SINGLE_TASK.getIdentifier())));
            staticFacetValues.add(new FacetValue(TaskTypeDisplayItem.Type.SERIES.getIdentifier(), new TaskTypeDisplayItem(
                TasksStrings.TASK_STATUS_SERIES,
                TaskTypeDisplayItem.Type.SERIES), FacetValue.UNKNOWN_COUNT, new Filter(
                Collections.singletonList(FIELD_TYPE),
                TaskTypeDisplayItem.Type.SERIES.getIdentifier())));
            final Facet taskTypeFacet = new Facet(TasksFacetType.TASK_TYPE, staticFacetValues);
            staticFacets.add(taskTypeFacet);
        }
    }

    @Override
    public AutocompleteResult doAutocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        List<Contact> contacts = autocompleteContacts(session, autocompleteRequest);
        List<FacetValue> contactValues = new ArrayList<FacetValue>(contacts.size());
        for (Contact contact : contacts) {
            Filter filter = new Filter(Constants.PERSONS_FILTER_FIELDS, new ArrayList<String>(extractMailAddessesFrom(contact)));
            String valueId = prepareFacetValueId("contact", session.getContextId(), Integer.toString(contact.getObjectID()));
            contactValues.add(new FacetValue(
                valueId,
                new ContactDisplayItem(contact),
                FacetValue.UNKNOWN_COUNT,
                filter));
        }

        Facet contactFacet = new Facet(MailFacetType.CONTACTS, contactValues);
        List<Facet> facets = Collections.singletonList(contactFacet);
        return new AutocompleteResult(facets);
    }

    private Set<String> extractMailAddessesFrom(final Contact contact) {
        final Set<String> addrs = new HashSet<String>(4);

        String mailAddress = contact.getEmail1();
        if (mailAddress != null) {
            addrs.add(mailAddress);
        }

        mailAddress = contact.getEmail2();
        if (mailAddress != null) {
            addrs.add(mailAddress);
        }

        mailAddress = contact.getEmail3();
        if (mailAddress != null) {
            addrs.add(mailAddress);
        }

        return addrs;
    }

    @Override
    public SearchResult search(SearchRequest searchRequest, ServerSession session) throws OXException {

        final TaskSearchObject searchObj = new TaskSearchObject();

        final int[] guiColumns = Task.ALL_COLUMNS;

        List<Task> taskList = new ArrayList<Task>();
        SearchIterator<Task> it = null;
        int orderBy = 0;
        try {
            final TasksSQLInterface taskssql = new TasksSQLImpl(session);
            it = taskssql.getTasksByExtendedSearch(searchObj, orderBy, Order.ASCENDING, guiColumns);
            while (it.hasNext()) {
                final Task taskObj = it.next();
                taskList.add(taskObj);
            }
        } finally {
            if (it != null) {
                it.close();
            }
        }

        List<Document> documents = new ArrayList<Document>(taskList.size());
        for (Task task : taskList) {
            documents.add(new TasksDocument(task));
        }

        return new SearchResult(-1, searchRequest.getStart(), documents);
    }

}
