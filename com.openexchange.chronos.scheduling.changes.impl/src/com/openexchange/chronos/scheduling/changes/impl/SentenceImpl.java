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

package com.openexchange.chronos.scheduling.changes.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.Transp;
import com.openexchange.chronos.compat.ShownAsTransparency;
import com.openexchange.chronos.scheduling.changes.Sentence;
import com.openexchange.chronos.scheduling.common.ContextSensitiveMessages;
import com.openexchange.i18n.tools.StringHelper;

/**
 * {@link SentenceImpl}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a> - Moved with v7.10.3
 */
public class SentenceImpl implements Sentence {

    private final static Logger LOGGER = LoggerFactory.getLogger(SentenceImpl.class);

    private final String message;
    private final List<Object> arguments = new ArrayList<Object>();
    private final List<ArgumentType> types = new ArrayList<ArgumentType>();
    private final List<Object[]> extra = new ArrayList<Object[]>();

    public SentenceImpl(String message) {
        this.message = message;
    }

    public SentenceImpl add(Object argument, ArgumentType type, Object... extra) {
        arguments.add(argument);
        types.add(type);
        this.extra.add(extra);
        return this;
    }

    public SentenceImpl add(Object argument) {
        return add(argument, ArgumentType.NONE);
    }

    public SentenceImpl addStatus(ParticipationStatus status) {
        return add("", ArgumentType.STATUS, status);
    }

    @Override
    public String getMessage(String format, Locale locale) {
        return "html".equalsIgnoreCase(format) ? getMessage(new HTMLWrapper(), locale) : getMessage(locale);
    }

    public String getMessage(TypeWrapper wrapper, Locale locale) {
        List<String> wrapped = new ArrayList<String>(arguments.size());
        StringHelper sh = StringHelper.valueOf(locale);

        for (int i = 0, size = arguments.size(); i < size; i++) {
            Object argument = arguments.get(i);
            ArgumentType type = types.get(i);
            Object[] extraInfo = extra.get(i);

            switch (type) {
                case NONE:
                    wrapped.add(wrapper.none(argument));
                    break;
                case ORIGINAL:
                    wrapped.add(wrapper.original(argument));
                    break;
                case UPDATED:
                    wrapped.add(wrapper.updated(argument));
                    break;
                case PARTICIPANT:
                    wrapped.add(wrapper.participant(argument));
                    break;
                case STATUS:
                    ParticipationStatus status = (ParticipationStatus) extraInfo[0];
                    if (ParticipationStatus.ACCEPTED.equals(status)) {
                        argument = ContextSensitiveMessages.getInstance().accepted(locale, ContextSensitiveMessages.Context.VERB);
                    } else if (ParticipationStatus.DECLINED.equals(status)) {
                        argument = ContextSensitiveMessages.getInstance().declined(locale, ContextSensitiveMessages.Context.VERB);
                    } else if (ParticipationStatus.TENTATIVE.equals(status)) {
                        argument = ContextSensitiveMessages.getInstance().tentative(locale, ContextSensitiveMessages.Context.VERB);
                    } else {
                        argument = sh.getString((String) argument);
                    }
                    wrapped.add(wrapper.state(argument, (ParticipationStatus) extraInfo[0]));
                    break;
                case EMPHASIZED:
                    wrapped.add(wrapper.emphasiszed(argument));
                    break;
                case ITALIC:
                    wrapped.add(wrapper.italic(argument));
                    break;
                case REFERENCE:
                    wrapped.add(wrapper.reference(argument));
                    break;
                case SHOWN_AS:
                    if (argument instanceof String) {
                        String str = (String) argument;
                        if (str.trim().length() != 0) {
                            argument = sh.getString(str);
                        }
                    }
                    if (null != extraInfo && 0 < extraInfo.length && null != extraInfo[0]) {
                        if (ShownAsTransparency.class.isInstance(extraInfo[0])) {
                            wrapped.add(wrapper.shownAs(argument, (ShownAsTransparency) extraInfo[0]));
                        } else if (Transp.class.isInstance(extraInfo[0])) {
                            ShownAsTransparency shownAs = Transp.TRANSPARENT.equals(extraInfo[0]) ? ShownAsTransparency.FREE : ShownAsTransparency.RESERVED;
                            wrapped.add(wrapper.shownAs(argument, shownAs));
                        } else {
                            LOGGER.warn("Unexpected transparency {}, skipping.", extraInfo[0]);
                        }
                    }
                    break;

                default:
                    LOGGER.debug("Unknown ArgumentType {}", argument);
                    break;
            }
        }

        String localized = sh.getString(message);
        return String.format(localized, wrapped.toArray(new Object[wrapped.size()]));
    }

    public String getMessage(Locale locale) {
        return getMessage(new PassthroughWrapper(), locale);
    }

}
