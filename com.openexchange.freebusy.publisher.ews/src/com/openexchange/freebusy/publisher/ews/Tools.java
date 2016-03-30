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

package com.openexchange.freebusy.publisher.ews;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import com.microsoft.schemas.exchange.services._2006.types.LegacyFreeBusyType;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.freebusy.BusyStatus;
import com.openexchange.freebusy.FreeBusyExceptionCodes;
import com.openexchange.freebusy.publisher.ews.internal.EWSFreeBusyPublisherLookup;

/**
 * {@link Tools}
 *
 * Utilities for the EWS free/busy provider
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class Tools {

    public static String getConfigProperty(String name, String defaultValue) throws OXException {
        return EWSFreeBusyPublisherLookup.getService(ConfigurationService.class).getProperty(name, defaultValue);
    }

    public static String getConfigProperty(String name) throws OXException {
        String value = EWSFreeBusyPublisherLookup.getService(ConfigurationService.class).getProperty(name);
        if (null == value || 0 == value.length()) {
            throw FreeBusyExceptionCodes.CONFIGURATION_ERROR.create(name);
        }
        return value;
    }

    public static int getConfigPropertyInt(String name) throws OXException {
        int value = EWSFreeBusyPublisherLookup.getService(ConfigurationService.class).getIntProperty(name, Integer.MIN_VALUE);
        if (Integer.MIN_VALUE == value) {
            throw FreeBusyExceptionCodes.CONFIGURATION_ERROR.create(name);
        }
        return value;
    }

    public static int getConfigPropertyInt(String name, int defaultValue) throws OXException {
        return EWSFreeBusyPublisherLookup.getService(ConfigurationService.class).getIntProperty(name, defaultValue);
    }

    public static boolean getConfigPropertyBool(String name, boolean defaultValue) throws OXException {
        return EWSFreeBusyPublisherLookup.getService(ConfigurationService.class).getBoolProperty(name, defaultValue);
    }

    public static BusyStatus getStatus(LegacyFreeBusyType type) {
        switch (type) {
        case FREE:
            return BusyStatus.FREE;
        case OOF:
            return BusyStatus.ABSENT;
        case TENTATIVE:
            return BusyStatus.TEMPORARY;
        case BUSY:
            return BusyStatus.RESERVED;
        default:
            return BusyStatus.UNKNOWN;
        }
    }

    public static Date getStartOfNextMonth(Date date) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getStartOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date getEndOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.MILLISECOND, -1);
        return calendar.getTime();
    }

}
