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

package com.openexchange.groupware.infostore.database.impl;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.infostore.Classes;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionFactory;


@OXExceptionSource(
		classId = Classes.COM_OPENEXCHANGE_GROUPWARE_INFOSTORE_DATABASE_IMPL_UPDATEVERSIONACTION,
		component = EnumComponent.INFOSTORE
)
public class UpdateVersionAction extends AbstractDocumentUpdateAction {
	private static final InfostoreExceptionFactory EXCEPTIONS = new InfostoreExceptionFactory(UpdateVersionAction.class);
	
	@OXThrowsMultiple(
			category = {Category.CODE_ERROR, Category.CONCURRENT_MODIFICATION},
			desc ={ "An invalid SQL Query was sent to the server","The document was updated in between do and undo. The Database is now probalby inconsistent."},
			exceptionId = {0,3},
			msg = {"Invalid SQL Query : %s","The document was updated in between do and undo. The Database is now probably inconsistent."} )
	@Override
	protected void undoAction() throws AbstractOXException {
		int counter = 0;
		try {
			counter = doUpdates(getQueryCatalog().getVersionUpdate(getModified()), getQueryCatalog().filterForVersion(getModified()), getOldDocuments());
		} catch (UpdateException e) {
			throw EXCEPTIONS.create(0, e.getSQLException(), e.getStatement());
		}
		
		
		if(counter < 0) {
			throw EXCEPTIONS.create(3);
		}
	
	}

	@OXThrowsMultiple(
			category = {Category.CODE_ERROR, Category.CONCURRENT_MODIFICATION},
			desc = {"An invalid SQL Query was sent to the server","The document could not be updated because it was modified."},
			exceptionId = {1,2},
			msg = {"Invalid SQL Query : %s","The document could not be updated because it was modified. Reload the view." })
	public void perform() throws AbstractOXException {
		int counter = 0;
		try {
			counter = doUpdates(getQueryCatalog().getVersionUpdate(getModified()), getQueryCatalog().filterForVersion(getModified()), getDocuments());
		} catch (UpdateException e) {
			throw EXCEPTIONS.create(1, e.getSQLException(), e.getStatement());
		}
		setTimestamp(System.currentTimeMillis());
		if(counter <= 0) {
			throw EXCEPTIONS.create(2);
		}
	
	}

	@Override
	protected Object[] getAdditionals(DocumentMetadata doc) {
		return new Object[]{getContext().getContextId(), doc.getId(), doc.getVersion(), getTimestamp()};
	}

}
