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

/**
 * Copyright 2012 Jos� Mart�nez
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.openexchange.realtime.xmpp.internal;

import static org.joox.JOOX.$;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.dom.DOMResult;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.w3c.dom.Document;
import com.fasterxml.aalto.stax.OutputFactoryImpl;

/**
 * Processes XML Events into XML Elements.
 */
public class XMLElementDecoder extends SimpleChannelUpstreamHandler {

    private static final XMLOutputFactory xmlOutputFactory = new OutputFactoryImpl();

    private static final DocumentBuilderFactory documentBuilderFactory;

    static {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setIgnoringElementContentWhitespace(true);
        documentBuilderFactory.setIgnoringComments(true);
    }

    private final int skip;

    private XMLEventWriter writer;

    private DOMResult result;

    private Document document;

    private int depth;

    public XMLElementDecoder() {
        this(1);
    }

    public XMLElementDecoder(int skip) {
        this.skip = skip;
        resetWriter();
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (!(e.getMessage() instanceof XMLEvent)) {
            ctx.sendUpstream(e);
            return;
        }

        final XMLEvent event = (XMLEvent) e.getMessage();

        if (event.isStartDocument() || event.isEndDocument())
            return;

        if (depth < skip && event.isStartElement()) {
            ctx.sendUpstream(e);
            depth++;
            return;
        }

        if (depth <= skip && event.isEndElement()) {
            ctx.sendUpstream(e);
            depth--;
            return;
        }

        writer.add(event);

        if (event.isStartElement()) {
            depth++;
        } else if (event.isEndElement()) {
            depth--;

            if (depth == skip) {
                writer.flush();
                Channels.fireMessageReceived(ctx, $(document.getDocumentElement()));
                writer.close();
                resetWriter();
            }
        }
    }

    private void resetWriter() {
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            document = documentBuilder.newDocument();
            result = new DOMResult(document);
            writer = xmlOutputFactory.createXMLEventWriter(result);
        } catch (XMLStreamException e) {
            // TODO: Handle
        } catch (ParserConfigurationException e) {
            // TODO: Handle
        }

    }

}
