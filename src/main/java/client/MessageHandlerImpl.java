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
        SwingUtilities.invokeLater(() -> chatApp.addMessage(usuarioEnvia + ": " + mensaje));
    }

    @Override
    public void serNotificadoUsuariosConectados(List<String> listaUsuariosConectados) throws RemoteException {
        SwingUtilities.invokeLater(() -> chatApp.updateOnlineUsers(listaUsuariosConectados.toArray(new String[0])));
    }

    @Override
    public void serNotificadoNuevoUsuario(String nombreCliente) throws RemoteException {
        SwingUtilities.invokeLater(() -> chatApp.addMessage("Nuevo usuario conectado: " + nombreCliente));
    }

    @Override
    public void serNotificadoNuevoAmigo(String nombreAmigo) throws RemoteException {
        SwingUtilities.invokeLater(() -> chatApp.addMessage("¡Tienes un nuevo amigo! " + nombreAmigo + " ahora es tu amigo."));
    }
}
