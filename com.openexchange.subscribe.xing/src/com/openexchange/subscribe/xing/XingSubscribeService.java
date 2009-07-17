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

package com.openexchange.subscribe.xing;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.subscribe.AbstractSubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionException;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.crawler.ContactObjectsByVcardTextPagesStep;
import com.openexchange.subscribe.crawler.LoginPageStep;
import com.openexchange.subscribe.crawler.Step;
import com.openexchange.subscribe.crawler.TextPagesByLinkStep;
import com.openexchange.subscribe.crawler.Workflow;

/**
 * {@link XingSubscribeService}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class XingSubscribeService extends AbstractSubscribeService {

    private static final String LOGIN = "login";

    private static final String PASSWORD = "password";

    private final SubscriptionSource SOURCE = new SubscriptionSource();

    private final DynamicFormDescription FORM = new DynamicFormDescription();

    public XingSubscribeService() {
        FORM.add(FormElement.input(LOGIN, "Login")).add(FormElement.password("password", "Password"));

        SOURCE.setDisplayName("XING");
        SOURCE.setId("com.openexchange.subscribe.xing");
        SOURCE.setFormDescription(FORM);
        SOURCE.setSubscribeService(this);
        SOURCE.setFolderModule(FolderObject.CONTACT);
        
    }

    public SubscriptionSource getSubscriptionSource() {
        return SOURCE;
    }

    public boolean handles(int folderModule) {
        return folderModule == FolderObject.CONTACT;
    }

    public Collection<Contact> getContent(Subscription subscription) throws SubscriptionException {
        Workflow xingWorkflow = getWorkflow();
        Map<String, Object> configuration = subscription.getConfiguration();
        return Arrays.asList(xingWorkflow.execute((String)configuration.get("login"), (String) configuration.get("password")));
    }

    private Workflow getWorkflow() {
        List<Step> steps = new LinkedList<Step>(); 
        steps.add(new LoginPageStep("Login to www.xing.com", "https://www.xing.com", "", "", "loginform", "login_user_name", "login_password","Home | XING"));
        steps.add(new TextPagesByLinkStep("Get all vcards as text pages", "https://www.xing.com/app/contact?notags_filter=0;card_mode=0;search_filter=;tags_filter=;offset=", 10, "", "/app/vcard"));
        steps.add(new ContactObjectsByVcardTextPagesStep());
        return new Workflow(steps);
    }

    @Override
    public void modifyIncoming(Subscription subscription) throws SubscriptionException {
        super.modifyIncoming(subscription);
        Map<String, Object> configuration = subscription.getConfiguration();
        encrypt(configuration, PASSWORD);
    }

    @Override
    public void modifyOutgoing(Subscription subscription) throws SubscriptionException {
        super.modifyOutgoing(subscription);
        Map<String, Object> configuration = subscription.getConfiguration();
        decrypt(configuration, PASSWORD);
        subscription.setDisplayName( (String) subscription.getConfiguration().get(LOGIN) );
    }
}
