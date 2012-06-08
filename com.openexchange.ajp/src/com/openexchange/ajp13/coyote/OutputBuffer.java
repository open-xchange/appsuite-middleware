/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.openexchange.ajp13.coyote;

import java.io.IOException;
import com.openexchange.ajp13.coyote.util.ByteChunk;

/**
 * Output buffer. This class is used internally by the protocol implementation. All writes from higher level code should happen via
 * Resonse.doWrite().
 *
 * @author Remy Maucherat
 */
public interface OutputBuffer {

    /**
     * Write the response. The caller (servlet container) owns the chunks.
     *
     * @param The chunk data to write
     * @return The number of written bytes
     * @throws IOException If an I/O error occurs
     */
    public int doWrite(ByteChunk chunk) throws IOException;

    /**
     * Gets the packet size.
     *
     * @return The packet size
     */
    public int getPacketSize();

}
