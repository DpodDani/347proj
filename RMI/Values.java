public enum Values {
    CLIENT(0), PRIMARY(1099), BACKUP(1100);

	private int value;

	private Values (int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}
}
