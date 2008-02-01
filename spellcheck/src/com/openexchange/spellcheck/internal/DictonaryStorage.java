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

package com.openexchange.spellcheck.internal;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;

import com.openexchange.config.Configuration;
import com.openexchange.server.ServiceException;
import com.openexchange.spellcheck.SpellCheckException;
import com.openexchange.spellcheck.services.SpellCheckConfigurationService;
import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;

/**
 * {@link DictonaryStorage} - Storage for global locale-specific spell check
 * dictionaries
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class DictonaryStorage {

	private static final String CHARSET_US_ASCII = "US-ASCII";

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(DictonaryStorage.class);

	/**
	 * Initializes a new {@link DictonaryStorage}
	 */
	private DictonaryStorage() {
		super();
	}

	/**
	 * The dictionary map
	 */
	private static final Map<Locale, SpellDictionary> dictionaries = new ConcurrentHashMap<Locale, SpellDictionary>();

	private static final AtomicBoolean initializing = new AtomicBoolean();

	/**
	 * The sub-directory pattern
	 */
	private static final Pattern PAT_LOCALE = Pattern.compile("([a-z]{2})_([A-Z]{2})");

	/**
	 * The filename pattern for phonetic files
	 */
	private static final Pattern PAT_FILENAME_PHON = Pattern.compile("([a-z]{2})_([A-Z]{2})\\.phon",
			Pattern.CASE_INSENSITIVE);

	/**
	 * The filename pattern for word lists
	 */
	private static final Pattern PAT_FILENAME_WL = Pattern.compile("([a-z]{2})_([A-Z]{2})([^.])*\\.wl",
			Pattern.CASE_INSENSITIVE);

	/**
	 * Gets the locale-specific dictionary specified through given locale string
	 * 
	 * @param localeStr
	 *            The locale string (e.g. <code>en_EN</code>)
	 * @return The locale-specific dictionary or <code>null</code>
	 * @throws SpellCheckException
	 *             If locale string is invalid
	 */
	public static SpellDictionary getDictionary(final String localeStr) throws SpellCheckException {
		final Matcher m = PAT_LOCALE.matcher(localeStr);
		if (!m.matches()) {
			throw new SpellCheckException(SpellCheckException.Code.INVALID_LOCALE_STR, localeStr);
		}
		return getDictionary(new Locale(m.group(1), m.group(2)));
	}

	/**
	 * Gets the locale-specific dictionary specified through given locale
	 * 
	 * @param locale
	 *            The locale
	 * @return The locale-specific dictionary or <code>null</code>
	 */
	public static SpellDictionary getDictionary(final Locale locale) {
		final SpellDictionary retval = dictionaries.get(locale);
		if (null != retval) {
			/*
			 * Exact match
			 */
			return retval;
		}
		final CombinedSpellDictionary dic = new CombinedSpellDictionary();
		if (null == locale.getCountry() || locale.getCountry().length() == 0) {
			/*
			 * Gather all locales with same language
			 */
			for (final Iterator<Locale> iter = dictionaries.keySet().iterator(); iter.hasNext();) {
				final Locale key = iter.next();
				if (locale.getLanguage().equals(key.getLanguage())) {
					dic.addSpellDictionaries(dictionaries.get(key));
				}
			}
		} else if (null == locale.getVariant() || locale.getVariant().length() == 0) {
			/*
			 * Gather all locales with same language
			 */
			for (final Iterator<Locale> iter = dictionaries.keySet().iterator(); iter.hasNext();) {
				final Locale key = iter.next();
				if (locale.getLanguage().equals(key.getLanguage()) && locale.getCountry().equals(key.getCountry())) {
					dic.addSpellDictionaries(dictionaries.get(key));
				}
			}
		}
		return dic.isEmpty() ? null : dic;
	}

	/**
	 * Returns an {@link Iterator} for available instances of {@link Locale} for
	 * which a dictionary has been added to this storage.
	 * 
	 * @return An {@link Iterator} for available instances of {@link Locale}
	 */
	public static Iterator<Locale> getAvailableLocales() {
		return dictionaries.keySet().iterator();
	}

	/**
	 * Puts a spell directory
	 * 
	 * @param localeStr
	 *            The dictionary's locale string
	 * @param spellDictionary
	 *            The spell directory
	 */
	public static void putDictionary(final String localeStr, final SpellDictionary spellDictionary) {
		final Matcher m = PAT_LOCALE.matcher(localeStr);
		if (!m.matches()) {
			throw new IllegalArgumentException("Illegal locale string: " + localeStr
					+ ". Excpected \"<language-in-lower-case>_<country-in-upper-case>\"");
		}
		putDictionary(new Locale(m.group(1), m.group(2)), spellDictionary);
	}

	/**
	 * Puts a spell directory
	 * 
	 * @param locale
	 *            The dictionary's locale
	 * @param spellDictionary
	 *            The spell directory
	 */
	public static void putDictionary(final Locale locale, final SpellDictionary spellDictionary) {
		dictionaries.put(locale, spellDictionary);
	}

	/**
	 * Clears global locale-specific dictionaries
	 */
	public static void clearDictionaries() {
		if (!initializing.compareAndSet(false, true)) {
			/*
			 * Another thread
			 */
			return;
		}
		dictionaries.clear();
		initializing.set(false);
	}

	/**
	 * Loads the global locale-specific dictionaries
	 * 
	 * @throws SpellCheckException
	 *             If loading of dictionaries fails
	 */
	public static void loadDictionaries() throws SpellCheckException {
		if (!initializing.compareAndSet(false, true)) {
			/*
			 * Another thread
			 */
			return;
		}
		if (dictionaries.size() > 0) {
			dictionaries.clear();
		}
		final File spellCheckDir;
		{
			final Configuration c = SpellCheckConfigurationService.getInstance().getService();
			if (null != c) {
				try {
					final String dirPath = c.getProperty("com.openexchange.spellcheck.dir");
					if (dirPath == null) {
						throw new SpellCheckException(SpellCheckException.Code.MISSING_PROPERTY,
								"com.openexchange.spellcheck.dir");
					}
					spellCheckDir = new File(dirPath);
				} finally {
					SpellCheckConfigurationService.getInstance().ungetService(c);
				}
			} else {
				throw new SpellCheckException(new ServiceException(ServiceException.Code.SERVICE_UNAVAILABLE));
			}
		}
		if (!spellCheckDir.exists() || !spellCheckDir.isDirectory()) {
			throw new SpellCheckException(SpellCheckException.Code.MISSING_DIR, spellCheckDir.getPath());
		}
		final List<Locale> ll = new ArrayList<Locale>();
		final File[] subdirs = spellCheckDir.listFiles(new FileFilter() {
			public boolean accept(final File pathname) {
				final Matcher m = PAT_LOCALE.matcher(pathname.getName());
				if (m.matches() && pathname.isDirectory()) {
					ll.add(new Locale(m.group(1), m.group(2)));
					return true;
				}
				return false;
			}
		});
		if (subdirs == null || subdirs.length == 0) {
			throw new SpellCheckException(SpellCheckException.Code.NO_LOCALE_FOUND);
		}
		for (int i = 0; i < subdirs.length; i++) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Processing dictionary for locale '" + ll.get(i).toString() + "' ...");
			}
			dictionaries.put(ll.get(i), getDicFromDirectory(subdirs[i]));
			if (LOG.isInfoEnabled()) {
				LOG.info("Dictionary for locale '" + ll.get(i).toString() + "' successfully added to spell check");
			}
		}
		initializing.set(false);
	}

	private static SpellDictionary getDicFromDirectory(final File dir) throws SpellCheckException {
		/*
		 * Detect phonetic file
		 */
		final File phonFile;
		{
			final File[] files = dir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return PAT_FILENAME_PHON.matcher(name).matches();
				}
			});
			if (files.length == 1) {
				phonFile = files[0];
				if (LOG.isInfoEnabled()) {
					LOG.info("Using phonetic file " + phonFile.getName());
				}
			} else if (files.length == 0) {
				phonFile = null;
			} else {
				throw new SpellCheckException(SpellCheckException.Code.ONLY_ONE_PHON_FILE);
			}
		}
		/*
		 * Iterate word list files
		 */
		final File[] files = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return PAT_FILENAME_WL.matcher(name).matches();
			}
		});
		if (files != null && files.length > 0) {
			SpellDictionary spellDictionary;
			try {
				spellDictionary = generateSpellDictionary(files[0], phonFile);
				if (LOG.isInfoEnabled()) {
					LOG.info("Added word list file " + files[0].getName());
				}
				if (files.length > 1) {
					final CombinedSpellDictionary combinedDict = new CombinedSpellDictionary(spellDictionary);
					for (int i = 1; i < files.length; i++) {
						combinedDict.addSpellDictionaries(generateSpellDictionary(files[i], phonFile));
						if (LOG.isInfoEnabled()) {
							LOG.info("Added word list file " + files[i].getName());
						}
					}
					spellDictionary = combinedDict;
				}
			} catch (final IOException e) {
				throw new SpellCheckException(SpellCheckException.Code.IO_ERROR, e, e.getLocalizedMessage());
			}
			return spellDictionary;
		}
		throw new SpellCheckException(SpellCheckException.Code.AT_LEAST_ONE_WL_FILE);
	}

	private static SpellDictionaryHashMap generateSpellDictionary(final File wordList, final File phonetic)
			throws IOException {
		Reader wordListReader = null;
		Reader phonetReader = null;
		try {
			wordListReader = new InputStreamReader(new FileInputStream(wordList), detectCharset(new FileInputStream(
					wordList)));
			if (null == phonetic) {
				return new SpellDictionaryHashMap(wordListReader);
			}
			phonetReader = new InputStreamReader(new FileInputStream(phonetic), detectCharset(new FileInputStream(
					phonetic)));
			return new SpellDictionaryHashMap(wordListReader, phonetReader);
		} finally {
			if (null != wordListReader) {
				try {
					wordListReader.close();
				} catch (IOException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
			}
			if (null != phonetReader) {
				try {
					phonetReader.close();
				} catch (IOException e) {
					LOG.error(e.getLocalizedMessage(), e);
				}
			}
		}
	}

	private static String detectCharset(final InputStream in) {
		final nsDetector det = new nsDetector(nsPSMDetector.ALL);
		/*
		 * Set an observer: The Notify() will be called when a matching charset
		 * is found.
		 */
		final CharsetDetectionObserver observer = new CharsetDetectionObserver();
		det.Init(observer);
		try {
			final byte[] buf = new byte[1024];
			int len;
			boolean done = false;
			boolean isAscii = true;

			while ((len = in.read(buf, 0, buf.length)) != -1) {
				/*
				 * Check if the stream is only ascii.
				 */
				if (isAscii) {
					isAscii = det.isAscii(buf, len);
				}
				/*
				 * DoIt if non-ascii and not done yet.
				 */
				if (!isAscii && !done) {
					done = det.DoIt(buf, len, false);
				}
			}
			det.DataEnd();
			/*
			 * Check if content is ascii
			 */
			if (isAscii) {
				return CHARSET_US_ASCII;
			}
			{
				/*
				 * Check observer
				 */
				final String charset = observer.getCharset();
				if (null != charset && Charset.isSupported(charset)) {
					return charset;
				}
			}
			/*
			 * Choose first possible charset
			 */
			final String prob[] = det.getProbableCharsets();
			for (int i = 0; i < prob.length; i++) {
				if (Charset.isSupported(prob[i])) {
					return prob[i];
				}
			}
			return CHARSET_US_ASCII;

		} catch (final FileNotFoundException e) {
			LOG.error(e.getLocalizedMessage(), e);
			return CHARSET_US_ASCII;
		} catch (final IOException e) {
			LOG.error(e.getLocalizedMessage(), e);
			return CHARSET_US_ASCII;
		}
	}

	private static final class CharsetDetectionObserver implements nsICharsetDetectionObserver {

		private String charset;

		/**
		 * Initializes a new {@link CharsetDetectionObserver}
		 */
		public CharsetDetectionObserver() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.mozilla.intl.chardet.nsICharsetDetectionObserver#Notify(java.lang.String)
		 */
		public void Notify(final String charset) {
			this.charset = charset;
		}

		/**
		 * @return The charset applied to this observer
		 */
		public String getCharset() {
			return charset;
		}
	}
}
