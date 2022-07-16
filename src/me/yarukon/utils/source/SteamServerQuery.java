package me.yarukon.utils.source;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class SteamServerQuery {
	
	private InetAddress serverAddress;
	private int serverPort;
	private DatagramSocket UDPClient;
	
	public SteamServerQuery(InetAddress Address, int Port) {
		try {
			this.UDPClient = new DatagramSocket();
			this.UDPClient.setSoTimeout(2000);
			this.serverAddress = Address;
			this.serverPort = Port;
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public SteamServerQuery(String address, int port) throws UnknownHostException {
		this(InetAddress.getByName(address), port);
	}
	
	public SteamServerQuery(String address) throws UnknownHostException {
		this(address.split(":")[0], Integer.parseInt(address.split(":")[1]));
	}

	public SteamServerInfo getInfo() {
		try {
			byte[] infoHeader = new byte[25];
			infoHeader[0] = (byte)0xFF;
			infoHeader[1] = (byte)0xFF;
			infoHeader[2] = (byte)0xFF;
			infoHeader[3] = (byte)0xFF;
			infoHeader[4] = (byte)0x54;
			byte[] sourceString = "Source Engine Query".getBytes();
			System.arraycopy(sourceString, 0, infoHeader, 5, sourceString.length);
			infoHeader[5 + sourceString.length] = (byte) 0x00;

			long time = System.currentTimeMillis();
			DatagramPacket sendInfoPacket = new DatagramPacket(infoHeader, infoHeader.length, this.serverAddress, this.serverPort);
			this.UDPClient.send(sendInfoPacket);

			byte[] receivedBuffer = new byte[512];
			DatagramPacket receivedInfoPacket = new DatagramPacket(receivedBuffer, receivedBuffer.length);
			this.UDPClient.receive(receivedInfoPacket);
			time = System.currentTimeMillis() - time;
			
			if (receivedBuffer[0] == (byte)0xFF && receivedBuffer[1] == (byte)0xFF && receivedBuffer[2] == (byte)0xFF && receivedBuffer[3] == (byte)0xFF && receivedBuffer[4] == SteamServerInfo.HEADER) {
				byte[] serverInfoBuffer = new byte[receivedBuffer.length - 5];
				System.arraycopy(receivedBuffer, 5, serverInfoBuffer, 0, serverInfoBuffer.length);
				return new SteamServerInfo(serverInfoBuffer, time);
			} else if (receivedBuffer[0] == (byte)0xFF && receivedBuffer[1] == (byte)0xFF && receivedBuffer[2] == (byte)0xFF && receivedBuffer[3] == (byte)0xFF && receivedBuffer[4] == SteamServerChallenge.HEADER) {
				//服务器返回CHALLENGE后再发送附上了CHALLENGE的A2S_INFO请求
				infoHeader = new byte[29];
				infoHeader[0] = (byte)0xFF;
				infoHeader[1] = (byte)0xFF;
				infoHeader[2] = (byte)0xFF;
				infoHeader[3] = (byte)0xFF;
				infoHeader[4] = (byte)0x54;

				System.arraycopy(sourceString, 0, infoHeader, 5, sourceString.length);
				infoHeader[5 + sourceString.length] = (byte) 0x00;

				// 附上CHALLENGE
				infoHeader[25] = receivedBuffer[5];
				infoHeader[26] = receivedBuffer[6];
				infoHeader[27] = receivedBuffer[7];
				infoHeader[28] = receivedBuffer[8];

				sendInfoPacket = new DatagramPacket(infoHeader, infoHeader.length, this.serverAddress, this.serverPort);
				this.UDPClient.send(sendInfoPacket);

				receivedBuffer = new byte[512];
				receivedInfoPacket = new DatagramPacket(receivedBuffer, receivedBuffer.length);
				this.UDPClient.receive(receivedInfoPacket);

				if (receivedBuffer[0] == (byte)0xFF && receivedBuffer[1] == (byte)0xFF && receivedBuffer[2] == (byte)0xFF && receivedBuffer[3] == (byte)0xFF && receivedBuffer[4] == SteamServerInfo.HEADER) {
					byte[] serverInfoBuffer = new byte[receivedBuffer.length - 5];
					System.arraycopy(receivedBuffer, 5, serverInfoBuffer, 0, serverInfoBuffer.length);
					return new SteamServerInfo(serverInfoBuffer, time);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return null;
	}
	
	public SteamServerPlayer getPlayer() {
		try {			
			byte[] playerHeader = this.getChallenge();
			playerHeader[4] = (byte)0x55;
			
			DatagramPacket sendPlayerPacket = new DatagramPacket(playerHeader, playerHeader.length, this.serverAddress, this.serverPort);
			this.UDPClient.send(sendPlayerPacket);
			
			byte[] receivedPlayerBuffer = new byte[8192];
			DatagramPacket receivedPlayerPacket = new DatagramPacket(receivedPlayerBuffer, receivedPlayerBuffer.length);
			this.UDPClient.receive(receivedPlayerPacket);
			
			if (receivedPlayerBuffer[0] == (byte)0xFF && receivedPlayerBuffer[1] == (byte)0xFF && receivedPlayerBuffer[2] == (byte)0xFF && receivedPlayerBuffer[3] == (byte)0xFF && receivedPlayerBuffer[4] == SteamServerPlayer.HEADER) {
				byte[] serverPlayerBuffer = new byte[receivedPlayerBuffer.length - 5];
				System.arraycopy(receivedPlayerBuffer, 5, serverPlayerBuffer, 0, serverPlayerBuffer.length);
				return new SteamServerPlayer(serverPlayerBuffer);
			} else {
				System.err.println("ERROR Player Packet!");
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public byte[] getChallenge() {
		try {
			byte[] challengeHeader = new byte[9];
			challengeHeader[0] = (byte)0xFF;
			challengeHeader[1] = (byte)0xFF;
			challengeHeader[2] = (byte)0xFF;
			challengeHeader[3] = (byte)0xFF;
			challengeHeader[4] = (byte)0x55;
			challengeHeader[5] = (byte)0xFF;
			challengeHeader[6] = (byte)0xFF;
			challengeHeader[7] = (byte)0xFF;
			challengeHeader[8] = (byte)0xFF;
			
			DatagramPacket sendChallengePacket = new DatagramPacket(challengeHeader, challengeHeader.length, this.serverAddress, this.serverPort);
			this.UDPClient.send(sendChallengePacket);
			
			byte[] receivedChallengeBuffer = new byte[9];
			DatagramPacket receivedChallengePacket = new DatagramPacket(receivedChallengeBuffer, receivedChallengeBuffer.length);
			this.UDPClient.receive(receivedChallengePacket);
			
			if(receivedChallengeBuffer[0] == (byte)0xFF && receivedChallengeBuffer[1] == (byte)0xFF && receivedChallengeBuffer[2] == (byte)0xFF && receivedChallengeBuffer[3] == (byte)0xFF && receivedChallengeBuffer[4] == SteamServerChallenge.HEADER) {
				return receivedChallengeBuffer;
			} else {
				System.err.println("ERROR Challenge Packet!");
				return new byte[9];
			}
		} catch(IOException e) {
			e.printStackTrace();
			return new byte[9];
		}
	}
}
