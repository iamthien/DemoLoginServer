package com.example.demologinserver;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

// For your API Response
class User {
    String _id;
    String email;
    String name;
    boolean isVerified;
    String lastLogin;
    String createdAt;
    String updatedAt;
    int __v;
}

class ApiResponse {
    boolean success;
    String message;
    User user;
}

public class MainActivity extends AppCompatActivity {

    private TextView dataTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize TextView to display data
        dataTextView = findViewById(R.id.dataTextView);

        // Login credentials (replace with actual input fields)
        String email = "nhthien.personal@gmail.com";
        String password = "Thien123@@" ;

        // Make the API login call
        loginUser(email, password);
    }

    // Create a custom CookieJar to manage cookies
    class MyCookieJar implements CookieJar {
        private final Map<String, List<Cookie>> cookieStore = new HashMap<>();

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            // Log the cookies when they are saved
            for (Cookie cookie : cookies) {
                Log.d("Cookie", "Saving cookie: " + cookie.name() + "=" + cookie.value());
            }
            cookieStore.put(url.host(), cookies);
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            List<Cookie> cookies = cookieStore.get(url.host());
            return cookies != null ? cookies : new ArrayList<>();
        }
    }

    // Login the user with email and password
    private void loginUser(String email, String password) {
        // OkHttpClient with custom CookieJar
        OkHttpClient client = new OkHttpClient.Builder()
                .cookieJar(new MyCookieJar())
                .build();

        // Retrofit setup
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://myapi-j4zl.onrender.com/") // Replace with your API base URL
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create the ApiService
        ApiService apiService = retrofit.create(ApiService.class);

        // Create login request body
        LoginRequest loginRequest = new LoginRequest(email, password);

        // Call the login API
        apiService.loginUser(loginRequest).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    // Process the response as you did previously
                    ApiResponse apiResponse = response.body();
                    if (apiResponse != null) {
                        String userData = "ID: " + apiResponse.user._id + "\n" +
                                "Email: " + apiResponse.user.email + "\n" +
                                "Name: " + apiResponse.user.name + "\n" +
                                "Verified: " + apiResponse.user.isVerified + "\n" +
                                "Last Login: " + apiResponse.user.lastLogin + "\n" +
                                "Created At: " + apiResponse.user.createdAt + "\n" +
                                "Updated At: " + apiResponse.user.updatedAt + "\n" +
                                "Version: " + apiResponse.user.__v;

                        // Display the data in the TextView
                        dataTextView.setText(userData);

                        // Get the OkHttpClient with cookies
                        List<Cookie> cookies = client.cookieJar().loadForRequest(HttpUrl.get("https://myapi-j4zl.onrender.com/"));
                        for (Cookie cookie : cookies) {
                            if (cookie.name().equals("token")) {  // Replace "token" with the actual name of the token cookie
                                Log.d("Token", "Token: " + cookie.value());
                            }
                        }

                        // Alternatively, check the response body or headers for the token
                        if (response.headers().get("Authorization") != null) {
                            String token = response.headers().get("Authorization");
                            Log.d("Token", "Authorization Header: " + token);
                        } else if (apiResponse.user != null && apiResponse.user._id != null) {
                            Log.d("Token", "Token from Body: " + apiResponse.user._id);
                        }
                    }
                } else {
                    // Log detailed information about the failure
                    Log.e("Error", "Request failed: " + response.code() + " " + response.message());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e("Error", "Response error body: " + errorBody);
                    } catch (IOException e) {
                        Log.e("Error", "Error reading the error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Log detailed information about the failure
                Log.e("Error", "Network error", t);
            }
        });
    }

    // Define your API interface (ApiService)
    interface ApiService {
        // POST login request
        @POST("api/auth/login/") // Replace with your login endpoint
        Call<ApiResponse> loginUser(@Body LoginRequest loginRequest);
    }

    // Define the LoginRequest object to send email and password
    class LoginRequest {
        String email;
        String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
}
