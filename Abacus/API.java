package Abacus;

import Abacus.Portable.AbacusVirtualMachine;
import Abacus.Portable.Compiler;
import Abacus.Syntax.Token.Element;
import Abacus.Syntax.Token.TokenArray;
import Abacus.Syntax.Tree.ASTBuilder;
import Abacus.Syntax.Tree.ASTInterpreter;
import Abacus.Syntax.Tree.TreeElement.SyntaxTreeFork;

import java.util.Vector;

public class API {
    static void InterpretSource(String name) {
        try {
            Vector<Element> ta = TokenArray.Read(name);
            SyntaxTreeFork Root = ASTBuilder.BuildFromTokens(ta);
            ASTInterpreter.Run(Root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    static void Compile(String name) {
        try {
            Compiler.Compile(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    static void CompileEx(String name1, String name2) {
        try {
            Compiler.CompileEx(name1,name2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    static void InterpretCode(String name) {
        try {
            AbacusVirtualMachine.Interpret(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String filepath, code;
        int l;
        switch (args.length) {
            case 1:
                filepath = args[0];
                l = filepath.length();
                if (filepath.startsWith(".aba", l-4))
                    InterpretSource(filepath);
                else if (filepath.startsWith(".abc", l-4))
                    InterpretCode(filepath);
                else
                    show_help();
                break;
            case 2:
                code = args[0];
                filepath = args[1];
                switch (code){
                    case "r":
                    case "-r":
                        InterpretSource(filepath);
                        break;
                    case "c":
                    case "-c":
                        Compile(filepath);
                        break;
                    case "-rc":
                        InterpretCode(filepath);
                        break;
                    default:
                        show_help();
                }
                break;
            case 3:
                code = args[0];
                if (code.equals("c")||code.equals("-c"))
                    CompileEx(args[1], args[2]);
                else
                    show_help();
                break;
            default:
                show_help();
        }
    }
    static void show_help(){
        String msg = "Usage:\n" +
                "Abacus.exe [xxx.aba|xxx.abc]       run source/code file (correct extension name required) \n" +
                "Abacus.exe -r xxx                  run xxx as abacus source file\n" +
                "Abacus.exe -c xxx                  compile xxx as abacus source file\n" +
                "Abacus.exe -rc xxx                 run xxx as abacus bytecode file\n" +
                "Abacus.exe -c fileA fileB          compile fileA as as abacus source file, store bytecode to fileB";
        System.err.print(msg);
    }
}
