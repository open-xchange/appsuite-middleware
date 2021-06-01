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

package com.openexchange.caldav.resources;

import static com.openexchange.dav.DAVProtocol.protocolException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.caldav.Tools;
import com.openexchange.caldav.mixins.CalendarOrder;
import com.openexchange.caldav.mixins.SupportedCalendarComponentSet;
import com.openexchange.caldav.mixins.SupportedCalendarComponentSets;
import com.openexchange.dav.reports.SyncStatus;
import com.openexchange.dav.resources.SyncToken;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.WebdavStatusImpl;

/**
 * {@link TaskCollection} - CalDAV collection for tasks.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class TaskCollection extends CalDAVFolderCollection<Task> {

    private static final Pattern OBJECT_ID_PATTERN = Pattern.compile("^\\d+$");

    private static final int[] BASIC_COLUMNS = {
        Task.UID, Task.FILENAME, Task.FOLDER_ID, Task.OBJECT_ID, Task.PARTICIPANTS, Task.LAST_MODIFIED, Task.CREATION_DATE,
        Task.CREATED_BY, Task.MODIFIED_BY, Task.RECURRENCE_TYPE
    };

    private final int folderID;

    public TaskCollection(GroupwareCaldavFactory factory, WebdavPath url, UserizedFolder folder) throws OXException {
        this(factory, url, folder, CalendarOrder.NO_ORDER);
    }

    public TaskCollection(GroupwareCaldavFactory factory, WebdavPath url, UserizedFolder folder, int order) throws OXException {
        super(factory, url, folder, order);
        includeProperties(
            new SupportedCalendarComponentSet(SupportedCalendarComponentSet.VTODO),
            new SupportedCalendarComponentSets(SupportedCalendarComponentSets.VTODO)
        );
        this.folderID = Tools.parse(folder.getID());
    }

    @Override
    protected TaskResource createResource(Task object, WebdavPath url) throws OXException {
        return new TaskResource(caldavFactory, this, object, url);
    }

    @Override
    protected Task getObject(String resourceName) throws OXException {
        Collection<Task> objects = this.getObjects();
        if (null != objects && 0 < objects.size()) {
            /*
             * try filename and uid properties
             */
            for (Task t : objects) {
                if (resourceName.equals(t.getFilename()) || resourceName.equals(t.getUid())) {
                    return t;
                }
            }
            /*
             * try object id as fallback to support previous implementation
             */
            Matcher matcher = OBJECT_ID_PATTERN.matcher(resourceName);
            if (matcher.find()) {
                try {
                    int objectID = Integer.parseInt(matcher.group(0));
                    for (Task t : objects) {
                        if (objectID == t.getObjectID()) {
                            return t;
                        }
                    }
                } catch (NumberFormatException e) {
                    // not an ID
                }
            }
        }
        return null;
    }

    @Override
    protected Collection<Task> getObjects() throws OXException {
        return filter(caldavFactory.getTaskInterface().getTaskList(folderID, 0, -1, 0, Order.NO_ORDER, BASIC_COLUMNS));
    }

    @Override
    protected Collection<Task> getObjects(Date rangeStart, Date rangeEnd) throws OXException {
        List<Task> tasks = new ArrayList<Task>();
        for (Task task : this.getObjects()) {
            if (isSupported(task) && isInInterval(task, rangeStart, rangeEnd)) {
                tasks.add(task);
            }
        }
        return tasks;
    }

    @Override
    protected WebdavPath constructPathForChildResource(Task object) {
        String fileName = object.getFilename();
        if (null == fileName || 0 == fileName.length()) {
            fileName = object.getUid();
        }
        String fileExtension = getFileExtension().toLowerCase();
        if (false == fileExtension.startsWith(".")) {
            fileExtension = "." + fileExtension;
        }
        return constructPathForChildResource(fileName + fileExtension);
    }

    @Override
    public String getSyncToken() throws WebdavProtocolException {
        Date lastModified = folder.getLastModifiedUTC(); 
        try {
            /*
             * new and modified objects
             */
            for (Task object : getModifiedObjects(lastModified)) {
                lastModified = Tools.getLatestModified(lastModified, object);
            }
            /*
             * deleted objects
             */
            for (Task object : getDeletedObjects(lastModified)) {
                lastModified = Tools.getLatestModified(lastModified, object);
            }
        } catch (OXException e) {
            throw protocolException(getUrl(), e);
        }
        return null == lastModified ? "0" : String.valueOf(lastModified.getTime());
    }

    @Override
    protected SyncStatus<WebdavResource> getSyncStatus(SyncToken syncToken) throws OXException {
        SyncStatus<WebdavResource> multistatus = new SyncStatus<WebdavResource>();
        Date since = new Date(syncToken.getTimestamp());
        boolean initialSync = 0 == since.getTime();
        Date nextSyncToken = Tools.getLatestModified(since, this.folder);
        /*
         * new and modified objects
         */
        Collection<Task> modifiedObjects = this.getModifiedObjects(since);
        for (Task object : modifiedObjects) {
            // add resource to multistatus
            WebdavResource resource = createResource(object, constructPathForChildResource(object));
            int status = null != object.getCreationDate() && object.getCreationDate().after(since) ? HttpServletResponse.SC_CREATED : HttpServletResponse.SC_OK;
            multistatus.addStatus(new WebdavStatusImpl<WebdavResource>(status, resource.getUrl(), resource));
            // remember aggregated last modified for next sync token
            nextSyncToken = Tools.getLatestModified(nextSyncToken, object);
        }
        /*
         * deleted objects
         */
        Collection<Task> deletedObjects = this.getDeletedObjects(since);
        for (Task object : deletedObjects) {
            // only include objects that are not also modified (due to move operations)
            if (null != object.getUid() && false == contains(modifiedObjects, object.getUid())) {
                if (false == initialSync) {
                    // add resource to multistatus
                    WebdavResource resource = createResource(object, constructPathForChildResource(object));
                    multistatus.addStatus(new WebdavStatusImpl<WebdavResource>(HttpServletResponse.SC_NOT_FOUND, resource.getUrl(), resource));
                }
                // remember aggregated last modified for parent folder
                nextSyncToken = Tools.getLatestModified(nextSyncToken, object);
            }
        }
        /*
         * Return response with new next sync-token in response
         */
        multistatus.setToken(new SyncToken(nextSyncToken.getTime()).toString());
        return multistatus;
    }

    private Collection<Task> getModifiedObjects(Date since) throws OXException {
        return filter(caldavFactory.getTaskInterface().getModifiedTasksInFolder(folderID, BASIC_COLUMNS, since));
    }

    private Collection<Task> getDeletedObjects(Date since) throws OXException {
        return filter(caldavFactory.getTaskInterface().getDeletedTasksInFolder(folderID, BASIC_COLUMNS, since));
    }

    public Task load(Task task) throws OXException {
        return caldavFactory.getTaskInterface().getTaskById(task.getObjectID(), task.getParentFolderID());
    }

    private boolean isSupported(Task task) throws WebdavProtocolException {
        return null != task && null == task.getParticipants() && task.getRecurrenceType() == CalendarObject.NO_RECURRENCE &&  isInInterval(task, minDateTime.getMinDateTime(), maxDateTime.getMaxDateTime());
    }

    private static <T extends CalendarObject> boolean isInInterval(T object, Date intervalStart, Date intervalEnd) {
        if (null == object) {
            return false;
        }
        if (null != intervalStart && null != object.getEndDate() && object.getEndDate().before(intervalStart)) {
            return false;
        }
        if (null != intervalEnd && null != object.getStartDate() && object.getStartDate().after(intervalEnd)) {
            return false;
        }
        return true;
    }

    private List<Task> filter(SearchIterator<Task> searchIterator) throws OXException {
        List<Task> list = new ArrayList<Task>();
        if (null != searchIterator) {
            try {
                while (searchIterator.hasNext()) {
                    Task t = searchIterator.next();
                    if (isSupported(t)) {
                        list.add(t);
                    }
                }
            } finally {
                searchIterator.close();
            }
        }
        return list;
    }

    private static <T extends CommonObject> boolean contains(Collection<T> objects, String uid) {
        for (T object : objects) {
            if (uid.equals(object.getUid())) {
                return true;
            }
        }
        return false;
    }

}
