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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.openexchange.subscribe.crawler.internal.AbstractStep;

/**
 * This step takes a page and returns all pages linked from this page (as HTMLAnchors) via links, also from subpages, that fulfill a regular
 * expression
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class AnchorsByLinkRegexStep extends AbstractStep<List<HtmlAnchor>, HtmlPage>{

    private String linkRegex;

    private String subpageLinkRegex;

    private ArrayList<String> subpagesHref;

    private final ArrayList<String> outputHref, uniqueIds;

    private ArrayList<HtmlPage> subpages;

    private String identifyingCriteria;

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AnchorsByLinkRegexStep.class);

    private boolean mayHaveEmptyOutput;

    public AnchorsByLinkRegexStep() {
        this(null, null, null, "", false);
    }

    public AnchorsByLinkRegexStep(final String description, final String subpageLinkRegex, final String linkRegex) {
        this(description, subpageLinkRegex, linkRegex, "", false);
    }

    public AnchorsByLinkRegexStep(final String description, final String subpageLinkRegex, final String linkRegex, boolean mayHaveEmptyOutput) {
        this(description, subpageLinkRegex, linkRegex, "", mayHaveEmptyOutput);
    }

    public AnchorsByLinkRegexStep(final String description, final String subpageLinkRegex, final String linkRegex, final String identifyingCriteria) {
        this(description, subpageLinkRegex, linkRegex, identifyingCriteria, false);
    }

    public AnchorsByLinkRegexStep(final String description, final String subpageLinkRegex, final String linkRegex, final String identifyingCriteria, boolean mayHaveEmptyOutput) {
        super();
        this.description = description;
        this.subpageLinkRegex = subpageLinkRegex;
        this.linkRegex = linkRegex;
        this.mayHaveEmptyOutput = mayHaveEmptyOutput;
        subpagesHref = new ArrayList<String>();
        outputHref = new ArrayList<String>();
        uniqueIds = new ArrayList<String>();
        subpages = new ArrayList<HtmlPage>();
        output = new ArrayList<HtmlAnchor>();
        this.identifyingCriteria = identifyingCriteria;
    }

    @Override
    public void execute(final WebClient webClient) {
        try {
            // add the first page as there should always be results there
            subpages.add(input);
            LOG.debug("Input page is : {}", input.getWebResponse().getContentAsString());
            // search for subpages
            for (final HtmlAnchor link : input.getAnchors()) {
                // get the subpages
                if (link.getHrefAttribute().matches(subpageLinkRegex)) {
                    if (!subpagesHref.contains(link.getHrefAttribute())) {
                        // remember this link is already noted
                        subpagesHref.add(link.getHrefAttribute());
                        // remember its page for later
                        subpages.add((HtmlPage) link.click());
                        LOG.debug("Subpage added : {}", link.getHrefAttribute());
                    }

                }
            }
            // traverse the subpages
            for (final HtmlPage subpage : subpages) {
                for (final HtmlAnchor possibleLinkToResultpage : subpage.getAnchors()) {
                    // get the result pages
                    if (possibleLinkToResultpage.getHrefAttribute().matches(linkRegex) && !outputHref.contains(possibleLinkToResultpage.getHrefAttribute())) {
                        if (identifyingCriteria.equals("")){
                            output.add(possibleLinkToResultpage);
                            outputHref.add(possibleLinkToResultpage.getHrefAttribute());
                            LOG.debug("Added this link to the list : {}", possibleLinkToResultpage.getHrefAttribute());
                        // if differentiating by href alone is not enough to prevent double links
                        } else {
                            final Pattern pattern = Pattern.compile(identifyingCriteria);
                            final Matcher matcher = pattern.matcher(possibleLinkToResultpage.getHrefAttribute());
                            if (matcher.matches()) {
                                final String uniqueId = matcher.group(1);
                                if (!uniqueIds.contains(uniqueId) && uniqueId!=null) {
                                    output.add(possibleLinkToResultpage);
                                    outputHref.add(possibleLinkToResultpage.getHrefAttribute());
                                    uniqueIds.add(uniqueId);
                                    LOG.debug("Added this link to the list : {}", possibleLinkToResultpage.getHrefAttribute());
                                }
                            }
                        }

                    }
                }
            }
            if (output != null && (output.size() != 0 || mayHaveEmptyOutput)) {
                executedSuccessfully = true;
            } else {
                LOG.error("No links matching the criteria were found.");
                LOG.info(input.getWebResponse().getContentAsString());
                for (HtmlAnchor link : input.getAnchors()){
                    LOG.info("Link available on the first page : {}", link.getHrefAttribute());
                }
            }
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

    public String getSubpageLinkRegex() {
        return subpageLinkRegex;
    }

    public void setSubpageLinkRegex(final String subpageLinkRegex) {
        this.subpageLinkRegex = subpageLinkRegex;
    }

    public ArrayList<String> getSubpagesHref() {
        return subpagesHref;
    }

    public void setSubpagesHref(final ArrayList<String> subpagesHref) {
        this.subpagesHref = subpagesHref;
    }

    public ArrayList<HtmlPage> getSubpages() {
        return subpages;
    }

    public void setSubpages(final ArrayList<HtmlPage> subpages) {
        this.subpages = subpages;
    }

    public String getLinkRegex() {
        return linkRegex;
    }

    public void setLinkRegex(final String linkRegex) {
        this.linkRegex = linkRegex;
    }


    public String getIdentifyingCriteria() {
        return identifyingCriteria;
    }


    public void setIdentifyingCriteria(final String identifyingCriteria) {
        this.identifyingCriteria = identifyingCriteria;
    }

    public boolean isMayHaveEmptyOutput() {
        return mayHaveEmptyOutput;
    }

    public void setMayHaveEmptyOutput(boolean mayHaveEmptyOutput) {
        this.mayHaveEmptyOutput = mayHaveEmptyOutput;
    }
}
