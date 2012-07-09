package com.openexchange.conversion.simple.impl;

import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.ResultConverter.Quality;
import com.openexchange.conversion.simple.SimpleConverter;
import com.openexchange.conversion.simple.SimplePayloadConverter;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

public class PayloadConverterAdapter implements ResultConverter{

	private SimplePayloadConverter converter;
	private SimpleConverter rtConverter;
	
	public PayloadConverterAdapter(SimplePayloadConverter converter, SimpleConverter rtConverter) {
		this.converter = converter;
		this.rtConverter = rtConverter;
	}
	
	public String getInputFormat() {
		return converter.getInputFormat();
	}

	public String getOutputFormat() {
		return converter.getOutputFormat();
	}

	public ResultConverter.Quality getQuality() {
		switch(converter.getQuality()) {
		case GOOD: return ResultConverter.Quality.GOOD;
		case BAD: return ResultConverter.Quality.BAD;
		}
		return ResultConverter.Quality.BAD;
	}

	public void convert(AJAXRequestData requestData, AJAXRequestResult result,
			ServerSession session, Converter converter) throws OXException {
		
		Object converted = this.converter.convert(result.getResultObject(), session, this.rtConverter);
		
		result.setResultObject(converted,  getOutputFormat());
		
	}

}