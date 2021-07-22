package top.lizhistudio.app.activity.ui.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import top.lizhistudio.app.App;
import top.lizhistudio.app.DebugService;
import top.lizhistudio.app.R;
import top.lizhistudio.app.activity.MainActivity;
import top.lizhistudio.app.activity.PrepareActivity;

public class SettingsFragment extends Fragment {
    private static final int EVENT_SCREENSHOT = 22;
    private final static String ERROR_IP = "未连接wifi";
    private EditText portEdit;
    private SwitchCompat switchCompat;
    private LocalBroadcastManager localBroadcastManager;
    private BroadcastReceiver debugServiceReceiver;
    private boolean debugServiceState =false;
    private static String intToIp(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        debugServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                debugServiceState = intent.getBooleanExtra("state",false);
                switchCompat.setChecked(debugServiceState);
            }
        };
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
        switchCompat = root.findViewById(R.id.debuggerSwitch);
        switchCompat.setChecked(debugServiceState);
        switchCompat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchCompat.isChecked())
                {
                    try{
                        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                                getContext().getApplicationContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), EVENT_SCREENSHOT);
                    }catch (Exception e)
                    {
                        switchCompat.setChecked(false);
                        Toast.makeText(getContext(),"启动调试器失败",Toast.LENGTH_LONG).show();
                    }
                }else
                {
                    Intent intent = new Intent(getContext(), DebugService.class);
                    getActivity().stopService(intent);
                }
            }
        });
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });
        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        localBroadcastManager.unregisterReceiver(debugServiceReceiver);
        savePort();
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(DebugService.STATE_ACTION);
        localBroadcastManager.registerReceiver(debugServiceReceiver,intentFilter);
        Intent intent = new Intent(DebugService.ASK_STATE_ACTION);
        localBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EVENT_SCREENSHOT)
        {
            int port = Integer.parseInt(portEdit.getText().toString());
            Intent intent = new Intent(getContext(), DebugService.class);
            intent.putExtra("port",port);
            intent.putExtra("code",resultCode);
            intent.putExtra("data",data);
            getActivity().startService(intent);
        }
    }
}