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

                    boolean loggedIn = false;

                    // Mostrar el diálogo de inicio de sesión o registro
                    while (!loggedIn) {
                        loggedIn = chatApp.showLoginDialog(server); // Maneja login y registro
                    }

                    // Crear y exportar el objeto MessageHandler
                    int rmiPort = 1099;
                    messageHandler = exportarMessageHandler(rmiPort, chatApp);

                    // Conectar al servidor
                    boolean connected = server.conectarCliente(chatApp.getUsername(), messageHandler);
                    if (connected) {
                        chatApp.addMessage("Conectado exitosamente al servidor.");
                        nombreUsuario = chatApp.getUsername();
                        cargarDatosIniciales(chatApp); // Actualiza amigos y solicitudes
                    } else {
                        chatApp.showError("Error: No se pudo conectar al servidor.");
                        System.exit(0);
                    }
                } catch (Exception e) {
                    chatApp.showError("Error al conectar al servidor: " + e.getMessage());
                    e.printStackTrace();
                    System.exit(0);
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

            // Manejo del cierre de la aplicación
            chatApp.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    int confirm = JOptionPane.showConfirmDialog(
                            chatApp,
                            "¿Estás seguro de que deseas desconectarte y salir?",
                            "Confirmar Cierre",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (confirm == JOptionPane.YES_OPTION) {
                        desconectarCliente(chatApp);
                        System.exit(0);
                    }
                }
            });

            // Mostrar el diálogo de conexión
            chatApp.showConnectDialog();

            // Hacer visible la GUI
            chatApp.setVisible(true);
        });
    }


    private static void cargarDatosIniciales(ChatClientApp chatApp) {
        try {
            chatApp.actualizarAmigos();
        } catch (Exception e) {
            chatApp.showError("Error al cargar datos iniciales: " + e.getMessage());
        }
    }

    private static MessageHandlerInterface exportarMessageHandler(int puerto, ChatClientApp chatApp) {
        try {
            MessageHandlerInterface messageHandler = new MessageHandlerImpl(chatApp);

            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(puerto);
            } catch (RemoteException e) {
                registry = LocateRegistry.getRegistry(puerto);
            }

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
            chatApp.addMessage(chatApp.getUsername() + ": " + message);
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

    public static ServerInterface getServer() {
        return server;
    }
}
