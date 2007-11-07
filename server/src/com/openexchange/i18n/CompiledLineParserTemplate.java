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

package com.openexchange.i18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Compiles a template as per LineParserUtility syntax, with a simple substitution of [variables]. Allows escaping via \ 
public abstract class CompiledLineParserTemplate extends AbstractTemplate {

	
	
	private String[] chunks;
	
	
	public String render(final Map<String, String> substitutions) {
		if(chunks == null) {
			load();
		}
		final StringBuilder result = new StringBuilder();
		
		boolean substitute = false;
		
		for(String chunk : chunks) {
			
			String substitution = chunk;
			if(substitute) {
				substitution = substitutions.get(chunk);
				if (null == substitution) {
					substitution = "";
				}
			}
			result.append(
				substitution
			);
			substitute = !substitute;
		}
		
		return result.toString();
	}

	private final void load() {
			if(chunks != null) {
				return;
			}
			final char[] content = getContent();
			if(null == content) {
				return;
			}
			
			final List<String> chunkCollector = new ArrayList<String>();
			
			// Lexer for the poor
			// LABSKAUSS!!!!! ;-)
			final StringBuilder currentChunk = new StringBuilder();
			boolean escaped = false;
			int lineCount = 1;
			int columnCount = 1;
			int[] open = null; 
			
			for(int i = 0; i < content.length; i++) {
				final char c = content[i];
				switch(c) {
				case '[' :
						if(escaped) {
							currentChunk.append(c);
							escaped = false;
						} else {
							chunkCollector.add(currentChunk.toString());
							currentChunk.setLength(0);
							open = new int[]{lineCount, columnCount};
						}
						columnCount++;
					break;
				case ']' :
					if(escaped) {
						currentChunk.append(c);
						escaped = false;
					} else {
						chunkCollector.add(currentChunk.toString());
						currentChunk.setLength(0);
						open = null;
					}
					columnCount++;
					break;
				case '\\' :
					if(escaped) {
						currentChunk.append(c);
						escaped = false;
					} else {
						escaped = true;
					}
					columnCount++;
					break;
				case '\n' :
					lineCount++;
					columnCount=0;
					currentChunk.append(c);
					break;
				default :
					if(escaped) {
						escaped = false;
					} else {
						currentChunk.append(c);
					}
				columnCount++;
				}
			}
			
			if(currentChunk.length() > 0) {
				chunkCollector.add(currentChunk.toString());
			}
			
			synchronized(this) {
				
				if(open != null) {
					chunks = new String[]{"Parser Error: Seems that the bracket opened on line "+open[0]+" column "+open[1]+" is never closed."};
					return;
				}
				
				chunks = chunkCollector.toArray(new String[chunkCollector.size()]);
				
			}
		
		
	}

	protected abstract char[] getContent();

}
