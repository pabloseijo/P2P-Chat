package client;

import javax.swing.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class MessageHandlerImpl extends UnicastRemoteObject implements MessageHandlerInterface {

    private final ChatClientApp chatApp;

    public MessageHandlerImpl(ChatClientApp chatApp) throws RemoteException {
        super();
        this.chatApp = chatApp;
    }

    @Override
    public void recibirMensaje(String mensaje, String usuarioEnvia) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            chatApp.agregarMensajeAConversacion(usuarioEnvia, usuarioEnvia + ": " + mensaje);
            if (usuarioEnvia.equals(chatApp.getUsuarioSeleccionado())) {
                chatApp.actualizarChatArea();
            }
        });
    }


    @Override
    public void serNotificadoUsuariosConectados(List<String> listaUsuariosConectados) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            chatApp.updateOnlineUsers(listaUsuariosConectados.toArray(new String[0]));
            chatApp.addMessage("La lista de usuarios conectados ha sido actualizada.");
        });
    }


    @Override
    public void serNotificadoNuevoUsuario(String nombreCliente) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            chatApp.addMessage("Nuevo usuario conectado: " + nombreCliente);
            // Solicitar la lista completa de usuarios conectados al servidor
            try {
                List<String> listaUsuariosActualizados = Client.getServer().obtenerClientesConectadosList();
                chatApp.updateOnlineUsers(listaUsuariosActualizados.toArray(new String[0]));
            } catch (RemoteException e) {
                chatApp.showError("Error al actualizar la lista de usuarios conectados: " + e.getMessage());
            }
        });
    }

    @Override
    public void serNotificadoNuevaSolicitud(String usuarioSolicitante) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            chatApp.addMessage("Nueva solicitud de amistad de: " + usuarioSolicitante);
            actualizarSolicitudesPendientes();
        });
    }

    private void actualizarSolicitudesPendientes() {
        try {
            List<String> solicitudes = Client.getServer().obtenerSolicitudesPendientes(chatApp.getUsername());
            chatApp.updatePendingRequests(solicitudes.toArray(new String[0]));
        } catch (RemoteException e) {
            chatApp.showError("Error al actualizar solicitudes pendientes: " + e.getMessage());
        }
    }

    @Override
    public void serNotificadoNuevoAmigo(String nombreAmigo) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            chatApp.addMessage("¡Tienes un nuevo amigo! " + nombreAmigo + " ahora es tu amigo.");
            actualizarListaAmigos();
        });
    }

    // Método para actualizar la lista de usuarios conectados
    private void actualizarListaUsuariosConectados() {
        try {
            List<String> usuariosConectados = Client.getServer().obtenerClientesConectadosList();
            chatApp.updateOnlineUsers(usuariosConectados.toArray(new String[0]));
        } catch (RemoteException e) {
            chatApp.showError("Error al actualizar la lista de usuarios conectados: " + e.getMessage());
        }
    }

    // Método para actualizar la lista de amigos
    private void actualizarListaAmigos() {
        try {
            List<String> listaAmigos = Client.getServer().obtenerListaAmigos(chatApp.getUsername());
            chatApp.updateFriendsList(listaAmigos.toArray(new String[0]));
        } catch (RemoteException e) {
            chatApp.showError("Error al actualizar la lista de amigos: " + e.getMessage());
        }
    }
}
