package itmo.anastasiya;

import java.util.List;

public class Main {
    public static void main(String[] args) {

        //  let a = new [10];
        String code = """
          let a = new [10];
          let a[0] = 1;
          let b = a[0];
          print [b];
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
