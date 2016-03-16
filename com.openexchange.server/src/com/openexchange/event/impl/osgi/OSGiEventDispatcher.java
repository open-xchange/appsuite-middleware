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
import com.openexchange.event.impl.AppointmentEventInterface;
import com.openexchange.event.impl.AppointmentEventInterface2;
import com.openexchange.event.impl.EventDispatcher;
import com.openexchange.event.impl.TaskEventInterface;
import com.openexchange.event.impl.TaskEventInterface2;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.Appointment;
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

    private final Queue<AppointmentEventInterface> appointmentListeners;

    private final Queue<TaskEventInterface> taskListeners;

    private volatile ServiceRegistration<EventHandler> serviceRegistration;

    /**
     * Initializes a new {@link OSGiEventDispatcher}.
     */
    public OSGiEventDispatcher() {
        super();
        appointmentListeners = new ConcurrentLinkedQueue<AppointmentEventInterface>();
        taskListeners = new ConcurrentLinkedQueue<TaskEventInterface>();
    }

    @Override
    public void addListener(final AppointmentEventInterface listener) {
        this.appointmentListeners.add(listener);
    }

    public void created(final Appointment appointment, final Session session) {
        for (final AppointmentEventInterface listener : appointmentListeners) {
            listener.appointmentCreated(appointment, session);
        }
    }

    public void modified(final Appointment oldAppointment, final Appointment newAppointment, final Session session) {
        for (final AppointmentEventInterface listener : appointmentListeners) {
            if (oldAppointment != null && AppointmentEventInterface2.class.isAssignableFrom(listener.getClass())) {
                ((AppointmentEventInterface2) listener).appointmentModified(oldAppointment, newAppointment, session);
            } else {
                listener.appointmentModified(newAppointment, session);
            }
        }
    }

    public void accepted(final Appointment appointment, final Session session) {
        for (final AppointmentEventInterface listener : appointmentListeners) {
            listener.appointmentAccepted(appointment, session);
        }
    }

    public void declined(final Appointment appointment, final Session session) {
        for (final AppointmentEventInterface listener : appointmentListeners) {
            listener.appointmentDeclined(appointment, session);
        }
    }

    public void tentativelyAccepted(final Appointment appointment, final Session session) {
        for (final AppointmentEventInterface listener : appointmentListeners) {
            listener.appointmentTentativelyAccepted(appointment, session);
        }
    }

    public void waiting(final Appointment appointment, final Session session) {
        for (final AppointmentEventInterface listener : appointmentListeners) {
            listener.appointmentWaiting(appointment, session);
        }
    }

    public void deleted(final Appointment appointment, final Session session) {
        for (final AppointmentEventInterface listener : appointmentListeners) {
            listener.appointmentDeleted(appointment, session);
        }
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
                if (Types.APPOINTMENT == module) {
                    if (CommonEvent.INSERT == action) {
                        created((Appointment) actionObj, session);
                    } else if (CommonEvent.UPDATE == action || CommonEvent.MOVE == action) {
                        modified((Appointment) oldObj, (Appointment) actionObj, session);
                    } else if (CommonEvent.DELETE == action) {
                        deleted((Appointment) actionObj, session);
                    } else if (CommonEvent.CONFIRM_ACCEPTED == action) {
                        accepted((Appointment) actionObj, session);
                    } else if (CommonEvent.CONFIRM_DECLINED == action) {
                        declined((Appointment) actionObj, session);
                    } else if (CommonEvent.CONFIRM_TENTATIVE == action) {
                        tentativelyAccepted((Appointment) actionObj, session);
                    } else if (CommonEvent.CONFIRM_WAITING == action) {
                        waiting((Appointment) actionObj, session);
                    }
                } else if (Types.TASK == module) {
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
        } catch (final Exception e) {
            // Catch all exceptions to get them into the normal logging
            // mechanism.
            LOG.error("", e);
        }
    }

    @Override
    public void registerService(final BundleContext context) {
        final Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
        serviceProperties.put(EventConstants.EVENT_TOPIC, new String[]
            { "com/openexchange/groupware/appointment/*", "com/openexchange/groupware/task/*" });
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
