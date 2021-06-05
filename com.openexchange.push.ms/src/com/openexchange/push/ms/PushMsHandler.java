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

package com.openexchange.push.ms;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import static com.openexchange.java.Autoboxing.i;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.context.ContextService;
import com.openexchange.event.CommonEvent;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;

/**
 * {@link PushMsHandler} - Listens for locally distributed OSGi events notifying about changes.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PushMsHandler implements EventHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PushMsHandler.class);

    private final DelayPushQueue delayPushQueue;

    /**
     * Initializes a new {@link PushMsHandler}.
     *
     * @param delayPushQueue the delayQueue that is used for pushing PIM objects
     */
    public PushMsHandler(final DelayPushQueue delayPushQueue) {
        super();
        this.delayPushQueue = delayPushQueue;
    }

    @Override
    public void handleEvent(final Event e) {
        final CommonEvent event;
        {
            final Object obj = e.getProperty(CommonEvent.EVENT_KEY);
            if (obj == null) {
                return;
            }
            try {
                event = (CommonEvent) obj;
            } catch (ClassCastException cce) {
                LOG.warn("Unexpected type", cce);
                return;
            }
        }

        final int contextId = event.getContextId();
        final Context ctx;
        try {
            final ContextService contextService = Services.getService(ContextService.class);
            ctx = contextService.getContext(contextId);
        } catch (OXException exc) {
            LOG.error("cannot resolve context id: {}", Integer.valueOf(contextId), exc);
            return;
        }

        final int module = event.getModule();

        final Object tmp = event.getSourceFolder();
        final FolderObject parentFolder;
        if (tmp instanceof FolderObject) {
            parentFolder = (FolderObject) event.getSourceFolder();
        } else {
            parentFolder = null;
        }
        if (parentFolder == null && module != Types.EMAIL && module != Types.APPOINTMENT) {
            LOG.warn("folder object in event is null");
            return;
        }
        switch (module) {
        case Types.APPOINTMENT: // new Event handle
            for (final Entry<Integer, Set<Integer>> entry : transform(event.getAffectedUsersWithFolder()).entrySet()) {
                publish(i(entry.getKey()), I2i(entry.getValue()), module, ctx, getTimestamp((com.openexchange.chronos.Event) event.getActionObj()), e, false);
            }
            break;
        case Types.TASK:
            // fall-through
        case Types.CONTACT:
            // fall-through
        case Types.FOLDER:
            for (final Entry<Integer, Set<Integer>> entry : transform(event.getAffectedUsersWithFolder()).entrySet()) {
                publish(i(entry.getKey()), I2i(entry.getValue()), module, ctx, getTimestamp((DataObject) event.getActionObj()), e, false);
            }
            break;
        case Types.EMAIL:
            publish(1, new int[] { event.getUserId() }, module, ctx, 0, e, true);
            break;
        case Types.INFOSTORE:
            for (final Entry<Integer, Set<Integer>> entry : transform(event.getAffectedUsersWithFolder()).entrySet()) {
                publish(i(entry.getKey()), I2i(entry.getValue()), module, ctx, getTimestamp(((DocumentMetadata) event.getActionObj()).getLastModified()), e, true);
            }
            break;
        default:
            LOG.warn("Got event with unimplemented module: {}", I(module));
        }
    }

    private void publish(final int folderId, final int[] users, final int module, final Context ctx, final long timestamp, final Event e, final boolean immediate) {
        if (users == null) {
            return;
        }
        delayPushQueue.add(newPushMsObject(folderId, users, module, ctx, timestamp, e), immediate);
    }

    private PushMsObject newPushMsObject(final int folderId, final int[] users, final int module, final Context ctx, final long timestamp, final Event e) {
        return new PushMsObject(folderId, module, ctx.getContextId(), users, false, timestamp, e.getTopic());
    }

    private static long getTimestamp(final Date lastModified) {
        return lastModified == null ? 0 : lastModified.getTime();
    }

    private static long getTimestamp(final DataObject object) {
        return null == object ? 0 : getTimestamp(object.getLastModified());
    }

    private static long getTimestamp(final com.openexchange.chronos.Event event) {
        return null == event ? 0 : getTimestamp(event.getLastModified());
    }

    private static final Map<Integer, Set<Integer>> transform(final Map<Integer, Set<Integer>> map) {
        final Map<Integer, Set<Integer>> retval = new HashMap<Integer, Set<Integer>>();
        for (final Entry<Integer, Set<Integer>> entry : map.entrySet()) {
            for (final Integer folderId : entry.getValue()) {
                Set<Integer> users = retval.get(folderId);
                if (null == users) {
                    users = new HashSet<Integer>();
                    retval.put(folderId, users);
                }
                users.add(entry.getKey());
            }
        }
        return retval;
    }

}
