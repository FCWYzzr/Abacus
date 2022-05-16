package Abacus.Syntax.Tree.TreeElement;

public abstract class SyntaxTreeNode {
    protected boolean isLeave;
    protected NodeType type;

    SyntaxTreeNode(boolean isLeave, NodeType NT){
        this.isLeave = isLeave;
        this.type = NT;
    }

    public boolean isFork() {
        return !isLeave;
    }

    public NodeType getType(){
        return type;
    }
}
