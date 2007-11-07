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

package com.openexchange.groupware.attach.impl;

import java.sql.SQLException;
import java.util.Date;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.attach.AttachmentExceptionFactory;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.Classes;

@OXExceptionSource(classId=Classes.COM_OPENEXCHANGE_GROUPWARE_ATTACH_IMPL_DELETEATTACHMENTACTION, component=Component.ATTACHMENT)
public class DeleteAttachmentAction extends AttachmentListQueryAction {

	private static final AttachmentExceptionFactory EXCEPTIONS = new AttachmentExceptionFactory(DeleteAttachmentAction.class);
	
	@OXThrows(
			category = Category.CODE_ERROR,
			desc = "An invalid SQL Query was sent to the Server. This can only be fixed in R&D.",
			exceptionId = 0,
			msg = "Invalid SQL Query: %s"
	)
	@Override
	protected void undoAction() throws AbstractOXException {
		if(getAttachments().size() == 0) {
			return;
		}
		try {
			
			doUpdates(new Update(getQueryCatalog().getDelete("del_attachment", getAttachments())) {

				@Override
				public void fillStatement() throws SQLException {
					stmt.setInt(1, getContext().getContextId());
				}
				
			});
			
			doUpdates(getQueryCatalog().getInsert(), getAttachments(), false);
		} catch (final UpdateException x) {
			throw EXCEPTIONS.create(0, x.getSQLException(), x.getStatement());
		}
	}

	@OXThrows(
			category = Category.CODE_ERROR,
			desc = "An invalid SQL Query was sent to the Server. This can only be fixed in R&D.",
			exceptionId = 1,
			msg = "Invalid SQL Query: %s"
	)
	public void perform() throws AbstractOXException {
		if(getAttachments().size() == 0) {
			return;
		}
		final Date delDate = new Date();
		final UpdateBlock[] updates = new UpdateBlock[getAttachments().size()+1];
		int i = 0;
		for(final AttachmentMetadata m : getAttachments()) {
			updates[i++] = new Update(getQueryCatalog().getInsertIntoDel()){

				@Override
				public void fillStatement() throws SQLException {
					stmt.setInt(1, m.getId());
					stmt.setLong(2, delDate.getTime());
					stmt.setInt(3, getContext().getContextId());
					stmt.setInt(4, m.getAttachedId());
					stmt.setInt(5, m.getModuleId());
				}
				
			};
		}
		updates[i++] = new Update(getQueryCatalog().getDelete("prg_attachment", getAttachments())) {

			@Override
			public void fillStatement() throws SQLException {
				stmt.setInt(1,getContext().getContextId());
			}
			
		};
		
		try {
			doUpdates(updates);
		} catch (final UpdateException x) {
			throw EXCEPTIONS.create(1, x.getSQLException(), x.getStatement());
		}
	}

}
