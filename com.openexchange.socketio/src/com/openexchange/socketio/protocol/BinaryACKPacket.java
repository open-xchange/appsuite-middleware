/**
 * The MIT License
 * Copyright (c) 2015 Alexander Sova (bird@codeminders.com)
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.openexchange.socketio.protocol;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Alexander Sova (bird@codeminders.com)
 */
public class BinaryACKPacket extends ACKPacket implements BinaryPacket {

    private final List<InputStream> attachments;
    private int number_attachments_expected;

    public BinaryACKPacket(int id, String ns, Object[] args) {
        super(Type.BINARY_ACK, id, ns, null);

        attachments = new LinkedList<>();

        setArgs((Object[]) SocketIOProtocol.extractBinaryObjects(args, attachments));
    }

    public BinaryACKPacket(int id, String ns, Object[] args, int number_attachments_expected) {
        super(Type.BINARY_ACK, id, ns, args);

        this.number_attachments_expected = number_attachments_expected;
        this.attachments = new ArrayList<>(number_attachments_expected);
    }

    @Override
    protected String encodeAttachments() {
        return SocketIOProtocol.encodeAttachments(attachments.size());
    }

    @Override
    public Collection<InputStream> getAttachments() {
        return attachments;
    }

    /**
     * @return true when all expected attachment arrived, false otherwise
     */
    @Override
    public boolean isComplete() {
        return number_attachments_expected == 0;
    }

    /**
     * This method to be called when new attachement arrives to the socket
     *
     * @param attachment new attachment
     */
    @Override
    public void addAttachment(InputStream attachment) {
        attachments.add(attachment);
        number_attachments_expected -= 1;
    }
}
