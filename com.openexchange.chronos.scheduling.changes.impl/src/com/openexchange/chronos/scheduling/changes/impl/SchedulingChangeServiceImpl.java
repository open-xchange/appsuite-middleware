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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.scheduling.changes.impl;

import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.CalendarObjectResource;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.scheduling.changes.Change;
import com.openexchange.chronos.scheduling.changes.ChangeAction;
import com.openexchange.chronos.scheduling.changes.ScheduleChange;
import com.openexchange.chronos.scheduling.changes.SchedulingChangeService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link SchedulingChangeService}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class SchedulingChangeServiceImpl implements SchedulingChangeService {

    /*
     * ---------------------------- TEMPLATES ----------------------------
     */
    private static final String ACCEPT = "notify.appointment.accept";
    //private static final String COUNTER_ORGANIZER = "notify.event.counter.organizer";
    private static final String COUNTER_ATTENDEE = "notify.appointment.counter.attendee";
    private static final String CREATE = "notify.appointment.create";
    private static final String CREATE_EXCEPTION = "notify.appointment.createexception";
    private static final String DECLINE = "notify.appointment.decline";
    private static final String DECLINE_COUNTER = "notify.appointment.declinecounter";
    private static final String DELETE = "notify.appointment.delete";
    private static final String NONE = "notify.appointment.none";
    //    private static final String REFRESH = "notify.event.refresh";
    private static final String TENTATIVE = "notify.appointment.tentative";
    private static final String UPDATE = "notify.appointment.update";
    /*
     * -------------------------------------------------------------------
     */

    private final ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link SchedulingChangeService}.
     * 
     * @param serviceLookup The {@link ServiceLookup}
     */
    public SchedulingChangeServiceImpl(ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
    }

    @Override
    public ScheduleChange describeCancel(CalendarUser originator, String comment, CalendarObjectResource resource) throws OXException {
        return describe(originator, comment, DELETE, null, resource, null, ChangeAction.CANCEL, Collections.emptyList());
    }

    @Override
    public ScheduleChange describeCancelInstance(CalendarUser originator, String comment, CalendarObjectResource resource, Event seriesMaster) {
        return describe(originator, comment, DELETE, null, resource, seriesMaster, ChangeAction.CANCEL, Collections.emptyList());
    }

    @Override
    public ScheduleChange describeCounter(CalendarUser originator, String comment, CalendarObjectResource resource, List<Change> changes, boolean isExceptionCreate) throws OXException {
        /*
         * Get correct template
         */
        String templateName;
        if (isExceptionCreate) {
            templateName = CREATE_EXCEPTION;
        } else {
            templateName = COUNTER_ATTENDEE;
        }

        return describe(originator, comment, templateName, null, resource, null, ChangeAction.UPDATE, changes);
    }

    @Override
    public ScheduleChange describeDeclineCounter(CalendarUser originator, String comment, CalendarObjectResource resource) throws OXException {
        return describe(originator, comment, DECLINE_COUNTER, null, resource, null, ChangeAction.CANCEL, Collections.emptyList());
    }

    @Override
    public ScheduleChange describeReply(CalendarUser originator, String comment, CalendarObjectResource resource, List<Change> changes, ParticipationStatus partStat) throws OXException {
        return describe(originator, comment, getReplyTemplateName(partStat), partStat, resource, null, ChangeAction.REPLY, changes);
    }

    @Override
    public ScheduleChange describeReplyInstance(CalendarUser originator, String comment, CalendarObjectResource resource, Event seriesMaster, List<Change> changes, ParticipationStatus partStat) {
        return describe(originator, comment, getReplyTemplateName(partStat), partStat, resource, seriesMaster, ChangeAction.REPLY, changes);
    }

    @Override
    public ScheduleChange describeCreationRequest(CalendarUser originator, String comment, CalendarObjectResource resource) throws OXException {
        return describe(originator, comment, CREATE, null, resource, null, ChangeAction.CREATE, Collections.emptyList());
    }

    @Override
    public ScheduleChange describeUpdateRequest(CalendarUser originator, String comment, CalendarObjectResource resource, List<Change> changes) throws OXException {
        return describe(originator, comment, UPDATE, null, resource, null, ChangeAction.UPDATE, changes);
    }

    @Override
    public ScheduleChange describeUpdateInstance(CalendarUser originator, String comment, CalendarObjectResource resource, Event seriesMaster, List<Change> changes) {
        return describe(originator, comment, UPDATE, null, resource, seriesMaster, ChangeAction.UPDATE, changes);
    }

    @Override
    public ScheduleChange describeNewException(CalendarUser originator, String comment, CalendarObjectResource resource, List<Change> changes) throws OXException {
        return describe(originator, comment, CREATE_EXCEPTION, null, resource, null, ChangeAction.UPDATE, changes);
    }

    private ScheduleChange describe(CalendarUser originator, String comment, String templateName, ParticipationStatus partStat, CalendarObjectResource resource, Event seriesMaster, ChangeAction action, List<Change> changes) {
        return new ScheduleChangeImpl(serviceLookup, originator, comment, templateName, partStat, resource, seriesMaster, action, changes);
    }

    private static String getReplyTemplateName(ParticipationStatus partStat) {
        if (ParticipationStatus.ACCEPTED.matches(partStat)) {
            return ACCEPT;
        }
        if (ParticipationStatus.TENTATIVE.matches(partStat)) {
            return TENTATIVE;
        }
        if (ParticipationStatus.DECLINED.matches(partStat)) {
            return DECLINE;
        }
        return NONE; // NEEDS_ACTION or unknown
    }

}
