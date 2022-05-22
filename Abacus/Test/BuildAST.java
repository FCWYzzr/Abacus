package Abacus.Test;

import Abacus.Syntax.Token.TokenArray;
import Abacus.Syntax.Tree.ASTBuilder;
import Abacus.Syntax.Tree.TreeElement.SyntaxTreeFork;

public class BuildAST {
    public static void main(String[] args) {
        TokenArray ta = new TokenArray("vector.aba");
        System.out.println(ta.getArray());
        try {
            SyntaxTreeFork Root = ASTBuilder.BuildFromTokens(ta.getArray());
            System.out.println(Root);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
