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
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.openexchange.exception.OXException;
import com.openexchange.subscribe.crawler.internal.AbstractStep;


/**
 * {@link AnchorsByLinkXPathStep}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class AnchorsByLinkXPathStep extends AbstractStep<List<HtmlAnchor>, HtmlPage> {
    private String linkRegex;

    private String subpageLinkRegex;

    private ArrayList<String> subpagesHref;

    private final ArrayList<String> outputHref;

    private ArrayList<HtmlPage> subpages;

    private String xpath;

    private int intervalStart;
    private int intervalStop;

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AnchorsByLinkXPathStep.class);

    public AnchorsByLinkXPathStep() {
        subpagesHref = new ArrayList<String>();
        outputHref = new ArrayList<String>();
        subpages = new ArrayList<HtmlPage>();
        output = new ArrayList<HtmlAnchor>();
    }

    public AnchorsByLinkXPathStep(final String description, final String subpageLinkRegex, final String xpath, final int intervalStart, final int intervalStop) {
        this.description = description;
        this.subpageLinkRegex = subpageLinkRegex;
        this.xpath = xpath;
        this.intervalStart = intervalStart;
        this.intervalStop = intervalStop;

        subpagesHref = new ArrayList<String>();
        outputHref = new ArrayList<String>();
        subpages = new ArrayList<HtmlPage>();
        output = new ArrayList<HtmlAnchor>();
    }



    @Override
    public void execute(final WebClient webClient) throws OXException {
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
                for (int i=intervalStart ; i<= intervalStop; i++){
                    System.out.println("Start : " + intervalStart + ", Stop : "+intervalStop);
                    HtmlAnchor possibleLinkToResultpage = null;
                    String currentXPath;
                    if (i == 0){
                        currentXPath = xpath.replace("[REPLACE_THIS]", "");

                    } else {
                        currentXPath = xpath.replace("REPLACE_THIS", Integer.toString(i));
                    }
                    if ( subpage.getByXPath(currentXPath) != null && subpage.getByXPath(currentXPath).size() != 0){
                        possibleLinkToResultpage = (HtmlAnchor) subpage.getByXPath(currentXPath).get(0);
                    }

                    if (possibleLinkToResultpage != null && !outputHref.contains(possibleLinkToResultpage.getHrefAttribute())) {

                            output.add(possibleLinkToResultpage);
                            outputHref.add(possibleLinkToResultpage.getHrefAttribute());
                            LOG.info("Added this link to the list : {}", possibleLinkToResultpage.getHrefAttribute());

                    }
                }

            }
            if (output != null && output.size() != 0){
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


    public String getXpath() {
        return xpath;
    }


    public void setXpath(String xpath) {
        this.xpath = xpath;
    }


    public int getIntervalStart() {
        return intervalStart;
    }


    public void setIntervalStart(int intervalStart) {
        this.intervalStart = intervalStart;
    }


    public int getIntervalStop() {
        return intervalStop;
    }


    public void setIntervalStop(int intervalStop) {
        this.intervalStop = intervalStop;
    }



}
