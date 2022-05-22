package Abacus.Syntax.Token;

import jdk.nashorn.internal.runtime.regexp.joni.exception.SyntaxException;

public final class Rule {
    static char type;

    private static boolean isDigit(int c){
        return '0' <= c && c <= '9';
    }
    private static boolean isAlpha(int c){
        return ('a' <= c && c <= 'z')||('A' <= c && c <= 'Z');
    }
    private static boolean is_(int c){
        return c == '_';
    }
    private static boolean isOperator(int c){
        return c == '+' ||
               c == '-' ||
               c == '*' ||
               c == '·' ||
               c == '/' ||
               c == '=' ||
               c == '%' ||
               c == '&' ||
               c == '|' ||
               c == '[' ||
               c == ']' ||
               c == '<' ||
               c == '>';

    }

    private static boolean isStr(int begin){
        return begin == '\'' || begin == '\"';
    }

    public static boolean isEmpty(int c){
        return  c == ' '  ||
                c == '\t' ||
                c == '\r' ||
                c == ':';
    }

    public static boolean initial(int begin) throws SyntaxException{
        if (isEmpty(begin)){
            return false;
        }
        if (isDigit(begin) || begin == '.') {
            type = 'n';
            return true;
        }
        else if (isAlpha(begin)) {
            type = 'i';
            return true;
        }
        else if (isOperator(begin)) {
            type = 'o';
            return true;
        }
        else if (isStr(begin)) {
            type = (char) begin;
            return true;
        }
        else
            throw new SyntaxException("Invalid begin:"+(char)begin);
    }

    public static boolean next(int mid){
        switch (type){
            case 'i':
                return isAlpha(mid) || isDigit(mid);
            case 'n':
                return isDigit(mid) || mid =='.';
            case 'o':
                return isOperator(mid);
            case '\'':
                return mid != '\'';
            case '\"':
                return mid != '\"';

        }
        throw new SyntaxException("Invalid Char");
    }
    public static boolean isReserved(String s){
        return  s.equals("input") ||
                s.equals("output")||
                s.equals("inf") ||
                s.equals("Y") ||
                s.equals("N") ||
                s.equals("is") ||
                s.equals("break");
    }
    public static boolean isType(String s){
        return  s.equals("int") ||
                s.equals("str") ||
                s.equals("real") ||
                s.equals("vector") ||
                s.equals("matrix");
    }

    public static boolean isBIF(String s) {
        return  s.equals("sqrt") ||
                s.equals("absolute") ||
                s.equals("round") ||
                s.equals("norm") ||
                s.equals("power") ||

                s.equals("build")||
                s.equals("summation")||
                s.equals("average")||
                s.equals("variance") ||

                s.equals("Triangle")||
                s.equals("Diagonal")||
                s.equals("Determinant")||
                s.equals("Minor")||
                s.equals("Adjoint")||
                s.equals("Inverse");
    }
}
