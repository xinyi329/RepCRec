public class Database {
    public static void main(String[] args) {
        TransactionManager transactionManager = new TransactionManager();
        transactionManager.begin(1, 1);
        transactionManager.beginRO(2, 2);
        transactionManager.write(1, 1, 101);
        transactionManager.read(2, 2);
        transactionManager.write(1, 2, 102);
        transactionManager.read(2, 1);
        transactionManager.end(1, 7);
        transactionManager.end(2, 8);
        transactionManager.dump();
    }
}
