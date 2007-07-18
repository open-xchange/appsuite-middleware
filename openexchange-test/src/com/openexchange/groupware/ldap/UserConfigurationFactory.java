package com.openexchange.groupware.ldap;

import java.util.HashMap;
import java.util.Map;

import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.ContextImpl;
import com.openexchange.imap.UserSettingMail;

/**
 * 
 * Provides UserConfigurations for the Users from the MockUserStorage.
 */
public class UserConfigurationFactory {
	
	private Map<Integer,UserConfiguration> configs = new HashMap<Integer,UserConfiguration>();
	
	public UserConfiguration getConfiguration(int userId) {
		return configs.get(userId);
	}
	
	public UserConfigurationFactory() {
		try {
		Context ctx = new ContextImpl(1);
		int permissions = 262143;
		MockUserLookup users = new MockUserLookup();
		
		
		User user = users.getUser(1);
		UserConfiguration config = new UserConfiguration(permissions, user.getId(), user.getGroups(), ctx);
		UserSettingMail mailSetting = new UserSettingMail();
		mailSetting.setNotifyAppointments(true);
		mailSetting.setNotifyTasks(true);
		config.setUserSettingMail(mailSetting);
		configs.put(1,config);
		
		user = users.getUser(2);
		config = new UserConfiguration(permissions, user.getId(), user.getGroups(), ctx);
		mailSetting = new UserSettingMail();
		mailSetting.setNotifyAppointments(true);
		mailSetting.setNotifyTasks(true);
		config.setUserSettingMail(mailSetting);
		configs.put(2,config);
		
		user = users.getUser(3);
		config = new UserConfiguration(permissions, user.getId(), user.getGroups(), ctx);
		mailSetting = new UserSettingMail();
		mailSetting.setNotifyAppointments(true);
		mailSetting.setNotifyTasks(true);
		config.setUserSettingMail(mailSetting);
		configs.put(3,config);
		
		user = users.getUser(4);
		config = new UserConfiguration(permissions, user.getId(), user.getGroups(), ctx);
		mailSetting = new UserSettingMail();
		mailSetting.setNotifyAppointments(true);
		mailSetting.setNotifyTasks(true);
		config.setUserSettingMail(mailSetting);
		configs.put(4,config);
		
		user = users.getUser(5);
		config = new UserConfiguration(permissions, user.getId(), user.getGroups(), ctx);
		mailSetting = new UserSettingMail();
		mailSetting.setNotifyAppointments(false);
		mailSetting.setNotifyTasks(true);
		config.setUserSettingMail(mailSetting);
		configs.put(5,config);
		
		user = users.getUser(6);
		config = new UserConfiguration(permissions, user.getId(), user.getGroups(), ctx);
		mailSetting = new UserSettingMail();
		mailSetting.setNotifyAppointments(true);
		mailSetting.setNotifyTasks(false);
		config.setUserSettingMail(mailSetting);
		configs.put(6,config);
		
		user = users.getUser(7);
		config = new UserConfiguration(permissions, user.getId(), user.getGroups(), ctx);
		mailSetting = new UserSettingMail();
		mailSetting.setNotifyAppointments(true);
		mailSetting.setNotifyTasks(true);
		config.setUserSettingMail(mailSetting);
		configs.put(7,config);
		
		user = users.getUser(8);
		config = new UserConfiguration(permissions, user.getId(), user.getGroups(), ctx);
		mailSetting = new UserSettingMail();
		mailSetting.setNotifyAppointments(true);
		mailSetting.setNotifyTasks(true);
		config.setUserSettingMail(mailSetting);
		configs.put(8,config);
		
		user = users.getUser(9);
		config = new UserConfiguration(permissions, user.getId(), user.getGroups(), ctx);
		mailSetting = new UserSettingMail();
		mailSetting.setNotifyAppointments(true);
		mailSetting.setNotifyTasks(true);
		config.setUserSettingMail(mailSetting);
		configs.put(9,config);
		
		user = users.getUser(10);
		config = new UserConfiguration(permissions, user.getId(), user.getGroups(), ctx);
		mailSetting = new UserSettingMail();
		mailSetting.setNotifyAppointments(true);
		mailSetting.setNotifyTasks(true);
		config.setUserSettingMail(mailSetting);
		configs.put(10,config);
		
		}  catch (LdapException x){
			throw new RuntimeException(x);
		}
	}
}
