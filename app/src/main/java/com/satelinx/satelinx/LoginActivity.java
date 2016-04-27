package com.satelinx.satelinx;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.satelinx.satelinx.events.AuthEvent;
import com.satelinx.satelinx.events.AuthFailedEvent;
import com.satelinx.satelinx.events.AuthRequestEvent;
import com.satelinx.satelinx.events.AuthSuccessEvent;
import com.satelinx.satelinx.helpers.SatelinxSession;
import com.satelinx.satelinx.helpers.Serialization;
import com.satelinx.satelinx.models.User;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.SubscriberExceptionEvent;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;


/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity {

    public static final String TAG = LoginActivity.class.getSimpleName();

    @BindView(R.id.username)     protected EditText mUsernameView;
    @BindView(R.id.password)     protected EditText mPasswordView;
    @BindView(R.id.login_button) protected CircularProgressButton mUsernameSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mUsernameSignInButton.setIndeterminateProgressMode(true);
        mUsernameSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        mUsernameSignInButton.setProgress(0);
        mUsernameSignInButton.setProgress(1);

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            AuthEvent event = new AuthRequestEvent(username, password);
            EventBus.getDefault().post(event);
        }
    }

    private boolean isUsernameValid(String username) {
        return username.length() >= 4;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    @SuppressWarnings("unused")
    public void onEventBackgroundThread(AuthRequestEvent event) {
        Gson gson = Serialization.getGsonInstance();
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setConverter(new GsonConverter(gson))
                .setEndpoint(SatelinxSession.API_ENDPOINT)
                .build();

        SatelinxSession session = restAdapter.create(SatelinxSession.class);

        try {
            /*
             * To authenticate we need three steps:
             *
             * 1. Get the user's salt (no ssl for now, can't
             *    transmit the password in plain text).
             * 2. Build the password hash with the obtained salt.
             * 3. Authenticate with the new hash.
             */
            User user = session.getSalt(event.getUsername());
            user.buildAuthorizationHash(event.getPassword());
            User authUser = session.authenticate(user.getUsername(), user.getAuthorizationHash(), "");

            // Reapply the auth hash from the previous user
            authUser.setAuthorizationHash(user.getAuthorizationHash());

            AuthSuccessEvent successEvent = new AuthSuccessEvent(user.getUsername(), authUser.toJson());
            EventBus.getDefault().post(successEvent);
        } catch (Exception e) {
            e.printStackTrace();

            // Something went wrong, let the user know
            AuthFailedEvent errorEvent = new AuthFailedEvent(event.getUsername());
            EventBus.getDefault().post(errorEvent);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEventMainThread(AuthSuccessEvent event) {
        mUsernameSignInButton.setProgress(100);
        startActivityWithUser(event.userJson);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEventMainThread(AuthFailedEvent event) {
        mUsernameSignInButton.setProgress(-1);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEventMainThread(SubscriberExceptionEvent e) {
        Log.d(TAG, "Exception event");
        mUsernameSignInButton.setProgress(-1);
    }

    protected void startActivityWithUser(JsonObject userJson) {
        if (userJson == null) {
            Toast.makeText(this, R.string.error_no_user, Toast.LENGTH_LONG).show();
            return;
        }
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra(MainActivity.KEY_USER_JSON, userJson.toString());
        startActivity(i);
    }
}
