package com.openexchange.groupware.ldap;

import com.openexchange.exception.OXException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.mail.usersetting.UserSettingMail;

/**
 *
 * Provides UserConfigurations for the Users from the MockUserStorage.
 */
public class UserConfigurationFactory {

	private final Map<Integer,UserConfiguration> configs = new HashMap<Integer,UserConfiguration>();

	private final Map<Integer,UserSettingMail> settings = new HashMap<Integer,UserSettingMail>();

	public UserConfiguration getConfiguration(final int userId) {
		return configs.get(userId);
	}

	public UserSettingMail getSetting(final int userId) {
		return settings.get(userId);
	}

	public UserConfigurationFactory() {
		try {
		final Context ctx = new ContextImpl(1);
		final int bits = 262143;
		
		Set<String> permissions = new HashSet<String>();
		for (Permission p: Permission.byBits(bits)) {
		    permissions.add(p.name().toLowerCase());
		}
		
		final MockUserLookup users = new MockUserLookup();


		User user = users.getUser(1);
		UserConfiguration config = new UserConfiguration(permissions, user.getId(), user.getGroups(), ctx);
		UserSettingMail mailSetting = new UserSettingMail(user.getId(), ctx.getContextId());
		mailSetting.setNotifyAppointments(true);
		mailSetting.setNotifyTasks(true);
        mailSetting.setNotifyAppointmentsConfirmOwner(true);
        mailSetting.setNotifyAppointmentsConfirmParticipant(true);
		//config.setUserSettingMail(mailSetting);
		configs.put(1,config);
		settings.put(1, mailSetting);

		user = users.getUser(2);
		config = new UserConfiguration(permissions, user.getId(), user.getGroups(), ctx);
		mailSetting = new UserSettingMail(user.getId(), ctx.getContextId());
		mailSetting.setNotifyAppointments(true);
		mailSetting.setNotifyTasks(true);
        mailSetting.setNotifyAppointmentsConfirmOwner(true);
        mailSetting.setNotifyAppointmentsConfirmParticipant(true);
		//config.setUserSettingMail(mailSetting);
		configs.put(2,config);
		settings.put(2, mailSetting);

		user = users.getUser(3);
		config = new UserConfiguration(permissions, user.getId(), user.getGroups(), ctx);
		mailSetting = new UserSettingMail(user.getId(), ctx.getContextId());
		mailSetting.setNotifyAppointments(true);
		mailSetting.setNotifyTasks(true);
        mailSetting.setNotifyAppointmentsConfirmOwner(true);
        mailSetting.setNotifyAppointmentsConfirmParticipant(true);
		//config.setUserSettingMail(mailSetting);
		configs.put(3,config);
		settings.put(3, mailSetting);

		user = users.getUser(4);
		config = new UserConfiguration(permissions, user.getId(), user.getGroups(), ctx);
		mailSetting = new UserSettingMail(user.getId(), ctx.getContextId());
		mailSetting.setNotifyAppointments(true);
		mailSetting.setNotifyTasks(true);
        mailSetting.setNotifyAppointmentsConfirmOwner(true);
        mailSetting.setNotifyAppointmentsConfirmParticipant(true);
		//config.setUserSettingMail(mailSetting);
		configs.put(4,config);
		settings.put(4, mailSetting);

		user = users.getUser(5);
		config = new UserConfiguration(permissions, user.getId(), user.getGroups(), ctx);
		mailSetting = new UserSettingMail(user.getId(), ctx.getContextId());
		mailSetting.setNotifyAppointments(false);
		mailSetting.setNotifyTasks(true);
        mailSetting.setNotifyAppointmentsConfirmOwner(true);
        mailSetting.setNotifyAppointmentsConfirmParticipant(true);
		//config.setUserSettingMail(mailSetting);
		configs.put(5,config);
		settings.put(5, mailSetting);

		user = users.getUser(6);
		config = new UserConfiguration(permissions, user.getId(), user.getGroups(), ctx);
		mailSetting = new UserSettingMail(user.getId(), ctx.getContextId());
		mailSetting.setNotifyAppointments(true);
		mailSetting.setNotifyTasks(false);
		mailSetting.setNotifyAppointmentsConfirmOwner(true);
        mailSetting.setNotifyAppointmentsConfirmParticipant(true);
		//config.setUserSettingMail(mailSetting);
		configs.put(6,config);
		settings.put(6, mailSetting);

		user = users.getUser(7);
		config = new UserConfiguration(permissions, user.getId(), user.getGroups(), ctx);
		mailSetting = new UserSettingMail(user.getId(), ctx.getContextId());
		mailSetting.setNotifyAppointments(true);
		mailSetting.setNotifyTasks(true);
        mailSetting.setNotifyAppointmentsConfirmOwner(true);
        mailSetting.setNotifyAppointmentsConfirmParticipant(true);
		//config.setUserSettingMail(mailSetting);
		configs.put(7,config);
		settings.put(7, mailSetting);

		user = users.getUser(8);
		config = new UserConfiguration(permissions, user.getId(), user.getGroups(), ctx);
		mailSetting = new UserSettingMail(user.getId(), ctx.getContextId());
		mailSetting.setNotifyAppointments(true);
		mailSetting.setNotifyTasks(true);
        mailSetting.setNotifyAppointmentsConfirmOwner(true);
        mailSetting.setNotifyAppointmentsConfirmParticipant(true);
		//config.setUserSettingMail(mailSetting);
		configs.put(8,config);
		settings.put(8, mailSetting);

		user = users.getUser(9);
		config = new UserConfiguration(permissions, user.getId(), user.getGroups(), ctx);
		mailSetting = new UserSettingMail(user.getId(), ctx.getContextId());
		mailSetting.setNotifyAppointments(true);
		mailSetting.setNotifyTasks(true);
        mailSetting.setNotifyAppointmentsConfirmOwner(true);
        mailSetting.setNotifyAppointmentsConfirmParticipant(true);
		//config.setUserSettingMail(mailSetting);
		configs.put(9,config);
		settings.put(9, mailSetting);

		user = users.getUser(10);
		config = new UserConfiguration(permissions, user.getId(), user.getGroups(), ctx);
		mailSetting = new UserSettingMail(user.getId(), ctx.getContextId());
		mailSetting.setNotifyAppointments(true);
		mailSetting.setNotifyTasks(true);
        mailSetting.setNotifyAppointmentsConfirmOwner(true);
        mailSetting.setNotifyAppointmentsConfirmParticipant(true);
		//config.setUserSettingMail(mailSetting);
		configs.put(10,config);
		settings.put(10, mailSetting);

		}  catch (final OXException x){
			throw new RuntimeException(x);
		}
	}
}
