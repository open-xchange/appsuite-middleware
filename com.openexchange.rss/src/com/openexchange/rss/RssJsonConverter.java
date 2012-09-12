package com.openexchange.rss;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

public class RssJsonConverter implements ResultConverter {

	@Override
	public String getInputFormat() {
		return "rss";
	}

	@Override
	public String getOutputFormat() {
		return "json";
	}

	@Override
	public Quality getQuality() {
		return Quality.GOOD;
	}

	@Override
	public void convert(AJAXRequestData requestData, AJAXRequestResult result,
			ServerSession session, Converter converter) throws OXException {
		JSONArray jArr = new JSONArray();
		Object resultObject = result.getResultObject();
		if (resultObject instanceof List) {
			for(RssResult rss: (List<RssResult>) resultObject) {
				jArr.put(conv(rss));
			}
		} else if (resultObject instanceof List) {
			jArr.put(conv((RssResult) resultObject));
		} else {
			//TODO: be unhappy
		}
		result.setResultObject(jArr, "json");

	}

	private JSONObject conv(RssResult result) throws OXException{
		try {
			return new JSONObject()
				.put("author", result.getAuthor())
				.put("body", result.getBody())
				.put("format", result.getFormat())
				.put("url", result.getUrl())
				.put("feedUrl", result.getFeedUrl())
				.put("feedTitle", result.getFeedTitle())
				.put("subject", result.getSubject())
				.put("image", result.getImageUrl())
				.put("date", result.getDate().getTime());
		} catch (JSONException e) {
			throw AjaxExceptionCodes.JSON_ERROR.create(e);
		}
	}

}
