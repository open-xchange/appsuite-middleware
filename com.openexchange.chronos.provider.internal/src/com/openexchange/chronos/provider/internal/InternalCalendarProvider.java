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

package com.openexchange.chronos.provider.internal;

import java.util.EnumSet;
import java.util.Locale;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
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
        return new InternalCalendarAccess(calendarSession, account, services);
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
        if(recurrenceId == null) {
            result = storage.getEventStorage().loadEvent(eventId, null);
        } else {
            result = storage.getEventStorage().loadException(eventId, recurrenceId, null);
        }
        return storage.getUtilities().loadAdditionalEventData(account.getUserId(), result, null);
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
