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
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.openexchange.exception.OXException;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.crawler.internal.AbstractStep;

/**
 * This Step gets a page reachable via Url in the current context (WebClient)
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class PageByUrlStep extends AbstractStep<HtmlPage, Object> {

    private String url;

    private Exception exception;

    private boolean executedSuccessfully;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PageByUrlStep.class);

    public PageByUrlStep() {

    }

    public PageByUrlStep(final String description, final String url) {
        this.description = description;
        this.url = url;
    }

    @Override
    public void execute(final WebClient webClient) throws OXException {
        try {
            Object object = webClient.getPage(url);
            final HtmlPage pageByUrl = (HtmlPage) object;
            output = pageByUrl;
            LOG.debug("Page : {}", pageByUrl.getWebResponse().getContentAsString());
            if (debuggingEnabled){
                LOG.info("Page : {}", pageByUrl.getWebResponse().getContentAsString());
                openPageInBrowser(output);
            }
            executedSuccessfully = true;
        } catch (final FailingHttpStatusCodeException e) {
            throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(e);
        } catch (final MalformedURLException e) {
            throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(e);
        } catch (final IOException e) {
            throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(e);
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

    @Override
    public void setInput(final Object input) {
        // this needs to do nothing
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public boolean isExecutedSuccessfully() {
        return executedSuccessfully;
    }

    public void setExecutedSuccessfully(final boolean executedSuccessfully) {
        this.executedSuccessfully = executedSuccessfully;
    }

    public void setException(final Exception exception) {
        this.exception = exception;
    }

}
