package top.lizhistudio.app.activity.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.immomo.mls.utils.MainThreadExecutor;

import java.util.Observable;
import java.util.Observer;

import top.lizhistudio.app.R;
import top.lizhistudio.app.core.DebuggerServer;

public class SettingsFragment extends Fragment {
    private final static String ERROR_IP = "未连接wifi";
    private Observer observer;
    private EditText portEdit;
    private static String intToIp(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }


    private String getIp()
    {
        String ipString;
        ConnectivityManager connectivityManager = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected())
        {
            WifiManager wifiManager = (WifiManager)getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            ipString = intToIp(wifiManager.getConnectionInfo().getIpAddress());
        }else
        {
            ipString = ERROR_IP;
        }
        return ipString;
    }


    private String getPort()
    {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("config",Context.MODE_PRIVATE);
        return sharedPreferences.getString("port","");
    }

    private void savePort()
    {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("config",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.apply();
        editor.putString("port",portEdit.getText().toString());
        editor.commit();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        final TextView textView = root.findViewById(R.id.text_ip);
        textView.setText(getIp());
        portEdit = root.findViewById(R.id.textPort);
        portEdit.setText(getPort());
        SwitchCompat switchCompat = root.findViewById(R.id.debuggerSwitch);
        switchCompat.setChecked(DebuggerServer.getInstance().isServing());



        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                {
                    int port;
                    try{
                        port = Integer.parseInt(portEdit.getText().toString());
                        DebuggerServer.getInstance().start(port);
                    }catch (Exception e)
                    {
                        switchCompat.setChecked(false);
                        Toast.makeText(getContext(),"启动调试器失败",Toast.LENGTH_LONG).show();
                    }
                }else
                {
                    DebuggerServer.getInstance().stop();
                }

            }
        });
        observer = new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                MainThreadExecutor.post(new Runnable() {
                    @Override
                    public void run() {
                        switchCompat.setChecked((boolean)arg);
                    }
                });
            }
        };
        DebuggerServer.getInstance().addObserver(observer);
        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        savePort();
        DebuggerServer.getInstance().deleteObserver(observer);
    }
}