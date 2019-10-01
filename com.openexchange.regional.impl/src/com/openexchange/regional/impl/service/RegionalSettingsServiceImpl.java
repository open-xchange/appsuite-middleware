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

package com.openexchange.regional.impl.service;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.c;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.settings.SettingExceptionCodes;
import com.openexchange.regional.RegionalSettingField;
import com.openexchange.regional.RegionalSettings;
import com.openexchange.regional.RegionalSettingsService;
import com.openexchange.regional.RegionalSettingsUtil;
import com.openexchange.regional.impl.storage.RegionalSettingStorage;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link RegionalSettingsServiceImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
public class RegionalSettingsServiceImpl implements RegionalSettingsService {

    private static final Logger LOG = LoggerFactory.getLogger(RegionalSettingsServiceImpl.class);
    private static final Locale FALLBACK_REGION = new Locale("en", "US");

    private final LoadingCache<Locale, RegionalSettings> settingsCache;
    private final RegionalSettingStorage storage;

    /**
     * Initializes a new {@link RegionalSettingsServiceImpl}.
     */
    public RegionalSettingsServiceImpl(RegionalSettingStorage storage) {
        super();
        this.storage = storage;
        this.settingsCache = CacheBuilder.newBuilder().initialCapacity(25).maximumSize(250).expireAfterAccess(365, TimeUnit.DAYS).build(new RegionalSettingsCacheLoader());
    }

    @Override
    public RegionalSettings get(int contextId, int userId) {
        try {
            return storage.get(contextId, userId);
        } catch (OXException e) {
            LOG.info("Unable to load regional settings for user {} in context {}. Assuming no custom settings.", I(userId), I(contextId), e);
        }
        return null;
    }

    @Override
    public void save(int contextId, int userId, RegionalSettings settings, Locale locale) throws OXException {
        RegionalSettings purged = purgeDefaults(settings, locale);
        if (purged == null) {
            // The setting doesn't contain any custom fields. Therefore the entry can be deleted
            storage.delete(contextId, userId);
        } else {
            check(purged);
            storage.upsert(contextId, userId, purged);
        }
    }

    @Override
    public void delete(int contextId, int id) throws OXException {
        storage.delete(contextId, id);
    }

    @Override
    public DateFormat getDateFormat(int contextId, int userId, Locale locale, int style) {
        return RegionalSettingsUtil.getDateFormat(get(contextId, userId), style, locale);
    }

    @Override
    public DateFormat getTimeFormat(int contextId, int userId, Locale locale, int style) {
        return RegionalSettingsUtil.getTimeFormat(get(contextId, userId), style, locale);
    }

    @Override
    public DateFormat getDateTimeFormat(int contextId, int userId, Locale locale, int dateStyle, int timeStyle) {
        return RegionalSettingsUtil.getDateTimeFormat(get(contextId, userId), locale, dateStyle, timeStyle);
    }

    @Override
    public NumberFormat getNumberFormat(int contextId, int userId, Locale locale, String format) {
        RegionalSettings settings = get(contextId, userId);
        Character decimalSeparator = getDecimalSeparator(settings);
        Character groupingSeparator = getGroupingSeparator(settings);
        DecimalFormatSymbols unusualSymbols = new DecimalFormatSymbols(null != locale ? locale : FALLBACK_REGION);
        if (null != decimalSeparator && Character.MIN_VALUE != decimalSeparator.charValue()) {
            unusualSymbols.setDecimalSeparator(c(decimalSeparator));
        }
        if (null != groupingSeparator && Character.MIN_VALUE != groupingSeparator.charValue()) {
            unusualSymbols.setGroupingSeparator(c(groupingSeparator));
        }
        NumberFormat numberFormat = new DecimalFormat(format, unusualSymbols);
        if (null != groupingSeparator && Character.MIN_VALUE == c(groupingSeparator)) {
            numberFormat.setGroupingUsed(false);
        }
        return numberFormat;
    }

    /**
     * Purges any default values from the specified RegionalSettings object.
     * 
     * @param settings The {@link RegionalSettings} to purge the defaults from
     * @param locale The {@link Locale} to retrieve the defaults from
     * @return A new {@link RegionalSettings} object with all defaults purged or <code>null</code>
     *         if only defaults where stored on the specified RegionalSettings object
     */
    private RegionalSettings purgeDefaults(RegionalSettings settings, Locale locale) throws OXException {
        RegionalSettingsImpl.Builder builder = RegionalSettingsImpl.newBuilder();
        RegionalSettings defaults = getDefaults(locale);
        boolean isDefault = true;
        for (RegionalSettingField field : RegionalSettingField.values()) {
            Object fieldValue = SerialiserUtil.getField(settings, field);
            if (fieldValue == null) {
                continue;
            }
            Object defaultFieldValue = SerialiserUtil.getField(defaults, field);
            if (fieldValue.equals(defaultFieldValue)) {
                continue;
            }
            SerialiserUtil.setField(builder, field, fieldValue);
            isDefault = false;
        }
        return isDefault ? null : builder.build();
    }

    /**
     * Gets the default settings for the given region
     *
     * @param region The region
     * @return The default settings
     * @throws OXException
     */
    private RegionalSettings getDefaults(Locale locale) throws OXException {
        try {
            return settingsCache.get(locale == null ? FALLBACK_REGION : locale);
        } catch (ExecutionException e) {
            throw ThreadPools.launderThrowable(e, OXException.class);
        }
    }

    /**
     * Checks if values in {@link RegionalSettings} object are valid
     *
     * @param settings The settings
     */
    private void check(RegionalSettings settings) throws OXException {
        for (RegionalSettingField field : RegionalSettingField.values()) {
            Object fieldValue = SerialiserUtil.getField(settings, field);
            if (fieldValue == null) {
                continue;
            }
            if (isValidPattern(fieldValue, field)) {
                continue;
            }
            throw SettingExceptionCodes.INVALID_VALUE.create(fieldValue, field.getName());
        }
    }

    /**
     * Checks if value if valid for given field
     *
     * @param value The value
     * @param field The field
     * @return <code>true</code> if value is valid for this field, <code>false</code> otherwise
     */
    private boolean isValidPattern(Object value, RegionalSettingField field) {
        switch (field) {
            case TIME:
            case TIME_LONG:
            case DATE:
            case DATE_FULL:
            case DATE_LONG:
            case DATE_MEDIUM:
            case DATE_SHORT:
                return checkDateTimePattern((String)value, field);
            case NUMBER:
                return true;
            case FIRST_DAY_OF_WEEK:
                if ("sunday".equals(value) || "monday".equals(value)) {
                    return true;
                }
                return false;
            case FIRST_DAY_OF_YEAR:
                try {
                    int v = Integer.parseInt((String)value);
                    return 0 <= v && 7 > v;
                } catch (NumberFormatException e) {
                    return false;
                }
            default:
                // Will not happen...
                return false;
        }
    }

    @SuppressWarnings("unused")
    /**
     * Validates a date field value
     *
     * @param pattern The pattern to validate
     * @param field The field
     * @return <code>true</code> if value is valid for this field, <code>false</code> otherwise
     */
    private boolean checkDateTimePattern(String pattern, RegionalSettingField field) {
        try {
            new SimpleDateFormat(pattern);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    private static Character getDecimalSeparator(RegionalSettings settings) {
        if (null != settings && null != settings.getNumberFormat()) {
            String numberFormat = settings.getNumberFormat();
            try {
                char c = numberFormat.charAt(numberFormat.indexOf('5') - 1);
                return Character.valueOf('4' == c ? Character.MIN_VALUE : c);
            } catch (Exception e) {
                LOG.error("Error parsing decimal separator char from {}, falling back to defaults.", numberFormat, e);
            }
        }
        return null;
    }

    private static Character getGroupingSeparator(RegionalSettings settings) {
        if (null != settings && null != settings.getNumberFormat()) {
            String numberFormat = settings.getNumberFormat();
            try {
                char c = numberFormat.charAt(numberFormat.indexOf('2') - 1);
                return Character.valueOf('1' == c ? Character.MIN_VALUE : c);
            } catch (Exception e) {
                LOG.error("Error parsing group separator char from {}, falling back to defaults.", numberFormat, e);
            }
        }
        return null;
    }
//
//    private void validate(RegionalSettings settings) throws OXException {
//        RegionalSettingField.values()
//        Map<String, String> localeData = new HashMap<>();
//        localeData.put("date", settings.getDateFormat());
//        localeData.put(key, value)
//        try {
//            DateFormat df = new SimpleDateFormat(settings.getDateFormat());
//        } catch (IllegalArgumentException e) {
//            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE
//        }
//    }

}
