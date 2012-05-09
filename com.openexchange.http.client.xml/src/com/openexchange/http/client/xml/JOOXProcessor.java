package com.openexchange.http.client.xml;

import java.io.InputStream;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.joox.JOOX;
import org.joox.Match;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.http.client.builder.HTTPResponse;
import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.http.client.builder.HTTPResponseWrapper;

public class JOOXProcessor implements HTTPResponseProcessor<InputStream, Match> {
	public Class<?>[] getTypes() {
		return new Class[]{InputStream.class, Match.class};
	}

	public HTTPResponse<Match> process(HTTPResponse<InputStream> response) throws AbstractOXException {
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(response.getPayload(), "UTF-8");
			return new HTTPResponseWrapper<Match>(response, JOOX.$(document));
		} catch (AbstractOXException e) {
			throw e;
		} catch (SAXException x) {
			if (x.getMessage().contains("Premature end of file.")) {
				return new HTTPResponseWrapper<Match>(response, null);
			}
			throw new AbstractOXException(x.getMessage(), x);
		} catch (Exception e) {
			throw new AbstractOXException(e.getMessage(), e);
		}
	}
}
