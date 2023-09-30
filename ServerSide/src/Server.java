import netscape.javascript.JSObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import org.json.JSONObject;

public class Server {
    static ObjectOutputStream outputStream = null;
    ObjectInputStream inputStream = null;
    public static final int PORT = 7777;
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        new Server();
    }
    public Server() throws IOException, ClassNotFoundException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started on port " + PORT);
        while (true){
            Socket socket = serverSocket.accept(); // wait for a client to connect (keeps the program running!

             outputStream = new ObjectOutputStream(socket.getOutputStream());

             inputStream = new ObjectInputStream(socket.getInputStream());

            Packet receivedPacket = (Packet) inputStream.readObject();
            System.out.println(receivedPacket.fromCurrency);

            if (receivedPacket.fromCurrency !=null){
                System.out.println("Received packet from client: " + receivedPacket.fromCurrency + " " + receivedPacket.toCurrency + " " + receivedPacket.amount);
                calculateResult(receivedPacket.fromCurrency,receivedPacket.toCurrency,receivedPacket.amount);

            }
            socket.close();
        }

        
    }
    static void calculateResult(
            String fromCurrency, String toCurrency, double amount
    ) throws IOException {
        double result = 0.0;
        try{
            String apiUrl = "https://v6.exchangerate-api.com/v6/023cfc04483a123782938bf2/latest/"+ fromCurrency;
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null){
                    response.append(line);
                }
                reader.close();

                // parse Json response
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONObject conversion_rates = jsonResponse.getJSONObject("conversion_rates");

                // get the exchage rate for the toCurrency
                double exchangeRate = conversion_rates.getDouble(toCurrency);
                result = amount * exchangeRate;
            }
        }catch ( Exception e){
            System.out.println(e);
        }

        Packet sendPacket = new Packet(result);
        outputStream.writeObject(sendPacket);
    }
}