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

import static com.openexchange.find.basic.tasks.Constants.FIELD_STATUS;
import static com.openexchange.find.basic.tasks.Constants.FIELD_TYPE;
import java.util.ArrayList;
import java.util.Collections;
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
import com.openexchange.find.common.FormattableDisplayItem;
import com.openexchange.find.common.SimpleDisplayItem;
import com.openexchange.find.facet.DefaultFacet;
import com.openexchange.find.facet.DisplayItem;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.SimpleFacet;
import com.openexchange.find.facet.Filter;
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
import com.openexchange.java.Strings;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link BasicTasksDriver}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.6.0
 */
public class BasicTasksDriver extends AbstractContactFacetingModuleSearchDriver {

    private final static int DEFAULT_TASKS_FIELDS[] = new int[] {
        Task.OBJECT_ID,
        Task.FOLDER_ID,
        Task.PRIVATE_FLAG,
        Task.TITLE,
        Task.END_DATE,
        Task.NOTE,
        Task.PARTICIPANTS,
        Task.STATUS,
        Task.PERCENT_COMPLETED,
        Task.PRIORITY
    };

    /**
     * Initializes a new {@link BasicTasksDriver}.
     */
    public BasicTasksDriver() {
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

    @Override
    protected Set<Integer> getSupportedFolderTypes() {
        return ALL_FOLDER_TYPES;
    }

    @Override
    public SearchResult doSearch(SearchRequest searchRequest, ServerSession session) throws OXException {
        TaskSearchObject searchObject = new TaskSearchObjectBuilder(session)
            .addFilters(searchRequest.getFilters())
            .addQueries(searchRequest.getQueries())
            .applyFolders(searchRequest.getFolderId(), searchRequest.getFolderType())
            .build();
        searchObject.setStart(searchRequest.getStart());
        searchObject.setSize(searchRequest.getSize());

        int[] fields = searchRequest.getColumns();
        if (fields == null) {
            fields = DEFAULT_TASKS_FIELDS;
        }

        final TasksSQLInterface tasksSQL = new TasksSQLImpl(session);
        SearchIterator<Task> si = tasksSQL.findTask(searchObject, Task.TITLE, Order.ASCENDING, fields);
        List<Document> documents = new ArrayList<Document>();
        while(si.hasNext()) {
            documents.add(new TasksDocument(si.next()));
        }
        return new SearchResult(documents.size(), searchRequest.getStart(), documents, searchRequest.getActiveFacets());
    }

    @Override
    protected String getFormatStringForGlobalFacet() {
        return TasksStrings.FACET_GLOBAL;
    }

    @Override
    protected AutocompleteResult doAutocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        String prefix = autocompleteRequest.getPrefix();
        List<Contact> contacts = autocompleteContacts(session, autocompleteRequest);
        List<Facet> facets = new ArrayList<Facet>();

        //add field facets
        if (false == Strings.isEmpty(prefix)) {
            facets.add(new SimpleFacet(TasksFacetType.TASK_TITLE, new FormattableDisplayItem(TasksStrings.FACET_TASK_TITLE, prefix), Constants.FIELD_TITLE, prefix));
            facets.add(new SimpleFacet(TasksFacetType.TASK_DESCRIPTION, new FormattableDisplayItem(TasksStrings.FACET_TASK_DESCRIPTION,  prefix), Constants.FIELD_DESCRIPTION, prefix));
            facets.add(new SimpleFacet(TasksFacetType.TASK_ATTACHMENT_NAME, new FormattableDisplayItem(TasksStrings.FACET_TASK_ATTACHMENT_NAME,  prefix), Constants.FIELD_ATTACHMENT_NAME, prefix));
        }

        //add participant facets
        List<FacetValue> participants = new ArrayList<FacetValue>(contacts.size());
        for(Contact c : contacts) {
            String valueId = prepareFacetValueId("contact", session.getContextId(), Integer.toString(c.getObjectID()));
            List<String> queries = extractMailAddessesFrom(c);
            participants.add(buildParticipantFacet(valueId, new ContactDisplayItem(c), queries));
        }
        if (!prefix.isEmpty())
            participants.add(buildParticipantFacet(prefix, new SimpleDisplayItem(prefix), Collections.singletonList(prefix)));
        if (!participants.isEmpty())
            facets.add(new DefaultFacet(TasksFacetType.TASK_PARTICIPANTS, participants));

        //add status facets
        final List<FacetValue> statusFacets = new ArrayList<FacetValue>(5);
        addStatusFacet(statusFacets, TaskStatusDisplayItem.Type.NOT_STARTED, TasksStrings.TASK_STATUS_NOT_STARTED);
        addStatusFacet(statusFacets, TaskStatusDisplayItem.Type.IN_PROGRESS, TasksStrings.TASK_STATUS_IN_PROGRESS);
        addStatusFacet(statusFacets, TaskStatusDisplayItem.Type.DONE, TasksStrings.TASK_STATUS_DONE);
        addStatusFacet(statusFacets, TaskStatusDisplayItem.Type.WAITING, TasksStrings.TASK_STATUS_WAITING);
        addStatusFacet(statusFacets, TaskStatusDisplayItem.Type.DEFERRED, TasksStrings.TASK_STATUS_DEFERRED);
        facets.add(new DefaultFacet(TasksFacetType.TASK_STATUS, statusFacets));

        //add type facets
        final List<FacetValue> typeFacets = new ArrayList<FacetValue>(5);
        addTypeFacet(typeFacets, TaskTypeDisplayItem.Type.SINGLE_TASK, TasksStrings.TASK_TYPE_SINGLE_TASK);
        addTypeFacet(typeFacets, TaskTypeDisplayItem.Type.SERIES, TasksStrings.TASK_TYPE_SERIES);
        facets.add(new DefaultFacet(TasksFacetType.TASK_TYPE, typeFacets));

        return new AutocompleteResult(facets);
    }

    /**
     * Build a participant facet
     * @param valueId
     * @param item
     * @param queries
     * @return
     */
    private static final FacetValue buildParticipantFacet(String valueId, DisplayItem item, List<String> queries) {
        return new FacetValue(valueId, item, FacetValue.UNKNOWN_COUNT,
                                        Collections.singletonList(
                                            new Filter(Constants.FIELD_PARTICIPANT, TasksStrings.FACET_TASK_PARTICIPANTS, Constants.PARTICIPANTS, queries)));
    }

    /**
     * Add a status facet
     * @param statusFacets
     * @param type
     * @param status
     */
    private static final void addStatusFacet(List<FacetValue> statusFacets, TaskStatusDisplayItem.Type type, String status) {
        statusFacets.add(new FacetValue(type.getIdentifier(),
                            new TaskStatusDisplayItem(status,type), FacetValue.UNKNOWN_COUNT,
                            new Filter(Collections.singletonList(FIELD_STATUS),type.getIdentifier())));
    }

    /**
     * Add task type facet
     * @param typeFacets
     * @param type
     * @param taskType
     */
    private static final void addTypeFacet(List<FacetValue> typeFacets, TaskTypeDisplayItem.Type type, String taskType) {
        typeFacets.add(new FacetValue(type.getIdentifier(),
                            new TaskTypeDisplayItem(taskType,type),
                            FacetValue.UNKNOWN_COUNT,
                            new Filter(Collections.singletonList(FIELD_TYPE),type.getIdentifier())));
    }

    //TODO: maybe move a level higher in the class hierarchy?
    private static List<String> extractMailAddessesFrom(final Contact contact) {
        List<String> addrs = new ArrayList<String>(3);
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
}
