package com.example.myapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jean.jcplayer.model.JcAudio;
import com.example.jean.jcplayer.view.JcPlayerView;
import com.example.myapplication.Adapter.JcSongsAdapter;
import com.example.myapplication.R;
import com.example.myapplication.model.Song;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CategoryActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ProgressBar progressBar;
    Boolean checkin = false;
    List<Song> mupload;
    JcSongsAdapter adapter;
    JcPlayerView jcPlayerView;
    ArrayList<JcAudio> jcAudios = new ArrayList<>();
    ImageView btn_back;
    ImageView search;
    EditText timkiem;
    String category;
    private int currentIndex;
    Query query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs_activity);
        recyclerView = findViewById(R.id.recycleview_id);
        progressBar = findViewById(R.id.processbarshowsong);
        String user = getIntent().getStringExtra("user2");
        jcPlayerView = findViewById(R.id.jcplayer);
        btn_back = findViewById(R.id.btn_back2);
        search=findViewById(R.id.btn_search_categorysong);
        timkiem=findViewById(R.id.edt_categorysong);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CategoryActivity.this, ClientHome.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mupload = new ArrayList<>();
        recyclerView.setAdapter(adapter);
        adapter = new JcSongsAdapter(getApplicationContext(), mupload, user, new JcSongsAdapter.RecyclerItemClickListener() {
            @Override
            public void onClickListener(Song uploadSong, int position) {
                changeSelectedSong(position);
                jcPlayerView.playAudio(jcAudios.get(position));
                jcPlayerView.setVisibility(View.VISIBLE);
                jcPlayerView.createNotification();
            }

        });
        category = getIntent().getExtras().getString("SongsCategory");
        query = FirebaseDatabase.getInstance().getReference("songs").orderByChild("songscategory").equalTo(category);
        query.addListenerForSingleValueEvent(valueEventListener);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!timkiem.getText().toString().equals("")) {
                    Query query2 = FirebaseDatabase.getInstance().getReference("songs")
                            .orderByChild("songtitle")
                            .startAt(timkiem.getText().toString())
                            .endAt(timkiem.getText().toString()+"\uf8ff");


                    query2.addListenerForSingleValueEvent(valueEventListener);
                } else {
                      query.addListenerForSingleValueEvent(valueEventListener);
                }
            }
        });
    }

    public void changeSelectedSong(int index) {
        adapter.notifyItemChanged(adapter.getSelectPosition());
        currentIndex = index;
        adapter.setSelectPosition(currentIndex);
        adapter.notifyItemChanged(currentIndex);
    }

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            mupload.clear();
            for (DataSnapshot dss : dataSnapshot.getChildren()) {
                Song uploadSong = dss.getValue(Song.class);
                uploadSong.setmKey(dss.getKey());
                currentIndex = 0;

                if (category.equals(uploadSong.getSongscategory())) {
                    mupload.add(uploadSong);
                    checkin = true;
                    jcAudios.add(JcAudio.createFromURL(uploadSong.getSongtitle(), uploadSong.getSongLink()));

                }
            }
            adapter.setSelectPosition(0);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
            if (checkin) {
                jcPlayerView.initPlaylist(jcAudios, null);

            } else {
                Toast.makeText(CategoryActivity.this, "there is songs!", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            progressBar.setVisibility(View.GONE);
        }

    };
}


