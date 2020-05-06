package killer_i.s2s.s2sconnect.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.UUID;

import killer_i.s2s.s2sconnect.ActionsMainActivity;


public class BluetoothManager {
    ActionsMainActivity.ActionsInterface actionsInterface;
    public ConnectThread deviceConnection;
    Logger logger;

    public BluetoothManager(ActionsMainActivity.ActionsInterface actionsInterface, String name, Logger logger) {
        this.actionsInterface = actionsInterface;
//        deviceConnection = new ConnectThread(name);
        this.logger = logger;
    }

    public static Object[] getPairedDevices() {
        Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        return pairedDevices.toArray();
    }

    public boolean sendMessage(String address, String message) {
        OutputStream outputStream;
        BluetoothSocket socket = getSocket(address);
        try {
            outputStream = socket.getOutputStream();
            outputStream.write(message.getBytes());
            Thread.sleep(1000);
            socket.close();
        } catch (Exception e) {
            logger.error(e.getClass().getName(), e.getMessage());
            return false;
        }
        return true;
    }

    public boolean sendSerialCommand(String address, String command) {
        OutputStream outputStream;
        BluetoothSocket socket = getSocket(address);
        try {
            outputStream = socket.getOutputStream();
            outputStream.write(hexStringToByteArray(command));
            Thread.sleep(1000);
            socket.close();
        } catch (Exception e) {
            logger.error(e.getClass().getName(), e.getMessage());
            return false;
        }
        return true;
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len/2];

        try {
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
            }
        } catch (IndexOutOfBoundsException e) {
            logger.error("Wrong message", "Command entered is in wrong format");
        }
        return data;
    }

    private BluetoothSocket getSocket(String address) {
        BluetoothDevice device;
        BluetoothSocket socket = null;
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        try {
            device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
            ParcelUuid[] uuids = device.getUuids();
            socket = device.createRfcommSocketToServiceRecord(uuid);
            socket.connect();
            return socket;
        } catch (Exception e) {
            logger.error(e.getClass().getName(), e.getMessage());
            try {
                Class<?> clazz = socket.getRemoteDevice().getClass();
                Class<?>[] paramTypes = new Class<?>[]{Integer.TYPE};
                Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[]{Integer.valueOf(1)};
                BluetoothSocket fallbackSocket = (BluetoothSocket) m.invoke(socket.getRemoteDevice(), params);
                fallbackSocket.connect();
                return fallbackSocket;
            } catch (Exception ex) {
                logger.error(ex.getClass().getName(), ex.getMessage());
            }
        }
        return null;
    }

    public class ConnectThread extends Thread {
        private BluetoothSocket socket = null;
        private ConnectedThread connectedThread;

        public ConnectThread(String address) {
            BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
            ParcelUuid[] uuids = device.getUuids();
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
                tmp.connect();
            } catch (IOException e) {
                Class<?> clazz = tmp.getRemoteDevice().getClass();
                Class<?>[] paramTypes = new Class<?>[] {Integer.TYPE};
                Method m = null;
                try {
                    m = clazz.getMethod("createRfcommSocket", paramTypes);
                    Object[] params = new Object[]{Integer.valueOf(1)};
                    BluetoothSocket fallbackSocket = (BluetoothSocket) m.invoke(tmp.getRemoteDevice(), params);
                    fallbackSocket.connect();
                    socket = fallbackSocket;
                } catch (Exception e2) {
                    actionsInterface.actionFailed();
                }
            }
        }

        @Override
        public void run() {
            if (socket != null) {
                ConnectedThread connectedThread = new ConnectedThread(socket);
                connectedThread.start();
            }
        }

        public void sendCommand(String command) {
            connectedThread.sendCommand(command);
        }

        public void close() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectedThread extends Thread {
        private InputStream inputStream;
        private OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket) {
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            try {
                while (true) {
                    bytes = inputStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    if (actionsInterface != null) {
                        actionsInterface.onMessageReceivedFromDevice(incomingMessage);
                    }
                }
            } catch (IOException e) {
            }
        }

        public void sendCommand(String command) {
            byte[] bytes = command.getBytes(Charset.defaultCharset());
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
