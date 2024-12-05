package itmo.anastasiya;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java Main <filename>");
            System.exit(1);
        }

        String filename = args[0];


        try {

            String code = Files.readString(Path.of(filename));


            Lexer lexer = new Lexer(code);
            List<Token> tokens = lexer.tokenize();


            Parser parser = new Parser(tokens);
            List<Instruction> instructions = parser.parse();


            Compiler compiler = new Compiler(instructions);
            String compiledFile = filename.replace(".sch", ".schc");
            compiler.saveToFile(compiledFile);


            VirtualMachine vm = new VirtualMachine();
            vm.loadFromFile(compiledFile);
            vm.run();

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        } catch (Exception e) {
            System.err.println("Error executing program: " + e.getMessage());
            e.printStackTrace();
            System.exit(3);
        }
    }
}
