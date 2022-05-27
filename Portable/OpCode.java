package Abacus.Portable;

public class OpCode {
    public static final char
    POP_TOP     =   0x00,
    INPUT       =   0x01,
    OUTPUT      =   0x02,
    DECLARE     =   0x03,
    TYPE_CAST   =   0x04,

    POS         =   0x05,
    NEG         =   0x06,

    ADD         =   0x07,
    SUB         =   0x08,
    MUL         =   0x09,
    DIV         =   0x0a,
    MOD         =   0x0b,
    DOT         =   0x0c,
    CALL        =   0x0d,

    SET_VALUE   =   0x0e,

    HEAD_STR    =   0x0f,
    HEAD_INT    =   0x10,
    HEAD_REA    =   0x11,
    HEAD_VEC    =   0x12,
    HEAD_MAT    =   0x13,

    HEAD_VAR    =   0x14,

    BLOCK_BEGIN =   0x15,
    BLOCK_END   =   0x16,

    SIGNAL_LOOP =   0x17,
    SIGNAL_CHOICE=  0x18,

    NULL_HEAD   =   0x19,

    VALUE_SAME  =   0x1a;
}
