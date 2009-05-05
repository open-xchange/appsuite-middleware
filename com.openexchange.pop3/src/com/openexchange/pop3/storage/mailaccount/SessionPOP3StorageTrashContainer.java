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

package com.openexchange.pop3.storage.mailaccount;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.mail.MailException;
import com.openexchange.pop3.POP3Access;
import com.openexchange.pop3.storage.POP3StorageTrashContainer;
import com.openexchange.session.Session;

/**
 * {@link SessionPOP3StorageTrashContainer} - Session-backed implementation of {@link POP3StorageTrashContainer}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessionPOP3StorageTrashContainer implements POP3StorageTrashContainer {

    /**
     * Gets the trash container bound to specified POP3 access.
     * 
     * @param pop3Access The POP3 access
     * @return The trash container bound to specified POP3 access
     * @throws MailException If instance cannot be returned
     */
    public static SessionPOP3StorageTrashContainer getInstance(final POP3Access pop3Access) throws MailException {
        final Session session = pop3Access.getSession();
        final String key = SessionParameterNames.getTrashContainer(pop3Access.getAccountId());
        SessionPOP3StorageTrashContainer cached = (SessionPOP3StorageTrashContainer) session.getParameter(key);
        if (null == cached) {
            cached = new SessionPOP3StorageTrashContainer(new RdbPOP3StorageTrashContainer(pop3Access));
            session.setParameter(key, cached);
        }
        return cached;
    }

    /*-
     * Member section
     */

    private final POP3StorageTrashContainer delegatee;

    private final Set<String> set;

    private SessionPOP3StorageTrashContainer(final POP3StorageTrashContainer delegatee) throws MailException {
        super();
        this.delegatee = delegatee;
        set = new HashSet<String>();
        init();
    }

    private void init() throws MailException {
        set.addAll(delegatee.getUIDLs());
    }

    public void addUIDL(final String uidl) throws MailException {
        set.add(uidl);
        delegatee.addUIDL(uidl);
    }

    public void clear() throws MailException {
        set.clear();
        delegatee.clear();
    }

    public Set<String> getUIDLs() throws MailException {
        final Set<String> tmp = new HashSet<String>();
        tmp.addAll(set);
        return set;
    }

    public void removeUIDL(final String uidl) throws MailException {
        set.remove(uidl);
        delegatee.removeUIDL(uidl);
    }

    public void addAllUIDL(final Collection<? extends String> uidls) throws MailException {
        set.addAll(uidls);
        delegatee.addAllUIDL(uidls);
    }

}
