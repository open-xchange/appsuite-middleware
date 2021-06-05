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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import com.openexchange.database.tx.AbstractDBAction;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.util.GetSwitch;
import com.openexchange.tx.UndoableAction;

public abstract class AbstractAttachmentAction extends AbstractDBAction
		implements UndoableAction {

	private AttachmentQueryCatalog queryCatalog = null;

	protected int fillFields(final AttachmentMetadata attachment, final PreparedStatement stmt) throws SQLException {
		final GetSwitch get = new GetSwitch(attachment);
		int i = 1;
		for(final AttachmentField field : queryCatalog.getFields()) {
			Object value = field.doSwitch(get);
			if (isDateField(field)) {
				value = Long.valueOf(((Date)value).getTime());
			}
			if (field.equals(AttachmentField.RTF_FLAG_LITERAL)) {
				value = Integer.valueOf((attachment.getRtfFlag() ) ? 1 : 0);
			}
			stmt.setObject(i++,value);
		}
		stmt.setInt(i++,getContext().getContextId()); //System.out.println(stmt);
		return i;
	}

	private final boolean isDateField(final AttachmentField field) {
		return field.equals(AttachmentField.CREATION_DATE_LITERAL);
	}


	public void setQueryCatalog(final AttachmentQueryCatalog queryCatalog) {
		this.queryCatalog = queryCatalog;
	}

	public AttachmentQueryCatalog getQueryCatalog(){
		return queryCatalog;
	}
}
