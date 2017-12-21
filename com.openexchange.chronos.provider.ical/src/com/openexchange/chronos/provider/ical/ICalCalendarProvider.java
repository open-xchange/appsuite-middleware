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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.provider.ical;

import java.util.EnumSet;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.auth.info.AuthType;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarCapability;
import com.openexchange.chronos.provider.caching.CachingCalendarProvider;
import com.openexchange.chronos.provider.folder.FolderCalendarAccess;
import com.openexchange.chronos.provider.ical.auth.ICalAuthParser;
import com.openexchange.chronos.provider.ical.conn.ICalFeedClient;
import com.openexchange.chronos.provider.ical.result.GetResponse;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 *
 * {@link ICalCalendarProvider}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class ICalCalendarProvider extends CachingCalendarProvider {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ICalCalendarProvider.class);

    @Override
    public String getId() {
        return ICalCalendarConstants.PROVIDER_ID;
    }

    @Override
    public String getDisplayName(Locale locale) {
        return "iCalendar Feeds";
    }

    @Override
    public EnumSet<CalendarCapability> getCapabilities() {
        return CalendarCapability.getCapabilities(ICalCalendarAccess.class);
    }

    @Override
    public FolderCalendarAccess connect(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        return new ICalCalendarAccess(session, account, parameters);
    }

    @Override
    protected JSONObject configureAccountOpt(Session session, JSONObject userConfig, CalendarParameters parameters) throws OXException {
        ICalCalendarFeedConfig iCalFeedConfig = new ICalCalendarFeedConfig.EncryptedBuilder(session, new JSONObject(userConfig), new JSONObject()).build();
        ICalFeedClient iCalFeedConnector = new ICalFeedClient(session, iCalFeedConfig);
        GetResponse response = iCalFeedConnector.executeRequest(); // check connections and authorization

        if (iCalFeedConfig.getAuthInfo().getAuthType().equals(AuthType.BASIC)) {
            ICalAuthParser.encrypt(userConfig, session.getPassword()); // encrypt password for persisting in user config column
        }
        return adaptInternalConfig(response);
    }

    private JSONObject adaptInternalConfig(GetResponse getResponse) {
        JSONObject internalConfig = new JSONObject();
        if (getResponse.getCalendar() == null) {
            return internalConfig;
        }
        JSONObject icalConfig = new JSONObject();
        try {
            internalConfig.put(ICalCalendarConstants.PROVIDER_ID, icalConfig);
            String feedName = getResponse.getFeedName();
            if (Strings.isNotEmpty(feedName)) {
                icalConfig.put("name", feedName);
            }
            String feedDesc = getResponse.getFeedDescription();
            if (Strings.isNotEmpty(feedDesc)) {
                icalConfig.put("description", feedDesc);
            }
        } catch (JSONException e) {
            LOG.warn("Unable to create internal config as desired.", e);
        }
        return internalConfig;
    }

    @Override
    protected JSONObject reconfigureAccountOpt(Session session, CalendarAccount calendarAccount, JSONObject userConfig, CalendarParameters parameters) throws OXException {
        ICalCalendarFeedConfig iCalFeedConfig = new ICalCalendarFeedConfig.EncryptedBuilder(session, new JSONObject(userConfig), new JSONObject()).build();
        ICalFeedClient iCalFeedConnector = new ICalFeedClient(session, iCalFeedConfig);
        GetResponse getResponse = iCalFeedConnector.executeRequest(); // check connections and authorization

        if (iCalFeedConfig.getAuthInfo().getAuthType().equals(AuthType.BASIC)) {
            ICalAuthParser.encrypt(userConfig, session.getPassword()); // encrypt password for persisting in user config column
        }
        return updateInternalConfig(calendarAccount, getResponse);
    }

    private JSONObject updateInternalConfig(CalendarAccount calendarAccount, GetResponse getResponse) {
        JSONObject internalConfig = calendarAccount.getInternalConfiguration();
        if (getResponse.getCalendar() == null) {
            return internalConfig;
        }
        try {
            internalConfig.remove(ICalCalendarConstants.PROVIDER_ID);
            JSONObject icalConfig = new JSONObject();
            String feedName = getResponse.getFeedName();
            if (Strings.isNotEmpty(feedName)) {
                icalConfig.put(ICalCalendarConstants.NAME, feedName);
            }
            String feedDesc = getResponse.getFeedDescription();
            if (Strings.isNotEmpty(feedDesc)) {
                icalConfig.put(ICalCalendarConstants.DESCRIPTION, feedDesc);
            }
            internalConfig.put(ICalCalendarConstants.PROVIDER_ID, icalConfig);
        } catch (JSONException e) {
            LOG.warn("Unable to create internal config as desired.", e);
        }
        return internalConfig;
    }

    @Override
    protected void onAccountCreatedOpt(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // nothing to do
    }

    @Override
    protected void onAccountUpdatedOpt(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // nothing to do
    }

    @Override
    protected void onAccountDeletedOpt(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // nothing to do
    }

    public void onAccountDeletedOpt(Context context, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // nothing to do
    }

    @Override
    public boolean triggerCacheInvalidation(Session session, JSONObject originUserConfiguration, JSONObject newUserConfiguration) throws OXException {
        ICalCalendarFeedConfig oldFeedConfig = new ICalCalendarFeedConfig.DecryptedBuilder(session, new JSONObject(originUserConfiguration), new JSONObject()).build();
        JSONObject newUserConfigurationCopy = new JSONObject(newUserConfiguration);
        ICalCalendarFeedConfig newFeedConfig = new ICalCalendarFeedConfig.EncryptedBuilder(session, newUserConfigurationCopy, new JSONObject()).build();
        return oldFeedConfig.mandatoryChanges(newFeedConfig);
    }

    @Override
    public void onAccountDeleted(Context context, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // TODO Auto-generated method stub
    }

}
