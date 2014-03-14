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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.realtime.hazelcast.cleanup;

import java.io.Serializable;
import java.util.concurrent.Callable;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.realtime.cleanup.LocalRealtimeCleanup;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.hazelcast.osgi.Services;
import com.openexchange.realtime.packet.ID;

/**
 * {@link CleanupDispatcher} - Issues a cleanup on the LocalRealtimeCleanup service.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class CleanupDispatcher implements Callable<Void>, Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(CleanupDispatcher.class);
    
    private static final long serialVersionUID = 2669822501149210448L;

    private final ID id;

    /**
     * Initializes a new {@link CleanupDispatcher}.
     * 
     * @param id The ID to clean up for.
     * @param cleanupScopes The scopes to clean up on the remote machines. 
     */
    public CleanupDispatcher(ID id) {
        Validate.notNull(id, "Mandatory parameter id is missing.");
        this.id = id;
    }

    @Override
    public Void call() throws Exception {
        LocalRealtimeCleanup localRealtimeCleanup = Services.getService(LocalRealtimeCleanup.class);
        if (localRealtimeCleanup != null) {
            localRealtimeCleanup.cleanForId(id);
        } else {
            LOG.error(
                "Error while trying to cleanup for ResponseChannel ID: {}",
                id,
                RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(LocalRealtimeCleanup.class.getName()));
        }
        return null;
    }

}
