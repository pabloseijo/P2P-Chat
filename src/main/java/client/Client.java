package client;

import server.ServerInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Client {

    private static final Scanner scanner = new Scanner(System.in); // acordarse de cerrarlo despues.

    //TODO: Añadir nombre y puerto exportar como atributos?
    //private String nombreUsuario = "";

    //MAIN: nos conectamos al registro RMI y exportamos un objeto MessageHandlerImpl
    public static void main(String[] args) {

        // Pedimos puerto y dirección IP.
        System.out.println("Bienvenido al cliente de mensajería RMI.\n");
        System.out.println("DATOS PARA EXPORTAR OBJETO MESSAGE HANDLER RMI:");
        int puertoMessageHandlerRMI = pedirPuerto(scanner);

        // Exportamos objeto RMI... (antes de conectarse a server para poder pasar referencia)
        MessageHandlerInterface messageHandlerInterfaceRMI = exportarMessageHandlerConPuerto(puertoMessageHandlerRMI);

        // Intentar conectarse al servidor RMI
        System.out.println("\nDATOS PARA CONECTARSE A OBJETO SERVER RMI:");
        String direccionIP = pedirDireccionIP(scanner);
        int puerto = pedirPuerto(scanner);


        //try-catch: para gestionar RemoteException.
        ServerInterface servidor = null;
        String nombre = null;
        try {
            servidor = obtenerServerRMI(direccionIP, puerto);
            if (servidor != null) {
                nombre = pedirNombreUsuario(scanner);
                System.out.println("\nCliente conectado exitosamente al servidor RMI.\n");
                servidor.conectarCliente(nombre, messageHandlerInterfaceRMI); //Mandamos referencia a objeto RMI exportado.
                // El metodo conectarCliente ya se encarga de hacer las gestiones de notificación del servidor.
            } else {
                System.out.println("No se pudo conectar al servido RMI. Verifique la dirección IP y el puerto.");
                return;
            }
        } catch (RemoteException e) {
            System.err.println("Error de comunicación RMI: " + e.getMessage());
            e.printStackTrace();
        }

        // Menú para mensajería.

        //1. Enviar mensaje: el input se pide en el main, el metodo solo realiza las tareas necesarias.
        enviarMensajeUsuario("cliente1", servidor, "hola que tal", nombre);


    }


    // Metodo para pedir un puerto válido al que conectarse:
    private static int pedirPuerto(Scanner scanner) {
        int puerto = -1;

        // Solicitar un puerto válido
        while (puerto < 1024 || puerto > 65535) {
            System.out.print("Por favor, ingrese un puerto válido (1024-65535): ");
            if (scanner.hasNextInt()) {
                puerto = scanner.nextInt();
                if (puerto < 1024 || puerto > 65535) {
                    System.out.println("El puerto debe estar entre 1024 y 65535.");
                }
            } else {
                System.out.println("Entrada no válida. Por favor ingrese un número entero.");
                scanner.next(); // Limpiar la entrada no válida
            }
        }

        // Consumir el salto de línea sobrante después de nextInt
        scanner.nextLine();
        return puerto;
    }

    // Metodo para pedir una dirección IP válida a la que conectarse:
    private static String pedirDireccionIP(Scanner scanner) {
        String direccionIP = "";
        int intentos = 3; // Número máximo de intentos permitidos

        while (intentos > 0) {
            System.out.print("Por favor, ingrese una dirección IP válida (o presione Enter para usar localhost): ");
            direccionIP = scanner.nextLine().trim(); // Leer la línea completa

            // Si no se introduce nada, usar localhost
            if (direccionIP.isEmpty()) {
                System.out.println("No se ingresó ninguna dirección. Usando 'localhost' como dirección IP.");
                return "localhost";
            }

            // Validar la dirección IP con una expresión regular
            if (direccionIP.matches(
                    "^((25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)$")) {
                return direccionIP; // Dirección IP válida
            } else {
                intentos--;
                System.out.println("La dirección IP ingresada no es válida. Intentos restantes: " + intentos);
            }
        }

        // Si se agotan los intentos, usar localhost
        System.out.println("Se agotaron los intentos. Usando 'localhost' como dirección IP.");
        return "localhost";
    }

    // Metodo para obtener instancia del objeto RMI.
    private static ServerInterface obtenerServerRMI(String direccionIP, int puerto) {
        try {
            // Localizar el registro RMI en la dirección y puerto especificados
            Registry registry = LocateRegistry.getRegistry(direccionIP, puerto);

            // Buscar la instancia exportada del servidor por su nombre
            ServerInterface servidor = (ServerInterface) registry.lookup("server");

            System.out.println("\nConexión exitosa al servidor en " + direccionIP + ":" + puerto);
            return servidor;

        } catch (Exception e) {
            System.err.println("Error al conectar con el servidor: " + e.getMessage());
            e.printStackTrace();
            return null; // Devolver null si no se pudo conectar
        }
    }

    // Metodo para exportar un objeto MessageHandlerImpl al registro RMI.
    private static MessageHandlerInterface exportarMessageHandlerConPuerto(int puerto)  {
        try {
            // Crear una instancia del objeto remoto
            MessageHandlerInterface messageHandler = new MessageHandlerImpl();

            // Crear o localizar el registro RMI
            Registry registry;
            try {
                // Intentamos crear el registro en el puerto dado
                registry = LocateRegistry.createRegistry(puerto);
            } catch (RemoteException e) {
                // Si ya existe, lo localizamos
                registry = LocateRegistry.getRegistry(puerto);
            }

            // Registrar la instancia del objeto remoto en el registro RMI
            registry.rebind("messageHandler", messageHandler);
            System.out.println("Objeto RMI MessageHandler exportado correctamente en el puerto: " + puerto);

            return messageHandler; // Devolver la referencia exportada

        } catch (RemoteException e) {
            System.err.println("Error al exportar el objeto MessageHandlerInterface: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Metodo para pedir un nombre para el cliente:
    private static String pedirNombreUsuario(Scanner scanner) {
        String nombreCliente = "";
        int intentos = 3; // Número máximo de intentos permitidos

        while (intentos > 0) {
            System.out.print("Por favor, ingrese un nombre para el cliente: ");
            nombreCliente = scanner.nextLine().trim(); // Leer la línea completa y eliminar espacios

            // Si no se introduce nada, volver a pedir
            if (nombreCliente.isEmpty()) {
                intentos--;
                System.out.println("El nombre no puede estar vacío. Intentos restantes: " + intentos);
                continue;
            }

            // Validar el nombre (opcional, sin caracteres especiales)
            if (nombreCliente.matches("^[a-zA-Z0-9_\\-]+$")) {
                return nombreCliente; // Nombre válido
            } else {
                intentos--;
                System.out.println("El nombre ingresado no es válido. Solo se permiten letras, números, guiones y guiones bajos. Intentos restantes: " + intentos);
            }
        }

        // Si se agotan los intentos, lanzar una excepción o manejar el error
        throw new IllegalArgumentException("Se agotaron los intentos para ingresar un nombre válido.");
    }

    // Metodo para enviar un mensaje: llama a solicitarReferenciaUsuario(String cliente)
    private static boolean enviarMensajeUsuario(String nombreUsuarioRecibe, ServerInterface server, String mensaje,
                                         String nombreUsuarioEnvia) {

        // Comprobamos que exista el serverInterface:
        if (server == null) {
            System.out.println("Error al enviar el mensaje: no se ha encontrado el objeto remoto del servidor.");
            return false;
        }
        // Llamamos al metodo del server para solicitar la referencia del usuario al que enviamos mensaje:
        MessageHandlerInterface clienteRecibe = null;
        try {
            clienteRecibe = server.solicitarReferenciaUsuario(nombreUsuarioRecibe);
        } catch (RemoteException e) {
            System.err.println("Error al enviar el mensaje: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        // Comprobamos si existe una referencia al usuario al que queremos mandar mensaje:
        if (clienteRecibe==null) {
            System.err.println("Error al enviar el mensaje: el usuario de destino no se encuentra en línea.");
            return false;
        }
        // Si ha habido éxito, llamamos al metodo remoto: "recibirMensaje" del objeto referenciado:
        try{
            clienteRecibe.recibirMensaje(mensaje, nombreUsuarioEnvia);
        } catch (RemoteException e) {
            System.err.println("Error al enviar el mensaje: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        // En este punto, ha habido éxito en todas las tareas, devolvemos true:
        return true;
    }



}
