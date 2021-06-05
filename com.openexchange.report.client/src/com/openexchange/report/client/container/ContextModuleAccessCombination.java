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

package com.openexchange.report.client.container;

public class ContextModuleAccessCombination {

	private String userAccessCombination;

	private String userCount;

	private String inactiveCount;

	public ContextModuleAccessCombination(String userAccessCombination,
			String userCount, String inactiveCount) {
		this.userAccessCombination = userAccessCombination;
		this.userCount = userCount;
		this.inactiveCount = inactiveCount;
	}

	public String getUserAccessCombination() {
		return userAccessCombination;
	}

	public void setUserAccessCombination(String userAccessCombination) {
		this.userAccessCombination = userAccessCombination;
	}

	public String getUserCount() {
		return userCount;
	}

	public void setUserCount(String userCount) {
		this.userCount = userCount;
	}

	public String getInactiveCount() {
		return inactiveCount;
	}

	public void setInactiveCount(String inactiveCount) {
		this.inactiveCount = inactiveCount;
	}

	@Override
	public String toString() {
		return "ContextModuleAccessCombinationObject [userAccessCombination="
				+ userAccessCombination + ", userCount=" + userCount + "]";
	}

}
