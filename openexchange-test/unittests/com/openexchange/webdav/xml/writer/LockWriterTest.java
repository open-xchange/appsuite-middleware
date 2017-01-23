
package com.openexchange.webdav.xml.writer;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.test.XMLCompare;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.xml.WebdavLockWriter;

public class LockWriterTest {

    private final XMLCompare xmlCompare = new XMLCompare();

    @Test
    public void testWriteLock() throws Exception {
        xmlCompare.setCheckTextNames("depth", "owner", "timeout", "href");

        final WebdavLockWriter writer = new WebdavLockWriter();

        final WebdavLock lock = new TestLock();

        lock.setType(WebdavLock.Type.WRITE_LITERAL);
        lock.setScope(WebdavLock.Scope.EXCLUSIVE_LITERAL);
        lock.setDepth(0);
        lock.setOwner("me");
        lock.setTimeout(WebdavLock.NEVER);
        lock.setToken("opaquelocktoken:blaaaa");

        String expect = "<blupp xmlns:D=\"DAV:\"> <D:activelock> <D:locktype><D:write /></D:locktype> <D:lockscope><D:exclusive/></D:lockscope> <D:depth>0</D:depth> <D:owner>me</D:owner> <D:timeout>Infinite</D:timeout> <D:locktoken><D:href>opaquelocktoken:blaaaa</D:href></D:locktoken> </D:activelock></blupp>";
        String got = "<blupp xmlns:D=\"DAV:\">" + writer.lock2xml(lock) + "</blupp>";

        assertTrue(xmlCompare.compare(expect, got));

        lock.setScope(WebdavLock.Scope.SHARED_LITERAL);

        expect = "<blupp xmlns:D=\"DAV:\"><D:activelock> <D:locktype><D:write /></D:locktype> <D:lockscope><D:shared/></D:lockscope> <D:depth>0</D:depth> <D:owner>me</D:owner> <D:timeout>Infinite</D:timeout> <D:locktoken><D:href>opaquelocktoken:blaaaa</D:href></D:locktoken> </D:activelock></blupp>";
        got = "<blupp xmlns:D=\"DAV:\">" + writer.lock2xml(lock) + "</blupp>";

        assertTrue(xmlCompare.compare(expect, got));

        lock.setDepth(1);

        expect = "<blupp xmlns:D=\"DAV:\"><D:activelock> <D:locktype><D:write /></D:locktype> <D:lockscope><D:shared/></D:lockscope> <D:depth>1</D:depth> <D:owner>me</D:owner> <D:timeout>Infinite</D:timeout> <D:locktoken><D:href>opaquelocktoken:blaaaa</D:href></D:locktoken> </D:activelock></blupp>";
        got = "<blupp xmlns:D=\"DAV:\">" + writer.lock2xml(lock) + "</blupp>";

        assertTrue(xmlCompare.compare(expect, got));

        lock.setDepth(WebdavCollection.INFINITY);

        expect = "<blupp xmlns:D=\"DAV:\"><D:activelock> <D:locktype><D:write /></D:locktype> <D:lockscope><D:shared/></D:lockscope> <D:depth>infinity</D:depth> <D:owner>me</D:owner> <D:timeout>Infinite</D:timeout> <D:locktoken><D:href>opaquelocktoken:blaaaa</D:href></D:locktoken> </D:activelock></blupp>";
        got = "<blupp xmlns:D=\"DAV:\">" + writer.lock2xml(lock) + "</blupp>";

        assertTrue(xmlCompare.compare(expect, got));

        // Bug 12575 Timeout in lock object is in milliseconds!
        lock.setTimeout(23000);
        expect = "<blupp xmlns:D=\"DAV:\"><D:activelock> <D:locktype><D:write /></D:locktype> <D:lockscope><D:shared/></D:lockscope> <D:depth>infinity</D:depth> <D:owner>me</D:owner> <D:timeout>Second-23</D:timeout> <D:locktoken><D:href>opaquelocktoken:blaaaa</D:href></D:locktoken> </D:activelock></blupp>";
        got = "<blupp xmlns:D=\"DAV:\">" + writer.lock2xml(lock) + "</blupp>";
        assertTrue(xmlCompare.compare(expect, got));

    }

    private static final class TestLock extends WebdavLock {

        private long timeout;

        @Override
        public long getTimeout() {
            return timeout;
        }

        @Override
        public void setTimeout(final long timeout) {
            this.timeout = timeout;
        }

    }

}
