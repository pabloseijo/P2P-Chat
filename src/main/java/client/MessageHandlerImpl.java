package client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class MessageHandlerImpl extends UnicastRemoteObject implements MessageHandlerInterface {

    //Constructor
    public MessageHandlerImpl() throws RemoteException {
        super();
    }

    //probablemente haya que añadir getters para nombres y otras cosas.



    @Override
    public void recibirMensaje(String mensaje, String usuarioEnvia) throws RemoteException{ //TODO: vamos a tener que poner quien lo manda:
        System.out.println("\nTe ha llegado un nuevo mensaje de " + usuarioEnvia + ":\n");
        System.out.println(mensaje);
    }

    //Ser notificado usuarios conectados:
    @Override
    public void serNotificadoUsuariosConectados(List<String> listaUsuariosConectados) throws RemoteException{
        // Imprimir el encabezado
        System.out.println("Usuarios online actualmente:");

        // Imprimir cada usuario de la lista
        for (String usuario : listaUsuariosConectados) {
            System.out.println("- " + usuario);
        }

        // Si no hay usuarios en la lista
        if (listaUsuariosConectados.isEmpty()) {
            System.out.println("No hay usuarios conectados en este momento.");
        }
    }

    //Metodo para notificar al resto de clientes de que se conecto un cliente, es un metodo RMI del cliente:
    @Override
    public void serNotificadoNuevoUsuario(String nombreCliente){
        System.out.println("Nuevo usuario online: " + nombreCliente);
    }


}
