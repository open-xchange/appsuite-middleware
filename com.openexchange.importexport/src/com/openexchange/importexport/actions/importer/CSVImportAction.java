package com.openexchange.importexport.actions.importer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.formats.csv.PropertyDrivenMapper;
import com.openexchange.importexport.importers.CSVContactImporter;
import com.openexchange.importexport.importers.Importer;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.log.LogFactory;

public class CSVImportAction extends AbstractImportAction implements AJAXActionService {

	public static Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(CSVImportAction.class));

	private Importer importer;

	@Override
	public Format getFormat() {
		return Format.CSV; //STOOOOPID. needs to be the other way around. Is this still used?
	}

    public Importer loadImporter() {
        final CSVContactImporter imp = new CSVContactImporter();
        try {
            final ConfigurationService conf = ImportExportServices.getConfigurationService();
            final String dirName = conf.getProperty("com.openexchange.import.mapper.path");
            if (dirName == null) {
                LOG.error("Reading the property 'com.openexchange.import.mapper.path' did not give directory name for mappers.");
                return imp;
            }

            final File dir = conf.getDirectory(dirName);
            if (dir == null || !dir.isDirectory()) {
                LOG.error("Directory " + dirName + " supposedly containing import mappers information wasn't actually a directory.");
                return imp;
            }
            final File[] files = dir.listFiles();

            int mapperAmount = 0;
            for (final File file : files) {
                if (!file.getName().endsWith(".properties")) {
                    continue;
                }
                final Properties props = new Properties();
                final InputStream in = new BufferedInputStream(new FileInputStream(file));
                try {
                    props.load(in);
                } finally {
                    in.close();
                }
                final PropertyDrivenMapper mapper = new PropertyDrivenMapper(props);
                imp.addFieldMapper(mapper);
                mapperAmount++;
            }
            if (mapperAmount == 0) {
                LOG.error("Did not load any CSV importer mappings from directory " + dirName +  ".");
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
