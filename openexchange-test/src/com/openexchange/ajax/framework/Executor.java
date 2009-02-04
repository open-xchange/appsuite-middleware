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

package com.openexchange.ajax.framework;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import junit.framework.Assert;

import org.json.JSONException;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AJAXRequest.FieldParameter;
import com.openexchange.ajax.framework.AJAXRequest.FileParameter;
import com.openexchange.ajax.framework.AJAXRequest.Method;
import com.openexchange.ajax.framework.AJAXRequest.Parameter;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.tools.URLParameter;
import com.openexchange.tools.servlet.AjaxException;

public class Executor extends Assert {

    /**
     * To use character encoding.
     */
    private static final String ENCODING = "UTF-8";

    /**
     * Prevent instantiation
     */
    private Executor() {
        super();
    }

    public static <T extends AbstractAJAXResponse> T execute(final AJAXClient client,
        final AJAXRequest<T> request) throws AjaxException, IOException,
        SAXException, JSONException {
        return execute(client.getSession(), request);
    }

    public static <T extends AbstractAJAXResponse> T execute(final AJAXSession session,
        final AJAXRequest<T> request) throws AjaxException, IOException,
        SAXException, JSONException {
		return execute(session, request,
            AJAXConfig.getProperty(Property.PROTOCOL),
            AJAXConfig.getProperty(Property.HOSTNAME));
	}

    public static <T extends AbstractAJAXResponse> T execute(final AJAXSession session,
        final AJAXRequest<T> request, final String hostname) throws AjaxException,
        IOException, SAXException, JSONException {
        return execute(session, request, AJAXConfig
            .getProperty(Property.PROTOCOL), hostname);
    }

	public static <T extends AbstractAJAXResponse> T execute(final AJAXSession session, final AJAXRequest<T> request,
			final String protocol, final String hostname) throws AjaxException, IOException, SAXException,
			JSONException {

		final String urlString = protocol + "://" + hostname + request.getServletPath();
		final WebRequest req;
		switch (request.getMethod()) {
		case GET:
			req = new GetMethodWebRequest(urlString);
			addParameter(req, session, request);
			break;
		case POST:
			req = new PostMethodWebRequest(urlString);
			addParameter(req, session, request);
			break;
		case UPLOAD:
			final PostMethodWebRequest post = new PostMethodWebRequest(urlString + getPUTParameter(session, request), true);
			req = post;
			addFieldParameter(post, request);
			addFileParameter(post, request);
			break;
		case PUT:
			req = new PutMethodWebRequest(urlString + getPUTParameter(session, request), createBody(request.getBody()),
					AJAXServlet.CONTENTTYPE_JAVASCRIPT);
			break;
		default:
			throw new AjaxException(AjaxException.Code.InvalidParameter, request.getMethod().name());
		}
		final WebConversation conv = session.getConversation();
		final WebResponse resp;
		// The upload returns a web page that should not be interpreted.
		final long startRequest = System.currentTimeMillis();
		// Doing only getResource does not handle cookie setting.
		if (Method.UPLOAD == request.getMethod()) {
			resp = conv.getResource(req);
		} else {
			resp = conv.getResponse(req);
		}
		final long requestDuration = System.currentTimeMillis() - startRequest;
		final AbstractAJAXParser<T> parser = request.getParser();
		parser.checkResponse(resp);
		final long startParse = System.currentTimeMillis();
		final T retval = parser.parse(resp.getText());
		final long parseDuration = System.currentTimeMillis() - startParse;
		retval.setRequestDuration(requestDuration);
		retval.setParseDuration(parseDuration);
		return retval;
	}

	public static WebResponse execute4Download(final AJAXSession session, final AJAXRequest<?> request,
			final String protocol, final String hostname) throws AjaxException, IOException {
		final String urlString = protocol + "://" + hostname + request.getServletPath();
		final WebRequest req;
		switch (request.getMethod()) {
		case GET:
			req = new GetMethodWebRequest(urlString);
			addParameter(req, session, request);
			break;
		default:
			throw new AjaxException(AjaxException.Code.InvalidParameter, request.getMethod().name());
		}
		final WebConversation conv = session.getConversation();
		final WebResponse resp;
		// The upload returns a web page that should not be interpreted.
		// final long startRequest = System.currentTimeMillis();
		resp = conv.getResource(req);
		//final long requestDuration = System.currentTimeMillis() - startRequest;
		return resp;
	}

    private static void addParameter(final WebRequest req,
        final AJAXSession session, final AJAXRequest<?> request) {
        if (null != session.getId()) {
            req.setParameter(AJAXServlet.PARAMETER_SESSION, session.getId());
        }
        for (final Parameter param : request.getParameters()) {
            if (!(param instanceof FileParameter)) {
                req.setParameter(param.getName(), param.getValue());
            }
        }
    }

    private static void addFieldParameter(final PostMethodWebRequest post, final AJAXRequest<?> request) {
		for (final Parameter param : request.getParameters()) {
			if (param instanceof FieldParameter) {
				final FieldParameter fparam = (FieldParameter) param;
				post.setParameter(fparam.getFieldName(), fparam.getFieldContent());
			}
		}
	}

    private static void addFileParameter(final PostMethodWebRequest post,
        final AJAXRequest<?> request) {
        for (final Parameter param : request.getParameters()) {
            if (param instanceof FileParameter) {
                final FileParameter fparam = (FileParameter) param;
                post.selectFile(fparam.getName(), fparam.getFileName(),
                    fparam.getInputStream(), fparam.getMimeType());
            }
        }
    }

    private static String getPUTParameter(final AJAXSession session,
        final AJAXRequest<?> request) throws UnsupportedEncodingException {
        final URLParameter parameter = new URLParameter();
        if (null != session.getId()) {
            parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session
                .getId());
        }
        for (final Parameter param : request.getParameters()) {
            if (!(param instanceof FileParameter) && !(param instanceof FieldParameter)) {
                parameter.setParameter(param.getName(), param.getValue());
            }
        }
        return parameter.getURLParameters();
    }

    private static InputStream createBody(final Object body)
        throws UnsupportedEncodingException {
        return new ByteArrayInputStream(body.toString().getBytes(ENCODING));
    }
}
