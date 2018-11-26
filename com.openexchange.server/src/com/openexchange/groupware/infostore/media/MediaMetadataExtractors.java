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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.groupware.infostore.media;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.common.collect.ImmutableList;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.MediaStatus;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link MediaMetadataExtractors} - Utility class for media metadata extractors.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class MediaMetadataExtractors {

    /**
     * Initializes a new {@link MediaMetadataExtractors}.
     */
    private MediaMetadataExtractors() {
        super();
    }

    private static List<SimpleDateFormat> DATE_FORMATS;
    private static List<SimpleDateFormat> TIME_FORMATS;

    static {
        // This seems to cover all known Exif and Xmp date strings
        // Note that "    :  :     :  :  " is a valid date string according to the Exif spec (which means 'unknown date'): http://www.awaresystems.be/imaging/tiff/tifftags/privateifd/exif/datetimeoriginal.html
        String datePatterns[] = {
            "yyyy:MM:dd HH:mm:ss",
            "yyyy:MM:dd HH:mm",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy.MM.dd HH:mm:ss",
            "yyyy.MM.dd HH:mm",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm",
            "yyyy-MM-dd",
            "yyyy-MM",
            "yyyyMMdd", // as used in IPTC data
            "yyyy" };

        ImmutableList.Builder<SimpleDateFormat> list = ImmutableList.builderWithExpectedSize(datePatterns.length);
        for (String datePattern : datePatterns) {
            list.add(new SimpleDateFormat(datePattern));
        }
        DATE_FORMATS = list.build();

        // ------------------------------------------------------------------------------------------------------------------------

        String timePatterns[] = {
            "HH:mm:ss",
            "HH:mm" };

        list = ImmutableList.builderWithExpectedSize(timePatterns.length);
        for (String timePattern : timePatterns) {
            list.add(new SimpleDateFormat(timePattern));
        }
        TIME_FORMATS = list.build();
    }

    private static final Pattern SUBSECOND_PATTERN = Pattern.compile("(\\d\\d:\\d\\d:\\d\\d)(\\.\\d+)");
    private static final Pattern TIME_ZONE_PATTERN = Pattern.compile("(Z|[+-]\\d\\d:\\d\\d)$");

    /**
     * Parses specified date/time string representation to a <code>java.util.Date</code> instance.
     *
     * @param dateString The date string
     * @param timeString The time string
     * @return The parsed <code>java.util.Date</code> instance
     */
    public static Date parseDateStringToDate(String dateString, String timeString) {
        if (Strings.isEmpty(dateString)) {
            return null;
        }

        String subsecond = null;
        TimeZone timeZone = null;
        Date date = null;

        String sDate = dateString;
        // If the date string has subsecond information, it supersedes the subsecond parameter
        {
            Matcher subsecondMatcher = SUBSECOND_PATTERN.matcher(sDate);
            if (subsecondMatcher.find()) {
                subsecond = subsecondMatcher.group(2).substring(1);
                sDate = subsecondMatcher.replaceAll("$1");
            }
        }

        // If the date string has time zone information, it supersedes the timeZone parameter
        {
            Matcher timeZoneMatcher = TIME_ZONE_PATTERN.matcher(sDate);
            if (timeZoneMatcher.find()) {
                timeZone = TimeZoneUtils.getTimeZone("GMT" + timeZoneMatcher.group().replaceAll("Z", ""));
                sDate = timeZoneMatcher.replaceAll("");
            }

            for (Iterator<SimpleDateFormat> it = DATE_FORMATS.iterator(); date == null && it.hasNext();) {
                DateFormat parser = it.next();
                synchronized (parser) {
                    try {
                        parser.setTimeZone(timeZone == null ? TimeZoneUtils.getTimeZone("GMT") : timeZone);
                        date = parser.parse(sDate);
                    } catch (@SuppressWarnings("unused") ParseException ex) {
                        // simply try the next pattern
                    }
                }
            }
        }

        if (date == null) {
            return null;
        }

        String sTime = timeString;
        if (Strings.isEmpty(sTime)) {
            return handleSubscecond(subsecond, date);
        }

        // also clean up the timeString, if present
        {
            Matcher subsecondMatcher = SUBSECOND_PATTERN.matcher(sTime);
            if (subsecondMatcher.find()) {
                sTime = subsecondMatcher.replaceAll("$1");
            }
        }

        Date time = null;
        // if the date string has time zone information, it supersedes the timeZone parameter
        {
            Matcher timeZoneMatcher = TIME_ZONE_PATTERN.matcher(sTime);
            if (timeZoneMatcher.find()) {
                timeZone = TimeZoneUtils.getTimeZone("GMT" + timeZoneMatcher.group().replaceAll("Z", ""));
                sTime = timeZoneMatcher.replaceAll("");
            }

            for (Iterator<SimpleDateFormat> it = TIME_FORMATS.iterator(); time == null && it.hasNext();) {
                DateFormat parser = it.next();
                synchronized (parser) {
                    try {
                        parser.setTimeZone(timeZone == null ? TimeZoneUtils.getTimeZone("GMT") : timeZone);
                        time = parser.parse(sTime);
                    } catch (@SuppressWarnings("unused") ParseException ex) {
                        // simply try the next pattern
                    }
                }
            }
        }

        if (time == null) {
            return handleSubscecond(subsecond, date);
        }

        try {
            Calendar calendarDate = Calendar.getInstance();
            calendarDate.setTime(date);

            Calendar calendarTime = Calendar.getInstance();
            calendarTime.setTime(time);

            calendarDate.set(Calendar.HOUR_OF_DAY, calendarTime.get(Calendar.HOUR_OF_DAY));
            calendarDate.set(Calendar.MINUTE, calendarTime.get(Calendar.MINUTE));
            calendarDate.set(Calendar.SECOND, calendarTime.get(Calendar.SECOND));
            calendarDate.set(Calendar.MILLISECOND, calendarTime.get(Calendar.MILLISECOND));

            date = calendarDate.getTime();
        } catch (@SuppressWarnings("unused") NumberFormatException e) {
            // do nothing and fall through to subsecond handler
        }
        return handleSubscecond(subsecond, date);
    }

    private static Date handleSubscecond(String subsecond, Date date) {
        if (subsecond == null) {
            return date;
        }

        try {
            int millisecond = (int) (Double.parseDouble("." + subsecond) * 1000);
            if (millisecond >= 0 && millisecond < 1000) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.set(Calendar.MILLISECOND, millisecond);
                return calendar.getTime();
            }
            return date;
        } catch (@SuppressWarnings("unused") NumberFormatException e) {
            return date;
        }
    }

    /**
     * Gets the <code>java.util.Date</code> value for given value.
     *
     * @param value The value
     * @return The <code>java.util.Date</code> value or <code>null</code>
     */
    public static Date getDateValue(Object value) {
        if (null == value) {
            return null;
        }
        if (value instanceof Date) {
            return (Date) value;
        }
        try {
            return parseDateStringToDate(value.toString(), null);
        } catch (@SuppressWarnings("unused") NumberFormatException e) {
            // NaN
            return null;
        }
    }

    /**
     * Gets the <code>long</code> value for given value.
     *
     * @param value The value
     * @return The <code>long</code> value or <code>null</code>
     */
    public static Long getLongValue(Object value) {
        if (null == value) {
            return null;
        }
        if (value instanceof Number) {
            return Long.valueOf(((Number) value).longValue());
        }
        try {
            return Long.valueOf(value.toString());
        } catch (@SuppressWarnings("unused") NumberFormatException e) {
            try {
                double d = Double.parseDouble(value.toString());
                return Long.valueOf((long) d);
            } catch (@SuppressWarnings("unused") NumberFormatException x) {
                // NaN
                return null;
            }
        }
    }

    /**
     * Saves the media meta-data from given document.
     *
     * @param document The document providing media meta-data
     * @param session The session providing user information
     * @return The ID tupe of saved document
     * @throws OXException If save operation fails
     */
    public static IDTuple saveMediaStatusForDocument(MediaStatus mediaStatus, DocumentMetadata document, Session session) throws OXException {
        DocumentMetadataImpl documentToPass = new DocumentMetadataImpl(document);
        documentToPass.setMediaStatus(mediaStatus);
        documentToPass.setCaptureDate(null);
        documentToPass.setGeoLocation(null);
        documentToPass.setWidth(-1);
        documentToPass.setHeight(-1);
        documentToPass.setIsoSpeed(-1);
        documentToPass.setCameraModel(null);
        documentToPass.setMediaMeta(null);
        documentToPass.setSequenceNumber(document.getSequenceNumber());
        return saveMediaMetaDataFromDocument(documentToPass, new InfostoreFacadeImpl(new DBPoolProvider()), session);
    }

    /**
     * Saves the media meta-data from given document.
     *
     * @param document The document providing media meta-data
     * @param session The session providing user information
     * @return The ID tupe of saved document
     * @throws OXException If save operation fails
     */
    public static IDTuple saveMediaMetaDataFromDocument(DocumentMetadata document, Session session) throws OXException {
        return saveMediaMetaDataFromDocument(document, new InfostoreFacadeImpl(new DBPoolProvider()), session);
    }

    /**
     * Saves the media meta-data from given document.
     *
     * @param document The document providing media meta-data
     * @param infostore The infostore instance to use
     * @param session The session providing user information
     * @return The ID tupe of saved document
     * @throws OXException If save operation fails
     */
    public static IDTuple saveMediaMetaDataFromDocument(DocumentMetadata document, InfostoreFacade infostore, Session session) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);

        // Examine
        Metadata[] mediaColumnsToModify;
        {
            List<Metadata> modifiedColumns = new ArrayList<>(8);
            modifiedColumns.add(Metadata.MEDIA_STATUS_LITERAL);
            if (document.getCaptureDate() != null) {
                modifiedColumns.add(Metadata.CAPTURE_DATE_LITERAL);
            }
            if (document.getGeoLocation() != null) {
                modifiedColumns.add(Metadata.GEOLOCATION_LITERAL);
            }
            if (document.getWidth() != null) {
                modifiedColumns.add(Metadata.WIDTH_LITERAL);
            }
            if (document.getHeight() != null) {
                modifiedColumns.add(Metadata.HEIGHT_LITERAL);
            }
            if (document.getIsoSpeed() != null) {
                modifiedColumns.add(Metadata.ISO_SPEED_LITERAL);
            }
            if (document.getCameraModel() != null) {
                modifiedColumns.add(Metadata.CAMERA_MODEL_LITERAL);
            }
            if (document.getMediaMeta() != null) {
                modifiedColumns.add(Metadata.MEDIA_META_LITERAL);
            }
            mediaColumnsToModify = modifiedColumns.toArray(new Metadata[modifiedColumns.size()]);
        }

        boolean rollback = false;
        try {
            infostore.startTransaction();
            rollback = true;

            IDTuple idTuple = infostore.saveDocument(document, null, document.getSequenceNumber(), mediaColumnsToModify, true, serverSession);

            infostore.commit();
            rollback = false;

            return idTuple;
        } finally {
            if (rollback) {
                infostore.rollback();
            }
            infostore.finish();
        }
    }
}
