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

package com.openexchange.mail;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * {@link MailAPITestSuite}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class MailAPITestSuite extends TestSuite {

	/**
	 * Initializes a new {@link MailAPITestSuite}
	 */
	public MailAPITestSuite() {
		super();
	}

	/**
	 * @return a test suite containing smoke tests.
	 */
	public static Test suite() {
		final TestSuite mailSuite = new TestSuite();
		/*
		 * Storages consistency
		 */
		mailSuite.addTestSuite(com.openexchange.mail.storagesconsistency.MailStoragesConsistencyTest.class);
		/*
		 * Message storage
		 */
//		mailSuite.addTestSuite(com.openexchange.mail.messagestorage.MailAppendTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.messagestorage.MailAttachmentTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.messagestorage.MailColorLabelTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.messagestorage.MailCopyTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.messagestorage.MailDeleteTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.messagestorage.MailFlagsTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.messagestorage.MailGetTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.messagestorage.MailImageTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.messagestorage.MailMoveTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.messagestorage.MailSaveDraftTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.messagestorage.MailSearchTest.class);
		/*
		 * Reply/forward
		 */
		mailSuite.addTestSuite(com.openexchange.mail.replyforward.MailForwardTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.replyforward.MailReplyTest.class);
		/*
		 * Folder storage
		 */
		mailSuite.addTestSuite(com.openexchange.mail.folderstorage.MailFolderTest.class);
		/*
		 * Utility tests
		 */
		mailSuite.addTestSuite(com.openexchange.mail.utilitytests.MailCharsetDetectorTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.utilitytests.MailMessageSerializationTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.utilitytests.CSSMatcherTest.class);
		/*
		 * Unique ID tests
		 */
		mailSuite.addTestSuite(com.openexchange.mail.MailIDTest.class);
		/*
		 * Bugfix tests
		 */
		mailSuite.addTestSuite(com.openexchange.mail.MailBugfixTest.class);
		/*
		 * Structured output tests
		 */
		mailSuite.addTestSuite(com.openexchange.mail.structure.MailSimpleStructureTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.structure.MailMultipartAlternativeStructureTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.structure.MailMultipartMixedStructureTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.structure.MailNestedMessageStructureTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.structure.MailTNEFStructureTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.structure.MailUUEncodedStructureTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.structure.MailPlainTextStructureTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.structure.Bug16174StructureTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.structure.Bug18846StructureTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.structure.Bug18981StructureTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.structure.Bug19471StructureTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.structure.Bug20425_StructureTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.structure.Bug22735_StructureTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.structure.Bug23037_StructureTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.structure.Bug26317_StructureTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.structure.Bug29227_StructureTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.structure.Bug29484_StructureTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.structure.Bug27640_StructureTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.structure.SMIMEStructureTest.class);
		mailSuite.addTestSuite(com.openexchange.mail.structure.SMIMEStructureTest2.class);
		/*
		 * MimeMessageConverter tests
		 */
		mailSuite.addTestSuite(MailConverterTest.class);
		return mailSuite;
	}
}
