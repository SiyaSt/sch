package itmo.anastasiya;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class Compiler {
    private List<Instruction> instructions;

    public Compiler(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public void saveToFile(String filename) {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(filename))) {
            for (Instruction instr : instructions) {
                out.writeByte(instr.opCode.ordinal()); // Пишем код операции
                out.writeUTF(instr.operand1 != null ? instr.operand1 : ""); // Пишем первый операнд
                out.writeUTF(instr.operand2 != null ? instr.operand2.toString() : ""); // Пишем второй операнд
                out.writeUTF(instr.operand3 != null ? instr.operand3.toString() : ""); // Пишем третий операнд
            }
        } catch (IOException e) {
            throw new RuntimeException("Error saving bytecode to file: " + e.getMessage());
        }
    }

}

