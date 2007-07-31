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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONWriter;

import com.openexchange.ajax.container.Response;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.tools.exceptions.LoggingLogic;

public abstract class CommonRequest {
	
	protected JSONWriter w;
	
	private final Log LOG = LogFactory.getLog(CommonRequest.class);
	private final LoggingLogic LL = LoggingLogic.getLoggingLogic(CommonRequest.class, LOG);
	
	public CommonRequest(final JSONWriter w) {
		this.w = w;
	}
	
	protected void sendErrorAsJS(final String error, final String...errorParams) {
		//final JSONObject response = new JSONObject();
		try {
			w.object();
			w.key("error").value(error);
			w.key("error_params").value(new JSONArray(Arrays.asList(errorParams)));
			w.endArray();
			/*response.put("error",error);
			final JSONArray arr = new JSONArray(Arrays.asList(errorParams));
			response.put("error_params",arr);
			w.value(response);*/
		} catch (final JSONException e) {
			LOG.debug(e.getMessage(),e);
		}
	}
	
	protected void handle(final Throwable t) {
		final Response res = new Response();
		if(t instanceof AbstractOXException) {
			LL.log((AbstractOXException) t);
			res.setException((AbstractOXException) t);
		} else {
			res.setException(new AbstractOXException(t));
		}
		try {
			Response.write(res, w);
		} catch (final JSONException e) {
			LOG.error("",t);
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
