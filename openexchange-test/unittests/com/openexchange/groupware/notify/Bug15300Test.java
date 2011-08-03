package com.openexchange.groupware.notify;

import java.util.List;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.notify.NotificationConfig.NotificationProperty;
import com.openexchange.groupware.tasks.Task;


public class Bug15300Test extends ParticipantNotifyTest {
    
    public void testSendMessage() throws Exception {
        Participant[] participants = getParticipants(U(2, 4, 6),G(),S(), R());
        Task t = getTask(participants);
        notify.taskCreated(t, session);        
        
        List<Message> msgs = notify.getMessages();
        
        String senderSource = NotificationConfig.getProperty(NotificationProperty.FROM_SOURCE, "primaryMail");
        if (senderSource.equals("defaultSenderAddress")) {
            for (Message msg : msgs) {
                assertTrue(msg.getFromAddr().equals("default@test"));
            }
        } else {
            for (Message msg : msgs) {
                assertTrue(msg.getFromAddr().equals("primary@test"));
            }
        }
        
    }

}
