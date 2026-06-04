package com.carlauncher;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // UI
    private TextView tvClock, tvDate, tvSpeed, tvBattery, tvFuel;
    private TextView tvNavDest, tvNavEta, tvNavOpen;
    private TextView tvTrackTitle, tvTrackArtist;
    private TextView tvBottomSpeed, tvBottomBattery;
    private ImageButton btnPlayPause, btnPrev, btnNext;
    private RecyclerView rvApps;

    // State
    private Handler handler = new Handler(Looper.getMainLooper());
    private AppGridAdapter appAdapter;
    private BroadcastReceiver batteryReceiver;
    private MediaController mediaController;
    private boolean isPlaying = false;

    // Runnables
    private Runnable clockRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep screen on, fullscreen, landscape
        getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_main);

        bindViews();
        setupClock();
        setupAppGrid();
        setupQuickActions();
        setupMediaControls();
        setupBatteryReceiver();
        setupNavButton();
    }

    private void bindViews() {
        tvClock        = findViewById(R.id.tvClock);
        tvDate         = findViewById(R.id.tvDate);
        tvSpeed        = findViewById(R.id.tvSpeed);
        tvBattery      = findViewById(R.id.tvBattery);
        tvFuel         = findViewById(R.id.tvFuel);
        tvNavDest      = findViewById(R.id.tvNavDest);
        tvNavEta       = findViewById(R.id.tvNavEta);
        tvNavOpen      = findViewById(R.id.tvNavOpen);
        tvTrackTitle   = findViewById(R.id.tvTrackTitle);
        tvTrackArtist  = findViewById(R.id.tvTrackArtist);
        tvBottomSpeed  = findViewById(R.id.tvBottomSpeed);
        tvBottomBattery= findViewById(R.id.tvBottomBattery);
        btnPlayPause   = findViewById(R.id.btnPlayPause);
        btnPrev        = findViewById(R.id.btnPrev);
        btnNext        = findViewById(R.id.btnNext);
        rvApps         = findViewById(R.id.rvApps);

        tvFuel.setText("Tam");
    }

    // ── CLOCK ──────────────────────────────────────────────
    private void setupClock() {
        clockRunnable = new Runnable() {
            @Override
            public void run() {
                Date now = new Date();
                tvClock.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(now));
                tvDate.setText(new SimpleDateFormat("EEE dd MMM", new Locale("tr")).format(now));
                handler.postDelayed(this, 15000);
            }
        };
        handler.post(clockRunnable);
    }

    // ── APP GRID ───────────────────────────────────────────
    private void setupAppGrid() {
        List<AppItem> apps = getInstalledApps();
        appAdapter = new AppGridAdapter(this, apps, pkg -> launchApp(pkg));
        rvApps.setLayoutManager(new GridLayoutManager(this, 5));
        rvApps.setAdapter(appAdapter);
    }

    private List<AppItem> getInstalledApps() {
        List<AppItem> list = new ArrayList<>();
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo ri : infos) {
            String pkg = ri.activityInfo.packageName;
            // Skip self
            if (pkg.equals(getPackageName())) continue;
            String name = ri.loadLabel(pm).toString();
            list.add(new AppItem(name, pkg, ri.loadIcon(pm)));
        }
        // Sort alphabetically
        list.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
        return list;
    }

    private void launchApp(String packageName) {
        try {
            Intent i = getPackageManager().getLaunchIntentForPackage(packageName);
            if (i != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            } else {
                Toast.makeText(this, "Uygulama açılamadı", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ── QUICK ACTION BUTTONS ───────────────────────────────
    private void setupQuickActions() {
        // Phone
        View btnPhone = findViewById(R.id.btnPhone);
        if (btnPhone != null) btnPhone.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_DIAL);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        });

        // Maps - opens Google Maps
        View btnMaps = findViewById(R.id.btnMaps);
        if (btnMaps != null) btnMaps.setOnClickListener(v -> openMaps());

        // Music
        View btnMusic = findViewById(R.id.btnMusic);
        if (btnMusic != null) btnMusic.setOnClickListener(v -> {
            // Try Spotify first, then YouTube Music, then generic
            String[] musicApps = {
                "com.spotify.music",
                "com.google.android.apps.youtube.music",
                "com.apple.android.music",
                "com.amazon.mp3"
            };
            for (String pkg : musicApps) {
                Intent i = getPackageManager().getLaunchIntentForPackage(pkg);
                if (i != null) { startActivity(i); return; }
            }
            // Fallback to any music player
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_APP_MUSIC);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try { startActivity(i); } catch (Exception ignored) {
                Toast.makeText(this, "Müzik uygulaması bulunamadı", Toast.LENGTH_SHORT).show();
            }
        });

        // Settings
        View btnSettings = findViewById(R.id.btnSettings);
        if (btnSettings != null) btnSettings.setOnClickListener(v -> {
            Intent i = new Intent(android.provider.Settings.ACTION_SETTINGS);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        });
    }

    private void openMaps() {
        try {
            Intent i = getPackageManager().getLaunchIntentForPackage("com.google.android.apps.maps");
            if (i == null) i = getPackageManager().getLaunchIntentForPackage("com.waze");
            if (i != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            } else {
                // Fallback: open maps in browser
                Intent web = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://maps.google.com"));
                web.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(web);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Harita açılamadı", Toast.LENGTH_SHORT).show();
        }
    }

    // ── NAV BUTTON ─────────────────────────────────────────
    private void setupNavButton() {
        if (tvNavOpen != null) {
            tvNavOpen.setOnClickListener(v -> openMaps());
        }
    }

    // ── MEDIA CONTROLS ─────────────────────────────────────
    private void setupMediaControls() {
        btnPlayPause.setOnClickListener(v -> {
            AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
            // Simulate media key event
            long now = android.os.SystemClock.uptimeMillis();
            android.view.KeyEvent down = new android.view.KeyEvent(
                now, now, android.view.KeyEvent.ACTION_DOWN,
                android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
            am.dispatchMediaKeyEvent(down);
            android.view.KeyEvent up = new android.view.KeyEvent(
                now, now, android.view.KeyEvent.ACTION_UP,
                android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, 0);
            am.dispatchMediaKeyEvent(up);
        });

        btnPrev.setOnClickListener(v -> {
            AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
            long now = android.os.SystemClock.uptimeMillis();
            am.dispatchMediaKeyEvent(new android.view.KeyEvent(now, now,
                android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS, 0));
            am.dispatchMediaKeyEvent(new android.view.KeyEvent(now, now,
                android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS, 0));
        });

        btnNext.setOnClickListener(v -> {
            AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
            long now = android.os.SystemClock.uptimeMillis();
            am.dispatchMediaKeyEvent(new android.view.KeyEvent(now, now,
                android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_MEDIA_NEXT, 0));
            am.dispatchMediaKeyEvent(new android.view.KeyEvent(now, now,
                android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_MEDIA_NEXT, 0));
        });

        // Poll media info every 2 seconds
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateMediaInfo();
                handler.postDelayed(this, 2000);
            }
        }, 2000);
    }

    private void updateMediaInfo() {
        try {
            MediaSessionManager msm = (MediaSessionManager)
                getSystemService(Context.MEDIA_SESSION_SERVICE);
            // Requires MEDIA_CONTENT_CONTROL or Notification Listener permission
            // This is a best-effort approach
        } catch (Exception ignored) {}
    }

    // ── BATTERY ────────────────────────────────────────────
    private void setupBatteryReceiver() {
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                if (level >= 0 && scale > 0) {
                    int pct = (int)(100f * level / scale);
                    String txt = pct + "%";
                    tvBattery.setText(txt);
                    tvBottomBattery.setText("🔋 " + txt);
                    // Color code
                    int color;
                    if (pct > 50)      color = getResources().getColor(R.color.accent_green, null);
                    else if (pct > 20) color = getResources().getColor(R.color.accent_orange, null);
                    else               color = 0xFFFF3333;
                    tvBattery.setTextColor(color);
                }
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
    }

    // ── LIFECYCLE ──────────────────────────────────────────
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh app list when returning
        if (appAdapter != null) {
            appAdapter.updateApps(getInstalledApps());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        try { unregisterReceiver(batteryReceiver); } catch (Exception ignored) {}
    }

    @Override
    public void onBackPressed() {
        // Prevent back from exiting launcher
        // Do nothing or show a menu
    }
}
