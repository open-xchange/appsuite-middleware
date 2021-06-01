/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.config.cascade.impl;

import static com.openexchange.java.Autoboxing.b;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewScope;
import com.openexchange.exception.OXException;
import com.openexchange.tools.strings.BasicTypesStringParser;


/**
 * {@link CascadingConfigurationImplTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CascadingConfigurationImplTest {
    private final ConfigCascade cascade = new ConfigCascade();
    private ConfigView view = null;

    @Before
    public void setUp() {
        cascade.setProvider(ConfigViewScope.SERVER.getScopeName(), new InMemoryConfigProvider());
        cascade.setProvider(ConfigViewScope.CONTEXT.getScopeName(), new InMemoryConfigProvider());
        cascade.setProvider(ConfigViewScope.USER.getScopeName(), new InMemoryConfigProvider());

        cascade.setSearchPath(ConfigViewScope.USER.getScopeName(), ConfigViewScope.CONTEXT.getScopeName(), ConfigViewScope.SERVER.getScopeName());

        cascade.setStringParser(new BasicTypesStringParser());

        view = cascade.getView(1, 23);
    }

         @Test
     public void testCascadingProperty() throws OXException {
        view.set(ConfigViewScope.SERVER.getScopeName(), "com.openexchange.test.property", "Rosebud");
        assertEquals("Rosebud", view.get("com.openexchange.test.property", String.class));

        // Now let's override this on context level

        view.set(ConfigViewScope.CONTEXT.getScopeName(), "com.openexchange.test.property", "Lemongrass");
        assertEquals("Lemongrass", view.get("com.openexchange.test.property", String.class));

        // And finally on user level

        view.set(ConfigViewScope.USER.getScopeName(), "com.openexchange.test.property", "Rootbeer");
        assertEquals("Rootbeer", view.get("com.openexchange.test.property", String.class));

        // Even if I change the context value, once the user value is set, it doesn't matter
        view.set(ConfigViewScope.CONTEXT.getScopeName(), "com.openexchange.test.property", "Forget-Me-Not");
        assertEquals("Rootbeer", view.get("com.openexchange.test.property", String.class));

    }

         @Test
     public void testPropertyMetadata() throws OXException {
        view.property(ConfigViewScope.SERVER.getScopeName(), "com.openexchange.test.property", String.class).set("published", "true");

        assertTrue(b(view.property("com.openexchange.test.property", String.class).get("published", boolean.class)));

        view.property(ConfigViewScope.SERVER.getScopeName(), "com.openexchange.test.property", String.class).set("final", ConfigViewScope.SERVER.getScopeName());
        view.property(ConfigViewScope.CONTEXT.getScopeName(), "com.openexchange.test.property", String.class).set("final", ConfigViewScope.CONTEXT.getScopeName());

        assertEquals(ConfigViewScope.CONTEXT.getScopeName(), view.property("com.openexchange.test.property", String.class).get("final"));

        // On combined properties the precedence may be changed
        assertEquals(ConfigViewScope.SERVER.getScopeName(), view.property("com.openexchange.test.property", String.class).precedence(ConfigViewScope.SERVER.getScopeName(), ConfigViewScope.CONTEXT.getScopeName(), ConfigViewScope.USER.getScopeName()).get("final"));
    }


         @Test
     public void testFinalProperty() throws OXException {
        // The metadata key "final" points to the Scope where the search iteration should stop, effectively prohibiting that a value is overridden
        view.set(ConfigViewScope.SERVER.getScopeName(), "com.openexchange.test.property", "Rosebud");
        view.set(ConfigViewScope.CONTEXT.getScopeName(), "com.openexchange.test.property", "Lemongrass");
        view.set(ConfigViewScope.USER.getScopeName(), "com.openexchange.test.property", "Rootbeer");

        view.property(ConfigViewScope.SERVER.getScopeName(), "com.openexchange.test.property", String.class).set("final", ConfigViewScope.CONTEXT.getScopeName());


        assertEquals("Lemongrass", view.get("com.openexchange.test.property", String.class));
    }

         @Test
     public void testFinalPropertyInversesSearchOrder() throws OXException {
        // The metadata key "final" points to the Scope where the search iteration should stop, effectively prohibiting that a value is overridden
        view.set(ConfigViewScope.SERVER.getScopeName(), "com.openexchange.test.property", "Rosebud");
        view.set(ConfigViewScope.CONTEXT.getScopeName(), "com.openexchange.test.property", "Lemongrass");
        view.set(ConfigViewScope.USER.getScopeName(), "com.openexchange.test.property", "Rootbeer");

        view.property(ConfigViewScope.SERVER.getScopeName(), "com.openexchange.test.property", String.class).set("final", ConfigViewScope.CONTEXT.getScopeName());
        view.property(ConfigViewScope.USER.getScopeName(), "com.openexchange.test.property", String.class).set("final", ConfigViewScope.USER.getScopeName());


        assertEquals("Lemongrass", view.get("com.openexchange.test.property", String.class));
    }

         @Test
     public void testAllProperties() throws OXException {
        view.set(ConfigViewScope.SERVER.getScopeName(), "com.openexchange.test.property1", "Rosebud");
        view.set(ConfigViewScope.SERVER.getScopeName(), "com.openexchange.test.property2", "Rosebud");
        view.set(ConfigViewScope.SERVER.getScopeName(), "com.openexchange.test.property3", "Rosebud");
        view.property("server", "com.openexchange.test.property4", String.class)
            .set("Rosebud")
            .set("final", ConfigViewScope.SERVER.getScopeName());

        view.set(ConfigViewScope.CONTEXT.getScopeName(), "com.openexchange.test.property2", "Lemongrass");
        view.set(ConfigViewScope.CONTEXT.getScopeName(), "com.openexchange.test.property3", "Lemongrass");
        view.set(ConfigViewScope.CONTEXT.getScopeName(), "com.openexchange.test.property4", "Lemongrass");

        view.set(ConfigViewScope.USER.getScopeName(), "com.openexchange.test.property3", "Rootbeer");
        view.set(ConfigViewScope.USER.getScopeName(), "com.openexchange.test.property4", "Rootbeer");

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
