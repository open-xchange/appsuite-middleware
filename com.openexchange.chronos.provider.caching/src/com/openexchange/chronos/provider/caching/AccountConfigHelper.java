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

package com.openexchange.chronos.provider.caching;

import static com.openexchange.chronos.provider.CalendarFolderProperty.COLOR;
import static com.openexchange.chronos.provider.CalendarFolderProperty.DESCRIPTION;
import static com.openexchange.chronos.provider.CalendarFolderProperty.LAST_UPDATE;
import static com.openexchange.chronos.provider.CalendarFolderProperty.SCHEDULE_TRANSP;
import static com.openexchange.chronos.provider.CalendarFolderProperty.USED_FOR_SYNC;
import static com.openexchange.java.Autoboxing.B;
import org.json.JSONObject;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.common.DataHandlers;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.UsedForSync;
import com.openexchange.chronos.provider.basic.BasicCalendarAccess;
import com.openexchange.chronos.provider.basic.CalendarSettings;
import com.openexchange.chronos.provider.basic.CommonCalendarConfigurationFields;
import com.openexchange.chronos.provider.caching.internal.CachingCalendarAccessConstants;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link AccountConfigHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class AccountConfigHelper {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AccountConfigHelper.class);

    private static final String DEFAULT_CALENDAR_NAME = "Calendar";

    private final CalendarAccount account;
    private final Session session;

    /**
     * Initializes a new {@link AccountConfigHelper}.
     * 
     * @param account The underlying account
     * @param session The user's session
     */
    public AccountConfigHelper(CalendarAccount account, Session session) {
        super();
        this.account = account;
        this.session = session;
    }

    /**
     * Creates and returns a new {@link CalendarSettings} instance. The following properties are
     * read from the {@link CalendarAccount#getInternalConfiguration()}:
     * <ul>
     * <li>{@link CommonCalendarConfigurationFields#NAME}</li>
     * <li>{@link CommonCalendarConfigurationFields#SUBSCRIBED} (default: <code>true</code></li>
     * <li>{@link CommonCalendarConfigurationFields#USED_FOR_SYNC} (default: <code>true</code></li>
     * </ul>
     *
     * It also sets the user configuration, the last modified timestamp, the extended properties from the account,
     * and whether there was an error while previously persisting the account configuration.
     *
     * @return The calendar settings for the account
     */
    public CalendarSettings getCalendarSettings() {
        JSONObject internalConfig = account.getInternalConfiguration();
        CalendarSettings settings = new CalendarSettings();
        settings.setLastModified(account.getLastModified());
        settings.setConfig(account.getUserConfiguration());
        settings.setName(internalConfig.optString(CommonCalendarConfigurationFields.NAME, DEFAULT_CALENDAR_NAME));
        settings.setExtendedProperties(getExtendedProperties());
        settings.setSubscribed(internalConfig.optBoolean("subscribed", true));
        if (CachingCalendarUtils.canBeUsedForSync(account.getProviderId(), session)) {
            settings.setUsedForSync(UsedForSync.of(internalConfig.optBoolean(CommonCalendarConfigurationFields.USED_FOR_SYNC, true)));
        } else {
            settings.setUsedForSync(UsedForSync.DEACTIVATED);
        }
        settings.setError(optAccountError());
        return settings;
    }

    /**
     * Optionally gets a persisted account error that occurred during previous cache update operations from the underlying configuration.
     * <p/>
     * If there is an account error, this error should be added when constructing the account's settings object for
     * {@link BasicCalendarAccess#getSettings()}.
     *
     * @return The account error, or <code>null</code> if there is none
     * @see {@link CalendarSettings#setError(OXException)}
     */
    public OXException optAccountError() {
        if (null != account.getInternalConfiguration()) {
            JSONObject jsonObject = account.getInternalConfiguration().optJSONObject("lastError");
            if (null != jsonObject) {
                DataHandler dataHandler = Services.getService(ConversionService.class).getDataHandler(DataHandlers.JSON2OXEXCEPTION);
                try {
                    ConversionResult result = dataHandler.processData(new SimpleData<JSONObject>(jsonObject), new DataArguments(), null);
                    if (null != result && null != result.getData() && OXException.class.isInstance(result.getData())) {
                        return (OXException) result.getData();
                    }
                } catch (OXException e) {
                    LOG.error("Unable to process data.", e);
                }
            }
        }
        return null;
    }

    /**
     * Creates and returns a new instance of the {@link ExtendedProperties} based on common configuration fields in the account's internal
     * configuration. The following properties are read from the {@link CalendarAccount#getInternalConfiguration()}:
     * <ul>
     * <li>{@link CommonCalendarConfigurationFields#DESCRIPTION}</li>
     * <li>{@link CommonCalendarConfigurationFields#USED_FOR_SYNC}</li>
     * <li>{@link CommonCalendarConfigurationFields#COLOR}</li>
     * <li>{@link CachingCalendarAccessConstants#LAST_UPDATE}</li>
     * </ul>
     *
     * @return The {@link ExtendedProperties}
     */
    public ExtendedProperties getExtendedProperties() {
        JSONObject internalConfig = account.getInternalConfiguration();
        ExtendedProperties extendedProperties = new ExtendedProperties();
        extendedProperties.add(SCHEDULE_TRANSP(TimeTransparency.TRANSPARENT, true));
        extendedProperties.add(DESCRIPTION(internalConfig.optString(CommonCalendarConfigurationFields.DESCRIPTION, null)));
        extendedProperties.add(COLOR(internalConfig.optString(CommonCalendarConfigurationFields.COLOR, null), false));
        extendedProperties.add(LAST_UPDATE(optLastUpdate()));
        extendedProperties.add(USED_FOR_SYNC(B(internalConfig.optBoolean(CommonCalendarConfigurationFields.USED_FOR_SYNC, false)), false));
        return extendedProperties;
    }

    /**
     * Optionally gets the timestamp when the calendar data of the account was last updated.
     *
     * @return The timestamp of the last update, or <code>null</code> if unknown
     */
    public Long optLastUpdate() {
        JSONObject internalConfig = account.getInternalConfiguration();
        if (null != internalConfig) {
            JSONObject cachingConfig = internalConfig.optJSONObject(CachingCalendarAccessConstants.CACHING);
            if (null != cachingConfig) {
                long value = cachingConfig.optLong(CachingCalendarAccessConstants.LAST_UPDATE, 0L);
                if (0 < value) {
                    return Long.valueOf(value);
                }
            }
        }
        return null;
    }

}
