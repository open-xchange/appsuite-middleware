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

package com.openexchange.importexport.impl;

import com.openexchange.exception.OXException;
import com.openexchange.importexport.Exporter;
import com.openexchange.importexport.Format;
import com.openexchange.importexport.ImportExportService;
import com.openexchange.importexport.Importer;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.exporters.CSVContactExporter;
import com.openexchange.importexport.exporters.ICalExporter;
import com.openexchange.importexport.exporters.VCardExporter;
import com.openexchange.importexport.importers.CSVContactImporter;
import com.openexchange.importexport.importers.ICalImporter;
import com.openexchange.importexport.importers.VCardImporter;
import com.openexchange.server.ServiceLookup;


/**
 * {@link ImportExportServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class ImportExportServiceImpl implements ImportExportService {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ImportExportServiceImpl}.
     */
    public ImportExportServiceImpl(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public Exporter getExporter(Format format) throws OXException {
        switch (format) {
            case CSV:
                return new CSVContactExporter();
            case ICAL:
                return new ICalExporter();
            case OUTLOOK_CSV:
                return new CSVContactExporter();
            case VCARD:
                return new VCardExporter();
            default:
                break;
        }

        throw ImportExportExceptionCodes.NO_SUCH_EXPORTER.create(format.getConstantName());
    }

    @Override
    public Importer getImporter(Format format) throws OXException {
        switch (format) {
            case CSV:
                return new CSVContactImporter(services);
            case ICAL:
                return new ICalImporter(services);
            case OUTLOOK_CSV:
                return new CSVContactImporter(services);
            case VCARD:
                return new VCardImporter(services);
            default:
                break;
        }

        throw ImportExportExceptionCodes.NO_SUCH_IMPORTER.create(format.getConstantName());
    }

}
