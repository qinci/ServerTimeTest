# Android时间与服务器同步方案

>  在部分软件应用场景里，我们对应用时间要求非常苛刻，如活动、商品秒杀倒计时操作，防止用户修改本机时间，导致应用错乱。
>  我们如果能让本地应用时间与服务器时间在误差应许范围内，保持同步，就能有效减少应用出错率。

---
（[博客地址](http://blog.csdn.net/qinci/article/details/70666631)）

### 1. 预备
1. SystemClock.elapsedRealtime() ：手机系统开机时间（包含睡眠时间），用户无法在设置里面修改
2. 在必要的时刻获取一下服务器时间，然后记录这个时刻的手机开机时间（elapsedRealtime）
3. 后续时间获取：**现在服务器时间  = 以前服务器时间 + 现在手机开机时间 - 以前服务器时间的获取时刻的手机开机时间**

#### 具体代码如下
```java
public class TimeManager {
    private static TimeManager instance;
    private long differenceTime;        //以前服务器时间 - 以前服务器时间的获取时刻的系统启动时间
    private boolean isServerTime;       //是否是服务器时间

    private TimeManager() {
    }
    public static TimeManager getInstance() {
        if (instance == null) {
            synchronized (AppManager.class) {
                if (instance == null) {
                    instance = new TimeManager();
                }
            }
        }
        return instance;
    }

    /**
     * 获取当前时间
     *
     * @return the time
     */
    public synchronized long getServiceTime() {
        if (!isServerTime) {
	        //todo 这里可以加上触发获取服务器时间操作
            return System.currentTimeMillis();
        }
        
        //时间差加上当前手机启动时间就是准确的服务器时间了
        return differenceTime + SystemClock.elapsedRealtime();
    }
    
    /**
     * 时间校准
     *
     * @param lastServiceTime 当前服务器时间
     * @return the long
     */
    public synchronized long initServerTime(long lastServiceTime) {
        //记录时间差
        differenceTime = lastServiceTime - SystemClock.elapsedRealtime();
        isServerTime = true;
        return lastServiceTime;
    }
}
```
然后把软件调用System.currentTimeMillis()全部替换成TimeManager.getInstance().getServiceTime();

---

### 2. 利用OkHttp的Interceptor自动同步时间（[不懂Interceptor？](http://blog.csdn.net/oyangyujun/article/details/50039403)）

1. 网络响应头包含Date字段（世界时间）
2. 利用Interceptor记录每次请求响应时间，如果本次网络操作的时间小于上一次网络操作的时间，则获取Date字段，转换时区后更新本地TimeManager。
3. 这样时间就只会越来越精确了

#### 代码如下
```java
public class TimeCalibrationInterceptor implements Interceptor {
    long minResponseTime = Long.MAX_VALUE;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        long startTime = System.nanoTime();
        Response response = chain.proceed(request);
        long responseTime = System.nanoTime() - startTime;

        Headers headers = response.headers();
        calibration(responseTime, headers);
        return response;
    }

    private void calibration(long responseTime, Headers headers) {
        if (headers == null) {
            return;
        }

		//如果这一次的请求响应时间小于上一次，则更新本地维护的时间
        if (responseTime >= minResponseTime) {
            return;
        }

        String standardTime = headers.get("Date");
        if (!TextUtils.isEmpty(standardTime)) {
            Date parse = HttpDate.parse(standardTime);
            if (parse != null) {
	            // 客户端请求过程一般大于比收到响应时间耗时，所以没有简单的除2 加上去，而是直接用该时间
                TimeManager.getInstance().initServerTime(parse.getTime());
                minResponseTime = responseTime;
            }
        }
    }
}
```

---

### 3. 此方案的不足之处

1. 连接服务器的过程是需要时间的，服务器收到请求时刻的时间与应用收到响应存在一定的时间差，导致误差的存在（误差=服务器发出响应->到本机收到响应这个时间）。通过上面的TimeCalibrationInterceptor每次判断，可以使得误差逐渐降低

2. 考虑到机器重启的原因，所以时间没有持久化