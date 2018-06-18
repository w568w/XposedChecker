package ml.w568w.checkxposed;

import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Iterator;
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
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView textView = (TextView) findViewById(R.id.a);
        ListView listView = (ListView) findViewById(R.id.b);
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return 7;
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
                TextView textView1 = new TextView(MainActivity.this);
                textView1.setPadding(8, 8, 8, 8);
                try {
                    Method method = MainActivity.class.getDeclaredMethod("check" + (position + 1));
                    method.setAccessible(true);
                    boolean pass = (int) method.invoke(MainActivity.this) == 0;
                    textView1.setText(CHECK_ITEM[position] + " " + (pass ? "未发现Xposed" : "发现Xposed"));
                    textView1.setTextColor(pass ? Color.GREEN : Color.RED);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                return textView1;
            }
        });
        int checkCode = check1() + check2() + check3() + check4() + check5() + check6() + check7();
        if (checkCode > 0) {
            textView.setTextColor(Color.RED);
            textView.setText("你安装了 Xposed ! 可信度: " + checkCode + "/7");
        } else {
            textView.setTextColor(Color.GREEN);
            textView.setText("你没有安装 Xposed !");
        }
    }

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

    private int check2() {
        return a("XposedBridge") ? 1 : 0;
    }

    private int check3() {
        try {
            throw new Exception();
        } catch (Exception e) {
            StackTraceElement[] arrayOfStackTraceElement = e.getStackTrace();
            int zginittime = 0;
            for (StackTraceElement s : arrayOfStackTraceElement) {
                Log.d("3", s.getClassName());
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

    private int check6() {
        try {
            Method method = Throwable.class.getDeclaredMethod("getStackTrace");
            return Modifier.isNative(method.getModifiers()) ? 1 : 0;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int check7() {
        return System.getProperty("vxp") != null ? 1 : 0;
    }

    //直接Copy自
    public static boolean a(String paramString) {
        try {
            HashSet<String> localObject = new HashSet<>();
            // 读取maps文件信息
            BufferedReader localBufferedReader =
                    new BufferedReader(new FileReader("/proc/" + Process.myPid() + "/maps"));
            for (; ; ) {
                String str = localBufferedReader.readLine();
                if (str == null) {
                    break;
                }
                localObject.add(str.substring(str.lastIndexOf(" ") + 1));
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
}
