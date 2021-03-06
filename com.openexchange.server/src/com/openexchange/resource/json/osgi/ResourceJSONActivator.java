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

package com.openexchange.resource.json.osgi;

import com.openexchange.ajax.anonymizer.AnonymizerService;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.resource.ResourceService;
import com.openexchange.resource.json.ResourceActionFactory;
import com.openexchange.resource.json.anonymizer.ResourceAnonymizer;
import com.openexchange.resource.json.resultconverter.ResourceJsonResultConverter;
import com.openexchange.server.ExceptionOnAbsenceServiceLookup;

/**
 * {@link ResourceJSONActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ResourceJSONActivator extends AJAXModuleActivator {

    /**
     * Initializes a new {@link ResourceJSONActivator}.
     */
    public ResourceJSONActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[0];
    }

    @Override
    protected void startBundle() throws Exception {
        trackService(ResourceService.class);
        openTrackers();
        registerModule(new ResourceActionFactory(new ExceptionOnAbsenceServiceLookup(this)), "resource");
        registerService(ResultConverter.class, new ResourceJsonResultConverter());
        registerService(AnonymizerService.class, new ResourceAnonymizer());
    }

}
