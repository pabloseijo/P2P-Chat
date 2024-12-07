package server;

import client.MessageHandlerInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface ServerInterface extends Remote {

    // Métodos básicos
    boolean conectarCliente(String nombreCliente, MessageHandlerInterface cliente) throws RemoteException;

    boolean desconectarCliente(String nombreCliente, MessageHandlerInterface cliente) throws RemoteException;

    List<String> obtenerClientesConectadosList() throws RemoteException;

    MessageHandlerInterface solicitarReferenciaUsuario(String nombreUsuario) throws RemoteException;

    // Métodos avanzados (con interacción con base de datos)

    // Solicitar amistad entre dos usuarios.
    boolean solicitarAmistad(String usuarioSolicitante, String usuarioReceptor) throws RemoteException;

    // Aceptar una solicitud de amistad pendiente.
    boolean aceptarAmistad(String usuarioSolicitante, String usuarioReceptor) throws RemoteException;

    boolean rechazarSolicitudAmistad(String solicitante, String receptor) throws RemoteException;

    Map<String, List<String>> obtenerAmigosYSolicitudes(String nombreCliente) throws RemoteException;

    // Obtener la lista de amigos de un cliente.
    List<String> obtenerListaAmigos(String nombreCliente) throws RemoteException;

    // Obtener solicitudes pendientes de amistad.
    List<String> obtenerSolicitudesPendientes(String nombreCliente) throws RemoteException;

    boolean validarUsuario(String nombreCliente, String clave) throws RemoteException;

    boolean registrarUsuario(String nombreCliente, String clave, MessageHandlerInterface cliente) throws RemoteException;

    boolean usuarioExiste(String username) throws RemoteException;
}
