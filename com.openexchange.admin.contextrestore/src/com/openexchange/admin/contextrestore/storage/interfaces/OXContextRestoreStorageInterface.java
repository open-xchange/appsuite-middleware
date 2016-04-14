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

/**
 *
 */
package com.openexchange.admin.contextrestore.storage.interfaces;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import com.openexchange.admin.contextrestore.rmi.exceptions.OXContextRestoreException;
import com.openexchange.admin.contextrestore.rmi.impl.OXContextRestore.Parser.PoolIdSchemaAndVersionInfo;
import com.openexchange.admin.contextrestore.tools.PropertyHandlerExtended;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * This factory class provides access to the right storage layer
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public abstract class OXContextRestoreStorageInterface {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OXContextRestoreStorageInterface.class);

    private static volatile OXContextRestoreStorageInterface instance;

    /**
     * Creates a new instance implementing the group storage interface.
     * @return an instance implementing the group storage interface.
     * @throws com.openexchange.admin.rmi.exceptions.StorageException Storage exception
     */
    public static OXContextRestoreStorageInterface getInstance() throws StorageException {
        OXContextRestoreStorageInterface inst = instance;
        if (null == inst) {
            synchronized (OXContextRestoreStorageInterface.class) {
                inst = instance;
                if (null == inst) {
                    Class<? extends OXContextRestoreStorageInterface> implementingClass;
                    PropertyHandlerExtended prop = new PropertyHandlerExtended(System.getProperties());
                    final String className = prop.getProp(PropertyHandlerExtended.CONTEXT_RESTORE_STORAGE, null);
                    if (null != className) {
                        try {
                            implementingClass = Class.forName(className).asSubclass(OXContextRestoreStorageInterface.class);
                        } catch (final ClassNotFoundException e) {
                            log.error("", e);
                            throw new StorageException(e);
                        }
                    } else {
                        final StorageException storageException = new StorageException("Property for user_storage not defined");
                        log.error("", storageException);
                        throw storageException;
                    }

                    Constructor<? extends OXContextRestoreStorageInterface> cons;
                    try {
                        cons = implementingClass.getConstructor(new Class[] {});
                        inst = cons.newInstance(new Object[] {});
                        instance = inst;
                    } catch (final SecurityException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (final NoSuchMethodException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (final IllegalArgumentException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (final InstantiationException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (final IllegalAccessException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    } catch (final InvocationTargetException e) {
                        log.error("", e);
                        throw new StorageException(e);
                    }
                }
            }
        }
        return inst;
    }

    public abstract String restorectx(final Context ctx, final PoolIdSchemaAndVersionInfo poolidandschema, String configdbname) throws SQLException, IOException, OXContextRestoreException, StorageException;

}
