/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */
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

/**
 * {@link RssJsonConverter} - Converts <code>"rss"</code> to <code>"json"</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
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
		JSONArray jArr;

		Object resultObject = result.getResultObject();
		if (resultObject instanceof List) {
		    @SuppressWarnings("unchecked") List<RssResult> list = (List<RssResult>) resultObject;
		    jArr = new JSONArray(list.size());
            for(RssResult rss: list) {
				jArr.put(conv(rss));
			}
		} else if (resultObject instanceof RssResult) {
		    jArr = new JSONArray(1);
			jArr.put(conv((RssResult) resultObject));
		} else {
			throw AjaxExceptionCodes.UNSUPPORTED_FORMAT.create(resultObject == null ? "null" : resultObject.getClass().getSimpleName());
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
				.put("date", result.getDate().getTime())
			    .put("droppedImages", result.hasDroppedExternalImages());
		} catch (JSONException e) {
			throw AjaxExceptionCodes.JSON_ERROR.create(e);
		}
	}

}
