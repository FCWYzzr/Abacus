package Abacus.Portable;

import Abacus.BuiltinUtils.Matrix;
import Abacus.BuiltinUtils.vector;

import javax.naming.NameNotFoundException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

import static java.lang.Math.*;
import static java.lang.Math.pow;

class RuntimeData{
    Object content;
    DataType type;

    public Object getContent() {
        return content;
    }
    public DataType getType() {
        return type;
    }

    public RuntimeData(Object content, DataType type) {
        this.content = content;
        this.type = type;
    }
}

class Utils{
    static Long BuildInt(InputStream fi) throws Exception{
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= ((long) (fi.read() & 0xff)) << (8 * i);
        }
        return value;
    }
    static Double BuildReal(InputStream fi) throws Exception{
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= ((long) (fi.read() & 0xff)) << (8 * i);
        }
        return Double.longBitsToDouble(value);
    }
    static String BuildStr(InputStream fi) throws Exception{
        StringBuilder stringBuilder = new StringBuilder();
        int length = fi.read();
        for (;length != 0; -- length)
            stringBuilder.append((char)fi.read());
        return stringBuilder.toString();
    }
//    static Double BuildVector(FileInputStream fi) throws Exception{}
//    static Double BuildMatrix(FileInputStream fi) throws Exception{}
}

class Types{
    static String getInnerType(Object o) throws Exception {
        if (o instanceof Long)
            return "int";
        else if (o instanceof Double)
            return "real";
        else if (o instanceof String)
            return "str";
        else if (o instanceof vector)
            return "vector";
        else if (o instanceof Matrix)
            return "matrix";
        else
            throw new Exception("no such type: "+o.getClass());
    }
}

class Identifiers{
    static HashMap<String, String> typeTable = new HashMap<>();
    static HashMap<String, Long> varInt = new HashMap<>();
    static HashMap<String, Double> varReal = new HashMap<>();
    static HashMap<String, vector> varVector = new HashMap<>();
    static HashMap<String, Matrix> varMatrix = new HashMap<>();
    static HashMap<String, String> varStr = new HashMap<>();

    static void Declare(String var, String type){
        typeTable.put(var, type);
        switch (type){
            case "int":
                varInt.put(var, 0L);
                break;
            case "real":
                varReal.put(var, 0D);
                break;
            case "str":
                varStr.put(var,"");
                break;
            case "vector":
                varVector.put(var, new vector());
                break;
            case "matrix":
                varMatrix.put(var, new Matrix());
                break;
            default:
                throw new UnknownError("unknown type: "+type);
        }
    }

    static Object getValue(String name) throws NameNotFoundException {
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
    static String IdentifierType(String name){
        return typeTable.get(name);
    }
    static boolean Undeclared(String name){
        return !typeTable.containsKey(name);
    }
    static void Delete(String var){
        if (!typeTable.containsKey(var))
            return;
        switch (typeTable.get(var)){
            case "int":
                varInt.remove(var);
                typeTable.remove(var);
                break;
            case "real":
                varReal.remove(var);
                typeTable.remove(var);
                break;
            case "str":
                varStr.remove(var);
                typeTable.remove(var);
                break;
            case "vector":
                varVector.remove(var);
                typeTable.remove(var);
                break;
            case "matrix":
                varMatrix.remove(var);
                typeTable.remove(var);
                break;
            default:
                throw new UnknownError("unknown type");
        }
    }

    static void storeValue(String var, Object value){
        if(_storeSameType(var, value)) return;
        Delete(var);
        if(value instanceof Long) {
            Declare(var, "int");
            varInt.put(var, (Long) value);
        }
        else if (value instanceof Double) {
            Declare(var, "real");
            varReal.put(var, (Double) value);
        }
        else if (value instanceof String) {
            Declare(var, "str");
            varStr.put(var, (String) value);
        }
        else if (value instanceof vector) {
            Declare(var, "vector");
            varVector.put(var, (vector) value);
        }
        else if (value instanceof Matrix) {
            Declare(var, "matrix");
            varMatrix.put(var, (Matrix) value);
        }
    }
    private static boolean _storeSameType(String var, Object value){
        if (Undeclared(var))
            return false;
        else if(value instanceof Long && IdentifierType(var).equals("int"))
            varInt.put(var, (Long) value);
        else if (value instanceof Double && IdentifierType(var).equals("real"))
            varReal.put(var, (Double) value);
        else if (value instanceof String && IdentifierType(var).equals("str"))
            varStr.put(var, (String) value);
        else if (value instanceof vector && IdentifierType(var).equals("vector"))
            varVector.put(var, (vector) value);
        else if (value instanceof Matrix && IdentifierType(var).equals("matrix"))
            varMatrix.put(var, (Matrix) value);
        else
            return false;
        return true;
    }
    static String readVarName(InputStream fi) throws Exception {
        int arg;

        StringBuilder SB = new StringBuilder(arg=fi.read());
        for(;arg != 0; --arg)
            SB.append((char)fi.read());
        return SB.toString();
    }
    static String readVarName_Ex(InputStream fi) throws Exception {
        if (fi.read() != OpCode.HEAD_VAR)
            throw new Exception("Var name Not Found: ");
        return readVarName(fi);
    }
}

class Values{
    static Object getValue(RuntimeData runtimeData) throws Exception {
        switch (runtimeData.getType()) {
            case DT_IDENTIFIER:
                return Identifiers.getValue((String) runtimeData.getContent());
            case DT_VEC:
            case DT_MAT:
            case DT_NUMBER:
            case DT_LITERAL:
                return runtimeData.content;
            default:
                throw new Exception("data type have no value");
        }
    }
    static RuntimeData store(int head, InputStream bi) throws Exception{
        switch (head){
            case OpCode.HEAD_VAR:
                return new RuntimeData(Identifiers.readVarName(bi), DataType.DT_IDENTIFIER);
            case OpCode.HEAD_INT:
                return new RuntimeData(Utils.BuildInt(bi), DataType.DT_NUMBER);
            case OpCode.HEAD_REA:
                return new RuntimeData(Utils.BuildReal(bi), DataType.DT_NUMBER);
            case OpCode.HEAD_STR:
                return new RuntimeData(Utils.BuildStr(bi), DataType.DT_LITERAL);
            case OpCode.NULL_HEAD:
                return new RuntimeData(null, DataType.DT_LITERAL);
        }
        throw new Exception("No such data: "+head);
    }
}

public class AbacusVirtualMachine {
    static FileInputStream fi;
    static Stack<RuntimeData> runtimeStack;
    static Scanner sc;

    static void Check(int code)throws Exception{
        if (code == -1) throw new Exception("Unexpected EOF");
    }

    static void CheckHead()throws Exception{
        int tmp1,tmp2;
        Check(tmp1 = fi.read());
        Check(tmp2 = fi.read());
        if (tmp1 != 0xab || tmp2 != 0xcf)
            throw new Exception("Invalid File");
    }

    static void runInput(int n) throws Exception {
        String var;
        for (;n != 0;-- n){
            var = (String)runtimeStack.peek().getContent();
            if(Identifiers.Undeclared(var))
                Identifiers.storeValue(var, sc.nextLine());
            else
                switch (Identifiers.IdentifierType(var)){
                    case "str":
                        Identifiers.storeValue(var, sc.next());
                        break;
                    case "int":
                        Identifiers.storeValue(var, sc.nextLong());
                        break;
                    case "real":
                        Identifiers.storeValue(var, sc.nextDouble());
                        break;
                    case "vector":
                        ((vector)Identifiers.getValue(var)).input(sc.nextLine());
                        break;
                    case "matrix":
                        ((Matrix)Identifiers.getValue(var)).input(sc);
                        break;
                    default:
                        throw new Exception("unknown input type");
            }
            runtimeStack.pop();
        }
    }

    static void runOutput(int n) throws Exception{
        for (;n != 0;-- n){
            switch (runtimeStack.peek().getType()) {
                case DT_NUMBER:
                case DT_LITERAL:
                case DT_MAT:
                case DT_VEC:
                    System.out.println(runtimeStack.peek().getContent());
                    runtimeStack.pop();
                    break;
                case DT_IDENTIFIER:
                    System.out.println(Identifiers.getValue((String) runtimeStack.peek().getContent()));
                    runtimeStack.pop();
                    break;
                default:
                    throw new Exception("unknown output type");
            }
        }
    }

    static void runDeclare(int n, InputStream bi) throws Exception{
        String type = Identifiers.readVarName_Ex(bi);
        for (;n!= 0; --n)
            Identifiers.Declare((String) runtimeStack.peek().getContent(), type);
    }
    static void runCast(InputStream fi) throws Exception {
        String type = Identifiers.readVarName_Ex(fi);
        Object tmp;
        switch (type){
            case "str":
                switch (runtimeStack.peek().getType()){
                    case DT_LITERAL:
                        break;
                    case DT_NUMBER:
                        tmp = runtimeStack.peek().getContent();
                        runtimeStack.pop();
                        runtimeStack.push(new RuntimeData(tmp, DataType.DT_LITERAL));
                        break;
                    case DT_IDENTIFIER:
                        tmp = runtimeStack.peek().getContent();
                        runtimeStack.push(new RuntimeData(Identifiers.getValue((String) tmp),DataType.DT_LITERAL));
                        break;
                    case CD_BLOCK:
                        throw new Exception("invalid syntax");
                }
                break;
            case "int":
                switch (runtimeStack.peek().getType()){
                    case DT_LITERAL:
                        tmp = runtimeStack.peek().getContent();
                        runtimeStack.pop();
                        runtimeStack.push(new RuntimeData(Long.parseLong((String) tmp), DataType.DT_NUMBER));
                        break;
                    case DT_NUMBER:
                        tmp = runtimeStack.peek().getContent();
                        runtimeStack.pop();
                        runtimeStack.push(new RuntimeData(((Number)tmp).longValue(), DataType.DT_NUMBER));
                        break;
                    case DT_IDENTIFIER:
                        tmp = runtimeStack.peek().getContent();
                        runtimeStack.pop();
                        if ( Identifiers.Undeclared((String) tmp)) throw new Exception("Name not found: "+tmp);
                        switch (Identifiers.IdentifierType((String) tmp)) {
                            case "int":
                            case "real":
                                runtimeStack.push(
                                        new RuntimeData(((Number) Identifiers.getValue((String) tmp)).longValue(),
                                                DataType.DT_NUMBER)
                                );
                                break;
                            case "vector":
                                runtimeStack.push(
                                        new RuntimeData(
                                                ((Double)((vector) Identifiers.getValue((String) tmp)).norm(2))
                                                        .longValue(),
                                                DataType.DT_NUMBER)
                                );
                                break;
                            case "matrix":
                                runtimeStack.push(
                                        new RuntimeData(
                                                ((Double)((Matrix) Identifiers.getValue((String) tmp)).norm(2))
                                                        .longValue(),
                                                DataType.DT_NUMBER)
                                );
                                break;
                            case "str":
                                runtimeStack.push(
                                        new RuntimeData(
                                                Long.parseLong((String) Identifiers.getValue((String) tmp)),
                                                DataType.DT_NUMBER)
                                );
                                break;
                        }
                        break;
                    case CD_BLOCK:
                        throw new Exception("invalid syntax");
                }
                break;
            case "real":
                switch (runtimeStack.peek().getType()){
                    case DT_LITERAL:
                        tmp = runtimeStack.peek().getContent();
                        runtimeStack.pop();
                        runtimeStack.push(new RuntimeData(Double.parseDouble((String) tmp), DataType.DT_NUMBER));
                        break;
                    case DT_NUMBER:
                        tmp = runtimeStack.peek().getContent();
                        runtimeStack.pop();
                        runtimeStack.push(new RuntimeData(((Number)tmp).doubleValue(), DataType.DT_NUMBER));
                        break;
                    case DT_IDENTIFIER:
                        tmp = runtimeStack.peek().getContent();
                        runtimeStack.pop();
                        if ( Identifiers.Undeclared((String) tmp)) throw new Exception("Name not found: "+tmp);
                        switch (Identifiers.IdentifierType((String) tmp)) {
                            case "int":
                            case "real":
                                runtimeStack.push(
                                        new RuntimeData(((Number) Identifiers.getValue((String) tmp)).doubleValue(),
                                                DataType.DT_NUMBER)
                                );
                                break;
                            case "vector":
                                runtimeStack.push(
                                        new RuntimeData(
                                                ((vector) Identifiers.getValue((String) tmp)).norm(2),
                                                DataType.DT_NUMBER)
                                );
                                break;
                            case "matrix":
                                runtimeStack.push(
                                        new RuntimeData(
                                                ((Matrix) Identifiers.getValue((String) tmp)).norm(2),
                                                DataType.DT_NUMBER)
                                );
                                break;
                            case "str":
                                runtimeStack.push(
                                        new RuntimeData(
                                                Double.parseDouble((String) Identifiers.getValue((String) tmp)),
                                                DataType.DT_NUMBER)
                                );
                                break;
                        }
                        break;
                    case CD_BLOCK:
                        throw new Exception("invalid syntax");
                }
                break;
            default:
                throw new Exception("vector & matrix do not support type cast");
        }
    }

    static void SingleOperation(int op) throws Exception {
        RuntimeData data = runtimeStack.peek();
        switch (op){
            case OpCode.POS:
                switch (data.getType()){
                    case DT_IDENTIFIER:
                        runtimeStack.pop();
                        switch (Identifiers.IdentifierType((String) data.getContent())){
                            case "int":
                            case "real":
                                runtimeStack.push(
                                        new RuntimeData(Identifiers.getValue((String) data.getContent()),
                                                DataType.DT_NUMBER)
                                );
                                break;
                            case "str":
                                throw new Exception("str do not support positive");
                            case "vec":
                                runtimeStack.push(
                                        new RuntimeData(Identifiers.getValue((String) data.getContent()),
                                                DataType.DT_VEC)
                                );
                                break;
                            case "Matrix":
                                runtimeStack.push(
                                        new RuntimeData(Identifiers.getValue((String) data.getContent()),
                                                DataType.DT_MAT)
                                );
                                break;
                        }
                        break;
                    case DT_LITERAL:
                        throw new Exception("str do not support positive");
                    case DT_NUMBER:
                    case DT_MAT:
                    case DT_VEC:
                        break;
                }
                break;
            case OpCode.NEG:
                switch (data.getType()){
                    case DT_IDENTIFIER:
                        runtimeStack.pop();
                        switch (Identifiers.IdentifierType((String) data.getContent())){
                            case "int":
                                runtimeStack.push(
                                        new RuntimeData(
                                                -((Integer)Identifiers.getValue((String) data.getContent())),
                                                DataType.DT_NUMBER)
                                );
                                break;
                            case "real":
                                runtimeStack.push(
                                        new RuntimeData(
                                                -((Double)Identifiers.getValue((String) data.getContent())),
                                                DataType.DT_NUMBER)
                                );
                                break;
                            case "str":
                                throw new Exception("str do not support positive");
                            case "vec":
                                runtimeStack.push(
                                        new RuntimeData(
                                                ((vector)Identifiers.getValue((String) data.getContent()))
                                                .negative(),
                                                DataType.DT_VEC)
                                );
                                break;
                            case "Matrix":
                                runtimeStack.push(
                                        new RuntimeData(
                                                ((Matrix)Identifiers.getValue((String) data.getContent()))
                                                        .negative(),
                                                DataType.DT_MAT)
                                );
                                break;
                        }
                        break;
                    case DT_LITERAL:
                        throw new Exception("str do not support negative");
                    case DT_NUMBER:
                        runtimeStack.peek().content = -(Double)runtimeStack.peek().content;
                        break;
                    case DT_MAT:
                        runtimeStack.peek().content = ((Matrix)runtimeStack.peek().content).negative();
                        break;
                    case DT_VEC:
                        runtimeStack.peek().content = ((vector)runtimeStack.peek().content).negative();
                        break;
                }
        }
    }
    static void BinaryOperation(int op) throws Exception {
        StringBuilder SB = new StringBuilder();
        Object B = Values.getValue(runtimeStack.peek());
        runtimeStack.pop();
        Object A = Values.getValue(runtimeStack.peek());
        runtimeStack.pop();
        switch (op){
            case OpCode.VALUE_SAME:
                runtimeStack.push(new RuntimeData(A.equals(B)?1:0, DataType.DT_NUMBER));
                break;
            case OpCode.ADD:
                if (A.getClass() == B.getClass()){
                    if (A instanceof Long)
                        runtimeStack.push(new RuntimeData((Long)A + (Long)B, DataType.DT_NUMBER));
                    else if (A instanceof Double)
                        runtimeStack.push(new RuntimeData((Double)A + (Double)B, DataType.DT_NUMBER));
                    else if (A instanceof String)
                        runtimeStack.push(new RuntimeData(A + (String)B, DataType.DT_LITERAL));
                    else if (A instanceof vector)
                        runtimeStack.push(new RuntimeData(((vector) A).add((vector) B), DataType.DT_VEC));
                    else if (A instanceof Matrix)
                        runtimeStack.push(new RuntimeData(((Matrix) A).add((Matrix) B), DataType.DT_MAT));
                }
                else if (A instanceof Number && B instanceof Number)
                    runtimeStack.push(new RuntimeData(
                            ((Number) A).doubleValue() + ((Number) B).doubleValue(),
                            DataType.DT_NUMBER)
                    );
                else
                    throw new Exception(Types.getInnerType(A)+" do not support operator '+' with "+Types.getInnerType(B));
                break;
            case OpCode.SUB:
                if (A.getClass() == B.getClass()){
                    if (A instanceof Long)
                        runtimeStack.push(new RuntimeData((Long)A - (Long)B, DataType.DT_NUMBER));
                    else if (A instanceof Double)
                        runtimeStack.push(new RuntimeData((Double)A - (Double)B, DataType.DT_NUMBER));
                    else if (A instanceof String)
                        throw new Exception("str do not support operator '-' with str");
                    else if (A instanceof vector)
                        runtimeStack.push(new RuntimeData(((vector) A).subtract((vector) B), DataType.DT_VEC));
                    else if (A instanceof Matrix)
                        runtimeStack.push(new RuntimeData(((Matrix) A).subtract((Matrix) B), DataType.DT_MAT));
                }
                else if (A instanceof Number && B instanceof Number)
                    runtimeStack.push(new RuntimeData(
                            ((Number) A).doubleValue() - ((Number) B).doubleValue(),
                            DataType.DT_NUMBER)
                    );
                else
                    throw new Exception(Types.getInnerType(A)+" do not support operator '+' with "+Types.getInnerType(B));
                break;
            case OpCode.MUL:
                if (A.getClass() == B.getClass()){
                    if (A instanceof Long)
                        runtimeStack.push(new RuntimeData((Long)A * (Long)B, DataType.DT_NUMBER));
                    else if (A instanceof Double)
                        runtimeStack.push(new RuntimeData((Double)A * (Double)B, DataType.DT_NUMBER));
                    else if (A instanceof String)
                        throw new Exception("str do not support operator '*' with str");
                    else if (A instanceof vector)
                        runtimeStack.push(new RuntimeData(((vector) A).outerMultiply((vector) B), DataType.DT_VEC));
                    else if (A instanceof Matrix)
                        runtimeStack.push(new RuntimeData(((Matrix) A).Multiply((Matrix) B), DataType.DT_MAT));
                }
                else if (A instanceof Number && B instanceof Number)
                    runtimeStack.push(new RuntimeData(
                            ((Number) A).doubleValue() * ((Number) B).doubleValue(),
                            DataType.DT_NUMBER)
                    );
                else if (A instanceof vector && B instanceof Number)
                    runtimeStack.push(new RuntimeData(
                            ((vector) A).Multiply(((Number) B).doubleValue()),
                            DataType.DT_VEC)
                    );
                else if (A instanceof String && B instanceof Long) {
                    SB.setLength((int) (((String) A).length() * (Long) B));
                    for (int i = 0; i < (Long) B; i++)
                        SB.append(A);
                    runtimeStack.push(new RuntimeData(SB.toString(), DataType.DT_LITERAL));
                    SB.delete(0, SB.length());
                }
                else
                    throw new Exception(Types.getInnerType(A)+" do not support operator '*' with "+Types.getInnerType(B));
                break;
            case OpCode.DIV:
                if (A.getClass() == B.getClass()){
                    if (A instanceof Long)
                        runtimeStack.push(new RuntimeData((Long)A / (Long)B, DataType.DT_NUMBER));
                    else if (A instanceof Double)
                        runtimeStack.push(new RuntimeData((Double)A / (Double)B, DataType.DT_NUMBER));
                    else if (A instanceof String)
                        throw new Exception("str do not support operator '/' with str");
                    else if (A instanceof vector)
                        throw new Exception("vector do not support operator '/' with vector");
                    else if (A instanceof Matrix)
                        runtimeStack.push(new RuntimeData(((Matrix) A).Multiply(((Matrix) B).inverseMatrix()), DataType.DT_MAT));
                }
                else if (A instanceof Number && B instanceof Number)
                    runtimeStack.push(new RuntimeData(
                            ((Number) A).doubleValue() / ((Number) B).doubleValue(),
                            DataType.DT_NUMBER)
                    );
                else if (A instanceof vector && B instanceof Number)
                    runtimeStack.push(new RuntimeData(
                            ((vector) A).div(((Number) B).doubleValue()),
                            DataType.DT_VEC)
                    );
                else
                    throw new Exception(Types.getInnerType(A)+" do not support operator '/' with "+Types.getInnerType(B));
                break;
            case OpCode.MOD:
                if (A instanceof Number && B instanceof Number){
                    if (A instanceof Long && B instanceof Long)
                        runtimeStack.push(new RuntimeData((Long)A % (Long)B, DataType.DT_NUMBER));
                    else if (A instanceof Double && B instanceof Double)
                        runtimeStack.push(new RuntimeData((Double)A % (Double)B, DataType.DT_NUMBER));
                    else
                        runtimeStack.push(new RuntimeData(
                                ((Number) A).doubleValue() % ((Number) B).doubleValue(),
                                DataType.DT_NUMBER)
                        );
                }
                else
                    throw new Exception("% only work at numberic values");
                break;
            case OpCode.DOT:
                if (A instanceof vector && B instanceof vector)
                    runtimeStack.push(new RuntimeData(
                            ((vector) A).innerMultiply((vector) B),
                            DataType.DT_NUMBER)
                    );
                else
                    throw new Exception("% only work at vectors");
                break;
        }
    }

    static void FunctionCall(int n) throws Exception{
        String func = (String) runtimeStack.peek().content;
        runtimeStack.pop();
        Object a=null,b=null,c=null,d=null;
        if (n > 0) {
            d = Values.getValue(runtimeStack.peek());
            runtimeStack.pop();
        }
        if (n > 1){
            c = Values.getValue(runtimeStack.peek());
            runtimeStack.pop();
        }
        if (n > 2){
            b = Values.getValue(runtimeStack.peek());
            runtimeStack.pop();
        }
        if (n > 3){
            a = Values.getValue(runtimeStack.peek());
            runtimeStack.pop();
        }

        switch (func){
            case "sqrt":
                if (n == 1) {
                    if (d instanceof Number) {
                        runtimeStack.push(new RuntimeData(sqrt(((Number) d).doubleValue()), DataType.DT_NUMBER));
                        return;
                    }
                    throw new Exception("absolute param type must be int or real");
                }
                else
                    throw new Exception("absolute param count must be 1");
            case "absolute":
                if (n == 1) {
                    if (d instanceof Number){
                        runtimeStack.push(new RuntimeData(abs(((Number) d).doubleValue()), DataType.DT_NUMBER));
                        return;
                    }
                    throw new Exception("absolute param type must be int or real");
                }
                else
                    throw new Exception("absolute param count must be 1");

            case "round":
                if (n == 1) {
                    if (d instanceof Number){
                        runtimeStack.push(new RuntimeData(round(((Number) d).doubleValue()), DataType.DT_NUMBER));
                        return;
                    }
                    throw new Exception("absolute param type must be int or real");
                }
                else
                    throw new Exception("absolute param count must be 1");
            case "norm":
                if (n == 2) {
                    if (c instanceof vector && d instanceof Long) {
                        runtimeStack.push(new RuntimeData(((vector) c).norm(((Long) d).intValue()), DataType.DT_NUMBER));
                        return;
                    }
                    else if (c instanceof Matrix && d instanceof Long) {
                        runtimeStack.push(new RuntimeData(((Matrix) c).norm(((Long) d).intValue()), DataType.DT_NUMBER));
                        return;
                    }
                    throw new Exception("norm params type must be (vector/matrix, int)");
                }
                else if (n == 1){
                    if (d instanceof vector) {
                        runtimeStack.push(new RuntimeData(((vector) d).norm(2), DataType.DT_NUMBER));
                        return;
                    }
                    else if (d instanceof Matrix) {
                        runtimeStack.push(new RuntimeData(((Matrix) d).norm(2), DataType.DT_NUMBER));
                        return;
                    }
                    throw new Exception("norm params type must be (vector/matrix, int)");
                }
                else
                    throw new Exception("absolute param count must be 2");
            case "power":
                if (n == 2) {
                    if (c instanceof Number && d instanceof Number) {
                        runtimeStack.push(new RuntimeData(pow(((Number) c).doubleValue(), ((Number) d).doubleValue()),
                                DataType.DT_NUMBER));
                        return;
                    }
                    else if (c instanceof Matrix && d instanceof Long){
                        runtimeStack.push(new RuntimeData(((Matrix) c).pow(((Long) d).intValue()),
                                DataType.DT_MAT));
                        return;
                    }
                    throw new Exception("power params type must be (matrix, int) or (int/real, int/real)");
                }
                else
                    throw new Exception("power param count must be 2");
            case "build":
                if (n == 2) {
                    if (c instanceof vector && d instanceof vector) {
                        runtimeStack.push(new RuntimeData(((vector) c).matrixMultiply((vector) d), DataType.DT_MAT));
                        return ;
                    }
                    throw new Exception("build params type must be (vector, vector)");
                }
                else
                    throw new Exception("build param count must be 2");
            case "summation":
                if (n == 1) {
                    if (d instanceof vector) {
                        runtimeStack.push(new RuntimeData(((vector) d).sum(), DataType.DT_NUMBER));
                        return ;
                    }
                    throw new Exception("summation param type must be vector");
                }
                else
                    throw new Exception("summation param count must be 1");
            case "average":
                if (n == 1) {
                    if (d instanceof vector){
                        runtimeStack.push(new RuntimeData(((vector) d).ave(), DataType.DT_NUMBER));
                        return ;
                    }
                    throw new Exception("average param type must be vector");
                }
                else
                    throw new Exception("average param count must be 1");
            case "variance":
                if (n == 1) {
                    if (d instanceof vector) {
                        runtimeStack.push(new RuntimeData(((vector) d).var(), DataType.DT_NUMBER));
                        return ;
                    }
                    throw new Exception("variance param type must be vector");
                }
                else
                    throw new Exception("variance param count must be 1");
            case "Triangle":
                if (n == 2) {
                    if (c instanceof Matrix && d instanceof String)
                        if (d.equals("U")) {
                            runtimeStack.push(new RuntimeData(((Matrix) c).toUpTriangle(), DataType.DT_MAT));
                            return ;
                        }
                        else if (d.equals("D")) {
                            runtimeStack.push(new RuntimeData(((Matrix) c).toDownTriangle(), DataType.DT_MAT));
                            return ;
                        }
                        else
                            throw new Exception("Triangle param 2 must be 'U' or 'D'");
                    throw new Exception("Triangle params types must be (matrix, str)");
                }
                else
                    throw new Exception("Triangle param count must be 2");
            case "Diagonal":
                if (n == 1) {
                    if (d instanceof Matrix) {
                        runtimeStack.push(new RuntimeData(((Matrix) d).toDiagonal(), DataType.DT_MAT));
                        return ;
                    }
                    throw new Exception("Diagonal param type must be matrix");
                }
                else
                    throw new Exception("Diagonal param count must be 1");
            case "Determinant":
                if (n == 1) {
                    if (d instanceof Matrix) {
                        runtimeStack.push(new RuntimeData(((Matrix) d).determinant(), DataType.DT_MAT));
                        return ;
                    }
                    throw new Exception("Determinant param type must be matrix");
                }
                else
                    throw new Exception("Determinant param count must be 1");
            case "Minor":
                if (n == 4 || n == 3) {
                    if (n == 3){
                        a = b;
                        b = c;
                        c = d;
                        d = "normal";
                    }
                    if (a instanceof Matrix && b instanceof Long && c instanceof Long &&
                            (d instanceof String)) {
                        if (d.equals("normal")) {
                            runtimeStack.push(new RuntimeData(
                                    ((Matrix) a).Minor(((Number) b).intValue(), ((Number) c).intValue()),
                                    DataType.DT_NUMBER));
                            return ;
                        }
                        else if (d.equals("complemental")){
                            runtimeStack.push(new RuntimeData(
                                    ((Matrix) a).complementalMinor(((Number) b).intValue(), ((Number) c).intValue()),
                                    DataType.DT_NUMBER));
                            return ;
                        }
                        else
                            throw new Exception("Minor param 4 must be \"normal\"(default) or \"complemental\"");
                    }
                    throw new Exception("Minor param types must be (matrix,int,int,str)");
                }
                else
                    throw new Exception("Minor param count must be 3 or 4, given "+n);
            case "Adjoint":
                if (n == 1) {
                    if (d instanceof Matrix) {
                        runtimeStack.push(new RuntimeData(((Matrix) d).adjointMatrix(), DataType.DT_MAT));
                        return ;
                    }
                    throw new Exception("Adjoint param type must be matrix");
                }
                else
                    throw new Exception("Adjoint param count must be 1");
            case "Inverse":
                if (n == 1) {
                    if (d instanceof Matrix) {
                        runtimeStack.push(new RuntimeData(((Matrix) d).inverseMatrix(), DataType.DT_MAT));
                        return ;
                    }
                    throw new Exception("Inverse param type must be matrix");
                }
                else
                    throw new Exception("Inverse param count must be 1");
            default:
                throw new Exception("no such BIF: "+func);
        }

    }

    static void Loop() throws Exception{
        Object times = runtimeStack.peek().content;
        runtimeStack.pop();

        String Body = (String) runtimeStack.peek().content;
        runtimeStack.pop();

        InputStream byteArrayInputStream = new ByteArrayInputStream(Body.getBytes(StandardCharsets.UTF_8));

        if (times instanceof Long)
            for (int i = 0; i < (Long)times; i++) {
                RunCode(byteArrayInputStream);
                byteArrayInputStream.reset();
            }
        else
            while (true) {
                RunCode(byteArrayInputStream);
                byteArrayInputStream.reset();
            }
    }
    static void Choice() throws Exception{
        Object cond = runtimeStack.peek().content;
        runtimeStack.pop();
        Object Y = runtimeStack.peek().content;
        runtimeStack.pop();
        Object N = runtimeStack.peek().content;
        runtimeStack.pop();
        if (cond instanceof Number && abs(((Number) cond).doubleValue()) > 1e-5)
            if (Y != null)
                RunCode(new ByteArrayInputStream(((String)Y).getBytes(StandardCharsets.UTF_8)));
        if (cond instanceof Number && abs(((Number) cond).doubleValue()) < 1e-5)
            if (N != null)
                RunCode(new ByteArrayInputStream(((String)N).getBytes(StandardCharsets.UTF_8)));
    }
    static void STORE_VALUE(InputStream bi) throws Exception{
        String name = Identifiers.readVarName_Ex(bi);
        System.out.println("left: "+name);
        Identifiers.storeValue(name, Values.getValue(runtimeStack.peek()));
    }

    static void RunCode(InputStream bi) throws Exception{
        //System.out.println("enter");
        int code, arg;
        int cnt = 0;
        StringBuilder SB = new StringBuilder();
        runtimeStack = new Stack<>();
        while(true) {
            code = bi.read();
            //System.out.printf("%x\n",code);
            if (cnt == 0)
            switch (code) {
                case -1:
                    return;

                case OpCode.BLOCK_BEGIN:
                    ++cnt;
                    break;

                case OpCode.POP_TOP:
                    runtimeStack.pop();
                    break;
                case OpCode.INPUT:
                    arg = bi.read();
                    Check(arg);
                    runInput(arg);
                    break;
                case OpCode.OUTPUT:
                    arg = bi.read();
                    Check(arg);
                    runOutput(arg);
                    break;
                case OpCode.DECLARE:
                    arg = bi.read();
                    Check(arg);
                    runDeclare(arg, bi);
                    break;
                case OpCode.TYPE_CAST:
                    runCast(bi);
                    break;
                case OpCode.POS:
                case OpCode.NEG:
                    SingleOperation(code);
                    break;
                case OpCode.ADD:
                case OpCode.SUB:
                case OpCode.MUL:
                case OpCode.DIV:
                case OpCode.MOD:
                case OpCode.DOT:
                case OpCode.VALUE_SAME:
                    BinaryOperation(code);
                    break;


                case OpCode.CALL:
                    arg = bi.read();
                    FunctionCall(arg);
                    break;
                case OpCode.SET_VALUE:
                    STORE_VALUE(bi);
                    break;
                case OpCode.HEAD_STR:
                case OpCode.HEAD_INT:
                case OpCode.HEAD_REA:
                case OpCode.HEAD_VAR:
//                case OpCode.HEAD_VEC:
//                case OpCode.HEAD_MAT:
                    runtimeStack.push(Values.store(code, bi));
                    break;
                case OpCode.SIGNAL_LOOP:
                    Loop();
                    break;
                case OpCode.SIGNAL_CHOICE:
                    Choice();
                    break;
                case OpCode.NULL_HEAD:
                    runtimeStack.push(new RuntimeData(null, DataType.CD_BLOCK));
            }

            else switch (code){
                case OpCode.BLOCK_END:
                    -- cnt;
                    if (cnt!= 0)
                        SB.append((char)code);
                    else
                        runtimeStack.push(new RuntimeData(SB.toString(), DataType.CD_BLOCK));
                    break;
                case OpCode.BLOCK_BEGIN:
                    ++cnt;
                default:
                    SB.append((char)code);
            }
        }

    }


    static void RunCore() throws Exception {
        RunCode(fi);
    }


    public static void Interpret(String filename) throws Exception {
        fi = new FileInputStream(filename);
        sc = new Scanner(System.in);
        CheckHead();
        RunCore();
    }
}
