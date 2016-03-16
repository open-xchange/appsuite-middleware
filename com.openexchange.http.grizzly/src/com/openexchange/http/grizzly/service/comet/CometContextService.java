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

package com.openexchange.http.grizzly.service.comet;

import org.glassfish.grizzly.comet.CometContext;
import org.glassfish.grizzly.comet.CometEngine;
import org.glassfish.grizzly.comet.NotificationHandler;

/**
 * {@link CometContextService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface CometContextService {

    /**
     * Registers a context path with the underlying comet engine using a default notification handler based on an comet-internal
     * thread pool.
     *
     * @param topic The topic used to register the context
     * @return CometContext A new configured comet context, or the existing one if it was registered previously.
     * @see CometEngine#register(String)
     */
    <E> CometContext<E> register(String topic);

    /**
     * Registers a context path with the underlying comet engine using a new instance of the supplied notification handler.
     *
     * @param topic The topic used to register the context
     * @param notificationClass The type of the desired notification handler
     * @return CometContext A new configured comet context, or the existing one if it was registered previously.
     * @see CometEngine#register(String, Class)
     */
    <E> CometContext<E> register(String topic, Class<? extends NotificationHandler> notificationClass);

    /**
     * Gets the comet context associated with the topic.
     *
     * @param topic The topic used to create the comet context
     * @see CometEngine#getCometContext(String)
     */
    <E> CometContext<E> getCometContext(String topic);

    /**
     * Unregisters and removes a previously registered comet context.
     *
     * @param topic The topic used to register the context
     * @return The removed comet context
     * @see CometEngine#deregister(String)
     */
    <E> CometContext<E> deregister(String topic);

}
