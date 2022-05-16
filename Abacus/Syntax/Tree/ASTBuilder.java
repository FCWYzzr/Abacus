package Abacus.Syntax.Tree;

import Abacus.Syntax.Token.Element;
import Abacus.Syntax.Tree.TreeBuilt.TreeBuilder;
import Abacus.Syntax.Tree.TreeElement.NodeType;
import Abacus.Syntax.Tree.TreeElement.SyntaxTreeFork;
import org.omg.CORBA.DynAnyPackage.Invalid;

import java.util.Vector;

public class ASTBuilder {
    public static SyntaxTreeFork BuildFromTokens(Vector<Element> Tokens) throws Exception{
        return TreeBuilder.Build(Tokens, 0, Tokens.size(), NodeType.Program_Root);
    }
}
