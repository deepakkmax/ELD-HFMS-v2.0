package com.hutchsystems.hutchconnect.tracklocations;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.content.Context;
import android.os.AsyncTask;

import com.hutchsystems.hutchconnect.db.TrackingDB;
import com.hutchsystems.hutchconnect.db.VehicleInfoDB;

public class ClientSocket extends AsyncTask<String, Void, String> {
    boolean status;
    Context context;
    String response;
    boolean isConnected;
    Socket socket = null;
    DataOutputStream dataOutputStream = null;
    boolean gpsFg;
    public boolean completedFg;

    String ServerIp = "207.194.137.58";
    int Port = 32501;

    public ClientSocket(Context context, boolean gpsFg) {
        this.context = context;
        this.gpsFg = gpsFg;
    }

    @Override
    protected String doInBackground(String... params) {

        try {
            // System.out.println(host + ":" + port);
            // Create a new Socket instance and connect to host
            // socket = new Socket(host, port);
            if (!isConnected) {
                socket = new Socket();
                socket.connect(new InetSocketAddress(ServerIp, Port), 30000);
                dataOutputStream = new DataOutputStream(
                        socket.getOutputStream());

            }
            isConnected = true;
            String response = params[0] + "\n";
            dataOutputStream.writeBytes(response);
            status = true;

        } catch (IOException e) {
            response = e.toString();
            e.printStackTrace();
            isConnected = false;
        } finally {
            if (!isConnected) {
                // close socket
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // close output stream
                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return params[1];
    }

    @Override
    protected void onPostExecute(String id) {
        completedFg = true;
        if (status) {
            if (gpsFg) {
                // TrackingDB.removeGpsSignal(id);
                TrackingDB.GPSSyncUpdate(id);
            } else {
                VehicleInfoDB.VehicleInfoUpdate(id);
            }
            status = false;
        }
    }
}
