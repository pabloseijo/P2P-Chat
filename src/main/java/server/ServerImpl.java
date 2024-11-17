package server;

import client.MessageHandlerInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerImpl extends UnicastRemoteObject implements ServerInterface{

    //Atributos
    private final Map<MessageHandlerInterface, String> usuariosConectados = new HashMap<>(); //Almacena los usuarios conectados

    //Setters y Getters
    public Map<MessageHandlerInterface, String> getUsuariosConectados() {
        return usuariosConectados;
    }

    //Constructor (RMI)
    public ServerImpl() throws RemoteException {
        super();
    }

    //Métodos remotos básicos:
    @Override // Vamos a tener que comprobar posteriormente que no este duplicado el nombre.
    public boolean conectarCliente(String nombreCliente, MessageHandlerInterface cliente) throws RemoteException{//posteriormente vamos a pedir clave
        if (!usuariosConectados.containsKey(cliente)) {

            //Metodo para notificar al cliente del resto de clientes conectados, es un método RMI del cliente:
            List<String> listaUsuariosConectados = this.obtenerClientesConectadosList();
            cliente.serNotificadoUsuariosConectados(listaUsuariosConectados);

            //TODO: Metodo para notificar al resto de clientes de que se conecto un cliente, es un metodo RMI del cliente:
            for (MessageHandlerInterface usuario : usuariosConectados.keySet()){
                usuario.serNotificadoNuevoUsuario(nombreCliente);
            }

            // Añadimos al usuario nuevo al mapa de usuariosConectados:
            usuariosConectados.put(cliente, nombreCliente);

            //Indicamos al recolector de basura que no la vamos a utilizar más.
            listaUsuariosConectados = null;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean desconectarCliente(String nombreCliente, MessageHandlerInterface cliente) throws RemoteException{
        return usuariosConectados.remove(cliente) != null;
    }

    @Override
    public List<String> obtenerClientesConectadosList() throws RemoteException{
        return new ArrayList<>(usuariosConectados.values());
    }

    @Override
    public MessageHandlerInterface solicitarReferenciaUsuario(String nombreUsuario) throws RemoteException {
        if (usuariosConectados.containsValue(nombreUsuario)) {
            for (Map.Entry<MessageHandlerInterface, String> usuario : usuariosConectados.entrySet()){
                if (usuario.getValue().equals(nombreUsuario)) {
                    return usuario.getKey();
                }
            }
        }
        return null; //Si no existe el usuario, devolvemos null -> chequeamos null en el cliente.
    }



    //métodos avanzados (por ahora no tocar)
    //vamos a necesitar conexión con una BBDD para el registro y gestión de amigos. Y para clave.
    //las solicitudes de amistad se pueden mandar aunque no esten en linea
    public boolean registrarUsuario(String nombreCliente, String clave, MessageHandlerInterface cliente) throws RemoteException{

        //PRIMERO: Hacer un query a la base de datos para



        return true;
    }

    public boolean solicitarAmistad() throws RemoteException{
        return true;
    }

    public boolean aceptarAmistad() throws RemoteException{
        return true;
    }

    public List<String> obtenerListaAmigos() throws RemoteException{
        return null;
    }




}
