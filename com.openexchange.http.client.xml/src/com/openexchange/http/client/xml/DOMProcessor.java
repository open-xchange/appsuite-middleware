package com.openexchange.http.client.xml;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.http.client.builder.HTTPResponse;
import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.http.client.builder.HTTPResponseWrapper;

public class DOMProcessor implements HTTPResponseProcessor<InputStream, Document> {

	public Class<?>[] getTypes() {
		return new Class[]{InputStream.class, Document.class};
	}

	public HTTPResponse<Document> process(HTTPResponse<InputStream> response) throws AbstractOXException {
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(response.getPayload(), "UTF-8");
			return new HTTPResponseWrapper<Document>(response, document);
		} catch (AbstractOXException e) {
			throw e;
		} catch (Exception e) {
			throw new AbstractOXException(e.getMessage(), e);
		}
	}

}
