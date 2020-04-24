package killer_i.s2s.s2sconnect;

public class ActionsMainActivity {
    public interface ActionsInterface {
        void updateSelectedDevice(String deviceName, String deviceID);
        void onMessageReceivedFromDevice(String message);
        void actionFailed();
        void updateWidget(String message);
    }
}
