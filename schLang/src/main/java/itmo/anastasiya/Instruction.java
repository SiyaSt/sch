package itmo.anastasiya;

class Instruction {
    public enum OpCode {
        STORE, PRINT, ARRAY
    }

    public OpCode opCode;
    public String operand1;
    public Integer operand2;
    public String varName;
    public Object value;

    public Instruction(OpCode opCode, String operand1) {
        this(opCode, operand1, null);
    }

    public Instruction(OpCode opCode, String operand1, Integer operand2) {
        this.opCode = opCode;
        this.operand1 = operand1;
        this.operand2 = operand2;
    }

    public Instruction(OpCode opCode, String varName, Object value) {
        this.opCode = opCode;
        this.varName = varName;
        this.value = value;
    }

    @Override
    public String toString() {
        return opCode + " " + operand1 + (operand2 != null ? " " + operand2 : "");
    }
}
