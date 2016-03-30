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

package com.openexchange.freebusy.provider.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.freebusy.BusyStatus;
import com.openexchange.freebusy.FreeBusyData;
import com.openexchange.freebusy.FreeBusyExceptionCodes;
import com.openexchange.freebusy.FreeBusyInterval;
import com.openexchange.freebusy.provider.InternalFreeBusyProvider;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceService;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.user.UserService;

/**
 * {@link InternalFreeBusyProviderImpl}
 *
 * Provider of free/busy information.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class InternalFreeBusyProviderImpl implements InternalFreeBusyProvider {

    /**
     * Initializes a new {@link InternalFreeBusyProviderImpl}.
     */
    public InternalFreeBusyProviderImpl() {
        super();
    }

    private AppointmentSQLInterface getAppointmentSql(Session session) throws OXException {
        return InternalFreeBusyProviderLookup.getService(AppointmentSqlFactoryService.class).createAppointmentSql(session);
    }

    @Override
    public FreeBusyData getUserFreeBusy(Session session, int userID, Date from, Date until) {
        FreeBusyData freeBusyData = new FreeBusyData(String.valueOf(userID), from, until);
        try {
            fillFreeBusyData(freeBusyData, getAppointmentSql(session).getFreeBusyInformation(userID, Participant.USER, from, until));
        } catch (OXException e) {
            freeBusyData.addWarning(e);
        }
        return freeBusyData;
    }

    @Override
    public FreeBusyData getResourceFreeBusy(Session session, int resourceID, Date from, Date until) {
        FreeBusyData freeBusyData = new FreeBusyData(String.valueOf(resourceID), from, until);
        try {
            fillFreeBusyData(freeBusyData, getAppointmentSql(session).getFreeBusyInformation(
                resourceID, Participant.RESOURCE, from, until));
        } catch (OXException e) {
            freeBusyData.addWarning(e);
        }
        return freeBusyData;
    }

    @Override
    public Map<String, FreeBusyData> getGroupFreeBusy(Session session, int groupID, Date from, Date until) {
        Map<String, FreeBusyData> freeBusyInformation = new HashMap<String, FreeBusyData>();
        FreeBusyData groupData = new FreeBusyData(String.valueOf(groupID), from, until);
        freeBusyInformation.put(String.valueOf(groupID), groupData);
        try {
            Context context = InternalFreeBusyProviderLookup.getService(ContextService.class).getContext(session.getContextId());
            Group group = InternalFreeBusyProviderLookup.getService(GroupService.class).getGroup(context, groupID);
            if (null == group) {
                throw FreeBusyExceptionCodes.PARTICIPANT_NOT_FOUND.create(String.valueOf(groupID));
            }
            int[] groupMembers = group.getMember();
            if (null != groupMembers && 0 < groupMembers.length) {
                AppointmentSQLInterface appointmentSql = getAppointmentSql(session);
                for (int member : groupMembers) {
                    FreeBusyData memberData = new FreeBusyData(String.valueOf(member), from, until);
                    freeBusyInformation.put(String.valueOf(member), memberData);
                    fillFreeBusyData(memberData, appointmentSql.getFreeBusyInformation(member, Participant.USER, from, until));
                    groupData.add(memberData);
                }
            }
        } catch (OXException e) {
            groupData.addWarning(e);
        }
        return freeBusyInformation;
    }

    @Override
    public Map<String, FreeBusyData> getFreeBusy(Session session, List<String> participants, Date from, Date until) {
        Map<String, FreeBusyData> freeBusyInformation = new HashMap<String, FreeBusyData>();
        for (String participant : participants) {
            FreeBusyData freeBusyData = freeBusyInformation.get(participant);
            if (null == freeBusyData) {
                freeBusyData = new FreeBusyData(participant, from, until);
                freeBusyInformation.put(participant, freeBusyData);
            }
            try {
                Context context = InternalFreeBusyProviderLookup.getService(ContextService.class).getContext(session.getContextId());
                int userID = getUserID(context, participant);
                if (-1 != userID) {
                    fillFreeBusyData(freeBusyData, getAppointmentSql(session).getFreeBusyInformation(
                        userID, Participant.USER, from, until));
                } else {
                    int resourceID = getResourceID(context, participant);
                    if (-1 != resourceID) {
                        fillFreeBusyData(freeBusyData, getAppointmentSql(session).getFreeBusyInformation(
                            resourceID, Participant.RESOURCE, from, until));
                    } else {
                        int[] groupMembers = getGroupMembers(context, participant);
                        if (null != groupMembers) {
                            List<String> memberIDs = new ArrayList<String>();
                            for (int member : groupMembers) {
                                memberIDs.add(Integer.toString(member));
                            }
                            Map<String, FreeBusyData> memberInformation = this.getFreeBusy(session, memberIDs, from, until);
                            for (Entry<String, FreeBusyData> entry : memberInformation.entrySet()) {
                                FreeBusyData memberData = freeBusyInformation.get(entry.getKey());
                                if (null == memberData) {
                                    freeBusyInformation.put(entry.getKey(), entry.getValue());
                                } else {
                                    memberData.add(entry.getValue());
                                }
                                freeBusyData.add(entry.getValue());
                            }
                        } else {
                            throw FreeBusyExceptionCodes.PARTICIPANT_NOT_FOUND.create(participant);
                        }
                    }
                }
            } catch (OXException e) {
                freeBusyData.addWarning(e);
            }
        }
        return freeBusyInformation;
    }

    private void fillFreeBusyData(FreeBusyData freeBusyData, SearchIterator<Appointment> freeBusyInformation) throws OXException {
        if (null != freeBusyInformation) {
            try {
                while (freeBusyInformation.hasNext()) {
                    freeBusyData.add(getFreeBusyInterval(freeBusyInformation.next()));
                }
            } finally {
                SearchIterators.close(freeBusyInformation);
            }
        } else {
            freeBusyData.add(new FreeBusyInterval(freeBusyData.getFrom(), freeBusyData.getUntil(), BusyStatus.UNKNOWN));
        }
    }

    /**
     * Creates new {@link FreeBusyInterval}, based on the supplied appointment.
     *
     * @param appointment The appointment to create the free/busy slot for
     * @return The free/busy interval
     */
    private static FreeBusyInterval getFreeBusyInterval(Appointment appointment) {
        FreeBusyInterval freeBusyInterval = new FreeBusyInterval(appointment.getStartDate(),
            appointment.getEndDate(), BusyStatus.valueOf(appointment.getShownAs()));
        freeBusyInterval.setFullTime(appointment.getFullTime());
        if (appointment.containsObjectID() && 0 < appointment.getObjectID()) {
            freeBusyInterval.setObjectID(String.valueOf(appointment.getObjectID()));
        }
        if (appointment.containsParentFolderID() && 0 < appointment.getParentFolderID()) {
            freeBusyInterval.setFolderID(String.valueOf(appointment.getParentFolderID()));
        }
        if (appointment.containsTitle()){
            freeBusyInterval.setTitle(appointment.getTitle());
        }
        if (appointment.containsLocation()){
            freeBusyInterval.setLocation(appointment.getLocation());
        }
        return freeBusyInterval;
    }

    private static int[] getGroupMembers(Context context, String participant) throws OXException {
        GroupService groupService = InternalFreeBusyProviderLookup.getService(GroupService.class);
        Group group = null;
        try {
            group = groupService.getGroup(context, Integer.parseInt(participant));
        } catch (OXException e) {
            if ("GRP-0017".equals(e.getErrorCode())) { // 'Cannot find group with identifier ... n context ....'
                return null;
            } else {
                throw e;
            }
        } catch (NumberFormatException e) {
            // no group
        }
        return null != group ? group.getMember() : null;
    }

    private static int getUserID(Context context, String participant) throws OXException {
        UserService userService = InternalFreeBusyProviderLookup.getService(UserService.class);
        User user = null;
        try {
            user = userService.getUser(Integer.parseInt(participant), context);
        } catch (OXException e) {
            if ("USR-0010".equals(e.getErrorCode())) { // 'Cannot find user with identifier ... in context ....'
                return -1;
            } else {
                throw e;
            }
        } catch (NumberFormatException e) {
            try {
                user = userService.searchUser(participant, context);
            } catch (OXException x) {
                // re-throw if not "Cannot find user with E-Mail abc"
                if (false == "USR-0014".equals(x.getErrorCode())) {
                    throw x;
                }
            }
        }
        return null != user ? user.getId() : -1;
    }

    private static int getResourceID(Context context, String participant) throws OXException {
        ResourceService resourceService = InternalFreeBusyProviderLookup.getService(ResourceService.class);
        Resource resource = null;
        try {
            resource = resourceService.getResource(Integer.parseInt(participant), context);
        } catch (OXException e) {
            if ("RES-0012".equals(e.getErrorCode())) { // 'Cannot find resource with identifier ....'
                return -1;
            } else {
                throw e;
            }
        } catch (NumberFormatException e) {
            Resource[] resources = resourceService.searchResourcesByMail(participant, context);
            if (null != resources && 0 < resources.length) {
                if (1 < resources.length) {
                    throw FreeBusyExceptionCodes.AMBIGUOUS_PARTICIPANT.create(participant);
                }
                resource = resources[0];
            }
        }
        return null != resource ? resource.getIdentifier() : -1;
    }

}
