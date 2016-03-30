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

package com.openexchange.groupware.notify;

import static com.openexchange.java.Autoboxing.I;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Set;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalSession;
import com.openexchange.data.conversion.ical.SimpleMode;
import com.openexchange.data.conversion.ical.ZoneInfo;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.mail.MailObject;
import com.openexchange.i18n.tools.RenderMap;
import com.openexchange.i18n.tools.StringTemplate;
import com.openexchange.i18n.tools.Template;
import com.openexchange.i18n.tools.TemplateReplacement;
import com.openexchange.i18n.tools.replacement.LocationReplacement;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link AppointmentState} - The state for appointment notifications
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco
 *         Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class AppointmentState extends LinkableState {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AppointmentState.class);

    private final TemplateReplacement actionRepl;

    private final TemplateReplacement confirmationActionRepl;

    private final String messageTemplate;

    private final Type type;

    /**
     * Initializes a new {@link AppointmentState}
     *
     * @param actionRepl
     *            The action replacement
     * @param messageTemplate
     *            The message template
     * @param type
     *            The notification type
     */
    public AppointmentState(final TemplateReplacement actionRepl, final String messageTemplate, final Type type) {
        this(actionRepl, null, messageTemplate, type);
    }

    /**
     * Initializes a new {@link AppointmentState}
     *
     * @param actionRepl
     *            The action replacement
     * @param confirmationActionRepl
     *            The confirmation action replacement (optional)
     * @param messageTemplate
     *            The message template
     * @param type
     *            The notification type
     */
    public AppointmentState(final TemplateReplacement actionRepl, final TemplateReplacement confirmationActionRepl,
            final String messageTemplate, final Type type) {
        super();
        this.actionRepl = actionRepl;
        this.confirmationActionRepl = confirmationActionRepl;
        this.messageTemplate = messageTemplate;
        this.type = type;
    }

    @Override
    public boolean sendMail(final UserSettingMail userSettingMail, final int owner, final int participant,
            final int modificationUser) {
        if (modificationUser == participant) {
            return false;
        }

        switch (type) {
        case ACCEPTED:
            /*fall through*/
        case DECLINED:
            /*fall through*/
        case TENTATIVELY_ACCEPTED:
            /*fall through*/
        case NONE_ACCEPTED:
            return (participant == owner) ? userSettingMail.isNotifyAppointmentsConfirmOwner() : userSettingMail
                    .isNotifyAppointmentsConfirmParticipant();
        case REMINDER:
            return false;
        default:
            return userSettingMail.isNotifyAppointments();
        }
    }

    @Override
    public void addSpecial(final CalendarObject obj, final CalendarObject oldObj, final RenderMap renderMap,
            final EmailableParticipant p) {
        super.addSpecial(obj, oldObj, renderMap, p);
        String location = ((Appointment) obj).getLocation();
        if (location == null) {
            location = "";
        }
        final TemplateReplacement tr = new LocationReplacement(location);
        tr.setLocale(p.getLocale());
        tr.setChanged(oldObj == null ? false : !ParticipantNotify.compareStrings(location, ((Appointment) oldObj)
                .getLocation()));
        renderMap.put(tr);
        if (!obj.containsUid() && null != oldObj && oldObj.containsUid()) {
            obj.setUid(oldObj.getUid());
        }
    }

    @Override
    public int getModule() {
        return Types.APPOINTMENT;
    }

    @Override
    public DateFormat getDateFormat(final Locale locale) {
        return tryAppendingTimeZone(DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, locale));
    }

    private DateFormat tryAppendingTimeZone(final DateFormat df) {
        if (df instanceof SimpleDateFormat) {
            final SimpleDateFormat sdf = (SimpleDateFormat) df;
            final String format = sdf.toPattern();
            return new SimpleDateFormat(format + ", z");
        }
        return df;
    }

    @Override
    public void modifyInternal(final MailObject mail, final CalendarObject obj, final ServerSession sessObj) {
        // Nothing to do
    }

    @Override
    public void modifyExternal(final MailObject mail, final CalendarObject obj, final ServerSession sessObj) {
        addICALAttachment(mail, (Appointment) obj, sessObj);
    }

    private void addICALAttachment(final MailObject mail, final Appointment obj, final ServerSession sessObj) {
        final ICalEmitter emitter = ServerServiceRegistry.getInstance().getService(ICalEmitter.class);
        if (emitter == null) {
            LOGGER.warn("Could not find ical emitter service. Skipping attachment");
            return;
        }

        try {
            final InputStream icalFile;
            {
                final UnsynchronizedByteArrayOutputStream byteArrayOutputStream = new UnsynchronizedByteArrayOutputStream();
                final ICalSession session = emitter.createSession(new SimpleMode(ZoneInfo.OUTLOOK));
                emitter.writeAppointment(session, obj, sessObj.getContext(), new LinkedList<ConversionError>(),
                        new LinkedList<ConversionWarning>());
                emitter.writeSession(session, byteArrayOutputStream);
                icalFile = new UnsynchronizedByteArrayInputStream(byteArrayOutputStream.toByteArray());
            }

            final ContentType ct = new ContentType();
            ct.setPrimaryType("text");
            ct.setSubType("calendar");
            ct.setCharsetParameter("utf-8");

            final String filename = "appointment.ics";

            mail.addFileAttachment(ct, filename, icalFile);

        } catch (final OXException e) {
            LOGGER.error("Can't add attachment", e);
        }
    }

    @Override
    public Template getTemplate() {
        return new StringTemplate(messageTemplate);
    }

    @Override
    public TemplateReplacement getAction() {
        return actionRepl;
    }

    @Override
    public TemplateReplacement getConfirmationAction() {
        return confirmationActionRepl;
    }

    @Override
    public Type getType() {
        return type;
    }

    private static final Set<Integer> FIELDS_TO_IGNORE = new HashSet<Integer>(Arrays.asList(
        I(Appointment.OBJECT_ID),
        I(Appointment.CREATED_BY),
        I(Appointment.MODIFIED_BY),
        I(Appointment.CREATION_DATE),
        I(Appointment.LAST_MODIFIED),
        I(Appointment.LAST_MODIFIED_UTC),
        I(Appointment.ALARM),
        I(Appointment.NOTIFICATION),
        I(Appointment.RECURRENCE_TYPE),
        I(Appointment.CATEGORIES),
        I(Appointment.SEQUENCE),
        I(Appointment.SHOWN_AS)
    ));

    @Override
    public boolean onlyIrrelevantFieldsChanged(final CalendarObject oldObj, final CalendarObject newObj) {
        return false; // no longer used
    }
}
