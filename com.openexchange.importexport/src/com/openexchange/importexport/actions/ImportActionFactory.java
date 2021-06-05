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

package com.openexchange.importexport.actions;

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.importexport.Format;
import com.openexchange.importexport.actions.importer.CSVImportAction;
import com.openexchange.importexport.actions.importer.ICalImportAction;
import com.openexchange.importexport.actions.importer.VCardImportAction;
import com.openexchange.server.ServiceLookup;

public class ImportActionFactory extends AbstractIEActionFactory {

    /**
     * Initializes a new {@link ImportActionFactory}.
     * 
     * @param services
     */
    public ImportActionFactory(ServiceLookup services) {
        super(services);
    }

    @Override
    protected Map<Format, AJAXActionService> getActions() {
        // @formatter:off
    	return ImmutableMap.of(Format.CSV, new CSVImportAction(services),
    		Format.OUTLOOK_CSV, new CSVImportAction(services),
    		Format.VCARD, new VCardImportAction(services),
    		Format.ICAL, new ICalImportAction(services));
    	// @formatter:on
    }
}
