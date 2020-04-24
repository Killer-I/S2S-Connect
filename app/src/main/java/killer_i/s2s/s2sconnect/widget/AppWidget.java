package killer_i.s2s.s2sconnect.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.sql.Ref;

import killer_i.s2s.s2sconnect.R;
import killer_i.s2s.s2sconnect.utils.BluetoothManager;
import killer_i.s2s.s2sconnect.utils.Logger;
import killer_i.s2s.s2sconnect.utils.SharedPreferenceService;

public class AppWidget  extends AppWidgetProvider {

    public static class WidgetActions {
        public final static String SEND_MESSAGE = "send_message";
        public final static String REFRESH = "refresh";
    }
    private static final String EXTRA_INTENT = "Message";

    enum CurrentStatus {
        NORMAL, SUCCESS, FAIL
    }

    private static RemoteViews views;
    private static CurrentStatus currentStatus = CurrentStatus.NORMAL;
    private static String hintText = "Click to send the command";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateRemoteViews(context);
        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        hintText = intent.getStringExtra(EXTRA_INTENT);
        currentStatus = CurrentStatus.NORMAL;
        if (intent.getAction().equalsIgnoreCase(WidgetActions.SEND_MESSAGE)) {
            actionSendMessage(context);
        }
    }

    private void actionSendMessage(Context c) {
        updateWidgets(c);
        String[] data = getDeviceAndCommand(c);
        BluetoothManager blm = new BluetoothManager(null, null, new Logger(c));
        boolean res = blm.sendSerialCommand(data[2], data[1]);
        if (res) {
            currentStatus = CurrentStatus.SUCCESS;
            hintText = "Operation success";
        } else {
            currentStatus = CurrentStatus.FAIL;
            hintText = "Operation failed";
        }
        updateWidgets(c);
    }

    private static void updateRemoteViews(Context c) {
        views = new RemoteViews(c.getPackageName(), R.layout.bluetooth_widget);

        // Set hint text color
        int textColor = Color.WHITE;
        if (currentStatus == CurrentStatus.SUCCESS) {
            textColor = Color.GREEN;
        } else if (currentStatus == CurrentStatus.FAIL) {
            textColor = Color.RED;
        }

        // Set Main text
        String[] data = getDeviceAndCommand(c);
        String mainText = (CharSequence) data[0] + " : " + data[1];

        views.setTextViewText(R.id.wid_tv_1, mainText);
        views.setTextViewText(R.id.wid_tv_2, hintText);
        views.setTextColor(R.id.wid_tv_2, textColor);

        views.setOnClickPendingIntent(
                R.id.wid_btn_1,
                getPendingIntent(c, WidgetActions.SEND_MESSAGE, "Sending the command")
        );
    }

    static private String[] getDeviceAndCommand(Context c) {
        SharedPreferenceService spService = new SharedPreferenceService(
                c,
                c.getResources().getString(R.string.shared_pref_bluetooth)
        );
        String command_to_send = spService.getString(
                c.getResources().getString(R.string.command_to_send)
        );
        String selected_device = spService.getString(
                c.getResources().getString(R.string.selected_device)
        );
        String selected_device_id = spService.getString(
                c.getResources().getString(R.string.selected_device_id)
        );
        return new String[]{selected_device, command_to_send, selected_device_id};
    }

    static private PendingIntent getPendingIntent(Context context, String action, String msg) {
        Intent intent = new Intent(context, AppWidget.class);
        intent.setAction(action);
        intent.putExtra(EXTRA_INTENT, msg);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void updateWidgets(Context c) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(c);
        updateRemoteViews(c);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(c, AppWidget.class));
        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

    private static class RefreshThread extends Thread {
        Context c;
        static PendingIntent pendingIntent = null;

        RefreshThread(Context c) {
            this.c = c;

            Toast.makeText(c, "in therwad", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void run() {
            if (pendingIntent != null)
                pendingIntent.cancel();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Toast.makeText(c, "in therwad", Toast.LENGTH_SHORT).show();
            try {
                getPendingIntent(c, WidgetActions.REFRESH, "Click to send the command").send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }
}
