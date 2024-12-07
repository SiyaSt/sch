package itmo.anastasiya;

import java.util.List;

public class Main {
    public static void main(String[] args) {

        String code = """
            fun x [let a]
                let a = 10;
                let c = 10;
                print [c];
                let c = 11;
                if [ c > a ]
                [ print [c]; ]
            return a;
        """;

        // Лексер
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        // Парсер
        Parser parser = new Parser(tokens);
        List<Instruction> instructions = parser.parse();

        // Компиляция в файл
        Compiler compiler = new Compiler(instructions);
        compiler.saveToFile("program.schc");

        // Загрузка и исполнение
        VirtualMachine vm = new VirtualMachine();
        vm.loadFromFile("program.schc");
        vm.run();
    }
}