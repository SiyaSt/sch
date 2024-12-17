fun random [let seed]
    let a = 1103515245;
    let c = 12345;
    let m = 2000000001;
    let temp = a * seed;
    let temp = temp + c;
    let seed = temp + m;
    return seed;

fun fillRandom [let arr, let size, let min, let max]
    let seed = 12345;
    let i = 0;
    loop [i < size]
    [
        let seed = random(seed);
        let diff = max - min;
        let diff = diff + 1;
        let random = seed % diff;
        let randomValue = min + random;
        let arr[i] = randomValue;
        let i = i + 1;
    ]
    let r = 0;
    return r;

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

let arr = new [10000];
let size = 10000;
let min = 1;
let max = 100;
let res = fillRandom(arr, size, min, max);
let size = size - 1;

let c = quickSort(arr, 0, size);

