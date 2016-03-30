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
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.subscribe.crawler.internal.AbstractStep;
import com.openexchange.subscribe.crawler.internal.Mappings;
import com.openexchange.subscribe.crawler.internal.PagePartSequence;


/**
 * This returns one CalendarDataObject per linked Page
 * {@link CalendarDataObjectsByHTMLAnchorsAndPagePartSequenceStep}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class CalendarDataObjectsByHTMLAnchorsAndPagePartSequenceStep extends AbstractStep<CalendarDataObject[], List<HtmlAnchor>> {

    private PagePartSequence pageParts;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalendarDataObjectsByHTMLAnchorsAndPagePartSequenceStep.class);

    public CalendarDataObjectsByHTMLAnchorsAndPagePartSequenceStep(String description, PagePartSequence pageParts){
        this.description = description;
        this.pageParts = pageParts;
    }

    /* (non-Javadoc)
     * @see com.openexchange.subscribe.crawler.internal.AbstractStep#execute(com.gargoylesoftware.htmlunit.WebClient)
     */
    @Override
    public void execute(WebClient webClient) throws OXException {
        ArrayList<CalendarDataObject> events = new ArrayList<CalendarDataObject>();
        for (HtmlAnchor link : input){
            try {
                HtmlPage page = link.click();
                final String pageString = StringEscapeUtils.unescapeHtml(page.getWebResponse().getContentAsString());
                pageParts.setPage(pageString);
                LOG.debug("Page evaluated is : {}", pageString);
                final HashMap<String, String> map = pageParts.retrieveInformation();

                final CalendarDataObject oxEvent = Mappings.translateMapToCalendarDataObject(map);
                events.add(oxEvent);

            } catch (IOException e) {
                LOG.error("", e);
            }
        }

        output = new CalendarDataObject[events.size()];
        for (int i = 0; i < events.size() && i < output.length; i++) {
            output[i] = events.get(i);
        }
        executedSuccessfully = true;
    }

    public PagePartSequence getPageParts() {
        return pageParts;
    }

    public void setPageParts(final PagePartSequence pageParts) {
        this.pageParts = pageParts;
    }
}
