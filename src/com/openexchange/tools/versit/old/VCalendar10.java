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



package com.openexchange.tools.versit.old;

public class VCalendar10 extends OldObjectDefinition {

	private static final String[] AttendeeParamNames = { "ENCODING", "CHARSET",
			"LANGUAGE", "VALUE", "ROLE", "STATUS", "RVSP", "EXPECT" };

	private static final OldParamDefinition[] AttendeeParams = {
			Encoding,
			TextParam,
			TextParam,
			ValueParam,
			new OldParamDefinition(new String[] { "ATTENDEE", "ORGANIZER",
					"OWNER", "DELEGATE" }),
			new OldParamDefinition(new String[] { "ACCEPTED", "NEEDS ACTION",
					"SENT", "TENTATIVE", "CONFIRMED", "DECLINED", "COMPLETED",
					"DELEGATED" }),
			new OldParamDefinition(new String[] { "YES", "NO" }),
			new OldParamDefinition(new String[] { "FYI", "REQUIRE", "REQUEST",
					"IMMEDIATE" }) };

	private static final String[] AAlarmParamNames = { "ENCODING", "CHARSET",
			"LANGUAGE", "VALUE", "TYPE" };

	private static final OldParamDefinition[] AAlarmParams = { Encoding,
			TextParam, TextParam, ValueParam,
			new OldParamDefinition(new String[] { "PCM", "WAVE", "AIFF" }) };

	private static final OldShortPropertyDefinition Trigger = new OldDateTimePropertyDefinition(
			NoNames, NoParams);

	private static final OldShortPropertyDefinition Duration = new OldDurationPropertyDefinition(
			NoNames, NoParams);

	private static final OldShortPropertyDefinition Repeat = new OldIntegerPropertyDefinition(
			NoNames, NoParams);

	private static final OldShortPropertyDefinition Description = new OldShortPropertyDefinition(
			NoNames, NoParams);

	private static final OldShortPropertyDefinition Uri = new OldURIPropertyDefinition(
			NoNames, NoParams);

	private static final OldPropertyDefinition IntegerProperty = new OldIntegerPropertyDefinition(
			DefaultParamNames, DefaultParams);

	private static final OldPropertyDefinition DateTimeListProperty = new OldDateTimeListPropertyDefinition(
			DefaultParamNames, DefaultParams);

	private static final OldPropertyDefinition RecurrenceProperty = new OldRecurrencePropertyDefinition(
			DefaultParamNames, DefaultParams);

	private static final OldPropertyDefinition ListProperty = new OldCompoundPropertyDefinition(
			DefaultParamNames, DefaultParams);

	private static OldObjectDefinition Child = new OldObjectDefinition(
			new String[] { "ATTACH", "ATTENDEE", "AALARM", "CATEGORIES",
					"CLASS", "DCREATED", "COMPLETED", "DESCRIPTION", "DALARM",
					"DUE", "DTEND", "EXDATE", "EXRULE", "LAST-MODIFIED",
					"LOCATION", "MALARM", "RNUM", "PRIORITY", "PALARM",
					"RELATED-TO", "RDATE", "RRULE", "RESOURCES", "SEQUENCE",
					"DTSTART", "STATUS", "SUMMARY", "TRANSP", "URI", "UID" },
			new OldPropertyDefinition[] {
					new OldAttachPropertyDefinition(DefaultParamNames,
							DefaultParams),
					new OldMailAddrPropertyDefinition(AttendeeParamNames,
							AttendeeParams),
					new OldAAlarmPropertyDefinition(AAlarmParamNames,
							AAlarmParams, new OldShortPropertyDefinition[] {
									Trigger, Duration, Repeat, Uri }),
					ListProperty,
					DefaultProperty,
					DateTimeProperty,
					DateTimeProperty,
					DefaultProperty,
					new OldAlarmPropertyDefinition("DISPLAY", "DESCRIPTION",
							DefaultParamNames, DefaultParams,
							new OldShortPropertyDefinition[] { Trigger,
									Duration, Repeat, Description }),
					DateTimeProperty,
					DateTimeProperty,
					DateTimeListProperty,
					RecurrenceProperty,
					DateTimeProperty,
					DefaultProperty,
					new OldMAlarmPropertyDefinition(DefaultParamNames,
							DefaultParams, new OldShortPropertyDefinition[] {
									Trigger,
									Duration,
									Repeat,
									new OldMailAddrPropertyDefinition(NoNames,
											NoParams), Description }),
					IntegerProperty,
					new OldPriorityPropertyDefinition(DefaultParamNames,
							DefaultParams),
					new OldAlarmPropertyDefinition("PROCEDURE", "ATTACH",
							DefaultParamNames, DefaultParams,
							new OldShortPropertyDefinition[] { Trigger,
									Duration, Repeat, Uri }),
					DefaultProperty,
					DateTimeListProperty,
					RecurrenceProperty,
					ListProperty,
					IntegerProperty,
					DateTimeProperty,
					new OldStatusPropertyDefinition(DefaultParamNames,
							DefaultParams),
					DefaultProperty,
					new OldTranspPropertyDefinition(DefaultParamNames,
							DefaultParams),
					new OldURIPropertyDefinition(DefaultParamNames,
							DefaultParams), DefaultProperty });

	public static final VCalendar10 definition = new VCalendar10(new String[] {
			"VERSION", "DAYLIGHT", "GEO", "PRODID", "TZ" },
			new OldPropertyDefinition[] {
					DefaultProperty,
					new OldDaylightPropertyDefinition(DefaultParamNames,
							DefaultParams),
					new OldGeoPropertyDefinition(DefaultParamNames,
							DefaultParams),
					DefaultProperty,
					new OldTZPropertyDefinition(DefaultParamNames,
							DefaultParams) }, new String[] { "VEVENT", "VTODO",
					"VALARM" }, new OldObjectDefinition[] {
					Child,
					Child,
					new OldAlarmObjectDefinition(new String[] {},
							new OldPropertyDefinition[] {}) });

	public VCalendar10(String[] propertyNames,
			OldPropertyDefinition[] properties, String[] childNames,
			OldObjectDefinition[] children) {
		super(propertyNames, properties, childNames, children);
		Name = "VCALENDAR";
	}

}
