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

package com.openexchange.tools.exceptions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.AbstractOXException.Category;

public abstract class LoggingLogic {

	public static LoggingLogic getLoggingLogic(Class klass) {
		// We could add hooks for custom logging logic for certain classes here, if needed. 
		// For now everyone uses the default logic.
		
		return new DefaultLoggingLogic(LogFactory.getLog(klass));
	}
	
	public static LoggingLogic getLoggingLogic(Class klass, Log log) {
		// We could add hooks for custom logging logic for certain classes here, if needed. 
		// For now everyone uses the default logic.
		
		return new DefaultLoggingLogic(log);
	}
	
	protected Log LOG;
	
	public LoggingLogic(Log log) {
		this.LOG = log;
	}
	
	public void log(AbstractOXException aox) {
		Category cat =aox.getCategory();
		if(Category.CODE_ERROR.equals(cat)) {
			this.codeError(aox);
		} else if (Category.CONCURRENT_MODIFICATION.equals(cat)) {
			this.concurrentModification(aox);
		} else if (Category.EXTERNAL_RESOURCE_FULL.equals(cat)){
			this.externalResourceFull(aox);
		} else if (Category.INTERNAL_ERROR.equals(cat)) {
			this.internalError(aox);
		} else if (Category.PERMISSION.equals(cat)) {
			this.permission(aox);
		} else if (Category.SETUP_ERROR.equals(cat)) {
			this.setupError(aox);
		} else if (Category.SOCKET_CONNECTION.equals(cat)) {
			this.socketConnection(aox);
		} else if (Category.SUBSYSTEM_OR_SERVICE_DOWN.equals(cat)) {
			this.subsystemOrServiceDown(aox);
		} else if (Category.TRUNCATED.equals(cat)) {
			this.truncated(aox);
		} else if (Category.TRY_AGAIN.equals(cat)) {
			this.tryAgain(aox);
		} else if (Category.USER_CONFIGURATION.equals(cat)) {
			this.userConfiguration(aox);
		} else if (Category.USER_INPUT.equals(cat)) {
			this.userInput(aox);
		} else if (Category.WARNING.equals(cat)) {
			this.warning(aox);
		} else {
			this.unknownCategory(aox);
		}
	}

	public abstract void unknownCategory(AbstractOXException aox);
	public abstract void warning(AbstractOXException aox);
	public abstract void userInput(AbstractOXException aox);
	public abstract void userConfiguration(AbstractOXException aox);
	public abstract void tryAgain(AbstractOXException aox);
	public abstract void truncated(AbstractOXException aox);
	public abstract void subsystemOrServiceDown(AbstractOXException aox);
	public abstract void socketConnection(AbstractOXException aox);
	public abstract void setupError(AbstractOXException aox);
	public abstract void permission(AbstractOXException aox);
	public abstract void internalError(AbstractOXException aox);
	public abstract void externalResourceFull(AbstractOXException aox);
	public abstract void concurrentModification(AbstractOXException aox);
	public abstract void codeError(AbstractOXException aox);
	
}
