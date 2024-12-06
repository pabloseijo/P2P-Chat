package server;

import client.MessageHandlerInterface;
import utils.DatabaseManager;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerImpl extends UnicastRemoteObject implements ServerInterface{

    //Atributos
    private final Map<MessageHandlerInterface, String> usuariosConectados = new HashMap<>(); //Almacena los usuarios conectados
    private DatabaseManager dbManager = new DatabaseManager(); //Gestiona la base de datos

    //Setters y Getters
    public Map<MessageHandlerInterface, String> getUsuariosConectados() {
        return usuariosConectados;
    }

    //Constructor (RMI)
    public ServerImpl(DatabaseManager dbManager) throws RemoteException {
        super();
        this.dbManager = dbManager;
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



    @Override
    public boolean validarUsuario(String nombreCliente, String clave) throws RemoteException {
        return dbManager.validarUsuario(nombreCliente, clave);
    }

    @Override
    public boolean registrarUsuario(String nombreCliente, String clave, MessageHandlerInterface cliente) throws RemoteException {
        if (!dbManager.usuarioExiste(nombreCliente)) {
            dbManager.addUser(nombreCliente, clave);
            return true;
        }
        return false;
    }

    @Override
    public boolean solicitarAmistad(String usuarioSolicitante, String usuarioReceptor) throws RemoteException {
        // Verificar si ambos usuarios existen en la base de datos
        if (dbManager.usuarioExiste(usuarioSolicitante) && dbManager.usuarioExiste(usuarioReceptor)) {
            dbManager.addFriendRequest(usuarioSolicitante, usuarioReceptor);
            return true;
        }
        return false;
    }

    @Override
    public boolean aceptarAmistad(String usuarioSolicitante, String usuarioReceptor) throws RemoteException {
        // Aceptar la solicitud de amistad en la base de datos
        if (dbManager.aceptarSolicitudAmistad(usuarioSolicitante, usuarioReceptor)) {
            // Actualizar amigos conectados (si están en línea)
            MessageHandlerInterface solicitante = solicitarReferenciaUsuario(usuarioSolicitante);
            if (solicitante != null) {
                solicitante.serNotificadoNuevoAmigo(usuarioReceptor);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean desconectarCliente(String nombreCliente, MessageHandlerInterface cliente) throws RemoteException {
        synchronized (usuariosConectados) {
            if (usuariosConectados.containsKey(cliente)) {
                usuariosConectados.remove(cliente);

                // Notificar a los demás usuarios que este cliente se ha desconectado
                for (MessageHandlerInterface usuario : usuariosConectados.keySet()) {
                    usuario.serNotificadoNuevoUsuario(nombreCliente + " se ha desconectado.");
                }
                System.out.println("Cliente desconectado: " + nombreCliente);
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean usuarioExiste(String username) throws RemoteException {
        return dbManager.usuarioExiste(username);
    }

    @Override
    public List<String> obtenerListaAmigos(String nombreCliente) throws RemoteException {
        return dbManager.obtenerAmigos(nombreCliente);
    }

    @Override
    public List<String> obtenerSolicitudesPendientes(String nombreCliente) throws RemoteException {
        return dbManager.obtenerSolicitudesPendientes(nombreCliente);
    }

}
