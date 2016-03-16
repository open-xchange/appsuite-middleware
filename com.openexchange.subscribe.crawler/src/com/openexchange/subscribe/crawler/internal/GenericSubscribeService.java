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

import static com.openexchange.subscribe.crawler.internal.FormStrings.FORM_LABEL_LOGIN;
import static com.openexchange.subscribe.crawler.internal.FormStrings.FORM_LABEL_PASSWORD;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.subscribe.AbstractSubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.crawler.Workflow;
import com.openexchange.subscribe.crawler.osgi.CrawlersActivator;

/**
 * {@link GenericSubscribeService}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class GenericSubscribeService extends AbstractSubscribeService {

    private static final String LOGIN = "login";

    private static final String PASSWORD = "password";

    private final SubscriptionSource SOURCE = new SubscriptionSource();

    private final DynamicFormDescription FORM = new DynamicFormDescription();

    private final String workflowString;

    private final CrawlersActivator activator;

    private final boolean enableJavascript;

    private int module = FolderObject.CONTACT;

    public GenericSubscribeService(final String displayName, final String id, final int module, final String workflowString, final int priority, final CrawlersActivator activator, final boolean enableJavascript) {
        FORM.add(FormElement.input(LOGIN, FORM_LABEL_LOGIN)).add(FormElement.password("password", FORM_LABEL_PASSWORD));
        addExtraFields(FORM);
        SOURCE.setDisplayName(displayName);
        SOURCE.setId(id);
        SOURCE.setFormDescription(FORM);
        SOURCE.setSubscribeService(this);
        SOURCE.setFolderModule(module);
        SOURCE.setPriority(priority);
        this.workflowString = workflowString;
        this.activator = activator;
        this.enableJavascript = enableJavascript;
        this.module = module;
    }

    protected void addExtraFields(final DynamicFormDescription form) {
        // May be overridden to include extra fields
    }

    @Override
    public SubscriptionSource getSubscriptionSource() {
        return SOURCE;
    }

    @Override
    public boolean handles(final int folderModule) {
        return folderModule == this.module;
    }

    @Override
    public Collection getContent(final Subscription subscription) throws OXException {

        final Workflow workflow = getWorkflow();
        workflow.setSubscription(subscription);
        workflow.setEnableJavascript(enableJavascript);
        final Map<String, Object> configuration = subscription.getConfiguration();
        // All contacts should get a UUID for aggregation
        if (this.module == FolderObject.CONTACT){
            final List list =  Arrays.asList(workflow.execute((String) configuration.get("login"), (String) configuration.get("password")));
            final List<Contact> contacts = new ArrayList<Contact>();
            for (final Object object : list){
                final Contact contact = (Contact) object;
                contacts.add(contact);
            }
            return contacts;
        }
        return Arrays.asList(workflow.execute((String) configuration.get("login"), (String) configuration.get("password")));
    }

    public Workflow getWorkflow() {
        Workflow workflow = new Workflow();
        try {
            workflow = WorkflowFactory.createWorkflowByString(workflowString);
        } catch (final OXException e) {
        }
        workflow.setActivator(activator);

        return workflow;
    }

    @Override
    public void modifyIncoming(final Subscription subscription) throws OXException {
        super.modifyIncoming(subscription);
        final Map<String, Object> configuration = subscription.getConfiguration();
        try {
            encrypt(subscription.getSession(), configuration, PASSWORD);
        } catch (final UnsupportedOperationException e) {
            // May be thrown by TargetFolderSession, retry with real session
            final SessiondService service = SessiondService.SERVICE_REFERENCE.get();
            if (null == service) {
                throw e;
            }
            final Session fake = subscription.getSession();
            final Session session = service.getAnyActiveSessionForUser(fake.getUserId(), fake.getContextId());
            if (null == session) {
                throw e;
            }
            encrypt(session, configuration, PASSWORD);
        }
    }

    @Override
    public void modifyOutgoing(final Subscription subscription) throws OXException {
        super.modifyOutgoing(subscription);
        final Map<String, Object> configuration = subscription.getConfiguration();
        try {
            decrypt(subscription, subscription.getSession(), configuration, PASSWORD);
        } catch (final UnsupportedOperationException e) {
            // May be thrown by TargetFolderSession, retry with real session
            final SessiondService service = SessiondService.SERVICE_REFERENCE.get();
            if (null == service) {
                throw e;
            }
            final Session fake = subscription.getSession();
            final Session session = service.getAnyActiveSessionForUser(fake.getUserId(), fake.getContextId());
            if (null == session) {
                throw e;
            }
            decrypt(subscription, session, configuration, PASSWORD);
        }
        subscription.setDisplayName((String) subscription.getConfiguration().get(LOGIN));
    }

}
