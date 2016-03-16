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

package com.openexchange.calendar.itip.generators;

import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import com.openexchange.calendar.AppointmentDiff;
import com.openexchange.calendar.itip.generators.changes.ChangeDescriber;
import com.openexchange.calendar.itip.generators.changes.generators.Attachments;
import com.openexchange.calendar.itip.generators.changes.generators.Details;
import com.openexchange.calendar.itip.generators.changes.generators.Participants;
import com.openexchange.calendar.itip.generators.changes.generators.Rescheduling;
import com.openexchange.calendar.itip.generators.changes.generators.ShownAs;
import com.openexchange.exception.OXException;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.resource.ResourceService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.UserService;


/**
 * {@link ChangeHelper}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ChangeHelper {

    private final ChangeDescriber describer;

    private final AppointmentDiff diff;
    private final Appointment update;
    private final Appointment original;
    private final TypeWrapper wrapper;

    private final Context ctx;

	private final Locale locale;

	private final TimeZone timezone;

    public ChangeHelper(final Context ctx, final NotificationParticipant participant, final Appointment original, final Appointment update, final AppointmentDiff diff, final Locale locale, final TimeZone tz, final TypeWrapper wrapper, final AttachmentMemory attachmentMemory, final ServiceLookup services) {
        super();
        this.original = original;
        this.update = update;
        this.diff = diff;
        this.locale = locale;
        this.timezone = tz;
        this.wrapper = wrapper;
        this.ctx = ctx;
        final Rescheduling rescheduling = new Rescheduling();
        boolean interested = true;
        if (participant.getConfiguration() != null) {
        	interested = participant.getConfiguration().interestedInStateChanges();
        }
        final Participants participants =  new Participants(services.getService(UserService.class), services.getService(GroupService.class), services.getService(ResourceService.class), interested);
        final Details details = new Details();
        final Attachments attachments = new Attachments(attachmentMemory);
        ShownAs shownAs = new ShownAs();

    	describer = new ChangeDescriber(rescheduling, details, participants, shownAs, attachments);

    }

    public List<String> getChanges() throws OXException {
        return describer.getChanges(ctx, original, update, diff, wrapper, locale, timezone);
    }



}
