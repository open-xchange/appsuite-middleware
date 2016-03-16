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

import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.sessiond.impl.SessionObject;
import com.openexchange.sessiond.impl.SessionObjectWrapper;

/**
 * {@link MailAccessTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class MailAccessTest extends AbstractMailTest {

	/**
	 *
	 */
	public MailAccessTest() {
		super();
	}

	/**
	 * @param name
	 */
	public MailAccessTest(final String name) {
		super(name);
	}

	public void testMailAccess() throws OXException, InterruptedException {
			final SessionObject session = getSession();
			MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();
			/*
			 * close
			 */
			mailAccess.close(true);

			/*
			 * Test with altered mail configuration
			 */
			try {
				mailAccess = MailAccess.getInstance(session);
				final MailConfig mailConfig = mailAccess.getMailConfig();
				mailConfig.setLogin(getLogin());
				mailConfig.setPassword(getPassword());
				mailConfig.setServer(getServer());
				mailConfig.setPort(getPort());
				mailAccess.connect();
			} catch (final Exception e) {
			} finally {
				try {
					/*
					 * close
					 */
					mailAccess.close(false);
				} catch (final Exception e2) {
				}
			}


			mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();
			/*
			 * close
			 */
			mailAccess.close(false);

			session.setPassword(null);
			mailAccess = MailAccess.getInstance(session);
			/*
			 * Should fail
			 */
			try {
				mailAccess.connect();
				assertTrue("Connect invocation should fail", false);
			} catch (final Exception e) {
				assertTrue(true);
			}


			session.setPassword(getPassword());
			mailAccess = MailAccess.getInstance(session);
			mailAccess.connect();
			mailAccess.getMessageStorage().getAllMessages("INBOX", null, null, null,
					new MailField[] { MailField.ID });
			/*
			 * close
			 */
			mailAccess.close(true);

			/*
			 * Test if cache closes connection
			 */
			Thread.sleep(10000);
	}

	public void testSimultaneousConnections() throws InterruptedException {
			final MyRunnable runnable = new MyRunnable(this);
			final Thread[] threads = new Thread[50];
			for (int i = 0; i < threads.length; i++) {
				threads[i] = new Thread(runnable);
			}

			for (final Thread thread : threads) {
				thread.start();
			}

			/*
			 * Test if cache closes connection(s)
			 */
			boolean wait = true;
			while (wait) {
				wait = false;
				for (int i = 0; i < threads.length && !wait; i++) {
					wait = threads[i].isAlive();
				}
				if (wait) {
					Thread.sleep(1000);
				}
			}
			Thread.sleep(10000);
			assertTrue("Zero connections should be open", 0 == MailAccess.getCounter());
	}

	private static final class MyRunnable implements Runnable {

		private final AbstractMailTest testRef;

		public MyRunnable(final AbstractMailTest testRef) {
			this.testRef = testRef;
		}

		@Override
        public void run() {
			try {
				final SessionObject session = SessionObjectWrapper.createSessionObject(testRef.getUser(),
						new ContextImpl(testRef.getCid()), "mail-test-session");
				session.setPassword(testRef.getPassword());
				MailAccess<?, ?> mailAccess = MailAccess.getInstance(session);
				mailAccess.connect();
				/*
				 * close
				 */
				mailAccess.close(false);

				mailAccess = MailAccess.getInstance(session);
				mailAccess.connect();
				/*
				 * close
				 */
				mailAccess.close(false);

				session.setPassword(null);
				mailAccess = MailAccess.getInstance(session);
				/*
				 * Should fail
				 */
				try {
					mailAccess.connect();
					assertTrue("Connect invocation should fail", false);
				} catch (final Exception e) {
					assertTrue(true);
				}

				session.setPassword(testRef.getPassword());
				mailAccess = MailAccess.getInstance(session);
				mailAccess.connect();
				mailAccess.getMessageStorage().getAllMessages("INBOX", null, null, null,
						new MailField[] { MailField.ID });
				/*
				 * close
				 */
				mailAccess.close(false);

			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

	}
}
