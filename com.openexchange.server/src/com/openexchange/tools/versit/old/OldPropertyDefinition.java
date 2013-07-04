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

package com.openexchange.tools.versit.old;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.mail.internet.MimeUtility;
import org.apache.commons.logging.Log;
import com.openexchange.java.Charsets;
import com.openexchange.log.LogFactory;
import com.openexchange.tools.versit.Parameter;
import com.openexchange.tools.versit.ParameterValue;
import com.openexchange.tools.versit.ParameterValueDefinition;
import com.openexchange.tools.versit.Property;
import com.openexchange.tools.versit.VersitException;
import com.openexchange.tools.versit.VersitObject;

public class OldPropertyDefinition {

    /**
     * Logger.
     */
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(OldPropertyDefinition.class));

    private final Map<String, OldParamDefinition> Params = new HashMap<String, OldParamDefinition>();

    private final Map<Object, String> ParamValues = new HashMap<Object, String>();

    public OldPropertyDefinition(final String[] paramNames, final OldParamDefinition[] params) {
        final Set<Object> duplicates = new HashSet<Object>();
        for (int i = 0; i < paramNames.length; i++) {
            Params.put(paramNames[i], params[i]);
            final int size = params[i].size();
            final Iterator<String> j = params[i].getValues();
            for (int k = 0; k < size; k++) {
                final Object value = j.next();
                if (ParamValues.containsKey(value)) {
                    ParamValues.remove(value);
                    duplicates.add(value);
                } else if (!duplicates.contains(value)) {
                    ParamValues.put(value, paramNames[i]);
                }
            }
        }
    }

    public void parse(final OldScanner s, final String name, final VersitObject object) throws IOException {
        final Property property = new Property(name);
        parse(s, property);
        object.addProperty(property);
    }

    public void parse(final OldScanner s, final Property property) throws IOException {
        s.skipWS();
        boolean uri = false;
        while (s.peek == ';') {
            s.read();
            s.skipWS();
            final String param = s.parseWord().toUpperCase();
            s.skipWS();
            if (s.peek == '=') {
                s.read();
                s.skipWS();
                OldParamDefinition paramdef = Params.get(param);
                if (paramdef == null) {
                    paramdef = OldParamDefinition.Default;
                }
                final Parameter parameter = new Parameter(param);
                paramdef.parse(s, parameter, (uri = "URI".equals(param)));
                property.addParameter(parameter);
                while (s.peek == ',') {
                    // New parameter value definition in old VCard
                    s.read();
                    final ParameterValue parameterValue = ParameterValueDefinition.Default.parse(s);
                    if (parameterValue != null) {
                        parameter.addValue(parameterValue);
                    }
                }

            } else {
                final String paramname = ParamValues.get(param);
                if (paramname != null) {
                    final Parameter parameter = new Parameter(paramname);
                    parameter.addValue(new ParameterValue(param));
                    property.addParameter(parameter);
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Unknown property parameter: " + param);
                    }
                }
            }
            s.skipWS();
        }
        if (s.peek != ':') {
            if (uri) {
                property.setValue("");
                return;
            }
            throw new VersitException(s, "':' expected");
        }
        s.read();
        OldEncoding encoding;
        Parameter param = property.getParameter("ENCODING");
        if (param != null) {
            final String encoding_str = param.getValue(0).getText();
            if ("QUOTED-PRINTABLE".equalsIgnoreCase(encoding_str)) {
                encoding = OldQuotedPrintable.Default;
            } else if ("BASE64".equalsIgnoreCase(encoding_str)) {
                encoding = OldBase64Encoding.Default;
            } else if ("8BIT".equalsIgnoreCase(encoding_str)) {
                encoding = OldXBitEncoding.Default;
            } else if ("7BIT".equalsIgnoreCase(encoding_str)) {
                encoding = OldXBitEncoding.Default;
            } else {
                throw new VersitException(s, "Unknown encoding: " + encoding_str);
            }
        } else {
            encoding = s.DefaultEncoding;
        }
        param = property.getParameter("CHARSET");
        final String charset = param == null ? s.DefaultCharset : param.getValue(0).getText();
        try {
            property.setValue(parseValue(property, s, encoding.decode(s), MimeUtility.javaCharset(charset)));
        } catch (final UnsupportedEncodingException e) {
            final VersitException ve = new VersitException(s, "Unsupported charset");
            ve.initCause(e);
            throw (ve);
        }
    }

    protected Object parseValue(final Property property, final OldScanner s, final byte[] value, final String charset) throws IOException {
        return new String(value, Charsets.forName(charset)).replaceAll("\\\\(.)", "$1");
    }

    protected void writeType(final OldFoldingWriter fw, final Property property) throws IOException {
        fw.write(property.name);
        final Parameter type = property.getParameter("TYPE");
        if (type != null) {
            for (int i = 0; i < type.getValueCount(); i++) {
                fw.write(";");
                final String val = type.getValue(i).getText();
                if (!ParamValues.containsKey(val)) {
                    fw.write("TYPE");
                    fw.write("=");
                }
                fw.write(val);
            }
        }
    }

    public void write(final OldFoldingWriter fw, final Property property) throws IOException {
        writeType(fw, property);
        String value = writeValue(property).replaceAll("([^\\r])\\n", "$1\\r\\n");
        if (value.length() > 0 && value.charAt(0) == '\n') {
            value = "\r" + value;
        }
        OldEncoding encoding = OldXBitEncoding.Default;
        if (value.length() <= 74 - fw.lineLength()) {
            for (int i = 0; i < value.length(); i++) {
                final char c = value.charAt(i);
                if (c != 9 && c < 32 || c > 126) {
                    encoding = OldQuotedPrintable.Default;
                    fw.write(";");
                    fw.write("QUOTED-PRINTABLE");
                    break;
                }
            }
        } else {
            encoding = OldQuotedPrintable.Default;
            fw.write(";");
            fw.write("QUOTED-PRINTABLE");
        }
        String charset = fw.charset;
        if (!fw.encoder.canEncode(value)) {
            fw.write(";");
            fw.write("CHARSET");
            fw.write("=");
            fw.write("UTF-8");
            charset = "UTF8";
        }
        fw.write(":");
        encoding.encode(fw, value.getBytes(charset));
    }

    protected String writeValue(final Property property) {
        return property.getValue().toString().replaceAll("\\\\", "\\\\\\\\");
    }

}
