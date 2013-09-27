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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.caldav.osgi;

import org.apache.commons.logging.Log;
import org.osgi.service.http.HttpService;
import com.openexchange.caldav.mixins.CalendarUserAddressSet;
import com.openexchange.caldav.mixins.DefaultAlarmVeventDate;
import com.openexchange.caldav.mixins.DefaultAlarmVeventDatetime;
import com.openexchange.caldav.mixins.ScheduleInboxURL;
import com.openexchange.caldav.mixins.ScheduleOutboxURL;
import com.openexchange.caldav.servlet.CalDAV;
import com.openexchange.caldav.servlet.CaldavPerformer;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.freebusy.service.FreeBusyService;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.ReadOnlyValue;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.session.Session;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.user.UserService;
import com.openexchange.webdav.DevNullServlet;
import com.openexchange.webdav.directory.PathRegistration;
import com.openexchange.webdav.protocol.helpers.PropertyMixin;
import com.openexchange.webdav.protocol.helpers.PropertyMixinFactory;
import com.openexchange.webdav.protocol.osgi.OSGiPropertyMixin;


/**
 * The {@link CaldavActivator} initializes and publishes the caldav servlet
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CaldavActivator extends HousekeepingActivator {

    private static final String SERVLET_PATH = "/servlet/dav/caldav";

    private static final String NULL_PATH = "/servlet/dav/dev/null";

    private static final Log LOG = com.openexchange.log.Log.loggerFor(CaldavActivator.class);

	protected static final String[] PREFERENCE_PATH = new String[]{"modules", "caldav", "module"};

    private volatile OSGiPropertyMixin mixin;

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] {
            ICalEmitter.class, ICalParser.class, AppointmentSqlFactoryService.class, CalendarCollectionService.class, FolderService.class,
            UserService.class, ConfigViewFactory.class, HttpService.class, FreeBusyService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            CalDAV.setServiceLookup(this);
            CaldavPerformer.setServices(this);

            final HttpService httpService = getService(HttpService.class);
            httpService.registerServlet(SERVLET_PATH, new CalDAV(), null, null);
            httpService.registerServlet(NULL_PATH, new DevNullServlet(), null, null);

            final CaldavPerformer performer = CaldavPerformer.getInstance();
            final OSGiPropertyMixin mixin = new OSGiPropertyMixin(context, performer);
            performer.setGlobalMixins(mixin);
            this.mixin = mixin;

            registerService(PropertyMixinFactory.class, new PropertyMixinFactory() {

                @Override
                public PropertyMixin create(final SessionHolder sessionHolder) {
                    return new CalendarUserAddressSet(sessionHolder);
                }

            });
            registerService(PropertyMixin.class, new ScheduleOutboxURL());
            registerService(PropertyMixin.class, new ScheduleInboxURL());
            registerService(PropertyMixin.class, new DefaultAlarmVeventDate());
            registerService(PropertyMixin.class, new DefaultAlarmVeventDatetime());

            registerService(PathRegistration.class, new PathRegistration("caldav"));

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
					return PREFERENCE_PATH;
				}
			});

            openTrackers();
        } catch (final Throwable t) {
            LOG.error(t.getMessage(), t);
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        final HttpService httpService = getService(HttpService.class);
        if (null != httpService) {
            httpService.unregister(SERVLET_PATH);
            httpService.unregister(NULL_PATH);
        }
        final OSGiPropertyMixin mixin = this.mixin;
        if (null != mixin) {
            mixin.close();
            this.mixin = null;
        }
        super.stopBundle();
    }

}
