package itmo.anastasiya;

class Instruction {
    public enum OpCode {
        STORE, PRINT, ARRAY, ADD, SUB, MUL, LESS, GREATER, EQUALS, NOT_EQUALS, FUN, RETURN
        STORE, PRINT, ARRAY, ADD, SUB, MUL, LESS, GREATER, EQUALS, NOT_EQUALS, IF
    }

    public OpCode opCode;
    public String operand1;
    public Object operand2;
    public Object operand3;
    public List<String> parameters;


    public Instruction(OpCode opCode, String operand1, Object operand2) {
        this(opCode, operand1, operand2, null, null);
    }

    public Instruction(OpCode opCode, String operand1, Object operand2, Object operand3) {
        this(opCode, operand1, operand2, operand3, null);
    }

    public Instruction(OpCode opCode, String operand1, Object operand2, List<Instruction> blockInstructions) {
        this(opCode, operand1, operand2, null, blockInstructions);
    }

    public Instruction(OpCode opCode, String operand1, Object operand2, Object operand3, List<Instruction> blockInstructions) {
        this.opCode = opCode;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.operand3 = operand3;
        this.block = blockInstructions;
    }

    @Override
    public String toString() {
        return opCode + " " + operand1 +
                (operand2 != null ? " " + operand2 : "") +
                (operand3 != null ? " " + operand3 : "");
    }
}
