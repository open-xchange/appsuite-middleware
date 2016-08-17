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

package com.openexchange.html;

import java.io.Reader;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.html.internal.HtmlServiceImpl;
import com.openexchange.html.osgi.HTMLServiceActivator;

/**
 * {@link SimHtmlService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SimHtmlService implements HtmlService {

    private final HtmlService htmlService;

    /**
     * Initializes a new {@link SimHtmlService}.
     */
    public SimHtmlService() {
        super();
        final Object[] maps = HTMLServiceActivator.getDefaultHTMLEntityMaps();

        @SuppressWarnings("unchecked") final Map<String, Character> htmlEntityMap = (Map<String, Character>) maps[1];
        @SuppressWarnings("unchecked") final Map<Character, String> htmlCharMap = (Map<Character, String>) maps[0];

        htmlEntityMap.put("apos", Character.valueOf('\''));

        htmlService = new HtmlServiceImpl(htmlCharMap, htmlEntityMap);
    }

    @Override
    public String replaceImages(final String content, final String sessionId) {
        return htmlService.replaceImages(content, sessionId);
    }

    @Override
    public String formatURLs(final String content, final String commentId) {
        return htmlService.formatURLs(content, commentId);
    }

    @Override
    public String formatHrefLinks(final String content) {
        return htmlService.formatHrefLinks(content);
    }

    @Override
    public String filterWhitelist(final String htmlContent) {
        return htmlService.filterWhitelist(htmlContent);
    }

    @Override
    public String filterWhitelist(final String htmlContent, final String configName) {
        return htmlService.filterWhitelist(htmlContent, configName);
    }

    @Override
    public String filterExternalImages(final String htmlContent, final boolean[] modified) {
        return htmlService.filterExternalImages(htmlContent, modified);
    }

    @Override
    public String sanitize(final String htmlContent, final String optConfigName, final boolean dropExternalImages, final boolean[] modified, final String cssPrefix) {
        return htmlService.sanitize(htmlContent, optConfigName, dropExternalImages, modified, cssPrefix);
    }

    @Override
    public String extractText(Reader htmlInput) throws OXException {
        return htmlService.extractText(htmlInput);
    }

    @Override
    public String extractText(String htmlContent) throws OXException {
        return htmlService.extractText(htmlContent);
    }

    @Override
    public String html2text(final String htmlContent, final boolean appendHref) {
        return htmlService.html2text(htmlContent, appendHref);
    }

    @Override
    public String htmlFormat(final String plainText, final boolean withQuote, final String commentId) {
        return htmlService.htmlFormat(plainText, withQuote, commentId);
    }

    @Override
    public String htmlFormat(final String plainText, final boolean withQuote) {
        return htmlService.htmlFormat(plainText, withQuote);
    }

    @Override
    public String htmlFormat(final String plainText) {
        return htmlService.htmlFormat(plainText);
    }

    @Override
    public String documentizeContent(final String htmlContent, final String charset) {
        return htmlService.documentizeContent(htmlContent, charset);
    }

    @Override
    public String getConformHTML(final String htmlContent, final String charset) {
        return htmlService.getConformHTML(htmlContent, charset);
    }

    @Override
    public String getConformHTML(final String htmlContent, final String charset, final boolean replaceUrls) {
        return htmlService.getConformHTML(htmlContent, charset, replaceUrls);
    }

    @Override
    public String dropScriptTagsInHeader(final String htmlContent) {
        return htmlService.dropScriptTagsInHeader(htmlContent);
    }

    @Override
    public String getCSSFromHTMLHeader(final String htmlContent) {
        return htmlService.getCSSFromHTMLHeader(htmlContent);
    }

    @Override
    public String checkBaseTag(final String htmlContent, final boolean externalImagesAllowed) {
        return htmlService.checkBaseTag(htmlContent, externalImagesAllowed);
    }

    @Override
    public String prettyPrint(final String htmlContent) {
        return htmlService.prettyPrint(htmlContent);
    }

    @Override
    public String replaceHTMLEntities(final String content) {
        return htmlService.replaceHTMLEntities(content);
    }

    @Override
    public Character getHTMLEntity(final String entity) {
        return htmlService.getHTMLEntity(entity);
    }

    @Override
    public String encodeForHTML(String input) {
        return htmlService.encodeForHTML(input);
    }

    @Override
    public String encodeForHTMLAttribute(String input) {
        return htmlService.encodeForHTMLAttribute(input);
    }

    @Override
    public String encodeForHTML(char[] candidates, String input) {
        return htmlService.encodeForHTML(candidates, input);
    }

    @Override
    public HtmlSanitizeResult sanitize(String htmlContent, String optConfigName, boolean dropExternalImages, boolean[] modified, String cssPrefix, int maxContentSize) {
        return htmlService.sanitize(htmlContent, optConfigName, dropExternalImages, modified, cssPrefix, maxContentSize);
    }

    @Override
    public HtmlSanitizeResult htmlFormat(String plainText, boolean withQuote, String commentId, int maxContentSize) {
        return htmlService.htmlFormat(plainText, withQuote, commentId, maxContentSize);
    }
}
