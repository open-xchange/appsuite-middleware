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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.file.storage.owncloud.internal;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.webdav.client.WebDAVXmlBody;

/**
 * {@link NextCloudSearchBody} defines a body for a search request against nextcloud
 *
 * <p>
 * This will create a search request like the following:
 * <pre>
 * {@code
 * <xml>
 *   <?xml version="1.0" encoding="UTF-8" standalone="no"?>
 *   <d:searchrequest xmlns:d="DAV:" xmlns:oc="http://owncloud.org/ns">
 *   <d:basicsearch>
 *       <d:select>
 *           <d:prop>
 *               <oc:fileid/>
 *           </d:prop>
 *       </d:select>
 *       <d:from>
 *           <d:scope>
 *               <d:href>/files/admin/</d:href>
 *               <d:depth>infinity</d:depth>
 *           </d:scope>
 *       </d:from>
 *       <d:where>
 *           <d:like>
 *               <d:prop>
 *                   <d:displayname/>
 *               </d:prop>
 *               <d:literal>%e%</d:literal>
 *           </d:like>
 *       </d:where>
 *       <d:orderby/>
 *  </d:basicsearch>
 *  </d:searchrequest>
 * </xml>
 * </pre>
 * }
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.4
 */
public class NextCloudSearchBody extends WebDAVXmlBody {

    private static final String DAV_NS = "DAV:";
    private static final String OWNCLOUD_NS = "http://owncloud.org/ns";
    private final String folder;
    private final String pattern;

    /**
     * Initializes a new {@link NextCloudSearchBody}.
     *
     * @param folder the folder to search in
     * @param pattern The pattern to search, this will match the displayname
     */
    public NextCloudSearchBody(String folder, String pattern) {
        this.folder = folder;
        this.pattern = pattern;
    }

    @Override
    public Element toXML() throws OXException {
        try {
            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            final Document doc = docBuilder.newDocument();

            Element searchRequest = doc.createElementNS(DAV_NS, "d:searchrequest");
            searchRequest.setAttribute("xmlns:oc", OWNCLOUD_NS);
            doc.appendChild(searchRequest);

            Element basicSearch = doc.createElement("d:basicsearch");
            searchRequest.appendChild(basicSearch);

            Element select = doc.createElement("d:select");
            basicSearch.appendChild(select);

            Element prop = doc.createElement("d:prop");
            select.appendChild(prop);

            prop.appendChild(doc.createElement("oc:fileid"));

            Element from = doc.createElement("d:from");
            basicSearch.appendChild(from);

            Element scope = doc.createElement("d:scope");
            from.appendChild(scope);

            Element href = doc.createElement("d:href");
            href.appendChild(doc.createTextNode(folder));
            scope.appendChild(href);

            Element depth = doc.createElement("d:depth");
            depth.appendChild(doc.createTextNode("infinity"));
            scope.appendChild(depth);

            Element where = doc.createElement("d:where");
            basicSearch.appendChild(where);

            Element like = doc.createElement("d:like");
            where.appendChild(like);

            Element propInLike = doc.createElement("d:prop");
            like.appendChild(propInLike);

            Element displayName = doc.createElement("d:displayname");
            propInLike.appendChild(displayName);

            Element literal = doc.createElement("d:literal");
            literal.appendChild(doc.createTextNode("%" + pattern + "%"));
            like.appendChild(literal);

            Element orderBy = doc.createElement("d:orderby");
            basicSearch.appendChild(orderBy);

            return doc.getDocumentElement();
        } catch (ParserConfigurationException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage(), e);
        }
    }
}
