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
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
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
