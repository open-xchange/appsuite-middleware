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

package com.openexchange.event.impl.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.event.CommonEvent;
import com.openexchange.event.impl.EventDispatcher;
import com.openexchange.event.impl.TaskEventInterface;
import com.openexchange.event.impl.TaskEventInterface2;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.session.Session;

/**
 * Grabs events from the OSGi Event Admin and disseminates them to server listeners. Only handles appointments, and has to be extended once
 * needed.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class OSGiEventDispatcher implements EventHandlerRegistration, EventDispatcher {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OSGiEventDispatcher.class);

    private final Queue<TaskEventInterface> taskListeners;

    private volatile ServiceRegistration<EventHandler> serviceRegistration;

    /**
     * Initializes a new {@link OSGiEventDispatcher}.
     */
    public OSGiEventDispatcher() {
        super();
        taskListeners = new ConcurrentLinkedQueue<TaskEventInterface>();
    }

    @Override
    public void addListener(final TaskEventInterface listener) {
        this.taskListeners.add(listener);
    }

    public void created(final Task task, final Session session) {
        for (final TaskEventInterface listener : taskListeners) {
            listener.taskCreated(task, session);
        }
    }

    public void modified(final Task oldTask, final Task newTask, final Session session) {
        for (final TaskEventInterface listener : taskListeners) {
            if (oldTask != null && TaskEventInterface2.class.isAssignableFrom(listener.getClass())) {
                ((TaskEventInterface2) listener).taskModified(oldTask, newTask, session);
            } else {
                listener.taskModified(newTask, session);
            }
        }
    }

    public void accepted(final Task oldTask, final Task task, final Session session) {
        for (final TaskEventInterface listener : taskListeners) {
            if (TaskEventInterface2.class.isAssignableFrom(listener.getClass())) {
                ((TaskEventInterface2) listener).taskAccepted(oldTask, task, session);
            }
        }
    }

    public void declined(final Task oldTask, final Task task, final Session session) {
        for (final TaskEventInterface listener : taskListeners) {
            if (TaskEventInterface2.class.isAssignableFrom(listener.getClass())) {
                ((TaskEventInterface2) listener).taskDeclined(oldTask, task, session);
            }
        }
    }

    public void tentativelyAccepted(final Task oldTask, final Task task, final Session session) {
        for (final TaskEventInterface listener : taskListeners) {
            if (TaskEventInterface2.class.isAssignableFrom(listener.getClass())) {
                ((TaskEventInterface2) listener).taskTentativelyAccepted(oldTask, task, session);
            }
        }
    }

    public void deleted(final Task task, final Session session) {
        for (final TaskEventInterface listener : taskListeners) {
            listener.taskDeleted(task, session);
        }
    }

    @Override
    public void handleEvent(final Event event) {
        try {
            final CommonEvent commonEvent = (CommonEvent) event.getProperty(CommonEvent.EVENT_KEY);
            if (commonEvent != null) {
                final Object actionObj = commonEvent.getActionObj();
                final Object oldObj = commonEvent.getOldObj();
                final Session session = commonEvent.getSession();

                final int module = commonEvent.getModule();
                final int action = commonEvent.getAction();
                if (Types.TASK == module) {
                    if (CommonEvent.INSERT == action) {
                        created((Task) actionObj, session);
                    } else if (CommonEvent.UPDATE == action || CommonEvent.MOVE == action) {
                        modified((Task) oldObj, (Task) actionObj, session);
                    } else if (CommonEvent.DELETE == action) {
                        deleted((Task) actionObj, session);
                    } else if (CommonEvent.CONFIRM_ACCEPTED == action) {
                        accepted((Task) oldObj, (Task) actionObj, session);
                    } else if (CommonEvent.CONFIRM_DECLINED == action) {
                        declined((Task) oldObj, (Task) actionObj, session);
                    } else if (CommonEvent.CONFIRM_TENTATIVE == action) {
                        tentativelyAccepted((Task) oldObj, (Task) actionObj, session);
                    }
                }
            }
        } catch (Exception e) {
            // Catch all exceptions to get them into the normal logging
            // mechanism.
            LOG.error("", e);
        }
    }

    @Override
    public void registerService(final BundleContext context) {
        final Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
        serviceProperties.put(EventConstants.EVENT_TOPIC, new String[]
            { "com/openexchange/groupware/task/*" });
        serviceRegistration = context.registerService(EventHandler.class, this, serviceProperties);
    }

    @Override
    public void unregisterService() {
        final ServiceRegistration<EventHandler> serviceRegistration = this.serviceRegistration;
        if (null != serviceRegistration) {
            serviceRegistration.unregister();
            this.serviceRegistration = null;
        }
    }
}
