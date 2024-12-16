let a = 10000;
let size = a + 1;
let arr = new[10001];
let i = 0;
loop [ i < size ]
[
    let arr[i] = i;
    let i = i + 1;
]
let i = 2;
loop [ i < size ]
[
    let b = arr[i];
    if [ b != 0 ]
    [
        print [b];
        let j = i * i;
        loop [j < size]
        [
           let arr[j] = 0;
           let j = j + i;
        ]
    ]
    let i = i + 1;
]