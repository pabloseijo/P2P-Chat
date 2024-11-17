package client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class MessageHandlerImpl extends UnicastRemoteObject implements MessageHandlerInterface {

    // Constructor
    public MessageHandlerImpl() throws RemoteException {
        super();
    }

    @Override
    public void recibirMensaje(String mensaje, String usuarioEnvia) throws RemoteException {
        System.out.println("\nTe ha llegado un nuevo mensaje de " + usuarioEnvia + ":\n");
        System.out.println(mensaje);
    }

    @Override
    public void serNotificadoUsuariosConectados(List<String> listaUsuariosConectados) throws RemoteException {
        System.out.println("Usuarios online actualmente:");
        for (String usuario : listaUsuariosConectados) {
            System.out.println("- " + usuario);
        }
        if (listaUsuariosConectados.isEmpty()) {
            System.out.println("No hay usuarios conectados en este momento.");
        }
    }

    @Override
    public void serNotificadoNuevoUsuario(String nombreCliente) throws RemoteException {
        System.out.println("Nuevo usuario online: " + nombreCliente);
    }

    @Override
    public void serNotificadoNuevoAmigo(String nombreAmigo) throws RemoteException {
        System.out.println("¡Tienes un nuevo amigo! " + nombreAmigo + " ahora es tu amigo.");
    }
}
