package org.lsposed.lspd.service;

import android.content.Context;
import android.util.Log;
import org.json.JSONArray;
import org.lsposed.lspd.models.Application;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.Server;
import com.yanzhenjie.andserver.annotation.QueryParam;
import com.yanzhenjie.andserver.util.MediaType;
import com.yanzhenjie.andserver.annotation.RequestMethod;
import com.yanzhenjie.andserver.annotation.GetMapping;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestBody;
import com.yanzhenjie.andserver.annotation.RequestMapping;
import com.yanzhenjie.andserver.annotation.RequestParam;
import com.yanzhenjie.andserver.annotation.RestController;

@RestController
@RequestMapping(path = "/lsp")
public class HttpService {
    public static void start(Context context){
        Server server = AndServer.webServer(context)
                .port(8091)
                .timeout(10, TimeUnit.SECONDS)
                .listener(new Server.ServerListener() {
                    @Override
                    public void onStarted() {
                        Log.e("HttpServer", "started");
                    }
                    @Override
                    public void onStopped() {
                        Log.e("HttpServer", "stopped");
                    }
                    @Override
                    public void onException(Exception e) {
                        Log.e("HttpServer", "exception");
                        e.printStackTrace();
                    }
                })
                .build();
        server.startup();
    }

    @RequestMapping(path = "/test", method = {RequestMethod.GET, RequestMethod.POST})
    public String test() {
        return "it works!";
    }

    @GetMapping(path = "/scope")
    public String getScope(@QueryParam("packageName") String packageName){
        List<Application> moduleScope = ConfigManager.getInstance().getModuleScope(packageName);
        JSONArray result = new JSONArray();
        for (Application app : moduleScope) {
            result.put(app.packageName);
        }
        return result.toString();
    }

    @PostMapping(path = "/scope", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String setScope(@QueryParam("packageName") String packageName, @RequestBody String json) {
        try {
            List<Application> apps = new ArrayList<>();
            JSONArray packages = new JSONArray(json);
            for (int i = 0; i < packages.length(); i++) {
                String appPackageName = packages.getString(i);
                Application app = new Application();
                app.packageName = appPackageName;
                app.userId = 0;
                apps.add(app);
            }
            if (apps.size() > 0){
                boolean ret = ConfigManager.getInstance().setModuleScope(packageName, apps);
                if (!ret) {
                    throw new Exception("setModuleScope fail");
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return "exception:\n" + e.getLocalizedMessage();
        }
        return getScope(packageName);
    }
}