package com.openexchange.webdav.action.behaviour;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.openexchange.webdav.action.MockWebdavRequest;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.behaviour.Behaviour;
import com.openexchange.webdav.action.behaviour.RequestSpecificBehaviourRegistry;

import junit.framework.TestCase;

public class RequestSpecificBehaviourRegistryTest extends TestCase {

	public interface TestInterface {

	}

	public class TestImplementation implements TestInterface{

	}

	public class TestBehaviour implements Behaviour {

		private TestImplementation implementation;

		public TestBehaviour(TestImplementation implementation) {
			this.implementation = implementation;
		}

		public boolean matches(WebdavRequest req) {
			return true;
		}

		public Set<Class<? extends Object>> provides() {
			return new HashSet<Class<? extends Object>>(Arrays.asList(TestInterface.class));

		}

		public <T> T get(Class<T> clazz) {
			return (T) implementation;
		}

	}

	public void testBasic() {
		RequestSpecificBehaviourRegistry registry = new RequestSpecificBehaviourRegistry();
		
		TestImplementation orig = new TestImplementation();
		
		Behaviour behaviour = new TestBehaviour(orig);
		
		registry.add(behaviour);
		
		TestInterface t = registry.get(new MockWebdavRequest(null, ""), TestInterface.class);
		
		assertTrue(t == orig);
	}
	
}
