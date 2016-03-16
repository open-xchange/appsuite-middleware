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
