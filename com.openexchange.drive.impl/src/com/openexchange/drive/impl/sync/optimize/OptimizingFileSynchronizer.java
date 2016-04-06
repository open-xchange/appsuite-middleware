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

package com.openexchange.drive.impl.sync.optimize;

import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.comparison.VersionMapper;
import com.openexchange.drive.impl.internal.SyncSession;
import com.openexchange.drive.impl.sync.FileSynchronizer;
import com.openexchange.drive.impl.sync.IntermediateSyncResult;
import com.openexchange.exception.OXException;


/**
 * {@link OptimizingFileSynchronizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class OptimizingFileSynchronizer extends FileSynchronizer {

    public OptimizingFileSynchronizer(SyncSession session, VersionMapper<FileVersion> mapper, String path) throws OXException {
        super(session, mapper, path);
    }

    @Override
    public IntermediateSyncResult<FileVersion> sync() throws OXException {
        IntermediateSyncResult<FileVersion> result = super.sync();
        if (false == result.isEmpty()) {
            int lastResults = 0;
            if (session.isTraceEnabled()) {
                lastResults = result.hashCode();
                session.trace("Sync results before optimizations:\n" + result.toString());
            }
            FileActionOptimizer[] optimizers = {
                new FileRenameOptimizer(mapper),
                new EmptyFileOptimizer(mapper),
                new FileCopyOptimizer(mapper),
                new FileMultipleUploadsOptimizer(mapper),
                new FileDelayMetadataDownloadOptimizer(mapper),
                new FileOrderOptimizer(mapper),
                new FileInlineMetadataOptimizer(mapper)
            };
            for (FileActionOptimizer optimizer : optimizers) {
                result = optimizer.optimize(session, result);
                if (session.isTraceEnabled()) {
                    int currentResults = result.hashCode();
                    if (currentResults != lastResults) {
                        lastResults = currentResults;
                        session.trace("Sync results after optimizations of " + optimizer.getClass().getSimpleName() + ":\n" + result.toString());
                    }
                }
            }
        }
        return result;
    }

}
