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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.contact.ContactConfig;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.ImageTypeDetector;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;
import com.openexchange.tools.versit.Parameter;
import com.openexchange.tools.versit.ParameterValue;
import com.openexchange.tools.versit.Property;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.values.DateTimeValue;
import com.openexchange.tools.versit.values.DurationValue;
import com.openexchange.tools.versit.values.RecurrenceValue;
import com.openexchange.tools.versit.values.RecurrenceValue.Weekday;

/**
 * This class transforms VersitObjects to OX Contacts, Appointments and Tasks and back. If you want to translate more fields used in ICAL or
 * VCard, you're at the right place - but don't forget to do it in both directions.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> (adapted Victor's parser for OX6)
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> (bugfixes: 7248, 7249, 7472, 7703, 7718, 7719, 8475)
 */
public class OXContainerConverter {

    private static final String P_ORGANIZER = "ORGANIZER";

    private static final String P_TLX = "TLX";

    private static final String P_EMAIL = "EMAIL";

    private static final String PARAM_VOICE = "voice";

    private static final String P_TEL = "TEL";

    private static final String PARAM_WORK = "work";

    private static final String PARAM_HOME = "home";

    private static final String P_TYPE = "TYPE";

    private static final String P_DESCRIPTION = "DESCRIPTION";

    private static final String P_RRULE = "RRULE";

    private static final String P_CATEGORIES = "CATEGORIES";

    private static final String P_ATTENDEE = "ATTENDEE";

    private static final String P_DTSTART = "DTSTART";

    private static final String P_SUMMARY = "SUMMARY";

    private static final String P_COMPLETED = "COMPLETED";

    private static final String P_CLASS = "CLASS";

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(OXContainerConverter.class);

    // ------------------------ START Prepare converter class
    // -------------------------------------

    private static final Map<Integer, Method> SET_INT_METHODS = new HashMap<Integer, Method>();

    private static final Map<Integer, Method> SET_LONG_METHODS = new HashMap<Integer, Method>();

    private static final Map<Integer, Method> SET_DATE_METHODS = new HashMap<Integer, Method>();

    private static final Map<Integer, Method> SET_STRING_METHODS = new HashMap<Integer, Method>();

    private static final Map<Integer, Method> SET_BOOLEAN_METHODS = new HashMap<Integer, Method>();

    private static final Map<Integer, Method> SET_FLOAT_METHODS = new HashMap<Integer, Method>();

    private static final String CHARSET_ISO_8859_1 = "ISO-8859-1";

    private static Method getSetIntegerMethod(final Class<?> containerObjClass, final String methodName) {
        return getSetMethod(containerObjClass, methodName, int.class);
    }

    private static Method getSetLongMethod(final Class<?> containerObjClass, final String methodName) {
        return getSetMethod(containerObjClass, methodName, long.class);
    }

    private static Method getSetDateMethod(final Class<?> containerObjClass, final String methodName) {
        return getSetMethod(containerObjClass, methodName, Date.class);
    }

    private static Method getSetStringMethod(final Class<?> containerObjClass, final String methodName) {
        return getSetMethod(containerObjClass, methodName, String.class);
    }

    private static Method getSetBooleanMethod(final Class<?> containerObjClass, final String methodName) {
        return getSetMethod(containerObjClass, methodName, boolean.class);
    }

    private static Method getSetFloatMethod(final Class<?> containerObjClass, final String methodName) {
        return getSetMethod(containerObjClass, methodName, float.class);
    }

    private static Method getSetMethod(final Class<?> containerObjClass, final String methodName, final Class<?> typeClass) {
        try {
            return containerObjClass.getMethod(methodName, new Class[] { typeClass });
        } catch (final Exception e) {
            LOG.error(e);
            return null;
        }
    }

    static {
        // setter methods for int values
        SET_INT_METHODS.put(Integer.valueOf(DataObject.OBJECT_ID), getSetIntegerMethod(DataObject.class, "setObjectID"));
        SET_INT_METHODS.put(Integer.valueOf(DataObject.CREATED_BY), getSetIntegerMethod(DataObject.class, "setCreatedBy"));
        SET_INT_METHODS.put(Integer.valueOf(DataObject.MODIFIED_BY), getSetIntegerMethod(DataObject.class, "setModifiedBy"));

        SET_INT_METHODS.put(Integer.valueOf(FolderChildObject.FOLDER_ID), getSetIntegerMethod(FolderChildObject.class, "setParentFolderID"));

        SET_INT_METHODS.put(Integer.valueOf(CommonObject.COLOR_LABEL), getSetIntegerMethod(CommonObject.class, "setLabel"));
        SET_INT_METHODS.put(Integer.valueOf(CommonObject.NUMBER_OF_LINKS), getSetIntegerMethod(CommonObject.class, "setNumberOfLinks"));
        SET_INT_METHODS.put(Integer.valueOf(CommonObject.NUMBER_OF_ATTACHMENTS), getSetIntegerMethod(
            CommonObject.class,
            "setNumberOfAttachments"));

        SET_INT_METHODS.put(Integer.valueOf(CalendarObject.RECURRENCE_ID), getSetIntegerMethod(CalendarObject.class, "setRecurrenceID"));
        SET_INT_METHODS.put(Integer.valueOf(CalendarObject.RECURRENCE_POSITION), getSetIntegerMethod(
            CalendarObject.class,
            "setRecurrencePosition"));
        SET_INT_METHODS.put(Integer.valueOf(CalendarObject.RECURRENCE_TYPE), getSetIntegerMethod(CalendarObject.class, "setRecurrenceType"));
        SET_INT_METHODS.put(Integer.valueOf(CalendarObject.DAYS), getSetIntegerMethod(CalendarObject.class, "setDays"));
        SET_INT_METHODS.put(Integer.valueOf(CalendarObject.DAY_IN_MONTH), getSetIntegerMethod(CalendarObject.class, "setDayInMonth"));
        SET_INT_METHODS.put(Integer.valueOf(CalendarObject.MONTH), getSetIntegerMethod(CalendarObject.class, "setMonth"));
        SET_INT_METHODS.put(Integer.valueOf(CalendarObject.INTERVAL), getSetIntegerMethod(CalendarObject.class, "setInterval"));
        SET_INT_METHODS.put(Integer.valueOf(CalendarObject.RECURRENCE_CALCULATOR), getSetIntegerMethod(
            CalendarObject.class,
            "setRecurrenceCalculator"));
        SET_INT_METHODS.put(Integer.valueOf(CalendarObject.ALARM), getSetIntegerMethod(Appointment.class, "setAlarm"));

        SET_INT_METHODS.put(Integer.valueOf(Task.STATUS), getSetIntegerMethod(Task.class, "setStatus"));
        SET_INT_METHODS.put(Integer.valueOf(Task.PERCENT_COMPLETED), getSetIntegerMethod(Task.class, "setPercentComplete"));
        SET_INT_METHODS.put(Integer.valueOf(Task.PROJECT_ID), getSetIntegerMethod(Task.class, "setProjectID"));
        SET_INT_METHODS.put(Integer.valueOf(Task.PRIORITY), getSetIntegerMethod(Task.class, "setPriority"));

        SET_INT_METHODS.put(Integer.valueOf(Appointment.SHOWN_AS), getSetIntegerMethod(Appointment.class, "setShownAs"));

        // setter methods for long values
        SET_LONG_METHODS.put(Integer.valueOf(Task.ACTUAL_DURATION), getSetLongMethod(Task.class, "setActualDuration"));
        SET_LONG_METHODS.put(Integer.valueOf(Task.TARGET_DURATION), getSetLongMethod(Task.class, "setTargetDuration"));

        // setter methods for float values
        SET_FLOAT_METHODS.put(Integer.valueOf(Task.ACTUAL_COSTS), getSetFloatMethod(Task.class, "setActualCosts"));
        SET_FLOAT_METHODS.put(Integer.valueOf(Task.TARGET_COSTS), getSetFloatMethod(Task.class, "setTargetCosts"));

        // setter methods for date values
        SET_DATE_METHODS.put(Integer.valueOf(DataObject.CREATION_DATE), getSetDateMethod(DataObject.class, "setCreationDate"));
        SET_DATE_METHODS.put(Integer.valueOf(DataObject.LAST_MODIFIED), getSetDateMethod(DataObject.class, "setLastModified"));

        SET_DATE_METHODS.put(Integer.valueOf(CalendarObject.ALARM), getSetDateMethod(Task.class, "setAlarm"));
        SET_DATE_METHODS.put(Integer.valueOf(CalendarObject.START_DATE), getSetDateMethod(CalendarObject.class, "setStartDate"));
        SET_DATE_METHODS.put(Integer.valueOf(CalendarObject.END_DATE), getSetDateMethod(CalendarObject.class, "setEndDate"));
        SET_DATE_METHODS.put(Integer.valueOf(CalendarObject.RECURRENCE_DATE_POSITION), getSetDateMethod(
            CalendarObject.class,
            "setRecurrenceDatePosition"));
        SET_DATE_METHODS.put(
            Integer.valueOf(CalendarObject.CHANGE_EXCEPTIONS),
            getSetDateMethod(CalendarObject.class, "addChangeException"));
        SET_DATE_METHODS.put(
            Integer.valueOf(CalendarObject.DELETE_EXCEPTIONS),
            getSetDateMethod(CalendarObject.class, "addDeleteException"));
        SET_DATE_METHODS.put(Integer.valueOf(CalendarObject.UNTIL), getSetDateMethod(CalendarObject.class, "setUntil"));

        SET_DATE_METHODS.put(Integer.valueOf(Task.DATE_COMPLETED), getSetDateMethod(Task.class, "setDateCompleted"));

        SET_DATE_METHODS.put(Integer.valueOf(Contact.BIRTHDAY), getSetDateMethod(Contact.class, "setBirthday"));
        SET_DATE_METHODS.put(Integer.valueOf(Contact.ANNIVERSARY), getSetDateMethod(Contact.class, "setAnniversary"));

        // setter methods for string values
        SET_STRING_METHODS.put(Integer.valueOf(CommonObject.CATEGORIES), getSetStringMethod(CommonObject.class, "setCategories"));

        SET_STRING_METHODS.put(Integer.valueOf(CalendarObject.TITLE), getSetStringMethod(CalendarObject.class, "setTitle"));
        SET_STRING_METHODS.put(Integer.valueOf(CalendarObject.NOTE), getSetStringMethod(CalendarObject.class, "setNote"));

        SET_STRING_METHODS.put(Integer.valueOf(Task.BILLING_INFORMATION), getSetStringMethod(Task.class, "setBillingInformation"));
        SET_STRING_METHODS.put(Integer.valueOf(Task.CURRENCY), getSetStringMethod(Task.class, "setCurrency"));
        SET_STRING_METHODS.put(Integer.valueOf(Task.TRIP_METER), getSetStringMethod(Task.class, "setTripMeter"));
        SET_STRING_METHODS.put(Integer.valueOf(Task.COMPANIES), getSetStringMethod(Task.class, "setCompanies"));

        SET_STRING_METHODS.put(Integer.valueOf(Appointment.LOCATION), getSetStringMethod(Appointment.class, "setLocation"));

        SET_STRING_METHODS.put(Integer.valueOf(Contact.DISPLAY_NAME), getSetStringMethod(Contact.class, "setDisplayName"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.GIVEN_NAME), getSetStringMethod(Contact.class, "setGivenName"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.SUR_NAME), getSetStringMethod(Contact.class, "setSurName"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.MIDDLE_NAME), getSetStringMethod(Contact.class, "setMiddleName"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.SUFFIX), getSetStringMethod(Contact.class, "setSuffix"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.TITLE), getSetStringMethod(Contact.class, "setTitle"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.STREET_HOME), getSetStringMethod(Contact.class, "setStreetHome"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.STREET_BUSINESS), getSetStringMethod(Contact.class, "setStreetBusiness"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.STREET_OTHER), getSetStringMethod(Contact.class, "setStreetOther"));
        SET_STRING_METHODS.put(
            Integer.valueOf(Contact.POSTAL_CODE_HOME),
            getSetStringMethod(Contact.class, "setPostalCodeHome"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.POSTAL_CODE_BUSINESS), getSetStringMethod(
            Contact.class,
            "setPostalCodeBusiness"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.POSTAL_CODE_OTHER), getSetStringMethod(
            Contact.class,
            "setPostalCodeOther"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.CITY_HOME), getSetStringMethod(Contact.class, "setCityHome"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.CITY_BUSINESS), getSetStringMethod(Contact.class, "setCityBusiness"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.CITY_OTHER), getSetStringMethod(Contact.class, "setCityOther"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.STATE_HOME), getSetStringMethod(Contact.class, "setStateHome"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.STATE_BUSINESS), getSetStringMethod(Contact.class, "setStateBusiness"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.STATE_OTHER), getSetStringMethod(Contact.class, "setStateOther"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.COUNTRY_HOME), getSetStringMethod(Contact.class, "setCountryHome"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.COUNTRY_BUSINESS), getSetStringMethod(
            Contact.class,
            "setCountryBusiness"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.COUNTRY_OTHER), getSetStringMethod(Contact.class, "setCountryOther"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.MARITAL_STATUS), getSetStringMethod(Contact.class, "setMaritalStatus"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.NUMBER_OF_CHILDREN), getSetStringMethod(
            Contact.class,
            "setNumberOfChildren"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.PROFESSION), getSetStringMethod(Contact.class, "setProfession"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.NICKNAME), getSetStringMethod(Contact.class, "setNickname"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.SPOUSE_NAME), getSetStringMethod(Contact.class, "setSpouseName"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.NOTE), getSetStringMethod(Contact.class, "setNote"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.DEPARTMENT), getSetStringMethod(Contact.class, "setDepartment"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.POSITION), getSetStringMethod(Contact.class, "setPosition"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.EMPLOYEE_TYPE), getSetStringMethod(Contact.class, "setEmployeeType"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.ROOM_NUMBER), getSetStringMethod(Contact.class, "setRoomNumber"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.NUMBER_OF_EMPLOYEE), getSetStringMethod(
            Contact.class,
            "setNumberOfEmployee"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.SALES_VOLUME), getSetStringMethod(Contact.class, "setSalesVolume"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.TAX_ID), getSetStringMethod(Contact.class, "setTaxID"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.COMMERCIAL_REGISTER), getSetStringMethod(
            Contact.class,
            "setCommercialRegister"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.BRANCHES), getSetStringMethod(Contact.class, "setBranches"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.BUSINESS_CATEGORY), getSetStringMethod(
            Contact.class,
            "setBusinessCategory"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.INFO), getSetStringMethod(Contact.class, "setInfo"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.MANAGER_NAME), getSetStringMethod(Contact.class, "setManagerName"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.ASSISTANT_NAME), getSetStringMethod(Contact.class, "setAssistantName"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.TELEPHONE_BUSINESS1), getSetStringMethod(
            Contact.class,
            "setTelephoneBusiness1"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.TELEPHONE_BUSINESS2), getSetStringMethod(
            Contact.class,
            "setTelephoneBusiness2"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.TELEPHONE_HOME1), getSetStringMethod(Contact.class, "setTelephoneHome1"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.TELEPHONE_HOME2), getSetStringMethod(Contact.class, "setTelephoneHome2"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.TELEPHONE_OTHER), getSetStringMethod(Contact.class, "setTelephoneOther"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.TELEPHONE_ASSISTANT), getSetStringMethod(
            Contact.class,
            "setTelephoneAssistant"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.TELEPHONE_CALLBACK), getSetStringMethod(
            Contact.class,
            "setTelephoneCallback"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.TELEPHONE_CAR), getSetStringMethod(Contact.class, "setTelephoneCar"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.TELEPHONE_COMPANY), getSetStringMethod(
            Contact.class,
            "setTelephoneCompany"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.TELEPHONE_IP), getSetStringMethod(Contact.class, "setTelephoneIP"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.TELEPHONE_ISDN), getSetStringMethod(Contact.class, "setTelephoneISDN"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.TELEPHONE_PAGER), getSetStringMethod(Contact.class, "setTelephonePager"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.TELEPHONE_PRIMARY), getSetStringMethod(
            Contact.class,
            "setTelephonePrimary"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.TELEPHONE_RADIO), getSetStringMethod(Contact.class, "setTelephoneRadio"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.TELEPHONE_TELEX), getSetStringMethod(Contact.class, "setTelephoneTelex"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.TELEPHONE_TTYTDD), getSetStringMethod(
            Contact.class,
            "setTelephoneTTYTTD"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.FAX_HOME), getSetStringMethod(Contact.class, "setFaxHome"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.FAX_BUSINESS), getSetStringMethod(Contact.class, "setFaxBusiness"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.FAX_OTHER), getSetStringMethod(Contact.class, "setFaxOther"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.CELLULAR_TELEPHONE1), getSetStringMethod(
            Contact.class,
            "setCellularTelephone1"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.CELLULAR_TELEPHONE2), getSetStringMethod(
            Contact.class,
            "setCellularTelephone2"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.EMAIL1), getSetStringMethod(Contact.class, "setEmail1"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.EMAIL2), getSetStringMethod(Contact.class, "setEmail2"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.EMAIL3), getSetStringMethod(Contact.class, "setEmail3"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.URL), getSetStringMethod(Contact.class, "setURL"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.INSTANT_MESSENGER1), getSetStringMethod(
            Contact.class,
            "setInstantMessenger1"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.INSTANT_MESSENGER2), getSetStringMethod(
            Contact.class,
            "setInstantMessenger2"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.USERFIELD01), getSetStringMethod(Contact.class, "setUserField01"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.USERFIELD02), getSetStringMethod(Contact.class, "setUserField02"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.USERFIELD03), getSetStringMethod(Contact.class, "setUserField03"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.USERFIELD04), getSetStringMethod(Contact.class, "setUserField04"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.USERFIELD05), getSetStringMethod(Contact.class, "setUserField05"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.USERFIELD06), getSetStringMethod(Contact.class, "setUserField06"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.USERFIELD07), getSetStringMethod(Contact.class, "setUserField07"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.USERFIELD08), getSetStringMethod(Contact.class, "setUserField08"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.USERFIELD09), getSetStringMethod(Contact.class, "setUserField09"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.USERFIELD10), getSetStringMethod(Contact.class, "setUserField10"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.USERFIELD11), getSetStringMethod(Contact.class, "setUserField11"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.USERFIELD12), getSetStringMethod(Contact.class, "setUserField12"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.USERFIELD13), getSetStringMethod(Contact.class, "setUserField13"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.USERFIELD14), getSetStringMethod(Contact.class, "setUserField14"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.USERFIELD15), getSetStringMethod(Contact.class, "setUserField15"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.USERFIELD16), getSetStringMethod(Contact.class, "setUserField16"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.USERFIELD17), getSetStringMethod(Contact.class, "setUserField17"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.USERFIELD18), getSetStringMethod(Contact.class, "setUserField18"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.USERFIELD19), getSetStringMethod(Contact.class, "setUserField19"));
        SET_STRING_METHODS.put(Integer.valueOf(Contact.USERFIELD20), getSetStringMethod(Contact.class, "setUserField20"));

        // setter methods for boolean values
        SET_BOOLEAN_METHODS.put(Integer.valueOf(CommonObject.PRIVATE_FLAG), getSetBooleanMethod(CommonObject.class, "setPrivateFlag"));

        SET_BOOLEAN_METHODS.put(Integer.valueOf(CalendarObject.NOTIFICATION), getSetBooleanMethod(CalendarObject.class, "setNotification"));

        SET_BOOLEAN_METHODS.put(Integer.valueOf(Appointment.FULL_TIME), getSetBooleanMethod(Appointment.class, "setFullTime"));
    }

    private static final String atdomain;

    static {
        String domain = "localhost";
        try {
            domain = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (final UnknownHostException e) {
            LOG.error(e.getMessage(), e);
        }
        atdomain = new StringBuilder().append('@').append(domain).toString();
    }

    // ------------------------ END Prepare converter class
    // -------------------------------------

    private final Context ctx;

    private final TimeZone timezone;

    private String organizerMailAddress;

    private boolean sendUTC = true;

    private boolean sendFloating;

    public OXContainerConverter(final TimeZone timezone, final String organizerMailAddress) {
        super();
        this.timezone = timezone;
        this.organizerMailAddress = organizerMailAddress;
        ctx = null;
    }

    public OXContainerConverter(final Session session) throws ConverterException {
        super();
        try {
            ctx = ContextStorage.getStorageContext(session.getContextId());
        } catch (final ContextException e) {
            throw new ConverterException(e);
        }
        timezone = TimeZone.getTimeZone(UserStorage.getStorageUser(session.getUserId(), ctx).getTimeZone());
    }

    public OXContainerConverter(final Session session, final Context ctx) {
        super();
        this.ctx = ctx;
        timezone = TimeZone.getTimeZone(UserStorage.getStorageUser(session.getUserId(), ctx).getTimeZone());
    }

    public OXContainerConverter(final Context ctx, final TimeZone tz) {
        super();
        this.ctx = ctx;
        timezone = tz;
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
            PrivacyProperty(taskContainer, object, P_CLASS, SET_BOOLEAN_METHODS.get(Integer.valueOf(Task.PRIVATE_FLAG)));
            // COMPLETED
            DateTimeProperty(taskContainer, object, P_COMPLETED, SET_DATE_METHODS.get(Integer.valueOf(Task.DATE_COMPLETED)));
            // GEO is ignored
            // LAST-MODIFIED is ignored
            // LOCATION is ignored
            // ORGANIZER is ignored
            // PERCENT-COMPLETE
            IntegerProperty(taskContainer, object, "PERCENT-COMPLETE", SET_INT_METHODS.get(Integer.valueOf(Task.PERCENT_COMPLETED)));
            // PRIORITY
            Property property = object.getProperty("PRIORITY");
            if (property != null) {
                final int priority = ((Integer) property.getValue()).intValue();
                final int[] priorities = { Task.HIGH, Task.HIGH, Task.HIGH, Task.HIGH, Task.NORMAL, Task.LOW, Task.LOW, Task.LOW, Task.LOW };
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
                if ("NEEDS-ACTION".equals(status)) {
                    taskContainer.setStatus(Task.NOT_STARTED);
                } else if ("IN-PROCESS".equals(status)) {
                    taskContainer.setStatus(Task.IN_PROGRESS);
                } else if (P_COMPLETED.equals(status)) {
                    taskContainer.setStatus(Task.DONE);
                } else if ("CANCELLED".equals(status)) {
                    taskContainer.setStatus(Task.DEFERRED);
                } else {
                    throw new ConverterException("Unknown status: \"" + status + "\"");
                }
            }
            // SUMMARY
            StringProperty(taskContainer, object, P_SUMMARY, SET_STRING_METHODS.get(Integer.valueOf(Task.TITLE)));
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
            if (!DateTimeProperty(taskContainer, object, "DUE", SET_DATE_METHODS.get(Integer.valueOf(Task.END_DATE)))) {
                DurationProperty(taskContainer, object, "DURATION", P_DTSTART, SET_DATE_METHODS.get(Integer.valueOf(Task.END_DATE)));
            }
            // Multiple properties
            final int count = object.getPropertyCount();
            final StringBuilder cats = new StringBuilder();
            for (int i = 0; i < count; i++) {
                property = object.getProperty(i);
                // ATTACH is ignored
                // ATTENDEE
                if (P_ATTENDEE.equals(property.name)) {
                    AttendeeProperty(taskContainer, property);
                }
                // CATEGORIES
                else if (P_CATEGORIES.equals(property.name)) {
                    final ArrayList<?> al = ((ArrayList<?>) property.getValue());
                    final int size = al.size();
                    final Iterator<?> j = al.iterator();
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
                else if (P_RRULE.equals(property.name)) {
                    RecurrenceProperty(taskContainer, property, object.getProperty(P_DTSTART));
                }
            }
            if (cats.length() != 0) {
                cats.deleteCharAt(cats.length() - 1);
                taskContainer.setCategories(cats.toString());
            }
            // DESCRIPTION (fix: 7718)
            StringProperty(taskContainer, object, P_DESCRIPTION, SET_STRING_METHODS.get(Integer.valueOf(Task.NOTE)));
            // VALARM
            AddAlarms(taskContainer, object);
            return taskContainer;
        } catch (final Exception e) {
            LOG.error(e);
            throw new ConverterException(e);
        }
    }

    public CalendarDataObject convertAppointment(final VersitObject object) throws ConverterException {
        final CalendarDataObject appContainer = new CalendarDataObject();
        // CLASS
        PrivacyProperty(appContainer, object, P_CLASS, SET_BOOLEAN_METHODS.get(Integer.valueOf(Task.PRIVATE_FLAG)));
        // CREATED is ignored
        // DESCRIPTION
        StringProperty(appContainer, object, P_DESCRIPTION, SET_STRING_METHODS.get(Integer.valueOf(Appointment.NOTE)));
        // DTSTART
        Property property = object.getProperty(P_DTSTART);
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
        StringProperty(appContainer, object, "LOCATION", SET_STRING_METHODS.get(Integer.valueOf(Appointment.LOCATION)));
        // ORGANIZER is ignored
        // PRIORITY is ignored
        // DTSTAMP is ignored
        // TODO SEQUENCE
        // STATUS is ignored
        // SUMMARY
        StringProperty(appContainer, object, P_SUMMARY, SET_STRING_METHODS.get(Integer.valueOf(Appointment.TITLE)));
        // TRANSP
        property = object.getProperty("TRANSP");
        if (property != null) {
            final String transp = ((String) property.getValue()).toUpperCase();
            if ("OPAQUE".equals(transp)) {
                appContainer.setShownAs(Appointment.RESERVED);
            } else if ("TRANSPARENT".equals(transp)) {
                appContainer.setShownAs(Appointment.FREE);
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
        if (!DateTimeProperty(appContainer, object, "DTEND", SET_DATE_METHODS.get(Integer.valueOf(Appointment.END_DATE))) && !DurationProperty(
            appContainer,
            object,
            "DURATION",
            P_DTSTART,
            SET_DATE_METHODS.get(Integer.valueOf(Appointment.END_DATE)))) {
            DateTimeProperty(appContainer, object, "DSTART", SET_DATE_METHODS.get(Integer.valueOf(Appointment.END_DATE)));
        }
        // Multiple properties
        final StringBuilder cats = new StringBuilder();
        final ArrayList exdates = new ArrayList<Object>();
        final int count = object.getPropertyCount();
        for (int i = 0; i < count; i++) {
            property = object.getProperty(i);
            // ATTACH is ignored
            // ATTENDEE
            if (P_ATTENDEE.equals(property.name)) {
                AttendeeProperty(appContainer, property);
            }
            // CATEGORIES
            else if (P_CATEGORIES.equals(property.name)) {
                final ArrayList<?> al = ((ArrayList<?>) property.getValue());
                final int size = al.size();
                final Iterator<?> j = al.iterator();
                for (int k = 0; k < size; k++) {
                    cats.append(j.next());
                    cats.append(',');
                }
            }
            // COMMENT is ignored
            // CONTACT is ignored
            // EXDATE
            else if ("EXDATE".equals(property.name)) {
                exdates.addAll((ArrayList) property.getValue());
            }
            // EXRULE is ignored
            // REQUEST-STATUS is ignored
            // TODO RELATED-TO
            // RESOURCES
            else if ("RESOURCES".equals(property.name)) {
                final ArrayList<?> al = ((ArrayList<?>) property.getValue());
                final int size = al.size();
                final Iterator<?> j = al.iterator();
                for (int k = 0; k < size; k++) {
                    final ResourceParticipant p = new ResourceParticipant();
                    p.setDisplayName((String) j.next());
                    appContainer.addParticipant(p);
                }
            }
            // RDATE is ignored
            // RRULE
            else if (P_RRULE.equals(property.name)) {
                RecurrenceProperty(appContainer, property, object.getProperty(P_DTSTART));
            }
        }
        if (cats.length() != 0) {
            cats.deleteCharAt(cats.length() - 1);
            appContainer.setCategories(cats.toString());
        }
        if (!exdates.isEmpty()) {
            final Date[] dates = new Date[exdates.size()];
            for (int i = 0; i < dates.length; i++) {
                dates[i] = ((DateTimeValue) exdates.get(i)).calendar.getTime();
            }
            appContainer.setDeleteExceptions(dates);
        }
        // VALARM
        AddAlarms(appContainer, object);
        return appContainer;
    }

    public Contact convertContact(final VersitObject object) throws ConverterException {
        final Contact contactContainer = new Contact();
        // SOURCE is ignored
        // NAME is ignored
        // PROFILE is ignored
        // FN
        StringProperty(contactContainer, object, "FN", SET_STRING_METHODS.get(Integer.valueOf(Contact.DISPLAY_NAME)));
        // N
        Property property = object.getProperty("N");
        if (property != null) {
            final ArrayList<?> N = (ArrayList<?>) property.getValue();
            // fix for 7248
            if (N != null) {
                for (int i = N.size(); i < 5; i++) {
                    N.add(null);
                }
            }
            // fix:end
            if (N.size() != 5) {
                throw new ConverterException("Invalid property N, has " + N.size() + " elements, not 5.");
            }
            ListValue(contactContainer, SET_STRING_METHODS.get(Integer.valueOf(Contact.SUR_NAME)), N.get(0), " ");
            ListValue(contactContainer, SET_STRING_METHODS.get(Integer.valueOf(Contact.GIVEN_NAME)), N.get(1), " ");
            ListValue(contactContainer, SET_STRING_METHODS.get(Integer.valueOf(Contact.MIDDLE_NAME)), N.get(2), " ");
            ListValue(contactContainer, SET_STRING_METHODS.get(Integer.valueOf(Contact.TITLE)), N.get(3), " ");
            ListValue(contactContainer, SET_STRING_METHODS.get(Integer.valueOf(Contact.SUFFIX)), N.get(4), " ");
        }
        // NICKNAME
        StringProperty(contactContainer, object, "NICKNAME", SET_STRING_METHODS.get(Integer.valueOf(Contact.NICKNAME)));
        // PHOTO
        property = object.getProperty("PHOTO");
        if (property != null) {
            final Parameter uriParam = property.getParameter("URI");
            if (uriParam == null) {
                String value;
                final Object propertyValue = property.getValue();
                if (propertyValue instanceof byte[]) {
                    /*
                     * Apply image data as it is since ValueDefinition#parse(Scanner s, Property property) already decodes value dependent
                     * on "ENCODING" parameter
                     */
                    contactContainer.setImage1((byte[]) propertyValue);
                    value = null;
                } else if (propertyValue instanceof URI) {
                    loadImageFromURL(contactContainer, propertyValue.toString());
                    value = null;
                } else {
                    value = propertyValue.toString();
                    if (value != null) {
                        try {
                            final URL url = new URL(value);
                            loadImageFromURL(contactContainer, url);
                            value = null;
                        } catch (final MalformedURLException e) {
                            // Not a valid URL
                            if (LOG.isTraceEnabled()) {
                                LOG.trace(e.getMessage(), e);
                            }
                        }
                    }
                }
                if (value != null) {
                    try {
                        contactContainer.setImage1(value.getBytes(CHARSET_ISO_8859_1));
                    } catch (final UnsupportedEncodingException e) {
                        LOG.error("Image could not be set", e);
                    }
                }
                final Parameter type = property.getParameter(P_TYPE);
                if (type != null && type.getValueCount() == 1) {
                    String stype = type.getValue(0).getText().toLowerCase();
                    if (!stype.startsWith("image/")) {
                        stype = "image/" + stype;
                    }
                    contactContainer.setImageContentType(stype);
                } else if (propertyValue instanceof byte[]) {
                    contactContainer.setImageContentType(ImageTypeDetector.getMimeType((byte[]) propertyValue));
                }
            } else {
                if (uriParam.getValueCount() == 1) {
                    // We expect that the URI/URL is parametes's only value
                    loadImageFromURL(contactContainer, uriParam.getValue(0).getText());
                }
            }
        }
        // BDAY
        DateTimeProperty(contactContainer, object, "BDAY", SET_DATE_METHODS.get(Integer.valueOf(Contact.BIRTHDAY)));
        // MAILER is ignored
        // TZ is ignored
        // GEO is ignored
        // TITLE
        StringProperty(contactContainer, object, "TITLE", SET_STRING_METHODS.get(Integer.valueOf(Contact.EMPLOYEE_TYPE)));
        // ROLE
        StringProperty(contactContainer, object, "ROLE", SET_STRING_METHODS.get(Integer.valueOf(Contact.POSITION)));
        // LOGO is ignored
        // TODO AGENT
        // ORG
        property = object.getProperty("ORG");
        if (property != null) {
            final ArrayList<?> elements = (ArrayList<?>) property.getValue();
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
        StringProperty(contactContainer, object, "NOTE", SET_STRING_METHODS.get(Integer.valueOf(Contact.NOTE)));
        // PRODID is ignored
        // REV is ignored
        // SORT-STRING is ignored
        // SOUND is ignored
        // URL
        StringProperty(contactContainer, object, "URL", SET_STRING_METHODS.get(Integer.valueOf(Contact.URL)));
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
                {
                    SET_STRING_METHODS.get(Integer.valueOf(Contact.TELEPHONE_BUSINESS1)),
                    SET_STRING_METHODS.get(Integer.valueOf(Contact.TELEPHONE_BUSINESS2)) },
                { SET_STRING_METHODS.get(Integer.valueOf(Contact.FAX_BUSINESS)) } },
            {
                {
                    SET_STRING_METHODS.get(Integer.valueOf(Contact.TELEPHONE_HOME1)),
                    SET_STRING_METHODS.get(Integer.valueOf(Contact.TELEPHONE_HOME2)) },
                { SET_STRING_METHODS.get(Integer.valueOf(Contact.FAX_HOME)) } },
            {
                {
                    SET_STRING_METHODS.get(Integer.valueOf(Contact.CELLULAR_TELEPHONE1)),
                    SET_STRING_METHODS.get(Integer.valueOf(Contact.CELLULAR_TELEPHONE2)) }, {} },
            { { SET_STRING_METHODS.get(Integer.valueOf(Contact.TELEPHONE_CAR)) }, {} },
            { { SET_STRING_METHODS.get(Integer.valueOf(Contact.TELEPHONE_ISDN)) }, {} },
            { { SET_STRING_METHODS.get(Integer.valueOf(Contact.TELEPHONE_PAGER)) }, {} },
            {
                { SET_STRING_METHODS.get(Integer.valueOf(Contact.TELEPHONE_OTHER)) },
                { SET_STRING_METHODS.get(Integer.valueOf(Contact.FAX_OTHER)) } } };

        final int[][][] index = {
            { { 0 }, { 0 } }, { { 0 }, { 0 } }, { { 0 }, { 0 } }, { { 0 }, { 0 } }, { { 0 }, { 0 } }, { { 0 }, { 0 } }, { { 0 }, { 0 } } };

        final Method[] emails = {
            SET_STRING_METHODS.get(Integer.valueOf(Contact.EMAIL1)), SET_STRING_METHODS.get(Integer.valueOf(Contact.EMAIL2)),
            SET_STRING_METHODS.get(Integer.valueOf(Contact.EMAIL3)) };

        final int[] emailIndex = { 0 };

        final ArrayList<Object> cats = new ArrayList<Object>();

        final int count = object.getPropertyCount();
        for (int i = 0; i < count; i++) {
            property = object.getProperty(i);
            // ADR
            if ("ADR".equals(property.name)) {
                boolean isHome = false, isWork = true;
                final Parameter type = property.getParameter(P_TYPE);
                if (type != null) {
                    isWork = false;
                    for (int j = 0; j < type.getValueCount(); j++) {
                        final String value = type.getValue(j).getText();
                        isHome |= PARAM_HOME.equalsIgnoreCase(value);
                        isWork |= PARAM_WORK.equalsIgnoreCase(value);
                    }
                }
                final ArrayList<?> A = (ArrayList<?>) property.getValue();
                // fix for 7248
                if (A != null) {
                    for (int j = A.size(); j < 7; j++) {
                        A.add(null);
                    }
                }
                // fix:end
                if (A == null || A.size() != 7) {
                    throw new ConverterException("Invalid property ADR");
                }
                if (isWork) {
                    ListValue(contactContainer, SET_STRING_METHODS.get(Integer.valueOf(Contact.STREET_BUSINESS)), A.get(2), "\n");
                    ListValue(contactContainer, SET_STRING_METHODS.get(Integer.valueOf(Contact.CITY_BUSINESS)), A.get(3), "\n");
                    ListValue(contactContainer, SET_STRING_METHODS.get(Integer.valueOf(Contact.STATE_BUSINESS)), A.get(4), "\n");
                    ListValue(contactContainer, SET_STRING_METHODS.get(Integer.valueOf(Contact.POSTAL_CODE_BUSINESS)), A.get(5), "\n");
                    ListValue(contactContainer, SET_STRING_METHODS.get(Integer.valueOf(Contact.COUNTRY_BUSINESS)), A.get(6), "\n");
                }
                if (isHome) {
                    ListValue(contactContainer, SET_STRING_METHODS.get(Integer.valueOf(Contact.STREET_HOME)), A.get(2), "\n");
                    ListValue(contactContainer, SET_STRING_METHODS.get(Integer.valueOf(Contact.CITY_HOME)), A.get(3), "\n");
                    ListValue(contactContainer, SET_STRING_METHODS.get(Integer.valueOf(Contact.STATE_HOME)), A.get(4), "\n");
                    ListValue(contactContainer, SET_STRING_METHODS.get(Integer.valueOf(Contact.POSTAL_CODE_HOME)), A.get(5), "\n");
                    ListValue(contactContainer, SET_STRING_METHODS.get(Integer.valueOf(Contact.COUNTRY_HOME)), A.get(6), "\n");
                }
            }
            // LABEL is ignored
            // TEL
            else if (P_TEL.equals(property.name)) {
                int idx = WORK;
                boolean isVoice = false;
                boolean isFax = false;
                final Parameter type = property.getParameter(P_TYPE);
                if (type != null) {
                    for (int j = 0; j < type.getValueCount(); j++) {
                        final String value = type.getValue(j).getText();
                        if (idx == WORK || idx == HOME) {
                            if (value.equalsIgnoreCase(PARAM_WORK)) {
                                idx = WORK;
                            } else if (value.equalsIgnoreCase(PARAM_HOME)) {
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
                        if (value.equalsIgnoreCase(PARAM_VOICE)) {
                            isVoice = true;
                        } else if (value.equalsIgnoreCase("fax")) {
                            isFax = true;
                        }
                    }
                }
                if (!isVoice && !isFax) {
                    isVoice = true;
                }
                final Object value = property.getValue();
                if (isVoice) {
                    ComplexProperty(contactContainer, phones[idx][VOICE], index[idx][VOICE], value);
                }
                if (isFax) {
                    ComplexProperty(contactContainer, phones[idx][FAX], index[idx][FAX], value);
                }
            }
            // EMAIL
            else if (P_EMAIL.equals(property.name)) {
                final String value = property.getValue().toString();
                // fix for: 7249
                boolean isProperEmailAddress = value != null && value.length() > 0;
                if (isProperEmailAddress) {
                    try {
                        final InternetAddress ia = new InternetAddress(value);
                        ia.validate();
                    } catch (final AddressException e) {
                        isProperEmailAddress = false;
                    }
                }
                // fix: end
                if (isProperEmailAddress) {
                    ComplexProperty(contactContainer, emails, emailIndex, value);
                } else {
                    // fix for: 7719
                    final Parameter type = property.getParameter(P_TYPE);
                    if (type != null && type.getValue(0) != null && type.getValue(0).getText() != null && P_TLX.equals(type.getValue(0).getText())) {
                        contactContainer.setTelephoneTelex(property.getValue().toString());
                    }
                }
            }
            // CATEGORIES
            else if (P_CATEGORIES.equals(property.name)) {
                final Object value = property.getValue();
                if (value != null) {
                    if (value instanceof ArrayList) {
                        cats.addAll((ArrayList) value);
                    } else if (value instanceof String) {
                        cats.addAll(Arrays.asList(value.toString().split(" *, *")));
                    } else {
                        LOG.error("Unexpected class: " + value.getClass().getName());
                    }
                }
            }
            // CLASS
            else if (P_CLASS.equals(property.name)) {
                if ("CONFIDENTIAL".equalsIgnoreCase(property.getValue().toString()) || "PRIVATE".equalsIgnoreCase(property.getValue().toString())) {
                    contactContainer.setPrivateFlag(true);
                }
            }
        }
        ListValue(contactContainer, SET_STRING_METHODS.get(Integer.valueOf(Contact.CATEGORIES)), cats, ",");

        return contactContainer;
    }

    /**
     * Open a new {@link URLConnection URL connection} to specified parameter's value which indicates to be an URI/URL. The image's data and
     * its MIME type is then read from opened connection and put into given {@link Contact contact container}.
     * 
     * @param contactContainer The contact container to fill
     * @param uri The URI parameter's value
     * @throws ConverterException If converting image's data fails
     */
    public static void loadImageFromURL(final Contact contactContainer, final String uri) throws ConverterException {
        try {
            loadImageFromURL(contactContainer, new URL(uri));
        } catch (final MalformedURLException e) {
            LOG.warn(new StringBuilder(32 + uri.length()).append("Image  URI could not be loaded: ").append(uri).toString(), e);
        }
    }

    /**
     * Open a new {@link URLConnection URL connection} to specified parameter's value which indicates to be an URI/URL. The image's data and
     * its MIME type is then read from opened connection and put into given {@link Contact contact container}.
     * 
     * @param contactContainer The contact container to fill
     * @param url The image URL
     * @throws ConverterException If converting image's data fails
     */
    private static void loadImageFromURL(final Contact contactContainer, final URL url) throws ConverterException {
        String mimeType = null;
        byte[] bytes = null;
        try {
            final URLConnection urlCon = url.openConnection();
            urlCon.setConnectTimeout(2500);
            urlCon.setReadTimeout(2500);
            urlCon.connect();
            mimeType = urlCon.getContentType();
            final BufferedInputStream in = new BufferedInputStream(urlCon.getInputStream());
            try {
                final ByteArrayOutputStream buffer = new UnsynchronizedByteArrayOutputStream();
                final byte[] bbuf = new byte[8192];
                int read = -1;
                while ((read = in.read(bbuf, 0, bbuf.length)) != -1) {
                    buffer.write(bbuf, 0, read);
                }
                //final String value;
                //try {
                //    value = new BASE64Encoding().encode(new String(buffer.toByteArray(), CHARSET_ISO_8859_1));
                //} catch (final IOException e) {
                //    throw new ConverterException(e.getMessage(), e);
                //}
                bytes = buffer.toByteArray(); //value.getBytes(CHARSET_ISO_8859_1);
                // In case the config-file was not read (yet) the default value is given here
                long maxSize=33750000;
                if (null != ContactConfig.getInstance().getProperty("max_image_size")){
                	maxSize = Long.parseLong(ContactConfig.getInstance().getProperty("max_image_size"));
                }	
                if (maxSize > 0 && bytes.length > maxSize) {
                    LOG.warn("Contact image is too large and is therefore ignored", new Throwable());
                    bytes = null;
                }
            } finally {
                try {
                    in.close();
                } catch (final IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        } catch (final java.net.SocketTimeoutException e) {
            final String uri = url.toString();
            LOG.warn(new StringBuilder(64 + uri.length()).append("Either connecting to or reading from an image's URI timed out: ").append(
                uri).toString(), e);
        } catch (final IOException e) {
            final String uri = url.toString();
            LOG.warn(new StringBuilder(32 + uri.length()).append("Image  URI could not be loaded: ").append(uri).toString(), e);
        }
        if (bytes != null) {
            contactContainer.setImage1(bytes);
            if (mimeType == null) {
                mimeType = ImageTypeDetector.getMimeType(bytes);
                if ("application/octet-stream".equals(mimeType)) {
                    mimeType = getMimeType(url.toString());
                }
            }
            contactContainer.setImageContentType(mimeType);
        }
    }

    private static boolean IntegerProperty(final Object containerObj, final VersitObject object, final String VersitName, final Method setStringMethod) throws ConverterException {
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
        } catch (final Exception e) {
            throw new ConverterException(e);
        }
    }

    private static boolean StringProperty(final Object containerObj, final VersitObject object, final String VersitName, final Method setStringMethod) throws ConverterException {
        try {
            final Property property = object.getProperty(VersitName);
            if (property == null) {
                return false;
            }
            final Object[] args = { property.getValue().toString() };
            setStringMethod.invoke(containerObj, args);
            return true;
        } catch (final Exception e) {
            throw new ConverterException(e);
        }
    }

    private static boolean PrivacyProperty(final Object containerObj, final VersitObject object, final String VersitName, final Method setPrivacyMethod) throws ConverterException {
        try {
            final Property property = object.getProperty(VersitName);
            if (property == null) {
                return false;
            }
            final String privacy = (String) property.getValue();

            boolean isPrivate = false;
            if ("PRIVATE".equals(privacy)) {
                isPrivate = true;
            }
            if ("CONFIDENTIAL".equals(privacy)) {
                throw new ConverterPrivacyException();
            }
            final Object[] args = { Boolean.valueOf(isPrivate) };
            setPrivacyMethod.invoke(containerObj, args);
            return false;
        } catch (final ConverterPrivacyException e) {
            throw e;
        } catch (final Exception e) {
            throw new ConverterException(e);
        }
    }

    private boolean DateTimeProperty(final Object containerObj, final VersitObject object, final String VersitName, final Method setDateMethod) throws ConverterException {
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
        } catch (final Exception e) {
            throw new ConverterException(e);
        }
    }

    private static boolean DurationProperty(final Object containerObj, final VersitObject object, final String DurationName, final String StartName, final Method setDateMethod) throws ConverterException {
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
            final Calendar cal = (Calendar) ((DateTimeValue) property.getValue()).calendar.clone();
            cal.add(Calendar.WEEK_OF_YEAR, dur.Negative ? -dur.Weeks : dur.Weeks);
            cal.add(Calendar.DATE, dur.Negative ? -dur.Days : dur.Days);
            cal.add(Calendar.HOUR, dur.Negative ? -dur.Hours : dur.Hours);
            cal.add(Calendar.MINUTE, dur.Negative ? -dur.Minutes : dur.Minutes);
            cal.add(Calendar.SECOND, dur.Negative ? -dur.Seconds : dur.Seconds);
            final Object[] args = { cal.getTime() };
            setDateMethod.invoke(containerObj, args);
            return true;
        } catch (final Exception e) {
            throw new ConverterException(e);
        }
    }

    private void AttendeeProperty(final CalendarObject calContainerObj, final Property property) throws ConverterException {
        try {
            final String mail = ((URI) property.getValue()).getSchemeSpecificPart();
            final Participant participant;
            if (isInternalUser(mail)) {
                // fix for bug 8475
                participant = new UserParticipant(getInternalUser(mail).getId());
                // end:fix
            } else {
                participant = new ExternalUserParticipant(mail);
                participant.setDisplayName(mail);
            }
            calContainerObj.addParticipant(participant);
        } catch (final Exception e) {
            throw new ConverterException(e);
        }
    }

    /**
     * Finds out whether a user is internal, since internal users get treated differently when entering appointments or tasks.
     * 
     * @param mail - Mail address as string
     * @return true if is internal user, false otherwise
     */
    public boolean isInternalUser(final String mail) {
        try {
            final User uo = UserStorage.getInstance().searchUser(mail, ctx);
            return uo != null;
        } catch (final LdapException e) {
            return false;
        }
    }

    /**
     * Finds an internal user by its e-mail address. Note that an e-mail address is unique, but the identifier for an internal user is its
     * id. Should only be called after using <code>isInternalUser</code> or you have to live with the LdapException.
     */
    public User getInternalUser(final String mail) throws LdapException {
        return UserStorage.getInstance().searchUser(mail, ctx);
    }

    private static void RecurrenceProperty(final CalendarObject calContainerObj, final Property property, final Property start) throws ConverterException {
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
            // throw new ConverterException("COUNT is not supported.");
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
                final Iterator<?> j = recur.ByDay.iterator();
                for (int k = 0; k < size; k++) {
                    final RecurrenceValue.Weekday wd = (RecurrenceValue.Weekday) j.next();
                    days |= 1 << (wd.day - Calendar.SUNDAY);
                    if (week != 0 && week != wd.week) {
                        throw new ConverterException("Multiple weeks of month are not supported.");
                    }
                    week = wd.week;
                    if (week < 0) {
                        if (week == -1) {
                            week = 5;
                        } else {
                            throw new ConverterException(
                                "Only the last week of a month is supported. Counting from the end of the month above the first is not supported.");
                        }
                    }
                }
                calContainerObj.setDays(days);
                calContainerObj.setDayInMonth(week);
            } else {
                calContainerObj.setDayInMonth(cal.get(Calendar.DAY_OF_MONTH));
            }
            break;
        case RecurrenceValue.WEEKLY:
        case RecurrenceValue.DAILY: // fix: 7703
            int days = 0;
            final int size = recur.ByDay.size();
            final Iterator<?> j = recur.ByDay.iterator();
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

    private static void AddAlarms(final CalendarObject calContainerObj, final VersitObject object) throws ConverterException {
        final int count = object.getChildCount();
        for (int i = 0; i < count; i++) {
            final VersitObject alarm = object.getChild(i);
            Property property = alarm.getProperty("ACTION");
            // if (property != null &&
            // property.getValue().toString().equalsIgnoreCase("EMAIL")) {
            if (property != null && property.getValue().toString().equalsIgnoreCase("DISPLAY")) { // bugfix
                // :
                // 7473
                property = alarm.getProperty("TRIGGER");
                if (property != null) {
                    int time;
                    if (property.getValue() instanceof DurationValue) {
                        final DurationValue trigger = (DurationValue) property.getValue();
                        if (trigger.Months != 0 || trigger.Years != 0) {
                            throw new ConverterException("Irregular durations not supported");
                        }
                        time = trigger.Minutes + (trigger.Hours + (trigger.Days + 7 * trigger.Weeks) * 24) * 60;
                        if (trigger.Negative) { // note: This does not make
                            // sense currently, because
                            // "NEGATIVE" is never set
                            time = -time;
                        }
                        /*
                         * fix for 7473: TRIGGERs in ICAL are always negative (because they are _before_ the event), alarms in OX are always
                         * positive (because there is no reason for them to be _after_ the event).
                         */
                        time = -time;
                        // fix:end
                    } else {
                        final DateTimeValue trigger = (DateTimeValue) property.getValue();
                        property = object.getProperty(P_DTSTART);
                        if (property == null) {
                            throw new ConverterException("VALARM without DTSTART not supported");
                        }
                        time = (int) (((DateTimeValue) property.getValue()).calendar.getTimeInMillis() - trigger.calendar.getTimeInMillis());
                    }
                    if (calContainerObj instanceof Appointment) {
                        final Appointment appObj = (Appointment) calContainerObj;
                        appObj.setAlarm(time);
                        appObj.setAlarmFlag(true); // bugfix: 7473
                    } else if (calContainerObj instanceof Task) {
                        final Task taskObj = (Task) calContainerObj;
                        taskObj.setAlarm(new Date(taskObj.getStartDate().getTime() - (time * 60 * 1000)));
                        taskObj.setAlarmFlag(true); // bugfix: 7473
                    }
                }
            }
        }
    }

    private static void ListValue(final Object containerObj, final Method setMethod, final Object list, final String separator) throws ConverterException {
        try {
            final List<?> al = (ArrayList<?>) list;
            if (al == null || al.isEmpty()) {
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
        } catch (final Exception e) {
            throw new ConverterException(e);
        }
    }

    private static void ComplexProperty(final Object containerObj, final Method[] phones, final int[] index, final Object value) throws ConverterException {
        try {
            if (index[0] >= phones.length) {
                return;
            }
            final Object[] args = { value };
            phones[index[0]++].invoke(containerObj, args);
        } catch (final Exception e) {
            throw new ConverterException(e);
        }
    }

    public VersitObject convertTask(final Task task) throws ConverterException {
        final VersitObject object = new VersitObject("VTODO");
        // TODO CLASS
        addProperty(object, P_CLASS, "PUBLIC");
        // COMPLETED
        addDateTime(object, P_COMPLETED, task.getDateCompleted());
        // CREATED
        addDateTime(object, "CREATED", task.getCreationDate());
        // DESCRIPTION
        addProperty(object, P_DESCRIPTION, task.getNote());
        // DTSTAMP
        addDateTime(object, "DTSTAMP", new Date());
        // DTSTART
        addWeirdTaskDate(object, P_DTSTART, task.getStartDate());
        // GEO is ignored
        // LAST-MODIFIED
        addDateTime(object, "LAST-MODIFIED", task.getLastModified());
        // LOCATION is ignored
        // ORGANIZER
        if (organizerMailAddress != null) {
            addAddress(object, P_ORGANIZER, organizerMailAddress);
        } else {
            addAddress(object, P_ORGANIZER, task.getCreatedBy());
        }
        // PERCENT-COMPLETE
        addProperty(object, "PERCENT-COMPLETE", Integer.valueOf(task.getPercentComplete()));
        // PRIORITY
        final int[] priorities = { 9, 5, 1 };
        final int priority = task.getPriority();
        /*
         * TODO REMOVED DUE REMOVAL OF com.openexchange.groupware.links if (priority >= OXTask.LOW && priority <= OXTask.HIGH)
         * addProperty(object, "PRIORITY", new Integer(priorities[priority - OXTask.LOW])); else throw new
         * ConverterException("Invalid priority");
         */
        // TODO RECURRENCE-ID
        // TODO SEQUENCE
        // STATUS
        final String[] statuses = { "NEEDS-ACTION", "IN-PROCESS", P_COMPLETED, "NEEDS-ACTION", "CANCELLED" };
        final int status = task.getStatus();
        /*
         * TODO REMOVED DUE REMOVAL OF com.openexchange.groupware.tasks if (status >= OXTask.NOT_STARTED && status <= OXTask.DEFERRED)
         * addProperty(object, "STATUS", statuses[status - OXTask.NOT_STARTED]); else throw new ConverterException("Invlaid status");
         */
        // SUMMARY
        addProperty(object, P_SUMMARY, task.getTitle());
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
            final Iterator<?> i = new ArrayIterator(task.getParticipants());
            for (int k = 0; k < length; k++) {
                final Participant p = (Participant) i.next();
                if (p.getType() == Participant.USER) {
                    addAddress(object, P_ATTENDEE, p.getEmailAddress());
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
        addProperty(object, P_CATEGORIES, categories);
        // COMMENT is ignored
        // CONTACT is ignored
        // EXDATE is ignored
        // EXRULE is ignored
        // REQUEST-STATUS is ignored
        // TODO RELATED-TO
        // RESOURCES is ignored
        // RDATE is ignored
        // RRULE
        addRecurrence(object, P_RRULE, task);
        // TODO VALARM
        return object;
    }

    public VersitObject convertAppointment(final Appointment app) throws ConverterException {
        modifyRecurring(app);
        final VersitObject object = new VersitObject("VEVENT");
        // TODO CLASS
        addProperty(object, P_CLASS, "PUBLIC");
        // CREATED
        addDateTime(object, "CREATED", app.getCreationDate());
        // DESCRIPTION
        addProperty(object, P_DESCRIPTION, app.getNote());
        // DTSTART
        if (app.getFullTime()) {
            addWeirdTaskDate(object, P_DTSTART, app.getStartDate());
        } else {
            addDateTime(object, P_DTSTART, app.getStartDate());
        }
        // GEO is ignored
        // LAST-MODIFIED
        addDateTime(object, "LAST-MODIFIED", app.getLastModified());
        // LOCATION
        addProperty(object, "LOCATION", app.getLocation());
        // ORGANIZER
        if (organizerMailAddress == null) {
            addAddress(object, P_ORGANIZER, app.getCreatedBy());
        } else {
            addAddress(object, P_ORGANIZER, organizerMailAddress);
        }
        // PRIORITY is ignored
        // DTSTAMP
        addDateTime(object, "DTSTAMP", new Date());
        // TODO SEQUENCE
        // STATUS is ignored
        // SUMMARY
        addProperty(object, P_SUMMARY, app.getTitle());
        // TRANSP
        addProperty(object, "TRANSP", app.getShownAs() == Appointment.FREE ? "TRANSPARENT" : "OPAQUE");
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
        Iterator<?> i = null;
        if (app.containsParticipants()) {
            final int length = app.getParticipants().length;
            i = new ArrayIterator(app.getParticipants());
            for (int k = 0; k < length; k++) {
                final Participant p = (Participant) i.next();
                if (p.getType() == Participant.USER) {
                    addAddress(object, P_ATTENDEE, p.getEmailAddress());
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
            addProperty(object, P_CATEGORIES, categories);
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
            if (!resources.isEmpty()) {
                addProperty(object, "RESOURCES", resources);
            }
        }
        // RDATE is ignored
        // RRULE
        addRecurrence(object, P_RRULE, app);
        // TODO VALARM
        return object;
    }

    private static void modifyRecurring(final Appointment app) throws ConverterException {
        if (app.getRecurrenceType() != CalendarObject.NONE) {
            RecurringResultsInterface result;
            try {
                CalendarCollectionService calColl = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
                result = calColl.calculateFirstRecurring(app);
            } catch (final OXException e) {
                LOG.error(e.getMessage(), e);
                throw new ConverterException(e);
            }
            if (result.size() == 1) {
                app.setStartDate(new Date(result.getRecurringResult(0).getStart()));
                app.setEndDate(new Date(result.getRecurringResult(0).getEnd()));
            } else {
                throw new ConverterException("Unable to calculate first occurence of an appointment.");
            }
        }
    }

    public VersitObject convertContact(final Contact contact, final String version) throws ConverterException {
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
        if (contact.getImage1() != null) {
            final byte[] imageData = contact.getImage1();
            // First try as URI
            try {
                addProperty(object, "PHOTO", "VALUE", new String[] { "URI" }, new URI(new String(imageData, CHARSET_ISO_8859_1)));
            } catch (final UnsupportedEncodingException e2) {
                LOG.error(e2);
                throw new ConverterException(e2);
            } catch (final URISyntaxException e) {
                // Insert raw base64-encoded image bytes
                final Parameter type = new Parameter(P_TYPE);
                {
                    final String mimeType = contact.getImageContentType();
                    final String param;
                    if (mimeType == null) {
                        param = "JPEG";
                    } else if (mimeType.indexOf('/') != -1) {
                        param = mimeType.substring(mimeType.indexOf('/') + 1).toUpperCase();
                    } else {
                        param = mimeType.toUpperCase();
                    }
                    type.addValue(new ParameterValue(param));
                }
                /*
                 * Add image data as it is since ValueDefinition#write(FoldingWriter fw, Property property)) applies proper encoding
                 * dependent on "ENCODING" parameter
                 */
                addProperty(object, "PHOTO", "ENCODING", new String[] { "B" }, imageData).addParameter(type);
            }
        }
        String s = null;
        // BDAY
        addDate(object, "BDAY", contact.getBirthday(), false);
        // ADR
        addADR(
            object,
            contact,
            new String[] { PARAM_WORK },
            Contact.STREET_BUSINESS,
            Contact.CITY_BUSINESS,
            Contact.STATE_BUSINESS,
            Contact.POSTAL_CODE_BUSINESS,
            Contact.COUNTRY_BUSINESS);
        // ADR HOME
        addADR(
            object,
            contact,
            new String[] { PARAM_HOME },
            Contact.STREET_HOME,
            Contact.CITY_HOME,
            Contact.STATE_HOME,
            Contact.POSTAL_CODE_HOME,
            Contact.COUNTRY_HOME);
        // LABEL is ignored
        // TEL
        addProperty(object, P_TEL, P_TYPE, new String[] { PARAM_WORK, PARAM_VOICE }, contact.getTelephoneBusiness1());
        addProperty(object, P_TEL, P_TYPE, new String[] { PARAM_WORK, PARAM_VOICE }, contact.getTelephoneBusiness2());
        addProperty(object, P_TEL, P_TYPE, new String[] { PARAM_WORK, "fax" }, contact.getFaxBusiness());
        addProperty(object, P_TEL, P_TYPE, new String[] { "car", PARAM_VOICE }, contact.getTelephoneCar());
        addProperty(object, P_TEL, P_TYPE, new String[] { PARAM_HOME, PARAM_VOICE }, contact.getTelephoneHome1());
        addProperty(object, P_TEL, P_TYPE, new String[] { PARAM_HOME, PARAM_VOICE }, contact.getTelephoneHome2());
        addProperty(object, P_TEL, P_TYPE, new String[] { PARAM_HOME, "fax" }, contact.getFaxHome());
        addProperty(object, P_TEL, P_TYPE, new String[] { "cell", PARAM_VOICE }, contact.getCellularTelephone1());
        addProperty(object, P_TEL, P_TYPE, new String[] { "cell", PARAM_VOICE }, contact.getCellularTelephone2());
        // addProperty(object, "TEL", "TYPE", null, contact
        // .get(OXContact.PHONE_OTHER));
        // addProperty(object, "TEL", "TYPE", new String[] { "fax" }, contact
        // .get(OXContact.FAX_OTHER));
        addProperty(object, P_TEL, P_TYPE, new String[] { "isdn" }, contact.getTelephoneISDN());
        addProperty(object, P_TEL, P_TYPE, new String[] { "pager" }, contact.getTelephonePager());
        // EMAIL
        addProperty(object, P_EMAIL, contact.getEmail1());
        addProperty(object, P_EMAIL, contact.getEmail2());
        addProperty(object, P_EMAIL, contact.getEmail3());
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
            addProperty(object, P_CATEGORIES, getList(s, ','));
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

    private void addADR(final VersitObject object, final Contact contactContainer, final String[] type, final int street, final int city, final int state, final int postalCode, final int country) throws ConverterException {
        try {
            final ArrayList<ArrayList<Object>> adr = new ArrayList<ArrayList<Object>>(7);
            adr.add(null);
            adr.add(null);
            adr.add(makeList(getStreet(street, contactContainer)));
            adr.add(makeList(getCity(city, contactContainer)));
            adr.add(makeList(getState(state, contactContainer)));
            adr.add(makeList(getPostalCode(postalCode, contactContainer)));
            adr.add(makeList(getCountry(country, contactContainer)));
            addProperty(object, "ADR", P_TYPE, type, adr);
        } catch (final Exception e) {
            throw new ConverterException(e);
        }
    }

    private String getStreet(final int id, final Contact contactContainer) throws Exception {

        switch (id) {
        case Contact.STREET_BUSINESS:
            return contactContainer.getStreetBusiness();
        case Contact.STREET_HOME:
            return contactContainer.getStreetHome();
        case Contact.STREET_OTHER:
            return contactContainer.getStreetOther();
        default:
            throw new Exception("Unknown street constant " + id);
        }
    }

    private String getCity(final int id, final Contact contactContainer) throws Exception {

        switch (id) {
        case Contact.CITY_BUSINESS:
            return contactContainer.getCityBusiness();
        case Contact.CITY_HOME:
            return contactContainer.getCityHome();
        case Contact.CITY_OTHER:
            return contactContainer.getCityOther();
        default:
            throw new Exception("Unknown city constant " + id);
        }
    }

    private String getState(final int id, final Contact contactContainer) throws Exception {

        switch (id) {
        case Contact.STATE_BUSINESS:
            return contactContainer.getStateBusiness();
        case Contact.STATE_HOME:
            return contactContainer.getStateHome();
        case Contact.STATE_OTHER:
            return contactContainer.getStateOther();
        default:
            throw new Exception("Unknown state constant " + id);
        }
    }

    private String getCountry(final int id, final Contact contactContainer) throws Exception {

        switch (id) {
        case Contact.COUNTRY_BUSINESS:
            return contactContainer.getCountryBusiness();
        case Contact.COUNTRY_HOME:
            return contactContainer.getCountryHome();
        case Contact.COUNTRY_OTHER:
            return contactContainer.getCountryOther();
        default:
            throw new Exception("Unknown country constant " + id);
        }
    }

    private String getPostalCode(final int id, final Contact contactContainer) throws Exception {

        switch (id) {
        case Contact.POSTAL_CODE_BUSINESS:
            return contactContainer.getPostalCodeBusiness();
        case Contact.POSTAL_CODE_HOME:
            return contactContainer.getPostalCodeHome();
        case Contact.POSTAL_CODE_OTHER:
            return contactContainer.getPostalCodeOther();
        default:
            throw new Exception("Unknown postal code constant " + id);
        }
    }

    private static class ArrayIterator implements Iterator<Object> {

        private final int size;

        private int cursor;

        private final Object array;

        public ArrayIterator(final Object array) {
            final Class<?> type = array.getClass();
            if (!type.isArray()) {
                throw new IllegalArgumentException("Invalid type: " + type);
            }
            this.array = array;
            size = Array.getLength(array);
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public boolean hasNext() {
            return (cursor < size);
        }

        public Object next() {
            if (cursor < size) {
                return Array.get(array, cursor++);
            }
            throw new NoSuchElementException("No next element present in underlying array");
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

    private static Property addProperty(final VersitObject object, final String name, final String paramName, final String[] param, final Object value) {
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
        // Fill date property
        final DateTimeValue dt = new DateTimeValue();
        // dt.calendar.setTimeZone(DateTimeValue.GMT);
        dt.calendar.setTimeInMillis(value.getTime());
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

    private void addAddress(final VersitObject object, final String name, final String address) throws ConverterException {
        try {
            final Property property = new Property(name);
            if (address != null) {
                try {
                    property.setValue(new URI("mailto:" + address));
                } catch (final URISyntaxException e) {
                    final ConverterException ce = new ConverterException(e.getMessage());
                    ce.initCause(e);
                    throw ce;
                }
                object.addProperty(property);
            }
        } catch (final Exception e) {
            LOG.error(e);
            throw new ConverterException(e);
        }
    }

    private void addAddress(final VersitObject object, final String name, final int userId) throws ConverterException {
        try {
            final User userObj = UserStorage.getInstance().getUser(userId, ctx);
            if (userObj == null) {
                return;
            }
            final Property property = new Property(name);
            final String address = userObj.getMail();
            if (address != null) {
                try {
                    property.setValue(new URI("mailto:" + address));
                } catch (final URISyntaxException e) {
                    final ConverterException ce = new ConverterException(e.getMessage());
                    ce.initCause(e);
                    throw ce;
                }
                object.addProperty(property);
            }
        } catch (final Exception e) {
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
                            recur.ByDay.add(new Weekday(monthDay, Calendar.SUNDAY + i));
                        }
                    }
                }
                break;
            case CalendarObject.WEEKLY:
                final int days = oxobject.getDays();
                for (int i = 0; i < 7; i++) {
                    if ((days & (1 << i)) != 0) {
                        recur.ByDay.add(new Weekday(0, Calendar.SUNDAY + i));
                    }
                }
            }
            final int[] freqs = { RecurrenceValue.DAILY, RecurrenceValue.WEEKLY, RecurrenceValue.MONTHLY, RecurrenceValue.YEARLY };
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

    private static String getMimeType(final String filename) {
        return MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(filename);
    }

    private static boolean isValidImage(final byte[] data) {
        java.awt.image.BufferedImage bimg = null;
        try {

            bimg = javax.imageio.ImageIO.read(new com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream(data));
        } catch (final Exception e) {
            return false;
        }
        return (bimg != null);
    }
}
