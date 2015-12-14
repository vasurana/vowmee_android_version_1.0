package net.xvidia.vowmee;

import android.accounts.Account;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentSender;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.user.IdentityManager;
import com.amazonaws.mobile.user.IdentityProvider;
import com.amazonaws.mobile.user.signin.FacebookSignInProvider;
import com.amazonaws.mobile.user.signin.SignInManager;
import com.amazonaws.mobile.user.signin.SignInProvider;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.crashlytics.android.Crashlytics;
import com.facebook.appevents.AppEventsLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import net.xvidia.vowmee.network.IAPIConstants;
import net.xvidia.vowmee.network.ServiceURLManager;
import net.xvidia.vowmee.network.VolleySingleton;
import net.xvidia.vowmee.network.model.ResponseSocialAuth;
import net.xvidia.vowmee.network.model.SocialAuthFacebook;
import net.xvidia.vowmee.network.model.SocialAuthTwitter;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private final static String LOG_TAG = LoginActivity.class.getSimpleName();
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "ravi:ravi", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private SignInManager signInManager;
    JsonObjectRequest request = null;
    /**
     * SignInResultsHandler handles the final result from sign in. Making it static is a best
     * practice since it may outlive the SplashActivity's life span.
     */
    private class SignInResultsHandler implements IdentityManager.SignInResultsHandler {
        /**
         * Receives the successful sign-in result and starts the main activity.
         * @param provider the identity provider used for sign-in.
         */
        @Override
        public void onSuccess(final IdentityProvider provider) {

            logoutTwitter();
            sendFacebookLogin(provider.getUserId(),provider.getToken(),provider.getTokenExpiryDate());
            // Load user name and image.
            AWSMobileClient.defaultMobileClient()
                    .getIdentityManager().loadUserInfoAndImage(provider, new Runnable() {
                @Override
                public void run() {
                    Log.d(LOG_TAG, "Launching Main Activity...");
//                    startActivity(new Intent(LoginActivity.this, RegisterActivity.class)
//                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
//                    // finish should always be called on the main thread.
//                    finish();
                }
            });
        }

        /**
         * Recieves the sign-in result indicating the user canceled and shows a toast.
         * @param provider the identity provider with which the user attempted sign-in.
         */
        @Override
        public void onCancel(final IdentityProvider provider) {
            Log.d(LOG_TAG, String.format("User sign-in with %s canceled.",
                    provider.getDisplayName()));

            Toast.makeText(LoginActivity.this, String.format("Sign-in with %s canceled.",
                    provider.getDisplayName()), Toast.LENGTH_LONG).show();
        }

        /**
         * Receives the sign-in result that an error occurred signing in and shows a toast.
         * @param provider the identity provider with which the user attempted sign-in.
         * @param ex the exception that occurred.
         */
        @Override
        public void onError(final IdentityProvider provider, final Exception ex) {
            Log.e(LOG_TAG, String.format("User Sign-in failed for %s : %s",
                    provider.getDisplayName(), ex.getMessage()), ex);

            final AlertDialog.Builder errorDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
            errorDialogBuilder.setTitle("Sign-In Error");
            errorDialogBuilder.setMessage(
                    String.format("Sign-in with %s failed.\n%s", provider.getDisplayName(), ex.getMessage()));
            errorDialogBuilder.setNeutralButton("Ok", null);
            errorDialogBuilder.show();
        }
    }
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    //    private CallbackManager callbackManager;
//    private TextView facebookTextView;
    //    private ProfileTracker profileTracker;
//    private AccessTokenTracker accessTokenTracker;
    private TwitterLoginButton twitterLoginButton;
    private ImageButton loginButton,customTwitterLogin;
    private TwitterSession twitterSession;
    private GoogleApiClient mGoogleApiClient;
    private boolean mIntentInProgress;
    private boolean signedInUser;
    private ConnectionResult mConnectionResult;
    private ImageButton signinButton;
    private ProgressDialog mConnectionProgressDialog;
    private ImageButton mPasswordVisibilityView;
    private static final int RC_SIGN_IN = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialiseSocialNetworks();

//        requestWindowFeature(Window.FEATURE_NO_TITLE);Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login);
        // Set up the login form.
//        facebookTextView = (TextView) findViewById(R.id.facebookTextView);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordVisibilityView =(ImageButton) findViewById(R.id.showpassword);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.password || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        mPasswordView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mPasswordVisibilityView.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
        });
        mPasswordVisibilityView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event){
            final boolean isOutsideView = event.getX() < 0 ||
                    event.getX() > v.getWidth() ||
                    event.getY() < 0 ||
                    event.getY() > v.getHeight();

            // change input type will reset cursor position, so we want to save it
            final int cursor = mPasswordView.getSelectionStart();

            if (isOutsideView || MotionEvent.ACTION_UP == event.getAction()) {
                mPasswordView.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }else {
                mPasswordView.setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }

            mPasswordView.setSelection(cursor);
            return true;
        }
        });
        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordVideo();
//                attemptLogin();
            }
        });

        TextView forgetPassword = (TextView) findViewById(R.id.forgot_password);
        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                recordVideo();
//                attemptLogin();
                openProfile();
            }
        });
        underlineTextView(getString(R.string.login_forgot_password),forgetPassword);
        TextView register = (TextView) findViewById(R.id.register_user);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                recordVideo();
//                attemptLogin();
            }
        });
        underlineTextView(getString(R.string.login_register), register);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        loginButton =(ImageButton) findViewById(R.id.facebook_login_button);
        signInManager = new SignInManager(this);
        signInManager = SignInManager.getSignInProviderManager();
        signInManager.setResultsHandler(this, new SignInResultsHandler());

        // Initialize sign-in buttons.
        signInManager.initializeSignInButton(FacebookSignInProvider.class,
                loginButton);

//        profileTracker = new ProfileTracker() {
//            @Override
//            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
//                setProfile(currentProfile);
//            }
//        };

//        accessTokenTracker = new AccessTokenTracker() {
//            @Override
//            protected void onCurrentAccessTokenChanged(
//                    AccessToken oldAccessToken,
//                    AccessToken currentAccessToken) {
//                // On AccessToken changes fetch the new profile which fires the event on
//                // the ProfileTracker if the profile is different
//                Profile.fetchProfileForCurrentAccessToken();
//            }
//        };

        // Ensure that our profile is up to date
//        Profile.fetchProfileForCurrentAccessToken();
        setProfile();
        customTwitterLogin = (ImageButton) findViewById(R.id.customer_twitter_login_button);
        customTwitterLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutFacebook();
                twitterLoginButton.performClick();

            }
        });
        twitterLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                twitterSession = result.data;
//                facebookTextView.setText(result.data.getUserName() + "  userid" + twitterSession.getUserId());
                getTwitterEmailAddress();
            }

            @Override
            public void failure(TwitterException exception) {
//                facebookTextView.setText(exception.getMessage());
            }
        });
        twitterLoginButton.setVisibility(View.INVISIBLE);
        signinButton = (ImageButton) findViewById(R.id.signin);
        signinButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                googlePlusLogin();

            }
        });
//
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PLUS_LOGIN))
                .addScope(new Scope(Scopes.PLUS_ME))
                .addScope(new Scope(Scopes.PROFILE))
                .addScope(new Scope(Scopes.EMAIL))
                .build();
        mConnectionProgressDialog = new ProgressDialog(this);
        mConnectionProgressDialog.setMessage("Signing in...");
    }

    private void initialiseSocialNetworks(){
        String consumerKey = getResources().getString(R.string.twitter_consumer_key);
        String consumerSecret = getResources().getString(R.string.twitter_secret_key);
//        FacebookSdk.sdkInitialize(getApplicationContext());
//        callbackManager = CallbackManager.Factory.create();
        TwitterAuthConfig authConfig =  new TwitterAuthConfig(consumerKey, consumerSecret);
        Fabric.with(this, new Twitter(authConfig), new Crashlytics());
    }

//    private void getProfile(String usrId){
//        new GraphRequest(
//                AccessToken.getCurrentAccessToken(),
//                "/"+usrId,
//                null,
//                HttpMethod.GET,
//                new GraphRequest.Callback() {
//                    public void onCompleted(GraphResponse response) {
//                        if(response!= null) {
//                            String result = response.getJSONObject().toString();//getRawResponse();
//                            facebookTextView.setText(result);
//                        }
//                    }
//                }
//        ).executeAsync();
//    }

    private void logoutFacebook(){
        try {
            final IdentityManager identityManager =
                    AWSMobileClient.defaultMobileClient().getIdentityManager();
            final IdentityProvider identityProvider =
                    identityManager.getCurrentIdentityProvider();
            identityProvider.signOut();
        }catch(NullPointerException e){

        }catch(Exception ex){

        }
    }

    private void logoutTwitter(){
        try{
            Twitter.getSessionManager().clearActiveSession();
            Twitter.logOut();
        }catch(NullPointerException e){

        }catch(Exception ex){

        }
    }
    private void underlineTextView(String text, TextView textView){
        SpannableString str = new SpannableString(text);
        str.setSpan(new UnderlineSpan(), 0, str.length(), Spanned.SPAN_PARAGRAPH);
        textView.setText(str);
    }
    private void recordVideo(){
        Intent desire = new Intent();
//        desire.setClass(LoginActivity.this,RecordVideo.class);
        desire.setClass(LoginActivity.this, Home.class);
        startActivity(desire);
    }

    private void openProfile(){
        Intent desire = new Intent();
//        desire.setClass(LoginActivity.this,RecordVideo.class);
        desire.setClass(LoginActivity.this,UserProfile.class);
        startActivity(desire);
    }
    private void setProfile() {
        final SignInProvider provider = signInManager.getPreviouslySignedInProvider();

        // if the user was already previously in to a provider.
        if (provider != null) {
            // asyncronously handle refreshing credentials and call our handler.
            signInManager.refreshCredentialsWithProvider(LoginActivity.this,
                    provider, new SignInResultsHandler());
        }
    }


    private void getTwitterEmailAddress(){
        twitterSession = Twitter.getSessionManager().getActiveSession();
        TwitterAuthToken authToken = twitterSession.getAuthToken();
        String token = authToken.token;
        String secret = authToken.secret;

        sendTwitterLogin(twitterSession.getUserId(),token,secret);
//        TwitterAuthClient authClient = new TwitterAuthClient();
//        authClient.requestEmail(twitterSession, new Callback<String>() {
//            @Override
//            public void success(Result<String> result) {
////                facebookTextView.setText(result.data);
//            }
//
//            @Override
//            public void failure(TwitterException exception) {
////                getResources().getString(getResources().getIdentifier("prompt_name", "string", getPackageName()));
////                facebookTextView.setText(exception.getMessage());
//            }
//        });
    }
    private void resolveSignInError() {
        if(mConnectionResult != null)
            if (mConnectionResult.hasResolution()) {
                try {
                    mIntentInProgress = true;
                    mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
                } catch (IntentSender.SendIntentException e) {
                    mIntentInProgress = false;
                    mGoogleApiClient.connect();
                }
            }
    }
    //
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }

        if (!mIntentInProgress) {
            // store mConnectionResult
            mConnectionResult = result;

            if (signedInUser) {
                resolveSignInError();
            }
        }
    }



    @Override
    public void onConnected(Bundle arg0) {
        signedInUser = false;
        mConnectionProgressDialog.dismiss();
        Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
        getProfileInformation();
    }

    private void updateProfile(boolean isSignedIn) {
        if (isSignedIn) {
            GetIdTokenTask gToken = new GetIdTokenTask();
            gToken.execute((Void) null);
        } else {
        }
    }

    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                String personName = currentPerson.getDisplayName();
                String personPhotoUrl = currentPerson.getImage().getUrl();
                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
                Log.i("PROFILE info", currentPerson.toString());
//				new LoadProfileImage(image).execute(personPhotoUrl);

                // update profile frame with new info about Google Account
                // profile
                updateProfile(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
        updateProfile(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    signedInUser = false;

                }
                mIntentInProgress = false;
                if (!mGoogleApiClient.isConnecting()) {
                    mGoogleApiClient.connect();
                }
                break;
        }
//        if(callbackManager != null)
//            callbackManager.onActivityResult(requestCode, resultCode, data);
        if(signInManager != null)
            signInManager.handleActivityResult(requestCode, resultCode, data);
        if(twitterLoginButton != null)
            twitterLoginButton.onActivityResult(requestCode, resultCode, data);
    }

    private void googlePlusLogin() {
        if (!mGoogleApiClient.isConnecting()) {
            signedInUser = true;
            resolveSignInError();
        }
    }

    private void googlePlusLogout() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
            updateProfile(false);
        }
    }
    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
//            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
//                    .setAction(android.R.string.ok, new View.OnClickListener() {
//                        @Override
//                        @TargetApi(Build.VERSION_CODES.M)
//                        public void onClick(View v) {
//                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
//                        }
//                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        final AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();

        if (awsMobileClient == null) {
            // At this point, the app is resuming after being shut down and the onCreate
            // method has already fired an intent to launch the splash activity. The splash
            // activity will refresh the user's credentials and re-initialize all required
            // components. We bail out here, because without that initialization, the steps
            // that follow here would fail. Note that if you don't have any features enabled,
            // then there may not be any steps here to skip.
            return;
        }

        // pause/resume Mobile Analytics collection
        awsMobileClient.handleOnResume();
        AppEventsLogger.activateApp(this);
    }
    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }
    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
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
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
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
                if (pieces[0].equals(mEmail)) {
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
            showProgress(false);

            if (success) {
                Intent desire = new Intent();
                desire.setClass(LoginActivity.this,RegisterActivity.class);
                startActivity(desire);
//                startActivityForResult(desire, 500);
//                overridePendingTransition(android.support.design.R.anim.abc_slide_in_bottom,android.support.design.R.anim.abc_slide_in_top);
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private class GetIdTokenTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            String accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
            Account account = new Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            String scopes = "audience:server:client_id:" + ""; // Not the app's client ID.
            try {
                return GoogleAuthUtil.getToken(getApplicationContext(), account, scopes);
            } catch (IOException e) {
                Log.e("Google token", "Error retrieving ID token.", e);
                return null;
            } catch (GoogleAuthException e) {
                Log.e("Google token", "Error retrieving ID token.", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i("Google token", "ID token: " + result);
            if (result != null) {
                // Successfully retrieved ID Token
                // ...
            } else {
                // There was some error getting the ID Token
                // ...
            }
        }

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
//        profileTracker.stopTracking();
//        accessTokenTracker.startTracking();
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /*Api Request & Response*/

    private void sendFacebookLogin(String userId,String accessToken,String expiryDate ){
        try {

            String url =new ServiceURLManager().getUrl(IAPIConstants.API_KEY_AUTH_FACEBOOK);
            SocialAuthFacebook socialAuthFacebook = new SocialAuthFacebook();
            socialAuthFacebook.setAccessToken(accessToken);
            socialAuthFacebook.setUserId(userId);
            socialAuthFacebook.setExpiryDate(expiryDate);

            ObjectMapper mapper = new ObjectMapper();
            String jsonObject = null;
            try {
                jsonObject = mapper.writeValueAsString(socialAuthFacebook);
            } catch (IOException e) {
                e.printStackTrace();
            }
             request= new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    ObjectMapper mapper = new ObjectMapper();
                    ResponseSocialAuth obj = null;
                    try {
                        obj = mapper.readValue(response.toString(), ResponseSocialAuth.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String getUsername = obj.getUsername();
                    Map<String, String> headers = request.getResponseHeaders();
                    if(!getUsername.isEmpty()) {

                        Log.i("X-AUTH-TOKEN ", "" + headers.get("X-AUTH-TOKEN"));
                        startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
                        finish();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                        Log.e("VolleyError ", ""+error.networkResponse.statusCode);
                    if(error.networkResponse.statusCode == HttpURLConnection.HTTP_NOT_FOUND){
                        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                         finish();
                    }
                }
            });
//            final JsonObjectRequest requestFinal = request;
            VolleySingleton.getInstance(MyApplication.getAppContext()).addToRequestQueue(request);
//			}
        }catch(Exception e){

        }
    }


    private void sendTwitterLogin(long userId,String accessToken,String accessSecret ){
        try {

            String url =new ServiceURLManager().getUrl(IAPIConstants.API_KEY_AUTH_TWITTER);
            SocialAuthTwitter socialAuthTwitter = new SocialAuthTwitter();
            socialAuthTwitter.setAccessToken(accessToken);
            socialAuthTwitter.setUserId(userId);
            socialAuthTwitter.setAccessSecret(accessSecret);

            ObjectMapper mapper = new ObjectMapper();
            String jsonObject = null;
            try {
                jsonObject = mapper.writeValueAsString(socialAuthTwitter);
            } catch (IOException e) {
                e.printStackTrace();
            }
            request= new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    ObjectMapper mapper = new ObjectMapper();
                    ResponseSocialAuth obj = null;
                    try {
                        obj = mapper.readValue(response.toString(), ResponseSocialAuth.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String getUsername = obj.getUsername();
                    Map<String, String> headers = request.getResponseHeaders();
                    if(!getUsername.isEmpty()) {

                        Log.i("X-AUTH-TOKEN ", "" + headers.get("X-AUTH-TOKEN"));
                        startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
                        finish();
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                    Log.e("VolleyError ", ""+error.networkResponse.statusCode);
                    if(error.networkResponse.statusCode == HttpURLConnection.HTTP_NOT_FOUND){
                        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                        finish();
                    }
                }
            });
//            final JsonObjectRequest requestFinal = request;
            VolleySingleton.getInstance(MyApplication.getAppContext()).addToRequestQueue(request);
//			}
        }catch(Exception e){

        }
    }
}

