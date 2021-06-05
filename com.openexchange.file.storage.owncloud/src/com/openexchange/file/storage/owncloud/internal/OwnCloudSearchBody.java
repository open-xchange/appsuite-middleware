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

import java.util.Set;
import javax.xml.namespace.QName;
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
 * {@link OwnCloudSearchBody}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class OwnCloudSearchBody extends WebDAVXmlBody {

    /**
     * The OwnCloudSearchBody.java.
     */
    private static final String OWNCLOUD_NS = "http://owncloud.org/ns";
    private static final String DAV_NS = "DAV:";
    private final String term;
    private final long start;
    private final long end;
    private final Set<QName> fields;

    /**
     * Initializes a new {@link OwnCloudSearchBody}.
     * @param term The search term
     * @param start The start value for pagination
     * @param end The end value for pagination
     * @param fields The fields to query
     */
    public OwnCloudSearchBody(String term, long start, long end, Set<QName> fields) {
        super();
        this.term = term;
        this.start = start;
        this.end = end;
        this.fields = fields;
    }

    @Override
    public Element toXML() throws OXException {
        DocumentBuilderFactory docFactory = XMLUtils.safeDbf(DocumentBuilderFactory.newInstance());;
        DocumentBuilder docBuilder;
        try {
            docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            Element root = doc.createElementNS(OWNCLOUD_NS, "oc:search-files");
            doc.appendChild(root);

            Element search = doc.createElementNS(OWNCLOUD_NS, "oc:search");
            root.appendChild(search);

            addFields(doc, root);

            if (start >= 0 && end > 0 && start<end) {
                Element limit = doc.createElementNS(OWNCLOUD_NS, "oc:limit");
                limit.setTextContent(String.valueOf(end-start));
                search.appendChild(limit);
                Element offset = doc.createElementNS(OWNCLOUD_NS, "oc:offset");
                offset.setTextContent(String.valueOf(start));
                search.appendChild(offset);
            }

            Element pattern = doc.createElementNS(OWNCLOUD_NS, "oc:pattern");
            pattern.setTextContent(term);
            search.appendChild(pattern);
            return doc.getDocumentElement();
        } catch (ParserConfigurationException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage(), e);
        }
    }

    /**
     * Adds the qname fields to the xml document
     *
     * @param doc The document
     * @param root The root element of the document
     */
    private void addFields(Document doc, Element root) {

        if (fields != null) {
            // Add properties to query
            Element props = doc.createElementNS(DAV_NS, "a:prop");
            root.appendChild(props);
            Element prop;
            for (QName qname : fields) {
                prop = doc.createElementNS(qname.getNamespaceURI(), qname.getPrefix()+":"+ qname.getLocalPart());
                props.appendChild(prop);
            }
        }
    }
}
