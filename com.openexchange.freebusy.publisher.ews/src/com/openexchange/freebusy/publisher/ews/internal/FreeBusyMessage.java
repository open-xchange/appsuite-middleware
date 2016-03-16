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

package com.openexchange.freebusy.publisher.ews.internal;

import static com.microsoft.schemas.exchange.services._2006.types.MapiPropertyTypeType.BINARY_ARRAY;
import static com.microsoft.schemas.exchange.services._2006.types.MapiPropertyTypeType.BOOLEAN;
import static com.microsoft.schemas.exchange.services._2006.types.MapiPropertyTypeType.INTEGER;
import static com.microsoft.schemas.exchange.services._2006.types.MapiPropertyTypeType.INTEGER_ARRAY;
import static com.microsoft.schemas.exchange.services._2006.types.MapiPropertyTypeType.STRING;
import static com.microsoft.schemas.exchange.services._2006.types.MapiPropertyTypeType.SYSTEM_TIME;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import com.microsoft.schemas.exchange.services._2006.types.ExtendedPropertyType;
import com.microsoft.schemas.exchange.services._2006.types.MapiPropertyTypeType;
import com.microsoft.schemas.exchange.services._2006.types.NonEmptyArrayOfPropertyValuesType;
import com.microsoft.schemas.exchange.services._2006.types.PathToExtendedFieldType;
import com.microsoft.schemas.exchange.services._2006.types.PostItemType;
import com.openexchange.freebusy.FreeBusyData;


/**
 * {@link FreeBusyMessage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FreeBusyMessage {

    private static final PathToExtendedFieldType PidTagScheduleInfoMonthsTentative = pathTo("0x6851", INTEGER_ARRAY);
    private static final PathToExtendedFieldType PidTagScheduleInfoFreeBusyTentative = pathTo("0x6852", BINARY_ARRAY);
    private static final PathToExtendedFieldType PidTagScheduleInfoMonthsBusy = pathTo("0x6853", INTEGER_ARRAY);
    private static final PathToExtendedFieldType PidTagScheduleInfoFreeBusyBusy = pathTo("0x6854", BINARY_ARRAY);
    private static final PathToExtendedFieldType PidTagScheduleInfoMonthsAway = pathTo("0x6855", INTEGER_ARRAY);
    private static final PathToExtendedFieldType PidTagScheduleInfoFreeBusyAway = pathTo("0x6856", BINARY_ARRAY);
    private static final PathToExtendedFieldType PidTagScheduleInfoMonthsMerged = pathTo("0x684F", INTEGER_ARRAY);
    private static final PathToExtendedFieldType PidTagScheduleInfoFreeBusyMerged = pathTo("0x6850", BINARY_ARRAY);
    private static final PathToExtendedFieldType PidTagFreeBusyPublishStart = pathTo("0x6847", INTEGER);
    private static final PathToExtendedFieldType PidTagFreeBusyPublishEnd = pathTo("0x6848", INTEGER);
    private static final PathToExtendedFieldType PidTagFreeBusyRangeTimestamp = pathTo("0x6868", SYSTEM_TIME);
    private static final PathToExtendedFieldType PidTagFreeBusyMessageEmailAddress = pathTo("0x6849", STRING);
    private static final PathToExtendedFieldType PidTagNormalizedSubject = pathTo("0x0E1D", STRING);
    private static final PathToExtendedFieldType PidTagScheduleInfoResourceType = pathTo("0x6841", INTEGER);
    private static final PathToExtendedFieldType PidTagGatewayNeedsToRefresh = pathTo("0x6846", BOOLEAN);

    private final EncodedFreeBusyData encodedData;

    /**
     * Initializes a new {@link FreeBusyMessage}.
     *
     * @param freeBusyData The free/busy data to use
     */
    public FreeBusyMessage(EncodedFreeBusyData freeBusyData) {
        super();
        this.encodedData = freeBusyData;
    }

    /**
     * Initializes a new {@link FreeBusyMessage}.
     *
     * @param freeBusyData The free/busy data to use
     */
    public FreeBusyMessage(FreeBusyData freeBusyData) {
        this(new EncodedFreeBusyData(freeBusyData));
    }

    /**
     * Creates a {@link PostItemType} for this  free/busy message.
     *
     * @return The post item
     */
    public PostItemType createPostItem(String emailAddress, String subject) {
        PostItemType postItem = new PostItemType();
        postItem.setItemClass("IPM.Post");
        List<ExtendedPropertyType> properties = postItem.getExtendedProperty();
        properties.add(createExtendedProperty(PidTagScheduleInfoResourceType, "0"));
        properties.add(createExtendedProperty(PidTagGatewayNeedsToRefresh, "1"));
        properties.add(createExtendedProperty(PidTagNormalizedSubject, subject));
        properties.add(createExtendedProperty(PidTagFreeBusyMessageEmailAddress, emailAddress));
        addScheduleInfo(properties, PidTagScheduleInfoMonthsMerged, encodedData.getMergedMonths(),
                PidTagScheduleInfoFreeBusyMerged, encodedData.getMergedTimes());
        addScheduleInfo(properties, PidTagScheduleInfoMonthsBusy, encodedData.getBusyMonths(),
                PidTagScheduleInfoFreeBusyBusy, encodedData.getBusyTimes());
        addScheduleInfo(properties, PidTagScheduleInfoMonthsAway, encodedData.getAwayMonths(),
                PidTagScheduleInfoFreeBusyAway, encodedData.getAwayTimes());
        addScheduleInfo(properties, PidTagScheduleInfoMonthsTentative, encodedData.getTentativeMonths(),
                PidTagScheduleInfoFreeBusyTentative, encodedData.getTentativeTimes());
        properties.add(createExtendedProperty(PidTagFreeBusyPublishStart, Long.toString(encodedData.getPublishStart())));
        properties.add(createExtendedProperty(PidTagFreeBusyPublishEnd, Long.toString(encodedData.getPublishEnd())));
        properties.add(createExtendedProperty(PidTagFreeBusyRangeTimestamp, getSystemTimeString(new Date())));
        return postItem;
    }

    private static void addScheduleInfo(List<ExtendedPropertyType> properties, PathToExtendedFieldType fieldURIMonths, List<Integer> months,
        PathToExtendedFieldType fieldURIFreeBusy, List<String> freeBusy) {
        if (null != months) {
            properties.add(createExtendedProperty(fieldURIMonths, createArrayOfPropertyTypes(months)));
        }
        if (null != freeBusy) {
            properties.add(createExtendedProperty(fieldURIFreeBusy, createArrayOfPropertyTypes(freeBusy)));
        }
    }

    /**
     * Gets an Exchange representation of the supplied date to be used in
     * <code>MapiPropertyTypeType.SYSTEM_TIME</code> properties.
     *
     * @param date the date
     * @return the formatted system time string
     */
    private static String getSystemTimeString(Date date) {
        SimpleDateFormat exchangeDateFormat = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'");
        exchangeDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return exchangeDateFormat.format(date);
    }

    private static <T> NonEmptyArrayOfPropertyValuesType createArrayOfPropertyTypes(List<T> values) {
        NonEmptyArrayOfPropertyValuesType nonEmptyArrayOfPropertyValuesType = new NonEmptyArrayOfPropertyValuesType();
        for (T t : values) {
            nonEmptyArrayOfPropertyValuesType.getValue().add(t.toString());
        }
        return nonEmptyArrayOfPropertyValuesType;
    }

    private static ExtendedPropertyType createExtendedProperty(PathToExtendedFieldType fieldURI, String value) {
        ExtendedPropertyType extendedPropertyType = new ExtendedPropertyType();
        extendedPropertyType.setExtendedFieldURI(fieldURI);
        extendedPropertyType.setValue(value);
        return extendedPropertyType;
    }

    private static ExtendedPropertyType createExtendedProperty(PathToExtendedFieldType fieldURI, NonEmptyArrayOfPropertyValuesType values) {
        ExtendedPropertyType extendedPropertyType = new ExtendedPropertyType();
        extendedPropertyType.setExtendedFieldURI(fieldURI);
        extendedPropertyType.setValues(values);
        return extendedPropertyType;
    }

    /**
     * Constructs a {@link PathToExtendedFieldType} using a property's tag and type.
     *
     * @param propertyTag The property tag
     * @param propertyType The property type
     * @return the path to the extended field structure
     */
    private static PathToExtendedFieldType pathTo(String propertyTag, MapiPropertyTypeType propertyType) {
        PathToExtendedFieldType extendedField = new PathToExtendedFieldType();
        extendedField.setPropertyTag(propertyTag);
        extendedField.setPropertyType(propertyType);
        return extendedField;
    }

}
