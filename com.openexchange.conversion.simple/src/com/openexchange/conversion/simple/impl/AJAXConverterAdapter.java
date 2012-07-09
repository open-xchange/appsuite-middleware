package com.openexchange.conversion.simple.impl;

import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.conversion.simple.SimpleConverter;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

public class AJAXConverterAdapter implements SimpleConverter{

	private Converter converter;

	public AJAXConverterAdapter(Converter converter) {
		this.converter = converter;
	}
	
	public Object convert(String from, String to, Object data,
			ServerSession session) throws OXException {
		AJAXRequestResult result = new AJAXRequestResult(data, from);
		converter.convert(from, to, new AJAXRequestData(), result, session);
		return result.getResultObject();
	}

}