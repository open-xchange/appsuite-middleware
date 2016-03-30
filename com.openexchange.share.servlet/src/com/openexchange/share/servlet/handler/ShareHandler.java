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

package com.openexchange.share.servlet.handler;

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

}
