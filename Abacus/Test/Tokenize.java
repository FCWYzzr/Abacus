package Abacus.Test;
import Abacus.Syntax.Token.TokenArray;

public class Tokenize {
    public static void main(String[] args) {
        TokenArray ta = new TokenArray("vector.aba");

        System.out.println(ta.getArray().toString().replace(", ","\n"));
    }
}
