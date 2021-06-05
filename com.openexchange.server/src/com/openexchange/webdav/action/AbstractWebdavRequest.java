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
import java.util.HashMap;
import java.util.Map;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.webdav.action.ifheader.IfHeader;
import com.openexchange.webdav.action.ifheader.IfHeaderParseException;
import com.openexchange.webdav.action.ifheader.IfHeaderParser;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.xml.jdom.JDOMParser;

public abstract class AbstractWebdavRequest implements WebdavRequest {
    private WebdavResource res;
    private WebdavResource dest;
    private final WebdavFactory factory;
    private final Map<String, Object> userInfo = new HashMap<String, Object>();
    private Document bodyDocument;

    public AbstractWebdavRequest(final WebdavFactory factory) {
        this.factory = factory;
    }

    @Override
    public WebdavResource getResource() throws WebdavProtocolException {
        if (res != null) {
            return res;
        }
        return res = factory.resolveResource(getUrl());
    }

    @Override
    public WebdavResource getDestination() throws WebdavProtocolException {
        if (null == getDestinationUrl()) {
            return null;
        }
        if (dest != null) {
            return dest;
        }
        return dest = factory.resolveResource(getDestinationUrl());
    }

    @Override
    public WebdavCollection getCollection() throws WebdavProtocolException {
        if (res != null && res.isCollection()) {
            return (WebdavCollection) res;
        }
        return (WebdavCollection) (res = factory.resolveCollection(getUrl()));
    }

    @Override
    public Document getBodyAsDocument() throws JDOMException, IOException {
        if (bodyDocument != null) {
            return bodyDocument;
        }
        return bodyDocument = ServerServiceRegistry.getInstance().getService(JDOMParser.class).parse(getBody());
    }

    @Override
    public IfHeader getIfHeader() throws IfHeaderParseException {
        final String ifHeader = getHeader("If");
        if (ifHeader == null) {
            return null;
        }
        return new IfHeaderParser().parse(getHeader("If"));
    }

    @Override
    public int getDepth(final int def){
        final String depth = getHeader("depth");
        if (null == depth) {
            return def;
        }
        return "Infinity".equalsIgnoreCase(depth) ? WebdavCollection.INFINITY : Integer.parseInt(depth);
    }

    @Override
    public WebdavFactory getFactory(){
        return factory;
    }

    @Override
    public boolean hasBody() {
        if (getHeader("Content-Length") == null) {
            return false;
        }
        int length;
        try {
            length = Integer.parseInt(getHeader("Content-Length"));
        } catch (NumberFormatException e) {
            length = -1;
        }
        return length > 0;
    }

    @Override
    public Map<String, Object> getUserInfo() {
        return userInfo;
    }

    @Override
    public boolean isBrief() {
        return "t".equals(getHeader("Brief")) || "return=minimal".equals(getHeader("Prefer"));
    }

}
