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

package com.openexchange.multifactor.storage;

import java.util.Optional;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.MultifactorRequest;
import com.openexchange.multifactor.MultifactorToken;

/**
 * {@link MultifactorTokenStorage} - A two layered storage which stores {@link MultifactorToken}s for a session associated with a given key.
 * <br>
 * A token is associated with exactly one key.
 * <br>
 * A token is associated with exactly one session.
 * <br>
 * A session can be associated with more than one token.
 *
 * @param <T> The type of the token to store.
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public interface MultifactorTokenStorage<T extends MultifactorToken<?>> {

    /**
     * Gets the token for the given key and session
     *
     * @param multifactorRequest The {@link MultifactorRequest} owning the token
     * @param key The key of the token to get
     * @return An {@link Optional} containing the Token associated with the specified key for the given session or an empty {@link Optional}
     * @throws OXException
     */
    Optional<T> getAndRemove(MultifactorRequest multifactorRequest, String key) throws OXException;

    /**
     * Adds a new token for the given key and session
     *
     * @param multifactorRequest The {@link MultifactorRequest} to add the token for
     * @param key the key to add the token for
     * @param token The token to add
     * @throws OXException
     */
    void add(MultifactorRequest multifactorRequest, String key, T token) throws OXException;

    /**
     * Gets the number of active tokens for the given session
     *
     * @param multifactorRequest The {@link MultifactorRequest} to get the active tokens for
     * @return The amount of the active tokens for the given session
     * @throws OXException
     */
    int getTokenCount(MultifactorRequest multifactorRequest) throws OXException;
}
