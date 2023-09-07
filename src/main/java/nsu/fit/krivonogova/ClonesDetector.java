package main.java.nsu.fit.krivonogova;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ClonesDetector {
    private static final int PORT = 8888;
    private static final int PERIOD = 1000;
    private static final int TIMEOUT = 2*PERIOD;
    private static final int SENDER_DELAY = 0;
    private static final String message = "i am alive";
    private static final byte[] buffer = message.getBytes(); //array of bytes for receiving messages
    private static final Timer sendTimer = new Timer(true); //timer with daemon flag, main thread will not wait for timer's exiting
    public static void detectClones(InetAddress multicastAddress) {
        try (MulticastSocket recvSocket = new MulticastSocket(new InetSocketAddress(PORT));
             DatagramSocket sendSocket = new DatagramSocket()) {

            recvSocket.joinGroup(multicastAddress);
            recvSocket.setSoTimeout(TIMEOUT);

            Map<SocketAddress, Long> aliveClones = new HashMap<>();
            DatagramPacket recvPacket = new DatagramPacket(buffer, buffer.length);

            launchSender(multicastAddress, sendSocket);
            while (true) {
                boolean isNewState = false;
                try {
                    recvSocket.receive(recvPacket);

                    if (aliveClones.put(recvPacket.getSocketAddress(), System.currentTimeMillis()) == null) {
                        System.out.println("New clone has joined the group: " + recvPacket.getSocketAddress());
                        isNewState = true;
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Connection has lost");
                    break;
                }
                for (Map.Entry<SocketAddress, Long> clone : aliveClones.entrySet()) {
                    if (System.currentTimeMillis() - clone.getValue() > TIMEOUT) {
                        System.out.println("Clone " + recvPacket.getSocketAddress() + " has left the group");
                        isNewState = true;
                        aliveClones.remove(recvPacket.getSocketAddress());
                    }
                }
                if (isNewState) printAliveClones(aliveClones);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void launchSender(InetAddress multicastAddress, DatagramSocket sendSocket) {
        sendTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    sendSocket.send(new DatagramPacket(buffer, buffer.length, multicastAddress, PORT));
                } catch (IOException e) {
                    System.out.println("IOException while sending messages");
                }
            }
        }, SENDER_DELAY, PERIOD);
    }

    private static void printAliveClones(Map<SocketAddress, Long> aliveClones) {
        System.out.println("\n" + aliveClones.size() + " alive clones was detected:");
        int index = 1;
        for (Map.Entry<SocketAddress, Long> clone : aliveClones.entrySet()) {
            System.out.println(index + ": " + clone.getKey());
        }
    }
}
