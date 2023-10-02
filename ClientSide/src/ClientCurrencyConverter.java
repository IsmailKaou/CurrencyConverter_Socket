
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.NumberFormatter;

public class ClientCurrencyConverter {

    static Socket socket = null;
     static  ObjectOutputStream outputStream = null;
     static ObjectInputStream inputStream = null;


    public static void main(String[] args) throws IOException {
        Map<String,String> countries = countries();




        SwingUtilities.invokeLater(() -> {
            createAndShowGUI(countries);
        });
    }

    private static void createAndShowGUI(Map<String,String> countries) {
        // Create the main JFrame
        JFrame frame = new JFrame("Currency Converter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        // Create a JPanel to hold the content
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Create a JPanel for the header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Color.lightGray);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel headerLabel = new JLabel("Currency Converter");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headerPanel.add(headerLabel);

        // Create a JPanel for the form
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(5, 1));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Result label
        JLabel resultLabel = new JLabel("Getting Exchange ...");

        // Amount input
        JPanel amountPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel amountLabel = new JLabel("Amount");

        // Create a formatter to only allow numbers and a maximum of 2 decimals
        JFormattedTextField amountField = new JFormattedTextField();



        Dimension amountFieldSize = new Dimension(300, 24);
        amountField.setPreferredSize(amountFieldSize);
        amountPanel.add(amountLabel);
        amountPanel.add(amountField);

        // From and To selectors
        JPanel fromToPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel fromLabel = new JLabel("From");
        JLabel toLabel = new JLabel("To");
        JComboBox<String> fromComboBox = new JComboBox<>();
        JComboBox<String> toComboBox = new JComboBox<>();

//        Create label for flag

        JLabel fromFlag = new JLabel();
        JLabel toFlag = new JLabel();

        for (String key : countries.keySet()){
            fromComboBox.addItem(key);
            toComboBox.addItem(key);
        }

        fromComboBox.setSelectedItem("USD");
        toComboBox.setSelectedItem("EUR");

        setFlagIcon(fromFlag, fromComboBox);
        setFlagIcon(toFlag, toComboBox);

        // Add action listeners to JComboBoxes to change flag icons
        fromComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setFlagIcon(fromFlag, fromComboBox);
            }
        });

        toComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setFlagIcon(toFlag, toComboBox);
            }
        });

//        Button to reverse the currencies
        ImageIcon icon = new ImageIcon("src/exchange.png");
        JButton reverseButton = new JButton(icon);

        reverseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reverseCurrency(fromComboBox, toComboBox,fromFlag, toFlag);

            }
        });



        amountField.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        Runnable format = new Runnable() {
                            @Override
                            public void run() {
                                String text = amountField.getText();
                                if (!text.matches("\\d*(\\.\\d{0,2})?")) {
                                    amountField.setText(text.substring(0, text.length() - 1));
                                }
                            }
                        };
                        SwingUtilities.invokeLater(format);
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {

                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {

                    }
                }
        );
        fromToPanel.add(fromLabel);
        fromToPanel.add(fromFlag);
        fromToPanel.add(fromComboBox);
        fromToPanel.add(reverseButton);
        fromToPanel.add(toLabel);
        fromToPanel.add(toFlag);
        fromToPanel.add(toComboBox);



        // Button
        JButton getRateButton = new JButton("Convert");

        getRateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    socket = new Socket("localhost", 7777);
                    outputStream = new ObjectOutputStream(socket.getOutputStream());

                    inputStream = new ObjectInputStream(socket.getInputStream());
                    getExchangeRate(fromComboBox,toComboBox,amountField,resultLabel);
                } catch (IOException | ClassNotFoundException ex) {
                    System.out.println("error in getExchangeRate");
                    System.out.println(ex.getMessage());
                }finally {
                    try {
                        socket.close();
                        outputStream.close();
                        inputStream.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }

                }

            }
        });



        // Add components to the form panel
        formPanel.add(amountPanel);
        formPanel.add(fromToPanel);
        formPanel.add(resultLabel);
        formPanel.add(getRateButton);

        // Add header and form to the main panel
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);

        // Add the main panel to the frame
        frame.add(panel);

        // Make the JFrame visible
        frame.setVisible(true);
    }

    public static void reverseCurrency(JComboBox<String> fromComboBox, JComboBox<String> toComboBox, JLabel fromFlag, JLabel toFlag){
        String from = fromComboBox.getSelectedItem().toString();
        String to = toComboBox.getSelectedItem().toString();

        fromComboBox.setSelectedItem(to);
        toComboBox.setSelectedItem(from);

        setFlagIcon(fromFlag, fromComboBox);
        setFlagIcon(toFlag, toComboBox);
    }

    public static void setFlagIcon(JLabel label,JComboBox<String> comboBox){
        String code = comboBox.getSelectedItem().toString();
        String countryCode = countries().get(code);
        String flagImageUrl = "https://flagcdn.com/48x36/" + countryCode.toLowerCase() + ".png";

        try {
            BufferedImage image = ImageIO.read(new URL(flagImageUrl));

            Image scaledImage = image.getScaledInstance(24, 18, Image.SCALE_SMOOTH);
            ImageIcon flagIcon = new ImageIcon(scaledImage);


            label.setIcon(flagIcon);
        } catch (IOException e) {
            System.out.println("Error setting flag icon");
            e.printStackTrace();
            label.setIcon(null);
        }
    }

    public static void getExchangeRate(JComboBox<String> fromComboBox, JComboBox<String> toComboBox, JFormattedTextField amountField,JLabel resultLabel) throws IOException, ClassNotFoundException {
        Packet sendPacket = new Packet(fromComboBox.getSelectedItem().toString(), toComboBox.getSelectedItem().toString(), Double.parseDouble(amountField.getText()));

        outputStream.writeObject(sendPacket);

        Packet receivedPacket = (Packet) inputStream.readObject();

        //System.out.println(receivedPacket.result);

        resultLabel.setText(amountField.getText() + " " + fromComboBox.getSelectedItem().toString() + " = " + receivedPacket.result + " " + toComboBox.getSelectedItem().toString());
        /*outputStream.close();
        socket.close();*/

    }

    public static Map<String,String> countries(){
        Map<String,String> countries = new HashMap<>();
        countries.put("USD","US");
        countries.put("EUR","EU");
        countries.put("GBP","GB");
        countries.put("JPY","JP");
        countries.put("MAD","MA");
        countries.put("CAD","CA");
        countries.put("CHF","CH");
        countries.put("AUD","AU");
//        countries.put("CNY","CN");
//        countries.put("HKD","HK");
//        countries.put("NZD","NZ");
//        countries.put("SEK","SE");
//        countries.put("KRW","KR");
//        countries.put("SGD","SG");
//        countries.put("NOK","NO");

        return countries;
    }
}

