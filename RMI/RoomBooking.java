public interface RoomBooking extends MyRemote {

    public boolean book(String booking, Date date);
    public String[] allBookingsOn(Date date);

}
