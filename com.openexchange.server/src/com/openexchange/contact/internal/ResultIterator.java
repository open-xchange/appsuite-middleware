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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.contact.internal;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link ResultIterator} - Filters a search iterator based on a user's 
 * permission.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ResultIterator implements SearchIterator<Contact> {
	
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ResultIterator.class));

    private final SearchIterator<Contact> delegate;
    private final boolean needsAttachmentInfo;
    private final int userID;
    private final int contextID;
    private Contact next;
	private final Map<String, Boolean> canReadAllMap;
	private final Boolean canReadAll;

	/**
	 * Initializes a new {@link ResultIterator} where the 'can read all' 
	 * information is evaluated dynamically based on the contact's parent 
	 * folders.
	 * 
	 * @param delegate
	 * @param needsAttachmentInfo
	 * @param contextID
	 * @param userID
	 * @throws OXException
	 */
	public ResultIterator(final SearchIterator<Contact> delegate, final boolean needsAttachmentInfo, final int contextID, final int userID) throws OXException {
		super();
		this.delegate = delegate;
		this.needsAttachmentInfo = needsAttachmentInfo;
		this.contextID = contextID;
		this.userID = userID;
		// query folder permissions dynamically
		this.canReadAll = null;
		this.canReadAllMap = new HashMap<String, Boolean>(); 
		initNext();
	}
	
	/**
	 * Initializes a new {@link ResultIterator} where the supplied 'can read 
	 * all' information is used statically. 
	 * 
	 * @param delegate
	 * @param needsAttachmentInfo
	 * @param contextID
	 * @param userID
	 * @param canReadAll
	 * @throws OXException
	 */
	public ResultIterator(final SearchIterator<Contact> delegate, final boolean needsAttachmentInfo, final int contextID, final int userID, final boolean canReadAll) throws OXException {
		super();
		this.delegate = delegate;
		this.needsAttachmentInfo = needsAttachmentInfo;
		this.contextID = contextID;
		this.userID = userID;
		// use fixed folder permissions
		this.canReadAll = Boolean.valueOf(canReadAll);
		this.canReadAllMap = null; 
		initNext();
	}
	
	private void initNext() throws OXException {
        while (delegate.hasNext()) {
            next = delegate.next();
            if (this.accept(next)) {
            	if (this.needsAttachmentInfo) {
            		Tools.addAttachmentInformation(next, contextID);
            	}
                return;
            }
        }
        next = null;	
	}
	
	/**
	 * Gets a value indicating whether the supplied contact should be passed
	 * through from the delegate or not.
	 * 
	 * @param contact
	 * @return
	 * @throws OXException
	 */
	private boolean accept(final Contact contact) throws OXException {
		if (contact.getCreatedBy() == userID) {
			return true;
		} else if (contact.containsPrivateFlag()) {
			return false;
		} else if (null != this.canReadAll) {
			return this.canReadAll;
		} else {
			final String folderID = Integer.toString(contact.getParentFolderID());
			if (false == canReadAllMap.containsKey(folderID)) {
				boolean canReadAll = false;
				try {
					final EffectivePermission permission = Tools.getPermission(this.contextID, folderID, this.userID);
					canReadAll = permission.canReadAllObjects();
				} catch (final OXException e) {
					LOG.warn("Unable to determine effective permissions for folder '" + folderID + "'", e);
				}
				canReadAllMap.put(folderID, Boolean.valueOf(canReadAll));
			}				
			return canReadAllMap.get(folderID).booleanValue();
		}
	}

	@Override
	public boolean hasNext() throws OXException {
		return null != this.next;
	}

	@Override
	public Contact next() throws OXException {
        final Contact current = next;
        initNext();
        return current;
	}

	@Override
	public void close() throws OXException {
		delegate.close();
	}

	@Override
	public int size() {
		return -1;
	}

	@Override
	public boolean hasWarnings() {
		return delegate.hasWarnings();
	}

	@Override
	public void addWarning(final OXException warning) {
        delegate.addWarning(warning);
	}

	@Override
	public OXException[] getWarnings() {
		return delegate.getWarnings();
	}
	
}
