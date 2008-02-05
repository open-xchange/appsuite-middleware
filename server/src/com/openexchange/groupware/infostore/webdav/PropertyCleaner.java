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

package com.openexchange.groupware.infostore.webdav;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.event.FolderEvent;
import com.openexchange.event.InfostoreEvent;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.exceptions.LoggingLogic;
import com.openexchange.session.Session;

public class PropertyCleaner implements FolderEvent, InfostoreEvent {
	
	private PropertyStore infoProperties;
	private PropertyStore folderProperties;
	
	private static final LoggingLogic LL = LoggingLogic.getLoggingLogic(PropertyCleaner.class);
	private static final Log LOG = LogFactory.getLog(PropertyCleaner.class);
	
	public PropertyCleaner(final PropertyStore folderProperties, final PropertyStore infoProperties){
		this.folderProperties = folderProperties;
		this.infoProperties = infoProperties;
		
	}

	public void folderCreated(final FolderObject folderObj, final Session sessionObj) {

	}

	public void folderDeleted(final FolderObject folderObj, final Session session) {
		try {
            ServerSession sessionObj = new ServerSessionAdapter(session);
            folderProperties.startTransaction();
			folderProperties.removeAll(folderObj.getObjectID(), sessionObj.getContext());
			folderProperties.commit();
		} catch (final TransactionException e) {
			LL.log(e); // What shall we do with the drunken Exception? what shall we do with the drunken Exception? What shall we do with the drunken Exception early in the morning?
		} catch (final OXException e) {
			if(e.getDetailNumber() == 3 && e.getComponent().equals(Component.USER_SETTING)) {
				LOG.debug("I assume the user was deleted, so these properties are cleaned elsewhere.");
			}
			LL.log(e); // What shall we do with the drunken Exception? what shall we do with the drunken Exception? What shall we do with the drunken Exception early in the morning?
		} catch (ContextException e) {
            LOG.error(e.getLocalizedMessage(), e);
        } finally {
			try {
				folderProperties.finish();
			} catch (final TransactionException e) {
				LL.log(e);
			}
		}
	}

	public void folderModified(final FolderObject folderObj, final Session sessionObj) {

	}

	public void infoitemCreated(final DocumentMetadata metadata,
			final Session sessionObject) {

	}

	public void infoitemDeleted(final DocumentMetadata metadata,
			final Session session) {
		try {
            ServerSession sessionObject = new ServerSessionAdapter(session);
            infoProperties.startTransaction();
			infoProperties.removeAll(metadata.getId(), sessionObject.getContext());
			infoProperties.commit();
		} catch (final TransactionException e) {
			LL.log(e); // What shall we do with the drunken Exception? what shall we do with the drunken Exception? What shall we do with the drunken Exception early in the morning?
		} catch (final OXException e) {
			if(e.getDetailNumber() == 3 && e.getComponent().equals(Component.USER_SETTING)) {
				LOG.debug("I assume the user was deleted, so these properties are cleaned elsewhere.");
			}
			LL.log(e); // What shall we do with the drunken Exception? what shall we do with the drunken Exception? What shall we do with the drunken Exception early in the morning?
		} catch (ContextException e) {
            LOG.error(e.getLocalizedMessage(), e);
        } finally {
			try {
				infoProperties.finish();
			} catch (final TransactionException e) {
				LL.log(e);
			}
		}
	}

	public void infoitemModified(final DocumentMetadata metadata,
			final Session sessionObj) {
		
	}

}
