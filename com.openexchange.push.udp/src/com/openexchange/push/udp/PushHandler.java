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

package com.openexchange.push.udp;

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
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.push.udp.registry.PushServiceRegistry;

/**
 * {@link PushHandler} - The push {@link EventHandler event handler}.
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class PushHandler implements EventHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PushHandler.class);

    public PushHandler() {
        super();
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
            final ContextService contextService = PushServiceRegistry.getServiceRegistry().getService(ContextService.class);
            ctx = contextService.getContext(contextId);
        } catch (OXException exc) {
            LOG.error("cannot resolve context id: {}", Integer.valueOf(contextId), exc);
            return;
        }

        final int module = event.getModule();

        Map<Integer, Set<Integer>> affectedUsersWithFolder = event.getAffectedUsersWithFolder();
        if (null == affectedUsersWithFolder && module != Types.EMAIL) {
            LOG.info("No folder information available in event {}, skipping.", event);
            return;
        }

        switch (module) {
        case Types.APPOINTMENT:
                for (final Entry<Integer, Set<Integer>> entry : transform(affectedUsersWithFolder).entrySet()) {
                event(i(entry.getKey()), I2i(entry.getValue()), module, ctx, getTimestamp(castTo(event.getActionObj(), com.openexchange.chronos.Event.class)));
            }
            break;
        case Types.TASK:
        case Types.CONTACT:
        case Types.FOLDER:
                for (final Entry<Integer, Set<Integer>> entry : transform(affectedUsersWithFolder).entrySet()) {
                event(i(entry.getKey()), I2i(entry.getValue()), module, ctx, getTimestamp(castTo(event.getActionObj(), DataObject.class)));
            }
            break;
        case Types.EMAIL:
            event(1, new int[] { event.getUserId() }, module, ctx, 0);
            break;
        case Types.INFOSTORE:
                for (final Entry<Integer, Set<Integer>> entry : transform(affectedUsersWithFolder).entrySet()) {
                event(i(entry.getKey()), I2i(entry.getValue()), module, ctx, getTimestamp((castTo(event.getActionObj(), DocumentMetadata.class))));
            }
            break;
        default:
            LOG.warn("Got event with unimplemented module: {}", Integer.valueOf(module));
        }
    }

    private static void event(final int folderId, final int[] users, final int module, final Context ctx, final long timestamp) {
        if (users == null) {
            return;
        }
        try {
            PushOutputQueue.add(new PushObject(folderId, module, ctx.getContextId(), users, false, timestamp));
        } catch (OXException e) {
            LOG.error("", e);
        } catch (Throwable t) {
            LOG.error("", t);
        }
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

    private static long getTimestamp(final DocumentMetadata meta) {
        return null == meta ? 0 : getTimestamp(meta.getLastModified());
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

    private static <T> T castTo(Object actionObject, Class<T> clazz) {
        if (null != actionObject && clazz.isAssignableFrom(actionObject.getClass())) {
            return clazz.cast(actionObject);
        }
        LOG.debug("Couldn't cast object {} to desired class {}.", actionObject == null ? "null" : actionObject.getClass().getName(), clazz.getName());
        return null;
    }
}
