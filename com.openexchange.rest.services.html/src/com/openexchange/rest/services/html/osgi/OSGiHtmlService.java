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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.rest.services.html.osgi;

import java.util.NoSuchElementException;
import org.osgi.framework.ServiceReference;
import com.openexchange.html.HtmlService;
import com.openexchange.osgi.ServiceSet;
import com.openexchange.osgi.SimpleRegistryListener;


/**
 * {@link OSGiHtmlService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OSGiHtmlService implements HtmlService, SimpleRegistryListener<HtmlService> {

    private final ServiceSet<HtmlService> allServices;

    /**
     * Initializes a new {@link OSGiHtmlService}.
     */
    public OSGiHtmlService() {
        super();
        allServices = new ServiceSet<HtmlService>();
    }

    @Override
    public void added(ServiceReference<HtmlService> ref, HtmlService service) {
        allServices.added(ref, service);
    }

    @Override
    public void removed(ServiceReference<HtmlService> ref, HtmlService service) {
        allServices.removed(ref, service);
    }

    private HtmlService service() {
        try {
            return allServices.last();
        } catch (NoSuchElementException e) {
            throw new IllegalStateException("Missing HtmlService instance", e);
        }
    }

    @Override
    public String replaceImages(String content, String sessionId) {
        return service().replaceImages(content, sessionId);
    }

    @Override
    public String formatURLs(String content, String commentId) {
        return service().formatURLs(content, commentId);
    }

    @Override
    public String formatHrefLinks(String content) {
        return service().formatHrefLinks(content);
    }

    @Override
    public String filterWhitelist(String htmlContent) {
        return service().filterWhitelist(htmlContent);
    }

    @Override
    public String filterWhitelist(String htmlContent, String configName) {
        return service().filterWhitelist(htmlContent, configName);
    }

    @Override
    public String filterExternalImages(String htmlContent, boolean[] modified) {
        return service().filterExternalImages(htmlContent, modified);
    }

    @Override
    public String sanitize(String htmlContent, String optConfigName, boolean dropExternalImages, boolean[] modified, String cssPrefix) {
        return service().sanitize(htmlContent, optConfigName, dropExternalImages, modified, cssPrefix);
    }

    @Override
    public String html2text(String htmlContent, boolean appendHref) {
        return service().html2text(htmlContent, appendHref);
    }

    @Override
    public String htmlFormat(String plainText, boolean withQuote, String commentId) {
        return service().htmlFormat(plainText, withQuote, commentId);
    }

    @Override
    public String htmlFormat(String plainText, boolean withQuote) {
        return service().htmlFormat(plainText, withQuote);
    }

    @Override
    public String htmlFormat(String plainText) {
        return service().htmlFormat(plainText);
    }

    @Override
    public String documentizeContent(String htmlContent, String charset) {
        return service().documentizeContent(htmlContent, charset);
    }

    @Override
    public String getConformHTML(String htmlContent, String charset) {
        return service().getConformHTML(htmlContent, charset);
    }

    @Override
    public String getConformHTML(String htmlContent, String charset, boolean replaceUrls) {
        return service().getConformHTML(htmlContent, charset, replaceUrls);
    }

    @Override
    public String dropScriptTagsInHeader(String htmlContent) {
        return service().dropScriptTagsInHeader(htmlContent);
    }

    @Override
    public String getCSSFromHTMLHeader(String htmlContent) {
        return service().getCSSFromHTMLHeader(htmlContent);
    }

    @Override
    public String checkBaseTag(String htmlContent, boolean externalImagesAllowed) {
        return service().checkBaseTag(htmlContent, externalImagesAllowed);
    }

    @Override
    public String prettyPrint(String htmlContent) {
        return service().prettyPrint(htmlContent);
    }

    @Override
    public String replaceHTMLEntities(String content) {
        return service().replaceHTMLEntities(content);
    }

    @Override
    public Character getHTMLEntity(String entity) {
        return service().getHTMLEntity(entity);
    }

    @Override
    public String encodeForHTML(String input) {
        return service().encodeForHTML(input);
    }

    @Override
    public String encodeForHTML(char[] candidates, String input) {
        return service().encodeForHTML(candidates, input);
    }

    @Override
    public String encodeForHTMLAttribute(String input) {
        return service().encodeForHTMLAttribute(input);
    }

}
