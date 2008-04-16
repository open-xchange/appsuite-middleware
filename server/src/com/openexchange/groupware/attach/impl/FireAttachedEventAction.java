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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrows;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.attach.AttachmentExceptionFactory;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.Classes;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.AbstractOXException.Category;

@OXExceptionSource(
		classId = Classes.COM_OPENEXCHANGE_GROUPWARE_ATTACH_IMPL_FIREDETACHEDEVENTACTION,
		component = EnumComponent.ATTACHMENT
)
public class FireAttachedEventAction extends AttachmentEventAction {

	private static final AttachmentExceptionFactory EXCEPTIONS = new AttachmentExceptionFactory(FireAttachedEventAction.class);
	
	@OXThrows(
			category = Category.INTERNAL_ERROR,
			desc = "The Object could not be detached because the update to an underlying object failed.",
			exceptionId = 2,
			msg = "The Object could not be detached because the update to an underlying object failed."
			
	)
	@Override
	protected void undoAction() throws AbstractOXException {
		try {
			fireDetached(getAttachments(), getUser(), getUserConfiguration(), getContext(), getProvider());
		} catch (AbstractOXException e) {
			throw e;
		} catch (Exception e) {
			throw EXCEPTIONS.create(2,e);
		}
	}

	@OXThrowsMultiple(
			category = { Category.INTERNAL_ERROR, Category.INTERNAL_ERROR },
			desc = { "Changes done to the object this attachment was added to could not be undone. Your database is probably inconsistent, run the consistency tool.", "An error occurred attaching to the given object." },
			exceptionId = { 0,1 },
			msg = { "Changes done to the object this attachment was added to could not be undone. Your database is probably inconsistent, run the consistency tool.","An error occurred attaching to the given object." }
	)
	public void perform() throws AbstractOXException {
		List<AttachmentMetadata> processed = new ArrayList<AttachmentMetadata>();
		try {
			fireAttached(getAttachments(), processed, getUser(), getUserConfiguration(), getContext(),getProvider());
		} catch (Exception e) {
			try {
				fireDetached(processed, getUser(), getUserConfiguration(), getContext(), getProvider());
			} catch (Exception e1) {
				throw EXCEPTIONS.create(0,e1);
			}
			if (e instanceof AbstractOXException) {
				AbstractOXException aoe = (AbstractOXException) e;
				throw aoe;
			}
			throw EXCEPTIONS.create(1,e);
		}
	}
	

}
