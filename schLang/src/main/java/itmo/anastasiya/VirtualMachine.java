package itmo.anastasiya;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VirtualMachine {
    private List<Instruction> instructions = new ArrayList<>();
    private Map<String, Integer> variables = new HashMap<>();

    public void loadFromFile(String filename) {
        try (DataInputStream in = new DataInputStream(new FileInputStream(filename))) {
            while (in.available() > 0) {
                int opCodeOrdinal = in.readByte();
                String operand1 = in.readUTF();
                int operand2 = in.readInt();
                Instruction.OpCode opCode = Instruction.OpCode.values()[opCodeOrdinal];
                instructions.add(new Instruction(opCode, operand1, operand2));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading bytecode from file: " + e.getMessage());
        }
    }

    public void run() {
        for (Instruction instruction : instructions) {
            switch (instruction.opCode) {
                case STORE -> variables.put(instruction.operand1, instruction.operand2);
                case PRINT -> {
                    if (variables.containsKey(instruction.operand1)) {
                        System.out.println(variables.get(instruction.operand1));
                    } else {
                        throw new RuntimeException("Variable not found: " + instruction.operand1);
                    }
                }
                default -> throw new RuntimeException("Unknown instruction: " + instruction.opCode);
            }
        }
    }
}
