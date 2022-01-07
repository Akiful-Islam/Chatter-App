package com.chaterpackage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatErAppUI extends JFrame implements Runnable {
    private JPanel mainPanel;
    private JButton sendTextButton;
    private JButton sendFileButton;
    private JTextField textBox;
    private JPanel chatBox;
    private JLabel labelTitle;

    private Socket socket;
    private String title;
    boolean isServer;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private boolean isText;
    private ArrayList<FileReceived> fileReceivedArrayList = new ArrayList<>();

    public ChatErAppUI(boolean isServer, String title) {
        this.isServer = isServer;
        this.title = title;
        initialize();


        sendFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select file to send");
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                sendFileMessage(fileChooser.getSelectedFile());
            }
        });

        sendTextButton.addActionListener(e -> {
            sendTextMessage();
        });
    }

    private void sendTextMessage() {
        isText = true;
        String text = textBox.getText();

        try {
            dataOutputStream.writeBoolean(isText);
            dataOutputStream.writeInt(text.getBytes().length);
            dataOutputStream.write(text.getBytes());
            textBox.setText("");
            addMessageToUI(true, text, false);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFileMessage(File file) {
        try {
            isText = false;
            String fileName = file.getName();
            FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());

            dataOutputStream.writeBoolean(isText);

            dataOutputStream.writeInt(fileName.getBytes().length);
            dataOutputStream.write(fileName.getBytes());

            byte[] fileContentBytes = new byte[(int) file.length()];
            fileInputStream.read(fileContentBytes);

            dataOutputStream.writeInt(fileContentBytes.length);
            dataOutputStream.write(fileContentBytes);
            addMessageToUI(false, fileName, false);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initialize() {
        setContentPane(mainPanel);
        setPreferredSize(new Dimension(600, 800));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);

        labelTitle.setText(title);
        chatBox.setLayout(new BoxLayout(chatBox, BoxLayout.Y_AXIS));

    }

    @Override
    public void run() {
        try {
            socket = isServer ? new ServerSocket(1236).accept() : new Socket("localhost", 1236);
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

            while (socket.isConnected()) {
                isText = dataInputStream.readBoolean();
                if (isText) {
                    receiveTextMessage();
                } else {
                    receiveFileMessage();
                }
            }

        } catch (
                IOException e) {
            e.printStackTrace();
            closeEverything();
        }

    }

    private void receiveFileMessage() {
        try {
            int receiveFileNameByteLength = dataInputStream.readInt();
            byte[] receiveFileNameByte = new byte[receiveFileNameByteLength];
            dataInputStream.readFully(receiveFileNameByte, 0, receiveFileNameByteLength);
            String fileName = new String(receiveFileNameByte);

            int receiveFileContentByteLength = dataInputStream.readInt();
            byte[] receiveFileContentByte = new byte[receiveFileContentByteLength];
            dataInputStream.readFully(receiveFileContentByte, 0, receiveFileContentByteLength);
            fileReceivedArrayList.add(new FileReceived(fileReceivedArrayList.size(), fileName, receiveFileContentByte));

            addMessageToUI(false, fileName, true);


        } catch (IOException e) {
            e.printStackTrace();
            closeEverything();
        }
    }

    private void receiveTextMessage() {
        try {
            int receiveByteLength = dataInputStream.readInt();
            byte[] receiveByte = new byte[receiveByteLength];
            dataInputStream.readFully(receiveByte, 0, receiveByteLength);
            addMessageToUI(false, new String(receiveByte), true);
        } catch (IOException e) {
            e.printStackTrace();
            closeEverything();
        }
    }

    private void closeEverything() {

        try {
            if (socket != null) {
                socket.close();
            }
            if (dataInputStream != null) {
                dataInputStream.close();
            }
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addMessageToUI(boolean isText, String message, boolean isReceived) {
        JLabel label = new JLabel();
        label.setText(isText ? "<html>" + message + "</html>" : "<html> <u>" + message + "</u> </html>");
        label.setFont(new Font("Tahoma", Font.PLAIN, 16));
        label.setOpaque(true);
        label.setBorder(new EmptyBorder(10, 10, 10, 10));
        label.setBackground(Color.decode(isReceived ? "#008AFF" : "#D8F3FF"));
        label.setForeground(isReceived ? Color.white : Color.black);
        label.setHorizontalAlignment(isReceived ? SwingConstants.LEFT : SwingConstants.RIGHT);

        if (!isText && isReceived) {
            label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            label.setName(String.valueOf(fileReceivedArrayList.size()));
            label.addMouseListener(getMouseListener());
        }

        chatBox.add(label);
        chatBox.add(Box.createRigidArea(new Dimension(0, 10)));

        validate();
    }

    private MouseListener getMouseListener() {
        return new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int dialogResult = JOptionPane.showConfirmDialog(
                        null,
                        "Download File?",
                        "Prompt",
                        JOptionPane.YES_NO_OPTION);

                if (dialogResult == JOptionPane.YES_OPTION) {
                    JLabel label = (JLabel) e.getSource();
                    for (FileReceived fileReceived : fileReceivedArrayList) {
                        if (fileReceived.getId() + 1 == Integer.parseInt(label.getName())) {
                            try {
                                File file = new File(System.getProperty("user.home") + "/Downloads/" + fileReceived.getName());
                                FileOutputStream fileOutputStream = new FileOutputStream(file);
                                fileOutputStream.write(fileReceived.getData());
                                fileOutputStream.close();

                                JOptionPane.showMessageDialog(null, file, "Downloaded to", JOptionPane.PLAIN_MESSAGE);

                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        };
    }
}

