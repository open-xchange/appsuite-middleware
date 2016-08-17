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

package com.openexchange.contact.vcard;

import java.io.InputStream;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link VCardService} - The VCard merge service for VCards and contacts.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
@SingletonService
public interface VCardService extends VCardParametersFactory {

    /**
     * Exports a contact to a vCard, optionally merging with an existing vCard.
     *
     * @param contact The contact to export
     * @param originalVCard The vCard to merge the contact into, or <code>null</code> to export to a new vCard
     * @param parameters Further parameters for the vCard export, or <code>null</code> if not used
     * @param warnings A reference to a collection to store any warnings, or <code>null</code> if not used
     * @return The exported contact as vCard
     * @throws OXException If exporting given contact to a vCard fails - non-fatal conversion warnings are accessible in the export result
     */
    VCardExport exportContact(Contact contact, InputStream originalVCard, VCardParameters parameters) throws OXException;

    /**
     * Imports a vCard, optionally merging with an existing contact.
     *
     * @param vCard The vCard to import
     * @param contact The contact to merge the vCard into, or <code>null</code> to import as a new contact
     * @param parameters Further parameters for the vCard import, or <code>null</code> if not used
     * @return The imported vCard as contact, which is a new instance if passed contact reference is <code>null</code>, or the passed
     *         contact itself, otherwise
     * @throws OXException If importing the vCard fails - non-fatal conversion warnings are accessible in the import result
     */
    VCardImport importVCard(InputStream vCard, Contact contact, VCardParameters parameters) throws OXException;

    /**
     * Imports multiple vCards.
     *
     * @param vCards The input stream holding the vCards to import
     * @param parameters Further parameters for the vCard import, or <code>null</code> if not used
     * @return The imported vCards wrapped by a search iterator
     * @throws OXException If importing the vCard fails - non-fatal conversion warnings are accessible in each import result
     */
    SearchIterator<VCardImport> importVCards(InputStream vCards, VCardParameters parameters) throws OXException;

    /**
     * Gets all contact fields from all known mappings corresponding to the supplied set of vCard properties.
     *
     * @param propertyNames The property names to get the corresponding fields for
     * @return The contact fields
     */
    ContactField[] getContactFields(Set<String> propertyNames);

}
