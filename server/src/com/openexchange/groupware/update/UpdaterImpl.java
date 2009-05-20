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

package com.openexchange.groupware.update;

import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.update.exception.Classes;
import com.openexchange.groupware.update.exception.SchemaException;
import com.openexchange.groupware.update.exception.UpdateException;
import com.openexchange.groupware.update.exception.UpdateExceptionFactory;

/**
 * Implementation for the updater interface.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
@OXExceptionSource(classId = Classes.UPDATER_IMPL, component = EnumComponent.UPDATE)
public class UpdaterImpl extends Updater {

    /**
     * For creating exceptions.
     */
    private static final UpdateExceptionFactory EXCEPTION = new UpdateExceptionFactory(UpdaterImpl.class);

    /**
     * Default constructor.
     */
    public UpdaterImpl() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLocked(final Context context) throws UpdateException {
        return getSchema(context).isLocked();
    }

    /**
     * {@inheritDoc}
     */
    @OXThrows(
            category = Category.CODE_ERROR,
            desc = "",
            exceptionId = 1,
            msg = "Update process initialization failed: %1$s."
    )
    @Override
    public void startUpdate(final Context context) throws UpdateException {
        UpdateProcess process;
        try {
            process = new UpdateProcess(context);
        } catch (final SchemaException e) {
            throw EXCEPTION.create(1, e, e.getMessage());
        }
        final Thread thread = new Thread(process);
        thread.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean toUpdate(final Context context) throws UpdateException {
        return toUpdateInternal(getSchema(context));
    }

    private static final boolean toUpdateInternal(final Schema schema) {
        return (UpdateTaskCollection.getHighestVersion() > schema.getDBVersion());
    }

    private Schema getSchema(Context context) throws UpdateException {
        final Schema schema;
        try {
            final SchemaStore store = SchemaStore.getInstance(SchemaStoreImpl.class.getName());
            schema = store.getSchema(context);
        } catch (final SchemaException e) {
            throw new UpdateException(e);
        }
        return schema;
    }

    private Schema getSchema(int poolId, String schemaName) throws UpdateException {
        final Schema schema;
        try {
            final SchemaStore store = SchemaStore.getInstance(SchemaStoreImpl.class.getName());
            schema = store.getSchema(poolId, schemaName);
        } catch (final SchemaException e) {
            throw new UpdateException(e);
        }
        return schema;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isLocked(final String schema, final int writePoolId) throws UpdateException {
        return getSchema(writePoolId, schema).isLocked();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean toUpdate(final String schema, final int writePoolId) throws UpdateException {
        return toUpdateInternal(getSchema(writePoolId, schema));
    }
}
