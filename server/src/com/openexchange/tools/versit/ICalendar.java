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



package com.openexchange.tools.versit;

import com.openexchange.tools.versit.encodings.BASE64Encoding;
import com.openexchange.tools.versit.paramvalues.definitions.CalAddressParamValueDefinition;
import com.openexchange.tools.versit.paramvalues.definitions.TokenParamValueDefinition;
import com.openexchange.tools.versit.paramvalues.definitions.URIParamValueDefinition;
import com.openexchange.tools.versit.valuedefinitions.rfc2425.IntegerValueDefinition;
import com.openexchange.tools.versit.valuedefinitions.rfc2425.ListValueDefinition;
import com.openexchange.tools.versit.valuedefinitions.rfc2425.URIValueDefinition;
import com.openexchange.tools.versit.valuedefinitions.rfc2445.BinaryValueDefinition;
import com.openexchange.tools.versit.valuedefinitions.rfc2445.CalAddressValueDefinition;
import com.openexchange.tools.versit.valuedefinitions.rfc2445.DateTimeValueDefinition;
import com.openexchange.tools.versit.valuedefinitions.rfc2445.DateValueDefinition;
import com.openexchange.tools.versit.valuedefinitions.rfc2445.DurationValueDefinition;
import com.openexchange.tools.versit.valuedefinitions.rfc2445.FloatingDateTimeValueDefinition;
import com.openexchange.tools.versit.valuedefinitions.rfc2445.GeoValueDefinition;
import com.openexchange.tools.versit.valuedefinitions.rfc2445.PeriodValueDefinition;
import com.openexchange.tools.versit.valuedefinitions.rfc2445.PositiveDurationValueDefinition;
import com.openexchange.tools.versit.valuedefinitions.rfc2445.RecurrenceValueDefinition;
import com.openexchange.tools.versit.valuedefinitions.rfc2445.TextValueDefinition;
import com.openexchange.tools.versit.valuedefinitions.rfc2445.UTCDateTimeValueDefinition;
import com.openexchange.tools.versit.valuedefinitions.rfc2445.UTCOffsetValueDefinition;
import com.openexchange.tools.versit.valuedefinitions.rfc2445.UTCPeriodValueDefinition;

/**
 * @author Viktor Pracht
 */
public class ICalendar {

	// Empty arrays

	private static final String[] NoNames = {};

	private static final ObjectDefinition[] NoChildren = {};

	private static final ValueDefinition[] NoValues = {};

	private static final ParameterDefinition[] NoParameters = {};

	// Parameter definitions

	private static final ParameterDefinition TokenParameter = new ParameterDefinition(
			TokenParamValueDefinition.Default);

	private static final ParameterDefinition CalAddressParameter = new ParameterDefinition(
			CalAddressParamValueDefinition.Default);

	private static final ParameterDefinition URIParameter = new ParameterDefinition(
			URIParamValueDefinition.Default);

	// Arrays of parameter definitions

	private static final String[] FreeBusyParameterNames = { "FBTYPE" };

	private static final ParameterDefinition[] TokenParameters = { TokenParameter };

	private static final String[] AttendeeParameterNames = { "CUTYPE",
			"MEMBER", "ROLE", "PARTSTAT", "RSVP", "DELEGATED-TO",
			"DELEGATED-FROM", "SENT-BY", "DIR" };

	private static final ParameterDefinition[] AttendeeParameters = {
			TokenParameter, CalAddressParameter, TokenParameter,
			TokenParameter, TokenParameter, CalAddressParameter,
			CalAddressParameter, CalAddressParameter, URIParameter };

	private static final String[] OrganizerParameterNames = { "DIR", "SENT-BY" };

	private static final ParameterDefinition[] OrganizerParameters = {
			URIParameter, CalAddressParameter };

	private static final String[] RecurIDParameterName = { "RANGE" };

	private static final String[] RelatedParameterName = { "RELTYPE" };

	private static final String[] TriggerParameterName = { "RELATED" };

	private static final String[] AltRepParameterName = { "ALTREP" };

	private static final ParameterDefinition[] AltRepParameter = { new ParameterDefinition(
			URIParamValueDefinition.Default) };

	// Arrays of encodings

	private static final String[] B64EncodingName = { "BASE64" };

	private static final Encoding[] B64Encoding = { new BASE64Encoding() };

	// List value definitions

	private static final ValueDefinition DateTimeList = new ListValueDefinition(
			',', DateTimeValueDefinition.Default);

	private static final ValueDefinition DateList = new ListValueDefinition(
			',', DateValueDefinition.Default);

	// Arrays of value definitions

	private static final String[] AttachValueNames = { "URI", "BINARY" };

	private static final ValueDefinition[] AttachValues = {
			URIValueDefinition.Default,
			new BinaryValueDefinition(B64EncodingName, B64Encoding) };

	private static final String[] OptionalTimeNames = { "DATE-TIME", "DATE" };

	private static final ValueDefinition[] OptionalTime = {
			DateTimeValueDefinition.Default, DateValueDefinition.Default };

	private static final ValueDefinition[] OptionalTimeList = { DateTimeList,
			DateList };

	private static final ValueDefinition[] OptionalUTCTime = {
			UTCDateTimeValueDefinition.Default, DateValueDefinition.Default };

	private static final String[] DateTimePeriodNames = { "DATE-TIME", "DATE",
			"PERIOD" };

	private static final ValueDefinition[] DateTimePeriod = { DateTimeList,
			DateList, PeriodValueDefinition.Default };

	private static final String[] TriggerValueNames = { "DURATION", "DATE-TIME" };

	private static final ValueDefinition[] TriggerValues = {
			DurationValueDefinition.Default, UTCDateTimeValueDefinition.Default };

	// Property definitions

	private static final PropertyDefinition Text = new PropertyDefinition(
			TextValueDefinition.Default);

	private static final PropertyDefinition Attach = new PropertyDefinition(
			URIValueDefinition.Default, AttachValueNames, AttachValues,
			NoNames, NoParameters);

	private static final PropertyDefinition Categories = new PropertyDefinition(
			new ListValueDefinition(',', TextValueDefinition.Default));

	private static final PropertyDefinition AltRepProperty = new PropertyDefinition(
			TextValueDefinition.Default, NoNames, NoValues,
			AltRepParameterName, AltRepParameter);

	private static final PropertyDefinition AltRepListProperty = new PropertyDefinition(
			new ListValueDefinition(',', TextValueDefinition.Default), NoNames,
			NoValues, AltRepParameterName, AltRepParameter);

	private static final PropertyDefinition Geo = new PropertyDefinition(
			GeoValueDefinition.Default);

	private static final PropertyDefinition IntegerProperty = new PropertyDefinition(
			IntegerValueDefinition.Default);

	private static final PropertyDefinition OptionalTimeProperty = new PropertyDefinition(
			DateTimeValueDefinition.Default, OptionalTimeNames, OptionalTime,
			NoNames, NoParameters);

	private static final PropertyDefinition OptionalTimeListProperty = new PropertyDefinition(
			DateTimeList, OptionalTimeNames, OptionalTimeList, NoNames,
			NoParameters);

	private static final PropertyDefinition Duration = new PropertyDefinition(
			PositiveDurationValueDefinition.Default);

	private static final PropertyDefinition Attendee = new PropertyDefinition(
			CalAddressValueDefinition.Default, NoNames, NoValues,
			AttendeeParameterNames, AttendeeParameters);

	private static final PropertyDefinition SimpleAttendee = new PropertyDefinition(
			CalAddressValueDefinition.Default);

	private static final PropertyDefinition Organizer = new PropertyDefinition(
			CalAddressValueDefinition.Default, NoNames, NoValues,
			OrganizerParameterNames, OrganizerParameters);

	private static final PropertyDefinition RecurID = new PropertyDefinition(
			DateTimeValueDefinition.Default, OptionalTimeNames, OptionalTime,
			RecurIDParameterName, TokenParameters);

	private static final PropertyDefinition Related = new PropertyDefinition(
			TextValueDefinition.Default, NoNames, NoValues,
			RelatedParameterName, TokenParameters);

	private static final PropertyDefinition URL = new PropertyDefinition(
			URIValueDefinition.Default);

	private static final PropertyDefinition RecurrenceProperty = new PropertyDefinition(
			RecurrenceValueDefinition.Default);

	private static final PropertyDefinition RDate = new PropertyDefinition(
			DateTimeList, DateTimePeriodNames, DateTimePeriod, NoNames,
			NoParameters);

	private static final PropertyDefinition UTCDateTimeProperty = new PropertyDefinition(
			UTCDateTimeValueDefinition.Default);

	// Arrays of property definitions

	private static final String[] PropNames2 = { "PRODID", "VERSION",
			"CALSCALE", "METHOD" };

	private static final PropertyDefinition[] Properties2 = { Text, Text, Text,
			Text };

	private static final String[] EventPropertyNames = { "ATTACH",
			"CATEGORIES", "COMMENT", "DESCRIPTION", "GEO", "LOCATION",
			"PRIORITY", "RESOURCES", "SUMMARY", "DTEND", "DTSTART", "DURATION",
			"ATTENDEE", "CONTACT", "ORGANIZER", "RECURRENCE-ID", "RELATED-TO",
			"URL", "EXDATE", "EXRULE", "RDATE", "RRULE", "CREATED", "DTSTAMP",
			"LAST-MODIFIED", "SEQUENCE", "CLASS", "STATUS", "TRANSP", "UID",
			"REQUEST-STATUS" };

	private static final PropertyDefinition[] EventProperties = {
			Attach,
			Categories,
			AltRepProperty,
			AltRepProperty,
			Geo,
			AltRepProperty,
			IntegerProperty,
			AltRepListProperty,
			AltRepProperty,
			new PropertyDefinition(DateTimeValueDefinition.Default,
					OptionalTimeNames, OptionalTime, NoNames, NoParameters),
			OptionalTimeProperty, Duration, Attendee, AltRepProperty,
			Organizer, RecurID, Related, URL, OptionalTimeListProperty,
			RecurrenceProperty, RDate, RecurrenceProperty, UTCDateTimeProperty,
			UTCDateTimeProperty, UTCDateTimeProperty, IntegerProperty, Text,
			Text, Text, Text, Text };

	private static final String[] ToDoPropertyNames = { "ATTACH", "CATEGORIES",
			"COMMENT", "DESCRIPTION", "GEO", "LOCATION", "PERCENT-COMPLETE",
			"PRIORITY", "RESOURCES", "SUMMARY", "COMPLETED", "DUE", "DTSTART",
			"DURATION", "ATTENDEE", "CONTACT", "ORGANIZER", "RECURRENCE-ID",
			"RELATED-TO", "URL", "EXDATE", "EXRULE", "RDATE", "RRULE",
			"CREATED", "DTSTAMP", "LAST-MODIFIED", "SEQUENCE", "CLASS",
			"STATUS", "UID", "REQUEST-STATUS" };

	private static final PropertyDefinition[] ToDoProperties = {
			Attach,
			Categories,
			AltRepProperty,
			AltRepProperty,
			Geo,
			AltRepProperty,
			IntegerProperty,
			IntegerProperty,
			AltRepListProperty,
			AltRepProperty,
			new PropertyDefinition(UTCDateTimeValueDefinition.Default),
			new PropertyDefinition(DateTimeValueDefinition.Default,
					OptionalTimeNames, OptionalTime, NoNames, NoParameters),
			OptionalTimeProperty, Duration, Attendee, AltRepProperty,
			Organizer, RecurID, Related, URL, OptionalTimeListProperty,
			RecurrenceProperty, RDate, RecurrenceProperty, UTCDateTimeProperty,
			UTCDateTimeProperty, UTCDateTimeProperty, IntegerProperty, Text,
			Text, Text, Text };

	private static final String[] JournalPropertyNames = { "ATTACH",
			"CATEGORIES", "COMMENT", "DESCRIPTION", "SUMMARY", "DTSTART",
			"ATTENDEE", "CONTACT", "ORGANIZER", "RECURRENCE-ID", "RELATED-TO",
			"URL", "EXDATE", "EXRULE", "RDATE", "RRULE", "CREATED", "DTSTAMP",
			"LAST-MODIFIED", "SEQUENCE", "CLASS", "STATUS", "UID",
			"REQUEST-STATUS" };

	private static final PropertyDefinition[] JournalProperties = { Attach,
			Categories, AltRepProperty, AltRepProperty, AltRepProperty,
			OptionalTimeProperty, Attendee, AltRepProperty, Organizer, RecurID,
			Related, URL, OptionalTimeListProperty, RecurrenceProperty, RDate,
			RecurrenceProperty, UTCDateTimeProperty, UTCDateTimeProperty,
			UTCDateTimeProperty, IntegerProperty, Text, Text, Text, Text };

	private static final String[] FreeBusyPropertyNames = { "CONTACT",
			"DTSTART", "DTEND", "DURATION", "DTSTAMP", "ORGANIZER", "URL",
			"ATTENDEE", "COMMENT", "FREEBUSY", "UID", "RSTATUS" };

	private static final PropertyDefinition[] FreeBusyProperties = {
			AltRepProperty,
			new PropertyDefinition(UTCDateTimeValueDefinition.Default,
					OptionalTimeNames, OptionalUTCTime, NoNames, NoParameters),
			new PropertyDefinition(UTCDateTimeValueDefinition.Default,
					OptionalTimeNames, OptionalUTCTime, NoNames, NoParameters),
			Duration,
			UTCDateTimeProperty,
			Organizer,
			URL,
			SimpleAttendee,
			AltRepProperty,
			new PropertyDefinition(new ListValueDefinition(',',
					UTCPeriodValueDefinition.Default), NoNames, NoValues,
					FreeBusyParameterNames, TokenParameters), Text, Text };

	private static final String[] AlarmPropertyNames = { "ATTACH", "COMMENT",
			"DESCRIPTION", "SUMMARY", "DURATION", "ATTENDEE", "REPEAT",
			"TRIGGER", "ACTION" };

	private static final PropertyDefinition[] AlarmProperties = {
			Attach,
			AltRepProperty,
			AltRepProperty,
			AltRepProperty,
			Duration,
			SimpleAttendee,
			new PropertyDefinition(IntegerValueDefinition.Default),
			new PropertyDefinition(DurationValueDefinition.Default,
					TriggerValueNames, TriggerValues, TriggerParameterName,
					TokenParameters), Text };

	private static final String[] TimeZonePropertyNames = { "COMMENT", "TZURL",
			"LAST-MODIFIED", "TZID" };

	private static final PropertyDefinition[] TimeZoneProperties = {
			AltRepProperty, new PropertyDefinition(URIValueDefinition.Default),
			UTCDateTimeProperty, Text };

	private static final String[] TZChildPropertyNames = { "DTSTART",
			"TZOFFSETFROM", "TZOFFSETTO", "RDATE", "RRULE", "COMMENT", "TZNAME" };

	private static final PropertyDefinition[] TZChildProperties = {
			new PropertyDefinition(FloatingDateTimeValueDefinition.Default),
			new PropertyDefinition(UTCOffsetValueDefinition.Default),
			new PropertyDefinition(UTCOffsetValueDefinition.Default), RDate,
			RecurrenceProperty, Text, Text };

	// Object definitions and arrays of object definitions

	private static final String[] TimeZoneChildNames = { "STANDARD", "DAYLIGHT" };

	private static final ObjectDefinition TimeZoneChild = new ObjectDefinition(
			TZChildPropertyNames, TZChildProperties, NoNames, NoChildren);

	private static final ObjectDefinition[] TimeZoneChildren = { TimeZoneChild,
			TimeZoneChild };

	private static final String[] AlarmName = { "VALARM" };

	private static final ObjectDefinition[] Alarm = { new ObjectDefinition(
			AlarmPropertyNames, AlarmProperties, NoNames, NoChildren) };

	public static final ObjectDefinition vEvent2 = new ObjectDefinition(
			EventPropertyNames, EventProperties, AlarmName, Alarm);

	public static final ObjectDefinition vToDo2 = new ObjectDefinition(
			ToDoPropertyNames, ToDoProperties, AlarmName, Alarm);

	public static final ObjectDefinition vJournal2 = new ObjectDefinition(
			JournalPropertyNames, JournalProperties, NoNames, NoChildren);

	public static final ObjectDefinition vFreeBusy2 = new ObjectDefinition(
			FreeBusyPropertyNames, FreeBusyProperties, NoNames, NoChildren);

	public static final ObjectDefinition vTimeZone2 = new ObjectDefinition(
			TimeZonePropertyNames, TimeZoneProperties, TimeZoneChildNames,
			TimeZoneChildren);

	private static final String[] ChildNames2 = { "VEVENT", "VTODO",
			"VJOURNAL", "VFREEBUSY", "VTIMEZONE" };

	private static final ObjectDefinition[] Children2 = { vEvent2, vToDo2,
			vJournal2, vFreeBusy2, vTimeZone2 };

	private static final String[] Versions = { "2.0" };

	private static final ObjectDefinition[] Definitions = { new ObjectDefinition(
			PropNames2, Properties2, ChildNames2, Children2) };

	// Versioned object definition

	public static final VersionedObjectDefinition definition = new VersionedObjectDefinition(
			Versions, Definitions);

}
