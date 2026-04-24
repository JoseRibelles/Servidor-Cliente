import java.io.*;
import java.net.*;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;

public class ServidorChat {

    private ServerSocket serverSocket;
    public List<ClientHandler> clientes = Collections.synchronizedList(new ArrayList<>()); // Cambiar a public
    private PrivateKey servidorPrivateKey;
    private PublicKey servidorPublicKey;
    private static final int PUERTO = 5000;

    public ServidorChat() {
        try {
            serverSocket = new ServerSocket(PUERTO);

            KeyPair keyPair = CryptoUtil.generateKeyPair(2048);
            servidorPrivateKey = keyPair.getPrivate();
            servidorPublicKey = keyPair.getPublic();

            System.out.println("Servidor iniciado en puerto: " + PUERTO);
            System.out.println("Esperando clientes...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clientes.add(handler);
                new Thread(handler).start();
                System.out.println("Cliente conectado. Total: " + clientes.size());
            }

        } catch (IOException ex) {
            System.err.println("Error en el servidor: " + ex);
        }
    }

    public void broadcastMessage(String mensaje, ClientHandler remitente) {
        for (ClientHandler cliente : clientes) {
            if (cliente != remitente) {
                cliente.enviarMensaje(mensaje);
            }
        }
    }

    public void removerCliente(ClientHandler cliente) {
        clientes.remove(cliente);
        System.out.println("Cliente desconectado. Total: " + clientes.size());
    }

    public PublicKey getServidorPublicKey() {
        return servidorPublicKey;
    }

    public PrivateKey getServidorPrivateKey() {
        return servidorPrivateKey;
    }

    public static void main(String[] args) {
        new ServidorChat();
    }
}