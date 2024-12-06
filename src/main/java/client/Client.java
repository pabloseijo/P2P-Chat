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

            // Mostrar la ventana inicial para solicitar nombre de usuario
            String username = askForUsername(chatApp);
            if (username == null || username.isEmpty()) {
                System.exit(0); // Salir si el usuario cancela
            }

            chatApp.setUsername(username);

            // Configurar la conexión al servidor
            chatApp.setOnConnect(params -> {
                String ip = params[0];
                int port = Integer.parseInt(params[1]);

                try {
                    Registry registry = LocateRegistry.getRegistry(ip, port);
                    server = (ServerInterface) registry.lookup("server");

                    // Crear y exportar el objeto MessageHandler
                    int rmiPort = 1099; // Cambia esto si el puerto es dinámico
                    messageHandler = exportarMessageHandler(rmiPort, chatApp);

                    // Conectar al servidor
                    boolean connected = server.conectarCliente(username, messageHandler);
                    if (connected) {
                        chatApp.addMessage("Conectado exitosamente al servidor.");
                        nombreUsuario = username;
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

            // Mostrar el diálogo de conexión y la GUI principal
            chatApp.setVisible(true);
            chatApp.showConnectDialog();
        });
    }

    private static String askForUsername(JFrame parent) {
        JTextField usernameField = new JTextField();
        Object[] message = {"Introduce tu nombre de usuario:", usernameField};
        int option = JOptionPane.showConfirmDialog(
                parent,
                message,
                "Nombre de Usuario",
                JOptionPane.OK_CANCEL_OPTION
        );
        if (option == JOptionPane.OK_OPTION) {
            return usernameField.getText().trim();
        }
        return null;
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
}
