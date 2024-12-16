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
let arr[1] = 11;
let arr[2] = 12;
let arr[3] = 6;
let arr[4] = 6;
let arr[5] = 5;
let arr[6] = 4;

let c = quickSort(arr, 0, 6);

let first = arr[0];

let i = 0;
loop [i < 7]
[
    let curr = arr[i];
    print[curr];
    let i = i + 1;
]
