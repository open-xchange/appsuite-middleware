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

package com.openexchange.groupware.settings.tree.modules.mail;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.impl.AbstractMailFuncs;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link UnifiedInboxEnablement}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UnifiedInboxEnablement implements PreferencesItemService {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UnifiedInboxEnablement.class);

    /**
     * Default constructor.
     */
    public UnifiedInboxEnablement() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getPath() {
        return new String[] { "modules", "mail", "unifiedinbox" };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IValueHandler getSharedValue() {
        return new AbstractMailFuncs() {

            @Override
            public boolean isAvailable(final UserConfiguration userConfig) {
                return userConfig.hasWebMail();
            }

            @Override
            protected Boolean isSet(final UserSettingMail settings) {
                if (false == MailProviderRegistry.isUnifiedMailAvailable()) {
                    return Boolean.FALSE;
                }

                final UnifiedInboxManagement management;
                try {
                    management = ServerServiceRegistry.getInstance().getService(UnifiedInboxManagement.class, true);
                } catch (final OXException e) {
                    LOG.warn("", e);
                    return Boolean.FALSE;
                }
                try {
                    return Boolean.valueOf(management.getUnifiedINBOXAccountID(settings.getUserId(), settings.getCid()) >= 0);
                } catch (final OXException e) {
                    LOG.error("", e);
                    return Boolean.FALSE;
                }
            }

            @Override
            protected void setValue(final UserSettingMail settings, final String value) {
                boolean enable = Boolean.parseBoolean(value);

                if (false == MailProviderRegistry.isUnifiedMailAvailable()) {
                    LOG.warn("{} of Unified Mail for user {} in context {} aborted: {}", enable ? "Enabling" : "Disabling", settings.getUserId(), settings.getCid(), "Not available");
                    return;
                }

                UnifiedInboxManagement management;
                try {
                    management = ServerServiceRegistry.getInstance().getService(UnifiedInboxManagement.class, true);
                } catch (final OXException e) {
                    LOG.warn("{} of Unified Mail for user {} in context {} aborted: {}", enable ? "Enabling" : "Disabling", settings.getUserId(), settings.getCid(), e.getMessage(), e);
                    return;
                }
                try {
                    final int userId = settings.getUserId();
                    final int cid = settings.getCid();
                    if (enable) {
                        if (management.getUnifiedINBOXAccountID(userId, cid) < 0) {
                            management.createUnifiedINBOX(userId, cid);
                        }
                    } else {
                        management.deleteUnifiedINBOX(userId, cid);
                    }
                } catch (final OXException e) {
                    LOG.error("", e);
                }
            }
        };
    }

}
