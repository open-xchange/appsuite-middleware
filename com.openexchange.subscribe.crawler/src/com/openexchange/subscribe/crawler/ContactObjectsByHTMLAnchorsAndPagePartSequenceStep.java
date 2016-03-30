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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang.StringEscapeUtils;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.subscribe.crawler.internal.AbstractStep;
import com.openexchange.subscribe.crawler.internal.ContactSanitizer;
import com.openexchange.subscribe.crawler.internal.Mappings;
import com.openexchange.subscribe.crawler.internal.PagePartSequence;

/**
 * This step takes HtmlPages that each contain contact information and converts them to ContactObjects for OX
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class ContactObjectsByHTMLAnchorsAndPagePartSequenceStep extends AbstractStep<Contact[], List<HtmlAnchor>> {

    private static final ContactSanitizer SANITIZER = new ContactSanitizer();

    private PagePartSequence pageParts;

    private String titleExceptionsRegex, linkToTargetPage;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContactObjectsByHTMLAnchorsAndPagePartSequenceStep.class);

    private boolean addPagesTogether;

    public ContactObjectsByHTMLAnchorsAndPagePartSequenceStep(final String description, final PagePartSequence pageParts) {
        this.description = description;
        this.pageParts = pageParts;
        titleExceptionsRegex = "";
        linkToTargetPage = "";
    }

    public ContactObjectsByHTMLAnchorsAndPagePartSequenceStep(final String description, final PagePartSequence pageParts, final String titleExceptionsRegex, final String linkToTargetPage) {
        this.description = description;
        this.pageParts = pageParts;
        this.titleExceptionsRegex = titleExceptionsRegex;
        this.linkToTargetPage = linkToTargetPage;
    }

    public ContactObjectsByHTMLAnchorsAndPagePartSequenceStep(final String description, final PagePartSequence pageParts, final String titleExceptionsRegex, final String linkToTargetPage, boolean addPagesTogether) {
        this.description = description;
        this.pageParts = pageParts;
        this.titleExceptionsRegex = titleExceptionsRegex;
        this.linkToTargetPage = linkToTargetPage;
        this.addPagesTogether = addPagesTogether;
    }

    public ContactObjectsByHTMLAnchorsAndPagePartSequenceStep() {
        titleExceptionsRegex = "";
        linkToTargetPage = "";
    }

    @Override
    public void execute(final WebClient webClient) throws OXException {
        final List<Contact> contactObjects = new ArrayList<Contact>();
        // final OXContainerConverter oxContainerConverter = new OXContainerConverter((TimeZone) null, (String) null);
        HtmlPage debugPage = null;
        for (final HtmlAnchor anchor : input) {
            try {
                HtmlPage page = anchor.click();
                String additionalPageString = "";
                if (isDebuggingEnabled()){
                    debugPage = page;
                }
                // in case the reached page is not yet the one with (all) the contact info and there is one more link to click
                if (!linkToTargetPage.equals("")){
                   final PageByLinkRegexStep step = new PageByLinkRegexStep("", linkToTargetPage);
                   step.setInput(page);
                   step.execute(webClient);
                   // sometimes additional Information is on a linked page. If that is the case this subpage needs to be added
                   if (! addPagesTogether){
                       page = step.getOutput();
                   } else {
                       additionalPageString = step.getOutput().getWebResponse().getContentAsString();
                   }
                }
                final String titleText = page.getTitleText();
                if (null != titleText && !titleText.matches(titleExceptionsRegex)){
                    String pageAsString = page.getWebResponse().getContentAsString() + additionalPageString;
                    final String pageString = StringEscapeUtils.unescapeHtml(pageAsString);
                    pageParts.setPage(pageString);
                    LOG.debug("Page evaluated is : {}", pageString);
                    final HashMap<String, String> map = pageParts.retrieveInformation();

                    final Contact contact = Mappings.translateMapToContact(map);

                    SANITIZER.sanitize(contact);
                    contactObjects.add(contact);
                }

            } catch (final OXException e) {
                LOG.error("{} for Context : {}, User : {}, Folder : {}.", e.getMessage(), workflow.getSubscription().getContext().getContextId(), workflow.getSubscription().getUserId(), workflow.getSubscription().getFolderId());
                exception = e;
            } catch (final IOException e) {
                exception = e;
            }
            executedSuccessfully = true;
        }

        if (input == null || input.isEmpty()){
            executedSuccessfully = true;
        }

        output = contactObjects.toArray(new Contact[contactObjects.size()]);

    }

    public static ContactSanitizer getSANITIZER() {
        return SANITIZER;
    }

    public PagePartSequence getPageParts() {
        return pageParts;
    }

    public void setPageParts(final PagePartSequence pageParts) {
        this.pageParts = pageParts;
    }


    public String getTitleExceptionsRegex() {
        return titleExceptionsRegex;
    }


    public void setTitleExceptionsRegex(final String titleExceptionsRegex) {
        this.titleExceptionsRegex = titleExceptionsRegex;
    }


    public String getLinkToTargetPage() {
        return linkToTargetPage;
    }


    public void setLinkToTargetPage(final String linkToTargetPage) {
        this.linkToTargetPage = linkToTargetPage;
    }


    public boolean isAddPagesTogether() {
        return addPagesTogether;
    }


    public void setAddPagesTogether(boolean addPagesTogether) {
        this.addPagesTogether = addPagesTogether;
    }



}
