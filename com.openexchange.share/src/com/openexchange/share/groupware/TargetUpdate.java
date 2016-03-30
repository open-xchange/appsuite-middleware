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

package com.openexchange.share.groupware;

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.share.ShareTarget;


/**
 * {@link TargetUpdate} allows to modify the groupware items for one or more share targets within a
 * single transaction. It behaves module-independent, i.e. you can modify items from different modules
 * within one update call.<br>
 * <br>
 * Using an instance of this class requires a specific call semantic:<br>
 * <pre>
 * TargetUpdate update = moduleSupport.prepareUpdate(session, writeCon);
 * try {
 *     update.fetch(targetsToModify);
 *     for (ShareTarget target : targetsToModify) {
 *         TargetProxy proxy = update.get(target);
 *         // apply modifications to proxy
 *     }
 *     update.run();
 * } finally {
 *     update.close();
 * }
 * </pre>
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public interface TargetUpdate {

    /**
     * Fetches all groupware items denoted by the given share targets. Subsequently a {@link TargetProxy}
     * instance can be obtained for every passed target by calling {@link #get(ShareTarget)}.
     *
     * @param targets The share targets to fetch
     * @throws OXException if fetching any target fails
     */
    void fetch(Collection<ShareTarget> targets) throws OXException;

    /**
     * Obtains a proxy object for the given share target.
     *
     * @param target The target
     * @return The proxy instance
     */
    TargetProxy get(ShareTarget target);

    /**
     * Runs the update, i.e. modifications of objects are applied to the underlying module storages for
     * every obtained {@link TargetProxy} whose {@link TargetProxy#wasModified()} method returns <code>true</code>.
     *
     * @throws OXException if any update call fails
     */
    void run() throws OXException;

    /**
     * Closes this instance and cleans up any open resources.
     */
    void close();

}
