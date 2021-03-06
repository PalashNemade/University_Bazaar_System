package com.example.cse6324.university_bazaar_system;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class EntryScreenActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    User currentUser;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageRef;
    ArrayList<Map<String, Object>> items;
    Map<String, byte[]> images;
    AdView mAdview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_screen);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");

        mAdview = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdview.loadAd(adRequest);

        FragmentManager fragMan = getFragmentManager();
        Fragment myFrag = FeedFragment.newInstance("", "");
        fragMan.beginTransaction()
                .add(R.id.content_entry, myFrag)
                .commit();
        getSupportActionBar().setTitle(R.string.home);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        //Get current logged in user and display on navigation drawer
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        final TextView textView  = (TextView) headerView.findViewById(R.id.textView);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        DocumentReference docRef = db.collection("users").document(auth.getUid());
        items = new ArrayList<>();
        images = new HashMap<>();

        //Method to go to edit profile on clicking the header of the Navigation Drawer.
        headerView.findViewById(R.id.user_name_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(EntryScreenActivity.this, EditProfileActivity.class);
                EntryScreenActivity.this.startActivity(myIntent);
            }
        });

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                currentUser = documentSnapshot.toObject(User.class);
                textView.setText(currentUser.getName());
            }
        });

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                db.collection("items")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        items.add(document.getData());
                                    }
                                }

                                AsyncTask.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (final Map<String, Object> map : items) {
                                            StorageReference islandRef = storageRef.child("images/" + map.get("imgPath").toString());

                                            final long KB200 = 1024 * 400;
                                            islandRef.getBytes(KB200).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                @Override
                                                public void onSuccess(byte[] bytes) {
                                                    images.put(map.get("imgPath").toString(), bytes);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception) {
                                                    // Handle any errors
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        });
            }
        });

    }

    public void setActionBarTitle(int title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.entry_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            FragmentManager fragMan = getFragmentManager();
            Fragment myFrag = FeedFragment.newInstance("", "");
            FragmentTransaction transaction = fragMan.beginTransaction();
            transaction.replace(R.id.content_entry, myFrag);
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (id == R.id.nav_clubs_organizations) {
            /*FragmentManager fragMan = getFragmentManager();
            Fragment myFrag = ClubFragment.newInstance("", "");
            FragmentTransaction transaction = fragMan.beginTransaction();
            transaction.replace(R.id.content_entry, myFrag);
            transaction.addToBackStack(null);
            transaction.commit();*/
            try{
                Intent myIntent = new Intent(EntryScreenActivity.this, ClubListActivity.class);
                EntryScreenActivity.this.startActivity(myIntent);
            }catch (Exception ex){
                Toast.makeText(EntryScreenActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }

        } else if (id == R.id.nav_messages) {
            Intent myIntent = new Intent(EntryScreenActivity.this, MessagingActivity.class);
            EntryScreenActivity.this.startActivity(myIntent);
        } else if (id == R.id.sellLend) {
            FragmentManager fragMan = getFragmentManager();
            Fragment myFrag = SellLendFragment.newInstance("", "");
            FragmentTransaction transaction = fragMan.beginTransaction();
            transaction.replace(R.id.content_entry, myFrag);
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (id == R.id.exchangeMerchandise) {
            FragmentManager fragMan = getFragmentManager();
            Fragment myFrag = ExchangeFragment.newInstance("", "");
            FragmentTransaction transaction = fragMan.beginTransaction();
            transaction.replace(R.id.content_entry, myFrag);
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (id == R.id.browseMerchandise) {
            FragmentManager fragMan = getFragmentManager();
            Fragment myFrag = BuyFragment.newInstance("", "");
            FragmentTransaction transaction = fragMan.beginTransaction();
            transaction.replace(R.id.content_entry, myFrag);
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (id == R.id.nav_logout) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signOut();
            // this listener will be called when there is change in firebase user session

            FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user == null) {
                        // user auth state is changed - user is null
                        // launch login activity
                        startActivity(new Intent(EntryScreenActivity.this, LoginActivity.class));
                        finish();
                    }
                }
            };
            auth.addAuthStateListener(authListener);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
