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


package com.openexchange.webdav.xml;

import java.util.Date;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.session.Session;
import com.openexchange.webdav.xml.fields.AppointmentFields;
import com.openexchange.webdav.xml.fields.CalendarFields;

/**
 * AppointmentParser
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public class AppointmentParser extends CalendarParser {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AppointmentParser.class);

	public AppointmentParser(final Session sessionObj) {
		this.sessionObj = sessionObj;
	}

	public void parse(final XmlPullParser parser, final Appointment appointmentobject) throws OXException, XmlPullParserException {
		try {
			while (true) {
				if (parser.getEventType() == XmlPullParser.END_TAG && parser.getName().equals("prop")) {
					break;
				}

				parseElementAppointment(appointmentobject, parser);
				parser.nextTag();
			}
		} catch (final XmlPullParserException exc) {
			throw exc;
		} catch (final Exception exc) {
			throw new OXException(exc);
		}
	}

	protected void parseElementAppointment(final Appointment ao, final XmlPullParser parser) throws Exception {
		if (!hasCorrectNamespace(parser)) {
			LOG.trace("unknown namespace in tag: {}", parser.getName());
			parser.nextText();
			return ;
		}

		if (isTag(parser, AppointmentFields.SHOW_AS)) {
			ao.setShownAs(getValueAsInt(parser));

			return ;
		} else if (isTag(parser, CalendarFields.DELETE_EXCEPTIONS)) {
			try {
				final String _s = getValue(parser);

				if (_s == null) {
					return;
				}

				final String[] _dates = _s.split(",");
				final java.util.Date[] deleteExceptions = new java.util.Date[_dates.length];

				for (int a = 0; a < _dates.length; a++) {
					deleteExceptions[a] = parseString2Date(_dates[a]);
				}

				ao.setDeleteExceptions(deleteExceptions);

				return ;
			} catch (final Exception exc) {
				throw new OXException(exc);
			}
		} else if (isTag(parser, AppointmentFields.FULL_TIME)) {
			ao.setFullTime(getValueAsBoolean(parser));

			return ;
		} else if (isTag(parser, AppointmentFields.LOCATION)) {
			ao.setLocation(getValue(parser));
			return ;
		} else if (isTag(parser, CalendarFields.ALARM)) {
			ao.setAlarm(getValueAsInt(parser));
			return ;
		} else if (isTag(parser, CalendarFields.ALARM_FLAG)) {
			ao.setAlarmFlag(getValueAsBoolean(parser));
			return ;
		} else if (isTag(parser, AppointmentFields.IGNORE_CONFLICTS)) {
			ao.setIgnoreConflicts(getValueAsBoolean(parser));
			return ;
		} else if (isTag(parser, AppointmentFields.UID)) {
		    ao.setUid(getValue(parser));
		} else {
			parseElementCalendar(ao, parser);
		}
	}

	private Date parseString2Date(final String s) {
		return new Date(Long.parseLong(s));
	}
}




