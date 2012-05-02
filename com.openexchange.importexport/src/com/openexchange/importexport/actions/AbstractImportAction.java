package com.openexchange.importexport.actions;

import java.util.List;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.importexport.ImportExportWriter;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.importers.Importer;
import com.openexchange.importexport.json.ImportRequest;
import com.openexchange.tools.session.ServerSession;

public abstract class AbstractImportAction implements AJAXActionService {

	@Override
	public AJAXRequestResult perform(AJAXRequestData requestData,
			ServerSession session) throws OXException {
		return perform(new ImportRequest(requestData, session));
	}

	public abstract Format getFormat();

	public abstract Importer getImporter();

	private AJAXRequestResult perform(ImportRequest req) throws OXException {
		final List<ImportResult> importResult;

		importResult = getImporter().importData(req.getSession(), getFormat(),
				req.getImportFileAsStream(), req.getFolders(), null);

		ImportExportWriter writer = new ImportExportWriter(req.getSession());
		AJAXRequestResult result = new AJAXRequestResult();
		// write import result to request result -> JSONify
		return result;

	}

}
