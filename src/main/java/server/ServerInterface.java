package server;

import client.MessageHandlerInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ServerInterface extends Remote {


    //Métodos básicos
    boolean conectarCliente(String nombreCliente, MessageHandlerInterface cliente) throws RemoteException;

    boolean desconectarCliente(String nombreCliente, MessageHandlerInterface cliente) throws RemoteException;

    List<String> obtenerClientesConectadosList() throws RemoteException;

    MessageHandlerInterface solicitarReferenciaUsuario(String nombreUsuario) throws RemoteException; //Devuelve la referencia del objeto rémoto asociado al nombre.


    //métodos avanzados (por ahora no tocar) - despues vamos a tener que pedir clave.

    boolean registrarUsuario() throws RemoteException;

    boolean solicitarAmistad() throws RemoteException;

    boolean aceptarAmistad() throws RemoteException;

    List<String> obtenerListaAmigos() throws RemoteException;


}
