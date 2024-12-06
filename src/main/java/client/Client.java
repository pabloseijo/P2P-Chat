package client;

import server.ServerInterface;

import javax.swing.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    private static ServerInterface server;
    private static MessageHandlerInterface messageHandler;
    private static String nombreUsuario;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Crear instancia de la GUI principal
            ChatClientApp chatApp = new ChatClientApp();

            // Configurar la conexión al servidor
            chatApp.setOnConnect(params -> {
                String ip = params[0];
                int port = Integer.parseInt(params[1]);

                try {
                    // Conexión al registro RMI
                    Registry registry = LocateRegistry.getRegistry(ip, port);
                    server = (ServerInterface) registry.lookup("server");

                    // Mostrar el diálogo de inicio de sesión o registro
                    if (!chatApp.showLoginDialog(server)) {
                        System.exit(0); // Salir si no se logra iniciar sesión
                    }

                    // Crear y exportar el objeto MessageHandler
                    int rmiPort = 1099; // Cambia esto si el puerto es dinámico
                    messageHandler = exportarMessageHandler(rmiPort, chatApp);

                    // Conectar al servidor
                    boolean connected = server.conectarCliente(chatApp.getUsername(), messageHandler);
                    if (connected) {
                        chatApp.addMessage("Conectado exitosamente al servidor.");
                        nombreUsuario = chatApp.getUsername();
                    } else {
                        chatApp.addMessage("Error: No se pudo conectar al servidor.");
                    }
                } catch (Exception e) {
                    chatApp.addMessage("Error al conectar al servidor: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            // Configurar el envío de mensajes
            chatApp.setOnSendMessage((recipient, message) -> {
                if (server != null) {
                    enviarMensaje(chatApp, recipient, message);
                } else {
                    chatApp.showError("No estás conectado al servidor.");
                }
            });

            // Mostrar el diálogo de conexión
            chatApp.showConnectDialog();

            // Hacer visible la GUI
            chatApp.setVisible(true);
        });
    }


    private static String askForUsername(ChatClientApp chatApp) {
        while (true) {
            JTextField usernameField = new JTextField();
            JPasswordField passwordField = new JPasswordField();

            Object[] message = {
                    "Usuario:", usernameField,
                    "Contraseña:", passwordField
            };

            int option = JOptionPane.showOptionDialog(
                    chatApp,
                    message,
                    "Iniciar Sesión o Registrar",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    new String[]{"Iniciar Sesión", "Registrar", "Cancelar"},
                    "Iniciar Sesión"
            );

            if (option == JOptionPane.CANCEL_OPTION || option == -1) {
                System.exit(0); // Salir si el usuario cancela
            }

            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                chatApp.showError("Usuario y contraseña son obligatorios.");
                continue;
            }

            try {
                if (option == 0) { // Iniciar Sesión
                    if (server.validarUsuario(username, password)) {
                        return username; // Usuario autenticado
                    } else {
                        chatApp.showError("Usuario o contraseña incorrectos.");
                    }
                } else if (option == 1) { // Registrar
                    if (!server.usuarioExiste(username)) {
                        if (server.registrarUsuario(username, password, null)) {
                            JOptionPane.showMessageDialog(chatApp, "Usuario registrado con éxito. Inicia sesión.");
                        } else {
                            chatApp.showError("Error al registrar el usuario.");
                        }
                    } else {
                        chatApp.showError("El usuario ya existe.");
                    }
                }
            } catch (RemoteException e) {
                chatApp.showError("Error de conexión con el servidor: " + e.getMessage());
            }
        }
    }


    private static MessageHandlerInterface exportarMessageHandler(int puerto, ChatClientApp chatApp) {
        try {
            MessageHandlerInterface messageHandler = new MessageHandlerImpl(chatApp);

            // Crear o localizar el registro RMI
            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(puerto);
            } catch (RemoteException e) {
                registry = LocateRegistry.getRegistry(puerto);
            }

            // Registrar el objeto en el registro RMI
            registry.rebind("messageHandler", messageHandler);
            System.out.println("Objeto RMI MessageHandler exportado correctamente en el puerto: " + puerto);

            return messageHandler;
        } catch (RemoteException e) {
            System.err.println("Error al exportar el objeto MessageHandlerInterface: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static void enviarMensaje(ChatClientApp chatApp, String recipient, String message) {
        try {
            MessageHandlerInterface clienteRecibe = server.solicitarReferenciaUsuario(recipient);

            if (clienteRecibe == null) {
                chatApp.addMessage("Error: El usuario de destino no está conectado.");
                return;
            }

            clienteRecibe.recibirMensaje(message, chatApp.getUsername());
            chatApp.addMessage("Mensaje enviado a " + recipient + ": " + message);
        } catch (RemoteException e) {
            chatApp.addMessage("Error al enviar el mensaje: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void desconectarCliente(ChatClientApp chatApp) {
        try {
            if (server != null && nombreUsuario != null && messageHandler != null) {
                boolean success = server.desconectarCliente(nombreUsuario, messageHandler);
                if (success) {
                    chatApp.addMessage("Te has desconectado del servidor.");
                    System.out.println("Cliente desconectado correctamente.");
                } else {
                    chatApp.addMessage("No se pudo desconectar correctamente.");
                }
            }
        } catch (RemoteException e) {
            chatApp.addMessage("Error al intentar desconectar: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
