public interface PrimaryBackup extends MyRemote {

    void join(String backup);
    void stateTransfer();
    void kill();

}
