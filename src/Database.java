import java.io.BufferedReader;
import java.io.FileReader;

public class Database {
    public static void main(String[] args) {
        BufferedReader bufferedReader;
        TransactionManager transactionManager = new TransactionManager();
        try {
            bufferedReader = new BufferedReader(new FileReader(args[0]));
            String line = bufferedReader.readLine();
            int timeStamp = 1;
            while (line != null) {
                String[] tokens = line.split("[(,) ]+");
                String option = tokens[0];
                if (option.equals("begin")) {
                    String transaction = tokens[1];
                    int transactionID = Integer.parseInt(transaction.substring(1));
                    transactionManager.begin(transactionID, timeStamp);
                }
                else if (option.equals("beginRO")) {
                    String transaction = tokens[1];
                    int transactionID = Integer.parseInt(transaction.substring(1));
                    transactionManager.beginRO(transactionID, timeStamp);
                }
                else if (option.equals("R")) {
                    int transactionID = Integer.parseInt(tokens[1].substring(1));
                    int variableId = Integer.parseInt(tokens[2].substring(1));
                    transactionManager.read(transactionID, variableId, timeStamp);
                }
                else if (option.equals("W")) {
                    int transactionID = Integer.parseInt(tokens[1].substring(1));
                    int variableId = Integer.parseInt(tokens[2].substring(1));
                    int value = Integer.parseInt(tokens[3]);
                    transactionManager.write(transactionID, variableId, value, timeStamp);
                }
                else if (option.equals("fail")) {
                    int siteID = Integer.parseInt(tokens[1]);
                    transactionManager.fail(siteID);
                }
                else if (option.equals("recover")) {
                    int siteID = Integer.parseInt(tokens[1]);
                    transactionManager.recover(siteID);
                }
                else if (option.equals("end")) {
                    String transaction = tokens[1];
                    int transactionID = Integer.parseInt(transaction.substring(1));
                    transactionManager.end(transactionID, timeStamp);
                }
                else {
                    transactionManager.dump();
                }
                timeStamp+=1;
                line = bufferedReader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
