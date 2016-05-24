/*
 * Copyright 2016. SHENQINCI(沈钦赐)<946736079@qq.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ren.qinc.servertimetest.core;

import android.os.SystemClock;

import java.util.Date;

/**
 * 服务器时间管理器
 * （启动软件的时候，先获取一下时间）
 * 现在服务器时间  = 以前服务器时间 + 现在系统启动时间 - 以前服务器时间的获取时刻的系统启动时间
 * Created by 沈钦赐 on 2016/5/23.
 */
public class ServerTime {
    private long mElapsedRealtime = 0;
    private long mServerTime = 0;

    private ServerTime() {

    }

    public static ServerTime getInstance() {
        return ServerTimeInstance.getTimeInstance();
    }

    /**
     * 获取当前时间
     * Gets time.
     *
     * @return the time
     */
    public synchronized long getmServerTime() {
        return mServerTime + SystemClock.elapsedRealtime() - mElapsedRealtime;
    }

    /**
     * 时间校准,软件启动后，调用这个方法初始化一下时间
     * Init serverTime.
     *
     * @param serverTime 当前服务器时间
     * @return the long
     */
    public synchronized long initServerTime(long serverTime) {
        //初始化时间
        this.mServerTime = serverTime;
        mElapsedRealtime = SystemClock.elapsedRealtime();
        return serverTime;
    }

    /**
     * 持久化时间
     * Save time.
     */
    protected static void saveTime(long serverTime,long elapsedRealtime){
        //保存时间到本地逻辑

    }

    /**
     * 从本地读取时间
     * Read time.
     *
     * @return the long
     */
    protected static long readTime(){
        //读取本地时间逻辑，这里用本机时间代替了
        //判断是否是服务器时间
        return new Date().getTime();
    }

    private static class ServerTimeInstance {
        public static ServerTime instance = new ServerTime();

        static {
            //初始化时间
            instance.mServerTime = readTime();
            instance.mElapsedRealtime = SystemClock.elapsedRealtime();
        }

        public static ServerTime getTimeInstance() {
            return instance;
        }
    }
}

