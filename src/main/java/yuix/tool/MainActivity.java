package yuix.tool;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private CardView cardTerminal, cardAiChat, cardFileManager, cardSettings;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupAnimations();
        setupClickListeners();
    }

    private void initViews() {
        cardTerminal = findViewById(R.id.cardTerminal);
        cardAiChat = findViewById(R.id.cardAiChat);
        cardFileManager = findViewById(R.id.cardFileManager);
        cardSettings = findViewById(R.id.cardSettings);
        bottomNav = findViewById(R.id.bottomNav);
    }

    private void setupAnimations() {
        // iOS-like spring animations
        cardTerminal.setTranslationY(100);
        cardTerminal.setAlpha(0);
        cardTerminal.animate()
            .translationY(0)
            .alpha(1)
            .setDuration(600)
            .setInterpolator(new OvershootInterpolator(1.2f))
            .setStartDelay(100)
            .start();

        cardAiChat.setTranslationY(100);
        cardAiChat.setAlpha(0);
        cardAiChat.animate()
            .translationY(0)
            .alpha(1)
            .setDuration(600)
            .setInterpolator(new OvershootInterpolator(1.2f))
            .setStartDelay(200)
            .start();

        cardFileManager.setTranslationY(100);
        cardFileManager.setAlpha(0);
        cardFileManager.animate()
            .translationY(0)
            .alpha(1)
            .setDuration(600)
            .setInterpolator(new OvershootInterpolator(1.2f))
            .setStartDelay(300)
            .start();

        cardSettings.setTranslationY(100);
        cardSettings.setAlpha(0);
        cardSettings.animate()
            .translationY(0)
            .alpha(1)
            .setDuration(600)
            .setInterpolator(new OvershootInterpolator(1.2f))
            .setStartDelay(400)
            .start();
    }

    private void setupClickListeners() {
        cardTerminal.setOnClickListener(v -> {
            animateCardClick(cardTerminal);
            startActivity(new Intent(this, TerminalActivity.class));
        });

        cardAiChat.setOnClickListener(v -> {
            animateCardClick(cardAiChat);
            startActivity(new Intent(this, AiChatActivity.class));
        });

        cardFileManager.setOnClickListener(v -> {
            animateCardClick(cardFileManager);
            // TODO: Open file manager
        });

        cardSettings.setOnClickListener(v -> {
            animateCardClick(cardSettings);
            startActivity(new Intent(this, SettingsActivity.class));
        });

        // Only bottom nav triggers page change animations
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_terminal) {
                startActivity(new Intent(this, TerminalActivity.class));
                return true;
            } else if (itemId == R.id.nav_ai) {
                startActivity(new Intent(this, AiChatActivity.class));
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }

    private void animateCardClick(CardView card) {
        card.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction(() -> {
                card.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start();
            })
            .start();
    }
}
