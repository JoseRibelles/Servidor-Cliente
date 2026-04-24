import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Scanner;

public class ClienteChat {

    private Socket socket;
    private PrintWriter salida;
    private BufferedReader entrada;
    private PrivateKey clientePrivateKey;
    private PublicKey clientePublicKey;
    private PublicKey servidorPublicKey;
    private String nombreUsuario;
    private static final String HOST = "localhost";
    private static final int PUERTO = 5000;

    public ClienteChat(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
        try {
            socket = new Socket(HOST, PUERTO);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            KeyPair keyPair = CryptoUtil.generateKeyPair(2048);
            clientePrivateKey = keyPair.getPrivate();
            clientePublicKey = keyPair.getPublic();

            String clientePublicKeyStr = CryptoUtil.publicKeyToString(clientePublicKey);
            salida.println(clientePublicKeyStr);

            String servidorPublicKeyStr = entrada.readLine();
            servidorPublicKey = CryptoUtil.stringToPublicKey(servidorPublicKeyStr);

            System.out.println("Conectado al servidor. Bienvenido " + nombreUsuario + "!");

            String nombreEncriptado = CryptoUtil.encrypt(nombreUsuario.getBytes(), servidorPublicKey);
            salida.println(nombreEncriptado);

            new Thread(new ReceptorMensajes()).start();
            iniciarEnviador();

        } catch (IOException ex) {
            System.err.println("Error conectando al servidor: " + ex);
        }
    }

    private void iniciarEnviador() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String mensaje = scanner.nextLine();
            if (mensaje.equalsIgnoreCase("SALIR")) {
                salida.println("SALIR");
                break;
            }

            String mensajeEncriptado = CryptoUtil.encrypt(mensaje.getBytes(), servidorPublicKey);
            salida.println(mensajeEncriptado);
        }
        System.exit(0);
    }

    private class ReceptorMensajes implements Runnable {
        @Override
        public void run() {
            try {
                String linea;
                while ((linea = entrada.readLine()) != null) {
                    String mensajeDesencriptado = CryptoUtil.decrypt(linea, clientePrivateKey);
                    System.out.println("\n" + mensajeDesencriptado);
                    System.out.print("Tú: ");
                }
            } catch (IOException ex) {
                System.err.println("Conexión perdida: " + ex);
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingresa tu nombre de usuario : ");
        String nombre = scanner.nextLine();
        new ClienteChat(nombre);
    }
}