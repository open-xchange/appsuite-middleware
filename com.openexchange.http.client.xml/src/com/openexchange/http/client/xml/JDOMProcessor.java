package com.openexchange.http.client.xml;

import java.io.InputStream;
import org.jdom2.Document;
import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.http.client.exceptions.OxHttpClientExceptionCodes;
import com.openexchange.xml.jdom.JDOMParser;

public class JDOMProcessor implements HTTPResponseProcessor {

	private final JDOMParser parser;

	public JDOMProcessor(final JDOMParser parser) {
		this.parser = parser;
	}
	
	public Class<?>[] getTypes() {
		return new Class[]{InputStream.class, Document.class};
	}

	public Object process(final Object response)
			throws OXException {
		try {
			return parser.parse((InputStream) response);
		} catch (final Exception x) {
			throw OxHttpClientExceptionCodes.CATCH_ALL.create(x.getMessage(), x);
		}
	}

}
