package org.apache.cordova.errorscreen;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import android.graphics.Color;
import android.app.Dialog;
import android.view.View;
import android.widget.Button;
import android.content.Context;
import android.widget.LinearLayout;

public class ErrorScreen extends CordovaPlugin {
  private static final String DEFAULT_COLOR = "#C20000";
  private static final String HEX_REGEX = "^#([A-Fa-f0-9]{6})$";

  private static Dialog screenDialog;
  private String backgroundColor;

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    backgroundColor = preferences.getString("BackgroundColor", DEFAULT_COLOR);

    if(!backgroundColor.matches(HEX_REGEX)){
      backgroundColor = DEFAULT_COLOR;
    }
  }

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    if(action.equals("show")) {
      cordova.getActivity().runOnUiThread(new Runnable() {
        public void run() {
          webView.postMessage("errorscreen", "show");
        }
      });
    } else if(action.equals("hide")) {
      cordova.getActivity().runOnUiThread(new Runnable() {
        public void run() {
          webView.postMessage("errorscreen", "hide");
        }
      });
    }

    return true;
  }

  @Override
  public Object onMessage(String id, Object data) {
    if ("errorscreen".equals(id)) {
      if ("show".equals(data.toString())) {
        this.showErrorScreen();
      } else if ("hide".equals(data.toString())) {
        this.hideErrorScreen();
      }
    }

    return null;
  }

  @SuppressWarnings("deprecation")
  private void showErrorScreen() {
    // If the screen dialog is showing don't try to show it again
    if (screenDialog != null && screenDialog.isShowing()) {
      return;
    }

    cordova.getActivity().runOnUiThread(new Runnable() {
      public void run() {
        Context context = webView.getContext();

        int network_error_layout = context.getResources().getIdentifier("network_error","layout", context.getPackageName());
        int networkErrorButtonRetry_button = context.getResources().getIdentifier("networkErrorButtonRetry","id", context.getPackageName());
        int networkErrorLayout_linear_layout = context.getResources().getIdentifier("networkErrorLayout","id", context.getPackageName());

        screenDialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        screenDialog.setContentView(network_error_layout);
        screenDialog.setCancelable(false);

        if(backgroundColor != null){
          LinearLayout error_layout = (LinearLayout) screenDialog.findViewById(networkErrorLayout_linear_layout);
          error_layout.setBackgroundColor(Color.parseColor(backgroundColor));
        }

        Button button_try_again = (Button) screenDialog.findViewById(networkErrorButtonRetry_button);
        button_try_again.setOnClickListener(new View.OnClickListener(){
          @Override
          public void onClick(View v) {
            webView.loadUrl("javascript:window.location.reload( true )");

            screenDialog.dismiss();
            screenDialog = null;
          }
        });

        screenDialog.show();
      }
    });
  }

  @SuppressWarnings("deprecation")
  private void hideErrorScreen() {
    // If the splash dialog is showing don't try to show it again
    if (screenDialog == null && !screenDialog.isShowing()) {
      return;
    }

    screenDialog.dismiss();
    screenDialog = null;
  }
}
