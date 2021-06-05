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

package com.openexchange.file.storage.owncloud.internal;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.webdav.client.WebDAVXmlBody;
import com.openexchange.xml.util.XMLUtils;

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
            final DocumentBuilderFactory docFactory = XMLUtils.safeDbf(DocumentBuilderFactory.newInstance());
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
