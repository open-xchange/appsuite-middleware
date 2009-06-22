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

package com.openexchange.datatypes.genericonf.storage.osgi.tools;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.datatypes.genericonf.storage.GenericConfigStorageException;
import com.openexchange.datatypes.genericonf.storage.GenericConfigurationStorageService;
import com.openexchange.groupware.contexts.Context;


/**
 * {@link WhiteboardGenericConfigurationStorageService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class WhiteboardGenericConfigurationStorageService implements GenericConfigurationStorageService {

    private ServiceTracker tracker;

    public WhiteboardGenericConfigurationStorageService(BundleContext context) {
        this.tracker = new ServiceTracker(context, GenericConfigurationStorageService.class.getName(), null);
        tracker.open();
    }
    
    public void close() {
        this.tracker.close();
    }
    
    public GenericConfigurationStorageService getDelegate() {
        return (GenericConfigurationStorageService) tracker.getService();
    }
    
    public void delete(Context ctx, int id) throws GenericConfigStorageException {
        getDelegate().delete(ctx, id);
    }

    public void delete(Connection con, Context ctx, int id) throws GenericConfigStorageException {
        getDelegate().delete(con, ctx, id);
    }

    public void delete(Connection writeConnection, Context ctx) throws GenericConfigStorageException {
        getDelegate().delete(writeConnection, ctx);
    }

    public void fill(Context ctx, int id, Map<String, Object> content) throws GenericConfigStorageException {
        getDelegate().fill(ctx, id, content);
    }

    public void fill(Connection con, Context ctx, int id, Map<String, Object> content) throws GenericConfigStorageException {
        getDelegate().fill(con, ctx, id, content);
    }

    public int save(Connection con, Context ctx, Map<String, Object> content) throws GenericConfigStorageException {
        return getDelegate().save(con, ctx, content);
    }

    public int save(Context ctx, Map<String, Object> content) throws GenericConfigStorageException {
        return getDelegate().save(ctx, content);
    }

    public void update(Connection con, Context ctx, int id, Map<String, Object> content) throws GenericConfigStorageException {
        getDelegate().update(con, ctx, id, content);
    }

    public void update(Context ctx, int id, Map<String, Object> content) throws GenericConfigStorageException {
        getDelegate().update(ctx, id, content);
    }

    public List<Integer> search(Context ctx, Map<String, Object> query) throws GenericConfigStorageException {
        return getDelegate().search(ctx, query);
    }

    public List<Integer> search(Connection con, Context ctx, Map<String, Object> query) throws GenericConfigStorageException {
        return getDelegate().search(con, ctx, query);
    }

    
}
