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

package com.openexchange.html.internal.parser.handler;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.html.internal.parser.HtmlHandler;

/**
 * {@link DumperHTMLHandler} - Used to debug HTML parsing behavior.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DumperHTMLHandler implements HtmlHandler {

    private static final String CRLF = "\r\n";

    private final StringBuilder sb;

    private final StringBuilder html;

    /**
     * Initializes a new {@link DumperHTMLHandler}.
     */
    public DumperHTMLHandler() {
        super();
        sb = new StringBuilder(1024);
        html = new StringBuilder(1024);
    }

    @Override
    public void handleXMLDeclaration(final String version, final Boolean standalone, final String encoding) {
        if (null != version) {
            sb.append(CRLF).append("XML Declaration: Version=").append(version).append(" standalone=").append(standalone).append(
                " encoding=").append(encoding);
            html.append("<?xml version=\"").append(version).append('"');
            if (null != standalone) {
                html.append(" standalone=\"").append(Boolean.TRUE.equals(standalone) ? "yes" : "no").append('"');
            }
            if (null != encoding) {
                html.append(" encoding=\"").append("encoding").append('"');
            }
            html.append("?>");
        }
    }

    @Override
    public void handleComment(final String comment) {
        sb.append(CRLF).append("Comment: ").append(comment);
        html.append("<!--").append(comment).append("-->");
    }

    @Override
    public void handleCDATA(final String text) {
        sb.append(CRLF).append("CDATA: ").append(text);
        html.append("<![CDATA[").append(text).append("]]>");
    }

    @Override
    public void handleDocDeclaration(final String docDecl) {
        sb.append(CRLF).append("DOCTYPE: ").append(docDecl);
        html.append("<!DOCTYPE").append(docDecl).append('>');
    }

    @Override
    public void handleEndTag(final String tag) {
        sb.append(CRLF).append("End Tag: ").append(tag);
        html.append("</").append(tag).append('>');
    }

    @Override
    public void handleError(final String errorMsg) {
        sb.append(CRLF).append("Error: ").append(errorMsg);
    }

    @Override
    public void handleSimpleTag(final String tag, final Map<String, String> attributes) {
        sb.append(CRLF).append("Simple Tag: ").append(tag);
        html.append('<').append(tag);
        final int size = attributes.size();
        final Iterator<Entry<String, String>> iter = attributes.entrySet().iterator();
        for (int i = 0; i < size; i++) {
            final Entry<String, String> e = iter.next();
            sb.append(CRLF).append('\t').append(e.getKey()).append('=').append(e.getValue());
            html.append(' ').append(e.getKey()).append("=\"").append(e.getValue()).append('"');
        }
        html.append("/>");
    }

    @Override
    public void handleStartTag(final String tag, final Map<String, String> attributes) {
        sb.append(CRLF).append("Start Tag: ").append(tag);
        html.append('<').append(tag);
        final int size = attributes.size();
        final Iterator<Entry<String, String>> iter = attributes.entrySet().iterator();
        for (int i = 0; i < size; i++) {
            final Entry<String, String> e = iter.next();
            sb.append(CRLF).append('\t').append(e.getKey()).append('=').append(e.getValue());
            html.append(' ').append(e.getKey()).append("=\"").append(e.getValue()).append('"');
        }
        html.append('>');
    }

    @Override
    public void handleText(final String text, final boolean ignorable) {
        sb.append(CRLF).append("Text: ").append(text);
        html.append(text);
    }

    /**
     * Gets the string
     *
     * @return The string
     */
    public String getString() {
        return sb.toString();
    }

    public String getHTML() {
        return html.toString();
    }
}
