package com.openexchange.groupware.ldap;

import java.util.HashMap;
import java.util.Map;

/**
 * MockUserStorage for now contains some testing data relevant to the notification tests.
 * This can be extended for other tests for testing in isolation.
 */
public class MockUserLookup {
	
	private Map<Integer, IUser> users = new HashMap<Integer, IUser>();
	
	
	public IUser getUser(int uid) throws LdapException {
		if(!users.containsKey(uid)) {
			throw new LdapException("User not found: "+uid);
		}
		return users.get(uid);
	}
	
	public MockUserLookup() {
		
		String tz = "Europe/Berlin";
		
		int i = 0;
		MockUser user = new MockUser();
		user.setId(++i);
		user.setDisplayName("The Mailadmin");
		user.setPreferredLanguage("en");
		user.setTimeZone(tz);
		user.setMail("mailadmin@test.invalid");
		user.setGroups(new int[]{1});
		addUser(user);
		
		user = new MockUser();
		user.setId(++i);
		user.setDisplayName("User 1");
		user.setPreferredLanguage("en");
		user.setTimeZone(tz);
		user.setMail("user1@test.invalid");
		user.setGroups(new int[]{1,4});
		addUser(user);
		
		user = new MockUser();
		user.setId(++i);
		user.setDisplayName("User 2");
		user.setPreferredLanguage("de");
		user.setTimeZone(tz);
		user.setMail("user2@test.invalid");
		user.setGroups(new int[]{1,2});
		addUser(user);
		
		user = new MockUser();
		user.setId(++i);
		user.setDisplayName("User 3");
		user.setPreferredLanguage("de");
		user.setTimeZone("Pacific/Samoa");
		user.setMail("user3@test.invalid");
		user.setGroups(new int[]{1,4});
		addUser(user);
		
		user = new MockUser();
		user.setId(++i);
		user.setDisplayName("User 4");
		user.setPreferredLanguage("de");
		user.setTimeZone(tz);
		user.setMail("user4@test.invalid");
		user.setGroups(new int[]{1,2,3});
		addUser(user);
		
		user = new MockUser();
		user.setId(++i);
		user.setDisplayName("User 5");
		user.setPreferredLanguage("en");
		user.setTimeZone(tz);
		user.setMail("user5@test.invalid");
		user.setGroups(new int[]{1,3,4});
		addUser(user);
		
		user = new MockUser();
		user.setId(++i);
		user.setDisplayName("User 6");
		user.setPreferredLanguage("de");
		user.setTimeZone(tz);
		user.setMail("user6@test.invalid");
		user.setGroups(new int[]{1,2});
		addUser(user);
		
		user = new MockUser();
		user.setId(++i);
		user.setDisplayName("User 7");
		user.setPreferredLanguage("en");
		user.setTimeZone(tz);
		user.setMail("user7@test.invalid");
		user.setGroups(new int[]{1,4});
		addUser(user);
		
		user = new MockUser();
		user.setId(++i);
		user.setDisplayName("User 8");
		user.setPreferredLanguage("de");
		user.setTimeZone(tz);
		user.setMail("user8@test.invalid");
		user.setGroups(new int[]{1,2,3});
		addUser(user);
		
		user = new MockUser();
		user.setId(++i);
		user.setDisplayName("User 9");
		user.setPreferredLanguage("fr");
		user.setTimeZone(tz);
		user.setMail("user9@test.invalid");
		user.setGroups(new int[]{1,4});
		addUser(user);
		
	}

	private void addUser(IUser user) {
		users.put(user.getId(),user);
	}

}
