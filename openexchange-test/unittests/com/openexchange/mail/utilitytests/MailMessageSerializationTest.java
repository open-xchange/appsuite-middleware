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

package com.openexchange.mail.utilitytests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.MimeTypes;

/**
 * {@link MailMessageSerializationTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class MailMessageSerializationTest extends AbstractMailTest {

	/**
	 *
	 */
	public MailMessageSerializationTest() {
		super();
	}

	/**
	 * @param name
	 */
	public MailMessageSerializationTest(final String name) {
		super(name);
	}

	public void testMailSerialization() {
		try {
			final MailMessage[] mails = getMessages(getTestMailDir(), -1);

			final File file = new File("/tmp/dummy" + System.currentTimeMillis() + ".dat");
			if (file.createNewFile()) {
				file.deleteOnExit();
			}

			for (final MailMessage mail : mails) {
				if (mail.getContentType().isMimeType(MimeTypes.MIME_MULTIPART_ALL)) {
					/*
					 * Serialize to file
					 */
					final OutputStream out = new FileOutputStream(file, false);
					final ObjectOutputStream outputStream = new ObjectOutputStream(out);
					try {
						outputStream.writeObject(mail);
						outputStream.flush();
					} finally {
						outputStream.close();
						out.close();
					}
					/*
					 * Check serialized object, if content still accessible
					 */
					final int count = mail.getEnclosedCount();
					for (int i = 0; i < count; i++) {
						final MailPart part = mail.getEnclosedMailPart(i);
						if (part.getContentType().isMimeType(MimeTypes.MIME_MULTIPART_ALL)) {
							final int c = part.getEnclosedCount();
							assertTrue("Count not available from multipart part", c != MailPart.NO_ENCLOSED_PARTS);
						} else {
							final Object content = part.getContent();
							assertTrue("Content object not available from part", content != null);
						}
					}
					/*
					 * Deserialize from file
					 */
					final MailMessage clone;
					final InputStream in = new FileInputStream(file);
					final ObjectInputStream inputStream = new ObjectInputStream(in);
					try {
						clone = (MailMessage) inputStream.readObject();
					} finally {
						inputStream.close();
						in.close();
					}
					/*
					 * Check deserialized object, if content still accessible
					 */
					final int cloneCount = clone.getEnclosedCount();
					assertTrue("Enclosed part count does not match", cloneCount == count);
					for (int i = 0; i < cloneCount; i++) {
						final MailPart part = clone.getEnclosedMailPart(i);
						if (part.getContentType().isMimeType(MimeTypes.MIME_MULTIPART_ALL)) {
							final int c = part.getEnclosedCount();
							assertTrue("Count not available from multipart part", c != MailPart.NO_ENCLOSED_PARTS);
						} else {
							final Object content = part.getContent();
							assertTrue("Content object not available from part", content != null);
						}
					}
				} else {
					/*
					 * Serialize to file
					 */
					final OutputStream out = new FileOutputStream(file);
					final ObjectOutputStream outputStream = new ObjectOutputStream(out);
					try {
						outputStream.writeObject(mail);
						outputStream.flush();
					} finally {
						outputStream.close();
						out.close();
					}
					/*
					 * Check serialized object, if content still accessible
					 */
					final Object origContent = mail.getContent();
					assertTrue("Original content not available after serialization", origContent != null);
					/*
					 * Deserialize from file
					 */
					final MailMessage clone;
					final InputStream in = new FileInputStream(file);
					final ObjectInputStream inputStream = new ObjectInputStream(in);
					try {
						clone = (MailMessage) inputStream.readObject();
					} finally {
						inputStream.close();
						in.close();
					}
					final Object cloneContent = clone.getContent();
					assertTrue("Cloned content not available after serialization", cloneContent != null);
					/*
					 * Check some conditions
					 */
					assertTrue("Original content class and cloned content class are not equal", origContent.getClass()
							.isInstance(cloneContent));
				}
			}

		} catch (final Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
