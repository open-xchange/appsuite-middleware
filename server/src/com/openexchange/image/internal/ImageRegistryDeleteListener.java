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

package com.openexchange.image.internal;

import java.sql.Connection;

import com.openexchange.api2.OXException;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.conversion.DataArguments;
import com.openexchange.groupware.contact.datasource.ContactImageDataSource;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedException;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.session.Session;

/**
 * {@link ImageRegistryDeleteListener}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ImageRegistryDeleteListener implements DeleteListener {

	/**
	 * Initializes a new {@link ImageRegistryDeleteListener}
	 */
	public ImageRegistryDeleteListener() {
		super();
	}

	public void deletePerformed(final DeleteEvent deleteEvent, final Connection readCon, final Connection writeCon)
			throws DeleteFailedException {
		if (DeleteEvent.TYPE_USER == deleteEvent.getType()) {
			/*
			 * Remove user image from registry
			 */
			final Session session = deleteEvent.getSession();
			final Contact contact;
			try {
				final RdbContactSQLInterface contactSql = new RdbContactSQLInterface(session);
				contact = contactSql.getUserById(deleteEvent.getId(), writeCon);
			} catch (final ContextException e) {
				throw new DeleteFailedException(e);
			} catch (final OXException e) {
				throw new DeleteFailedException(e);
			}
			final String id;
			{
				final String[] reqArgs = new ContactImageDataSource().getRequiredArguments();
				final DataArguments args = new DataArguments(2);
				args.put(reqArgs[0], String.valueOf(FolderObject.SYSTEM_LDAP_FOLDER_ID));
				args.put(reqArgs[1], String.valueOf(contact.getObjectID()));
				id = args.getID();
			}
			ImageRegistry.getInstance().removeImageData(session.getContextId(), id);
		}
	}

}
