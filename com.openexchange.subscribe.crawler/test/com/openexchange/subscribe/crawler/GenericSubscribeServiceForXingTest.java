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

import java.util.LinkedList;
import java.util.List;
import org.yaml.snakeyaml.Yaml;
import com.openexchange.subscribe.crawler.internal.Step;

/**
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class GenericSubscribeServiceForXingTest extends GenericSubscribeServiceTestHelpers {

    public void testGenericSubscribeServiceForXing() {
        // insert valid credentials here
        String username = "";
        String password = "";

        // create a CrawlerDescription
        CrawlerDescription crawler = new CrawlerDescription();
        crawler.setDisplayName("XING");
        crawler.setId("com.openexchange.subscribe.xing");
        crawler.setPriority(2);

        List<Step<?, ?>> steps = new LinkedList<Step<?, ?>>();
        steps.add(new LoginPageByFormActionStep(
            "Login to www.xing.com",
            "https://www.xing.com",
            "",
            "",
            "https://login.xing.com/login",
            "login_form[username]",
            "login_form[password]",
            "/app/contact",
            1,
            "https://www.xing.com"));
        steps.add(new TextPagesByLinkStep(
            "Get all vcards as text pages",
            "https://www.xing.com/app/contact?notags_filter=0;card_mode=0;search_filter=;tags_filter=;offset=",
            10,
            "",
            "/app/contact?op=vcard;scr_id"));
        steps.add(new ContactObjectsByVcardTextPagesStep());

        Workflow workflow = new Workflow(steps);
        crawler.setWorkflowString(new Yaml().dump(workflow));

        findOutIfThereAreContactsForThisConfiguration(username, password, crawler);
        // uncomment this if the if the crawler description was updated to get the new config-files
        // file get written to /conf/crawlers in this bundle as well as /crawlers in open-xchange-development
        // dumpThis(crawler, crawler.getDisplayName());
    }

}
