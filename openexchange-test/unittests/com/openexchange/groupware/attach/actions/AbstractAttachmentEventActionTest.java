
package com.openexchange.groupware.attach.actions;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentEvent;
import com.openexchange.groupware.attach.AttachmentListener;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.contexts.Context;

public abstract class AbstractAttachmentEventActionTest extends AbstractAttachmentActionTest {

    protected static final class MockAttachmentListener implements AttachmentListener {

        List<AttachmentMetadata> attached = new ArrayList<AttachmentMetadata>();

        Set<Integer> detached = new HashSet<Integer>();

        @Override
        public long attached(final AttachmentEvent e) throws Exception {
            attached.add(e.getAttachment());
            e.getWriteConnection();
            return System.currentTimeMillis();
        }

        @Override
        public long detached(final AttachmentEvent e) throws Exception {
            for (final int id : e.getDetached()) {
                detached.add(id);
            }
            e.getWriteConnection();
            return System.currentTimeMillis();
        }

        public List<AttachmentMetadata> getAttached() {
            return attached;
        }

        public Set<Integer> getDetached() {
            return detached;
        }

        public void clear() {
            attached.clear();
            detached.clear();
        }
    }

    public static final class MockDBProvider implements DBProvider {

        private final Set<Connection> read = new HashSet<Connection>();

        private final Set<Connection> write = new HashSet<Connection>();

        private boolean ok = true;

        private boolean called = false;

        private final StringBuffer log = new StringBuffer();

        private final DBProvider delegate;

        public MockDBProvider(DBProvider delegate) {
            this.delegate = delegate;
        }

        @Override
        public Connection getReadConnection(final Context ctx) throws OXException {
            Connection con = delegate.getReadConnection(ctx);
            read.add(con);
            log.append("Get ReadConnection: " + con.hashCode() + "\n");
            called = true;
            return con;
        }

        @Override
        public Connection getWriteConnection(final Context ctx) throws OXException {
            Connection con = delegate.getWriteConnection(ctx);
            write.add(con);
            log.append("Get WriteConnection: " + con.hashCode() + "\n");
            called = true;
            return con;
        }

        @Override
        public void releaseReadConnection(final Context ctx, final Connection con) {
            ok(read.remove(con));
            log.append("Release ReadConnection: " + con.hashCode() + "\n");
            delegate.releaseReadConnection(ctx, con);

        }

        @Override
        public void releaseWriteConnection(final Context ctx, final Connection con) {
            ok(write.remove(con));
            log.append("Release WriteConnection: " + con + "\n");
            delegate.releaseWriteConnection(ctx, con);
        }

        @Override
        public void releaseWriteConnectionAfterReading(Context ctx, Connection con) {
            ok(write.remove(con));
            log.append("Release WriteConnectionAfterReading: " + con + "\n");
            delegate.releaseWriteConnectionAfterReading(ctx, con);
        }

        private void ok(final boolean b) {
            this.ok = ok && b;
        }

        public boolean allOK() {
            return ok && read.isEmpty() && write.isEmpty();
        }

        public boolean called() {
            return called;
        }

        public String getStatus() {
            return String.format(
                "OK : %s ReadIds: %s WriteIds: %s Called: %s \n LOG: %s",
                String.valueOf(ok),
                read.toString(),
                write.toString(),
                String.valueOf(called),
                log.toString());
        }

    }

}
