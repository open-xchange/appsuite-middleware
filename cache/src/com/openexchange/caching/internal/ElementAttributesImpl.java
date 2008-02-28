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

package com.openexchange.caching.internal;

import java.io.Serializable;
import java.util.ArrayList;

import com.openexchange.caching.ElementAttributes;
import com.openexchange.caching.ElementEventHandler;

/**
 * {@link ElementAttributesImpl}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ElementAttributesImpl implements ElementAttributes, Serializable, Cloneable {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 5870240777519850914L;

	/**
	 * Can this item be flushed to disk
	 */
	private boolean isSpool = true;

	/**
	 * Is this item laterally distributable
	 */
	private boolean isLateral = true;

	/**
	 * Can this item be sent to the remote cache
	 */
	private boolean isRemote = true;

	/**
	 * You can turn off expiration by setting this to true. This causes the
	 * cache to bypass both max life and idle time expiration.
	 */
	private boolean isEternal = true;

	/**
	 * The object version. This is currently not used.
	 */
	private long version;

	/**
	 * Max life seconds
	 */
	private long maxLifeSeconds = -1;

	/**
	 * The maximum time an entry can be idle. Setting this to -1 causes the idle
	 * time check to be ignored.
	 */
	private long maxIdleTimeSeconds = -1;

	/**
	 * The byte size of the field. Must be manually set.
	 */
	private int size;

	/**
	 * The creation time. This is used to enforce the max life.
	 */
	private long createTime;

	/**
	 * The last access time. This is used to enforce the max idle time.
	 */
	private long lastAccessTime;

	/**
	 * The list of Event handlers to use. This is transient, since the event
	 * handlers cannot usually be serialized. This means that you cannot attach
	 * a post serialization event to an item.
	 * <p>
	 * TODO we need to check that when an item is passed to a non-local cache
	 * that if the local cache had a copy with event handlers, that those
	 * handlers are used.
	 */
	private transient ArrayList<ElementEventHandler> eventHandlers;

	/**
	 * Initializes a new {@link ElementAttributesImpl}
	 */
	public ElementAttributesImpl() {
		super();
		this.createTime = System.currentTimeMillis();
		this.lastAccessTime = this.createTime;
	}

	/**
	 * Constructor for the element attributes object
	 * 
	 * @param attr
	 *            The element attributes object
	 */
	protected ElementAttributesImpl(final ElementAttributesImpl attr) {
		isEternal = attr.isEternal;
		// Waterfall onto disk, for pure disk set memory to 0
		isSpool = attr.isSpool;
		// lateral
		isLateral = attr.isLateral;
		// central RMI store
		isRemote = attr.isRemote;
		maxLifeSeconds = attr.maxLifeSeconds;
		// time-to-live
		maxIdleTimeSeconds = attr.maxIdleTimeSeconds;
		size = attr.size;
	}

	@Override
	public Object clone() {
		try {
			final ElementAttributesImpl attr = (ElementAttributesImpl) super.clone();
			/*
			 * Set create/last-access time to now and do not copy from this
			 * attributes
			 */
			attr.createTime = System.currentTimeMillis();
			attr.lastAccessTime = attr.createTime;
			attr.eventHandlers = (ArrayList<ElementEventHandler>) eventHandlers.clone();
			return attr;
		} catch (final CloneNotSupportedException e) {
			/*
			 * Cannot occur since we are cloneable
			 */
			throw new InternalError("Clone failed even though java.lang.Cloneable interface is implemented");
		}
	}

	public void addElementEventHandler(final ElementEventHandler eventHandler) {
		// lazy here, no concurrency problems expected
		if (this.eventHandlers == null) {
			this.eventHandlers = new ArrayList<ElementEventHandler>();
		}
		this.eventHandlers.add(eventHandler);
	}

	public void addElementEventHandlers(final ArrayList<ElementEventHandler> eventHandlers) {
		if (eventHandlers == null || eventHandlers.size() == 0) {
			return;
		}
		if (this.eventHandlers == null) {
			this.eventHandlers = new ArrayList<ElementEventHandler>();
		}
		eventHandlers.addAll(eventHandlers);
	}

	public ElementAttributes copy() {
		try {
			return (ElementAttributes) clone();
		} catch (final Exception e) {
			return new ElementAttributesImpl();
		}
	}

	public long getCreateTime() {
		return createTime;
	}

	public ArrayList<ElementEventHandler> getElementEventHandlers() {
		return (ArrayList<ElementEventHandler>) eventHandlers.clone();
	}

	public long getIdleTime() {
		return maxIdleTimeSeconds;
	}

	public boolean getIsEternal() {
		return isEternal;
	}

	public boolean getIsLateral() {
		return isLateral;
	}

	public boolean getIsRemote() {
		return isRemote;
	}

	public boolean getIsSpool() {
		return isSpool;
	}

	public long getLastAccessTime() {
		return lastAccessTime;
	}

	public long getMaxLifeSeconds() {
		return maxLifeSeconds;
	}

	public int getSize() {
		return size;
	}

	public long getTimeToLiveSeconds() {
		final long now = System.currentTimeMillis();
		return ((this.getCreateTime() + (this.getMaxLifeSeconds() * 1000)) - now) / 1000;
	}

	public long getVersion() {
		return version;
	}

	public void setIdleTime(final long idle) {
		this.maxIdleTimeSeconds = idle;
	}

	public void setIsEternal(final boolean val) {
		isEternal = val;

	}

	public void setIsLateral(final boolean val) {
		isLateral = val;
	}

	public void setIsRemote(final boolean val) {
		isRemote = val;
	}

	public void setIsSpool(final boolean val) {
		isSpool = val;
	}

	public void setLastAccessTimeNow() {
		this.lastAccessTime = System.currentTimeMillis();
	}

	public void setMaxLifeSeconds(final long mls) {
		this.maxLifeSeconds = mls;
	}

	public void setSize(final int size) {
		this.size = size;
	}

	public void setVersion(final long version) {
		this.version = version;
	}

}
