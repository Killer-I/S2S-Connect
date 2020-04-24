package killer_i.s2s.s2sconnect.dialogs;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;
import killer_i.s2s.s2sconnect.ActionsMainActivity;
import killer_i.s2s.s2sconnect.utils.BluetoothManager;

public class SelectDeviceDialog extends AlertDialog.Builder {
    ActionsMainActivity.ActionsInterface actionsInterface = null;
    String selectedDeviceID;
    int selectedPosition = 0;
    Object[] deviceList = null;

    public SelectDeviceDialog(Context context, String selectedDeviceID) {
        super(context);
        this.actionsInterface = (ActionsMainActivity.ActionsInterface)context;
        this.selectedDeviceID = selectedDeviceID;
        initialize();
    }

    public void initialize(){
        this.setTitle("Select device");
        String[] item_list = getPairedDevices();
        this.setSingleChoiceItems(item_list, selectedPosition, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedPosition = which;
            }
        });
        this.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendChangedMessage();
                dialog.dismiss();
            }
        });
        this.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        this.setCancelable(false);
    }

    private String[] getPairedDevices() {
        deviceList = BluetoothManager.getPairedDevices();
        String[] deviceListArray = new String[deviceList.length];
        for (int i=0; i<deviceList.length; i++) {
            BluetoothDevice device = (BluetoothDevice) deviceList[i];
            if (device.getAddress().equals(selectedDeviceID)) {
                selectedPosition = i;
            }
            deviceListArray[i] = device.getName();
        }
        return deviceListArray;
    }

    private void sendChangedMessage() {
        BluetoothDevice device = (BluetoothDevice) deviceList[selectedPosition];
        this.actionsInterface.updateSelectedDevice(
                device.getName(),
                device.getAddress()
        );
    }
}
