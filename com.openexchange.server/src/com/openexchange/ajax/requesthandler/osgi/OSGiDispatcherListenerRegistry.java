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

package com.openexchange.ajax.requesthandler.osgi;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.ActionBoundDispatcherListener;
import com.openexchange.ajax.requesthandler.DispatcherListener;
import com.openexchange.ajax.requesthandler.DispatcherListenerRegistry;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link OSGiDispatcherListenerRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class OSGiDispatcherListenerRegistry implements DispatcherListenerRegistry, ServiceTrackerCustomizer<DispatcherListener, DispatcherListener> {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(OSGiDispatcherListenerRegistry.class);

    private final BundleContext context;
    private final AtomicBoolean anyListenerAdded;
    private final Queue<DispatcherListener> allListeners;
    private final ConcurrentMap<String, Queue<DispatcherListener>> startsWithListeners;
    private final ConcurrentMap<ActionKey, Queue<DispatcherListener>> certainListeners;

    /**
     * Initializes a new {@link OSGiDispatcherListenerRegistry}.
     */
    public OSGiDispatcherListenerRegistry(BundleContext context) {
        super();
        this.context = context;
        anyListenerAdded = new AtomicBoolean(false);
        allListeners = new ConcurrentLinkedQueue<DispatcherListener>();
        startsWithListeners = new ConcurrentHashMap<String, Queue<DispatcherListener>>(16, 0.9F, 1);
        certainListeners = new ConcurrentHashMap<ActionKey, Queue<DispatcherListener>>(16, 0.9F, 1);
    }

    @Override
    public synchronized DispatcherListener addingService(ServiceReference<DispatcherListener> reference) {
        DispatcherListener listener = context.getService(reference);
        if (add(listener)) {
            anyListenerAdded.set(true);
            return listener;
        }

        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<DispatcherListener> reference, DispatcherListener listener) {
        // Nothing
    }

    @Override
    public synchronized void removedService(ServiceReference<DispatcherListener> reference, DispatcherListener listener) {
        remove(listener);
        context.ungetService(reference);
    }

    @Override
    public List<DispatcherListener> getDispatcherListenersFor(AJAXRequestData requestData) throws OXException {
        if (false == anyListenerAdded.get()) {
            return Collections.emptyList();
        }

        List<DispatcherListener> listeners = new LinkedList<DispatcherListener>();
        String module = requestData.getNormalizedModule();

        if (Strings.isNotEmpty(module)) {
            // Grab action
            String action = requestData.getAction();
            if (Strings.isNotEmpty(action)) {
                // Add action-specific ones
                ActionKey actionKey = new ActionKey(action, module);
                Queue<DispatcherListener> q = certainListeners.get(actionKey);
                if (null != q) {
                    listeners.addAll(q);
                }
            }

            // Add module-specific ones
            Queue<DispatcherListener> q = startsWithListeners.get(module);
            if (null != q) {
                listeners.addAll(q);
            }
        }

        // Add the ones signaling applicability
        List<DispatcherListener> applicables = new LinkedList<DispatcherListener>();
        for (DispatcherListener listener : listeners) {
            if (listener.applicable(requestData)) {
                applicables.add(listener);
            }
        }
        return applicables;
    }

    private boolean add(DispatcherListener listener) {
        if (!(listener instanceof ActionBoundDispatcherListener)) {
            return allListeners.offer(listener);
        }

        // Listener is action-bound
        ActionBoundDispatcherListener actionBoundListener = (ActionBoundDispatcherListener) listener;
        String module = actionBoundListener.getModule();
        if (Strings.isEmpty(module)) {
            LOG.error("Action-bound dispatcher listener '{}' does not specify a module, but should.", listener.getClass().getName());
            return false;
        }

        Set<String> actions = actionBoundListener.getActions();
        if (null == actions || actions.isEmpty()) {
            return getQueueFor(module, startsWithListeners).offer(actionBoundListener);
        }

        List<Queue<DispatcherListener>> toRemoveFrom = new LinkedList<Queue<DispatcherListener>>();
        boolean added = true;
        try {
            for (Iterator<String> it = actions.iterator(); added && it.hasNext();) {
                Queue<DispatcherListener> q = getQueueFor(new ActionKey(it.next(), module), certainListeners);
                added = q.offer(actionBoundListener);
                if (added) {
                    toRemoveFrom.add(q);
                }
            }
            return added;
        } finally {
            if (!added) {
                // Remove from queues, to which the listener was already offered
                for (Queue<DispatcherListener> q : toRemoveFrom) {
                    q.remove(actionBoundListener);
                }
            }
        }
    }

    private boolean remove(DispatcherListener listener) {
        if (!(listener instanceof ActionBoundDispatcherListener)) {
            return allListeners.remove(listener);
        }

        // Listener is action-bound
        ActionBoundDispatcherListener actionBoundListener = (ActionBoundDispatcherListener) listener;
        String module = actionBoundListener.getModule();

        Set<String> actions = actionBoundListener.getActions();
        if (null == actions || actions.isEmpty()) {
            return getQueueFor(module, startsWithListeners).remove(actionBoundListener);
        }

        for (String action : actions) {
            getQueueFor(new ActionKey(action, module), certainListeners).remove(actionBoundListener);
        }
        return true;
    }

    private <K> Queue<DispatcherListener> getQueueFor(K key, ConcurrentMap<K, Queue<DispatcherListener>> map) {
        Queue<DispatcherListener> q = map.get(key);
        if (null == q) {
            Queue<DispatcherListener> nq = new ConcurrentLinkedQueue<DispatcherListener>();
            q = map.putIfAbsent(key, nq);
            if (null == q) {
                q = nq;
            }
        }
        return q;
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    private static final class ActionKey {

        private final String module;
        private final String action;
        private final int hash;

        ActionKey(String action, String module) {
            super();
            this.action = action;
            this.module = module;

            int prime = 31;
            int result = prime * 1 + ((action == null) ? 0 : action.hashCode());
            result = prime * result + ((module == null) ? 0 : module.hashCode());
            this.hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ActionKey)) {
                return false;
            }
            ActionKey other = (ActionKey) obj;
            if (module == null) {
                if (other.module != null) {
                    return false;
                }
            } else if (!module.equals(other.module)) {
                return false;
            }
            if (action == null) {
                if (other.action != null) {
                    return false;
                }
            } else if (!action.equals(other.action)) {
                return false;
            }
            return true;
        }
    } // End of class ActionKey

}
