package Abacus.Syntax.Tree.TreeBuilt;

import Abacus.Syntax.Token.Element;
import Abacus.Syntax.Token.ElementType;
import Abacus.Syntax.Tree.TreeElement.NodeType;
import jdk.nashorn.internal.runtime.regexp.joni.exception.SyntaxException;
import org.omg.CORBA.DynAnyPackage.Invalid;

public final class Rule{
    NodeType type;
    boolean flag;
    int cnt=0;

    public boolean initial(Element e) throws Exception {
        switch (e.getType()){
            case Command:
                switch (e.getName()){
                    case "input":
                        flag = true;
                        type = NodeType.Multiple_Input;
                        return true;
                    case "output":
                        type = NodeType.Multiple_Output;
                        return true;
                    default:
                        throw new Invalid("No such command type :"+e);
                }
            case Type:
                type = NodeType.Multiple_Declaration;
                return true;

            case Identifier:
            case Literal:
            case Number:
            case Operation:
            case BIF:
                type = NodeType.Multiple_Expression;
                if (e.getName().equals("(")) {
                    ++cnt;
                    return true;
                }
                else return !e.getName().equals(")");
            case StopPoint:
                return false;

            case Block_begin:
                cnt = 1;
                type = NodeType.Multiple_Block;
                return true;


            default:
                throw new Invalid("No such ele type :"+e);
        }
    }
    public boolean next(Element e) throws Invalid {
        switch (type){
            case Multiple_Input:
                if (e.getType() == ElementType.Identifier) {
                    flag = true;
                    return true;
                }
                else if(e.getType() == ElementType.Type && flag){
                    flag = false;
                    return true;
                }
                else if(e.getType() == ElementType.StopPoint){
                    return false;
                }
                else if (e.getType() == ElementType.Signal && e.getName().equals("paramSplit"))
                    return true;
                else throw new SyntaxException("[maybe]:Duplicate type declaration in input");

            case Multiple_Output:
                if (e.getType() == ElementType.Identifier ||
                    e.getType() == ElementType.Number ||
                    e.getType() == ElementType.Literal ||
                    e.getType() == ElementType.Type ||
                    e.getType() == ElementType.Operation ||
                    e.getType() == ElementType.BIF ||
                    e.getType() == ElementType.FunctionParamBegin||
                    e.getType() == ElementType.Signal)
                        return true;
                else if (e.getType() == ElementType.StopPoint)
                    return false;

                else
                    throw new  SyntaxException("Unprintable obj in output command:"+e);

            case Multiple_Declaration:
                if (e.getType() == ElementType.Identifier)
                    return true;
                else if (e.getType() == ElementType.StopPoint)
                    return false;
                else
                    throw new SyntaxException("Syntax: type id1,id2,id3..., Error"+e.getType());
            case Multiple_Expression:
                if (e.getType() == ElementType.Identifier ||
                        e.getType() == ElementType.Number ||
                        e.getType() == ElementType.Literal ||
                        e.getType() == ElementType.BIF)
                    return true;
                else if (e.getType() == ElementType.Operation){
                    if (e.getName().equals("(")) {
                        ++cnt;
                        return true;
                    }
                    else if (e.getName().equals(")"))
                        --cnt;
                    return cnt >=0;
                }
                else if (e.getType() ==ElementType.Signal && e.getName().equals("paramSplit"))
                    return cnt > 0;
                else if (e.getType() == ElementType.Command &&
                         e.getName().equals("inf"))
                    return true;
                else if (e.getType() == ElementType.StopPoint ||
                        (e.getType() ==ElementType.Signal && e.getName().equals("if")))
                    return false;
                else
                    throw new SyntaxException("Syntax: id1 op id2, error:"+e);

            case Multiple_Block:
                if (e.getType() == ElementType.Block_begin)
                    ++ cnt;
                else if (e.getType() == ElementType.Block_end)
                    -- cnt;
                return cnt != 0;
            case Multiple_If:
                return false;
            default:
                throw new Invalid("No such ele type "+type);
        }
    }
    public NodeType getType(){
        return type;
    }
}
