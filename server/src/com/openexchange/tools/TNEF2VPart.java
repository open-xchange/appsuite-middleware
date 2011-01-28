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

package com.openexchange.tools;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Categories;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
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

/**
 * {@link TNEF2VPart}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TNEF2VPart {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(TNEF2VPart.class);

    /**
     * Initializes a new {@link TNEF2VPart}.
     */
    public TNEF2VPart() {
        super();
    }

    // http://api.kde.org/4.x-api/kdepimlibs-apidocs/ktnef/html/formatter_8cpp_source.html

    public String tnef2VPart(final net.freeutils.tnef.Message message) {
        try {
            final Attr messageClass = message.getAttribute(Attr.attMessageClass);
            final String messageClassName = messageClass == null ? "" : ((String) messageClass.getValue());
            final MAPIProps mapiProps = message.getMAPIProps();
            if (mapiProps == null) {
                return null;
            }

            boolean bCompatMethodRequest = false;
            boolean bCompatMethodCancled = false;
            boolean bCompatMethodAccepted = false;
            boolean bCompatMethodAcceptedCond = false;
            boolean bCompatMethodDeclined = false;
            /*
             * Check if TNEF c can be converted to a VCal part
             */
            if (!messageClassName.startsWith("IPM.MICROSOFT SCHEDULE.") && !"IPM.APPOINTMENT".equals(messageClassName)) {
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
                propertyList.add(new ProdId(
                    new StringBuilder("-//Microsoft Corporation//Outlook ").append(findProp(0x8554, "9.0", mapiProps)).append("MIMEDIR/EN").toString()));
                propertyList.add(net.fortuna.ical4j.model.property.Version.VERSION_2_0);
            }
            /*
             * Sender
             */
            String sSenderSearchKeyEmail = findProp(0x0C1D, mapiProps);
            if (sSenderSearchKeyEmail != null && sSenderSearchKeyEmail.length() > 0) {
                final int colon = sSenderSearchKeyEmail.indexOf(':');
                // May be e.g. "SMTP:KHZ@KDE.ORG"
                if (colon != -1) {
                    sSenderSearchKeyEmail = sSenderSearchKeyEmail.substring(colon + 1);
                }
            }
            boolean bIsReply = false;
            if (bCompatMethodAccepted || bCompatMethodAcceptedCond || bCompatMethodDeclined) {
                bIsReply = true;
            } else {
                if ("1".equals(findProp(0x0c17, mapiProps))) {
                    bIsReply = true;
                }
            }
            /*
             * VEvent
             */
            final VEvent event = new VEvent();
            final PropertyList eventPropertyList = calendar.getProperties();
            String s = findProp(0x0e04, mapiProps);
            final String[] attendees = s.split(" *; *");
            if (attendees.length > 0) {
                for (final String sAttendee : attendees) {
                    if (sAttendee.indexOf('@') == -1) {
                        s = sAttendee.trim();
                        final Attendee attendee = new Attendee(s);
                        if (bIsReply) {
                            if (bCompatMethodAccepted) {
                                attendee.getParameters().add(PartStat.ACCEPTED);
                            }
                            if (bCompatMethodDeclined) {
                                attendee.getParameters().add(PartStat.DECLINED);
                            }
                            if (bCompatMethodAcceptedCond) {
                                attendee.getParameters().add(PartStat.TENTATIVE);
                            }
                        } else {
                            attendee.getParameters().add(PartStat.NEEDS_ACTION);
                            attendee.getParameters().add(Role.REQ_PARTICIPANT);
                        }
                        eventPropertyList.add(attendee);
                    }
                }
            } else {
                s = sSenderSearchKeyEmail;
                if (!isEmpty(s)) {
                    final Attendee attendee = new Attendee("");
                    if (bIsReply) {
                        if (bCompatMethodAccepted) {
                            attendee.getParameters().add(PartStat.ACCEPTED);
                        }
                        if (bCompatMethodDeclined) {
                            attendee.getParameters().add(PartStat.DECLINED);
                        }
                        if (bCompatMethodAcceptedCond) {
                            attendee.getParameters().add(PartStat.TENTATIVE);
                        }
                    } else {
                        attendee.getParameters().add(PartStat.NEEDS_ACTION);
                        attendee.getParameters().add(Role.REQ_PARTICIPANT);
                    }
                    eventPropertyList.add(attendee);
                }
            }
            /*
             * Look for organizer property
             */
            s = findProp(0x0c1f, mapiProps);
            if (isEmpty(s) && !bIsReply) {
                s = sSenderSearchKeyEmail;
            }
            // TODO: Use the common name?
            if (!isEmpty(s)) {
                eventPropertyList.add(new Organizer(s));
            }
            /*
             * Start date
             */
            s = findProp(0x8516, mapiProps);
            s = remove(s, '-', ':');
            eventPropertyList.add(new DtStart(s));
            /*
             * End date
             */
            s = findProp(0x8517, mapiProps);
            s = remove(s, '-', ':');
            eventPropertyList.add(new DtEnd(s));
            /*
             * Location
             */
            s = findProp(0x8208, mapiProps);
            eventPropertyList.add(new Location(s));
            /*-
             * UID
             * 
             * Is "0x0023" OK  -  or should we look for "0x0003" ??
             */
             s = findProp( 0x0023, mapiProps );
             eventPropertyList.add(new Uid(s));
             /*-
              * PENDING(khz): is this value in local timezone? Must it be
              * adjusted? Most likely this is a bug in the server or in
              * Outlook - we ignore it for now.
              */
             s = findProp(0x8202, mapiProps);
             s = remove(s, '-', ':');
             // propertyList.add(new DtStamp(new DateTime(s));
             /*
              * Categories
              */
             s = findProp(MAPIProp.PR_KEYWORD, mapiProps);
             eventPropertyList.add(new Categories(s));
             /*
              * Description
              */
             s = findProp(0x1000, mapiProps);
             eventPropertyList.add(new Description(s));
             /*
              * Summary
              */
             s = findProp(0x0070, mapiProps);
             eventPropertyList.add(new Summary(s));
             /*
              * Priority
              */
             s = findProp(0x0026, mapiProps);
             eventPropertyList.add(new Priority(Integer.parseInt(s.trim())));
             /*
              * Is reminder flag set?
              */
             if (!isEmpty(findProp(0x8503, mapiProps))) {
                 final VAlarm vAlarm = new VAlarm();
                 /*
                  * Always DSIPLAY
                  */
                 vAlarm.getProperties().add(Action.DISPLAY);
                 s = findProp(0x8502, mapiProps);
                 s = remove(s, '-', ':');
                 final Date highNoonTime = pureISOToLocalQDateTime(s);
                 /*
                  * Trigger
                  */
                 s = findProp(0x8560, "", mapiProps);
                 s = remove(s, '-', ':');
                 vAlarm.getProperties().add(new Trigger(new DateTime(pureISOToLocalQDateTime(s))));
                 
                 eventPropertyList.add(vAlarm);
             }
             /*
              * Add to VCalendar
              */
             calendar.getComponents().add(event);
             return calendar.toString();
        } catch (final NumberFormatException e) {
            LOG.error(e.getMessage(), e);
            return null;
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
            return null;
        } catch (final URISyntaxException e) {
            LOG.error(e.getMessage(), e);
            return null;
        } catch (final ParseException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    private static Date pureISOToLocalQDateTime(final String dtStr) {
        return pureISOToLocalQDateTime(dtStr, false);
    }

    private static Date pureISOToLocalQDateTime(final String dtStr, final boolean bDateOnly) {
        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.ENGLISH);
        int year, month, day, hour, minute, second;
        if (bDateOnly) {
            year = Integer.parseInt(left(dtStr, 4));
            month = Integer.parseInt(mid(dtStr, 4, 2));
            day = Integer.parseInt(mid(dtStr, 6, 2));
            hour = 0;
            minute = 0;
            second = 0;
        } else {
            year = Integer.parseInt(left(dtStr, 4));
            month = Integer.parseInt(mid(dtStr, 4, 2));
            day = Integer.parseInt(mid(dtStr, 6, 2));
            hour = Integer.parseInt(mid(dtStr, 9, 2));
            minute = Integer.parseInt(mid(dtStr, 11, 2));
            second = Integer.parseInt(mid(dtStr, 13, 2));
        }
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);

        if (!bDateOnly) {
            /*
             * Correct for GMT ( == Zulu time == UTC )
             */
            if (dtStr.charAt(dtStr.length() - 1) == 'Z') {
                // TODO:
            }
        }

        return cal.getTime();
    }

    private static String mid(final String s, final int pos, final int len) {
        if (null == s) {
            return null;
        }
        if (len < 0) {
            return s.substring(pos);
        }
        final char[] ca = s.toCharArray();
        final int length = Math.min(ca.length, pos + len);
        final StringBuilder sb = new StringBuilder(len);
        for (int i = pos; i < length; i++) {
            sb.append(ca[i]);
        }
        return sb.toString();
    }

    private static String left(final String s, final int n) {
        if (null == s) {
            return null;
        }
        final char[] ca = s.toCharArray();
        final int length = ca.length;
        final StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n && i < length; i++) {
            sb.append(ca[i]);
        }
        return sb.toString();
    }

    private static String remove(final String s, final char... chars) {
        if (null == s) {
            return null;
        }
        final char[] ca = s.toCharArray();
        final int length = ca.length;
        final StringBuilder sb = new StringBuilder(length);
        Arrays.sort(chars);
        for (int i = 0; i < length; i++) {
            final char cur = ca[i];
            if (Arrays.binarySearch(chars, cur) < 0) {
                sb.append(cur);
            }
        }
        return sb.toString();
    }

    private static boolean isEmpty(final String s) {
        if (null == s) {
            return true;
        }
        boolean whiteSpace = true;
        final char[] chars = s.toCharArray();
        final int length = chars.length;
        for (int i = 0; whiteSpace && i < length; i++) {
            whiteSpace &= Character.isWhitespace(chars[i]);
        }
        return whiteSpace;
    }

    private static <V> V findProp(final int id, final V fallback, final MAPIProps mapiProps) throws IOException {
        @SuppressWarnings("unchecked") final V retval = (V) mapiProps.getPropValue(id);
        return null == retval ? fallback : retval;
    }

    private static String findProp(final int id, final MAPIProps mapiProps) throws IOException {
        return findProp(id, null, mapiProps);
    }

    private static String findProp(final int id, final String fallback, final MAPIProps mapiProps) throws IOException {
        @SuppressWarnings("unchecked") final Object retval = mapiProps.getPropValue(id);
        return null == retval ? fallback : retval.toString();
    }

}
