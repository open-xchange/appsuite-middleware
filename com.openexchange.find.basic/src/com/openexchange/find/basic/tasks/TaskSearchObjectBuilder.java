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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.find.FindExceptionCode;
import com.openexchange.find.Module;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.basic.Folders;
import com.openexchange.find.basic.Services;
import com.openexchange.find.common.FolderType;
import com.openexchange.find.facet.Filter;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.TaskContentType;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link TaskSearchBuilder}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class TaskSearchObjectBuilder {

    private final TaskSearchObject searchObject;

    private final ServerSession session;

    private enum SupportedFields {title, description, status, folder_type, type, participant, attachment};

    /**
     * Initializes a new {@link TaskSearchBuilder}.
     */
    public TaskSearchObjectBuilder(ServerSession ss) {
        super();
        session = ss;
        searchObject = new TaskSearchObject();
    }

    /**
     * Build the {@link TaskSearchObject}
     *
     * @return the {@link TaskSearchObject}
     */
    public TaskSearchObject build() {
        return searchObject;
    }

    /**
     * Add the specified {@link Filter}s to the search object
     * @param filters
     * @return
     * @throws OXException if the provided {@Filter} contains an unsupported field (@see {@link SupportedFields}).
     */
    public TaskSearchObjectBuilder addFilters(List<Filter> filters) throws OXException {
        for(Filter f : filters) {
            addFilter(f);
        }
        return this;
    }

    /**
     * Add a {@link Filter} to the search object
     * @param filter
     * @return
     * @throws OXException if the provided {@link Filter} either contains an unsupported field (@see {@link SupportedFields})
     * or a field that is <code>null</code>.
     */
    public TaskSearchObjectBuilder addFilter(Filter filter) throws OXException {
        for(String f : filter.getFields()) {
            SupportedFields sf;
            try {
                sf = SupportedFields.valueOf(f);
            } catch (IllegalArgumentException e) {
                throw FindExceptionCode.UNSUPPORTED_FILTER_FIELD.create(f);
            } catch (NullPointerException e) {
                throw FindExceptionCode.NULL_FIELD.create(f);
            }

            switch(sf) {
                case title:
                    addTitleFilters(filter.getQueries());
                    break;
                case description:
                    addDescriptionFilters(filter.getQueries());
                    break;
                case status:
                    addStateFilters(filter.getQueries());
                    break;
                case folder_type:
                    addFolderTypeFilters(filter.getQueries());
                    break;
                case type:
                    addRecurrenceTypeFilters(filter.getQueries());
                    break;
                case participant:
                    addParticipantFilters(filter.getQueries());
                    break;
                case attachment:
                    addAttachmentFilters(filter.getQueries());
                    break;
                default:
                    throw FindExceptionCode.UNSUPPORTED_FILTER_FIELD.create(f);
            }
        }
        return this;
    }

    /**
     * Add the specified queries to the search object
     * @param queries
     * @return
     */
    public TaskSearchObjectBuilder addQueries(List<String> queries) {
        for (String q : queries) {
            addQuery(q);
        }
        return this;
    }

    /**
     * Applies folder IDs to the search, depending on the existence of a specific
     * folder ID or a folder type.
     *
     * @param folderID The folder ID to apply, or <code>null</code> if not specified
     * @param folderType The folder type for that all folder shall be applied; <code>null</code> if not specified.
     * @return The builder
     * @throws OXException
     */
    public TaskSearchObjectBuilder applyFolders(SearchRequest searchRequest) throws OXException {
        List<Integer> folderIDs = Folders.getIDs(searchRequest, Module.TASKS, session);
        if (folderIDs != null && !folderIDs.isEmpty()) {
            searchObject.setFolders(folderIDs);
        }
        return this;
    }

    /**
     * Add a query to the search object
     * @param query
     * @return
     */
    public TaskSearchObjectBuilder addQuery(String query) {
        Set<String> queries = searchObject.getQueries();
        if (queries == null)
            queries = new HashSet<String>();
        queries.add(wrapWithWildcards(query));
        searchObject.setQueries(queries);
        return this;
    }

    /**
     * Append the title queries to the search object.
     *
     * @param filters
     */
    private void addTitleFilters(List<String> filters) {
        Set<String> tf = searchObject.getTitles();
        if (tf == null)
            tf = new HashSet<String>(filters.size());
        for(String q : filters) {
            tf.add(wrapWithWildcards(q));
        }
        searchObject.setTitles(tf);
    }

    /**
     * Append the description queries to the search object.
     *
     * @param filters
     */
    private void addDescriptionFilters(List<String> filters) {
        Set<String> df = searchObject.getNotes();
        if (df == null)
            df = new HashSet<String>(filters.size());
        for(String q : filters) {
            df.add(wrapWithWildcards(q));
        }
        searchObject.setNotes(df);
    }

    /**
     * Add the status filter
     * @param filters
     * @throws OXException
     */
    private void addStateFilters(List<String> filters) throws OXException {
        Set<Integer> sf = searchObject.getStateFilters();
        if (sf == null)
            sf = new HashSet<Integer>(filters.size());
        for(String q : filters) {
            int s = Integer.parseInt(q);
            if (s > 5 || s < 1)
                throw FindExceptionCode.UNSUPPORTED_FILTER_QUERY.create(q, "status");
            sf.add(s);
        }
        searchObject.setStateFilters(sf);
    }

    /**
     * Prefetch all relevant folder ids, according to the specified filters
     * @param filters
     * @throws OXException
     */
    private void addFolderTypeFilters(List<String> filters) throws OXException {
        for(String q : filters) {
            Type t;
            if (FolderType.PUBLIC.getIdentifier().equals(q)) {
                t = PublicType.getInstance();
            } else if (FolderType.SHARED.getIdentifier().equals(q)) {
                t = SharedType.getInstance();
            } else if (FolderType.PRIVATE.getIdentifier().equals(q)) {
                t = PrivateType.getInstance();
            } else {
                throw FindExceptionCode.UNSUPPORTED_FILTER_QUERY.create(q, "folder_type");
            }
            UserizedFolder[] folders = Services.getFolderService().getVisibleFolders(FolderStorage.REAL_TREE_ID, TaskContentType.getInstance(), t, false, session, null).getResponse();
            if (folders != null && folders.length > 0) {
                for (UserizedFolder uf : folders) {
                    searchObject.addFolder(Integer.valueOf(uf.getID()));
                }
            }
        }
    }

    /**
     * Add the attachment filters
     * @param filters
     */
    private void addAttachmentFilters(List<String> filters) {
        Set<String> af = searchObject.getAttachmentNames();
        if (af == null)
            af = new HashSet<String>(filters.size());
        for(String q : filters) {
            af.add(wrapWithWildcards(q));
        }
        searchObject.setAttachmentNames(af);
    }

    /**
     * Set the recurrence type filter
     * @param filters
     * @throws OXException
     */
    private void addRecurrenceTypeFilters(List<String> filters) throws OXException {
        for(String r : filters) {
            if (TaskType.SERIES.getIdentifier().equals(r))
                searchObject.setSeriesFilter(true);
            else if(TaskType.SINGLE_TASK.getIdentifier().equals(r))
                searchObject.setSingleOccurrenceFilter(true);
            else
                throw FindExceptionCode.UNSUPPORTED_FILTER_QUERY.create(r, "type");
        }
    }

    private void addParticipantFilters(List<String> filters) throws OXException {
        Set<Integer> intP = searchObject.getUserIDs();
        Set<String> extP = searchObject.getExternalParticipants();

        if (intP == null)
            intP = new HashSet<Integer>();

        if (extP == null)
            extP = new HashSet<String>();

        for(String f : filters) {
            if (f.matches("\\d+")) {
                intP.add(Integer.parseInt(f));
            } else {
                extP.add(f);
            }
        }

        searchObject.setUserIDs(intP);
        searchObject.setExternalParticipants(extP);

        searchObject.setHasInternalParticipants(intP.size() > 0);
        searchObject.setHasExternalParticipants(extP.size() > 0);
    }

    /**
     * Wrap query with wildcards
     *
     * @param query
     * @return
     */
    private String wrapWithWildcards(String query) {
        String wrapped = null;
        if (query.charAt(0) != '*')
            wrapped = '*' + query;
        if (query.charAt(query.length() - 1) != '*')
            wrapped = query + '*';
        return wrapped;
    }
}
