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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.contacts.ldap.contacts;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.openexchange.contacts.ldap.property.FolderProperties;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.container.Contact;
import com.openexchange.session.Session;

/**
 * {@link LdapContactInterfaceProvider} - Provider for LDAP contact interface.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class LdapContactInterfaceProvider {

    private final int adminId;

    private final FolderProperties folderProperties;

    private final int folderId;

    private final int contextId;

    public List<Contact> cached_contacts;

    // Mapping tables - only used if the folder content is the same for all user (admin dn auth)
    public volatile Map<Integer, String> keytable;

    public volatile Map<String, Integer> valuetable;

    public ReentrantReadWriteLock rwlock_cached_contacts = new ReentrantReadWriteLock(true);

    /**
     * Initializes a new {@link LdapContactInterfaceProvider}.
     *
     * @param folderProperties The folder properties
     * @param adminId The admin ID
     * @param folderId The folder ID
     * @param contextId The context ID
     */
    public LdapContactInterfaceProvider(final FolderProperties folderProperties, final int adminId, final int folderId, final int contextId) {
        super();
        this.folderProperties = folderProperties;
        this.adminId = adminId;
        this.contextId = contextId;
        this.folderId = folderId;
    }

    public ContactInterface newContactInterface(final Session session) throws OXException {
        final LdapContactInterface ldapContactInterface = new LdapContactInterface(contextId, adminId, folderProperties, folderId, this);
        ldapContactInterface.setSession(session);
        return ldapContactInterface;
    }

    /**
     * Gets the folder properties.
     *
     * @return the folder properties
     */
    public FolderProperties getProperties() {
        return this.folderProperties;
    }

}
