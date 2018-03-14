package liquibase.precondition.core;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.precondition.Precondition;

public class ErrorPrecondition {
    private final Throwable cause;
    private final Precondition precondition;
    private final DatabaseChangeLog changeLog;


    public ErrorPrecondition(Throwable exception, DatabaseChangeLog changeLog, Precondition precondition) {
        this.cause = exception;
        this.changeLog = changeLog;
        this.precondition = precondition;
    }


    public Throwable getCause() {
        return cause;
    }

    public Precondition getPrecondition() {
        return precondition;
    }


    @Override
    public String toString() {
        Throwable cause = this.cause;
        for (Throwable t; (t = cause.getCause()) != null;) {
            cause = t;
        }

        String causeMessage = cause.getMessage();
        if (causeMessage == null) {
            causeMessage = this.cause.getMessage();
        }
        if (changeLog == null) {
            return causeMessage;
        } else {
            return changeLog.toString()+" : "+ precondition.toString()+" : "+causeMessage;
        }
    }
}