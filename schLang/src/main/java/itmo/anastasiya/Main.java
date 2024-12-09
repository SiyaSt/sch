package itmo.anastasiya;

import java.util.List;

public class Main {
    public static void main(String[] args) {

        String code = """
                    fun x [let v]
                        let v = 1 + v;
                        print[v];
                        if [v == 3]
                        [ return v; ]
                        return x(v);
                    let a = 1;
                    let b = 2;
                    let c = x(a);
                    print[c];
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