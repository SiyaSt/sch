package itmo.anastasiya;

import java.util.List;

public class Instruction {
    public enum OpCode {
        STORE, PRINT, ARRAY, ADD, SUB, MUL, LESS, GREATER, EQUALS, NOT_EQUALS, IF, LOOP, FUN, RETURN, NEW, READ_INDEX,
        WRITE_INDEX, STORE_ARRAY_VAR, CALL, MOD
    }

    public OpCode opCode;
    public String operand1;
    public Object operand2;
    public Object operand3;
    public List<Instruction> block;
    // используется для того, чтобы хранить имя переменной, которая будет результатом операции.
    public String target;
    // add parameters to для хранения списка параметров при объявлении функции
    public List<String> parameters;

    // Constructor for simple operations (store, print)
    public Instruction(OpCode opCode, String operand1) {
        this(opCode, operand1, null, null, null);
    }

    public Instruction(OpCode opCode, String operand1, Object operand2) {
        this(opCode, operand1, operand2, null, null);
    }

    // Constructor for unary operations (like STORE with a single value)
    public Instruction(OpCode opCode, String target, String operand1) {
        this.opCode = opCode;
        this.target = target;
        this.operand1 = operand1;
    }

    // Constructor for binary operations (add, sub, mul, comparisons)
    public Instruction(OpCode opCode, String operand1, Object operand2, Object operand3) {
        this(opCode, operand1, operand2, operand3, null);
    }

    public Instruction(OpCode opCode, String operand1, Object operand2, List<Instruction> blockInstructions) {
        this(opCode, operand1, operand2, null, blockInstructions);
    }


    // Constructor for function declarations
    public Instruction(OpCode opCode, String operand1, List<String> parameters) {
        this.opCode = opCode;
        this.operand1 = operand1;
        this.parameters = parameters;
    }

    public Instruction(OpCode opCode, String operand1, Object operand2, Object operand3, List<Instruction> blockInstructions) {
        this.opCode = opCode;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.operand3 = operand3;
        this.block = blockInstructions;
    }

    public Instruction(OpCode opCode, String operand1, List<Instruction> blockInstructions, List<String> parameters) {
        this.opCode = opCode;
        this.operand1 = operand1;
        this.block = blockInstructions;
        this.parameters = parameters;
    }

    public Instruction() {}


    public static Instruction FunctionInstruction(String functionName, List<String> parameters, List<Instruction> instructions) {
        Instruction newInstruction =  new Instruction();
        newInstruction.opCode = Instruction.OpCode.FUN;
        newInstruction.operand1 = functionName;
        newInstruction.block = instructions;
        newInstruction.parameters = parameters;
        return newInstruction;
    }

    //может использовать string builder? Так как будто более безопасно
    @Override
    public String toString() {
        return opCode + " " + operand1 +
                (operand2 != null ? " " + operand2 : "") +
                (operand3 != null ? " " + operand3 : "") +
                (block != null ? " " + block : "");
    }
}