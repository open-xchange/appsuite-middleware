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

package com.openexchange.subscribe.sql;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.sql.grammar.Constant.PLACEHOLDER;
import static com.openexchange.sql.schema.Tables.subscriptions;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.storage.SimConfigurationStorageService;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.DELETE;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.IN;
import com.openexchange.sql.tools.SQLTools;
import com.openexchange.subscribe.SimSubscriptionSourceDiscoveryService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.SubscriptionStorage;
import com.openexchange.tools.sql.SQLTestCase;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class AbstractSubscriptionSQLStorageTest extends SQLTestCase {

    protected SubscriptionStorage storage = null;

    protected Subscription subscription = null;

    protected Subscription subscription2 = null;

    protected List<Integer> subscriptionsToDelete = new ArrayList<Integer>();

    protected Context ctx = new SimContext(1);

    protected String folderId = "eins";

    protected int userId = 44;

    protected long lastUpdate;

    @Override
    public void setUp() throws Exception {
        loadProperties();
        super.setUp();

        // First
        final FormElement formElementLogin = new FormElement();
        formElementLogin.setName("login");
        formElementLogin.setDisplayName("Login");
        formElementLogin.setMandatory(true);
        formElementLogin.setWidget(FormElement.Widget.INPUT);
        formElementLogin.setDefaultValue("default login");

        final FormElement formElementPassword = new FormElement();
        formElementPassword.setName("password");
        formElementPassword.setDisplayName("Password");
        formElementPassword.setMandatory(true);
        formElementPassword.setWidget(FormElement.Widget.PASSWORD);

        final DynamicFormDescription formDescription = new DynamicFormDescription();
        formDescription.addFormElement(formElementLogin);
        formDescription.addFormElement(formElementPassword);

        final SubscriptionSource subscriptionSource = new SubscriptionSource();
        subscriptionSource.setId("com.openexchange.subscribe.test.basic");
        subscriptionSource.setDisplayName("Basic Subscription for Tests");
        subscriptionSource.setIcon("http://path/to/icon");
        subscriptionSource.setFormDescription(formDescription);
        subscriptionSource.setFolderModule(FolderObject.CONTACT);

        final Map<String, Object> configuration = new HashMap<String, Object>();
        configuration.put("login", "user_a");
        configuration.put("password", "password_a");

        subscription = new Subscription();
        subscription.setContext(ctx);
        subscription.setFolderId(folderId);
        lastUpdate = new Date().getTime();
        subscription.setLastUpdate(lastUpdate);
        subscription.setUserId(userId);
        subscription.setSource(subscriptionSource);
        subscription.setConfiguration(configuration);
        subscription.setEnabled(true);

        // Second
        final FormElement formElementLogin2 = new FormElement();
        formElementLogin2.setName("login2");
        formElementLogin2.setDisplayName("Login2");
        formElementLogin2.setMandatory(true);
        formElementLogin2.setWidget(FormElement.Widget.INPUT);
        formElementLogin2.setDefaultValue("default login2");

        final FormElement formElementPassword2 = new FormElement();
        formElementPassword2.setName("password2");
        formElementPassword2.setDisplayName("Password2");
        formElementPassword2.setMandatory(true);
        formElementPassword2.setWidget(FormElement.Widget.PASSWORD);

        final DynamicFormDescription formDescription2 = new DynamicFormDescription();
        formDescription2.addFormElement(formElementLogin2);
        formDescription2.addFormElement(formElementPassword2);

        final SubscriptionSource subscriptionSource2 = new SubscriptionSource();
        subscriptionSource2.setId("com.openexchange.subscribe.test.basic2");
        subscriptionSource2.setDisplayName("Basic Subscription for Tests2");
        subscriptionSource2.setIcon("http://path/to/icon2");
        subscriptionSource2.setFormDescription(formDescription2);
        subscriptionSource2.setFolderModule(FolderObject.CONTACT);

        final Map<String, Object> configuration2 = new HashMap<String, Object>();
        configuration2.put("login", "user_a2");
        configuration2.put("password", "password_a2");

        subscription2 = new Subscription();
        subscription2.setContext(ctx);
        subscription2.setFolderId(folderId);
        lastUpdate = new Date().getTime();
        subscription2.setLastUpdate(lastUpdate);
        subscription2.setUserId(userId);
        subscription2.setSource(subscriptionSource2);
        subscription2.setConfiguration(configuration2);
        subscription2.setEnabled(false);

        final SimSubscriptionSourceDiscoveryService discoveryService = new SimSubscriptionSourceDiscoveryService();
        discoveryService.addSource(subscriptionSource);
        discoveryService.addSource(subscriptionSource2);
        storage = new SubscriptionSQLStorage(getDBProvider(), new SimConfigurationStorageService(), discoveryService);
    }

    @Override
    public void tearDown() throws Exception {
        if (subscriptionsToDelete.size() > 0) {
            for (final int delId : subscriptionsToDelete) {
                final Subscription subscriptionToDelete = new Subscription();
                subscriptionToDelete.setId(delId);
                subscriptionToDelete.setContext(ctx);
                storage.forgetSubscription(subscriptionToDelete);
            }

            final DELETE delete = new DELETE().FROM(subscriptions).WHERE(
                new EQUALS("cid", PLACEHOLDER).AND(new IN("id", SQLTools.createLIST(subscriptionsToDelete.size(), PLACEHOLDER))));

            final Connection writeConnection = getDBProvider().getWriteConnection(ctx);
            final List<Integer> values = new ArrayList<Integer>();
            values.add(I(ctx.getContextId()));
            values.addAll(subscriptionsToDelete);
            new StatementBuilder().executeStatement(writeConnection, delete, values);
            getDBProvider().releaseWriteConnection(ctx, writeConnection);
        }
        storage = null;

        super.tearDown();
    }

    protected void clearFolder(final String folderId) throws Exception {
        final Connection writeConnection = getDBProvider().getWriteConnection(ctx);

        final DELETE delete = new DELETE().FROM(subscriptions).WHERE(new EQUALS("folder_id", PLACEHOLDER));
        final List<Object> values = new ArrayList<Object>();
        values.add(folderId);
        new StatementBuilder().executeStatement(writeConnection, delete, values);

        getDBProvider().releaseWriteConnection(ctx, writeConnection);
    }

    protected void assertEquals(final Subscription expected, final Subscription actual) {
        if (expected != null) {
            assertNotNull(actual);
        }
        if (expected == null) {
            assertTrue("Expected null", actual == null);
            return;
        }
        assertEquals(expected.getContext().getContextId(), actual.getContext().getContextId());
        assertEquals(expected.getFolderId(), actual.getFolderId());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getLastUpdate(), actual.getLastUpdate());
        assertEquals(expected.getUserId(), actual.getUserId());
        assertEquals(expected.getSource(), actual.getSource());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.isEnabled(), actual.isEnabled());
    }

    protected void assertEquals(final SubscriptionSource expected, final SubscriptionSource actual) {
        if (expected != null) {
            assertNotNull(actual);
        }
        if (expected == null) {
            assertTrue("Expected null", actual == null);
            return;
        }
        assertEquals(expected.getDisplayName(), actual.getDisplayName());
        assertEquals(expected.getFolderModule(), actual.getFolderModule());
        assertEquals(expected.getIcon(), actual.getIcon());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getFormDescription(), actual.getFormDescription());
    }

    protected void assertEquals(final DynamicFormDescription expected, final DynamicFormDescription actual) {
        assertEquals("Form Element size does notg match", expected.getFormElements().size(), actual.getFormElements().size());
        for (final FormElement formElementExpected : expected.getFormElements()) {
            boolean found = false;
            for (final FormElement formElementActual : actual.getFormElements()) {
                if (formElementExpected.getName().equals(formElementActual.getName())) {
                    found = true;
                    assertEquals(formElementExpected, formElementActual);
                }
            }
            if (!found) {
                fail("Missing FormElement");
            }
        }
    }

    protected void assertEquals(final FormElement expected, final FormElement actual) {
        if (expected != null) {
            assertNotNull(actual);
        }
        if (expected == null) {
            assertTrue("Expected null", actual == null);
            return;
        }
        assertEquals(expected.getDefaultValue(), actual.getDefaultValue());
        assertEquals(expected.getDisplayName(), actual.getDisplayName());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getWidget(), actual.getWidget());
    }

}
