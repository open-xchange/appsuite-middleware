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

package com.openexchange.groupware.attach.impl;

import java.sql.SQLException;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentMetadata;

public abstract class AttachmentListQueryAction extends AbstractAttachmentAction {

	private List<AttachmentMetadata> attachments;


	protected void doUpdates(final String query, final List<AttachmentMetadata> attachments, final boolean addId) throws OXException {
		final UpdateBlock[] updates = new UpdateBlock[attachments.size()];
		int i = 0;
		for(final AttachmentMetadata m : attachments) {
			updates[i++] = new Update(query) {

				@Override
				public void fillStatement() throws SQLException {
					final int number = fillFields(m, stmt);
					if (addId) {
						stmt.setInt(number,m.getId());
					}
				}

			};
		}
		doUpdates(updates);
	}

	public List<AttachmentMetadata> getAttachments() {
		return attachments;
	}

	public void setAttachments(final List<AttachmentMetadata> attachments) {
		this.attachments = attachments;
	}

}
