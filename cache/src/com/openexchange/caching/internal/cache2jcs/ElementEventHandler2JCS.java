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

package com.openexchange.caching.internal.cache2jcs;

import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;

import com.openexchange.caching.ElementEvent;
import com.openexchange.caching.ElementEventHandler;
import com.openexchange.caching.internal.jcs2cache.JCSElementEventDelegator;

/**
 * {@link ElementEventHandler2JCS}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ElementEventHandler2JCS implements ElementEventHandler {

	private final IElementEventHandler handler;

	/**
	 * Initializes a new {@link ElementEventHandler2JCS}
	 */
	public ElementEventHandler2JCS(final IElementEventHandler handler) {
		super();
		this.handler = handler;
	}

	public void handleElementEvent(ElementEvent event) {
		handler.handleElementEvent(new JCSElementEventDelegator(event));
	}

	public void onExceededIdletimeBackground(ElementEvent event) {
		handler.handleElementEvent(new JCSElementEventDelegator(event));
	}

	public void onExceededIdletimeOnRequest(ElementEvent event) {
		handler.handleElementEvent(new JCSElementEventDelegator(event));
	}

	public void onExceededMaxlifeBackground(ElementEvent event) {
		handler.handleElementEvent(new JCSElementEventDelegator(event));
	}

	public void onExceededMaxlifeOnRequest(ElementEvent event) {
		handler.handleElementEvent(new JCSElementEventDelegator(event));
	}

	public void onSpooledDiskAvailable(ElementEvent event) {
		handler.handleElementEvent(new JCSElementEventDelegator(event));
	}

	public void onSpooledDiskNotAvailable(ElementEvent event) {
		handler.handleElementEvent(new JCSElementEventDelegator(event));
	}

	public void onSpooledNotAllowed(ElementEvent event) {
		handler.handleElementEvent(new JCSElementEventDelegator(event));
	}

}
