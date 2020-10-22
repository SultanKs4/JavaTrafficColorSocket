import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class Server {

    private JPanel root;
    private JTextField textFieldIPAddress;
    private JTextField textFieldPort;
    private JButton buttonListen;
    private JLabel labelStatus;
    private JPanel panelColorMerah;
    private JPanel panelColorKuning;
    private JPanel panelColorHijau;
    private final String MERAH = "Merah";
    private final String KUNING = "Kuning";
    private final String HIJAU = "Hijau";
    private final String HIDUP = "Hidup";
    private final String MATI = "Mati";

    public Server() {
        buttonListen.addActionListener(actionEvent -> listenData());
    }

    private void listenData() {
        try {
            new Thread(() -> {
                try {
                    EchoServer(textFieldIPAddress.getText(), Integer.parseInt(textFieldPort.getText()));
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }).start();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }

    private Color chooseColor(String choose) {
        return switch (choose) {
            case MERAH -> Color.RED;
            case KUNING -> Color.YELLOW;
            case HIJAU -> Color.GREEN;
            default -> throw new IllegalStateException("Unexpected value: " + choose);
        };
    }

    public static void main(String[] args) {
        JFrame gui = new JFrame("Color Picker Server");
        gui.setContentPane(new Server().root);
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.pack();
        gui.setVisible(true);
    }

    ///////////////////
    // Socket Server //
    //////////////////
    public void EchoServer(String bindAddr, int bindPort) throws IOException {
        InetSocketAddress sockAddr = new InetSocketAddress(bindAddr, bindPort);

        //create a socket channel and bind to local bind address
        AsynchronousServerSocketChannel serverSock = AsynchronousServerSocketChannel.open().bind(sockAddr);

        //start to accept the connection from client
        serverSock.accept(serverSock, new CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel>() {

            @Override
            public void completed(AsynchronousSocketChannel sockChannel, AsynchronousServerSocketChannel serverSock) {
                //a connection is accepted, start to accept next connection
                serverSock.accept(serverSock, this);
                //start to read message from the client
                startRead(sockChannel);

            }

            @Override
            public void failed(Throwable exc, AsynchronousServerSocketChannel serverSock) {
                System.out.println("fail to accept a connection");
            }

        });
    }

    private void startRead(AsynchronousSocketChannel sockChannel) {
        final ByteBuffer buf = ByteBuffer.allocate(2048);

        //read message from client
        sockChannel.read(buf, sockChannel, new CompletionHandler<Integer, AsynchronousSocketChannel>() {

            /**
             * some message is read from client, this callback will be called
             */
            @Override
            public void completed(Integer result, AsynchronousSocketChannel channel) {
                buf.flip();

                // echo the message
                startWrite(channel, buf);

                //start to read next message again
                startRead(channel);

                // convert and display
                String bufArray = new String(buf.array());
                String[] receiveData = bufArray.split(",");
                Color colorData = Color.BLACK;
                if (receiveData[1].equals(HIDUP)) {
                    colorData = chooseColor(receiveData[0]);
                }
                switch (receiveData[0]) {
                    case MERAH -> panelColorMerah.setBackground(colorData);
                    case KUNING -> panelColorKuning.setBackground(colorData);
                    case HIJAU -> panelColorHijau.setBackground(colorData);
                }
                labelStatus.setText("Lampu " + receiveData[0] + " " + receiveData[1]);
            }

            @Override
            public void failed(Throwable exc, AsynchronousSocketChannel channel) {
                System.out.println("fail to read message from client");
            }
        });
    }

    private void startWrite(AsynchronousSocketChannel sockChannel, final ByteBuffer buf) {
        sockChannel.write(buf, sockChannel, new CompletionHandler<Integer, AsynchronousSocketChannel>() {

            @Override
            public void completed(Integer result, AsynchronousSocketChannel channel) {
                //finish to write message to client, nothing to do
            }

            @Override
            public void failed(Throwable exc, AsynchronousSocketChannel channel) {
                //fail to write message to client
                System.out.println("Fail to write message to client");
            }

        });
    }
}
