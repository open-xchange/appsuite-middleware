/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.passwordchange.script.impl;

import static com.openexchange.passwordchange.script.services.SPWServiceRegistry.getServiceRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;

import com.openexchange.config.ConfigurationService;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserException;
import com.openexchange.passwordchange.PasswordChangeEvent;
import com.openexchange.passwordchange.PasswordChangeService;
import com.openexchange.passwordchange.script.services.SPWServiceRegistry;
import com.openexchange.server.ServiceException;
import com.openexchange.user.UserService;

/**
 * {@link ScriptPasswordChange}
 * 
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 */
public final class ScriptPasswordChange extends PasswordChangeService {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ScriptPasswordChange.class);

	/**
	 * Initializes a new {@link ScriptPasswordChange}
	 */
	public ScriptPasswordChange() {
		super();
	}

	private String getShellCommand()
			throws com.openexchange.server.ServiceException {
		ConfigurationService configservice = SPWServiceRegistry
				.getServiceRegistry().getService(ConfigurationService.class,
						true);
		return configservice
				.getProperty("com.openexchange.passwordchange.script.shellscript");
	}

	@Override
	protected void update(final PasswordChangeEvent event) throws UserException {

		String shellscript_to_execute = null;
		try {
			shellscript_to_execute = getShellCommand();
		} catch (ServiceException e1) {
			throw new UserException(e1);
		}
		User user = null;
		{
			final UserService userService = getServiceRegistry().getService(UserService.class);
			if (userService == null) {
				throw new UserException(new ServiceException(ServiceException.Code.SERVICE_UNAVAILABLE,UserService.class.getName()));
			}
			user = userService.getUser(event.getSession().getUserId(), event.getContext());

		}
		String usern = user.getLoginInfo();
		String oldpw = event.getOldPassword();
		String newpw = event.getNewPassword();
		String cid = event.getContext().getContextId()+"";
		String userid = user.getId()+"";
		
		/*
		 * Update passwd via executing a shell script
		 * 
		 * Following values must be passed to the script in given order:
		 * 
		 *  0. cid -  Context ID 
		 *  1. user - Username of the logged in user 
		 *  2. userid - User ID of the logged in user 
		 *  3. oldpwd - Old user password 
		 *  4. newpwd - New user password
		 */
		
		String[] cmd = new String[6];
		cmd[0] = shellscript_to_execute; // the script, after that, the parameter 
		cmd[1] = cid;
		cmd[2] = usern;
		cmd[3] = userid;
		cmd[4] = oldpw;
		cmd[5] = newpw;
		
		LOG.debug("Executing following command to change password: "+Arrays.toString(cmd));
		
		try {
			if(executePasswordUpdateShell(cmd)!=0){
				LOG.error("Passwordchange script returned exit code != 0");
				throw new UserException(new ServiceException(ServiceException.Code.IO_ERROR));
			}
		} catch (IOException e) {
			LOG.fatal("IO error while changing password for user "+usern+" in context "+cid+"\n",e);
			throw new UserException(new ServiceException(ServiceException.Code.IO_ERROR, e));			
		} catch (InterruptedException e) {
			LOG.fatal("Error while changing password for user "+usern+" in context "+cid+"\n",e);
			throw new UserException(new ServiceException(ServiceException.Code.IO_ERROR, e));			
		}
		
	}

	private int executePasswordUpdateShell(String[] cmd) throws IOException, InterruptedException {
		
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec(cmd);
		InputStream stderr = proc.getInputStream();
		InputStreamReader isr = new InputStreamReader(stderr);
		BufferedReader br = new BufferedReader(isr);
		String line = null;
		
		while ((line = br.readLine()) != null){
			LOG.info("PWD CHANGE: "+line);
		}
		
		return proc.waitFor();
		

	}

}
