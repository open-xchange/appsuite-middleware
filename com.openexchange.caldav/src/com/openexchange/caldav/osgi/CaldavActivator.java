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
import com.openexchange.log.LogFactory;
import org.osgi.service.http.HttpService;
import com.openexchange.caldav.mixins.CalendarHomeSet;
import com.openexchange.caldav.mixins.CalendarUserAddressSet;
import com.openexchange.caldav.mixins.ScheduleOutboxURL;
import com.openexchange.caldav.servlet.CalDAV;
import com.openexchange.caldav.servlet.CaldavPerformer;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.user.UserService;
import com.openexchange.webdav.DevNullServlet;
import com.openexchange.webdav.directory.PathRegistration;
import com.openexchange.webdav.protocol.helpers.PropertyMixin;
import com.openexchange.webdav.protocol.helpers.PropertyMixinFactory;
import com.openexchange.webdav.protocol.osgi.OSGiPropertyMixin;


/**
 * The {@link CaldavActivator} initialises and publishes the caldav servlet
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CaldavActivator extends HousekeepingActivator {
    
    private static final String SERVLET_PATH = "/servlet/dav/caldav";
    
    private static final String NULL_PATH = "/servlet/dav/dev/null";

    private static final Log LOG = LogFactory.getLog(CaldavActivator.class);
    
    private static final Class<?>[] NEEDED = new Class[]{ICalEmitter.class, ICalParser.class, AppointmentSqlFactoryService.class, CalendarCollectionService.class, FolderService.class, UserService.class, ConfigViewFactory.class, HttpService.class};

    private OSGiPropertyMixin mixin;
    
    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED;
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            CalDAV.setServiceLookup(this);
            CaldavPerformer.setServices(this);
            
            HttpService httpService = getService(HttpService.class);
            httpService.registerServlet(SERVLET_PATH, new CalDAV(), null, null);
            httpService.registerServlet(NULL_PATH, new DevNullServlet(), null, null);
            
            CaldavPerformer performer = CaldavPerformer.getInstance();
            mixin = new OSGiPropertyMixin(context, performer);
            performer.setGlobalMixins(mixin);
            
            registerService(PropertyMixin.class, new CalendarHomeSet());
            registerService(PropertyMixinFactory.class, new PropertyMixinFactory() {

                @Override
                public PropertyMixin create(SessionHolder sessionHolder) {
                    return new CalendarUserAddressSet(sessionHolder);
                }
                
            });
            registerService(PropertyMixin.class, new ScheduleOutboxURL());
            
            registerService(PathRegistration.class, new PathRegistration("caldav"));
            openTrackers();
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        }
    }
    
    @Override
    protected void stopBundle() throws Exception {
        HttpService httpService = getService(HttpService.class);
        httpService.unregister(SERVLET_PATH);
        httpService.unregister(NULL_PATH);
        
        mixin.close();
        super.stopBundle();
    }

}
