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

package com.openexchange.subscribe.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.openexchange.exception.OXException;
import com.openexchange.subscribe.crawler.internal.AbstractStep;

/**
 * This step takes a pattern of url and offset and returns all pages reachable by links meeting the specified criteria
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class TextPagesByLinkStep extends AbstractStep<List<TextPage>, HtmlPage>{

    private String urlBeforeOffset, urlAfterOffset, linkpart;

    private int offset;

    public TextPagesByLinkStep() {
        output = new ArrayList<TextPage>();
    }

    public TextPagesByLinkStep(final String description, final String urlBeforeOffset, final int offset, final String urlAfterOffset, final String linkpart) {
        this.description = description;
        this.urlBeforeOffset = urlBeforeOffset;
        this.urlAfterOffset = urlAfterOffset;
        this.offset = offset;
        this.linkpart = linkpart;
        output = new ArrayList<TextPage>();
    }

    @Override
    public void execute(final WebClient webClient) throws OXException {
        try {

            int tempOffset = 0;
            boolean oneSuccess = true;
            Set<String> hrefGuard = new HashSet<String>();
            while (oneSuccess) {
                oneSuccess = false;
                final HtmlPage tempPage = webClient.getPage(urlBeforeOffset + Integer.toString(tempOffset) + urlAfterOffset);
                final List<HtmlAnchor> allLinks = tempPage.getAnchors();
                for (final HtmlAnchor link : allLinks) {
                    if (link.getHrefAttribute().startsWith(linkpart)) {
                    	if (!hrefGuard.add(link.getHrefAttribute())) {
                    		continue;
                    	}
                    	oneSuccess = true;
                        final TextPage tempTextPage = link.click();
                        output.add(tempTextPage);
                    }
                }
                tempOffset += offset;
            }
            executedSuccessfully = true;

        } catch (final FailingHttpStatusCodeException e) {
            exception = e;
        } catch (final MalformedURLException e) {
            exception = e;
        } catch (final IOException e) {
            exception = e;
        }

    }

    @Override
    public boolean executedSuccessfully() {
        return executedSuccessfully;
    }

    @Override
    public Exception getException() {
        return exception;
    }

    public String getUrlBeforeOffset() {
        return urlBeforeOffset;
    }

    public String getUrlAfterOffset() {
        return urlAfterOffset;
    }

    public String getLinkpart() {
        return linkpart;
    }

    public int getOffset() {
        return offset;
    }

    public void setUrlBeforeOffset(final String urlBeforeOffset) {
        this.urlBeforeOffset = urlBeforeOffset;
    }

    public void setUrlAfterOffset(final String urlAfterOffset) {
        this.urlAfterOffset = urlAfterOffset;
    }

    public void setLinkpart(final String linkpart) {
        this.linkpart = linkpart;
    }

    public void setOffset(final int offset) {
        this.offset = offset;
    }

}
