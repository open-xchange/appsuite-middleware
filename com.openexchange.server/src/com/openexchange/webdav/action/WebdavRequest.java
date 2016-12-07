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
