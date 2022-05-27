package Abacus.Portable;

import Abacus.Syntax.Token.Element;
import Abacus.Syntax.Token.TokenArray;
import Abacus.Syntax.Tree.ASTBuilder;
import Abacus.Syntax.Tree.TreeElement.NodeType;
import Abacus.Syntax.Tree.TreeElement.SyntaxTreeFork;
import Abacus.Syntax.Tree.TreeElement.SyntaxTreeLeaves;
import Abacus.Syntax.Tree.TreeElement.SyntaxTreeNode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Vector;


class BitsUtils{
    public static byte[] double2Bytes(double d) {
        long value = Double.doubleToRawLongBits(d);
        byte[] byteRet = new byte[8];
        for (int i = 0; i < 8; i++) {
            byteRet[i] = (byte) ((value >> 8 * i) & 0xff);
        }
        return byteRet;
    }
    public static byte[] long2Bytes(long value) {
        byte[] byteRet = new byte[8];
        for (int i = 0; i < 8; i++) {
            byteRet[i] = (byte) ((value >> 8 * i) & 0xff);
        }
        return byteRet;
    }
}

public class Compiler {
    static FileOutputStream fo;

    public static void DumpVar(SyntaxTreeNode varLeaf) throws IOException {
        String name = varLeaf.getBaseName();

        fo.write(OpCode.HEAD_VAR);
        fo.write(name.length());
        fo.write(name.getBytes(StandardCharsets.UTF_8));
    }
    public static void DumpObject(SyntaxTreeLeaves obj) throws IOException {
        if (obj.getType() == NodeType.Single_Number){
            if (obj.getBaseName().contains(".")) {
                fo.write(OpCode.HEAD_REA);
                fo.write(BitsUtils.double2Bytes(Double.parseDouble(obj.getBaseName())));
            }
            else{
                fo.write(OpCode.HEAD_INT);
                fo.write(BitsUtils.long2Bytes(Long.parseLong(obj.getBaseName())));
            }
        }
        else if (obj.getType() == NodeType.Single_Literal){
            fo.write(OpCode.HEAD_STR);
            fo.write(obj.getBaseName().length());
            fo.write(obj.getBaseName().getBytes(StandardCharsets.UTF_8));
        }
//        else if (obj instanceof vector){
//            fo.write(OpCode.HEAD_VEC);
//            fo.write(((vector) obj).dim());
//            for (int i = 0; i < ((vector) obj).dim(); i++)
//                fo.write(((Double)((vector) obj).at(i)).byteValue());
//        }
//        else if (obj instanceof Matrix){
//            fo.write(OpCode.HEAD_MAT);
//            fo.write(((Matrix) obj).getRows());
//            fo.write(((Matrix) obj).getCols());
//            for (int i = 0; i < ((Matrix) obj).getRows(); i++)
//                for (int j = 0; j < ((Matrix) obj).getCols(); j++)
//                    fo.write(((Double)((Matrix) obj).At(i,j)).byteValue());
//        }
    }

    public static void DumpExpression(SyntaxTreeNode stn) throws Exception {
        switch (stn.getType()){
            case Single_Identifier:
                DumpVar(stn);
                return;
            case Single_Literal:
            case Single_Number:
                DumpObject(stn.Leaf());
                return;
            case Multiple_Expression:
                DumpExpression(stn.Fork().at(
                        stn.getBaseName().equals("=")?1:0));
                switch (stn.getBaseName()){
                    case "+..":
                        fo.write(OpCode.POS);
                        return;
                    case "-..":
                        fo.write(OpCode.NEG);
                        return;
                    case "+":
                        DumpExpression(stn.Fork().at(1));
                        fo.write(OpCode.ADD);
                        return;
                    case "-":
                        DumpExpression(stn.Fork().at(1));
                        fo.write(OpCode.SUB);
                        return;
                    case "*":
                        DumpExpression(stn.Fork().at(1));
                        fo.write(OpCode.MUL);
                        return;
                    case "/":
                        DumpExpression(stn.Fork().at(1));
                        fo.write(OpCode.DIV);
                        return;
                    case "%":
                        DumpExpression(stn.Fork().at(1));
                        fo.write(OpCode.MOD);
                        return;
                    case "·":
                        DumpExpression(stn.Fork().at(1));
                        fo.write(OpCode.DOT);
                        return;
                    case "=":
                        fo.write(OpCode.SET_VALUE);
                        DumpVar(stn.Fork().at(0));
                        return;
                    case "is":
                        DumpExpression(stn.Fork().at(1));
                        fo.write(OpCode.VALUE_SAME);
                        return;
                    default:
                        throw new Exception("unexpected: "+stn);
                }
            case Multiple_FunctionCall:
                DumpFunctionCall(stn);
        }
    }
    public static void DumpFunctionCall(SyntaxTreeNode stn) throws Exception{
        for (SyntaxTreeNode stn1 : stn.Fork().kids())
            DumpExpression(stn1);
        DumpVar(stn);
        fo.write(OpCode.CALL);
        fo.write(stn.Fork().getChildrenCount());
    }

    public static void DumpBlock(SyntaxTreeNode stn) throws Exception{
        fo.write(OpCode.BLOCK_BEGIN);
        Dump(stn);
        fo.write(OpCode.BLOCK_END);
    }
    public static void DumpDeclaration(SyntaxTreeNode dec) throws IOException {
        Vector<SyntaxTreeNode> kids = dec.Fork().kids();
        for (int i = kids.size()-1; i != -1 ; --i) {
            SyntaxTreeNode sub = kids.get(i);
            DumpVar(sub.Leaf());
        }

        fo.write(OpCode.DECLARE);
        fo.write(dec.Fork().getChildrenCount());
        DumpVar(dec);
    }
    public static void DumpTypeCast(SyntaxTreeNode dec) throws IOException {
        DumpVar(dec.Fork().at(0).Leaf());
        fo.write(OpCode.TYPE_CAST);
        DumpVar(dec);
    }

    public static void Super_DumpInput(SyntaxTreeNode input) throws IOException {
        Vector<SyntaxTreeNode> kids = input.Fork().kids();
        for (int i = kids.size()-1; i != -1; --i) {
            SyntaxTreeNode sub = kids.get(i);
            switch (sub.getType()) {
                case Single_Identifier:
                    DumpVar(sub);
                    break;
                case Multiple_Declaration:
                    DumpDeclaration(sub);
                    break;
            }
        }
        fo.write(OpCode.INPUT);
        fo.write(input.Fork().getChildrenCount());
    }
    public static void Super_DumpOutput(SyntaxTreeNode output) throws Exception {
        Vector<SyntaxTreeNode> kids = output.Fork().kids();
        for (int i = kids.size()-1; i != -1; --i)  {
            SyntaxTreeNode sub = kids.get(i);
            switch (sub.getType()) {
                case Single_Identifier:
                    DumpVar(sub);
                    break;
                case Multiple_Expression:
                case Multiple_FunctionCall:
                    DumpExpression(sub);
                    break;
                case Single_TypeCast:
                    DumpTypeCast(sub);
                    break;
                case Single_Number:
                case Single_Literal:
                    DumpObject(sub.Leaf());
                    break;
            }
        }
        fo.write(OpCode.OUTPUT);
        fo.write(output.Fork().getChildrenCount());
    }
    public static void Super_DumpLoop(SyntaxTreeNode loop) throws Exception{
        DumpBlock(loop.Fork().at(0));
        if (loop.Fork().at(1).getType() == NodeType.Single_Identifier)
            DumpVar(loop.Fork().at(1).Leaf());
        else
            DumpObject(loop.Fork().at(1).Leaf());
        fo.write(OpCode.SIGNAL_LOOP);
    }
    public static void Super_DumpChoice(SyntaxTreeNode choice) throws Exception{
        if (choice.Fork().getChildrenCount() == 1)
            return;
        if (choice.Fork().at(1) == null)
            fo.write(OpCode.NULL_HEAD);
        else if (choice.Fork().at(1).getType() != NodeType.Multiple_Block){
            SyntaxTreeFork Y = new SyntaxTreeFork(Element.Block, NodeType.Multiple_Block);
            Y.push(choice.Fork().at(1));
            DumpBlock(Y);
        }
        else
            DumpBlock(choice.Fork().at(1));

        if (choice.Fork().at(2) == null)
            fo.write(OpCode.NULL_HEAD);
        else if (choice.Fork().at(2).getType() != NodeType.Multiple_Block){
            SyntaxTreeFork N = new SyntaxTreeFork(Element.Block, NodeType.Multiple_Block);
            N.push(choice.Fork().at(2));
            DumpBlock(N);
        }
        else
            DumpBlock(choice.Fork().at(2));
        DumpExpression(choice.Fork().at(0));
        fo.write(OpCode.SIGNAL_CHOICE);
    }
    public static void Super_DumpStmt(SyntaxTreeNode stmt) throws Exception {
        DumpExpression(stmt);
        fo.write(OpCode.POP_TOP);
    }

    public static void Super_DumpDeclaration(SyntaxTreeNode dec) throws IOException {
        DumpDeclaration(dec);
        for (int i = 0; i < dec.Fork().getChildrenCount(); i++)
            fo.write(OpCode.POP_TOP);
    }

    public static void Dump(SyntaxTreeNode Program_or_Root) throws Exception {
        for (SyntaxTreeNode kid :
                Program_or_Root.Fork().kids())
            switch (kid.getType()){
                case Multiple_Declaration:
                    Super_DumpDeclaration(kid);
                    break;
                case Multiple_Expression:
                case Multiple_FunctionCall:
                    Super_DumpStmt(kid);
                    break;
                case Multiple_If:
                    Super_DumpChoice(kid);
                    break;
                case Multiple_Input:
                    Super_DumpInput(kid);
                    break;
                case Multiple_Output:
                    Super_DumpOutput(kid);
                    break;
                case Multiple_Loop:
                    Super_DumpLoop(kid);
                    break;
                case Single_Identifier:
                    DumpVar(kid);
                    break;
                case Single_Literal:
                case Single_Number:
                    DumpObject(kid.Leaf());
                    break;
                default:
                    throw new Exception("Stmt not dumpable: "+kid);
            }
    }

    public static void Compile(String filepath)throws Exception{
        Vector<Element> ta = TokenArray.Read(filepath);

        SyntaxTreeNode Root = ASTBuilder.BuildFromTokens(ta);
        fo = new FileOutputStream(filepath.substring(0, filepath.length()-3)+"abc");
        fo.write(0xab);
        fo.write(0xcf);
        Dump(Root);
    }
    public static void CompileEx(String Source, String Dest) throws Exception{
        Vector<Element> ta = TokenArray.Read(Source);

        SyntaxTreeNode Root = ASTBuilder.BuildFromTokens(ta);
        fo = new FileOutputStream(Dest);
        fo.write(0xab);
        fo.write(0xcf);
        Dump(Root);
    }
}
