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

package com.openexchange.groupware.infostore.facade.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.webdav.EntityLockManager;
import com.openexchange.groupware.infostore.webdav.Lock;

/**
 * {@link LockedUntilLoader}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class LockedUntilLoader extends MetadataLoader<List<Lock>> {

    private final EntityLockManager lockManager;

    /**
     * Initializes a new {@link LockedUntilLoader}.
     *
     * @param lockManager The underlying lock manager
     */
    public LockedUntilLoader(EntityLockManager lockManager) {
        super();
        this.lockManager = lockManager;
    }

    @Override
    protected DocumentMetadata set(DocumentMetadata document, List<Lock> metadata) {
        if (null != metadata && 0 < metadata.size()) {
            long maximumTimeout = 0;
            for (Lock lock : metadata) {
                maximumTimeout = Math.max(maximumTimeout, lock.getTimeout());
            }
            document.setLockedUntil(new Date(System.currentTimeMillis() + maximumTimeout));
        }
        return document;
    }

    @Override
    public Map<Integer, List<Lock>> load(Collection<Integer> ids, Context context) throws OXException {
        if (null == ids || 0 == ids.size()) {
            return Collections.emptyMap();
        }
        return lockManager.findLocks(new ArrayList<Integer>(ids), context);
    }

}
