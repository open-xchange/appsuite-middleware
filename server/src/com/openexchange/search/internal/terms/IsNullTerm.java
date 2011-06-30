package com.openexchange.search.internal.terms;

import com.openexchange.search.SingleSearchTerm;

public class IsNullTerm extends SingleSearchTerm {

	public IsNullTerm() {
        super(SingleSearchTerm.SingleOperation.ISNULL);
    }
}
