package com.openexchange.utils.propertyhandling;

/**
 * A class which defines the different types of requirements
 * 
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class Required {

    public static Required TRUE = new Required(Value.TRUE);

    public static Required FALSE = new Required(Value.FALSE);

    public enum Value {
        TRUE,
        FALSE,
        /**
         * If this requirement is used, a condition must be set too
         */
        CONDITION;
    }

    private final Required.Value value;

    private Condition[] condition;

    public Required(final Required.Value value, final Condition[] condition) {
        super();
        this.value = value;
        this.condition = condition;
    }

    public Required(final Required.Value value) {
        super();
        this.value = value;
    }

    public Required.Value getValue() {
        return value;
    }

    public Condition[] getCondition() {
        return condition;
    }

}