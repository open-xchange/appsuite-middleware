package com.openexchange.http.client.xml;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.joox.JOOX;
import org.joox.Match;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.http.client.exceptions.OxHttpClientExceptionCodes;

public class JOOXProcessor implements HTTPResponseProcessor {
	public Class<?>[] getTypes() {
		return new Class[]{InputStream.class, Match.class};
	}

	public Object process(Object response) throws OXException {
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse((InputStream) response, "UTF-8");
			return JOOX.$(document);
		} catch (SAXException x) {
			if (x.getMessage().contains("Premature end of file.")) {
				return null;
			}
			throw OxHttpClientExceptionCodes.SAX_ERROR.create(x.getMessage(), x);
		} catch (Exception e) {
			throw OxHttpClientExceptionCodes.CATCH_ALL.create(e.getMessage(), e);
		}
	}
}
