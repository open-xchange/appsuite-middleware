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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.calendar.itip.generators;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.calendar.AppointmentDiff;
import com.openexchange.calendar.itip.generators.changes.ChangeDescriber;
import com.openexchange.calendar.itip.generators.changes.generators.Details;
import com.openexchange.calendar.itip.generators.changes.generators.Participants;
import com.openexchange.calendar.itip.generators.changes.generators.Rescheduling;
import com.openexchange.calendar.itip.generators.changes.generators.Style;
import com.openexchange.exception.OXException;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.resource.ResourceService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.UserService;


/**
 * {@link ChangeHelper}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ChangeHelper {

    private ChangeDescriber describer;
    
    private AppointmentDiff diff;
    private Appointment update;
    private Appointment original;
    private TypeWrapper wrapper;

    private Context ctx;

	private Locale locale;

	private TimeZone timezone;

    public ChangeHelper(Context ctx, NotificationParticipant participant, Appointment original, Appointment update, AppointmentDiff diff, Locale locale, TimeZone tz, TypeWrapper wrapper, ServiceLookup services, Style style) {
        super();
        this.original = original;
        this.update = update;
        this.diff = diff;
        this.locale = locale;
        this.timezone = tz;
        this.wrapper = wrapper;
        this.ctx = ctx;
        Rescheduling rescheduling = new Rescheduling(style);
        boolean interested = true;
        if (participant.getConfiguration() != null) {
        	interested = participant.getConfiguration().interestedInStateChanges();
        }
        Participants participants =  new Participants(services.getService(UserService.class), services.getService(GroupService.class), services.getService(ResourceService.class), style, interested);
        Details details = new Details(style);

    	
    	describer = new ChangeDescriber(rescheduling, details, participants);
        
    }
    
    public List<String> getChanges() throws OXException {
        return describer.getChanges(ctx, original, update, diff, wrapper, locale, timezone);
    }
    
    

}
