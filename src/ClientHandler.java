import java.io.*;
import java.net.Socket;
import java.security.PublicKey;

public class ClientHandler implements Runnable {

    private Socket socket;
    private ServidorChat servidor;
    private PrintWriter salida;
    private BufferedReader entrada;
    private PublicKey clientePublicKey;
    private String nombreCliente;

    public ClientHandler(Socket socket, ServidorChat servidor) {
        this.socket = socket;
        this.servidor = servidor;
        try {
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        } catch (IOException ex) {
            System.err.println("Error en ClientHandler: " + ex);
        }
    }

    @Override
    public void run() {
        try {
            String clientePublicKeyStr = entrada.readLine();
            clientePublicKey = CryptoUtil.stringToPublicKey(clientePublicKeyStr);

            String servidorPublicKeyStr = CryptoUtil.publicKeyToString(servidor.getServidorPublicKey());
            salida.println(servidorPublicKeyStr);

            String nombreEncriptado = entrada.readLine();
            nombreCliente = CryptoUtil.decrypt(nombreEncriptado, servidor.getServidorPrivateKey());
            System.out.println("Cliente conectado: " + nombreCliente);

            String notificacion = nombreCliente + " se ha conectado.";
            broadcastEncryptedMessage(notificacion, this);

            String linea;
            while ((linea = entrada.readLine()) != null) {
                if (linea.equals("SALIR")) {
                    break;
                }

                String mensajeDesencriptado = CryptoUtil.decrypt(linea, servidor.getServidorPrivateKey());
                System.out.println("[" + nombreCliente + "]: " + mensajeDesencriptado);

                String mensajeFormato = nombreCliente + ": " + mensajeDesencriptado;
                broadcastEncryptedMessage(mensajeFormato, this);
            }
            String despedida = nombreCliente + " se ha desconectado.";
            broadcastEncryptedMessage(despedida, this);
            servidor.removerCliente(this);
            socket.close();

        } catch (IOException ex) {
            System.err.println("Error comunicando con cliente: " + ex);
            servidor.removerCliente(this);
        }
    }

    private void broadcastEncryptedMessage(String mensaje, ClientHandler remitente) {
        for (ClientHandler cliente : servidor.clientes) {
            if (cliente != remitente) {
                String mensajeEncriptado = CryptoUtil.encrypt(mensaje.getBytes(), cliente.clientePublicKey);
                cliente.enviarMensaje(mensajeEncriptado);
            }
        }
    }
    public void enviarMensaje(String mensaje) {
        salida.println(mensaje);
    }

    public PublicKey getClientePublicKey() {
        return clientePublicKey;
    }
}