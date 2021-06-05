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
