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

package com.openexchange.mail.spellcheck;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * SpellCheckConfig
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class SpellCheckConfig {

	private boolean enabled;

	private int breakpoint;

	private Map<String, DictionaryConfig> dictionaries;

	public SpellCheckConfig() {
		super();
		dictionaries = new HashMap<String, DictionaryConfig>();
	}

	public static final class DictionaryConfig {

		private String id;

		private Map<String, String> titles;

		private boolean debug;

		private String command;

		public DictionaryConfig() {
			super();
			titles = new HashMap<String, String>();
		}

		public String getCommand() {
			return command;
		}

		public void setCommand(final String command) {
			this.command = command;
		}

		public boolean isDebug() {
			return debug;
		}

		public void setDebug(final boolean debug) {
			this.debug = debug;
		}

		public void addTitle(final String language, final String title) {
			titles.put(language, title);
		}

		public String removeTitle(final String language) {
			return titles.remove(language);
		}

		public String getTitle(final String language) {
			return titles.get(language);
		}

		public Map<String, String> getTitles() {
			return titles;
		}

		public void setTitles(final Map<String, String> titles) {
			this.titles = titles;
		}

		public String getId() {
			return id;
		}

		public void setId(final String id) {
			this.id = id;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append("Dictionary: id=").append(id).append(" | debug=").append(debug);
			sb.append(" | command=").append(command);
			sb.append(" | titles=").append(titles);
			return sb.toString();
		}

	}

	public int getBreakpoint() {
		return breakpoint;
	}

	public void setBreakpoint(final int breakpoint) {
		this.breakpoint = breakpoint;
	}

	public void addDictionary(final String language, final DictionaryConfig d) {
		dictionaries.put(language, d);
	}

	public DictionaryConfig removeDictionary(final String language) {
		return dictionaries.remove(language);
	}

	public DictionaryConfig getDictionary(final String language) {
		return dictionaries.get(language);
	}

	public Map<String, DictionaryConfig> getDictionaries() {
		return dictionaries;
	}

	public void setDictionaries(final Map<String, DictionaryConfig> dictionaries) {
		this.dictionaries = dictionaries;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("SpellCheckConfig: enabled=").append(enabled).append(" | breakpoint=").append(breakpoint);
		if (dictionaries != null && !dictionaries.isEmpty()) {
			sb.append(" | dictionaries={");
			final int dicSize = dictionaries.size();
			final Iterator<Map.Entry<String, DictionaryConfig>> iter = dictionaries.entrySet().iterator();
			for (int i = 0; i < dicSize; i++) {
				final Map.Entry<String, DictionaryConfig> entry = iter.next();
				sb.append(entry.getKey()).append('=');
				sb.append(entry.getValue().toString());
				if (i < dicSize - 1) {
					sb.append(", ");
				}
			}
			sb.append('}');
		}
		return sb.toString();
	}

}
