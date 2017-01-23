
package com.openexchange.groupware.results;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

public class AbstractTimedResultTest {

    private TestTimedResult results;

    @Test
    public void testExtractsLargestTimestampAfterIteration() throws Exception {
        consume(results, 1, 2, 6, 4, 3);
        assertEquals(6, results.sequenceNumber());
    }

    @Test
    public void testExtractsTimestampBeforeIteration() throws Exception {
        assertEquals(6, results.sequenceNumber());
        consume(results, 1, 2, 6, 4, 3);
    }

    @Test
    public void testExtractsTimestampInIteration() throws Exception {
        consume(results, 1, 2, 6);
        assertEquals(6, results.sequenceNumber());
        consume(results, 4, 3);
    }

    private void consume(TestTimedResult ttr, long... values) throws Exception {
        SearchIterator<Thing> iter = ttr.results();
        for (int i = 0; i < values.length; i++) {
            long timestamp = values[i];
            assertTrue(iter.hasNext());
            assertEquals(iter.next().getSequenceNumber(), timestamp);
        }
    }

    @Before
    public void setUp() {
        SearchIterator<Thing> iterator = new SearchIteratorAdapter<Thing>(new ArrayList<Thing>() {

            {
                add(new Thing(1));
                add(new Thing(2));
                add(new Thing(6));
                add(new Thing(4));
                add(new Thing(3));
            }
        }.iterator());
        results = new TestTimedResult(iterator);
    }

    private static final class Thing {

        private final long sequenceNumber;

        public Thing(long sequenceNumber) {
            this.sequenceNumber = sequenceNumber;
        }

        private long getSequenceNumber() {
            return sequenceNumber;
        }
    }

    private static class TestTimedResult extends AbstractTimedResult<Thing> {

        public TestTimedResult(SearchIterator<Thing> results) {
            super(results);
        }

        @Override
        protected long extractTimestamp(Thing object) {
            return object.getSequenceNumber();
        }

    }

}
