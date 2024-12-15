package itmo.anastasiya;

import java.util.List;

public class Main {
    public static void main(String[] args) {

        String code = """
                    fun x [let v, let h, let arr]
                        let v = 1 + v;
                        let h = 1 + h;
                        let g = v + h;
                        let k = arr[0];
                        print[k];
                        if [g > 8]
                        [ return g; ]
                        return h;
                    let a = 1;
                    let b = 1;
                    let arr = new [10];
                    let arr[0] = 1;
                    let b = arr[0];
                    let c = x(a, b, arr);
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