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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This describes a sequence of ->PageParts to unequivocally identify information (e.g. a contact«s name) in a webpages sourcecode. To
 * identify a particular bit of information two factors are used: - Its place in the sequence (e.g. in the page«s sourcecode the last name
 * is listed after the first name) - The sourcecode immediately surrounding it. There are two kinds of page parts: - Fillers, only used to
 * make the sequence unequivocal and containing a single-capture-group regex identifiyng them - Infos, containing a three-capture-group
 * regex (immediately before, relevant part, immediately after)
 * 
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class PagePartSequence {

    private ArrayList<PagePart> pageParts;

    private String page;

    public PagePartSequence() {

    }

    public PagePartSequence(ArrayList<PagePart> pageParts, String page) {
        this.pageParts = pageParts;
        this.page = page;
    }

    public HashMap<String, String> retrieveInformation() {
        HashMap<String, String> retrievedInformation = new HashMap<String, String>();

        for (PagePart pagePart : pageParts) {
            Pattern pattern = Pattern.compile(pagePart.getRegex());
            Matcher matcher = pattern.matcher(page);
            // find out if the part matches the remaining page
            if (matcher.find()) {
                int indexOfPageRest = matcher.end();
                // if it is an info-part and its info is not empty get its info and put it into the map
                if (pagePart.getType() == PagePart.INFO && matcher.groupCount() == 3) {
                    String info = matcher.group(2);
                    if (!info.equals(""))
                        retrievedInformation.put(pagePart.getTypeOfInfo(), info);
                }
                // set the page to the rest (after this part)
                page = page.substring(indexOfPageRest);
            }
        }

        return retrievedInformation;
    }

    public ArrayList<PagePart> getPageParts() {
        return pageParts;
    }

    public void setPageParts(ArrayList<PagePart> pageParts) {
        this.pageParts = pageParts;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

}
