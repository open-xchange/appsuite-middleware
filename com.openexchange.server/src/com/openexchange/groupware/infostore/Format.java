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

package com.openexchange.groupware.infostore;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Format implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int HTML = 1;
	public static final int PDF = 2;
	public static final int WIKI = 3;
	public static final int WIKI_WITH_XML = 4;
	public static final int MS_WORD_DOC = 5;
	public static final int OPEN_OFFICE = 6;

	public static final Format HTML_LITERAL = new Format(HTML,"HTML","text/html","html");
	public static final Format PDF_LITERAL = new Format(PDF,"PDF","application/octet-stream","pdf");
	public static final Format WIKI_LITERAL = new Format(WIKI,"WIKI","text/plain","wiki");
	public static final Format WIKI_WITH_XML_LITERAL = new Format(WIKI_WITH_XML,"WIKI_WITH_XML","text/plain","xwiki");
	public static final Format MS_WORD_DOC_LITERAL = new Format(MS_WORD_DOC,"MS_WORD_DOC","text/todo","doc");
	public static final Format OPEN_OFFICE_LITERAL = new Format(OPEN_OFFICE,"OPEN_OFFICE","text/todo","odt");

	private static final Format[] VALUES_ARRAY = new Format[]{
		HTML_LITERAL,
		PDF_LITERAL,
		WIKI_LITERAL,
		WIKI_WITH_XML_LITERAL,
		MS_WORD_DOC_LITERAL,
		OPEN_OFFICE_LITERAL
	};

	public static transient final List<Format> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));


	private final int id;
	private final String name;
	private final String mimeType;
	private final String extension;

	private Format(final int id, final String name, final String mimeType, final String extension){
		this.id = id;
		this.name = name;
		this.mimeType = mimeType;
		this.extension = extension;
	}

	public static Format get(final int i){
		switch(i){
		case HTML: return HTML_LITERAL;
		case PDF: return PDF_LITERAL;
		case WIKI: return WIKI_LITERAL;
		case WIKI_WITH_XML: return WIKI_WITH_XML_LITERAL;
		case MS_WORD_DOC: return MS_WORD_DOC_LITERAL;
		default: return null;
		}
	}

	public static Format get(final String format){
		for(final Format f : VALUES){
			if(f.getName().equals(format)) {
				return f;
			}
		}
		return null;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getMimeType() {
		return mimeType;
	}

	public String getExtension(){
		return extension;
	}

	@Override
	public int hashCode(){
		return id;
	}

    @Override
    public boolean equals(final Object o) {
        if (null == o) {
            return false;
        }
        try {
            return ((Format) o).id == id;
        } catch (final ClassCastException x) {
            return false;
        }
    }

    public static Format getByExtension(final String extension) {
        for (final Format f : VALUES) {
            if (f.getExtension().equals(extension)) {
                return f;
            }
        }
        return null;
    }


}
