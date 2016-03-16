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

package com.openexchange.tools.tnef;

import static com.openexchange.tools.tnef.TNEF2ICalUtility.findNamedProp;
import static com.openexchange.tools.tnef.TNEF2ICalUtility.findNamedPropString;
import static com.openexchange.tools.tnef.TNEF2ICalUtility.findProp;
import static com.openexchange.tools.tnef.TNEF2ICalUtility.findPropString;
import static com.openexchange.tools.tnef.TNEF2ICalUtility.getEmailAddress;
import static com.openexchange.tools.tnef.TNEF2ICalUtility.isEmpty;
import static com.openexchange.tools.tnef.TNEF2ICalUtility.toDateTime;
import static com.openexchange.tools.tnef.TNEF2ICalUtility.toHexString;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.CuType;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.parameter.Rsvp;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Categories;
import net.fortuna.ical4j.model.property.Clazz;
import net.fortuna.ical4j.model.property.Created;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.Priority;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Trigger;
import net.fortuna.ical4j.model.property.Uid;
import net.freeutils.tnef.Attr;
import net.freeutils.tnef.MAPIProp;
import net.freeutils.tnef.MAPIProps;
import net.freeutils.tnef.RawInputStream;
import com.openexchange.groupware.i18n.Notifications;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.i18n.tools.StringHelper;

/**
 * {@link TNEF2ICal}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TNEF2ICal {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TNEF2ICal.class);

    private static final Pattern SPLIT_SEMICOLON = Pattern.compile(" *; *");

    /**
     * Initializes a new {@link TNEF2ICal}.
     */
    private TNEF2ICal() {
        super();
    }

    /**
     * Checks if specified TNEF's message class name indicates a convertible VPart.
     * <p>
     * The message class name needs to be one of:
     * <ul>
     * <li>IPM.Microsoft Schedule.MtgCncl (CANCELED)</li>
     * <li>IPM.Microsoft Schedule.MtgReq (REQUEST)</li>
     * <li>IPM.Microsoft Schedule.MtgRespP (ACCEPTED)</li>
     * <li>IPM.Microsoft Schedule.MtgRespN (DECLINED)</li>
     * <li>IPM.Microsoft Schedule.MtgRespA (TENTATIVE)</li>
     * </ul>
     *
     * @param message The TNEF message
     * @return <code>true</code> if a convertible VPart; otherwise <code>false</code>
     */
    public static boolean isVPart(final net.freeutils.tnef.Message message) {
        try {
            final Attr messageClass = message.getAttribute(Attr.attMessageClass);
            return isVPart(messageClass == null ? "" : ((String) messageClass.getValue()).toUpperCase(Locale.ENGLISH));
        } catch (final IOException e) {
            LOG.error("", e);
            return false;
        }
    }

    /**
     * Checks if specified TNEF's message class name indicates a convertible VPart.
     * <p>
     * The message class name needs to be one of:
     * <ul>
     * <li>IPM.Microsoft Schedule.MtgCncl (CANCELED)</li>
     * <li>IPM.Microsoft Schedule.MtgReq (REQUEST)</li>
     * <li>IPM.Microsoft Schedule.MtgRespP (ACCEPTED)</li>
     * <li>IPM.Microsoft Schedule.MtgRespN (DECLINED)</li>
     * <li>IPM.Microsoft Schedule.MtgRespA (TENTATIVE)</li>
     * </ul>
     *
     * @param messageClassName The message class name of TNEF message
     * @return <code>true</code> if a convertible VPart; otherwise <code>false</code>
     */
    public static boolean isVPart(final String messageClassName) {
        if (null == messageClassName) {
            return false;
        }
        final String mcn = messageClassName.toUpperCase(Locale.ENGLISH);
        return (mcn.startsWith("IPM.MICROSOFT SCHEDULE.") || mcn.startsWith("IPM.SCHEDULE.") || "IPM.APPOINTMENT".equals(mcn));
    }

    // http://api.kde.org/4.x-api/kdepimlibs-apidocs/ktnef/html/formatter_8cpp_source.html
    // ttp://www.pokorra.de/kolab/tnef/
    // http://api.kde.org/3.5-api/kdepim-apidocs/libkcal/html/incidenceformatter_8cpp_source.html

    /**
     * Converts specified TNEF message to a VCalendar if message class name is one of:
     * <ul>
     * <li>IPM.Microsoft Schedule.MtgCncl (CANCELED)</li>
     * <li>IPM.Microsoft Schedule.MtgReq (REQUEST)</li>
     * <li>IPM.Microsoft Schedule.MtgRespP (ACCEPTED)</li>
     * <li>IPM.Microsoft Schedule.MtgRespN (DECLINED)</li>
     * <li>IPM.Microsoft Schedule.MtgRespA (TENTATIVE)</li>
     * </ul>
     *
     * @param message The TNEF message to convert
     * @return The resulting VCalendar or <code>null</code> if not cannot be converted
     */
    public static net.fortuna.ical4j.model.Calendar tnef2VPart(final net.freeutils.tnef.Message message) {
        try {
            final Attr messageClass = message.getAttribute(Attr.attMessageClass);
            final String messageClassName;
            if (messageClass == null) {
                final MAPIProp prop = message.getMAPIProps().getProp(MAPIProp.PR_MESSAGE_CLASS);
                messageClassName = null == prop ? "" : prop.getValue().toString().toUpperCase(Locale.ENGLISH);
            } else {
                messageClassName = ((String) messageClass.getValue()).toUpperCase(Locale.ENGLISH);
            }
            final MAPIProps mapiProps = message.getMAPIProps();
            if (mapiProps == null) {
                return null;
            }


//            final FileWriter fw = new FileWriter("/home/thorben/Desktop/mapi.txt");
//            try {
//                final MAPIProp[] allProps = mapiProps.getProps();
//                for (final MAPIProp mapiProp : allProps) {
//                    fw.write(mapiProp.toString() + "\r\n");
//                }
//                fw.flush();
//            } finally {
//                fw.close();
//            }


            boolean bCompatMethodRequest = false;
            boolean bCompatMethodCancled = false;
            boolean bCompatMethodAccepted = false;
            boolean bCompatMethodAcceptedCond = false;
            boolean bCompatMethodDeclined = false;
            /*
             * Check if TNEF c can be converted to a VCal part
             */
            if (!isVPart(messageClassName)) {
                return null;
            }
            /*
             * Create VEvent instance
             */
            final net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
            /*
             * Check message class type
             */
            if (messageClassName.endsWith(".MTGREQ")) {
                bCompatMethodRequest = true;
            }
            if (messageClassName.endsWith(".MTGCNCL")) {
                bCompatMethodCancled = true;
            }
            if (messageClassName.endsWith(".MTGRESPP")) {
                bCompatMethodAccepted = true;
            }
            if (messageClassName.endsWith(".MTGRESPA")) {
                bCompatMethodAcceptedCond = true;
            }
            if (messageClassName.endsWith(".MTGRESPN")) {
                bCompatMethodDeclined = true;
            }
            /*
             * ProdId & Version
             */
            {
                final PropertyList propertyList = calendar.getProperties();
                propertyList.add(new ProdId(new StringBuilder("-//Microsoft Corporation//Outlook ").append(
                    findNamedProp("0x8554", "9.0", mapiProps)).append(" MIMEDIR//EN").toString()));
                propertyList.add(net.fortuna.ical4j.model.property.Version.VERSION_2_0);
            }
            /*
             * Sender
             */
            final String sSenderName = findProp(MAPIProp.PR_SENDER_NAME, mapiProps);
            final String sSenderSearchKeyEmail = getEmailAddress(findPropString(MAPIProp.PR_SENDER_EMAIL_ADDRESS, mapiProps));
            boolean bIsReply = false;
            if (bCompatMethodAccepted || bCompatMethodAcceptedCond || bCompatMethodDeclined) {
                bIsReply = true;
                calendar.getProperties().add(net.fortuna.ical4j.model.property.Method.REPLY);
            } else {
                if ("1".equals(findProp(MAPIProp.PR_REPLY_REQUESTED, mapiProps))) {
                    bIsReply = true;
                    calendar.getProperties().add(net.fortuna.ical4j.model.property.Method.REPLY);
                } else {
                    calendar.getProperties().add(net.fortuna.ical4j.model.property.Method.REQUEST);
                }
            }
            /*
             * VEvent
             */
            final VEvent event = new VEvent();
            final PropertyList eventPropertyList = event.getProperties();
            eventPropertyList.add(Clazz.PUBLIC);
            /*
             * Look for organizer property
             */
            String s = getEmailAddress(findPropString(MAPIProp.PR_SENDER_EMAIL_ADDRESS, mapiProps));
            if (isEmpty(s) && !bIsReply) {
                s = sSenderSearchKeyEmail;
            }
            // TODO: Use the common name?
            final String organizer;
            if (!isEmpty(s)) {
                organizer = s;
                final Organizer property = new Organizer(organizer);
                if (null != sSenderName) {
                    property.getParameters().add(new Cn(sSenderName));
                }
                eventPropertyList.add(property);
            } else {
                organizer = null;
            }
            /*
             * Attendees
             */
            s = findProp(MAPIProp.PR_DISPLAY_TO, mapiProps);
            final String[] attendees = null == s ? new String[0] : SPLIT_SEMICOLON.split(s, 0);
            if (attendees.length > 0) {
                for (final String sAttendee : attendees) {
                    final String addr = getEmailAddress(sAttendee);
                    if (null != addr) {
                        s = addr;
                        final Attendee attendee = generateAttendee(
                            bCompatMethodAccepted,
                            bCompatMethodAcceptedCond,
                            bCompatMethodDeclined,
                            bIsReply,
                            s);
                        eventPropertyList.add(attendee);
                    }
                }
            } else {
                s = sSenderSearchKeyEmail;
                if (!isEmpty(s)) {
                    final Attendee attendee = generateAttendee(
                        bCompatMethodAccepted,
                        bCompatMethodAcceptedCond,
                        bCompatMethodDeclined,
                        bIsReply,
                        s);
                    eventPropertyList.add(attendee);
                }
            }
            /*
             * Time zone ID
             */
            String tzid;
            {
                String tmp = findNamedProp("0x8234", mapiProps);
                if (null == tmp) {
                    tzid = "GMT";
                } else {
                    try {
                        final int p1 = tmp.indexOf('(') + 1;
                        final int p2 = tmp.indexOf(')', p1);
                        if (p1 >= 0 && p2 > 0) {
                            tmp = tmp.substring(p1, p2);
                            tzid = java.util.TimeZone.getTimeZone(tmp).getID();
                        } else {
                            LOG.warn("Cannot parse time zone information from: \"{}\"", tmp);
                            tzid = "GMT";
                        }
                    } catch (Exception e) {
                        LOG.warn("Cannot parse time zone information from: \"{}\"", tmp, e);
                        tzid = "GMT";
                    }
                }
            }
            /*
             * Creation date
             */
            Date d = findProp(MAPIProp.PR_CREATION_TIME, mapiProps);
            if (d != null) {
                eventPropertyList.add(new Created(toDateTime(d, tzid)));
            }
            /*
             * Start date
             */
            d = findProp(MAPIProp.PR_START_DATE, mapiProps);
            if (d != null) {
                eventPropertyList.add(new DtStart(toDateTime(d, tzid)));
            }
            /*
             * End date
             */
            d = findProp(MAPIProp.PR_END_DATE, mapiProps);
            if (d != null) {
                eventPropertyList.add(new DtEnd(toDateTime(d, tzid)));
            }
            /*
             * Location
             */
            s = findNamedProp("0x8208", mapiProps);
            if (!isEmpty(s)) {
                eventPropertyList.add(new Location(s));
            }
            /*-
             * UID
             *
             * Is "0x23" OK  -  or should we look for "0x3" ??
             */
            {
                RawInputStream ris = findNamedProp("0x23", mapiProps);
                if (ris == null) {
                    ris = findNamedProp("0x3", mapiProps);
                }
                if (ris != null) {
                    s = toHexString(ris.toByteArray());
                    eventPropertyList.add(new Uid(s));
                }
            }
            /*-
             * Is this value in local time zone? Must it be
             * adjusted? Most likely this is a bug in the server or in
             * Outlook - we ignore it for now.
             */
            d = findNamedProp("0x8202", mapiProps);
            if (d != null) {
                eventPropertyList.add(new DtStamp(toDateTime(d, tzid)));
            }
            /*
             * Categories
             */
            s = findPropString(MAPIProp.PR_KEYWORD, mapiProps);
            if (!isEmpty(s)) {
                eventPropertyList.add(new Categories(s));
            }
            /*
             * Description
             */
            s = findPropString(MAPIProp.PR_BODY, mapiProps);
            if (!isEmpty(s)) {
                eventPropertyList.add(new Description(s));
            }
            /*
             * Summary
             */
            s = findPropString(MAPIProp.PR_CONVERSATION_TOPIC, mapiProps);
            if (!isEmpty(s)) {
                eventPropertyList.add(new Summary(s));
            }
            /*
             * Priority
             */
            s = findPropString(MAPIProp.PR_PRIORITY, mapiProps);
            if (!isEmpty(s)) {
                eventPropertyList.add(new Priority(Integer.parseInt(s.trim())));
            }
            /*
             * Locale
             */
            final Locale locale;
            s = findNamedPropString("acceptlanguage", mapiProps);
            if (s == null) {
                locale = Locale.ENGLISH;
            } else {
                final Locale tmp = LocaleTools.getLocale(s.replaceAll("-", "_"));
                locale = tmp == null ? Locale.ENGLISH : tmp;
            }
            /*
             * Is reminder flag set?
             */
            if (!isEmpty(findNamedPropString("0x8503", mapiProps))) {
                final VAlarm vAlarm = new VAlarm();
                /*
                 * Always DSIPLAY
                 */
                vAlarm.getProperties().add(Action.DISPLAY);
                d = findNamedProp("0x8502", mapiProps);
                final Date highNoonTime = d;
                /*
                 * Trigger
                 */
                d = findNamedProp("0x8560", mapiProps);
                if (null != d) {
                    vAlarm.getProperties().add(new Trigger(new DateTime(d)));
                }
                /*
                 * Reminder
                 */
                vAlarm.getProperties().add(new Description(StringHelper.valueOf(locale).getString(Notifications.REMINDER)));
                event.getAlarms().add(vAlarm);
            }
            /*
             * Add to VCalendar
             */
            calendar.getComponents().add(event);
            return calendar;
        } catch (final NumberFormatException e) {
            LOG.error("", e);
            return null;
        } catch (final IOException e) {
            LOG.error("", e);
            return null;
        } catch (final URISyntaxException e) {
            LOG.error("", e);
            return null;
        }
    }

    private static Attendee generateAttendee(final boolean bCompatMethodAccepted, final boolean bCompatMethodAcceptedCond, final boolean bCompatMethodDeclined, final boolean bIsReply, final String s) throws URISyntaxException {
        final Attendee attendee = new Attendee(s);
        if (bIsReply) {
            if (bCompatMethodAccepted) {
                attendee.getParameters().add(PartStat.ACCEPTED);
                attendee.getParameters().add(Role.REQ_PARTICIPANT);
            }
            if (bCompatMethodDeclined) {
                attendee.getParameters().add(PartStat.DECLINED);
                attendee.getParameters().add(Role.REQ_PARTICIPANT);
            }
            if (bCompatMethodAcceptedCond) {
                attendee.getParameters().add(PartStat.TENTATIVE);
                attendee.getParameters().add(Role.REQ_PARTICIPANT);
            }
        } else {
            attendee.getParameters().add(PartStat.NEEDS_ACTION);
            attendee.getParameters().add(Role.REQ_PARTICIPANT);
            attendee.getParameters().add(Rsvp.TRUE);
        }
        attendee.getParameters().add(CuType.INDIVIDUAL);
        return attendee;
    }

}
