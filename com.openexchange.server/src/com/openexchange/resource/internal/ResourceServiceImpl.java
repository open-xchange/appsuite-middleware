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

package com.openexchange.resource.internal;

import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceService;
import com.openexchange.resource.storage.ResourceStorage;

/**
 * {@link ResourceServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ResourceServiceImpl implements ResourceService {

    /**
     * The permission path to access create, update, and delete methods
     */
    static final String PATH = "com.openexchange.resource.managerequest";

    private static final ResourceServiceImpl instance = new ResourceServiceImpl();

    /**
     * Gets the singleton instance of {@link ResourceServiceImpl}
     *
     * @return The singleton instance of {@link ResourceServiceImpl}
     */
    public static ResourceServiceImpl getInstance() {
        return instance;
    }

    private ResourceServiceImpl() {
        super();
    }

    @Override
    public void create(final User user, final Context ctx, final Resource resource) throws OXException {
        new ResourceCreate(user, ctx, resource).perform();
    }

    @Override
    public void update(final User user, final Context ctx, final Resource resource, final Date clientLastModified) throws OXException {
        new ResourceUpdate(user, ctx, resource, clientLastModified).perform();
    }

    @Override
    public void delete(final User user, final Context ctx, final Resource resource, final Date clientLastModified) throws OXException {
        new ResourceDelete(user, ctx, resource, clientLastModified).perform();
    }

    @Override
    public Resource getResource(final int resourceId, final Context context) throws OXException {
        return ResourceStorage.getInstance().getResource(resourceId, context);
    }

    @Override
    public Resource[] listModified(final Date modifiedSince, final Context context) throws OXException {
        return ResourceStorage.getInstance().listModified(modifiedSince, context);
    }

    @Override
    public Resource[] listDeleted(final Date modifiedSince, final Context context) throws OXException {
        return ResourceStorage.getInstance().listDeleted(modifiedSince, context);
    }

    @Override
    public Resource[] searchResources(final String pattern, final Context context) throws OXException {
        return ResourceStorage.getInstance().searchResources(pattern, context);
    }

    @Override
    public Resource[] searchResourcesByMail(final String pattern, final Context context) throws OXException {
        return ResourceStorage.getInstance().searchResourcesByMail(pattern, context);
    }

}
