
package com.openexchange.webdav.action.behaviour;

import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import com.openexchange.webdav.action.MockWebdavRequest;
import com.openexchange.webdav.action.WebdavRequest;

public class RequestSpecificBehaviourRegistryTest {

    public interface TestInterface {

    }

    public class TestImplementation implements TestInterface {

    }

    public class TestBehaviour implements Behaviour {

        private final TestImplementation implementation;

        public TestBehaviour(final TestImplementation implementation) {
            this.implementation = implementation;
        }

        @Override
        public boolean matches(final WebdavRequest req) {
            return true;
        }

        @Override
        public Set<Class<? extends Object>> provides() {
            return new HashSet<Class<? extends Object>>(Arrays.asList(TestInterface.class));

        }

        @Override
        public <T> T get(final Class<T> clazz) {
            return (T) implementation;
        }

    }

    @Test
    public void testBasic() {
        final RequestSpecificBehaviourRegistry registry = new RequestSpecificBehaviourRegistry();

        final TestImplementation orig = new TestImplementation();

        final Behaviour behaviour = new TestBehaviour(orig);

        registry.add(behaviour);

        final TestInterface t = registry.get(new MockWebdavRequest(null, ""), TestInterface.class);

        assertTrue(t == orig);
    }

}
