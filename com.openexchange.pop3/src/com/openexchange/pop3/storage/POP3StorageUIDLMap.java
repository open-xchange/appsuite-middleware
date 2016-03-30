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

import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * {@link POP3StorageUIDLMap} - Maps POP3 UIDL to a fullname-UID-pair.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface POP3StorageUIDLMap {

    /**
     * Gets the fullname-UID-pairs to specified POP3 UIDLs.
     *
     * @param uidls The POP3 UIDLs
     * @return The fullname-UID-pairs to specified POP3 UIDLs. If no mapping could be found the corresponding entry is <code>null</code>
     * @throws OXException If mapping retrieval fails
     */
    public FullnameUIDPair[] getFullnameUIDPairs(String[] uidls) throws OXException;

    /**
     * Gets the fullname-UID-pair to specified POP3 UIDL.
     *
     * @param uidls The POP3 UIDL
     * @return The fullname-UID-pair to specified POP3 UIDL or <code>null</code> if no such mapping exists
     * @throws OXException If mapping retrieval fails
     */
    public FullnameUIDPair getFullnameUIDPair(String uidl) throws OXException;

    /**
     * Gets the POP3 UIDLs to specified fullname-UID-pairs.
     *
     * @param fullnameUIDPairs The fullname-UID-pairs
     * @return The POP3 UIDLs to specified fullname-UID-pairs
     * @throws OXException If mapping retrieval fails
     */
    public String[] getUIDLs(FullnameUIDPair[] fullnameUIDPairs) throws OXException;

    /**
     * Gets the POP3 UIDL to specified fullname-UID-pair.
     *
     * @param fullnameUIDPairs The fullname-UID-pair
     * @return The POP3 UIDL to specified fullname-UID-pair or <code>null</code> if no such mapping exists
     * @throws OXException If mapping retrieval fails
     */
    public String getUIDL(FullnameUIDPair fullnameUIDPair) throws OXException;

    /**
     * Adds specified mappings to this map.
     *
     * @param uidls The POP3 UIDLs
     * @param fullnameUIDPairs The fullname-UID-pairs. If no mapping could be found the corresponding entry is <code>null</code>
     * @throws OXException If adding mappings fails
     */
    public void addMappings(String[] uidls, FullnameUIDPair[] fullnameUIDPairs) throws OXException;

    /**
     * Gets all mappings known by this UIDL map.
     *
     * @return All mappings known by this UIDL map
     * @throws OXException If mapping retrieval fails
     */
    public Map<String, FullnameUIDPair> getAllUIDLs() throws OXException;

    /**
     * Deletes the mappings for specified UIDLs.
     *
     * @param uidls The UIDLs to clean from this map
     * @throws OXException If mapping deletion fails
     */
    public void deleteUIDLMappings(String[] uidls) throws OXException;

    /**
     * Deletes the mappings for specified fullname-UID-pairs.
     *
     * @param fullnameUIDPairs The fullname-UID-pairs to clean from this map
     * @throws OXException If mapping deletion fails
     */
    public void deleteFullnameUIDPairMappings(FullnameUIDPair[] fullnameUIDPairs) throws OXException;

}
