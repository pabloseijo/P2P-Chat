package client;

import server.ServerInterface;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
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
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Prevenir cierre automático
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

        // Manejo del cierre de la ventana
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        ChatClientApp.this,
                        "¿Estás seguro de que deseas desconectarte y salir?",
                        "Confirmar Desconexión",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    Client.desconectarCliente(ChatClientApp.this);
                    dispose(); // Cerrar la ventana
                    System.exit(0); // Finalizar la aplicación
                }
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
        exitItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Estás seguro de que deseas desconectarte y salir?",
                    "Confirmar Salida",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                Client.desconectarCliente(this);
                dispose();
                System.exit(0);
            }
        });
        fileMenu.add(exitItem);

        // Menú Chat
        JMenu chatMenu = new JMenu("Chat");
        JMenuItem disconnectItem = new JMenuItem("Desconectar");
        disconnectItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "¿Estás seguro de que deseas desconectarte?",
                    "Confirmar Desconexión",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                Client.desconectarCliente(this);
            }
        });
        chatMenu.add(disconnectItem);

        menuBar.add(fileMenu);
        menuBar.add(chatMenu);

        setJMenuBar(menuBar);
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

    public boolean showLoginDialog(ServerInterface server) {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        Object[] message = {
                "Usuario:", usernameField,
                "Contraseña:", passwordField
        };

        while (true) {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    message,
                    "Inicio de Sesión",
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (option == JOptionPane.CANCEL_OPTION) {
                System.exit(0); // Salir si se cancela
            }

            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (!username.isEmpty() && !password.isEmpty()) {
                try {
                    boolean loggedIn = server.validarUsuario(username, password);
                    if (loggedIn) {
                        setUsername(username);
                        return true; // Iniciar sesión con éxito
                    } else {
                        int registerOption = JOptionPane.showConfirmDialog(
                                this,
                                "Usuario no encontrado. ¿Quieres registrarte?",
                                "Registro",
                                JOptionPane.YES_NO_OPTION
                        );
                        if (registerOption == JOptionPane.YES_OPTION) {
                            boolean registered = server.registrarUsuario(username, password, null);
                            if (registered) {
                                JOptionPane.showMessageDialog(this, "Usuario registrado con éxito. Inicia sesión.");
                            } else {
                                JOptionPane.showMessageDialog(this, "Error al registrar usuario.");
                            }
                        }
                    }
                } catch (RemoteException e) {
                    showError("Error de conexión con el servidor: " + e.getMessage());
                }
            } else {
                showError("Usuario y contraseña son obligatorios.");
            }
        }
    }
}
