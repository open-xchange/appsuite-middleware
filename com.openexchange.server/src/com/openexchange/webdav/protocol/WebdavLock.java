/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
		if (neverExpires) {
			return NEVER;
		}
		final long timeout = expires-System.currentTimeMillis();
		if (timeout < 0) {
			return 0;
		}
		return timeout;
	}
	public void setTimeout(final long timeout) {
		if (timeout == NEVER) {
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

		if (!urlRes.startsWith(urlLocked)) {
			return false;
		}
		if (depth == WebdavCollection.INFINITY) {
			return true;
		}
		if (depth == 0) {
			return urlLocked.equals(urlRes);
		}
		if (depth == 1) {
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
