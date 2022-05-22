package Abacus.Syntax.Tree.TreeElement;


import Abacus.Syntax.Token.Element;

import java.util.Vector;

public final class SyntaxTreeFork extends SyntaxTreeNode{
    Vector<SyntaxTreeNode> Children;

    public SyntaxTreeFork(Element base,NodeType t){
        super(base, t);
        Children = new Vector<>();
    }

    public SyntaxTreeNode at(int i) throws ArrayIndexOutOfBoundsException{
        return Children.get(i);
    }

    public void push(SyntaxTreeNode node){
        Children.add(node);
    }

    public int getChildrenCount(){
        return Children.size();
    }

    public Vector<SyntaxTreeNode> kids(){
        return Children;
    }

    @Override
    public String toString() {
        return "Fork{" + getBase() + "}("+type+")->\n" + Children+'\n';
    }
}
