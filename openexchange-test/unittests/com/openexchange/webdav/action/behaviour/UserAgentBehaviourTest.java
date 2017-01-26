
package com.openexchange.webdav.action.behaviour;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.HashSet;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.webdav.action.MockWebdavRequest;

public class UserAgentBehaviourTest {

    private static interface I1 {
    }

    private static interface I2 {
    }

    private static interface I3 {
    }

    private static class C13 implements I1, I3 {
    }

    private static class C2 implements I2 {
    }

    private static class C123 extends C13 implements I2 {
    }

    @Test
    public void testConflict() {
        try {
            new UserAgentBehaviour(".*", new C13(), new C2(), new C123());
            fail("Could create conflicting behaviour");
        } catch (final OXException x) {
            assertTrue(true);
        }
    }

    @Test
    public void testProvides() throws OXException {

        assertProvides(new UserAgentBehaviour(".*", new C13()), I1.class, I3.class);
        assertProvides(new UserAgentBehaviour(".*", new C2()), I2.class);
        assertProvides(new UserAgentBehaviour(".*", new C123()), I1.class, I2.class, I3.class);
        assertProvides(new UserAgentBehaviour(".*", new C13(), new C2()), I1.class, I2.class, I3.class);

    }

    @Test
    public void testMatches() throws OXException {
        final MockWebdavRequest req = new MockWebdavRequest(null, "");
        req.setHeader("User-Agent", "Bla");

        assertTrue("Didn't match!", new UserAgentBehaviour("Bl.?").matches(req));
        assertFalse("Did match!", new UserAgentBehaviour("Ab.?").matches(req));

    }

    @Test
    public void testGet() throws OXException {
        final C13 c13 = new C13();
        final C2 c2 = new C2();

        final Behaviour behaviour = new UserAgentBehaviour(".*", c13, c2);

        assertTrue(c13 == behaviour.get(I1.class));
        assertTrue(c13 == behaviour.get(I3.class));
        assertTrue(c2 == behaviour.get(I2.class));

    }

    private static void assertProvides(final Behaviour behaviour, final Class<? extends Object>... classes) {
        final HashSet<Class<? extends Object>> copy = new HashSet<Class<? extends Object>>(behaviour.provides());
        for (final Class clazz : classes) {
            assertTrue("Didn't find " + clazz, copy.remove(clazz));
        }
        assertTrue(copy.toString(), copy.isEmpty());
    }

}
