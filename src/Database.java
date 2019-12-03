public class Database {
    public static void main(String[] args) {
        TransactionManager transactionManager = new TransactionManager();
        transactionManager.begin(1, 1);
        transactionManager.begin(2, 2);
        transactionManager.write(1, 1, 101, 3);
        transactionManager.write(2, 2, 102, 4);
        transactionManager.write(1, 2, 102, 5);
        transactionManager.write(2, 1, 201, 6);
        transactionManager.end(1, 7);
        transactionManager.dump();

    }
}
