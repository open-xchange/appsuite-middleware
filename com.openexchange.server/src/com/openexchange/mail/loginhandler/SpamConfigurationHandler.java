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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.mail.loginhandler;

import org.slf4j.Logger;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginResult;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;

/**
 * 
 * {@link SpamConfigurationHandler} - rewrites user permission bit if it is defined via ConfigCascade. Preferred configuration is taken from UserSettingMail; ConfigCascade will only be used if the UserSettingMail permission bit is false.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class SpamConfigurationHandler implements LoginHandlerService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SpamConfigurationHandler.class);

    protected static final String SPAM_ENABLED = "com.openexchange.spamhandler.enabled";

    private final ConfigViewFactory configViewFactory;

    public SpamConfigurationHandler(ConfigViewFactory configViewFactory) {
        super();
        this.configViewFactory = configViewFactory;
    }

    @Override
    public void handleLogin(LoginResult login) throws OXException {
        if (login == null) {
            LOG.debug("Provided LoginResult is null. Cannot update UserSettingMail permission bit.");
            return;
        }
        User user = login.getUser();
        Context ctx = login.getContext();

        if ((user == null) || (ctx == null)) {
            LOG.warn("Unable to update 'spam enabled' user permission bit. User and/or context is null.");
            return;
        }
        UserSettingMailStorage usmStorage = UserSettingMailStorage.getInstance();
        UserSettingMail userSettingMail = usmStorage.loadUserSettingMail(user.getId(), login.getContext());
        if (userSettingMail.isSpamOptionEnabled()) {
            LOG.debug("Spam handling already enabled by configuration. Skip reset.");
            return;
        }

        ConfigView configView = this.configViewFactory.getView(user.getId(), ctx.getContextId());
        Boolean spamEnabledByConfig = configView.get(SPAM_ENABLED, Boolean.class);
        if (spamEnabledByConfig == null) {
            LOG.debug("Unable to update 'spam enabled' user permission bit. No config for user {} in context {} available. Spam handling will be disabled.", user.getId(), ctx.getContextId());
            return;
        }
        boolean boolSpamEnabledByConfig = spamEnabledByConfig.booleanValue();
        if (userSettingMail.isSpamEnabled() != boolSpamEnabledByConfig) {
            userSettingMail.setSpamEnabled(boolSpamEnabledByConfig);
            usmStorage.saveUserSettingMail(userSettingMail, user.getId(), ctx);
        }
    }

    @Override
    public void handleLogout(LoginResult logout) throws OXException {
        // nothing to do
    }
}
