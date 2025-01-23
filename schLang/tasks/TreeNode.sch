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
                    let maxSize = maxSize + 1;
                    let result = tree[index];
                    let checkL = itemCheck(tree, left, maxSize);
                    let result = result + checkL;
                    let checkR = itemCheck(tree, right, maxSize);
                    let result = result - checkR;
                    return result;


        fun bottomUpTree[let item, let depth]
              let depthPlusOne = depth + 1;
              let size = 1 | depthPlusOne;
              let size = size - 1;
              let tree = new [10000];
              let a = buildTree(tree, 0, item, depth);
              return tree;


        fun buildTree[let tree, let index, let item, let depth]
            let tree[index] = item;
            if [depth > 0] [
                       let left = 2 * index;
                       let left = left  + 1;
                       let right = 2 * index;
                       let right = right + 2;
                       let min = 2 * item;
                       let min  = min - 1;
                       let a = 2 * item;
                       let depthMinusOne = depth - 1;
                       let res = buildTree(tree, left, min, depthMinusOne);
                       let res = buildTree(tree, right, a, depthMinusOne);
            ]
            let h = 0;
            return h;




        let ret = 0;
        let n = 4;
        loop [n < 8] [
             let n = n + 1;
             let minDepth = 4;
             let maxDepth = 0;
             let nn = n - 2;
             let maxDepth = n + 0;
             if [minDepth > nn] [
                let maxDepth = minDepth + 2;
             ]



             let stretchDepth = maxDepth + 1;

             let stretchTreeSize = stretchDepth + 1;
             let stretchTreeSize = 1 | stretchDepth;
             let stretchTreeSize = stretchTreeSize - 1;

             let zero = 0;
             let stretchTree = bottomUpTree(zero, stretchDepth);
             let check = itemCheck(stretchTree, 0, stretchTreeSize);

             let longLivedTreeSize = maxDepth + 1;
             let longLivedTreeSize = 1 | longLivedTreeSize;
             let longLivedTreeSize = longLivedTreeSize - 1;
             let longLivedTree = bottomUpTree(0, maxDepth);

             let depth = minDepth + 0;

             let max = maxDepth + 1;
             loop [depth < max] [
                   let depth = depth + 2;
                   let minus = maxDepth - depth;
                   let minusPlus = minus + minDepth;
                   let iterations = 1 | minusPlus;
                   let iterations = iterations + 1;

                   let check = 0;
                   let i = 1;

                   loop [i < iterations] [
                                        let i = i + 1;
                                        let posTree = bottomUpTree(i, depth);
                                        let treeSize = depth + 1;
                                        let treeSize = 1 | treeSize;
                                        let treeSize = treeSize - 1;
                                        let t = itemCheck(posTree, 0, treeSize);
                                        let check = check + t;
                                        let neg = 0 - i;
                                        let negTree = bottomUpTree(neg, depth);
                                        let t = itemCheck(negTree, 0, treeSize);
                                        let check = check + t;
                   ]
             ]

             let v = itemCheck(longLivedTree, 0, longLivedTreeSize);
             let ret =  ret + v;
             print [ret];

        ]

        let expected = 0 - 4;
        if [ret != expected] [

             let err = error;
             print [err];
        ]

