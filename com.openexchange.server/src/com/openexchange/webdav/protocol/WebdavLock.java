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

package com.openexchange.webdav.protocol;


public class WebdavLock {

	public static enum Type { WRITE_LITERAL }
	public static enum Scope { EXCLUSIVE_LITERAL, SHARED_LITERAL }

	public static final long NEVER = -1;

	private Type type;
	private Scope scope;
	private int depth;
	private String owner;
	private long expires;
	private boolean neverExpires;
	private String token;
	private final long creationTime = System.currentTimeMillis();
	private int ownerID;

	/**
	 * Initializes a new {@link WebdavLock}.
	 */
	public WebdavLock() {
	    super();
	}

	public int getDepth() {
		return depth;
	}
	public void setDepth(final int depth) {
		this.depth = depth;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(final String owner) {
		this.owner = owner;
	}
	public Scope getScope() {
		return scope;
	}
	public void setScope(final Scope scope) {
		this.scope = scope;
	}
	public long getTimeout() {
		if(neverExpires) {
			return NEVER;
		}
		final long timeout = expires-System.currentTimeMillis();
		if(timeout < 0) {
			return 0;
		}
		return timeout;
	}
	public void setTimeout(final long timeout) {
		if(timeout == NEVER) {
			neverExpires = true;
			return;
		}
		neverExpires = false;
		this.expires = creationTime+timeout;
	}
	public String getToken() {
		return token;
	}
	public void setToken(final String token) {
		this.token = token;
	}
	public Type getType() {
		return type;
	}
	public void setType(final Type type) {
		this.type = type;
	}

    /**
     * Gets the user ID of the lock's owner.
     *
     * @return The lock owner's user ID
     */
    public int getOwnerID() {
        return ownerID;
    }

    /**
     * Sets the user ID of the lock's owner.
     *
     * @param ownerID The owner ID to set
     */
    public void setOwnerID(int ownerID) {
        this.ownerID = ownerID;
    }

	public boolean locks(final WebdavResource locked, final WebdavResource resource) {
		final WebdavPath urlLocked = locked.getUrl();
		final WebdavPath urlRes = resource.getUrl();

		if(!urlRes.startsWith(urlLocked)) {
			return false;
		}
		if(depth == WebdavCollection.INFINITY) {
			return true;
		}
		if(depth == 0) {
			return urlLocked.equals(urlRes);
		}
		if(depth == 1) {
			return urlLocked.equals(urlRes.parent());
		}
		return false;
	}

	@Override
	public int hashCode(){
		return token.hashCode();
	}

	@Override
	public boolean equals(final Object other){
		if (other instanceof WebdavLock) {
			final WebdavLock otherLock = (WebdavLock) other;
			return otherLock.token.equals(token);
		}
		return false;
	}

	public boolean isActive(final long time) {
		return getTimeout()!=0;
	}

}
