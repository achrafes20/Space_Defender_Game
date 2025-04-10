public class ServerLauncher {
    public static void main(String[] args) {
        int port = 5555;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Port invalide, utilisation du port par défaut 5555");
            }
        }

        System.out.println("Démarrage du serveur Space Defender sur le port " + port);
        GameServer server = new GameServer();
        server.start();
    }
}