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

package com.openexchange.mail.compose.mailstorage;

import java.io.File;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link ThresholdFileHolderFactory} - A factory for file holder instances.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class ThresholdFileHolderFactory {

    private static final ThresholdFileHolderFactory INSTANCE = new ThresholdFileHolderFactory();

    /**
     * Gets the factory instance.
     *
     * @return The factory instance
     */
    public static ThresholdFileHolderFactory getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link ThresholdFileHolderFactory}.
     */
    private ThresholdFileHolderFactory() {
        super();
    }

    /**
     * Create a new auto-managed file holder.
     *
     * @param session The session
     * @return The newly created file holder
     * @throws OXException If file holder cannot be created
     */
    public ThresholdFileHolder createFileHolder(Session session) throws OXException {
        return createFileHolder(session, true);
    }

    /**
     * Create a new file holder.
     *
     * @param session The session
     * @param automanaged Whether the file holder shall be auto-managed
     * @return The newly created file holder
     * @throws OXException If file holder cannot be created
     */
    public ThresholdFileHolder createFileHolder(Session session, boolean automanaged) throws OXException {
        if (session == null) {
            throw new IllegalArgumentException("Session must not be null");
        }
        return createFileHolder(session.getUserId(), session.getContextId(), automanaged);
    }

    /**
     * Create a new file holder.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param automanaged Whether the file holder shall be auto-managed
     * @return The newly created file holder
     * @throws OXException If file holder cannot be created
     */
    public ThresholdFileHolder createFileHolder(int userId, int contextId, boolean automanaged) throws OXException {
        MailStorageCompositionSpaceConfig config = MailStorageCompositionSpaceConfig.getInstance();
        int memoryThreshold = config.getInMemoryThreshold(userId, contextId);
        File spoolDirectory = config.getSpoolDirectory();
        return new ThresholdFileHolder(memoryThreshold, -1, automanaged, spoolDirectory);
    }

}
