package main.java.nsu.fit.krivonogova;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class ClonesDetector {
    private static final int PORT = 1234;
    private static final int PERIOD = 1000;
    private static final int TIMEOUT = 2*PERIOD;
    private static final int SENDER_DELAY = 0;
    private static final byte[] buffer = "i am alive".getBytes();
    private static final Timer sendTimer = new Timer(true);
    public static void detectClones(String multicastAddress) {
        InetAddress multicastInetAddress;
        try {
            multicastInetAddress = InetAddress.getByName(multicastAddress);
        } catch (UnknownHostException e) {
            System.out.println("Multicast address is incorrect");
            multicastInetAddress = null;
        }


        try (MulticastSocket recvSocket = new MulticastSocket(PORT);
             DatagramSocket sendSocket = new DatagramSocket()) {

            NetworkInterface myNetInt = NetworkInterfaceGetter.getNetworkInterface("wlan1");
            if (myNetInt != null) recvSocket.setNetworkInterface(myNetInt);
            else System.out.println("MyNetInt = null");

            recvSocket.joinGroup(multicastInetAddress);
            recvSocket.setSoTimeout(TIMEOUT);

            Map<InetSocketAddress, Long> aliveClones = new HashMap<>();
            DatagramPacket packetForReceiving = new DatagramPacket(buffer, buffer.length);

            launchSender(multicastInetAddress, sendSocket);

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
        System.out.println("\n" + aliveClones.size() + " alive clones were detected:");
        int index = 1;
        for (Map.Entry<InetSocketAddress, Long> clone : aliveClones.entrySet()) {
            System.out.println(index + ": " + clone.getKey().toString().split("/")[1]);
            ++index;
        }
    }
}