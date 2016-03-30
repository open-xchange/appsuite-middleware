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

package com.openexchange.pubsub.sql;

import static com.openexchange.sql.grammar.Constant.PLACEHOLDER;
import static com.openexchange.sql.schema.Tables.publications;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import com.openexchange.config.ConfigurationService;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.storage.SimConfigurationStorageService;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.SimPublicationTargetDiscoveryService;
import com.openexchange.publish.sql.PublicationSQLStorage;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.sql.builder.StatementBuilder;
import com.openexchange.sql.grammar.DELETE;
import com.openexchange.sql.grammar.EQUALS;
import com.openexchange.sql.grammar.Expression;
import com.openexchange.sql.grammar.IN;
import com.openexchange.sql.grammar.LIST;
import com.openexchange.tools.sql.SQLTestCase;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class AbstractPublicationSQLStorageTest extends SQLTestCase {

    protected Publication pub1 = null;

    protected Publication pub2 = null;

    protected Context ctx = new SimContext(1);

    protected List<Integer> publicationsToDelete = new ArrayList<Integer>();

    protected String entityId1 = "10";

    protected String entityId2 = entityId1;

    protected String module1 = "myModule";

    protected String module2 = module1;

    protected int userId = 44;

    protected PublicationSQLStorage storage;


    @Override
    protected void loadProperties() throws IOException {
        final ConfigurationService confService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        properties = new Properties();
        properties.setProperty("driver", confService.getProperty("writeDriverClass"));
        properties.setProperty("login", confService.getProperty("writeProperty.1"));
        properties.setProperty("password", confService.getProperty("writeProperty.2"));
        properties.setProperty("url", confService.getProperty("writeUrl"));
    }

    @Override
    public void setUp() throws Exception {
        Init.startServer();
        loadProperties();
        super.setUp();


        // First
        FormElement formElementLogin1 = new FormElement();
        formElementLogin1.setName("login1");
        formElementLogin1.setDisplayName("Login1");
        formElementLogin1.setMandatory(true);
        formElementLogin1.setWidget(FormElement.Widget.INPUT);
        formElementLogin1.setDefaultValue("default login1");

        FormElement formElementPassword1 = new FormElement();
        formElementPassword1.setName("password1");
        formElementPassword1.setDisplayName("Password1");
        formElementPassword1.setMandatory(true);
        formElementPassword1.setWidget(FormElement.Widget.PASSWORD);

        DynamicFormDescription formDescription1 = new DynamicFormDescription();
        formDescription1.addFormElement(formElementLogin1);
        formDescription1.addFormElement(formElementPassword1);

        PublicationTarget target1 = new PublicationTarget();
        target1.setDisplayName("Target 1");
        target1.setFormDescription(formDescription1);
        target1.setIcon("/path/to/icon1");
        target1.setModule(module1);
        target1.setId("com.openexchange.publication.test.basic1");

        Map<String, Object> config1 = new HashMap<String, Object>();
        config1.put("key1.1", 123);
        config1.put("key1.2", "Hello World!");

        pub1 = new Publication();
        pub1.setContext(ctx);
        pub1.setEntityId(entityId1);
        pub1.setModule(module1);
        pub1.setUserId(userId);
        pub1.setTarget(target1);
        pub1.setConfiguration(config1);

        // Second
        FormElement formElementLogin2 = new FormElement();
        formElementLogin2.setName("login2");
        formElementLogin2.setDisplayName("Login2");
        formElementLogin2.setMandatory(true);
        formElementLogin2.setWidget(FormElement.Widget.INPUT);
        formElementLogin2.setDefaultValue("default login2");

        FormElement formElementPassword2 = new FormElement();
        formElementPassword2.setName("password2");
        formElementPassword2.setDisplayName("Password2");
        formElementPassword2.setMandatory(true);
        formElementPassword2.setWidget(FormElement.Widget.PASSWORD);

        DynamicFormDescription formDescription2 = new DynamicFormDescription();
        formDescription2.addFormElement(formElementLogin2);
        formDescription2.addFormElement(formElementPassword2);

//        PublicationTarget target2 = new PublicationTarget();
//        target2.setDisplayName("Target 2");
//        target2.setFormDescription(formDescription2);
//        target2.setIcon("/path/to/icon2");
//        target2.setModule(module2);

        pub2 = new Publication();
        pub2.setContext(ctx);
        pub2.setEntityId(entityId2);
        pub2.setModule(module2);
        pub2.setUserId(userId);
        pub2.setTarget(target1);

        SimPublicationTargetDiscoveryService discoveryService = new SimPublicationTargetDiscoveryService();
        discoveryService.addTarget(target1);
        discoveryService.addTarget(target1);
        storage = new PublicationSQLStorage(getDBProvider(), new SimConfigurationStorageService(), discoveryService);
    }

    @Override
    public void tearDown() throws Exception {
        if (publicationsToDelete.size() > 0) {
            List<Expression> placeholder = new ArrayList<Expression>();
            for (int delId : publicationsToDelete) {
                placeholder.add(PLACEHOLDER);

                Publication publicationToDelete = new Publication();
                publicationToDelete.setId(delId);
                publicationToDelete.setContext(ctx);
                storage.forgetPublication(publicationToDelete);
            }

            DELETE delete = new DELETE().FROM(publications).WHERE(new EQUALS("cid", PLACEHOLDER).AND(new IN("id", new LIST(placeholder))));

            Connection writeConnection = getDBProvider().getWriteConnection(ctx);
            List<Integer> values = new ArrayList<Integer>();
            values.add(ctx.getContextId());
            values.addAll(publicationsToDelete);
            new StatementBuilder().executeStatement(writeConnection, delete, values);
            getDBProvider().releaseWriteConnection(ctx, writeConnection);
        }

        super.tearDown();
    }


    protected void assertEquals(Publication expected, Publication actual) {
        assertEquals(expected.getEntityId(), actual.getEntityId());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getModule(), actual.getModule());
        assertEquals(expected.getUserId(), actual.getUserId());
        assertEquals(expected.getTarget(), actual.getTarget());
    }

    protected void assertEquals(PublicationTarget expected, PublicationTarget actual) {
        assertEquals(expected.getDisplayName(), actual.getDisplayName());
        assertEquals(expected.getIcon(), actual.getIcon());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getModule(), actual.getModule());
        assertEquals(expected.getFormDescription(), actual.getFormDescription());
    }

    protected void assertEquals(DynamicFormDescription expected, DynamicFormDescription actual) {
        assertEquals("Form Element size does notg match", expected.getFormElements().size(), actual.getFormElements().size());
        for (FormElement formElementExpected : expected.getFormElements()) {
            boolean found = false;
            for (FormElement formElementActual : actual.getFormElements()) {
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

    protected void removePublicationsForTarget(String targetId) throws Exception {
        Connection writeConnection = getDBProvider().getWriteConnection(ctx);

        DELETE delete = new DELETE().FROM(publications).WHERE(new EQUALS("target_id", PLACEHOLDER));
        List<Object> values = new ArrayList<Object>();
        values.add(targetId);
        new StatementBuilder().executeStatement(writeConnection, delete, values);

        getDBProvider().releaseWriteConnection(ctx, writeConnection);
    }

    protected void removePublicationsForEntity(String entity, String module) throws Exception {
        Connection writeConnection = getDBProvider().getWriteConnection(ctx);

        DELETE delete = new DELETE().FROM(publications).WHERE(new EQUALS("entity", PLACEHOLDER).AND(new EQUALS("module", PLACEHOLDER)));
        List<Object> values = new ArrayList<Object>();
        values.add(entity);
        values.add(module);
        new StatementBuilder().executeStatement(writeConnection, delete, values);

        getDBProvider().releaseWriteConnection(ctx, writeConnection);
    }
}
