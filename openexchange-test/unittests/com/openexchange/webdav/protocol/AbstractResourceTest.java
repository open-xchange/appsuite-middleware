
package com.openexchange.webdav.protocol;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.webdav.protocol.Protocol.Property;
import com.openexchange.webdav.protocol.util.PropertySwitch;

public abstract class AbstractResourceTest implements PropertySwitch {

    public static final int SKEW = 1000;

    protected WebdavFactory resourceManager;

    protected WebdavPath testCollection = new WebdavPath("public_infostore", "testCollection" + Math.random());

    protected abstract WebdavFactory getWebdavFactory() throws Exception;

    protected abstract List<Property> getPropertiesToTest() throws Exception;

    protected abstract WebdavResource createResource() throws Exception;

    @Before
    public void setUp() throws Exception {
        resourceManager = getWebdavFactory();

        final WebdavResource resource = resourceManager.resolveCollection(testCollection);
        assertTrue(resource.isCollection());
        resource.create();
    }

    @After
    public void tearDown() throws Exception {
        resourceManager.resolveCollection(testCollection).delete();
    }

    @Test
    public void testProperties() throws Exception {
        WebdavResource res = createResource();
        WebdavProperty prop = new WebdavProperty();
        prop.setNamespace("OXTest");
        prop.setName("myvalue");
        prop.setLanguage("en");
        prop.setValue("testValue");

        res.putProperty(prop);
        res.save();

        res = resourceManager.resolveResource(res.getUrl());

        Assert.assertEquals(prop, res.getProperty("OXTest", "myvalue"));

        prop = new WebdavProperty();
        prop.setNamespace("OXTest");
        prop.setName("myvalue");
        prop.setLanguage("en");
        prop.setValue("testValue2");

        res.putProperty(prop);
        res.save();

        res = resourceManager.resolveResource(res.getUrl());

        Assert.assertEquals(prop, res.getProperty("OXTest", "myvalue"));

        res.removeProperty("OXTest", "myvalue");
        res.save();

        assertNull(res.getProperty("OXTest", "myvalue"));
    }

    @Test
    public void testMandatoryProperties() throws Exception {
        final List<Property> mandatory = getPropertiesToTest();
        for (final Property prop : mandatory) {
            prop.doSwitch(this);
        }
    }

    @Test
    public void testCreateLoadAndDelete() throws Exception {
        WebdavResource resource = createResource();
        assertTrue(resource.exists());
        resource.delete();
        resource = resourceManager.resolveResource(resource.getUrl());
        assertFalse(resource.exists());
    }

    public static void assertEquals(final Date d1, final Date d2, final int skew) {
        final long l1 = d1.getTime();
        final long l2 = d2.getTime();
        final long diff = (l1 < l2) ? l2 - l1 : l1 - l2;
        if (diff > skew) {
            Assert.assertEquals(l1, l2);
        }
        assertTrue(true);
    }

    public static void assertResources(final Iterable<WebdavResource> resources, final String... displayNames) throws OXException {
        //assertEquals(displayNames.length, resources.size());

        final Set<String> nameSet = new HashSet<String>(Arrays.asList(displayNames));

        for (final WebdavResource res : resources) {
            assertTrue(res.getDisplayName() + " not expected", nameSet.remove(res.getDisplayName()));
        }
        assertTrue(nameSet.toString(), nameSet.isEmpty());
    }

    public static void assertOptions(final Iterable<WebdavMethod> expect, final WebdavMethod... methods) throws OXException {
        //assertEquals(displayNames.length, resources.size());

        final Set<WebdavMethod> methodSet = new HashSet<WebdavMethod>(Arrays.asList(methods));

        for (final WebdavMethod method : expect) {
            assertTrue(method + " not expected", methodSet.remove(method));
        }
        assertTrue(methodSet.toString(), methodSet.isEmpty());
    }

    public void throwEx(final OXException x) throws OXException {
        throw new WebdavProtocolException(new WebdavPath(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, x);
    }
}
