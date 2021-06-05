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

package com.openexchange.jslob.json.osgi;

import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.jslob.json.JSlobActionFactory;
import com.openexchange.jslob.json.converter.JSlobJSONResultConverter;
import com.openexchange.jslob.registry.JSlobServiceRegistry;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link JSlobJSONActivator} - Activator for the JSlob JSON interface.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class JSlobJSONActivator extends AJAXModuleActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { JSlobServiceRegistry.class, ThreadPoolService.class, DispatcherPrefixService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JSlobJSONActivator.class);
        registerModule(new JSlobActionFactory(this), "jslob");
        registerService(ResultConverter.class, new JSlobJSONResultConverter());
        log.info("Bundle successfully started: com.openexchange.jslob.json");
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        org.slf4j.LoggerFactory.getLogger(JSlobJSONActivator.class).info("Bundle stopped: com.openexchange.jslob.json");
    }
}
