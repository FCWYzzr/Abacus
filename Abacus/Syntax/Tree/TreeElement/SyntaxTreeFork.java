package Abacus.Syntax.Tree.TreeElement;


import Abacus.Syntax.Token.Element;

import java.util.Vector;

public final class SyntaxTreeFork extends SyntaxTreeNode{
    Element base;
    Vector<SyntaxTreeNode> Children;

    public SyntaxTreeFork(Element base,NodeType t){
        super(false, t);
        this.base = base;
        Children = new Vector<SyntaxTreeNode>();
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

    public Element getBase(){
        return base;
    }

    @Override
    public String toString() {
        return "Fork{" + base + "}->\n" + Children+'\n';
    }
}
