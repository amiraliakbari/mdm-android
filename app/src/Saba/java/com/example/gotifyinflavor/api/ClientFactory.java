package com.example.gotifyinflavor.api;

import com.example.gotifyinflavor.SSLSettings;
import com.example.gotifyinflavor.Settings;
import com.github.gotify.client.ApiClient;
import com.github.gotify.client.api.UserApi;
import com.github.gotify.client.api.VersionApi;
import com.github.gotify.client.auth.ApiKeyAuth;
import com.github.gotify.client.auth.HttpBasicAuth;

public class ClientFactory {
    public static com.github.gotify.client.ApiClient unauthorized(
            String baseUrl, SSLSettings sslSettings) {
        return defaultClient(new String[0], baseUrl + "/", sslSettings);
    }

    public static ApiClient basicAuth(
            String baseUrl, SSLSettings sslSettings, String username, String password) {
        ApiClient client = defaultClient(new String[] {"basicAuth"}, baseUrl + "/", sslSettings);
        HttpBasicAuth auth = (HttpBasicAuth) client.getApiAuthorizations().get("basicAuth");
        auth.setUsername(username);
        auth.setPassword(password);
        return client;
    }

    public static ApiClient clientToken(String baseUrl, SSLSettings sslSettings, String token) {
        ApiClient client =
                defaultClient(new String[] {"clientTokenHeader"}, baseUrl + "/", sslSettings);
        ApiKeyAuth tokenAuth = (ApiKeyAuth) client.getApiAuthorizations().get("clientTokenHeader");
        tokenAuth.setApiKey(token);
        return client;
    }

    public static VersionApi versionApi(String baseUrl, SSLSettings sslSettings) {
        return unauthorized(baseUrl, sslSettings).createService(VersionApi.class);
    }

    public static UserApi userApiWithToken(Settings settings) {
        return clientToken(settings.url(), settings.sslSettings(), settings.token())
                .createService(UserApi.class);
    }

    private static ApiClient defaultClient(
            String[] authentications, String baseUrl, SSLSettings sslSettings) {
        ApiClient client = new ApiClient(authentications);
        CertUtils.applySslSettings(client.getOkBuilder(), sslSettings);
        client.getAdapterBuilder().baseUrl(baseUrl);
        return client;
    }
}
