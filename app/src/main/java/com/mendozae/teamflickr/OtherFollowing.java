package com.mendozae.teamflickr;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class OtherFollowing extends AppCompatActivity {


    Toolbar toolbar;
    Intent intent;
    String userShown;
    private FirebaseFirestore mStore;
    private DocumentReference userShownRef;
    ListView listView;
    SwipeRefreshLayout swipeRefresh;
    ArrayList<String> userShownFollowing;
    ArrayList<String> following;
    CustomAdapter adapter;
    ArrayList<String> followingOrNot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_following);
        toolbar = (Toolbar) findViewById(R.id.followingtoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setBackgroundColor(Color.parseColor("#333333"));

        intent = getIntent();
        userShown = intent.getStringExtra("Name");

        mStore = FirebaseFirestore.getInstance();
        userShownRef = mStore.collection("Users").document(userShown);
        followingOrNot = new ArrayList<>();


    }

    public void onStart(){
        super.onStart();
        userShownRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    final DocumentSnapshot snapshot = task.getResult();
                    if(snapshot.exists()){

                        UserProfile.userReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot snapshot2 = task.getResult();
                                    if(snapshot2.exists()) {
                                        userShownFollowing = (ArrayList<String>) snapshot.get("Following");
                                        following = (ArrayList<String>) snapshot2.get("Following");
                                        for(int i = 0; i < userShownFollowing.size(); i++){
                                            if(following.contains(userShownFollowing.get(i))){
                                                followingOrNot.add("Followed");
                                            }else{
                                                followingOrNot.add("+  Follow");
                                            }
                                        }

                                        listView = (ListView) findViewById(R.id.followinglistview);
                                        adapter = new CustomAdapter();
                                        listView.setAdapter(adapter);

                                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                                final String name = userShownFollowing.get(i);
                                                Intent intent;
                                                if (name.equals(UserProfile.user)) {
                                                    intent = new Intent(OtherFollowing.this, UserInterface.class);
                                                   intent.putExtra("Tab", 2);
                                                } else {
                                                    intent = new Intent(OtherFollowing.this, OtherUsersProfile.class);
                                                    intent.putExtra("Name", name);
                                                }
                                                startActivity(intent);
                                            }
                                        });

                                        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
                                        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                            @Override
                                            public void onRefresh() {
                                                Log.i("Info", "Refreshing");
                                                updateFollowing();
                                            }
                                        });
                                    }
                                }
                            }
                        });


                    }
                }
            }
        });
    }

    private void updateFollowing(){
        userShownRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    final DocumentSnapshot snapshot = task.getResult();
                    if(snapshot.exists()){

                        UserProfile.userReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentSnapshot snapshot2 = task.getResult();
                                    if(snapshot2.exists()){
                                        userShownFollowing = (ArrayList<String>) snapshot.get("Following");
                                        following = (ArrayList<String>) snapshot2.get("Following");
                                        followingOrNot = new ArrayList<>();
                                        for(int i = 0; i < userShownFollowing.size(); i++){
                                            if(following.contains(userShownFollowing.get(i))){
                                                followingOrNot.add("Followed");
                                            }else{
                                                followingOrNot.add("+  Follow");
                                            }
                                        }
                                        adapter.notifyDataSetChanged();
                                        swipeRefresh.setRefreshing(false);
                                    }
                                }
                            }
                        });

                    }
                }
            }
        });
    }

    private void follow(final String name){
        final DocumentReference clickedNameRef = mStore.collection("Users").document(name);
        UserProfile.userReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot snapshot = task.getResult();
                    if(snapshot.exists()){
                        ArrayList<String> following = (ArrayList<String>) snapshot.get("Following");
                        ArrayList<String> followingOrNot = (ArrayList<String>) snapshot.get("FollowingOrNotFollowing");
                        ArrayList<String> followers = (ArrayList<String>) snapshot.get("Followers");
                        ArrayList<String> followingOrNot2 = (ArrayList<String>) snapshot.get("FollowingOrNotFollowers");

                        following.add(name);
                        followingOrNot.add("Followed");
                        if(followers.contains(name)){
                            followingOrNot2.set(followers.indexOf(name), "Followed");
                            UserProfile.userReference.update("FollowingOrNotFollowers", followingOrNot2);
                        }

                        UserProfile.userReference.update("Following", following);
                        UserProfile.userReference.update("FollowingOrNotFollowing", followingOrNot);
                    }
                }
            }
        });

        clickedNameRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot snapshot = task.getResult();
                    if(snapshot.exists()){
                        ArrayList<String> followers = (ArrayList<String>) snapshot.get("Followers");
                        ArrayList<String> followingOrNot = (ArrayList<String>) snapshot.get("FollowingOrNotFollowers");
                        ArrayList<String> following = (ArrayList<String>) snapshot.get("Following");

                        if(following.contains(UserProfile.user)){
                            followingOrNot.add("Followed");
                        }else{
                            followingOrNot.add("+  Follow");
                        }


                        followers.add(UserProfile.user);

                        clickedNameRef.update("Followers", followers);
                        clickedNameRef.update("FollowingOrNotFollowers", followingOrNot);
                    }
                }
            }
        });
    }

    private void unfollow(final String name) {
        final DocumentReference clickedNameRef = mStore.collection("Users").document(name);
        UserProfile.userReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        ArrayList<String> following = (ArrayList<String>) snapshot.get("Following");
                        ArrayList<String> followingOrNot = (ArrayList<String>) snapshot.get("FollowingOrNotFollowing");
                        ArrayList<String> followers = (ArrayList<String>) snapshot.get("Followers");
                        ArrayList<String> followingOrNot2 = (ArrayList<String>) snapshot.get("FollowingOrNotFollowers");

                        followingOrNot.remove(following.indexOf(name));
                        following.remove(name);
                        if (followers.contains(name)) {
                            followingOrNot2.set(followers.indexOf(name), "+  Follow");
                            UserProfile.userReference.update("FollowingOrNotFollowers", followingOrNot2);
                        }

                        UserProfile.userReference.update("Following", following);
                        UserProfile.userReference.update("FollowingOrNotFollowing", followingOrNot);


                    }
                }
            }
        });

        clickedNameRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        ArrayList<String> followers = (ArrayList<String>) snapshot.get("Followers");
                        ArrayList<String> followingOrNot = (ArrayList<String>) snapshot.get("FollowingOrNotFollowers");

                        followingOrNot.remove(followers.indexOf(UserProfile.user));
                        followers.remove(UserProfile.user);

                        clickedNameRef.update("Followers", followers);
                        clickedNameRef.update("FollowingOrNotFollowers", followingOrNot);
                    }
                }

            }
        });
    }



    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    private class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return userShownFollowing.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            final int index = i;
            final ViewHolder viewHolder;
            if(view == null) {
                view = getLayoutInflater().inflate(R.layout.layout_custom, null);
                viewHolder = new ViewHolder();
                viewHolder.followButton = (Button) view.findViewById(R.id.followbutton);
                view.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) view.getTag();
            }
            ImageView imageView = (ImageView) view.findViewById(R.id.pfp);
            TextView textView_name = (TextView) view.findViewById(R.id.name);
            if(userShownFollowing.get(index).equals(UserProfile.user)){
                viewHolder.followButton.setVisibility(View.GONE);
            }else {
                viewHolder.followButton.setText(followingOrNot.get(index));
            }
            textView_name.setText(userShownFollowing.get(index));

            viewHolder.followButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(viewHolder.followButton.getText().toString().equals("Followed")){
                        viewHolder.followButton.setText("+  Follow");
                        followingOrNot.set(index, "+  Follow");
                        unfollow(userShownFollowing.get(index));
                    }else{
                        viewHolder.followButton.setText("Followed");
                        followingOrNot.set(index, "Followed");
                        follow(userShownFollowing.get(index));
                    }
                }
            });


            //imageView.setImageResource(IMAGES[i]);

            // textView_desc.setText(DESCRIPTIONS[i]);
            return view;
        }

        private class ViewHolder{
            public Button followButton;
        }
    }

}
