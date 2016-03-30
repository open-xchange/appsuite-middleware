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

package com.openexchange.pop3.storage;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.MailAccountDeleteListener;
import com.openexchange.pop3.POP3Access;

/**
 * {@link POP3StorageProvider} - Provider for POP3 storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface POP3StorageProvider {

    /**
     * Gets an appropriate POP3 storage.
     *
     * @param pop3Access The POP3 access to which the storage shall be bound
     * @param properties The properties for the storage; especially the POP3 {@link POP3StoragePropertyNames#PROPERTY_PATH path}
     * @return An appropriate POP3 storage
     * @throws OXException If no such storage can be found
     * @see POP3StoragePropertyNames
     */
    public POP3Storage getPOP3Storage(POP3Access pop3Access, POP3StorageProperties properties) throws OXException;

    /**
     * Gets the appropriate POP3 storage properties.
     *
     * @param pop3Access The POP3 access to which the storage properties belong
     * @return The appropriate POP3 storage properties
     * @throws OXException If no such storage properties can be found
     */
    public POP3StorageProperties getPOP3StorageProperties(POP3Access pop3Access) throws OXException;

    /**
     * Gets the POP3 storage name.
     *
     * @return The POP3 storage name
     */
    public String getPOP3StorageName();

    /**
     * Gets the {@link MailAccountDeleteListener delete listeners} for this provider.
     *
     * @return The {@link MailAccountDeleteListener delete listeners} or an empty list
     */
    public List<MailAccountDeleteListener> getDeleteListeners();

    /**
     * Indicates whether to unregister {@link MailAccountDeleteListener delete listeners} on provider's absence.
     *
     * @return <code>true</code> to unregister {@link MailAccountDeleteListener delete listeners} on provider's absence; otherwise
     *         <code>false</code>
     */
    public boolean unregisterDeleteListenersOnAbsence();

}
