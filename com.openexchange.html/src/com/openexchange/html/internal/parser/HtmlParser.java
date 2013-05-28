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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.html.internal.parser;

import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import com.openexchange.java.Strings;

/**
 * {@link HtmlParser} - Parses a well-formed HTML document based on {@link XmlPullParser}. The corresponding events are delegated to a given
 * instance of {@link HtmlHandler}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HtmlParser {

    private static final String PROPERTY_XMLDECL_STANDALONE = "http://xmlpull.org/v1/doc/features.html#xmldecl-standalone";

    private static final String PROPERTY_XMLDECL_VERSION = "http://xmlpull.org/v1/doc/properties.html#xmldecl-version";

    private static final String FEATURE_RELAXED = "http://xmlpull.org/v1/doc/features.html#relaxed";

    private static final String FEATURE_PRESERVE_TEXT = "open-xchange.org/preserveText";

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.loggerFor(HtmlParser.class);
    private static final boolean DEBUG = LOG.isDebugEnabled();

    private static final int INT_IS_EMPTY_TAG = 1;

    /**
     * Initializes a new {@link HtmlParser}
     */
    private HtmlParser() {
        super();
    }

    /**
     * Parses specified well-formed HTML document and delegates events to given instance of {@link HtmlHandler}
     *
     * @param html The well-formed HTML document
     * @param handler The HTML handler
     */
    public static void parse(final String html, final HtmlHandler handler) {
        final XmlPullParser parser = new KXmlParser();
        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            // parser.setFeature(FEATURE_PRESERVE_TEXT, true);
            parser.setFeature(FEATURE_RELAXED, true);
            parser.setInput(new StringReader(html));
            int event = XmlPullParser.END_DOCUMENT;
            int[] holderForStartAndLength = null;
            final StringBuilder textBuilder = new StringBuilder(512);
            final StringBuilder cdataBuilder = new StringBuilder(512);
            int prevEvent = XmlPullParser.END_DOCUMENT;
            int[] depthInfo = new int[8];
            boolean ignoreWhitespace = false;
            boolean obtainXMLDecl = true;
            while ((event = parser.nextToken()) != XmlPullParser.END_DOCUMENT) {
                if (obtainXMLDecl) {
                    final Object version = parser.getProperty(PROPERTY_XMLDECL_VERSION);
                    if (null != version) {
                        final Object standalone = parser.getProperty(PROPERTY_XMLDECL_STANDALONE);
                        handler.handleXMLDeclaration(
                            version.toString(),
                            standalone == null ? null : (Boolean) standalone,
                            parser.getInputEncoding());
                    }
                    obtainXMLDecl = false;
                }
                if (event == XmlPullParser.CDSECT) {
                    /*
                     * Gather subsequent text inside CDATA (ex. 'fo<o' from <!CDATA[fo<o]]>)
                     */
                    if (prevEvent != XmlPullParser.CDSECT && textBuilder.length() > 0) {
                        handler.handleText(textBuilder.toString(), ignoreWhitespace);
                        textBuilder.setLength(0);
                    }
                    prevEvent = XmlPullParser.CDSECT;
                    cdataBuilder.append(parser.getText());
                } else if (event == XmlPullParser.TEXT) {
                    if (prevEvent != XmlPullParser.TEXT && cdataBuilder.length() > 0) {
                        handler.handleCDATA(cdataBuilder.toString());
                        cdataBuilder.setLength(0);
                    }
                    /*
                     * Gather subsequent text inside element's content
                     */
                    prevEvent = XmlPullParser.TEXT;
                    if (!parser.isWhitespace()) {
                        ignoreWhitespace = false;
                    }
                    textBuilder.append(parser.getText());
                } else if (event == XmlPullParser.ENTITY_REF) {
                    /*
                     * Entity reference, such as "&amp;"
                     */
                    if (null == holderForStartAndLength) {
                        holderForStartAndLength = new int[2];
                    }
                    ignoreWhitespace = false;
                    if (prevEvent == XmlPullParser.TEXT) {
                        final char[] textCharacters = parser.getTextCharacters(holderForStartAndLength);
                        if (null != textCharacters) {
                            textBuilder.append('&').append(textCharacters).append(';');
                        }
                    } else if (prevEvent == XmlPullParser.CDSECT) {
                        cdataBuilder.append('&').append(parser.getTextCharacters(holderForStartAndLength)).append(';');
                    } else {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Unexpected entity occurring inside non-text and non-CDATA area");
                        }
                    }
                } else {
                    /*
                     * Check for text/CDATA
                     */
                    if (textBuilder.length() > 0) {
                        handler.handleText(textBuilder.toString(), ignoreWhitespace);
                        textBuilder.setLength(0);
                    }
                    if (cdataBuilder.length() > 0) {
                        handler.handleCDATA(cdataBuilder.toString());
                        textBuilder.setLength(0);
                    }
                    /*
                     * Handle non-text event
                     */
                    if (event == XmlPullParser.COMMENT) {
                        handler.handleComment(parser.getText());
                    } else if (event == XmlPullParser.DOCDECL) {
                        handler.handleDocDeclaration(parser.getText());
                    } else if (event == XmlPullParser.END_TAG) {
                        if ((depthInfo[parser.getDepth()] & INT_IS_EMPTY_TAG) == 0) {
                            handler.handleEndTag(parser.getName());
                        }
                        ignoreWhitespace = true;
                    } else if (event == XmlPullParser.IGNORABLE_WHITESPACE) {
                        // Ignore
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("IGNORABLE_WHITESPACE: " + parser.getText());
                        }
                    } else if (event == XmlPullParser.PROCESSING_INSTRUCTION) {
                        // Ignore
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("PROCESSING_INSTRUCTION: " + parser.getText());
                        }
                    } else if (event == XmlPullParser.START_DOCUMENT) {
                        /*
                         * Cannot occur since initial nextToken() has already been invoked
                         */
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("START_DOCUMENT: " + parser.getText());
                        }
                    } else if (event == XmlPullParser.START_TAG) {
                        final Map<String, String> attributes = new HashMap<String, String>();
                        final int count = parser.getAttributeCount();
                        for (int i = 0; i < count; i++) {
                            attributes.put(parser.getAttributeName(i), parser.getAttributeValue(i));
                        }
                        final int depth = parser.getDepth();
                        if (depth >= depthInfo.length) {
                            /*
                             * Increase array
                             */
                            final int[] tmp = depthInfo;
                            depthInfo = new int[tmp.length << 1];
                            System.arraycopy(tmp, 0, depthInfo, 0, tmp.length);
                        }
                        if (parser.isEmptyElementTag()) {
                            depthInfo[depth] = (depthInfo[depth] | INT_IS_EMPTY_TAG);
                            handler.handleSimpleTag(parser.getName(), attributes);
                        } else {
                            depthInfo[depth] = (depthInfo[depth] & ~INT_IS_EMPTY_TAG);
                            handler.handleStartTag(parser.getName(), attributes);
                        }
                        ignoreWhitespace = true;
                    } else {
                        handler.handleError(new StringBuilder(32).append("Unknown event type: ").append(event).toString());
                    }
                }
            }
        } catch (final XmlPullParserException e) {
            LOG.error(composeErrorMessage(e, DEBUG, html), e);
            handler.handleError(e.getMessage());
        } catch (final IOException e) {
            LOG.error(composeErrorMessage(e, DEBUG, html), e);
            handler.handleError(e.getMessage());
        } catch (final RuntimeException e) {
            LOG.error(composeErrorMessage(e, DEBUG, html), e);
            handler.handleError(e.getMessage());
        }
    }

    private static final String ERR01 = "Parsing of HTML content failed: ";

    private static String composeErrorMessage(final Exception e, final boolean appendHtml, final String html) {
        final StringBuilder sb = new StringBuilder(html.length() + 256);
        sb.append(ERR01);
        sb.append(e.getMessage());
        if (appendHtml) {
            sb.append(". Affected HTML content:\n");
            dumpHtml(html, sb);
        }
        return sb.toString();
    }

    private static void dumpHtml(final String html, final StringBuilder sb) {
        final String[] lines = Strings.splitByCRLF(html);
        final DecimalFormat df = new DecimalFormat("0000");
        int count = 1;
        for (final String line : lines) {
            sb.append(df.format(count++)).append(' ').append(line).append("\r\n");
        }
    }

}
