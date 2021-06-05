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

package com.openexchange.oauth;

import java.sql.Connection;
import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * {@link OAuthAccountDeleteListener} - Listener interface for OAuth account deletion.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface OAuthAccountDeleteListener {

    /**
     * Handles the event <i>before</i> the denoted OAuth account is deleted.
     *
     * @param id The OAuth account ID
     * @param eventProps Optional properties for delete event
     * @param user The user ID
     * @param cid The context ID
     * @param con The used connection <i>in transactional state</i>
     * @throws OXException If a critical error occurs which should abort OAuth account deletion
     */
    public void onBeforeOAuthAccountDeletion(int id, Map<String, Object> eventProps, int user, int cid, Connection con) throws OXException;

    /**
     * Handles the event <i>after</i> the denoted OAuth account is deleted.
     *
     * @param id The OAuth account ID
     * @param eventProps Optional properties for delete event
     * @param user The user ID
     * @param cid The context ID
     * @param con The used connection <i>in transactional state</i>
     * @throws OXException If a critical error occurs which should abort OAuth account deletion
     */
    public void onAfterOAuthAccountDeletion(int id, Map<String, Object> eventProps, int user, int cid, Connection con) throws OXException;

}
