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

package com.openexchange.webdav.action;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import com.openexchange.java.Streams;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.webdav.action.ifheader.IfHeader;
import com.openexchange.webdav.action.ifheader.IfHeaderParseException;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.xml.jdom.JDOMParser;

public class ReplayWebdavRequest implements WebdavRequest{

	private final WebdavRequest delegate;
	private byte[] body;

	/**
	 * Initializes a new {@link ReplayWebdavRequest}.
	 *
	 * @param req The delegate request
	 */
	public ReplayWebdavRequest(final WebdavRequest req) {
		this.delegate = req;
	}

	@Override
    public InputStream getBody() throws IOException {
	    if (null == body) {
	        body = Streams.stream2bytes(delegate.getBody());
	    }
	    return Streams.newByteArrayInputStream(body);
	}

	@Override
    public Document getBodyAsDocument() throws JDOMException, IOException {
        return ServerServiceRegistry.getInstance().getService(JDOMParser.class).parse(getBody());
	}

	@Override
    public WebdavCollection getCollection() throws WebdavProtocolException {
		return delegate.getCollection();
	}

	@Override
    public WebdavResource getDestination() throws WebdavProtocolException {
		return delegate.getDestination();
	}

	@Override
    public WebdavPath getDestinationUrl() {
		return delegate.getDestinationUrl();
	}

	@Override
    public String getHeader(final String header) {
		return delegate.getHeader(header);
	}

	@Override
    public List<String> getHeaderNames() {
		return delegate.getHeaderNames();
	}

	@Override
    public IfHeader getIfHeader() throws IfHeaderParseException {
		return delegate.getIfHeader();
	}

	@Override
    public WebdavResource getResource() throws WebdavProtocolException {
		return delegate.getResource();
	}

	@Override
    public WebdavPath getUrl() {
		return delegate.getUrl();
	}

	@Override
    public String getURLPrefix() {
		return delegate.getURLPrefix();
	}

	@Override
    public int getDepth(final int depth) {
		return delegate.getDepth(depth);
	}

	@Override
    public WebdavFactory getFactory() throws WebdavProtocolException {
		return delegate.getFactory();
	}

	@Override
    public String getCharset() {
		return delegate.getCharset();
	}

	@Override
    public boolean hasBody() {
	    return delegate.hasBody();
	}

	@Override
    public Map<String, Object> getUserInfo() {
	    return delegate.getUserInfo();
	}

    @Override
    public boolean isBrief() {
        return delegate.isBrief();
    }

    @Override
    public String getParameter(String name) {
        return delegate.getParameter(name);
    }

}
