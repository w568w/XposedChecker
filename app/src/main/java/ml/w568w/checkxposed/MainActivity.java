package ml.w568w.checkxposed;

import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Process;
import android.support.annotation.Keep;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jrummyapps.android.shell.CommandResult;
import com.jrummyapps.android.shell.Shell;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;

/**
 * @author w568w
 */
public class MainActivity extends AppCompatActivity {
    private static String CHECK_ITEM[] = {
            "载入Xposed工具类",
            "寻找特征静态链接库",
            "异常代码堆栈特征符",
            "代码堆栈寻找调起者",
            "检测Xposed安装情况",
            "判定系统方法调用钩子",
            "检测虚拟Xposed环境",
            "寻找Xposed运行库文件"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Xposed Checker");
        setContentView(R.layout.activity_main);
        final TextView textView = (TextView) findViewById(R.id.a);
        final ListView listView = (ListView) findViewById(R.id.b);
        final TextView about = (TextView) findViewById(R.id.about);
        about.getPaint().setAntiAlias(true);
        about.getPaint().setUnderlineText(true);
        listView.setDivider(null);
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return CHECK_ITEM.length;
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
                try {
                    Method method = MainActivity.class.getDeclaredMethod("check" + (position + 1));
                    method.setAccessible(true);
                    boolean pass = (int) method.invoke(MainActivity.this) == 0;
                    textView1.setText(CHECK_ITEM[position]);
                    textView2.setText((pass ? "未发现Xposed" : "发现Xposed"));
                    textView2.setTextColor(pass ? Color.GREEN : Color.RED);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                return layout;
            }
        });
        int checkCode = check1() + check2() + check3() + check4() + check5() + check6() + check7() + check8();
        if (checkCode > 0) {
            textView.setTextColor(Color.RED);
            textView.setText("你安装了 Xposed ! 可信度: " + checkCode + "/" + CHECK_ITEM.length);
        } else {
            textView.setTextColor(Color.GREEN);
            textView.setText("你没有安装 Xposed !");
        }
    }

    public void about(View view) {
        new AlertDialog.Builder(this).setTitle("关于")
                .setMessage("由w568w开发...\n最近微信的事儿闹得纷纷扬扬，这款应用可以让你了解你的防Xposed检测模块到底有没有效果。")
                .setPositiveButton("我知道了", null)
                .show();
    }

    @Keep
    private int check1() {
        try {
            ClassLoader.getSystemClassLoader()
                    .loadClass("de.robv.android.xposed.XposedHelpers");
            return 1;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Keep
    private int check2() {
        return checkContains("XposedBridge") ? 1 : 0;
    }

    @Keep
    private int check3() {
        try {
            throw new Exception();
        } catch (Exception e) {
            StackTraceElement[] arrayOfStackTraceElement = e.getStackTrace();
            int zginittime = 0;
            for (StackTraceElement s : arrayOfStackTraceElement) {
                if ("com.android.internal.os.ZygoteInit".equals(s.getClassName())) {
                    ++zginittime;
                    if (zginittime == 2) {
                        return 1;
                    }
                }
            }
            return 0;
        }
    }

    @Keep
    private int check4() {
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
    private int check5() {
        try {
            List<PackageInfo> list = getPackageManager().getInstalledPackages(0);
            for (PackageInfo info : list) {
                if ("de.robv.android.xposed.installer".equals(info.packageName)) {
                    return 1;
                }
            }
        } catch (Throwable ignored) {

        }
        return 0;
    }

    @Keep
    private int check6() {
        try {
            Method method = Throwable.class.getDeclaredMethod("getStackTrace");
            return Modifier.isNative(method.getModifiers()) ? 1 : 0;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Keep
    private int check7() {
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
    private int check8() {
        CommandResult commandResult = Shell.run("ls /system/lib");
        return commandResult.isSuccessful() ? commandResult.getStdout().contains("xposed") ? 1 : 0 : 0;
    }
}
