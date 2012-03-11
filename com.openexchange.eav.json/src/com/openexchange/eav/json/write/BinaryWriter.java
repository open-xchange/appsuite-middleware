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

package com.openexchange.eav.json.write;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.eav.EAVNode;
import com.openexchange.eav.EAVType;
import com.openexchange.eav.json.exception.EAVJsonException;
import com.openexchange.eav.json.exception.EAVJsonExceptionMessage;
import com.openexchange.tools.encoding.Base64;


/**
 * {@link BinaryWriter}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class BinaryWriter extends AbstractWriter<String>{
    {
        TYPES = EnumSet.of(EAVType.BINARY);
    }
    
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(BinaryWriter.class));
    
    public void write(EAVNode node, JSONObject json) throws JSONException, EAVJsonException {
        if (node.isMultiple()) {
            writeMultiple(node.getName(), encode((InputStream[]) node.getPayload()), json);
        } else {
            writeSingle(node.getName(), encode((InputStream) node.getPayload()), json);
        }
    }
    
    private String[] encode(InputStream[] payload) throws EAVJsonException {
        String[] retval = new String[payload.length];
        int index = 0;
        for (InputStream inputStream : payload) {
            retval[index++] = encode(inputStream);
        }
        return retval;
    }

    private String encode(InputStream payload) throws EAVJsonException {
        BufferedInputStream inputStream = new BufferedInputStream(payload);
        try {
            List<Byte> bytes = new ArrayList<Byte>(1024);
            int read = -1;
            while((read = inputStream.read()) != -1) {
                bytes.add((byte)read);
            }
            byte[] byteArr = new byte[bytes.size()];
            int index = 0;
            for (Byte byte1 : bytes) {
                byteArr[index++] = byte1;
            }
            
            return Base64.encode(byteArr);
        } catch (IOException x) {
            throw EAVJsonExceptionMessage.IOException.create(x);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        } 
    }

}
