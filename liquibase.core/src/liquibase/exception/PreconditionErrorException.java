package liquibase.exception;

import java.util.ArrayList;
import java.util.List;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.precondition.Precondition;
import liquibase.precondition.core.ErrorPrecondition;

public class PreconditionErrorException extends Exception {

    private static final long serialVersionUID = 1L;
    private List<ErrorPrecondition> erroredPreconditions;

    public PreconditionErrorException(String message, List<ErrorPrecondition> erroredPreconditions) {
        super(message);
        this.erroredPreconditions = erroredPreconditions;
    }

    public PreconditionErrorException(Exception cause, DatabaseChangeLog changeLog, Precondition precondition) {
        this(new ErrorPrecondition(cause, changeLog, precondition));
    }

    public PreconditionErrorException(ErrorPrecondition errorPrecondition) {
        super("Precondition Error", errorPrecondition.getCause());
        this.erroredPreconditions = new ArrayList<ErrorPrecondition>();
        erroredPreconditions.add(errorPrecondition);
    }

    public PreconditionErrorException(List<ErrorPrecondition> errorPreconditions) {
        super("Precondition Error");
        this.erroredPreconditions = errorPreconditions;
    }

    public List<ErrorPrecondition> getErrorPreconditions() {
        return erroredPreconditions;
    }
}
