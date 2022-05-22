package Abacus.Syntax.Token;

public enum ElementType {
    Identifier,
    Command,
    Number,
    Operation,
    Literal,
    StopPoint,
    Type,

    Block_begin,
    Block_end,
    Block,

    FunctionParamBegin,
    Signal,
    BIF,
    FunctionCall
}
