package itmo.anastasiya;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Compiler {
    private final List<Instruction> instructions;
    private final Map<String, Instruction> functions = new HashMap<>();
    private final Map<String, Long> variableIndexes = new HashMap<>();
    private long nextVariableIndex = 0;

    private final Set<String> usedVariables = new HashSet<>();

    public Compiler(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public void saveToFile(String filename) {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(filename))) {
            List<Instruction> filteredInstructions = reverseList(filterDeadCode(preprocessInstructions(instructions)));
            List<Instruction> optimizedInstructions = optimizeInstructions(filteredInstructions);


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
                case ADD, SUB, MUL, MOD, BITWISE_SHIFT -> {
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


    private long getVariableIndex(String variableName) {
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

    private List<Instruction> optimizeInstructions(List<Instruction> instructions) {
        List<Instruction> optimizedInstructions = new ArrayList<>();
        for (long i = 0; i < instructions.size(); i++) {
            Instruction current = instructions.get(Math.toIntExact(i));

            if (i + 1 < instructions.size()) {
                Instruction next = instructions.get(Math.toIntExact(i + 1));

                //  устранение избыточных STORE
                if (current.opCode == Instruction.OpCode.STORE &&
                        next.opCode == Instruction.OpCode.STORE &&
                        current.target.equals(next.operand1) &&
                        next.operand2.equals(next.operand1) ) {
                    optimizedInstructions.add(current);
                    i++; // Пропускаем next инструкцию
                }
                else {
                    optimizedInstructions.add(current);
                }
            } else {
                optimizedInstructions.add(current);
            }
        }
        return optimizedInstructions;
    }

    private List<Instruction> filterDeadCode(List<Instruction> instructions) {
        List<Instruction> optimizedInstructions = new ArrayList<>();

        for (long i = instructions.size() - 1; i >= 0; i--) {
            Instruction instruction = instructions.get(Math.toIntExact(i));

            switch (instruction.opCode) {
                case PRINT -> {
                    usedVariables.add(instruction.operand1);
                    optimizedInstructions.add(instruction);
                }
                case ADD, SUB, MUL, MOD, BITWISE_SHIFT-> {
                    if (instruction.target != null && usedVariables.contains(instruction.target) || usedVariables.contains(instruction.operand1)) {
                        usedVariables.add(instruction.operand2.toString());
                        usedVariables.add(instruction.operand3.toString());
                        optimizedInstructions.add(instruction);
                    }
                }
                case STORE -> {
                    if (instruction.target != null && usedVariables.contains(instruction.target) || usedVariables.contains(instruction.operand1)) {
                        addUsedVariable(instruction.operand2);
                        optimizedInstructions.add(instruction);
                    }
                }
                case IF -> {
                    // Для IF анализируем условие и вложенные блоки
                    usedVariables.add(instruction.operand1);
                    usedVariables.add(instruction.operand3.toString());
                    if (instruction.block != null) {
                        instruction.block = reverseList(filterDeadCode(instruction.block));
                    }
                    optimizedInstructions.add(instruction);
                }
                case LOOP -> {
                    usedVariables.add(instruction.operand1);
                    usedVariables.add(instruction.operand3.toString());
                    if (instruction.block != null) {
                        instruction.block = reverseList(filterDeadCode(instruction.block));
                    }
                    optimizedInstructions.add(instruction);
                }
                case CALL -> {
                    List<Object> args = (List<Object>) instruction.operand2;
                    for (var arg : args) {
                        addUsedVariable(arg);
                    }
                    optimizedInstructions.add(instruction);
                }
                case STORE_ARRAY_VAR -> {
                    if (instruction.target != null && usedVariables.contains(instruction.target)) {
                        usedVariables.add(instruction.operand1);
                        usedVariables.add(instruction.operand2.toString());
                        optimizedInstructions.add(instruction);
                    }
                }
                default -> {
                    optimizedInstructions.add(instruction);
                }
            }
        }

        return optimizedInstructions;
    }
    private void addUsedVariable(Object operand){
        if(operand instanceof String strOperand && !isInteger(strOperand)){
            usedVariables.add(strOperand);
        }
    }

    private boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }


    private void writeInstruction(DataOutputStream out, Instruction instr) throws IOException {
        out.writeByte(instr.opCode.ordinal()); // Пишем код операции

        // Обработка различных типов инструкций
        switch (instr.opCode) {
            case FUN:
                out.writeUTF(instr.operand1 != null ? instr.operand1 : "");

                // Запись списка параметров
                List<String> parameters = instr.parameters;
                out.writeLong(parameters.size());
                for (String param : parameters) {
                    out.writeUTF(param);
                }
                out.writeLong(instr.block.size());
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
                    out.writeLong(instr.block.size());
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
                    out.writeLong(instr.block.size());
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

    public static List<Instruction> reverseList(List<Instruction> original) {
        List<Instruction> reversed = new ArrayList<>();
        for (int i = original.size() - 1; i >= 0; i--) {
            reversed.add(original.get(i));
        }
        return reversed;
    }
}