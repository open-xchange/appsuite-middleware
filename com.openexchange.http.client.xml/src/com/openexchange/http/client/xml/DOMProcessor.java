package com.openexchange.http.client.xml;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPResponse;
import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.http.client.builder.HTTPResponseWrapper;
import com.openexchange.http.client.exceptions.OxHttpClientExceptionCodes;

public class DOMProcessor implements HTTPResponseProcessor<InputStream, Document> {

	public Class<?>[] getTypes() {
		return new Class[]{InputStream.class, Document.class};
	}

	public HTTPResponse<Document> process(HTTPResponse<InputStream> response) throws OXException {
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(response.getPayload(), "UTF-8");
			return new HTTPResponseWrapper<Document>(response, document);
		} catch (Exception e) {
			throw OxHttpClientExceptionCodes.CATCH_ALL.create(e.getMessage(), e);
		}
	}

}
