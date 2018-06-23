package ml.w568w.checkxposed;

import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.Keep;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jrummyapps.android.shell.CommandResult;
import com.jrummyapps.android.shell.Shell;
import com.unionpay.mobile.device.utils.RootCheckerUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * @author w568w
 */
public class MainActivity extends AppCompatActivity {
    private static String[] CHECK_ITEM = {
            "载入Xposed工具类",
            "寻找特征动态链接库",
            "代码堆栈寻找调起者",
            "检测Xposed安装情况",
            "判定系统方法调用钩子",
            "检测虚拟Xposed环境",
            "寻找Xposed运行库文件",
            "内核查找Xposed链接库",
            "环境变量特征字判断",
    };
    String[] rootStatus = {"出错", "未发现Root", "发现Root"};
    private ArrayList<Integer> status = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Xposed Checker");
        try {
            FutureTask futureTask = new FutureTask<>(new UnpackThread());
            new Thread(futureTask).start();
            futureTask.get();
            futureTask = new FutureTask<>(new CheckThread());
            new Thread(futureTask).start();
            futureTask.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_main);
        final TextView textView = (TextView) findViewById(R.id.a);
        final ListView listView = (ListView) findViewById(R.id.b);
        final TextView about = (TextView) findViewById(R.id.about);
        final TextView donation = (TextView) findViewById(R.id.donation);
        about.getPaint().setAntiAlias(true);
        about.getPaint().setUnderlineText(true);
        donation.getPaint().setAntiAlias(true);
        donation.getPaint().setUnderlineText(true);
        listView.setDivider(null);
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return CHECK_ITEM.length + 1;
            }

            @Override
            public Object getItem(int position) {
                return position;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                RelativeLayout layout = (RelativeLayout) LayoutInflater.from(MainActivity.this).inflate(R.layout.items, null);
                TextView textView1 = (TextView) layout.findViewById(R.id.check_item);
                TextView textView2 = (TextView) layout.findViewById(R.id.check_result);
                int s = status.get(position);
                boolean pass = s == 0;
                if (position == CHECK_ITEM.length) {
                    textView1.setText("Root检查");
                    textView2.setText(rootStatus[s + 1]);

                } else {
                    textView1.setText(CHECK_ITEM[position]);
                    textView2.setText((pass ? "未发现Xposed" : "发现Xposed"));
                }
                textView2.setTextColor(pass ? Color.GREEN : Color.RED);

                return layout;
            }
        });
        int checkCode = 0;
        for (int i = 0; i < CHECK_ITEM.length; ++i) {
            checkCode += status.get(i);
        }
        if (checkCode > 0) {
            textView.setTextColor(Color.RED);
            textView.setText("你安装了 Xposed ! 可信度: " + checkCode + "/" + CHECK_ITEM.length);
        } else {
            textView.setTextColor(Color.GREEN);
            textView.setText("你没有安装 Xposed !");
        }
    }

    private class CheckThread implements Callable<Void> {

        @Override
        public Void call() throws Exception {
            status.clear();
            for (int i = 0; i <= CHECK_ITEM.length; i++) {
                Method method = MainActivity.class.getDeclaredMethod("check" + (i + 1));
                method.setAccessible(true);
                try {
                    status.add((int) method.invoke(MainActivity.this));
                } catch (Throwable e) {
                    status.add(0);
                }
            }
            return null;
        }
    }

    public void about(View view) {
        new AlertDialog.Builder(this).setTitle("关于")
                .setMessage("由w568w开发...\n最近微信的事儿闹得纷纷扬扬，这款应用可以让你了解你的防Xposed检测模块到底有没有效果。\n\nDEBUG INFO:PID=" + Process.myPid())
                .setPositiveButton("我知道了", null)
                .show();
    }

    public void donation(View view) {
        new AlertDialog.Builder(this).setTitle("关于捐赠")
                .setMessage("写这个应用也花了不少时间收集代码,\n希望您能给我买根辣条，支持我后续的开发和防检测模块的开发进程...")
                .setPositiveButton("支付宝捐赠", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!AlipayDonate.startAlipayClient(MainActivity.this, "a6x06490c5kpcbnsr84hr23")) {
                            Toast.makeText(MainActivity.this, "朋友你看起来大概是没有安装支付宝...", Toast.LENGTH_LONG).show();
                        }

                    }
                })
                .show();
    }

    @Keep
    private int check1() {

        return testClassLoader() || testUseClassDirectly() ? 1 : 0;
    }

    private boolean testClassLoader() {
        try {
            ClassLoader.getSystemClassLoader()
                    .loadClass("de.robv.android.xposed.XposedHelpers");

            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean testUseClassDirectly() {
        try {
            XposedBridge.log("fuck wechat");
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Keep
    private int check2() {
        return checkContains("XposedBridge") ? 1 : 0;
    }
//
//    @Keep
//    private int check3() {
//        try {
//            throw new Exception();
//        } catch (Exception e) {
//            StackTraceElement[] arrayOfStackTraceElement = e.getStackTrace();
//            int zginittime = 0;
//            for (StackTraceElement s : arrayOfStackTraceElement) {
//                if ("com.android.internal.os.ZygoteInit".equals(s.getClassName())) {
//                    ++zginittime;
//                    if (zginittime == 2) {
//                        return 1;
//                    }
//                }
//            }
//            return 0;
//        }
//    }

    @Keep
    private int check3() {
        try {
            throw new Exception();
        } catch (Exception e) {
            StackTraceElement[] arrayOfStackTraceElement = e.getStackTrace();
            for (StackTraceElement s : arrayOfStackTraceElement) {
                if ("de.robv.android.xposed.XposedBridge".equals(s.getClassName())) {
                    return 1;
                }
            }
            return 0;
        }
    }

    @Keep
    private int check4() {
        try {
            List<PackageInfo> list = getPackageManager().getInstalledPackages(0);
            for (PackageInfo info : list) {
                if ("de.robv.android.xposed.installer".equals(info.packageName)) {
                    return 1;
                }
                if ("io.va.exposed".equals(info.packageName)) {
                    return 1;
                }
            }
        } catch (Throwable ignored) {

        }
        return 0;
    }

    @Keep
    private int check5() {
        try {
            Method method = Throwable.class.getDeclaredMethod("getStackTrace");
            return Modifier.isNative(method.getModifiers()) ? 1 : 0;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Keep
    private int check6() {
        return System.getProperty("vxp") != null ? 1 : 0;
    }

    //直接Copy自
    public static boolean checkContains(String paramString) {
        try {
            HashSet<String> localObject = new HashSet<>();
            // 读取maps文件信息
            BufferedReader localBufferedReader =
                    new BufferedReader(new FileReader("/proc/" + Process.myPid() + "/maps"));
            while (true) {
                String str = localBufferedReader.readLine();
                if (str == null) {
                    break;
                }
                localObject.add(str.substring(str.lastIndexOf(" ") + 1));
            }
            //应用程序的链接库不可能是空，除非是高于7.0。。。
            if (localObject.isEmpty() && Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                return true;
            }
            localBufferedReader.close();
            for (String aLocalObject : localObject) {
                if (aLocalObject.contains(paramString)) {
                    return true;
                }
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    @Keep
    private int check7() {
        CommandResult commandResult = Shell.run("ls /system/lib");
        return commandResult.isSuccessful() ? commandResult.getStdout().contains("xposed") ? 1 : 0 : 0;
    }

    @Keep
    private int check8() {
        CommandResult commandResult = Shell.run(getFilesDir().getAbsolutePath() + "/checkman " + Process.myPid());
        return commandResult.isSuccessful() ? 1 : 0;
    }

    @Keep
    private int check9() {
        return System.getenv("CLASSPATH").contains("XposedBridge") ? 1 : 0;
    }

    @Keep
    private int check10() {
        try {
            return RootCheckerUtils.detect(this) ? 1 : 0;
        } catch (Throwable t) {
            t.printStackTrace();
            return -1;
        }
    }

    private class UnpackThread implements Callable<Void> {

        @Override
        public Void call() throws Exception {
            if (!new File(getFilesDir().getAbsolutePath() + "/checkman").exists()) {
                InputStream inputStream = getAssets().open("checkman");
                OutputStream outputStream = openFileOutput("checkman", MODE_PRIVATE);
                int bit;
                while ((bit = inputStream.read()) != -1) {
                    outputStream.write(bit);
                }
            }
            setFilePermissions(getFilesDir(), 0777, -1, -1);
            setFilePermissions(getFilesDir().getAbsolutePath() + "/checkman", 0777, -1, -1);
            return null;
        }

        /**
         * 修改文件权限
         * setFilePermissions(file, 0777, -1, -1);
         *
         * @param file
         * @param chmod
         * @param uid
         * @param gid
         * @return
         */
        public boolean setFilePermissions(File file, int chmod, int uid, int gid) {
            if (file != null) {
                Class<?> fileUtils;
                try {
                    fileUtils = Class.forName("android.os.FileUtils");
                    Method setPermissions = fileUtils.getMethod("setPermissions", File.class, int.class, int.class, int.class);
                    int result = (Integer) setPermissions.invoke(null, file, chmod, uid, gid);

                    return result == 0;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return false;
            } else {
                return false;
            }
        }

        public boolean setFilePermissions(String file, int chmod, int uid, int gid) {
            return setFilePermissions(new File(file), chmod, uid, gid);
        }
    }
}
