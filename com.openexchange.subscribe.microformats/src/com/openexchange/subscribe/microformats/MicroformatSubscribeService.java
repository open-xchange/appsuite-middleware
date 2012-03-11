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

package com.openexchange.subscribe.microformats;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.subscribe.AbstractSubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.microformats.datasources.OXMFDataSource;
import com.openexchange.subscribe.microformats.parser.ObjectParser;
import com.openexchange.subscribe.microformats.transformers.MapToObjectTransformer;

/**
 * {@link MicroformatSubscribeService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MicroformatSubscribeService extends AbstractSubscribeService {

    private MapToObjectTransformer transformer;

    private OXMFParserFactoryService parserFactory;

    private OXMFDataSource mfSource;

    private final List<String> containers = new LinkedList<String>();

    private final List<String> prefixes = new LinkedList<String>();

    private SubscriptionSource source;

    private final List<ObjectParser> objectParsers = new LinkedList<ObjectParser>();

    @Override
    public Collection getContent(Subscription subscription) throws OXException {
        Reader htmlData = mfSource.getData(subscription);
        return getContent(htmlData);
    }

    public Collection getContent(Reader htmlData) throws OXException {
        String data = null;

        if (!objectParsers.isEmpty()) {
            data = read(htmlData);
            htmlData = new StringReader(data);
        }

        OXMFParser parser = parserFactory.getParser();
        configureParser(parser);
        List<Map<String, String>> parsed = parser.parse(htmlData);
        List results = new ArrayList(transformer.transform(parsed));

        for (ObjectParser objectParser : objectParsers) {
            Collection parsedObjects = objectParser.parse(new StringReader(data));
            results.addAll(parsedObjects);
        }

        return results;
    }

    public void addObjectParser(ObjectParser parser) {
        objectParsers.add(parser);
    }

    private String read(Reader htmlData) throws OXException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(htmlData);
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
            return builder.toString();

        } catch (IOException e) {
            throw OXMFSubscriptionErrorMessage.IOException.create(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    public SubscriptionSource getSubscriptionSource() {
        return source;
    }

    public void setSource(SubscriptionSource source) {
        this.source = source;
    }

    @Override
    public boolean handles(int folderModule) {
        return source.getFolderModule() == folderModule;
    }

    /**
     * @param mfSource
     */
    public void setOXMFSource(OXMFDataSource mfSource) {
        this.mfSource = mfSource;
    }

    /**
     * @param parserFactory
     */
    public void setOXMFParserFactory(OXMFParserFactoryService parserFactory) {
        this.parserFactory = parserFactory;
    }

    /**
     * @param transformer
     */
    public void setTransformer(MapToObjectTransformer transformer) {
        this.transformer = transformer;
    }

    /**
     * @param parser
     */
    public void configureParser(OXMFParser parser) {
        for (String container : containers) {
            parser.addContainerElement(container);
        }

        for (String prefix : prefixes) {
            parser.addAttributePrefix(prefix);
        }
    }

    /**
     * @param string
     */
    public void addContainerElement(String string) {
        containers.add(string);
    }

    /**
     * @param string
     */
    public void addPrefix(String string) {
        prefixes.add(string);
    }

    @Override
    public void modifyOutgoing(Subscription subscription) throws OXException {
        subscription.setDisplayName(getDisplayName(subscription));
    }

    protected String getDisplayName(Subscription subscription) {
        return (String) subscription.getConfiguration().get("url");
    }

}
