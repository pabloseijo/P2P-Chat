package server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ServerApp {

    // En el main exportaremos una instancia de ServerImpl.
    public static void main(String[] args) {
        try{
            //1. Creamos una instancia del servidor (RMI)
            ServerInterface server = new ServerImpl();

            //2. Solicitamos el puerto donde se desea exportar el registro RMI y lo creamos.
            int puerto = pedirPuerto();
            Registry registry;
            try {
                // Intentamos crear el registro en el puerto dado.
                registry = LocateRegistry.createRegistry(puerto);
            } catch (RemoteException e){ // Si ya existe, lo localizamos.
                registry = LocateRegistry.getRegistry(puerto);
            }

            //3. Registramos la instancia del servidor en el registro RMI con nombre:
            registry.rebind("server", server);
            System.out.println("Servidor RMI iniciado y listo para recibir conexiones.");

        } catch (RemoteException e) {
            System.out.println("Error al iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Función para pedir un puerto al que conectarse:
    private static int pedirPuerto() {
        Scanner scanner = new Scanner(System.in);
        int puerto = -1;

        // Solicitar un puerto válido
        while (puerto < 1024 || puerto > 65535) {
            System.out.print("Por favor, ingrese un puerto válido para exportar el servicio (1024-65535): ");
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
        scanner.close();
        return puerto;
    }

}
