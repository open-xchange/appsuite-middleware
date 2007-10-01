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
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SpellCheckConfigParser
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class SpellCheckConfigParser {

	public SpellCheckConfigParser() {
		super();
	}
	
	final public SpellCheckConfig parseSpellCheckConfig(final String spellCheckConfigStr) throws Exception {
		final XMLReader reader = createXMLReader();
		final SpellCheckConfig retval = new SpellCheckConfig();
		reader.setContentHandler(new SpellCheckConfigHandler(retval));
		reader.parse(new InputSource(spellCheckConfigStr));
		return retval;
	}

	final private XMLReader createXMLReader() throws Exception {
		final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		final SAXParser saxParser = saxParserFactory.newSAXParser();
		final XMLReader parser = saxParser.getXMLReader();
		return parser;
	}
	
	final private class SpellCheckConfigHandler extends DefaultHandler {
		
		private static final String TAG_SPELLCHECK = "SpellCheck";
		
		private static final String TAG_DICTIONARY = "dictionary";
		
		private static final String TAG_ELEMENT = "element";
		
		private static final String TAG_EXECUTE = "execute";
		
		private static final String ATTRIB_ENABLED = "enabled";
		
		private static final String ATTRIB_BREAKPOINT = "breakpoint";
		
		private static final String ATTRIB_LANGUAGE = "language";
		
		private static final String ATTRIB_DEFAULT = "default";
		
		private static final String ATTRIB_ID = "id";
		
		private static final String ATTRIB_TITLE = "title";
		
		private static final String ATTRIB_DEBUG = "debug";
		
		private static final String ATTRIB_CMD = "cmd";
		
		private static final String EXC_MSG = "Invalid structure in SpellCheck config file";
		
		private static final int IDLE = 0;
		
		private static final int SPELLCHECK = 1;
		
		private static final int DICTIONARY = 2;
		
		private int state = IDLE;
		
		private Map<String,String> expectedDictionaries;
		
		private SpellCheckConfig spellCheckConfig;
		
		private SpellCheckConfig.DictionaryConfig currentDic;
		
		private SpellCheckConfigHandler(SpellCheckConfig scc) {
			spellCheckConfig = scc;
			expectedDictionaries = new HashMap<String,String>();
		}
		
		private void print(final String context, final String text) {
			System.out.println(new StringBuilder().append(context).append(": \"").append(text).append("\".").toString());
		}

		@Override
		public void startElement(final String namespace, final String localname, final String type,
				final Attributes attributes) throws SAXException {
			if (type.equalsIgnoreCase(TAG_SPELLCHECK)) {
				state = SPELLCHECK;
			} else if (type.equalsIgnoreCase(TAG_DICTIONARY)) {
				if (state != SPELLCHECK) {
					throw new SAXException(EXC_MSG);
				}
				state = DICTIONARY;
				if (attributes == null || attributes.getLength() == 0) {
					throw new SAXException("Missing attributes in tag " + TAG_DICTIONARY);
				}
				final String dicID = attributes.getValue(ATTRIB_ID);
				if (dicID == null) {
					throw new SAXException("Invalid structure in SpellCheck config file. Missing attribute " + ATTRIB_ID);
				}
				if (expectedDictionaries.containsKey(dicID)) {
					final SpellCheckConfig.DictionaryConfig dic = new SpellCheckConfig.DictionaryConfig();
					dic.setId(dicID);
					spellCheckConfig.addDictionary(expectedDictionaries.get(dicID), dic);
					currentDic = dic;
				}
			} else if (type.equalsIgnoreCase(TAG_ELEMENT)) {
				if (state == IDLE) {
					throw new SAXException(EXC_MSG);
				}
				if (attributes == null || attributes.getLength() == 0) {
					throw new SAXException("Missing attributes in tag " + TAG_ELEMENT);
				}
				if (state == SPELLCHECK) {
					int index = -1;
					if ((index = attributes.getIndex(ATTRIB_ENABLED)) != -1) {
						spellCheckConfig.setEnabled(Boolean.parseBoolean(attributes.getValue(index)));
					} else if ((index = attributes.getIndex(ATTRIB_BREAKPOINT)) != -1) {
						spellCheckConfig.setBreakpoint(Integer.parseInt(attributes.getValue(index)));
					} else if ((index = attributes.getIndex(ATTRIB_LANGUAGE)) != -1) {
						final String dictionaryLanguage = attributes.getValue(index);
						final String defaultDicID = attributes.getValue(ATTRIB_DEFAULT);
						if (defaultDicID == null) {
							throw new SAXException("Invalid structure in SpellCheck config file. Missing attribute " + ATTRIB_DEFAULT);
						}
						expectedDictionaries.put(defaultDicID,dictionaryLanguage);
					}
				} else if (state == DICTIONARY) {
					if (currentDic == null) {
						return;
					}
					int index = -1;
					if ((index = attributes.getIndex(ATTRIB_DEBUG)) != -1) {
						currentDic.setDebug(Boolean.parseBoolean(attributes.getValue(index)));
					} else if ((index = attributes.getIndex(ATTRIB_TITLE)) != -1) {
						final String title = attributes.getValue(index);
						final String language = attributes.getValue(ATTRIB_LANGUAGE);
						if (language == null) {
							throw new SAXException("Invalid structure in SpellCheck config file. Missing attribute " + ATTRIB_LANGUAGE);
						}
						currentDic.addTitle(language, title);
					}
				}
			} else if (type.equalsIgnoreCase(TAG_EXECUTE)) {
				if (state != DICTIONARY) {
					throw new SAXException(EXC_MSG);
				}
				int index = -1;
				if ((index = attributes.getIndex(ATTRIB_CMD)) != -1) {
					currentDic.setCommand(attributes.getValue(index));
				} 
			}
		}

		@Override
		public void endElement(final String namespace, final String localname, final String type)
				throws org.xml.sax.SAXException {
			if (type.equalsIgnoreCase(TAG_SPELLCHECK)) {
				if (state != SPELLCHECK) {
					throw new SAXException(EXC_MSG);
				}
				state = IDLE;
			} else if (type.equalsIgnoreCase(TAG_DICTIONARY)) {
				if (state != DICTIONARY) {
					throw new SAXException(EXC_MSG);
				}
				state = SPELLCHECK;
			}
		}

		@Override
		public void characters(final char[] ch, final int start, final int len) {
			final String text = new String(ch, start, len);
			final String text1 = text.trim();
			if (text1.length() > 0) {
				print("characters  ", text1);
			}
		}
	}

}
