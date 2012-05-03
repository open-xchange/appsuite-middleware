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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.webdav.action;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavPath;

public class ServletWebdavRequest extends AbstractWebdavRequest implements WebdavRequest {
	private final HttpServletRequest req;
	private String urlPrefix;
	private final WebdavPath url;
	private WebdavPath destUrl;

	private final ApacheURLDecoder decoder = new ApacheURLDecoder();

	private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ServletWebdavRequest.class));

	public ServletWebdavRequest(final WebdavFactory factory, final HttpServletRequest req) {
		super(factory);
		this.req = req;
		final StringBuilder builder = new StringBuilder();
		builder.append(req.getServletPath());
		builder.append('/');
		this.urlPrefix = builder.toString();
		LOG.debug("WEBDAV URL PREFIX FROM CONTAINER: "+this.urlPrefix);
        this.url = toWebdavURL(req.getRequestURI());
	}

	@Override
    public InputStream getBody() throws IOException {
		return req.getInputStream();
	}

	@Override
    public String getHeader(final String header) {
		return req.getHeader(header);
	}

	@Override
    public List<String> getHeaderNames() {
		final List<String> headers = new ArrayList<String>();
		final Enumeration enumeration = req.getHeaderNames();
		while(enumeration.hasMoreElements()) {
			headers.add(enumeration.nextElement().toString());
		}
		return headers;
	}

	@Override
    public String getURLPrefix() {
		return urlPrefix;
	}

    public void setUrlPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

	@Override
    public WebdavPath getUrl() {
		return url;
	}

	@Override
    public WebdavPath getDestinationUrl() {
		if(destUrl != null) {
			return destUrl;
		}

		return destUrl = toWebdavURL(req.getHeader("destination"));
	}

	protected WebdavPath toWebdavURL(String url) {
		if(url == null) {
			return null;
		}

		try {
			final URL urlO = new URL(url);
			url = urlO.getPath();
		} catch (final MalformedURLException x ){
			LOG.debug("",x);
		}

		if(url.startsWith(req.getServletPath())) {
			url =  url.substring(req.getServletPath().length());
		}
		try {
            final String encoding = req.getCharacterEncoding() == null ? ServerConfig.getProperty(Property.DefaultEncoding) : req.getCharacterEncoding();
            final WebdavPath path = new WebdavPath();
            for(final String component : url.split("/+")) {
                if(component.equals("")){
                    continue;
                }

                path.append(decode(component,encoding));
            }
            return path;
		} catch (final UnsupportedEncodingException e) {
			return new WebdavPath(url);
		}
	}

    public String decode(final String component, final String encoding) throws UnsupportedEncodingException {
        return decoder.decode(component, encoding);
    }

    @Override
    public String getCharset() {
		return req.getCharacterEncoding() == null ? ServerConfig.getProperty(Property.DefaultEncoding) : req.getCharacterEncoding();
	}

}
