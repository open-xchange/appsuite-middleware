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
import java.util.Arrays;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.passwordchange.PasswordChangeEvent;
import com.openexchange.passwordchange.PasswordChangeService;
import com.openexchange.passwordchange.script.services.SPWServiceRegistry;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.user.UserService;

/**
 * {@link ScriptPasswordChange}
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 */
public final class ScriptPasswordChange extends PasswordChangeService {

	private static final org.apache.commons.logging.Log LOG = com.openexchange.log.LogFactory
			.getLog(ScriptPasswordChange.class);

	/**
	 * Initializes a new {@link ScriptPasswordChange}
	 */
	public ScriptPasswordChange() {
		super();
	}

	private String getShellCommand() throws OXException {
		final ConfigurationService configservice = SPWServiceRegistry
				.getServiceRegistry().getService(ConfigurationService.class,
						true);
		return configservice
				.getProperty("com.openexchange.passwordchange.script.shellscript");
	}

	@Override
	protected void update(final PasswordChangeEvent event) throws OXException {

		String shellscript_to_execute = null;
		try {
			shellscript_to_execute = getShellCommand();
		} catch (final OXException e1) {
			throw new OXException(e1);
		}
		User user = null;
		{
			final UserService userService = getServiceRegistry().getService(UserService.class);
			if (userService == null) {
				throw new OXException(ServiceExceptionCode.SERVICE_UNAVAILABLE.create(UserService.class.getName()));
			}
			user = userService.getUser(event.getSession().getUserId(), event.getContext());

		}
		final String usern = user.getLoginInfo();
		final String oldpw = event.getOldPassword();
		final String newpw = event.getNewPassword();
		final String cid = event.getContext().getContextId()+"";
		final String userid = user.getId()+"";

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

		final String[] cmd = new String[11];
		cmd[0] = shellscript_to_execute; // the script, after that, the parameter
		cmd[1] = "--cid";
		cmd[2] = cid;
		cmd[3] = "--username";
		cmd[4] = usern;
		cmd[5] = "--userid";
		cmd[6] = userid;
		cmd[7] = "--oldpassword";
		cmd[8] = oldpw;
		cmd[9] = "--newpassword";
		cmd[10] = newpw; //

		LOG.debug("Executing following command to change password: "+Arrays.toString(cmd));

		try {
		    final int ret = executePasswordUpdateShell(cmd);
		    if(ret!=0) {
		        LOG.error("Passwordchange script returned exit code != 0, ret="+ret);
		        switch(ret){
		        case 1:
		            throw PasswordExceptionCode.PASSWORD_FAILED.create(" failed with return code "+ret+" ");
		        case 2:
		            throw PasswordExceptionCode.PASSWORD_SHORT.create();
		        case 3:
		            throw PasswordExceptionCode.PASSWORD_WEAK.create();
		        case 4:
		            throw PasswordExceptionCode.PASSWORD_NOUSER.create();
		        case 5:
		            throw PasswordExceptionCode.LDAP_ERROR.create();
		        default:
		            throw ServiceExceptionCode.IO_ERROR.create();
		        }
		    }
		} catch (final IOException e) {
			LOG.fatal("IO error while changing password for user "+usern+" in context "+cid+"\n",e);
			throw new OXException(ServiceExceptionCode.IO_ERROR.create( e));
		} catch (final InterruptedException e) {
            // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
            Thread.currentThread().interrupt();
			LOG.fatal("Error while changing password for user "+usern+" in context "+cid+"\n",e);
			throw new OXException(ServiceExceptionCode.IO_ERROR.create( e));
		}

	}

	private int executePasswordUpdateShell(final String[] cmd) throws IOException, InterruptedException {

		final Runtime rt = Runtime.getRuntime();
		final Process proc = rt.exec(cmd);
		final InputStream stderr = proc.getInputStream();
		final InputStreamReader isr = new InputStreamReader(stderr);
		final BufferedReader br = new BufferedReader(isr);
		String line = null;

		while ((line = br.readLine()) != null){
			LOG.debug("PWD CHANGE: "+line);
		}

		return proc.waitFor();


	}

}
