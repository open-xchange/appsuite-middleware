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

package com.openexchange.fitnesse.calendar;

import java.util.LinkedList;
import java.util.List;
import com.openexchange.ajax.kata.appointments.ParticipantComparisonFailure;
import com.openexchange.ajax.kata.appointments.UserParticipantComparisonFailure;
import com.openexchange.fitnesse.AbstractStepFixture;
import com.openexchange.fitnesse.environment.PrincipalResolver;
import com.openexchange.fitnesse.exceptions.FitnesseException;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.test.fixtures.Fixture;

/**
 * 
 * {@link AbstractCalendarFixture}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 * @param <T>
 */
public abstract class AbstractCalendarFixture<T extends CalendarObject> extends AbstractStepFixture {

    public AbstractCalendarFixture() {
        super();
    }

    protected void resolveUserParticipants(Fixture<T> entry, String userParticipants) throws FitnesseException {
        if(userParticipants == null)
            return;
        String[] participantsList = userParticipants.split("\\s*,\\s*");
        PrincipalResolver resolver = new PrincipalResolver(environment.getClient());
        
        List<UserParticipant> users = new LinkedList<UserParticipant>();
        for (String participant : participantsList) {
            try {
                UserParticipant resolvedParticipant = (UserParticipant) resolver.resolveEntity(participant);
                users.add(resolvedParticipant);
            } catch(ClassCastException e){
                throw new FitnesseException("Could not find an existing user with the name: "+participant);
            }
        }
        entry.getEntry().setUsers(users);
    }

    protected void resolveParticipants(Fixture<T> entry, String participants) throws FitnesseException {
        if(participants == null)
            return;
        String[] participantsList = participants.split("\\s*,\\s*");
        PrincipalResolver resolver = new PrincipalResolver(environment.getClient());
        for (String participant : participantsList) {
            Participant resolvedParticipant = resolver.resolveEntity(participant);
            entry.getEntry().addParticipant(resolvedParticipant);
        }
    
    }

    public int findFailedFieldPosition(String expectedValue, Throwable t) {
        if (UserParticipantComparisonFailure.class.isInstance(t)) {
            return data.getHeader().indexOf("users");
        }
        if (ParticipantComparisonFailure.class.isInstance(t)) {
            return data.getHeader().indexOf("participants");
        }
        return super.findFailedFieldPosition(expectedValue, t);
    }

}
