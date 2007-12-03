package com.openexchange.webdav.action.behaviour;

import java.util.HashSet;

import com.openexchange.configuration.ConfigurationException;
import com.openexchange.webdav.action.MockWebdavRequest;

import junit.framework.TestCase;

public class UserAgentBehaviourTest extends TestCase {
	
	private static interface I1{}
	private static interface I2{}
	private static interface I3{}
	
	
	private static class C13 implements I1, I3 {}
	private static class C2 implements I2 {}
	private static class C123 extends C13  implements  I2 {}
	
	
	public void testConflict(){
		try {
			new UserAgentBehaviour(".*", new C13(), new C2(), new C123());
			fail("Could create conflicting behaviour");
		} catch (ConfigurationException x) {
			assertTrue(true);
		}
	}
	
	public void testProvides() throws ConfigurationException {
		
		assertProvides(new UserAgentBehaviour(".*", new C13()) , I1.class, I3.class);
		assertProvides(new UserAgentBehaviour(".*", new C2()) , I2.class);
		assertProvides(new UserAgentBehaviour(".*", new C123()) , I1.class, I2.class, I3.class);
		assertProvides(new UserAgentBehaviour(".*", new C13(), new C2()) , I1.class, I2.class, I3.class);
		
		
	}
	
	public void testMatches() throws ConfigurationException {
		MockWebdavRequest req = new MockWebdavRequest(null, "");
		req.setHeader("User-Agent", "Bla");
		
		
		assertTrue("Didn't match!", new UserAgentBehaviour("Bl.?").matches(req));
		assertFalse("Did match!", new UserAgentBehaviour("Ab.?").matches(req));
		
	}
	
	public void testGet() throws ConfigurationException {
		C13 c13 = new C13();
		C2 c2 = new C2();
		
		Behaviour behaviour = new UserAgentBehaviour(".*", c13, c2);
		
		assertTrue(c13 == behaviour.get(I1.class));
		assertTrue(c13 == behaviour.get(I3.class));
		assertTrue(c2 == behaviour.get(I2.class));
		
	}
	
	private static void assertProvides(Behaviour behaviour, Class<? extends Object>...classes) {
		HashSet<Class<? extends Object>> copy = new HashSet<Class<? extends Object>>(behaviour.provides());
		for(Class clazz : classes) {
			assertTrue("Didn't find "+clazz, copy.remove(clazz));
		}
		assertTrue( copy.toString(), copy.isEmpty());
	}

}
