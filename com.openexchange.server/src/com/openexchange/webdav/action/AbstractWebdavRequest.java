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
        if(res != null) {
            return res;
        }
        return res = factory.resolveResource(getUrl());
    }

    @Override
    public WebdavResource getDestination() throws WebdavProtocolException {
        if(null == getDestinationUrl()) {
            return null;
        }
        if(dest != null) {
            return dest;
        }
        return dest = factory.resolveResource(getDestinationUrl());
    }

    @Override
    public WebdavCollection getCollection() throws WebdavProtocolException {
        if(res != null && res.isCollection()) {
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
        if(ifHeader == null) {
            return null;
        }
        return new IfHeaderParser().parse(getHeader("If"));
    }

    @Override
    public int getDepth(final int def){
        final String depth = getHeader("depth");
        if(null == depth) {
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
        if(getHeader("Content-Length") == null) {
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
