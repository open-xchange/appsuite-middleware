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

package com.openexchange.groupware.settings.tree.modules.mail;

import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.impl.AbstractMailFuncs;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.mail.usersetting.UserSettingMail;

/**
 * {@link AllowHTMLImages} - Requests or changes whether user allowed to display
 * images appearing in HTML content of a message or not.
 * <p>
 * Path in config tree:<br>
 * <code>modules -&gt; mail -&gt; allowhtmlimages</code>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AllowHTMLImages implements PreferencesItemService {

	/**
	 * Default constructor.
	 */
	public AllowHTMLImages() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    public String[] getPath() {
		return new String[] { "modules", "mail", "allowhtmlimages" };
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
				return Boolean.valueOf(settings.isAllowHTMLImages());
			}

			@Override
			protected void setValue(final UserSettingMail settings, final String value) {
				settings.setAllowHTMLImages(Boolean.parseBoolean(value));
			}
		};
	}
}
