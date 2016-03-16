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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

public class ServletRequestAdapter implements SimpleRequest {

	private final HttpServletRequest req;
	private final HttpServletResponse res;
	private PrintWriter w;
	private Object body;


	public ServletRequestAdapter(final HttpServletRequest req, final HttpServletResponse res) {
		this.req=req;
		this.res=res;
	}

	@Override
    public String getParameter(final String param) {
		return req.getParameter(param);
	}

	public Writer getWriter() throws IOException {
		if(w==null) {
			w = res.getWriter();
		}
		return w;
	}

	@Override
    public String[] getParameterValues(final String param) {
		return Strings.splitByComma(req.getParameter(param));
	}

	@Override
    public Object getBody() {
		if(null != body) {
			return body;
		}
		try {
			final InputStream input = req.getInputStream();
	        final ByteArrayOutputStream baos = new UnsynchronizedByteArrayOutputStream(
	            input.available());
	        final byte[] buf = new byte[512];
	        int length = -1;
	        while ((length = input.read(buf)) > 0) {
	            baos.write(buf, 0, length);
	        }
	        String characterEncoding = req.getCharacterEncoding();
	        if (null == characterEncoding) {
				characterEncoding=ServerConfig.getProperty(Property.DefaultEncoding); // "UTF-8"
			}
			final String body =  new String(baos.toByteArray(), Charsets.forName(characterEncoding));

			return this.body = new JSONObject("{\"data\": "+body+'}').get("data");
		} catch (final IOException e) {
			return null;
		} catch (final JSONException e) {
			return null;
		}
	}

	@Override
	public String toString(){
		final StringBuilder b = new StringBuilder();
		final Enumeration<?> e = req.getParameterNames();
		while(e.hasMoreElements()) {
			final String name = e.nextElement().toString();
			b.append(" | ");
			b.append(name);
			b.append(" : ");
			b.append(getParameter(name));
			b.append(" | ");
		}
		b.append("BODY: ");
		final Object body = getBody();
		if(null == body) {
			b.append("No body");
		} else {
			b.append(body);
		}
		return b.toString();
	}
}
