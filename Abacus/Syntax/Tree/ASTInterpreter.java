package Abacus.Syntax.Tree;

import Abacus.Syntax.Tree.TreeElement.NodeType;
import Abacus.Syntax.Tree.TreeElement.SyntaxTreeFork;
import Abacus.Syntax.Tree.TreeElement.SyntaxTreeLeaves;
import Abacus.Syntax.Tree.TreeElement.SyntaxTreeNode;
import jdk.nashorn.internal.runtime.regexp.joni.exception.SyntaxException;
import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;

import javax.naming.NameNotFoundException;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;

public class ASTInterpreter {
    static HashMap<String, String> typeTable = new HashMap<>();
    static HashMap<String, Long> varInt = new HashMap<>();
    static HashMap<String, Double> varReal = new HashMap<>();
    static HashMap<String, String> varStr = new HashMap<>();
    static Double evalNumber(SyntaxTreeNode node) throws Exception{
        SyntaxTreeNode left;
        if (node instanceof SyntaxTreeLeaves)
            switch (node.getType()) {
                case Single_Number:
                    return new Double(((SyntaxTreeLeaves) node).getElementName());
                case Single_Identifier:
                    if (typeTable.get(((SyntaxTreeLeaves) node).getElementName()).equals("int"))
                        return Double.valueOf(varInt.get(((SyntaxTreeLeaves) node).getElementName()));
                    else if (typeTable.get(((SyntaxTreeLeaves) node).getElementName()).equals("real"))
                        return varReal.get(((SyntaxTreeLeaves) node).getElementName());
                    else
                        throw new NameNotFoundException(
                                "Int var not found :" + ((SyntaxTreeLeaves) node).getElementName()
                        );
            }
        else if (node instanceof SyntaxTreeFork && node.getType() == NodeType.Multiple_Expression){
            switch (((SyntaxTreeFork) node).getBase().getName()){
                case "+":
                    if(((SyntaxTreeFork) node).getChildrenCount() > 1)
                        return evalNumber(((SyntaxTreeFork) node).at(0))
                                + evalNumber(((SyntaxTreeFork) node).at(1));
                    break;
                case "-":
                    if(((SyntaxTreeFork) node).getChildrenCount() > 1)
                        return evalNumber(((SyntaxTreeFork) node).at(0))
                                - evalNumber(((SyntaxTreeFork) node).at(1));
                    break;
                case "*":
                    if(((SyntaxTreeFork) node).getChildrenCount() > 1)
                        return evalNumber(((SyntaxTreeFork) node).at(0))
                                * evalNumber(((SyntaxTreeFork) node).at(1));
                    break;
                case "/":
                    if(((SyntaxTreeFork) node).getChildrenCount() > 1)
                        return evalNumber(((SyntaxTreeFork) node).at(0))
                                / evalNumber(((SyntaxTreeFork) node).at(1));
                    break;

                case "is":
                    if(((SyntaxTreeFork) node).getChildrenCount() > 1)
                        return Objects.equals(
                                evalNumber(((SyntaxTreeFork) node).at(0)),
                                evalNumber(((SyntaxTreeFork) node).at(1))
                        )? 1.:0;
                    break;

                case "%":
                    if(((SyntaxTreeFork) node).getChildrenCount() > 1)
                        return evalNumber(((SyntaxTreeFork) node).at(0))
                                % evalNumber(((SyntaxTreeFork) node).at(1));
                    break;
                case "+..":
                    if(((SyntaxTreeFork) node).getChildrenCount() == 1)
                        return evalNumber(((SyntaxTreeFork) node).at(0));
                    break;
                case "-..":
                    if(((SyntaxTreeFork) node).getChildrenCount() == 1)
                        return -evalNumber(((SyntaxTreeFork) node).at(0));
                    break;

                case "=":
                    left = ((SyntaxTreeFork) node).at(0);
                    if ((left instanceof SyntaxTreeLeaves)) {
                        if (!typeTable.containsKey(((SyntaxTreeLeaves) left).getElementName())) {
                            typeTable.put(((SyntaxTreeLeaves) left).getElementName(), "real");

                            varReal.put(((SyntaxTreeLeaves) left).getElementName(),
                                    evalNumber(((SyntaxTreeFork) node).at(1)));
                            return varReal.get(((SyntaxTreeLeaves) left).getElementName());
                        }
                        else switch(((SyntaxTreeLeaves) left).getElementName()){
                            case "str":
                                varStr.put(
                                        ((SyntaxTreeLeaves) left).getElementName(),
                                        evalNumber(((SyntaxTreeFork) node).at(1)).toString()
                                );
                                break;
                            case "int":
                                varInt.put(
                                        ((SyntaxTreeLeaves) left).getElementName(),
                                        Long.valueOf(evalNumber(((SyntaxTreeFork) node).at(1)).toString())
                                );
                                break;
                        }
                    }
                    throw new SyntaxException("Invalid assignment");
                default:
                    throw new SyntaxException("Invalid Operation");
            }
            throw new SyntaxException("Invalid Operation");
        }
        else
            throw new SyntaxException("Unknown Error");
        return 0.;
    }

    static void Output(SyntaxTreeFork STF){
        assert STF.getBase().getName().equals("output");
        for (SyntaxTreeNode stl:STF.kids()) {
            switch (stl.getType()) {
                case Single_Identifier:
                    switch (typeTable.get(((SyntaxTreeLeaves)stl).getElementName())){
                        case "str":
                            System.out.println(varStr.get(((SyntaxTreeLeaves)stl).getElementName()));
                            break;
                        case "int":
                            System.out.println(varInt.get(((SyntaxTreeLeaves)stl).getElementName()));
                            break;
                        case "real":
                            System.out.println(varReal.get(((SyntaxTreeLeaves)stl).getElementName()));
                            break;
                    }

                    break;
                case Single_Number:
                case Single_Literal:
                    System.out.println(((SyntaxTreeLeaves)stl).getElementName());
                    break;
                default:
                    throw new ValueException("unprintable type");
            }

        }
    }

    static void DeclareIdentifier(SyntaxTreeFork STF){
        String type = STF.getBase().getName();
        SyntaxTreeLeaves variable;
        for (SyntaxTreeNode stn:STF.kids()) {
            variable = (SyntaxTreeLeaves) stn;

            switch (type){
                case "int":
                    varInt.put(variable.getElementName(), 0L);
                    break;
                case "str":
                    varStr.put(variable.getElementName(), "Null");
                    break;
                case "real":
                    varReal.put(variable.getElementName(), 0D);
                    break;
                default:
                    throw new SyntaxException("Type do not support");
            }
            typeTable.put(variable.getElementName(), type);
        }
    }

    static void Input(SyntaxTreeFork STF){
        assert STF.getBase().getName().equals("input");
        Scanner sc = new Scanner(System.in);
        for (SyntaxTreeNode stl:STF.kids()) {
            switch (stl.getType()) {
                case Single_Identifier:
                    if (!typeTable.containsKey(((SyntaxTreeLeaves)stl).getElementName())){
                        typeTable.put(((SyntaxTreeLeaves)stl).getElementName(), "str");
                        varStr.put(((SyntaxTreeLeaves)stl).getElementName(), sc.nextLine());
                    }
                    else
                        switch (typeTable.get(((SyntaxTreeLeaves)stl).getElementName())){
                            case "str":
                                varStr.put(((SyntaxTreeLeaves)stl).getElementName(), sc.next());
                                break;
                            case "int":
                                varInt.put(((SyntaxTreeLeaves)stl).getElementName(), sc.nextLong());
                                break;
                            case "real":
                                varReal.put(((SyntaxTreeLeaves)stl).getElementName(), sc.nextDouble());
                                break;
                            default:
                                throw new SyntaxException("Type do not support");
                        }
                    break;
                case Multiple_Declaration:
                    assert stl instanceof SyntaxTreeFork;
                    DeclareIdentifier((SyntaxTreeFork) stl);
                    switch (((SyntaxTreeFork) stl).getBase().getName()){
                        case "int":
                            for (SyntaxTreeNode stn : ((SyntaxTreeFork) stl).kids()) {
                                SyntaxTreeLeaves id = (SyntaxTreeLeaves) stn;
                                typeTable.put(id.getElementName(), "int");
                                varInt.put(id.getElementName(), sc.nextLong());
                            }
                            break;
                        case "str":
                            for (SyntaxTreeNode stn : ((SyntaxTreeFork) stl).kids()) {
                                SyntaxTreeLeaves id = (SyntaxTreeLeaves) stn;
                                typeTable.put(id.getElementName(), "str");
                                varStr.put(id.getElementName(), sc.next());
                            }
                            break;
                        case "real":
                            for (SyntaxTreeNode stn : ((SyntaxTreeFork) stl).kids()) {
                                SyntaxTreeLeaves id = (SyntaxTreeLeaves) stn;
                                typeTable.put(id.getElementName(), "real");
                                varReal.put(id.getElementName(), sc.nextDouble());
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
    }

    public static boolean Run(SyntaxTreeFork root) throws Exception{
        for (SyntaxTreeNode stn :root.kids()) {
            switch (stn.getType()){
                case Multiple_Output:
                    Output((SyntaxTreeFork)stn);
                    break;
                case Multiple_Expression:
                    evalNumber(stn);
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
                             i < Integer.parseInt(((SyntaxTreeLeaves)stf.kids().lastElement()).getElementName());
                             ++i) {
                            Run((SyntaxTreeFork) stf.kids().firstElement());
                        }
                    else
                        while(Run((SyntaxTreeFork) stf.kids().firstElement()));
                    break;

                case Multiple_If:
                    runIf((SyntaxTreeFork) stn);
            }
        }
        return true;
    }

    public static void runIf(SyntaxTreeFork ifRoot) throws Exception{
        if(evalNumber(ifRoot.at(0)) == 1) {
            if (ifRoot.at(1) != null)
                switch (ifRoot.at(1).getType()){
                    case Multiple_Output:
                        Output((SyntaxTreeFork)ifRoot.at(1));
                        break;
                    case Multiple_Expression:
                        evalNumber(ifRoot.at(1));
                        break;
                    case Multiple_Declaration:
                        DeclareIdentifier((SyntaxTreeFork) ifRoot.at(1));
                        break;
                    case Multiple_Input:
                        Input((SyntaxTreeFork) ifRoot.at(1));
                        break;
                    case Multiple_Loop:
                        SyntaxTreeFork stf = (SyntaxTreeFork) ifRoot.at(1);
                        if (stf.kids().lastElement().getType() == NodeType.Single_Number)
                            for (int i = 0;
                                 i < Integer.parseInt(((SyntaxTreeLeaves)stf.kids().lastElement()).getElementName());
                                 ++i) {
                                Run((SyntaxTreeFork) stf.kids().firstElement());
                            }
                        else
                            while(Run((SyntaxTreeFork) stf.kids().firstElement()));
                        break;

                    case Multiple_If:
                        runIf((SyntaxTreeFork) ifRoot.at(1));
                }

        }
        else if (ifRoot.at(2) != null)
            switch (ifRoot.at(2).getType()){
                case Multiple_Output:
                    Output((SyntaxTreeFork)ifRoot.at(2));
                    break;
                case Multiple_Expression:
                    evalNumber(ifRoot.at(2));
                    break;
                case Multiple_Declaration:
                    DeclareIdentifier((SyntaxTreeFork) ifRoot.at(2));
                    break;
                case Multiple_Input:
                    Input((SyntaxTreeFork) ifRoot.at(2));
                    break;
                case Multiple_Loop:
                    SyntaxTreeFork stf = (SyntaxTreeFork) ifRoot.at(2);
                    if (stf.kids().lastElement().getType() == NodeType.Single_Number)
                        for (int i = 0;
                             i < Integer.parseInt(((SyntaxTreeLeaves)stf.kids().lastElement()).getElementName());
                             ++i) {
                            Run((SyntaxTreeFork) stf.kids().firstElement());
                        }
                    else
                        while(Run((SyntaxTreeFork) stf.kids().firstElement()));
                    break;

                case Multiple_If:
                    runIf((SyntaxTreeFork) ifRoot.at(2));
            }
    }
}
