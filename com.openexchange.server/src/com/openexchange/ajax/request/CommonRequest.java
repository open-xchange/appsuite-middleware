/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
		} catch (final JSONException e) {
			LOG.debug("",e);
		}
	}

	protected void handle(final Throwable t, final Session session) {
		final Response res = new Response();
		if(t instanceof OXException) {
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
		} catch (final JSONException e) {
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
			if(req.getParameter(param) == null) {
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
