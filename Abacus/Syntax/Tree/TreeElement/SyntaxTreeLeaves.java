package Abacus.Syntax.Tree.TreeElement;

import Abacus.Syntax.Token.Element;
import Abacus.Syntax.Token.ElementType;

public final class SyntaxTreeLeaves extends SyntaxTreeNode{
    Element element;

    @Override
    public String toString() {
        return element.toString()+"<"+type+">";
    }

    public SyntaxTreeLeaves(Element e, NodeType NT){
        super(true, NT);
        element = e;
    }

    public ElementType getElementType() {
        return element.getType();
    }

    public String getElementName(){
        return element.getName();
    }

}
