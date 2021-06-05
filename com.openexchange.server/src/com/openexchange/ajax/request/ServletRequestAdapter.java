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
		if (w==null) {
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
		if (null != body) {
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
		} catch (IOException e) {
			return null;
		} catch (JSONException e) {
			return null;
		}
	}

	@Override
	public String toString(){
		final StringBuilder b = new StringBuilder();
		final Enumeration<?> e = req.getParameterNames();
		while (e.hasMoreElements()) {
			final String name = e.nextElement().toString();
			b.append(" | ");
			b.append(name);
			b.append(" : ");
			b.append(getParameter(name));
			b.append(" | ");
		}
		b.append("BODY: ");
		final Object body = getBody();
		if (null == body) {
			b.append("No body");
		} else {
			b.append(body);
		}
		return b.toString();
	}
}
