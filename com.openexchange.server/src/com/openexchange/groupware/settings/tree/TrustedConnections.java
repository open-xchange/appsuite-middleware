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

package com.openexchange.groupware.settings.tree;

import java.util.Iterator;
import java.util.Set;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.impl.AbstractUserFuncs;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.session.Session;
import com.openexchange.user.UserService;

/**
 * 
 * {@link TrustedConnections}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public final class TrustedConnections implements PreferencesItemService {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TrustedConnections.class);

    private static final String NAME = "trustedConnections";
    private UserService userService;
    private ConfigViewFactory configView;

    /**
     * Default constructor.
     */
    public TrustedConnections(UserService userService, ConfigViewFactory configView) {
        super();
        this.userService = userService;
        this.configView = configView;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getPath() {
        return new String[] { NAME };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IValueHandler getSharedValue() {
        return new AbstractUserFuncs() {

            @Override
            public void writeValue(Session session, Context ctx, User user, Setting setting) throws OXException {
                Boolean activated = configView.getView(user.getId(), ctx.getContextId()).property("com.openexchange.net.ssl.user.configuration.enabled", Boolean.class).get();
                if ((activated == null) || !(activated.booleanValue())) {
                    LOG.info("Setting {} has been disabled due to configuration ('com.openexchange.net.ssl.user.configuration.enabled'). The request will be ignored.", NAME);
                    return;
                }

                userService.setUserAttribute(NAME, setting.getSingleValue().toString(), user.getId(), ctx);
            }

            @Override
            public boolean isWritable() {
                return true;
            }

            /**
             * Decides whether this config option should be provided within the initial jslob response
             */
            @Override
            public boolean isAvailable(final UserConfiguration userConfig) {
                try {
                    Boolean activated = configView.getView(userConfig.getUserId(), userConfig.getContext().getContextId()).property("com.openexchange.net.ssl.user.configuration.enabled", Boolean.class).get();
                    if (activated != null) {
                        return activated.booleanValue();
                    }
                    return false;
                } catch (OXException e) {
                    LOG.error("Unable to retrieve configuration for user related SSL configuration", e);
                }
                return false;
            }

            @Override
            public void getValue(final Session session, final Context ctx, final User user, final UserConfiguration userConfig, final Setting setting) throws OXException {
                Boolean activated = configView.getView(user.getId(), ctx.getContextId()).property("com.openexchange.net.ssl.user.configuration.enabled", Boolean.class).get();
                if ((activated == null) || !(activated.booleanValue())) {
                    LOG.debug("Reading {} has been disabled due to configuration ('com.openexchange.net.ssl.user.configuration.enabled'). The request will be ignored.", NAME);
                    return;
                }
                
                Set<String> set = user.getAttributes().get(NAME);
                if (set == null) {
                    setting.setSingleValue(Boolean.FALSE);
                    return;
                }
                Iterator iter = set.iterator();
                Object next = iter.next();
                setting.setSingleValue(next);
            }
        };
    }
}
