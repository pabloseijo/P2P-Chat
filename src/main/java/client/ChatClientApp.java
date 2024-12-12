package client;

import org.mindrot.jbcrypt.BCrypt;
import server.ServerInterface;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ChatClientApp extends JFrame {

    private Consumer<String[]> onConnectCallback; // Callback para conectar al servidor
    private BiConsumer<String, String> onSendMessageCallback; // Callback para enviar mensajes

    private JTextArea chatArea; // Área para mostrar mensajes
    private JTextField messageField; // Campo para escribir mensajes
    private JList<String> onlineUsers; // Lista de usuarios conectados
    private DefaultListModel<String> onlineUsersModel; // Modelo para usuarios conectados
    private JList<String> friendsList; // Lista de amigos
    private DefaultListModel<String> friendsListModel; // Modelo para amigos
    private JList<String> pendingRequestsList; // Lista de solicitudes de amistad pendientes
    private DefaultListModel<String> pendingRequestsModel; // Modelo para solicitudes pendientes
    private String username; // Nombre del usuario actual
    // Formateador de la hora
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final Map<String, List<String>> conversaciones = new HashMap<>();
    private String usuarioSeleccionado = null; // Usuario actualmente seleccionado



    public ChatClientApp() {
        // Configuración de la ventana principal
        setTitle("Chat RMI");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        setupMenu();
        setupMainLayout();
        setupCloseListener();
    }

    private void setupMainLayout() {
        // Panel central: área de chat
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        add(chatScrollPane, BorderLayout.CENTER);

        // Panel inferior: campo de mensaje
        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        JButton sendButton = new JButton("Enviar");
        sendButton.addActionListener(e -> sendMessage());
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        // Panel derecho: listas de usuarios y amigos
        JPanel rightPanel = new JPanel(new GridLayout(3, 1));

        // Usuarios conectados
        onlineUsersModel = new DefaultListModel<>();
        onlineUsers = new JList<>(onlineUsersModel);
        onlineUsers.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                usuarioSeleccionado = onlineUsers.getSelectedValue();
                actualizarChatArea(); // Actualiza la conversación del usuario seleccionado
            }
        });
        JScrollPane onlineUsersScroll = new JScrollPane(onlineUsers);
        onlineUsersScroll.setBorder(BorderFactory.createTitledBorder("Usuarios Conectados"));
        rightPanel.add(onlineUsersScroll);

        // Lista de amigos
        friendsListModel = new DefaultListModel<>();
        friendsList = new JList<>(friendsListModel);
        friendsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                usuarioSeleccionado = friendsList.getSelectedValue();
                actualizarChatArea(); // Actualiza la conversación del usuario seleccionado
            }
        });
        JScrollPane friendsScroll = new JScrollPane(friendsList);
        friendsScroll.setBorder(BorderFactory.createTitledBorder("Amigos"));
        rightPanel.add(friendsScroll);

        // Solicitudes de amistad pendientes
        pendingRequestsModel = new DefaultListModel<>();
        pendingRequestsList = new JList<>(pendingRequestsModel);
        JScrollPane pendingRequestsScroll = new JScrollPane(pendingRequestsList);
        pendingRequestsScroll.setBorder(BorderFactory.createTitledBorder("Solicitudes Pendientes"));
        rightPanel.add(pendingRequestsScroll);

        // Agregar el panel derecho a la ventana principal
        add(rightPanel, BorderLayout.EAST);
    }



    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Archivo");
        JMenuItem exitItem = new JMenuItem("Salir");
        exitItem.addActionListener(e -> confirmAndExit());
        fileMenu.add(exitItem);

        JMenu friendsMenu = new JMenu("Amigos");
        JMenuItem sendRequestItem = new JMenuItem("Enviar Solicitud de Amistad");
        sendRequestItem.addActionListener(e -> sendFriendRequest());
        friendsMenu.add(sendRequestItem);

        JMenuItem manageRequestsItem = new JMenuItem("Gestionar Solicitudes");
        manageRequestsItem.addActionListener(e -> manageFriendRequests());
        friendsMenu.add(manageRequestsItem);

        menuBar.add(fileMenu);
        menuBar.add(friendsMenu);
        setJMenuBar(menuBar);
    }

    private void setupCloseListener() {
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                confirmAndExit();
            }
        });
    }

    public void showConnectDialog() {
        if (onConnectCallback == null) {
            throw new IllegalStateException("El callback de conexión no está configurado. Usa setOnConnect antes de llamar a este método.");
        }

        JTextField ipField = new JTextField("localhost");
        JTextField portField = new JTextField("1099");

        Object[] message = {"Dirección IP:", ipField, "Puerto:", portField};

        int option = JOptionPane.showConfirmDialog(this, message, "Conexión al servidor", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String ip = ipField.getText().trim();
            String port = portField.getText().trim();
            if (!ip.isEmpty() && !port.isEmpty()) {
                try {
                    int portNumber = Integer.parseInt(port);
                    onConnectCallback.accept(new String[]{ip, String.valueOf(portNumber), username});
                } catch (NumberFormatException e) {
                    showError("El puerto debe ser un número.");
                    showConnectDialog();
                }
            } else {
                showError("Todos los campos son obligatorios.");
                showConnectDialog();
            }
        } else {
            System.exit(0);
        }
    }

    public boolean showLoginDialog(ServerInterface server) {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        Object[] message = {"Usuario:", usernameField, "Contraseña:", passwordField};

        while (true) {
            int option = JOptionPane.showOptionDialog(
                    this,
                    message,
                    "Inicio de Sesión",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"Iniciar Sesión", "Registrar", "Cancelar"},
                    "Iniciar Sesión"
            );

            if (option == 2 || option == JOptionPane.CLOSED_OPTION) { // Cancelar
                System.exit(0);
            } else if (option == 1) { // Registrar
                showRegisterDialog(server);
                continue;
            }

            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (!username.isEmpty() && !password.isEmpty()) {
                try {
                    if (server.validarUsuario(username, password)) {
                        setUsername(username);

                        // Actualizar las solicitudes pendientes al iniciar sesión
                        List<String> solicitudes = server.obtenerSolicitudesPendientes(username);
                        updatePendingRequests(solicitudes.toArray(new String[0]));

                        return true; // Inicio de sesión exitoso
                    } else {
                        showError("Usuario o contraseña incorrectos.");
                    }
                } catch (RemoteException e) {
                    showError("Error de conexión: " + e.getMessage());
                }
            } else {
                showError("Usuario y contraseña son obligatorios.");
            }
        }
    }


    private void confirmAndExit() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Estás seguro de que deseas desconectarte y salir?",
                "Confirmar Desconexión",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            Client.desconectarCliente(this);
            dispose();
            System.exit(0);
        }
    }

    private void sendMessage() {
        String recipient = onlineUsers.getSelectedValue();
        String message = messageField.getText().trim();

        if (recipient != null && !message.isEmpty()) {
            onSendMessageCallback.accept(recipient, message);
            agregarMensajeAConversacion(recipient, "Tú: " + message);
            actualizarChatArea();
            messageField.setText("");
        } else {
            showError("Selecciona un usuario y escribe un mensaje.");
        }
    }

    public void agregarMensajeAConversacion(String usuario, String mensaje) {
        conversaciones.putIfAbsent(usuario, new ArrayList<>());
        conversaciones.get(usuario).add(mensaje);
    }

    public void actualizarChatArea() {
        chatArea.setText(""); // Limpiar el área de chat
        if (usuarioSeleccionado != null && conversaciones.containsKey(usuarioSeleccionado)) {
            List<String> mensajes = conversaciones.get(usuarioSeleccionado);
            for (String mensaje : mensajes) {
                chatArea.append(mensaje + "\n");
            }
        }
    }

    public String getUsuarioSeleccionado() {
        return usuarioSeleccionado;
    }

    private void sendFriendRequest() {
        String friendUsername = JOptionPane.showInputDialog(this, "Introduce el nombre del usuario:");
        if (friendUsername != null && !friendUsername.trim().isEmpty()) {
            try {
                boolean success = Client.getServer().solicitarAmistad(username, friendUsername);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Solicitud de amistad enviada correctamente.");
                } else {
                    // Mensaje más específico para solicitudes duplicadas o usuario inexistente
                    showError("No se pudo enviar la solicitud. El usuario no existe o ya hay una solicitud pendiente.");
                }
            } catch (RemoteException e) {
                showError("Error al enviar solicitud: " + e.getMessage());
            }
        } else {
            showError("El nombre del usuario no puede estar vacío.");
        }
    }


    private void manageFriendRequests() {
        try {
            List<String> requests = Client.getServer().obtenerSolicitudesPendientes(username);
            pendingRequestsModel.clear();
            pendingRequestsModel.addAll(requests);

            String selectedRequest = (String) JOptionPane.showInputDialog(
                    this, "Selecciona una solicitud para aceptar:", "Solicitudes Pendientes",
                    JOptionPane.PLAIN_MESSAGE, null, requests.toArray(), null);

            if (selectedRequest != null) {
                // Aceptar la solicitud en el servidor
                boolean success = Client.getServer().aceptarAmistad(selectedRequest, username);

                if (success) {
                    JOptionPane.showMessageDialog(this, "Solicitud aceptada. " + selectedRequest + " es tu amigo.");

                    // Actualizar lista de amigos y solicitudes pendientes
                    actualizarAmigos();
                    List<String> updatedRequests = Client.getServer().obtenerSolicitudesPendientes(username);
                    updatePendingRequests(updatedRequests.toArray(new String[0]));
                } else {
                    showError("Error al aceptar la solicitud.");
                }
            }
        } catch (RemoteException e) {
            showError("Error al gestionar solicitudes: " + e.getMessage());
        }
    }


    public void actualizarAmigos() {
        try {
            List<String> friends = Client.getServer().obtenerListaAmigos(username);
            friendsListModel.clear();
            friendsListModel.addAll(friends);
        } catch (RemoteException e) {
            showError("Error al actualizar amigos: " + e.getMessage());
        }
    }

    private void openChatWithFriend() {
        String selectedFriend = friendsList.getSelectedValue();
        if (selectedFriend != null) {
            addMessage("Iniciando chat con " + selectedFriend);
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
        setTitle("Chat RMI - " + username);
    }

    public String getUsername() {
        return username;
    }

    public void updateFriendsList(String[] friends) {
        friendsListModel.clear();
        for (String friend : friends) {
            friendsListModel.addElement(friend);
        }
    }


    // Metemos el método de hasheo con BCrypt porque mete el salt automáticamente
    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public void showRegisterDialog(ServerInterface server) {
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
                    "Registrar Usuario",
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
                break; // Salir si el usuario cancela
            }

            String newUsername = usernameField.getText().trim();
            String newPassword = new String(passwordField.getPassword());

            if (!newUsername.isEmpty() && !newPassword.isEmpty()) {
                try {
                    boolean registered = server.registrarUsuario(newUsername, newPassword, null);
                    if (registered) {
                        JOptionPane.showMessageDialog(this, "Registro exitoso. Inicia sesión.");
                        break; // Salir después de un registro exitoso
                    } else {
                        showError("El nombre de usuario ya existe. Inténtalo de nuevo.");
                    }
                } catch (RemoteException e) {
                    showError("Error de conexión al servidor: " + e.getMessage());
                    break;
                }
            } else {
                showError("El nombre de usuario y la contraseña no pueden estar vacíos.");
            }
        }
    }

    public void updatePendingRequests(String[] requests) {
        pendingRequestsModel.clear();
        for (String request : requests) {
            pendingRequestsModel.addElement(request);
        }
    }

}