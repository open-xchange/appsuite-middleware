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

package com.openexchange.report.osgi;

import java.rmi.Remote;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.osgi.DependentServiceRegisterer;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.report.InfostoreInformationService;
import com.openexchange.report.LoginCounterService;
import com.openexchange.report.internal.InfostoreInformationImpl;
import com.openexchange.report.internal.LastLoginRecorder;
import com.openexchange.report.internal.LastLoginUpdater;
import com.openexchange.report.internal.LoginCounterImpl;
import com.openexchange.report.internal.LoginCounterRMIServiceImpl;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.user.UserService;

/**
 * {@link ReportActivator} - The activator for reporting. Registers the services writing reporting information to the database and the
 * services providing interfaces for fetching report data.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ReportActivator extends HousekeepingActivator {

    public ReportActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        track(new DependentServiceRegisterer<LoginHandlerService>(context, LoginHandlerService.class, LastLoginRecorder.class, null, ConfigurationService.class, UserService.class));

        Dictionary<String, Object> dict = new Hashtable<String, Object>(1);
        dict.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.TOPIC_TOUCH_SESSION);
        track(new DependentServiceRegisterer<EventHandler>(context, EventHandler.class, LastLoginUpdater.class, dict, ContextService.class, UserService.class));

        LoginCounterService counterService = new LoginCounterImpl();
        registerService(LoginCounterService.class, counterService);
        registerService(InfostoreInformationService.class, new InfostoreInformationImpl());
        Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
        serviceProperties.put("RMI_NAME", LoginCounterRMIServiceImpl.RMI_NAME);
        registerService(Remote.class, new LoginCounterRMIServiceImpl(counterService), serviceProperties);
    }

    private final void track(DependentServiceRegisterer<?> registerer) throws InvalidSyntaxException {
        track(registerer.getFilter(), registerer);
    }
}
