/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.passwordchange.script.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.passwordchange.PasswordChangeEvent;
import com.openexchange.passwordchange.PasswordChangeService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.encoding.Base64;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link ScriptPasswordChange}
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 */
public final class ScriptPasswordChange extends PasswordChangeService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ScriptPasswordChange.class);

    private final ScriptPasswordChangeConfig config;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link ScriptPasswordChange}
     */
    public ScriptPasswordChange(ScriptPasswordChangeConfig config, ServiceLookup services) {
        super();
        this.config = config;
        this.services = services;
    }

    @Override
    protected void update(final PasswordChangeEvent event) throws OXException {
        String shellscript_to_execute = config.getScriptPath();
        if (Strings.isEmpty(shellscript_to_execute)) {
            final String message = "Shell command is empty. Please check property \"com.openexchange.passwordchange.script.shellscript\"";
            LOG.error(message);
            throw PasswordExceptionCode.PASSWORD_FAILED_WITH_MSG.create(message);
        }

        User user = null;
        {
            final UserService userService = services.getOptionalService(UserService.class);
            if (userService == null) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(UserService.class.getName());
            }
            user = userService.getUser(event.getSession().getUserId(), event.getContext());

        }
        String usern = user.getLoginInfo();
        if (null == usern) {
            final String message = "User name is null.";
            LOG.error(message);
            throw PasswordExceptionCode.PASSWORD_FAILED_WITH_MSG.create(message);
        }
        String oldpw = event.getOldPassword();
        if (null == oldpw) {
            final String message = "Old password is null.";
            LOG.error(message);
            throw PasswordExceptionCode.PASSWORD_FAILED_WITH_MSG.create(message);
        }
        String newpw = event.getNewPassword();
        if (null == newpw) {
            final String message = "New password is null.";
            LOG.error(message);
            throw PasswordExceptionCode.PASSWORD_FAILED_WITH_MSG.create(message);
        }
        final String cid = Integer.toString(event.getContext().getContextId());
        final String userid = Integer.toString(user.getId());

        // Convert UTF-8 bytes of String to base64
        boolean asBase64 = config.asBase64();
        if (asBase64) {
            usern = Base64.encode(usern);
            oldpw= Base64.encode(oldpw);
            newpw = Base64.encode(newpw);
        }

        /*
         * Update passwd via executing a shell script
         *
         * Following values must be passed to the script in given order:
         *
         * 0. cid - Context ID
         * 1. user - Username of the logged in user
         * 2. userid - User ID of the logged in user
         * 3. oldpwd - Old user password
         * 4. newpwd - New user password
         */

        final String[] cmd = new String[11];
        cmd[0] = shellscript_to_execute; // the script, after that, the parameters
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

        LOG.debug("Executing following command to change password: {}", Arrays.toString(cmd));

        try {
            final int ret = executePasswordUpdateShell(cmd);
            if (ret != 0) {
                LOG.error("Passwordchange script returned exit code != 0, ret={}", Integer.valueOf(ret));
                switch (ret) {
                    case 1:
                        throw PasswordExceptionCode.PASSWORD_FAILED.create(" failed with return code " + ret + " ");
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
        } catch (IOException e) {
            LOG.error("IO error while changing password for user {} in context {}\n", usern, cid, e);
            throw ServiceExceptionCode.IO_ERROR.create(e);
        } catch (InterruptedException e) {
            // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
            Thread.currentThread().interrupt();
            LOG.error("Error while changing password for user {} in context {}\n", usern, cid, e);
            throw ServiceExceptionCode.IO_ERROR.create(e);
        }

    }

    private int executePasswordUpdateShell(final String[] cmd) throws IOException, InterruptedException {

        final Runtime rt = Runtime.getRuntime();
        final Process proc = rt.exec(cmd);
        final InputStream stderr = proc.getInputStream();
        final InputStreamReader isr = new InputStreamReader(stderr, StandardCharsets.UTF_8);
        final BufferedReader br = new BufferedReader(isr);
        String line = null;

        while ((line = br.readLine()) != null) {
            LOG.debug("PWD CHANGE: {}", line);
        }

        return proc.waitFor();

    }

}
