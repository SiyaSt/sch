package itmo.anastasiya;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Compiler {
    private final List<Instruction> instructions;
    private final Map<String, Instruction> functions = new HashMap<>();
    private final Map<String, Integer> variableIndexes = new HashMap<>(); // Map to store variable indexes
    private int nextVariableIndex = 0;

    public Compiler(List<Instruction> instructions) {
        this.instructions = instructions;
    }


    public void saveToFile(String filename) {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(filename))) {

            List<Instruction> optimizedInstructions = preprocessInstructions(instructions);
            for (Instruction instr : optimizedInstructions) {
                writeInstruction(out, instr);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error saving bytecode to file: " + e.getMessage());
        }
    }


    private List<Instruction> preprocessInstructions(List<Instruction> instructions) {
        List<Instruction> preprocessedInstructions = new ArrayList<>();
        for (Instruction instruction : instructions) {
            List<Instruction> processedBlock;
            switch (instruction.opCode) {
                case FUN -> {
                    functions.put(instruction.operand1, instruction);
                    processedBlock = preprocessInstructions(instruction.block);
                    preprocessedInstructions.add(Instruction.FunctionInstruction(instruction.operand1, instruction.parameters, processedBlock));
                }
                case IF -> {
                    if (instruction.block != null) {
                        processedBlock = preprocessInstructions(instruction.block);
                        instruction.block = processedBlock;
                    }
                    preprocessedInstructions.add(instruction);
                }
                case LOOP -> {
                    if (instruction.block != null) {
                        processedBlock = preprocessInstructions(instruction.block);
                        instruction.block = processedBlock;
                        preprocessedInstructions.add(compileLoop(instruction));
                    } else {
                        preprocessedInstructions.add(compileLoop(instruction));
                    }
                }
                case STORE -> {
                    instruction.target =  instruction.operand1;

                    preprocessedInstructions.add(instruction);

                }
                case PRINT -> {

                    preprocessedInstructions.add(instruction);
                }
                case ADD, SUB, MUL, MOD -> {
                    instruction.target =  instruction.operand1;

                    preprocessedInstructions.add(instruction);
                }
                case LESS, GREATER, EQUALS, NOT_EQUALS -> {
                    instruction.target =  instruction.operand1;
                    preprocessedInstructions.add(instruction);

                }
                case NEW -> {
                    instruction.target =  instruction.operand1;

                    preprocessedInstructions.add(instruction);

                }
                case WRITE_INDEX -> {
                    instruction.target =  instruction.operand1;
                    preprocessedInstructions.add(instruction);
                }
                case STORE_ARRAY_VAR -> {
                    instruction.target =  (String) instruction.operand3;
                    preprocessedInstructions.add(instruction);
                }
                case READ_INDEX -> {
                    preprocessedInstructions.add(instruction);
                }
                case CALL -> {
                    preprocessedInstructions.add(instruction);
                }
                case RETURN -> {
                    preprocessedInstructions.add(instruction);
                }
                default -> preprocessedInstructions.add(instruction);
            }

        }
        return preprocessedInstructions;
    }

    private int getVariableIndex(String variableName) {
        return variableIndexes.computeIfAbsent(variableName, k -> nextVariableIndex++);
    }

    private Instruction compileLoop(Instruction loopInstruction) {
        return  new Instruction(
                Instruction.OpCode.LOOP,
                loopInstruction.operand1,
                loopInstruction.operand2,
                loopInstruction.operand3,
                loopInstruction.block
        );
    }

    private void writeInstruction(DataOutputStream out, Instruction instr) throws IOException {
        out.writeByte(instr.opCode.ordinal()); // Пишем код операции

        // Обработка различных типов инструкций
        switch (instr.opCode) {
            case FUN:
                out.writeUTF(instr.operand1 != null ? instr.operand1 : "");

                // Запись списка параметров
                List<String> parameters = instr.parameters;
                out.writeInt(parameters.size());
                for (String param : parameters) {
                    out.writeUTF(param);
                }
                out.writeInt(instr.block.size());
                for (Instruction blockInstr : instr.block) {
                    writeInstruction(out, blockInstr);
                }
                break;

            case RETURN:
                // Для возврата пишем возвращаемое значение
                out.writeUTF(instr.operand1 != null ? instr.operand1 : "");

                if (instr.operand2 != null) {
                    writeInstruction(out, (Instruction) instr.operand2);
                }
                break;
            case IF:
                // Существующая логика для IF
                out.writeUTF(instr.operand1 != null ? instr.operand1 : "");
                out.writeUTF(instr.operand2 != null ? instr.operand2.toString() : ""); // OpCode сравнения
                out.writeUTF(instr.operand3 != null ? instr.operand3.toString() : "");

                if (instr.block != null) {
                    out.writeInt(instr.block.size());
                    for (Instruction blockInstr : instr.block) {
                        writeInstruction(out, blockInstr);
                    }
                }
                break;
            case LOOP:
                // Существующая логика для Loop
                out.writeUTF(instr.operand1 != null ? instr.operand1 : "");
                out.writeUTF(instr.operand2 != null ? instr.operand2.toString() : ""); // OpCode сравнения
                out.writeUTF(instr.operand3 != null ? instr.operand3.toString() : "");

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