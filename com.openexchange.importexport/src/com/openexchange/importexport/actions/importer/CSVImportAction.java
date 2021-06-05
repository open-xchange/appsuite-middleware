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

package com.openexchange.importexport.actions.importer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.importexport.Format;
import com.openexchange.importexport.Importer;
import com.openexchange.importexport.formats.csv.PropertyDrivenMapper;
import com.openexchange.importexport.importers.CSVContactImporter;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.java.Streams;
import com.openexchange.server.ServiceLookup;

public class CSVImportAction extends AbstractImportAction implements AJAXActionService {

	/**
     * Initializes a new {@link CSVImportAction}.
     * @param services
     */
    public CSVImportAction(ServiceLookup services) {
        super(services);
    }

    public static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CSVImportAction.class);

	private Importer importer;

	@Override
	public Format getFormat() {
		return Format.CSV; //STOOOOPID. needs to be the other way around. Is this still used?
	}

    public Importer loadImporter() {
        final CSVContactImporter imp = new CSVContactImporter(services);
        try {
            final ConfigurationService conf = ImportExportServices.getConfigurationService();
            final String path = conf.getProperty("com.openexchange.import.mapper.path");
            if (path == null) {
                LOG.error("Reading the property 'com.openexchange.import.mapper.path' did not give path to mappers. Defaulting to deprecated mappers as fallback.");
                return imp;
            }

            final File dir = new File(path);
            if (!dir.isDirectory()) {
                LOG.error("Directory {} supposedly containing import mappers information wasn't actually a directory, defaulting to deprecated mappers as fallback.", path);
                return imp;
            }
            final File[] files = dir.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File directory, String name) {
                    return name.endsWith(".properties");
                }
            });

            int mapperAmount = 0;
            for (final File file : files) {
                final Properties props = new Properties();
                final InputStream in = new BufferedInputStream(new FileInputStream(file), 65536);
                try {
                    props.load(in);
                } finally {
                    Streams.close(in);
                }
                final PropertyDrivenMapper mapper = new PropertyDrivenMapper(props, file.getName());
                imp.addFieldMapper(mapper);
                mapperAmount++;
            }
            if (mapperAmount == 0) {
                LOG.error("Did not load any CSV importer mappings from directory {}.", path);
            }
        } catch (IOException e) {
            LOG.error("Failed when trying to load CSV importer mappings.", e);
        }
        return imp;
    }

	@Override
	public Importer getImporter() {
		if (this.importer == null) {
            this.importer = loadImporter();
        }
		return this.importer;
	}

}
