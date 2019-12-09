# RepCRec

This project is implemented by Xinyi Liu (xl2700), Ming Xu (mx562).

## Project Structure

* `src/`: this is the source folder storing the code.
* `input/`: this is the folder storing our tests.
* `output/`: this the the folder storing outputs of our tests.
* `RepCRec.jar`: the jar file built from the source code.
* `RepCRec.rpz`: the zipped file made with Reprozip for reproducibility on other platforms.

### Execution

Here is the command to execute the jar file.

```
java -jar RepCRec.jar /path/to/input
```

If you want to execute our tests in `input/`, please execute `./run.sh`.

### Reprounzip

Please make sure that reprounzip is properly installed.

```
reprounzip directory setup RepCRec.rpz RepCRec
reprounzip directory run RepCRec
```

### Test Cases

We have done all the tests with provided examples and tried to come out something new (see `input/`).

#### test1

T1 read should be blocked because the only copy available is not readable. The site just recovers and no transaction commits yet. Since T1 is not holding read lock on x2, so T2 can obtain a write lock and commit the new value. T1 is able to read after T2 commits.

#### test2

T1 tries to promote its read lock to a write lock but fails. This is because T2 is waiting for a write lock, which causes a deadlock. T2 is younger, so it aborts.

#### test3

T2 commits successfully but doesn't write to site 2 because it's down. T1 aborts due to previous access of site 2. This is handled when end(T1) is called.

#### test4

T1 reads 101 because T1 is able to see its uncommitted value, and then commits successfully. However, T2 still read the initial value 10 rather than the value 101 that T1 commits due to multiversion read consistency.

#### test5

A deadlock exists: T1 -> T2, T2 -> T3, T3 -> T1. All are blocked. T3 aborts since it's the youngest, which allows T2 to commit. T1 is unblocked after T2 commits, and also commits successfully.
