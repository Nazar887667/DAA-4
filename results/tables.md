### LCS and Edit Distance (average of 5 runs)

| Length n (both strings) | LCS time (ms) | Edit Distance time (ms) | DP cells computed (n*n) | LCS length | Edit distance |
|---:|---:|---:|---:|---:|---:|
| 100 | 0,0550 | 0,0707 | 10000 | 64 | 55 |
| 500 | 0,8688 | 1,2490 | 250000 | 326 | 267 |
| 1000 | 3,8618 | 5,0802 | 1000000 | 648 | 521 |

### String matching, pattern present (pattern length 50, average of 5 runs)

| Text length | Occurrences | Naive time (ms) | KMP time (ms) | Rabin-Karp time (ms) | Naive comparisons | KMP comparisons | Rabin-Karp comparisons | Rabin-Karp spurious hits |
|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 10000 | 1 | 0,0732 | 0,0623 | 0,1392 | 13408 | 12559 | 50 | 0 |
| 50000 | 1 | 0,3432 | 0,3110 | 0,7115 | 66662 | 61861 | 50 | 0 |
| 100000 | 1 | 0,7779 | 0,6812 | 1,3562 | 132907 | 124726 | 50 | 0 |

### String matching, pattern absent (pattern length 50, average of 5 runs)

| Text length | Occurrences | Naive time (ms) | KMP time (ms) | Rabin-Karp time (ms) | Naive comparisons | KMP comparisons | Rabin-Karp comparisons | Rabin-Karp spurious hits |
|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| 10000 | 0 | 0,0741 | 0,0650 | 0,1827 | 13388 | 12604 | 0 | 0 |
| 50000 | 0 | 0,3887 | 0,3476 | 0,6769 | 66438 | 62470 | 0 | 0 |
| 100000 | 0 | 0,8001 | 0,6905 | 1,3790 | 132683 | 124736 | 0 | 0 |

### Kasai's algorithm (average of 5 runs, suffix array construction not timed)

| String length n | Time (ms) | Character comparisons |
|---:|---:|---:|
| 1000 | 0,0165 | 1990 |
| 5000 | 0,0915 | 9991 |
| 10000 | 0,1827 | 19989 |
