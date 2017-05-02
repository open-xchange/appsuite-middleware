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

package com.openexchange.html.internal.jsoup.handler;

import static com.openexchange.html.internal.jsoup.handler.CleaningJsoupHandler.checkPossibleURL;
import java.util.Set;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.XmlDeclaration;
import com.openexchange.html.internal.jsoup.JsoupHandler;


/**
 * {@link UrlReplacerJsoupHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class UrlReplacerJsoupHandler implements JsoupHandler {

    private static final Set<String> URI_ATTRS = CleaningJsoupHandler.URI_ATTRS;

    private final StringBuilder urlBuilder;
    private Document document;

    /**
     * Initializes a new {@link UrlReplacerJsoupHandler}.
     */
    public UrlReplacerJsoupHandler() {
        super();
        this.urlBuilder = new StringBuilder(256);
    }

    /**
     * Gets the HTML document
     *
     * @return The HTML document
     */
    public Document getDocument() {
        return document;
    }

    @Override
    public void handleComment(Comment comment) {
        // Ignore
    }

    @Override
    public void handleDataNode(DataNode dataNode) {
        // Ignore
    }

    @Override
    public void handleDocumentType(DocumentType documentType) {
        // Ignore
    }

    @Override
    public void handleTextNode(TextNode textNode) {
        // Ignore
    }

    @Override
    public void handleXmlDeclaration(XmlDeclaration xmlDeclaration) {
        // Ignore
    }

    @Override
    public void handleElementStart(Element element) {
        Attributes attributes = element.attributes();
        for (Attribute attribute : attributes) {
            String attr = attribute.getKey();
            if (URI_ATTRS.contains(attr)) {
                attribute.setValue(checkPossibleURL(attribute.getValue(), urlBuilder));
            }
        }
    }

    @Override
    public void handleElementEnd(Element element) {
        // Ignore
    }

    @Override
    public void finished(Document document) {
        this.document = document;
    }

}
