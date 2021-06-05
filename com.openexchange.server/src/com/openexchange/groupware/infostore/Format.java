/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
			if (f.getName().equals(format)) {
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
        } catch (ClassCastException x) {
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
