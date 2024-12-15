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
                        let arr[1] = 2;
                        print[k];
                        if [g > 8]
                        [ return g; ]
                        return arr;

                
                    let a = 1;
                    let b = 1;
                   
                    let arr = new [10];
                    let arr[0] = 1;
         
                    let b = arr[0];
                    let c = x(a, b, arr);
                    let i = 1;
                    let d = c[i];
                    print[d];
                    let t = arr[1];
               
                    loop [ a < 4 ]
                    [ print [a];
                      let a = a + 1;
                      if [ a == 2 ]
                      [ print [a];]
                    ]
                    
                    print[arr];
                """;


        String task1 = """
                  let name = 1;
                  fun x[let a]
                     if [a < 2] [
                         return name;
                     ]
                     let b = a - 1;
                     let tmpa = x(b);
                     let tmp = a * tmpa;
                     return tmp;
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