fun x[let a]
    let name = 1;
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