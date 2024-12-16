package itmo.anastasiya;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        var code ="""
                fun x[let a]
                    let name = 1;
                    if [a < 2] [
                        return name;
                    ]
                   return name;
                let a = 10;
                let result = x(a);
                print[result];
                
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