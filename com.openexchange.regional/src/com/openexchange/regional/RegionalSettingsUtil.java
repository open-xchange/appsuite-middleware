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

package com.openexchange.regional;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RegionalSettingsUtil}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class RegionalSettingsUtil {

    private static final Logger LOG = LoggerFactory.getLogger(RegionalSettingsUtil.class);

    /**
     * Gets a date format for the specified locale, considering customized regional settings if configured.
     *
     * @param regionalSettings The regional settings to consider, or <code>null</code> if not configured
     * @param style The formatting style. @see {@link DateFormat#getDateInstance(int, Locale)}
     * @param locale The locale to use
     * @return The {@link DateFormat}
     */
    public static DateFormat getDateFormat(RegionalSettings regionalSettings, int style, Locale locale) {
        String pattern = getDatePattern(regionalSettings, style);
        if (null != pattern) {
            try {
                return new SimpleDateFormat(pattern);
            } catch (Exception e) {
                LOG.error("Error applying date format {}, falling back to defaults for locale {}.", pattern, locale, e);
            }
        }
        return DateFormat.getDateInstance(style, locale);
    }

    /**
     * Gets a time format for the specified locale, considering customized regional settings if configured.
     *
     * @param regionalSettings The regional settings to consider, or <code>null</code> if not configured
     * @param style The formatting style. @see {@link DateFormat#getTimeInstance(int, Locale)}
     * @param locale The locale to use
     * @return The {@link DateFormat}
     */
    public static DateFormat getTimeFormat(RegionalSettings regionalSettings, int style, Locale locale) {
        String pattern = getTimePattern(regionalSettings, style);
        if (null != pattern) {
            try {
                return new SimpleDateFormat(pattern);
            } catch (Exception e) {
                LOG.error("Error applying time format \"{}\", falling back to defaults for locale {}.", pattern, locale, e);
            }
        }
        return DateFormat.getTimeInstance(style, locale);
    }

    /**
     * Gets a date/time format for the specified locale, considering customized regional settings if configured.
     *
     * @param regionalSettings The regional settings to consider, or <code>null</code> if not configured
     * @param dateStyle The formatting style. @see {@link DateFormat#getDateInstance(int, Locale)}
     * @param timeStyle The formatting style. @see {@link DateFormat#getTimeInstance(int, Locale)}
     * @param locale The locale to use
     * @return The {@link DateFormat}
     */
    public static DateFormat getDateTimeFormat(RegionalSettings regionalSettings, Locale locale, int dateStyle, int timeStyle) {
        if (null != regionalSettings) {
            String datePattern = getDatePattern(regionalSettings, dateStyle);
            String timePattern = getTimePattern(regionalSettings, timeStyle);
            if (null == datePattern) {
                datePattern = ((SimpleDateFormat) SimpleDateFormat.getDateInstance(dateStyle, locale)).toLocalizedPattern();
            }
            if (null == timePattern) {
                timePattern = ((SimpleDateFormat) SimpleDateFormat.getTimeInstance(timeStyle, locale)).toLocalizedPattern();
            }
            String pattern = datePattern + ' ' + timePattern;
            try {
                return new SimpleDateFormat(datePattern + ' ' + timePattern);
            } catch (Exception e) {
                LOG.error("Error applying date/time format {}, falling back to defaults for locale {}.", pattern, locale, e);
            }
        }
        return SimpleDateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
    }

    private static String getTimePattern(RegionalSettings settings, int style) {
        if (null != settings) {
            switch (style) {
                case DateFormat.LONG:
                    return settings.getTimeFormatLong();
                default:
                    return settings.getTimeFormat();
            }
        }
        return null;
    }

    private static String getDatePattern(RegionalSettings settings, int style) {
        if (null != settings) {
            switch (style) {
                case DateFormat.SHORT:
                    return settings.getDateFormatShort();
                case DateFormat.FULL:
                    return settings.getDateFormatFull();
                case DateFormat.LONG:
                    return settings.getDateFormatLong();
                case DateFormat.MEDIUM:
                    return settings.getDateFormatMedium();
                default:
                    return settings.getDateFormat();
            }
        }
        return null;
    }
}
