package org.lsposed.lspd.service;

import android.util.Log;
import org.json.JSONArray;
import fi.iki.elonen.NanoHTTPD;
import org.lsposed.lspd.models.Application;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpService extends NanoHTTPD {
    public static final int PORT = 8091;
    public static final String TAG = "HttpService";
    public static final String HTTP_URI_CHECK = "/lsp/check";
    public static final String HTTP_URI_SCOPE = "/lsp/scope";

    public HttpService() {
        super(PORT);
    }

    @Override
    public Response serve(IHTTPSession session) {
        if (session.getUri().equalsIgnoreCase(HTTP_URI_CHECK)){
            return newFixedLengthResponse(check());
        }
        if (session.getUri().equalsIgnoreCase(HTTP_URI_SCOPE)){
            Map<String, List<String>> parameters = session.getParameters();
            if (parameters == null || !parameters.containsKey("packageName")){
                return newFixedLengthResponse("please input packageName.");
            }
            String packageName = parameters.get("packageName").get(0);
            if (packageName == null || packageName.trim().length() <= 0){
                return newFixedLengthResponse("packageName is empty.");
            }
            if (session.getMethod() == Method.GET){
                return newFixedLengthResponse(getScope(packageName));
            } else {
                try {
                    Map<String, String> map = new HashMap<>();
                    session.parseBody(map);
                    String json = map.get("postData");
                    return newFixedLengthResponse(setScope(packageName, json));
                } catch (Exception e) {
                    return newFixedLengthResponse(e.getMessage());
                }
            }
        }
        return newFixedLengthResponse("no support!");
    }

    public String check() {
        return "it works!";
    }

    public String getScope(String packageName){
        JSONArray result = new JSONArray();
        try {
            List<Application> moduleScope = ConfigManager.getInstance().getModuleScope(packageName);
            for (Application app : moduleScope) {
                result.put(app.packageName);
            }
        }catch (Throwable e){
            Log.e(TAG, "HttpService getScope error : " + e.getMessage());
        }
        return result.toString();
    }

    public String setScope(String packageName, String json) {
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