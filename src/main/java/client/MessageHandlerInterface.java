package client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface MessageHandlerInterface extends Remote {

    // Métodos para mensajería
    void recibirMensaje(String mensaje, String usuarioEnvia) throws RemoteException;

    // Notificación de usuarios conectados
    void serNotificadoUsuariosConectados(List<String> listaUsuariosConectados) throws RemoteException;

    // Notificación de un nuevo usuario conectado
    void serNotificadoNuevoUsuario(String nombreCliente) throws RemoteException;

    // Notificación de un nuevo amigo
    void serNotificadoNuevoAmigo(String nombreAmigo) throws RemoteException; // Nuevo método
}
