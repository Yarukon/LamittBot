package me.yarukon.utils.source;

public class ServerPlayer {

	public int playerIndex;
	public String playerName;
	public long playerScore;
	public float playerDuration;

	public ServerPlayer(int index, String name, long score, float duration) {
		this.playerIndex = index;
		this.playerName = name;
		this.playerScore = score;
		this.playerDuration = duration;
	}

	public int getIndex() {
		return this.playerIndex;
	}

	public String getName() {
		return this.playerName;
	}

	public long getScore() {
		return this.playerScore;
	}

	public float getDuration() {
		return this.playerDuration;
	}
}
