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

import java.util.ArrayList;

import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;

import com.openexchange.caching.ElementAttributes;
import com.openexchange.caching.ElementEventHandler;
import com.openexchange.caching.internal.jcs2cache.JCSElementEventHandlerDelegator;

/**
 * {@link ElementAttributes2JCS} - Delegates its method invocations to an
 * instance of {@link IElementAttributes}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ElementAttributes2JCS implements ElementAttributes {

	private final IElementAttributes attributes;

	/**
	 * Initializes a new {@link ElementAttributes2JCS}
	 */
	public ElementAttributes2JCS(final IElementAttributes attributes) {
		super();
		this.attributes = attributes;
	}

	public void addElementEventHandler(final ElementEventHandler eventHandler) {
		attributes.addElementEventHandler(new JCSElementEventHandlerDelegator(eventHandler));
	}

	public void addElementEventHandlers(final ArrayList<ElementEventHandler> eventHandlers) {
		for (final Object object : eventHandlers) {
			attributes.addElementEventHandler(new JCSElementEventHandlerDelegator((ElementEventHandler) object));
		}
	}

	public ElementAttributes copy() {
		return new ElementAttributes2JCS(attributes.copy());
	}

	public long getCreateTime() {
		return attributes.getCreateTime();
	}

	public ArrayList<ElementEventHandler> getElementEventHandlers() {
		final ArrayList<?> l = attributes.getElementEventHandlers();
		final ArrayList<ElementEventHandler> retval = new ArrayList<ElementEventHandler>(l.size());
		for (final Object object : l) {
			retval.add(new ElementEventHandler2JCS((IElementEventHandler) object));
		}
		return retval;
	}

	public long getIdleTime() {
		return attributes.getIdleTime();
	}

	public boolean getIsEternal() {
		return attributes.getIsEternal();
	}

	public boolean getIsLateral() {
		return attributes.getIsLateral();
	}

	public boolean getIsRemote() {
		return attributes.getIsRemote();
	}

	public boolean getIsSpool() {
		return attributes.getIsSpool();
	}

	public long getLastAccessTime() {
		return attributes.getLastAccessTime();
	}

	public long getMaxLifeSeconds() {
		return attributes.getMaxLifeSeconds();
	}

	public int getSize() {
		return attributes.getSize();
	}

	public long getTimeToLiveSeconds() {
		return attributes.getTimeToLiveSeconds();
	}

	public long getVersion() {
		return attributes.getVersion();
	}

	public void setIdleTime(final long idle) {
		attributes.setIdleTime(idle);
	}

	public void setIsEternal(final boolean val) {
		attributes.setIsEternal(val);
	}

	public void setIsLateral(final boolean val) {
		attributes.setIsLateral(val);
	}

	public void setIsRemote(final boolean val) {
		attributes.setIsRemote(val);
	}

	public void setIsSpool(final boolean val) {
		attributes.setIsSpool(val);
	}

	public void setLastAccessTimeNow() {
		attributes.setLastAccessTimeNow();
	}

	public void setMaxLifeSeconds(final long mls) {
		attributes.setMaxLifeSeconds(mls);
	}

	public void setSize(final int size) {
		attributes.setSize(size);
	}

	public void setVersion(final long version) {
		attributes.setVersion(version);
	}

}
