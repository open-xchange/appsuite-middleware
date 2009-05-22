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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.push.udp;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.context.ContextService;
import com.openexchange.event.CommonEvent;
import com.openexchange.folder.FolderException;
import com.openexchange.folder.FolderService;
import com.openexchange.group.Group;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.push.udp.registry.PushServiceRegistry;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link PushHandler} - The push {@link EventHandler event handler}.
 * 
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */
public class PushHandler implements EventHandler {

    private GroupStorage groupStorage;

    private static final Log LOG = LogFactory.getLog(PushHandler.class);

    /**
     * Initializes a new {@link PushHandler push handler}
     */
    public PushHandler() {
        super();
    }

    public void handleEvent(final Event event) {
        final CommonEvent genericEvent;
        {
            final Object obj = event.getProperty(CommonEvent.EVENT_KEY);
            if (obj == null) {
                return;
            }
            genericEvent = (CommonEvent) obj;
        }

        final int userId = genericEvent.getUserId();
        final int contextId = genericEvent.getContextId();

        final Context ctx;
        try {
            final ContextService contextService = PushServiceRegistry.getServiceRegistry().getService(ContextService.class);
            ctx = contextService.getContext(contextId);
        } catch (final ContextException exc) {
            LOG.error("cannot resolve context id: " + contextId, exc);
            return;
        }

        final int module = genericEvent.getModule();

        final FolderObject parentFolder = (FolderObject) genericEvent.getSourceFolder();
        if (parentFolder == null && module != Types.EMAIL) {
            LOG.warn("folder object in event is null");
            return;
        }

        if (module == Types.APPOINTMENT) {
            final int[] users = getAffectedUsers4Object(parentFolder, ctx);
            final AppointmentObject appointmentObj = (AppointmentObject) genericEvent.getActionObj();
            event(
                userId,
                appointmentObj.getObjectID(),
                appointmentObj.getParentFolderID(),
                users,
                module,
                ctx,
                getTimestamp(appointmentObj.getLastModified()));
            // Check for move action
            check4MoveAction(userId, (AppointmentObject) genericEvent.getOldObj(), appointmentObj.getParentFolderID(), module, ctx);
        } else if (module == Types.TASK) {
            final int[] users = getAffectedUsers4Object(parentFolder, ctx);
            final Task taskObj = (Task) genericEvent.getActionObj();
            event(userId, taskObj.getObjectID(), taskObj.getParentFolderID(), users, module, ctx, getTimestamp(taskObj.getLastModified()));
            // Check for move action
            check4MoveAction(userId, (Task) genericEvent.getOldObj(), taskObj.getParentFolderID(), module, ctx);
        } else if (module == Types.CONTACT) {
            final int[] users = getAffectedUsers4Object(parentFolder, ctx);
            final ContactObject contactObj = (ContactObject) genericEvent.getActionObj();
            event(
                userId,
                contactObj.getObjectID(),
                contactObj.getParentFolderID(),
                users,
                module,
                ctx,
                getTimestamp(contactObj.getLastModified()));
            // Check for move action
            check4MoveAction(userId, (ContactObject) genericEvent.getOldObj(), contactObj.getParentFolderID(), module, ctx);
        } else if (module == Types.FOLDER) {
            final int[] users = getAffectedUsers4Folder(parentFolder, ctx);
            final FolderObject folderObj = (FolderObject) genericEvent.getActionObj();
            event(
                userId,
                folderObj.getObjectID(),
                folderObj.getParentFolderID(),
                users,
                module,
                ctx,
                getTimestamp(folderObj.getLastModified()));
            // Check for move action
            check4MoveAction(userId, (FolderObject) genericEvent.getOldObj(), folderObj.getParentFolderID(), module, ctx);
        } else if (module == Types.EMAIL) {
            final int[] users = new int[] { userId };
            event(userId, 1, 1, users, module, ctx, 0);
        } else if (module == Types.INFOSTORE) {
            final int[] users = getAffectedUsers4Object(parentFolder, ctx);
            final DocumentMetadata object = (DocumentMetadata) genericEvent.getActionObj();
            event(userId, object.getId(), (int) object.getFolderId(), users, module, ctx, getTimestamp(object.getLastModified()));
            // Check for move action
            check4MoveAction(userId, (DocumentMetadata) genericEvent.getOldObj(), (int) object.getFolderId(), module, ctx);
        } else {
            LOG.warn("Got event with unimplemented module: " + module);
        }
    }

    private static void event(final int userId, final int objectId, final int folderId, final int[] users, final int module, final Context ctx, final long timestamp) {
        if (users == null) {
            return;
        }

        try {
            final PushObject pushObject = new PushObject(folderId, module, ctx.getContextId(), users, false, timestamp);
            PushOutputQueue.add(pushObject);
        } catch (final Exception exc) {
            LOG.error("event", exc);
        }
    }

    private void check4MoveAction(final int userId, final FolderChildObject oldObj, final int folderId, final int module, final Context ctx) {
        if (null != oldObj && oldObj.getParentFolderID() != folderId) {
            /*
             * Obviously object was moved, therefore an event for old folder is needed, too
             */
            final FolderObject oldFolder;
            try {
                final FolderService folderService = PushServiceRegistry.getServiceRegistry().getService(FolderService.class);
                if (null == folderService) {
                    LOG.error("missing folder service", new Throwable());
                    return;
                }
                oldFolder = folderService.getFolderObject(oldObj.getParentFolderID(), ctx.getContextId());
            } catch (final FolderException e) {
                LOG.error("cannot load folder by id: " + oldObj.getParentFolderID(), e);
                return;
            }
            final int[] oldUsers = getAffectedUsers4Object(oldFolder, ctx);
            event(userId, oldObj.getObjectID(), oldObj.getParentFolderID(), oldUsers, module, ctx, oldObj.getLastModified().getTime());
        }
    }

    private void check4MoveAction(final int userId, final FolderObject oldObj, final int folderId, final int module, final Context ctx) {
        if (null != oldObj && oldObj.getParentFolderID() != folderId) {
            /*
             * Obviously object was moved, therefore an event for old folder is needed, too
             */
            final FolderObject oldFolder;
            try {
                final FolderService folderService = PushServiceRegistry.getServiceRegistry().getService(FolderService.class);
                if (null == folderService) {
                    LOG.error("missing folder service", new Throwable());
                    return;
                }
                oldFolder = folderService.getFolderObject(oldObj.getParentFolderID(), ctx.getContextId());
            } catch (final FolderException e) {
                LOG.error("cannot load folder by id: " + oldObj.getParentFolderID(), e);
                return;
            }
            final int[] oldUsers = getAffectedUsers4Folder(oldFolder, ctx);
            event(userId, oldObj.getObjectID(), oldObj.getParentFolderID(), oldUsers, module, ctx, getTimestamp(oldObj.getLastModified()));
        }
    }

    private void check4MoveAction(final int userId, final DocumentMetadata oldObj, final int folderId, final int module, final Context ctx) {
        if (null != oldObj && oldObj.getFolderId() != folderId) {
            /*
             * Obviously object was moved, therefore an event for old folder is needed, too
             */
            final FolderObject oldFolder;
            try {
                final FolderService folderService = PushServiceRegistry.getServiceRegistry().getService(FolderService.class);
                if (null == folderService) {
                    LOG.error("missing folder service", new Throwable());
                    return;
                }
                oldFolder = folderService.getFolderObject((int) oldObj.getFolderId(), ctx.getContextId());
            } catch (final FolderException e) {
                LOG.error("cannot load folder by id: " + oldObj.getFolderId(), e);
                return;
            }
            final int[] oldUsers = getAffectedUsers4Object(oldFolder, ctx);
            event(userId, oldObj.getId(), (int) oldObj.getFolderId(), oldUsers, module, ctx, getTimestamp(oldObj.getLastModified()));
        }
    }

    private static long getTimestamp(final Date lastModified) {
        return lastModified == null ? 0 : lastModified.getTime();
    }

    protected int[] getAffectedUsers4Object(final FolderObject folderObj, final Context ctx) {
        try {
            groupStorage = GroupStorage.getInstance();

            final OCLPermission[] oclp = folderObj.getPermissionsAsArray();

            final Set<Integer> hs = new HashSet<Integer>(oclp.length);

            for (int a = 0; a < oclp.length; a++) {
                final OCLPermission p = oclp[a];
                if (p.canReadOwnObjects() || p.canReadAllObjects()) {
                    if (p.isGroupPermission()) {
                        final Group g = groupStorage.getGroup(p.getEntity(), ctx);
                        addMembers(g, hs);
                    } else {
                        hs.add(Integer.valueOf(p.getEntity()));
                    }
                }
            }

            return hashSet2Array(hs);
        } catch (final Exception exc) {
            LOG.error("getAffectedUser4Object", exc);
        }

        return new int[] {};
    }

    protected int[] getAffectedUsers4Folder(final FolderObject folderObj, final Context ctx) {
        try {
            groupStorage = GroupStorage.getInstance();

            final OCLPermission[] oclp = folderObj.getPermissionsAsArray();

            final Set<Integer> hs = new HashSet<Integer>(oclp.length);

            for (int a = 0; a < oclp.length; a++) {
                final OCLPermission p = oclp[a];
                if (p.isFolderVisible()) {
                    if (p.isGroupPermission()) {
                        final Group g = groupStorage.getGroup(p.getEntity(), ctx);
                        addMembers(g, hs);
                    } else {
                        hs.add(Integer.valueOf(p.getEntity()));
                    }
                }
            }

            return hashSet2Array(hs);
        } catch (final Exception exc) {
            LOG.error("getAffectedUsers4Folder", exc);
        }

        return new int[] {};
    }

    protected void addMembers(final Group g, final Set<Integer> hs) {
        final int members[] = g.getMember();
        for (int a = 0; a < members.length; a++) {
            hs.add(Integer.valueOf(members[a]));
        }
    }

    protected int[] hashSet2Array(final Set<Integer> hs) {
        final int[] arr = new int[hs.size()];
        int counter = 0;
        for (final Integer integer : hs) {
            arr[counter++] = integer.intValue();
        }
        return arr;
    }

}
