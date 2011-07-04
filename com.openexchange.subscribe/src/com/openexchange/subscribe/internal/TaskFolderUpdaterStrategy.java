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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.subscribe.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.generic.TargetFolderDefinition;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.subscribe.TargetFolderSession;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link TaskFolderUpdaterStrategy}
 * 
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class TaskFolderUpdaterStrategy implements FolderUpdaterStrategy<Task> {

    private static final int SQL_INTERFACE = 1;

    private static final int TARGET = 2;

    private static final int[] COMPARISON_COLUMNS = {
        Task.OBJECT_ID, Task.FOLDER_ID, Task.TITLE, Task.START_DATE, Task.END_DATE, Task.UID, Task.NOTE, Task.LAST_MODIFIED, Task.SEQUENCE };

    public int calculateSimilarityScore(Task original, Task candidate, Object session) throws AbstractOXException {
        int score = 0;
        // A score of 10 is sufficient for a match
        if ((isset(original.getUid()) || isset(candidate.getUid())) && eq(original.getUid(), candidate.getUid())) {
            score += 10;
        }
        if ((isset(original.getTitle()) || isset(candidate.getTitle())) && eq(original.getTitle(), candidate.getTitle())) {
            score += 5;
        }
        if ((isset(original.getNote()) || isset(candidate.getNote())) && eq(original.getNote(), candidate.getNote())) {
            score += 3;
        }
        if (original.getStartDate() != null && candidate.getStartDate() != null && eq(original.getStartDate(), candidate.getStartDate())) {
            score += 3;
        }
        if (original.getEndDate() != null && candidate.getEndDate() != null && eq(original.getEndDate(), candidate.getEndDate())) {
            score += 3;
        }

        return score;
    }

    private boolean isset(String s) {
        return s == null || s.length() > 0;
    }

    protected boolean eq(Object o1, Object o2) {
        if (o1 == null || o2 == null) {
            return false;
        } else {
            return o1.equals(o2);
        }
    }

    public void closeSession(Object session) throws AbstractOXException {

    }

    public Collection<Task> getData(TargetFolderDefinition target, Object session) throws AbstractOXException {
        TasksSQLInterface taskSql = (TasksSQLInterface) getFromSession(SQL_INTERFACE, session);

        int folderId = target.getFolderIdAsInt();
        SearchIterator<Task> tasksInFolder;
        List<Task> retval = new ArrayList<Task>();
        int[] columns = Task.ALL_COLUMNS;
        
        // filter out LAST_MODIFIED_UTC as it is a virtual column and will not work
        ArrayList<Integer> filteredColumns = new ArrayList<Integer>();
        for (int i = 0; i<columns.length; i++){
            if (columns[i] != Task.LAST_MODIFIED_UTC){
                filteredColumns.add(columns[i]);
            }
        }
        columns = new int[filteredColumns.size()];
        int counter = 0;
        for (Integer integer : filteredColumns){
            columns[counter] = integer;
            counter++;
        }
        
        tasksInFolder = taskSql.getTaskList(folderId, 0, Integer.MAX_VALUE, 0, Order.ASCENDING, columns);
        while (tasksInFolder.hasNext()) {
            retval.add((Task) tasksInFolder.next());
        }

        return retval;
    }

    public int getThreshold(Object session) throws AbstractOXException {
        return 9;
    }

    public boolean handles(FolderObject folder) {
        return folder.getModule() == FolderObject.TASK;
    }

    public void save(Task newElement, Object session) throws AbstractOXException {
        TasksSQLInterface taskSql = (TasksSQLInterface) getFromSession(SQL_INTERFACE, session);
        TargetFolderDefinition target = (TargetFolderDefinition) getFromSession(TARGET, session);
        newElement.setParentFolderID(target.getFolderIdAsInt());
        taskSql.insertTaskObject(newElement);
    }

    private Object getFromSession(int key, Object session) {
        return ((Map<Integer, Object>) session).get(key);
    }

    public Object startSession(TargetFolderDefinition target) throws AbstractOXException {
        Map<Integer, Object> userInfo = new HashMap<Integer, Object>();
        userInfo.put(SQL_INTERFACE, new TasksSQLImpl(new TargetFolderSession(target)));
        userInfo.put(TARGET, target);
        return userInfo;
    }

    public void update(Task original, Task update, Object session) throws AbstractOXException {
        TasksSQLInterface taskSql = (TasksSQLInterface) getFromSession(SQL_INTERFACE, session);

        update.setParentFolderID(original.getParentFolderID());
        update.setObjectID(original.getObjectID());
        update.setLastModified(original.getLastModified());

        taskSql.updateTaskObject(update, original.getParentFolderID(), original.getLastModified());
    }

}
