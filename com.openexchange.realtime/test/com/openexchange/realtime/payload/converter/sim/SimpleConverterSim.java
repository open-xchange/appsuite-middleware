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

package com.openexchange.realtime.payload.converter.sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.conversion.simple.SimpleConverter;
import com.openexchange.conversion.simple.SimplePayloadConverter;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link SimpleConverterSim} Adapts AbstractPOJOConverters and AbstractJSONConverters to ease simulation of the DefaultConverter service.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class SimpleConverterSim implements SimpleConverter {
    
    private Map<String, List<ConverterBox>> converterMap;
    
    public SimpleConverterSim() {
        converterMap = new HashMap<String, List<ConverterBox>>();
    }
    
    public void registerConverter(SimplePayloadConverter payloadConverter) {
        String inputFormat = payloadConverter.getInputFormat();
        List<ConverterBox> converters = converterMap.get(inputFormat);
        if(converters == null) {
            converters = new ArrayList<ConverterBox>();
            converterMap.put(inputFormat, converters);
        }
        converters.add(new ConverterBox(payloadConverter.getOutputFormat(), adaptPayloadConverter(payloadConverter, this)));
    }
    
    @Override
    public Object convert(String from, String to, Object data, ServerSession session) throws OXException {
        List<ConverterBox> list = converterMap.get(from);
        SimpleConverter converter = null;
        for (ConverterBox converterBox : list) {
            if(converterBox.getOutPutFormat().equals(to)) {
                converter = converterBox.getSimpleConverter();
            }
        }
        return converter.convert(from, to, data, session);
    }
    
    private SimpleConverter adaptPayloadConverter(final SimplePayloadConverter payloadConverter, final SimpleConverter simpleConverter) {
        return new SimpleConverter() {
            
            SimpleConverter collectingSimpleConverter = simpleConverter;
            
            @Override
            public Object convert(String from, String to, Object data, ServerSession session) throws OXException {
                return payloadConverter.convert(data, null, collectingSimpleConverter);
            }
        };
    }

    private class ConverterBox {

        String to;

        SimpleConverter converter;

        public ConverterBox(String to, SimpleConverter converter) {
            super();
            this.to = to;
            this.converter = converter;
        }

        public String getOutPutFormat() {
            return to;
        }

        public SimpleConverter getSimpleConverter() {
            return converter;
        }

    }
}
