package com.openexchange.importexport.actions.importer;

import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.importers.CSVContactImporter;
import com.openexchange.importexport.importers.Importer;

public class CsvImportAction extends AbstractImportAction {

	private CSVContactImporter importer;

	@Override
	public Format getFormat() {
		return Format.CSV;
	}

	@Override
	public Importer getImporter() {
		if(this.importer == null)
			this.importer = new CSVContactImporter();

		return this.importer;
	}

}
