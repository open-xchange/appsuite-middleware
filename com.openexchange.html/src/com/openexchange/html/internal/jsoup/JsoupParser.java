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

import java.util.Map;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.nodes.XmlDeclaration;
import org.jsoup.parser.InterruptedParsingException;
import org.jsoup.parser.InterruptibleHtmlTreeBuilder;
import org.jsoup.parser.ParseErrorList;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;
import com.google.common.collect.ImmutableMap;
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

        // Run as a monitored task
        new JsoupParseTask(html, handler, timeout, this).call();
    }

    /**
     * Parses specified real-life HTML document and delegates events to given instance of {@link HtmlHandler}
     *
     * @param html The real-life HTML document
     * @param handler The handler
     * @throws OXException If specified HTML content cannot be parsed
     */
    public void doParse(String html, JsoupHandler handler) throws OXException {
        try {
            // Parse HTML input to a Jsoup document
            InterruptibleHtmlTreeBuilder treeBuilder = new InterruptibleHtmlTreeBuilder();
            Document document = treeBuilder.parse(html, "", ParseErrorList.noTracking(), treeBuilder.defaultSettings());

            // Check <style> tag sizes against threshold
            {
                int cssThreshold = HtmlServices.cssThreshold();
                if (cssThreshold > 0) {
                    Elements styleTags = document.getElementsByTag("style");
                    for (Element styleTag : styleTags) {
                        if (styleTag.html().length() > cssThreshold) {
                            styleTag.remove();
                        }
                    }
                }
            }

            // Traverse document, giving call-backs to specified handler
            document.traverse(new InterruptibleJsoupNodeVisitor(handler));
            handler.finished(document);
        } catch (InterruptedParsingException e) {
            throw HtmlExceptionCodes.PARSING_FAILED.create("Parser timeout.", e);
        } catch (StackOverflowError parserOverflow) {
            throw HtmlExceptionCodes.PARSING_FAILED.create("Parser overflow detected.", parserOverflow);
        }
    }

    private static final class InterruptibleJsoupNodeVisitor implements NodeVisitor {

        private static interface Invoker {

            void invoke(Node node, JsoupHandler handler);
        }

        private static final Map<Class<?>, Invoker> CALLS = ImmutableMap.<Class<?>, Invoker> builder()
            .put(Element.class, new Invoker() {

                @Override
                public void invoke(Node node, JsoupHandler handler) {
                    handler.handleElementStart((Element) node);
                }
            })
            .put(FormElement.class, new Invoker() {

                @Override
                public void invoke(Node node, JsoupHandler handler) {
                    handler.handleElementStart((Element) node);
                }
            })
            .put(TextNode.class, new Invoker() {

                @Override
                public void invoke(Node node, JsoupHandler handler) {
                    handler.handleTextNode((TextNode) node);
                }
            })
            .put(Comment.class, new Invoker() {

                @Override
                public void invoke(Node node, JsoupHandler handler) {
                    handler.handleComment((Comment) node);
                }
            })
            .put(DataNode.class, new Invoker() {

                @Override
                public void invoke(Node node, JsoupHandler handler) {
                    handler.handleDataNode((DataNode) node);
                }
            })
            .put(DocumentType.class, new Invoker() {

                @Override
                public void invoke(Node node, JsoupHandler handler) {
                    handler.handleDocumentType((DocumentType) node);
                }
            })
            .put(Document.class, new Invoker() {

                @Override
                public void invoke(Node node, JsoupHandler handler) {
                    // Nothing
                }
            })
            .put(XmlDeclaration.class, new Invoker() {

                @Override
                public void invoke(Node node, JsoupHandler handler) {
                    handler.handleXmlDeclaration((XmlDeclaration) node);
                }
            })
            .build();

        private final JsoupHandler handler;

        InterruptibleJsoupNodeVisitor(JsoupHandler handler) {
            super();
            this.handler = handler;
        }

        @Override
        public void head(Node node, int depth) {
            if (Thread.interrupted()) { // clears flag if set
                throw new InterruptedParsingException();
            }

            Invoker invoker = CALLS.get(node.getClass());
            if (null != invoker) {
                invoker.invoke(node, handler);
            } else {
                LOG.warn("Unexpected node: {}", node.getClass().getName());
            }
        }

        @Override
        public void tail(Node node, int depth) {
            if (Thread.interrupted()) { // clears flag if set
                throw new InterruptedParsingException();
            }
            if (node instanceof Element) {
                Element element = (Element) node;
                handler.handleElementEnd(element);
            }
        }
    }
}
