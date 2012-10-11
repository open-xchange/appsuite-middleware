package com.openexchange.http.client.xml;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.http.client.exceptions.OxHttpClientExceptionCodes;

public class DOMProcessor implements HTTPResponseProcessor {

	public Class<?>[] getTypes() {
		return new Class[]{InputStream.class, Document.class};
	}

	public Object process(Object response) throws OXException {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse((InputStream) response, "UTF-8");
		} catch (Exception e) {
			throw OxHttpClientExceptionCodes.CATCH_ALL.create(e.getMessage(), e);
		}
	}

}
