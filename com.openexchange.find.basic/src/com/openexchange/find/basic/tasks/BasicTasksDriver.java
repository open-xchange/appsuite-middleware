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

import java.util.ArrayList;
import java.util.List;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.exception.OXException;
import com.openexchange.find.Document;
import com.openexchange.find.SearchRequest;
import com.openexchange.find.SearchResult;
import com.openexchange.find.tasks.TasksDocument;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link BasicTasksDriver}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class BasicTasksDriver extends MockTasksDriver {
    
    private final static int TASKS_FIELDS[] = {DataObject.OBJECT_ID, DataObject.CREATED_BY, Task.TITLE, Task.STATUS, Task.NOTE};

    /**
     * Initializes a new {@link BasicTasksDriver}.
     */
    public BasicTasksDriver() {
        super();
    }
    
    /*
     * (non-Javadoc)
     * @see com.openexchange.find.basic.tasks.MockTasksDriver#search(com.openexchange.find.SearchRequest, com.openexchange.tools.session.ServerSession)
     */
    @Override
    public SearchResult search(SearchRequest searchRequest, ServerSession session) throws OXException {
        TaskSearchObjectBuilder builder = new TaskSearchObjectBuilder(session);
        TaskSearchObject searchObject = builder.addFilters(searchRequest.getFilters()).addQueries(searchRequest.getQueries()).build();
        
        final TasksSQLInterface tasksSQL = new TasksSQLImpl(session);
        SearchIterator<Task> si = tasksSQL.findTask(searchObject, Task.TITLE, Order.ASCENDING, TASKS_FIELDS);
        List<Document> documents = new ArrayList<Document>();
        while(si.hasNext()) {
            documents.add(new TasksDocument(si.next()));
        }
        return new SearchResult(-1, searchRequest.getStart(), documents);
    }

}
