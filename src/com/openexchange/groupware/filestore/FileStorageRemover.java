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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.filestore;

import java.sql.Connection;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedException;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.FileStorageException;

/**
 * This class implements a delete listener and removes the directories of the
 * file store if the context is deleted.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class FileStorageRemover implements DeleteListener {

    /**
     * Default constructor.
     */
    public FileStorageRemover() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public void deletePerformed(final DeleteEvent deleteEvent,
        final Connection readCon, final Connection writeCon)
        throws DeleteFailedException {
        switch (deleteEvent.getType()) {
        case DeleteEvent.TYPE_CONTEXT:
            removeFileStorage(deleteEvent.getContext());
            break;
        default:
            break;
        }
    }

    private void removeFileStorage(final Context ctx) throws DeleteFailedException {
        try {
            final FileStorage stor = getFileStorage(ctx);
            stor.remove();
        } catch (FileStorageException e) {
            throw new DeleteFailedException(e);
        } catch (FilestoreException e) {
            throw new DeleteFailedException(e);
        }
    }

    private FileStorage getFileStorage(final Context ctx)
        throws FileStorageException, FilestoreException {
        return FileStorage.getInstance(FilestoreStorage.createURI(ctx), ctx,
            new DBPoolProvider()); 
    }
}
