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

package com.openexchange.subscribe.linkedin.osgi;

import java.util.ArrayList;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.exceptions.osgi.ComponentRegistration;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.crawler.ContactObjectsByHTMLPagesStep;
import com.openexchange.subscribe.crawler.LoginPageStep;
import com.openexchange.subscribe.crawler.PageByUrlStep;
import com.openexchange.subscribe.crawler.PagesByLinkRegexStep;
import com.openexchange.subscribe.crawler.Step;
import com.openexchange.subscribe.crawler.Workflow;
import com.openexchange.subscribe.linkedin.LinkedInSubscribeService;

/**
 * {@link Activator}
 * 
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class Activator implements BundleActivator {

    private ComponentRegistration componentRegistration;

    private ServiceRegistration serviceRegistration;

    public void start(BundleContext context) throws Exception {
        componentRegistration = new ComponentRegistration(
            context,
            "LinkedIn",
            "com.openexchange.subscribe.linkedin",
            SubscriptionErrorMessage.EXCEPTIONS);

        ArrayList<Step> listOfSteps = new ArrayList<Step>();

        listOfSteps.add(new LoginPageStep(
            "Login to www.linkedin.com",
            "https://www.linkedin.com/secure/login",
            "",
            "",
            "login",
            "session_key",
            "session_password",
            "LinkedIn: Home"));
        listOfSteps.add(new PageByUrlStep(
            "Get to the contacts list", 
            "http://www.linkedin.com/connections?trk=hb_side_cnts"));
        listOfSteps.add(new PageByUrlStep(
            "Get to the no-javascript contacts list",
            "http://www.linkedin.com/connectionsnojs?trk=cnx_nojslink"));
        listOfSteps.add(new PagesByLinkRegexStep(
            "Get all pages that link to a connections profile",
            "(/profile\\?viewProfile=).*(goback).*"));
        listOfSteps.add(new ContactObjectsByHTMLPagesStep(
            "Extract the contact information from these pages",
            "/addressBookExport?exportMemberVCard",
            "http://media.linkedin.com/mpr/mpr/shrink_80_80"));

        Workflow linkedInWorkflow = new Workflow(listOfSteps);
        LinkedInSubscribeService subscribeService = new LinkedInSubscribeService();
        subscribeService.setWorkflow(linkedInWorkflow);

        serviceRegistration = context.registerService(SubscribeService.class.getName(), subscribeService, null);
    }

    public void stop(BundleContext context) throws Exception {
        serviceRegistration.unregister();
        componentRegistration.unregister();
    }

}
