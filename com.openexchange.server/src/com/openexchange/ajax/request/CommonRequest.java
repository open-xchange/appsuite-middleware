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

package com.openexchange.ajax.request;

import java.util.Arrays;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONWriter;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

public abstract class CommonRequest {

	protected JSONWriter w;

	private final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CommonRequest.class);

	public CommonRequest(final JSONWriter w) {
		this.w = w;
	}

    private static Locale localeFrom(final Session session) throws OXException {
        if (null == session) {
            return Locale.US;
        }
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUser().getLocale();
        }
        return UserStorage.getInstance().getUser(session.getUserId(), session.getContextId()).getLocale();
    }

	protected void sendErrorAsJS(final String error, final String...errorParams) {
		//final JSONObject response = new JSONObject();
		try {
			w.object();
			w.key(ResponseFields.ERROR).value(error);
			w.key(ResponseFields.ERROR_PARAMS).value(new JSONArray(Arrays.asList(errorParams)));
			w.endArray();
			/*response.put(ERROR,error);
			final JSONArray arr = new JSONArray(Arrays.asList(errorParams));
			response.put("error_params",arr);
			w.value(response);*/
		} catch (JSONException e) {
			LOG.debug("",e);
		}
	}

	protected void handle(final Throwable t, final Session session) {
		final Response res = new Response();
		if (t instanceof OXException) {
		    final OXException e = (OXException) t;
            switch (e.getCategories().get(0).getLogLevel()) {
                case TRACE:
                    LOG.trace("", e);
                    break;
                case DEBUG:
                    LOG.debug("", e);
                    break;
                case INFO:
                    LOG.info("", e);
                    break;
                case WARNING:
                    LOG.warn("", e);
                    break;
                case ERROR:
                    LOG.error("", e);
                    break;
                default:
                    break;
            }
		    res.setException(e);
		} else {
            LOG.error("", t);
            res.setException(new OXException(t));
		}
		try {
			ResponseWriter.write(res, w, localeFrom(session));
		} catch (JSONException e) {
			LOG.error("", t);
		} catch (OXException e) {
		    LOG.error("", e);
        }
	}

	protected void invalidParameter(final String parameter, final String value) {
		sendErrorAsJS("Invalid parameter value '%s' for parameter %s",value,parameter);
	}

	protected void unknownColumn(final String columnId) {
		sendErrorAsJS("Unknown column id: %s",columnId);
	}

	protected boolean checkRequired(final SimpleRequest req, final String action, final String ...parameters) {
		for(final String param : parameters) {
			if (req.getParameter(param) == null) {
				missingParameter(param,action);
				return false;
			}
		}
		return true;
	}

	protected void missingParameter(final String parameter, final String action) {
		sendErrorAsJS("Missing Parameter: %s for action: %s",parameter,action);

	}

}
