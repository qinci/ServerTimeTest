package ren.qinc.servicetimetest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Date;

import ren.qinc.servicetimetest.core.ServerTime;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //启动成功后获取服务器时间并初始化(这里用本机时间模拟)
        ServerTime.getInstance().initServerTime(new Date().getTime());

        
        //以后应用所有的地方都可以使用了
       Toast.makeText(this, "服务器时间："+ServerTime.getInstance().getmServerTime(),Toast.LENGTH_SHORT).show();
    }
}
