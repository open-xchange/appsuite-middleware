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

package com.openexchange.smtp.dataobjects;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.dataobjects.compose.UploadFileMailPart;

/**
 * {@link SMTPFilePart} - A {@link MailPart} implementation that keeps a
 * reference to a temporary uploaded file that shall be added as an attachment
 * later
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class SMTPFilePart extends UploadFileMailPart {

	private static final long serialVersionUID = -3267699308710097989L;

	/**
	 * Constructor
	 *
	 * @throws OXException
	 *             If upload file's content type cannot be parsed
	 */
	public SMTPFilePart(final UploadFile uploadFile) throws OXException {
		super(uploadFile);
	}

}
