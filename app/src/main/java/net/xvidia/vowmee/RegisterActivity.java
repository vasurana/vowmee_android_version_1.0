package net.xvidia.vowmee;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.user.IdentityManager;
import com.amazonaws.mobile.user.IdentityProvider;
import com.amazonaws.mobile.user.signin.FacebookSignInProvider;
import com.amazonaws.mobile.user.signin.SignInManager;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import net.xvidia.vowmee.network.IAPIConstants;
import net.xvidia.vowmee.network.ServiceURLManager;
import net.xvidia.vowmee.network.VolleySingleton;
import net.xvidia.vowmee.network.model.Register;
import net.xvidia.vowmee.network.model.ResponseSocialAuth;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.regex.Pattern;

import io.fabric.sdk.android.Fabric;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText mFullnameEditText;
    private EditText mPasswordEditText;
    private AutoCompleteTextView mEmailEditText;
    private AutoCompleteTextView mUsernameEditText;
    private Button mRegisterButton;
    private SignInManager signInManager;
    private TwitterLoginButton twitterLoginButton;
    private TwitterSession twitterSession;
    private ImageButton mPasswordVisibilityView;
    private ImageButton customTwitterLogin;
    private ImageButton facebookLoginButton;
    /**
     * SignInResultsHandler handles the final result from sign in. Making it static is a best
     * practice since it may outlive the SplashActivity's life span.
     */
    private class SignInResultsHandler implements IdentityManager.SignInResultsHandler {
        @Override
        public void onSuccess(final IdentityProvider provider) {
            // Load user name and image.
            AWSMobileClient.defaultMobileClient()
                    .getIdentityManager().loadUserInfoAndImage(provider, new Runnable() {
                @Override
                public void run() {
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

            Toast.makeText(RegisterActivity.this, String.format("Sign-in with %s canceled.",
                    provider.getDisplayName()), Toast.LENGTH_LONG).show();
        }

        /**
         * Receives the sign-in result that an error occurred signing in and shows a toast.
         * @param provider the identity provider with which the user attempted sign-in.
         * @param ex the exception that occurred.
         */
        @Override
        public void onError(final IdentityProvider provider, final Exception ex) {

            final AlertDialog.Builder errorDialogBuilder = new AlertDialog.Builder(RegisterActivity.this);
            errorDialogBuilder.setTitle("Sign-In Error");
            errorDialogBuilder.setMessage(
                    String.format("Sign-in with %s failed.\n%s", provider.getDisplayName(), ex.getMessage()));
            errorDialogBuilder.setNeutralButton("Ok", null);
            errorDialogBuilder.show();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialiseSocialNetworks();
        setContentView(R.layout.activity_register);
        initialiseView();

    }

    private void initialiseView() {
        mFullnameEditText = (EditText) findViewById(R.id.register_full_name);
        mEmailEditText = (AutoCompleteTextView) findViewById(R.id.register_email);
        mUsernameEditText = (AutoCompleteTextView) findViewById(R.id.register_username);
        mPasswordEditText = (EditText) findViewById(R.id.register_password);
        mRegisterButton = (Button) findViewById(R.id.register_button);
        mPasswordVisibilityView =(ImageButton) findViewById(R.id.showpassword);
        customTwitterLogin = (ImageButton) findViewById(R.id.customer_twitter_login_button);
        twitterLoginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        facebookLoginButton =(ImageButton) findViewById(R.id.facebook_login_button);
        mPasswordVisibilityView.setVisibility(View.GONE);
        signInManager = new SignInManager(this);
        signInManager = SignInManager.getSignInProviderManager();
        signInManager.setResultsHandler(this, new SignInResultsHandler());

        // Initialize sign-in buttons.
        signInManager.initializeSignInButton(FacebookSignInProvider.class, facebookLoginButton);

        customTwitterLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                twitterLoginButton.performClick();

            }
        });
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                twitterSession = result.data;
            }

            @Override
            public void failure(TwitterException exception) {
//                facebookTextView.setText(exception.getMessage());
            }
        });
        twitterLoginButton.setVisibility(View.INVISIBLE);
        mPasswordEditText.addTextChangedListener(new TextWatcher() {
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
            public boolean onTouch(View v, MotionEvent event) {
                final boolean isOutsideView = event.getX() < 0 ||
                        event.getX() > v.getWidth() ||
                        event.getY() < 0 ||
                        event.getY() > v.getHeight();

                // change input type will reset cursor position, so we want to save it
                final int cursor = mPasswordEditText.getSelectionStart();

                if (isOutsideView || MotionEvent.ACTION_UP == event.getAction()) {
                    mPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    mPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }

                mPasswordEditText.setSelection(cursor);
                return true;
            }
        });
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        mEmailEditText.setText(getEmailId());
    }



    private String getEmailId(){
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        String possibleEmail="";
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                possibleEmail = account.name;
            }
        }

        return possibleEmail;
    }



    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mUsernameEditText.setError(null);
        mPasswordEditText.setError(null);
        mEmailEditText.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailEditText.getText().toString();
        String username = mUsernameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(username)) {
            mUsernameEditText.setError(getString(R.string.error_field_required));
            focusView = mUsernameEditText;
            cancel = true;
        }
        if (TextUtils.isEmpty(password)) {
            mPasswordEditText.setError(getString(R.string.error_field_required));
            focusView = mPasswordEditText;
            cancel = true;
        } else if (!isPasswordValid(email)) {
            mPasswordEditText.setError(getString(R.string.error_incorrect_password));
            focusView = mPasswordEditText;
            cancel = true;
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailEditText.setError(getString(R.string.error_field_required));
            focusView = mEmailEditText;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailEditText.setError(getString(R.string.error_invalid_email));
            focusView = mEmailEditText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        }else{
            startActivity(new Intent(RegisterActivity.this, NewUserProfile.class));
            finish();
        }
    }

    private void initialiseSocialNetworks(){
        String consumerKey = getResources().getString(R.string.twitter_consumer_key);
        String consumerSecret = getResources().getString(R.string.twitter_secret_key);
//        FacebookSdk.sdkInitialize(getApplicationContext());
//        callbackManager = CallbackManager.Factory.create();
        TwitterAuthConfig authConfig =  new TwitterAuthConfig(consumerKey, consumerSecret);
        Fabric.with(this, new Twitter(authConfig), new Crashlytics());
    }

    private boolean isEmailValid(String email) {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
            if (emailPattern.matcher(email).matches()) {
                return true;
            }
        return false;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if(callbackManager != null)
//            callbackManager.onActivityResult(requestCode, resultCode, data);
        if(signInManager != null)
            signInManager.handleActivityResult(requestCode, resultCode, data);
        if(twitterLoginButton != null)
            twitterLoginButton.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onResume() {
        super.onResume();
        ;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */

    private void sendRegisterRequest(String name, String emailId, String contactNo, String facebookId,long twitterId){
        try {

            String url =new ServiceURLManager().getUrl(IAPIConstants.API_KEY_SOCIAL_REGISTER);
            Register register = new Register();
            register.setName(name);
            register.setEmailId(emailId);
            register.setContactNo(contactNo);
            register.setFaceBookId(facebookId);
            register.setTwitterId(twitterId);
            register.setUsername(contactNo);

            ObjectMapper mapper = new ObjectMapper();
            String jsonObject = null;
            try {
                jsonObject = mapper.writeValueAsString(register);
            } catch (IOException e) {
                e.printStackTrace();
            }
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObject, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    ObjectMapper mapper = new ObjectMapper();
                    ResponseSocialAuth obj = null;
                    try {
                        obj = mapper.readValue(response.toString(), ResponseSocialAuth.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String username = obj.getUsername();
                    Intent desire = new Intent();
                    desire.setClass(RegisterActivity.this,ProfileActivity.class);
                    startActivity(desire);
//                startActivityForResult(desire, 500);
//                overridePendingTransition(android.support.design.R.anim.abc_slide_in_bottom,android.support.design.R.anim.abc_slide_in_top);
                    finish();
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {

                    Log.e("VolleyError ", "" + error.networkResponse.statusCode);
                    if(error.networkResponse.statusCode == HttpURLConnection.HTTP_NOT_FOUND){
                        startActivity(new Intent(RegisterActivity.this, RegisterActivity.class));
                        finish();
                    }
                }
            });
            VolleySingleton.getInstance(MyApplication.getAppContext()).addToRequestQueue(request);
//			}
        }catch(Exception e){

        }
    }
}
