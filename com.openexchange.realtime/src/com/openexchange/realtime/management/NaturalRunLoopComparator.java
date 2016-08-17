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

package com.openexchange.realtime.management;

import static com.openexchange.realtime.synthetic.RunLoopManager.LOOP_NAMING_INFIX;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.realtime.synthetic.SyntheticChannelRunLoop;


/**
 * {@link NaturalRunLoopComparator}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since v7.8.0
 */
public class NaturalRunLoopComparator implements Comparator<SyntheticChannelRunLoop> {
    private final static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(NaturalRunLoopComparator.class);
    private final static Pattern COMPARISON_PATTERN = Pattern.compile("(\\S+"+LOOP_NAMING_INFIX+")(\\d+)");
    
    @Override
    public int compare(SyntheticChannelRunLoop o1, SyntheticChannelRunLoop o2) {
        Matcher matcher1 = COMPARISON_PATTERN.matcher(o1.getName());
        Matcher matcher2 = COMPARISON_PATTERN.matcher(o2.getName());
        try {
            if(matcher1.matches() && matcher2.matches()) {
                int prefixComparison = matcher1.group(1).compareTo(matcher2.group(1));
                if(prefixComparison != 0) {
                    return prefixComparison;
                }
                return Integer.valueOf(matcher1.group(2)).compareTo(Integer.valueOf(matcher2.group(2)));
            }
        } catch (NumberFormatException | IllegalStateException | IndexOutOfBoundsException e) {
            LOG.warn("RunLoop name doesn't match pattern. Continuing with default String comparison");
        }
        return o1.getName().compareTo(o2.getName());
    }
}
