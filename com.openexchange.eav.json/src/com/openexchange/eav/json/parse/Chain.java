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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.eav.json.parse;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.eav.EAVNode;
import com.openexchange.eav.json.exception.EAVJsonException;
import com.openexchange.eav.json.exception.EAVJsonExceptionMessage;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Chain implements ParserChain {

    private List<Parser> parsers = new ArrayList<Parser>();

    public Chain(Parser... parsers) {
        for (Parser parser : parsers) {
            this.parsers.add(parser);
            parser.setChain(this);
        }
    }

    public void parse(String key, Object object, EAVNode node) throws JSONException, EAVJsonException {
        for (Parser parser : parsers) {
            if (parser.isResponsibeFor(object)) {
                parser.parse(key, object, node);
                return;
            }
        }
    }

    public void parseMultiple(String key, Object[] objects, EAVNode node) throws EAVJsonException {
        if (objects.length == 0) {
            return;
        }
        for (Parser parser : parsers) {
            if (parser.isResponsibeFor(objects[0])) {
                try {
                    parser.parseMultiple(key, objects, node);
                } catch (ClassCastException e) {
                    throw EAVJsonExceptionMessage.DifferentTypesInArray.create(e);
                }
                return;
            }
        }

    }

}
