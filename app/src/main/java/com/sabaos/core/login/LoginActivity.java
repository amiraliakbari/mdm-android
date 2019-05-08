package com.sabaos.core.login;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;

import android.widget.EditText;
import android.widget.TextView;

import com.sabaos.core.R;
import com.sabaos.core.init.InitializationActivity;
import com.sabaos.messaging.client.ApiClient;
import com.sabaos.messaging.client.api.ClientApi;
import com.sabaos.messaging.client.api.UserApi;
import com.sabaos.messaging.client.model.Client;
import com.sabaos.messaging.messaging.SSLSettings;
import com.sabaos.messaging.messaging.Settings;
import com.sabaos.messaging.messaging.Utils;
import com.sabaos.messaging.messaging.api.ApiException;
import com.sabaos.messaging.messaging.api.Callback;
import com.sabaos.messaging.messaging.api.ClientFactory;
import com.sabaos.messaging.messaging.log.Log;
import com.sabaos.messaging.messaging.log.UncaughtExceptionHandler;

import static com.sabaos.messaging.messaging.api.Callback.callInUI;

public class LoginActivity extends AppCompatActivity {

    // return value from startActivityForResult when choosing a certificate
    private final int FILE_SELECT_CODE = 1;

    private Settings settings;

    private boolean disableSSLValidation;
    private String caCertContents;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UncaughtExceptionHandler.registerCurrentThread();
        setContentView(R.layout.activity_login);
        textView = (TextView) findViewById(R.id.firstrun);
        textView.setText("Initializing . . .");
        textView.setTextSize(20);
        Log.i("Entering " + getClass().getSimpleName());
        settings = new Settings(this);
        doCheckUrl();
        onValidUrl("https://push.sabaos.com");
        doLogin();

    }

    public void doCheckUrl() {
        String url = "https://push.sabaos.com";

        final String fixedUrl = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;

        ClientFactory.versionApi(fixedUrl, tempSSLSettings())
                .getVersion();
    }

    public void onValidUrl(String url) {
        settings.url(url);
    }

    private Callback.ErrorCallback onInvalidUrl(String url) {
        return (exception) -> {
            Utils.showSnackBar(LoginActivity.this, versionError(url, exception));
        };
    }

    public void doLogin() {
        String username = "admin";
        String password = "admin";

        ApiClient client =
                ClientFactory.basicAuth(settings.url(), tempSSLSettings(), username, password);
        client.createService(UserApi.class)
                .currentUser()
                .enqueue(callInUI(this, (user) -> newClientDialog(client), this::onInvalidLogin));
    }

    private void onInvalidLogin(ApiException e) {
//        Utils.showSnackBar(this, getString(R.string.wronguserpw));
    }

    private void newClientDialog(ApiClient client) {
        EditText clientName = new EditText(this);
        clientName.setText(Build.MODEL);
        doCreateClient(client, clientName);
    }

    public void doCreateClient(ApiClient client, EditText nameProvider) {

        Client newClient = new Client().name(nameProvider.getText().toString());
        client.createService(ClientApi.class)
                .createClient(newClient)
                .enqueue(callInUI(this, this::onCreatedClient, this::onFailedToCreateClient));

    }


    private void onCreatedClient(Client client) {
        settings.token(client.getToken());
        settings.validateSSL(!disableSSLValidation);
        settings.cert(caCertContents);

        Utils.showSnackBar(this, getString(R.string.created_client));
        startActivity(new Intent(this, InitializationActivity.class));
        finish();
    }

    private void onFailedToCreateClient(ApiException e) {
//        Utils.showSnackBar(this, getString(R.string.create_client_failed));
    }

    private void onCancelClientDialog(DialogInterface dialog, int which) {
    }

    private String versionError(String url, ApiException exception) {
        return getString(R.string.version_failed, url + "/version", exception.code());
    }

    private SSLSettings tempSSLSettings() {
        return new SSLSettings(!disableSSLValidation, caCertContents);
    }
}
