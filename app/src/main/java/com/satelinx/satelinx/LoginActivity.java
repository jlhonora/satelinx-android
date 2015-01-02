package com.satelinx.satelinx;

import android.app.Activity;
import android.os.AsyncTask;
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
import com.satelinx.satelinx.events.AuthEvent;
import com.satelinx.satelinx.events.AuthFailedEvent;
import com.satelinx.satelinx.events.AuthRequestEvent;
import com.satelinx.satelinx.helpers.SatelinxSession;

import de.greenrobot.event.EventBus;
import retrofit.RestAdapter;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {

    public static final String TAG = LoginActivity.class.getSimpleName();

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

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

        final CircularProgressButton mUsernameSignInButton = (CircularProgressButton) findViewById(R.id.login_button);
        mUsernameSignInButton.setIndeterminateProgressMode(true);
        mUsernameSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }


        final CircularProgressButton mUsernameSignInButton = (CircularProgressButton) findViewById(R.id.login_button);
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

        AuthEvent event = new AuthRequestEvent(username);
        EventBus.getDefault().post(event);

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
            // mAuthTask = new UserLoginTask(username, password);
            // mAuthTask.execute((Void) null);
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
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(SatelinxSession.API_ENDPOINT)
                .build();

        SatelinxSession session = restAdapter.create(SatelinxSession.class);

        try {
            User user = session.getSalt(event.getUsername());
            Log.d(TAG, "Got salt " + user.salt);
            user.buildAuthorizationHash(event.getPassword());
            Log.d(TAG, "Auth hash: " + user.authorizationHash);
            session.authenticate(user.username, user.authorizationHash);
        } catch (Exception e) {
            e.printStackTrace();
            AuthEvent errorEvent = new AuthFailedEvent(event.getUsername());
            EventBus.getDefault().post(errorEvent);
        }
    }

    public void onEventMainThread(AuthFailedEvent event) {
        Log.d(TAG, "Got auth failed");
        final CircularProgressButton mUsernameSignInButton = (CircularProgressButton) findViewById(R.id.login_button);
        mUsernameSignInButton.setProgress(-1);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mUsername = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mUsername)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            // showProgress(false);

            final CircularProgressButton mUsernameSignInButton = (CircularProgressButton) findViewById(R.id.login_button);
            if (success) {
                mUsernameSignInButton.setProgress(100);
            } else {
                mUsernameSignInButton.setProgress(-1);
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            // showProgress(false);
        }
    }
}



