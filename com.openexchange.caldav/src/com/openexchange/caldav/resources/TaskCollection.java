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

package com.openexchange.caldav.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.caldav.mixins.SupportedCalendarComponentSet;
import com.openexchange.caldav.mixins.SupportedCalendarComponentSets;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link TaskCollection} - CalDAV collection for tasks.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class TaskCollection extends CalDAVFolderCollection<Task> {

    private static final int[] BASIC_COLUMNS = {
        Task.UID, Task.FILENAME, Task.FOLDER_ID, Task.OBJECT_ID, Task.PARTICIPANTS, Task.LAST_MODIFIED, Task.CREATION_DATE,
        Task.CREATED_BY, Task.MODIFIED_BY
    };

    public TaskCollection(GroupwareCaldavFactory factory, WebdavPath url, UserizedFolder folder, int order) throws OXException {
        super(factory, url, folder, order);
        includeProperties(
            new SupportedCalendarComponentSet(SupportedCalendarComponentSet.VTODO),
            new SupportedCalendarComponentSets(SupportedCalendarComponentSets.VTODO)
        );
    }

    public TaskCollection(GroupwareCaldavFactory factory, WebdavPath url, UserizedFolder folder) throws OXException {
        this(factory, url, folder, NO_ORDER);
    }

    @Override
    protected Collection<Task> getModifiedObjects(Date since) throws OXException {
        return filter(factory.getTaskInterface().getModifiedTasksInFolder(folderID, BASIC_COLUMNS, since));
    }

    @Override
    protected Collection<Task> getDeletedObjects(Date since) throws OXException {
        return filter(factory.getTaskInterface().getDeletedTasksInFolder(folderID, BASIC_COLUMNS, since));
    }

    @Override
    protected Collection<Task> getObjects() throws OXException {
        return filter(factory.getTaskInterface().getTaskList(folderID, 0, -1, 0, Order.NO_ORDER, BASIC_COLUMNS));
    }

    @Override
    protected TaskResource createResource(Task object, WebdavPath url) throws OXException {
        return new TaskResource(factory, this, object, url);
    }

    protected Task load(Task task) throws OXException {
        return factory.getTaskInterface().getTaskById(task.getObjectID(), task.getParentFolderID());
    }

    @Override
    protected boolean isSupported(Task task) throws WebdavProtocolException {
        return null != task && null == task.getParticipants() && isInInterval(task, getIntervalStart(), getIntervalEnd());
    }

    @Override
    protected List<Task> getObjectsInRange(Date from, Date until) throws OXException {
        List<Task> tasks = new ArrayList<Task>();
        for (Task task : this.getObjects()) {
            if (isSupported(task) && isInInterval(task, from, until)) {
                tasks.add(task);
            }
        }
        return tasks;
    }

}
