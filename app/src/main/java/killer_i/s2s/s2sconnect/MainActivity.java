package killer_i.s2s.s2sconnect;

import androidx.appcompat.app.AppCompatActivity;
import killer_i.s2s.s2sconnect.dialogs.SelectDeviceDialog;
import killer_i.s2s.s2sconnect.utils.BluetoothManager;
import killer_i.s2s.s2sconnect.utils.Logger;
import killer_i.s2s.s2sconnect.utils.SharedPreferenceService;
import killer_i.s2s.s2sconnect.widget.AppWidget;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ActionsMainActivity.ActionsInterface{
    // Variables
    SharedPreferenceService spService;
    boolean COMMAND_EDIT_STATE = false;

    // Views
    Button changeDevice, changeCommand, test_btn;
    TextView selectedDevice, logsView;
    EditText commandToSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        changeDevice = findViewById(R.id.btn_change_device);
        changeCommand = findViewById(R.id.btn_change_command);
        selectedDevice = findViewById(R.id.tv_selected_device_name);
        commandToSend = findViewById(R.id.et_command);
        logsView = findViewById(R.id.view_logs);
        test_btn = findViewById(R.id.test_btn);
        initServciesAndViews();
    }

    private void initServciesAndViews() {
        spService = new SharedPreferenceService(
                getApplicationContext(),
                getResources().getString(R.string.shared_pref_bluetooth)
        );
        logsView.setMovementMethod(new ScrollingMovementMethod());
        Logger logger = new Logger(MainActivity.this);
        logsView.setText(logger.getLogsAsText());
        changeDevice.setOnClickListener(this);
        changeCommand.setOnClickListener(this);
        test_btn.setOnClickListener(this);
        refreshViews();
    }

    private void refreshViews() {
        selectedDevice.setText(spService.getString(
                getResources().getString(R.string.selected_device)
        ));
        commandToSend.setText(spService.getString(
                getResources().getString(R.string.command_to_send)
        ));
        updateWidget("Update");
    }

    @Override
    public void onClick(View v) {
        if (v == changeDevice) {
            new SelectDeviceDialog(
                    MainActivity.this,
                    spService.getString(getResources().getString(R.string.selected_device_id))
            )
                    .show();
        } else if (v == changeCommand) {
            updatetSendCommand();
        } else if (v == test_btn) {
//            BluetoothManager blm = new BluetoothManager(
//                    (ActionsMainActivity.ActionsInterface) MainActivity.this,
//                    spService.getString(getResources().getString(R.string.selected_device_id))
//            );
//            blm.deviceConnection.start();
            BluetoothManager blm = new BluetoothManager(null, null, new Logger(getApplicationContext()));
            boolean res = blm.sendSerialCommand(
                    spService.getString(getResources().getString(R.string.selected_device_id)),
                    spService.getString(getResources().getString(R.string.command_to_send))
            );
            if (res) {
                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Fail", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updatetSendCommand() {
        if (COMMAND_EDIT_STATE) {
            spService.putValue(
                    getResources().getString(R.string.command_to_send),
                    commandToSend.getText().toString().trim()
            );
            commandToSend.setEnabled(false);
            changeCommand.setText(
                    getResources().getString(R.string.btn_change_command_1)
            );
            COMMAND_EDIT_STATE = false;
        } else {
            commandToSend.setEnabled(true);
            changeCommand.setText(
                    getResources().getString(R.string.btn_change_command_2)
            );
            COMMAND_EDIT_STATE = true;
        }
        updateWidget("Changed");
    }

    @Override
    public void updateSelectedDevice(String deviceName, String deviceID) {
        spService.putValue(
                getResources().getString(R.string.selected_device),
                deviceName
        );
        spService.putValue(
                getResources().getString(R.string.selected_device_id),
                deviceID
        );
        refreshViews();
    }

    @Override
    public void onMessageReceivedFromDevice(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void actionFailed() {
        Toast.makeText(getApplicationContext(), "Failed action", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void updateWidget(String message) {
        AppWidget.updateWidgets(MainActivity.this);
    }
}
