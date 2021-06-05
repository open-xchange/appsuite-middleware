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

package com.openexchange.advertisement.json.osgi;

import org.slf4j.LoggerFactory;
import com.openexchange.advertisement.AdvertisementPackageService;
import com.openexchange.advertisement.json.AdConfigRestService;
import com.openexchange.advertisement.json.AdvertisementActionFactory;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;

/**
 * {@link Activator}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class Activator extends AJAXModuleActivator {

    private static final String MODULE = "advertisement";

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { AdvertisementPackageService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LoggerFactory.getLogger(Activator.class).info("starting bundle com.openexchange.advertisement.json");

        Services.setServiceLookup(this);
        registerModule(AdvertisementActionFactory.getInstance(), MODULE);
        registerService(AdConfigRestService.class, new AdConfigRestService());
    }
    
    @Override
    protected void stopBundle() throws Exception {
        LoggerFactory.getLogger(Activator.class).info("stopping bundle com.openexchange.advertisement.json");

        super.stopBundle();
    	Services.setServiceLookup(null);
    }

}
