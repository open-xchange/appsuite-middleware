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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.tools.versit.valuedefinitions.rfc2445;

import java.io.IOException;

import com.openexchange.tools.encoding.Base64;
import com.openexchange.tools.versit.Encoding;
import com.openexchange.tools.versit.FoldingWriter;
import com.openexchange.tools.versit.Parameter;
import com.openexchange.tools.versit.Property;
import com.openexchange.tools.versit.Scanner;
import com.openexchange.tools.versit.StringScanner;
import com.openexchange.tools.versit.ValueDefinition;
import com.openexchange.tools.versit.VersitException;
import com.openexchange.tools.versit.encodings.BASE64Encoding;

/**
 * @author Viktor Pracht
 */
public class BinaryValueDefinition extends ValueDefinition {

    public BinaryValueDefinition(final String[] encodingNames, final Encoding[] encodings) {
        super(encodingNames, encodings);
    }

    @Override
    public Object createValue(final StringScanner s, final Property property) throws IOException {
        return s.getRest().getBytes(com.openexchange.java.Charsets.ISO_8859_1);
    }

    @Override
    public String writeValue(final Object value) {
        return new String((byte[]) value, com.openexchange.java.Charsets.ISO_8859_1);
    }

    @Override
    public void write(final FoldingWriter fw, final Property property) throws IOException {
    	String value = null;
        final Parameter encodingParam = property.getParameter("ENCODING");
        if (encodingParam != null) {
            final String enc_name = encodingParam.getValue(0).getText();
            final Encoding encoding = getEncoding(enc_name);
            if (encoding == null) {
                throw new IOException("Unknown encoding: " + enc_name);
            }           
            final Object propValue = property.getValue(); 
            if (null != propValue && BASE64Encoding.class.equals(encoding.getClass()) && byte[].class.equals(propValue.getClass())) {
            	// no need to encode to Latin-1 and back
            	value = Base64.encode((byte[])propValue);
            } else {
            	// use default write method
                value = writeValue(property.getValue());
            }
        } else {
            value = writeValue(property.getValue());
        }
        fw.writeln(value);
    }
    
    @Override
    public Object parse(final Scanner s, final Property property) throws IOException {
        final StringBuilder sb = new StringBuilder();
        while ((s.peek >= ' ' || s.peek == '\t') && s.peek != 0x7f) {
            sb.append((char) s.read());
        }
        final String text = sb.toString();
        final Parameter encodingParam = property.getParameter("ENCODING");
        if (encodingParam != null) {
            final String EncName = encodingParam.getValue(0).getText();
            final Encoding encoding = getEncoding(EncName);
            if (encoding == null) {
                throw new VersitException(s, "Unknown encoding: " + EncName);
            }
            if (BASE64Encoding.class.equals(encoding.getClass())) {
            	// no need to encode to Latin-1 and back
            	return Base64.decode(text);
            } else {
            	// use encoding with default createValue method
            	return createValue(new StringScanner(s, encoding.decode(text)), property);
            }
        } else {
        	return createValue(new StringScanner(s, text), property);
        }    	
    }
}
