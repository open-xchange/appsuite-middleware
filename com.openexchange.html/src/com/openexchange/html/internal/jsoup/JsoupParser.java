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

package com.openexchange.html.internal.jsoup;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.XmlDeclaration;
import org.jsoup.select.NodeVisitor;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlExceptionCodes;
import com.openexchange.html.HtmlServices;
import com.openexchange.html.internal.jsoup.control.JsoupParseControl;
import com.openexchange.html.internal.jsoup.control.JsoupParseControlTask;
import com.openexchange.html.internal.jsoup.control.JsoupParseTask;
import com.openexchange.html.internal.parser.HtmlHandler;
import com.openexchange.html.services.ServiceRegistry;

/**
 * {@link JsoupParser}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class JsoupParser {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JsoupParser.class);

    private static final JsoupParser INSTANCE = new JsoupParser();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static JsoupParser getInstance() {
        return INSTANCE;
    }

    /**
     * Shuts-down the parser.
     */
    public static void shutDown() {
        INSTANCE.stop();
    }

    // -----------------------------------------------------------------------------------------------------------------------

    private final int htmlParseTimeoutSec;
    private final Thread controlRunner;

    /**
     * Initializes a new {@link JsoupParser}.
     */
    private JsoupParser() {
        super();
        ConfigurationService service = ServiceRegistry.getInstance().getService(ConfigurationService.class);
        int defaultValue = 10;
        htmlParseTimeoutSec = null == service ? defaultValue : service.getIntProperty("com.openexchange.html.parse.timeout", defaultValue);
        if (htmlParseTimeoutSec > 0) {
            controlRunner = new Thread(new JsoupParseControlTask(), "JsoupControl");
            controlRunner.start();
        } else {
            controlRunner = null;
        }
    }

    /**
     * Stops this parser.
     */
    private void stop() {
        if (htmlParseTimeoutSec > 0) {
            JsoupParseControl.getInstance().add(JsoupParseTask.POISON);
        }
        Thread controlRunner = this.controlRunner;
        if (null != controlRunner) {
            controlRunner.interrupt();
        }
    }

    /**
     * Parses specified real-life HTML document and delegates events to given instance of {@link HtmlHandler}
     *
     * @param html The real-life HTML document
     * @param handler The handler
     * @throws OXException If specified HTML content cannot be parsed
     */
    public void parse(String html, JsoupHandler handler, boolean checkSize) throws OXException {
        // Check size
        int maxLength = checkSize ? HtmlServices.htmlThreshold() : 0;
        if (maxLength > 0 && html.length() > maxLength) {
            LOG.info("HTML content is too big: max. '{}', but is '{}'.", maxLength, html.length());
            throw HtmlExceptionCodes.TOO_BIG.create(maxLength, html.length());
        }

        int timeout = htmlParseTimeoutSec;
        if (timeout <= 0) {
            doParse(html, handler);
            return;
        }

        // TODO: Run as a task
        doParse(html, handler);
    }

    /**
     * Parses specified real-life HTML document and delegates events to given instance of {@link HtmlHandler}
     *
     * @param html The real-life HTML document
     * @param handler The handler
     * @throws OXException If specified HTML content cannot be parsed
     */
    public void doParse(String html, JsoupHandler handler) throws OXException {
        Document document = Jsoup.parse(html);
        document.traverse(new JsoupNodeVisitor(handler));
    }

    private static final class JsoupNodeVisitor implements NodeVisitor {

        private final JsoupHandler handler;

        JsoupNodeVisitor(JsoupHandler handler) {
            super();
            this.handler = handler;
        }

        @Override
        public void head(Node node, int depth) {
            if (node instanceof Element) {
                Element element = (Element) node;
                handler.handleElementStart(element);
            } else if (node instanceof TextNode) {
                TextNode textNode = (TextNode) node;
                handler.handleTextNode(textNode);
            } else if (node instanceof Comment) {
                Comment comment = (Comment) node;
                handler.handleComment(comment);
            } else if (node instanceof DataNode) {
                DataNode dataNode = (DataNode) node;
                handler.handleDataNode(dataNode);
            } else if (node instanceof DocumentType) {
                DocumentType documentType = (DocumentType) node;
                handler.handleDocumentType(documentType);
            } else if (node instanceof Document) {
                // Ignore root
            } else if (node instanceof XmlDeclaration) {
                XmlDeclaration xmlDeclaration = (XmlDeclaration) node;
                handler.handleXmlDeclaration(xmlDeclaration);
            }
        }

        @Override
        public void tail(Node node, int depth) {
            if (node instanceof Element) {
                Element element = (Element) node;
                handler.handleElementEnd(element);
            }
        }
    }
}
