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

package com.openexchange.webdav.action.ifheader;

public abstract class IfHeaderEntity {

	public static class  LockToken extends IfHeaderEntity {

		public LockToken(final String payload) {
			super(payload);
		}

		@Override
		public boolean isETag() {
			return false;
		}

		@Override
		public boolean isLockToken() {
			return true;
		}

	}

	public static class ETag extends IfHeaderEntity{

		public ETag(final String etag) {
			super(etag);
		}

		@Override
		public boolean isETag(){
			return true;
		}

		@Override
		public boolean isLockToken() {
			return false;
		}
	}

	private final String payload;
	private boolean matches;

	public IfHeaderEntity(final String payload) {
		this.payload = payload;
	}

	public abstract boolean isETag();

	public abstract boolean isLockToken();

	public String getPayload() {
		return payload;
	}

	public boolean mustMatch() {
		return matches;
	}

	public void setMatches(final boolean matches) {
		this.matches = matches;
	}

}
