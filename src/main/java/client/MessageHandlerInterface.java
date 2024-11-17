package client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface MessageHandlerInterface extends Remote {

    //Métodos para mensajería:
    void recibirMensaje(String mensaje, String usuarioEnvia) throws RemoteException;

    //Ser notificado usuarios conectados:
    void serNotificadoUsuariosConectados(List<String> listaUsuariosConectados) throws RemoteException;

    //Metodo para notificar al resto de clientes de que se conecto un cliente, es un metodo RMI del cliente:
    void serNotificadoNuevoUsuario(String nombreCliente) throws RemoteException;

}
