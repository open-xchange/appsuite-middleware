package com.openexchange.http.client.xml;

import java.io.InputStream;
import java.io.Reader;

import org.jdom.Document;

import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPResponse;
import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.http.client.builder.HTTPResponseWrapper;
import com.openexchange.http.client.exceptions.OxHttpClientExceptionCodes;
import com.openexchange.xml.jdom.JDOMParser;

public class JDOMProcessor implements HTTPResponseProcessor<InputStream, Document> {

	private JDOMParser parser;

	public JDOMProcessor(JDOMParser parser) {
		this.parser = parser;
	}
	
	public Class<?>[] getTypes() {
		return new Class[]{InputStream.class, Document.class};
	}

	public HTTPResponse<Document> process(HTTPResponse<InputStream> response)
			throws OXException {
		try {
			Document document = parser.parse(response.getPayload());
			return new HTTPResponseWrapper<Document>(response, document);
		} catch (Exception x) {
			throw OxHttpClientExceptionCodes.CATCH_ALL.create(x.getMessage(), x);
		}
	}

}
