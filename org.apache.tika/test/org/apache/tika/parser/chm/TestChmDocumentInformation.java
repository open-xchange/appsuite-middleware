/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tika.parser.chm;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import junit.framework.TestCase;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.junit.Assert;

public class TestChmDocumentInformation extends TestCase {

    //    private CHMDocumentInformation chmDoc = null;
    private Metadata meta;
    private BodyContentHandler handler;

    @Override
    public void setUp() throws Exception {
        ChmParser parser = new ChmParser();
        handler = new BodyContentHandler();
        meta = new Metadata();
        ParseContext context = new ParseContext();
        parser.parse(new ByteArrayInputStream(TestParameters.chmData), handler, meta, context);
    }

    public void testGetCHMDocInformation() throws TikaException, IOException {
        Assert.assertEquals(TestParameters.VP_CHM_MIME_TYPE, meta.toString().trim());
    }

    public void testGetText() throws TikaException {
        Assert.assertTrue(handler.toString().contains("The TCard method accepts only numeric arguments"));
    }

    @Override
    public void tearDown() throws Exception {
    }

}
