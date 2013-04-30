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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.drive.sync;

import java.util.Map.Entry;
import org.apache.commons.logging.Log;
import com.openexchange.drive.DriveVersion;
import com.openexchange.drive.comparison.Change;
import com.openexchange.drive.comparison.ThreeWayComparison;
import com.openexchange.drive.comparison.VersionMapper;
import com.openexchange.drive.internal.DriveSession;
import com.openexchange.exception.OXException;


/**
 * {@link Synchronizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class Synchronizer<T extends DriveVersion> {

    protected static final Log LOG = com.openexchange.log.Log.loggerFor(Synchronizer.class);

    protected final DriveSession session;
    protected final VersionMapper<T> mapper;

    public Synchronizer(DriveSession session, VersionMapper<T> mapper) throws OXException {
        super();
        this.session = session;
        this.mapper = mapper;
    }

    public SyncResult<T> sync() throws OXException {
        SyncResult<T> result = new SyncResult<T>();
        for (Entry<String, ThreeWayComparison<T>> entry : mapper) {
            process(result, entry.getValue());
        }
        return result;
    }

    private void process(SyncResult<T> result, ThreeWayComparison<T> comparison) throws OXException {
        Change clientChange = comparison.getClientChange();
        Change serverChange = comparison.getServerChange();
        T clientVersion = comparison.getClientVersion();
        T serverVersion = comparison.getServerVersion();
        T originalVersion = comparison.getOriginalVersion();
        if (Change.NONE == clientChange && Change.NONE == serverChange) {
            /*
             * nothing to do
             */
            return;
        } else if (Change.NONE == clientChange && Change.NONE != serverChange) {
            /*
             * process server-only change
             */
            processServerChange(result, serverChange, originalVersion, clientVersion, serverVersion);
        } else if (Change.NONE != clientChange && Change.NONE == serverChange) {
            /*
             * process client-only change
             */
            processClientChange(result, clientChange, originalVersion, clientVersion, serverVersion);
        } else {
            /*
             * process changes on both sides
             */
            processConflictingChange(result, clientChange, serverChange, originalVersion, clientVersion, serverVersion);
        }
    }

    protected abstract void processServerChange(SyncResult<T> result, Change serverChange, T originalVersion, T clientVersion, T serverVersion) throws OXException;

    protected abstract void processClientChange(SyncResult<T> result, Change clientChange, T originalVersion, T clientVersion, T serverVersion) throws OXException;

    protected abstract void processConflictingChange(SyncResult<T> result, Change clientChange, Change serverChange, T originalVersion, T clientVersion, T serverVersion) throws OXException;

}
