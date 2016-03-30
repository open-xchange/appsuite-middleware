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

package com.openexchange.calendar.printing.templating;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.UserService;


/**
 * {@link EnhancedAppointment}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class EnhancedAppointment {

    private final List<SimpleParticipant> accepted = new ArrayList<SimpleParticipant>();
    private final List<SimpleParticipant> declined = new ArrayList<SimpleParticipant>();
    private final List<SimpleParticipant> tentative = new ArrayList<SimpleParticipant>();
    private final List<SimpleParticipant> undecided = new ArrayList<SimpleParticipant>();
    private final List<SimpleParticipant> resources = new ArrayList<SimpleParticipant>();

    private final ServiceLookup services;

    private final Map<String, Object> appointment;
    private final Context ctx;

    public EnhancedAppointment(Map<String, Object> appointment, ServiceLookup services, Context ctx) throws OXException {
        super();
        this.services = services;
        this.appointment = appointment;
        this.ctx = ctx;

        List<Object> users = (List<Object>) appointment.get("users");
        if (users != null) {
            for(Object o: users) {
                Map<String, Object> user = (Map<String, Object>) o;
                Integer status = (Integer) user.get("confirmation");
                getList(status).add(resolveUser(user));
            }
        }

        List<Object> confirmations = (List<Object>) appointment.get("confirmations");

        if (confirmations != null) {
            for (Object o: confirmations) {
                Map<String, Object> confirmation = (Map<String, Object>) o;
                Integer status = (Integer) confirmation.get("confirmation");
                getList(status).add(resolveConfirmation(confirmation));

            }
        }

        List<Object> participants = (List<Object>) appointment.get("participants");

        if (participants != null) {
            for(Object o: participants) {
                Map<String, Object> participant = (Map<String, Object>) o;
                if (Integer.valueOf(Participant.RESOURCE).equals(participant.get("type"))) {
                    resources.add(resolveResource(participant));
                }
            }
        }
    }

    public boolean hasParticipants() {
        return hasAccepted() || hasDeclined() || hasTentative() || hasUndecided();
    }

    public boolean hasAccepted() {
        return !accepted.isEmpty();
    }

    public boolean hasDeclined() {
        return !declined.isEmpty();
    }

    public boolean hasTentative() {
        return !tentative.isEmpty();
    }

    public boolean hasUndecided() {
        return !undecided.isEmpty();
    }

    public boolean hasResources() {
        return !resources.isEmpty();
    }

    public List<SimpleParticipant> getAcceptedParticipants() {
        return accepted;
    }

    public List<SimpleParticipant> getDeclinedParticipants() {
        return declined;
    }

    public List<SimpleParticipant> getTentativeParticipants() {
        return tentative;
    }

    public List<SimpleParticipant> getUndecidedParticipants() {
        return undecided;
    }

    public List<SimpleParticipant> getResources() {
        return resources;
    }

    private List<SimpleParticipant> getList(Integer status) {
        List<SimpleParticipant> list = undecided;
        if (status == null) {
            return undecided;
        }
        switch (status) {
        case Appointment.ACCEPT:
            list = accepted;
            break;
        case Appointment.TENTATIVE:
            list = tentative;
            break;
        case Appointment.DECLINE:
            list = declined;
            break;
        }

        return list;
    }

    private SimpleParticipant resolveResource(Map<String, Object> participant) throws OXException {
        Resource resource = services.getService(ResourceService.class).getResource((Integer)participant.get("id"), ctx);


        return new SimpleParticipant()
            .setDisplayName(resource.getDisplayName());
    }

    private SimpleParticipant resolveUser(Map<String, Object> userO) throws OXException {
        User user = services.getService(UserService.class).getUser((Integer)userO.get("id"), ctx);

        return new SimpleParticipant()
            .setDisplayName(user.getDisplayName())
            .setMessage((String) userO.get("confirmmessage"));
    }

    private SimpleParticipant resolveConfirmation(Map<String, Object> confirmation) {
        String dname = (String) confirmation.get("display_name");
        String mail = (String) confirmation.get("mail");

        String displayName = (dname != null) ? dname : mail;

        return new SimpleParticipant()
            .setDisplayName(displayName);
    }

}
