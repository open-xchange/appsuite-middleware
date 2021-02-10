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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.contact.provider.composition.impl.idmangling;

import java.util.Date;
import java.util.List;
import com.openexchange.contact.common.ContactsFolder;
import com.openexchange.contact.common.ContactsPermission;
import com.openexchange.contact.common.ExtendedProperties;
import com.openexchange.contact.common.UsedForSync;
import com.openexchange.exception.OXException;

/**
 * {@link IDManglingContactsFolder}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class IDManglingContactsFolder implements ContactsFolder {

    protected final String newId;
    private final ContactsFolder delegate;

    /**
     * Initializes a new {@link IDManglingContactsFolder}.
     *
     * @param delegate The contacts folder delegate
     * @param newId The new identifier to hide the delegate's one
     */
    public IDManglingContactsFolder(ContactsFolder delegate, String newId) {
        super();
        this.delegate = delegate;
        this.newId = newId;
    }

    @Override
    public String getId() {
        return newId;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Boolean isSubscribed() {
        return delegate.isSubscribed();
    }

    @Override
    public Date getLastModified() {
        return delegate.getLastModified();
    }

    @Override
    public List<ContactsPermission> getPermissions() {
        return delegate.getPermissions();
    }

    @Override
    public ExtendedProperties getExtendedProperties() {
        return delegate.getExtendedProperties();
    }

    @Override
    public OXException getAccountError() {
        return delegate.getAccountError();
    }

    @Override
    public String toString() {
        return "IDManglingContactsFolder [newId=" + newId + ", delegate=" + delegate + "]";
    }

    @Override
    public UsedForSync getUsedForSync() {
        return delegate.getUsedForSync();
    }
}
