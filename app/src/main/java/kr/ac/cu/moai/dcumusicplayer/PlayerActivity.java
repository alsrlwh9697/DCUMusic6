package kr.ac.cu.moai.dcumusicplayer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.media.MediaPlayer;
import android.widget.Toast;
import android.os.Bundle;
import android.os.Handler;
import android.widget.SeekBar;

import java.io.IOException;
import java.util.Objects;

public class PlayerActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;
    String mp3Path;
    SeekBar seekBar;
    Handler handler;
    Runnable updateSeekBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);


        seekBar = findViewById(R.id.seekBar);
        handler = new Handler();

        mp3Path = getIntent().getStringExtra("mp3");
        Log.i("DCU_MP", "Playing: " + mp3Path);

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(mp3Path);
            mediaPlayer.prepare();
            mediaPlayer.start();

            Toast.makeText(this, "Playing: " + mp3Path, Toast.LENGTH_SHORT).show();

            // SeekBar 초기화
            seekBar.setMax(mediaPlayer.getDuration());
            updateSeekBar = new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        handler.postDelayed(this, 1000);
                    }
                }
            };
            handler.postDelayed(updateSeekBar, 0);

            // SeekBar 변경 리스너
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        mediaPlayer.seekTo(progress);
                    }
                }
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // 사용자가 SeekBar 조작을 시작할 때 호출
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // 사용자가 SeekBar 조작을 멈출 때 호출
                }
            });
        } catch (Exception e) {
            Log.e("DCU_MP", "Error playing MP3", e);
            Toast.makeText(this, "Error playing MP3", Toast.LENGTH_SHORT).show();
        }



        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String mp3file = intent.getStringExtra("mp3");
        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            ImageView ivCover = findViewById(R.id.ivCover);
            retriever.setDataSource(mp3file);
            byte[] b = retriever.getEmbeddedPicture();
            Bitmap cover = BitmapFactory.decodeByteArray(b, 0, b.length);
            ivCover.setImageBitmap(cover);

            TextView tvTitle = findViewById(R.id.tvTitle);
            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            tvTitle.setText(title);

            TextView tvDuration = findViewById(R.id.tvDuration);
            tvDuration.setText(ListViewMP3Adapter.getDuration(retriever));

            TextView tvArtist = findViewById(R.id.tvArtist);
            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            tvArtist.setText(artist);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (handler != null && updateSeekBar != null) {
            handler.removeCallbacks(updateSeekBar);
        }
    }

}