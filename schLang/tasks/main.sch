let a = 10;
let c = a + 1;
print [c];
let c = 1;
let a = c + 1;
print [a];
loop [ c < 3 ]
[ print [c];
  let c = c + 1;
]