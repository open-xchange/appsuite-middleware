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

package com.openexchange.appsuite;

import java.util.Collection;
import java.util.Collections;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.Dispatcher;
import com.openexchange.login.DefaultAppSuiteLoginRampUp;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AppSuiteLoginRampUp}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AppSuiteLoginRampUp extends DefaultAppSuiteLoginRampUp {

    /**
     * Initializes a new {@link AppSuiteLoginRampUp}.
     *
     * @param services The service look-up
     */
    public AppSuiteLoginRampUp(ServiceLookup services) {
        super(services);
    }

    @Override
    protected String getConfigInfix() {
        return "open-xchange-appsuite";
    }

    @Override
    public boolean contributesTo(String client) {
        return "open-xchange-appsuite".equals(client);
    }

    @Override
    protected Collection<Contribution> getExtendedContributions() {
        Dispatcher ox = services.getService(Dispatcher.class);
        if (null == ox) {
            return Collections.emptyList();
        }

        return Collections.<Contribution> singletonList(new OnboardingDevicesContribution(ox));
    }

    // -------------------------------------------------------------------------------

    private static final class OnboardingDevicesContribution extends AbstractDispatcherContribution {

        private final String moduleName;

        OnboardingDevicesContribution(Dispatcher ox) {
            super(ox);
            moduleName = "onboarding";
        }

        @Override
        public String getKey() {
            return "onboardingDevices";
        }

        @Override
        protected String[] getParams() {
            return new String[0];
        }

        @Override
        protected String getModule() {
            return moduleName;
        }

        @Override
        protected String getAction() {
            return "devices";
        }

        @Override
        protected boolean isApplicable(ServerSession session, AJAXRequestData loginRequest, Dispatcher ox) {
            return null != ox.lookupFactory(moduleName);
        }
    }
}
