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

package com.openexchange.groupware.notify;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.mail.MailObject;
import com.openexchange.groupware.i18n.Notifications;
import com.openexchange.i18n.tools.RenderMap;
import com.openexchange.i18n.tools.StringTemplate;
import com.openexchange.i18n.tools.Template;
import com.openexchange.i18n.tools.TemplateReplacement;
import com.openexchange.i18n.tools.TemplateToken;
import com.openexchange.i18n.tools.replacement.FormatLocalizedStringReplacement;
import com.openexchange.mail.MailException;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;

public class AppointmentState extends LinkableState {

	private static final org.apache.commons.logging.Log LOGGER = org.apache.commons.logging.LogFactory
			.getLog(AppointmentState.class);

	private final TemplateReplacement actionRepl;

	private final TemplateReplacement confirmationActionRepl;

	private final String messageTemplate;

	private final Type type;

	public AppointmentState(final TemplateReplacement actionRepl, final String messageTemplate, final Type type) {
		this(actionRepl, null, messageTemplate, type);
	}

	public AppointmentState(final TemplateReplacement actionRepl, final TemplateReplacement confirmationActionRepl,
			final String messageTemplate, final Type type) {
		super();
		this.actionRepl = actionRepl;
		this.confirmationActionRepl = confirmationActionRepl;
		this.messageTemplate = messageTemplate;
		this.type = type;
	}

	public boolean sendMail(final UserSettingMail userSettingMail) {
		return userSettingMail.isNotifyAppointments();
	}

	@Override
	public void addSpecial(final CalendarObject obj, final CalendarObject oldObj, final RenderMap renderMap,
			final EmailableParticipant p) {
		super.addSpecial(obj, oldObj, renderMap, p);
		String location = ((AppointmentObject) obj).getLocation();
		if (location == null) {
			location = "";
		}
		final TemplateReplacement tr = new FormatLocalizedStringReplacement(TemplateToken.LOCATION,
				Notifications.FORMAT_LOCATION, location);
		tr.setLocale(p.locale);
		tr.setChanged(oldObj == null ? false : !ParticipantNotify.compareStrings(location, ((AppointmentObject) oldObj)
				.getLocation()));
		renderMap.put(tr);
	}

	public int getModule() {
		return Types.APPOINTMENT;
	}

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

	public void modifyInternal(final MailObject mail, final CalendarObject obj, final ServerSession sessObj) {
		// Nothing to do
	}

	public void modifyExternal(final MailObject mail, final CalendarObject obj, final ServerSession sessObj) {
		addICALAttachment(mail, (AppointmentObject) obj, sessObj);
	}

	private void addICALAttachment(final MailObject mail, final AppointmentObject obj, final ServerSession sessObj) {
		OXContainerConverter oxContainerConverter = null;
		try {
			final VersitDefinition versitDefinition = Versit.getDefinition("text/calendar");
			final InputStream icalFile;
			{
				final UnsynchronizedByteArrayOutputStream byteArrayOutputStream = new UnsynchronizedByteArrayOutputStream();
				final VersitDefinition.Writer versitWriter = versitDefinition.getWriter(byteArrayOutputStream, "UTF-8");
				final VersitObject versitObjectContainer = OXContainerConverter.newCalendar("2.0");
				versitDefinition.writeProperties(versitWriter, versitObjectContainer);
				oxContainerConverter = new OXContainerConverter(sessObj.getContext(), TimeZone.getDefault());
				final VersitDefinition eventDef = versitDefinition.getChildDef("VEVENT");

				final VersitObject versitObject = oxContainerConverter.convertAppointment(obj);
				eventDef.write(versitWriter, versitObject);
				versitDefinition.writeEnd(versitWriter, versitObjectContainer);
				versitWriter.flush();
				versitWriter.close();
				icalFile = new UnsynchronizedByteArrayInputStream(byteArrayOutputStream.toByteArray());
			}

			final ContentType ct = new ContentType();
			ct.setPrimaryType("text");
			ct.setSubType("calendar");
			ct.setCharsetParameter("utf-8");

			final String filename = "appointment.ics";

			mail.addFileAttachment(ct, filename, icalFile);

		} catch (final IOException e) {
			LOGGER.error("Can't convert appointment for notification mail.", e);
		} catch (final ConverterException e) {
			LOGGER.error("Can't convert appointment for notification mail.", e);
		} catch (final MailException e) {
			LOGGER.error("Can't add attachment", e);
		} finally {
			if (oxContainerConverter != null) {
				oxContainerConverter.close();
			}
		}
	}

	public Template getTemplate() {
		return new StringTemplate(messageTemplate);
	}

	public TemplateReplacement getAction() {
		return actionRepl;
	}

	public TemplateReplacement getConfirmationAction() {
		return confirmationActionRepl;
	}

	public Type getType() {
		return type;
	}

}