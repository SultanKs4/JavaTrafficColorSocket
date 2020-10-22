import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {
    private JPanel root;
    private JTextField textFieldIPAddress;
    private JTextField textFieldPort;
    private JRadioButton radioButtonRedOn;
    private JRadioButton radioButtonYellowOn;
    private JRadioButton radioButtonGreenOn;
    private JRadioButton radioButtonRedOff;
    private JRadioButton radioButtonYellowOff;
    private JRadioButton radioButtonGreenOff;
    private final String MERAH = "Merah";
    private final String KUNING = "Kuning";
    private final String HIJAU = "Hijau";
    private final String HIDUP = "Hidup";
    private final String MATI = "Mati";

    public Client() {
        radioButtonRedOn.addActionListener(actionEvent -> sendData(setMessage(MERAH, HIDUP)));
        radioButtonRedOff.addActionListener(actionEvent -> sendData(setMessage(MERAH, MATI)));
        radioButtonYellowOn.addActionListener(actionEvent -> sendData(setMessage(KUNING, HIDUP)));
        radioButtonYellowOff.addActionListener(actionEvent -> sendData(setMessage(KUNING, MATI)));
        radioButtonGreenOn.addActionListener(actionEvent -> sendData(setMessage(HIJAU, HIDUP)));
        radioButtonGreenOff.addActionListener(actionEvent -> sendData(setMessage(HIJAU, MATI)));
    }

    private String setMessage(String color, String status) {
        return color + "," + status + ",";
    }

    private void sendData(String message) {
        try {
            AtomicInteger messageWritten = new AtomicInteger(0);
            AtomicInteger messageRead = new AtomicInteger(0);

            EchoClient(textFieldIPAddress.getText(), Integer.parseInt(textFieldPort.getText()), message, messageWritten, messageRead);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }

    //////////////////////////
    // Socket Client Helper //
    //////////////////////////
    public void EchoClient(String host, int port, final String message, final AtomicInteger messageWritten, final AtomicInteger messageRead) throws IOException {
        //create a socket channel
        AsynchronousSocketChannel sockChannel = AsynchronousSocketChannel.open();

        //try to connect to the server side
        sockChannel.connect(new InetSocketAddress(host, port), sockChannel, new CompletionHandler<Void, AsynchronousSocketChannel>() {
            @Override
            public void completed(Void result, AsynchronousSocketChannel channel) {
                //start to read message
                startRead(channel, messageRead);

                //write an message to server side
                startWrite(channel, String.valueOf(message), messageWritten);
            }

            @Override
            public void failed(Throwable exc, AsynchronousSocketChannel channel) {
                JOptionPane.showMessageDialog(null, "Failed to connect to Server");
            }

        });
    }

    private void startRead(final AsynchronousSocketChannel sockChannel, final AtomicInteger messageRead) {
        final ByteBuffer buf = ByteBuffer.allocate(2048);

        sockChannel.read(buf, sockChannel, new CompletionHandler<Integer, AsynchronousSocketChannel>() {

            @Override
            public void completed(Integer result, AsynchronousSocketChannel channel) {
                //message is read from server
                messageRead.getAndIncrement();

                //print the message
                System.out.println("Read message:" + new String(buf.array()));
            }

            @Override
            public void failed(Throwable exc, AsynchronousSocketChannel channel) {
                System.out.println("fail to read message from server");
            }
        });
    }

    private void startWrite(final AsynchronousSocketChannel sockChannel, final String message, final AtomicInteger messageWritten) {
        ByteBuffer buf = ByteBuffer.allocate(2048);
        buf.put(message.getBytes());
        buf.flip();
        messageWritten.getAndIncrement();
        sockChannel.write(buf, sockChannel, new CompletionHandler<Integer, AsynchronousSocketChannel>() {
            @Override
            public void completed(Integer result, AsynchronousSocketChannel channel) {
                //after message written
                //NOTHING TO DO
            }

            @Override
            public void failed(Throwable exc, AsynchronousSocketChannel channel) {
                System.out.println("Fail to write the message to server");
            }
        });
    }

    public static void main(String[] args) {
        JFrame gui = new JFrame("Traffic Color Client");
        gui.setContentPane(new Client().root);
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.pack();
        gui.setVisible(true);
    }
}
