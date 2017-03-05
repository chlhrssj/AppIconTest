package app.appicontest;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button button;

    //删除快捷方式的action
    public static final String ACTION_REMOVE_SHORTCUT = "com.android.launcher.action.UNINSTALL_SHORTCUT";
    //添加快捷方式的action
    public static final String ACTION_ADD_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";

    int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("teste","test     " + flag);
                if(flag == 0){
                    addShortcut(getApplicationContext(),"testtest1",false,drawableToBitamp(getDrawable(R.drawable.icon1)));
                }
                else{
                    addShortcut(getApplicationContext(),"testtest2",false,drawableToBitamp(getDrawable(R.drawable.icon2)));
                }
                flag = 1 - flag;
            }
        });
    }

    //删除快捷方式
    public static void removeShortcut(Context context, Intent actionIntent, String name) {
        Intent intent = new Intent(ACTION_REMOVE_SHORTCUT);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
        intent.putExtra("duplicate", false);
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, actionIntent);
        context.sendBroadcast(intent);
    }
    //增加快捷方式
    public static void addShortcut(Context context, String name,
                                   boolean allowRepeat, Bitmap iconBitmap) {

        Intent actionIntent = new Intent(Intent.ACTION_MAIN);

        Intent addShortcutIntent = new Intent(ACTION_ADD_SHORTCUT);
        // 是否允许重复创建
        addShortcutIntent.putExtra("duplicate", allowRepeat);
        // 快捷方式的标题
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
        // 快捷方式的图标
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, iconBitmap);
        // 快捷方式的动作
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, actionIntent);
        context.sendBroadcast(addShortcutIntent);
    }

    private Bitmap drawableToBitamp(Drawable drawable)
    {
        BitmapDrawable bd = (BitmapDrawable) drawable;
        return bd.getBitmap();
    }

    //此函数返回当前rom下的lanucher的包名
    private String getCurrentLanucherPackageName(Context context)
    {
        //这个intent很好理解 就是启动lanucher的intent
        Intent intent=new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        //getPackageManager().resolveActivity 这个函数就是查询是否有符合条件的activity的
        ResolveInfo res=context.getPackageManager().resolveActivity(intent,0);
        //为避免空指针 我们要判定下空，虽然你我都知道这种情况不会发生
        if(res==null||res.activityInfo==null)
        {
            return "";
        }
        return res.activityInfo.packageName;
    }

    //此函数返回 要查找的permission的 provider的 authority
    private String getAuthorityFromPermission(Context context, String permission) {
        //返回安装的app的 provider的信息
        List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS);
        //遍历获取到的安装包的信息
        for (PackageInfo pack : packs) {
            //每个安装包提供的provider 都在这个数组里面
            ProviderInfo[] providers = pack.providers;
            if (providers != null) {
                //遍历每个provider 看需要的权限是否与我们传进来的权限参数相等
                for (ProviderInfo providerInfo : providers) {
                    if (permission.equals(providerInfo.readPermission) || permission.equals(providerInfo.writePermission)) {
                        return providerInfo.authority;
                    }
                }
            }
        }
        return "";
    }

    private String getAuthorityFromPermissionDefault(Context context) {
        return getAuthorityFromPermission(context, "com.android.l" +
                "auncher.permission.READ_SETTINGS");
    }

    private Uri getUriFromLauncher(Context context) {
        StringBuilder uriStrBuilder = new StringBuilder();
        //为了速度考虑，这里我们先查找默认的 看是否能查找到 因为多数手机的rom还是用的默认的lanucher
        String authority = getAuthorityFromPermissionDefault(context);
        //如果找不到的话 就说明这个rom一定是用的其他的自定义的lanucher。那就拼一下 这个自定义的lanucher的permission再去查找一次
        if (authority == null || authority.trim().equals("")) {
            authority = getAuthorityFromPermission(context,getCurrentLanucherPackageName(context) + ".permission.READ_SETTINGS");
        }
        uriStrBuilder.append("content://");
        //如果连上面的方法都查找不到这个authority的话 那下面的方法 就肯定查找到了 但是很少有情况会是如下这种
        //多数都是else里面的逻辑
        if (TextUtils.isEmpty(authority)) {
            int sdkInt = android.os.Build.VERSION.SDK_INT;
            if (sdkInt < 8) { // Android 2.1.x(API 7)以及以下的
                uriStrBuilder.append("com.android.launcher.settings");
            } else if (sdkInt < 19) {// Android 4.4以下
                uriStrBuilder.append("com.android.launcher2.settings");
            } else {// 4.4以及以上
                uriStrBuilder.append("com.android.launcher3.settings");
            }
        } else {
            uriStrBuilder.append(authority);
        }
        uriStrBuilder.append("/favorites?notify=true");
        return Uri.parse(uriStrBuilder.toString());
    }
}
