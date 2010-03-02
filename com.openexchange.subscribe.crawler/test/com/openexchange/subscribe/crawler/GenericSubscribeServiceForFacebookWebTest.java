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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.subscribe.crawler.internal.PagePart;
import com.openexchange.subscribe.crawler.internal.PagePartSequence;
import com.openexchange.subscribe.crawler.internal.Step;

/**
 * {@link GenericSubscribeServiceForFacebookWebTest}
 * This is the preferred way to crawl Facebook.
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class GenericSubscribeServiceForFacebookWebTest extends GenericSubscribeServiceTestHelpers {

    public void testGenericSubscribeServiceForFacebook() {
        // insert valid credentials here
        final String username = "";
        final String password = "";

        // create a CrawlerDescription
        final CrawlerDescription crawler = new CrawlerDescription();
        crawler.setDisplayName("Facebook");
        crawler.setId("com.openexchange.subscribe.crawler.facebook");
        crawler.setCrawlerApiVersion(616);
        // increment priority with each new version to override (=be able to update) older versions
        crawler.setPriority(5);
        // this crawler gets contacts
        crawler.setModule(FolderObject.CONTACT);
        
        final List<Step> steps = new LinkedList<Step>();

        steps.add(new LoginPageByFormActionRegexStep(
            "Login to facebook.com",
            "http://m.facebook.com/",
            "",
            "",
            ".*login.*",
            "email",
            "pass",
            "(.*friends.*)",
            1,
            "http://m.facebook.com"));
        steps.add(new PageByLinkRegexStep("click the friends-link", "\\/friends.*"));
        steps.add(new PageByLinkRegexStep("click the all-link", "\\/friends.php?.*&a.*"));
        steps.add(new AnchorsByLinkXPathStep(
            "click all the individual friends links on all subpages.",
            "\\/friends.php?.*&a&f.*",
            "/html/body/div[4]/div[6]/table/tbody/tr[REPLACE_THIS]/td/a",
            0,
            17));
        final ArrayList<PagePart> pageParts = new ArrayList<PagePart>();
        pageParts.add(new PagePart("(<div class=\"section_title\">)([^<]*)(</div>)", "display_name"));
        pageParts.add(new PagePart("(<img src=\")(http:\\/\\/profile\\.ak\\.fbcdn\\.net[^\"]*)(\")", "image"));
        pageParts.add(new PagePart("(AIM|Google Talk|Skype|Windows Live|Yahoo):<\\/td><td  valign=\"top\">([^<]*)(</td>)","instant_messenger1",1));
        pageParts.add(new PagePart("(Mobile Number|Handynummer|Num\u00e9ro de mobile|N\u00famero de m\u00f3vil):</td><td ><a href=\"tel:[0-9]*\">"+VALID_PHONE_REGEX+"(<\\/a>)", "cellular_telephone1"));
        pageParts.add(new PagePart("(Phone|Telefon|T\u00e9l\u00e9phone|Tel\u00e9fono):</td><td ><a href=\"tel:[0-9]*\">"+VALID_PHONE_REGEX+"(<\\/a>)", "telephone_business1"));
        pageParts.add(new PagePart("(Current address|Aktuelle Adresse|Adresse actuelle|Direcci\u00f3n actual):<\\/td><td  valign=\"top\">(.+?)(<\\/td>)","address_note"));
        pageParts.add(new PagePart("(Member of|Mitglied von):<\\/td><td  valign=\"top\">(.+?)(<\\/td>)","company"));
        pageParts.add(new PagePart("(Birthday|Geburtstag|Date de naissance|Fecha de nacimiento):<\\/td><td  valign=\"top\">([0-9]{1,2})(\\.|\\sde|)", "birthday_day"));
        pageParts.add(new PagePart("(\\s)([^,0-9\\s]*)(,|)", "birthday_month_string"));
        pageParts.add(new PagePart("(\\s)([0-9]{4})(<)", "birthday_year"));
        pageParts.add(new PagePart("(Hometown|Heimatstadt|Originaire de|Ciudad):<\\/td><td  valign=\"top\">(.+?)(<\\/td>)","city_home"));
        pageParts.add(new PagePart("(Firma|Company|Entreprise|Empresa):<\\/td><td  valign=\"top\">([^<]*)(<\\/td>)", "company"));
        final PagePartSequence sequence = new PagePartSequence(pageParts, "");
        steps.add(new ContactObjectsByHTMLAnchorsAndPagePartSequenceStep(
            "Get the info-bits from the contact-page.",
            sequence,
            "Facebook.*(Your Profile|Dein Profil)", 
            ".*&v=info.*"));
        steps.add(new RemoveDuplicateContactsStep());
        final Workflow workflow = new Workflow(steps);
        
        
        final String yamlString = Yaml.dump(workflow);
        crawler.setWorkflowString(yamlString);

        findOutIfThereAreContactsForThisConfiguration(username, password, crawler, true);
        // uncomment this if the if the crawler description was updated to get the new config-files
        // dumpThis(crawler, crawler.getDisplayName());
    }
}
