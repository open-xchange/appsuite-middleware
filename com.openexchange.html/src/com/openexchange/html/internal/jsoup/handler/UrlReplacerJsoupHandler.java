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
