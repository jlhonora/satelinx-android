package com.satelinx.satelinx;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.dd.CircularProgressButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.satelinx.satelinx.events.AuthEvent;
import com.satelinx.satelinx.events.AuthFailedEvent;
import com.satelinx.satelinx.events.AuthRequestEvent;
import com.satelinx.satelinx.events.AuthSuccessEvent;
import com.satelinx.satelinx.helpers.SatelinxSession;
import com.satelinx.satelinx.models.User;
import com.satelinx.satelinx.models.typeAdapters.UserTypeAdapter;

import de.greenrobot.event.EventBus;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {

    public static final String TAG = LoginActivity.class.getSimpleName();

    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private CircularProgressButton mUsernameSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);
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

        mUsernameSignInButton = (CircularProgressButton) findViewById(R.id.login_button);
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
     * If there are form errors (invalid email, missing fields, etc.), the
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

        // Check for a valid email address.
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

    private boolean isUsernameValid(String email) {
        return email.length() >= 4;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    public void onEventBackgroundThread(AuthRequestEvent event) {
        Log.d(TAG, "Got AuthRequestEvent with username " + event.getUsername());
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(User.class, new UserTypeAdapter())
                .create();
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setConverter(new GsonConverter(gson))
                .setEndpoint(SatelinxSession.API_ENDPOINT)
                .build();

        SatelinxSession session = restAdapter.create(SatelinxSession.class);

        try {
            User user = session.getSalt(event.getUsername());
            Log.d(TAG, "Got salt " + user.getSalt());
            user.buildAuthorizationHash(event.getPassword());
            Log.d(TAG, "Auth hash: " + user.getAuthorizationHash());
            session.authenticate(user.getUsername(), user.getAuthorizationHash());
            AuthSuccessEvent successEvent = new AuthSuccessEvent(user.getUsername());
            EventBus.getDefault().post(successEvent);
        } catch (Exception e) {
            e.printStackTrace();
            AuthFailedEvent errorEvent = new AuthFailedEvent(event.getUsername());
            EventBus.getDefault().post(errorEvent);
        }
    }

    public void onEventMainThread(AuthSuccessEvent event) {
        Log.d(TAG, "Got auth success");
        mUsernameSignInButton.setProgress(100);
    }

    public void onEventMainThread(AuthFailedEvent event) {
        Log.d(TAG, "Got auth failed");
        mUsernameSignInButton.setProgress(-1);
    }
}



