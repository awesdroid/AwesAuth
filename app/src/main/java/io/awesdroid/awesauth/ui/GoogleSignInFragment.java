package io.awesdroid.awesauth.ui;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONObject;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.awesdroid.awesauth.R;
import io.awesdroid.awesauth.utils.Utils;
import io.awesdroid.awesauth.viewmodel.GoogleSignInViewModel;
import io.awesdroid.awesauth.viewmodel.SettingsViewModel;

import static io.awesdroid.awesauth.ui.MainActivity.RC_SIGN_IN;


public class GoogleSignInFragment extends Fragment {
    private static final String TAG = GoogleSignInFragment.class.getSimpleName();
    private boolean useIdToken = false;
    private String authType = "-1";

    private GoogleSignInViewModel googleSignInViewModel;
    private Unbinder unbinder;

    @BindView(R.id.button_signin)
    Button signInButton;
    @BindView(R.id.button_signout)
    Button signOutButton;
    @BindView(R.id.button_refresh_token)
    Button refreshTokenButton;

    @BindView(R.id.auth_status)
    TextView authStatusTextView;

    @BindView(R.id.toke_info_container)
    ViewGroup tokeInfoContainer;
    @BindView(R.id.token_info_expire)
    TextView tokenInfoExpireTextView;
    @BindView(R.id.token_info_id)
    TextView tokenInfoIdTextView;

    @BindView(R.id.userinfo_avatar)
    ImageView userInfoAvatarImageView;
    @BindView(R.id.userinfo_name)
    TextView userInfoNameTextView;
    @BindView(R.id.userinfo)
    TextView userInfoTextView;
    @BindView(R.id.userinfo_container)
    ViewGroup userInfoContainer;
    @BindView(R.id.userinfo_scrollview)
    ScrollView userInfoScrollview;

    private Dialog progressDialog;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        View rootView = inflater.inflate(R.layout.fragment_google_signin, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        View rv = inflater.inflate(R.layout.progress_bar, container, false);
        progressDialog = new Dialog(requireActivity());
        progressDialog.setContentView(rv);
        progressDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        signInButton.setOnClickListener(v -> signIn());
        signOutButton.setOnClickListener(v -> signOut());
        refreshTokenButton.setOnClickListener(v -> refreshToken());

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        googleSignInViewModel = ViewModelProviders.of(requireActivity()).get(GoogleSignInViewModel.class);
        googleSignInViewModel.getAccount().observe(requireActivity(), account -> {
            Log.d(TAG, "onChanged(): account = " + account);
            updateUI(account);
        });

        SettingsViewModel settingsViewModel =
                ViewModelProviders.of(requireActivity()).get(SettingsViewModel.class);
        settingsViewModel.getAuthType().observe(requireActivity(), this::setAuthType);
        settingsViewModel.isGoogleSignInUseIdToken().observe(requireActivity(), this::setUseIdToken);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult(): resultCode = " + requestCode);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        if (unbinder != null) {
            unbinder.unbind();
            unbinder = null;
        }
        super.onDestroy();
    }

    private void updateUI(GoogleSignInAccount account) {
        Log.d(TAG, "updateUI(): account = " + account);
        progressDialog.dismiss();
        if (account != null) {
            if (account.getPhotoUrl() != null) {
                Glide.with(requireActivity())
                        .load(account.getPhotoUrl())
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .into(userInfoAvatarImageView);
            }

            authStatusTextView.setText(getString(R.string.auth_granted));
            authStatusTextView.setTextColor(Color.WHITE);

            signInButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.VISIBLE);
            refreshTokenButton.setVisibility(View.VISIBLE);
            refreshTokenButton.setEnabled(useIdToken);

            tokeInfoContainer.setVisibility(View.VISIBLE);
            tokenInfoExpireTextView.setText(account.isExpired()?"ture":"false");
            if (useIdToken) {
                tokenInfoIdTextView.setText(account.getIdToken());
            } else {
                tokenInfoIdTextView.setText("n/a");
            }

            userInfoContainer.setVisibility(View.VISIBLE);
            userInfoNameTextView.setText(account.getDisplayName());
            userInfoScrollview.setFocusableInTouchMode(true);
            userInfoScrollview.fullScroll(ScrollView.FOCUS_UP);
            userInfoTextView.setText(buildUserInfo(account));

        } else {
            authStatusTextView.setText(getString(R.string.auth_not_granted));
            authStatusTextView.setTextColor(Color.DKGRAY);

            signInButton.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.GONE);
            refreshTokenButton.setVisibility(View.GONE);

            tokeInfoContainer.setVisibility(View.GONE);

            userInfoNameTextView.setText("");
            userInfoContainer.setVisibility(View.GONE);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            updateUI(account);
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                    .setTitle("Error")
                    .setMessage("Failed code: " + e.getStatusCode())
                    .setPositiveButton(R.string.ok, null)
                    .setCancelable(true)
                    .show();
        }
    }

    private void signIn() {
        switch (authType) {
            case "-1":
                new AlertDialog.Builder(requireActivity())
                        .setMessage("Please select Auth type firstly in Settings")
                        .setPositiveButton(R.string.ok, (dialog, which) ->
                                ((MainActivity) requireActivity()).navigateToSettings())
                        .setNegativeButton(R.string.cancel, null)
                        .setCancelable(true)
                        .show();
                break;
            case "1":
                progressDialog.show();
                googleSignInViewModel.signIn(useIdToken, RC_SIGN_IN);
                break;
            default:
                throw new IllegalStateException("authType is not 1");
        }
    }

    private void signOut() {
        progressDialog.show();
        googleSignInViewModel.signOut();
    }

    private void refreshToken() {
        progressDialog.show();
        googleSignInViewModel.refreshToken();
    }

    private String buildUserInfo(GoogleSignInAccount account) {
        String prettyJsonString = "";
        try {
            JSONObject json = new JSONObject(account.zac());
            if (json.has("tokenId"))
                json.remove("tokenId");
            prettyJsonString = Utils.prettyJson(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "buildUserInfo(): " + prettyJsonString);
        return prettyJsonString;
    }

    private void setAuthType(String type) {
        Log.d(TAG, "setAuthType(): type = " + type);
        authType = type;
    }

    private void setUseIdToken(boolean useIdToken) {
        Log.d(TAG, "setUseIdToken(): useIdToken = " + useIdToken);
        this.useIdToken = useIdToken;
    }
}
