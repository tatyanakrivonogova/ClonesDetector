package main.java.nsu.fit.krivonogova;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class ClonesDetector {
    private static final int PORT = 8888;
    private static final int PERIOD = 1000;
    private static final int TIMEOUT = 2*PERIOD;
    private static final int SENDER_DELAY = 0;
    private static final byte[] buffer = "i am alive".getBytes(); //array of bytes for receiving messages
    private static final Timer sendTimer = new Timer(true); //timer with daemon flag, main thread will not wait for timer's exiting
    public static void detectClones(InetAddress multicastAddress) {
        try (MulticastSocket recvSocket = new MulticastSocket(PORT);
             DatagramSocket sendSocket = new DatagramSocket()) {

            recvSocket.joinGroup(multicastAddress);
            recvSocket.setSoTimeout(TIMEOUT);

            Map<InetSocketAddress, Long> aliveClones = new HashMap<>();
            DatagramPacket packetForReceiving = new DatagramPacket(buffer, buffer.length);

            launchSender(multicastAddress, sendSocket);

            while (true) {
                boolean hasUpdated = false;
                try {
                    recvSocket.receive(packetForReceiving);

                    if (aliveClones.put(new InetSocketAddress(packetForReceiving.getAddress(), packetForReceiving.getPort()), System.currentTimeMillis()) == null) {
                        System.out.println("New clone has joined the group: " + packetForReceiving.getSocketAddress());
                        hasUpdated = true;
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Connection has lost");
                    break;
                }
                Iterator<Map.Entry<InetSocketAddress, Long>> iterator = aliveClones.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<InetSocketAddress, Long> clone = iterator.next();
                    if (System.currentTimeMillis() - clone.getValue() > TIMEOUT) {
                        System.out.println("Clone " + packetForReceiving.getSocketAddress() + " has left the group");
                        hasUpdated = true;
                        iterator.remove();
                    }
                }
                if (hasUpdated) printAliveClones(aliveClones);
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

    private static void printAliveClones(Map<InetSocketAddress, Long> aliveClones) {
        System.out.println("\n" + aliveClones.size() + " alive clones was detected:");
        int index = 1;
        for (Map.Entry<InetSocketAddress, Long> clone : aliveClones.entrySet()) {
            System.out.println(index + ": " + clone.getKey());
            ++index;
        }
    }
}
