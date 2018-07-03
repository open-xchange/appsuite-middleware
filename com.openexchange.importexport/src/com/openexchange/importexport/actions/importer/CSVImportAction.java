/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.importexport.actions.importer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.formats.csv.PropertyDrivenMapper;
import com.openexchange.importexport.importers.CSVContactImporter;
import com.openexchange.importexport.importers.Importer;
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
            if (dir == null || !dir.isDirectory()) {
                LOG.error("Directory {} supposedly containing import mappers information wasn't actually a directory, defaulting to deprecated mappers as fallback.", path);
                return imp;
            }
            final File[] files = dir.listFiles();

            int mapperAmount = 0;
            for (final File file : files) {
                if (!file.getName().endsWith(".properties")) {
                    continue;
                }
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
        } catch (final IOException e) {
            LOG.error("Failed when trying to load CSV importer mappings.", e);
        }
        return imp;
    }

	@Override
	public Importer getImporter() {
		if(this.importer == null) {
            this.importer = loadImporter();
        }
		return this.importer;
	}

}
