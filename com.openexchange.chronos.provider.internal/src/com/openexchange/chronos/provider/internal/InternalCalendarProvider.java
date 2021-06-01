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

package com.openexchange.chronos.provider.internal;

import java.util.EnumSet;
import java.util.Locale;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.AdministrativeCalendarProvider;
import com.openexchange.chronos.provider.AutoProvisioningCalendarProvider;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarCapability;
import com.openexchange.chronos.provider.folder.FolderCalendarAccess;
import com.openexchange.chronos.provider.folder.FolderCalendarProvider;
import com.openexchange.chronos.provider.internal.config.UserConfigHelper;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.osgi.Tools;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link InternalCalendarProvider}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class InternalCalendarProvider implements FolderCalendarProvider, AutoProvisioningCalendarProvider, AdministrativeCalendarProvider {

    private static final Logger LOG = LoggerFactory.getLogger(InternalCalendarProvider.class);
    private final ServiceLookup services;

    /**
     * Initializes a new {@link InternalCalendarProvider}.
     *
     * @param services A service lookup reference
     */
    public InternalCalendarProvider(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public String getId() {
        return Constants.PROVIDER_ID;
    }

    @Override
    public int getDefaultMaxAccounts() {
        return 1;
    }

    @Override
    public String getDisplayName(Locale locale) {
        return StringHelper.valueOf(locale).getString(InternalCalendarStrings.PROVIDER_NAME);
    }

    @Override
    public EnumSet<CalendarCapability> getCapabilities() {
        return CalendarCapability.getCapabilities(InternalCalendarAccess.class);
    }

    @Override
    public FolderCalendarAccess connect(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        CalendarService calendarService = services.getService(CalendarService.class);
        CalendarSession calendarSession = calendarService.init(session, parameters);
        return new InternalCalendarAccess(calendarSession, services);
    }

    @Override
    public JSONObject configureAccount(Session session, JSONObject userConfig, CalendarParameters parameters) throws OXException {
        /*
         * no manual account creation allowed as accounts are provisioned automatically
         */
        throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(Constants.PROVIDER_ID);
    }

    @Override
    public JSONObject reconfigureAccount(Session session, CalendarAccount calendarAccount, JSONObject userConfig, CalendarParameters parameters) throws OXException {
        /*
         * initialize & check user config
         */
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        new UserConfigHelper(services).checkUserConfig(serverSession, userConfig);
        return null;
    }

    @Override
    public void onAccountCreated(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // no
    }

    @Override
    public void onAccountUpdated(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // no
    }

    @Override
    public void onAccountDeleted(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // no
    }

    @Override
    public void onAccountDeleted(Context context, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // no
    }

    @Override
    public JSONObject autoConfigureAccount(Session session, JSONObject userConfig, CalendarParameters parameters) throws OXException {
        /*
         * init user config based on migrated legacy settings
         */
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        new UserConfigHelper(services).applyLegacyConfig(serverSession, userConfig);
        return new JSONObject();
    }

    @Override
    public Event getEventByAlarm(Context context, CalendarAccount account, String eventId, RecurrenceId recurrenceId) throws OXException {
        CalendarStorageFactory factory = Tools.requireService(CalendarStorageFactory.class, services);
        CalendarStorage storage = factory.create(context, account.getAccountId(), optEntityResolver(context.getContextId()));
        Event result;
        if (recurrenceId == null) {
            result = storage.getEventStorage().loadEvent(eventId, null);
        } else {
            result = storage.getEventStorage().loadException(eventId, recurrenceId, null);
        }
        result = storage.getUtilities().loadAdditionalEventData(account.getUserId(), result, null);
        if (result != null) {
            result.setFolderId(CalendarUtils.getFolderView(result, account.getUserId()));
        }
        return result;
    }

    /**
     * Optionally gets an entity resolver for the supplied context.
     *
     * @param contextId The identifier of the context to get the entity resolver for
     * @return The entity resolver, or <code>null</code> if not available
     * @throws OXException
     */
    private EntityResolver optEntityResolver(int contextId) throws OXException {
        CalendarUtilities utils = Tools.requireService(CalendarUtilities.class, services);
        try {
            return utils.getEntityResolver(contextId);
        } catch (OXException e) {
            LOG.trace("Error getting entity resolver for context: {}", Integer.valueOf(contextId), e);
        }
        return null;
    }

    @Override
    public void touchEvent(Context context, CalendarAccount account, String eventId) throws OXException {
        CalendarStorageFactory factory = Tools.requireService(CalendarStorageFactory.class, services);
        Event eventUpdate = new Event();
        eventUpdate.setId(eventId);
        eventUpdate.setTimestamp(System.currentTimeMillis());
        CalendarStorage storage = factory.create(context, account.getAccountId(), optEntityResolver(context.getContextId()));
        storage.getEventStorage().updateEvent(eventUpdate);
    }




}
