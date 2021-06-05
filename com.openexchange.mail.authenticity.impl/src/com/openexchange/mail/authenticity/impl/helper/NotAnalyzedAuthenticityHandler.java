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

package com.openexchange.mail.authenticity.impl.helper;

import java.util.Collection;
import java.util.Collections;
import com.openexchange.mail.MailField;
import com.openexchange.mail.authenticity.MailAuthenticityHandler;
import com.openexchange.mail.dataobjects.MailAuthenticityResult;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.session.Session;


/**
 * {@link NotAnalyzedAuthenticityHandler} - The special handler, which marks every mail as not-analyzed.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class NotAnalyzedAuthenticityHandler implements MailAuthenticityHandler {

    private static final NotAnalyzedAuthenticityHandler INSTANCE = new NotAnalyzedAuthenticityHandler();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static NotAnalyzedAuthenticityHandler getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link NotAnalyzedAuthenticityHandler}.
     */
    private NotAnalyzedAuthenticityHandler() {
        super();
    }

    @Override
    public void handle(Session session, MailMessage mail) {
        if (null != mail) {
            mail.setAuthenticityResult(MailAuthenticityResult.NOT_ANALYZED_RESULT);
        }
    }

    @Override
    public Collection<MailField> getRequiredFields() {
        return Collections.emptyList();
    }

    @Override
    public Collection<String> getRequiredHeaders() {
        return Collections.emptyList();
    }

    @Override
    public boolean isEnabled(Session session) {
        return true;
    }

    @Override
    public int getRanking() {
        return 0;
    }

}
