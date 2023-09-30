import java.io.Serializable;

public class Packet implements Serializable {
    String fromCurrency;
    String toCurrency;
    double amount;
    double result;

    public Packet(String fromCurrency, String toCurrency, double amount) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.amount = amount;
    }
    public Packet(double result){
        this.result = result;
    }

}
