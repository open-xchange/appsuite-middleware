package com.openexchange.ajax.requesthandler.converters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

import edu.emory.mathcs.backport.java.util.Collections;

public class BasicTypeAPIResponseConverter implements ResultConverter {

	public static final List<ResultConverter> CONVERTERS = Collections.unmodifiableList(new ArrayList<ResultConverter>() {{
		add(new BasicTypeAPIResponseConverter("string"));
		add(new BasicTypeAPIResponseConverter("int"));
		add(new BasicTypeAPIResponseConverter("float"));
		add(new BasicTypeAPIResponseConverter("boolean"));
		add(new BasicTypeAPIResponseConverter("json"));

	}});

	private String inputFormat;

	private BasicTypeAPIResponseConverter(String inputFormat) {
		this.inputFormat = inputFormat;
	}

	@Override
	public String getInputFormat() {
		return inputFormat;
	}

	@Override
	public String getOutputFormat() {
		return "apiResponse";
	}

	@Override
	public Quality getQuality() {
		return Quality.GOOD;
	}

	@Override
	public void convert(AJAXRequestData request, AJAXRequestResult result,
			ServerSession session, Converter converter) throws OXException {
		final Response response = new Response(session);
        response.setData(result.getResultObject());
        response.setTimestamp(result.getTimestamp());
        final Collection<OXException> warnings = result.getWarnings();
        if (null != warnings && !warnings.isEmpty()) {
            for (final OXException warning : warnings) {
                response.addWarning(warning);
            }
        }
        result.setResultObject(response);
	}

}
