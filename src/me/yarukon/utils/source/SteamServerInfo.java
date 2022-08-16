package me.yarukon.utils.source;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class SteamServerInfo {
	private int position = 0;

	public static byte HEADER = (byte) 0x49;
	private int serverProtocol;
	private byte[] serverName;
	private byte[] serverMap;
	private byte[] serverFolder;
	private byte[] serverGame;
	private short serverAppID;
	private int serverPlayers;
	private int serverMaxPlayers;
	private int serverBots;
	private int serverType;
	private int serverEnvironment;
	private int serverVisibility;
	private int serverVAC;
	private byte[] serverVersion;
	private int serverEDF;

	private long latency;

	public SteamServerInfo(byte[] buffer, long latency) {
		this.serverProtocol = buffer[0];

		this.position++;
		int serverNameLength = this.getStringLength(this.position, buffer);
		this.serverName = new byte[serverNameLength];
		System.arraycopy(buffer, this.position, this.serverName, 0, serverNameLength);

		this.position = this.position + serverNameLength + 1;
		int serverMapLength = this.getStringLength(this.position, buffer);
		this.serverMap = new byte[serverMapLength];
		System.arraycopy(buffer, this.position, this.serverMap, 0, serverMapLength);

		this.position = this.position + serverMapLength + 1;
		int serverFolderLength = this.getStringLength(this.position, buffer);
		this.serverFolder = new byte[serverFolderLength];
		System.arraycopy(buffer, this.position, this.serverFolder, 0, serverFolderLength);

		this.position = this.position + serverFolderLength + 1;
		int serverGameLength = this.getStringLength(this.position, buffer);
		this.serverGame = new byte[serverGameLength];
		System.arraycopy(buffer, this.position, this.serverGame, 0, serverGameLength);

		this.position = this.position + serverGameLength + 1;
		this.serverAppID = ByteBuffer.wrap(buffer, this.position, this.position + 1).order(ByteOrder.LITTLE_ENDIAN)
				.getShort();

		this.position = this.position + 2;
		this.serverPlayers = buffer[this.position];

		this.position++;
		this.serverMaxPlayers = buffer[this.position];

		this.position++;
		this.serverBots = buffer[this.position];

		this.position++;
		this.serverType = buffer[this.position];

		this.position++;
		this.serverEnvironment = buffer[this.position];

		this.position++;
		this.serverVisibility = buffer[this.position];

		this.position++;
		this.serverVAC = buffer[this.position];

		this.position++;
		int serverVersionLength = getStringLength(this.position, buffer);
		this.serverVersion = new byte[serverVersionLength];
		System.arraycopy(buffer, this.position, this.serverVersion, 0, serverVersionLength);

		this.position = this.position + serverVersionLength + 1;
		this.serverEDF = buffer[this.position];

		this.latency = latency;
	}

	public int getProtocol() {
		return this.serverProtocol;
	}

	public String getName() {
		return new String(this.serverName, StandardCharsets.UTF_8);
	}

	public String getMap() {
		return new String(this.serverMap);
	}

	public String getFolder() {
		return new String(this.serverFolder);
	}

	public String getGame() {
		return new String(this.serverGame);
	}

	public short getAppID() {
		return this.serverAppID;
	}

	public int getPlayers() {
		return this.serverPlayers;
	}

	public int getMaxPlayers() {
		return this.serverMaxPlayers;
	}

	public int getBots() {
		return this.serverBots;
	}

	public int getType() {
		return this.serverType;
	}

	public int getEnvironment() {
		return this.serverEnvironment;
	}

	public int getVisibility() {
		return this.serverVisibility;
	}

	public int getVAC() {
		return this.serverVAC;
	}

	public String getVersion() {
		return new String(this.serverVersion);
	}

	public int getEDF() {
		return this.serverEDF;
	}

	public long getLatency() {
		return latency;
	}

	public String getNormalServerType() {
		switch ((char) this.getType()) {
			case 'l':
				return "Linux";

			case 'w':
				return "Windows";

			case 'm':
				return "MacOS";

			default:
				return "Unknown";
		}
	}


	private int getStringLength(int start, byte[] buffer) {
		for (int i = start; i < buffer.length; i++) {
			if (buffer[i] == 0)
				return i - start;
		}

		return 0;
	}

	public String toString() {
		return "Protocol : " + this.getProtocol() + "\nName : " + this.getName() + "\nMap : " + this.getMap()
				+ "\nFolder : " + this.getFolder() + "\nGame : " + this.getGame() + "\nAppID : " + this.getAppID()
				+ "\nPlayers : " + this.getPlayers() + "\nMax Players : " + this.getMaxPlayers() + "\nBots : "
				+ this.getBots() + "\nServer Type : " + (char) this.getType()
				+ " (d = DEDICATED|l = NON-DEDICATED|p = SourceTV/proxy)\nEnvironment : " + (char) this.getEnvironment()
				+ " (l = Linux|w = Windows|m = MAC)\nVisibility : " + this.getVisibility()
				+ " (0 = Public|1 = Private)\nVAC : " + this.getVAC() + " (0 = UNSECURED|1 = SECURED)\nVersion : "
				+ this.getVersion() + "\nExtra Data Flag (EDF) : " + this.getEDF();
	}
}
