package Abacus.Syntax.Tree.TreeElement;

public enum NodeType {
    Program_Root,

    Temp_Uncertain,

    Single_Number,
    Single_String,
    Single_Identifier,
    Single_Literal,
    Single_TypeCast,

    Multiple_Stmt,
    Multiple_Block,
    Multiple_Expression,
    Multiple_If,
    Multiple_Switch,
    Multiple_Input,
    Multiple_Output,
    Multiple_Filter,
    Multiple_Declaration,
    Multiple_FunctionCall,
    Multiple_Loop

}
