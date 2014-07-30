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

package com.openexchange.share.internal;

import com.openexchange.contact.ContactService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.rdb.ShareStorage;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;


/**
 * {@link SharePerformer}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public abstract class SharePerformer<R> {

    protected final ServerSession session;

    protected final ShareStorage storage;

    protected final ServiceLookup services;


    protected SharePerformer(ShareStorage storage, ServiceLookup services, ServerSession session) {
        super();
        this.storage = storage;
        this.services = services;
        this.session = session;
    }

    protected abstract R perform() throws OXException;

    protected ShareStorage getShareStorage() throws OXException {
        return storage;
    }

    protected UserService getUserService() throws OXException {
        return getService(UserService.class, true);
    }

    protected ContactService getContactService() throws OXException {
        return getService(ContactService.class, true);
    }

    protected FolderService getFolderService() throws OXException {
        return getService(FolderService.class, true);
    }

    protected DatabaseService getDatabaseService() throws OXException {
        return getService(DatabaseService.class, true);
    }

    protected <S> S getService(Class<S> serviceClass, boolean failIfAbsent) throws OXException {
        S service = services.getService(serviceClass);
        if (service == null && failIfAbsent) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(serviceClass.getName());
        }

        return service;
    }

}
