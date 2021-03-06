package com.prominentdev.blog.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.prominentdev.blog.R;
import com.prominentdev.blog.fragments.FragmentADHome;
import com.prominentdev.blog.helpers.PDUtils;
import com.prominentdev.blog.provider.PersistData;

import static com.prominentdev.blog.models.ConstantData.EMAIL;
import static com.prominentdev.blog.models.ConstantData.FULL_NAME;
import static com.prominentdev.blog.models.ConstantData.PROFILE_PICTURE;

/**
 * Created by Narender Kumar on 11/12/2018.
 * For Prominent, Faridabad (India)
 * narender.kumar.nishad@gmail.com
 */
public class ActivityDashboard extends ActivityBase {

    Toolbar toolbar;
    FloatingActionButton ad_fab;
    boolean doubleBackToExitPressedOnce = false;
    private DrawerLayout mDrawerLayout;

    private ImageView profilePicture;
    private TextView fullName,email;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.dashboard);
        ad_fab = findViewById(R.id.ad_fab);
        ad_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, ActivityPost.class));
            }
        });

        mDrawerLayout = findViewById(R.id.drawer_layout);




        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        profilePicture=(ImageView)headerView.findViewById(R.id.profile_header);
        fullName=(TextView) headerView.findViewById(R.id.full_name);
        email=(TextView) headerView.findViewById(R.id.email_address);

        if(!TextUtils.isEmpty(PersistData.getStringData(context,FULL_NAME)))
        {
            fullName.setText(PersistData.getStringData(context,FULL_NAME));
        }

        if(!TextUtils.isEmpty(PersistData.getStringData(context,EMAIL)))
        {
            email.setText(PersistData.getStringData(context,EMAIL));
        }



        if(!TextUtils.isEmpty(PersistData.getStringData(context,PROFILE_PICTURE)))
        {
            Glide
                    .with(context)
                    .load(PersistData.getStringData(context,PROFILE_PICTURE))
                    .apply(RequestOptions.circleCropTransform())
                    .into(profilePicture);
        }



        Menu nav_Menu = navigationView.getMenu();

        if (sessionManager.isLoggedIn()) {
            nav_Menu.findItem(R.id.loginButton).setVisible(false);
            nav_Menu.findItem(R.id.logoutButton).setVisible(true);
        }else {
            nav_Menu.findItem(R.id.loginButton).setVisible(true);
            nav_Menu.findItem(R.id.logoutButton).setVisible(false);
        }

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        switch (menuItem.getItemId()) {
                            case R.id.loginButton:

                                startActivity(new Intent(context, ActivityAuthentication.class));
                                finish();

                                break;

                            case R.id.logoutButton:

                                if(mGoogleSignInClient!=null)
                                {
                                    signOut();

                                }



                                break;
                        }

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });


        if (findViewById(R.id.ad_fl_root) != null) {
            if (savedInstanceState != null) {
                return;
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.ad_fl_root, FragmentADHome.newInstance(), FragmentADHome.class.getSimpleName())
                    .commit();
        }

        googleSignInInit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //Checking for fragment count on backstack
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else if (!doubleBackToExitPressedOnce) {
            doubleBackToExitPressedOnce = true;
            PDUtils.showToast(context, getString(R.string.back_pressed));

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 1000);
        } else {
            super.onBackPressed();
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            ad_fab.hide();
        } else {
            ad_fab.show();
        }


    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        sessionManager.logoutUser();
                        startActivity(new Intent(context, ActivityAuthentication.class));
                        finish();
                    }
                });
    }

    private void googleSignInInit() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);

//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
//        if (account != null) {
//            updateUI(account, true);
//        }
    }
}