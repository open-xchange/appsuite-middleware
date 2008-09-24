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
package com.openexchange.event.impl.osgi;

import com.openexchange.event.impl.*;
import com.openexchange.event.CommonEvent;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.tasks.Task;

import java.util.Dictionary;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;

import com.openexchange.session.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.framework.BundleContext;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */

/**
 * Grabs events from the OSGi Event Admin and disseminates them to server listeners.
 *
 * Only handles appointments, and has to be extended once needed
 */

public class OSGiEventDispatcher implements EventHandler, EventDispatcher {

    private static final Log LOG = LogFactory.getLog(OSGiEventDispatcher.class);

    private List<AppointmentEventInterface> appointmentListeners = new ArrayList<AppointmentEventInterface>();
    private List<TaskEventInterface> taskListeners = new ArrayList<TaskEventInterface>();

    public void addListener(AppointmentEventInterface listener) {
        this.appointmentListeners.add(listener);
    }

    public void created(AppointmentObject appointment, Session session) {
        for(AppointmentEventInterface listener : appointmentListeners) {
            listener.appointmentCreated(appointment, session);
        }
    }

    public void modified(AppointmentObject oldAppointment, AppointmentObject newAppointment, Session session) {
        for(AppointmentEventInterface listener : appointmentListeners) {
            if(oldAppointment != null && AppointmentEventInterface2.class.isAssignableFrom(listener.getClass())) {
                ((AppointmentEventInterface2)listener).appointmentModified(oldAppointment, newAppointment, session);   
            } else {
                listener.appointmentModified(newAppointment, session);
            }
        }
    }

    public void deleted(AppointmentObject appointment, Session session) {
        for(AppointmentEventInterface listener : appointmentListeners) {
            listener.appointmentDeleted(appointment, session);
        }
    }

    public void addListener(TaskEventInterface listener) {
        this.taskListeners.add(listener);
    }

    public void created(Task task, Session session) {
        for(TaskEventInterface listener : taskListeners) {
            listener.taskCreated(task, session);
        }
    }

    public void modified(Task oldTask, Task newTask, Session session) {
        for(TaskEventInterface listener : taskListeners) {
            if(oldTask != null && TaskEventInterface2.class.isAssignableFrom(listener.getClass())) {
                ((TaskEventInterface2)listener).taskModified(oldTask, newTask, session);
            } else {
                listener.taskModified(newTask, session);
            }
        }
    }

    public void modified(Task task, Session session) {
        modified(null, task, session);    
    }

    public void deleted(Task task, Session session) {
        for(TaskEventInterface listener : taskListeners) {
            listener.taskDeleted(task, session);
        }    
    }

    public void handleEvent(Event event) {
        try {
        CommonEvent commonEvent = (CommonEvent) event.getProperty(CommonEvent.EVENT_KEY);

        Object actionObj = commonEvent.getActionObj();
        Object oldObj = commonEvent.getOldObj();
        Session session = commonEvent.getSession();                

        switch(commonEvent.getModule()) {
            case Types.APPOINTMENT:
                switch( commonEvent.getAction() ) {
                    case CommonEvent.INSERT:
                        created((AppointmentObject) actionObj, session);
                        break;
                    case CommonEvent.UPDATE: case CommonEvent.MOVE:
                        modified((AppointmentObject) oldObj, (AppointmentObject) actionObj, session);
                        break;
                    case CommonEvent.DELETE:
                        deleted((AppointmentObject) actionObj, session);
                        break;
                }
                break;
            case Types.TASK:
                switch( commonEvent.getAction() ) {
                    case CommonEvent.INSERT:
                        created((Task) actionObj, session);
                        break;
                    case CommonEvent.UPDATE: case CommonEvent.MOVE:
                        modified((Task) oldObj, (Task) actionObj, session);
                        break;
                    case CommonEvent.DELETE:
                        deleted((Task) actionObj, session);
                        break;
                }
                break;
        }
        } catch (final Exception e) {
            // Catch all exceptions to get them into the normal logging mechanism.
            LOG.error(e.getMessage(), e);
        }
    }

    public void registerService(BundleContext context) {
        final Dictionary<Object,Object> serviceProperties = new Hashtable<Object,Object>();
        serviceProperties.put(EventConstants.EVENT_TOPIC, new String[]{"com/openexchange/groupware/*"});
        context.registerService(EventHandler.class.getName(), this, serviceProperties);
    }
}
