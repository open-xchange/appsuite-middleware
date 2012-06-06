package com.openexchange.mail.search;


public interface SearchTermVisitor {
    public void visit(ANDTerm term);
    public void visit(BccTerm term);
    public void visit(BodyTerm term);
    public void visit(BooleanTerm term);
    public void visit(CcTerm term);
    public void visit(FlagTerm term);
    public void visit(FromTerm term);
    public void visit(HeaderTerm term);
    public void visit(NOTTerm term);
    public void visit(ORTerm term);
    public void visit(ReceivedDateTerm term);
    public void visit(SentDateTerm term);
    public void visit(SizeTerm term);
    public void visit(SubjectTerm term);
    public void visit(ToTerm term);
    
}
