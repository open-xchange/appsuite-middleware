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

package com.openexchange.groupware.infostore.webdav;

public class Lock {
    private int entity;
	private int owner;
	private int id;
	private long timeout;
	private EntityLockManager.Scope scope;
	private EntityLockManager.Type type;
	private String ownerDescription;

	public int getId() {
		return id;
	}
	public void setId(final int id) {
		this.id = id;
	}
	public EntityLockManager.Scope getScope() {
		return scope;
	}
	public void setScope(final EntityLockManager.Scope scope) {
		this.scope = scope;
	}
	public long getTimeout() {
		return timeout;
	}
	public void setTimeout(final long timeout) {
		this.timeout = timeout;
	}
	public EntityLockManager.Type getType() {
		return type;
	}
	public void setType(final EntityLockManager.Type type) {
		this.type = type;
	}
	public int getOwner() {
		return owner;
	}
	public void setOwner(final int userid) {
		this.owner = userid;
	}
	public String getOwnerDescription() {
		return ownerDescription;
	}
	public void setOwnerDescription(final String ownerDescription) {
		this.ownerDescription = ownerDescription;
	}
    public int getEntity() {
        return entity;
    }
	public void setEntity(int entity) {
        this.entity = entity;
    }


}
