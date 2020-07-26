package ml.w568w.checkxposed.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.net.Uri;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jrummyapps.android.shell.CommandResult;
import com.jrummyapps.android.shell.Shell;
import com.tencent.bugly.crashreport.CrashReport;
import com.unionpay.mobile.device.utils.RootCheckerUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import de.robv.android.xposed.XposedBridge;
import ml.w568w.checkxposed.R;
import ml.w568w.checkxposed.util.AlipayDonate;
import ml.w568w.checkxposed.util.NativeDetect;

/**
 * @author w568w
 */
public class MainActivity extends AppCompatActivity {
    private static String[] CHECK_ITEM;
    private static String[] ROOT_STATUS;

    private ArrayList<Integer> status = new ArrayList<>();
    private ArrayList<String> techDetails = new ArrayList<>();
    private static final String[] XPOSED_APPS_LIST = new String[]{"de.robv.android.xposed.installer", "io.va.exposed", "org.meowcat.edxposed.manager", "com.topjohnwu.magisk"};
    private static final int ALL_ALLOW = 0777;
    ListView mListView;
    TextView mStatus;
    BaseAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Xposed Checker");
        CHECK_ITEM = getResources().getStringArray(R.array.inspect_item);
        ROOT_STATUS = getResources().getStringArray(R.array.root_status);
        try {

//            FutureTask<Void> futureTask = new FutureTask<>(new UnpackThread());
//            new Thread(futureTask).start();
//            futureTask.get();

            FutureTask<Void> futureTask = new FutureTask<>(new CheckThread());
            new Thread(futureTask).start();
            futureTask.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_main);
        mStatus = (TextView) findViewById(R.id.a);
        mListView = (ListView) findViewById(R.id.b);
        final TextView about = (TextView) findViewById(R.id.about);
        final TextView donation = (TextView) findViewById(R.id.donation);
        if (about != null) {
            about.getPaint().setAntiAlias(true);
            about.getPaint().setUnderlineText(true);
        }
        if (donation != null) {
            donation.getPaint().setAntiAlias(true);
            donation.getPaint().setUnderlineText(true);
        }

        if (mListView != null) {
            mAdapter = new BaseAdapter() {
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
                    @SuppressLint("InflateParams")
                    RelativeLayout itemLayout = (RelativeLayout) LayoutInflater.from(MainActivity.this).inflate(R.layout.items, null);
                    TextView name = (TextView) itemLayout.findViewById(R.id.check_item);
                    TextView result = (TextView) itemLayout.findViewById(R.id.check_result);
                    int itemStatus = status.get(position);
                    boolean pass = itemStatus == 0;
                    if (position == CHECK_ITEM.length) {
                        name.setText(R.string.root_check);
                        result.setText(ROOT_STATUS[itemStatus + 1]);
                    } else {
                        name.setText(CHECK_ITEM[position]);
                        result.setText((pass ? getString(R.string.item_no_xposed) : getString(R.string.item_found_xposed)));
                    }
                    result.setTextColor(pass ? Color.GREEN : Color.RED);
                    return itemLayout;
                }
            };
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(getString(R.string.tech_detail))
                            .setMessage(techDetails.get(i))
                            .setPositiveButton(R.string.OK, null)
                            .show();
                }
            });
        }
        refreshStatus();

    }

    private void refreshStatus() {
        int checkCode = 0;
        for (int i = 0; i < CHECK_ITEM.length; ++i) {
            checkCode += status.get(i);
        }
        if (mStatus != null) {
            if (checkCode > 0) {

                mStatus.setTextColor(Color.RED);

                mStatus.setText(String.format(getString(R.string.found_xposed), checkCode, CHECK_ITEM.length));
            } else {
                mStatus.setTextColor(Color.GREEN);
                mStatus.setText(R.string.no_xposed);
            }
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
                    e.printStackTrace();
                    techDetails.add(getString(R.string.item_exception) + Log.getStackTraceString(e));
                    CrashReport.postCatchedException(e);
                    status.add(0);
                }
            }
            return null;
        }
    }

    public void refresh(final View view) {
        status.clear();
        techDetails.clear();
        final TextView textView = (TextView) view;
        textView.setText(R.string.refreshing);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    FutureTask<Void> futureTask = new FutureTask<>(new CheckThread());
                    new Thread(futureTask).start();
                    futureTask.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mAdapter.notifyDataSetChanged();
                refreshStatus();
                textView.setText(R.string.refresh);
            }
        }, 200);

    }

    public void about(View view) {
        new AlertDialog.Builder(this).setTitle(getString(R.string.about_title))
                .setMessage(String.format(getString(R.string.about), Process.myPid()))
                .setPositiveButton(R.string.OK, null)
                .setNeutralButton(R.string.about_me, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://w568w.eu.org/about.w568w.html")));
                        } catch (Throwable ignored) {
                        }
                    }
                })
                .show();
    }

    public void donation(View view) {
        new AlertDialog.Builder(this).setTitle(getString(R.string.donation_title))
                .setMessage(getString(R.string.donation))
                .setPositiveButton(getString(R.string.Alipay), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (AlipayDonate.startAlipayClient(MainActivity.this, "a6x06490c5kpcbnsr84hr23")) {
                            Toast.makeText(MainActivity.this, R.string.coffee_please, Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .show();
    }

    private String toStatus(boolean bool) {
        return bool ? getString(R.string.item_found_xposed) : getString(R.string.item_no_xposed);
    }

    @Keep
    private int check1() {
        techDetails.add(String.format(getString(R.string.item_1)
                , toStatus(testClassLoader("de.robv.android.xposed.XposedHelpers"))
                , toStatus(testUseClassDirectly())));
        return testClassLoader("de.robv.android.xposed.XposedHelpers") || testUseClassDirectly() ? 1 : 0;
    }

    private boolean testClassLoader(String clazz) {
        try {
            ClassLoader.getSystemClassLoader()
                    .loadClass(clazz);

            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean testUseClassDirectly() {
        try {
            XposedBridge.log("fuck");
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Keep
    private int check2() {
        boolean result = checkContains("XposedBridge");
        techDetails.add(String.format(getString(R.string.item_2)
                , Process.myPid()
                , toStatus(result)));
        return result ? 1 : 0;
    }

    @Keep
    private int check3() {
        StringBuilder builder = new StringBuilder(getString(R.string.item_3));
        try {
            throw new Exception();
        } catch (Exception e) {
            StackTraceElement[] arrayOfStackTraceElement = e.getStackTrace();

            for (StackTraceElement s : arrayOfStackTraceElement) {
                builder.append(s.toString());
                builder.append("\n");
                if ("de.robv.android.xposed.XposedBridge".equals(s.getClassName())) {
                    builder.append(String.format("[%s]", toStatus(true)));
                    techDetails.add(builder.toString());
                    return 1;
                }
            }
            builder.append(String.format("[%s]", toStatus(false)));
            techDetails.add(builder.toString());
            return 0;
        }
    }

    @Keep
    private int check4() {
        StringBuilder builder = new StringBuilder(String.format(getString(R.string.item_4_1), 0));
        try {
            List<PackageInfo> list = getPackageManager().getInstalledPackages(0);
            builder = new StringBuilder(String.format(getString(R.string.item_4_1), list.size()));
            for (PackageInfo info : list) {
                for (String pkg : XPOSED_APPS_LIST) {
                    if (pkg.equals(info.packageName)) {
                        builder.append(getString(R.string.item_4_2)).append(pkg).append("\n");
                        techDetails.add(builder.toString());
                        return 1;
                    }
                }

            }
        } catch (Throwable ignored) {

        }
        builder.append("[").append(toStatus(false)).append("]");
        techDetails.add(builder.toString());
        return 0;
    }

    @Keep
    private int check5() {
        StringBuilder builder = new StringBuilder(getString(R.string.item_5_1));
        try {
            Method method = Throwable.class.getDeclaredMethod("getStackTrace");
            builder.append("[").append(toStatus(Modifier.isNative(method.getModifiers()))).append("]");
            techDetails.add(builder.toString());
            return Modifier.isNative(method.getModifiers()) ? 1 : 0;
        } catch (NoSuchMethodException e) {
            builder.append(getString(R.string.item_5_2));
            e.printStackTrace();
        }
        techDetails.add(builder.toString());
        return 0;
    }

    @Keep
    private int check6() {
        boolean result = System.getProperty("vxp") != null;
        techDetails.add(String.format(getString(R.string.item_6), toStatus(result)));
        return result ? 1 : 0;
    }


    /**
     * @param paramString check string
     * @return whether check string is found in maps
     */
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
        StringBuilder builder = new StringBuilder(getString(R.string.item_7_1));
        CommandResult commandResult = Shell.run("ls /system/lib");
        builder.append(commandResult.isSuccessful() ? getString(R.string.item_7_2) : getString(R.string.item_7_3));
        String out = commandResult.getStdout();
        boolean result = out.contains("xposed") || out.contains("Xposed");
        builder.append("[").append(toStatus(result)).append("]");
        techDetails.add(builder.toString());
        return commandResult.isSuccessful() ? (result ? 1 : 0) : 0;
    }

    @Keep
    private int check8() {
//        CommandResult commandResult = Shell.run(getFilesDir().getAbsolutePath() + "/checkman " + Process.myPid());
//        return commandResult.isSuccessful() ? 1 : 0;
        techDetails.add(getString(R.string.item_8));
        try {
            return NativeDetect.detectXposed() ? 1 : 0;
        } catch (Throwable t) {
            CrashReport.postCatchedException(t);
            return 0;
        }
    }

    @Keep
    private int check9() {
        boolean result;
        try {
            result = System.getenv("CLASSPATH").contains("XposedBridge");
            techDetails.add(String.format(getString(R.string.item_9_1), toStatus(result)));
        } catch (NullPointerException e) {
            result = false;
            techDetails.add(getString(R.string.item_9_2));
        }

        return result ? 1 : 0;
    }

    @Keep
    private int check10() {
        boolean result = testClassLoader("com.elderdrivers.riru.edxp.config.EdXpConfigGlobal");
        techDetails.add(String.format(getString(R.string.item_10)
                , toStatus(result)));
        return result ? 1 : 0;
    }

    @Keep
    private int check11() {
        try {
            techDetails.add(getString(R.string.item_root));
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
            setFilePermissions(getFilesDir(), ALL_ALLOW, -1, -1);
            setFilePermissions(getFilesDir().getAbsolutePath() + "/checkman", ALL_ALLOW, -1, -1);
            return null;
        }

        /**
         * 修改文件权限
         * setFilePermissions(file, 0777, -1, -1);
         */
        boolean setFilePermissions(File file, int chmod, int uid, int gid) {
            if (file != null) {
                Class<?> fileUtils;
                try {
                    fileUtils = Class.forName("android.os.FileUtils");
                    Method setPermissions = fileUtils.getMethod("setPermissions", File.class, int.class, int.class, int.class);
                    int result = (Integer) setPermissions.invoke(null, file, chmod, uid, gid);

                    return result == 0;
                } catch (Throwable e) {
                    e.printStackTrace();
                }

                return false;
            } else {
                return false;
            }
        }

        boolean setFilePermissions(String file, int chmod, int uid, int gid) {
            return setFilePermissions(new File(file), chmod, uid, gid);
        }
    }
}
