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

package com.openexchange.find.basic.tasks;

import static com.openexchange.find.basic.tasks.Constants.FIELD_STATUS;
import static com.openexchange.find.basic.tasks.Constants.FIELD_TYPE;
import static com.openexchange.find.facet.Facets.newDefaultBuilder;
import static com.openexchange.find.facet.Facets.newExclusiveBuilder;
import static com.openexchange.find.facet.Facets.newSimpleBuilder;
import static com.openexchange.java.SimpleTokenizer.tokenize;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.find.AutocompleteRequest;
import com.openexchange.find.AutocompleteResult;
import com.openexchange.find.Document;
import com.openexchange.find.Module;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.basic.AbstractContactFacetingModuleSearchDriver;
import com.openexchange.find.basic.Services;
import com.openexchange.find.common.CommonFacetType;
import com.openexchange.find.common.FolderType;
import com.openexchange.find.facet.DisplayItem;
import com.openexchange.find.facet.Facet;
import com.openexchange.find.facet.FacetValue;
import com.openexchange.find.facet.Filter;
import com.openexchange.find.facet.SimpleDisplayItem;
import com.openexchange.find.tasks.TasksDocument;
import com.openexchange.find.tasks.TasksFacetType;
import com.openexchange.find.tasks.TasksStrings;
import com.openexchange.find.util.DisplayItems;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.i18n.I18nServiceRegistry;
import com.openexchange.java.Strings;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
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
    protected Set<FolderType> getSupportedFolderTypes(AutocompleteRequest autocompleteRequest, ServerSession session) {
        UserPermissionBits userPermissionBits = session.getUserPermissionBits();
        if (userPermissionBits.hasFullSharedFolderAccess()) {
            return ALL_FOLDER_TYPES;
        }

        Set<FolderType> types = EnumSet.noneOf(FolderType.class);
        types.add(FolderType.PRIVATE);
        types.add(FolderType.PUBLIC);
        return types;
    }

    @Override
    public SearchResult doSearch(SearchRequest searchRequest, ServerSession session) throws OXException {
        TaskSearchObject searchObject = new TaskSearchObjectBuilder(session)
            .addFilters(searchRequest.getFilters())
            .addQueries(searchRequest.getQueries())
            .applyFolders(searchRequest)
            .build();
        searchObject.setStart(searchRequest.getStart());
        searchObject.setSize(searchRequest.getSize());

        int[] fields = searchRequest.getColumns().getIntColumns();
        if (fields.length == 0) {
            fields = DEFAULT_TASKS_FIELDS;
        }

        final TasksSQLInterface tasksSQL = new TasksSQLImpl(session);
        SearchIterator<Task> si = tasksSQL.findTask(searchObject, Task.TITLE, Order.ASCENDING, fields);
        try {
            List<Document> documents = new ArrayList<Document>();
            while (si.hasNext()) {
                documents.add(new TasksDocument(si.next()));
            }
            return new SearchResult(documents.size(), searchRequest.getStart(), documents, searchRequest.getActiveFacets());
        } finally {
            SearchIterators.close(si);
        }
    }

    @Override
    protected AutocompleteResult doAutocomplete(AutocompleteRequest autocompleteRequest, ServerSession session) throws OXException {
        String prefix = autocompleteRequest.getPrefix();
        List<Contact> contacts = autocompleteContacts(session, autocompleteRequest);
        List<Facet> facets = new ArrayList<Facet>();

        List<FacetValue> participants = new ArrayList<FacetValue>(contacts.size());
        for(Contact c : contacts) {
            String valueId = prepareFacetValueId("contact", session.getContextId(), Integer.toString(c.getObjectID()));
            List<String> queries;
            if (c.getInternalUserId() > 0) {
                queries = Collections.singletonList(Integer.toString(c.getInternalUserId()));
            } else {
                queries = extractMailAddessesFrom(c);
            }

            participants.add(buildParticipantFacet(valueId, DisplayItems.convert(c, session.getUser().getLocale(), Services.optionalService(I18nServiceRegistry.class)), queries));
        }

        //add field facets
        int minimumSearchCharacters = ServerConfig.getInt(ServerConfig.Property.MINIMUM_SEARCH_CHARACTERS);
        if (Strings.isNotEmpty(prefix) && prefix.length() >= minimumSearchCharacters) {
            /*
             * add prefix-aware field facets
             */
            List<String> prefixTokens = tokenize(prefix, minimumSearchCharacters);
            if (prefixTokens.isEmpty()) {
                prefixTokens = Collections.singletonList(prefix);
            }

            facets.add(newSimpleBuilder(CommonFacetType.GLOBAL)
                .withSimpleDisplayItem(prefix)
                .withFilter(Filter.of(CommonFacetType.GLOBAL.getId(), prefixTokens))
                .build());

            facets.add(newSimpleBuilder(TasksFacetType.TASK_TITLE)
                .withFormattableDisplayItem(TasksStrings.FACET_TASK_TITLE, prefix)
                .withFilter(Filter.of(Constants.FIELD_TITLE, prefixTokens))
                .build());

            facets.add(newSimpleBuilder(TasksFacetType.TASK_DESCRIPTION)
                .withFormattableDisplayItem(TasksStrings.FACET_TASK_DESCRIPTION, prefix)
                .withFilter(Filter.of(Constants.FIELD_DESCRIPTION, prefixTokens))
                .build());

            facets.add(newSimpleBuilder(TasksFacetType.TASK_ATTACHMENT_NAME)
                .withFormattableDisplayItem(TasksStrings.FACET_TASK_ATTACHMENT_NAME, prefix)
                .withFilter(Filter.of(Constants.FIELD_ATTACHMENT_NAME, prefixTokens))
                .build());

            participants.add(buildParticipantFacet(prefix, new SimpleDisplayItem(prefix), tokenize(prefix)));
        }

        if (!participants.isEmpty()) {
            facets.add(newDefaultBuilder(TasksFacetType.TASK_PARTICIPANTS)
                .withValues(participants)
                .build());
        }

        //add status facets

        final List<FacetValue> statusFacets = new ArrayList<FacetValue>(5);
        addStatusFacet(statusFacets, TaskStatus.NOT_STARTED, TasksStrings.TASK_STATUS_NOT_STARTED);
        addStatusFacet(statusFacets, TaskStatus.IN_PROGRESS, TasksStrings.TASK_STATUS_IN_PROGRESS);
        addStatusFacet(statusFacets, TaskStatus.DONE, TasksStrings.TASK_STATUS_DONE);
        addStatusFacet(statusFacets, TaskStatus.WAITING, TasksStrings.TASK_STATUS_WAITING);
        addStatusFacet(statusFacets, TaskStatus.DEFERRED, TasksStrings.TASK_STATUS_DEFERRED);
        facets.add(newExclusiveBuilder(TasksFacetType.TASK_STATUS)
            .withValues(statusFacets)
            .build());

        //add type facets
        final List<FacetValue> typeFacets = new ArrayList<FacetValue>(5);
        addTypeFacet(typeFacets, TaskType.SINGLE_TASK, TasksStrings.TASK_TYPE_SINGLE_TASK);
        addTypeFacet(typeFacets, TaskType.SERIES, TasksStrings.TASK_TYPE_SERIES);
        facets.add(newExclusiveBuilder(TasksFacetType.TASK_TYPE)
            .withValues(typeFacets)
            .build());

        return new AutocompleteResult(facets);
    }

    private static final FacetValue buildParticipantFacet(String valueId, DisplayItem item, List<String> queries) {
        return FacetValue.newBuilder(valueId)
            .withDisplayItem(item)
            .withFilter(Filter.of(Constants.PARTICIPANTS, queries))
            .build();
    }

    private static final void addStatusFacet(List<FacetValue> statusFacets, TaskStatus type, String status) {
        statusFacets.add(FacetValue.newBuilder(type.getIdentifier())
                            .withLocalizableDisplayItem(status)
                            .withFilter(Filter.of(FIELD_STATUS, type.getIdentifier()))
                            .build());
    }

    private static final void addTypeFacet(List<FacetValue> typeFacets, TaskType type, String taskType) {
        typeFacets.add(FacetValue.newBuilder(type.getIdentifier())
            .withLocalizableDisplayItem(taskType)
            .withFilter(Filter.of(FIELD_TYPE, type.getIdentifier()))
            .build());
    }
}
