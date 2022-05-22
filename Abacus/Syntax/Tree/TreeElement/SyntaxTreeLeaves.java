package Abacus.Syntax.Tree.TreeElement;

import Abacus.Syntax.Token.Element;

public final class SyntaxTreeLeaves extends SyntaxTreeNode{
    @Override
    public String toString() {
        return getBase().toString()+"<"+type+">";
    }

    public SyntaxTreeLeaves(Element e, NodeType NT){
        super(e, NT);
    }
}
