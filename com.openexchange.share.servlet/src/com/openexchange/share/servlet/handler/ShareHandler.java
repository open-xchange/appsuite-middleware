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

package com.openexchange.share.servlet.handler;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.exception.OXException;

/**
 * {@link ShareHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public interface ShareHandler {

    /**
     * Gets the ranking for this handler.
     * <p>
     * The default ranking is zero (<tt>0</tt>). A handler with a ranking of {@code Integer.MAX_VALUE} is very likely to be returned as the
     * appropriate handler, whereas a handler with a ranking of {@code Integer.MIN_VALUE} is very unlikely to be returned.
     *
     * @return The ranking
     */
    int getRanking();

    /**
     * Handles the given share.
     * <p>
     * If this handler feels responsible for the given share <code>{@link ShareHandlerReply#ACCEPT}</code> is returned; otherwise <code>{@link ShareHandlerReply#NEUTRAL}</code> to let the share be handled by the next handler in chain.
     * <p>
     * In case this handler wants to abort further handling of the share, <code>{@link ShareHandlerReply#DENY}</code> is returned.
     *
     * @param shareRequest The share request
     * @param request The associated HTTP request
     * @param response The associated HTTP response
     * @return One of <code>{@link ShareHandlerReply#DENY}</code>, <code>{@link ShareHandlerReply#NEUTRAL}</code>, or <code>{@link ShareHandlerReply#ACCEPT}</code>.
     * @throws OXException If the attempt to resolve given share fails
     */
    ShareHandlerReply handle(AccessShareRequest shareRequest, HttpServletRequest request, HttpServletResponse response) throws OXException;

    /**
     * Handles the "share not found" case.
     *
     * @param request The associated HTTP request
     * @param response The associated HTTP response
     * @param status The share status
     * @return {@link ShareHandlerReply#ACCEPT} if handled, {@link ShareHandlerReply#NEUTRAL}, otherwise
     * @throws IOException
     */
    ShareHandlerReply handleNotFound(HttpServletRequest request, HttpServletResponse response, String status) throws IOException;

}
