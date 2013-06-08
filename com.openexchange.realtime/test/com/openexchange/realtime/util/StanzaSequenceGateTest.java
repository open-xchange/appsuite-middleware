
package com.openexchange.realtime.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Message;
import com.openexchange.realtime.packet.Stanza;

public class StanzaSequenceGateTest extends StanzaSequenceGate {

    public StanzaSequenceGateTest() {
        super("testgate");
    }

    private long lastInternallyHandledSeqNum = -1L;

    @Before
    public void before() {
        clearAll();
    }

    private void clearAll() {
        sequenceNumbers.clear();
        inboxes.clear();
        lastInternallyHandledSeqNum = -1L;
    }

    @Test
    public void testFirstStanzaGetsLost() throws Exception {
        Stanza stanza0 = createStanza(0L);
        Stanza stanza1 = createStanza(1L);
        Stanza stanza2 = createStanza(2L);

        handle(stanza1, stanza1.getTo());
        AtomicLong threshold = sequenceNumbers.get(stanza0.getSequencePrincipal());
        List<StanzaWithCustomAction> inbox = inboxes.get(stanza0.getSequencePrincipal());
        assertEquals("Wrong threshold", 0L, threshold.get());
        assertEquals("Should not have handled stanza", -1L, lastInternallyHandledSeqNum);
        assertNotNull("Inbox has not been created", inbox);
        assertEquals("Wrong number of stored stanzas", 1, inbox.size());

        handle(stanza0, stanza0.getTo());
        inbox = inboxes.get(stanza0.getSequencePrincipal());
        assertEquals("Wrong threshold", 2L, threshold.get());
        assertNull("Inbox has not been removed", inbox);

        handle(stanza2, stanza2.getTo());
        inbox = inboxes.get(stanza0.getSequencePrincipal());
        assertEquals("Wrong threshold", 3L, threshold.get());
        assertNull("Inbox has not been removed", inbox);
    }

    @Test
    public void testLostStanza() throws Exception {
        Stanza stanza0 = createStanza(0L);
        Stanza stanza1 = createStanza(1L);
        Stanza stanza2 = createStanza(2L);

        handle(stanza0, stanza0.getTo());
        AtomicLong threshold = sequenceNumbers.get(stanza0.getSequencePrincipal());
        List<StanzaWithCustomAction> inbox = inboxes.get(stanza0.getSequencePrincipal());
        assertEquals("Wrong threshold", 1L, threshold.get());
        assertNull("Inbox has been created unnecessarily", inbox);

        handle(stanza2, stanza2.getTo());
        inbox = inboxes.get(stanza0.getSequencePrincipal());
        assertEquals("Wrong threshold", 1L, threshold.get());
        assertNotNull("Inbox has not been created", inbox);
        assertEquals("Wrong inbox size", 1, inbox.size());

        handle(stanza1, stanza1.getTo());
        inbox = inboxes.get(stanza0.getSequencePrincipal());
        assertEquals("Wrong threshold", 3L, threshold.get());
        assertNull("Inbox has not been removed", inbox);
    }

    @Test
    public void testGapGreaterOne() throws Exception {
        Stanza stanza0 = createStanza(0L);
        Stanza stanza1 = createStanza(1L);
        Stanza stanza2 = createStanza(2L);
        Stanza stanza3 = createStanza(3L);

        handle(stanza0, stanza0.getTo());
        handle(stanza3, stanza3.getTo());
        AtomicLong threshold = sequenceNumbers.get(stanza0.getSequencePrincipal());
        List<StanzaWithCustomAction> inbox = inboxes.get(stanza0.getSequencePrincipal());
        assertEquals("Wrong threshold", 1L, threshold.get());
        assertNotNull("Inbox has not been created", inbox);
        assertEquals("Wrong inbox size", 1, inbox.size());

        handle(stanza1, stanza1.getTo());
        inbox = inboxes.get(stanza0.getSequencePrincipal());
        assertEquals("Wrong threshold", 2L, threshold.get());
        assertNotNull("Inbox has not been created", inbox);
        assertEquals("Wrong inbox size", 1, inbox.size());

        handle(stanza2, stanza2.getTo());
        inbox = inboxes.get(stanza0.getSequencePrincipal());
        assertEquals("Wrong threshold", 4L, threshold.get());
        assertNull("Inbox has not been removed", inbox);

        clearAll();

        handle(stanza0, stanza0.getTo());
        handle(stanza3, stanza3.getTo());
        threshold = sequenceNumbers.get(stanza0.getSequencePrincipal());
        inbox = inboxes.get(stanza0.getSequencePrincipal());
        assertEquals("Wrong threshold", 1L, threshold.get());
        assertNotNull("Inbox has not been created", inbox);
        assertEquals("Wrong inbox size", 1, inbox.size());

        handle(stanza2, stanza2.getTo());
        inbox = inboxes.get(stanza0.getSequencePrincipal());
        assertEquals("Wrong threshold", 1L, threshold.get());
        assertEquals("Wrong inbox size", 2, inbox.size());
        assertEquals("Handled stanzas in wrong order", 0L, lastInternallyHandledSeqNum);

        handle(stanza1, stanza1.getTo());
        inbox = inboxes.get(stanza0.getSequencePrincipal());
        assertEquals("Wrong threshold", 4L, threshold.get());
        assertNull("Inbox has not been removed", inbox);
        assertEquals("Handled stanzas in wrong order", 3L, lastInternallyHandledSeqNum);
    }

    @Test
    public void testMultipleGaps() throws Exception {
        Stanza stanza0 = createStanza(0L);
        Stanza stanza1 = createStanza(1L);
        Stanza stanza2 = createStanza(2L);
        Stanza stanza3 = createStanza(3L);
        Stanza stanza4 = createStanza(4L);
        Stanza stanza5 = createStanza(5L);

        handle(stanza0, stanza0.getTo());
        handle(stanza2, stanza2.getTo());
        handle(stanza4, stanza4.getTo());
        handle(stanza5, stanza5.getTo());
        AtomicLong threshold = sequenceNumbers.get(stanza0.getSequencePrincipal());
        List<StanzaWithCustomAction> inbox = inboxes.get(stanza0.getSequencePrincipal());
        assertEquals("Wrong threshold", 1L, threshold.get());
        assertNotNull("Inbox has not been created", inbox);
        assertEquals("Wrong inbox size", 3, inbox.size());

        handle(stanza1, stanza1.getTo());
        inbox = inboxes.get(stanza0.getSequencePrincipal());
        assertEquals("Wrong threshold", 3L, threshold.get());
        assertNotNull("Inbox has been deleted", inbox);
        assertEquals("Wrong inbox size", 2, inbox.size());

        handle(stanza3, stanza3.getTo());
        inbox = inboxes.get(stanza0.getSequencePrincipal());
        assertEquals("Wrong threshold", 6L, threshold.get());
        assertNull("Inbox has not been removed", inbox);
        assertEquals("Handled stanzas in wrong order", 5L, lastInternallyHandledSeqNum);
    }

    @Test
    public void testBufferSize() throws Exception {
        Stanza stanza = createStanza(0L);
        for (int i = 1; i <= BUFFER_SIZE; i++) {
            Stanza tmp = createStanza(i);
            assertTrue("ACK would not be sent.", handle(tmp, tmp.getTo()));
        }

        AtomicLong threshold = sequenceNumbers.get(stanza.getSequencePrincipal());
        List<StanzaWithCustomAction> inbox = inboxes.get(stanza.getSequencePrincipal());
        assertEquals("Wrong threshold", 0L, threshold.get());
        assertNotNull("Inbox has not been created", inbox);
        assertEquals("Wrong inbox size", BUFFER_SIZE, inbox.size());

        Stanza last = createStanza(BUFFER_SIZE + 1);
        assertFalse("Stanza was stored although buffer was full.", handle(last, last.getTo()));
        inbox = inboxes.get(stanza.getSequencePrincipal());
        assertEquals("Wrong threshold", 0L, threshold.get());
        assertNotNull("Inbox has not been created", inbox);
        assertEquals("Wrong inbox size", BUFFER_SIZE, inbox.size());

        assertTrue("ACK would not be sent.", handle(stanza, stanza.getTo()));
        inbox = inboxes.get(stanza.getSequencePrincipal());
        assertEquals("Wrong threshold", BUFFER_SIZE + 1, threshold.get());
        assertNull("Inbox has not been removed", inbox);
        assertEquals("Handled stanzas in wrong order", BUFFER_SIZE, lastInternallyHandledSeqNum);
    }

    @Test
    public void testArbitraryStanzaLoss() throws Exception {
        int messages = BUFFER_SIZE;
        int div;
        Random random = new Random(System.currentTimeMillis());
        do {
            div = random.nextInt(messages / 2);
        } while (div == 0);

        int modul = messages / div;
        List<Stanza> stanzas = new ArrayList<Stanza>(messages);
        for (int i = 0; i < messages; i++) {
            stanzas.add(i, createStanza(i));
        }

        List<Stanza> lost = new ArrayList<Stanza>();
        for (Stanza stanza : stanzas) {
            if (stanza.getSequenceNumber() == 0) {
                handle(stanza, stanza.getTo());
            } else if (stanza.getSequenceNumber() % modul == 0) {
                lost.add(stanza);
            } else {
                handle(stanza, stanza.getTo());
            }
        }

        Iterator<Stanza> it = lost.iterator();
        while (it.hasNext()) {
            Stanza stanza = it.next();
            AtomicLong threshold = sequenceNumbers.get(stanza.getSequencePrincipal());
            List<StanzaWithCustomAction> inbox = inboxes.get(stanza.getSequencePrincipal());
            assertEquals("Wrong threshold", stanza.getSequenceNumber(), threshold.get());
            assertEquals("Wrong inbox size", messages - stanza.getSequenceNumber() - lost.size(), inbox.size());
            handle(stanza, stanza.getTo());
            it.remove();
        }

        AtomicLong threshold = sequenceNumbers.get(stanzas.get(0).getSequencePrincipal());
        List<StanzaWithCustomAction> inbox = inboxes.get(stanzas.get(0).getSequencePrincipal());
        assertEquals("Wrong threshold", messages, threshold.get());
        assertNull("Inbox has not been removed", inbox);
    }

    private Stanza createStanza(long seqNum) {
        Stanza stanza = new Message();
        stanza.setFrom(new ID("ox", "testuser", "testcontext", "resource"));
        stanza.setTo(new ID("synthetic", "testcomponent", "", "", "resource"));
        stanza.setSequenceNumber(seqNum);
        stanza.setTracer("#Stanza " + seqNum);
        return stanza;
    }

    @Override
    public void handleInternal(Stanza stanza, ID recipient) throws OXException {
        lastInternallyHandledSeqNum = stanza.getSequenceNumber();
    }

}
