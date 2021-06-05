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
import com.openexchange.webdav.action.ifheader.IfHeader;
import com.openexchange.webdav.action.ifheader.IfHeaderParseException;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

public interface WebdavRequest {

	WebdavResource getResource() throws WebdavProtocolException;

	WebdavCollection getCollection() throws WebdavProtocolException;

	WebdavPath getUrl();

	List<String> getHeaderNames();

	String getHeader(String header);

	InputStream getBody() throws IOException;

	Document getBodyAsDocument() throws JDOMException, IOException;

	String getURLPrefix();

	IfHeader getIfHeader() throws IfHeaderParseException;

	WebdavResource getDestination() throws WebdavProtocolException;

	WebdavPath getDestinationUrl();

	int getDepth(int def);

	WebdavFactory getFactory() throws WebdavProtocolException;

	String getCharset();

    boolean hasBody();

    Map<String, Object> getUserInfo();

    /**
     * Gets a value indicating whether the WebDAV <code>Brief</code> header was present in the request and set to <code>t</code>, or,
     * if the <code>Prefer</code> header was present and set to <code>return=minimal</code>.
     * <p/>
     * The WebDAV Brief header is used to reduce the verbosity of DAV responses by omitting portions of the response that may be implied
     * by the absence of a response. The Brief header may be used in the PROPFIND Method, the BPROPFIND Method, the PROPPATCH Method, and
     * the BPROPPATCH Method. Using it in any other method will have no effect.
     *<p/>
     * https://tools.ietf.org/html/draft-murchison-webdav-prefer
     * @return <code>true</code> if the header was set to <code>t</code>, <code>false</code>, otherwise
     */
    boolean isBrief();

    /**
     * Gets the value of a specific request parameter, as supplied by the underlying servlet request.
     *
     * @param name The name of the parameter to get
     * @return The parameter value, or <code>null</code> if not specified in the underlying request
     */
    String getParameter(String name);

}
