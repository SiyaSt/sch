fun itemCheck[let tree, let index, let maxSize]
            let left = 2 * index;
            let left = left + 1;
            let right = 2 * index;
            let right = right + 2;
            let maxSize = maxSize - 1;
            if [left > maxSize] [
                let a = tree[index];
                return a;
            ]
            if [right > maxSize] [
                let a = tree[index];
                return a;
            ]
            let treeLeft = tree[left];
            if [treeLeft == 0] [
                let b = tree[right];
                if [b == 0] [
                      let c = tree[index];
                      return c;
                ]
            ]
            let result = tree[index];
            let checkL = itemCheck(tree, left);
            let result = result + checkL;
            let checkR = itemCheck(tree, right);
            let result = result - checkR;
            return result;


fun bottomUpTree[let item, let depth]
    let depthPlusOne = depth + 1;
    let size = 1 | depthPlusOne;
    size = size - 1;
    let tree = new [10000];
    buildTree[tree, 0, item, depth];
    return tree;


fun buildTree[let tree, let index, let item, let depth]
    tree[index] = item;
    if [depth > 0] [
       let left = 2 * index + 1;
       let right = 2 * index + 2;
       let min = 2 * item - 1;
       let a = 2 * item;
       let depthMinusOne = depth - 1;
       buildTree[tree, left, min, depthMinusOne];
       buildTree[tree, right, a, depthMinusOne];
    ]

let ret = 0;
let n = 4;
loop [n <= 7] [
     n = n + 1;
     let minDepth = 4;
     let maxDepth = 0;
     if [minDepth + 2 > n] [
        maxDepth = minDepth + 2;
     ] else [
        maxDepth = n;
     ]

     let stretchDepth = maxDepth + 1;

     let stretchTree = bottomUpTree[0, stretchDepth];
     let check = itemCheck[stretchTree, 0];

     let longLivedTree = bottomUpTree[0, maxDepth];
     let depth = minDepth
     loop [depth <= maxDepth] [
         depth = depth + 2;
         let minus = maxDepth - depth;
         let minusPlus = minus + minDepth
         let iterations = 1 | minusPlus;

         check = 0;
         let i = 1;
         loop [i - 1 < iterations] [
             i = i + 1;
             let posTree = bottomUpTree[i, depth];
             check = check + itemCheck[posTree, 0];
             let negTree = bottomUpTree[-i, depth];
             check = check + itemCheck[negTree, 0];
        ]
     ]

     ret =  ret + itemCheck[longLivedTree, 0];
     ]

let expected = -4;
  if [ret != expected] [
      print["ERROR: bad result"];
]
