package client;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ChatClientApp extends JFrame {

    private Consumer<String[]> onConnectCallback; // Callback para conectar al servidor
    private BiConsumer<String, String> onSendMessageCallback; // Callback para enviar mensajes

    private JTextArea chatArea; // Área para mostrar mensajes
    private JTextField messageField; // Campo para escribir mensajes
    private JList<String> onlineUsers; // Lista de usuarios conectados
    private DefaultListModel<String> onlineUsersModel; // Modelo para gestionar la lista de usuarios
    private String username; // Atributo para almacenar el nombre de usuario

    public ChatClientApp() {
        // Configuración de la ventana principal
        setTitle("Chat RMI");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Crear el menú
        setupMenu();

        // Área de chat
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        add(chatScrollPane, BorderLayout.CENTER);

        // Campo de mensaje y botón de enviar
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        messageField = new JTextField();
        JButton sendButton = new JButton("Enviar");
        sendButton.addActionListener(e -> {
            String recipient = onlineUsers.getSelectedValue();
            String message = messageField.getText().trim();
            if (recipient != null && !message.isEmpty()) {
                if (onSendMessageCallback != null) {
                    onSendMessageCallback.accept(recipient, message);
                    messageField.setText(""); // Limpiar el campo de mensaje
                } else {
                    showError("El envío de mensajes no está configurado.");
                }
            } else {
                showError("Selecciona un usuario y escribe un mensaje.");
            }
        });
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        // Lista de usuarios conectados
        onlineUsersModel = new DefaultListModel<>();
        onlineUsers = new JList<>(onlineUsersModel);
        JScrollPane usersScrollPane = new JScrollPane(onlineUsers);
        usersScrollPane.setPreferredSize(new Dimension(150, 0));
        add(usersScrollPane, BorderLayout.EAST);
    }

    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();

        // Menú Archivo
        JMenu fileMenu = new JMenu("Archivo");
        JMenuItem exitItem = new JMenuItem("Salir");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // Menú Chat
        JMenu chatMenu = new JMenu("Chat");
        JMenuItem newChatItem = new JMenuItem("Nuevo Chat");
        newChatItem.addActionListener(e -> openNewChatWindow());
        chatMenu.add(newChatItem);

        // Agregar menús al menú principal
        menuBar.add(fileMenu);
        menuBar.add(chatMenu);

        setJMenuBar(menuBar);
    }

    private void openNewChatWindow() {
        JTextField recipientField = new JTextField();

        Object[] message = {
                "Usuario para chatear:", recipientField
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                message,
                "Nuevo Chat",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {
            String recipient = recipientField.getText().trim();
            if (!recipient.isEmpty()) {
                addMessage("Iniciando chat con " + recipient + "...");
                // Aquí puedes agregar lógica adicional para gestionar múltiples ventanas de chat
            } else {
                showError("El nombre del usuario no puede estar vacío.");
            }
        }
    }

    public void showConnectDialog() {
        if (onConnectCallback == null) {
            throw new IllegalStateException("El callback de conexión no está configurado. Usa setOnConnect antes de llamar a este método.");
        }

        JTextField ipField = new JTextField("localhost");
        JTextField portField = new JTextField("1099");

        Object[] message = {
                "Dirección IP:", ipField,
                "Puerto:", portField
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                message,
                "Conexión al servidor",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {
            String ip = ipField.getText().trim();
            String port = portField.getText().trim();
            if (!ip.isEmpty() && !port.isEmpty()) {
                try {
                    int portNumber = Integer.parseInt(port);
                    onConnectCallback.accept(new String[]{ip, String.valueOf(portNumber), username});
                } catch (NumberFormatException e) {
                    showError("El puerto debe ser un número.");
                    showConnectDialog(); // Reintentar si el puerto no es válido
                }
            } else {
                showError("Todos los campos son obligatorios.");
                showConnectDialog(); // Reintentar si algún campo está vacío
            }
        } else {
            System.exit(0); // Cerrar la aplicación si se cancela
        }
    }

    public void setOnConnect(Consumer<String[]> onConnectCallback) {
        this.onConnectCallback = onConnectCallback;
    }

    public void setOnSendMessage(BiConsumer<String, String> onSendMessageCallback) {
        this.onSendMessageCallback = onSendMessageCallback;
    }

    public void addMessage(String message) {
        chatArea.append(message + "\n");
    }

    public void updateOnlineUsers(String[] users) {
        onlineUsersModel.clear();
        for (String user : users) {
            onlineUsersModel.addElement(user);
        }
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void setUsername(String username) {
        this.username = username;
        setTitle("Chat RMI - " + username); // Actualizar el título con el nombre del usuario
    }

    public String getUsername() {
        return username;
    }
}
