package com.openexchange.groupware.ldap;

import java.util.HashMap;
import java.util.Map;

import com.openexchange.groupware.Component;
import com.openexchange.groupware.ldap.LdapException.Code;

/**
 * MockUserStorage for now contains some testing data relevant to the notification tests.
 * This can be exten_USded for other tests for testing in isolation.
 */
public class MockUserLookup {
	
	private Map<Integer, User> users = new HashMap<Integer, User>();
	
	
	public User getUser(int uid) throws LdapException {
		if(!users.containsKey(uid)) {
			throw new LdapException(Component.USER, Code.USER_NOT_FOUND, uid,
                -1);
		}
		return users.get(uid);
	}
	
	public MockUserLookup() {
		
		String tz = "Europe/Berlin";
		
		int i = 0;
		MockUser user = new MockUser();
		user.setId(++i);
		user.setDisplayName("The Mailadmin");
		user.setPreferredLanguage("en_US");
		user.setTimeZone(tz);
		user.setMail("mailadmin@test.invalid");
		user.setGroups(new int[]{1});
		addUser(user);
		
		user = new MockUser();
		user.setId(++i);
		user.setDisplayName("User 1");
		user.setPreferredLanguage("en_US");
		user.setTimeZone(tz);
		user.setMail("user1@test.invalid");
		user.setGroups(new int[]{1,4});
		addUser(user);
		
		user = new MockUser();
		user.setId(++i);
		user.setDisplayName("User 2");
		user.setPreferredLanguage("de_DE");
		user.setTimeZone(tz);
		user.setMail("user2@test.invalid");
		user.setGroups(new int[]{1,2});
		addUser(user);
		
		user = new MockUser();
		user.setId(++i);
		user.setDisplayName("User 3");
		user.setPreferredLanguage("de_DE");
		user.setTimeZone("Pacific/Samoa");
		user.setMail("user3@test.invalid");
		user.setGroups(new int[]{1,4});
		addUser(user);
		
		user = new MockUser();
		user.setId(++i);
		user.setDisplayName("User 4");
		user.setPreferredLanguage("de_DE");
		user.setTimeZone(tz);
		user.setMail("user4@test.invalid");
		user.setGroups(new int[]{1,2,3});
		addUser(user);
		
		user = new MockUser();
		user.setId(++i);
		user.setDisplayName("User 5");
		user.setPreferredLanguage("en_US");
		user.setTimeZone(tz);
		user.setMail("user5@test.invalid");
		user.setGroups(new int[]{1,3,4});
		addUser(user);
		
		user = new MockUser();
		user.setId(++i);
		user.setDisplayName("User 6");
		user.setPreferredLanguage("de_DE");
		user.setTimeZone(tz);
		user.setMail("user6@test.invalid");
		user.setGroups(new int[]{1,2});
		addUser(user);
		
		user = new MockUser();
		user.setId(++i);
		user.setDisplayName("User 7");
		user.setPreferredLanguage("en_US");
		user.setTimeZone(tz);
		user.setMail("user7@test.invalid");
		user.setGroups(new int[]{1,4});
		addUser(user);
		
		user = new MockUser();
		user.setId(++i);
		user.setDisplayName("User 8");
		user.setPreferredLanguage("de_DE");
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

	private void addUser(User user) {
		users.put(user.getId(),user);
	}

}
