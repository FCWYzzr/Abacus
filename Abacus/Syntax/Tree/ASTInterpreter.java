package Abacus.Syntax.Tree;

import Abacus.BuiltinUtils.Matrix;
import Abacus.BuiltinUtils.vector;
import Abacus.Syntax.Tree.TreeElement.NodeType;
import Abacus.Syntax.Tree.TreeElement.SyntaxTreeFork;
import Abacus.Syntax.Tree.TreeElement.SyntaxTreeLeaves;
import Abacus.Syntax.Tree.TreeElement.SyntaxTreeNode;
import jdk.nashorn.internal.runtime.regexp.joni.exception.SyntaxException;
import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;

import javax.naming.NameNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

import static java.lang.Math.*;

class Values{
    static String toS(Object o){
        if (o instanceof String)
            return String.format("\"%s\"", o);
        return o.toString();
    }
    static String toT(Object o){
        if (o instanceof String)
            return "str";
        else if (o instanceof vector)
            return "vector";
        else if (o instanceof Matrix)
            return "matrix";
        else if (o instanceof Long)
            return "int";
        else if (o instanceof Double)
            return "real";
        else
            return "unknown";
    }
}

class Identifiers{
    static HashMap<String, String> typeTable = new HashMap<>();
    static HashMap<String, Long> varInt = new HashMap<>();
    static HashMap<String, Double> varReal = new HashMap<>();
    static HashMap<String, vector> varVector = new HashMap<>();
    static HashMap<String, Matrix> varMatrix = new HashMap<>();
    static HashMap<String, String> varStr = new HashMap<>();

    static void Declare(SyntaxTreeLeaves stl, String type){
        typeTable.put(stl.getBaseName(), type);
        switch (type){
            case "int":
                varInt.put(stl.getBaseName(), 0L);
                break;
            case "real":
                varReal.put(stl.getBaseName(), 0D);
                break;
            case "str":
                varStr.put(stl.getBaseName(),"");
                break;
            case "vector":
                varVector.put(stl.getBaseName(), new vector());
                break;
            case "matrix":
                varMatrix.put(stl.getBaseName(), new Matrix());
                break;
        }
    }

    static Object getValue(SyntaxTreeLeaves stl) throws NameNotFoundException {
        String name = stl.getBaseName();
        switch (typeTable.get(name)){
            case "int":
                return varInt.get(name);
            case "real":
                return varReal.get(name);
            case "str":
                return varStr.get(name);
            case "vector":
                return varVector.get(name);
            case "matrix":
                return varMatrix.get(name);
            default:
                throw new NameNotFoundException(name+" not declared");
        }
    }
    static String IdentifierType(SyntaxTreeLeaves stl){
        return typeTable.get(stl.getBaseName());
    }
    static boolean Undeclared(SyntaxTreeLeaves stl){
        return !typeTable.containsKey(stl.getBaseName());
    }
    static void Delete(SyntaxTreeLeaves stl){
        if (!typeTable.containsKey(stl.getBaseName())) return;
        switch (typeTable.get(stl.getBaseName())){
            case "int":
                varInt.remove(stl.getBaseName());
                typeTable.remove(stl.getBaseName());
                break;
            case "real":
                varReal.remove(stl.getBaseName());
                typeTable.remove(stl.getBaseName());
                break;
            case "str":
                varStr.remove(stl.getBaseName());
                typeTable.remove(stl.getBaseName());
                break;
            case "vector":
                varVector.remove(stl.getBaseName());
                typeTable.remove(stl.getBaseName());
                break;
            case "matrix":
                varMatrix.remove(stl.getBaseName());
                typeTable.remove(stl.getBaseName());
                break;
            default:
                throw new UnknownError("unknown type");
        }
    }

    static void storeValue(SyntaxTreeLeaves stl, Object value){
        if(_storeSameType(stl, value)) return;
        Delete(stl);
        if(value instanceof Long) {
            Declare(stl, "int");
            varInt.put(stl.getBaseName(), (Long) value);
        }
        else if (value instanceof Double) {
            Declare(stl, "real");
            varReal.put(stl.getBaseName(), (Double) value);
        }
        else if (value instanceof String) {
            Declare(stl, "str");
            varStr.put(stl.getBaseName(), (String) value);
        }
        else if (value instanceof vector) {
            Declare(stl, "vector");
            varVector.put(stl.getBaseName(), (vector) value);
        }
        else if (value instanceof Matrix) {
            Declare(stl, "matrix");
            varMatrix.put(stl.getBaseName(), (Matrix) value);
        }
    }
    private static boolean _storeSameType(SyntaxTreeLeaves stl, Object value){
        if (Undeclared(stl))
            return false;
        else if(value instanceof Long && IdentifierType(stl).equals("int"))
            varInt.put(stl.getBaseName(), (Long) value);
        else if (value instanceof Double && IdentifierType(stl).equals("real"))
            varReal.put(stl.getBaseName(), (Double) value);
        else if (value instanceof String && IdentifierType(stl).equals("str"))
            varStr.put(stl.getBaseName(), (String) value);
        else if (value instanceof vector && IdentifierType(stl).equals("vector"))
            varVector.put(stl.getBaseName(), (vector) value);
        else if (value instanceof Matrix && IdentifierType(stl).equals("matrix"))
            varMatrix.put(stl.getBaseName(), (Matrix) value);
        else
            return false;
        return true;
    }

    static void check(SyntaxTreeNode stl) throws Exception {
        if (stl instanceof SyntaxTreeLeaves)
            return;
        throw new Exception("not assignable left value");
    }
}


public class ASTInterpreter {
    public static Object eval(SyntaxTreeNode node) throws Exception{
        SyntaxTreeNode left, right;
        Object a,b;

        String baseName = node.getBaseName();
        if (node instanceof SyntaxTreeLeaves){

            switch (node.getBaseType()){
                case Number:
                    return Double.valueOf(baseName);
                case Identifier:
                    return Identifiers.getValue(node.Leaf());
                case Literal:
                    return baseName;
                default:
                    throw new Exception("not Evaluable operation");
            }
        }
        else if (node.getType() == NodeType.Multiple_FunctionCall){
            return CallFunction(node);
        }
        else if ("=".equals(baseName)) {

            left = node.Fork().at(0);
            right = node.Fork().at(1);
            Identifiers.check(left);

            a = eval(right);
            Identifiers.storeValue(left.Leaf(), a);
            return a;
        }
        else if ("+".equals(baseName) && node.Fork().getChildrenCount() == 1) {

            a = eval(node.Fork().at(0));

            if (a instanceof String) throw new Exception("str can not be positive");
            return eval(node.Fork().at(0));
        }
        else if ("-".equals(baseName) && node.Fork().getChildrenCount() == 1) {

            a = eval(node.Fork().at(0));

            if (a instanceof Long)  return  -(Long)   a;
            if (a instanceof Double) return -(Double) a;
            if (a instanceof vector) return ((vector) a).negative();
            if (a instanceof Matrix) return ((Matrix) a).negative();
            if (a instanceof String) throw new Exception("str can not be negative");
        }
        else {
            left = node.Fork().at(0);
            right = node.Fork().at(1);
            a = eval(left);
            b = eval(right);
            assert a != null && b != null;
            switch (baseName){
                case "+":
                    if (a.getClass().equals(b.getClass())){
                        if (a instanceof Long)
                            return (Long)a + (Long) b;
                        else if (a instanceof Double)
                            return (Double)a + (Double) b;
                        else if (a instanceof String)
                            return a + (String) b;
                        else if (a instanceof vector)
                            return ((vector) a).add((vector) b);
                        else if (a instanceof Matrix)
                            return ((Matrix) a).add((Matrix) b);
                    }
                    else if(a instanceof Number && b instanceof Number)
                        return ((Number) a).doubleValue() + ((Number) b).doubleValue();
                    else throw new Exception(String.format("%s and %s is not addable", a, b));
                    break;
                case "-":
                    if (a.getClass().equals(b.getClass())){
                        if (a instanceof Long)
                            return (Long)a - (Long) b;
                        else if (a instanceof Double)
                            return (Double)a - (Double) b;
                        else if (a instanceof vector)
                            return ((vector) a).subtract((vector) b);
                        else if (a instanceof Matrix)
                            return ((Matrix) a).subtract((Matrix) b);
                        else if (a instanceof String)
                            throw new Exception("str can not subtract");
                    }
                    else if(a instanceof Number && b instanceof Number)
                        return ((Number) a).doubleValue() + ((Number) b).doubleValue();
                    else
                        throw new Exception(
                                String.format("%s and %s can not subtract",
                                        Values.toT(a), Values.toT(b)
                                )
                        );
                    break;
                case "*":
                    if (a.getClass().equals(b.getClass())){
                        if (a instanceof Long)
                            return (Long)a * (Long) b;
                        else if (a instanceof Double)
                            return (Double)a * (Double) b;
                        else if (a instanceof vector)
                            return ((vector) a).outerMultiply((vector) b);
                        else if (a instanceof Matrix)
                            return ((Matrix) a).Multiply((Matrix) b);
                        else if (a instanceof String)
                            throw new Exception("str can not multiply");
                    }
                    else if(a instanceof Number && b instanceof Number)
                        return ((Number) a).doubleValue() * ((Number) b).doubleValue();
                    else if((a instanceof String && b instanceof Long)){
                        StringBuilder SB = new StringBuilder();
                        for (long i = 0; i < (Long) b; i++)
                            SB.append(a);
                        return SB.toString();
                    }
                    else if((a instanceof Number && b instanceof vector))
                        return ((vector) b).Multiply(((Number) a).doubleValue());
                    else if((b instanceof Number && a instanceof vector))
                        return ((vector) a).Multiply(((Number) b).doubleValue());
                    else throw new Exception(String.format("%s and %s can not multiply", a, b));
                    break;
                case "/":
                    if (a.getClass().equals(b.getClass())){
                        if (a instanceof Long)
                            return (Long)a / (Long) b;
                        else if (a instanceof Double)
                            return (Double)a / (Double) b;
                        else if (a instanceof vector)
                            return ((vector) a).outerMultiply((vector) b);
                        else if (a instanceof Matrix)
                            return ((Matrix) a).Multiply(((Matrix) b).inverseMatrix());
                        else if (a instanceof String)
                            throw new Exception("str can not divide");
                    }
                    else if(a instanceof Number && b instanceof Number)
                        return ((Number) a).doubleValue() * ((Number) b).doubleValue();
                    else if((b instanceof Number && a instanceof vector))
                        return ((vector) a).div(((Number) b).doubleValue());
                    else throw new Exception(String.format("%s and %s can not divide", a, b));
                    break;
                case "%":
                    if (a instanceof Number && b instanceof Number)
                        return ((Number) a).doubleValue() % ((Number) b).doubleValue();
                    else throw new Exception(String.format("%s and %s can not mod", a, b));
                case "is":
                    return a.equals(b)?1:0;
                case "·":
                    if (a instanceof Number && b instanceof Number)
                        return ((Number) a).doubleValue() * ((Number) b).doubleValue();
                    else if (a instanceof vector && b instanceof vector)
                        return ((vector) a).innerMultiply((vector) b);
                    else throw new Exception(String.format("%s and %s can not mod", Values.toS(a), Values.toS(b)));
            }
        }
        throw new Exception("this part should not be execute");
    }

    public static void Output(SyntaxTreeFork STF) throws Exception {
        assert STF.getBaseName().equals("output");
        for (SyntaxTreeNode stl:STF.kids()) {
            switch (stl.getType()) {
                case Single_Identifier:
                    System.out.println(Identifiers.getValue(stl.Leaf()));
                    break;
                case Single_Number:
                case Single_Literal:
                    System.out.println(stl.getBaseName());
                    break;
                case Single_TypeCast:
                    Object value = eval(stl.Fork().at(0));
                    switch (stl.getBaseName()){
                        case "int":
                            if (value instanceof Number)
                                System.out.println(((Number) value).longValue());
                            else if(value instanceof vector)
                                System.out.println(((Number)((vector) value).norm(2)).longValue());
                            else if(value instanceof Matrix)
                                System.out.println(((Number)((Matrix) value).norm(2)).longValue());
                            break;
                        case "real":
                            if (value instanceof Number)
                                System.out.println(((Number) value).doubleValue());
                            else if(value instanceof vector)
                                System.out.println(((Number)((vector) value).norm(2)).doubleValue());
                            else if(value instanceof Matrix)
                                System.out.println(((Number)((Matrix) value).norm(2)).doubleValue());
                            break;
                    }
                    break;
                case Multiple_Expression:
                case Multiple_FunctionCall:
                    System.out.println(eval(stl));
                    break;
                default:
                    throw new ValueException("unprintable type");
            }

        }
    }

    public static void DeclareIdentifier(SyntaxTreeFork STF){
        for (SyntaxTreeNode stn:STF.kids())
            Identifiers.Declare(stn.Leaf(), STF.getBaseName());
    }

    public static void Input(SyntaxTreeFork STF) throws Exception {
        assert STF.getBaseName().equals("input");
        Scanner sc = new Scanner(System.in);
        for (SyntaxTreeNode stl:STF.kids())
            switch (stl.getType()) {
                case Single_Identifier:
                    if (Identifiers.Undeclared(stl.Leaf())){
                        Identifiers.storeValue(stl.Leaf(), sc.nextLine());
                    }
                    else
                        switch (Identifiers.IdentifierType(stl.Leaf())){
                            case "str":
                                Identifiers.storeValue(stl.Leaf(), sc.next());
                                break;
                            case "int":
                                Identifiers.storeValue(stl.Leaf(), sc.nextLong());
                                break;
                            case "real":
                                Identifiers.storeValue(stl.Leaf(), sc.nextDouble());
                                break;
                            case "matrix":
                                ((Matrix)Identifiers.getValue(stl.Leaf())).input(sc);
                                break;
                            case "vector":
                                ((vector)Identifiers.getValue(stl.Leaf())).input(sc.nextLine());
                                break;
                            default:
                                throw new SyntaxException("Type do not support");
                        }
                    break;
                case Multiple_Declaration:
                    assert stl instanceof SyntaxTreeFork;

                    DeclareIdentifier((SyntaxTreeFork) stl);

                    switch (stl.getBaseName()){
                        case "int":
                            for (SyntaxTreeNode stn : stl.Fork().kids())
                                Identifiers.storeValue(stn.Leaf(), sc.nextLong());
                            break;
                        case "str":
                            for (SyntaxTreeNode stn : stl.Fork().kids())
                                Identifiers.storeValue(stn.Leaf(), sc.next());
                            break;
                        case "real":
                            for (SyntaxTreeNode stn : stl.Fork().kids())
                                Identifiers.storeValue(stn.Leaf(), sc.nextDouble());
                            break;
                        case "vector":
                            for (SyntaxTreeNode stn : stl.Fork().kids()) {
                                ((vector)Identifiers.getValue(stn.Leaf())).input(sc.nextLine());
                            }
                            break;
                        case "matrix":
                            for (SyntaxTreeNode stn : stl.Fork().kids()) {
                                ((Matrix)Identifiers.getValue(stn.Leaf())).input(sc);
                            }
                            break;
                        default:
                            throw new SyntaxException("Type do not support");
                    }
                    break;
                default:
                    throw new ValueException("unscanable type: "+stl.getType());
            }
    }

    public static boolean Run(SyntaxTreeFork root) throws Exception{
        for (SyntaxTreeNode stn :root.kids())
            if (!run_single(stn))
                return false;
        return true;
    }
    private static boolean run_single(SyntaxTreeNode stn) throws Exception {
        long lim;
        switch (stn.getType()){
            case Multiple_Output:
                Output((SyntaxTreeFork)stn);
                break;
            case Multiple_Expression:
                eval(stn);
                break;
            case Multiple_Declaration:
                DeclareIdentifier((SyntaxTreeFork) stn);
                break;
            case Multiple_Input:
                Input((SyntaxTreeFork) stn);
                break;
            case Multiple_Loop:
                SyntaxTreeFork stf = (SyntaxTreeFork) stn;
                if ((stf.kids().lastElement()).getType() == NodeType.Single_Number)
                    for (int i = 0;
                         i != Integer.parseInt(stf.kids().lastElement().getBaseName());
                         ++i) {
                        if(!Run(stf.kids().firstElement().Fork()))
                            return false;
                    }
                else if ((stf.kids().lastElement()).getType() == NodeType.Single_Identifier) {
                    lim = Long.parseLong(stf.kids().lastElement().getBaseName());
                    for (int i = 0;
                         i != lim;
                         ++i)
                        if(!Run(stf.kids().firstElement().Fork()))
                            return false;
                }
                else
                    while(Run((SyntaxTreeFork) stf.kids().firstElement()));
                break;

            case Multiple_If:
                runIf((SyntaxTreeFork) stn);
                break;
            default:
                return false;
        }
        return true;
    }

    public static void runIf(SyntaxTreeFork ifRoot) throws Exception{
        Object cond = eval(ifRoot.at(0));
        SyntaxTreeNode T,F;
        if((cond instanceof Number && abs(((Number) cond).doubleValue()) > Double.MIN_VALUE) ||
           (cond instanceof vector && ((vector)cond).norm(2) > Double.MIN_VALUE) ||
           (cond instanceof Matrix && ((Matrix)cond).determinant() > Double.MIN_VALUE)) {
            T = ifRoot.at(1);
            if (T != null)
                run_single(T);
        }
        else if (ifRoot.at(2) != null) {
            F = ifRoot.at(2);
            if (F != null)
                run_single(F);
        }
    }

    public static Object CallFunction(SyntaxTreeNode stn)throws Exception{
        int params = stn.Fork().getChildrenCount();
        Object a=null,b=null,c=null,d=null;
        if (params > 0)
            a = eval(stn.Fork().at(0));
        if (params > 1)
            b = eval(stn.Fork().at(1));
        if (params > 2)
            c = eval(stn.Fork().at(2));
        if (params > 3)
            d = eval(stn.Fork().at(3));

        switch (stn.getBaseName()){
            case "sqrt":
                if (params == 1) {
                    if (a instanceof Number)
                        return sqrt(((Number) a).doubleValue());
                    throw new Exception("absolute param type must be int or real");
                }
                else
                    throw new Exception("absolute param count must be 1");
            case "absolute":
                if (params == 1) {
                    if (a instanceof Number)
                        return abs(((Number) a).doubleValue());
                    throw new Exception("absolute param type must be int or real");
                }
                else
                    throw new Exception("absolute param count must be 1");

            case "round":
                if (params == 1) {
                    if (a instanceof Number)
                        return round(((Number) a).doubleValue());
                    throw new Exception("absolute param type must be int or real");
                }
                else
                    throw new Exception("absolute param count must be 1");
            case "norm":
                if (params == 2) {
                    if (a instanceof vector && b instanceof Long)
                        return ((vector) a).norm(((Long) b).intValue());
                    else if (a instanceof Matrix && b instanceof Long)
                        return ((Matrix) a).norm(((Long) b).intValue());
                    throw new Exception("norm params type must be (vector/matrix, int)");
                }
                else if (params == 1){
                    if (a instanceof vector)
                        return ((vector) a).norm(2);
                    else if (a instanceof Matrix)
                        return ((Matrix) a).norm(2);
                    throw new Exception("norm params type must be (vector/matrix, int)");
                }
                else
                    throw new Exception("absolute param count must be 2");
            case "power":
                if (params == 2) {
                    if (a instanceof Number && b instanceof Number)
                        return pow(((Number) a).doubleValue(), ((Number) b).doubleValue());
                    else if (a instanceof Matrix && b instanceof Long)
                        return ((Matrix) a).pow(((Long) b).intValue());
                    throw new Exception("power params type must be (matrix, int) or (int/real, int/real)");
                }
                else
                    throw new Exception("power param count must be 2");
            case "build":
                if (params == 2) {
                    if (a instanceof vector && b instanceof vector)
                        return ((vector) a).matrixMultiply((vector) b);
                    throw new Exception("build params type must be (vector, vector)");
                }
                else
                    throw new Exception("build param count must be 2");
            case "summation":
                if (params == 1) {
                    if (a instanceof vector)
                        return ((vector) a).sum();
                    throw new Exception("summation param type must be vector");
                }
                else
                    throw new Exception("summation param count must be 1");
            case "average":
                if (params == 1) {
                    if (a instanceof vector)
                        return ((vector) a).ave();
                    throw new Exception("average param type must be vector");
                }
                else
                    throw new Exception("average param count must be 1");
            case "variance":
                if (params == 1) {
                    if (a instanceof vector)
                        return ((vector) a).var();
                    throw new Exception("variance param type must be vector");
                }
                else
                    throw new Exception("variance param count must be 1");
            case "Triangle":
                if (params == 2) {
                    if (a instanceof Matrix && b instanceof String)
                        if (b.equals("U"))
                            return ((Matrix) a).toUpTriangle();
                        else if (b.equals("D"))
                            return ((Matrix) a).toDownTriangle();
                        else
                            throw new Exception("Triangle param 2 must be 'U' or 'D'");
                    throw new Exception("Triangle params types must be (matrix, str)");
                }
                else
                    throw new Exception("Triangle param count must be 2");
            case "Diagonal":
                if (params == 1) {
                    if (a instanceof Matrix)
                        return ((Matrix) a).toDiagonal();
                    throw new Exception("Diagonal param type must be matrix");
                }
                else
                    throw new Exception("Diagonal param count must be 1");
            case "Determinant":
                if (params == 1) {
                    if (a instanceof Matrix)
                        return ((Matrix) a).determinant();
                    throw new Exception("Determinant param type must be matrix");
                }
                else
                    throw new Exception("Determinant param count must be 1");
            case "Minor":
                if (params == 3 ||params == 4) {
                    if (a instanceof Matrix && b instanceof Long && c instanceof Long &&
                        (d==null||(d instanceof String))) {
                        if (d == null || d.equals("normal"))
                            return ((Matrix) a).Minor(((Number)b).intValue(),((Number)c).intValue());
                        else if (d.equals("complemental"))
                            return ((Matrix) a).complementalMinor(((Number)b).intValue(),((Number)c).intValue());
                        else
                            throw new Exception("Minor param 4 must be \"normal\"(default) or \"complemental\"");
                    }
                    throw new Exception("Minor param types must be (matrix,int,int,str)");
                }
                else
                    throw new Exception("Minor param count must be 3 or 4");
            case "Adjoint":
                if (params == 1) {
                    if (a instanceof Matrix)
                        return ((Matrix) a).adjointMatrix();
                    throw new Exception("Adjoint param type must be matrix");
                }
                else
                    throw new Exception("Adjoint param count must be 1");
            case "Inverse":
                if (params == 1) {
                    if (a instanceof Matrix)
                        return ((Matrix) a).inverseMatrix();
                    throw new Exception("Inverse param type must be matrix");
                }
                else
                    throw new Exception("Inverse param count must be 1");
            default:
                throw new Exception("no such BIF");
        }

    }
}
