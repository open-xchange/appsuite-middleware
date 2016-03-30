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

package com.openexchange.subscribe.crawler.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This describes a sequence of ->PageParts to unequivocally identify information (e.g. a contact\u00b4s name) in a webpages sourcecode. To
 * identify a particular bit of information two factors are used: - Its place in the sequence (e.g. in the page\u00b4s sourcecode the last
 * name is listed after the first name) - The sourcecode immediately surrounding it. There are two kinds of page parts: - Fillers, only used
 * to make the sequence unequivocal and containing a single-capture-group regex identifiyng them - Infos, containing a three-capture-group
 * regex (immediately before, relevant part, immediately after)
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class PagePartSequence {

    private ArrayList<PagePart> pageParts;

    private String page, limit;

    public PagePartSequence() {

    }

    public PagePartSequence(final ArrayList<PagePart> pageParts, final String page) {
        this.pageParts = pageParts;
        this.page = page;
    }

    /**
     * This includes the parameter "limit" as a way to separate the input into parts beforehand that each will become a contact later on.
     * This is by far easier to do than using regular expressions to make sure that a specific bit of content belongs to one object and not
     * another. Initializes a new {@link PagePartSequence}.
     *
     * @param pageParts
     * @param page
     * @param limit
     */
    public PagePartSequence(final ArrayList<PagePart> pageParts, final String page, String limit) {
        this.pageParts = pageParts;
        this.page = page;
        this.limit = limit;
    }

    public HashMap<String, String> retrieveInformation() {
        final HashMap<String, String> retrievedInformation = new HashMap<String, String>();
        extractInformationOnce(retrievedInformation);

        return retrievedInformation;
    }

    public Collection<HashMap<String, String>> retrieveMultipleInformation() {
        HashMap<String, String> retrievedInformation = null;
        ArrayList<HashMap<String, String>> multiple = new ArrayList<HashMap<String, String>>();

        final Pattern pattern = Pattern.compile(pageParts.get(1).getRegex());
        Matcher matcher = pattern.matcher(page);
        while (matcher.find()) {
            retrievedInformation = new HashMap<String, String>();
            extractInformationOnce(retrievedInformation);
            multiple.add(retrievedInformation);
        }

        return multiple;
    }

    public Collection<HashMap<String, String>> retrieveMultipleInformationConstrainedByLimit() {
        HashMap<String, String> retrievedInformation = null;
        ArrayList<HashMap<String, String>> multiple = new ArrayList<HashMap<String, String>>();

        while (page.indexOf(limit) != -1) {
            String substring = page.substring(0, page.indexOf(limit) + limit.length());
            String restOfPage = page.substring(page.indexOf(limit) + limit.length());
            page = substring;
            retrievedInformation = new HashMap<String, String>();
            extractInformationOnce(retrievedInformation);
            multiple.add(retrievedInformation);
            page = restOfPage;
        }

        return multiple;
    }

    private void extractInformationOnce(final HashMap<String, String> retrievedInformation) {
        for (final PagePart pagePart : pageParts) {
            final Pattern pattern = Pattern.compile(pagePart.getRegex());
            final Matcher matcher = pattern.matcher(page);
            // find out if the part matches the remaining page
            if (matcher.find()) {
                final int indexOfPageRest = matcher.end();
                // if it is an info-part and its info is not empty get its info and put it into the map
                if (pagePart.getType() == PagePart.INFO && matcher.groupCount() == 3) {
                    final String info = matcher.group(2);
                    if (!info.equals("") && pagePart.getAddInfo() == 0) {
                        retrievedInformation.put(pagePart.getTypeOfInfo(), info);
                    } else if (!info.equals("") && pagePart.getAddInfo() != 0 && pagePart.getAddInfo() <= 3 && pagePart.getAddInfo() >= 1) {
                        retrievedInformation.put(pagePart.getTypeOfInfo(), info + " (" + matcher.group(pagePart.getAddInfo()) + ")");
                    }
                }
                // throw away the part of the String that was just matched or leave it
                if (!pagePart.isKeepStringAfterMatching()) {
                    // set the page to the rest (after this part)
                    page = page.substring(indexOfPageRest);
                }
            }
        }
    }

    public ArrayList<PagePart> getPageParts() {
        return pageParts;
    }

    public void setPageParts(final ArrayList<PagePart> pageParts) {
        this.pageParts = pageParts;
    }

    public String getPage() {
        return page;
    }

    public void setPage(final String page) {
        this.page = page;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

}
