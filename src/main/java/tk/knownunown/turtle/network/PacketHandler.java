package tk.knownunown.turtle.network;

import java.net.DatagramPacket;

import tk.knownunown.turtle.Turtle;
import tk.knownunown.turtle.network.minecraftpe.misc.AcknowledgePacket;
import tk.knownunown.turtle.network.raknet.*;
import tk.knownunown.turtle.network.minecraftpe.*;

/**
 * Created by andrew on 5/31/14.
 */
public class PacketHandler extends Thread {

    protected DatagramPacket packet;
    protected NetworkHandler networkHandler;

    public PacketHandler(NetworkHandler networkHandler, DatagramPacket packet){
        Turtle.log("[PacketHandler] new PacketHandler started with packet ID " + packet.getData()[0]);
        this.packet = packet;
        this.networkHandler = networkHandler;
    }
    @SuppressWarnings("unchecked")
    public void run(){
        switch(packet.getData()[0]){
            case RakPacketInfo.ConnectedPing:
            case RakPacketInfo.UnconnectedPing: {
                ConnectedPing recv = new ConnectedPing(packet);
                recv.decode();
                Turtle.log("Received packet " + RakPacketInfo.ConnectedPing + " from "  + packet.getAddress().toString() + ":" + packet.getPort() + " :)");
                Turtle.log("0x01 Ping ID: " + String.valueOf(recv.pingID));
                UnconnectedPingOpenConnections reply = new UnconnectedPingOpenConnections(packet.getAddress(), packet.getPort());
                reply.pingID = recv.pingID;
                reply.serverID = ProtocolInfo.serverID;
                reply.identifier = Turtle.getIdentifier();
                reply.encode();
                Turtle.log("[PacketHandler] Adding packet " + RakPacketInfo.UnconnectedPingOpenConnections + " to NetworkHandler queue for sending :)");
                networkHandler.sendQueue.add(reply.getPacket());
                break;
            }
            case RakPacketInfo.OpenConnectionRequest1: {
                OpenConnectionRequest1 recv = new OpenConnectionRequest1(packet);
                recv.decode();
                if((int) recv.structure == ProtocolInfo.currentStructure){
                    OpenConnectionReply1 reply = new OpenConnectionReply1(packet.getAddress(), packet.getPort());
                    reply.serverID = ProtocolInfo.serverID;
                    reply.security = 0;
                    reply.mtu = recv.mtu;
                    reply.encode();
                    networkHandler.sendQueue.add(reply.getPacket());
                } else {
                    Turtle.log("[PacketHandler] Encountered client using incorrect structure #" + recv.structure + ", sending 0x1A");
                    IncompatibleProtocolVersion reply = new IncompatibleProtocolVersion(recv.ip, recv.port);
                    reply.structure = ProtocolInfo.currentStructure;
                    reply.serverID = ProtocolInfo.serverID;
                    reply.encode();
                    networkHandler.sendQueue.add(reply.getPacket());
                }
                break;
            }
            case RakPacketInfo.OpenConnectionRequest2: {
                OpenConnectionRequest2 recv = new OpenConnectionRequest2(packet);
                recv.decode();
                OpenConnectionReply2 reply = new OpenConnectionReply2(packet.getAddress(), packet.getPort());
                reply.serverID = ProtocolInfo.serverID;
                Turtle.log(String.valueOf(reply.mtu));
                reply.mtu = recv.mtu;
                reply.security = 0; //as security is always off, constant value
                reply.encode();
                networkHandler.sendQueue.add(reply.getPacket());
            }
            case PEPacketInfo.ReadyPacket: {

            }
        }
    }
}
