package itmo.anastasiya;

import org.objectweb.asm.*;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class JWTCompiler {
    private final MemoryManager memoryManager = new MemoryManager();
    // Добавляем хранение индексов локальных переменных
    private final Map<String, Integer> variableIndexes = new HashMap<>();
    private int nextVariableIndex = 0;


    private int getVariableIndex(String variableName) {
        return variableIndexes.computeIfAbsent(variableName, k -> nextVariableIndex++);
    }

    public byte[] compile(String className, List<Instruction> instructions,
                          Map<String, Instruction> functions) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        String internalClassName = className.replace('.', '/');

        cw.visit(V1_8, ACC_PUBLIC, internalClassName, null, "java/lang/Object", null);

        // Создание конструктора
        MethodVisitor constructorVisitor = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        constructorVisitor.visitCode();
        constructorVisitor.visitVarInsn(ALOAD, 0); // Загружаем this
        constructorVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false); // Вызываем конструктор суперкласса
        constructorVisitor.visitInsn(RETURN);
        constructorVisitor.visitMaxs(1, 1);
        constructorVisitor.visitEnd();

        // Создаем метод run
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "run", "([Ljava/lang/String;)V", null, null);
        mv.visitCode();

        compileInstructions(mv, instructions, functions);

        mv.visitInsn(RETURN);
        mv.visitMaxs(100, 100); // Установим большие значения для стека и локальных переменных
        mv.visitEnd();

        cw.visitEnd();

        return cw.toByteArray();
    }

    private void compileInstructions(MethodVisitor mv, List<Instruction> instructions, Map<String, Instruction> functions) {

        for (Instruction instruction : instructions) {
            switch (instruction.opCode) {
                case STORE -> {
                    if (instruction.target != null){
                        compileStore(mv, instruction.target, instruction.operand2);
                    } else {
                        compileStore(mv, instruction.operand1, instruction.operand2);
                    }
                }
                case PRINT -> {
                    compilePrint(mv, instruction.operand1);
                }
                case ADD -> {
                    if (instruction.target != null){
                        compileAdd(mv, instruction.target, instruction.operand2, instruction.operand3);
                    } else {
                        compileAdd(mv, instruction.operand1, instruction.operand2, instruction.operand3);
                    }
                }
                case SUB -> {
                    if (instruction.target != null){
                        compileSub(mv, instruction.target, instruction.operand2, instruction.operand3);
                    } else {
                        compileSub(mv, instruction.operand1, instruction.operand2, instruction.operand3);
                    }
                }
                case MUL -> {
                    if (instruction.target != null){
                        compileMul(mv, instruction.target, instruction.operand2, instruction.operand3);
                    } else {
                        compileMul(mv, instruction.operand1, instruction.operand2, instruction.operand3);
                    }

                }
                case MOD -> {
                    if (instruction.target != null){
                        compileMod(mv, instruction.target, instruction.operand2, instruction.operand3);
                    } else {
                        compileMod(mv, instruction.operand1, instruction.operand2, instruction.operand3);
                    }
                }
                case LESS -> {
                    if (instruction.target != null){
                        compileLess(mv, instruction.target, instruction.operand2, instruction.operand3);
                    } else {
                        compileLess(mv, instruction.operand1, instruction.operand2, instruction.operand3);
                    }
                }
                case GREATER -> {
                    if (instruction.target != null){
                        compileGreater(mv, instruction.target, instruction.operand2, instruction.operand3);
                    } else {
                        compileGreater(mv, instruction.operand1, instruction.operand2, instruction.operand3);
                    }
                }
                case EQUALS -> {
                    if (instruction.target != null){
                        compileEquals(mv, instruction.target, instruction.operand2, instruction.operand3);
                    } else {
                        compileEquals(mv, instruction.operand1, instruction.operand2, instruction.operand3);
                    }
                }
                case NOT_EQUALS -> {
                    if (instruction.target != null){
                        compileNotEquals(mv, instruction.target, instruction.operand2, instruction.operand3);
                    } else {
                        compileNotEquals(mv, instruction.operand1, instruction.operand2, instruction.operand3);
                    }
                }
                case NEW -> {
                    if (instruction.target != null){
                        compileNew(mv, instruction.target, instruction.operand2);
                    } else {
                        compileNew(mv, instruction.operand1, instruction.operand2);
                    }
                }
                case WRITE_INDEX -> {
                    if (instruction.target != null){
                        compileWriteIndex(mv, instruction.target, instruction.operand2, instruction.operand3);
                    } else {
                        compileWriteIndex(mv, instruction.operand1, instruction.operand2, instruction.operand3);
                    }
                }
                case STORE_ARRAY_VAR -> {
                    if (instruction.target != null) {
                        compileStoreArrayVar(mv, instruction.target, instruction.operand1, instruction.operand2);
                    } else {
                        compileStoreArrayVar(mv, (String) instruction.operand3, instruction.operand1, instruction.operand2);
                    }
                }
                case READ_INDEX -> {
                    compileReadIndex(mv, instruction.operand1, instruction.operand2);
                }
                case IF -> {
                    compileIf(mv, instruction);
                }
                case LOOP -> {
                    compileLoop(mv, instruction);
                }
                case FUN -> {
                    compileFunctionDefinition(mv, instruction, functions);
                }
                case CALL -> {
                    compileCall(mv, instruction, functions);
                }
                case RETURN -> {
                    compileReturn(mv, instruction, functions);
                }
                default -> {
                    throw new RuntimeException("Unknown instruction: " + instruction.opCode);
                }
            }
        }
    }

    private void compileReturn(MethodVisitor mv, Instruction instruction, Map<String, Instruction> functions) {
        if (instruction.operand1 == null || instruction.operand1.isEmpty()) {
            if(instruction.operand2 != null) {
                Instruction instruction1 = (Instruction) instruction.operand2;
                String functionName = instruction1.operand1;
                Instruction functionInstruction = functions.get(functionName);
                if (functionInstruction == null) {
                    throw new RuntimeException("Function " + functionName + " is not defined");
                }

                List<String> parameters = functionInstruction.parameters;
                List<Instruction> functionBody = functionInstruction.block;
                String args = String.valueOf(instruction1.operand2);
                List<Object> arguments = VirtualMachine.parseToListOfObjects(args);
                if (parameters.size() != arguments.size()) {
                    throw new RuntimeException("Function " + functionName + " expects " + parameters.size() + " arguments, but got " + arguments.size());
                }
                // Создаем локальную переменную для результата
                int resultVarIndex = getVariableIndex("_return_value_" + functionName); // unique name

                // Создание массива для хранения результатов параметров
                int argsArrVarIndex = getVariableIndex("_argsArr_" + functionName);

                // Создаем массив
                mv.visitIntInsn(BIPUSH, parameters.size());
                mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
                mv.visitVarInsn(ASTORE, argsArrVarIndex);

                // Загрузка аргументов функции
                for (int i = 0; i < parameters.size(); i++) {
                    mv.visitVarInsn(ALOAD, argsArrVarIndex);
                    mv.visitIntInsn(BIPUSH, i);
                    compileLoadValue(mv, arguments.get(i));
                    mv.visitInsn(AASTORE);
                }

                // вызов функции
                compileFunctionCall(mv, functionInstruction, argsArrVarIndex, resultVarIndex);
                // Загрузка результата
                mv.visitVarInsn(ALOAD, resultVarIndex);
                mv.visitInsn(ARETURN);
            } else {
                mv.visitInsn(ACONST_NULL);
                mv.visitInsn(ARETURN);
            }
        }
        else {
            compileLoadValue(mv, instruction.operand1);
            mv.visitInsn(ARETURN);
        }
    }

    private void compileFunctionCall(MethodVisitor mv, Instruction functionInstruction,
                                     int argsArrVarIndex, int resultVarIndex) {
        String functionName = functionInstruction.operand1;

        String internalClassName = "itmo/anastasiya/JITGeneratedClass"; // Класс, где сгенерированный код
        String methodDescriptor = "(";
        List<String> params = functionInstruction.parameters;
        for (int i = 0; i < params.size(); i++){
            methodDescriptor += "Ljava/lang/Object;";
        }
        methodDescriptor += ")Ljava/lang/Object;";
        // Создаем локальную переменную для массива аргументов
        mv.visitVarInsn(ALOAD, argsArrVarIndex); // Загружаем массив аргументов

        mv.visitMethodInsn(INVOKESTATIC, internalClassName, functionName, methodDescriptor, false);
        // mv.visitTypeInsn(CHECKCAST, "java/lang/Object"); // Cast результата к Object
        mv.visitVarInsn(ASTORE, resultVarIndex);
    }

    private void compileCall(MethodVisitor mv, Instruction instruction, Map<String, Instruction> functions) {
        String functionName = instruction.operand1;
        Instruction functionInstruction = functions.get(functionName);
        if (functionInstruction == null) {
            throw new RuntimeException("Function " + functionName + " is not defined");
        }

        List<String> parameters = functionInstruction.parameters;

        var args = instruction.operand2;
        List<Object> arguments = VirtualMachine.parseToListOfObjects(args);
        if (parameters.size() != arguments.size()) {
            throw new RuntimeException("Function " + functionName + " expects " + parameters.size() + " arguments, but got " + arguments.size());
        }

        int resultVarIndex = getVariableIndex("_return_value_" + functionName); // unique name

        // Создание массива для хранения результатов параметров
        int argsArrVarIndex = getVariableIndex("_argsArr_" + functionName);

        // Создаем массив
        mv.visitIntInsn(BIPUSH, parameters.size());
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
        mv.visitVarInsn(ASTORE, argsArrVarIndex);

        // Загрузка аргументов функции
        for (int i = 0; i < parameters.size(); i++) {
            mv.visitVarInsn(ALOAD, argsArrVarIndex);
            mv.visitIntInsn(BIPUSH, i);
            compileLoadValue(mv, arguments.get(i));
            mv.visitInsn(AASTORE);
        }

        compileFunctionCall(mv, functionInstruction, argsArrVarIndex, resultVarIndex);
        if (instruction.operand3 != null) {
            mv.visitVarInsn(ALOAD, resultVarIndex);
            compileStore(mv, (String)instruction.operand3, "_return_value_" + functionName);
        }
    }

    private void compileFunctionDefinition(MethodVisitor mv, Instruction instruction, Map<String, Instruction> functions) {
        String functionName = instruction.operand1;
        List<String> parameters = instruction.parameters;
        List<Instruction> functionBody = instruction.block;
        String internalClassName = "itmo/anastasiya/JITGeneratedClass";
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        String methodDescriptor = "(";
        for (int i = 0; i < parameters.size(); i++){
            methodDescriptor += "Ljava/lang/Object;";
        }
        methodDescriptor += ")Ljava/lang/Object;";
        MethodVisitor funMv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, functionName, methodDescriptor, null, null);
        funMv.visitCode();
        for(int i = 0; i < parameters.size(); i++){
            funMv.visitVarInsn(ALOAD, i); // Загружаем параметр функции

            compileStore(funMv, parameters.get(i), "_fun_param_" + i);
        }

        compileInstructions(funMv, functionBody, functions);
        funMv.visitInsn(ARETURN);
        funMv.visitMaxs(100, 100);
        funMv.visitEnd();
        cw.visitEnd();

        byte[] compiledFunction = cw.toByteArray();
        try (FileOutputStream fos = new FileOutputStream("JITGeneratedClass.class")) {
            fos.write(compiledFunction);
        } catch (IOException e) {
            throw new RuntimeException("Can't save to file " + e.getMessage());
        }

    }

    private void compileLoop(MethodVisitor mv, Instruction instruction) {
        Label loopStart = new Label();
        Label loopEnd = new Label();

        // Начало цикла
        mv.visitLabel(loopStart);
        // Проверка условия
        compileCondition(mv, instruction.operand1, instruction.operand2, instruction.operand3, loopEnd);

        if (instruction.block != null) {
            compileInstructions(mv, instruction.block, Map.of());
        }
        // Переход на начало
        mv.visitJumpInsn(GOTO, loopStart);
        // Конец цикла
        mv.visitLabel(loopEnd);
    }


    private void compileIf(MethodVisitor mv, Instruction instruction) {
        Label ifEnd = new Label();
        // Проверка условия
        compileCondition(mv, instruction.operand1, instruction.operand2, instruction.operand3, ifEnd);

        if (instruction.block != null) {
            compileInstructions(mv, instruction.block, Map.of());
        }

        mv.visitLabel(ifEnd);
    }
    private void compileCondition(MethodVisitor mv, Object operand1, Object operand2, Object operand3, Label endLabel) {
        compileLoadValue(mv, operand1);
        compileLoadValue(mv, operand3);

        if (operand2.equals(Token.Type.LESS.name())){
            mv.visitJumpInsn(IF_ICMPGE, endLabel); // Если меньше, то идем в конец
        } else if (operand2.equals(Token.Type.GREATER.name())){
            mv.visitJumpInsn(IF_ICMPLE, endLabel); // Если больше, то идем в конец
        } else if (operand2.equals(Token.Type.EQUALS.name())){
            mv.visitJumpInsn(IF_ICMPNE, endLabel); // Если не равно, то идем в конец
        } else if (operand2.equals(Token.Type.NOT_EQUALS.name())){
            mv.visitJumpInsn(IF_ICMPEQ, endLabel); // Если равно, то идем в конец
        }
        else {
            throw new RuntimeException("Unknown condition" + operand2);
        }

    }

    private void compileStoreArrayVar(MethodVisitor mv, String target, String arrayName, Object index) {
        compileLoadValue(mv, arrayName); // Загружаем массив
        compileLoadValue(mv, index); // Загружаем индекс
        mv.visitInsn(AALOAD); // Загружаем элемент массива
        compileStore(mv, target, "_array_element_" + target); // сохраняем значение в переменную
    }

    private void compileReadIndex(MethodVisitor mv, String arrayName, Object index) {
        compileLoadValue(mv, arrayName); // Загружаем массив
        compileLoadValue(mv, index); // Загружаем индекс
        mv.visitInsn(AALOAD); // Загружаем элемент массива
        //System.out.println
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitInsn(SWAP);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
    }


    private void compileWriteIndex(MethodVisitor mv, String arrayName, Object index, Object value) {
        compileLoadValue(mv, arrayName); // Загружаем массив
        compileLoadValue(mv, index); // Загружаем индекс
        compileLoadValue(mv, value); // Загружаем значение
        mv.visitInsn(AASTORE); // Сохраняем значение в массив
    }


    private void compileNew(MethodVisitor mv, String target, Object size) {
        if (size instanceof String strSize) {
            try {
                int arraySize = Integer.parseInt(strSize);
                mv.visitIntInsn(BIPUSH, arraySize);
                mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
                compileStore(mv, target, "_array_"+target);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid array size: " + strSize, e);
            }
        } else if (size instanceof List) {
            List<?> array = (List<?>) size;
            mv.visitIntInsn(BIPUSH, array.size());
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            for(int i = 0; i < array.size(); i++) {
                mv.visitInsn(DUP); // Duplicate the array reference for array element access
                mv.visitIntInsn(BIPUSH, i);
                compileLoadValue(mv, array.get(i));
                mv.visitInsn(AASTORE);
            }
            compileStore(mv, target, "_array_"+target);
        } else {
            throw new RuntimeException("Invalid array size: " + size);
        }
    }

    private void compileNotEquals(MethodVisitor mv, String target, Object operand2, Object operand3) {
        compileLoadValue(mv, operand2); // загружаем операнд 2
        compileLoadValue(mv, operand3); // загружаем операнд 3
        Label notEqualsLabel = new Label();
        Label endLabel = new Label();
        mv.visitJumpInsn(IF_ICMPEQ, notEqualsLabel);
        mv.visitInsn(ICONST_1); // true
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(notEqualsLabel);
        mv.visitInsn(ICONST_0); // false
        mv.visitLabel(endLabel);
        compileStore(mv, target, "_notEquals_" + target);

    }

    private void compileEquals(MethodVisitor mv, String target, Object operand2, Object operand3) {
        compileLoadValue(mv, operand2); // загружаем операнд 2
        compileLoadValue(mv, operand3); // загружаем операнд 3
        Label equalsLabel = new Label();
        Label endLabel = new Label();
        mv.visitJumpInsn(IF_ICMPNE, equalsLabel);
        mv.visitInsn(ICONST_1); // true
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(equalsLabel);
        mv.visitInsn(ICONST_0); // false
        mv.visitLabel(endLabel);
        compileStore(mv, target, "_equals_" + target);
    }

    private void compileGreater(MethodVisitor mv, String target, Object operand2, Object operand3) {
        compileLoadValue(mv, operand2); // загружаем операнд 2
        compileLoadValue(mv, operand3); // загружаем операнд 3
        Label greaterLabel = new Label();
        Label endLabel = new Label();
        mv.visitJumpInsn(IF_ICMPLE, greaterLabel);
        mv.visitInsn(ICONST_1); // true
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(greaterLabel);
        mv.visitInsn(ICONST_0); // false
        mv.visitLabel(endLabel);
        compileStore(mv, target, "_greater_" + target);
    }

    private void compileLess(MethodVisitor mv, String target, Object operand2, Object operand3) {
        compileLoadValue(mv, operand2); // загружаем операнд 2
        compileLoadValue(mv, operand3); // загружаем операнд 3

        Label lessLabel = new Label();
        Label endLabel = new Label();
        mv.visitJumpInsn(IF_ICMPGE, lessLabel);
        mv.visitInsn(ICONST_1); // true
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(lessLabel);
        mv.visitInsn(ICONST_0); // false
        mv.visitLabel(endLabel);
        compileStore(mv, target, "_less_" + target);
    }

    private void compileMod(MethodVisitor mv, String target, Object operand2, Object operand3) {
        compileLoadValue(mv, operand2); // загружаем операнд 2
        compileLoadValue(mv, operand3); // загружаем операнд 3
        mv.visitInsn(IREM);
        compileStore(mv, target, "_mod_" + target);
    }

    private void compileMul(MethodVisitor mv, String target, Object operand2, Object operand3) {
        compileLoadValue(mv, operand2); // загружаем операнд 2
        compileLoadValue(mv, operand3); // загружаем операнд 3
        mv.visitInsn(IMUL);
        compileStore(mv, target, "_mul_" + target);
    }

    private void compileSub(MethodVisitor mv, String target, Object operand2, Object operand3) {
        compileLoadValue(mv, operand2); // загружаем операнд 2
        compileLoadValue(mv, operand3); // загружаем операнд 3
        mv.visitInsn(ISUB);
        compileStore(mv, target, "_sub_" + target);
    }

    private void compileAdd(MethodVisitor mv, String target, Object operand2, Object operand3) {
        compileLoadValue(mv, operand2); // загружаем операнд 2
        compileLoadValue(mv, operand3); // загружаем операнд 3
        mv.visitInsn(IADD);
        compileStore(mv, target, "_add_" + target);
    }


    private void compilePrint(MethodVisitor mv, String operand1) {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        compileLoadValue(mv, operand1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
    }

    private void compileStore(MethodVisitor mv, String target, Object value) {
        int index = getVariableIndex(target);

        compileLoadValue(mv, value); // Загружаем значение
        mv.visitVarInsn(ASTORE, index); // Сохраняем значение в локальной переменной
    }

    private void compileLoadValue(MethodVisitor mv, Object value) {
        if (value == null) {
            mv.visitInsn(ACONST_NULL);
            return;
        }

        if (value instanceof Integer intValue) {
            if (intValue >= -1 && intValue <= 5) {
                mv.visitInsn(ICONST_0 + intValue);
            } else if (intValue >= Byte.MIN_VALUE && intValue <= Byte.MAX_VALUE) {
                mv.visitIntInsn(BIPUSH, intValue);
            } else if (intValue >= Short.MIN_VALUE && intValue <= Short.MAX_VALUE) {
                mv.visitIntInsn(SIPUSH, intValue);
            } else {
                mv.visitLdcInsn(intValue);
            }
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);

        } else if (value instanceof String strValue) {
            try {
                int integerValue = Integer.parseInt(strValue);
                if (integerValue >= -1 && integerValue <= 5) {
                    mv.visitInsn(ICONST_0 + integerValue);
                } else if (integerValue >= Byte.MIN_VALUE && integerValue <= Byte.MAX_VALUE) {
                    mv.visitIntInsn(BIPUSH, integerValue);
                } else if (integerValue >= Short.MIN_VALUE && integerValue <= Short.MAX_VALUE) {
                    mv.visitIntInsn(SIPUSH, integerValue);
                } else {
                    mv.visitLdcInsn(integerValue);
                }
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            } catch (NumberFormatException e) {
                int index = getVariableIndex(strValue);
                mv.visitVarInsn(ALOAD, index); // Загружаем значение из локальной переменной
            }

        }
        else {
            mv.visitLdcInsn(value);
        }
    }

    public void saveClassToFile(String className, byte[] bytecode) {
        try (FileOutputStream fos = new FileOutputStream(className + ".class")) {
            fos.write(bytecode);
        } catch (IOException e) {
            throw new RuntimeException("Can't save to file " + e.getMessage());
        }
    }


    public void traceClass(String className, byte[] bytecode) {
        try (FileOutputStream fos = new FileOutputStream("traced_" + className + ".txt");
             PrintWriter printWriter = new PrintWriter(fos)) {
            ClassReader cr = new ClassReader(bytecode);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

            TraceClassVisitor traceClassVisitor = new TraceClassVisitor(cw, printWriter);
            cr.accept(traceClassVisitor, 0);

            printWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException("Can't trace to file" + e.getMessage());
        }
    }
}