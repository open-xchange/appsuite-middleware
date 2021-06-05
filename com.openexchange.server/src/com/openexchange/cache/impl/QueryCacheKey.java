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

package com.openexchange.cache.impl;

import java.io.Serializable;

/**
 * QueryCacheKey
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class QueryCacheKey implements Serializable {

	private static final long serialVersionUID = -8531547389903446885L;

	public static enum Module {

		FOLDER(1);

		private int num;

		private Module(final int num) {
			this.num = num;
		}

		public int getNum() {
			return num;
		}
	}

	private final int cid;

	private final int userId;

	private final Module module;

	private final int queryNum;

	private final int hash;

	/**
     * Constructor
     */
	public QueryCacheKey(final int cid, final int userId, final Module module, final int queryNum) {
		super();
		this.cid = cid;
		this.userId = userId;
		this.module = module;
		this.queryNum = queryNum;
		hash = queryNum ^ (module.getNum() ^ (userId ^ cid));
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof QueryCacheKey)) {
			return false;
		}
		final QueryCacheKey other = (QueryCacheKey) obj;
		return (this.cid == other.cid) && (this.userId == other.userId) && (this.module.num == other.module.num)
				&& (this.queryNum == other.queryNum);
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public String toString() {
		return new StringBuilder(50).append("QueryCacheKey context=").append(cid).append(" | userId=").append(userId)
				.append(" | module=").append(module.num).append(" | queryNum=").append(queryNum).toString();
	}

}
