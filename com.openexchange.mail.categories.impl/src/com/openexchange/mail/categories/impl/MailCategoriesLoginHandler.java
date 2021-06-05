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

package com.openexchange.mail.categories.impl;

import com.openexchange.ajax.Client;
import com.openexchange.exception.OXException;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginResult;
import com.openexchange.session.Session;

/**
 * {@link MailCategoriesLoginHandler} initializes the MailCategoriesRuleEngine if it is the first login after activation.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class MailCategoriesLoginHandler implements LoginHandlerService {

    private final MailCategoriesConfigServiceImpl mailCategoriesService;

    /**
     * Initializes a new {@link MailCategoriesLoginHandler}.
     */
    public MailCategoriesLoginHandler(MailCategoriesConfigServiceImpl mailCategoriesService) {
        super();
        this.mailCategoriesService = mailCategoriesService;
    }

    @Override
    public void handleLogin(LoginResult login) throws OXException {
        Session session = login.getSession();
        if (false == Client.isAppSuiteUI(session)) {
            return;
        }

        mailCategoriesService.initMailCategories(session);
    }

    @Override
    public void handleLogout(LoginResult logout) {
        // nothing to do
    }

}
