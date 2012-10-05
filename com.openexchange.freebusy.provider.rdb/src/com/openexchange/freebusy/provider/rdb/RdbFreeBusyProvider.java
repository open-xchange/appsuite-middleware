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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.freebusy.provider.rdb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.freebusy.BusyStatus;
import com.openexchange.freebusy.FreeBusyData;
import com.openexchange.freebusy.FreeBusyExceptionCodes;
import com.openexchange.freebusy.FreeBusySlot;
import com.openexchange.freebusy.provider.FreeBusyProvider;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceService;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.user.UserService;

/**
 * {@link RdbFreeBusyProvider}
 * 
 * Provider of free/busy information.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class RdbFreeBusyProvider implements FreeBusyProvider {
    
    /**
     * Initializes a new {@link RdbFreeBusyProvider}.
     */
    public RdbFreeBusyProvider() {
        super();
    }
    
    private AppointmentSQLInterface getAppointmentSql(Session session) throws OXException {
        return RdbFreeBusyProviderLookup.getService(AppointmentSqlFactoryService.class).createAppointmentSql(session);
    }
    
    private static int getUserID(Context context, String participant) throws OXException {
        UserService userService = RdbFreeBusyProviderLookup.getService(UserService.class);
        User user = null;
        try {
            user = userService.getUser(Integer.parseInt(participant), context);
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
        ResourceService resourceService = RdbFreeBusyProviderLookup.getService(ResourceService.class);
        Resource resource = null;
        try {
            resource = resourceService.getResource(Integer.parseInt(participant), context);
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
    
    @Override
    public List<FreeBusyData> getFreeBusy(Session session, List<String> participants, Date from, Date until) {
        List<FreeBusyData> freeBusyData = new ArrayList<FreeBusyData>();
        for (String participant : participants) {
            freeBusyData.add(getFreeBusy(session, participant, from, until));            
        }
        return freeBusyData;
    }

    @Override
    public FreeBusyData getFreeBusy(Session session, String participant, Date from, Date until) {
        FreeBusyData freeBusyData = new FreeBusyData(participant, from, until);
        try {
            Context context = RdbFreeBusyProviderLookup.getService(ContextService.class).getContext(session.getContextId());
            int id = getUserID(context, participant);
            if (-1 != id) {
                fillFreeBusyData(freeBusyData, getAppointmentSql(session).getFreeBusyInformation(id, Participant.USER, from, until));
            } else {
                id = getResourceID(context, participant);
                if (-1 != id) {
                    fillFreeBusyData(freeBusyData, getAppointmentSql(session).getFreeBusyInformation(id, Participant.RESOURCE, from, until));
                } else {
                    throw FreeBusyExceptionCodes.PARTICIPANT_NOT_FOUND.create(participant);
                }
            }
        } catch (OXException error) {
            freeBusyData.setError(error);
        }
        return freeBusyData;
    }
    
    private void fillFreeBusyData(FreeBusyData freeBusyData, SearchIterator<Appointment> freeBusyInformation) throws OXException {
        if (null != freeBusyInformation) {
            try {
                while (freeBusyInformation.hasNext()) {
                    freeBusyData.add(new FreeBusySlot(freeBusyInformation.next()));                    
                }
            } finally {
                if (null != freeBusyInformation) {
                    try {
                        freeBusyInformation.close();
                    } catch (OXException e) {
                        // ignore
                    }
                }
            }
        } else {
            freeBusyData.add(new FreeBusySlot(freeBusyData.getFrom(), freeBusyData.getUntil(), BusyStatus.UNKNOWN));
        }
    }
    
}
