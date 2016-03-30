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

package com.openexchange.webdav.xml;

import java.io.OutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import com.openexchange.api2.FolderSQLInterface;
import com.openexchange.api2.RdbFolderSQLInterface;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tools.iterator.FolderObjectIterator;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.webdav.WebdavExceptionCode;

/**
 * {@link FolderWriter}
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FolderWriter extends FolderChildWriter {

    protected static final String PRIVATE_STRING = "private";

    protected static final String PUBLIC_STRING = "public";

    protected static final String SHARED_STRING = "shared";

    protected int counter;

    protected int userId = -1;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FolderWriter.class);

    public FolderWriter(final int userId) {
        this.userId = userId;
    }

    public FolderWriter(final Session sessionObj, final Context ctx) {
        this.sessionObj = sessionObj;
        this.ctx = ctx;
        userId = sessionObj.getUserId();
    }

    public void startWriter(final int objectId, final OutputStream os) throws Exception {
        final FolderSQLInterface sqlinterface = new RdbFolderSQLInterface(ServerSessionAdapter.valueOf(sessionObj));

        final Element eProp = new Element("prop", "D", "DAV:");
        final XMLOutputter xo = new XMLOutputter();
        try {
            final FolderObject folderObj = sqlinterface.getFolderById(objectId);
            writeObject(folderObj, eProp, false, xo, os);
        } catch (final OXException exc) {
            if (exc.isGeneric(Generic.NOT_FOUND) || OXFolderExceptionCode.FOLDER_COULD_NOT_BE_LOADED.equals(exc) || OXFolderExceptionCode.NOT_EXISTS.equals(exc)) {
                writeResponseElement(eProp, 0, HttpServletResponse.SC_NOT_FOUND, XmlServlet.OBJECT_NOT_FOUND_EXCEPTION, xo, os);
            } else {
                writeResponseElement(eProp, 0, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getErrorMessage(XmlServlet.SERVER_ERROR_EXCEPTION, XmlServlet.SERVER_ERROR_STATUS), xo, os);
            }
        } catch (final Exception ex) {
            LOG.error("", ex);
            writeResponseElement(eProp, 0, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, getErrorMessage(XmlServlet.SERVER_ERROR_EXCEPTION, XmlServlet.SERVER_ERROR_STATUS), xo, os);
        }
    }

    public void startWriter(final boolean modified, final boolean deleted, final boolean bList, final Date lastsync, final OutputStream os) throws Exception {
        final ServerSession serverSession = ServerSessionAdapter.valueOf(sessionObj);
        final FolderSQLInterface sqlinterface = new RdbFolderSQLInterface(serverSession);
        final XMLOutputter xo = new XMLOutputter();
        final Date dLastSync = lastsync == null ? new Date(0) : lastsync;
        /*
         * Calculate updated and "deleted" folders
         */
        if (modified || deleted) {
            final UpdatesResult updatesResult = calculateUpdates(sqlinterface, dLastSync, !deleted, serverSession);
            /*
             * Fist send all 'deletes', than all 'modified'
             */
            if (deleted) {
                final Queue<FolderObject> deletedQueue = updatesResult.deletedQueue;
                writeIterator(new SearchIteratorAdapter<FolderObject>(deletedQueue.iterator()), true, xo, os);
            }
            if (modified) {
                final Queue<FolderObject> updatedQueue = updatesResult.updatedQueue;
                writeIterator(new SearchIteratorAdapter<FolderObject>(updatedQueue.iterator()), false, xo, os);
            }
        }

        if (bList) {
            SearchIterator<FolderObject> it = null;
            try {
                it = sqlinterface.getModifiedUserFolders(new Date(0));
                writeList(it, xo, os);
            } finally {
                if (it != null) {
                    it.close();
                }
            }
        }
    }

    public void writeIterator(final SearchIterator<FolderObject> it, final boolean delete, final XMLOutputter xo, final OutputStream os) throws Exception {
        while (it.hasNext()) {
            writeObject(it.next(), delete, xo, os);
        }
    }

    public void writeObject(final FolderObject folderobject, final boolean delete, final XMLOutputter xo, final OutputStream os) throws Exception {
        writeObject(folderobject, new Element("prop", "D", "DAV:"), delete, xo, os);
    }

    public void writeObject(final FolderObject folderobject, final Element e_prop, final boolean delete, final XMLOutputter xo, final OutputStream os) throws Exception {
        final int module = folderobject.getModule();

        if (module == FolderObject.CALENDAR || module == FolderObject.TASK || module == FolderObject.CONTACT) {
            int status = 200;
            String description = "OK";
            int object_id = 0;

            try {
                object_id = folderobject.getObjectID();
                addContent2PropElement(e_prop, folderobject, delete);
            } catch (final Exception exc) {
                LOG.error("writeObject", exc);
                status = 500;
                description = "Server Error: " + exc.getMessage();
                object_id = 0;
            }

            writeResponseElement(e_prop, object_id, status, description, xo, os);
        }
    }

    public void addContent2PropElement(final Element e_prop, final FolderObject folderobject, final boolean delete) throws Exception {
        counter++;
        if (delete) {
            addElement("object_id", folderobject.getObjectID(), e_prop);
            addElement("object_status", "DELETE", e_prop);
        } else {
            final int type = folderobject.getType();
            final int owner = folderobject.getCreator();
            final int module = folderobject.getModule();

            addElement("object_status", "CREATE", e_prop);

            String folderName = null;
            if ((folderName = folderobject.getFolderName()) != null && folderName.length() > 0) {
                addElement("title", folderobject.getFolderName(), e_prop);
            } else {
                addElement("title", "no folder name " + counter, e_prop);
            }

            addElement("owner", folderobject.getCreator(), e_prop);

            switch (module) {
            case FolderObject.CALENDAR:
                addElement("module", "calendar", e_prop);
                break;
            case FolderObject.CONTACT:
                addElement("module", "contact", e_prop);
                break;
            case FolderObject.TASK:
                addElement("module", "task", e_prop);
                break;
            default:
                throw WebdavExceptionCode.IO_ERROR.create("invalid module");
            }

            if (type == FolderObject.PRIVATE) {
                if (owner == userId) {
                    addElement("type", PRIVATE_STRING, e_prop);
                } else {
                    addElement("type", SHARED_STRING, e_prop);
                    folderobject.setParentFolderID(3);
                }
            } else {
                addElement("type", PUBLIC_STRING, e_prop);
            }

            addElement("defaultfolder", folderobject.isDefaultFolder(), e_prop);

            writeFolderChildElements(folderobject, e_prop);

            addElementPermission(folderobject.getPermissions(), e_prop);
        }
    }

    public static void addElementPermission(final List<OCLPermission> permissions, final Element e_prop) throws Exception {
        final Element e_permissions = new Element("permissions", XmlServlet.PREFIX, XmlServlet.NAMESPACE);

        if (permissions != null) {
            for (int a = 0; a < permissions.size(); a++) {
                final OCLPermission oclp = permissions.get(a);
                final int entity = oclp.getEntity();
                final int fp = oclp.getFolderPermission();
                final int orp = oclp.getReadPermission();
                final int owp = oclp.getWritePermission();
                final int odp = oclp.getDeletePermission();

                if (oclp.isGroupPermission()) {
                    addElementGroup(e_permissions, entity, fp, orp, owp, odp, oclp.isFolderAdmin());
                } else {
                    addElementUser(e_permissions, entity, fp, orp, owp, odp, oclp.isFolderAdmin());
                }
            }
        }

        e_prop.addContent(e_permissions);
    }

    protected static void addElementUser(final Element e_permissions, final int entity, final int fp, final int orp, final int owp, final int odp, final boolean adminFlag) throws Exception {
        final Element e = new Element("user", namespace);
        addAttributes(e, fp, orp, owp, odp, adminFlag);
        e.addContent(Integer.toString(entity));
        e_permissions.addContent(e);
    }

    protected static void addElementGroup(final Element e_permissions, final int entity, final int fp, final int orp, final int owp, final int odp, final boolean adminFlag) throws Exception {
        final Element e = new Element("group", namespace);
        addAttributes(e, fp, orp, owp, odp, adminFlag);
        e.addContent(Integer.toString(entity));
        e_permissions.addContent(e);
    }

    protected static void addAttributes(final Element e, final int fp, final int orp, final int owp, final int odp, final boolean adminFlag) throws Exception {
        e.setAttribute("folderpermission", Integer.toString(fp), namespace);
        e.setAttribute("objectreadpermission", Integer.toString(orp), namespace);
        e.setAttribute("objectwritepermission", Integer.toString(owp), namespace);
        e.setAttribute("objectdeletepermission", Integer.toString(odp), namespace);
        e.setAttribute("admin_flag", Boolean.toString(adminFlag), namespace);
    }

    /*-
     * ----------------------------- Stuff for calculating deleted/updated folders -----------------------------
     */

    private static final class UpdatesResult {

        public final Queue<FolderObject> updatedQueue;

        public final Queue<FolderObject> deletedQueue;

        public UpdatesResult(final Queue<FolderObject> updatedQueue, final Queue<FolderObject> deletedQueue) {
            super();
            this.updatedQueue = updatedQueue;
            this.deletedQueue = deletedQueue;
        }

    }

    private static interface Enqueuer {
        public void enqueue(FolderObject fo) throws OXException;
    } // End of Enqueuer interface

    private static class FullEnqueuer implements Enqueuer {

        protected final int userId;
        protected final UserPermissionBits userPermissionBits;
        protected final Queue<FolderObject> updatedQueue;
        protected final Queue<FolderObject> deletedQueue;

        public FullEnqueuer(final Queue<FolderObject> updatedQueue, final Queue<FolderObject> deletedQueue, final UserPermissionBits userPermissionBits) {
            super();
            this.updatedQueue = updatedQueue;
            this.deletedQueue = deletedQueue;
            this.userId = userPermissionBits.getUserId();
            this.userPermissionBits = userPermissionBits;
        }

        @Override
        public void enqueue(final FolderObject fo) throws OXException {
            try {
                if (fo.isVisible(userId, userPermissionBits)) {
                    updatedQueue.add(fo);
                } else {
                    deletedQueue.add(fo);
                }
            } catch (final RuntimeException e) {
                throw OXFolderExceptionCode.RUNTIME_ERROR.create(e, e.getMessage());
            }
        }

    } // End of FullEnqueuer class

    private static final class NoSharedAccessEnqueuer extends FullEnqueuer {

        public NoSharedAccessEnqueuer(final Queue<FolderObject> updatedQueue, final Queue<FolderObject> deletedQueue, final UserPermissionBits userPermissionBits) {
            super(updatedQueue, deletedQueue, userPermissionBits);
        }

        @Override
        public void enqueue(final FolderObject fo) throws OXException {
            try {
                if (fo.isVisible(userId, userPermissionBits)) {
                    if (fo.isShared(userId)) {
                        /*
                         * No shared folder access: Enqueue to deleted queue
                         */
                        deletedQueue.add(fo);
                    } else {
                        updatedQueue.add(fo);
                    }
                } else {
                    deletedQueue.add(fo);
                }
            } catch (final RuntimeException e) {
                throw OXFolderExceptionCode.RUNTIME_ERROR.create(e, e.getMessage());
            }
        }

    } // End of NoSharedAccessEnqueuer class

    private static final class IgnoreDeletedFullEnqueuer extends FullEnqueuer {

        public IgnoreDeletedFullEnqueuer(final Queue<FolderObject> updatedQueue, final UserPermissionBits userPermissionBits) {
            super(updatedQueue, null, userPermissionBits);
        }

        @Override
        public void enqueue(final FolderObject fo) throws OXException {
            try {
                if (fo.isVisible(userId, userPermissionBits)) {
                    updatedQueue.add(fo);
                }
            } catch (final RuntimeException e) {
                throw OXFolderExceptionCode.RUNTIME_ERROR.create(e, e.getMessage());
            }
        }

    } // End of IgnoreDeletedFullEnqueuer class

    private static final class IgnoreDeletedNoSharedAccessEnqueuer extends FullEnqueuer {

        public IgnoreDeletedNoSharedAccessEnqueuer(final Queue<FolderObject> updatedQueue, final UserPermissionBits userPermissionBits) {
            super(updatedQueue, null, userPermissionBits);
        }

        @Override
        public void enqueue(final FolderObject fo) throws OXException {
            try {
                if (!fo.isShared(userId) && fo.isVisible(userId, userPermissionBits)) {
                    /*
                     * A visible, non-shared folder
                     */
                    updatedQueue.add(fo);
                }
            } catch (final RuntimeException e) {
                throw OXFolderExceptionCode.RUNTIME_ERROR.create(e, e.getMessage());
            }
        }

    } // End of IgnoreDeletedNoSharedAccessEnqueuer class

    private static Enqueuer getEnqueuer(final boolean ignoreDeleted, final UserPermissionBits userPermissionBits, final Queue<FolderObject> updatedQueue, final Queue<FolderObject> deletedQueue) {
        if (ignoreDeleted) {
            if (userPermissionBits.hasFullSharedFolderAccess()) {
                return new IgnoreDeletedFullEnqueuer(updatedQueue, userPermissionBits);
            }
            return new IgnoreDeletedNoSharedAccessEnqueuer(updatedQueue, userPermissionBits);
        }
        if (userPermissionBits.hasFullSharedFolderAccess()) {
            return new FullEnqueuer(updatedQueue, deletedQueue, userPermissionBits);
        }
        return new NoSharedAccessEnqueuer(updatedQueue, deletedQueue, userPermissionBits);
    }

    private static UpdatesResult calculateUpdates(final FolderSQLInterface sqlInterface, final Date timestamp, final boolean ignoreDeleted, final ServerSession session) throws OXException {
        /*
         * Get all updated folders
         */
        Queue<FolderObject> queue = ((FolderObjectIterator) sqlInterface.getAllModifiedFolders(timestamp)).asQueue();
        final Queue<FolderObject> updatedQueue = new LinkedList<FolderObject>();
        final Queue<FolderObject> deletedQueue = ignoreDeleted ? null : new LinkedList<FolderObject>();
        /*
         * Enqueue each folder in proper queue
         */
        final Enqueuer enqueuer = getEnqueuer(ignoreDeleted, session.getUserPermissionBits(), updatedQueue, deletedQueue);
        for (final FolderObject fo : queue) {
            enqueuer.enqueue(fo);
        }
        /*
         * Prepare result
         */
        if (ignoreDeleted) {
            /*
             * Return without deleted folders
             */
            return new UpdatesResult(updatedQueue, null);
        }
        /*
         * Get all deleted folders
         */
        queue = ((FolderObjectIterator) sqlInterface.getDeletedFolders(timestamp)).asQueue();
        /*
         * Add deleted folders from above
         */
        queue.addAll(deletedQueue);
        /*
         * Return with deleted folders
         */
        return new UpdatesResult(updatedQueue, queue);
    }

}
