package itmo.anastasiya;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        var code ="""
                fun partition [let arr, let low, let high]
                    let pivot = arr[high];
                    let pivot = pivot + 1;
                    let i = low - 1;
                    let j = low + 0;
                    loop [j < high]
                    [
                    let f = arr[j];
                    if [f < pivot]
                    [
                        let i = i + 1;
                        let temp = arr[i];
                        let temp2 = arr[j];
             
                        let arr[i] = temp2;
                        let arr[j] = temp;
                    ]
               
                    let j = j + 1;
                    ]
                
                    let i = i + 1;
                    let temp = arr[i];
                    let t = arr[high];
                    let arr[i] = t;
                    let arr[high] = temp;
                    return i;
                
                fun quickSort [let arr, let low, let high]
                    if [low < high]
                    [
                        let pi = partition(arr, low, high);
                        let a = pi - 1;
                        let c = quickSort(arr, low, a);
                        let pi2 = pi + 1;
                        let c = quickSort(arr, pi2, high);
                    ]
                    let h = 0;
                    return h;
                
                let arr = new [7];
                let arr[0] = 13;
                let arr[1] = 12;
                let arr[2] = 11;
                let arr[3] = 5;
                let arr[4] = 5;
                let arr[5] = 4;
                let arr[6] = 1;
                
                let c = quickSort(arr, 0, 6);
             
                let first = arr[0];
                
                let i = 0;
                loop [i < 7]
                [
                    let curr = arr[i];
                    print[curr];
                    let i = i + 1;
                ]
                
               
                """;

        String code1 = """
                     
                    fun x [let v, let h, let arr]
                        let v = 1 + v;
                        let h = 1 + h;
                        let g = v + h;
                        let k = arr[0];
                        let arr[1] = 2;
                        print[k];
                        if [g > 8]
                        [ return g; ]
                        return x(v, h, arr);

                
                    let a = 1;
                    let b = 1;
                   
                    let arr = new [10];
                    let arr[0] = 1;
         
                    let b = arr[0];
                    let c = x(a, b, arr);
                    let i = 1;
                    let t = arr[1];
                    let arr[1] = t;
                    let j = arr[i];
                    print[j];
               
                    loop [ a < 4 ]
                    [ print [a];
                      let a = a + 1;
                      if [ a == 2 ]
                      [ print [a];]
                    ]
                    
                    print[arr];
                """;

        var code3 = """
                fun x[let a]
                    let name = 1;
                    if [a < 2] [
                        return name;
                    ]
                    let b = a - 1;
                    let tmpa = x(b);
                    let tmp = a * tmpa;
                    return tmp;
                let a = 17;
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