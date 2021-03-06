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

package com.openexchange.conversion.servlet.osgi;

import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.servlet.ConversionActionFactory;
import com.openexchange.server.ExceptionOnAbsenceServiceLookup;

/**
 * {@link ConversionServletActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ConversionServletActivator extends AJAXModuleActivator {

    /**
     * Initializes a new {@link ConversionServletActivator}
     */
    public ConversionServletActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConversionService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        registerModule(new ConversionActionFactory(new ExceptionOnAbsenceServiceLookup(this)), "conversion");
    }

}
