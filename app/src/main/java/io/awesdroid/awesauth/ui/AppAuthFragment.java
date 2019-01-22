package io.awesdroid.awesauth.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.awesdroid.awesauth.R;
import io.awesdroid.awesauth.model.AppAuthState;
import io.awesdroid.awesauth.utils.Utils;
import io.awesdroid.awesauth.viewmodel.AppAuthViewModel;
import io.awesdroid.awesauth.viewmodel.SettingsViewModel;

import static io.awesdroid.awesauth.ui.MainActivity.RC_AUTH;


public class AppAuthFragment extends Fragment {
    private static final String TAG = AppAuthFragment.class.getSimpleName();

    private AppAuthViewModel appAuthViewModel;
    private boolean initViewModel = false;
    private String authType;
    private boolean usePendingIntent = false;
    private Unbinder unbinder;

    @BindView(R.id.button_signin)
    Button signInButton;
    @BindView(R.id.button_signout)
    Button signOutButton;
    @BindView(R.id.button_refresh_token)
    Button refreshTokenButton;
    @BindView(R.id.button_get_info)
    Button getInfoButton;

    @BindView(R.id.auth_status)
    TextView authStatusTextView;

    @BindView(R.id.token_info_container)
    ViewGroup tokenInfoContainer;
    @BindView(R.id.token_info_refresh)
    TextView tokenInfoRefreshTextView;
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

    private Dialog progressDialog;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        View rootView = inflater.inflate(R.layout.fragment_appauth, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        View rv = inflater.inflate(R.layout.progress_bar, container, false);
        progressDialog = new Dialog(getActivity());
        progressDialog.setContentView(rv);
        progressDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT));

        signOutButton.setVisibility(View.GONE);
        refreshTokenButton.setVisibility(View.GONE);
        getInfoButton.setVisibility(View.GONE);

        signInButton.setOnClickListener(v -> signIn(usePendingIntent));
        signOutButton.setOnClickListener(v -> signOut());
        getInfoButton.setOnClickListener(v -> fetchUserInfo());
        refreshTokenButton.setOnClickListener(v -> refreshToken());

        tokenInfoContainer.setVisibility(View.GONE);
        userInfoContainer.setVisibility(View.GONE);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SettingsViewModel settingsViewModel =
                ViewModelProviders.of(requireActivity()).get(SettingsViewModel.class);
        settingsViewModel.getAuthType().observe(this, this::handleAuthType);
        settingsViewModel.isAppAuthUsePendingIntent().observe(this,
                this::handleUsePendingIntent);

        appAuthViewModel = ViewModelProviders.of(requireActivity()).get(AppAuthViewModel.class);
        appAuthViewModel.getAuthState().observe(this, this::handleAuthState);
        appAuthViewModel.getUserInfo().observe(this, this::handleUserInfo);
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


    private void handleAuthType(String type) {
        Log.d(TAG, "handleAuthType(): type = " + type);
        authType = type;
        if (type.equals("0") && !initViewModel) {
            appAuthViewModel.init(getActivity(), getActivity());
            initViewModel = true;
        }
    }

    private void handleUsePendingIntent(boolean usePendingIntent) {
        Log.d(TAG, "handleUsePendingIntent(): usePendingIntent = " + usePendingIntent);
        this.usePendingIntent = usePendingIntent;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void handleAuthState(AppAuthState state) {
        Log.d(TAG, "handleAuthState(): state = " + state);
        if (state == null)
            return;

        if (state.hasLastTokenResponse()) {
            progressDialog.dismiss();

            // update UI
            authStatusTextView.setText(getString(R.string.auth_granted));
            authStatusTextView.setTextColor(Color.WHITE);

            signInButton.setVisibility(View.GONE);
            signOutButton.setVisibility(View.VISIBLE);
            refreshTokenButton.setVisibility(View.VISIBLE);
            if (state.getRefreshToken() != null) {
                refreshTokenButton.setEnabled(true);
            } else {
                refreshTokenButton.setEnabled(false);
            }
            getInfoButton.setVisibility(View.VISIBLE);

            showTokenInfo(state);
        } else if (state.getAuthorizationCode() != null) {
            // Handle Authorization response
            authStatusTextView.setText(getString(R.string.fetching_token));
            authStatusTextView.setTextColor(Color.WHITE);
            progressDialog.show();
        } else {
            // handle exception
            Exception exception =  state.getAuthorizationException();
            if (exception != null) {
                new AlertDialog.Builder(requireActivity())
                        .setMessage(exception.getMessage())
                        .setPositiveButton(R.string.ok, (dialog, which) ->
                                signOut())
                        .setNegativeButton(R.string.cancel, null)
                        .setCancelable(true)
                        .show();
            }

        }
    }

    private void signIn(boolean usePendingIntent) {
        switch (authType) {
            case "-1":
                new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                        .setMessage("Please select Auth type firstly in Settings")
                        .setPositiveButton(R.string.ok, (dialog, which) ->
                                ((MainActivity) getActivity()).navigateToSettings())
                        .setNegativeButton(R.string.cancel, null)
                        .setCancelable(true)
                        .show();
                break;
            case "0":
                progressDialog.show();
                appAuthViewModel.signIn(usePendingIntent, RC_AUTH);
                break;
            default:
                throw new IllegalStateException("authType is not 0");
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private void showTokenInfo(AppAuthState state) {
        tokenInfoContainer.setVisibility(View.VISIBLE);

        tokenInfoRefreshTextView.setText(
                (state.getRefreshToken() == null? "n/a" : state.getRefreshToken()));
        tokenInfoIdTextView.setText((state.getIdToken() == null? "n/a": state.getIdToken()));
        if (state.getAccessToken() == null) {
            tokenInfoExpireTextView.setText("n/a");
        } else {
            Long expire = state.getAccessTokenExpirationTime();
            if (expire == null) {
                tokenInfoExpireTextView.setText("no expired time");
            } else if (expire < System.currentTimeMillis()) {
                tokenInfoExpireTextView.setText("already expired");
            } else {
                DateTimeFormatter ftf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss ZZ");
                String expireString = ftf.format(
                        ZonedDateTime.ofInstant(
                                Instant.ofEpochMilli(expire),ZoneId.systemDefault()));
                tokenInfoExpireTextView.setText("expired at " + expireString);
            }
        }
    }

    private void handleUserInfo(JSONObject userInfo) {
        Log.d(TAG, "handleUserInfo(): userInfo = " + userInfo);
        if (appAuthViewModel.getAuthState().getValue() == null)
            return;

        progressDialog.dismiss();
        if (userInfo == null) {
            new AlertDialog.Builder(requireActivity())
                    .setMessage("No user info available!")
                    .setPositiveButton(R.string.ok, null)
                    .setCancelable(true)
                    .show();
            return;
        }

        userInfoContainer.setVisibility(View.VISIBLE);
        try {
            if (userInfo.has("name")) {
                userInfoNameTextView.setText(userInfo.getString("name"));
            }
            if (userInfo.has("picture")) {
                String url = userInfo.getString("picture");
                Glide.with(requireActivity())
                        .load(url)
                        .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                        .into(userInfoAvatarImageView);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.userInfoTextView.setText(Utils.prettyJson(userInfo.toString()));

    }

    private void signOut() {
        authStatusTextView.setText(getString(R.string.auth_not_granted));
        authStatusTextView.setTextColor(Color.DKGRAY);

        signInButton.setVisibility(View.VISIBLE);
        signOutButton.setVisibility(View.GONE);
        refreshTokenButton.setVisibility(View.GONE);
        getInfoButton.setVisibility(View.GONE);

        tokenInfoContainer.setVisibility(View.GONE);

        userInfoContainer.setVisibility(View.GONE);
        userInfoTextView.setText("");
        userInfoNameTextView.setText("");

        appAuthViewModel.signOut();
    }

    private void fetchUserInfo() {
        progressDialog.show();
        appAuthViewModel.fetchUserInfo();
    }

    private void refreshToken() {
        progressDialog.show();
        appAuthViewModel.refreshToken();
    }

}
