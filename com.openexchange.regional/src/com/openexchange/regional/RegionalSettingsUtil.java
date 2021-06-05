/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.regional;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import org.slf4j.Logger;

/**
 * {@link RegionalSettingsUtil}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class RegionalSettingsUtil {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(RegionalSettingsUtil.class);
    }

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
                if (LoggerHolder.LOG.isDebugEnabled()) {
                    // Log exception, too
                    LoggerHolder.LOG.info("Could not apply date format \"{}\", falling back to defaults for locale {}.", pattern, locale, e);
                } else {
                    LoggerHolder.LOG.info("Could not apply date format \"{}\", falling back to defaults for locale {}.", pattern, locale);
                }
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
                if (LoggerHolder.LOG.isDebugEnabled()) {
                    // Log exception, too
                    LoggerHolder.LOG.info("Could not apply time format \"{}\", falling back to defaults for locale {}.", pattern, locale, e);
                } else {
                    LoggerHolder.LOG.info("Could not apply time format \"{}\", falling back to defaults for locale {}.", pattern, locale);
                }
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
                if (LoggerHolder.LOG.isDebugEnabled()) {
                    // Log exception, too
                    LoggerHolder.LOG.info("Could not apply date/time format \"{}\", falling back to defaults for locale {}.", pattern, locale, e);
                } else {
                    LoggerHolder.LOG.info("Could not apply date/time format \"{}\", falling back to defaults for locale {}.", pattern, locale);
                }
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
