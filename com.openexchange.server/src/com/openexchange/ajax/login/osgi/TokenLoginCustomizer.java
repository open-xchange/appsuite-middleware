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

package com.openexchange.ajax.login.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tokenlogin.TokenLoginService;


/**
 * {@link TokenLoginCustomizer}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class TokenLoginCustomizer implements ServiceTrackerCustomizer<TokenLoginService, TokenLoginService> {
    
    private final BundleContext context;

    /**
     * Initializes a new {@link TokenLoginCustomizer}.
     */
    public TokenLoginCustomizer(BundleContext context) {
        super();
        this.context = context;
    }

    @Override
    public TokenLoginService addingService(ServiceReference<TokenLoginService> arg0) {
        TokenLoginService service = context.getService(arg0);
        ServerServiceRegistry.getInstance().addService(TokenLoginService.class, service);
        return service;
    }

    @Override
    public void modifiedService(ServiceReference<TokenLoginService> arg0, TokenLoginService arg1) {
        // nothing to do
    }

    @Override
    public void removedService(ServiceReference<TokenLoginService> arg0, TokenLoginService arg1) {
        ServerServiceRegistry.getInstance().removeService(TokenLoginService.class);
        context.ungetService(arg0);
    }

}
