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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import static com.openexchange.tools.io.IOUtils.closeStreamStuff;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.activation.FileTypeMap;
import javax.imageio.ImageIO;
import javax.mail.internet.AddressException;
import javax.mail.internet.idn.IDNA;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.contact.ContactConfig;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DistributionListEntryObject;
import com.openexchange.groupware.container.ExternalUserParticipant;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.StringAllocator;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.ImageTypeDetector;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.encoding.Base64;
import com.openexchange.tools.images.ImageTransformationService;
import com.openexchange.tools.images.ScaleType;
import com.openexchange.tools.images.TransformedImage;
import com.openexchange.tools.io.IOUtils;
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
 * <p>
 * <a href="http://tools.ietf.org/html/rfc2426">vCard MIME Directory Profile</a>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> (adapted Viktor's parser for OX6)
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> (bugfixing and refactoring)
 */
public class OXContainerConverter {

    private static final String P_OPEN_XCHANGE_CTYPE = "X-OPEN-XCHANGE-CTYPE";

    private static final String P_ORGANIZER = "ORGANIZER";

    private static final String P_TLX = "TLX";

    private static final String P_EMAIL = "EMAIL";

    private static final String PARAM_VOICE = "voice";

    private static final String P_TEL = "TEL";

    private static final String PARAM_WORK = "work";

    private static final String PARAM_HOME = "home";

    private static final String PARAM_OTHER = "dom";

    private static final String P_TYPE = "TYPE";

    private static final String P_DESCRIPTION = "DESCRIPTION";

    private static final String P_RRULE = "RRULE";

    private static final String P_CATEGORIES = "CATEGORIES";

    private static final String P_ATTENDEE = "ATTENDEE";

    private static final String P_DTSTART = "DTSTART";

    private static final String P_SUMMARY = "SUMMARY";

    private static final String P_COMPLETED = "COMPLETED";

    private static final String P_CLASS = "CLASS";

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(OXContainerConverter.class));

    private static final String CHARSET_ISO_8859_1 = "ISO-8859-1";

    private static final String CTYPE_CONTACT = "contact";

    private static final String CTYPE_DISTRIBUTION_LIST = "dlist";

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

    private final Context ctx;

    private final TimeZone timezone;

    private String organizerMailAddress;

    private boolean sendUTC = true;

    private boolean sendFloating;

    private boolean addDisplayName4DList;

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
        } catch (final OXException e) {
            throw new ConverterException(e);
        }
        timezone = TimeZoneUtils.getTimeZone(UserStorage.getStorageUser(session.getUserId(), ctx).getTimeZone());
    }

    public OXContainerConverter(final Session session, final Context ctx) {
        super();
        this.ctx = ctx;
        timezone = TimeZoneUtils.getTimeZone(UserStorage.getStorageUser(session.getUserId(), ctx).getTimeZone());
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

    /**
     * Gets the addDisplayName4DList
     *
     * @return The addDisplayName4DList
     */
    public boolean isAddDisplayName4DList() {
        return addDisplayName4DList;
    }

    /**
     * Sets the addDisplayName4DList
     *
     * @param addDisplayName4DList The addDisplayName4DList to set
     */
    public void setAddDisplayName4DList(final boolean addDisplayName4DList) {
        this.addDisplayName4DList = addDisplayName4DList;
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
            PrivacyProperty(taskContainer, object, P_CLASS, CommonObject.PRIVATE_FLAG);
            // COMPLETED
            DateTimeProperty(taskContainer, object, P_COMPLETED, Task.DATE_COMPLETED);
            // GEO is ignored
            // LAST-MODIFIED is ignored
            // LOCATION is ignored
            // ORGANIZER is ignored
            // PERCENT-COMPLETE
            IntegerProperty(taskContainer, object, "PERCENT-COMPLETE", Task.PERCENT_COMPLETED);
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
            StringProperty(taskContainer, object, P_SUMMARY, CalendarObject.TITLE);
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
            if (!DateTimeProperty(taskContainer, object, "DUE", CalendarObject.END_DATE)) {
                DurationProperty(taskContainer, object, "DURATION", P_DTSTART, CalendarObject.END_DATE);
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
            StringProperty(taskContainer, object, P_DESCRIPTION, CalendarObject.NOTE);
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
        PrivacyProperty(appContainer, object, P_CLASS, CommonObject.PRIVATE_FLAG);
        // CREATED is ignored
        // DESCRIPTION
        StringProperty(appContainer, object, P_DESCRIPTION, CalendarObject.NOTE);
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
        StringProperty(appContainer, object, "LOCATION", Appointment.LOCATION);
        // ORGANIZER is ignored
        // PRIORITY is ignored
        // DTSTAMP is ignored
        // TODO SEQUENCE
        // STATUS is ignored
        // SUMMARY
        StringProperty(appContainer, object, P_SUMMARY, CalendarObject.TITLE);
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
        if (!DateTimeProperty(appContainer, object, "DTEND", CalendarObject.END_DATE) && !DurationProperty(
            appContainer,
            object,
            "DURATION",
            P_DTSTART,
            CalendarObject.END_DATE)) {
            DateTimeProperty(appContainer, object, "DSTART", CalendarObject.END_DATE);
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
        StringProperty(contactContainer, object, "FN", Contact.DISPLAY_NAME);
        // N
        Property property = object.getProperty("N");
        if (property != null) {
            final ArrayList<?> N = (ArrayList<?>) property.getValue();

            fillArrayUpTo(N, 5); // fix for 7248
            ListValue(contactContainer, Contact.SUR_NAME, N.get(0), " ");
            ListValue(contactContainer, Contact.GIVEN_NAME, N.get(1), " ");
            ListValue(contactContainer, Contact.MIDDLE_NAME, N.get(2), " ");
            ListValue(contactContainer, Contact.TITLE, N.get(3), " ");
            ListValue(contactContainer, Contact.SUFFIX, N.get(4), " ");
        }
        // NICKNAME
        StringFromListProperty(contactContainer, object, "NICKNAME", Contact.NICKNAME);
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
                	byte[] imageData = (byte[])propertyValue;
                	Rectangle clipRect = extractClipRect(property.getParameter("X-ABCROP-RECTANGLE"));
                	if (null != clipRect) {
                		/*
                		 * determine image format
                		 */
        				String formatName = "JPEG";
        				Parameter typeParameter = property.getParameter("TYPE");
        				if (null != typeParameter && 1 == typeParameter.getValueCount()) {
        					String type = typeParameter.getValue(0).getText();
        					if (null != type && 0 < type.length()) {
        						formatName = type;
        					}
        				}
        				/*
                		 * try to crop the image
                		 */
                		try {
                			imageData = doABCrop(imageData, clipRect, formatName);
                		} catch (IOException e) {
                			LOG.error("error cropping image, falling back to uncropped image.", e);
                		} catch (OXException e) {
                			LOG.error("error cropping image, falling back to uncropped image.", e);
						}
                	}
                    contactContainer.setImage1(imageData);
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
        DateTimeProperty(contactContainer, object, "BDAY", Contact.BIRTHDAY);
        // MAILER is ignored
        // TZ is ignored
        // GEO is ignored
        // TITLE
        StringProperty(contactContainer, object, "TITLE", Contact.PROFESSION);
        // ROLE
        StringProperty(contactContainer, object, "ROLE", Contact.POSITION);
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
        StringProperty(contactContainer, object, "NOTE", Contact.NOTE);
        // PRODID is ignored
        // REV is ignored
        // SORT-STRING is ignored
        // SOUND is ignored
        // URL
        StringProperty(contactContainer, object, "URL", Contact.URL);
        // UID
        StringProperty(contactContainer, object, "UID", CommonObject.UID);
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
        final int OTHER = 6;

        final int VOICE = 0;
        final int FAX = 1;

        final int[][][] phones = {
            { { Contact.TELEPHONE_BUSINESS1, Contact.TELEPHONE_BUSINESS2 }, { Contact.FAX_BUSINESS } },
            { { Contact.TELEPHONE_HOME1, Contact.TELEPHONE_HOME2 }, { Contact.FAX_HOME } },
            { { Contact.CELLULAR_TELEPHONE1, Contact.CELLULAR_TELEPHONE2 }, {} }, { { Contact.TELEPHONE_CAR }, {} },
            { { Contact.TELEPHONE_ISDN }, {} }, { { Contact.TELEPHONE_PAGER }, {} }, { { Contact.TELEPHONE_OTHER }, { Contact.FAX_OTHER } } };

        final int[][][] index = {
            { { 0 }, { 0 } }, { { 0 }, { 0 } }, { { 0 }, { 0 } }, { { 0 }, { 0 } }, { { 0 }, { 0 } }, { { 0 }, { 0 } }, { { 0 }, { 0 } } };

        final int[] emails = { Contact.EMAIL1, Contact.EMAIL2, Contact.EMAIL3 };

        final int[] emailIndex = { 0 };

        final ArrayList<Object> cats = new ArrayList<Object>();

        boolean dlist = false;
        {
            final Property oxCtype = object.getProperty(P_OPEN_XCHANGE_CTYPE);
            if (oxCtype != null && CTYPE_DISTRIBUTION_LIST.equalsIgnoreCase(oxCtype.getValue().toString())) {
                dlist = true;
                contactContainer.setMarkAsDistributionlist(true);
            }
        }

        final int count = object.getPropertyCount();
        for (int i = 0; i < count; i++) {
            property = object.getProperty(i);
            // ADR
            if ("ADR".equals(property.name)) {
                boolean isHome = false, isWork = false, isOther = true;
                final Parameter type = property.getParameter(P_TYPE);
                if (type != null) {
                    isOther = false;
                    for (int j = 0; j < type.getValueCount(); j++) {
                        final String value = type.getValue(j).getText();
                        isHome  |= PARAM_HOME.equalsIgnoreCase(value);
                        isWork  |= PARAM_WORK.equalsIgnoreCase(value);
                        isOther |= PARAM_OTHER.equalsIgnoreCase(value);
                    }
                }
                final ArrayList<?> A = (ArrayList<?>) property.getValue();
                fillArrayUpTo(A, 7);
                if (A == null) {
                    throw new ConverterException("Invalid property ADR");
                }
                if (isWork) {
                    ListValue(contactContainer, Contact.STREET_BUSINESS, A.get(2), "\n");
                    ListValue(contactContainer, Contact.CITY_BUSINESS, A.get(3), "\n");
                    ListValue(contactContainer, Contact.STATE_BUSINESS, A.get(4), "\n");
                    ListValue(contactContainer, Contact.POSTAL_CODE_BUSINESS, A.get(5), "\n");
                    ListValue(contactContainer, Contact.COUNTRY_BUSINESS, A.get(6), "\n");
                }
                if (isHome) {
                    ListValue(contactContainer, Contact.STREET_HOME, A.get(2), "\n");
                    ListValue(contactContainer, Contact.CITY_HOME, A.get(3), "\n");
                    ListValue(contactContainer, Contact.STATE_HOME, A.get(4), "\n");
                    ListValue(contactContainer, Contact.POSTAL_CODE_HOME, A.get(5), "\n");
                    ListValue(contactContainer, Contact.COUNTRY_HOME, A.get(6), "\n");
                }
                if (isOther) {
                    ListValue(contactContainer, Contact.STREET_OTHER, A.get(2), "\n");
                    ListValue(contactContainer, Contact.CITY_OTHER, A.get(3), "\n");
                    ListValue(contactContainer, Contact.STATE_OTHER, A.get(4), "\n");
                    ListValue(contactContainer, Contact.POSTAL_CODE_OTHER, A.get(5), "\n");
                    ListValue(contactContainer, Contact.COUNTRY_OTHER, A.get(6), "\n");
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
                            } else if (value.equalsIgnoreCase(PARAM_OTHER)) {
                                idx = OTHER;
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
            // IM
            else if ("IMPP".equals(property.name)) {
                String value = property.getValue().toString();
                if (null != value) {
                    try {
                        value = java.net.URLDecoder.decode(value, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        LOG.debug("Error decoding IMPP value", e);
                    }
                }
                Parameter type = property.getParameter(P_TYPE);
                boolean set = false;
                if (null != type) {
                    for (int j = 0; j < type.getValueCount() && false == set; j++) {
                        String typeValue = type.getValue(j).getText();
                        if (PARAM_HOME.equalsIgnoreCase(typeValue)) {
                            /*
                             * use IM 2 if possible
                             */
                            contactContainer.setInstantMessenger2(value);
                            set = true;
                        } else if (PARAM_WORK.equalsIgnoreCase(typeValue)) {
                            /*
                             * use IM 1 if possible
                             */
                            contactContainer.setInstantMessenger1(value);
                            set = true;
                        }
                    }
                }
                /*
                 * fill first available
                 */
                if (false == set) {
                    if (false == contactContainer.containsInstantMessenger2()) {
                        contactContainer.setInstantMessenger2(value);
                    } else if (false == contactContainer.containsInstantMessenger1()) {
                        contactContainer.setInstantMessenger1(value);
                    }
                }
            }
            // EMAIL
            else if (P_EMAIL.equals(property.name)) {
                String value = property.getValue().toString();
                String personal = null;
                // fix for: 7249
                boolean isProperEmailAddress = value != null && value.length() > 0;
                if (isProperEmailAddress) {
                    try {
                    	final QuotedInternetAddress address = new QuotedInternetAddress(value);
                        address.validate();
                        value = address.getIDNAddress();
                        personal = address.getPersonal();
                    } catch (final AddressException e) {
                        isProperEmailAddress = false;
                    }
                }
                // fix: end
                if (isProperEmailAddress) {
                    if (dlist) {
                        try {
                            DistributionListEntryObject[] distributionList = contactContainer.getDistributionList();
                            if (null == distributionList) {
                                distributionList = new DistributionListEntryObject[1];
                            } else {
                                final DistributionListEntryObject[] newDistributionList = new DistributionListEntryObject[distributionList.length + 1];
                                System.arraycopy(distributionList, 0, newDistributionList, 0, distributionList.length);
                                distributionList = newDistributionList;
                            }
                            /*
                             * Append new entry
                             */
                            final DistributionListEntryObject newEntry = new DistributionListEntryObject();
                            if (addDisplayName4DList) {
                                final Parameter displayNameParameter = property.getParameter("FN");
                                if (null != displayNameParameter) {
                                    newEntry.setDisplayname(decodeQP(displayNameParameter.getValue(0).getText()));
                                }
                            } else {
                                newEntry.setDisplayname(null == personal ? value : personal);
                            }
                            newEntry.setEmailaddress(value);
                            distributionList[distributionList.length - 1] = newEntry;
                            contactContainer.setDistributionList(distributionList);
                        } catch (final OXException e) {
                            throw new ConverterException(e);
                        }
                    } else {
                        Parameter type = property.getParameter(P_TYPE);
                        int set = 0;
                        if (null != type) {
                            for (int j = 0; j < type.getValueCount() && 0 == set; j++) {
                                String typeValue = type.getValue(j).getText();
                                if (PARAM_HOME.equalsIgnoreCase(typeValue)) {
                                    /*
                                     * use email 2 if possible
                                     */
                                    if (contactContainer.containsEmail2()) {
                                        // try to move somewhere else
                                        if (false == contactContainer.containsEmail1()) {
                                            contactContainer.setEmail1(contactContainer.getEmail2());
                                        } else if (false == contactContainer.containsEmail3()) {
                                            contactContainer.setEmail3(contactContainer.getEmail2());
                                        } else {
                                            LOG.debug("Can only save one 'home' email address, going to overwrite existing value.");
                                        }
                                    }
                                    contactContainer.setEmail2(value);
                                    set = 2;
                                } else if (PARAM_WORK.equalsIgnoreCase(typeValue)) {
                                    /*
                                     * use email 1 if possible
                                     */
                                    if (contactContainer.containsEmail1()) {
                                        // try to move somewhere else
                                        if (false == contactContainer.containsEmail2()) {
                                            contactContainer.setEmail2(contactContainer.getEmail1());
                                        } else if (false == contactContainer.containsEmail3()) {
                                            contactContainer.setEmail3(contactContainer.getEmail1());
                                        } else {
                                            LOG.debug("Can only save one 'work' email address, going to overwrite existing value.");
                                        }
                                    }
                                    contactContainer.setEmail1(value);
                                    set = 1;
                                } else if ("other".equalsIgnoreCase(typeValue)) {
                                    /*
                                     * use email 3 if possible
                                     */
                                    if (contactContainer.containsEmail3()) {
                                        // try to move somewhere else
                                        if (false == contactContainer.containsEmail1()) {
                                            contactContainer.setEmail1(contactContainer.getEmail3());
                                        } else if (false == contactContainer.containsEmail2()) {
                                            contactContainer.setEmail2(contactContainer.getEmail3());
                                        } else {
                                            LOG.debug("Can only save one 'other' email address, going to overwrite existing value.");
                                        }
                                    }
                                    contactContainer.setEmail3(value);
                                    set = 3;
                                }
                            }
                        }
                        /*
                         * fill first available
                         */
                        if (0 == set) {
                            if (false == contactContainer.containsEmail1()) {
                                contactContainer.setEmail1(value);
                                set = 1;
                            } else if (false == contactContainer.containsEmail2()) {
                                contactContainer.setEmail2(value);
                                set = 2;
                            } else if (false == contactContainer.containsEmail3()) {
                                contactContainer.setEmail3(value);
                                set = 3;
                            }
                        }
                        /*
                         * mark default address if defined
                         */
                        if (0 < set && null != type) {
                            for (int j = 0; j < type.getValueCount(); j++) {
                                if ("pref".equalsIgnoreCase(type.getValue(j).getText())) {
                                    contactContainer.setDefaultAddress(set);
                                    break;
                                }
                            }
                        }
//                        ComplexProperty(contactContainer, emails, emailIndex, value);
                    }
                } else {
                    // fix for: 7719
                    final Parameter type = property.getParameter(P_TYPE);
                    if (type != null) {
                        final ParameterValue parameterValue = type.getValue(0);
                        if (parameterValue != null && parameterValue.getText() != null && P_TLX.equals(parameterValue.getText())) {
                            contactContainer.setTelephoneTelex(property.getValue().toString());
                        }
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
        ListValue(contactContainer, CommonObject.CATEGORIES, cats, ",");

        return contactContainer;
    }

    /**
     * fills and array with null up to a specified amount
     */
    private void fillArrayUpTo(ArrayList<?> a, final int limit) {
        if (a == null) {
            a = new ArrayList<Object>();
        }
        for (int i = a.size(); i < limit; i++) {
            a.add(null);
        }
    }

    /**
     * Extracts the clipping information from the supplied 'X-ABCROP-RECTANGLE'
     * rectangle if defined. The result's 'width' and 'height' properties
     * represent the dimensions of the target image. The 'x' property is the
     * horizontal offset to draw the source image in the target image from the
     * left border, the 'y' property is the vertical offset from the bottom.
     *
     * @param cropParameter the 'X-ABCROP-RECTANGLE' parameter
     * @return the clipping rectangle, or <code>null</code>, if not defined
     */
    private static Rectangle extractClipRect(Parameter cropParameter) {
    	if (null != cropParameter && 0 < cropParameter.getValueCount()) {
    		Pattern clipRectPattern = Pattern.compile("ABClipRect_1&([-+]?\\d+?)&([-+]?\\d+?)&([-+]?\\d+?)&([-+]?\\d+?)&");
    		for (int i = 0; i < cropParameter.getValueCount(); i++) {
    			String text = cropParameter.getValue(i).getText();
    			Matcher matcher = clipRectPattern.matcher(text);
    			if (matcher.find()) {
    				try {
        				int offsetLeft = Integer.parseInt(matcher.group(1));
        				int offsetBottom = Integer.parseInt(matcher.group(2));
        				int targetWidth = Integer.parseInt(matcher.group(3));
        				int targetHeight = Integer.parseInt(matcher.group(4));
        				return new Rectangle(offsetLeft, offsetBottom, targetWidth, targetHeight);
    				} catch (NumberFormatException e) {
    					LOG.warn("unable to parse clipping rectangle from " + text, e);
    				}
    			}
    		}
    	}
		return null;
    }

    /**
     * Generates a "X-ABCROP-RECTANGLE" parameter for the supplied transformed image.
     *
     * @param transformedImage The transformed image
     * @return The parameter
     */
    private static Parameter getABCropRectangle(TransformedImage transformedImage) {
        Parameter parameter = new Parameter("X-ABCROP-RECTANGLE");
        StringAllocator stringAllocator = new StringAllocator(64);
        stringAllocator.append("ABClipRect_1&");
        int width = transformedImage.getWidth();
        int height = transformedImage.getHeight();
        if (width < height) {
            stringAllocator.append('-').append((height - width) / 2).append("&0&").append(height).append('&').append(height);
        } else if (width > height) {
            stringAllocator.append("0&-").append((width - height) / 2).append('&').append(width).append('&').append(width);
        } else {
            stringAllocator.append("0&0&").append(width).append('&').append(height);
        }
        if (null != transformedImage.getMD5()) {
            stringAllocator.append('&').append(Base64.encode(transformedImage.getMD5()));
        }
        parameter.addValue(new ParameterValue(stringAllocator.toString()));
        return parameter;
    }

    /**
     * Performs a crop operation on the source image as defined by the
     * supplied clipping rectangle.
     *
     * @param source the source image
     * @param clipRect the clip rectangle from an 'X-ABCROP-RECTANGLE' property
     * @param formatName the target image format
     * @return the cropped image
     * @throws IOException
     * @throws OXException
     */
    private static byte[] doABCrop(byte[] source, Rectangle clipRect, String formatName) throws IOException, OXException {
    	InputStream inputStream = null;
    	try {
    		/*
    		 * read source image
    		 */
    		inputStream = new ByteArrayInputStream(source);
        	BufferedImage sourceImage = ImageIO.read(inputStream);
        	/*
        	 * crop the image
        	 */
        	ImageTransformationService imageService = ServerServiceRegistry.getInstance().getService(ImageTransformationService.class, true);
        	return imageService.transfom(sourceImage).crop(clipRect.x * -1,
        			clipRect.height + clipRect.y - sourceImage.getHeight(), clipRect.width, clipRect.height).getBytes(formatName);
    	} finally {
    		Streams.close(inputStream);
    	}
    }

    /**
     * Scales an image if needed to fit into the supplied rectangular area.
     *
     * @param source The image data
     * @param maxWidth The maximum target width
     * @param maxHeight The maximum target height
     * @param formatName The image format name
     * @return The scaled image
     * @throws IOException
     * @throws OXException
     */
    private static TransformedImage scaleImageIfNeeded(byte[] source, int maxWidth, int maxHeight, String formatName) throws IOException, OXException {
        ImageTransformationService imageService = ServerServiceRegistry.getInstance().getService(
            ImageTransformationService.class, true);
        if (0 < maxWidth || 0 < maxHeight) {
            return imageService.transfom(source).scale(maxWidth, maxHeight, ScaleType.CONTAIN).getTransformedImage(formatName);
        } else {
            return imageService.transfom(source).getTransformedImage(formatName);
        }
    }

    /**
     * Scales a contact image if configured via <code>com.openexchange.contact.scaleVCardImages</code>.
     *
     * @param source The image data
     * @param formatName The image format name
     * @return The scaled image data, or the unchanged <code>source</code> if not configured
     * @throws IOException
     * @throws OXException
     */
    private static TransformedImage scaleImageIfNeeded(byte[] source, String formatName) throws IOException, OXException {
        if (null != source) {
            int maxWidth = -1;
            int maxHeight = -1;
            ConfigurationService configService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class, true);
            String value = configService.getProperty("com.openexchange.contact.scaleVCardImages", "");
            if (null != value && 0 < value.length()) {
                int idx = value.indexOf('x');
                if (1 > idx) {
                    throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(value);
                }
                try {
                    maxWidth = Integer.parseInt(value.substring(0, idx));
                    maxHeight = Integer.parseInt(value.substring(idx + 1));
                } catch (NumberFormatException e) {
                    throw ConfigurationExceptionCodes.INVALID_CONFIGURATION.create(e, value);
                }
            }
            return scaleImageIfNeeded(source, maxWidth, maxHeight, formatName);
        }
        return null;
    }

    /**
     * Open a new {@link URLConnection URL connection} to specified parameter's value which indicates to be an URI/URL. The image's data and
     * its MIME type is then read from opened connection and put into given {@link Contact contact container}.
     *
     * @param contact The contact container to fill
     * @param url The URI parameter's value
     * @throws ConverterException If converting image's data fails
     */
    public static void loadImageFromURL(final Contact contact, final String url) throws ConverterException {
        try {
            loadImageFromURL(contact, new URL(url));
        } catch (final MalformedURLException e) {
            throw new ConverterException("Image URL is not wellformed.", e);
        }
    }

    /**
     * Open a new {@link URLConnection URL connection} to specified parameter's value which indicates to be an URI/URL. The image's data and
     * its MIME type is then read from opened connection and put into given {@link Contact contact container}.
     *
     * @param contact The contact container to fill
     * @param url The image URL
     * @throws ConverterException If converting image's data fails
     */
    private static void loadImageFromURL(final Contact contact, final URL url) throws ConverterException {
        String mimeType = null;
        byte[] bytes = null;
        try {
            final URLConnection urlCon = url.openConnection();
            urlCon.setConnectTimeout(2500);
            urlCon.setReadTimeout(2500);
            urlCon.connect();
            mimeType = urlCon.getContentType();
            final InputStream in = urlCon.getInputStream();
            try {
                final ByteArrayOutputStream buffer = new UnsynchronizedByteArrayOutputStream(in.available());
                IOUtils.transfer(in, buffer);
                bytes = buffer.toByteArray();
                // In case the configuration file was not read (yet) the default value is given here
                final long maxSize = ContactConfig.getInstance().getMaxImageSize();
                if (maxSize > 0 && bytes.length > maxSize) {
                    final ConverterException e = new ConverterException(
                        "Contact image is " + bytes.length + " bytes large and limit is " + maxSize + " bytes. Image is therefore ignored.");
                    LOG.warn(e.getMessage(), e);
                    bytes = null;
                }
            } finally {
                closeStreamStuff(in);
            }
        } catch (final SocketTimeoutException e) {
            throw new ConverterException("Timeout reading \"" + url.toString() + "\"", e);
        } catch (final IOException e) {
            throw new ConverterException("IO problem while reading \"" + url.toString() + "\"", e);
        }
        if (mimeType == null) {
            mimeType = ImageTypeDetector.getMimeType(bytes);
            if ("application/octet-stream".equals(mimeType)) {
                mimeType = getMimeType(url.toString());
            }
        }
        if (bytes != null && isValidImage(bytes)) {
            // Mime type should be of image type. Otherwise web server send some error page instead of 404 error code.
            contact.setImage1(bytes);
            contact.setImageContentType(mimeType);
        }
    }

    private static boolean IntegerProperty(final CommonObject containerObj, final VersitObject object, final String VersitName, final int fieldNumber) throws ConverterException {
        try {
            final Property property = object.getProperty(VersitName);
            if (property == null) {
                return false;
            }
            if (property.getValue() instanceof Integer) {
                final Integer val = (Integer) property.getValue();
                containerObj.set(fieldNumber, val);
                return true;
            }
            return false;
        } catch (final Exception e) {
            throw new ConverterException(e);
        }
    }

    private static boolean StringProperty(final CommonObject containerObj, final VersitObject object, final String VersitName, final int fieldNumber) throws ConverterException {
        try {
            final Property property = object.getProperty(VersitName);
            if (property == null) {
                return false;
            }
            containerObj.set(fieldNumber, property.getValue().toString());
            return true;
        } catch (final Exception e) {
            throw new ConverterException(e);
        }
    }

    /**
     * Bridges the gap between a Versit object using a list and OX using a single string field
     */
    private static boolean StringFromListProperty(final CommonObject containerObj, final VersitObject object, final String VersitName, final int fieldNumber) throws ConverterException {
        try {
            final Property property = object.getProperty(VersitName);
            if (property == null) {
                return false;
            }
            if (property.getValue() instanceof String) {
                return StringProperty(containerObj, object, VersitName, fieldNumber);
            }

            final List<String> args = (List<String>) property.getValue();
            containerObj.set(fieldNumber, Strings.join(args, ", "));
            return true;
        } catch (final Exception e) {
            throw new ConverterException(e);
        }
    }

    private static boolean PrivacyProperty(final CalendarObject containerObj, final VersitObject object, final String VersitName, final int fieldNumber) throws ConverterException {
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
            containerObj.set(fieldNumber, Boolean.valueOf(isPrivate));
            return false;
        } catch (final ConverterPrivacyException e) {
            throw e;
        } catch (final Exception e) {
            throw new ConverterException(e);
        }
    }

    private boolean DateTimeProperty(final CommonObject containerObj, final VersitObject object, final String VersitName, final int fieldNumber) throws ConverterException {
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
            containerObj.set(fieldNumber, date.calendar.getTime());
            return true;
        } catch (final Exception e) {
            throw new ConverterException(e);
        }
    }

    private static boolean DurationProperty(final CommonObject containerObj, final VersitObject object, final String DurationName, final String StartName, final int fieldNumber) throws ConverterException {
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
            containerObj.set(fieldNumber, cal.getTime());
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
        } catch (final OXException e) {
            return false;
        }
    }

    /**
     * Finds an internal user by its e-mail address. Note that an e-mail address is unique, but the identifier for an internal user is its
     * id. Should only be called after using <code>isInternalUser</code> or you have to live with the OXException.
     */
    public User getInternalUser(final String mail) throws OXException {
        return UserStorage.getInstance().searchUser(mail, ctx);
    }

    private static void RecurrenceProperty(final CalendarObject calContainerObj, final Property property, final Property start) throws ConverterException {
        final RecurrenceValue recur = (RecurrenceValue) property.getValue();
        if (start == null) {
            throw new ConverterException("RRULE without DTSTART");
        }
        final Calendar cal = ((DateTimeValue) start.getValue()).calendar;
        final int[] recurTypes = { CalendarObject.NONE, CalendarObject.NONE, CalendarObject.NONE, CalendarObject.DAILY, CalendarObject.WEEKLY, CalendarObject.MONTHLY, CalendarObject.YEARLY };
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
            //$FALL-THROUGH$
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

    private static void ListValue(final CommonObject containerObj, final int fieldNumber, final Object list, final String separator) throws ConverterException {
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
            containerObj.set(fieldNumber, sb.toString());
        } catch (final Exception e) {
            throw new ConverterException(e);
        }
    }

    private static void ComplexProperty(final CommonObject containerObj, final int[] phones, final int[] index, final Object value) throws ConverterException {
        try {
            if (index[0] >= phones.length) {
                return;
            }
            containerObj.set(phones[index[0]++], value);
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
                final CalendarCollectionService calColl = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
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
        // Distribution list?
        if (!contact.containsMarkAsDistributionlist() || !contact.getMarkAsDistribtuionlist()) {
            addProperty(object, P_OPEN_XCHANGE_CTYPE, CTYPE_CONTACT);
            // PHOTO
            if (contact.getImage1() != null) {
                byte[] imageData = contact.getImage1();
                // First try as URI
                try {
                    addProperty(object, "PHOTO", "VALUE", new String[] { "URI" }, new URI(new String(imageData, Charsets.ISO_8859_1)));
                } catch (final UnsupportedCharsetException e2) {
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
                    TransformedImage transformedImage = null;
                    try {
                        transformedImage = scaleImageIfNeeded(imageData, contact.getImageContentType());
                    } catch (IOException x) {
                        LOG.error("error scaling image, falling back to unscaled image.", x);
                    } catch (OXException x) {
                        LOG.error("error scaling image, falling back to unscaled image.", x);
                    } catch (RuntimeException x) {
                        LOG.error("error scaling image, falling back to unscaled image.", x);
                    }
                    /*
                     * Add image data as it is since ValueDefinition#write(FoldingWriter fw, Property property)) applies proper encoding
                     * dependent on "ENCODING" parameter
                     */
                    if (null != transformedImage) {
                        Property photoProperty = addProperty(object, "PHOTO", "ENCODING", new String[] { "B" }, transformedImage.getImageData());
                        photoProperty.addParameter(type);
                        photoProperty.addParameter(getABCropRectangle(transformedImage));
                    } else {
                        addProperty(object, "PHOTO", "ENCODING", new String[] { "B" }, imageData).addParameter(type);
                    }
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
            // ADR OTHER (as "dom" since there is no equivalent)
        	addADR(
                object,
                contact,
                new String[] { PARAM_OTHER },
                Contact.STREET_OTHER,
                Contact.CITY_OTHER,
                Contact.STATE_OTHER,
                Contact.POSTAL_CODE_OTHER,
                Contact.COUNTRY_OTHER);
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

            addProperty(object, P_TEL, P_TYPE, new String[] { PARAM_OTHER, PARAM_VOICE }, contact.getTelephoneOther());
            addProperty(object, P_TEL, P_TYPE, new String[] { PARAM_OTHER, "fax" }, contact.getFaxOther());
            addProperty(object, P_TEL, P_TYPE, new String[] { "isdn" }, contact.getTelephoneISDN());
            addProperty(object, P_TEL, P_TYPE, new String[] { "pager" }, contact.getTelephonePager());
            // EMAIL
            addProperty(object, P_EMAIL, P_TYPE, 1 == contact.getDefaultAddress() ?
                new String[] { "INTERNET", PARAM_WORK, "pref" } : new String[] { "INTERNET", PARAM_WORK }, contact.getEmail1());
            addProperty(object, P_EMAIL, P_TYPE, 2 == contact.getDefaultAddress() ?
                new String[] { "INTERNET", PARAM_HOME, "pref" } : new String[] { "INTERNET", PARAM_HOME }, contact.getEmail2());
            addProperty(object, P_EMAIL, P_TYPE, 3 == contact.getDefaultAddress() ?
                new String[] { "INTERNET", "pref", "other" } : new String[] { "INTERNET", "other" }, contact.getEmail3());
            // MAILER is ignored
            // TZ is ignored
            // GEO is ignored
            // TITLE
            addProperty(object, "TITLE", contact.getProfession());
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
        } else {
            addProperty(object, P_OPEN_XCHANGE_CTYPE, CTYPE_DISTRIBUTION_LIST);
            final DistributionListEntryObject[] distributionList = contact.getDistributionList();
            if (null != distributionList && 0 < distributionList.length) {
                for (final DistributionListEntryObject distributionListEntry : distributionList) {
                    final String address = distributionListEntry.getEmailaddress();
                    if (address != null) {
                        final Property property = new Property(P_EMAIL);
                        property.setValue(address);
                        {
                            final Parameter parameter = new Parameter(P_TYPE);
                            parameter.addValue(new ParameterValue("INTERNET"));
                            property.addParameter(parameter);
                        }
                        if (addDisplayName4DList) {
                            final String displayName = distributionListEntry.getDisplayname();
                            if (null != displayName) {
                                final Parameter parameter = new Parameter("FN");
                                parameter.addValue(new ParameterValue(encodeQP(displayName)));
                                property.addParameter(parameter);
                            }
                        }
                        object.addProperty(property);
                    }
                }
            }
        }
        // CATEGORIES
        if (null != contact.getCategories() && 0 < contact.getCategories().length()) {
            ArrayList<String> categories = new ArrayList<String>();
            StringTokenizer tokenizer = new StringTokenizer(contact.getCategories(), ",");
            while (tokenizer.hasMoreTokens()) {
                categories.add(tokenizer.nextToken());
            }
            if (0 < categories.size()) {
                addProperty(object, P_CATEGORIES, categories);
            }
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
        addProperty(object, "UID", contact.getUid());
        // IMPP
        addProperty(object, "IMPP", P_TYPE, new String[] { PARAM_WORK }, contact.getInstantMessenger1());
        addProperty(object, "IMPP", P_TYPE, new String[] { PARAM_HOME }, contact.getInstantMessenger2());
        // TODO CLASS
        // KEY is ignored
        return object;
    }

    private void addADR(final VersitObject object, final Contact contactContainer, final String[] type, final int street, final int city, final int state, final int postalCode, final int country) throws ConverterException {
        try {
        	String streetValue = getStreet(street, contactContainer);
        	String cityValue = getCity(city, contactContainer);
        	String stateValue = getState(state, contactContainer);
        	String postalCodeValue = getPostalCode(postalCode, contactContainer);
        	String countryValue = getCountry(country, contactContainer);
        	if (null != streetValue || null != cityValue || null != stateValue || null != postalCodeValue || null != countryValue) {
                final ArrayList<ArrayList<Object>> adr = new ArrayList<ArrayList<Object>>(7);
                adr.add(null);
                adr.add(null);
                adr.add(makeList(streetValue));
                adr.add(makeList(cityValue));
                adr.add(makeList(stateValue));
                adr.add(makeList(postalCodeValue));
                adr.add(makeList(countryValue));
                addProperty(object, "ADR", P_TYPE, type, adr);
        	}
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

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext() {
            return (cursor < size);
        }

        @Override
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
                    property.setValue(new URI("mailto:" + IDNA.toACE(address)));
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
        return FileTypeMap.getDefaultFileTypeMap().getContentType(filename);
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

    private static final BitSet PRINTABLE_CHARS = new BitSet(256);
    // Static initializer for printable chars collection
    static {
        for (int i = '0'; i <= '9'; i++) {
            PRINTABLE_CHARS.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            PRINTABLE_CHARS.set(i);
        }
        for (int i = 'a'; i <= 'z'; i++) {
            PRINTABLE_CHARS.set(i);
        }
    }

    private static String encodeQP(final String string) throws ConverterException {
        try {
            return Charsets.toAsciiString(QuotedPrintableCodec.encodeQuotedPrintable(PRINTABLE_CHARS, string.getBytes(com.openexchange.java.Charsets.UTF_8))).replaceAll("=", "%");
        } catch (final UnsupportedCharsetException e) {
            // Cannot occur
            throw new ConverterException(e);
        }
    }

    private static String decodeQP(final String string) throws ConverterException {
        try {
            return new String(QuotedPrintableCodec.decodeQuotedPrintable(Charsets.toAsciiBytes(string.replaceAll("%", "="))), com.openexchange.java.Charsets.UTF_8);
        } catch (final DecoderException e) {
            throw new ConverterException(e);
        }
    }

}
