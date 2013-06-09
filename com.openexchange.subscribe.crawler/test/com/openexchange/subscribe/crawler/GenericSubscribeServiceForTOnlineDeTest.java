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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import java.util.LinkedList;
import java.util.List;
import org.ho.yaml.Yaml;
import com.openexchange.subscribe.crawler.internal.PagePart;
import com.openexchange.subscribe.crawler.internal.PagePartSequence;
import com.openexchange.subscribe.crawler.internal.Step;


/**
 * {@link GenericSubscribeServiceForTOnlineDeTest}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class GenericSubscribeServiceForTOnlineDeTest extends GenericSubscribeServiceTestHelpers {

    public void testTOnlineDE(){
     // insert valid credentials here
        String username = "";
        String password = "";

        // create a CrawlerDescription
        CrawlerDescription crawler = new CrawlerDescription();
        crawler.setDisplayName("t-online.de");
        crawler.setId("com.openexchange.subscribe.crawler.t-online.de");
        crawler.setCrawlerApiVersion(618);
        List<Step<?, ?>> steps = new LinkedList<Step<?, ?>>();
        //
        steps.add(new LoginPageByFormActionStep("Log into the mobile version as the full one does not work", "https://m-email.t-online.de/", "", "", "/", "usr", "pwd", ".*overview.*", 1, ""));
        steps.add(new PageByLinkRegexStep("Click on 'Meine Kontakte'", ".*addresslist.*"));
        steps.add(new AnchorsByLinkRegexStep("Get all contact links, including those on subpages", ".*addresslist.*", ".*addressshow.*", ".*\\[adid\\]=([0-9]*)", true));
        ArrayList<PagePart> pageParts = new ArrayList<PagePart>();
        pageParts.add(new PagePart(
            "(<td class=\"headline_else_td\">)" + VALID_NAME + "(</td>)",
            "display_name"));
        pageParts.add(new PagePart("(Geburtstag</div>\\s<div>)([0-9]{2})(\\.)", "birthday_day"));
        pageParts.add(new PagePart("()([0-9]{2})(\\.)", "birthday_month"));
        pageParts.add(new PagePart("()([0-9]{4})(</div>)", "birthday_year"));
        pageParts.add(new PagePart("(?s)(<tr>(?:[^>]*>){4}[^>]*write_mail[^>]*>)"+VALID_EMAIL_REGEX+"(</a>)", "email2"));
        pageParts.add(new PagePart("(?s)(<tr>(?:[^>]*>){5}[^>]*write_mail[^>]*>)"+VALID_EMAIL_REGEX+"(</a>)", "email1"));
        pageParts.add(new PagePart("(?s)(Mobil(?:[^>]*>){7}[^>]*<span>)"+VALID_PHONE_REGEX+"(<\\/span>)", "cellular_telephone2", true));
        pageParts.add(new PagePart("(?s)(Mobil(?:[^>]*>){16}[^>]*<span>)"+VALID_PHONE_REGEX+"(<\\/span>)", "cellular_telephone1"));
        pageParts.add(new PagePart("(?s)(Festnetz(?:[^>]*>){7}[^>]*<span>)"+VALID_PHONE_REGEX+"(<\\/span>)", "telephone_home1", true));
        pageParts.add(new PagePart("(?s)(Festnetz(?:[^>]*>){16}[^>]*<span>)"+VALID_PHONE_REGEX+"(<\\/span>)", "telephone_business1"));
        pageParts.add(new PagePart("(?s)(PrvPostal1\">(?:[^>]*>){2}\\s<div>)([^>]*)(</div>)", "street_home", true));
        pageParts.add(new PagePart("(?s)(PrvPostal1\">(?:[^>]*>){4}\\s<div>)([0-9]*)[^>]*(</div>)", "postal_code_home", true));
        pageParts.add(new PagePart("(?s)(PrvPostal1\">(?:[^>]*>){4}\\s<div>)[0-9]*([^>0-9]*)(</div>)", "city_home", true));
        pageParts.add(new PagePart("(?s)(PrvPostal1\">(?:[^>]*>){6}\\s<div>)([^>]*)(</div>)", "country_home"));
        pageParts.add(new PagePart("(?s)(BusPostal1\">(?:[^>]*>){2}\\s<div>)([^>]*)(</div>)", "street_business", true));
        pageParts.add(new PagePart("(?s)(BusPostal1\">(?:[^>]*>){4}\\s<div>)([0-9]*)[^>]*(</div>)", "postal_code_business", true));
        pageParts.add(new PagePart("(?s)(BusPostal1\">(?:[^>]*>){4}\\s<div>)[0-9]*([^>0-9]*)(</div>)", "city_business", true));
        pageParts.add(new PagePart("(?s)(BusPostal1\">(?:[^>]*>){6}\\s<div>)([^>]*)(</div>)", "country_business"));
        PagePartSequence sequence = new PagePartSequence(pageParts, "");
        steps.add(new ContactObjectsByHTMLAnchorsAndPagePartSequenceStep(
            "Get the information of each contact from the individual webpages",
            sequence,
            "",
            ".*postalshow.*",
            true));

        Workflow workflow = new Workflow(steps);
        crawler.setWorkflowString(Yaml.dump(workflow));

        findOutIfThereAreContactsForThisConfiguration(username, password, crawler, true);
        // uncomment this if the if the crawler description was updated to get the new config-files
        // dumpThis(crawler, crawler.getDisplayName());
    }

}
