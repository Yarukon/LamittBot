package me.yarukon.utils.source;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SteamServerPlayer {
	public static byte HEADER = (byte)0x44;
	
	public int playersLength;
	public ServerPlayer[] players;
	
	public SteamServerPlayer(byte[] buffer) {
		int position = 0;
		this.playersLength = buffer[position];
		this.players = new ServerPlayer[this.getPlayersLength()];
		
		position++;
		for (int i = 0; i < this.getPlayersLength(); i++) {			
			int playerIndex = buffer[position];
			
			position++;
			int playerNameLength = getStringLength(position, buffer);
			byte[] playerName = new byte[playerNameLength];
			System.arraycopy(buffer, position, playerName, 0, playerNameLength);
			
			
			position = position + playerNameLength + 1;
			long playerScore = ((buffer[position + 3] & 0xFFL) << 24) | ((buffer[position + 2] & 0xFFL) << 16) | ((buffer[position + 1] & 0xFFL) <<  8) | ((buffer[position] & 0xFFL));
			
			position = position + 4;
			float playerDuration = ByteBuffer.wrap(new byte[] {buffer[position], buffer[position +1], buffer[position +2], buffer[position +3]}).order(ByteOrder.LITTLE_ENDIAN).getFloat();
			
			position = position + 4;
			
			this.players[i] = new ServerPlayer(playerIndex, new String(playerName), playerScore, playerDuration);
		}
	}
	
	public int getPlayersLength() {
		return this.playersLength;
	}
	
	public ServerPlayer[] getPlayers() {
		return this.players;
	}
	
	private int getStringLength(int start, byte[] buffer) {
		for (int i = start; i < buffer.length; i++) {
			if (buffer[i] == 0)
				return i - start;
		}
		
		return 0;
	}
	
	public String toString() {
		StringBuilder playerTable = new StringBuilder();
		
		for (ServerPlayer player : this.getPlayers()) {			
			playerTable.append(player.getName()).append((player.getName().length() <= 7) ? "\t\t\t\t" : ((player.getName().length() <= 15) ? "\t\t\t" : ((player.getName().length() <= 23) ? "\t\t" : "\t")));
			playerTable.append(new Long(player.getScore()).intValue()).append("\t\t");
			playerTable.append(Math.round(player.getDuration() / 3600)).append(":").append(Math.round((player.getDuration() % 3600) / 60)).append(":").append(Math.round((player.getDuration() % 3600) % 60)).append("\n");
		}
		
		return "Players: " + this.getPlayersLength() + "\nPlayer Name :\t\t\tScore :\t\tDuration:\n" + playerTable;
	}
}
