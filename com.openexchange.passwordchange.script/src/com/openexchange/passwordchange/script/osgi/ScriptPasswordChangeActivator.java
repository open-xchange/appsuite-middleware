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

package com.openexchange.passwordchange.script.osgi;

import org.osgi.framework.ServiceRegistration;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.passwordchange.PasswordChangeService;
import com.openexchange.passwordchange.script.impl.ScriptPasswordChange;
import com.openexchange.passwordchange.script.impl.ScriptPasswordChangeConfig;
import com.openexchange.user.UserService;

/**
 * {@link ScriptPasswordChangeActivator}
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 */
public final class ScriptPasswordChangeActivator extends HousekeepingActivator implements Reloadable {

    private ServiceRegistration<PasswordChangeService> registration;

    /**
     * Initializes a new {@link ScriptPasswordChangeActivator}
     */
    public ScriptPasswordChangeActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class, UserService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(Reloadable.class, this);
        registerScriptPasswordChange(getService(ConfigurationService.class));
    }

    private synchronized void registerScriptPasswordChange(ConfigurationService configService) {
        unregisterScriptPasswordChange();

        ScriptPasswordChangeConfig changeConfig = ScriptPasswordChangeConfig.builder().init(configService).build();
        registration = context.registerService(PasswordChangeService.class, new ScriptPasswordChange(changeConfig, this), null);
    }

    private synchronized void unregisterScriptPasswordChange() {
        ServiceRegistration<PasswordChangeService> registration = this.registration;
        if (null != registration) {
            this.registration = null;
            registration.unregister();
        }
    }

    @Override
    protected void stopBundle() throws Exception {
        unregisterScriptPasswordChange();
        super.stopBundle();
    }

    // -------------------------------------------------------------------------------------

    @Override
    public Interests getInterests() {
        return ScriptPasswordChangeConfig.getInterests();
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        registerScriptPasswordChange(configService);
    }

}
