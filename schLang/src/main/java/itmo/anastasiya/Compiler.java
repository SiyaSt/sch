package itmo.anastasiya;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class Compiler {
    private final List<Instruction> instructions;

    public Compiler(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public void saveToFile(String filename) {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(filename))) {
            for (Instruction instr : instructions) {
                writeInstruction(out, instr);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error saving bytecode to file: " + e.getMessage());
        }
    }

    private void writeInstruction(DataOutputStream out, Instruction instr) throws IOException {
        out.writeByte(instr.opCode.ordinal()); // Пишем код операции

        // Обработка различных типов инструкций
        switch (instr.opCode) {
            case FUN:
                // Для функции пишем имя и параметры
                out.writeUTF(instr.operand1 != null ? instr.operand1 : ""); // Имя функции

                // Запись списка параметров
                List<String> parameters = (List<String>) instr.operand2;
                out.writeInt(parameters.size());
                for (String param : parameters) {
                    out.writeUTF(param);
                }
                break;

            case RETURN:
                // Для возврата пишем возвращаемое значение
                out.writeUTF(instr.operand1 != null ? instr.operand1 : "");
                break;

            case IF:
                // Существующая логика для IF
                out.writeUTF(instr.operand1 != null ? instr.operand1 : "");
                out.writeUTF(instr.operand2 != null ? instr.operand2.toString() : ""); // OpCode сравнения
                out.writeUTF(instr.operand3 != null ? instr.operand3.toString() : "");

                // Запись блока инструкций
                if (instr.block != null) {
                    out.writeInt(instr.block.size());
                    for (Instruction blockInstr : instr.block) {
                        writeInstruction(out, blockInstr);
                    }
                }
                break;

            default:
                // Для остальных типов инструкций - существующая логика
                out.writeUTF(instr.operand1 != null ? instr.operand1 : "");
                out.writeUTF(instr.operand2 != null ? instr.operand2.toString() : "");
                out.writeUTF(instr.operand3 != null ? instr.operand3.toString() : "");
        }
    }

}

