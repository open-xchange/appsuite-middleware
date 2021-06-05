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

package com.openexchange.dav.actions;

import static com.openexchange.webdav.action.WebdavOption.ADDRESSBOOK;
import static com.openexchange.webdav.action.WebdavOption.CALENDAR_ACCESS;
import static com.openexchange.webdav.action.WebdavOption.CALENDAR_AUTO_SCHEDULE;
import static com.openexchange.webdav.action.WebdavOption.CALENDAR_MANAGED_ATTACHMENT;
import static com.openexchange.webdav.action.WebdavOption.CALENDAR_PRINCIPAL_PROPERTY_SEARCH;
import static com.openexchange.webdav.action.WebdavOption.CALENDAR_PRINCIPAL_SEARCH;
import static com.openexchange.webdav.action.WebdavOption.CALENDAR_SCHEDULE;
import static com.openexchange.webdav.action.WebdavOption.CALENDAR_SERVER_PRIVATE_EVENTS;
import static com.openexchange.webdav.action.WebdavOption.CALENDAR_SERVER_RECURRENCE_SPLIT;
import static com.openexchange.webdav.action.WebdavOption.CALENDAR_SERVER_SHARING;
import static com.openexchange.webdav.action.WebdavOption.CALENDAR_SERVER_SUBSCRIBED;
import static com.openexchange.webdav.action.WebdavOption.EXTENDED_MKCOL;
import static com.openexchange.webdav.action.WebdavOption.RESOURCE_SHARING;
import java.util.EnumSet;
import java.util.Optional;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.chronos.provider.CalendarProviders;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.dav.DAVFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.webdav.action.DefaultWebdavOptionsAction;
import com.openexchange.webdav.action.WebdavOption;

/**
 * {@link OPTIONSAction}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class OPTIONSAction extends DefaultWebdavOptionsAction {

    private static final Optional<EnumSet<WebdavOption>> ATTACHMENT_OPTION = Optional.of(EnumSet.of(CALENDAR_MANAGED_ATTACHMENT));
    private static final Optional<EnumSet<WebdavOption>> PRINCIPAL_OPTIONS = Optional.of(EnumSet.of(CALENDAR_PRINCIPAL_SEARCH, CALENDAR_PRINCIPAL_PROPERTY_SEARCH));
    private static final Optional<EnumSet<WebdavOption>> ROOT_OPTIONS = Optional.of(EnumSet.of(CALENDAR_ACCESS, ADDRESSBOOK));
    private static final EnumSet<WebdavOption> BASIC_CALDAV_OPTIONS = EnumSet.of(CALENDAR_ACCESS, //@formatter:off
        CALENDAR_AUTO_SCHEDULE,
        CALENDAR_SCHEDULE,
        CALENDAR_PRINCIPAL_SEARCH,
        CALENDAR_PRINCIPAL_PROPERTY_SEARCH,
        CALENDAR_SERVER_PRIVATE_EVENTS,
        CALENDAR_SERVER_RECURRENCE_SPLIT,
        EXTENDED_MKCOL); //@formatter:on

    private static final EnumSet<WebdavOption> SHARING_OPTIONS = EnumSet.of(CALENDAR_SERVER_SHARING, RESOURCE_SHARING);
    private static final WebdavOption SUBSCRIBE_OPTION = CALENDAR_SERVER_SUBSCRIBED;
    private static final WebdavOption MANAGED_ATTACHMENT_OPTION = CALENDAR_MANAGED_ATTACHMENT;

    private static final EnumSet<WebdavOption> BASIC_CARD_DAV_OPTIONS = EnumSet.of(EXTENDED_MKCOL, ADDRESSBOOK, CALENDAR_PRINCIPAL_SEARCH, CALENDAR_PRINCIPAL_PROPERTY_SEARCH);


    private final DAVFactory factory;

    /**
     * Initializes a new {@link OPTIONSAction}.
     *
     * @param factory The underlying factory
     */
    public OPTIONSAction(DAVFactory factory) {
        super();
        this.factory = factory;
    }

    @Override
    protected EnumSet<WebdavOption> getDAVOptions(ServerSession session) throws OXException {
        EnumSet<WebdavOption> result = EnumSet.copyOf(super.getDAVOptions(session));
        addOptions(getAttachmentOptions(session), result);
        addOptions(getPrincipalOptions(), result);
        addOptions(getRootOptions(), result);
        addOptions(getCalDAVOptions(session), result);
        addOptions(getCardDAVOptions(session), result);
        return result;
    }

    /**
     * Adds the given options if available to given {@link EnumSet}
     *
     * @param optOptions The optional {@link WebdavOption} to add
     * @param result The {@link EnumSet} of {@link WebdavOption} the options should be added to
     */
    private void addOptions(Optional<EnumSet<WebdavOption>> optOptions, EnumSet<WebdavOption> result) {
        optOptions.ifPresent((set) -> result.addAll(set));
    }

    /**
     * Gets the attachment options
     *
     * @param session The users {@link ServerSession}
     * @return The {@link Optional} {@link EnumSet} of {@link WebdavOption}
     * @throws OXException
     */
    private Optional<EnumSet<WebdavOption>> getAttachmentOptions(ServerSession session) throws OXException {
        if (factory.getServiceSafe(CapabilityService.class).getCapabilities(session).contains("filestore")) {
            return ATTACHMENT_OPTION;
        }
        return Optional.empty();
    }

    /**
     * Gets the principal options
     *
     * @return The {@link Optional} {@link EnumSet} of {@link WebdavOption}
     */
    public Optional<EnumSet<WebdavOption>> getPrincipalOptions() {
        return PRINCIPAL_OPTIONS;
    }

    /**
     * Gets the root options
     *
     * @return The {@link Optional} {@link EnumSet} of {@link WebdavOption}
     */
    public Optional<EnumSet<WebdavOption>> getRootOptions() {
        return ROOT_OPTIONS;
    }

    /**
     * Gets the caldav options
     *
     * @param session The users {@link ServerSession}
     * @return The {@link Optional} {@link EnumSet} of {@link WebdavOption}
     * @throws OXException
     */
    public Optional<EnumSet<WebdavOption>> getCalDAVOptions(ServerSession session) throws OXException {
        if ((session.getUserPermissionBits().hasCalendar() || session.getUserPermissionBits().hasTask()) && factory.getServiceSafe(CapabilityService.class).getCapabilities(session).contains(Permission.CALDAV.getCapabilityName())) {
            EnumSet<WebdavOption> result = EnumSet.copyOf(BASIC_CALDAV_OPTIONS);
            if (session.getUserPermissionBits().hasFullSharedFolderAccess()) {
                result.addAll(SHARING_OPTIONS);
            }
            ConfigView configView = factory.getServiceSafe(ConfigViewFactory.class).getView(session.getUserId(), session.getContextId());

            if (factory.getServiceSafe(CapabilityService.class).getCapabilities(session).contains(CalendarProviders.getCapabilityName(CalendarProviders.ID_ICAL)) &&
                ConfigViews.getDefinedBoolPropertyFrom(CalendarProviders.getUsedForSyncPropertyName(CalendarProviders.ID_ICAL), true, configView)) {
                result.add(SUBSCRIBE_OPTION);
            }

            if (session.getUserPermissionBits().hasInfostore()) {
                result.add(MANAGED_ATTACHMENT_OPTION);
            }
            return Optional.of(result);
        }
        return Optional.empty();
    }

    /**
     * Gets the carddav options
     *
     * @param session The users {@link ServerSession}
     * @return The {@link Optional} {@link EnumSet} of {@link WebdavOption}
     * @throws OXException
     */
    public Optional<EnumSet<WebdavOption>> getCardDAVOptions(ServerSession session) throws OXException {
        if (session.getUserPermissionBits().hasContact() && factory.getServiceSafe(CapabilityService.class).getCapabilities(session).contains(Permission.CARDDAV.getCapabilityName())) {
            EnumSet<WebdavOption> result = EnumSet.copyOf(BASIC_CARD_DAV_OPTIONS);
            if (session.getUserPermissionBits().hasFullSharedFolderAccess()) {
                result.addAll(SHARING_OPTIONS);
            }
            if (session.getUserPermissionBits().hasInfostore()) {
                result.add(MANAGED_ATTACHMENT_OPTION);
            }
            return Optional.of(result);
        }
        return Optional.empty();
    }
}
