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

package com.openexchange.config.cascade.impl;

import java.util.Map;
import junit.framework.TestCase;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.exception.OXException;
import com.openexchange.tools.strings.BasicTypesStringParser;


/**
 * {@link CascadingConfigurationImplTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CascadingConfigurationImplTest extends TestCase {

    private final ConfigCascade cascade = new ConfigCascade();
    private ConfigView view = null;

    @Override
    public void setUp() {
        cascade.setProvider("server", new InMemoryConfigProvider());
        cascade.setProvider("context", new InMemoryConfigProvider());
        cascade.setProvider("user", new InMemoryConfigProvider());

        cascade.setSearchPath("user", "context", "server");

        cascade.setStringParser(new BasicTypesStringParser());

        view = cascade.getView(1, 23);
    }

    public void testCascadingProperty() throws OXException {
        view.set("server", "com.openexchange.test.property", "Rosebud");
        assertEquals("Rosebud", view.get("com.openexchange.test.property", String.class));

        // Now let's override this on context level

        view.set("context", "com.openexchange.test.property", "Lemongrass");
        assertEquals("Lemongrass", view.get("com.openexchange.test.property", String.class));

        // And finally on user level

        view.set("user", "com.openexchange.test.property", "Rootbeer");
        assertEquals("Rootbeer", view.get("com.openexchange.test.property", String.class));

        // Even if I change the context value, once the user value is set, it doesn't matter
        view.set("context", "com.openexchange.test.property", "Forget-Me-Not");
        assertEquals("Rootbeer", view.get("com.openexchange.test.property", String.class));

    }

    public void testPropertyMetadata() throws OXException {
        view.property("server", "com.openexchange.test.property", String.class).set("published", "true");

        assertTrue(view.property("com.openexchange.test.property", String.class).get("published", boolean.class));

        view.property("server", "com.openexchange.test.property", String.class).set("final", "server");
        view.property("context", "com.openexchange.test.property", String.class).set("final", "context");

        assertEquals("context", view.property("com.openexchange.test.property", String.class).get("final"));

        // On combined properties the precedence may be changed
        assertEquals("server", view.property("com.openexchange.test.property", String.class).precedence("server","context", "user").get("final"));
    }


    public void testFinalProperty() throws OXException {
        // The metadata key "final" points to the Scope where the search iteration should stop, effectively prohibiting that a value is overridden
        view.set("server", "com.openexchange.test.property", "Rosebud");
        view.set("context", "com.openexchange.test.property", "Lemongrass");
        view.set("user", "com.openexchange.test.property", "Rootbeer");

        view.property("server", "com.openexchange.test.property", String.class).set("final", "context");


        assertEquals("Lemongrass", view.get("com.openexchange.test.property", String.class));
    }

    public void testFinalPropertyInversesSearchOrder() throws OXException {
        // The metadata key "final" points to the Scope where the search iteration should stop, effectively prohibiting that a value is overridden
        view.set("server", "com.openexchange.test.property", "Rosebud");
        view.set("context", "com.openexchange.test.property", "Lemongrass");
        view.set("user", "com.openexchange.test.property", "Rootbeer");

        view.property("server", "com.openexchange.test.property", String.class).set("final", "context");
        view.property("user", "com.openexchange.test.property", String.class).set("final", "user");


        assertEquals("Lemongrass", view.get("com.openexchange.test.property", String.class));
    }

    public void testAllProperties() throws OXException {
        view.set("server", "com.openexchange.test.property1", "Rosebud");
        view.set("server", "com.openexchange.test.property2", "Rosebud");
        view.set("server", "com.openexchange.test.property3", "Rosebud");
        view.property("server", "com.openexchange.test.property4", String.class)
            .set("Rosebud")
            .set("final", "server");

        view.set("context", "com.openexchange.test.property2", "Lemongrass");
        view.set("context", "com.openexchange.test.property3", "Lemongrass");
        view.set("context", "com.openexchange.test.property4", "Lemongrass");

        view.set("user", "com.openexchange.test.property3", "Rootbeer");
        view.set("user", "com.openexchange.test.property4", "Rootbeer");

        final Map<String, ComposedConfigProperty<String>> allProps = view.all();

        assertNotNull(allProps);
        assertEquals(4, allProps.size());

        for(int i = 1; i <= 4; i++) {
            assertTrue(allProps.containsKey("com.openexchange.test.property"+i));
        }

        assertEquals("Rosebud", allProps.get("com.openexchange.test.property1").get());
        assertEquals("Lemongrass", allProps.get("com.openexchange.test.property2").get());
        assertEquals("Rootbeer", allProps.get("com.openexchange.test.property3").get());
        assertEquals("Rosebud", allProps.get("com.openexchange.test.property4").get());

    }



}
