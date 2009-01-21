package com.openexchange.groupware.tasks.mapping;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import com.openexchange.groupware.tasks.Mapper;
import com.openexchange.groupware.tasks.Task;


public class Alarm implements Mapper<Date>{

    public boolean equals(Task task1, Task task2) {
        Date d1 = task1.getAlarm();
        Date d2 = task2.getAlarm();
        if( d1 == d2) {
            return true;
        }
        if(d1 == null && d2 != null) {
            return false;
        }
        return d1.equals(d2);
    }

    public void fromDB(ResultSet result, int pos, Task task) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Date get(Task task) {
        return task.getAlarm();
    }

    public String getDBColumnName() {
        throw new UnsupportedOperationException();
    }

    public int getId() {
        return Task.ALARM;
    }

    public boolean isSet(Task task) {
        return task.containsAlarm();
    }

    public void set(Task task, Date value) {
        task.setAlarm(value);
    }

    public void toDB(PreparedStatement stmt, int pos, Task task) throws SQLException {
        throw new UnsupportedOperationException();
    }

}
