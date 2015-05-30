package com.ndnhealthnet.androidudpclient.Comm;

import android.os.AsyncTask;

import com.ndnhealthnet.androidudpclient.Utility.StringConst;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class handles outbound UDP packets and listens when reply expected.
 */
public class UDPSocket extends AsyncTask<byte[], Void, Void> {

    String destAddr, messageType;
    int destPort;

    public UDPSocket(int port, String addr, String type){
        destPort = port;
        destAddr = addr;
        messageType = type;
    }

    @Override
    protected Void doInBackground(byte[]... message) {

        try {
            final DatagramSocket clientSocket = new DatagramSocket();


            InetAddress IPAddress = InetAddress.getByName(destAddr);

            byte[] packetContent = message[0]; // TODO - does this resolve the2d array input issue?

            // NOTE: temporary debugging print
            System.out.println("sent packet: " + message[0]);
            System.out.println("IPADDR: " + IPAddress.toString());
            System.out.println("port: " + destPort);

            DatagramPacket sendPacket = new DatagramPacket(packetContent, packetContent.length, IPAddress, destPort);
            clientSocket.send(sendPacket);

            /**
             * method must listen for incoming packet if INTEREST_TYPE, otherwise
             * we cannot detect incoming, requested packets from the server
             */
            if (messageType.equals(StringConst.INTEREST_TYPE)) {

                // create listener in new thread, listen for 2 seconds
                Timer t = new Timer();
                t.schedule(new TimerTask() {

                    @Override
                    public void run() {

                        byte[] receiveData = new byte[1024];
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                        try {
                            clientSocket.receive(receivePacket);
                            String packetSourceIP = receivePacket.getAddress().getLocalHost().getHostAddress();
                            int packetPort = receivePacket.getPort();

                            UDPListener.handleNDNPacket(receiveData, packetSourceIP, packetPort);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        clientSocket.close();
                        this.cancel();

                    }
                }, 2000L); // keep listener open for 2 seconds
            } else {
                clientSocket.close(); // DATA_TYPE sent, no return expected; close socket
            }

        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return null;
    }
}