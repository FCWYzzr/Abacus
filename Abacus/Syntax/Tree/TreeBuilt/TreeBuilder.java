package Abacus.Syntax.Tree.TreeBuilt;

import Abacus.Syntax.Token.Element;
import Abacus.Syntax.Token.ElementType;
import Abacus.Syntax.Tree.TreeElement.NodeType;
import Abacus.Syntax.Tree.TreeElement.SyntaxTreeFork;
import Abacus.Syntax.Tree.TreeElement.SyntaxTreeLeaves;
import Abacus.Syntax.Tree.TreeElement.SyntaxTreeNode;
import jdk.nashorn.internal.runtime.regexp.joni.exception.SyntaxException;

import java.util.*;

class Utils{
    static HashMap<String, Integer> level;
    static void init(){
        level=new HashMap<>();
        level.put("=",0);

        level.put("is",1);

        level.put("+",2);
        level.put("-",2);

        level.put("*",3);
        level.put("/",3);
        level.put("%",3);

        level.put("+..", 4);
        level.put("-..", 4);

        level.put("(..", 0);
        level.put("paramSplit", -1);
    }
    static boolean greater_than(String a, String b) throws SyntaxException{
        if (level.containsKey(a) && level.containsKey(b))
            return level.get(a) > level.get(b);
        throw new SyntaxException("Invalid Expression: "+a+" or "+b);
    }


    static SyntaxTreeLeaves CastElement(Element e) throws Exception {
        switch (e.getType()){
            case Identifier:
                return new SyntaxTreeLeaves(e, NodeType.Single_Identifier);
            case Number:
                return new SyntaxTreeLeaves(e, NodeType.Single_Number);
            case Literal:
                return new SyntaxTreeLeaves(e, NodeType.Single_Literal);
            default:
                throw new Exception("Can not be cast");
        }
    }
}

public class TreeBuilder {
    public static SyntaxTreeFork Build(Vector<Element> Tokens, int Begin, int End, NodeType type) throws Exception {
        Utils.init();

        boolean isFirst=true;
        int b=0;
        boolean if_flag=false, Y_flag=false, N_flag=false;
        SyntaxTreeFork Root = new SyntaxTreeFork(Element.Block,type);
        SyntaxTreeFork tmp, LastIf=null;
        SyntaxTreeNode tmp1;

        Rule rule = new Rule();
        while (Begin != End) {
            if (isFirst) {
                switch (Tokens.get(Begin).getName()){
                    case "if":
                        if_flag = true;
                        LastIf = new SyntaxTreeFork(Element.IfBegin, NodeType.Multiple_If);
                        LastIf.push(Root.kids().lastElement());
                        LastIf.push(null);
                        LastIf.push(null);
                        Root.kids().set(Root.getChildrenCount()-1, LastIf);
                        break;
                    case "if true":
                        Y_flag = if_flag;
                        break;
                    case "if false":
                        N_flag = if_flag;
                        break;
                    default:
                        if (rule.initial(Tokens.get(Begin))) {
                            isFirst = false;
                            b = Begin;
                        }
                }
                ++Begin;
            }
            else if (rule.next(Tokens.get(Begin))) {
                ++Begin;
            }
            else {
                switch (rule.getType()) {
                    case Multiple_Input:
                        Root.push(BuildInput(Tokens, b, Begin));
                        break;
                    case Multiple_Declaration:
                        Root.push(BuildDeclaration(Tokens, b, Begin));
                        break;
                    case Multiple_Output:
                        Root.push(BuildOutput(Tokens, b, Begin));
                        break;
                    case Multiple_Expression:
                        if (Root.getChildrenCount() > 0 &&
                                Root.kids().lastElement().getType() == NodeType.Multiple_Block &&
                                b+2 == Begin && Tokens.get(b).getName().equals("*") &&
                                (Tokens.get(b + 1).getType() == ElementType.Number ||
                                Tokens.get(b + 1).getType() == ElementType.Command)) {
                            tmp = new SyntaxTreeFork(Element.Loop, NodeType.Multiple_Loop);
                            tmp.push(Root.kids().lastElement());
                            Root.kids().remove(Root.getChildrenCount()-1);
                            tmp.push(new SyntaxTreeLeaves(Tokens.get(Begin-1),
                                    Tokens.get(Begin-1).getType() == ElementType.Number?
                                    NodeType.Single_Number: NodeType.Single_Literal));
                            Root.push(tmp);
                        }
                        else if (Tokens.get(b).getType() == ElementType.Identifier ||
                                Tokens.get(b).getType() == ElementType.Number ||
                                Tokens.get(b).getType() == ElementType.BIF ||
                                Tokens.get(b).getName().equals("+") ||
                                Tokens.get(b).getName().equals("-"))
                            Root.push(BuildExpression(Tokens, b, Begin));
                        else
                            throw new Exception("Unknown operation:" + Tokens.subList(b, Begin));

                        break;
                    case Multiple_Block:
                        Root.push(Build(Tokens, b+1, Begin++, NodeType.Multiple_Block));
                        break;

                    default:
                        throw new SyntaxException("Syntax not implement "+rule.getType());
                }
                isFirst = true;
                if (Y_flag){
                    Y_flag = false;
                    tmp1 = Root.kids().lastElement();
                    Root.kids().remove(Root.getChildrenCount()-1);
                    LastIf.kids().set(1, tmp1);
                    if (LastIf.kids().get(2)!= null)
                        if_flag = false;

                }
                else if (N_flag){
                    N_flag = false;
                    tmp1 = Root.kids().lastElement();
                    Root.kids().remove(Root.getChildrenCount()-1);
                    LastIf.kids().set(2, tmp1);
                    if (LastIf.kids().get(1)!= null)
                        if_flag = false;

                }
            }
        }
        if (!isFirst) {
            switch (rule.getType()) {
                case Multiple_Input:
                    Root.push(BuildInput(Tokens, b, Begin));
                    break;
                case Multiple_Declaration:
                    Root.push(BuildDeclaration(Tokens, b, End));
                    break;
                case Multiple_Output:
                    Root.push(BuildOutput(Tokens, b, End));
                    break;
                case Multiple_Expression:
                    if (Root.getChildrenCount() > 0 &&
                            (Root.kids().lastElement().getType() == NodeType.Multiple_Block ||
                                    Root.kids().lastElement().getType() == NodeType.Multiple_Loop) &&
                            b + 3 == Begin && Tokens.get(b).getName().equals("*") &&
                            (Tokens.get(b + 1).getType() == ElementType.Number ||
                                    Tokens.get(b + 1).getType() == ElementType.Command)) {
                        tmp = new SyntaxTreeFork(Element.Loop, NodeType.Multiple_Loop);
                        tmp.push(Root.kids().lastElement());
                        Root.kids().remove(Root.getChildrenCount() - 1);
                        tmp.push(new SyntaxTreeLeaves(Tokens.get(Begin - 1),
                                Tokens.get(b + 1).getType() == ElementType.Number ?
                                        NodeType.Single_Number : NodeType.Single_Literal));
                        Root.push(tmp);
                    } else if (Tokens.get(b).getType() == ElementType.Identifier ||
                            Tokens.get(b).getType() == ElementType.Number ||
                            Tokens.get(b).getName().equals("+") ||
                            Tokens.get(b).getName().equals("-"))
                        Root.push(BuildExpression(Tokens, b, End));
                    else
                        throw new Exception("Unknown operation:" + Tokens.subList(b, End));
                    break;
                case Multiple_Block:
                    Root.push(Build(Tokens, b + 1, End, NodeType.Multiple_Block));
                    break;
                default:
                    throw new SyntaxException("Syntax not implement " + rule.getType());
            }
            if (Y_flag){
                tmp1 = Root.kids().lastElement();
                Root.kids().remove(Root.getChildrenCount()-1);
                LastIf.kids().set(1, tmp1);

            }
            else if (N_flag){
                tmp1 = Root.kids().lastElement();
                Root.kids().remove(Root.getChildrenCount()-1);
                LastIf.kids().set(2, tmp1);
            }
        }
        return Root;
    }
    public static SyntaxTreeNode BuildDeclaration(Vector<Element> Tokens, int Begin, int End){
        SyntaxTreeFork Declaration = new SyntaxTreeFork(Tokens.get(Begin ++), NodeType.Multiple_Declaration);
        for (; Begin != End; ++ Begin) {
            Declaration.push(new SyntaxTreeLeaves(Tokens.get(Begin), NodeType.Single_Identifier));
        }
        return Declaration;
    }
    public static SyntaxTreeNode BuildExpression(Vector<Element> Tokens, int Begin, int End) throws Exception {
        //System.out.println(Tokens.subList(Begin,End));
        Stack<Element> Operator = new Stack<>();
        Deque<Element> Suffix = new LinkedList<>();
        boolean lastIsOp = true;

        while (Begin != End) {
            //System.out.println(Operator);
            if (Tokens.get(Begin).getType() == ElementType.Identifier ||
                Tokens.get(Begin).getType() == ElementType.Number ||
                Tokens.get(Begin).getType() == ElementType.BIF) {
                Suffix.offer(Tokens.get(Begin++));
                lastIsOp = false;
            }
            else{
                String op = Tokens.get(Begin).getName();
                switch (op) {
                    case "inf":
                        throw new Exception("inf could not use in normal expression");
                    case "(":
                        if (lastIsOp)
                            Operator.push(Tokens.get(Begin));
                        else
                            Operator.push(Element.paramBegin);
                        lastIsOp = true;
                        break;
                    case "paramSplit":
                        while (!Operator.peek().getName().equals("(") &&
                                !Operator.peek().getName().equals("(..")) {
                            Suffix.offer(Operator.peek());
                            Operator.pop();
                        }
                        Suffix.offer(Tokens.get(Begin));
                        lastIsOp = true;
                        break;
                    case ")":
                        while (!Operator.peek().getName().equals("(") &&
                                !Operator.peek().getName().equals("(..")) {
                            Suffix.offer(Operator.peek());
                            Operator.pop();
                        }
                        if (Operator.peek().getName().equals("(")) {
                            lastIsOp = false;
                            Operator.pop();
                        } else if (Operator.peek().getName().equals("(..")) {
                            lastIsOp = false;
                            Operator.pop();
                            Suffix.offer(Element.paramBegin);
                        }
                        break;
                    default:
                        if (lastIsOp && (op.equals("+") || op.equals("-")))
                            Tokens.get(Begin).ExtendName("..");
                        while (!Operator.empty()
                                && !(Operator.peek().getName().equals("(") || Operator.peek().getName().equals("(.."))
                                && !Utils.greater_than(op, Operator.peek().getName())) {
                            Suffix.offer(Operator.peek());
                            Operator.pop();
                        }
                        lastIsOp = true;
                        Operator.push(Tokens.get(Begin));
                        break;
                }
                ++ Begin;
            }
        }
        while (!Operator.empty()){
            Suffix.offer(Operator.peek());
            Operator.pop();
        }

        //System.out.println(Suffix);
        return BuildExpressionFromSuffix(Suffix);
    }
    private static SyntaxTreeNode BuildExpressionFromSuffix(Queue<Element> Suffix){
        Stack<SyntaxTreeNode> NodeStack = new Stack<>();
        SyntaxTreeNode tmp1, tmp2;
        SyntaxTreeFork tmp3;
        Element top;
        while(!Suffix.isEmpty()) {
            top = Suffix.poll();
            switch (top.getType()){
                case Number:
                    NodeStack.push(new SyntaxTreeLeaves(top, NodeType.Single_Number));
                    break;
                case Identifier:
                    NodeStack.push(new SyntaxTreeLeaves(top, NodeType.Single_Identifier));
                    break;
                case Literal:
                    NodeStack.push(new SyntaxTreeLeaves(top, NodeType.Single_Literal));
                    break;
                case BIF:
                    NodeStack.push(new SyntaxTreeFork(top, NodeType.Multiple_FunctionCall));
                    break;
                case Signal:
                    if (top.getName().equals("paramSplit")){
                        tmp1 = NodeStack.peek();
                        NodeStack.pop();
                        NodeStack.peek().Fork().push(tmp1);
                    }
                    break;
                case FunctionParamBegin:
                    if (top.getType() != ElementType.FunctionCall){
                        tmp1 = NodeStack.peek();
                        NodeStack.pop();
                        NodeStack.peek().Fork().push(tmp1);
                    }
                    break;
                case Operation:
                    switch (top.getName()) {
                        case "+..":
                        case "-..":
                            tmp1 = NodeStack.peek();
                            NodeStack.pop();
                            tmp3 = new SyntaxTreeFork(top, NodeType.Multiple_Expression);
                            tmp3.push(tmp1);
                            NodeStack.push(tmp3);
                            break;

                        default:
                            tmp1 = NodeStack.peek();
                            NodeStack.pop();
                            tmp2 = NodeStack.peek();
                            NodeStack.pop();
                            tmp3 = new SyntaxTreeFork(top, NodeType.Multiple_Expression);
                            tmp3.push(tmp2);
                            tmp3.push(tmp1);
                            NodeStack.push(tmp3);
                            break;
                    }
                    break;

                default:
                    throw new SyntaxException("Invalid Expression: "+top);
            }
        }
        assert NodeStack.size() == 1;
        return NodeStack.peek();
    }

    public static SyntaxTreeNode BuildInput(Vector<Element> Tokens, int Begin, int End) throws SyntaxException{
        SyntaxTreeFork Input = new SyntaxTreeFork(Tokens.get(Begin ++),NodeType.Multiple_Input);
        while (Begin != End)
            if (Tokens.get(Begin).getType() == ElementType.Signal)
                ++ Begin;
            else if (Tokens.get(Begin).getType() == ElementType.Type){
                Input.push(BuildDeclaration(Tokens, Begin, Begin + 2));
                Begin += 2;
            }
            else if (Tokens.get(Begin).getType() == ElementType.Identifier){
                Input.push(new SyntaxTreeLeaves(Tokens.get(Begin), NodeType.Single_Identifier));
                ++Begin;
            }
            else
                throw new SyntaxException("Input Syntax not allow type:"+Tokens.get(Begin).getType());
        return Input;
    }
    public static SyntaxTreeNode BuildOutput(Vector<Element> Tokens, int Begin, final int End) throws Exception {
        SyntaxTreeFork Output = new SyntaxTreeFork(Tokens.get(Begin ++),NodeType.Multiple_Output);
        boolean toBeCast=false;
        Rule rule = new Rule();
        int b=Begin;
        boolean isFirst=true;

        if (End - Begin == 1) {
            switch (Tokens.get(Begin).getType()) {
                case Identifier:
                    Output.push(new SyntaxTreeLeaves(Tokens.get(Begin), NodeType.Single_Identifier));
                    break;
                case Literal:
                    Output.push(new SyntaxTreeLeaves(Tokens.get(Begin), NodeType.Single_Literal));
                    break;
                case Number:
                    Output.push(new SyntaxTreeLeaves(Tokens.get(Begin), NodeType.Single_Number));
                    break;
                case Signal:
                    break;
            }
            return Output;
        }

        while (Begin != End) {
            if(isFirst){
                if (Tokens.get(Begin).getType() == ElementType.Type) {
                    toBeCast = true;
                    b = Begin;
                    isFirst = false;
                }
                else if (Tokens.get(Begin).getType() != ElementType.Signal){
                    if (rule.initial(Tokens.get(Begin))) {
                        b = Begin;
                        isFirst = false;
                    }
                }
                ++Begin;
                if(toBeCast)rule.initial(Tokens.get(Begin));
            }
            else if (rule.next(Tokens.get(Begin))){
                ++Begin;
            }
            else if(Begin == b + 1) {
                isFirst = true;
                switch (Tokens.get(Begin).getType()) {
                    case Identifier:
                        Output.push(new SyntaxTreeLeaves(Tokens.get(Begin++), NodeType.Single_Identifier));
                        break;
                    case Literal:
                        Output.push(new SyntaxTreeLeaves(Tokens.get(Begin++), NodeType.Single_Literal));
                        break;
                    case Number:
                        Output.push(new SyntaxTreeLeaves(Tokens.get(Begin++), NodeType.Single_Number));
                        break;

                }
            }
            else {
                isFirst = true;
                if(toBeCast) {
                    toBeCast = false;
                    //System.out.println(Tokens.subList(b, Begin));
                    Output.push(TypeCast(Tokens, b, Begin));
                }
                else{
                    //System.out.println(Tokens.subList(b,Begin));
                    Output.push(BuildExpression(Tokens, b, Begin));
                }
            }
        }
        if(!isFirst){
            if(Begin == b + 1) {
                switch (Tokens.get(Begin).getType()) {
                    case Identifier:
                        Output.push(new SyntaxTreeLeaves(Tokens.get(Begin+1), NodeType.Single_Identifier));
                        break;
                    case Literal:
                        Output.push(new SyntaxTreeLeaves(Tokens.get(Begin+1), NodeType.Single_Literal));
                        break;
                    case Number:
                        Output.push(new SyntaxTreeLeaves(Tokens.get(Begin+1), NodeType.Single_Number));
                        break;
                    case Signal:
                        break;
                }
            }
            else if(toBeCast) {
                Output.push(TypeCast(Tokens, b, Begin));
            }
            else {
                //System.out.println(Tokens.subList(b,Begin));
                Output.push(BuildExpression(Tokens, b, Begin));
            }

        }
        return Output;
    }

    public static SyntaxTreeFork TypeCast(Vector<Element> Tokens, int Begin, final int End) throws Exception {
        SyntaxTreeFork cast = new SyntaxTreeFork(Tokens.get(Begin++), NodeType.Single_TypeCast);
        //System.out.println(Tokens.subList(Begin,End));
        if (Begin == End - 1)
            cast.push(Utils.CastElement(Tokens.get(Begin)));
        else
            cast.push(BuildExpression(Tokens, Begin, End));
        return cast;
    }
}