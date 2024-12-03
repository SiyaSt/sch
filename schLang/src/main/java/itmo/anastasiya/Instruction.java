package itmo.anastasiya;

import java.awt.*;
import java.util.List;

class Instruction {
    public enum OpCode {
        STORE, PRINT, ARRAY, ADD, SUB, MUL, LESS, GREATER, EQUALS, NOT_EQUALS, FUN, RETURN
    }

    public OpCode opCode;
    public String target;
    public String operand1;
    public Object operand2;
    public Object operand3;
    public List<String> parameters;


    // Full constructor for all cases
    public Instruction(OpCode opCode, String operand1, Object operand2, Object operand3, String target) {
        this.opCode = opCode;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.operand3 = operand3;
        this.target = target;
    }

    // Constructor for simple operations (store, print)
    public Instruction(OpCode opCode, String target) {
        this.opCode = opCode;
        this.target = target;
    }

    // Constructor for unary operations (like STORE with a single value)
    public Instruction(OpCode opCode, String target, String operand1) {
        this.opCode = opCode;
        this.target = target;
        this.operand1 = operand1;
    }

    // Constructor for binary operations (add, sub, mul, comparisons)
    public Instruction(OpCode opCode, String target, Object operand1, Object operand2) {
        this.opCode = opCode;
        this.target = target;
        this.operand1 = String.valueOf(operand1);
        this.operand2 = operand2;
    }

    // Constructor for function declarations
    public Instruction(OpCode opCode, String target, List<String> parameters) {
        this.opCode = opCode;
        this.target = target;
        this.parameters = parameters;
    }


    public Instruction(OpCode opCode, String target, Object operand1) {
        this.opCode = opCode;
        this.target = target;
        this.operand1 = String.valueOf(operand1);
    }

    /*
    @Override
    public String toString() {
        return opCode + " " + operand1 +
                (operand2 != null ? " " + operand2 : "") +
                (operand3 != null ? " " + operand3 : "");
    }*/

    @Override
    public String toString() {
        switch (opCode) {
            case PRINT:
                return "PRINT(" + target + ")";
            case STORE:
                return target + " = " + operand1;
            case ADD:
                return target + " = " + operand1 + " + " + operand2;
            case SUB:
                return target + " = " + operand1 + " - " + operand2;
            case MUL:
                return target + " = " + operand1 + " * " + operand2;
            case LESS:
                return target + " = " + operand1 + " < " + operand2;
            case GREATER:
                return target + " = " + operand1 + " > " + operand2;
            case EQUALS:
                return target + " = " + operand1 + " == " + operand2;
            case NOT_EQUALS:
                return target + " = " + operand1 + " != " + operand2;
            case FUN:
                return "FUNCTION " + target + "(" + parameters + ")";
            case RETURN:
                return "RETURN " + target;
            default:
                return "UNKNOWN INSTRUCTION";
        }
    }
}
