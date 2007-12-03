package com.openexchange.groupware.ldap;

import java.util.HashMap;
import java.util.Map;

public class MockResourceLookup {
	
	private Map<Integer,Resource> resources = new HashMap<Integer,Resource>();
	
	public MockResourceLookup(){
		Resource res = new Resource();
		res.setAvailable(true);
		res.setDescription("The secret sauce");
		res.setDisplayName("Secret Sauce");
		res.setIdentifier(1);
		res.setMail("resource_admin1@test.invalid");
		
		addResource(res);
	}

	private void addResource(Resource res) {
		this.resources.put(res.getIdentifier(), res);
	}
	
	public Resource getResource(int id) {
		return this.resources.get(id);
	}
}
