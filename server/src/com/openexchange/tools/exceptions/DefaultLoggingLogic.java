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

import com.openexchange.groupware.AbstractOXException;

public class DefaultLoggingLogic extends LoggingLogic {

	public DefaultLoggingLogic(Log log) {
		super(log);
	}

	@Override
	public void codeError(AbstractOXException aox) {
		LOG.error("Coding Error: "+aox.toString(), aox);
	}

	@Override
	public void concurrentModification(AbstractOXException aox) {
		LOG.debug("Concurrent Modification: "+aox.toString(), aox);
	}

	@Override
	public void externalResourceFull(AbstractOXException aox) {
		LOG.fatal("External Resource is full: "+aox.toString(), aox);
	}

	@Override
	public void internalError(AbstractOXException aox) {
		LOG.error("An internal error occurred: "+aox.toString(), aox);
	}

	@Override
	public void permission(AbstractOXException aox) {
		LOG.debug("Permission Exception: "+aox.toString(), aox);
	}

	@Override
	public void setupError(AbstractOXException aox) {
		LOG.fatal("Setup Error: "+aox.toString(), aox);
	}

	@Override
	public void socketConnection(AbstractOXException aox) {
		LOG.fatal("Socket Connection Excpetion: "+aox.toString(), aox);
	}

	@Override
	public void subsystemOrServiceDown(AbstractOXException aox) {
		LOG.fatal("Subsystem or service down: "+aox.toString(), aox);
	}

	@Override
	public void truncated(AbstractOXException aox) {
		LOG.debug("Database truncated fields: "+aox.toString(), aox);
	}

	@Override
	public void tryAgain(AbstractOXException aox) {
		LOG.error("Temporarily Disabled? "+aox.toString(), aox);
	}

	@Override
	public void unknownCategory(AbstractOXException aox) {
		LOG.error("Unkown Category: "+aox.toString(), aox);
	}

	@Override
	public void userConfiguration(AbstractOXException aox) {
		LOG.error("User Configuration Error: "+aox.toString(), aox);
	}

	@Override
	public void userInput(AbstractOXException aox) {
		LOG.debug("User Input: "+aox.toString(), aox);
	}

	@Override
	public void warning(AbstractOXException aox) {
		LOG.warn("Warning: "+aox.toString(), aox);
	}

}
