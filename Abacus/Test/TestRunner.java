package Abacus.Test;

import Abacus.Syntax.Token.TokenArray;
import Abacus.Syntax.Tree.ASTBuilder;
import Abacus.Syntax.Tree.ASTInterpreter;
import Abacus.Syntax.Tree.TreeElement.SyntaxTreeFork;

public class TestRunner {
    public static void main(String[] args) {
        try {
            TokenArray ta = new TokenArray("vector.aba");
            //System.out.println(ta.getArray());
            SyntaxTreeFork Root = ASTBuilder.BuildFromTokens(ta.getArray());
            //System.out.println(Root);
            ASTInterpreter.Run(Root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
