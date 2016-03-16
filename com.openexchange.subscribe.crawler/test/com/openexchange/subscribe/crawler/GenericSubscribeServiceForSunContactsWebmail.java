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

import java.util.ArrayList;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.subscribe.crawler.internal.Step;


/**
 * {@link GenericSubscribeServiceForSunContactsWebmail}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class GenericSubscribeServiceForSunContactsWebmail extends GenericSubscribeServiceTestHelpers {

    public void testWebmailContacts(){

        // insert valid credentials here
        String username = "";
        String password = "";

        // create a CrawlerDescription
        CrawlerDescription crawler = new CrawlerDescription();
        crawler.setDisplayName("JESWebmailContacts");
        crawler.setId("com.openexchange.subscribe.crawler.suncontactswebmail");
        crawler.setCrawlerApiVersion(618);
        crawler.setModule(FolderObject.CONTACT);
        crawler.setPriority(1);
        crawler.setJavascriptEnabled(true);



        ArrayList<Step<?, ?>> steps = new ArrayList<Step<?, ?>>();

        LoginPageByFormActionRegexVerifiedByStringStep loginStep = new LoginPageByFormActionRegexVerifiedByStringStep();
        loginStep.setUrl("https://uwc1.us.es:444/uwc/auth");
        loginStep.setUsername(username);
        loginStep.setPassword(password);
        loginStep.setActionOfLoginForm(".*Login.*");
        loginStep.setNumberOfForm(1);
        loginStep.setNameOfUserField("IDToken1");
        loginStep.setNameOfPasswordField("IDToken2");
        loginStep.setStringAvailableAfterLogin(".*favicon.ico.*");
        steps.add(loginStep);

        PageByUrlStep pageByUrlStep = new PageByUrlStep();
        pageByUrlStep.setUrl("https://uwc1.us.es/en/mail.html?lang=en");
        //pageByUrlStep.setDebuggingEnabled(true);
        steps.add(pageByUrlStep);

        PageByFrameNumberStep pageByFrameNumberStep = new PageByFrameNumberStep();
        pageByFrameNumberStep.setFrameNumber(1);
        steps.add(pageByFrameNumberStep);

        PageByLinkRegexStep pageByLinkRegexStep = new PageByLinkRegexStep();
        pageByLinkRegexStep.setUrl(".*displayPab.*");
        pageByLinkRegexStep.setDebuggingEnabled(true);
        steps.add(pageByLinkRegexStep);




        crawler.finishUp(steps);

        findOutIfThereAreContactsForThisConfiguration(username, password, crawler, true);
        // uncomment this if the crawler description was updated to get the new config-files
        // dumpThis(crawler, crawler.getDisplayName());
    }
}
