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

package com.openexchange.caldav.osgi;

import static com.openexchange.dav.DAVTools.getExternalPath;
import static com.openexchange.dav.DAVTools.getInternalPath;
import org.osgi.service.http.HttpService;
import com.openexchange.ajax.customizer.folder.AdditionalFolderField;
import com.openexchange.caldav.CalDAVURLField;
import com.openexchange.caldav.Tools;
import com.openexchange.caldav.mixins.DefaultAlarmVeventDate;
import com.openexchange.caldav.mixins.DefaultAlarmVeventDatetime;
import com.openexchange.caldav.mixins.ScheduleInboxURL;
import com.openexchange.caldav.mixins.ScheduleOutboxURL;
import com.openexchange.caldav.servlet.CalDAV;
import com.openexchange.caldav.servlet.CaldavPerformer;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccessFactory;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.contact.ContactService;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.database.DatabaseService;
import com.openexchange.dav.WellKnownServlet;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.ReadOnlyValue;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.jslob.ConfigTreeEquivalent;
import com.openexchange.login.Interface;
import com.openexchange.oauth.provider.resourceserver.scope.AbstractScopeProvider;
import com.openexchange.oauth.provider.resourceserver.scope.OAuthScopeProvider;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.service.http.HttpServices;
import com.openexchange.resource.ResourceService;
import com.openexchange.session.Session;
import com.openexchange.user.User;
import com.openexchange.user.UserService;
import com.openexchange.webdav.DevNullServlet;
import com.openexchange.webdav.protocol.helpers.PropertyMixin;
import com.openexchange.webdav.protocol.osgi.OSGiPropertyMixin;
import com.openexchange.xml.jdom.JDOMParser;


/**
 * The {@link CaldavActivator} initializes and publishes the caldav servlet
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CaldavActivator extends HousekeepingActivator {

    private static final String SERVLET_PATH = "/caldav";

    private static final String NULL_PATH = "/dev/null";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CaldavActivator.class);

    private OSGiPropertyMixin mixin;
    private String httpAliasCalDAV;
    private String httpAliasDevNull;

    /**
     * Initializes a new {@link CaldavActivator}.
     */
    public CaldavActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] {
            ICalEmitter.class, ICalParser.class, FolderService.class, RecurrenceService.class, CalendarUtilities.class, IDBasedCalendarAccessFactory.class,
            UserService.class, ConfigViewFactory.class, HttpService.class, JDOMParser.class, GroupService.class, CapabilityService.class,
            ContactService.class, ResourceService.class, DatabaseService.class, ICalService.class, CalendarService.class, LeanConfigurationService.class
        };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        try {
            CaldavPerformer performer = new CaldavPerformer(this);
            final HttpService httpService = getService(HttpService.class);

            ConfigViewFactory configViewFactory = getServiceSafe(ConfigViewFactory.class);
            httpAliasCalDAV = getInternalPath(configViewFactory, SERVLET_PATH);
            httpService.registerServlet(httpAliasCalDAV, new CalDAV(performer), null, null);
            httpService.registerServlet("/.well-known/caldav", new WellKnownServlet(getExternalPath(configViewFactory, SERVLET_PATH), Interface.CALDAV), null, null);
            httpAliasDevNull = getInternalPath(configViewFactory, NULL_PATH);
            httpService.registerServlet(httpAliasDevNull, new DevNullServlet(), null, null);

            final OSGiPropertyMixin mixin = new OSGiPropertyMixin(context, performer);
            performer.setGlobalMixins(mixin);
            this.mixin = mixin;
            registerService(PropertyMixin.class, new ScheduleOutboxURL(this));
            registerService(PropertyMixin.class, new ScheduleInboxURL(this));
            registerService(PropertyMixin.class, new DefaultAlarmVeventDate());
            registerService(PropertyMixin.class, new DefaultAlarmVeventDatetime());
            registerService(PreferencesItemService.class, new PreferencesItemService() {

                @Override
                public IValueHandler getSharedValue() {
                    return new ReadOnlyValue() {

                        @Override
                        public boolean isAvailable(UserConfiguration userConfig) {
                            return true;
                        }

                        @Override
                        public void getValue(Session session, Context ctx, User user,
                                UserConfiguration userConfig, Setting setting) throws OXException {
                            setting.setSingleValue(Boolean.FALSE);
                        }
                    };
                }

                @Override
                public String[] getPath() {
                    return new String[] { "modules", "caldav", "module" };
                }
            });

            /*-
             * # CalDAV
             * modules/caldav/active > io.ox/caldav//active
             * modules/caldav/url > io.ox/caldav//url
             */
            registerService(ConfigTreeEquivalent.class, new ConfigTreeEquivalent() {

                @Override
                public String getConfigTreePath() {
                    return "modules/caldav/active";
                }

                @Override
                public String getJslobPath() {
                    return "io.ox/caldav//active";
                }

                @Override
                public String toString() {
                    return "modules/caldav/active > io.ox/caldav//active";
                }
            });
            registerService(ConfigTreeEquivalent.class, new ConfigTreeEquivalent() {

                @Override
                public String getConfigTreePath() {
                    return "modules/caldav/url";
                }

                @Override
                public String getJslobPath() {
                    return "io.ox/caldav//url";
                }

                @Override
                public String toString() {
                    return "modules/caldav/url > io.ox/caldav//url";
                }
            });

            registerService(OAuthScopeProvider.class, new AbstractScopeProvider(Tools.OAUTH_SCOPE, OAuthStrings.SYNC_CALENDAR) {
                @Override
                public boolean canBeGranted(CapabilitySet capabilities) {
                    return capabilities.contains(Permission.CALDAV.getCapabilityName());
                }
            });

            registerService(AdditionalFolderField.class, new CalDAVURLField(this));

            openTrackers();
        } catch (Exception e) {
            LOG.error("", e);
            throw e;
        }
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        final HttpService httpService = getService(HttpService.class);
        if (null != httpService) {
            String alias = this.httpAliasCalDAV;
            if (alias != null) {
                this.httpAliasCalDAV = null;
                HttpServices.unregister(alias, httpService);
            }
            alias = this.httpAliasDevNull;
            if (alias != null) {
                this.httpAliasDevNull = null;
                HttpServices.unregister(alias, httpService);
            }
        }
        final OSGiPropertyMixin mixin = this.mixin;
        if (null != mixin) {
            this.mixin = null;
            mixin.close();
        }
        super.stopBundle();
    }

}
