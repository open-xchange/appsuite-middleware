package com.openexchange.groupware.ldap;

import java.util.HashMap;
import java.util.Map;

import com.openexchange.groupware.Component;
import com.openexchange.groupware.ldap.LdapException.Code;

/**
 * MockUserStorage for now contains some testing data relevant to the notification tests.
 * This can be extended for other tests for testing in isolation.
 */
public class MockGroupLookup {

	private Map<Integer,Group> groups = new HashMap<Integer,Group>();
	
	public Group getGroup(int gid) throws LdapException {
		if(!groups.containsKey(gid))
			throw new LdapException(Component.GROUP, Code.GROUP_NOT_FOUND,
                gid, -1);
		return groups.get(gid);
	}
	
	public MockGroupLookup(){
		int i = 0;
		Group group = new Group();
		group.setIdentifier(++i);
		group.setDisplayName("users");
		group.setMember(new int[]{1,2,3,4,5,6,7,8,9,10});
		addGroup(group);
		
		group = new Group();
		group.setIdentifier(++i);
		group.setDisplayName("Even Users");
		group.setMember(new int[]{3,5,7,9});
		addGroup(group);
		
		group = new Group();
		group.setIdentifier(++i);
		group.setDisplayName("Some group");
		group.setMember(new int[]{5,6,9});
		addGroup(group);
		
		group = new Group();
		group.setIdentifier(++i);
		group.setDisplayName("Uneven Users");
		group.setMember(new int[]{2,4,6,8,10});
		
	}

	private void addGroup(Group group) {
		groups.put(group.getIdentifier(),group);
	}

}
