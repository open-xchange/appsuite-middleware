package com.openexchange.importexport.json;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.tools.session.ServerSession;

public class ImportRequest {

	private AJAXRequestData request;
	private ServerSession session;
	private final List<String> folders;
	private InputStream inputStream;

	public AJAXRequestData getRequest() {
		return request;
	}

	public void setRequest(AJAXRequestData request) {
		this.request = request;
	}

	public ServerSession getSession() {
		return session;
	}

	public void setSession(ServerSession session) {
		this.session = session;
	}

	public ImportRequest(AJAXRequestData request, ServerSession session) throws OXException {
		this.session = session;
		this.request = request;
		//TODO check or fail:
		if(request.getParameter(AJAXServlet.PARAMETER_FOLDERID) == null){
			throw new OXException(); //TODO
		}
		this.folders = Arrays.asList(request.getParameter(AJAXServlet.PARAMETER_FOLDERID).split(","));

		if(! request.hasUploads()){
			throw ImportExportExceptionCodes.NO_FILE_UPLOADED.create();
		}
		if(request.getFiles().size() > 1){
			throw ImportExportExceptionCodes.ONLY_ONE_FILE.create();
		}
		try {
			this.inputStream = request.getUploadStream();
		} catch (IOException e) {
			throw ImportExportExceptionCodes.IOEXCEPTION.create();
		}
	}

	public int getContextId() {
		return session.getContextId();
	}

	public List<String> getFolders() {
		return this.folders;
	}

	public InputStream getImportFileAsStream() {
		return this.inputStream;
	}
}
