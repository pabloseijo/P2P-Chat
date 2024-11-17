package client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface MessageHandlerInterface extends Remote {

    void recibirMensaje(String mensaje, String usuarioEnvia) throws RemoteException;

    void serNotificadoUsuariosConectados(List<String> listaUsuariosConectados) throws RemoteException;

    void serNotificadoNuevoUsuario(String nombreCliente) throws RemoteException;

    void serNotificadoNuevoAmigo(String nombreAmigo) throws RemoteException; // Asegúrate de incluir throws RemoteException
}
