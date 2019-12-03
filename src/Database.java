public class Database {
    public static void main(String[] args) {
        TransactionManager transactionManager = new TransactionManager();
//        /* Test 1 */
//        transactionManager.begin(1, 1);
//        transactionManager.begin(2, 2);
//        transactionManager.write(1, 1, 101, 3);
//        transactionManager.write(2, 2, 202, 4);
//        transactionManager.write(1, 2, 102, 5);
//        transactionManager.write(2, 1, 201, 6);
//        transactionManager.end(1, 7);
//        transactionManager.dump();
//        /* Test 2 */
//        transactionManager.begin(1, 1);
//        transactionManager.beginRO(2, 2);
//        transactionManager.write(1, 1, 101, 3);
//        transactionManager.read(2, 2, 4);
//        transactionManager.write(1, 2, 102, 5);
//        transactionManager.read(2, 1, 6);
//        transactionManager.end(1, 7);
//        transactionManager.end(2, 7);
//        transactionManager.dump();
//        /* Test 3 */
//        transactionManager.begin(1, 1);
//        transactionManager.begin(2, 2);
//        transactionManager.read(1, 3, 3);
//        transactionManager.fail(2);
//        transactionManager.write(2, 8, 88, 5);
//        transactionManager.read(2, 3, 6);
//        transactionManager.write(1, 5, 91, 7);
//        transactionManager.end(2, 8);
//        transactionManager.recover(2);
//        transactionManager.end(1, 10);
//        transactionManager.dump();
//        /* Test 3.5 */
//        transactionManager.begin(1, 1);
//        transactionManager.begin(2, 2);
//        transactionManager.read(1, 3, 3);
//        transactionManager.write(2, 8, 88, 4);
//        transactionManager.fail(2);
//        transactionManager.read(2, 3, 6);
//        transactionManager.write(1, 4, 91, 7);
//        transactionManager.recover(2);
//        transactionManager.end(2, 9);
//        transactionManager.end(1, 10);
//        transactionManager.dump();
//        /* Test 3.7 */
//        transactionManager.begin(1, 1);
//        transactionManager.begin(2, 2);
//        transactionManager.read(1, 3, 3);
//        transactionManager.write(2, 8, 88, 4);
//        transactionManager.fail(2);
//        transactionManager.read(2, 3, 6);
//        transactionManager.recover(2);
//        transactionManager.write(1, 4, 91, 8);
//        transactionManager.end(2, 9);
//        transactionManager.end(1, 10);
//        transactionManager.dump();
//        /* Test 4 */
//        transactionManager.begin(1, 1);
//        transactionManager.begin(2, 2);
//        transactionManager.read(1, 1, 3);
//        transactionManager.fail(2);
//        transactionManager.write(2, 8, 88, 5);
//        transactionManager.read(2, 3, 6);
//        transactionManager.read(1, 5, 7);
//        transactionManager.end(2, 8);
//        transactionManager.recover(2);
//        transactionManager.end(1, 10);
//        transactionManager.dump();
//        /* Test 5 */
//        transactionManager.begin(1, 1);
//        transactionManager.begin(2, 2);
//        transactionManager.write(1, 6, 66, 3);
//        transactionManager.fail(2);
//        transactionManager.write(2, 8, 88, 5);
//        transactionManager.read(2, 3, 6);
//        transactionManager.read(1, 5, 7);
//        transactionManager.end(2, 8);
//        transactionManager.recover(2);
//        transactionManager.end(1, 10);
//        transactionManager.dump();
        /* Test 6 */
        transactionManager.begin(1, 1);
        transactionManager.begin(2, 2);
        transactionManager.write(1, 6, 66, 3);
        transactionManager.fail(2);
        transactionManager.write(2, 8, 88, 5);
        transactionManager.read(2, 3, 6);
        transactionManager.read(1, 5, 7);
        transactionManager.end(2, 8);
        transactionManager.recover(2);
        transactionManager.end(1, 10);
        transactionManager.dump();


    }
}
