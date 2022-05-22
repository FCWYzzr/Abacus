package Abacus.Syntax.Tree.TreeElement;

import Abacus.Syntax.Token.Element;
import Abacus.Syntax.Token.ElementType;

public abstract class SyntaxTreeNode {
    protected Element Base;
    protected NodeType type;

    SyntaxTreeNode(Element base, NodeType NT){
        this.type = NT;
        this.Base = base;
    }

    protected Element getBase(){
        return Base;
    }
    public String getBaseName() {
        return Base.getName();
    }

    public ElementType getBaseType() {
        return Base.getType();
    }

    public SyntaxTreeFork Fork() {
        return (SyntaxTreeFork) this;
    }

    public SyntaxTreeLeaves Leaf() {
        return (SyntaxTreeLeaves) this;
    }

    public NodeType getType(){
        return type;
    }
}
