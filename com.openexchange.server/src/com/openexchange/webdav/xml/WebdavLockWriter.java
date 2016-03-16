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

package com.openexchange.webdav.xml;

import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavLock;

public class WebdavLockWriter {

	public String lock2xml(final WebdavLock lock) {
		final StringBuffer lockXML = new StringBuffer();
		activeLock(lockXML);
		lockType(lock, lockXML);
		lockScope(lock, lockXML);
		depth(lock, lockXML);
		owner(lock, lockXML);
		timeout(lock, lockXML);
		lockToken(lock, lockXML);
		endActiveLock(lockXML);
		return lockXML.toString();
	}

	private final void endActiveLock(final StringBuffer lockXML) {
		lockXML.append("</D:activelock>");
	}

	private final void lockToken(final WebdavLock lock, final StringBuffer lockXML) {
		lockXML.append("<D:locktoken><D:href>");
		lockXML.append(lock.getToken());
		lockXML.append("</D:href></D:locktoken>");

	}

	private final void timeout(final WebdavLock lock, final StringBuffer lockXML) {
		lockXML.append("<D:timeout>");
		if(WebdavLock.NEVER == lock.getTimeout()) {
			lockXML.append("Infinite");
		} else {
			lockXML.append("Second-"+lock.getTimeout()/1000);
		}
		lockXML.append("</D:timeout>");
	}

	private final void owner(final WebdavLock lock, final StringBuffer lockXML) {
		lockXML.append("<D:owner>");
		lockXML.append(lock.getOwner()); //TODO: OWNER NS
		lockXML.append("</D:owner>");
	}

	private final void depth(final WebdavLock lock, final StringBuffer lockXML) {
		lockXML.append("<D:depth>");
		if(lock.getDepth() == WebdavCollection.INFINITY) {
			lockXML.append("infinity");
		} else {
			lockXML.append(lock.getDepth());
		}
		lockXML.append("</D:depth>");
	}

	private final void lockScope(final WebdavLock lock, final StringBuffer lockXML) {
		lockXML.append("<D:lockscope>");
		if(lock.getScope().equals(WebdavLock.Scope.EXCLUSIVE_LITERAL)){
			lockXML.append("<D:exclusive/>");
		} else if (lock.getScope().equals(WebdavLock.Scope.SHARED_LITERAL)) {
			lockXML.append("<D:shared/>");
		}
		lockXML.append("</D:lockscope>");
	}

	private final void lockType(final WebdavLock lock, final StringBuffer lockXML) {
		lockXML.append("<D:locktype>");
		/*switch(lock.getType()) {
		case WebdavLock.Type.WRITE_LITERAL: lockXML.append("<write />"); break;
		default: break;
		}*/
		if(lock.getType().equals(WebdavLock.Type.WRITE_LITERAL)) {
			lockXML.append("<D:write />");
		}
		lockXML.append("</D:locktype>");
	}

	private final void activeLock(final StringBuffer lockXML) {
		lockXML.append("<D:activelock>");
	}


}
