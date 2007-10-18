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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.charset;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.charset.spi.CharsetProvider;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link AliasCharsetProvider}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class AliasCharsetProvider extends CharsetProvider {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(AliasCharsetProvider.class);

	private static final Lock LOCK = new ReentrantLock();

	private static final AtomicBoolean initialized = new AtomicBoolean();

	private static Map<String, Charset> name2charset;

	private static Collection<Charset> charsets;

	private static final Charset FALLBACK = Charset.forName("US-ASCII");

	/**
	 * Default constructor
	 */
	public AliasCharsetProvider() {
		super();
	}

	/**
	 * Retrieves a charset for the given charset name.
	 * </p>
	 * 
	 * @param charsetName
	 *            The name of the requested charset; may be either a canonical
	 *            name or an alias
	 * 
	 * @return A charset object for the named charset, or <tt>null</tt> if the
	 *         named charset is not supported by this provider
	 */
	@Override
	public Charset charsetForName(final String charsetName) {
		if (!initialized.get()) {
			init();
		}
		/*
		 * Get charset instance for given name (case insensitive)
		 */
		Charset c = name2charset.get(charsetName.toLowerCase());
		try {
			if (c == null) {
				if (LOG.isErrorEnabled()) {
					LOG.error(new StringBuilder(128).append("Unknown charset: ").append(charsetName).append(
							"\nPlease add a proper delegate charset to AliasCharsetProvider"));
				}
				c = FALLBACK;
			}
		} catch (final Exception e) {
			/*
			 * If we can't get an instance, we don't.
			 */
			LOG.error(e.getLocalizedMessage(), e);
			c = null;
		}
		return c;
	}

	/**
	 * Creates an iterator that iterates over the charsets supported by this
	 * provider. This method is used in the implementation of the {@link
	 * java.nio.charset.Charset#availableCharsets Charset.availableCharsets}
	 * method.
	 * </p>
	 * 
	 * @return The new iterator
	 */
	@Override
	public Iterator<Charset> charsets() {
		if (!initialized.get()) {
			init();
		}
		return charsets.iterator();
	}

	/**
	 * Initializes this charset provider's data.
	 */
	private void init() {
		LOCK.lock();
		try {
			if (initialized.get()) {
				return;
			}
			/*
			 * Prepare supported charsets
			 */
			final Charset[] cs = new Charset[] {
			    new AliasCharset("BIG-5", new String[] { "BIG_5" }, Charset.forName("BIG5")),
			    new AliasCharset("UTF_8", null, Charset.forName("UTF-8")),
			    new AliasCharset("x-unknown", null, FALLBACK)
			};
			charsets = Collections.unmodifiableCollection(Arrays.asList(cs));
			final Map<String, Charset> n2c = new HashMap<String, Charset>();
			for (int i = 0; i < cs.length; i++) {
				final Charset c = cs[i];
				n2c.put(c.name().toLowerCase(), c);
				for (final Iterator<String> iter = c.aliases().iterator(); iter.hasNext();) {
					n2c.put(iter.next().toLowerCase(), c);
				}
			}
			name2charset = n2c;
			initialized.set(true);
			if (LOG.isInfoEnabled()) {
				LOG.info("Alias charset provider successfully initialized");
			}
		} catch (final IllegalCharsetNameException e) {
			LOG.error(e.getLocalizedMessage(), e);
		} catch (final UnsupportedCharsetException e) {
			LOG.error(e.getLocalizedMessage(), e);
		} catch (final IllegalArgumentException e) {
			LOG.error(e.getLocalizedMessage(), e);
		} finally {
			LOCK.unlock();
		}
	}
}
