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

package com.openexchange.cache;

import org.apache.jcs.engine.control.event.ElementEvent;
import org.apache.jcs.engine.control.event.behavior.IElementEvent;
import org.apache.jcs.engine.control.event.behavior.IElementEventConstants;
import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;

/**
 * ElementEventHandlerWrapper: A simple wrapper class for events arising from
 * JCS caching system
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class ElementEventHandlerWrapper implements IElementEventHandler {

	public ElementEventHandlerWrapper() {
		super();
	}

	/* (non-Javadoc)
	 * 
	 * @see org.apache.jcs.engine.control.event.behavior.IElementEventHandler#handleElementEvent(org.apache.jcs.engine.control.event.behavior.IElementEvent)
	 */
	public void handleElementEvent(final IElementEvent elemEvent) {
		if (elemEvent instanceof ElementEvent) {
			final ElementEvent event = (ElementEvent) elemEvent;
			switch (event.getElementEvent()) {
			case IElementEventConstants.ELEMENT_EVENT_EXCEEDED_IDLETIME_BACKGROUND:
				onExceededIdletimeBackground(event);
				break;
			case IElementEventConstants.ELEMENT_EVENT_EXCEEDED_IDLETIME_ONREQUEST:
				onExceededIdletimeOnRequest(event);
				break;
			case IElementEventConstants.ELEMENT_EVENT_EXCEEDED_MAXLIFE_BACKGROUND:
				onExceededMaxlifeBackground(event);
				break;
			case IElementEventConstants.ELEMENT_EVENT_EXCEEDED_MAXLIFE_ONREQUEST:
				onExceededMaxlifeOnRequest(event);
				break;
			case IElementEventConstants.ELEMENT_EVENT_SPOOLED_DISK_AVAILABLE:
				onSpooledDiskAvailable(event);
				break;
			case IElementEventConstants.ELEMENT_EVENT_SPOOLED_DISK_NOT_AVAILABLE:
				onSpooledDiskNotAvailable(event);
				break;
			case IElementEventConstants.ELEMENT_EVENT_SPOOLED_NOT_ALLOWED:
				onSpooledNotAllowed(event);
				break;
			default:
				return;
			}
		}
	}

	/**
	 * The element exceeded its max life. This was detected in a background
	 * cleanup
	 * 
	 * @param event -
	 *            the element event containing event code and instance of
	 *            <code>org.apache.jcs.engine.CacheElement</code> as source
	 */
	protected void onExceededIdletimeBackground(final ElementEvent event) {
	}

	/**
	 * The element exceeded its max life. This was detected on request
	 * 
	 * @param event -
	 *            the element event containing event code and instance of
	 *            <code>org.apache.jcs.engine.CacheElement</code> as source
	 */
	protected void onExceededIdletimeOnRequest(final ElementEvent event) {
	}

	/**
	 * The element exceeded its max idle. This was detected in a background
	 * cleanup
	 * 
	 * @param event -
	 *            the element event containing event code and instance of
	 *            <code>org.apache.jcs.engine.CacheElement</code> as source
	 */
	protected void onExceededMaxlifeBackground(final ElementEvent event) {
	}

	/**
	 * The element exceeded its max idle time. This was detected on request
	 * 
	 * @param event -
	 *            the element event containing event code and instance of
	 *            <code>org.apache.jcs.engine.CacheElement</code> as source
	 */
	protected void onExceededMaxlifeOnRequest(final ElementEvent event) {
	}

	/**
	 * The element was pushed out of the memory store, there is a disk store
	 * available for the region, and the element is marked as spoolable
	 * 
	 * @param event -
	 *            the element event containing event code and instance of
	 *            <code>org.apache.jcs.engine.CacheElement</code> as source
	 */
	protected void onSpooledDiskAvailable(final ElementEvent event) {
	}

	/**
	 * The element was pushed out of the memory store, and there is not a disk
	 * store available for the region
	 * 
	 * @param event -
	 *            the element event containing event code and instance of
	 *            <code>org.apache.jcs.engine.CacheElement</code> as source
	 */
	protected void onSpooledDiskNotAvailable(final ElementEvent event) {
	}

	/**
	 * The element was pushed out of the memory store, there is a disk store
	 * available for the region, but the element is marked as not spoolable
	 * 
	 * @param event -
	 *            the element event containing event code and instance of
	 *            <code>org.apache.jcs.engine.CacheElement</code> as source
	 */
	protected void onSpooledNotAllowed(final ElementEvent event) {
	}
}
