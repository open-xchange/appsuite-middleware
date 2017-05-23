
package com.openexchange.groupware.infostore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.infostore.webdav.PropertyStore;
import com.openexchange.groupware.infostore.webdav.PropertyStoreImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.webdav.protocol.WebdavProperty;

public class PropertyStoreTest {

    private PropertyStore propertyStore;

    protected List<Integer> clean = new ArrayList<Integer>();

    protected int entity = 23;
    protected int entity2 = 42;

    private final Context ctx = new ContextImpl(1);
    private final User user = null;
    private final UserConfiguration userConfig = null;

    @Before
    public void setUp() throws Exception {
        Init.startServer();
        this.propertyStore = new PropertyStoreImpl(new DBPoolProvider(), "infostore_property");

        propertyStore.startTransaction();

        final WebdavProperty property = new WebdavProperty();
        property.setName("testprop");
        property.setNamespace("OX:");
        property.setValue("I'm the value");

        final WebdavProperty property2 = new WebdavProperty();
        property2.setName("testprop2");
        property2.setNamespace("OX:");
        property2.setValue("<bla>I'm the value2</bla>");
        property2.setXML(true);

        propertyStore.saveProperties(entity, Arrays.asList(property, property2), ctx);
        clean.add(entity);

        propertyStore.saveProperties(entity2, Arrays.asList(property, property2), ctx);
        clean.add(entity2);
    }

    @After
    public void tearDown() throws Exception {
        propertyStore.removeAll(clean, ctx);
        clean.clear();
        propertyStore.commit();
        propertyStore.finish();
        Init.stopServer();
    }

    @Test
    public void testLoadSpecificForOneEntity() throws Exception {
        final List<WebdavProperty> props = propertyStore.loadProperties(entity, p("OX:", "testprop"), ctx);
        assertEquals(1, props.size());
        assertEquals("I'm the value", props.get(0).getValue());
        assertFalse(props.get(0).isXML());
    }

    @Test
    public void testLoadSpecificForManyEntities() throws Exception {
        final Map<Integer, List<WebdavProperty>> props = propertyStore.loadProperties(Arrays.asList(entity, entity2), p("OX:", "testprop"), ctx);

        assertEquals(2, props.keySet().size());

        List<WebdavProperty> p = props.get(entity);
        assertNotNull(p);
        assertEquals(1, p.size());
        assertEquals("I'm the value", p.get(0).getValue());
        assertFalse(p.get(0).isXML());

        p = props.get(entity2);
        assertNotNull(p);
        assertEquals(1, p.size());
        assertEquals("I'm the value", p.get(0).getValue());
        assertFalse(p.get(0).isXML());

    }

    @Test
    public void testLoadAllForOneEntity() throws Exception {
        final List<WebdavProperty> props = propertyStore.loadAllProperties(entity, ctx);
        checkProps(props);
    }

    @Test
    public void testLoadAllForManyEntities() throws Exception {
        final Map<Integer, List<WebdavProperty>> props = propertyStore.loadAllProperties(Arrays.asList(entity, entity2), ctx);

        assertEquals(2, props.keySet().size());
        List<WebdavProperty> p = props.get(entity);
        checkProps(p);

        p = props.get(entity2);
        checkProps(p);
    }

    @Test
    public void testDeleteSpecificForOneEntity() throws Exception {
        propertyStore.removeProperties(entity, p("OX:", "testprop"), ctx);
        final List<WebdavProperty> props = propertyStore.loadAllProperties(entity, ctx);
        assertEquals(1, props.size());
        assertEquals("testprop2", props.get(0).getName());
    }

    @Test
    public void testDeleteAllForOneEntity() throws Exception {
        propertyStore.removeAll(entity, ctx);
        final List<WebdavProperty> props = propertyStore.loadAllProperties(entity, ctx);
        assertTrue(props.isEmpty());
    }

    private void checkProps(final List<WebdavProperty> props) {
        assertNotNull(props);
        assertEquals(2, props.size());
        int mask = 0;
        for (final WebdavProperty prop : props) {
            if (prop.getName().equals("testprop")) {
                mask += 1;
                assertEquals("I'm the value", prop.getValue());
                assertFalse(prop.isXML());
            } else if (prop.getName().equals("testprop2")) {
                mask += 2;
                assertEquals("<bla>I'm the value2</bla>", prop.getValue());
                assertTrue(prop.isXML());
            }
        }

        assertEquals(3, mask);
    }

    private List<WebdavProperty> p(final String... props) {
        final List<WebdavProperty> propList = new ArrayList<WebdavProperty>();
        for (int i = 0; i < props.length; i++) {
            final String namespace = props[i++];
            final String name = props[i];
            propList.add(new WebdavProperty(namespace, name));
        }
        return propList;
    }
}
