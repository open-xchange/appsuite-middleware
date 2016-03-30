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

package com.openexchange.contact.storage.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.contact.storage.ContactStorage;
import com.openexchange.contact.storage.registry.ContactStorageRegistry;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link DefaultContactStorageRegistry}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class DefaultContactStorageRegistry implements ContactStorageRegistry {

    private final List<ContactStorage> knownStorages;

    /**
     * Initializes a new {@link DefaultContactStorageRegistry}.
     */
    public DefaultContactStorageRegistry() {
        super();
        this.knownStorages = new ArrayList<ContactStorage>();
    }

    @Override
    public ContactStorage getStorage(Session session, String folderId) throws OXException {
        ContactStorage contactStorage = null;
        for (ContactStorage storage : this.knownStorages) {
            if (storage.supports(session, folderId)) {
                if (null == contactStorage || storage.getPriority() > contactStorage.getPriority()) {
                    contactStorage = storage;
                }
            }
        }
        return contactStorage;
    }

    @Override
    public List<ContactStorage> getStorages(Session session) throws OXException {
    	return Collections.unmodifiableList(knownStorages);
    }

    /**
     * Adds a contact storage to the registry.
     *
     * @param storage the storage to add
     */
    public void addStorage(final ContactStorage storage) {
        this.knownStorages.add(storage);
    }

    /**
     * Removes a storage from the registry.
     *
     * @param storage the storage to remove
     */
    public void removeStorage(final ContactStorage storage) {
        this.knownStorages.remove(storage);
    }

}
