package camchua.genshinimpactapi.data.user.model.spiralabyss.rank;

public class SpiralAbyssTakeDamageRank {

	private int avatarId;
	private int rarity;
	private int value;

	public SpiralAbyssTakeDamageRank(int avatarId, int rarity, int value) {
		this.avatarId = avatarId;
		this.rarity = rarity;
		this.value = value;
	}

	public int getAvatarId() {
		return avatarId;
	}

	public int getRarity() {
		return rarity;
	}

	public int getValue() {
		return value;
	}

}
