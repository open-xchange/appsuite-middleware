package com.openexchange.importexport.actions;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.formats.csv.PropertyDrivenMapper;
import com.openexchange.importexport.importers.Importer;
import com.openexchange.importexport.importers.OutlookCSVContactImporter;
import com.openexchange.importexport.osgi.ImportExportServices;

public class OutlookCsvImportAction extends AbstractImportAction {
	public static Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(OutlookCsvImportAction.class));

	private Importer importer;

	@Override
	public Format getFormat() {
		return Format.OUTLOOK_CSV;
	}

	@Override
	public Importer getImporter() {
		if(this.importer == null) {
            this.importer = getOutlookImporter();
        }
		return this.importer;
	}

    public Importer getOutlookImporter() {
        final OutlookCSVContactImporter outlook = new OutlookCSVContactImporter();
        try {
            final ConfigurationService conf = ImportExportServices.getConfigurationService();
            final String path = conf.getProperty("com.openexchange.import.mapper.path");
            if (path == null) {
                LOG.error("Reading the property 'com.openexchange.import.mapper.path' did not give path to mappers. Defaulting to deprecated mappers as fallback.");
                return outlook;
            }

            final File dir = new File(path);
            if (!dir.isDirectory()) {
                LOG.error("Directory " + path + " supposedly containing import mappers information wasn't actually a directory, defaulting to deprecated mappers as fallback.");
                return outlook;
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
                outlook.addFieldMappers(mapper);
                mapperAmount++;
            }
            if (mapperAmount == 0) {
                LOG.error("Did not load any CSV importer mappings, defaulting to deprecated mappers as fallback.");
            }
        } catch (final IOException e) {
            LOG.error("Failed when trying to load CSV importer mappings, defaulting to deprecated mappers as fallback.", e);
        }
        return outlook;
    }
}
