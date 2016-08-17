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

package com.openexchange.groupware.infostore.validation;

import java.util.Set;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.tools.session.ServerSession;

/**
 *
 * @author francisco.laguna@open-xchange.com
 *
 * An InfostoreValidator is used to validate a DocumentMetadata for according to some criterium
 *
 */
public interface InfostoreValidator {

	/**
	 * This method is expected to check a DocumentMetadata and fill a new instance of DocumentMetadataValidation with the relevant errors.
	 * <p />
	 * A few implementation hints:
	 * <ol>
     * <li>Don't include the field name in the error messages, it will be included by the ValidationChain.</li>
     * <li>Try to reuse error messages. All fields with the same error message are collected and displayed together.</li>
     * </ol>
	 *
     * @param session The session
     * @param metadata The DocumentMetadata to check
     * @param originalDocument The original document in case of an update, or <code>null</code> for new documents
     * @param updatedColumns The updated columns, or <code>null</code> for new documents
	 * @return A DocumentMetadataValidation filled with the relevant errors
	 */
	DocumentMetadataValidation validate(ServerSession session, DocumentMetadata metadata, DocumentMetadata originalDocument, Set<Metadata> updatedColumns);

	/**
	 * Returns the name used for displaying error messages
	 */
	String getName();

}
