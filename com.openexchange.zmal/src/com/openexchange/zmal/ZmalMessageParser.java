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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.zmal;

import java.util.Collections;
import java.util.List;
import com.openexchange.mail.dataobjects.MailMessage;
import com.zimbra.common.soap.Element;

/**
 * {@link ZmalMessageParser}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ZmalMessageParser {

    /**
     * Initializes a new {@link ZmalMessageParser}.
     */
    private ZmalMessageParser() {
        super();
    }

    public static List<MailMessage> parseMessages(final ZmalSoapResponse response) {
        List<Element> results = response.getResults();

    }

    public static List<MailMessage> parseElement(final Element element) {
        if (null == element) {
            return null;
        }
        // Check if current element denoted a message
        if (isMessage(element)) {
            return Collections.singletonList(parseSingleMessage(element));
        }

        List<Element> elements = element.listElements();
        for (Element sub : elements) {

        }
    }

    /**
     * <pre>
     * &lt;m id="{message-id}" f="{flags}" s="{size}" d="{date}" cid="{conv-id}" l="{folder} origid="{original-id}"&gt;
     *    &lt;content&gt;....&lt;/content&gt;*
     *    &lt;e .../&gt;*
     *    &lt;su&gt;{subject}&lt;/su&gt;
     *    &lt;fr&gt;{fragment}&lt;/fr&gt;
     *    
     *    &lt;mid&gt;{Message-ID header}&lt;/mid&gt;
     *    [&lt;inv&gt;...&lt;/inv&gt;]
     *    [&lt;mp&gt;...&lt;/mp&gt;]
     *    [&lt;content (url="{url}")&gt;...&lt;/content&gt;]
     *   &lt;/m&gt;
     * 
     *   {content} = complete rfc822 message. only present during certain operations that deal with the raw content
     *               of a messasage.  There is at most 1 content element.
     *   {conv-id}  = converstation id. only present if &lt;m&gt; is not enclosed within a &lt;c&gt; element
     *   {size}     = size in bytes
     *   {flags}    = (u)nread, (f)lagged, has (a)ttachment, (r)eplied, (s)ent by me, for(w)arded,
     *                (d)raft, deleted (x), (n)otification sent
     *   {date}     = secs since epoch, from date header in message
     *   {original-id} = message id of message being replied to/forwarded (outbound messages only)
     *   {url}      = content servlet relative url for retrieving message content
     *   {subject}  = subject of the message, only returned on an expanded message
     *   {fragment} = first n-bytes of the message (probably between 40-100)
     *   &lt;e .../&gt;*  = zero or more addresses in the message, indentified by
     *   type (t="f|t|c")
     *   &lt;inv ...&gt;...&lt;/inv&gt; = Parsed out iCal invite.  See soap-calendar.txt
     *   &lt;mp ...&gt;...&lt;/mp&gt; =  The root MIME part of the message.  There is exactly 1 MIME part under
     *                a message element.  The "body" will be tagged with body="1", and the content 
     *                of the body will also be present
     *   &lt;content&gt;  = the raw content of the message.  cannot have more than one of &lt;mp&gt;, &lt;content&gt; url, and &lt;content&gt; body.
     * </pre>
     * 
     * @param element
     * @return
     */
    private static MailMessage parseSingleMessage(final Element element) {
        if (!isMessage(element)) {
            return null;
        }
        
        

        return null;
    }

    /*-
     * <m id="{message-id}" f="{flags}" s="{size}" d="{date}" cid="{conv-id}" l="{folder} origid="{original-id}">
       <content>....</content>*
       <e .../>*
       <su>{subject}</su>
       <fr>{fragment}</fr>
       
       <mid>{Message-ID header}</mid>
       [<inv>...</inv>]
       [<mp>...</mp>]
       [<content (url="{url}")>...</content>]
      </m>
    
      {content} = complete rfc822 message. only present during certain operations that deal with the raw content
                  of a messasage.  There is at most 1 content element.
      {conv-id}  = converstation id. only present if <m> is not enclosed within a <c> element
      {size}     = size in bytes
      {flags}    = (u)nread, (f)lagged, has (a)ttachment, (r)eplied, (s)ent by me, for(w)arded,
                   (d)raft, deleted (x), (n)otification sent
      {date}     = secs since epoch, from date header in message
      {original-id} = message id of message being replied to/forwarded (outbound messages only)
      {url}      = content servlet relative url for retrieving message content
      {subject}  = subject of the message, only returned on an expanded message
      {fragment} = first n-bytes of the message (probably between 40-100)
      <e .../>*  = zero or more addresses in the message, indentified by
      type (t="f|t|c")
      <inv ...>...</inv> = Parsed out iCal invite.  See soap-calendar.txt
      <mp ...>...</mp> =  The root MIME part of the message.  There is exactly 1 MIME part under
                   a message element.  The "body" will be tagged with body="1", and the content 
                   of the body will also be present
      <content>  = the raw content of the message.  cannot have more than one of <mp>, <content> url, and <content> body.
     */

    public static boolean isMessage(final Element element) {
        if (null == element) {
            return false;
        }
        return "m".equals(element.getName());
    }

}
