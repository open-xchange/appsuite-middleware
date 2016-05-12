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

package com.openexchange.mail.json.compose.share.settings;

import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.ReadOnlyValue;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.jslob.ConfigTreeEquivalent;
import com.openexchange.mail.json.compose.share.ShareComposeHandler;
import com.openexchange.session.Session;

/**
 * {@link AbstractShareComposeSetting}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public abstract class AbstractShareComposeSetting<V> implements PreferencesItemService, ConfigTreeEquivalent {

    /** The logger constant */
    static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractShareComposeSetting.class);

    /** The name in setting's path; e.g. <code>"name"</code> */
    protected final String nameInPath;

    /** The share compose handler */
    protected final ShareComposeHandler shareComposeHandler;

    /**
     * Initializes a new {@link AbstractShareComposeSetting}.
     */
    protected AbstractShareComposeSetting(String nameInPath, ShareComposeHandler shareComposeHandler) {
        super();
        this.nameInPath = nameInPath;
        this.shareComposeHandler = shareComposeHandler;
    }

    /**
     * gets the value for this setting.
     *
     * @param session The session
     * @param ctx The context
     * @param user The user
     * @param userConfig The user configuration
     * @return The value
     * @throws OXException If value cannot be returned
     */
    protected abstract V getSettingValue(Session session, Context ctx, User user, UserConfiguration userConfig) throws OXException;

    // modules/mail/vcard > io.ox/mail//appendVcard

    @Override
    public String[] getPath() {
        return new String[] { "modules", "mail", "compose", "shareAttachments", nameInPath };
    }

    @Override
    public IValueHandler getSharedValue() {
        return new ReadOnlyValue() {

            @Override
            public void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws OXException {
                Object value = getSettingValue(session, ctx, user, userConfig);
                setting.setSingleValue(value);
            }

            @Override
            public boolean isAvailable(UserConfiguration userConfig) {
                return true;
            }

        };
    }

    @Override
    public String getConfigTreePath() {
        return "modules/mail/compose/shareAttachments/" + nameInPath;
    }

    @Override
    public String getJslobPath() {
        return "io.ox/mail//compose/shareAttachments/" + nameInPath;
    }
}
