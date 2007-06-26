package com.openexchange.admin.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class TaskMgmtTest extends AbstractTest {

    @Test
    public void testGetTaskResultsContextCredentialsInt() throws MalformedURLException, RemoteException, NotBoundException, Exception {
        final OXTaskMgmtInterface oxtask = getTaskClient();
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(112, 50);

        try {
            oxtask.getTaskResults(ctx, cred, 1);
        } catch (final InvalidCredentialsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final StorageException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final InvalidDataException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testGetJobList() throws MalformedURLException, RemoteException, NotBoundException, InvalidDataException, InvalidCredentialsException, StorageException, Exception {
        final OXTaskMgmtInterface oxtask = getTaskClient();
        final Credentials cred = DummyCredentials();
        final Context ctx = getTestContextObject(112, 50);
        
        System.out.println(oxtask.getJobList(ctx, cred));
    }

    private OXTaskMgmtInterface getTaskClient() throws MalformedURLException, RemoteException, NotBoundException {
        return (OXTaskMgmtInterface) Naming.lookup(getRMIHostUrl()+ OXTaskMgmtInterface.RMI_NAME);
    }

}
