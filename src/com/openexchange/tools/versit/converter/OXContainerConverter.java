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

package com.openexchange.tools.versit.converter;

import com.openexchange.groupware.calendar.CalendarDataObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.versit.Parameter;
import com.openexchange.tools.versit.ParameterValue;
import com.openexchange.tools.versit.Property;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.encodings.BASE64Encoding;
import com.openexchange.tools.versit.values.DateTimeValue;
import com.openexchange.tools.versit.values.DurationValue;
import com.openexchange.tools.versit.values.RecurrenceValue;

/**
 * This class transforms VersitObjects to OX Contacts, Appointments and Tasks
 * and back.
 * 
 * If you want to translate more fields used in ICAL or VCard, you're at the
 * right place - but don't forget to do it in both directions.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> (adapted Victor's parser for OX6)
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> (bugfixes: 7248, 7249, 7472, 7703, 7718, 7719, 8475)
 * 
 */
public class OXContainerConverter {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(OXContainerConverter.class);

	// ------------------------ START Prepare converter class
	// -------------------------------------

	private static final Map<Integer, Method> SET_INT_METHODS = new HashMap<Integer, Method>();

	private static final Map<Integer, Method> SET_LONG_METHODS = new HashMap<Integer, Method>();

	private static final Map<Integer, Method> SET_DATE_METHODS = new HashMap<Integer, Method>();

	private static final Map<Integer, Method> SET_STRING_METHODS = new HashMap<Integer, Method>();

	private static final Map<Integer, Method> SET_BOOLEAN_METHODS = new HashMap<Integer, Method>();

	private static final Map<Integer, Method> SET_FLOAT_METHODS = new HashMap<Integer, Method>();

	private static Method getSetIntegerMethod(final Class containerObjClass, final String methodName) {
		return getSetMethod(containerObjClass, methodName, int.class);
	}

	private static Method getSetLongMethod(final Class containerObjClass, final String methodName) {
		return getSetMethod(containerObjClass, methodName, long.class);
	}

	private static Method getSetDateMethod(final Class containerObjClass, final String methodName) {
		return getSetMethod(containerObjClass, methodName, Date.class);
	}

	private static Method getSetStringMethod(final Class containerObjClass, final String methodName) {
		return getSetMethod(containerObjClass, methodName, String.class);
	}

	private static Method getSetBooleanMethod(final Class containerObjClass, final String methodName) {
		return getSetMethod(containerObjClass, methodName, boolean.class);
	}

	private static Method getSetFloatMethod(final Class containerObjClass, final String methodName) {
		return getSetMethod(containerObjClass, methodName, float.class);
	}

	private static Method getSetMethod(final Class containerObjClass, final String methodName, final Class typeClass) {
		try {
			return containerObjClass.getMethod(methodName, new Class[] { typeClass });
		} catch (Exception e) {
			LOG.error(e);
			return null;
		}
	}

	static {
		// setter methods for int values
		SET_INT_METHODS
				.put(Integer.valueOf(DataObject.OBJECT_ID), getSetIntegerMethod(DataObject.class, "setObjectID"));
		SET_INT_METHODS.put(Integer.valueOf(DataObject.CREATED_BY), getSetIntegerMethod(DataObject.class,
				"setCreatedBy"));
		SET_INT_METHODS.put(Integer.valueOf(DataObject.MODIFIED_BY), getSetIntegerMethod(DataObject.class,
				"setModifiedBy"));

		SET_INT_METHODS.put(Integer.valueOf(FolderChildObject.FOLDER_ID), getSetIntegerMethod(FolderChildObject.class,
				"setParentFolderID"));

		SET_INT_METHODS.put(Integer.valueOf(CommonObject.COLOR_LABEL), getSetIntegerMethod(CommonObject.class,
				"setLabel"));
		SET_INT_METHODS.put(Integer.valueOf(CommonObject.NUMBER_OF_LINKS), getSetIntegerMethod(CommonObject.class,
				"setNumberOfLinks"));
		SET_INT_METHODS.put(Integer.valueOf(CommonObject.NUMBER_OF_ATTACHMENTS), getSetIntegerMethod(
				CommonObject.class, "setNumberOfAttachments"));

		SET_INT_METHODS.put(Integer.valueOf(CalendarObject.RECURRENCE_ID), getSetIntegerMethod(CalendarObject.class,
				"setRecurrenceID"));
		SET_INT_METHODS.put(Integer.valueOf(CalendarObject.RECURRENCE_POSITION), getSetIntegerMethod(
				CalendarObject.class, "setRecurrencePosition"));
		SET_INT_METHODS.put(Integer.valueOf(CalendarObject.RECURRENCE_TYPE), getSetIntegerMethod(CalendarObject.class,
				"setRecurrenceType"));
		SET_INT_METHODS.put(Integer.valueOf(CalendarObject.DAYS), getSetIntegerMethod(CalendarObject.class, "setDays"));
		SET_INT_METHODS.put(Integer.valueOf(CalendarObject.DAY_IN_MONTH), getSetIntegerMethod(CalendarObject.class,
				"setDayInMonth"));
		SET_INT_METHODS.put(Integer.valueOf(CalendarObject.MONTH),
				getSetIntegerMethod(CalendarObject.class, "setMonth"));
		SET_INT_METHODS.put(Integer.valueOf(CalendarObject.INTERVAL), getSetIntegerMethod(CalendarObject.class,
				"setInterval"));
		SET_INT_METHODS.put(Integer.valueOf(CalendarObject.RECURRENCE_CALCULATOR), getSetIntegerMethod(
				CalendarObject.class, "setRecurrenceCalculator"));
		SET_INT_METHODS.put(Integer.valueOf(CalendarObject.ALARM), getSetIntegerMethod(AppointmentObject.class,
				"setAlarm"));

		SET_INT_METHODS.put(Integer.valueOf(Task.STATUS), getSetIntegerMethod(Task.class, "setStatus"));
		SET_INT_METHODS.put(Integer.valueOf(Task.PERCENT_COMPLETED), getSetIntegerMethod(Task.class,
				"setPercentComplete"));
		SET_INT_METHODS.put(Integer.valueOf(Task.PROJECT_ID), getSetIntegerMethod(Task.class, "setProjectID"));
		SET_INT_METHODS.put(Integer.valueOf(Task.PRIORITY), getSetIntegerMethod(Task.class, "setPriority"));

		SET_INT_METHODS.put(Integer.valueOf(AppointmentObject.SHOWN_AS), getSetIntegerMethod(AppointmentObject.class,
				"setShownAs"));

		// setter methods for long values
		SET_LONG_METHODS.put(Integer.valueOf(Task.ACTUAL_DURATION), getSetLongMethod(Task.class, "setActualDuration"));
		SET_LONG_METHODS.put(Integer.valueOf(Task.TARGET_DURATION), getSetLongMethod(Task.class, "setTargetDuration"));

		// setter methods for float values
		SET_FLOAT_METHODS.put(Integer.valueOf(Task.ACTUAL_COSTS), getSetFloatMethod(Task.class, "setActualCosts"));
		SET_FLOAT_METHODS.put(Integer.valueOf(Task.TARGET_COSTS), getSetFloatMethod(Task.class, "setTargetCosts"));

		// setter methods for date values
		SET_DATE_METHODS.put(Integer.valueOf(DataObject.CREATION_DATE), getSetDateMethod(DataObject.class,
				"setCreationDate"));
		SET_DATE_METHODS.put(Integer.valueOf(DataObject.LAST_MODIFIED), getSetDateMethod(DataObject.class,
				"setLastModified"));

		SET_DATE_METHODS.put(Integer.valueOf(CalendarObject.ALARM), getSetDateMethod(Task.class, "setAlarm"));
		SET_DATE_METHODS.put(Integer.valueOf(CalendarObject.START_DATE), getSetDateMethod(CalendarObject.class,
				"setStartDate"));
		SET_DATE_METHODS.put(Integer.valueOf(CalendarObject.END_DATE), getSetDateMethod(CalendarObject.class,
				"setEndDate"));
		SET_DATE_METHODS.put(Integer.valueOf(CalendarObject.RECURRENCE_DATE_POSITION), getSetDateMethod(
				CalendarObject.class, "setRecurrenceDatePosition"));
		SET_DATE_METHODS.put(Integer.valueOf(CalendarObject.CHANGE_EXCEPTIONS), getSetDateMethod(CalendarObject.class,
				"addChangeException"));
		SET_DATE_METHODS.put(Integer.valueOf(CalendarObject.DELETE_EXCEPTIONS), getSetDateMethod(CalendarObject.class,
				"addDeleteException"));
		SET_DATE_METHODS.put(Integer.valueOf(CalendarObject.UNTIL), getSetDateMethod(CalendarObject.class, "setUntil"));

		SET_DATE_METHODS.put(Integer.valueOf(Task.DATE_COMPLETED), getSetDateMethod(Task.class, "setDateCompleted"));

		SET_DATE_METHODS.put(Integer.valueOf(ContactObject.BIRTHDAY), getSetDateMethod(ContactObject.class,
				"setBirthday"));
		SET_DATE_METHODS.put(Integer.valueOf(ContactObject.ANNIVERSARY), getSetDateMethod(ContactObject.class,
				"setAnniversary"));

		// setter methods for string values
		SET_STRING_METHODS.put(Integer.valueOf(CommonObject.CATEGORIES), getSetStringMethod(CommonObject.class,
				"setCategories"));

		SET_STRING_METHODS.put(Integer.valueOf(CalendarObject.TITLE), getSetStringMethod(CalendarObject.class,
				"setTitle"));
		SET_STRING_METHODS.put(Integer.valueOf(CalendarObject.NOTE),
				getSetStringMethod(CalendarObject.class, "setNote"));

		SET_STRING_METHODS.put(Integer.valueOf(Task.BILLING_INFORMATION), getSetStringMethod(Task.class,
				"setBillingInformation"));
		SET_STRING_METHODS.put(Integer.valueOf(Task.CURRENCY), getSetStringMethod(Task.class, "setCurrency"));
		SET_STRING_METHODS.put(Integer.valueOf(Task.TRIP_METER), getSetStringMethod(Task.class, "setTripMeter"));
		SET_STRING_METHODS.put(Integer.valueOf(Task.COMPANIES), getSetStringMethod(Task.class, "setCompanies"));

		SET_STRING_METHODS.put(Integer.valueOf(AppointmentObject.LOCATION), getSetStringMethod(AppointmentObject.class,
				"setLocation"));

		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.DISPLAY_NAME), getSetStringMethod(ContactObject.class,
				"setDisplayName"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.GIVEN_NAME), getSetStringMethod(ContactObject.class,
				"setGivenName"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.SUR_NAME), getSetStringMethod(ContactObject.class,
				"setSurName"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.MIDDLE_NAME), getSetStringMethod(ContactObject.class,
				"setMiddleName"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.SUFFIX), getSetStringMethod(ContactObject.class,
				"setSuffix"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.TITLE),
				getSetStringMethod(ContactObject.class, "setTitle"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.STREET_HOME), getSetStringMethod(ContactObject.class,
				"setStreetHome"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.STREET_BUSINESS), getSetStringMethod(ContactObject.class,
				"setStreetBusiness"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.STREET_OTHER), getSetStringMethod(ContactObject.class,
				"setStreetOther"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.POSTAL_CODE_HOME), getSetStringMethod(ContactObject.class,
				"setPostalCodeHome"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.POSTAL_CODE_BUSINESS), getSetStringMethod(
				ContactObject.class, "setPostalCodeBusiness"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.POSTAL_CODE_OTHER), getSetStringMethod(
				ContactObject.class, "setPostalCodeOther"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.CITY_HOME), getSetStringMethod(ContactObject.class,
				"setCityHome"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.CITY_BUSINESS), getSetStringMethod(ContactObject.class,
				"setCityBusiness"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.CITY_OTHER), getSetStringMethod(ContactObject.class,
				"setCityOther"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.STATE_HOME), getSetStringMethod(ContactObject.class,
				"setStateHome"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.STATE_BUSINESS), getSetStringMethod(ContactObject.class,
				"setStateBusiness"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.STATE_OTHER), getSetStringMethod(ContactObject.class,
				"setStateOther"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.COUNTRY_HOME), getSetStringMethod(ContactObject.class,
				"setCountryHome"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.COUNTRY_BUSINESS), getSetStringMethod(ContactObject.class,
				"setCountryBusiness"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.COUNTRY_OTHER), getSetStringMethod(ContactObject.class,
				"setCountryOther"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.MARITAL_STATUS), getSetStringMethod(ContactObject.class,
				"setMaritalStatus"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.NUMBER_OF_CHILDREN), getSetStringMethod(
				ContactObject.class, "setNumberOfChildren"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.PROFESSION), getSetStringMethod(ContactObject.class,
				"setProfession"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.NICKNAME), getSetStringMethod(ContactObject.class,
				"setNickname"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.SPOUSE_NAME), getSetStringMethod(ContactObject.class,
				"setSpouseName"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.NOTE), getSetStringMethod(ContactObject.class, "setNote"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.DEPARTMENT), getSetStringMethod(ContactObject.class,
				"setDepartment"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.POSITION), getSetStringMethod(ContactObject.class,
				"setPosition"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.EMPLOYEE_TYPE), getSetStringMethod(ContactObject.class,
				"setEmployeeType"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.ROOM_NUMBER), getSetStringMethod(ContactObject.class,
				"setRoomNumber"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.NUMBER_OF_EMPLOYEE), getSetStringMethod(
				ContactObject.class, "setNumberOfEmployee"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.SALES_VOLUME), getSetStringMethod(ContactObject.class,
				"setSalesVolume"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.TAX_ID), getSetStringMethod(ContactObject.class,
				"setTaxID"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.COMMERCIAL_REGISTER), getSetStringMethod(
				ContactObject.class, "setCommercialRegister"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.BRANCHES), getSetStringMethod(ContactObject.class,
				"setBranches"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.BUSINESS_CATEGORY), getSetStringMethod(
				ContactObject.class, "setBusinessCategory"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.INFO), getSetStringMethod(ContactObject.class, "setInfo"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.MANAGER_NAME), getSetStringMethod(ContactObject.class,
				"setManagerName"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.ASSISTANT_NAME), getSetStringMethod(ContactObject.class,
				"setAssistantName"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.TELEPHONE_BUSINESS1), getSetStringMethod(
				ContactObject.class, "setTelephoneBusiness1"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.TELEPHONE_BUSINESS2), getSetStringMethod(
				ContactObject.class, "setTelephoneBusiness2"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.TELEPHONE_HOME1), getSetStringMethod(ContactObject.class,
				"setTelephoneHome1"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.TELEPHONE_HOME2), getSetStringMethod(ContactObject.class,
				"setTelephoneHome2"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.TELEPHONE_OTHER), getSetStringMethod(ContactObject.class,
				"setTelephoneOther"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.TELEPHONE_ASSISTANT), getSetStringMethod(
				ContactObject.class, "setTelephoneAssistant"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.TELEPHONE_CALLBACK), getSetStringMethod(
				ContactObject.class, "setTelephoneCallback"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.TELEPHONE_CAR), getSetStringMethod(ContactObject.class,
				"setTelephoneCar"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.TELEPHONE_COMPANY), getSetStringMethod(
				ContactObject.class, "setTelephoneCompany"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.TELEPHONE_IP), getSetStringMethod(ContactObject.class,
				"setTelephoneIP"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.TELEPHONE_ISDN), getSetStringMethod(ContactObject.class,
				"setTelephoneISDN"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.TELEPHONE_PAGER), getSetStringMethod(ContactObject.class,
				"setTelephonePager"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.TELEPHONE_PRIMARY), getSetStringMethod(
				ContactObject.class, "setTelephonePrimary"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.TELEPHONE_RADIO), getSetStringMethod(ContactObject.class,
				"setTelephoneRadio"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.TELEPHONE_TELEX), getSetStringMethod(ContactObject.class,
				"setTelephoneTelex"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.TELEPHONE_TTYTDD), getSetStringMethod(ContactObject.class,
				"setTelephoneTTYTTD"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.FAX_HOME), getSetStringMethod(ContactObject.class,
				"setFaxHome"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.FAX_BUSINESS), getSetStringMethod(ContactObject.class,
				"setFaxBusiness"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.FAX_OTHER), getSetStringMethod(ContactObject.class,
				"setFaxOther"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.CELLULAR_TELEPHONE1), getSetStringMethod(
				ContactObject.class, "setCellularTelephone1"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.CELLULAR_TELEPHONE2), getSetStringMethod(
				ContactObject.class, "setCellularTelephone2"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.EMAIL1), getSetStringMethod(ContactObject.class,
				"setEmail1"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.EMAIL2), getSetStringMethod(ContactObject.class,
				"setEmail2"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.EMAIL3), getSetStringMethod(ContactObject.class,
				"setEmail3"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.URL), getSetStringMethod(ContactObject.class, "setURL"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.INSTANT_MESSENGER1), getSetStringMethod(
				ContactObject.class, "setInstantMessenger1"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.INSTANT_MESSENGER2), getSetStringMethod(
				ContactObject.class, "setInstantMessenger2"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.USERFIELD01), getSetStringMethod(ContactObject.class,
				"setUserField01"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.USERFIELD02), getSetStringMethod(ContactObject.class,
				"setUserField02"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.USERFIELD03), getSetStringMethod(ContactObject.class,
				"setUserField03"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.USERFIELD04), getSetStringMethod(ContactObject.class,
				"setUserField04"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.USERFIELD05), getSetStringMethod(ContactObject.class,
				"setUserField05"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.USERFIELD06), getSetStringMethod(ContactObject.class,
				"setUserField06"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.USERFIELD07), getSetStringMethod(ContactObject.class,
				"setUserField07"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.USERFIELD08), getSetStringMethod(ContactObject.class,
				"setUserField08"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.USERFIELD09), getSetStringMethod(ContactObject.class,
				"setUserField09"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.USERFIELD10), getSetStringMethod(ContactObject.class,
				"setUserField10"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.USERFIELD11), getSetStringMethod(ContactObject.class,
				"setUserField11"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.USERFIELD12), getSetStringMethod(ContactObject.class,
				"setUserField12"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.USERFIELD13), getSetStringMethod(ContactObject.class,
				"setUserField13"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.USERFIELD14), getSetStringMethod(ContactObject.class,
				"setUserField14"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.USERFIELD15), getSetStringMethod(ContactObject.class,
				"setUserField15"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.USERFIELD16), getSetStringMethod(ContactObject.class,
				"setUserField16"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.USERFIELD17), getSetStringMethod(ContactObject.class,
				"setUserField17"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.USERFIELD18), getSetStringMethod(ContactObject.class,
				"setUserField18"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.USERFIELD19), getSetStringMethod(ContactObject.class,
				"setUserField19"));
		SET_STRING_METHODS.put(Integer.valueOf(ContactObject.USERFIELD20), getSetStringMethod(ContactObject.class,
				"setUserField20"));

		// setter methods for boolean values
		SET_BOOLEAN_METHODS.put(Integer.valueOf(CommonObject.PRIVATE_FLAG), getSetBooleanMethod(CommonObject.class,
				"setPrivateFlag"));

		SET_BOOLEAN_METHODS.put(Integer.valueOf(CalendarObject.NOTIFICATION), getSetBooleanMethod(CalendarObject.class,
				"setNotification"));

		SET_BOOLEAN_METHODS.put(Integer.valueOf(AppointmentObject.FULL_TIME), getSetBooleanMethod(
				AppointmentObject.class, "setFullTime"));
	}

	private static final String atdomain;

	static {
		String domain = "localhost";
		try {
			domain = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			LOG.error(e.getMessage(), e);
		}
		atdomain = new StringBuilder().append('@').append(domain).toString();
	}

	// ------------------------ END Prepare converter class
	// -------------------------------------

	private final SessionObject session;

	private final TimeZone timezone;

	private String organizerMailAddress;

	private boolean sendUTC = true;

	private boolean sendFloating;

	public OXContainerConverter(TimeZone timezone, String organizerMailAddress) {
		super();
		this.timezone = timezone;
		this.organizerMailAddress = organizerMailAddress;
		session = null;
	}

	public OXContainerConverter(SessionObject session) {
		super();
		this.session = session;
		this.timezone = TimeZone.getTimeZone(session.getUserObject().getTimeZone());
	}

	public void close() {
		if (LOG.isTraceEnabled()) {
			LOG.trace("OXContainerConverter.close()");
		}
	}

	public boolean isSendFloating() {
		return sendFloating;
	}

	public void setSendFloating(final boolean sendFloating) {
		this.sendFloating = sendFloating;
	}

	public boolean isSendUTC() {
		return sendUTC;
	}

	public void setSendUTC(final boolean sendUTC) {
		this.sendUTC = sendUTC;
	}

	public Task convertTask(final VersitObject object) throws ConverterException {
		try {
			final Task taskContainer = new Task();
			// CLASS
			PrivacyProperty(taskContainer, object, "CLASS", SET_BOOLEAN_METHODS.get(Task.PRIVATE_FLAG));
			// COMPLETED
			DateTimeProperty(taskContainer, object, "COMPLETED", SET_DATE_METHODS.get(Integer
					.valueOf(Task.DATE_COMPLETED)));
			// GEO is ignored
			// LAST-MODIFIED is ignored
			// LOCATION is ignored
			// ORGANIZER is ignored
			// PERCENT-COMPLETE
			IntegerProperty(taskContainer, object, "PERCENT-COMPLETE", SET_INT_METHODS.get(Task.PERCENT_COMPLETED));
			// PRIORITY
			Property property = object.getProperty("PRIORITY");
			if (property != null) {
				final int priority = ((Integer) property.getValue()).intValue();
				final int[] priorities = { Task.HIGH, Task.HIGH, Task.HIGH, Task.HIGH, Task.NORMAL, Task.LOW, Task.LOW,
						Task.LOW, Task.LOW };
				if (priority >= 1 && priority <= 9) {
					taskContainer.setPriority(priorities[priority - 1]);
				} else if (priority != 0) {
					throw new ConverterException("Invalid priority");
				}
			}
			// TODO RECURRENCE-ID
			// TODO SEQUENCE
			// STATUS
			property = object.getProperty("STATUS");
			if (property != null) {
				final String status = ((String) property.getValue()).toUpperCase();
				if (status.equals("NEEDS-ACTION")) {
					taskContainer.setStatus(Task.NOT_STARTED);
				} else if (status.equals("IN-PROCESS")) {
					taskContainer.setStatus(Task.IN_PROGRESS);
				} else if (status.equals("COMPLETED")) {
					taskContainer.setStatus(Task.DONE);
				} else if (status.equals("CANCELLED")) {
					taskContainer.setStatus(Task.DEFERRED);
				} else {
					throw new ConverterException("Unknown status: \"" + status + "\"");
				}
			}
			// SUMMARY
			StringProperty(taskContainer, object, "SUMMARY", SET_STRING_METHODS.get(Task.TITLE));
			// TODO UID
			// property = object.getProperty("UID");
			// if (property != null) {
			// String uid = property.getValue().toString();
			// if (uid.endsWith(atdomain))
			// task.setObjectID(Integer.parseInt(uid.substring(0, uid.length()
			// - atdomain.length())));
			// }
			// URL is ignored
			// DUE and DURATION
			if (!DateTimeProperty(taskContainer, object, "DUE", SET_DATE_METHODS.get(Task.END_DATE))) {
				DurationProperty(taskContainer, object, "DURATION", "DTSTART", SET_DATE_METHODS.get(Task.END_DATE));
			}
			// Multiple properties
			final int count = object.getPropertyCount();
			final StringBuilder cats = new StringBuilder();
			for (int i = 0; i < count; i++) {
				property = object.getProperty(i);
				// ATTACH is ignored
				// ATTENDEE
				if (property.name.equals("ATTENDEE")) {
					AttendeeProperty(taskContainer, property);
				}
				// CATEGORIES
				else if (property.name.equals("CATEGORIES")) {
					final ArrayList al = ((ArrayList) property.getValue());
					final int size = al.size();
					final Iterator j = al.iterator();
					for (int k = 0; k < size; k++) {
						cats.append(j.next());
						cats.append(',');
					}
				}
				// COMMENT is ignored
				// CONTACT is ignored
				// EXDATE is ignored
				// EXRULE is ignored
				// REQUEST-STATUS is ignored
				// TODO RELATED-TO
				// RESOURCES is ignored
				// RDATE is ignored
				// RRULE
				else if (property.name.equals("RRULE")) {
					RecurrenceProperty(taskContainer, property, object.getProperty("DTSTART"));
				}
			}
			if (cats.length() != 0) {
				cats.deleteCharAt(cats.length() - 1);
				taskContainer.setCategories(cats.toString());
			}
			// DESCRIPTION (fix: 7718)
			StringProperty(taskContainer, object, "DESCRIPTION", SET_STRING_METHODS.get(Task.NOTE));
			// VALARM
			AddAlarms(taskContainer, object);
			return taskContainer;
		} catch (Exception e) {
			LOG.error(e);
			throw new ConverterException(e);
		}
	}

	public CalendarDataObject convertAppointment(final VersitObject object) throws ConverterException {
		final CalendarDataObject appContainer = new CalendarDataObject();
		//CLASS
		PrivacyProperty(appContainer, object, "CLASS", SET_BOOLEAN_METHODS.get(Task.PRIVATE_FLAG));
		// CREATED is ignored
		// DESCRIPTION
		StringProperty(appContainer, object, "DESCRIPTION", SET_STRING_METHODS.get(AppointmentObject.NOTE));
		// DTSTART
		Property property = object.getProperty("DTSTART");
		if (property != null) {
			final DateTimeValue date = (DateTimeValue) property.getValue();
			if (date.isFloating) {
				date.calendar.setTimeZone(timezone);
			}
			date.calendar.set(Calendar.SECOND, 0);
			date.calendar.set(Calendar.MILLISECOND, 0);
			appContainer.setStartDate(date.calendar.getTime());
			appContainer.setFullTime(!date.hasTime);
		}
		// GEO is ignored
		// LAST-MODIFIED is ignored
		// LOCATION
		StringProperty(appContainer, object, "LOCATION", SET_STRING_METHODS.get(AppointmentObject.LOCATION));
		// ORGANIZER is ignored
		// PRIORITY is ignored
		// DTSTAMP is ignored
		// TODO SEQUENCE
		// STATUS is ignored
		// SUMMARY
		StringProperty(appContainer, object, "SUMMARY", SET_STRING_METHODS.get(AppointmentObject.TITLE));
		// TRANSP
		property = object.getProperty("TRANSP");
		if (property != null) {
			final String transp = ((String) property.getValue()).toUpperCase();
			if (transp.equals("OPAQUE")) {
				appContainer.setShownAs(AppointmentObject.RESERVED);
			} else if (transp.equals("TRANSPARENT")) {
				appContainer.setShownAs(AppointmentObject.FREE);
			} else {
				throw new ConverterException("Invalid transparency");
			}
		}
		// TODO UID
		// property = object.getProperty("UID");
		// if (property != null) {
		// String uid = property.getValue().toString();
		// if (uid.endsWith(atdomain))
		// app.setObjectID(Integer.parseInt(uid.substring(0, uid.length()
		// - atdomain.length())));
		// }
		// URL is ignored
		// TODO RECURRENCE-ID
		// DTEND and DURATION
		if (!DateTimeProperty(appContainer, object, "DTEND", SET_DATE_METHODS.get(AppointmentObject.END_DATE))
				&& !DurationProperty(appContainer, object, "DURATION", "DTSTART", SET_DATE_METHODS
						.get(AppointmentObject.END_DATE))) {
			DateTimeProperty(appContainer, object, "DSTART", SET_DATE_METHODS.get(AppointmentObject.END_DATE));
		}
		// Multiple properties
		final StringBuilder cats = new StringBuilder();
		final ArrayList exdates = new ArrayList();
		final int count = object.getPropertyCount();
		for (int i = 0; i < count; i++) {
			property = object.getProperty(i);
			// ATTACH is ignored
			// ATTENDEE
			if (property.name.equals("ATTENDEE")) {
				AttendeeProperty(appContainer, property);
			}
			// CATEGORIES
			else if (property.name.equals("CATEGORIES")) {
				final ArrayList al = ((ArrayList) property.getValue());
				final int size = al.size();
				final Iterator j = al.iterator();
				for (int k = 0; k < size; k++) {
					cats.append(j.next());
					cats.append(',');
				}
			}
			// COMMENT is ignored
			// CONTACT is ignored
			// EXDATE
			else if (property.name.equals("EXDATE")) {
				exdates.addAll((ArrayList) property.getValue());
			}
			// EXRULE is ignored
			// REQUEST-STATUS is ignored
			// TODO RELATED-TO
			// RESOURCES
			else if (property.name.equals("RESOURCES")) {
				final ArrayList al = ((ArrayList) property.getValue());
				final int size = al.size();
				final Iterator j = al.iterator();
				for (int k = 0; k < size; k++) {
					final ResourceParticipant p = new ResourceParticipant();
					p.setDisplayName((String) j.next());
					appContainer.addParticipant(p);
				}
			}
			// RDATE is ignored
			// RRULE
			else if (property.name.equals("RRULE")) {
				RecurrenceProperty(appContainer, property, object.getProperty("DTSTART"));
			}
		}
		if (cats.length() != 0) {
			cats.deleteCharAt(cats.length() - 1);
			appContainer.setCategories(cats.toString());
		}
		if (exdates.size() != 0) {
			Date[] dates = new Date[exdates.size()];
			for (int i = 0; i < dates.length; i++) {
				dates[i] = ((DateTimeValue) exdates.get(i)).calendar.getTime();
			}
			appContainer.setDeleteExceptions(dates);
		}
		// VALARM
		AddAlarms(appContainer, object);
		return appContainer;
	}

	public ContactObject convertContact(final VersitObject object) throws ConverterException {
		final ContactObject contactContainer = new ContactObject();
		// SOURCE is ignored
		// NAME is ignored
		// PROFILE is ignored
		// FN
		StringProperty(contactContainer, object, "FN", SET_STRING_METHODS.get(ContactObject.DISPLAY_NAME));
		// N
		Property property = object.getProperty("N");
		if (property != null) {
			final ArrayList N = (ArrayList) property.getValue();
			//fix for 7248
			if(N != null){
				for(int i = N.size(); i < 5; i++){
					N.add(null);
				}
			}
			//fix:end
			if (N.size() != 5) {
				throw new ConverterException("Invalid property N, has " + N.size() + " elements, not 5.");
			}
			ListValue(contactContainer, SET_STRING_METHODS.get(ContactObject.SUR_NAME), N.get(0), " ");
			ListValue(contactContainer, SET_STRING_METHODS.get(ContactObject.GIVEN_NAME), N.get(1), " ");
			ListValue(contactContainer, SET_STRING_METHODS.get(ContactObject.MIDDLE_NAME), N.get(2), " ");
			ListValue(contactContainer, SET_STRING_METHODS.get(ContactObject.TITLE), N.get(3), " ");
			ListValue(contactContainer, SET_STRING_METHODS.get(ContactObject.SUFFIX), N.get(4), " ");
		}
		// NICKNAME
		StringProperty(contactContainer, object, "NICKNAME", SET_STRING_METHODS.get(ContactObject.NICKNAME));
		// PHOTO
		property = object.getProperty("PHOTO");
		if (property != null) {
			String value;
			if (property.getValue() instanceof byte[]) {
				try {
					value = new BASE64Encoding().encode(new String((byte[]) property.getValue(), "ISO-8859-1"));
				} catch (IOException e) {
					final ConverterException ce = new ConverterException(e.getMessage());
					ce.initCause(e);
					throw ce;
				}
			} else {
				value = property.getValue().toString();
			}
			try {
				contactContainer.setImage1(value.getBytes("ISO-8859-1"));
			} catch (UnsupportedEncodingException e) {
				LOG.error("Image could not be set", e);
			}
		}
		// BDAY
		DateTimeProperty(contactContainer, object, "BDAY", SET_DATE_METHODS.get(ContactObject.BIRTHDAY));
		// MAILER is ignored
		// TZ is ignored
		// GEO is ignored
		// TITLE
		StringProperty(contactContainer, object, "TITLE", SET_STRING_METHODS.get(ContactObject.EMPLOYEE_TYPE));
		// ROLE
		StringProperty(contactContainer, object, "ROLE", SET_STRING_METHODS.get(ContactObject.POSITION));
		// LOGO is ignored
		// TODO AGENT
		// ORG
		property = object.getProperty("ORG");
		if (property != null) {
			final ArrayList elements = (ArrayList) property.getValue();
			if (elements.size() < 1) {
				throw new ConverterException("Invalid property ORG");
			}
			contactContainer.setCompany((String) elements.get(0));
			final int last = elements.size() - 1;
			if (last > 1) {
				final StringBuilder sb = new StringBuilder();
				sb.append(elements.get(1));
				for (int i = 2; i < last; i++) {
					sb.append(',');
					sb.append(elements.get(i));
				}
				contactContainer.setBranches(sb.toString());
			}
			if (elements.size() >= 2) {
				contactContainer.setDepartment((String) elements.get(last));
			}
		}
		// NOTE
		StringProperty(contactContainer, object, "NOTE", SET_STRING_METHODS.get(ContactObject.NOTE));
		// PRODID is ignored
		// REV is ignored
		// SORT-STRING is ignored
		// SOUND is ignored
		// URL
		StringProperty(contactContainer, object, "URL", SET_STRING_METHODS.get(ContactObject.URL));
		// TODO UID
		// property = object.getProperty("UID");
		// if (property != null) {
		// String uid = property.getValue().toString();
		// if (uid.endsWith(atdomain))
		// contact.setObjectID(Integer.parseInt(uid.substring(0, uid
		// .length()
		// - atdomain.length())));
		// }
		// VERSION is ignored
		// TODO CLASS
		// KEY is ignored

		// Multiple properties

		final int WORK = 0;
		final int HOME = 1;
		final int CELL = 2;
		final int CAR = 3;
		final int ISDN = 4;
		final int PAGER = 5;

		final int VOICE = 0;
		final int FAX = 1;

		final Method[][][] phones = {
				{
						{ SET_STRING_METHODS.get(ContactObject.TELEPHONE_BUSINESS1),
								SET_STRING_METHODS.get(ContactObject.TELEPHONE_BUSINESS2) },
						{ SET_STRING_METHODS.get(ContactObject.FAX_BUSINESS) } },
				{
						{ SET_STRING_METHODS.get(ContactObject.TELEPHONE_HOME1),
								SET_STRING_METHODS.get(ContactObject.TELEPHONE_HOME2) },
						{ SET_STRING_METHODS.get(ContactObject.FAX_HOME) } },
				{
						{ SET_STRING_METHODS.get(ContactObject.CELLULAR_TELEPHONE1),
								SET_STRING_METHODS.get(ContactObject.CELLULAR_TELEPHONE2) }, {} },
				{ { SET_STRING_METHODS.get(ContactObject.TELEPHONE_CAR) }, {} },
				{ { SET_STRING_METHODS.get(ContactObject.TELEPHONE_ISDN) }, {} },
				{ { SET_STRING_METHODS.get(ContactObject.TELEPHONE_PAGER) }, {} },
				{ { SET_STRING_METHODS.get(ContactObject.TELEPHONE_OTHER) },
						{ SET_STRING_METHODS.get(ContactObject.FAX_OTHER) } } };

		final int[][][] index = { { { 0 }, { 0 } }, { { 0 }, { 0 } }, { { 0 }, { 0 } }, { { 0 }, { 0 } },
				{ { 0 }, { 0 } }, { { 0 }, { 0 } }, { { 0 }, { 0 } } };

		final Method[] emails = { SET_STRING_METHODS.get(ContactObject.EMAIL1),
				SET_STRING_METHODS.get(ContactObject.EMAIL2), SET_STRING_METHODS.get(ContactObject.EMAIL3) };

		final int[] emailIndex = { 0 };

		final ArrayList<Object> cats = new ArrayList<Object>();

		final int count = object.getPropertyCount();
		for (int i = 0; i < count; i++) {
			property = object.getProperty(i);
			// ADR
			if (property.name.equals("ADR")) {
				boolean isHome = false, isWork = true;
				final Parameter type = property.getParameter("TYPE");
				if (type != null) {
					isWork = false;
					for (int j = 0; j < type.getValueCount(); j++) {
						String value = type.getValue(j).getText();
						isHome |= value.equalsIgnoreCase("home");
						isWork |= value.equalsIgnoreCase("work");
					}
				}
				final ArrayList A = (ArrayList) property.getValue();
				//fix for 7248
				if(A != null){
					for(int j = A.size(); j < 7; j++){
						A.add(null);
					}
				}
				//fix:end
				if (A == null || A.size() != 7) {
					throw new ConverterException("Invalid property ADR");
				}
				if (isWork) {
					ListValue(contactContainer, SET_STRING_METHODS.get(ContactObject.STREET_BUSINESS), A.get(2), "\n");
					ListValue(contactContainer, SET_STRING_METHODS.get(ContactObject.CITY_BUSINESS), A.get(3), "\n");
					ListValue(contactContainer, SET_STRING_METHODS.get(ContactObject.STATE_BUSINESS), A.get(4), "\n");
					ListValue(contactContainer, SET_STRING_METHODS.get(ContactObject.POSTAL_CODE_BUSINESS), A.get(5),
							"\n");
					ListValue(contactContainer, SET_STRING_METHODS.get(ContactObject.COUNTRY_BUSINESS), A.get(6), "\n");
				}
				if (isHome) {
					ListValue(contactContainer, SET_STRING_METHODS.get(ContactObject.STREET_HOME), A.get(2), "\n");
					ListValue(contactContainer, SET_STRING_METHODS.get(ContactObject.CITY_HOME), A.get(3), "\n");
					ListValue(contactContainer, SET_STRING_METHODS.get(ContactObject.STATE_HOME), A.get(4), "\n");
					ListValue(contactContainer, SET_STRING_METHODS.get(ContactObject.POSTAL_CODE_HOME), A.get(5), "\n");
					ListValue(contactContainer, SET_STRING_METHODS.get(ContactObject.COUNTRY_HOME), A.get(6), "\n");
				}
			}
			// LABEL is ignored
			// TEL
			else if (property.name.equals("TEL")) {
				int idx = WORK;
				boolean isVoice = false;
				boolean isFax = false;
				final Parameter type = property.getParameter("TYPE");
				if (type != null) {
					for (int j = 0; j < type.getValueCount(); j++) {
						String value = type.getValue(j).getText();
						if (idx == WORK) {
							if (value.equalsIgnoreCase("work")) {
								idx = WORK;
							} else if (value.equalsIgnoreCase("home")) {
								idx = HOME;
							} else if (value.equalsIgnoreCase("car")) {
								idx = CAR;
							} else if (value.equalsIgnoreCase("isdn")) {
								idx = ISDN;
							} else if (value.equalsIgnoreCase("cell")) {
								idx = CELL;
							} else if (value.equalsIgnoreCase("pager")) {
								idx = PAGER;
							}
						}
						if (value.equalsIgnoreCase("voice")) {
							isVoice = true;
						} else if (value.equalsIgnoreCase("fax")) {
							isFax = true;
						}
					}
				}
				if (!isVoice && !isFax) {
					isVoice = true;
				}
				Object value = property.getValue();
				if (isVoice) {
					ComplexProperty(contactContainer, phones[idx][VOICE], index[idx][VOICE], value);
				}
				if (isFax) {
					ComplexProperty(contactContainer, phones[idx][FAX], index[idx][FAX], value);
				}
			}
			// EMAIL
			else if (property.name.equals("EMAIL")) {
				String value = property.getValue().toString();
				//fix for: 7249
				boolean isProperEmailAddress = value != null && value.length() > 0;
				if(isProperEmailAddress){
					try {
						InternetAddress  ia = new InternetAddress(value);
						ia.validate();
					} catch (AddressException e) {
						isProperEmailAddress = false;
					}
				} 
				//fix: end
				if (isProperEmailAddress) {
					ComplexProperty(contactContainer, emails, emailIndex, value);
				}else {
					//fix for: 7719
					final Parameter type = property.getParameter("TYPE");
					if(type != null && type.getValue(0) != null && type.getValue(0).getText() != null){
						if( "TLX".equals( type.getValue(0).getText() ) ){
							contactContainer.setTelephoneTelex( property.getValue().toString() );
						}
					}
					//fix:end
				}
			}
			// CATEGORIES
			else if (property.name.equals("CATEGORIES")) {
				cats.addAll((ArrayList) property.getValue());
			}
		}
		ListValue(contactContainer, SET_STRING_METHODS.get(ContactObject.CATEGORIES), cats, ",");

		return contactContainer;
	}

	private boolean IntegerProperty(final Object containerObj, final VersitObject object, final String VersitName,
			final Method setStringMethod) throws ConverterException {
		try {
			final Property property = object.getProperty(VersitName);
			if (property == null) {
				return false;
			}
			if (property.getValue() instanceof Integer) {
				final Integer val = (Integer) property.getValue();
				final Object[] args = { val };
				setStringMethod.invoke(containerObj, args);
				return true;
			}
			return false;
		} catch (Exception e) {
			throw new ConverterException(e);
		}
	}

	private boolean StringProperty(final Object containerObj, final VersitObject object, final String VersitName,
			final Method setStringMethod) throws ConverterException {
		try {
			final Property property = object.getProperty(VersitName);
			if (property == null) {
				return false;
			}
			final Object[] args = { property.getValue().toString() };
			setStringMethod.invoke(containerObj, args);
			return true;
		} catch (Exception e) {
			throw new ConverterException(e);
		}
	}

	private boolean PrivacyProperty(final Object containerObj, final VersitObject object, final String VersitName,
			final Method setPrivacyMethod) throws ConverterException {
		try {
			final Property property = object.getProperty(VersitName);
			if (property == null) {
				return false;
			}
			String privacy = (String) property.getValue();
			
			boolean isPrivate = false;
			if("PRIVATE".equals(privacy)){
				isPrivate = true;
			} 
			if("CONFIDENTIAL".equals(privacy)){
				throw new ConverterPrivacyException();
			}
			final Object[] args = { isPrivate };
			setPrivacyMethod.invoke(containerObj, args);
			return false;
		} catch (ConverterPrivacyException e){
			throw e;
		} catch (Exception e){
			throw new ConverterException(e);
		}
	}
	
	private boolean DateTimeProperty(final Object containerObj, final VersitObject object, final String VersitName,
			final Method setDateMethod) throws ConverterException {
		try {
			final Property property = object.getProperty(VersitName);
			if (property == null) {
				return false;
			}
			final DateTimeValue date = (DateTimeValue) property.getValue();
			if (date.isFloating) {
				date.calendar.setTimeZone(timezone);
			}
			date.calendar.set(Calendar.SECOND, 0);
			date.calendar.set(Calendar.MILLISECOND, 0);
			final Object[] args = { date.calendar.getTime() };
			setDateMethod.invoke(containerObj, args);
			return true;
		} catch (Exception e) {
			throw new ConverterException(e);
		}
	}

	private static boolean DurationProperty(final Object containerObj, final VersitObject object,
			final String DurationName, final String StartName, final Method setDateMethod) throws ConverterException {
		try {
			Property property = object.getProperty(DurationName);
			if (property == null) {
				return false;
			}
			final DurationValue dur = (DurationValue) property.getValue();
			property = object.getProperty(StartName);
			if (property == null) {
				throw new ConverterException("Duration without start is not supported.");
			}
			final Calendar cal = ((DateTimeValue) property.getValue()).calendar;
			cal.add(Calendar.WEEK_OF_YEAR, dur.Negative ? -dur.Weeks : dur.Weeks);
			cal.add(Calendar.DATE, dur.Negative ? -dur.Days : dur.Days);
			cal.add(Calendar.HOUR, dur.Negative ? -dur.Hours : dur.Hours);
			cal.add(Calendar.MINUTE, dur.Negative ? -dur.Minutes : dur.Minutes);
			cal.add(Calendar.SECOND, dur.Negative ? -dur.Seconds : dur.Seconds);
			final Object[] args = { cal.getTime() };
			setDateMethod.invoke(containerObj, args);
			return true;
		} catch (Exception e) {
			throw new ConverterException(e);
		}
	}

	private void AttendeeProperty(final CalendarObject calContainerObj, final Property property)
			throws ConverterException {
		try {
			final String mail = ((URI) property.getValue()).getSchemeSpecificPart();
			final Participant participant;
			if(isInternalUser(mail)){
				//fix for bug 8475
				participant = new UserParticipant(
						getInternalUser(mail).
							getContactId()
				);
				//end:fix
			} else {
				participant = new ExternalUserParticipant(mail);
				participant.setDisplayName(mail);
			}
			calContainerObj.addParticipant(participant);
		} catch (Exception e) {
			throw new ConverterException(e);
		}
	}

	/**
	 * Finds out whether a user is internal, since internal users get treated differently
	 * when entering appointments or tasks.
	 * 
	 * @param mail - Mail address as string
	 * @return true if is internal user, false otherwise
	 */
	public boolean isInternalUser(String mail) {
		try {
			final UserStorage us = UserStorage.getInstance(session.getContext());
			final User uo = us.searchUser(mail);
			return uo != null;
		} catch (LdapException e){
			return false;
		}
	}
	
	/**
	 * Finds an internal user by its e-mail address. Note that an e-mail
	 * address is unique, but the identifier for an internal user is
	 * its id.
	 * 
	 * Should only be called after using <code>isInternalUser</code> or
	 * you have to live with the LdapException.
	 */
	public User getInternalUser(String mail) throws LdapException {
		final UserStorage us = UserStorage.getInstance(session.getContext());
		return us.searchUser(mail);
	}

	private static void RecurrenceProperty(final CalendarObject calContainerObj, final Property property,
			final Property start) throws ConverterException {
		final RecurrenceValue recur = (RecurrenceValue) property.getValue();
		if (start == null) {
			throw new ConverterException("RRULE without DTSTART");
		}
		final Calendar cal = ((DateTimeValue) start.getValue()).calendar;
		final int[] recurTypes = { Task.NONE, Task.NONE, Task.NONE, Task.DAILY, Task.WEEKLY, Task.MONTHLY, Task.YEARLY };
		calContainerObj.setRecurrenceType(recurTypes[recur.Freq]);
		if (recur.Until != null) {
			calContainerObj.setUntil(recur.Until.calendar.getTime());
		}
		if (recur.Count != -1) {
			calContainerObj.setOccurrence(recur.Count);
			//throw new ConverterException("COUNT is not supported.");
		}
		calContainerObj.setInterval(recur.Interval);
		switch (recur.Freq) {
		case RecurrenceValue.YEARLY:
			int month;
			if (recur.ByMonth.length > 0) {
				if (recur.ByMonth.length > 1) {
					throw new ConverterException("Multiple months of the year are not supported.");
				}
				month = recur.ByMonth[0] - 1 + Calendar.JANUARY;
			} else {
				month = cal.get(Calendar.MONTH);
			}
			calContainerObj.setMonth(month);
		// no break
		case RecurrenceValue.MONTHLY:
			if (recur.ByMonthDay.length > 0) {
				if (recur.ByDay.size() != 0) {
					throw new ConverterException("Simultaneous day in month and weekday in month are not supported.");
				}
				if (recur.ByMonthDay.length > 1) {
					throw new ConverterException("Multiple days of the month are not supported.");
				}
				final int dayOfMonth = recur.ByMonthDay[0];
				if (dayOfMonth <= 0) {
					throw new ConverterException("Counting days from end of the month is not supported.");
				}
				calContainerObj.setDayInMonth(dayOfMonth);
			} else if (recur.ByDay.size() > 0) {
				int days = 0, week = 0;
				final int size = recur.ByDay.size();
				final Iterator j = recur.ByDay.iterator();
				for (int k = 0; k < size; k++) {
					final RecurrenceValue.Weekday wd = (RecurrenceValue.Weekday) j.next();
					days |= 1 << (wd.day - Calendar.SUNDAY);
					if (week != 0 && week != wd.week) {
						throw new ConverterException("Multiple weeks of month are not supported.");
					}
					week = wd.week;
				}
				calContainerObj.setDays(days);
				calContainerObj.setDayInMonth(week);
			} else {
				calContainerObj.setDayInMonth(cal.get(Calendar.DAY_OF_MONTH));
			}
			break;
		case RecurrenceValue.WEEKLY:
		case RecurrenceValue.DAILY: //fix: 7703
			int days = 0;
			final int size = recur.ByDay.size();
			final Iterator j = recur.ByDay.iterator();
			for (int k = 0; k < size; k++) {
				days |= 1 << ((RecurrenceValue.Weekday) j.next()).day - Calendar.SUNDAY;
			}
			if (days == 0) {
				days = 1 << cal.get(Calendar.DAY_OF_WEEK);
			}
			calContainerObj.setDays(days);
			break;
		default:
			throw new ConverterException("Unknown Recurrence Property: " + recur.Freq);
		}
	}

	private static void AddAlarms(final CalendarObject calContainerObj, final VersitObject object)
			throws ConverterException {
		final int count = object.getChildCount();
		for (int i = 0; i < count; i++) {
			final VersitObject alarm = object.getChild(i);
			Property property = alarm.getProperty("ACTION");
			//if (property != null && property.getValue().toString().equalsIgnoreCase("EMAIL")) {
			if (property != null && property.getValue().toString().equalsIgnoreCase("DISPLAY")) { //bugfix: 7473
				property = alarm.getProperty("TRIGGER");
				if (property != null) {
					int time;
					if (property.getValue() instanceof DurationValue) {
						final DurationValue trigger = (DurationValue) property.getValue();
						if (trigger.Months != 0 || trigger.Years != 0) {
							throw new ConverterException("Irregular durations not supported");
						}
						time = trigger.Minutes + (trigger.Hours + (trigger.Days + 7 * trigger.Weeks) * 24) * 60;
						if (trigger.Negative) { //note: This does not make sense currently, because "NEGATIVE" is never set
							time = -time;
						}
						/*fix for 7473: 
						 * TRIGGERs in ICAL are always negative 
						 * (because they are _before_ the event), 
						 * alarms in OX are always positive 
						 * (because there is no reason for them to 
						 * be _after_ the event).
						 */
						time = -time;
						//fix:end
					} else {
						final DateTimeValue trigger = (DateTimeValue) property.getValue();
						property = object.getProperty("DTSTART");
						if (property == null) {
							throw new ConverterException("VALARM without DTSTART not supported");
						}
						time = (int) (((DateTimeValue) property.getValue()).calendar.getTimeInMillis() - trigger.calendar
								.getTimeInMillis());
					}
					if (calContainerObj instanceof AppointmentObject) {
						final AppointmentObject appObj = (AppointmentObject) calContainerObj;
						appObj.setAlarm(time);
						appObj.setAlarmFlag(true); //bugfix: 7473
					} else if (calContainerObj instanceof Task) {
						final Task taskObj = (Task) calContainerObj;
						taskObj.setAlarm(new Date(taskObj.getStartDate().getTime() - (time * 60 * 1000)));
						taskObj.setAlarmFlag(true); //bugfix: 7473
					}
				}
			}
		}
	}

	private static void ListValue(final Object containerObj, final Method setMethod, final Object list,
			final String separator) throws ConverterException {
		try {
			final ArrayList al = (ArrayList) list;
			if (al == null || al.size() == 0) {
				return;
			}
			final StringBuilder sb = new StringBuilder();
			Object val = al.get(0);
			if (val != null) {
				sb.append(val);
			}
			final int count = al.size();
			for (int i = 1; i < count; i++) {
				sb.append(separator);
				val = al.get(i);
				if (val != null) {
					sb.append(val);
				}
			}
			final Object[] args = { sb.toString() };
			setMethod.invoke(containerObj, args);
		} catch (Exception e) {
			throw new ConverterException(e);
		}
	}

	private static void ComplexProperty(final Object containerObj, final Method[] phones, final int[] index,
			final Object value) throws ConverterException {
		try {
			if (index[0] >= phones.length) {
				return;
			}
			final Object[] args = { value };
			phones[index[0]++].invoke(containerObj, args);
		} catch (Exception e) {
			throw new ConverterException(e);
		}
	}

	public VersitObject convertTask(final Task task) throws ConverterException {
		final VersitObject object = new VersitObject("VTODO");
		// TODO CLASS
		addProperty(object, "CLASS", "PUBLIC");
		// COMPLETED
		addDateTime(object, "COMPLETED", task.getDateCompleted());
		// CREATED
		addDateTime(object, "CREATED", task.getCreationDate());
		// DESCRIPTION
		addProperty(object, "DESCRIPTION", task.getNote());
		// DTSTAMP
		addDateTime(object, "DTSTAMP", new Date());
		// DTSTART
		addWeirdTaskDate(object, "DTSTART", task.getStartDate());
		// GEO is ignored
		// LAST-MODIFIED
		addDateTime(object, "LAST-MODIFIED", task.getLastModified());
		// LOCATION is ignored
		// ORGANIZER
		if (organizerMailAddress != null) {
			addAddress(object, "ORGANIZER", organizerMailAddress);
		} else {
			addAddress(object, "ORGANIZER", task.getCreatedBy());
		}
		// PERCENT-COMPLETE
		addProperty(object, "PERCENT-COMPLETE", task.getPercentComplete());
		// PRIORITY
		final int[] priorities = { 9, 5, 1 };
		final int priority = task.getPriority();
		/*
		 * TODO REMOVED DUE REMOVAL OF com.openexchange.groupware.links
		 * 
		 * if (priority >= OXTask.LOW && priority <= OXTask.HIGH)
		 * addProperty(object, "PRIORITY", new Integer(priorities[priority -
		 * OXTask.LOW])); else throw new ConverterException("Invalid priority");
		 */
		// TODO RECURRENCE-ID
		// TODO SEQUENCE
		// STATUS
		final String[] statuses = { "NEEDS-ACTION", "IN-PROCESS", "COMPLETED", "NEEDS-ACTION", "CANCELLED" };
		final int status = task.getStatus();
		/*
		 * TODO REMOVED DUE REMOVAL OF com.openexchange.groupware.tasks
		 * 
		 * if (status >= OXTask.NOT_STARTED && status <= OXTask.DEFERRED)
		 * addProperty(object, "STATUS", statuses[status - OXTask.NOT_STARTED]);
		 * 
		 * else throw new ConverterException("Invlaid status");
		 */
		// SUMMARY
		addProperty(object, "SUMMARY", task.getTitle());
		// UID
		addProperty(object, "UID", task.getObjectID() + atdomain);
		// URL is ignored
		// DUE and DURATION
		addWeirdTaskDate(object, "DUE", task.getEndDate());
		// ATTACH
		// TODO addAttachments(object, task, OXAttachment.TASK);
		// ATTENDEE
		if (task.containsParticipants()) {
			final int length = task.getParticipants().length;
			final Iterator i = new ArrayIterator(task.getParticipants());
			for (int k = 0; k < length; k++) {
				final Participant p = (Participant) i.next();
				if (p.getType() == Participant.USER) {
					addAddress(object, "ATTENDEE", p.getEmailAddress());
				}
			}
		}
		// CATEGORIES
		final ArrayList<String> categories = new ArrayList<String>();
		if (task.getCategories() != null) {
			final StringTokenizer tokenizer = new StringTokenizer(task.getCategories(), ",");
			while (tokenizer.hasMoreTokens()) {
				categories.add(tokenizer.nextToken());
			}
		}
		addProperty(object, "CATEGORIES", categories);
		// COMMENT is ignored
		// CONTACT is ignored
		// EXDATE is ignored
		// EXRULE is ignored
		// REQUEST-STATUS is ignored
		// TODO RELATED-TO
		// RESOURCES is ignored
		// RDATE is ignored
		// RRULE
		addRecurrence(object, "RRULE", task);
		// TODO VALARM
		return object;
	}

	public VersitObject convertAppointment(final AppointmentObject app) throws ConverterException {
		final VersitObject object = new VersitObject("VEVENT");
		// TODO CLASS
		addProperty(object, "CLASS", "PUBLIC");
		// CREATED
		addDateTime(object, "CREATED", app.getCreationDate());
		// DESCRIPTION
		addProperty(object, "DESCRIPTION", app.getNote());
		// DTSTART
		if (app.getFullTime()) {
			addWeirdTaskDate(object, "DTSTART", app.getStartDate());
		} else {
			addDateTime(object, "DTSTART", app.getStartDate());
		}
		// GEO is ignored
		// LAST-MODIFIED
		addDateTime(object, "LAST-MODIFIED", app.getLastModified());
		// LOCATION
		addProperty(object, "LOCATION", app.getLocation());
		// ORGANIZER
		if (organizerMailAddress != null) {
			addAddress(object, "ORGANIZER", organizerMailAddress);
		} else {
			addAddress(object, "ORGANIZER", app.getCreatedBy());
		}
		// PRIORITY is ignored
		// DTSTAMP
		addDateTime(object, "DTSTAMP", new Date());
		// TODO SEQUENCE
		// STATUS is ignored
		// SUMMARY
		addProperty(object, "SUMMARY", app.getTitle());
		// TRANSP
		addProperty(object, "TRANSP", app.getShownAs() == AppointmentObject.FREE ? "TRANSPARENT" : "OPAQUE");
		// UID
		addProperty(object, "UID", app.getObjectID() + atdomain);
		// URL is ignored
		// TODO RECURRENCE-ID
		// DTEND and DURATION
		if (app.getFullTime()) {
			final Calendar cal = new GregorianCalendar();
			cal.setTimeZone(timezone);
			cal.setTime(app.getEndDate());
			cal.add(Calendar.HOUR_OF_DAY, -24);
			final Date end = cal.getTime();
			if (end.after(app.getStartDate())) {
				addWeirdTaskDate(object, "DTEND", end);
			}
		} else {
			addDateTime(object, "DTEND", app.getEndDate());
		}
		// ATTACH
		// TODO addAttachments(object, app, OXAttachment.APPOINTMENT);
		// ATTENDEE
		Iterator i = null;
		if (app.containsParticipants()) {
			final int length = app.getParticipants().length;
			i = new ArrayIterator(app.getParticipants());
			for (int k = 0; k < length; k++) {
				final Participant p = (Participant) i.next();
				if (p.getType() == Participant.USER) {
					addAddress(object, "ATTENDEE", p.getEmailAddress());
				}
			}
		}
		// CATEGORIES
		final String cat_str = app.getCategories();
		if (cat_str != null) {
			final ArrayList<String> categories = new ArrayList<String>();
			final StringTokenizer tokenizer = new StringTokenizer(cat_str, ",");
			while (tokenizer.hasMoreTokens()) {
				categories.add(tokenizer.nextToken());
			}
			addProperty(object, "CATEGORIES", categories);
		}
		// COMMENT is ignored
		// CONTACT is ignored
		// EXDATE
		final ArrayList<DateTimeValue> exlist = new ArrayList<DateTimeValue>();
		addExceptions(exlist, app.getDeleteException());
		addExceptions(exlist, app.getChangeException());
		if (!exlist.isEmpty()) {
			addProperty(object, "EXDATE", exlist);
		}
		// EXRULE is ignored
		// REQUEST-STATUS is ignored
		// TODO RELATED-TO
		// RESOURCES
		final ArrayList<String> resources = new ArrayList<String>();
		if (app.containsParticipants()) {
			final int length = app.getParticipants().length;
			i = new ArrayIterator(app.getParticipants());
			for (int k = 0; k < length; k++) {
				final Participant p = (Participant) i.next();
				if (p.getType() == Participant.RESOURCE) {
					resources.add(String.valueOf(p.getIdentifier()));
				}
			}
			if (resources.size() > 0) {
				addProperty(object, "RESOURCES", resources);
			}
		}
		// RDATE is ignored
		// RRULE
		addRecurrence(object, "RRULE", app);
		// TODO VALARM
		return object;
	}

	public VersitObject convertContact(final ContactObject contact, final String version) throws ConverterException {
		final VersitObject object = new VersitObject("VCARD");
		// VERSION
		addProperty(object, "VERSION", version);
		// PRODID
		addProperty(object, "PRODID", "OPEN-XCHANGE");
		// SOURCE is ignored
		// NAME is ignored
		// PROFILE is ignored
		// FN
		addProperty(object, "FN", contact.getDisplayName());
		// N
		final ArrayList<ArrayList> n = new ArrayList<ArrayList>();
		n.add(makeList(contact.getSurName()));
		n.add(makeList(contact.getGivenName()));
		n.add(getList(contact.getMiddleName(), ' '));
		n.add(getList(contact.getTitle(), ' '));
		n.add(getList(contact.getSuffix(), ' '));
		addProperty(object, "N", n);
		// NICKNAME
		addProperty(object, "NICKNAME", getList(contact.getNickname(), ','));
		// PHOTO
		String s = null;
		if (contact.getImage1() != null) {
			try {
				s = new String(contact.getImage1(), "ISO-8859-1");
			} catch (UnsupportedEncodingException e2) {
				LOG.error(e2);
				throw new ConverterException(e2);
			}
		}
		if (s != null) {
			try {
				addProperty(object, "PHOTO", "VALUE", new String[] { "URI" }, new URI(s));
			} catch (URISyntaxException e) {
				try {
					final Parameter type = new Parameter("TYPE");
					type.addValue(new ParameterValue("JPEG"));
					addProperty(object, "PHOTO", "ENCODING", new String[] { "B" },
							(new BASE64Encoding()).decode(s).getBytes("ISO-8859-1")).addParameter(type);
				} catch (IOException e1) {
					final ConverterException ce = new ConverterException(e.getMessage());
					ce.initCause(e1);
					throw ce;
				}
			}
		}
		// BDAY
		addDate(object, "BDAY", contact.getBirthday(), false);
		// ADR
		addADR(object, contact, new String[] { "work" }, ContactObject.STREET_BUSINESS, ContactObject.CITY_BUSINESS,
				ContactObject.STATE_BUSINESS, ContactObject.POSTAL_CODE_BUSINESS, ContactObject.COUNTRY_BUSINESS);
		// LABEL is ignored
		// TEL
		addProperty(object, "TEL", "TYPE", new String[] { "work", "voice" }, contact.getTelephoneBusiness1());
		addProperty(object, "TEL", "TYPE", new String[] { "work", "voice" }, contact.getTelephoneBusiness2());
		addProperty(object, "TEL", "TYPE", new String[] { "work", "fax" }, contact.getFaxBusiness());
		addProperty(object, "TEL", "TYPE", new String[] { "car", "voice" }, contact.getTelephoneCar());
		addProperty(object, "TEL", "TYPE", new String[] { "home", "voice" }, contact.getTelephoneHome1());
		addProperty(object, "TEL", "TYPE", new String[] { "home", "voice" }, contact.getTelephoneHome2());
		addProperty(object, "TEL", "TYPE", new String[] { "home", "fax" }, contact.getFaxHome());
		addProperty(object, "TEL", "TYPE", new String[] { "cell", "voice" }, contact.getCellularTelephone1());
		addProperty(object, "TEL", "TYPE", new String[] { "cell", "voice" }, contact.getCellularTelephone2());
		// addProperty(object, "TEL", "TYPE", null, contact
		// .get(OXContact.PHONE_OTHER));
		// addProperty(object, "TEL", "TYPE", new String[] { "fax" }, contact
		// .get(OXContact.FAX_OTHER));
		addProperty(object, "TEL", "TYPE", new String[] { "isdn" }, contact.getTelephoneISDN());
		addProperty(object, "TEL", "TYPE", new String[] { "pager" }, contact.getTelephonePager());
		// EMAIL
		addProperty(object, "EMAIL", contact.getEmail1());
		addProperty(object, "EMAIL", contact.getEmail2());
		addProperty(object, "EMAIL", contact.getEmail3());
		// MAILER is ignored
		// TZ is ignored
		// GEO is ignored
		// TITLE
		addProperty(object, "TITLE", contact.getEmployeeType());
		// ROLE
		addProperty(object, "ROLE", contact.getPosition());
		// LOGO is ignored
		// TODO AGENT
		// ORG
		final ArrayList<String> list = new ArrayList<String>();
		list.add(s = contact.getCompany());
		boolean set = (s != null);
		s = contact.getBranches();
		if (s != null) {
			final StringTokenizer st = new StringTokenizer(s, ",");
			set |= st.hasMoreTokens();
			while (st.hasMoreTokens()) {
				list.add(st.nextToken());
			}
		}
		s = contact.getDepartment();
		set |= (s != null);
		if (s != null) {
			list.add(s);
		}
		if (set) {
			addProperty(object, "ORG", list);
		}
		// CATEGORIES
		s = contact.getCategories();
		if (s != null) {
			addProperty(object, "CATEGORIES", getList(s, ','));
		}
		// NOTE
		addProperty(object, "NOTE", contact.getNote());
		// REV
		addDateTime(object, "REV", contact.getLastModified());
		// SORT-STRING is ignored
		// SOUND is ignored
		// URL
		addProperty(object, "URL", contact.getURL());
		// UID
		addProperty(object, "UID", contact.getObjectID() + atdomain);
		// TODO CLASS
		// KEY is ignored
		return object;
	}

	private void addADR(final VersitObject object, final ContactObject contactContainer, final String[] type,
			final int street, final int city, final int state, final int postalCode, final int country)
			throws ConverterException {
		try {
			final ArrayList<ArrayList> adr = new ArrayList<ArrayList>(7);
			adr.add(null);
			adr.add(null);
			adr.add(makeList(getStreet(street, contactContainer)));
			adr.add(makeList(getCity(city, contactContainer)));
			adr.add(makeList(getState(state, contactContainer)));
			adr.add(makeList(getPostalCode(postalCode, contactContainer)));
			adr.add(makeList(getCountry(country, contactContainer)));
			addProperty(object, "ADR", "TYPE", type, adr);
		} catch (Exception e) {
			throw new ConverterException(e);
		}
	}

	private String getStreet(final int id, final ContactObject contactContainer) throws Exception {

		switch (id) {
		case ContactObject.STREET_BUSINESS:
			return contactContainer.getStreetBusiness();
		case ContactObject.STREET_HOME:
			return contactContainer.getStreetHome();
		case ContactObject.STREET_OTHER:
			return contactContainer.getStreetOther();
		default:
			throw new Exception("Unknown street constant " + id);
		}
	}

	private String getCity(final int id, final ContactObject contactContainer) throws Exception {

		switch (id) {
		case ContactObject.CITY_BUSINESS:
			return contactContainer.getCityBusiness();
		case ContactObject.CITY_HOME:
			return contactContainer.getCityHome();
		case ContactObject.CITY_OTHER:
			return contactContainer.getCityOther();
		default:
			throw new Exception("Unknown city constant " + id);
		}
	}

	private String getState(final int id, final ContactObject contactContainer) throws Exception {

		switch (id) {
		case ContactObject.STATE_BUSINESS:
			return contactContainer.getStateBusiness();
		case ContactObject.STATE_HOME:
			return contactContainer.getStateHome();
		case ContactObject.STATE_OTHER:
			return contactContainer.getStateOther();
		default:
			throw new Exception("Unknown state constant " + id);
		}
	}

	private String getCountry(final int id, final ContactObject contactContainer) throws Exception {

		switch (id) {
		case ContactObject.COUNTRY_BUSINESS:
			return contactContainer.getCountryBusiness();
		case ContactObject.COUNTRY_HOME:
			return contactContainer.getCountryHome();
		case ContactObject.COUNTRY_OTHER:
			return contactContainer.getCountryOther();
		default:
			throw new Exception("Unknown country constant " + id);
		}
	}

	private String getPostalCode(final int id, final ContactObject contactContainer) throws Exception {

		switch (id) {
		case ContactObject.POSTAL_CODE_BUSINESS:
			return contactContainer.getPostalCodeBusiness();
		case ContactObject.POSTAL_CODE_HOME:
			return contactContainer.getPostalCodeHome();
		case ContactObject.POSTAL_CODE_OTHER:
			return contactContainer.getPostalCodeOther();
		default:
			throw new Exception("Unknown postal code constant " + id);
		}
	}

	private static class ArrayIterator implements Iterator {

		private final int size;

		private int cursor;

		private final Object array;

		public ArrayIterator(Object array) {
			Class type = array.getClass();
			if (!type.isArray()) {
				throw new IllegalArgumentException("MailInterface.ArrayIterator:\tInvalid type: " + type);
			}
			this.array = array;
			this.size = Array.getLength(array);
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		public boolean hasNext() {
			return (cursor < size);
		}

		public Object next() {
			return Array.get(array, cursor++);
		}
	}

	private static void addProperty(final VersitObject object, final String name, final Object value) {
		if (value == null) {
			return;
		}
		final Property property = new Property(name);
		property.setValue(value);
		object.addProperty(property);
	}

	private static Property addProperty(final VersitObject object, final String name, final String paramName,
			final String[] param, final Object value) {
		if (value == null) {
			return null;
		}
		final Property property = new Property(name);
		if (param != null && param.length > 0) {
			final Parameter parameter = new Parameter(paramName);
			for (int i = 0; i < param.length; i++) {
				parameter.addValue(new ParameterValue(param[i]));
			}
			property.addParameter(parameter);
		}
		property.setValue(value);
		object.addProperty(property);
		return property;
	}

	private void addDateTime(final VersitObject object, final String name, final Date value) {
		if (value == null) {
			return;
		}
		final DateTimeValue dt = new DateTimeValue();
		dt.calendar.setTimeZone(sendUTC ? DateTimeValue.GMT : timezone);
		dt.calendar.setTime(value);
		dt.isUTC = sendUTC;
		dt.isFloating = sendFloating;
		final Property property = new Property(name);
		property.setValue(dt);
		object.addProperty(property);
	}

	private static void addDate(final VersitObject object, final String name, final Date value, final boolean setValue) {
		if (value == null) {
			return;
		}
		final DateTimeValue dt = new DateTimeValue();
		dt.calendar.setTimeZone(DateTimeValue.GMT);
		dt.calendar.setTime(value);
		dt.hasTime = false;
		final Property property = new Property(name);
		if (setValue) {
			final Parameter parameter = new Parameter("VALUE");
			parameter.addValue(new ParameterValue("DATE"));
			property.addParameter(parameter);
		}
		property.setValue(dt);
		object.addProperty(property);
	}

	private void addWeirdTaskDate(final VersitObject object, final String name, final Date value) {
		if (value == null) {
			return;
		}
		final DateTimeValue dt = new DateTimeValue();
		dt.calendar.setTimeZone(timezone);
		dt.calendar.setTime(value);
		dt.hasTime = false;
		dt.isFloating = true;
		dt.isUTC = false;
		final Property property = new Property(name);
		final Parameter parameter = new Parameter("VALUE");
		parameter.addValue(new ParameterValue("DATE"));
		property.addParameter(parameter);
		property.setValue(dt);
		object.addProperty(property);
	}

	private void addExceptions(final ArrayList<DateTimeValue> list, final Date[] exceptions) {
		if (exceptions == null) {
			return;
		}
		for (int i = 0; i < exceptions.length; i++) {
			final DateTimeValue dtv = new DateTimeValue();
			dtv.calendar.setTime(exceptions[i]);
			dtv.hasTime = false;
			list.add(dtv);
		}
	}

	private void addAddress(final VersitObject object, final String name, final String address)
			throws ConverterException {
		try {
			final Property property = new Property(name);
			if (address != null) {
				try {
					property.setValue(new URI("mailto:" + address));
				} catch (URISyntaxException e) {
					final ConverterException ce = new ConverterException(e.getMessage());
					ce.initCause(e);
					throw ce;
				}
				object.addProperty(property);
			}
		} catch (Exception e) {
			LOG.error(e);
			throw new ConverterException(e);
		}
	}

	private void addAddress(final VersitObject object, final String name, final int userId) throws ConverterException {
		try {
			final UserStorage us = UserStorage.getInstance(session.getContext());
			final User userObj = us.getUser(userId);
			if (userObj == null) {
				return;
			}
			final Property property = new Property(name);
			final String address = userObj.getMail();
			if (address != null) {
				try {
					property.setValue(new URI("mailto:" + address));
				} catch (URISyntaxException e) {
					final ConverterException ce = new ConverterException(e.getMessage());
					ce.initCause(e);
					throw ce;
				}
				object.addProperty(property);
			}
		} catch (Exception e) {
			LOG.error(e);
			throw new ConverterException(e);
		}
	}

	private static void addRecurrence(final VersitObject object, final String name, final CalendarObject oxobject) {
		if (oxobject.getRecurrenceType() != CalendarObject.NONE) {
			final RecurrenceValue recur = new RecurrenceValue();
			final Date until = oxobject.getUntil();
			if (until != null) {
				recur.Until = new DateTimeValue();
				recur.Until.calendar.setTime(until);
			}
			final int interval = oxobject.getInterval();
			if (interval != 1) {
				recur.Interval = interval;
			}
			final int type = oxobject.getRecurrenceType();
			switch (oxobject.getRecurrenceType()) {
			case CalendarObject.YEARLY:
				final int[] byMonth = { oxobject.getMonth() - Calendar.JANUARY + 1 };
				recur.ByMonth = byMonth;
			// no break
			case CalendarObject.MONTHLY:
				final int monthDay = oxobject.getDayInMonth();
				final int mdays = oxobject.getDays();
				if (mdays == 0) {
					final int[] byMonthDay = { monthDay };
					recur.ByMonthDay = byMonthDay;
				} else {
					for (int i = 0; i < 7; i++) {
						if ((mdays & (1 << i)) != 0) {
							recur.ByDay.add(recur.new Weekday(monthDay, Calendar.SUNDAY + i));
						}
					}
				}
				break;
			case CalendarObject.WEEKLY:
				final int days = oxobject.getDays();
				for (int i = 0; i < 7; i++) {
					if ((days & (1 << i)) != 0) {
						recur.ByDay.add(recur.new Weekday(0, Calendar.SUNDAY + i));
					}
				}
			}
			final int[] freqs = { RecurrenceValue.DAILY,
					RecurrenceValue.WEEKLY, RecurrenceValue.MONTHLY,
					RecurrenceValue.YEARLY };
			recur.Freq = freqs[type - CalendarObject.DAILY];
			addProperty(object, name, recur);
		}
	}

	private ArrayList<Object> makeList(final Object element) {
		final ArrayList<Object> retval = new ArrayList<Object>(1);
		retval.add(element);
		return retval;
	}

	private static ArrayList<String> getList(final Object val, final char separator) {
		if (val == null) {
			return null;
		}
		final String values = (String) val;
		final ArrayList<String> retval = new ArrayList<String>();
		int start = 0;
		final int length = values.length();
		for (int end = 0; end < length; end++) {
			if (values.charAt(end) == separator) {
				retval.add(values.substring(start, end));
				start = end + 1;
			}
		}
		retval.add(values.substring(start));
		return retval;
	}

	public static VersitObject newCalendar(final String version) {
		final VersitObject object = new VersitObject("VCALENDAR");
		Property property = new Property("VERSION");
		property.setValue(version);
		object.addProperty(property);
		property = new Property("PRODID");
		property.setValue("OPEN-XCHANGE");
		object.addProperty(property);
		return object;
	}

}
