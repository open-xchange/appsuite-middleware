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


package com.openexchange.ajax.spellcheck;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * AJAXSpellCheckResult
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class AJAXSpellCheckResult {
	
	public static final int TYPE_ERROR = 0;
	
    public static final int TYPE_OK = 1;
    
    public static final int TYPE_NONE = 2;
    
    public static final int TYPE_SUGGESTION = 3;
	
	private int lineNumber = -1;
	
	private int offset = -1;
	
	private String originalWord;
	
	private int type = -1;
	
	private List<String> suggestions;

	public AJAXSpellCheckResult() {
		super();
		suggestions = new ArrayList<String>();
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(final int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public int getOffset() {
		return offset;
	}
	
	public int getOffsetEnd() {
		return (offset + originalWord.length());
	}

	public void setOffset(final int offset) {
		this.offset = offset;
	}

	public String getOriginalWord() {
		return originalWord;
	}

	public void setOriginalWord(final String originalWord) {
		this.originalWord = originalWord;
	}

	public int getType() {
		return type;
	}

	public void setType(final int type) {
		this.type = type;
	}
	
	public void addSuggestion(final String suggestion) {
		suggestions.add(suggestion);
	}
	
	public void addAllSuggestion(final List<String> suggestions) {
		this.suggestions.addAll(suggestions);
	}
	
	public void clearSuggestions() {
		suggestions.clear();
	}
	
	public int getSuggestionsSize() {
		return suggestions.size();
	}
	
	public Iterator<String> getSuggestionsIterator() {
		return suggestions.iterator();
	}
}
